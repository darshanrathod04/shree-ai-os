package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
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

            // R1 Multi-Hop Reasoning integration (backward-compatible)
            // If the knowledge pipeline stored a ReliabilityResult and ConceptGraph
            // in state metadata, run the multi-hop engine and attach the
            // ReasoningGraph to CognitiveState.
            try {
                Object reliabilityObj = state.getMetadata().get("reliabilityResult");
                Object conceptGraphObj = state.getMetadata().get("conceptGraph");
                if (reliabilityObj instanceof ReliabilityResult reliability
                        && conceptGraphObj instanceof ConceptGraph conceptGraph) {
                    ReasoningGraph reasoningGraph = multiHopEngine.reason(reliability, conceptGraph);
                    state.updateCognitiveState(cs -> cs.withReasoningGraph(reasoningGraph));
                    state.addMessage("R1 multi-hop reasoning completed: "
                            + reasoningGraph.nodes().size() + " nodes, "
                            + reasoningGraph.hypotheses().size() + " hypotheses");

                    // R2 - Evidence Synthesis: merge trusted evidence into a
                    // unified, provenance-preserving SynthesisGraph.
                    try {
                        SynthesisGraph synthesisGraph = synthesisEngine.synthesize(reasoningGraph, reliability, conceptGraph);
                        state.updateCognitiveState(cs -> cs.withSynthesisGraph(synthesisGraph));
                        state.addMessage("R2 evidence synthesis completed: "
                                + synthesisGraph.clusterCount() + " clusters, "
                                + synthesisGraph.factCount() + " facts");

                        // R3 - Causal Reasoning: discover cause-effect relationships
                        // from the SynthesisGraph and ReasoningGraph.
                        try {
                            CausalGraph causalGraph = causalEngine.analyze(reasoningGraph, synthesisGraph, conceptGraph);
                            state.updateCognitiveState(cs -> cs.withCausalGraph(causalGraph));
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
                                    state.addMessage("R5 uncertainty modeling completed: "
                                            + uncertaintyGraph.nodes().size() + " nodes, "
                                            + uncertaintyGraph.issues().size() + " issues, "
                                            + "certainty=" + uncertaintyGraph.overallCertainty());
                                } catch (Exception e) {
                                    state.addMessage("R5 uncertainty modeling skipped: " + e.getMessage());
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
}