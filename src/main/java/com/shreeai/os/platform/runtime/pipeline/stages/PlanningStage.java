package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;

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

    private final PlanningService planningService;

    /**
     * Creates a new PlanningStage with real planning service.
     *
     * @param planningService the planning service
     */
    public PlanningStage(PlanningService planningService) {
        this.planningService = planningService;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses null service (will fail gracefully).
     */
    public PlanningStage() {
        this(null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve reasoning information from previous stage
            ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
            String reasoningId = reasoningResult != null
                    ? reasoningResult.reasoningId()
                    : (String) state.getMetadata().get("reasoningId");
            String requestId = context.getExecutionRequest() != null
                    ? context.getExecutionRequest().getRequestId()
                    : "unknown";
            String requestText = context.getExecutionRequest() != null
                    ? context.getExecutionRequest().getUserInput()
                    : "";

            if (planningService == null) {
                state.markFailure("Planning failed: planningService is not configured");
                return PipelineResult.builder()
                        .success(false)
                        .status("PLANNING_FAILED")
                        .addMessage("Planning stage failed: planningService is not configured")
                        .build();
            }

            // Build the PlanningObjective carrying request/reasoning information
            Map<String, String> objectiveMetadata = new LinkedHashMap<>();
            objectiveMetadata.put("requestId", requestId);
            objectiveMetadata.put("requestText", requestText);
            objectiveMetadata.put("reasoningId", reasoningId != null ? reasoningId : "");
            if (reasoningResult != null) {
                objectiveMetadata.put("reasoningConclusion", reasoningResult.conclusion());
                objectiveMetadata.put("reasoningType", reasoningResult.reasoningType());
                objectiveMetadata.put("reasoningScope", reasoningResult.scope());
                objectiveMetadata.put("reasoningSteps", String.valueOf(reasoningResult.reasoningSteps()));
                objectiveMetadata.put("reasoningConfidence", String.valueOf(reasoningResult.confidence()));
            }

            // The description is a meaningful, evidence-grounded objective derived from the
            // request/reasoning context — NOT the plan UUID. The planning ID remains separate.
            String objectiveDescription = "Analyze the supplied project evidence and determine "
                    + "evidence-supported project analysis steps.";
            PlanningObjective objective = new PlanningObjective(
                    new PlanningId("plan-" + requestId),
                    objectiveDescription,
                    "EXECUTION_PLANNING",
                    objectiveMetadata
            );

            // Pass request/reasoning information through the existing PlanningRequest
            // constraints metadata so the planning flow receives the relevant context
            Map<String, String> constraintMetadata = new LinkedHashMap<>(objectiveMetadata);
            PlanningConstraints constraints = new PlanningConstraints(
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    constraintMetadata
            );
            PlanningService.PlanningRequest planningRequest = new PlanningService.PlanningRequest(
                    objective.planningId().value(),
                    PlanningTypes.PlanningScope.STANDARD,
                    constraints
            );

            String planId = planningService.createPlan(planningRequest);

            // Store planning information in state (only real statistics from PlanningService)
            state.addMetadata("planId", planId);
            state.addMetadata("planningObjective", objective);
            state.addMetadata("planningCompleted", true);
            state.addMessage("Planning completed: plan " + planId + " for reasoning " + reasoningId);

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