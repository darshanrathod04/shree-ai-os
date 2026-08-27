package com.shreeai.os.platform.runtime.routing;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import com.shreeai.os.platform.runtime.pipeline.stages.IdentityStage;
import com.shreeai.os.platform.runtime.pipeline.stages.KnowledgeStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryRecallStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryStoreStage;
import com.shreeai.os.platform.runtime.pipeline.stages.PlanningStage;
import com.shreeai.os.platform.runtime.routing.RuntimeIntentRouter.ExecutionRoute;
import com.shreeai.os.platform.runtime.routing.RuntimeIntentRouter.TargetKernel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the deterministic {@link RuntimeIntentRouter}.
 *
 * <p>Proves the EO-V1-001 routing table without executing any kernel.</p>
 */
public class RuntimeIntentRouterTest {

    private RuntimeIntentRouter router;

    @BeforeEach
    public void setUp() {
        router = new RuntimeIntentRouter(
                new IdentityStage(),
                new ContextStage(),
                new KnowledgeStage(),
                new PlanningStage(),
                new MemoryRecallStage(),
                new MemoryStoreStage()
        );
    }

    private ExecutionRequest requestWithOperation(String operation) {
        return ExecutionRequest.builder()
                .requestId("router-test")
                .requestType("CHAT")
                .payload("test payload")
                .metadata(operation == null ? Map.of() : Map.of("operation", operation))
                .build();
    }

    /* ==========================================================
       Routing table coverage
       ========================================================== */

    @Test
    public void testSearchKnowledgeRoutesToKnowledgeKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("SEARCH_KNOWLEDGE"));

        assertTrue(route.isPresent(), "SEARCH_KNOWLEDGE must route");
        assertEquals(TargetKernel.KNOWLEDGE, route.get().kernel());
        assertEquals("Knowledge Kernel", route.get().kernelName());
        assertEquals(List.of("Identity", "Context", "Knowledge"), route.get().stageNames());
    }

    @Test
    public void testQueryKnowledgeRoutesToKnowledgeKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("QUERY_KNOWLEDGE"));

        assertTrue(route.isPresent(), "QUERY_KNOWLEDGE must route");
        assertEquals(TargetKernel.KNOWLEDGE, route.get().kernel());
        assertEquals("Knowledge Kernel", route.get().kernelName());
    }

    @Test
    public void testRetrieveEntityRoutesToKnowledgeKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("RETRIEVE_ENTITY"));

        assertTrue(route.isPresent(), "RETRIEVE_ENTITY must route");
        assertEquals(TargetKernel.KNOWLEDGE, route.get().kernel());
        assertEquals("Knowledge Kernel", route.get().kernelName());
    }

    @Test
    public void testPlanProjectRoutesToPlanningKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("PLAN_PROJECT"));

        assertTrue(route.isPresent(), "PLAN_PROJECT must route");
        assertEquals(TargetKernel.PLANNING, route.get().kernel());
        assertEquals("Planning Kernel", route.get().kernelName());
        assertEquals(List.of("Identity", "Context", "Planning"), route.get().stageNames());
    }

    @Test
    public void testCreatePlanRoutesToPlanningKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("CREATE_PLAN"));

        assertTrue(route.isPresent(), "CREATE_PLAN must route");
        assertEquals(TargetKernel.PLANNING, route.get().kernel());
        assertEquals("Planning Kernel", route.get().kernelName());
    }

    @Test
    public void testRecallMemoryRoutesToMemoryKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("RECALL_MEMORY"));

        assertTrue(route.isPresent(), "RECALL_MEMORY must route");
        assertEquals(TargetKernel.MEMORY, route.get().kernel());
        assertEquals("Memory Kernel", route.get().kernelName());
        assertEquals(List.of("Identity", "Context", "MemoryRecall"), route.get().stageNames());
    }

    @Test
    public void testStoreMemoryRoutesToMemoryKernel() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("STORE_MEMORY"));

        assertTrue(route.isPresent(), "STORE_MEMORY must route");
        assertEquals(TargetKernel.MEMORY, route.get().kernel());
        assertEquals("Memory Kernel", route.get().kernelName());
        assertEquals(List.of("Identity", "Context", "MemoryStore"), route.get().stageNames());
    }

    /* ==========================================================
       Determinism and normalisation
       ========================================================== */

    @Test
    public void testRoutingIsCaseInsensitiveAndTrimmed() {
        Optional<ExecutionRoute> lowerCase =
                router.route(requestWithOperation("search_knowledge"));

        assertTrue(lowerCase.isPresent(), "Operation normalisation must be deterministic");
        assertEquals("SEARCH_KNOWLEDGE", lowerCase.get().operation());
        assertEquals(TargetKernel.KNOWLEDGE, lowerCase.get().kernel());

        Optional<ExecutionRoute> padded =
                router.route(requestWithOperation("  PLAN_PROJECT  "));

        assertTrue(padded.isPresent());
        assertEquals("PLAN_PROJECT", padded.get().operation());
    }

    @Test
    public void testRoutingIsDeterministicAcrossCalls() {
        ExecutionRequest request = requestWithOperation("QUERY_KNOWLEDGE");

        Optional<ExecutionRoute> first = router.route(request);
        Optional<ExecutionRoute> second = router.route(request);

        assertEquals(first, second, "Routing must be deterministic");
        assertEquals(
                first.map(ExecutionRoute::stageNames).orElse(List.of()),
                second.map(ExecutionRoute::stageNames).orElse(List.of()));
    }

    @Test
    public void testRoutedStageListIsImmutable() {
        Optional<ExecutionRoute> route =
                router.route(requestWithOperation("RECALL_MEMORY"));

        assertTrue(route.isPresent());
        List<ExecutionStage> stages = route.get().stages();

        assertThrows(UnsupportedOperationException.class, () -> stages.add(new ContextStage()));
    }

    /* ==========================================================
       Chief orchestration fallback
       ========================================================== */

    @Test
    public void testUnknownOperationIsNotRouted() {
        assertFalse(router.isRouted(requestWithOperation("MAKE_COFFEE")));
        assertTrue(router.route(requestWithOperation("MAKE_COFFEE")).isEmpty());
    }

    @Test
    public void testMissingOperationIsNotRouted() {
        assertFalse(router.isRouted(requestWithOperation(null)));
        assertTrue(router.route(requestWithOperation(null)).isEmpty());
    }

    @Test
    public void testNullRequestIsNotRouted() {
        assertFalse(router.isRouted(null));
        assertTrue(router.route(null).isEmpty());
    }

    /* ==========================================================
       Construction contract
       ========================================================== */

    @Test
    public void testNullStagesAreRejected() {
        assertThrows(NullPointerException.class, () -> new RuntimeIntentRouter(
                null,
                new ContextStage(),
                new KnowledgeStage(),
                new PlanningStage(),
                new MemoryRecallStage(),
                new MemoryStoreStage()));

        assertThrows(NullPointerException.class, () -> new RuntimeIntentRouter(
                new IdentityStage(),
                new ContextStage(),
                new KnowledgeStage(),
                new PlanningStage(),
                new MemoryRecallStage(),
                null));
    }
}
