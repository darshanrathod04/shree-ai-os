package com.shreeai.os.platform.kernels.reasoning;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.MultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdge;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningEdgeType;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic tests for {@link DefaultMultiHopReasoningEngine}.
 */
class DefaultMultiHopReasoningEngineTest {

    private static final TrustScore TRUST_0_9 = new TrustScore(0.9, 1.0, 0.9, 0.8, "official");
    private static final TrustScore TRUST_0_6 = new TrustScore(0.6, 0.7, 0.7, 0.5, "community");

    private static TrustedEvidence evidence(String chunkId, String docId, String content) {
        return new TrustedEvidence(
                new EvidenceItem(chunkId, docId, content, 0.9, List.of()),
                TRUST_0_9);
    }

    private static TrustedEvidence evidenceWithTrust(String chunkId, String docId,
                                                     String content, TrustScore trust) {
        return new TrustedEvidence(
                new EvidenceItem(chunkId, docId, content, 0.9, List.of()),
                trust);
    }

    private static GraphConcept concept(String name) {
        return new GraphConcept(sha256("CONCEPT|" + name), name, ConceptType.TECHNOLOGY);
    }

    private static ConceptRelationship rel(String from, String to) {
        return new ConceptRelationship(sha256(from + "|" + to), from, to, RelationshipType.RELATED_TO, 0.8);
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final MultiHopReasoningEngine engine = new DefaultMultiHopReasoningEngine();

    @Test
    void engineIsStatelessAndDeterministic() {
        ReliabilityResult r1 = buildSampleResult();
        ConceptGraph g1 = buildSampleGraph();
        ReasoningGraph graph1 = engine.reason(r1, g1);
        ReasoningGraph graph2 = engine.reason(r1, g1);
        assertEquals(graph1, graph2);
    }

    @Test
    void nodesAreImmutable() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);
        assertThrows(UnsupportedOperationException.class,
                () -> graph.nodes().add(null));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.edges().add(null));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.hypotheses().add(null));
    }

    @Test
    void evidenceNodesCreatedFromTrustedEvidence() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long evidenceCount = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.EVIDENCE)
                .count();
        assertEquals(2, evidenceCount);
    }

    @Test
    void conceptNodesCreatedFromGraphConcepts() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long conceptCount = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.CONCEPT)
                .count();
        // Java, Collections, Streams, Spring are in evidence or reachable
        assertTrue(conceptCount >= 2);
    }

    @Test
    void supportsEdgesLinkEvidenceToConcepts() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long supportsCount = graph.edges().stream()
                .filter(e -> e.type() == ReasoningEdgeType.SUPPORTS)
                .count();
        assertTrue(supportsCount >= 1);
    }

    @Test
    void connectsEdgesCreatedDuringMultiHopTraversal() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long connectsCount = graph.edges().stream()
                .filter(e -> e.type() == ReasoningEdgeType.CONNECTS)
                .count();
        // Java -> Collections -> Streams is at least 2 CONNECTS edges
        assertTrue(connectsCount >= 2);
    }

    @Test
    void maxHopDepthIsThree() {
        // Java -> Collections -> Streams -> (no further outgoing)
        // The depth limit is enforced by the engine constant
        assertEquals(3, DefaultMultiHopReasoningEngine.MAX_HOP_DEPTH);
    }

    @Test
    void cyclesIgnoredNoInfiniteLoop() {
        // Add a cycle: Collections -> Java -> Collections ...
        GraphConcept java = concept("Java");
        GraphConcept collections = concept("Collections");
        ConceptGraph cyclicGraph = new ConceptGraph(
                List.of(java, collections),
                List.of(
                        rel(java.conceptId(), collections.conceptId()),
                        rel(collections.conceptId(), java.conceptId())  // cycle
                ));

        ReliabilityResult r = new ReliabilityResult(
                List.of(evidence("chunk-1", "doc-1", "Learn Java and Collections")),
                0.9);

        ReasoningGraph graph = engine.reason(r, cyclicGraph);
        // Must terminate without stack overflow
        assertNotNull(graph);
        // No duplicate CONNECTS edges between same pair
        long distinctEdges = graph.edges().stream()
                .map(e -> e.fromNode() + "->" + e.toNode())
                .distinct()
                .count();
        assertEquals(graph.edges().size(), distinctEdges);
    }

    @Test
    void hypothesisGeneratedFromChain() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        assertFalse(graph.hypotheses().isEmpty());
        Hypothesis h = graph.hypotheses().get(0);
        assertTrue(h.statement().contains("Learn"));
        assertTrue(h.statement().contains("before"));
    }

    @Test
    void hypothesisNodeCreatedForEachChain() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long hypothesisNodeCount = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.HYPOTHESIS)
                .count();
        assertEquals(graph.hypotheses().size(), hypothesisNodeCount);
    }

    @Test
    void leadsToEdgeConnectsLastConceptToHypothesis() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        long leadsToCount = graph.edges().stream()
                .filter(e -> e.type() == ReasoningEdgeType.LEADS_TO)
                .count();
        assertTrue(leadsToCount >= 1);
    }

    @Test
    void stableNodeIdsDeterministic() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        // All node IDs are SHA-256 hex strings (64 chars)
        for (ReasoningNode node : graph.nodes()) {
            assertEquals(64, node.nodeId().length());
            assertTrue(node.nodeId().matches("[0-9a-f]{64}"));
        }
    }

    @Test
    void stableEdgeIdsDerivedFromEndpoints() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        // Edges should have non-null fromNode and toNode
        for (ReasoningEdge edge : graph.edges()) {
            assertNotNull(edge.fromNode());
            assertNotNull(edge.toNode());
            assertNotEquals(0, edge.fromNode().length());
            assertNotEquals(0, edge.toNode().length());
        }
    }

    @Test
    void sameInputProducesSameOutput() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();

        ReasoningGraph g1 = engine.reason(r, g);
        ReasoningGraph g2 = engine.reason(r, g);

        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());
    }

    @Test
    void originalContentPreservedInEvidenceNodes() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        // Evidence nodes should contain the original text
        boolean foundOriginal = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.EVIDENCE)
                .anyMatch(n -> n.title().contains("Collections"));
        assertTrue(foundOriginal);
    }

    @Test
    void confidenceBasedOnTrustScores() {
        ReliabilityResult r = new ReliabilityResult(
                List.of(
                        evidenceWithTrust("chunk-1", "doc-1",
                                "Learn Java", new TrustScore(1.0, 1.0, 1.0, 1.0, "official"))
                ),
                1.0);
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        // Hypothesis confidence should reflect trust
        assertFalse(graph.hypotheses().isEmpty());
        double confidence = graph.hypotheses().get(0).confidence();
        assertTrue(confidence > 0.0 && confidence <= 1.0);
    }

    @Test
    void emptyEvidenceProducesEmptyGraph() {
        ReliabilityResult r = new ReliabilityResult(List.of(), 0.0);
        ConceptGraph g = buildSampleGraph();
        ReasoningGraph graph = engine.reason(r, g);

        assertTrue(graph.nodes().isEmpty());
        assertTrue(graph.edges().isEmpty());
        assertTrue(graph.hypotheses().isEmpty());
    }

    @Test
    void emptyGraphProducesNoConceptsOrEdges() {
        ReliabilityResult r = buildSampleResult();
        ConceptGraph g = new ConceptGraph(List.of(), List.of());
        ReasoningGraph graph = engine.reason(r, g);

        // Evidence nodes exist, but no concept/hypothesis nodes
        long conceptCount = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.CONCEPT)
                .count();
        long hypothesisCount = graph.nodes().stream()
                .filter(n -> n.type() == ReasoningNodeType.HYPOTHESIS)
                .count();
        assertEquals(0, conceptCount);
        assertEquals(0, hypothesisCount);
        assertTrue(graph.edges().isEmpty());
    }

    @Test
    void nullReliabilityThrowsNullPointerException() {
        ConceptGraph g = buildSampleGraph();
        assertThrows(NullPointerException.class,
                () -> engine.reason(null, g));
    }

    @Test
    void nullConceptGraphThrowsNullPointerException() {
        ReliabilityResult r = buildSampleResult();
        assertThrows(NullPointerException.class,
                () -> engine.reason(r, null));
    }

    // ---- helper methods ----

    private ReliabilityResult buildSampleResult() {
        return new ReliabilityResult(
                List.of(
                        evidence("chunk-1", "doc-1",
                                "Learn Java Collections before Streams"),
                        evidence("chunk-2", "doc-1",
                                "Spring Boot uses Java and Collections")
                ),
                0.9);
    }

    private ConceptGraph buildSampleGraph() {
        GraphConcept java = concept("Java");
        GraphConcept collections = concept("Collections");
        GraphConcept streams = concept("Streams");
        GraphConcept spring = concept("Spring Boot");

        return new ConceptGraph(
                List.of(java, collections, streams, spring),
                List.of(
                        rel(java.conceptId(), collections.conceptId()),
                        rel(collections.conceptId(), streams.conceptId()),
                        rel(spring.conceptId(), java.conceptId()),
                        rel(spring.conceptId(), collections.conceptId())
                ));
    }
}
