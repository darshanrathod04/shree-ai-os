package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.legacy.execution.ExecutionRequest;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.engine.DefaultPlanningProcessingEngine;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.planning.service.DefaultPlanningService;
import com.shreeai.os.platform.kernels.planning.validation.PlanningValidator;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for EO-V1-REL1-INT-005 — Critical Cognitive Pipeline Wiring Fix.
 *
 * <p>Verifies:</p>
 * <ul>
 *   <li>A. Actual user request reaches ReasoningStage/ReasoningEngine</li>
 *   <li>B. Actual ReasoningResult reaches InferenceStage without information loss</li>
 *   <li>C. PlanningStage does not discard its PlanningObjective information</li>
 *   <li>D. PlanningStage does not report fabricated "3 goals"</li>
 *   <li>E. Planning failure stops the pipeline rather than silently continuing</li>
 *   <li>F. Existing successful Runtime → Pipeline → Kernel integration remains working</li>
 * </ul>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since EO-V1-REL1-INT-005
 */
public class CognitivePipelineWiringTest {

    private static final String TEST_USER_INPUT = "What is quantum computing and how does it work?";

    private ExecutionRequest buildExecutionRequest() {
        return ExecutionRequest.builder()
                .requestId("test-request-001")
                .decisionId("test-decision")
                .capabilityName("CHAT")
                .intent("CHAT_REQUEST")
                .userInput(TEST_USER_INPUT)
                .build();
    }

    private PipelineContext buildContext() {
        return PipelineContext.builder()
                .executionRequest(buildExecutionRequest())
                .build();
    }

    private DefaultPlanningService buildPlanningService() {
        return new DefaultPlanningService(
                new PlanningValidator(),
                new DefaultPlanningProcessingEngine()
        );
    }

    // =====================================================
    // A. ACTUAL USER REQUEST REACHES REASONING STAGE/ENGINE
    // =====================================================

    @Test
    public void testActualUserRequestReachesReasoningEngine() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        List<ExecutionStage> stages = List.of(reasoningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext();
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        // Pipeline should complete (single stage calls chain.next() → terminal COMPLETED)
        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // The actual ReasoningResult must be preserved in state
        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be stored in state");

        // The reasoning engine must have received the actual user text.
        // DefaultReasoningEngine adds "Request analyzed: <request>" as a finding.
        boolean requestAnalyzed = reasoningResult.findings().stream()
                .anyMatch(f -> f.contains(TEST_USER_INPUT));
        assertTrue(requestAnalyzed,
                "Reasoning findings must contain the actual user input. Findings: " + reasoningResult.findings());

        // The reasoning metadata must contain the request length of the actual user input
        Object requestLength = reasoningResult.metadata().get("requestLength");
        assertNotNull(requestLength, "Reasoning metadata must contain requestLength");
        assertEquals(TEST_USER_INPUT.length(), ((Number) requestLength).intValue(),
                "requestLength must match the actual user input length");
    }

    // =====================================================
    // B. ACTUAL REASONING RESULT REACHES INFERENCE STAGE
    // =====================================================

    @Test
    public void testActualReasoningResultReachesInferenceStageWithoutInformationLoss() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext();
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // The actual ReasoningResult must be preserved
        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be stored in state");

        // Findings must be preserved
        assertFalse(reasoningResult.findings().isEmpty(), "Findings must not be empty");
        assertTrue(reasoningResult.findings().size() > 0, "Findings must be preserved");

        // Evidence must be preserved
        assertNotNull(reasoningResult.evidence(), "Evidence must not be null");

        // Risks must be preserved
        assertNotNull(reasoningResult.risks(), "Risks must not be null");

        // Alternatives must be preserved
        assertFalse(reasoningResult.alternatives().isEmpty(), "Alternatives must not be empty");

        // Conclusion must be preserved
        assertNotNull(reasoningResult.conclusion(), "Conclusion must not be null");
        assertFalse(reasoningResult.conclusion().isBlank(), "Conclusion must not be blank");

        // Reasoning type/scope/steps must be preserved
        assertEquals("EVIDENCE_BASED_REASONING", reasoningResult.reasoningType(),
                "Reasoning type must be preserved");
        assertNotNull(reasoningResult.scope(), "Reasoning scope must not be null");
        assertTrue(reasoningResult.reasoningSteps() > 0, "Reasoning steps must be > 0");

        // Inference must have completed and consumed the actual reasoning result
        assertTrue((Boolean) state.getMetadata().get("inferenceCompleted"),
                "Inference must have completed");

        // The inference supporting evidence must reference the actual reasoning conclusion
        @SuppressWarnings("unchecked")
        List<String> supportingEvidence = (List<String>) state.getMetadata().get("supportingEvidence");
        assertNotNull(supportingEvidence, "Supporting evidence must be present");
        boolean hasReasoningConclusion = supportingEvidence.stream()
                .anyMatch(e -> e.contains(reasoningResult.conclusion()));
        assertTrue(hasReasoningConclusion,
                "Inference supporting evidence must reference the actual reasoning conclusion. Evidence: "
                        + supportingEvidence);
    }

    // =====================================================
    // C. PLANNING STAGE DOES NOT DISCARD PLANNING OBJECTIVE
    // =====================================================

    @Test
    public void testPlanningStagePreservesPlanningObjectiveInformation() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        PlanningStage planningStage = new PlanningStage(buildPlanningService());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage, planningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext();
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // The PlanningObjective must be preserved in state
        PlanningObjective objective = (PlanningObjective) state.getMetadata().get("planningObjective");
        assertNotNull(objective, "PlanningObjective must be stored in state");

        // The objective metadata must carry the request/reasoning information
        Map<String, Object> objectiveMetadata = objective.metadata();
        assertEquals("test-request-001", objectiveMetadata.get("requestId"),
                "Objective metadata must contain requestId");
        assertEquals(TEST_USER_INPUT, objectiveMetadata.get("requestText"),
                "Objective metadata must contain the actual request text");

        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be present");
        assertEquals(reasoningResult.reasoningId(), objectiveMetadata.get("reasoningId"),
                "Objective metadata must contain reasoningId");
        assertEquals(reasoningResult.conclusion(), objectiveMetadata.get("reasoningConclusion"),
                "Objective metadata must contain reasoning conclusion");
        assertEquals(reasoningResult.reasoningType(), objectiveMetadata.get("reasoningType"),
                "Objective metadata must contain reasoning type");
        assertEquals(reasoningResult.scope(), objectiveMetadata.get("reasoningScope"),
                "Objective metadata must contain reasoning scope");
        assertEquals(String.valueOf(reasoningResult.reasoningSteps()), objectiveMetadata.get("reasoningSteps"),
                "Objective metadata must contain reasoning steps");
        assertEquals(String.valueOf(reasoningResult.confidence()), objectiveMetadata.get("reasoningConfidence"),
                "Objective metadata must contain reasoning confidence");

        // The plan must have been created through the real PlanningService
        String planId = (String) state.getMetadata().get("planId");
        assertNotNull(planId, "planId must be present");
        assertFalse(planId.isBlank(), "planId must not be blank");
        assertTrue((Boolean) state.getMetadata().get("planningCompleted"),
                "Planning must have completed");
    }

    // =====================================================
    // D. NO FABRICATED "3 GOALS" / "3 STEPS"
    // =====================================================

    @Test
    public void testPlanningStageDoesNotReportFabricatedGoalCounts() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        PlanningStage planningStage = new PlanningStage(buildPlanningService());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage, planningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext();
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // No fabricated planSteps metadata
        assertFalse(state.getMetadata().containsKey("planSteps"),
                "planSteps metadata must not be fabricated");

        // The planId must be present in state metadata (the only real statistic
        // available from the PlanningService.createPlan() contract)
        String planId = (String) state.getMetadata().get("planId");
        assertNotNull(planId, "planId must be present");
        assertFalse(planId.isBlank(), "planId must not be blank");
        assertTrue(planId.contains("goal"), "planId must be a real result from PlanningService");
    }

    // =====================================================
    // E. PLANNING FAILURE STOPS THE PIPELINE
    // =====================================================

    @Test
    public void testPlanningFailureStopsPipeline() {
        PlanningService failingPlanningService = new PlanningService() {
            @Override
            public String createPlan(PlanningRequest planningRequest) {
                throw new RuntimeException("Simulated planning failure");
            }

            @Override
            public String refinePlan(PlanRefinementRequest planRefinementRequest) {
                return planRefinementRequest.planId();
            }

            @Override
            public String validatePlan(PlanValidationRequest planValidationRequest) {
                return "validated";
            }
        };

        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        PlanningStage planningStage = new PlanningStage(failingPlanningService);
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage, planningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext();
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        // The pipeline must NOT silently continue after planning failure
        assertFalse(result.isSuccess(), "Pipeline must fail when planning fails");
        assertEquals("PLANNING_FAILED", result.getStatus(),
                "Result status must be PLANNING_FAILED");

        // The pipeline must stop — no further stages should be visited
        // (Planning is the last stage in this test chain, so verify no completion)
        assertFalse(state.getCompletedStages().contains("Planning"),
                "Planning must not be marked as completed");
    }

    @Test
    public void testPlanningFailureStopsFullPipeline() {
        PlanningService failingPlanningService = new PlanningService() {
            @Override
            public String createPlan(PlanningRequest planningRequest) {
                throw new RuntimeException("Simulated planning failure");
            }

            @Override
            public String refinePlan(PlanRefinementRequest planRefinementRequest) {
                return planRefinementRequest.planId();
            }

            @Override
            public String validatePlan(PlanValidationRequest planValidationRequest) {
                return "validated";
            }
        };

        // Build a full pipeline with a failing planning service
        List<ExecutionStage> stages = List.of(
                new IdentityStage(),
                new ContextStage(),
                new MemoryRecallStage(),
                new KnowledgeStage(),
                new ReasoningStage(new DefaultReasoningEngine()),
                new InferenceStage(new DefaultInferenceEngine()),
                new PlanningStage(failingPlanningService),
                new ActionExecutionStage(),
                new MemoryStoreStage(),
                new ChiefReviewStage()
        );

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = buildContext();
        PipelineResult result = pipeline.execute(context);

        // Pipeline must fail and stop at Planning
        assertFalse(result.isSuccess(), "Pipeline must fail when planning fails");
        assertEquals("FAILED", result.getStatus(), "Pipeline status must be FAILED");

        // Stages after Planning must NOT be completed
        assertFalse(result.getCompletedStages().contains("Execution"),
                "Execution stage must not run after planning failure");
        assertFalse(result.getCompletedStages().contains("MemoryStore"),
                "MemoryStore stage must not run after planning failure");
        assertFalse(result.getCompletedStages().contains("ChiefReview"),
                "ChiefReview stage must not run after planning failure");
    }

    // =====================================================
    // F. EXISTING SUCCESSFUL RUNTIME → PIPELINE → KERNEL
    // =====================================================

    @Test
    public void testFullPipelineSucceedsWithRealKernels() {
                // Build the full canonical 11-stage pipeline with real kernel services
        List<ExecutionStage> stages = List.of(
                new IdentityStage(),
                new ContextStage(),
                new MemoryRecallStage(),
                new KnowledgeStage(),
                new ReasoningStage(new DefaultReasoningEngine()),
                new InferenceStage(new DefaultInferenceEngine()),
                new PlanningStage(buildPlanningService()),
                new ActionExecutionStage(),
                new ReflectionStage(null),
                new MemoryStoreStage(),
                new ChiefReviewStage()
        );

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = buildContext();
        PipelineResult result = pipeline.execute(context);

        // Full pipeline must succeed
        assertNotNull(result, "Pipeline result must not be null");
        assertTrue(result.isSuccess(), "Pipeline should succeed. Status: " + result.getStatus());
        assertEquals("COMPLETED", result.getStatus(), "Pipeline should complete");
                assertEquals(11, result.getCompletedStages().size(),
                "All 11 stages should complete");
    }

    @Test
    public void testPlanningStagePreservesGoalIntelligenceInformation() {
        ReasoningStage reasoningStage =
                new ReasoningStage(new DefaultReasoningEngine());

        InferenceStage inferenceStage =
                new InferenceStage(new DefaultInferenceEngine());

        PlanningStage planningStage =
                new PlanningStage(buildPlanningService());

        List<ExecutionStage> stages =
                List.of(
                        reasoningStage,
                        inferenceStage,
                        planningStage
                );

        DefaultExecutionChain chain =
                new DefaultExecutionChain(stages);

        PipelineContext context =
                buildContext();

        PipelineExecutionState state =
                new PipelineExecutionState(stages);

        PipelineResult result =
                chain.next(context, state);

        assertTrue(
                result.isSuccess(),
                "Reasoning → Inference → Goal Intelligence → Planning must succeed"
        );

        /*
         * Goal Intelligence must actually execute.
         */
        assertEquals(
                Boolean.TRUE,
                state.getMetadata().get("goalIntelligenceCompleted"),
                "Goal Intelligence must complete before Planning"
        );

        /*
         * A real GoalAnalysis must be preserved in pipeline state.
         */
        Object goalAnalysisObject =
                state.getMetadata().get("goalAnalysis");

        assertNotNull(
                goalAnalysisObject,
                "GoalAnalysis must be preserved in pipeline state"
        );

        assertTrue(
                goalAnalysisObject instanceof GoalIntelligenceEngine.GoalAnalysis,
                "goalAnalysis must contain the actual GoalAnalysis result"
        );

        GoalIntelligenceEngine.GoalAnalysis goalAnalysis =
                (GoalIntelligenceEngine.GoalAnalysis) goalAnalysisObject;

        /*
         * Goal Intelligence must produce an identifiable analysis.
         */
        assertNotNull(
                goalAnalysis.analysisId(),
                "GoalAnalysis must have an analysis ID"
        );

        assertFalse(
                goalAnalysis.analysisId().isBlank(),
                "GoalAnalysis analysis ID must not be blank"
        );

        /*
         * PlanningObjective must preserve Goal Intelligence metadata.
         */
        PlanningObjective objective =
                (PlanningObjective)
                        state.getMetadata().get("planningObjective");

        assertNotNull(
                objective,
                "PlanningObjective must be present"
        );

        Map<String, Object> metadata =
                objective.metadata();

        assertEquals(
                "GoalIntelligenceEngine",
                metadata.get("goalIntelligenceSource"),
                "PlanningObjective must identify Goal Intelligence provenance"
        );

        assertEquals(
                goalAnalysis.analysisId(),
                metadata.get("goalAnalysisId"),
                "PlanningObjective must preserve the actual GoalAnalysis ID"
        );

        assertEquals(
                goalAnalysis.status().name(),
                metadata.get("goalStatus"),
                "Goal status must survive into Planning"
        );

        assertEquals(
                goalAnalysis.priority().name(),
                metadata.get("goalPriority"),
                "Goal priority must survive into Planning"
        );

        assertEquals(
                goalAnalysis.feasibility().name(),
                metadata.get("goalFeasibility"),
                "Goal feasibility must survive into Planning"
        );

        assertEquals(
                String.valueOf(goalAnalysis.confidence()),
                metadata.get("goalConfidence"),
                "Goal confidence must survive into Planning"
        );

        /*
         * Planning itself must still complete.
         */
        assertEquals(
                Boolean.TRUE,
                state.getMetadata().get("planningCompleted"),
                "Planning must complete after Goal Intelligence"
        );

        String planId =
                (String) state.getMetadata().get("planId");

        assertNotNull(
                planId,
                "Real planId must be produced"
        );

        assertFalse(
                planId.isBlank(),
                "Real planId must not be blank"
        );
    }
}