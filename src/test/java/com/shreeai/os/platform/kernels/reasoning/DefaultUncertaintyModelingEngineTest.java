package com.shreeai.os.platform.kernels.reasoning;

import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultUncertaintyModelingEngine;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNodeType;
import com.shreeai.os.platform.kernels.reasoning.model.EvidenceCluster;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesizedFact;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyIssue;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyLevel;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyNode;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyType;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationIssue;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationIssueType;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationNode;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic unit tests for {@link DefaultUncertaintyModelingEngine}.
 *
 * <p>Every test asserts exact, locked behavior: classification thresholds,
 * knowledge-gap detection, conflicting-reliability detection, the overall
 * certainty formula and full determinism (identical inputs produce
 * identical graphs).</p>
 */
class DefaultUncertaintyModelingEngineTest {

    private final DefaultUncertaintyModelingEngine engine =
            new DefaultUncertaintyModelingEngine();

    private static String sha256(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ---- standard deterministic fixtures ------------------------------------

    private static final String HYP_ID = sha256("HYPOTHESIS|Java is supported");

    private static EvidenceItem evidence(String chunkId, double trust) {
        return new EvidenceItem(chunkId, "doc-" + chunkId,
                "content of " + chunkId, 0.9, List.of("Java"));
    }

    private static TrustedEvidence trusted(String chunkId, double trust) {
        return new TrustedEvidence(evidence(chunkId, trust),
                new TrustScore(trust, trust, trust, trust, "fixed trust " + trust));
    }

    private static ReliabilityResult reliability(double overallTrust,
                                                 TrustedEvidence... evidenceItems) {
        List<TrustedEvidence> evidence = new ArrayList<>();
        for (TrustedEvidence item : evidenceItems) {
            evidence.add(item);
        }
        return new ReliabilityResult(evidence, overallTrust);
    }

    /** A reasoning graph with one fully supported hypothesis. */
    private static ReasoningGraph supportedReasoningGraph() {
        return new ReasoningGraph(
                List.of(),
                List.of(),
                List.of(new Hypothesis(HYP_ID, "Java is supported",
                        List.of("e1"), 0.9)));
    }

    /** An empty reasoning graph (no nodes, no hypotheses). */
    private static ReasoningGraph emptyReasoningGraph() {
        return new ReasoningGraph(List.of(), List.of(), List.of());
    }

    /** A synthesis graph with one cluster and one supported fact. */
    private static SynthesisGraph supportedSynthesisGraph() {
        return new SynthesisGraph(
                List.of(new EvidenceCluster(sha256("CLUSTER|Java"),
                        List.of("e1"), List.of("Java"))),
                List.of(new SynthesizedFact(sha256("FACT|Java supports streams"),
                        "Java supports streams", List.of("e1"), 0.9)),
                List.of());
    }

    /** An empty synthesis graph (no clusters, no facts). */
    private static SynthesisGraph emptySynthesisGraph() {
        return new SynthesisGraph(List.of(), List.of(), List.of());
    }

    /** A causal graph with two connected nodes (c1 -&gt; c2). */
    private static CausalGraph connectedCausalGraph() {
        return new CausalGraph(
                List.of(new CausalNode("c1", "Cause Concept", CausalNodeType.CAUSE),
                        new CausalNode("c2", "Effect Concept", CausalNodeType.EFFECT)),
                List.of(new CausalEdge("c1", "c2", 0.8)),
                List.of());
    }

    /** An empty causal graph (no nodes, no edges). */
    private static CausalGraph emptyCausalGraph() {
        return new CausalGraph(List.of(), List.of(), List.of());
    }

    /** A verification graph with the given nodes and no issues. */
    private static VerificationGraph verificationGraph(VerificationNode... nodes) {
        return new VerificationGraph(List.of(nodes), List.of(), 1.0);
    }

    private static VerificationGraph verificationGraph(List<VerificationNode> nodes) {
        return new VerificationGraph(nodes, List.of(), 1.0);
    }

    private static VerificationGraph verificationGraph(List<VerificationNode> nodes,
                                                       List<VerificationIssue> issues) {
        double score = issues.isEmpty() ? 1.0 : 0.75;
        return new VerificationGraph(nodes, issues, score);
    }
// ---- Stage 1: certainty classification ------------------------------------

    private UncertaintyGraph classify(VerificationNode node, double overallTrust) {
        return engine.model(supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(), verificationGraph(node),
                reliability(overallTrust, trusted("e1", overallTrust)));
    }

    private UncertaintyNode classifyOneNode(VerificationNode node,
                                            double overallTrust) {
        UncertaintyGraph graph = classify(node, overallTrust);
        assertEquals(1, graph.nodes().size(), "one verification node -> one uncertainty node");
        return graph.nodes().get(0);
    }

    @Test
    void certainClassification_highVerificationAndTrust() {
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 1.0);
        assertEquals(1.0, n.certaintyScore(), 0.0);
        assertEquals(UncertaintyLevel.CERTAIN, n.level());
    }

    @Test
    void certainClassification_exactlyAtZeroPointNine() {
        // 0.5 * 1.0 + 0.5 * 0.80 = 0.90 -> CERTAIN (>= 0.90)
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.80);
        assertEquals(0.90, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.CERTAIN, n.level());
    }

    @Test
    void likelyClassification_midTrust() {
        // 0.5 * 1.0 + 0.5 * 0.60 = 0.80 -> LIKELY
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.60);
        assertEquals(0.80, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.LIKELY, n.level());
    }

    @Test
    void likelyClassification_partiallyVerifiedFullTrust() {
        // 0.5 * 0.5 + 0.5 * 1.0 = 0.75 -> LIKELY (>= 0.75)
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.PARTIALLY_VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 1.0);
        assertEquals(0.75, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.LIKELY, n.level());
    }

    @Test
    void uncertainClassification_partiallyVerifiedHighTrust() {
        // 0.5 * 0.5 + 0.5 * 0.90 = 0.70 -> UNCERTAIN
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.PARTIALLY_VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.90);
        assertEquals(0.70, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.UNCERTAIN, n.level());
    }

    @Test
    void uncertainClassification_exactlyAtZeroPointFive() {
        // 0.5 * 0.5 + 0.5 * 0.50 = 0.50 -> UNCERTAIN (>= 0.50)
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.PARTIALLY_VERIFIED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.50);
        assertEquals(0.50, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.UNCERTAIN, n.level());
    }

    @Test
    void unknownClassification_unsupportedNode() {
        // 0.5 * 0.0 + 0.5 * 0.20 = 0.10 -> UNKNOWN
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.UNSUPPORTED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.20);
        assertEquals(0.10, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.UNKNOWN, n.level());
    }

    @Test
    void unknownClassification_highTrustButUnsupported() {
        // 0.5 * 0.0 + 0.5 * 0.99 = 0.495 -> UNKNOWN (< 0.50)
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.UNSUPPORTED, List.of("e1"));
        UncertaintyNode n = classifyOneNode(node, 0.99);
        assertEquals(0.495, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.UNKNOWN, n.level());
    }

    @Test
    void uncertaintyLevels_coversAllLockedThresholds() {
        assertEquals(List.of(UncertaintyLevel.CERTAIN, UncertaintyLevel.LIKELY,
                UncertaintyLevel.UNCERTAIN, UncertaintyLevel.UNKNOWN),
                List.of(UncertaintyLevel.values()));
    }

    // ---- Stage 1: issue penalty and per-node trust ----------------------------

    @Test
    void certaintyScore_reducedByIssuePenalty() {
        // 0.5 * 0.0 + 0.5 * 0.90 - 0.1 * 0.75 = 0.375 -> UNKNOWN
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.CONTRADICTED, List.of("e1"));
        List<VerificationIssue> issues = List.of(new VerificationIssue(
                VerificationIssueType.CONTRADICTION, "c1",
                "causal edge not backed by concept graph", 0.75));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(List.of(node), issues),
                reliability(1.0, trusted("e1", 0.90)));
        UncertaintyNode n = graph.nodes().get(0);
        assertEquals(0.375, n.certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.UNKNOWN, n.level());
    }

    @Test
    void certaintyScore_penaltyDoesNotApplyToOtherNodes() {
        // Penalty on c1 must not touch c2.
        List<VerificationNode> nodes = List.of(
                new VerificationNode("c1", VerificationStatus.VERIFIED, List.of("e1")),
                new VerificationNode("c2", VerificationStatus.VERIFIED, List.of("e1")));
        List<VerificationIssue> issues = List.of(new VerificationIssue(
                VerificationIssueType.CONTRADICTION, "c1",
                "causal edge not backed by concept graph", 1.00));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(nodes, issues),
                reliability(0.80, trusted("e1", 0.80)));
        UncertaintyNode n1 = graph.nodes().stream()
                .filter(n -> n.nodeId().equals("c1")).findFirst().orElseThrow();
        UncertaintyNode n2 = graph.nodes().stream()
                .filter(n -> n.nodeId().equals("c2")).findFirst().orElseThrow();
        // c1: 0.5*1.0 + 0.5*0.80 - 0.1*0.75 = 0.825 (locked CONTRADICTION
        // severity of 0.75 overrides the caller-provided 1.00) ;
        // c2: 0.5*1.0 + 0.5*0.80 = 0.90 (penalty must not leak to other nodes)
        assertEquals(0.825, n1.certaintyScore(), 0.0001);
        assertEquals(0.90, n2.certaintyScore(), 0.0001);
    }

    @Test
    void nodeTrust_isMeanOfSupportingEvidenceTrust() {
        // e1 trust 1.0, e2 trust 0.0 -> node trust 0.5
        // 0.5 * 1.0 + 0.5 * 0.50 = 0.75 -> LIKELY
        VerificationNode node = new VerificationNode("c1",
                VerificationStatus.VERIFIED, List.of("e1", "e2"));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(), verificationGraph(node),
                reliability(0.90, trusted("e1", 1.0), trusted("e2", 0.0)));
        assertEquals(0.75, graph.nodes().get(0).certaintyScore(), 0.0001);
        assertEquals(UncertaintyLevel.LIKELY, graph.nodes().get(0).level());
    }

    // ---- Stage 2: knowledge gap detection ----------------------------------

    @Test
    void knowledgeGap_unsupportedHypothesis() {
        ReasoningGraph reasoning = new ReasoningGraph(
                List.of(),
                List.of(),
                List.of(new Hypothesis(HYP_ID, "H is unsupported", List.of(), 0.5)));
        UncertaintyGraph graph = engine.model(
                reasoning, supportedSynthesisGraph(), connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.KNOWLEDGE_GAP)
                .findFirst().orElseThrow();
        assertEquals(HYP_ID, issue.nodeId());
        assertEquals(1.00, issue.impact(), 0.0);
    }

    @Test
    void knowledgeGap_missingEvidenceOnSynthesizedFact() {
        SynthesisGraph synthesis = new SynthesisGraph(
                List.of(new EvidenceCluster(sha256("CLUSTER|Java"),
                        List.of("e1"), List.of("Java"))),
                List.of(new SynthesizedFact(sha256("FACT|unbacked"),
                        "unbacked fact", List.of(), 0.5)),
                List.of());
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), synthesis, connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.KNOWLEDGE_GAP)
                .findFirst().orElseThrow();
        assertEquals(sha256("FACT|unbacked"), issue.nodeId());
        assertEquals(0.75, issue.impact(), 0.0);
    }
@Test
    void knowledgeGap_isolatedCausalNode() {
        CausalGraph causal = new CausalGraph(
                List.of(new CausalNode("c1", "Alone", CausalNodeType.CAUSE)),
                List.of(),
                List.of());
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(), causal,
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.KNOWLEDGE_GAP)
                .findFirst().orElseThrow();
        assertEquals("c1", issue.nodeId());
        assertEquals(0.50, issue.impact(), 0.0);
    }

    @Test
    void knowledgeGap_incompleteSynthesis() {
        // Cluster exists with evidence but no synthesized facts were produced.
        SynthesisGraph synthesis = new SynthesisGraph(
                List.of(new EvidenceCluster(sha256("CLUSTER|Java"),
                        List.of("e1"), List.of("Java"))),
                List.of(),
                List.of());
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), synthesis, connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.KNOWLEDGE_GAP)
                .findFirst().orElseThrow();
        assertEquals(sha256("CLUSTER|Java"), issue.nodeId());
        assertEquals(0.50, issue.impact(), 0.0);
    }

    @Test
    void noKnowledgeGaps_whenEverythingIsSupported() {
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(
                        new VerificationNode("c1", VerificationStatus.VERIFIED, List.of()),
                        new VerificationNode("c2", VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        assertTrue(graph.issues().isEmpty(),
                "clean world must produce zero uncertainty issues");
        assertEquals(2, graph.nodes().size());
        assertEquals(0.975, graph.overallCertainty(), 0.0001);
    }

    // ---- Stage 3: conflicting reliability -------------------------------------

    @Test
    void lowReliability_detectedWhenTrustBelowThreshold() {
        // Verification high (VERIFIED) but overall trust 0.40 < 0.50.
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.40));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.LOW_RELIABILITY)
                .findFirst().orElse(null);
        assertNotNull(issue, "LOW_RELIABILITY issue must be present");
        assertEquals("c1", issue.nodeId());
        assertEquals(0.50, issue.impact(), 0.0);
    }

    @Test
    void lowReliability_notDetectedWhenTrustIsHigh() {
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.90));
        assertTrue(graph.issues().stream()
                        .noneMatch(i -> i.type() == UncertaintyType.LOW_RELIABILITY),
                "no LOW_RELIABILITY issue when trust is high");
    }

    @Test
    void conflictingEvidence_detectedFromContradiction() {
        List<VerificationIssue> verificationIssues = List.of(
                new VerificationIssue(VerificationIssueType.CONTRADICTION, "c1",
                        "causal edge is not backed by a concept graph relationship", 0.75));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(List.of(
                        new VerificationNode("c1", VerificationStatus.VERIFIED, List.of()),
                        new VerificationNode("c2", VerificationStatus.VERIFIED, List.of())),
                        verificationIssues),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.CONFLICTING_EVIDENCE)
                .findFirst().orElse(null);
        assertNotNull(issue, "CONFLICTING_EVIDENCE issue must be present");
        assertEquals("c1", issue.nodeId());
        assertEquals(0.75, issue.impact(), 0.0);
    }

    @Test
    void conflictingEvidence_notDetectedWithoutContradiction() {
        List<VerificationIssue> verificationIssues = List.of(
                new VerificationIssue(VerificationIssueType.LOW_TRUST, "c1",
                        "low trust evidence", 0.25));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(List.of(
                        new VerificationNode("c1", VerificationStatus.VERIFIED, List.of())),
                        verificationIssues),
                reliability(0.95));
        assertTrue(graph.issues().stream()
                        .noneMatch(i -> i.type() == UncertaintyType.CONFLICTING_EVIDENCE),
                "non-contradiction verification issues must not map to CONFLICTING_EVIDENCE");
    }

    @Test
    void lowEvidence_detectedForUnsupportedNode() {
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.UNSUPPORTED, List.of("e1"))),
                reliability(0.90));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.LOW_EVIDENCE)
                .findFirst().orElse(null);
        assertNotNull(issue, "LOW_EVIDENCE issue must be present for UNSUPPORTED node");
        assertEquals("c1", issue.nodeId());
        assertEquals(0.50, issue.impact(), 0.0);
    }

    @Test
    void incompleteReasoning_detectedWhenNoCausalAnalysis() {
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), emptySynthesisGraph(), emptyCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of())),
                reliability(0.95));
        UncertaintyIssue issue = graph.issues().stream()
                .filter(i -> i.type() == UncertaintyType.INCOMPLETE_REASONING)
                .findFirst().orElse(null);
        assertNotNull(issue, "INCOMPLETE_REASONING issue must be present");
        assertEquals(HYP_ID, issue.nodeId());
        assertEquals(1.00, issue.impact(), 0.0);
    }

    // ---- Stage 4: overall certainty ----------------------------------------

    @Test
    void overallCertainty_isMeanOfNodeCertainty() {
        List<VerificationNode> nodes = List.of(
                new VerificationNode("c1", VerificationStatus.VERIFIED, List.of("e1")),
                new VerificationNode("c2", VerificationStatus.UNSUPPORTED, List.of()));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(), verificationGraph(nodes),
                reliability(0.0, trusted("e1", 1.0)));
        // c1: 0.5*1.0 + 0.5*1.0 = 1.0 ; c2: 0.5*0 + 0.5*0.0 = 0.0 -> mean 0.5
        assertEquals(0.5, graph.overallCertainty(), 0.0001);
    }

    @Test
    void overallCertainty_singleCertainNode_isOne() {
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(),
                verificationGraph(new VerificationNode("c1",
                        VerificationStatus.VERIFIED, List.of("e1"))),
                reliability(1.0, trusted("e1", 1.0)));
        assertEquals(1.0, graph.overallCertainty(), 0.0);
        assertEquals(UncertaintyLevel.CERTAIN, graph.nodes().get(0).level());
    }

    @Test
    void overallCertainty_emptyVerification_isOne() {
        UncertaintyGraph graph = engine.model(
                emptyReasoningGraph(), emptySynthesisGraph(), emptyCausalGraph(),
                new VerificationGraph(List.of(), List.of(), 1.0),
                reliability(0.0));
        assertEquals(0, graph.nodes().size());
        assertEquals(1.0, graph.overallCertainty(), 0.0);
    }

    @Test
    void overallCertainty_alwaysWithinZeroOne() {
        List<VerificationNode> nodes = List.of(
                new VerificationNode("c1", VerificationStatus.CONTRADICTED, List.of()),
                new VerificationNode("c2", VerificationStatus.VERIFIED, List.of("e1")));
        UncertaintyGraph graph = engine.model(
                supportedReasoningGraph(), supportedSynthesisGraph(),
                connectedCausalGraph(), verificationGraph(nodes),
                reliability(1.0, trusted("e1", 1.0)));
        assertTrue(graph.overallCertainty() >= 0.0);
        assertTrue(graph.overallCertainty() <= 1.0);
    }
}