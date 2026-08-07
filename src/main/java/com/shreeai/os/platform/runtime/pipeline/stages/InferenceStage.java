package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

import java.util.List;

/**
 * InferenceStage - Performs real inference and hypothesis generation.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Consuming reasoning results from ReasoningStage</li>
 *   <li>Generating hypotheses from evidence</li>
 *   <li>Selecting most likely hypothesis</li>
 *   <li>Identifying unknown information</li>
 *   <li>Suggesting next investigation</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 7
 */
public final class InferenceStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Inference")
            .priority(6)
            .enabled(true)
            .version("1.0")
            .description("Performs inference and hypothesis generation")
            .build();

    private final DefaultInferenceEngine inferenceEngine;

    /**
     * Creates a new InferenceStage with a real inference engine.
     *
     * @param inferenceEngine the inference engine
     */
    public InferenceStage(DefaultInferenceEngine inferenceEngine) {
        this.inferenceEngine = inferenceEngine;
    }

    /**
     * Default constructor for backward compatibility.
     */
    public InferenceStage() {
        this(new DefaultInferenceEngine());
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Get request text
            String requestText = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().toString() 
                    : "";
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

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

            // Get reasoning result from state
            String reasoningConclusion = (String) state.getMetadata().get("reasoningConclusion");
            Double reasoningConfidence = (Double) state.getMetadata().get("reasoningConfidence");
            if (reasoningConfidence == null) {
                reasoningConfidence = 0.0;
            }
            
            // Build ReasoningResult from state metadata
            ReasoningResult reasoningResult = new ReasoningResult(
                    (String) state.getMetadata().get("reasoningId"),
                    "Reasoning summary",
                    List.of(), // findings
                    List.of(), // evidence
                    reasoningConclusion != null ? reasoningConclusion : "No conclusion",
                    reasoningConfidence,
                    List.of(), // risks
                    List.of(), // alternatives
                    "general",
                    "EVIDENCE_BASED_REASONING",
                    0,
                    java.util.Map.of(),
                    java.time.Instant.now()
            );

            // Run inference engine
            InferenceResult result = inferenceEngine.infer(
                    requestText,
                    reasoningResult,
                    rankedMemories,
                    rankedKnowledge,
                    "request-" + requestId
            );

            // Store inference information in state
            state.addMetadata("inferenceId", result.inferenceId());
            state.addMetadata("hypotheses", result.hypotheses());
            state.addMetadata("bestHypothesis", result.bestHypothesis().description());
            state.addMetadata("inferenceConfidence", result.confidence());
            state.addMetadata("supportingEvidence", result.supportingEvidence());
            state.addMetadata("contradictingEvidence", result.contradictingEvidence());
            state.addMetadata("unknowns", result.unknownInformation());
            state.addMetadata("nextInvestigation", result.recommendedNextInvestigation());
            state.addMetadata("inferenceCompleted", true);
            state.addMessage("Inference completed: " + result.bestHypothesis().description());

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Inference failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("INFERENCE_FAILED")
                    .addMessage("Inference stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}