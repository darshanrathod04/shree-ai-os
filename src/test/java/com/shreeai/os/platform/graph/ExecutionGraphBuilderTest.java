package com.shreeai.os.platform.graph;

import com.shreeai.os.platform.resolver.CapabilityPlan;
import com.shreeai.os.platform.resolver.CapabilityRequirement;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.resolver.DefaultCapabilityResolver;
import com.shreeai.os.platform.sdk.SDKRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ExecutionGraphBuilder}.
 *
 * <p>Verifies automatic dependency generation for every graph rule, node
 * structure (PENDING-only states), edge generation, and metadata attachment.
 * The builder is never expected to execute anything.</p>
 */
class ExecutionGraphBuilderTest {

    private final ExecutionGraphBuilder builder = new ExecutionGraphBuilder();

    // ========================================================================
    // Developer request — the canonical full-chain sample
    // ========================================================================

    @Test
    void developerRequestBuildsFullGraph() {
        CapabilityPlan plan = new DefaultCapabilityResolver().resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .sessionId("session-dev")
                .build());
        ExecutionGraph graph = builder.build(plan);

        assertEquals("session-dev", graph.requestId());
        assertNotNull(graph.graphId());
        assertTrue(graph.graphId().startsWith("graph-"));

        // Identity must always be root.
        assertEquals("IDENTITY", graph.rootNode().nodeId());
        assertEquals(CapabilityType.IDENTITY, graph.rootNode().capability());
        assertTrue(graph.rootNode().dependencies().isEmpty());

        // All plan capabilities become nodes — except the governance
        // interceptors (SAFETY / VALIDATION / OBSERVABILITY), which are
        // deliberately NOT graph nodes. They remain runtime interceptors
        // applied by the executor, as asserted by
        // governanceInterceptorsAreNotGraphNodes().
        for (CapabilityRequirement requirement : plan.requiredCapabilities()) {
            if (isGovernance(requirement.capability())) {
                continue;
            }
            ExecutionNode node = graph.nodeById(requirement.capability().name());
            assertNotNull(node, "Node must exist for " + requirement.capability());
            assertEquals(requirement.capability(), node.capability());
            assertEquals(ExecutionNode.State.PENDING, node.state());
        }
    }

    @Test
    void developerRequestFollowsChainRules() {
        ExecutionGraph graph = builder.build(new DefaultCapabilityResolver().resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .build()));

        // Context depends on Identity.
        assertEquals(List.of("IDENTITY"), graph.nodeById("CONTEXT").dependencies());

        // Knowledge depends on Context.
        assertEquals(List.of("CONTEXT"), graph.nodeById("KNOWLEDGE").dependencies());

        // Reasoning depends on Knowledge (developer plan has no Memory).
        assertEquals(List.of("KNOWLEDGE"), graph.nodeById("REASONING").dependencies());

        // Planning depends on Reasoning.
        assertEquals(List.of("REASONING"), graph.nodeById("PLANNING").dependencies());

        // Execution depends on Planning.
        assertEquals(List.of("PLANNING"), graph.nodeById("EXECUTION").dependencies());

        // Tools depend on Context.
        assertEquals(List.of("CONTEXT"), graph.nodeById("TOOLS").dependencies());

        // Models depend on Reasoning.
        assertEquals(List.of("REASONING"), graph.nodeById("MODELS").dependencies());

        // Agents depend on Planning.
        assertEquals(List.of("PLANNING"), graph.nodeById("AGENTS").dependencies());

        // Orchestration depends on the terminal node of the main chain (Execution).
        assertEquals(List.of("EXECUTION"), graph.nodeById("ORCHESTRATION").dependencies());
    }

    @Test
    void developerRequestGeneratesEdgesForEveryDependency() {
        ExecutionGraph graph = builder.build(new DefaultCapabilityResolver().resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .build()));

        int dependencyCount = graph.nodes().stream()
                .mapToInt(node -> node.dependencies().size())
                .sum();

        assertEquals(dependencyCount, graph.edges().size(),
                "Every dependency must be represented by exactly one edge");

        // Spot-check one deterministic edge: REASONING depends on KNOWLEDGE.
        ExecutionEdge reasoningEdge = graph.edges().stream()
                .filter(e -> "edge-REASONING-KNOWLEDGE".equals(e.edgeId()))
                .findFirst()
                .orElse(null);
        assertNotNull(reasoningEdge);
        assertEquals("REASONING", reasoningEdge.sourceNodeId());
        assertEquals("KNOWLEDGE", reasoningEdge.targetNodeId());
        assertTrue(reasoningEdge.reason().contains("depends on"));
    }

    // ========================================================================
    // Governance interceptors replace graph nodes
    // ========================================================================

    @Test
    void governanceInterceptorsAreNotGraphNodes() {
        // Safety, Validation, and Observability are runtime interceptors
        // and are never graph nodes.
        ExecutionGraph graph = builder.build(new DefaultCapabilityResolver().resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .build()));
        assertNull(graph.nodeById("SAFETY"));
        assertNull(graph.nodeById("VALIDATION"));
        assertNull(graph.nodeById("OBSERVABILITY"));
    }

    // ========================================================================
    // Reasoning depends on Knowledge and/or Memory
    // ========================================================================

    @Test
    void reasoningDependsOnMemoryWhenOnlyMemoryPresent() {
        ExecutionGraph graph = builder.build(plan(
                CapabilityType.IDENTITY,
                CapabilityType.MEMORY,
                CapabilityType.REASONING));

        assertEquals(List.of("MEMORY"), graph.nodeById("REASONING").dependencies());
    }

    @Test
    void reasoningDependsOnBothKnowledgeAndMemoryWhenPresent() {
        // The graph rule is "Reasoning depends on Knowledge and/or Memory" —
        // both dependencies materialize only when both capabilities are present.
        // The DEVELOPER intent plan (used elsewhere in this class) contains only
        // KNOWLEDGE, so it is exercised with an explicit plan here.
        ExecutionGraph graph = builder.build(plan(
                CapabilityType.IDENTITY,
                CapabilityType.KNOWLEDGE,
                CapabilityType.MEMORY,
                CapabilityType.REASONING));

        List<String> reasoningDeps = graph.nodeById("REASONING").dependencies();
        assertEquals(2, reasoningDeps.size());
        assertTrue(reasoningDeps.contains("KNOWLEDGE"));
        assertTrue(reasoningDeps.contains("MEMORY"));
    }

    // ========================================================================
    // Memory depends on Context; falls back to Identity when Context absent
    // ========================================================================

    @Test
    void memoryDependsOnContextWhenContextAbsent() {
        ExecutionGraph graph = builder.build(plan(
                CapabilityType.IDENTITY,
                CapabilityType.MEMORY));

        assertEquals(List.of("IDENTITY"), graph.nodeById("MEMORY").dependencies());
    }

    @Test
    void safetyRemainsInterceptorNotGraphNode() {
        // SAFETY is a runtime interceptor, never a graph node. Supplying it in
        // the plan must NOT insert it into the graph nor alter EXECUTION deps;
        // the newer dependency chain (Governance) simply falls back to the
        // previous capability in the chain.
        ExecutionGraph graph = builder.build(plan(
                CapabilityType.IDENTITY,
                CapabilityType.PLANNING,
                CapabilityType.EXECUTION,
                CapabilityType.SAFETY));

        assertNull(graph.nodeById("SAFETY"),
                "SAFETY must never become a graph node under the interceptor model");
        assertEquals(List.of("PLANNING"), graph.nodeById("EXECUTION").dependencies());
    }

    @Test
    void executionDependsOnPlanningWhenSafetyAbsent() {
        ExecutionGraph graph = builder.build(plan(
                CapabilityType.IDENTITY,
                CapabilityType.PLANNING,
                CapabilityType.EXECUTION));

        assertEquals(List.of("PLANNING"), graph.nodeById("EXECUTION").dependencies());
    }

    // ========================================================================
    // Identity is always root — even when the plan omits it
    // ========================================================================

    @Test
    void identityForcedAsRootWhenPlanOmitsIt() {
        CapabilityPlan plan = plan(CapabilityType.MEMORY);
        ExecutionGraph graph = builder.build(plan);

        assertEquals(2, graph.nodes().size());
        assertEquals("IDENTITY", graph.rootNode().nodeId());
        assertNotNull(graph.nodeById("IDENTITY"));
        assertEquals(List.of("IDENTITY"), graph.nodeById("MEMORY").dependencies());
        assertTrue(graph.contains(CapabilityType.IDENTITY));
        assertNull(graph.nodeById("NONEXISTENT"));
    }

    // ========================================================================
    // Minimal graph — Identity only
    // ========================================================================

    @Test
    void minimalPlanBuildsIdentityOnlyGraph() {
        ExecutionGraph graph = builder.build(plan(CapabilityType.IDENTITY));

        assertEquals(1, graph.nodes().size());
        assertEquals("IDENTITY", graph.rootNode().nodeId());
        assertTrue(graph.rootNode().dependencies().isEmpty());
        assertTrue(graph.edges().isEmpty());
        assertEquals("DEVELOPER", graph.metadata().get("detectedIntent"));
        assertEquals("ExecutionGraphBuilder", graph.metadata().get("source"));
        assertEquals(1, graph.metadata().get("nodeCount"));
        assertEquals(0, graph.metadata().get("edgeCount"));
    }

    // ========================================================================
    // Guardrails
    // ========================================================================

    @Test
    void nullPlanRejected() {
        assertThrows(NullPointerException.class, () -> builder.build(null));
    }

    @Test
    void allNodeStatesArePending() {
        ExecutionGraph graph = builder.build(new DefaultCapabilityResolver().resolve(SDKRequest.builder()
                .message("Implement a class that sorts a list")
                .build()));

        for (ExecutionNode node : graph.nodes()) {
            assertEquals(ExecutionNode.State.PENDING, node.state(),
                    "Graph is a planning artifact — nodes must stay PENDING");
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static CapabilityPlan plan(CapabilityType... capabilities) {
        CapabilityPlan.Builder planBuilder = CapabilityPlan.builder()
                .requestId("req-test")
                .detectedIntent(
                        com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType.DEVELOPER)
                .confidence(0.9);
        for (CapabilityType capability : capabilities) {
            planBuilder.addRequirement(new CapabilityRequirement(
                    capability,
                    CapabilityRequirement.Priority.REQUIRED,
                    "test requirement"));
        }
        return planBuilder.build();
    }

    /**
     * Returns true when the capability is a governance interceptor that the
     * builder must exclude from the graph (they run in the runtime executor's
     * interceptor chain, not as graph nodes).
     */
    private static boolean isGovernance(CapabilityType capability) {
        return capability == CapabilityType.SAFETY
                || capability == CapabilityType.VALIDATION
                || capability == CapabilityType.OBSERVABILITY;
    }
}
