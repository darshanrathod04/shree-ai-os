package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionEdge;
import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.graph.NodeType;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.identity.api.IdentityType;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DefaultGraphRuntimeExecutor}.
 *
 * <p>Verifies that the graph runtime executor correctly dispatches to real
 * kernel services through {@link CapabilityNodeExecutors}, applies the
 * governance interceptor chain, and produces a terminal execution session.</p>
 */
class DefaultGraphRuntimeExecutorIntegrationTest {

    // ─── Test doubles ──────────────────────────────────────────────────────

    /**
     * Real-ish identity service that returns a fixed IdentityContext for any input.
     */
    private static final class TestIdentityService implements IdentityService {
        private final IdentityContext fixedContext;

        TestIdentityService() {
            this.fixedContext = IdentityContext.builder()
                    .identityId(new IdentityId("test-agent-001"))
                    .identityType(IdentityType.AGENT)
                    .sessionId("test-session")
                    .applicationId("test-app")
                    .workspaceId("test-workspace")
                    .build();
        }

        @Override
        public IdentityContext resolveIdentity(String requestId, String sessionId,
                                               String applicationId, String workspaceId) {
            return fixedContext;
        }
    }

    /**
     * Real-ish knowledge service that returns a fixed list of knowledge nodes
     * matching the query.
     */
    private static final class TestKnowledgeSearchService implements KnowledgeSearchService {
        private final List<KnowledgeNode> knowledge;

        TestKnowledgeSearchService() {
            this.knowledge = List.of(
                    KnowledgeNode.of(
                            new com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId("kn-1"),
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType.CONCEPT,
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState.ACTIVE,
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope.PRIVATE,
                            "Java Programming Language",
                            "Java is a high-performance, object-oriented programming language.",
                            Map.of(),
                            Instant.now(),
                            Instant.now()
                    ),
                    KnowledgeNode.of(
                            new com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId("kn-2"),
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType.CONCEPT,
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState.ACTIVE,
                            com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope.PRIVATE,
                            "Spring Framework",
                            "Spring is a Java application framework for dependency injection.",
                            Map.of(),
                            Instant.now(),
                            Instant.now()
                    )
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<KnowledgeNode> search(String keyword) {
            if (keyword == null || keyword.isBlank()) {
                return List.of();
            }
            String lower = keyword.toLowerCase();
            return knowledge.stream()
                    .filter(kn -> kn.getLabel().toLowerCase().contains(lower)
                            || kn.getDescription().toLowerCase().contains(lower))
                    .toList();
        }

        @Override
        public List<KnowledgeNode> searchByTopic(String topic) {
            return List.of();
        }

        @Override
        public List<KnowledgeNode> searchByConcept(String concept) {
            return List.of();
        }

        @Override
        public List<KnowledgeNode> searchByTags(Iterable<String> tags) {
            return List.of();
        }

        @Override
        public List<KnowledgeNode> searchBySimilarity(String text) {
            return List.of();
        }
    }

    /**
     * Real-ish memory service that returns a fixed list of memories matching the query.
     */
    private static final class TestMemorySearchService implements MemorySearchService {
        private final List<Memory> memories;

        TestMemorySearchService() {
            this.memories = List.of(
                    new Memory(
                            new MemoryId("mem-1"),
                            new MemoryContent("Java is a compiled, statically-typed language.",
                                    null, Map.of(), Instant.now()),
                            new MemoryMetadata(
                                    new MemoryId("mem-1"),
                                    MemoryType.EPISODIC,
                                    MemoryStatus.ACTIVE,
                                    MemoryVisibility.PRIVATE,
                                    new IdentityId("test-agent-001"),
                                    Set.of("java", "language"),
                                    0.8,
                                    0.9,
                                    "test",
                                    Instant.now(),
                                    Instant.now(),
                                    Instant.now(),
                                    1L
                            ),
                            Instant.now(),
                            Instant.now()
                    ),
                    new Memory(
                            new MemoryId("mem-2"),
                            new MemoryContent("Shree AI OS is built on Java.",
                                    null, Map.of(), Instant.now()),
                            new MemoryMetadata(
                                    new MemoryId("mem-2"),
                                    MemoryType.SEMANTIC,
                                    MemoryStatus.ACTIVE,
                                    MemoryVisibility.PRIVATE,
                                    new IdentityId("test-agent-001"),
                                    Set.of("shree-ai", "java", "os"),
                                    0.9,
                                    0.95,
                                    "test",
                                    Instant.now(),
                                    Instant.now(),
                                    Instant.now(),
                                    2L
                            ),
                            Instant.now(),
                            Instant.now()
                    )
            );
        }

        @Override
        public List<Memory> search(String query) {
            if (query == null || query.isBlank()) {
                return List.copyOf(memories);
            }
            String lower = query.toLowerCase();
            return memories.stream()
                    .filter(m -> m.content().text().toLowerCase().contains(lower))
                    .toList();
        }

        @Override
        public List<Memory> searchByTags(java.util.Set<String> tags) {
            return List.of();
        }

        @Override
        public List<Memory> searchByDate(java.time.Instant from, java.time.Instant to) {
            return List.of();
        }

        @Override
        public List<Memory> searchBySimilarity(String text) {
            return search(text);
        }

        @Override
        public List<Memory> searchByOwner(com.shreeai.os.platform.kernels.identity.model.IdentityId ownerId) {
            return List.of();
        }
    }

    /**
     * Real-ish planning service that returns a synthetic plan ID.
     */
    private static final class TestPlanningService implements PlanningService {
        @Override
        public String createPlan(PlanningService.PlanningRequest planningRequest) {
            return "plan-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        @Override
        public String refinePlan(PlanningService.PlanRefinementRequest refinementRequest) {
            return "plan-refined-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        @Override
        public String validatePlan(PlanningService.PlanValidationRequest planValidationRequest) {
            return "valid";
        }
    }

    // ─── Graph builders ────────────────────────────────────────────────────

    /**
     * Builds an {@link ExecutionEdge} between two nodes via its builder.
     */
    private static ExecutionEdge edge(String source, String target) {
        return ExecutionEdge.builder()
                .edgeId(source + "->" + target)
                .sourceNodeId(source)
                .targetNodeId(target)
                .build();
    }

    /**
     * Builds a minimal graph with two nodes: IDENTITY → MEMORY (dependency chain).
     */
    private static ExecutionGraph buildIdentityMemoryGraph() {
        ExecutionNode identityNode = ExecutionNode.builder()
                .nodeId("IDENTITY")
                .capability(CapabilityType.IDENTITY)
                .nodeType(NodeType.ROOT)
                .build();

        ExecutionNode memoryNode = ExecutionNode.builder()
                .nodeId("MEMORY")
                .capability(CapabilityType.MEMORY)
                .nodeType(NodeType.RETRIEVAL)
                .addDependency("IDENTITY")
                .build();

        return ExecutionGraph.builder()
                .graphId("graph-test-" + System.currentTimeMillis())
                .requestId("req-test")
                .rootNode(identityNode)
                .addNode(identityNode)
                .addNode(memoryNode)
                .addEdge(edge("IDENTITY", "MEMORY"))
                .build();
    }

    /**
     * Builds a linear chain graph: IDENTITY → MEMORY → KNOWLEDGE → PLANNING.
     */
    private static ExecutionGraph buildFullChainGraph() {
        ExecutionNode identityNode = ExecutionNode.builder()
                .nodeId("IDENTITY")
                .capability(CapabilityType.IDENTITY)
                .nodeType(NodeType.ROOT)
                .build();

        ExecutionNode memoryNode = ExecutionNode.builder()
                .nodeId("MEMORY")
                .capability(CapabilityType.MEMORY)
                .nodeType(NodeType.RETRIEVAL)
                .addDependency("IDENTITY")
                .build();

        ExecutionNode knowledgeNode = ExecutionNode.builder()
                .nodeId("KNOWLEDGE")
                .capability(CapabilityType.KNOWLEDGE)
                .nodeType(NodeType.RETRIEVAL)
                .addDependency("MEMORY")
                .build();

        ExecutionNode planningNode = ExecutionNode.builder()
                .nodeId("PLANNING")
                .capability(CapabilityType.PLANNING)
                .nodeType(NodeType.COGNITIVE)
                .addDependency("KNOWLEDGE")
                .build();

        return ExecutionGraph.builder()
                .graphId("graph-full-" + System.currentTimeMillis())
                .requestId("req-full")
                .rootNode(identityNode)
                .addNode(identityNode)
                .addNode(memoryNode)
                .addNode(knowledgeNode)
                .addNode(planningNode)
                .addEdge(edge("IDENTITY", "MEMORY"))
                .addEdge(edge("MEMORY", "KNOWLEDGE"))
                .addEdge(edge("KNOWLEDGE", "PLANNING"))
                .build();
    }

    // ─── Tests ─────────────────────────────────────────────────────────────

    @Test
    void testIdentityMemoryGraphExecutesSuccessfully() {
        ExecutionGraph graph = buildIdentityMemoryGraph();

        Map<CapabilityType, NodeExecutor> nodeExecutors =
                CapabilityNodeExecutors.buildNodeExecutors(
                        new TestIdentityService(),
                        new TestKnowledgeSearchService(),
                        new TestPlanningService(),
                        new TestMemorySearchService());

        List<RuntimeInterceptor> interceptors = List.of(
                new SafetyInterceptor(),
                new ValidationInterceptor(),
                new ObservabilityInterceptor());

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(nodeExecutors, interceptors);

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-001")
                .payload("Java programming language")
                .metadata(Map.of("requestId", "test-req-001"))
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session, "Session must not be null");
        assertEquals(ExecutionSession.SessionStatus.COMPLETED, session.status(),
                "Session status should be COMPLETED when all nodes succeed");
    }

    @Test
    void testFullChainGraphExecutesAllCapabilities() {
        ExecutionGraph graph = buildFullChainGraph();

        Map<CapabilityType, NodeExecutor> nodeExecutors =
                CapabilityNodeExecutors.buildNodeExecutors(
                        new TestIdentityService(),
                        new TestKnowledgeSearchService(),
                        new TestPlanningService(),
                        new TestMemorySearchService());

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(nodeExecutors); // uses default interceptors

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-002")
                .payload("Java")
                .metadata(Map.of("requestId", "test-req-002"))
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session);
        assertEquals(ExecutionSession.SessionStatus.COMPLETED, session.status());
        assertNotNull(session.result(), "Result must not be null");
        assertTrue(session.result().isSuccess(), "Result must be successful");
    }

    @Test
    void testGraphWithUnregisteredCapabilityIsSkipped() {
        // Graph has IDENTITY → MEMORY → KNOWLEDGE → PLANNING, but we only register
        // IDENTITY and MEMORY executors: KNOWLEDGE/PLANNING have no executor registered.
        ExecutionGraph graph = buildFullChainGraph();

        Map<CapabilityType, NodeExecutor> nodeExecutors =
                CapabilityNodeExecutors.buildNodeExecutors(
                        new TestIdentityService(),
                        null,  // no knowledge service
                        null,  // no planning service
                        new TestMemorySearchService());

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(nodeExecutors);

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-003")
                .payload("Java")
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session);
        // Both registered nodes (IDENTITY, MEMORY) should complete successfully.
        assertEquals(ExecutionSession.SessionStatus.COMPLETED, session.status());
    }

    @Test
    void testSafetyInterceptorBlocksDestructivePayload() {
        ExecutionGraph graph = buildIdentityMemoryGraph();

        Map<CapabilityType, NodeExecutor> nodeExecutors =
                CapabilityNodeExecutors.buildNodeExecutors(
                        new TestIdentityService(),
                        null, null,
                        new TestMemorySearchService());

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(nodeExecutors);

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-004")
                .payload("drop table users")  // blocked by SafetyInterceptor
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session);
        assertEquals(ExecutionSession.SessionStatus.FAILED, session.status(),
                "Session should be FAILED when SafetyInterceptor blocks a node");
        assertNotNull(session.result(), "Result must not be null");
        assertFalse(session.result().isSuccess());
        // The error message must originate from the safety gate.
        session.result().errorMessage().ifPresentOrElse(
                msg -> assertTrue(msg.contains("safety"), () -> "error: " + msg),
                () -> fail("Expected a non-empty error message"));
        // Structured payload surfaces the canonical safety interception that
        // blocked the node, so callers can see exactly what was denied.
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = session.result().structuredPayload();
        assertTrue(payload.containsKey("safetyInterceptions"),
                "structured payload must contain safetyInterceptions");
        List<Map<String, Object>> interceptions =
                (List<Map<String, Object>>) payload.get("safetyInterceptions");
        assertEquals(1, interceptions.size());
        Map<String, Object> interception = interceptions.get(0);
        assertEquals("DENY", interception.get("outcome"));
        assertEquals("IDENTITY", interception.get("nodeId"));
        assertTrue(((String) interception.get("reason")).contains("destructive pattern"));
    }

    @Test
    void testValidationInterceptorRetriesAndStopsOnFailure() {
        // A graph with a single KNOWLEDGE node whose executor always returns
        // an output that triggers the validation interceptor's RETRY verdict
        // (output containing "error:"). After MAX_RETRIES, the node is marked
        // FAILED and the graph stops.
        ExecutionNode knowledgeNode = ExecutionNode.builder()
                .nodeId("KNOWLEDGE")
                .capability(CapabilityType.KNOWLEDGE)
                .nodeType(NodeType.RETRIEVAL)
                .build();

        ExecutionGraph graph = ExecutionGraph.builder()
                .graphId("graph-validation-" + System.currentTimeMillis())
                .requestId("req-validation")
                .rootNode(knowledgeNode)
                .addNode(knowledgeNode)
                .build();

        // Executor that always produces a failing output.
        NodeExecutor failingExecutor = (node, request, context) ->
                ExecutionNodeResult.completed(
                        node.nodeId(), node.capability(),
                        "error: something went wrong", 0.9, 0L);

        Map<CapabilityType, NodeExecutor> nodeExecutors = Map.of(
                CapabilityType.KNOWLEDGE, failingExecutor);

        List<RuntimeInterceptor> interceptors = List.of(
                new SafetyInterceptor(),
                new ValidationInterceptor(),
                new ObservabilityInterceptor());

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(nodeExecutors, interceptors);

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-006")
                .payload("test")
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session);
        assertEquals(ExecutionSession.SessionStatus.FAILED, session.status(),
                "Session should be FAILED when validation fails after retries");
        assertFalse(session.result().isSuccess());
        assertTrue(session.result().errorMessage().isPresent());
        // The error message must mention validation.
        assertTrue(session.result().errorMessage().get().toLowerCase().contains("validation"),
                "error: " + session.result().errorMessage().orElse(""));
    }

    @Test
    void testEmptyNodeExecutorsProducesFailure() {
        ExecutionGraph graph = buildIdentityMemoryGraph();

        DefaultGraphRuntimeExecutor executor =
                new DefaultGraphRuntimeExecutor(Map.of());

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-005")
                .payload("test")
                .build();

        ExecutionSession session = executor.execute(graph, request);

        assertNotNull(session);
        assertEquals(ExecutionSession.SessionStatus.FAILED, session.status(),
                "Session should be FAILED when no executors are registered");
    }
}
