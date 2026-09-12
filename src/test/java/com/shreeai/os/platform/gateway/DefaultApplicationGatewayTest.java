package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.resolver.CapabilityPlan;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultApplicationGateway}.
 *
 * <p>Verifies the gateway contract: request validation, session
 * normalization, identity attachment, runtime forwarding, and response
 * mapping.</p>
 */
class DefaultApplicationGatewayTest {

    @Test
    void handleReturnsRuntimeResponseForValidRequest() {
        Runtime runtime = mock(Runtime.class);
        ExecutionResult result = ExecutionResult.success("session-1", "Hello from runtime");
        ExecutionSession session = ExecutionSession.builder()
                .sessionId("session-1")
                .requestId("session-1")
                .status(ExecutionSession.SessionStatus.COMPLETED)
                .result(result)
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        SDKResponse response = gateway.handle(SDKRequest.builder()
                .message("Hello")
                .sessionId("session-1")
                .build());

        assertNotNull(response);
        assertEquals("Hello from runtime", response.answer());
        assertEquals(1.0, response.confidence());
        assertTrue(response.reasoningAvailable());

        ArgumentCaptor<ExecutionRequest> captor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(runtime).submit(captor.capture());
        assertEquals("Hello", captor.getValue().payload());
        assertEquals("CHAT", captor.getValue().requestType());
    }

    @Test
    void handleRejectsNullRequest() {
        Runtime runtime = mock(Runtime.class);
        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        assertThrows(GatewayException.class, () -> gateway.handle(null));
    }

    @Test
    void blankMessageIsRejectedAtSdkRequestConstruction() {
        // SDKRequest's own builder validates blank messages before the
        // request can ever reach the gateway (defense in depth). The
        // gateway additionally validates in validate().
        Runtime runtime = mock(Runtime.class);
        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);
        assertNotNull(gateway);

        assertThrows(ValidationException.class, () -> SDKRequest.builder()
                .message("   ")
                .sessionId("session-1")
                .build());
    }

    @Test
    void handleThrowsGatewayExceptionWhenRuntimeReturnsSessionWithoutResult() {
        Runtime runtime = mock(Runtime.class);
        ExecutionSession session = ExecutionSession.builder()
                .sessionId("session-1")
                .requestId("session-1")
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        assertThrows(GatewayException.class, () -> gateway.handle(SDKRequest.builder()
                .message("Hello")
                .sessionId("session-1")
                .build()));
    }

    @Test
    void handleNormalizesMissingSessionId() {
        Runtime runtime = mock(Runtime.class);
        ExecutionResult result = ExecutionResult.success("generated", "ok");
        ExecutionSession session = ExecutionSession.builder()
                .requestId("generated")
                .status(ExecutionSession.SessionStatus.COMPLETED)
                .result(result)
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        gateway.handle(SDKRequest.builder()
                .message("Hello without session")
                .build());

        ArgumentCaptor<ExecutionRequest> captor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(runtime).submit(captor.capture());
        String requestId = captor.getValue().requestId();
        assertNotNull(requestId);
        assertFalse(requestId.isBlank());
    }

    @Test
    void handleAttachesResolvedCapabilityPlanToRuntimeRequest() {
        Runtime runtime = mock(Runtime.class);
        ExecutionResult result = ExecutionResult.success("session-1", "ok");
        ExecutionSession session = ExecutionSession.builder()
                .sessionId("session-1")
                .requestId("session-1")
                .status(ExecutionSession.SessionStatus.COMPLETED)
                .result(result)
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        gateway.handle(SDKRequest.builder()
                .message("Remember my name")
                .sessionId("session-1")
                .build());

        ArgumentCaptor<ExecutionRequest> captor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(runtime).submit(captor.capture());
        Map<String, Object> metadata = captor.getValue().metadata();

        Object planValue = metadata.get("capabilityPlan");
        assertNotNull(planValue, "capabilityPlan must be attached to runtime metadata");
        CapabilityPlan plan = assertInstanceOf(CapabilityPlan.class, planValue);
        assertEquals(
                com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType.MEMORY_STORE,
                plan.detectedIntent());
        assertEquals("MEMORY_STORE", metadata.get("detectedIntent"));

        // Runtime path is unchanged: standard CHAT execution request.
        assertEquals("CHAT", captor.getValue().requestType());
    }

    @Test
    void handleAttachesExecutionGraphToRuntimeRequest() {
        Runtime runtime = mock(Runtime.class);
        ExecutionResult result = ExecutionResult.success("session-1", "ok");
        ExecutionSession session = ExecutionSession.builder()
                .sessionId("session-1")
                .requestId("session-1")
                .status(ExecutionSession.SessionStatus.COMPLETED)
                .result(result)
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        gateway.handle(SDKRequest.builder()
                .message("Remember my name")
                .sessionId("session-1")
                .build());

        ArgumentCaptor<ExecutionRequest> captor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(runtime).submit(captor.capture());
        Map<String, Object> metadata = captor.getValue().metadata();

        Object graphValue = metadata.get("executionGraph");
        assertNotNull(graphValue, "executionGraph must be attached to runtime metadata");
        ExecutionGraph graph = assertInstanceOf(ExecutionGraph.class, graphValue);
        assertEquals("session-1", graph.requestId());
        assertEquals("IDENTITY", graph.rootNode().nodeId());
        assertNotNull(metadata.get("executionGraphId"));

        // Graph is a planning artifact only — nodes must never leave PENDING.
        graph.nodes().forEach(node ->
                assertEquals(ExecutionNode.State.PENDING, node.state()));
    }

    @Test
    void handleExtractsSynthesizedResponseFromStructuredPayload() {
        Runtime runtime = mock(Runtime.class);
        SynthesizedResponse synthesized = SynthesizedResponse.simple(
                "Structured answer", 0.85);
        ExecutionResult result = ExecutionResult.builder()
                .requestId("session-1")
                .success(true)
                .output("fallback")
                .structuredPayload(Map.of("response", synthesized))
                .build();
        ExecutionSession session = ExecutionSession.builder()
                .sessionId("session-1")
                .requestId("session-1")
                .status(ExecutionSession.SessionStatus.COMPLETED)
                .result(result)
                .build();
        when(runtime.submit(any())).thenReturn(session);

        DefaultApplicationGateway gateway = new DefaultApplicationGateway(runtime);

        SDKResponse response = gateway.handle(SDKRequest.builder()
                .message("Explain streams")
                .sessionId("session-1")
                .build());

        assertEquals("Structured answer", response.answer());
        assertEquals(0.85, response.confidence());
        assertSame(synthesized, response.structuredPayload().get("response"));
    }

    @Test
    void isReadyReflectsRuntimePresence() {
        Runtime runtime = mock(Runtime.class);

        assertTrue(new DefaultApplicationGateway(runtime).isReady());
        assertFalse(new DefaultApplicationGateway(null).isReady());
    }
}