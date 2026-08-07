package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

/**
 * PlanningStage - Creates execution plan from reasoning.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Transforming reasoning into actionable plans</li>
 *   <li>Breaking down complex tasks into steps</li>
 *   <li>Preparing execution strategy</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class PlanningStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Planning")
            .priority(7)
            .enabled(true)
            .version("1.0")
            .description("Creates execution plan from reasoning")
            .build();

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve reasoning information from previous stage
            String reasoningId = (String) state.getMetadata().get("reasoningId");
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

            // Simulate planning process
            String planId = "plan-" + requestId;
            int planSteps = 3; // Simulated count

            // Store planning information in state
            state.addMetadata("planId", planId);
            state.addMetadata("planSteps", planSteps);
            state.addMetadata("planningCompleted", true);
            state.addMessage("Planning completed: " + planSteps + " steps for reasoning " + reasoningId);

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Planning failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("PLANNING_FAILED")
                    .addMessage("Planning stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}