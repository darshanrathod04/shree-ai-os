package com.shreeai.os.platform.resolver;

import com.shreeai.os.platform.gateway.GatewayRequest;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.sdk.SDKRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultCapabilityResolver}.
 *
 * <p>Verifies intent detection, capability assembly, baseline inclusion,
 * metadata override behavior, and gateway-request delegation.</p>
 */
class DefaultCapabilityResolverTest {

    private final DefaultCapabilityResolver resolver = new DefaultCapabilityResolver();

    @Test
    void fallbackChatIntentOnConversationalMessage() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Hello, how are you?")
                .sessionId("session-1")
                .build());

        assertEquals("session-1", plan.requestId());
        assertEquals(IntentType.CHAT, plan.detectedIntent());
        assertTrue(plan.confidence() >= 0.0 && plan.confidence() <= 1.0);
        assertBaselinePresent(plan);
        assertTrue(plan.requires(CapabilityType.MODELS));
        assertTrue(plan.requires(CapabilityType.REASONING));
    }

    @Test
    void rememberDetectsMemoryStoreIntent() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Remember that my favorite color is blue")
                .build());

        assertEquals(IntentType.MEMORY_STORE, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.MEMORY));
        assertTrue(plan.requires(CapabilityType.IDENTITY));
        assertBaselinePresent(plan);
    }

    @Test
    void whatIsDetectsKnowledgeQueryIntent() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("What is a linked list?")
                .build());

        assertEquals(IntentType.KNOWLEDGE_QUERY, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.KNOWLEDGE));
        assertTrue(plan.requires(CapabilityType.REASONING));
    }

    @Test
    void implementDetectsDeveloperIntentWithBroadCapabilities() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .build());

        assertEquals(IntentType.DEVELOPER, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.KNOWLEDGE));
        assertTrue(plan.requires(CapabilityType.PLANNING));
        assertTrue(plan.requires(CapabilityType.EXECUTION));
        assertTrue(plan.requires(CapabilityType.TOOLS));
        assertTrue(plan.requires(CapabilityType.MODELS));
        assertTrue(plan.requires(CapabilityType.AGENTS));
        assertTrue(plan.requires(CapabilityType.ORCHESTRATION));
    }

    @Test
    void planDetectsPlanningIntentWithOrchestration() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Plan the steps to launch a new feature")
                .build());

        assertEquals(IntentType.PLANNING, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.PLANNING));
        assertTrue(plan.requires(CapabilityType.EXECUTION));
        assertTrue(plan.requires(CapabilityType.ORCHESTRATION));
    }

    @Test
    void executeDetectsExecutionIntentWithTools() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Execute the build and run the tests")
                .build());

        assertEquals(IntentType.EXECUTION, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.EXECUTION));
        assertTrue(plan.requires(CapabilityType.TOOLS));
    }

    @Test
    void metadataIntentOverrideWinsOverKeywords() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Please remember my name")
                .metadata(Map.of("intent", "KNOWLEDGE_QUERY"))
                .build());

        assertEquals(IntentType.KNOWLEDGE_QUERY, plan.detectedIntent());
        assertEquals(0.98, plan.confidence());
        assertEquals("metadata", plan.metadata().get("intentSource"));
    }

    @Test
    void planMetadataCarriesResolverIdentity() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Hello")
                .build());

        assertEquals("CapabilityResolver", plan.metadata().get("source"));
        assertEquals("DefaultCapabilityResolver", plan.metadata().get("resolver"));
        assertEquals("keyword", plan.metadata().get("intentSource"));
    }

    @Test
    void requiredCapabilitiesAreOrderedAndImmutable() {
        CapabilityPlan plan = resolver.resolve(SDKRequest.builder()
                .message("Plan a rollout")
                .build());

        List<CapabilityRequirement> requirements = plan.requiredCapabilities();
        assertFalse(requirements.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> requirements.add(null));
        assertNotNull(requirements.get(0).capability());
        assertNotNull(requirements.get(0).priority());
        assertFalse(requirements.get(0).reason().isBlank());
    }

    @Test
    void resolveRejectsNullSdkRequest() {
        assertThrows(NullPointerException.class, () -> resolver.resolve((SDKRequest) null));
    }

    @Test
    void gatewayRequestDelegatesToSdkRequest() {
        GatewayRequest gatewayRequest = GatewayRequest.builder()
                .sdkRequest(SDKRequest.builder()
                        .message("Explain neural networks")
                        .sessionId("gateway-session")
                        .build())
                .gatewayRequestId("gw-1")
                .build();

        CapabilityPlan plan = resolver.resolve(gatewayRequest);

        assertEquals("gateway-session", plan.requestId());
        assertEquals(IntentType.KNOWLEDGE_QUERY, plan.detectedIntent());
        assertTrue(plan.requires(CapabilityType.KNOWLEDGE));
    }

    private void assertBaselinePresent(CapabilityPlan plan) {
        assertTrue(plan.requires(CapabilityType.CONTEXT), "CONTEXT must be baseline");
        assertTrue(plan.requires(CapabilityType.VALIDATION), "VALIDATION must be baseline");
        assertTrue(plan.requires(CapabilityType.SAFETY), "SAFETY must be baseline");
        assertTrue(plan.requires(CapabilityType.OBSERVABILITY), "OBSERVABILITY must be baseline");
    }
}