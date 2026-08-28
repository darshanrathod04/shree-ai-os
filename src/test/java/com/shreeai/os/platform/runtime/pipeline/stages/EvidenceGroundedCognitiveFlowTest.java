package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.legacy.execution.ExecutionRequest;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
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
 * Focused regression tests for EO-V1-REL1-FIX-007 — Restore Real Cognitive Data Flow.
 *
 * <p>Verifies that supplied project evidence survives the full cognitive pipeline
 * and is consumed semantically by Reasoning, Inference, and Planning.</p>
 *
 * <p>Specifically verifies:</p>
 * <ol>
 *   <li>Full evidence reaches Reasoning</li>
 *   <li>Full evidence reaches Inference</li>
 *   <li>ReasoningResult information reaches Inference</li>
 *   <li>Planning preserves semantic information</li>
 *   <li>Evidence-grounded reasoning no longer collapses to "Insufficient evidence"
 *       when sufficient structural evidence is present</li>
 * </ol>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since EO-V1-REL1-FIX-007
 */
public class EvidenceGroundedCognitiveFlowTest {

    private static final String COLLEGE_MGMT_EVIDENCE =
            "College Management System. 38 total files. 30 source files. 1 test file. "
            + "Java. Maven. StudentController. TeacherController. DepartmentController. "
            + "Student/Teacher/Department entities, services, repositories, DTOs, exceptions, etc.";

    private ExecutionRequest buildExecutionRequest(String userInput) {
        return ExecutionRequest.builder()
                .requestId("test-request-evidence")
                .decisionId("test-decision")
                .capabilityName("CHAT")
                .intent("CHAT_REQUEST")
                .userInput(userInput)
                .build();
    }

    private PipelineContext buildContext(String userInput) {
        return PipelineContext.builder()
                .executionRequest(buildExecutionRequest(userInput))
                .build();
    }

    private DefaultPlanningService buildPlanningService() {
        return new DefaultPlanningService(
                new PlanningValidator(),
                new DefaultPlanningProcessingEngine()
        );
    }

    // =====================================================
    // 1. FULL EVIDENCE REACHES REASONING
    // =====================================================

    @Test
    public void testFullEvidenceReachesReasoning() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        List<ExecutionStage> stages = List.of(reasoningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be stored in state");

        // The full evidence must be present in the reasoning findings
        boolean requestAnalyzed = reasoningResult.findings().stream()
                .anyMatch(f -> f.contains(COLLEGE_MGMT_EVIDENCE));
        assertTrue(requestAnalyzed,
                "Reasoning findings must contain the full evidence. Findings: " + reasoningResult.findings());

        // The full evidence must be present in the reasoning evidence list
        boolean evidencePresent = reasoningResult.evidence().stream()
                .anyMatch(e -> e.contains(COLLEGE_MGMT_EVIDENCE));
        assertTrue(evidencePresent,
                "Reasoning evidence must contain the full evidence. Evidence: " + reasoningResult.evidence());

        // The request length metadata must match the full evidence length
        Object requestLength = reasoningResult.metadata().get("requestLength");
        assertEquals(COLLEGE_MGMT_EVIDENCE.length(), ((Number) requestLength).intValue(),
                "requestLength must match the full evidence length");
    }

    // =====================================================
    // 2. FULL EVIDENCE REACHES INFERENCE
    // =====================================================

    @Test
    public void testFullEvidenceReachesInference() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // Inference must have completed
        assertTrue((Boolean) state.getMetadata().get("inferenceCompleted"),
                "Inference must have completed");

        // The inference supporting evidence must contain the evidence-grounded reasoning
        // conclusion, which references the supplied evidence facts (Java, Maven, Controller)
        @SuppressWarnings("unchecked")
        List<String> supportingEvidence = (List<String>) state.getMetadata().get("supportingEvidence");
        assertNotNull(supportingEvidence, "Supporting evidence must be present");
        boolean hasEvidenceGroundedConclusion = supportingEvidence.stream()
                .anyMatch(e -> e.contains("Java") && e.contains("Maven") && e.contains("Controller"));
        assertTrue(hasEvidenceGroundedConclusion,
                "Inference supporting evidence must contain the evidence-grounded conclusion. Evidence: "
                        + supportingEvidence);
    }

    // =====================================================
    // 3. REASONING RESULT INFORMATION REACHES INFERENCE
    // =====================================================

    @Test
    public void testReasoningResultInformationReachesInference() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be stored in state");

        // The inference supporting evidence must reference the actual reasoning conclusion
        @SuppressWarnings("unchecked")
        List<String> supportingEvidence = (List<String>) state.getMetadata().get("supportingEvidence");
        assertNotNull(supportingEvidence, "Supporting evidence must be present");
        boolean hasReasoningConclusion = supportingEvidence.stream()
                .anyMatch(e -> e.contains(reasoningResult.conclusion()));
        assertTrue(hasReasoningConclusion,
                "Inference supporting evidence must reference the actual reasoning conclusion. Evidence: "
                        + supportingEvidence);

        // The best hypothesis must be grounded in the reasoning conclusion, not "Insufficient evidence"
        String bestHypothesis = (String) state.getMetadata().get("bestHypothesis");
        assertNotNull(bestHypothesis, "Best hypothesis must be present");
        assertFalse(bestHypothesis.contains("Insufficient evidence"),
                "Best hypothesis must not be 'Insufficient evidence'. Got: " + bestHypothesis);
    }

    // =====================================================
    // 4. PLANNING PRESERVES SEMANTIC INFORMATION
    // =====================================================

    @Test
    public void testPlanningPreservesSemanticInformation() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        InferenceStage inferenceStage = new InferenceStage(new DefaultInferenceEngine());
        PlanningStage planningStage = new PlanningStage(buildPlanningService());
        List<ExecutionStage> stages = List.of(reasoningStage, inferenceStage, planningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        // The PlanningObjective must be preserved in state
        PlanningObjective objective = (PlanningObjective) state.getMetadata().get("planningObjective");
        assertNotNull(objective, "PlanningObjective must be stored in state");

        // The objective metadata must carry the request/reasoning information
        Map<String, Object> objectiveMetadata = objective.metadata();
        assertEquals("test-request-evidence", objectiveMetadata.get("requestId"),
                "Objective metadata must contain requestId");
        assertEquals(COLLEGE_MGMT_EVIDENCE, objectiveMetadata.get("requestText"),
                "Objective metadata must contain the full request text");

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
    // 5. EVIDENCE-GROUNDED REASONING NO LONGER COLLAPSES
    // =====================================================

    @Test
    public void testEvidenceGroundedReasoningDoesNotCollapseToInsufficientEvidence() {
        ReasoningStage reasoningStage = new ReasoningStage(new DefaultReasoningEngine());
        List<ExecutionStage> stages = List.of(reasoningStage);
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineExecutionState state = new PipelineExecutionState(stages);

        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess(), "Pipeline should complete successfully");

        ReasoningResult reasoningResult = (ReasoningResult) state.getMetadata().get("reasoningResult");
        assertNotNull(reasoningResult, "ReasoningResult must be stored in state");

        // The conclusion must NOT collapse to "Insufficient evidence"
        String conclusion = reasoningResult.conclusion();
        assertNotNull(conclusion, "Conclusion must not be null");
        assertFalse(conclusion.contains("Insufficient evidence"),
                "Conclusion must not collapse to 'Insufficient evidence'. Got: " + conclusion);

        // The conclusion must be grounded in the supplied evidence
        assertTrue(conclusion.contains("Java"), "Conclusion must reference Java technology");
        assertTrue(conclusion.contains("Maven"), "Conclusion must reference Maven technology");
        assertTrue(conclusion.contains("Controller"), "Conclusion must reference Controller layer");
        assertTrue(conclusion.contains("Service"), "Conclusion must reference Service layer");
        assertTrue(conclusion.contains("Repository"), "Conclusion must reference Repository layer");
        assertTrue(conclusion.contains("Entity"), "Conclusion must reference Entity layer");
        assertTrue(conclusion.contains("38"), "Conclusion must reference total file count");
        assertTrue(conclusion.contains("30"), "Conclusion must reference source file count");
        assertTrue(conclusion.contains("1"), "Conclusion must reference test file count");

        // The conclusion must be honest about limitations
        assertTrue(conclusion.contains("source-level defects cannot be determined"),
                "Conclusion must state source-level limitations");
    }

    // =====================================================
    // 6. FULL PIPELINE WITH EVIDENCE SUCCEEDS
    // =====================================================

    @Test
    public void testFullPipelineWithEvidenceSucceeds() {
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
        PipelineContext context = buildContext(COLLEGE_MGMT_EVIDENCE);
        PipelineResult result = pipeline.execute(context);

        assertNotNull(result, "Pipeline result must not be null");
        assertTrue(result.isSuccess(), "Pipeline should succeed. Status: " + result.getStatus());
        assertEquals("COMPLETED", result.getStatus(), "Pipeline should complete");
                assertEquals(11, result.getCompletedStages().size(),
                "All 11 stages should complete");
    }
}