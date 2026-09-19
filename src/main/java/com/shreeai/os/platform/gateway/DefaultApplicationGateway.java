package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.graph.ExecutionGraphBuilder;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.resolver.CapabilityPlan;
import com.shreeai.os.platform.resolver.CapabilityResolver;
import com.shreeai.os.platform.resolver.DefaultCapabilityResolver;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of the Application Gateway.
 *
 * <p>Accepts SDK requests, validates structure, normalizes session
 * metadata, attaches request identity, generates a {@link GatewayRequest},
 * forwards to the Runtime, and returns the response.</p>
 *
 * <p>This gateway is deliberately thin: it does not execute capabilities,
 * run kernels, or perform reasoning/planning. It inserts the capability
 * planning layer — the {@link CapabilityResolver} decides WHICH capabilities
 * are required — and forwards the plan (currently ignored) to the Runtime
 * alongside the unchanged execution request.</p>
 */
public class DefaultApplicationGateway implements ApplicationGateway {

    private final Runtime runtime;
    private final String applicationId;
    private final boolean ready;
    private final CapabilityResolver capabilityResolver;
    private final ExecutionGraphBuilder executionGraphBuilder;

    public DefaultApplicationGateway(Runtime runtime) {
        this(runtime, null);
    }

    public DefaultApplicationGateway(Runtime runtime, String applicationId) {
        // Runtime may be null for gateway instances used in foundation/no-runtime
        // fallback mode; isReady() returns false in that case.
        this.runtime = runtime;
        this.applicationId = applicationId;
        this.ready = true;
        this.capabilityResolver = new DefaultCapabilityResolver();
        this.executionGraphBuilder = new ExecutionGraphBuilder();
    }

    @Override
    public SDKResponse handle(SDKRequest request) throws GatewayException {
        if (request == null) {
            throw new GatewayException("Gateway request must not be null");
        }
        GatewayContext context = validate(request);
        normalizeSession(context);
        attachIdentity(context);
        // Insert the capability planning layer: the resolver decides WHICH
        // capabilities are required. The plan rides along in request metadata;
        // the Runtime currently ignores it.
        CapabilityPlan plan = capabilityResolver.resolve(request);
        attachCapabilityPlan(context, plan);
        // Insert the execution graph layer: the builder converts the plan into
        // nodes/edges. The graph is attached to metadata only — the Runtime
        // currently ignores it and the graph is never executed here.
        ExecutionGraph graph = executionGraphBuilder.build(plan);
        attachExecutionGraph(context, graph);
        GatewayRequest gatewayRequest = buildGatewayRequest(context);
        return forwardToRuntime(gatewayRequest, context);
    }

    @Override
    public boolean isReady() {
        return ready && runtime != null;
    }

    private GatewayContext validate(SDKRequest request) throws GatewayException {
        GatewayContext context = GatewayContext.builder().sdkRequest(request).build();
        if (request.message() == null || request.message().isBlank()) {
            context.setValidated(false);
            throw new ValidationException("Gateway: message must not be null or blank");
        }
        context.setValidated(true);
        return context;
    }

    private void normalizeSession(GatewayContext context) throws GatewayException {
        String sessionId = context.sdkRequest().sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }
        context.setNormalizedSessionId(sessionId);
    }

    private void attachIdentity(GatewayContext context) throws GatewayException {
        context.addMetadata("gatewayRequestId", context.gatewayRequestId());
        context.addMetadata("gatewayApplicationId", applicationId != null ? applicationId : "default");
        context.addMetadata("gatewayTimestamp", System.currentTimeMillis());
    }

    /**
     * Attaches the resolved capability plan to the gateway context so it flows
     * into the runtime request metadata. The Runtime currently ignores it.
     */
    private void attachCapabilityPlan(GatewayContext context, CapabilityPlan plan) {
        context.addMetadata("capabilityPlan", plan);
        context.addMetadata("detectedIntent", plan.detectedIntent().name());
    }

    /**
     * Attaches the built execution graph to the gateway context so it flows
     * into the runtime request metadata. The graph is a planning artifact
     * only: it is never executed and the Runtime currently ignores it.
     */
    private void attachExecutionGraph(GatewayContext context, ExecutionGraph graph) {
        context.addMetadata("executionGraph", graph);
        context.addMetadata("executionGraphId", graph.graphId());
    }

    private GatewayRequest buildGatewayRequest(GatewayContext context) throws GatewayException {
        return GatewayRequest.builder()
                .sdkRequest(context.sdkRequest())
                .gatewayRequestId(context.gatewayRequestId())
                .normalizedMetadata(context.metadata())
                .build();
    }

    private SDKResponse forwardToRuntime(GatewayRequest gatewayRequest, GatewayContext context) throws GatewayException {
        if (!isReady()) {
            throw new GatewayException("Gateway is not ready to accept requests");
        }
        try {
            SDKRequest sdkRequest = gatewayRequest.sdkRequest();
            Map<String, Object> metadata = new HashMap<>(sdkRequest.metadata());
            for (Map.Entry<String, Object> entry : gatewayRequest.normalizedMetadata().entrySet()) {
                metadata.putIfAbsent(entry.getKey(), entry.getValue());
            }
            if (context.normalizedSessionId() != null) {
                metadata.put("sessionId", context.normalizedSessionId());
            }

            ExecutionRequest executionRequest = ExecutionRequest.builder()
                    .requestId(context.normalizedSessionId())
                    .requestType("CHAT")
                    .payload(sdkRequest.message())
                    .context(sdkRequest.context() != null ? sdkRequest.context() : "")
                    .metadata(metadata)
                    .build();

            ExecutionSession session = runtime.submit(executionRequest);
            context.setForwarded();

            ExecutionResult executionResult = session.result();
            if (executionResult == null) {
                throw new GatewayException("Runtime returned a session without an execution result");
            }

            // Extract structured payload for answer/confidence if available
            String answer = executionResult.output().orElse("");
            double confidence = 1.0;
            Map<String, Object> structuredPayload = executionResult.structuredPayload();

            if (structuredPayload != null
                    && structuredPayload.get("response") instanceof SynthesizedResponse response) {
                answer = response.answer();
                confidence = response.confidence();
            }

            return SDKResponse.builder()
                    .answer(answer)
                    .confidence(confidence)
                    .reasoningAvailable(true)
                    .metadata("sdk-version:gateway")
                    .structuredPayload(structuredPayload)
                    .build();

        } catch (Exception e) {
            throw new GatewayException("Gateway forward failed: " + e.getMessage(), e);
        }
    }
}
