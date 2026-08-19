package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalRequest;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PlanningStage - Converts upstream cognitive intelligence into an
 * evidence-aware execution plan.
 *
 * <p>The stage now incorporates Goal Intelligence before creating the
 * PlanningObjective. This keeps the canonical ten-stage runtime pipeline
 * unchanged while making planning goal-aware.</p>
 *
 * <p>Flow:</p>
 *
 * <pre>
 * ReasoningResult
 *      +
 * Inference metadata
 *      ↓
 * GoalIntelligenceEngine
 *      ↓
 * GoalAnalysis
 *      ↓
 * PlanningObjective
 *      ↓
 * PlanningService
 * </pre>
 */
public final class PlanningStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR =
            PipelineStageDescriptor.builder()
                    .stageName("Planning")
                    .priority(7)
                    .enabled(true)
                    .version("2.0")
                    .description(
                            "Creates a goal-aware execution plan from reasoning and inference intelligence"
                    )
                    .build();

    private final PlanningService planningService;
    private final GoalIntelligenceEngine goalIntelligenceEngine;

    /**
     * Creates a PlanningStage with explicit dependencies.
     */
    public PlanningStage(
            PlanningService planningService,
            GoalIntelligenceEngine goalIntelligenceEngine) {

        this.planningService = planningService;
        this.goalIntelligenceEngine = goalIntelligenceEngine;
    }

    /**
     * Creates a PlanningStage with the real Planning service and the
     * canonical Goal Intelligence engine.
     */
    public PlanningStage(PlanningService planningService) {
        this(
                planningService,
                new GoalIntelligenceEngine()
        );
    }

    /**
     * Default constructor retained for compatibility.
     */
    public PlanningStage() {
        this(
                null,
                new GoalIntelligenceEngine()
        );
    }

    @Override
    public PipelineResult process(
            PipelineContext context,
            ExecutionChain chain,
            PipelineExecutionState state) {

        try {

            if (context == null) {
                return failure(
                        "Planning stage requires a non-null PipelineContext"
                );
            }

            if (state == null) {
                return failure(
                        "Planning stage requires a non-null PipelineExecutionState"
                );
            }

            if (chain == null) {
                return failure(
                        "Planning stage requires a non-null ExecutionChain"
                );
            }

            if (planningService == null) {

                state.markFailure(
                        "Planning failed: planningService is not configured"
                );

                return PipelineResult.builder()
                        .success(false)
                        .status("PLANNING_FAILED")
                        .addMessage(
                                "Planning stage failed: planningService is not configured"
                        )
                        .build();
            }

            /*
             * -------------------------------------------------------------
             * 1. Extract request information
             * -------------------------------------------------------------
             */

            String requestId =
                    context.getExecutionRequest() != null
                            ? context.getExecutionRequest().getRequestId()
                            : "unknown";

            String requestText =
                    context.getExecutionRequest() != null
                            && context.getExecutionRequest().getUserInput() != null
                            ? context.getExecutionRequest().getUserInput()
                            : "";

            /*
             * -------------------------------------------------------------
             * 2. Retrieve authoritative reasoning
             * -------------------------------------------------------------
             */

            ReasoningResult reasoningResult =
                    readReasoningResult(state);

            String reasoningId =
                    reasoningResult != null
                            ? reasoningResult.reasoningId()
                            : stringMetadata(
                            state,
                            "reasoningId"
                    );

            /*
             * -------------------------------------------------------------
             * 3. Read inference intelligence
             * -------------------------------------------------------------
             */

            List<String> supportingEvidence =
                    readStringList(
                            state,
                            "supportingEvidence"
                    );

            List<String> contradictingEvidence =
                    readStringList(
                            state,
                            "contradictingEvidence"
                    );

            List<String> unknowns =
                    readStringList(
                            state,
                            "unknowns"
                    );

            String nextInvestigation =
                    stringMetadata(
                            state,
                            "nextInvestigation"
                    );

            /*
             * -------------------------------------------------------------
             * 4. Build Goal Intelligence request
             * -------------------------------------------------------------
             *
             * The goal intelligence layer receives actual upstream
             * cognitive evidence instead of independently inventing
             * context.
             */

            List<String> evidence =
                    new ArrayList<>(supportingEvidence);

            if (reasoningResult != null
                    && reasoningResult.conclusion() != null
                    && !reasoningResult.conclusion().isBlank()) {

                evidence.add(
                        reasoningResult.conclusion()
                );
            }

            List<String> blockers =
                    new ArrayList<>();

            blockers.addAll(
                    contradictingEvidence
            );

            blockers.addAll(
                    unknowns
            );

            List<String> dependencies =
                    new ArrayList<>();

            if (nextInvestigation != null
                    && !nextInvestigation.isBlank()) {

                dependencies.add(
                        nextInvestigation
                );
            }

            double reasoningConfidence =
                    reasoningResult != null
                            ? clamp(
                            reasoningResult.confidence()
                    )
                            : 0.0;

            int confidenceScore =
                    (int) Math.round(
                            reasoningConfidence * 10.0
                    );

            /*
             * -------------------------------------------------------------
             * 5. Analyze the goal
             * -------------------------------------------------------------
             */

            GoalRequest goalRequest =
                    new GoalRequest(
                            "goal-" + requestId,
                            requestText,
                            evidence,
                            List.of(),
                            List.of(),
                            blockers,
                            dependencies,
                            List.of(),
                            null,
                            confidenceScore,
                            confidenceScore,
                            confidenceScore,
                            null,
                            GoalIntelligenceEngine.GoalStability.STABLE,
                            false
                    );

            GoalAnalysis goalAnalysis =
                    goalIntelligenceEngine.analyze(
                            goalRequest
                    );

            /*
             * -------------------------------------------------------------
             * 6. Build goal-aware PlanningObjective
             * -------------------------------------------------------------
             */

            Map<String, String> objectiveMetadata =
                    new LinkedHashMap<>();

            objectiveMetadata.put(
                    "requestId",
                    requestId
            );

            objectiveMetadata.put(
                    "requestText",
                    requestText
            );

            objectiveMetadata.put(
                    "reasoningId",
                    safe(reasoningId)
            );

            /*
             * Preserve authoritative reasoning information.
             */

            if (reasoningResult != null) {

                objectiveMetadata.put(
                        "reasoningConclusion",
                        safe(reasoningResult.conclusion())
                );

                objectiveMetadata.put(
                        "reasoningType",
                        safe(reasoningResult.reasoningType())
                );

                objectiveMetadata.put(
                        "reasoningScope",
                        safe(reasoningResult.scope())
                );

                objectiveMetadata.put(
                        "reasoningSteps",
                        String.valueOf(
                                reasoningResult.reasoningSteps()
                        )
                );

                objectiveMetadata.put(
                        "reasoningConfidence",
                        String.valueOf(
                                reasoningResult.confidence()
                        )
                );
            }

            /*
             * -------------------------------------------------------------
             * Goal Intelligence metadata
             * -------------------------------------------------------------
             */

            objectiveMetadata.put(
                    "goalIntelligenceSource",
                    "GoalIntelligenceEngine"
            );

            objectiveMetadata.put(
                    "goalAnalysisId",
                    goalAnalysis.analysisId()
            );

            objectiveMetadata.put(
                    "goalStatus",
                    goalAnalysis.status().name()
            );

            objectiveMetadata.put(
                    "goalPriority",
                    goalAnalysis.priority().name()
            );

            objectiveMetadata.put(
                    "goalFeasibility",
                    goalAnalysis.feasibility().name()
            );

            objectiveMetadata.put(
                    "goalProgress",
                    String.valueOf(
                            goalAnalysis.progress()
                    )
            );

            objectiveMetadata.put(
                    "goalConfidence",
                    String.valueOf(
                            goalAnalysis.confidence()
                    )
            );

            objectiveMetadata.put(
                    "goalConfidenceBand",
                    goalAnalysis.confidenceBand().name()
            );

            objectiveMetadata.put(
                    "goalDecompositionRequired",
                    String.valueOf(
                            goalAnalysis.decompositionRequired()
                    )
            );

            objectiveMetadata.put(
                    "goalReplanningRelevant",
                    String.valueOf(
                            goalAnalysis.replanningRelevant()
                    )
            );

            objectiveMetadata.put(
                    "goalSubtasks",
                    String.valueOf(
                            goalAnalysis.subtasks()
                    )
            );

            objectiveMetadata.put(
                    "goalDependencies",
                    String.valueOf(
                            goalAnalysis.dependencies()
                    )
            );

            objectiveMetadata.put(
                    "goalBlockers",
                    String.valueOf(
                            goalAnalysis.blockers()
                    )
            );

            objectiveMetadata.put(
                    "goalConflicts",
                    String.valueOf(
                            goalAnalysis.conflicts()
                    )
            );

            objectiveMetadata.put(
                    "goalMissingInformation",
                    String.valueOf(
                            goalAnalysis.requiredInformation()
                    )
            );

            objectiveMetadata.put(
                    "goalEvolutionSignals",
                    String.valueOf(
                            goalAnalysis.evolutionSignals()
                    )
            );

            objectiveMetadata.put(
                    "goalRecommendations",
                    String.valueOf(
                            goalAnalysis.recommendations()
                    )
            );

            /*
             * The Planning Kernel can consume these through its existing
             * metadata-aware processing contract.
             */

            objectiveMetadata.put(
                    "goalEvidence",
                    String.valueOf(
                            goalAnalysis.evidence()
                    )
            );

            objectiveMetadata.put(
                    "goalConstraints",
                    String.valueOf(
                            goalAnalysis.constraints()
                    )
            );

            /*
             * -------------------------------------------------------------
             * 7. Create immutable PlanningObjective
             * -------------------------------------------------------------
             */

            String objectiveDescription =
                    goalAnalysis.normalizedGoal();

            if (objectiveDescription == null
                    || objectiveDescription.isBlank()) {

                objectiveDescription =
                        "Analyze the supplied project evidence and determine "
                                + "evidence-supported project analysis steps.";
            }

            PlanningObjective objective =
                    new PlanningObjective(
                            new PlanningId(
                                    "plan-" + requestId
                            ),
                            objectiveDescription,
                            "EXECUTION_PLANNING",
                            objectiveMetadata
                    );

            /*
             * -------------------------------------------------------------
             * 8. Build PlanningRequest
             * -------------------------------------------------------------
             */

            Map<String, String> constraintMetadata =
                    new LinkedHashMap<>(
                            objectiveMetadata
                    );

            PlanningConstraints constraints =
                    new PlanningConstraints(
                            Map.of(),
                            Map.of(),
                            Map.of(),
                            constraintMetadata
                    );

            PlanningService.PlanningRequest planningRequest =
                    new PlanningService.PlanningRequest(
                            objective.planningId().value(),
                            selectPlanningScope(goalAnalysis),
                            constraints
                    );

            /*
             * -------------------------------------------------------------
             * 9. Execute existing Planning Kernel
             * -------------------------------------------------------------
             */

            String planId =
                    planningService.createPlan(
                            planningRequest
                    );

            /*
             * -------------------------------------------------------------
             * 10. Preserve Goal Intelligence in runtime state
             * -------------------------------------------------------------
             */

            state.addMetadata(
                    "goalAnalysis",
                    goalAnalysis
            );

            state.addMetadata(
                    "goalIntelligenceCompleted",
                    true
            );

            state.addMetadata(
                    "goalIntelligenceConfidence",
                    goalAnalysis.confidence()
            );

            state.addMetadata(
                    "goalReplanningRelevant",
                    goalAnalysis.replanningRelevant()
            );

            state.addMetadata(
                    "goalEvolutionSignals",
                    goalAnalysis.evolutionSignals()
            );

            state.addMetadata(
                    "planId",
                    planId
            );

            state.addMetadata(
                    "planningObjective",
                    objective
            );

            state.addMetadata(
                    "planningCompleted",
                    true
            );

            state.addMessage(
                    "Goal intelligence completed: "
                            + goalAnalysis.status()
                            + ", priority="
                            + goalAnalysis.priority()
                            + ", confidence="
                            + goalAnalysis.confidence()
            );

            state.addMessage(
                    "Planning completed: plan "
                            + planId
                            + " for reasoning "
                            + reasoningId
            );

            /*
             * -------------------------------------------------------------
             * 11. Continue canonical pipeline
             * -------------------------------------------------------------
             */

            return chain.next(
                    context,
                    state
            );

        } catch (Exception e) {

            state.markFailure(
                    "Planning failed: "
                            + safeMessage(e)
            );

            return PipelineResult.builder()
                    .success(false)
                    .status("PLANNING_FAILED")
                    .addMessage(
                            "Planning stage failed: "
                                    + safeMessage(e)
                    )
                    .build();
        }
    }

    /**
     * Returns the canonical stage descriptor.
     */
    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * Reads the authoritative reasoning result.
     */
    private ReasoningResult readReasoningResult(
            PipelineExecutionState state) {

        Object value =
                state.getMetadata()
                        .get("reasoningResult");

        if (value instanceof ReasoningResult result) {
            return result;
        }

        return null;
    }

    /**
     * Reads a string metadata value safely.
     */
    private String stringMetadata(
            PipelineExecutionState state,
            String key) {

        Object value =
                state.getMetadata()
                        .get(key);

        return value != null
                ? String.valueOf(value)
                : "";
    }

    /**
     * Reads list metadata while protecting the pipeline from
     * incompatible metadata values.
     */
    private List<String> readStringList(
            PipelineExecutionState state,
            String key) {

        Object value =
                state.getMetadata()
                        .get(key);

        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> result =
                new ArrayList<>();

        for (Object item : list) {

            if (item != null) {
                result.add(
                        String.valueOf(item)
                );
            }
        }

        return List.copyOf(result);
    }

    /**
     * Maps Goal Intelligence feasibility/status into the existing
     * Planning scope without changing the Planning Kernel contract.
     */
    private PlanningTypes.PlanningScope selectPlanningScope(
            GoalAnalysis goalAnalysis) {

        if (goalAnalysis.decompositionRequired()
                || goalAnalysis.replanningRelevant()) {

            return PlanningTypes.PlanningScope.COMPREHENSIVE;
        }

        if (goalAnalysis.subtasks().size() > 1) {
            return PlanningTypes.PlanningScope.DEEP;
        }

        return PlanningTypes.PlanningScope.STANDARD;
    }

    private String safe(String value) {
        return value != null
                ? value
                : "";
    }

    private String safeMessage(Exception e) {
        return e.getMessage() != null
                ? e.getMessage()
                : e.getClass().getSimpleName();
    }

    private double clamp(double value) {
        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        value
                )
        );
    }

    private PipelineResult failure(
            String message) {

        return PipelineResult.builder()
                .success(false)
                .status("PLANNING_FAILED")
                .addMessage(message)
                .build();
    }
}