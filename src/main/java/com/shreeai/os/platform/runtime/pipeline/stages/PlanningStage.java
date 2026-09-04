package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalRequest;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.response.contracts.PlanningResponse;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.kernels.planning.response.PlanningResponseBuilder;
import com.shreeai.os.platform.kernels.response.contracts.PlanningResponse;

import java.time.Instant;
import java.util.Map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * PlanningStage - Converts upstream cognitive intelligence into an
 * evidence-aware execution plan.
 *
  * <p>The stage now incorporates Goal Intelligence before creating the
 * PlanningObjective. This keeps the canonical eleven-stage runtime pipeline
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
    private final PlanningResponseBuilder responseBuilder =
            new PlanningResponseBuilder();

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

            // Prefer the developer-supplied planning objective (set by the
            // SDK) so goal intelligence classifies the real intent rather
            // than the SDK message marker. Falls back to the request text.
            String goalText = requestObjective(context);

            if (goalText.isBlank()) {
                goalText = requestText;
            }

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
                            goalText,
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

            PlanningResponse planningResponse =
                    responseBuilder.build(goalAnalysis);

            PlanningResponse response =
                    responseBuilder.build(goalAnalysis);

            /*
             * -------------------------------------------------------------
             * 6. Build goal-aware PlanningObjective
             * -------------------------------------------------------------
             */

            Map<String, Object> objectiveMetadata = new LinkedHashMap<>();

            objectiveMetadata.put("goalIntelligenceSource", "GoalIntelligenceEngine");
            objectiveMetadata.put("requestId", requestId);
            objectiveMetadata.put("requestText", requestText);
            objectiveMetadata.put("reasoningId", reasoningId);
            objectiveMetadata.put("reasoningConfidence", reasoningConfidence);
            objectiveMetadata.put("goalAnalysisId", goalAnalysis.analysisId());
            objectiveMetadata.put("goalStatus", goalAnalysis.status().name());
            objectiveMetadata.put("goalPriority", goalAnalysis.priority().name());
            objectiveMetadata.put("goalFeasibility", goalAnalysis.feasibility().name());

            objectiveMetadata.put("goalProgress",
                    String.valueOf(goalAnalysis.progress()));

            objectiveMetadata.put("goalConfidence",
                    String.valueOf(goalAnalysis.confidence()));

            objectiveMetadata.put("goalConfidenceBand",
                    goalAnalysis.confidenceBand().name());

            objectiveMetadata.put("goalDecompositionRequired",
                    String.valueOf(goalAnalysis.decompositionRequired()));

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

                objectiveMetadata.put("goalSubtasks", goalAnalysis.subtasks());

                objectiveMetadata.put("goalDependencies", goalAnalysis.dependencies());

                objectiveMetadata.put("goalBlockers", goalAnalysis.blockers());

                objectiveMetadata.put("goalRecommendations", goalAnalysis.recommendations());

                objectiveMetadata.put("goalEvidence", goalAnalysis.evidence());

                objectiveMetadata.put("goalConstraints", goalAnalysis.constraints());

                objectiveMetadata.put("goalConflicts", goalAnalysis.conflicts());

                objectiveMetadata.put("goalEvolutionSignals", goalAnalysis.evolutionSignals());
            }


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



            PlanningConstraints constraints =
                    new PlanningConstraints(
                            Map.of(),
                            Map.of(),
                            Map.of(),
                            objectiveMetadata
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
             * Sprint-11: Extract the domain-aware PlanBlueprint that the
             * DefaultPlanningService embedded in objectiveMetadata. The
             * blueprint carries rich phase/milestone/risk data used by
             * DefaultResponseSynthesizer to render an executive-grade plan.
             */
            PlanBlueprint planBlueprint = null;
            if (objective != null && objective.metadata() != null) {
                Object embedded = objective.metadata().get("planBlueprint");
                if (embedded instanceof PlanBlueprint bp) {
                    planBlueprint = bp;
                }
            }

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
                    "planningResponse",
                    planningResponse
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

            if (planBlueprint != null) {
                state.addMetadata("planBlueprint", planBlueprint);
                state.addMetadata(
                        "planBlueprintDomain",
                        planBlueprint.metadata().getOrDefault("domain", "GENERAL"));
            }

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

            publishPlanningEvent(
                    context,
                    requestId,
                    planId,
                    1
            );

            return chain.next(context, state);

        } catch (Exception e) {

            publishPlanningEvent(
                    context,
                    context.getExecutionRequest() != null
                            ? context.getExecutionRequest().getRequestId()
                            : "unknown",
                    "FAILED",
                    0
            );

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

    private void publishPlanningEvent(
            PipelineContext context,
            String requestId,
            String planId,
            int stepCount
    ) {
        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.PLANNING_COMPLETED,
                        requestId,
                        "Planning",
                        Instant.now(),
                        Map.of(
                                "planId", planId,
                                "stepCount", stepCount
                        )
                )
        );
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
     * Reads the developer-supplied planning objective from the request
     * metadata, falling back to an empty string when absent.
     */
    private String requestObjective(
            PipelineContext context) {

        if (context == null
                || context.getExecutionRequest() == null
                || context.getExecutionRequest().getMetadata() == null) {

            return "";
        }

        Object value =
                context.getExecutionRequest()
                        .getMetadata()
                        .get("objective");

        return value != null
                ? String.valueOf(value).trim()
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