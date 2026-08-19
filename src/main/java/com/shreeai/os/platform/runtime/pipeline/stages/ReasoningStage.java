package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
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

    /**
     * Creates a new ReasoningStage with a real reasoning engine.
     *
     * @param reasoningEngine the reasoning engine
     */
    public ReasoningStage(DefaultReasoningEngine reasoningEngine) {
        this.reasoningEngine = reasoningEngine;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses a new DefaultReasoningEngine instance.
     */
    public ReasoningStage() {
        this(new DefaultReasoningEngine());
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve memory and knowledge information from previous stages
            String knowledgeId = (String) state.getMetadata().get("knowledgeId");
            String memoryId = (String) state.getMetadata().get("memoryId");
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

            // Store the full ReasoningResult in state so downstream stages
            // consume the actual reasoning output without information loss
            state.addMetadata("reasoningResult", result);
            state.addMetadata("reasoningId", result.reasoningId());
            state.addMetadata("reasoningSummary", result.summary());
            state.addMetadata("reasoningConfidence", result.confidence());
            state.addMetadata("reasoningFindings", result.findings());
            state.addMetadata("reasoningEvidence", result.evidence());
            state.addMetadata("reasoningAlternatives", result.alternatives());
            state.addMetadata("reasoningRisk", result.risks());
            state.addMetadata("reasoningConclusion", result.conclusion());
            state.addMetadata("reasoningType", result.reasoningType());
            state.addMetadata("reasoningSteps", result.reasoningSteps());
            state.addMetadata("reasoningScope", result.scope());
            state.addMetadata("reasoningCompleted", true);
            state.addMessage("Reasoning completed: " + result.conclusion());

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
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
}