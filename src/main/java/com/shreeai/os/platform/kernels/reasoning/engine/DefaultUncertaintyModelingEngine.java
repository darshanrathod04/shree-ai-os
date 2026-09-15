package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
import com.shreeai.os.platform.kernels.reasoning.model.EvidenceCluster;
import com.shreeai.os.platform.kernels.reasoning.model.Hypothesis;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningNode;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultUncertaintyModelingEngine</b>
 *
 * <p>The R5 implementation of {@link UncertaintyModelingEngine}. Performs five
 * deterministic uncertainty-modeling stages:</p>
 * <ol>
 *   <li><b>Certainty Classification</b> - every verified node receives a
 *       deterministic certainty score and a locked level.</li>
 *   <li><b>Knowledge Gap Detection</b> - missing supporting evidence,
 *       isolated causal nodes, unsupported hypotheses and incomplete
 *       synthesis.</li>
 *   <li><b>Conflicting Reliability</b> - LOW_RELIABILITY when verification is
 *       high but trust is low, CONFLICTING_EVIDENCE when evidence disagrees.</li>
 *   <li><b>Overall Certainty</b> - locked formula
 *       {@code sum(certaintyScore) / verifiedNodes}, clamped to [0.0, 1.0].</li>
 *   <li><b>Build UncertaintyGraph</b> - canonical, stably ordered graph with
 *       certainty nodes, uncertainty issues and the overall certainty score.</li>
 * </ol>
 *
 * <p>The engine is stateless, thread-safe and deterministic - identical inputs
 * always produce structurally equal uncertainty graphs. It never retrieves
 * knowledge, never generates answers and never makes decisions; it only
 * evaluates certainty. No LLM, no ML, no embeddings, no probability.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultUncertaintyModelingEngine implements UncertaintyModelingEngine {

    /** Locked CERTAIN threshold: certainty score &gt;= 0.90. */
    public static final double CERTAIN_THRESHOLD = 0.90;

    /** Locked LIKELY threshold: certainty score in [0.75, 0.90). */
    public static final double LIKELY_THRESHOLD = 0.75;

    /** Locked UNCERTAIN threshold: certainty score in [0.50, 0.75). */
    public static final double UNCERTAIN_THRESHOLD = 0.50;

    /** Locked low-trust threshold for LOW_RELIABILITY detection. */
    public static final double LOW_TRUST_THRESHOLD = 0.50;

    /** Locked weight of the node status (verification) component. */
    public static final double WEIGHT_STATUS = 0.5;

    /** Locked weight of the node trust component. */
    public static final double WEIGHT_TRUST = 0.5;

    /** Locked penalty weight applied per verification issue severity. */
    public static final double WEIGHT_ISSUE_PENALTY = 0.1;

    /** Locked impact: unsupported hypothesis. */
    public static final double IMPACT_UNSUPPORTED_HYPOTHESIS = 1.00;

    /** Locked impact: missing supporting evidence. */
    public static final double IMPACT_MISSING_EVIDENCE = 0.75;

    /** Locked impact: conflicting evidence. */
    public static final double IMPACT_CONFLICTING_EVIDENCE = 0.75;

    /** Locked impact: low reliability / low evidence. */
    public static final double IMPACT_LOW_RELIABILITY = 0.50;

    /** Locked impact: isolated causal node. */
    public static final double IMPACT_ISOLATED_CAUSAL_NODE = 0.50;

    /** Locked impact: incomplete synthesis. */
    public static final double IMPACT_INCOMPLETE_SYNTHESIS = 0.50;

    /** Locked impact: incomplete reasoning. */
    public static final double IMPACT_INCOMPLETE_REASONING = 1.00;

    @Override
    public UncertaintyGraph model(ReasoningGraph reasoningGraph,
                                  SynthesisGraph synthesisGraph,
                                  CausalGraph causalGraph,
                                  VerificationGraph verificationGraph,
                                  ReliabilityResult reliabilityResult) {
        Objects.requireNonNull(reasoningGraph, "reasoningGraph must not be null");
        Objects.requireNonNull(synthesisGraph, "synthesisGraph must not be null");
        Objects.requireNonNull(causalGraph, "causalGraph must not be null");
        Objects.requireNonNull(verificationGraph, "verificationGraph must not be null");
        Objects.requireNonNull(reliabilityResult, "reliabilityResult must not be null");

        double overallTrust = reliabilityResult.overallTrust();
        Map<String, Double> evidenceTrust = evidenceTrustIndex(reliabilityResult);
        Map<String, String> titles = buildTitleIndex(reasoningGraph, causalGraph);

        // ---- Stage 1 + 4: certainty classification ---------------------------
        List<UncertaintyNode> nodes = new ArrayList<>();
        for (VerificationNode vn : verificationGraph.nodes()) {
            double nodeTrust = nodeTrust(vn, evidenceTrust, overallTrust);
            double penalty = maxIssueSeverity(verificationGraph, vn.nodeId());
            double certainty = certaintyScore(vn.status(), nodeTrust, penalty);
            String title = titles.getOrDefault(vn.nodeId(), vn.nodeId());
            nodes.add(new UncertaintyNode(vn.nodeId(), title,
                    classify(certainty), certainty));
        }

        // ---- Stage 2 + 3: issues -------------------------------------------
        List<UncertaintyIssue> issues = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        detectUnsupportedHypotheses(reasoningGraph, issues, seen);
        detectMissingEvidence(synthesisGraph, issues, seen);
        detectIsolatedCausalNodes(causalGraph, issues, seen);
        detectIncompleteSynthesis(synthesisGraph, issues, seen);
        detectLowReliability(verificationGraph, reliabilityResult, evidenceTrust, overallTrust, issues, seen);
        detectConflictingEvidence(verificationGraph, issues, seen);
        detectLowEvidence(verificationGraph, issues, seen);
        detectIncompleteReasoning(reasoningGraph, causalGraph, verificationGraph, issues, seen);

        // ---- Stage 4: overall certainty ------------------------------------
        double overall = overallCertainty(nodes);

        return new UncertaintyGraph(nodes, issues, overall);
    }

    // ---- Stage 1 helpers ----------------------------------------------------



    /**
     * Computes the locked per-node trust: the mean overall trust of the node's
     * supporting evidence chunk ids, or the global overall trust when the node
     * carries no evidence ids.
     */
    private static double nodeTrust(VerificationNode vn,
                                    Map<String, Double> evidenceTrust,
                                    double overallTrust) {
        if (vn.supportingEvidenceIds().isEmpty()) {
            return overallTrust;
        }
        double sum = 0.0;
        int count = 0;
        for (String evidenceId : vn.supportingEvidenceIds()) {
            Double trust = evidenceTrust.get(evidenceId);
            if (trust != null) {
                sum += trust;
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    /**
     * Returns the maximum severity of any verification issue attached to the
     * given node, or 0.0 when none exist.
     */
    private static double maxIssueSeverity(VerificationGraph graph, String nodeId) {
        double max = 0.0;
        for (VerificationIssue issue : graph.issues()) {
            if (nodeId.equals(issue.nodeId())) {
                max = Math.max(max, issue.severity());
            }
        }
        return max;
    }

    /**
     * Locked per-node certainty score:
     * {@code clamp(0.5 * statusScore + 0.5 * trust - 0.1 * penalty, 0.0, 1.0)}.
     */
    private static double certaintyScore(VerificationStatus status,
                                         double nodeTrust,
                                         double penalty) {
        double raw = WEIGHT_STATUS * statusScore(status)
                + WEIGHT_TRUST * nodeTrust
                - WEIGHT_ISSUE_PENALTY * penalty;
        return round4(clamp(raw, 0.0, 1.0));
    }

    private static double statusScore(VerificationStatus status) {
        return switch (status) {
            case VERIFIED -> 1.0;
            case PARTIALLY_VERIFIED -> 0.5;
            case UNSUPPORTED, CONTRADICTED -> 0.0;
        };
    }

    private static UncertaintyLevel classify(double certainty) {
        if (certainty >= CERTAIN_THRESHOLD) {
            return UncertaintyLevel.CERTAIN;
        }
        if (certainty >= LIKELY_THRESHOLD) {
            return UncertaintyLevel.LIKELY;
        }
        if (certainty >= UNCERTAIN_THRESHOLD) {
            return UncertaintyLevel.UNCERTAIN;
        }
        return UncertaintyLevel.UNKNOWN;
    }

    // ---- Stage 2 helpers ----------------------------------------------------

    private static void detectUnsupportedHypotheses(ReasoningGraph graph,
                                                    List<UncertaintyIssue> issues,
                                                    Set<String> seen) {
        for (Hypothesis hypothesis : graph.hypotheses()) {
            if (hypothesis.supportingEvidenceIds().isEmpty()) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.KNOWLEDGE_GAP,
                        hypothesis.hypothesisId(),
                        "Hypothesis is not supported by any trusted evidence",
                        IMPACT_UNSUPPORTED_HYPOTHESIS));
            }
        }
    }

    private static void detectMissingEvidence(SynthesisGraph graph,
                                              List<UncertaintyIssue> issues,
                                              Set<String> seen) {
        for (SynthesizedFact fact : graph.facts()) {
            if (fact.supportingEvidenceIds().isEmpty()) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.KNOWLEDGE_GAP,
                        fact.factId(),
                        "Synthesized fact has no supporting evidence",
                        IMPACT_MISSING_EVIDENCE));
            }
        }
        for (EvidenceCluster cluster : graph.clusters()) {
            if (cluster.evidenceIds().isEmpty()) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.KNOWLEDGE_GAP,
                        cluster.clusterId(),
                        "Evidence cluster has no supporting evidence items",
                        IMPACT_MISSING_EVIDENCE));
            }
        }
    }

    private static void detectIsolatedCausalNodes(CausalGraph graph,
                                                  List<UncertaintyIssue> issues,
                                                  Set<String> seen) {
        Set<String> connected = new LinkedHashSet<>();
        for (CausalEdge edge : graph.edges()) {
            connected.add(edge.fromNode());
            connected.add(edge.toNode());
        }
        for (CausalNode node : graph.nodes()) {
            if (!connected.contains(node.nodeId())) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.KNOWLEDGE_GAP,
                        node.nodeId(),
                        "Causal node is isolated with no causal relationships",
                        IMPACT_ISOLATED_CAUSAL_NODE));
            }
        }
    }

    private static void detectIncompleteSynthesis(SynthesisGraph graph,
                                                  List<UncertaintyIssue> issues,
                                                  Set<String> seen) {
        if (!graph.clusters().isEmpty() && graph.facts().isEmpty()) {
            addUnique(issues, seen, new UncertaintyIssue(
                    UncertaintyType.KNOWLEDGE_GAP,
                    graph.clusters().get(0).clusterId(),
                    "Synthesis is incomplete: clusters exist but no facts were produced",
                    IMPACT_INCOMPLETE_SYNTHESIS));
        }
    }

    // ---- Stage 3 helpers ----------------------------------------------------

private static void detectLowReliability(VerificationGraph verification,
                                             ReliabilityResult reliability,
                                             Map<String, Double> evidenceTrust,
                                             double overallTrust,
                                             List<UncertaintyIssue> issues,
                                             Set<String> seen) {
        double overallTrustValue = reliability.overallTrust();
        for (VerificationNode vn : verification.nodes()) {
            if (vn.status() == VerificationStatus.VERIFIED
                    || vn.status() == VerificationStatus.PARTIALLY_VERIFIED) {
                double trust = nodeTrust(vn, evidenceTrust, overallTrust);
                // Verification is high (node evaluated) but trust is low.
                if (trust < LOW_TRUST_THRESHOLD || overallTrustValue < LOW_TRUST_THRESHOLD) {
                    addUnique(issues, seen, new UncertaintyIssue(
                            UncertaintyType.LOW_RELIABILITY,
                            vn.nodeId(),
                            "Node is verified but supporting evidence trust is low",
                            IMPACT_LOW_RELIABILITY));
                }
            }
        }
    }

    private static void detectConflictingEvidence(VerificationGraph verification,
                                                  List<UncertaintyIssue> issues,
                                                  Set<String> seen) {
        for (VerificationIssue issue : verification.issues()) {
            if (issue.type() == VerificationIssueType.CONTRADICTION) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.CONFLICTING_EVIDENCE,
                        issue.nodeId(),
                        issue.explanation(),
                        IMPACT_CONFLICTING_EVIDENCE));
            }
        }
    }

    private static void detectLowEvidence(VerificationGraph verification,
                                          List<UncertaintyIssue> issues,
                                          Set<String> seen) {
        for (VerificationNode vn : verification.nodes()) {
            if (vn.status() == VerificationStatus.UNSUPPORTED
                    || vn.status() == VerificationStatus.CONTRADICTED) {
                addUnique(issues, seen, new UncertaintyIssue(
                        UncertaintyType.LOW_EVIDENCE,
                        vn.nodeId(),
                        "Node is not supported by trusted evidence",
                        IMPACT_LOW_RELIABILITY));
            }
        }
    }

    private static void detectIncompleteReasoning(ReasoningGraph reasoning,
                                                  CausalGraph causal,
                                                  VerificationGraph verification,
                                                  List<UncertaintyIssue> issues,
                                                  Set<String> seen) {
        if (!reasoning.hypotheses().isEmpty() && causal.nodes().isEmpty()) {
            addUnique(issues, seen, new UncertaintyIssue(
                    UncertaintyType.INCOMPLETE_REASONING,
                    reasoning.hypotheses().get(0).hypothesisId(),
                    "Reasoning is incomplete: hypotheses exist but no causal analysis",
                    IMPACT_INCOMPLETE_REASONING));
        }
        if (!reasoning.nodes().isEmpty() && verification.nodes().isEmpty()) {
            addUnique(issues, seen, new UncertaintyIssue(
                    UncertaintyType.INCOMPLETE_REASONING,
                    reasoning.nodes().get(0).nodeId(),
                    "Reasoning is incomplete: reasoning nodes exist but no verification",
                    IMPACT_INCOMPLETE_REASONING));
        }
    }

    // ---- Stage 4 helpers ----------------------------------------------------
/**
     * Locked overall certainty formula:
     * {@code sum(certaintyScore) / verifiedNodes}, clamped to [0.0, 1.0]. When
     * no nodes are verified the overall certainty is 1.0.
     */
    private static double overallCertainty(List<UncertaintyNode> nodes) {
        if (nodes.isEmpty()) {
            return 1.0;
        }
        double sum = 0.0;
        for (UncertaintyNode node : nodes) {
            sum += node.certaintyScore();
        }
        return round4(clamp(sum / nodes.size(), 0.0, 1.0));
    }

    // ---- shared helpers -----------------------------------------------------

    private static Map<String, Double> evidenceTrustIndex(ReliabilityResult reliability) {
        Map<String, Double> index = new HashMap<>();
        for (TrustedEvidence trusted : reliability.evidence()) {
            index.put(trusted.evidence().chunkId(), trusted.trust().overall());
        }
        return index;
    }

    private static Map<String, String> buildTitleIndex(ReasoningGraph reasoning,
                                                       CausalGraph causal) {
        Map<String, String> titles = new HashMap<>();
        for (ReasoningNode node : reasoning.nodes()) {
            titles.putIfAbsent(node.nodeId(), node.title());
        }
        for (CausalNode node : causal.nodes()) {
            titles.putIfAbsent(node.nodeId(), node.title());
        }
        return titles;
    }

    private static void addUnique(List<UncertaintyIssue> issues,
                                  Set<String> seen,
                                  UncertaintyIssue issue) {
        String key = issue.type().name() + "|" + issue.nodeId();
        if (seen.add(key)) {
            issues.add(issue);
        }
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
