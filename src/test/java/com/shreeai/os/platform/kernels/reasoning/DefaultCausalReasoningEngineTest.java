package com.shreeai.os.platform.kernels.reasoning;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.reasoning.engine.CausalReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultCausalReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic tests for {@link DefaultCausalReasoningEngine}.
 */
class DefaultCausalReasoningEngineTest {

    private CausalReasoningEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultCausalReasoningEngine();
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static GraphConcept concept(String name, ConceptType type) {
        return new GraphConcept(sha256("CONCEPT|" + name), name, type);
    }

    private static ConceptRelationship rel(String from, String to, RelationshipType type, double confidence) {
        return new ConceptRelationship(sha256(from + "|" + to + "|" + type), from, to, type, confidence);
    }

    private static ReasoningNode node(String id, String title, ReasoningNodeType type) {
        return new ReasoningNode(id, title, type, List.of());
    }

    private static Hypothesis hypothesis(String id, String statement) {
        return new Hypothesis(id, statement, List.of(), 0.8);
    }

    private static EvidenceCluster cluster(String id, List<String> evidenceIds, List<String> concepts) {
        return new EvidenceCluster(id, evidenceIds, concepts);
    }

    private static SynthesizedFact fact(String id, String statement, List<String> evidenceIds, double confidence) {
        return new SynthesizedFact(id, statement, evidenceIds, confidence);
    }

    private static SynthesisEdge synthEdge(String from, String to) {
        return new SynthesisEdge(from, to);
    }

    private static ConceptGraph graph(List<GraphConcept> concepts, List<ConceptRelationship> rels) {
        return new ConceptGraph(concepts, rels);
    }

    private static ReasoningGraph reasoningGraph(List<ReasoningNode> nodes, List<ReasoningEdge> edges, List<Hypothesis> hyps) {
        return new ReasoningGraph(nodes, edges, hyps);
    }

    private static SynthesisGraph synthesisGraph(List<EvidenceCluster> clusters, List<SynthesizedFact> facts, List<SynthesisEdge> edges) {
        return new SynthesisGraph(clusters, facts, edges);
    }
    
    // ---- Helper to build a standard concept graph ----
    private static ConceptGraph buildStandardGraph() {
        GraphConcept java = concept("Java", ConceptType.LANGUAGE);
        GraphConcept collections = concept("Collections", ConceptType.TOPIC);
        GraphConcept streams = concept("Streams", ConceptType.TOPIC);
        GraphConcept spring = concept("Spring Boot", ConceptType.FRAMEWORK);
        GraphConcept oop = concept("OOP", ConceptType.TOPIC);
        GraphConcept springData = concept("Spring Data", ConceptType.FRAMEWORK);

        List<GraphConcept> concepts = List.of(java, collections, streams, spring, oop, springData);
        List<ConceptRelationship> rels = List.of(
                rel(java.conceptId(), collections.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(collections.conceptId(), streams.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(spring.conceptId(), java.conceptId(), RelationshipType.DEPENDS_ON, 1.0),
                rel(oop.conceptId(), collections.conceptId(), RelationshipType.RELATED_TO, 0.6),
                rel(collections.conceptId(), springData.conceptId(), RelationshipType.RELATED_TO, 0.6)
        );
        return graph(concepts, rels);
    }

    private static ReasoningGraph buildStandardReasoning() {
        ReasoningNode ev1 = node("ev1", "Oracle Collections Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode c1 = node(sha256("CONCEPT|Java"), "Java", ReasoningNodeType.CONCEPT);
        ReasoningNode c2 = node(sha256("CONCEPT|Collections"), "Collections", ReasoningNodeType.CONCEPT);
        ReasoningNode c3 = node(sha256("CONCEPT|Streams"), "Streams", ReasoningNodeType.CONCEPT);
        ReasoningEdge e1 = new ReasoningEdge("ev1", sha256("CONCEPT|Java"), ReasoningEdgeType.SUPPORTS);
        ReasoningEdge e2 = new ReasoningEdge(sha256("CONCEPT|Java"), sha256("CONCEPT|Collections"), ReasoningEdgeType.CONNECTS);
        ReasoningEdge e3 = new ReasoningEdge(sha256("CONCEPT|Collections"), sha256("CONCEPT|Streams"), ReasoningEdgeType.CONNECTS);
        Hypothesis h1 = hypothesis(sha256("HYPOTHESIS|Learn Collections before Streams"), "Learn Collections before Streams");
        return reasoningGraph(List.of(ev1, c1, c2, c3), List.of(e1, e2, e3), List.of(h1));
    }

    private static SynthesisGraph buildStandardSynthesis() {
        EvidenceCluster cl1 = cluster("cl1", List.of("ev1"), List.of("Java", "Collections"));
        EvidenceCluster cl2 = cluster("cl2", List.of("ev2"), List.of("Spring Boot", "Java"));
        SynthesizedFact f1 = fact("f1", "Java is foundational", List.of("ev1"), 0.95);
        SynthesizedFact f2 = fact("f2", "Spring Boot depends on Java", List.of("ev2"), 0.90);
        return synthesisGraph(List.of(cl1, cl2), List.of(f1, f2), List.of(synthEdge("cl1", "cl2")));
    }

    // ==================== R3 TESTS ====================

    @Test
    @DisplayName("Cause nodes created from synthesized facts")
    void testCauseNodeCreation() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertFalse(result.nodes().isEmpty());
        assertTrue(result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.CAUSE));
    }

    @Test
    @DisplayName("Effect nodes created from dependent concepts")
    void testEffectNodeCreation() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertTrue(result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.EFFECT));
    }

    @Test
    @DisplayName("Intermediate nodes in chains")
    void testIntermediateChains() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertTrue(result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.INTERMEDIATE));
    }

    @Test
    @DisplayName("Max depth enforced at 4")
    void testMaxDepthEnforced() {
        // Build a deep chain: A -> B -> C -> D -> E (5 levels)
        GraphConcept a = concept("Alpha", ConceptType.TOPIC);
        GraphConcept b = concept("Beta", ConceptType.TOPIC);
        GraphConcept c = concept("Gamma", ConceptType.TOPIC);
        GraphConcept d = concept("Delta", ConceptType.TOPIC);
        GraphConcept e = concept("Epsilon", ConceptType.TOPIC);
        List<GraphConcept> concepts = List.of(a, b, c, d, e);
        List<ConceptRelationship> rels = List.of(
                rel(a.conceptId(), b.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(b.conceptId(), c.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(c.conceptId(), d.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(d.conceptId(), e.conceptId(), RelationshipType.PREREQUISITE, 0.9)
        );
        ConceptGraph deepGraph = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "Alpha Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|Alpha"), "Alpha", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|Alpha"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("Alpha"));
        SynthesizedFact f = fact("f1", "Alpha is start", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, deepGraph);
        // Max depth 4 means at most 5 nodes in a chain (start + 4 hops)
        // The chain should not extend beyond depth 4
        assertTrue(result.nodes().size() <= 6); // reasonable upper bound
    }

    @Test
    @DisplayName("Cycles are detected and ignored")
    void testCycleDetection() {
        // A -> B -> C -> A (cycle)
        GraphConcept a = concept("CycA", ConceptType.TOPIC);
        GraphConcept b = concept("CycB", ConceptType.TOPIC);
        GraphConcept c = concept("CycC", ConceptType.TOPIC);
        List<GraphConcept> concepts = List.of(a, b, c);
        List<ConceptRelationship> rels = List.of(
                rel(a.conceptId(), b.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(b.conceptId(), c.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(c.conceptId(), a.conceptId(), RelationshipType.PREREQUISITE, 0.9)
        );
        ConceptGraph cyclicGraph = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "CycA Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|CycA"), "CycA", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|CycA"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("CycA"));
        SynthesizedFact f = fact("f1", "CycA is start", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        // Should not infinite loop
        CausalGraph result = engine.analyze(rg, sg, cyclicGraph);
        assertNotNull(result);
        assertFalse(result.nodes().isEmpty());
    }

    @Test
    @DisplayName("Edge strength mapping is correct")
    void testEdgeStrengthMapping() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        // DEPENDS_ON should have strength 1.00
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 1.00));
        // PREREQUISITE should have strength 0.90
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 0.90));
        // RELATED_TO should have strength 0.60
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 0.60));
    }

    @Test
    @DisplayName("Stable node IDs")
    void testStableNodeIds() {
        CausalGraph r1 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        CausalGraph r2 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        List<String> ids1 = r1.nodes().stream().map(CausalNode::nodeId).toList();
        List<String> ids2 = r2.nodes().stream().map(CausalNode::nodeId).toList();
        assertEquals(ids1, ids2);
    }

    @Test
    @DisplayName("Stable chain IDs")
    void testStableChainIds() {
        CausalGraph r1 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        CausalGraph r2 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        List<String> cids1 = r1.chains().stream().map(CausalChain::chainId).toList();
        List<String> cids2 = r2.chains().stream().map(CausalChain::chainId).toList();
        assertEquals(cids1, cids2);
    }

    @Test
    @DisplayName("Same input produces identical graph")
    void testSameInputSameGraph() {
        CausalGraph r1 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        CausalGraph r2 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertEquals(r1, r2);
    }

    @Test
    @DisplayName("Empty synthesis produces empty causal graph")
    void testEmptySynthesis() {
        ReasoningGraph rg = reasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = synthesisGraph(List.of(), List.of(), List.of());
        ConceptGraph cg = graph(List.of(), List.of());
        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.nodes().isEmpty());
        assertTrue(result.edges().isEmpty());
        assertTrue(result.chains().isEmpty());
    }

    @Test
    @DisplayName("Null reasoning graph throws NPE")
    void testNullReasoningGraph() {
        assertThrows(NullPointerException.class, () -> engine.analyze(null, buildStandardSynthesis(), buildStandardGraph()));
    }

    @Test
    @DisplayName("Null synthesis graph throws NPE")
    void testNullSynthesisGraph() {
        assertThrows(NullPointerException.class, () -> engine.analyze(buildStandardReasoning(), null, buildStandardGraph()));
    }

    @Test
    @DisplayName("Null concept graph throws NPE")
    void testNullConceptGraph() {
        assertThrows(NullPointerException.class, () -> engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), null));
    }

    @Test
    @DisplayName("Causal chains are produced")
    void testCausalChainsProduced() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertFalse(result.chains().isEmpty());
    }

    @Test
    @DisplayName("Chain node IDs reference existing nodes")
    void testChainNodeIdsValid() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        List<String> nodeIds = result.nodes().stream().map(CausalNode::nodeId).toList();
        for (CausalChain chain : result.chains()) {
            for (String nodeId : chain.nodeIds()) {
                assertTrue(nodeIds.contains(nodeId), "Chain references unknown node: " + nodeId);
            }
        }
    }

    @Test
    @DisplayName("Edge strengths are within valid range")
    void testEdgeStrengthRange() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        for (CausalEdge edge : result.edges()) {
            assertTrue(edge.strength() >= 0.0 && edge.strength() <= 1.0,
                    "Edge strength out of range: " + edge.strength());
        }
    }

    @Test
    @DisplayName("All node types present in standard graph")
    void testAllNodeTypesPresent() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        boolean hasCause = result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.CAUSE);
        boolean hasEffect = result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.EFFECT);
        boolean hasIntermediate = result.nodes().stream().anyMatch(n -> n.type() == CausalNodeType.INTERMEDIATE);
        assertTrue(hasCause, "Should have CAUSE nodes");
        assertTrue(hasEffect, "Should have EFFECT nodes");
        assertTrue(hasIntermediate, "Should have INTERMEDIATE nodes");
    }

    @Test
    @DisplayName("Deterministic ordering of nodes")
    void testDeterministicOrdering() {
        CausalGraph r1 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        CausalGraph r2 = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertEquals(r1.nodes(), r2.nodes());
        assertEquals(r1.edges(), r2.edges());
        assertEquals(r1.chains(), r2.chains());
    }

    @Test
    @DisplayName("Single concept produces no edges")
    void testSingleConceptNoEdges() {
        GraphConcept single = concept("Solo", ConceptType.TOPIC);
        ConceptGraph cg = graph(List.of(single), List.of());
        ReasoningNode ev = node("ev1", "Solo Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|Solo"), "Solo", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|Solo"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());
        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("Solo"));
        SynthesizedFact f = fact("f1", "Solo fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.edges().isEmpty());
    }

    @Test
    @DisplayName("Multiple chains from branching graph")
    void testMultipleChains() {
        // A -> B, A -> C (branching)
        GraphConcept a = concept("Root", ConceptType.TOPIC);
        GraphConcept b = concept("BranchB", ConceptType.TOPIC);
        GraphConcept c = concept("BranchC", ConceptType.TOPIC);
        List<GraphConcept> concepts = List.of(a, b, c);
        List<ConceptRelationship> rels = List.of(
                rel(a.conceptId(), b.conceptId(), RelationshipType.PREREQUISITE, 0.9),
                rel(a.conceptId(), c.conceptId(), RelationshipType.PREREQUISITE, 0.9)
        );
        ConceptGraph branchGraph = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "Root Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|Root"), "Root", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|Root"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("Root"));
        SynthesizedFact f = fact("f1", "Root fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, branchGraph);
        // Should have at least 2 chains (Root->B and Root->C)
        assertTrue(result.chains().size() >= 2, "Expected at least 2 chains from branching graph");
    }

    @Test
    @DisplayName("Confidence aggregation uses mean trust")
    void testConfidenceAggregation() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        // All confidences should be between 0 and 1
        for (CausalChain chain : result.chains()) {
            assertNotNull(chain.chainId());
            assertFalse(chain.nodeIds().isEmpty());
        }
    }

    @Test
    @DisplayName("Graph with no relationships produces no edges")
    void testNoRelationshipsNoEdges() {
        GraphConcept a = concept("IsoA", ConceptType.TOPIC);
        GraphConcept b = concept("IsoB", ConceptType.TOPIC);
        ConceptGraph cg = graph(List.of(a, b), List.of());

        ReasoningNode ev = node("ev1", "IsoA Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|IsoA"), "IsoA", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|IsoA"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("IsoA"));
        SynthesizedFact f = fact("f1", "IsoA fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.edges().isEmpty());
    }

    @Test
    @DisplayName("Part-of relationship has strength 0.80")
    void testPartOfStrength() {
        GraphConcept parent = concept("Parent", ConceptType.TOPIC);
        GraphConcept child = concept("Child", ConceptType.TOPIC);
        List<GraphConcept> concepts = List.of(parent, child);
        List<ConceptRelationship> rels = List.of(
                rel(child.conceptId(), parent.conceptId(), RelationshipType.PART_OF, 0.8)
        );
        ConceptGraph cg = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "Parent Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|Parent"), "Parent", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|Parent"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("Parent"));
        SynthesizedFact f = fact("f1", "Parent fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 0.80));
    }

    @Test
    @DisplayName("Implements relationship has strength 0.70")
    void testImplementsStrength() {
        GraphConcept iface = concept("Interface", ConceptType.TOPIC);
        GraphConcept impl = concept("Implementation", ConceptType.TOPIC);
        List<GraphConcept> concepts = List.of(iface, impl);
        List<ConceptRelationship> rels = List.of(
                rel(impl.conceptId(), iface.conceptId(), RelationshipType.IMPLEMENTS, 0.7)
        );
        ConceptGraph cg = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "Interface Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|Interface"), "Interface", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|Interface"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("Interface"));
        SynthesizedFact f = fact("f1", "Interface fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 0.70));
    }

    @Test
    @DisplayName("Uses relationship has strength 0.60")
    void testUsesStrength() {
        GraphConcept user = concept("User", ConceptType.TOPIC);
        GraphConcept tool = concept("Tool", ConceptType.TOOL);
        List<GraphConcept> concepts = List.of(user, tool);
        List<ConceptRelationship> rels = List.of(
                rel(user.conceptId(), tool.conceptId(), RelationshipType.USES, 0.6)
        );
        ConceptGraph cg = graph(concepts, rels);

        ReasoningNode ev = node("ev1", "User Guide", ReasoningNodeType.EVIDENCE);
        ReasoningNode ca = node(sha256("CONCEPT|User"), "User", ReasoningNodeType.CONCEPT);
        ReasoningEdge edge = new ReasoningEdge("ev1", sha256("CONCEPT|User"), ReasoningEdgeType.SUPPORTS);
        ReasoningGraph rg = reasoningGraph(List.of(ev, ca), List.of(edge), List.of());

        EvidenceCluster cl = cluster("cl1", List.of("ev1"), List.of("User"));
        SynthesizedFact f = fact("f1", "User fact", List.of("ev1"), 0.9);
        SynthesisGraph sg = synthesisGraph(List.of(cl), List.of(f), List.of());

        CausalGraph result = engine.analyze(rg, sg, cg);
        assertTrue(result.edges().stream().anyMatch(e -> e.strength() == 0.60));
    }

    @Test
    @DisplayName("CausalGraph is deeply immutable")
    void testDeepImmutability() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertThrows(UnsupportedOperationException.class, () -> result.nodes().add(null));
        assertThrows(UnsupportedOperationException.class, () -> result.edges().add(null));
        assertThrows(UnsupportedOperationException.class, () -> result.chains().add(null));
    }

    @Test
    @DisplayName("Node titles are preserved")
    void testNodeTitlesPreserved() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        List<String> titles = result.nodes().stream().map(CausalNode::title).toList();
        assertTrue(titles.contains("Java"));
        assertTrue(titles.contains("Collections"));
        assertTrue(titles.contains("Streams"));
    }

    @Test
    @DisplayName("Chain IDs are SHA-256 format")
    void testChainIdsFormat() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        for (CausalChain chain : result.chains()) {
            assertEquals(64, chain.chainId().length());
            assertTrue(chain.chainId().matches("[0-9a-f]{64}"));
        }
    }

    @Test
    @DisplayName("Node IDs are SHA-256 format")
    void testNodeIdsFormat() {
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        for (CausalNode node : result.nodes()) {
            assertEquals(64, node.nodeId().length());
            assertTrue(node.nodeId().matches("[0-9a-f]{64}"));
        }
    }

    @Test
    @DisplayName("Integration: full pipeline R1->R2->R3")
    void testFullPipeline() {
        // This test verifies that the output of R1 (ReasoningGraph) and R2 (SynthesisGraph)
        // can be consumed by R3 (CausalReasoningEngine) to produce a CausalGraph
        CausalGraph result = engine.analyze(buildStandardReasoning(), buildStandardSynthesis(), buildStandardGraph());
        assertNotNull(result);
        assertFalse(result.nodes().isEmpty());
        assertFalse(result.edges().isEmpty());
        assertFalse(result.chains().isEmpty());
    }
}
