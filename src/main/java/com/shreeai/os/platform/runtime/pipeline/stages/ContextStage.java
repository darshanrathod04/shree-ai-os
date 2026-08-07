package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

/**
 * ContextStage - Builds and enriches the execution context.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Building the execution context from the request</li>
 *   <li>Enriching context with identity information</li>
 *   <li>Preparing context for downstream kernel stages</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class ContextStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Context")
            .priority(2)
            .enabled(true)
            .version("1.0")
            .description("Builds and enriches execution context")
            .build();

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve identity information from previous stage
            String identityId = (String) state.getMetadata().get("identityId");
            String identityType = (String) state.getMetadata().get("identityType");

            // Build context information
            String contextId = "ctx-" + System.currentTimeMillis();
            String contextType = "EXECUTION_CONTEXT";

            // Store context information in state
            state.addMetadata("contextId", contextId);
            state.addMetadata("contextType", contextType);
            state.addMetadata("contextBuilt", true);
            state.addMessage("Context built: " + contextId + " for identity " + identityId);

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Context building failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("CONTEXT_FAILED")
                    .addMessage("Context stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}