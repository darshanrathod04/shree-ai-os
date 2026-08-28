package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EO-V1-001 — Runtime Intent Router integration verification.
 *
 * <p>Proves that SDK requests carrying {@code metadata.operation} are
 * dispatched to their owning kernel instead of the generic Chief reasoning
 * path, while unrouted requests still traverse the canonical 10-stage
 * Chief orchestration pipeline.</p>
 */
public class RuntimeIntentRoutingIntegrationTest {

    private ShreeAI ai;

    @BeforeEach
    public void setUp() {
        ai = ShreeAI.builder().apiKey("local").build();
    }

    private void assertRoutedTo(
            SDKResponse response,
            String expectedOperation,
            String expectedKernel,
            String expectedKernelStage) {

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload should not be null");

        assertEquals(expectedOperation,
                payload.get("routedOperation"),
                "Routed operation mismatch");
        assertEquals(expectedKernel,
                payload.get("routedKernel"),
                "Routed kernel mismatch");

        @SuppressWarnings("unchecked")
        List<String> routedStages = (List<String>) payload.get("routedStages");
        assertNotNull(routedStages, "Routed stages should be recorded");
        assertTrue(routedStages.contains(expectedKernelStage),
                "Kernel stage should execute. Stages: " + routedStages);
        assertFalse(routedStages.contains("Reasoning"),
                "Generic reasoning must not run for routed operations");
        assertFalse(routedStages.contains("ChiefReview"),
                "Chief review must not run for routed operations");
    }

    /* ==========================================================
       Knowledge Kernel routing
       ========================================================== */

    @Test
    public void testKnowledgeSearchRoutesToKnowledgeKernel() {
        SDKResponse response = ai.knowledge().search("Java");
        assertRoutedTo(response, "SEARCH_KNOWLEDGE", "Knowledge Kernel", "Knowledge");
    }

    @Test
    public void testKnowledgeQueryRoutesToKnowledgeKernel() {
        SDKResponse response = ai.knowledge().query("What is the JVM?");
        assertRoutedTo(response, "QUERY_KNOWLEDGE", "Knowledge Kernel", "Knowledge");
    }

    @Test
    public void testKnowledgeRetrieveRoutesToKnowledgeKernel() {
        SDKResponse response = ai.knowledge().retrieve("knowledge-1");
        assertRoutedTo(response, "RETRIEVE_ENTITY", "Knowledge Kernel", "Knowledge");
    }

    /* ==========================================================
       Planning Kernel routing
       ========================================================== */

    @Test
    public void testCreatePlanRoutesToPlanningKernel() {
        SDKResponse response =
                ai.planning().createPlan("obj-1", "Build a login system", "STANDARD");
        assertRoutedTo(response, "CREATE_PLAN", "Planning Kernel", "Planning");
    }

        @Test
    public void testPlanProjectRoutesToPlanningKernel() {
        SDKRequest request = SDKRequest.builder()
                .message("Plan the migration project")
                .metadata(Map.of("operation", "PLAN_PROJECT"))
                .build();

        SDKResponse response = ai.chat(request);
        assertRoutedTo(response, "PLAN_PROJECT", "Planning Kernel", "Planning");
    }

    /* ==========================================================
       Planning content verification (EO-V1-003)
       ========================================================== */

    @Test
    public void testCreatePlanWorkoutProducesDomainSubtasks() {

        SDKResponse response = ai.planning().createPlan(
                "workout-001",
                "Create a 3-day beginner Push Pull Legs workout",
                "COMPREHENSIVE"
        );

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");

        String answer = response.answer();

        // Must NOT contain the placeholder SDK message
        assertFalse(answer.contains("PLANNING_CREATE"),
                "Plan must not contain the SDK message placeholder: " + answer);

        // Must contain domain-specific subtasks from GYM domain
        assertTrue(answer.contains("Push workout"),
                "Plan must contain 'Push workout': " + answer);
        assertTrue(answer.contains("Pull workout"),
                "Plan must contain 'Pull workout': " + answer);
        assertTrue(answer.contains("Legs workout"),
                "Plan must contain 'Legs workout': " + answer);
        assertTrue(answer.contains("Recovery strategy"),
                "Plan must contain 'Recovery strategy': " + answer);

        // Verify structured payload carries the subtasks
        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload should not be null");

        com.shreeai.os.platform.kernels.response.model.SynthesizedResponse synth =
                (com.shreeai.os.platform.kernels.response.model.SynthesizedResponse)
                        payload.get("response");
        assertNotNull(synth, "SynthesizedResponse must be in structured payload");

        @SuppressWarnings("unchecked")
        List<String> subtasks = (List<String>) synth.structuredData().get("subtasks");
        assertNotNull(subtasks, "Subtasks must be in structured data");
        assertTrue(subtasks.contains("Push workout"),
                "Subtasks list must contain 'Push workout'");
        assertTrue(subtasks.contains("Pull workout"),
                "Subtasks list must contain 'Pull workout'");
        assertTrue(subtasks.contains("Legs workout"),
                "Subtasks list must contain 'Legs workout'");
        assertTrue(subtasks.contains("Recovery strategy"),
                "Subtasks list must contain 'Recovery strategy'");
    }

    @Test
    public void testCreatePlanSoftwareProducesDomainSubtasks() {

        SDKResponse response = ai.planning().createPlan(
                "software-001",
                "Build a backend software application with a database",
                "COMPREHENSIVE"
        );

        String answer = response.answer();

        assertFalse(answer.contains("PLANNING_CREATE"),
                "Plan must not contain the SDK message placeholder");

        assertTrue(answer.contains("Architecture"),
                "Plan must contain 'Architecture': " + answer);
        assertTrue(answer.contains("Backend"),
                "Plan must contain 'Backend': " + answer);
        assertTrue(answer.contains("Deployment"),
                "Plan must contain 'Deployment': " + answer);
    }

    @Test
    public void testCreatePlanFallbackProducesGeneralSubtasks() {

        SDKResponse response = ai.planning().createPlan(
                "general-001",
                "Organize the annual team offsite",
                "COMPREHENSIVE"
        );

        String answer = response.answer();

        assertFalse(answer.contains("PLANNING_CREATE"),
                "Plan must not contain the SDK message placeholder");

        assertTrue(answer.contains("Research"),
                "Fallback plan must contain 'Research': " + answer);
        assertTrue(answer.contains("Implementation"),
                "Fallback plan must contain 'Implementation': " + answer);
    }

    /* ==========================================================
       Memory Kernel routing
       ========================================================== */

    @Test
    public void testRecallMemoryRoutesToMemoryKernel() {
        SDKResponse response = ai.memory().recall("Java streams");
        assertRoutedTo(response, "RECALL_MEMORY", "Memory Kernel", "MemoryRecall");
    }

    @Test
    public void testStoreMemoryRoutesToMemoryKernel() {
        SDKResponse response = ai.memory().store("Note", "Remember the design decision");
        assertRoutedTo(response, "STORE_MEMORY", "Memory Kernel", "MemoryStore");
    }

    /* ==========================================================
       Chief orchestration fallback
       ========================================================== */

    @Test
    public void testUnroutedChatKeepsChiefOrchestration() {
        SDKResponse response = ai.chat("Hello Shree");

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "Structured payload should not be null");
        assertFalse(payload.containsKey("routedKernel"),
                "Unrouted chat must not claim a kernel route");
    }

    @Test
    public void testCanonicalPipelineStillHasTenStages() {
        Runtime runtime = ai.client().runtime();
        assertTrue(runtime instanceof DefaultRuntimeService,
                "Runtime should remain the DefaultRuntimeService");

        com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline pipeline =
                (com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline)
                        runtime.pipeline();

        assertEquals(10, pipeline.getStages().size(),
                "Canonical Chief orchestration pipeline must keep 10 stages");
    }

    @Test
    public void testDirectRuntimeSubmitRoutesByMetadata() {
        Runtime runtime = ai.client().runtime();

        ExecutionSession session = runtime.submit(
                ExecutionRequest.builder()
                        .requestId("routing-direct-1")
                        .requestType("CHAT")
                        .payload("Plan the project")
                        .addMetadata("operation", "CREATE_PLAN")
                        .build());

        assertNotNull(session, "Session should not be null");
        assertNotNull(session.result(), "Result should not be null");
        assertTrue(session.result().isSuccess(), "Routed execution should succeed");

        Map<String, Object> payload = session.result().structuredPayload();
        assertEquals("CREATE_PLAN", payload.get("routedOperation"));
        assertEquals("Planning Kernel", payload.get("routedKernel"));
    }
}
