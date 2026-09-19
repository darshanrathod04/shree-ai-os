package com.shreeai.os.platform.kernels.reasoning;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultEvidenceSynthesisEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultCausalReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultSelfVerificationEngine;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalChain;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationIssue;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationIssueType;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic unit tests for {@link DefaultSelfVerificationEngine}.
 */
class DefaultSelfVerificationEngineTest {

    private static String sha256(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static ConceptGraph buildStandardConceptGraph() {
        String javaId = sha256("CONCEPT|Java");
        String collId = sha256("CONCEPT|Collections");
        String streamsId = sha256("CONCEPT|Streams");
        List<GraphConcept> concepts = List.of(
                new GraphConcept(javaId, "Java", ConceptType.LANGUAGE),
                new GraphConcept(collId, "Collections", ConceptType.TOPIC),
                new GraphConcept(streamsId, "Streams", ConceptType.TOPIC)
        );
        List<ConceptRelationship> rels = List.of(
                new ConceptRelationship(sha256("REL|Java|PREREQUISITE|Collections"),
                        javaId, collId, RelationshipType.PREREQUISITE, 0.95),
                new ConceptRelationship(sha256("REL|Collections|PREREQUISITE|Streams"),
                        collId, streamsId, RelationshipType.PREREQUISITE, 0.95)
        );
        return new ConceptGraph(concepts, rels);
    }

        private static EvidenceItem evidenceItem(String chunkId, String docId, String content, double score) {
        return new EvidenceItem(chunkId, docId, content, score, List.of());
    }

    private static TrustedEvidence trustedEvidence(String chunkId, String docId, String content, double trust) {
        EvidenceItem item = evidenceItem(chunkId, docId, content, trust);
        TrustScore ts = new TrustScore(trust, trust, trust, trust, "test");
        return new TrustedEvidence(item, ts);
    }

    private static ReliabilityResult buildStandardReliability() {
        List<TrustedEvidence> evidence = new ArrayList<>();
        evidence.add(trustedEvidence("chunk-1", "doc-1", "Java is a programming language. Collections framework provides data structures.", 0.95));
        evidence.add(trustedEvidence("chunk-2", "doc-2", "Java Collections is a prerequisite for Streams API.", 0.90));
        return new ReliabilityResult(evidence, 0.925);
    }

    private static ReasoningGraph buildStandardReasoningGraph(ReliabilityResult rel, ConceptGraph cg) {
        return new DefaultMultiHopReasoningEngine().reason(rel, cg);
    }

    private static SynthesisGraph buildStandardSynthesisGraph(ReasoningGraph rg, ReliabilityResult rel, ConceptGraph cg) {
        return new DefaultEvidenceSynthesisEngine().synthesize(rg, rel, cg);
    }

    private static CausalGraph buildStandardCausalGraph(ReasoningGraph rg, SynthesisGraph sg, ConceptGraph cg) {
        return new DefaultCausalReasoningEngine().analyze(rg, sg, cg);
    }

    private static DefaultSelfVerificationEngine engine(ConceptGraph cg) {
        return new DefaultSelfVerificationEngine(cg);
    }

    @Test
    void testEvidenceCoverageVerified() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().stream().noneMatch(i -> i.type() == VerificationIssueType.MISSING_EVIDENCE));
    }

    @Test
    void testHypothesisWithoutEvidenceDetected() {
        ConceptGraph cg = buildStandardConceptGraph();
        List<ReasoningNode> nodes = new ArrayList<>();
        List<Hypothesis> hypotheses = List.of(
                new Hypothesis(sha256("HYP|no-evidence"), "Test hypothesis",
                        List.of("nonexistent-chunk"), 0.9)
        );
        ReasoningGraph rg = new ReasoningGraph(nodes, List.of(), hypotheses);
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.MISSING_EVIDENCE));
        assertTrue(vg.nodes().stream().anyMatch(vn -> vn.status() == VerificationStatus.UNSUPPORTED));
    }

    @Test
    void testContradictionWhenCausalEdgeNotBackedByConceptGraph() {
        ConceptGraph cg = buildStandardConceptGraph();
        String nodeA = sha256("CONCEPT|Java");
        String nodeB = sha256("CONCEPT|NonExistent");
        CausalNode ca = new CausalNode(nodeA, "Java", CausalNodeType.CAUSE);
        CausalNode cb = new CausalNode(nodeB, "NonExistent", CausalNodeType.EFFECT);
        CausalEdge edge = new CausalEdge(nodeA, nodeB, 1.0);
        CausalChain chain = new CausalChain(sha256("CHAIN|test"), List.of(nodeA, nodeB));
        CausalGraph causal = new CausalGraph(List.of(ca, cb), List.of(edge), List.of(chain));
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.CONTRADICTION));
    }

    @Test
    void testNoContradictionForValidConceptGraphEdge() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertFalse(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.CONTRADICTION));
    }

    @Test
    void testIncompleteChainWhenFactNotInChain() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.INCOMPLETE_CHAIN));
    }

    @Test
    void testNoIncompleteChainWhenFactInChain() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertFalse(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.INCOMPLETE_CHAIN));
    }

    @Test
    void testVerificationScoreInRange() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.verificationScore() >= 0.0);
        assertTrue(vg.verificationScore() <= 1.0);
    }

    @Test
    void testScoreWithIssuesLowerThanPerfect() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.verificationScore() < 1.0);
        assertTrue(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.INCOMPLETE_CHAIN));
    }

    @Test
    void testStableVerificationGraph() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg1 = engine(cg).verify(rg, sg, causal);
        VerificationGraph vg2 = engine(cg).verify(rg, sg, causal);
        assertEquals(vg1, vg2);
        assertEquals(vg1.verificationScore(), vg2.verificationScore());
    }

    @Test
    void testDeterministicIds() {
        String id1 = sha256("CONCEPT|Java");
        String id2 = sha256("CONCEPT|Java");
        assertEquals(id1, id2);
    }

    @Test
    void testNodesContainVerifiedStatus() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().stream().anyMatch(vn -> vn.status() == VerificationStatus.VERIFIED));
    }

    @Test
    void testIssuesContainExplanation() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        for (VerificationIssue issue : vg.issues()) {
            assertFalse(issue.explanation().isBlank());
            assertTrue(issue.severity() > 0);
        }
    }

    @Test
    void testSeverityLockedValues() {
        assertEquals(1.00, VerificationIssueType.MISSING_EVIDENCE.severity(), 0.001);
        assertEquals(1.00, VerificationIssueType.UNSUPPORTED_HYPOTHESIS.severity(), 0.001);
        assertEquals(0.75, VerificationIssueType.CONTRADICTION.severity(), 0.001);
        assertEquals(0.50, VerificationIssueType.INCOMPLETE_CHAIN.severity(), 0.001);
        assertEquals(0.25, VerificationIssueType.LOW_TRUST.severity(), 0.001);
    }

    @Test
    void testTrustThresholdsLocked() {
        assertEquals(0.90, DefaultSelfVerificationEngine.TRUST_THRESHOLD_VERIFIED, 0.001);
        assertEquals(0.70, DefaultSelfVerificationEngine.TRUST_THRESHOLD_PARTIAL, 0.001);
    }

    @Test
    void testEmptyInputsProduceEmptyGraph() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().isEmpty());
        assertTrue(vg.issues().isEmpty());
        assertEquals(1.0, vg.verificationScore(), 0.001);
    }

    @Test
    void testNullReasoningGraphThrowsNPE() {
        ConceptGraph cg = buildStandardConceptGraph();
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        assertThrows(NullPointerException.class, () -> engine(cg).verify(null, sg, causal));
    }

    @Test
    void testNullSynthesisGraphThrowsNPE() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        assertThrows(NullPointerException.class, () -> engine(cg).verify(rg, null, causal));
    }

    @Test
    void testNullCausalGraphThrowsNPE() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        assertThrows(NullPointerException.class, () -> engine(cg).verify(rg, sg, null));
    }

    @Test
    void testEndToEndPipeline() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().size() > 0);
        assertTrue(vg.verificationScore() >= 0.0);
    }

    @Test
    void testMultipleSourcesSameScore() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel1 = buildStandardReliability();
        ReliabilityResult rel2 = buildStandardReliability();
        ReasoningGraph rg1 = buildStandardReasoningGraph(rel1, cg);
        SynthesisGraph sg1 = buildStandardSynthesisGraph(rg1, rel1, cg);
        CausalGraph causal1 = buildStandardCausalGraph(rg1, sg1, cg);
        VerificationGraph vg1 = engine(cg).verify(rg1, sg1, causal1);
        ReasoningGraph rg2 = buildStandardReasoningGraph(rel2, cg);
        SynthesisGraph sg2 = buildStandardSynthesisGraph(rg2, rel2, cg);
        CausalGraph causal2 = buildStandardCausalGraph(rg2, sg2, cg);
        VerificationGraph vg2 = engine(cg).verify(rg2, sg2, causal2);
        assertEquals(vg1, vg2);
        assertEquals(vg1.verificationScore(), vg2.verificationScore(), 0.0001);
    }

    @Test
    void testStandardPipelineHasNoIssues() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().isEmpty());
        assertEquals(1.0, vg.verificationScore(), 0.0001);
    }

    @Test
    void testLowTrustHypothesisDetected() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningNode ev = new ReasoningNode("ev-low", "Evidence content",
                ReasoningNodeType.EVIDENCE, List.of("chunk-low"));
        Hypothesis lowTrust = new Hypothesis("hyp-low", "Low trust hypothesis",
                List.of("chunk-low"), 0.55);
        ReasoningGraph rg = new ReasoningGraph(List.of(ev), List.of(), List.of(lowTrust));
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.LOW_TRUST));
        assertTrue(vg.nodes().stream()
                .anyMatch(n -> n.status() == VerificationStatus.PARTIALLY_VERIFIED));
        double severity = vg.issues().stream()
                .filter(i -> i.type() == VerificationIssueType.LOW_TRUST)
                .findFirst().orElseThrow().severity();
        assertEquals(0.25, severity, 0.001);
    }

    @Test
    void testPartiallyVerifiedThreshold() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningNode ev = new ReasoningNode("ev-partial", "Content",
                ReasoningNodeType.EVIDENCE, List.of("chunk-partial"));
        Hypothesis partial = new Hypothesis("hyp-partial", "Partial",
                List.of("chunk-partial"), 0.80);
        ReasoningGraph rg = new ReasoningGraph(List.of(ev), List.of(), List.of(partial));
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().stream()
                .anyMatch(n -> n.status() == VerificationStatus.PARTIALLY_VERIFIED));
        assertFalse(vg.issues().stream().anyMatch(i -> i.type() == VerificationIssueType.LOW_TRUST));
    }

    @Test
    void testBoundaryTrustExactlyNinetyVerified() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningNode ev = new ReasoningNode("ev-90", "Content",
                ReasoningNodeType.EVIDENCE, List.of("chunk-90"));
        Hypothesis h = new Hypothesis("hyp-90", "Boundary", List.of("chunk-90"), 0.90);
        ReasoningGraph rg = new ReasoningGraph(List.of(ev), List.of(), List.of(h));
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().stream().anyMatch(n -> n.status() == VerificationStatus.VERIFIED));
    }

    @Test
    void testBoundaryTrustBelowNinetyPartial() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningNode ev = new ReasoningNode("ev-89", "Content",
                ReasoningNodeType.EVIDENCE, List.of("chunk-89"));
        Hypothesis h = new Hypothesis("hyp-89", "Boundary", List.of("chunk-89"), 0.8999);
        ReasoningGraph rg = new ReasoningGraph(List.of(ev), List.of(), List.of(h));
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertTrue(vg.nodes().stream()
                .anyMatch(n -> n.status() == VerificationStatus.PARTIALLY_VERIFIED));
    }

    @Test
    void testVerificationScoreFormulaLocked() {
        // One verified + one unsupported hypothesis: issues = 1 (severity 1.0).
        // score = 1 - 1/(1 verified + 1.0 severity sum) = 0.5
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningNode ev = new ReasoningNode("ev-formula", "Content",
                ReasoningNodeType.EVIDENCE, List.of("chunk-formula"));
        Hypothesis supported = new Hypothesis("hyp-supported", "Supported",
                List.of("chunk-formula"), 0.95);
        Hypothesis unsupportedHyp = new Hypothesis("hyp-unsupported", "Unsupported",
                List.of("missing-chunk"), 0.5);
        ReasoningGraph rg = new ReasoningGraph(List.of(ev), List.of(),
                List.of(supported, unsupportedHyp));
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertEquals(0.5, vg.verificationScore(), 0.0001);
    }

    @Test
    void testStableNodeIdsAcrossRuns() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph v1 = engine(cg).verify(rg, sg, causal);
        VerificationGraph v2 = engine(cg).verify(rg, sg, causal);
        assertEquals(v1.nodes().stream().map(VerificationNode::nodeId).toList(),
                v2.nodes().stream().map(VerificationNode::nodeId).toList());
    }

    @Test
    void testStableIssueSeveritiesAcrossRuns() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph v1 = engine(cg).verify(rg, sg, causal);
        VerificationGraph v2 = engine(cg).verify(rg, sg, causal);
        assertEquals(v1.issues().stream().map(VerificationIssue::severity).toList(),
                v2.issues().stream().map(VerificationIssue::severity).toList());
    }

    @Test
    void testVerificationGraphImmutable() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReliabilityResult rel = buildStandardReliability();
        ReasoningGraph rg = buildStandardReasoningGraph(rel, cg);
        SynthesisGraph sg = buildStandardSynthesisGraph(rg, rel, cg);
        CausalGraph causal = buildStandardCausalGraph(rg, sg, cg);
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertThrows(UnsupportedOperationException.class,
                () -> vg.nodes().add(
                        new VerificationNode("x", VerificationStatus.VERIFIED, List.of())));
        assertThrows(UnsupportedOperationException.class,
                () -> vg.issues().add(new VerificationIssue(
                        VerificationIssueType.LOW_TRUST, "x", "e", 0.25)));
    }

    @Test
    void testNoHypothesesNoMissingIssues() {
        ConceptGraph cg = buildStandardConceptGraph();
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        CausalGraph causal = new CausalGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        assertFalse(vg.issues().stream()
                .anyMatch(i -> i.type() == VerificationIssueType.MISSING_EVIDENCE));
    }

    @Test
    void testContradictionIssueReferencesCausalEdgeNode() {
        ConceptGraph cg = buildStandardConceptGraph();
        String nodeA = sha256("CONCEPT|Java");
        String nodeB = sha256("CONCEPT|NonExistent");
        CausalNode ca = new CausalNode(nodeA, "Java", CausalNodeType.CAUSE);
        CausalNode cb = new CausalNode(nodeB, "NonExistent", CausalNodeType.EFFECT);
        CausalEdge edge = new CausalEdge(nodeA, nodeB, 1.0);
        CausalGraph causal = new CausalGraph(List.of(ca, cb), List.of(edge), List.of());
        ReasoningGraph rg = new ReasoningGraph(List.of(), List.of(), List.of());
        SynthesisGraph sg = new SynthesisGraph(List.of(), List.of(), List.of());
        VerificationGraph vg = engine(cg).verify(rg, sg, causal);
        VerificationIssue issue = vg.issues().stream()
                .filter(i -> i.type() == VerificationIssueType.CONTRADICTION)
                .findFirst().orElseThrow();
        assertEquals(edge.fromNode(), issue.nodeId());
    }
}
