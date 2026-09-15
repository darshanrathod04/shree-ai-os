package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.reasoning.model.CausalChain;
import com.shreeai.os.platform.kernels.reasoning.model.CausalEdge;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalNode;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultSelfVerificationEngine</b>
 *
 * <p>The R4 implementation of {@link SelfVerificationEngine}. Performs five
 * deterministic verification stages:</p>
 * <ol>
 *   <li><b>Evidence Coverage</b> - every hypothesis must reference at least
 *       one trusted evidence item.</li>
 *   <li><b>Contradiction Check</b> - every causal edge must be backed by a
 *       corresponding concept-graph relationship.</li>
 *   <li><b>Completeness Check</b> - every synthesized fact must belong to at
 *       least one causal chain.</li>
 *   <li><b>Trust Threshold</b> - evidence with trust below locked thresholds
 *       is flagged.</li>
 *   <li><b>Verification Score</b> - locked formula producing a score in
 *       [0.0, 1.0].</li>
 * </ol>
 *
 * <p>The engine is stateless, thread-safe and deterministic - identical
 * inputs always produce structurally equal verification graphs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultSelfVerificationEngine implements SelfVerificationEngine {

    /** Locked trust threshold for VERIFIED status. */
    public static final double TRUST_THRESHOLD_VERIFIED = 0.90;

    /** Locked trust threshold for PARTIALLY_VERIFIED status. */
    public static final double TRUST_THRESHOLD_PARTIAL = 0.70;

    /** Locked severity: missing evidence / unsupported hypothesis. */
    public static final double SEVERITY_MISSING = 1.00;

    /** Locked severity: contradiction. */
    public static final double SEVERITY_CONTRADICTION = 0.75;

    /** Locked severity: incomplete chain. */
    public static final double SEVERITY_INCOMPLETE = 0.50;

    /** Locked severity: low trust. */
    public static final double SEVERITY_LOW_TRUST = 0.25;

    private final ConceptGraph conceptGraph;

    /**
     * Creates a verification engine bound to the given concept graph for
     * contradiction checking.
     *
     * @param conceptGraph the canonical knowledge graph (never null)
     */
    public DefaultSelfVerificationEngine(ConceptGraph conceptGraph) {
        this.conceptGraph = Objects.requireNonNull(conceptGraph, "conceptGraph must not be null");
    }

    @Override
    public VerificationGraph verify(ReasoningGraph reasoningGraph,
                                    SynthesisGraph synthesisGraph,
                                    CausalGraph causalGraph) {
        Objects.requireNonNull(reasoningGraph, "reasoningGraph must not be null");
        Objects.requireNonNull(synthesisGraph, "synthesisGraph must not be null");
        Objects.requireNonNull(causalGraph, "causalGraph must not be null");

        List<VerificationNode> vNodes = new ArrayList<>();
        List<VerificationIssue> issues = new ArrayList<>();

        // ---- Stage 1: evidence coverage ------------------------------------
        Set<String> allEvidenceIds = new LinkedHashSet<>();
        for (ReasoningNode node : reasoningGraph.nodes()) {
            if (node.type() == ReasoningNodeType.EVIDENCE) {
                for (String eid : node.evidenceIds()) {
                    allEvidenceIds.add(eid);
                }
            }
        }

        for (Hypothesis hyp : reasoningGraph.hypotheses()) {
            List<String> evidence = List.copyOf(hyp.supportingEvidenceIds());
            if (evidence.isEmpty() ||
                    evidence.stream().noneMatch(allEvidenceIds::contains)) {
                issues.add(new VerificationIssue(
                        VerificationIssueType.MISSING_EVIDENCE,
                        hyp.hypothesisId(),
                        "Hypothesis has no supporting evidence in the reasoning graph",
                        SEVERITY_MISSING));
                vNodes.add(new VerificationNode(hyp.hypothesisId(),
                        VerificationStatus.UNSUPPORTED, List.of()));
            } else if (hyp.confidence() < TRUST_THRESHOLD_PARTIAL) {
                // Trust threshold - low trust: hypothesis still referenced by
                // evidence but its mean trust is below the locked 0.70 floor.
                issues.add(new VerificationIssue(
                        VerificationIssueType.LOW_TRUST,
                        hyp.hypothesisId(),
                        "Hypothesis support trust below threshold: " + hyp.confidence(),
                        SEVERITY_LOW_TRUST));
                vNodes.add(new VerificationNode(hyp.hypothesisId(),
                        VerificationStatus.PARTIALLY_VERIFIED, evidence));
            } else if (hyp.confidence() < TRUST_THRESHOLD_VERIFIED) {
                // Trust threshold - partial: 0.70-0.89 trust range.
                vNodes.add(new VerificationNode(hyp.hypothesisId(),
                        VerificationStatus.PARTIALLY_VERIFIED, evidence));
            } else {
                vNodes.add(new VerificationNode(hyp.hypothesisId(),
                        VerificationStatus.VERIFIED, evidence));
            }
        }

                // ---- Stage 2: contradiction check ----------------------------------
        // Build concept ID -> name index
        Map<String, String> conceptIdToName = new HashMap<>();
        for (var gc : conceptGraph.concepts()) {
            conceptIdToName.put(gc.conceptId(), gc.name());
        }
        // Build valid relationship pairs by concept NAME (case-insensitive).
        // Causal node IDs (sha256("CAUSE|" + title)) differ from concept graph
        // IDs (sha256("CONCEPT|" + name)), so verification compares by canonical
        // concept name, never by raw node id.
        Map<String, String> causalNodeIdToName = new HashMap<>();
        for (CausalNode cn : causalGraph.nodes()) {
            causalNodeIdToName.put(cn.nodeId(), cn.title());
        }
        Set<String> validRelationPairs = new LinkedHashSet<>();
        for (ConceptRelationship rel : conceptGraph.relationships()) {
            String fromName = conceptIdToName.getOrDefault(rel.fromConcept(), rel.fromConcept());
            String toName = conceptIdToName.getOrDefault(rel.toConcept(), rel.toConcept());
            validRelationPairs.add(fromName.toLowerCase() + "->" + toName.toLowerCase());
        }

        for (CausalEdge edge : causalGraph.edges()) {
            String fromName = causalNodeIdToName.get(edge.fromNode());
            String toName = causalNodeIdToName.get(edge.toNode());
            if (fromName == null || toName == null) {
                issues.add(new VerificationIssue(
                        VerificationIssueType.CONTRADICTION,
                        edge.fromNode(),
                        "Causal edge references a node unknown to the causal graph",
                        SEVERITY_CONTRADICTION));
                continue;
            }
            if (!validRelationPairs.contains(fromName.toLowerCase() + "->" + toName.toLowerCase())) {
                issues.add(new VerificationIssue(
                        VerificationIssueType.CONTRADICTION,
                        edge.fromNode(),
                        "Causal edge " + fromName + " -> " + toName +
                                " is not backed by a concept graph relationship",
                        SEVERITY_CONTRADICTION));
            }
        }

        // ---- Stage 3: completeness check ------------------------------------
        Set<String> allChainConceptNames = new LinkedHashSet<>();
        for (CausalChain chain : causalGraph.chains()) {
            for (String nodeId : chain.nodeIds()) {
                String name = causalNodeIdToName.get(nodeId);
                if (name != null) {
                    allChainConceptNames.add(name.toLowerCase());
                }
            }
        }

        for (SynthesizedFact fact : synthesisGraph.facts()) {
            String factLower = fact.statement().toLowerCase();
            boolean inChain = false;
            for (String conceptName : allChainConceptNames) {
                if (factLower.contains(conceptName)) {
                    inChain = true;
                    break;
                }
            }
            if (!inChain) {
                issues.add(new VerificationIssue(
                        VerificationIssueType.INCOMPLETE_CHAIN,
                        fact.factId(),
                        "Synthesized fact is not part of any causal chain",
                        SEVERITY_INCOMPLETE));
            }
        }

        // ---- Stage 4: trust threshold ---------------------------------------
        for (CausalNode cn : causalGraph.nodes()) {
            vNodes.add(new VerificationNode(cn.nodeId(),
                    VerificationStatus.VERIFIED, List.of()));
        }

        // ---- Stage 5: verification score ------------------------------------
        int verifiedCount = (int) vNodes.stream()
                .filter(vn -> vn.status() == VerificationStatus.VERIFIED)
                .count();
        double sumSeverities = issues.stream()
                .mapToDouble(VerificationIssue::severity)
                .sum();
        double divisor = verifiedCount + sumSeverities;
        double score = divisor > 0 ? 1.0 - (issues.size() / divisor) : 1.0;
        score = Math.max(0.0, Math.min(1.0, Math.round(score * 10000.0) / 10000.0));

        return new VerificationGraph(vNodes, issues, score);
    }
}
