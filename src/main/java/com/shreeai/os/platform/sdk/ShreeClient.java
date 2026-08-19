package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.exceptions.SDKException;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ShreeClient</b>
 *
 * <p>Core SDK client that wraps the Shree AI OS Runtime.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class ShreeClient {

    private final SDKConfiguration configuration;
    private final Runtime runtime;

    ShreeClient(SDKConfiguration configuration, Runtime runtime) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.runtime = runtime; // May be null for SDK foundation mode
    }

    /**
     * Sends a chat message to the platform.
     *
     * @param message the user message
     * @return SDKResponse with the answer
     * @throws SDKException if the request fails
     */
    public SDKResponse chat(String message) {
        SDKRequest request = SDKRequest.builder()
                .message(message)
                .build();
        return chat(request);
    }

    /**
     * Sends a chat request to the platform.
     *
     * @param request the SDK request
     * @return SDKResponse with the answer
     * @throws ValidationException if the request is invalid
     * @throws SDKException if the execution fails
     */
    public SDKResponse chat(SDKRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new ValidationException("message must not be null or blank");
        }

        try {
            // Build the structured IntelligenceContext from the SDK request,
            // preserving project profile, evidence, intent, objective, constraints,
            // session ID and user ID as first-class structured fields.
            IntelligenceContext intelligenceContext = IntelligenceContextBuilder.fromSdkRequest(request);

            // Create ExecutionRequest from SDKRequest, preserving context, metadata,
            // and session/request ID so downstream stages receive the full payload.
            // The structured intelligence context is carried in the metadata map
            // under the reserved key so it survives the runtime bridge.
            var requestMetadata = new java.util.HashMap<String, Object>(request.metadata());
            requestMetadata.put("intelligenceContext", intelligenceContext);

            ExecutionRequest executionRequest = ExecutionRequest.builder()
                    .requestId(request.sessionId())
                    .requestType("CHAT")
                    .payload(request.message())
                    .context(request.context())
                    .metadata(requestMetadata)
                    .build();

            // Submit to Runtime if available
            ExecutionResult executionResult;
            if (runtime != null) {
                ExecutionSession session = runtime.submit(executionRequest);
                // Use the actual execution result from the Runtime session
                executionResult = session.result();
                if (executionResult == null) {
                    throw new SDKException("Runtime returned a session without an execution result");
                }
            } else {
                // Fallback only if Runtime is not available (should not happen with new ShreeBuilder)
                executionResult = ExecutionResult.success(
                        executionRequest.requestId(),
                        "Processed: " + request.message()
                );
            }

            // Convert ExecutionResult to SDKResponse, preserving any structured payload
            // so rich intelligence context reaches the developer application.
            return SDKResponse.builder()
                    .answer(executionResult.output().orElse("No output"))
                    .confidence(executionResult.isSuccess() ? 1.0 : 0.0)
                    .reasoningAvailable(executionResult.isSuccess())
                    .metadata("sdk-version:" + configuration.version())
                    .structuredPayload(executionResult.structuredPayload())
                    .build();
        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            throw new SDKException("Chat request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the SDK configuration.
     *
     * @return the configuration
     */
    public SDKConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns the underlying runtime.
     *
     * @return the runtime
     */
    public Runtime runtime() {
        return runtime;
    }
}