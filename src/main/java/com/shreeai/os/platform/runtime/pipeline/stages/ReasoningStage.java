package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultAlternativeGenerationEngine;
import com.shreeai.os.platform.kernels.inference.engine.AlternativeGenerationEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultConfidenceCalibrationEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultExplainableDecisionEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultDecisionOptimizationEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultTradeoffAnalysisEngine;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ExplainableDecision;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.reasoning.engine.MultiHopReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultEvidenceSynthesisEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.EvidenceSynthesisEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.CausalReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultCausalReasoningEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultSelfVerificationEngine;
import com.shreeai.os.platform.kernels.reasoning.engine.DefaultUncertaintyModelingEngine;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.TrustScore;
import com.shreeai.os.platform.kernels.knowledge.model.TrustedEvidence;

/**
 * ReasoningStage - Performs real cognitive reasoning on the request.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Consuming memory results from MemoryRecallStage</li>
 *   <li>Consuming knowledge results from KnowledgeStage</li>
 *   <li>Running the DefaultReasoningEngine to derive conclusions</li>
 *   <li>Updating pipeline state with reasoning results</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 6
 */
public final class ReasoningStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Reasoning")
            .priority(5)
            .enabled(true)
            .version("1.0")
            .description("Performs cognitive reasoning on the request")
            .build();

    private final DefaultReasoningEngine reasoningEngine;
    private final MultiHopReasoningEngine multiHopEngine;
    private final EvidenceSynthesisEngine synthesisEngine;
    private final CausalReasoningEngine causalEngine;

    /**
     * Creates a new ReasoningStage with a real reasoning engine.
     *
     * @param reasoningEngine the reasoning engine
     * @param multiHopEngine  the R1 multi-hop reasoning engine
     * @param synthesisEngine the R2 evidence synthesis engine
     * @param causalEngine    the R3 causal reasoning engine
     */
    public ReasoningStage(DefaultReasoningEngine reasoningEngine,
                          MultiHopReasoningEngine multiHopEngine,
                          EvidenceSynthesisEngine synthesisEngine,
                          CausalReasoningEngine causalEngine) {
        this.reasoningEngine = reasoningEngine;
        this.multiHopEngine = multiHopEngine;
        this.synthesisEngine = synthesisEngine;
        this.causalEngine = causalEngine;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses a new DefaultReasoningEngine instance.
     */
    public ReasoningStage() {
        this(new DefaultReasoningEngine(),
             new com.shreeai.os.platform.kernels.reasoning.engine.DefaultMultiHopReasoningEngine(),
             new DefaultEvidenceSynthesisEngine(),
             new DefaultCausalReasoningEngine());
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve memory and knowledge information from previous stages
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

            // Get request text from the real user payload
            String requestText = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getUserInput() 
                    : "";

            // Get ranked memories from state
            @SuppressWarnings("unchecked")
            List<Memory> rankedMemories = (List<Memory>) state.getMetadata().get("rankedMemories");
            if (rankedMemories == null) {
                rankedMemories = List.of();
            }

            // Get ranked knowledge from state
            @SuppressWarnings("unchecked")
            List<KnowledgeNode> rankedKnowledge = (List<KnowledgeNode>) state.getMetadata().get("rankedKnowledge");
            if (rankedKnowledge == null) {
                rankedKnowledge = List.of();
            }

            // Run the reasoning engine
            ReasoningResult result = reasoningEngine.reason(requestText, rankedMemories, rankedKnowledge);

            // R1-R5 and I1-I5 Cognitive Reasoning & Inference Engines
            // Deterministic fallback construction ensures that if ReliabilityResult or ConceptGraph
            // were not explicitly staged by previous components, they are derived deterministically
            // from the available knowledge nodes, prompt tokens, and baseline confidence.
            try {
                Object reliabilityObj = state.getMetadata().get("reliabilityResult");
                ReliabilityResult reliability;
                if (reliabilityObj instanceof ReliabilityResult rr) {
                    reliability = rr;
                } else {
                    reliability = createFallbackReliability(requestId, requestText, result, rankedKnowledge);
                    state.addMetadata("reliabilityResult", reliability);
                }

                Object conceptGraphObj = state.getMetadata().get("conceptGraph");
                ConceptGraph conceptGraph;
                if (conceptGraphObj instanceof ConceptGraph cg) {
                    conceptGraph = cg;
                } else {
                    conceptGraph = createFallbackConceptGraph(requestText, rankedKnowledge);
                    state.addMetadata("conceptGraph", conceptGraph);
                }

                ReasoningGraph reasoningGraph = multiHopEngine.reason(reliability, conceptGraph);
                state.updateCognitiveState(cs -> cs.withReasoningGraph(reasoningGraph));
                state.addMetadata("reasoningGraph", reasoningGraph);
                state.addMessage("R1 multi-hop reasoning completed: "
                        + reasoningGraph.nodes().size() + " nodes, "
                        + reasoningGraph.hypotheses().size() + " hypotheses");

                // R2 - Evidence Synthesis: merge trusted evidence into a
                // unified, provenance-preserving SynthesisGraph.
                try {
                    SynthesisGraph synthesisGraph = synthesisEngine.synthesize(reasoningGraph, reliability, conceptGraph);
                    state.updateCognitiveState(cs -> cs.withSynthesisGraph(synthesisGraph));
                    state.addMetadata("synthesisGraph", synthesisGraph);
                    state.addMessage("R2 evidence synthesis completed: "
                            + synthesisGraph.clusterCount() + " clusters, "
                            + synthesisGraph.factCount() + " facts");

                    // R3 - Causal Reasoning: discover cause-effect relationships
                    // from the SynthesisGraph and ReasoningGraph.
                    try {
                        CausalGraph causalGraph = causalEngine.analyze(reasoningGraph, synthesisGraph, conceptGraph);
                        state.updateCognitiveState(cs -> cs.withCausalGraph(causalGraph));
                        state.addMetadata("causalGraph", causalGraph);
                        state.addMessage("R3 causal reasoning completed: "
                                + causalGraph.nodes().size() + " nodes, "
                                + causalGraph.chains().size() + " chains");

                        // R4 - Self Verification: verify coverage, consistency,
                        // completeness and trust of the reasoning artifacts.
                        try {
                            VerificationGraph verificationGraph =
                                    new DefaultSelfVerificationEngine(conceptGraph)
                                            .verify(reasoningGraph, synthesisGraph, causalGraph);
                            state.updateCognitiveState(cs -> cs.withVerificationGraph(verificationGraph));
                            state.addMetadata("verificationGraph", verificationGraph);
                            state.addMessage("R4 self verification completed: "
                                    + verificationGraph.nodes().size() + " nodes, "
                                    + verificationGraph.issues().size() + " issues, "
                                    + "score=" + verificationGraph.verificationScore());

                            // R5 - Uncertainty Modeling: evaluate certainty,
                            // uncertainty and knowledge gaps from the verified
                            // reasoning artifacts.
                            try {
                                UncertaintyGraph uncertaintyGraph =
                                        new DefaultUncertaintyModelingEngine()
                                                .model(reasoningGraph, synthesisGraph,
                                                        causalGraph, verificationGraph, reliability);
                                state.updateCognitiveState(cs -> cs.withUncertaintyGraph(uncertaintyGraph));
                                state.addMetadata("uncertaintyGraph", uncertaintyGraph);
                                state.addMessage("R5 uncertainty modeling completed: "
                                        + uncertaintyGraph.nodes().size() + " nodes, "
                                        + uncertaintyGraph.issues().size() + " issues, "
                                        + "certainty=" + uncertaintyGraph.overallCertainty());
                            } catch (Exception e) {
                                state.addMessage("R5 uncertainty modeling skipped: " + e.getMessage());
                            }

                            // I1 - Alternative Generation: transform the
                            // verified knowledge into deterministic,
                            // executable solution alternatives. No
                            // ranking, no selection - comparison belongs
                            // to I2 Trade-off Analysis.
                            try {
                                AlternativeSet alternativeSet =
                                        new DefaultAlternativeGenerationEngine().generate(
                                                reasoningGraph, synthesisGraph,
                                                causalGraph, verificationGraph);
                                state.updateCognitiveState(
                                        cs -> cs.withAlternativeSet(alternativeSet));
                                state.addMetadata("alternativeSet", alternativeSet);
                                state.addMessage("I1 alternative generation completed: "
                                        + alternativeSet.size() + " alternatives");
                            } catch (Exception e) {
                                state.addMessage("I1 alternative generation skipped: " + e.getMessage());
                            }

                            // I2 - Trade-off Analysis: compare every
                            // generated alternative deterministically. The
                            // engine compares only - selecting the winner
                            // belongs to I3 Decision Optimization.
                            try {
                                AlternativeSet generated =
                                        state.getCognitiveState().alternativeSet();
                                if (generated != null && !generated.isEmpty()) {
                                    TradeoffAnalysisSet analysisSet =
                                            new DefaultTradeoffAnalysisEngine().analyze(
                                                    generated,
                                                    state.getCognitiveState().userConstraints(),
                                                    verificationGraph,
                                                    state.getCognitiveState().uncertaintyGraph());
                                    state.addMetadata("tradeoffAnalysisSet", analysisSet);
                                    state.addMessage("I2 trade-off analysis completed: "
                                            + analysisSet.size() + " analyses");

                                    // I3 - Decision Optimization: select the
                                    // single optimized decision from the
                                    // trade-off analysis set with deterministic
                                    // weighted optimization. The engine runs
                                    // only when a candidate is available - an
                                    // empty decision space carries no decision.
                                    try {
                                        if (!analysisSet.isEmpty()) {
                                            OptimizedDecision optimizedDecision =
                                                    new DefaultDecisionOptimizationEngine().decide(
                                                            generated,
                                                            analysisSet,
                                                            state.getCognitiveState().userConstraints(),
                                                            verificationGraph,
                                                            state.getCognitiveState().uncertaintyGraph());
                                            state.updateCognitiveState(
                                                    cs -> cs.withOptimizedDecision(optimizedDecision));
                                            state.addMetadata("optimizedDecision", optimizedDecision);
                                            state.addMessage("I3 decision optimization completed: "
                                                    + optimizedDecision.strategy() + " selected"
                                                    + " (score=" + optimizedDecision.optimizationScore()
                                                    + ", justifications="
                                                    + optimizedDecision.justifications().size() + ")");
                                        }
                                    } catch (Exception e) {
                                        state.addMessage("I3 decision optimization skipped: " + e.getMessage());
                                    }

                                    // I4 - Confidence Calibration: measure
                                    // how trustworthy the optimized
                                    // decision is, deterministically. The
                                    // engine calibrates only - it never
                                    // changes the selected decision.
                                    try {
                                        if (state.getCognitiveState().optimizedDecision() != null) {
                                            CalibratedDecision calibratedDecision =
                                                    new DefaultConfidenceCalibrationEngine().calibrate(
                                                            state.getCognitiveState().optimizedDecision(),
                                                            analysisSet,
                                                            generated,
                                                            state.getCognitiveState().userConstraints(),
                                                            verificationGraph,
                                                            state.getCognitiveState().uncertaintyGraph());
                                            state.updateCognitiveState(
                                                    cs -> cs.withCalibratedDecision(calibratedDecision));
                                            state.addMetadata("calibratedDecision", calibratedDecision);
                                            state.addMessage("I4 confidence calibration completed: "
                                                    + calibratedDecision.confidence() + " ("
                                                    + calibratedDecision.level() + ", factors="
                                                    + calibratedDecision.factors().size() + ")");
                                        }
                                    } catch (Exception e) {
                                        state.addMessage("I4 confidence calibration skipped: " + e.getMessage());
                                    }

                                    // I5 - Explainable Decision: convert
                                    // all cognitive artifacts into a
                                    // single structured, display-ready
                                    // decision explanation. The engine
                                    // generates structure only - never
                                    // natural language.
                                    try {
                                        if (state.getCognitiveState().calibratedDecision() != null) {
                                            ExplainableDecision explainableDecision =
                                                    new DefaultExplainableDecisionEngine().explain(
                                                            state.getCognitiveState().optimizedDecision(),
                                                            state.getCognitiveState().calibratedDecision(),
                                                            generated,
                                                            analysisSet,
                                                            state.getCognitiveState().goalStructure(),
                                                            state.getCognitiveState().userConstraints(),
                                                            verificationGraph);
                                            state.updateCognitiveState(
                                                    cs -> cs.withExplainableDecision(explainableDecision));
                                            state.addMetadata("explainableDecision", explainableDecision);
                                            state.addMessage("I5 explainable decision completed: "
                                                    + explainableDecision.sections().size()
                                                    + " sections");
                                        }
                                    } catch (Exception e) {
                                        state.addMessage("I5 explainable decision skipped: " + e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                state.addMessage("I2 trade-off analysis skipped: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            state.addMessage("R4 self verification skipped: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        state.addMessage("R3 causal reasoning skipped: " + e.getMessage());
                    }
                } catch (Exception e) {
                    state.addMessage("R2 evidence synthesis skipped: " + e.getMessage());
                }
            } catch (Exception e) {
                state.addMessage("R1 multi-hop reasoning skipped: " + e.getMessage());
            }

            // P0.2 â Store the reasoning artifact in the immutable cognitive
            // state. Downstream stages consume the reasoning output via
            // state.getCognitiveState().reasoning() â the artifact is no
            // longer decomposed into the metadata map.
            state.updateCognitiveState(cs -> cs.withReasoning(result));
            state.addMessage("Reasoning completed: " + result.conclusion());

            publishReasoningEvent(
                    context,
                    requestId,
                    "COGNITIVE"
            );

            return chain.next(context, state);

        } catch (Exception e) {

            publishReasoningEvent(
                    context,
                    context.getExecutionRequest() != null
                            ? context.getExecutionRequest().getRequestId()
                            : "unknown",
                    "FAILED"
            );

            state.markFailure("Reasoning failed: " + e.getMessage());

            return PipelineResult.builder()
                    .success(false)
                    .status("REASONING_FAILED")
                    .addMessage("Reasoning stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private void publishReasoningEvent(
            PipelineContext context,
            String requestId,
            String reasoningMode
    ) {
        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.REASONING_COMPLETED,
                        requestId,
                        "Reasoning",
                        Instant.now(),
                        Map.of(
                                "reasoningMode", reasoningMode
                        )
                )
        );
    }

    private ReliabilityResult createFallbackReliability(
            String requestId,
            String requestText,
            ReasoningResult result,
            List<KnowledgeNode> rankedKnowledge
    ) {
        double baselineConfidence = (result != null && result.confidence() > 0.0)
                ? Math.max(0.0, Math.min(1.0, result.confidence()))
                : 0.80;

        List<TrustedEvidence> trustedEvidenceList = new ArrayList<>();

        if (rankedKnowledge != null && !rankedKnowledge.isEmpty()) {
            for (int i = 0; i < rankedKnowledge.size(); i++) {
                KnowledgeNode kn = rankedKnowledge.get(i);
                String chunkId = (kn.getId() != null && !kn.getId().value().isBlank())
                        ? kn.getId().value()
                        : ("kwn-" + i);
                String docId = "doc-" + chunkId;
                String label = kn.getLabel() != null ? kn.getLabel().trim() : "";
                String desc = kn.getDescription() != null ? kn.getDescription().trim() : "";
                String content;
                if (!label.isBlank() && !desc.isBlank()) {
                    content = label + ": " + desc;
                } else if (!desc.isBlank()) {
                    content = desc;
                } else if (!label.isBlank()) {
                    content = label;
                } else {
                    content = "Knowledge item " + chunkId;
                }
                List<String> matchedConcepts = label.isBlank() ? List.of() : List.of(label);
                EvidenceItem item = new EvidenceItem(chunkId, docId, content, baselineConfidence, matchedConcepts);
                TrustScore trust = new TrustScore(baselineConfidence, baselineConfidence, baselineConfidence, baselineConfidence, "Baseline knowledge trust");
                trustedEvidenceList.add(new TrustedEvidence(item, trust));
            }
        }

        if (requestText != null && !requestText.isBlank()) {
            String chunkId = "req-" + (requestId != null && !requestId.isBlank() ? requestId : "0");
            String docId = "doc-request";
            EvidenceItem item = new EvidenceItem(chunkId, docId, requestText.trim(), baselineConfidence, List.of());
            TrustScore trust = new TrustScore(baselineConfidence, baselineConfidence, baselineConfidence, baselineConfidence, "Baseline user request evidence");
            trustedEvidenceList.add(new TrustedEvidence(item, trust));
        }

        if (trustedEvidenceList.isEmpty()) {
            EvidenceItem item = new EvidenceItem("default-0", "doc-default", "Default cognitive evidence", baselineConfidence, List.of());
            TrustScore trust = new TrustScore(baselineConfidence, baselineConfidence, baselineConfidence, baselineConfidence, "Default cognitive evidence");
            trustedEvidenceList.add(new TrustedEvidence(item, trust));
        }

        return new ReliabilityResult(trustedEvidenceList, baselineConfidence);
    }

    private ConceptGraph createFallbackConceptGraph(
            String requestText,
            List<KnowledgeNode> rankedKnowledge
    ) {
        List<GraphConcept> concepts = new ArrayList<>();
        Set<String> seenNames = new LinkedHashSet<>();

        if (rankedKnowledge != null) {
            for (KnowledgeNode kn : rankedKnowledge) {
                if (kn.getLabel() != null && !kn.getLabel().isBlank()) {
                    String name = kn.getLabel().trim();
                    if (seenNames.add(name.toLowerCase())) {
                        String conceptId = sha256("CONCEPT|" + name);
                        concepts.add(new GraphConcept(conceptId, name, ConceptType.GENERAL));
                    }
                }
            }
        }

        if (concepts.size() < 3 && requestText != null && !requestText.isBlank()) {
            String[] words = requestText.replaceAll("[^a-zA-Z0-9 ]", " ").trim().split("\\s+");
            for (String word : words) {
                String trimmed = word.trim();
                if (trimmed.length() >= 3 && !isStopWord(trimmed) && seenNames.add(trimmed.toLowerCase())) {
                    String name = Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase();
                    String conceptId = sha256("CONCEPT|" + name);
                    concepts.add(new GraphConcept(conceptId, name, ConceptType.GENERAL));
                    if (concepts.size() >= 5) {
                        break;
                    }
                }
            }
        }

        if (concepts.isEmpty()) {
            String fallbackName = "Cognitive Reasoning";
            concepts.add(new GraphConcept(sha256("CONCEPT|" + fallbackName), fallbackName, ConceptType.GENERAL));
        }

        List<ConceptRelationship> relationships = new ArrayList<>();
        if (concepts.size() >= 2) {
            for (int i = 0; i < concepts.size() - 1; i++) {
                GraphConcept from = concepts.get(i);
                GraphConcept to = concepts.get(i + 1);
                if (!from.conceptId().equals(to.conceptId())) {
                    String relId = sha256(from.conceptId() + "|RELATED_TO|" + to.conceptId());
                    relationships.add(new ConceptRelationship(
                            relId,
                            from.conceptId(),
                            to.conceptId(),
                            RelationshipType.RELATED_TO,
                            0.80
                    ));
                }
            }
        }

        return new ConceptGraph(concepts, relationships);
    }

    private static boolean isStopWord(String word) {
        String lower = word.toLowerCase();
        return switch (lower) {
            case "the", "and", "for", "what", "how", "why", "who", "when",
                 "where", "can", "you", "are", "with", "this", "that", "from",
                 "about", "all", "any", "some", "our", "your", "has", "have" -> true;
            default -> false;
        };
    }

    private static String sha256(String input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}