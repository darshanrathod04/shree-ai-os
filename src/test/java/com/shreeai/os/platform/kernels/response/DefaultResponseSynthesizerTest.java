package com.shreeai.os.platform.kernels.response;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.ConfidenceBand;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.Feasibility;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalStatus;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.Priority;
import com.shreeai.os.platform.kernels.response.engine.DefaultResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EO-V1-002 — ResponseSynthesizer V2 (Planning Rendering) tests.
 *
 * <p>Verifies that the {@link DefaultResponseSynthesizer} renders a
 * structured, human-readable plan when the pipeline state carries a routed
 * Planning result, while keeping non-planning and reasoning responses
 * byte-for-byte unchanged.</p>
 */
public class DefaultResponseSynthesizerTest {

    private final DefaultResponseSynthesizer synthesizer =
            new DefaultResponseSynthesizer();

    private static final String GOAL =
            "Create a 3-day beginner Push Pull Legs workout";

    private GoalAnalysis sampleGoalAnalysis() {

        return new GoalAnalysis(
                "analysis-1",
                GOAL,
                GoalStatus.NOT_STARTED,
                Priority.MEDIUM,
                Feasibility.PLAUSIBLE,
                0.0,
                0.90,
                ConfidenceBand.LOW,
                true,
                false,
                List.of(
                        "Design Push day",
                        "Design Pull day",
                        "Design Legs day"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        "Begin with progressive overload.",
                        "Allow recovery between sessions."),
                List.of(),
                List.of(),
                Map.of(),
                Instant.now()
        );
    }

    private PipelineContext planningContext(String operation) {

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("plan-render-test")
                .requestType("CHAT")
                .payload(GOAL)
                .metadata(Map.of(
                        "operation", operation,
                        "objective", GOAL))
                .build();

        return PipelineContext.builder()
                .executionRequest(request)
                .addAttribute("requestMetadata", request.metadata())
                .build();
    }

    private PipelineContext plainContext() {

        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("plain-render-test")
                .requestType("CHAT")
                .payload("Explain Java streams")
                .metadata(Map.of())
                .build();

        return PipelineContext.builder()
                .executionRequest(request)
                .addAttribute("requestMetadata", request.metadata())
                .build();
    }

    private PipelineExecutionState planningState(GoalAnalysis goal) {

        PipelineExecutionState state =
                new PipelineExecutionState(List.<ExecutionStage>of());

        state.addMetadata("planningCompleted", true);
        state.addMetadata("planId", "plan-123");
        state.addMetadata("goalAnalysis", goal);

        return state;
    }

    private PipelineExecutionState reasoningState() {

        PipelineExecutionState state =
                new PipelineExecutionState(List.<ExecutionStage>of());

        state.addMetadata("reasoningSummary",
                "The request was analyzed against available evidence.");
        state.addMetadata("reasoningConclusion",
                "Evidence-grounded assessment: Explain Java streams");
        state.addMetadata("reasoningConfidence", 0.92);

        return state;
    }
    /* ==========================================================
       Planning payload → formatted plan
       ========================================================== */

    @Test
    public void testPlanningPayloadRendersFormattedPlan() {

        SynthesizedResponse response = synthesizer.synthesize(
                planningContext("CREATE_PLAN"),
                planningState(sampleGoalAnalysis()));

        String answer = response.answer();

        assertTrue(answer.startsWith("# "),
                "Answer should start with a markdown title");
        assertTrue(answer.contains("## Executive Summary"),
                "Answer should contain an executive summary");
        assertTrue(answer.contains("## Goal"),
                "Answer should contain a Goal section");
        assertTrue(answer.contains(GOAL),
                "Answer should include the goal text");
        assertTrue(answer.contains("## Subtasks"),
                "Answer should contain a Subtasks section");
        assertTrue(answer.contains("1. Design Push day"),
                "Subtasks must be numbered");
        assertTrue(answer.contains("3. Design Legs day"),
                "Subtasks must be numbered sequentially");
        assertTrue(answer.contains("## Recommendations"),
                "Answer should contain a Recommendations section");
        assertTrue(answer.contains("* Begin with progressive overload."),
                "Recommendations must be rendered as bullets");
    }

    @Test
    public void testPlanningPayloadExposesStructuredData() {

        SynthesizedResponse response = synthesizer.synthesize(
                planningContext("CREATE_PLAN"),
                planningState(sampleGoalAnalysis()));

        Map<String, Object> data = response.structuredData();

        assertNotNull(data, "Structured data should never be null");
        assertTrue(data.containsKey("planningSummary"),
                "planningSummary must be exposed when available");
        assertTrue(data.get("subtasks") instanceof List<?>,
                "subtasks must be exposed when available");
        assertTrue(data.get("recommendations") instanceof List<?>,
                "recommendations must be exposed when available");
        assertFalse(data.containsKey("blockers"),
                "blockers must be omitted when absent");
    }

    @Test
    public void testPlanningPlanProjectAlsoRendersPlan() {

        SynthesizedResponse response = synthesizer.synthesize(
                planningContext("PLAN_PROJECT"),
                planningState(sampleGoalAnalysis()));

        assertTrue(response.answer().contains("## Subtasks"),
                "PLAN_PROJECT should render a plan too");
    }

    @Test
    public void testPlanningConfidenceReflectsGoalAnalysis() {

        SynthesizedResponse response = synthesizer.synthesize(
                planningContext("CREATE_PLAN"),
                planningState(sampleGoalAnalysis()));

        assertEquals(0.90, response.confidence(), 1e-9);
    }

    /* ==========================================================
       Non-planning payload → unchanged behavior
       ========================================================== */

    @Test
    public void testNonPlanningPayloadIsUnchanged() {

        SynthesizedResponse response = synthesizer.synthesize(
                plainContext(),
                reasoningState());

        String answer = response.answer();

        assertTrue(answer.contains(
                        "The request was analyzed against available evidence."),
                "Default summary must be preserved");
        assertTrue(answer.contains(
                        "Evidence-grounded assessment: Explain Java streams"),
                "Default reasoning conclusion must be preserved");
        assertFalse(answer.contains("## Subtasks"),
                "No planning sections should appear for a non-planning payload");
        assertFalse(answer.contains("## Goal"),
                "No plan title should appear for a non-planning payload");
        assertEquals(0.92, response.confidence(), 1e-9,
                "Reasoning confidence must be preserved");
        assertTrue(response.structuredData().isEmpty(),
                "No additive structured data on the default path");
    }

    @Test
    public void testReasoningResponsesRemainIdentical() {

        SynthesizedResponse first = synthesizer.synthesize(
                plainContext(),
                reasoningState());

        SynthesizedResponse second = synthesizer.synthesize(
                plainContext(),
                reasoningState());

        assertEquals(first.answer(), second.answer(),
                "Reasoning rendering must be deterministic and identical");
        assertEquals(first.sections(), second.sections(),
                "Default path must not add planning sections");
    }

    /* ==========================================================
       Routing guard — chat behavior unchanged
       ========================================================== */

    @Test
    public void testPlanningWithoutRoutedOperationIsNotRendered() {

        // planningCompleted + planId + goalAnalysis exist, but the request
        // was NOT routed to the Planning Kernel (e.g. plain chat that passed
        // through the canonical pipeline's Planning stage).
        SynthesizedResponse response = synthesizer.synthesize(
                plainContext(),
                planningState(sampleGoalAnalysis()));

        assertFalse(response.answer().contains("## Subtasks"),
                "Unrouted requests must keep legacy rendering");
        assertFalse(response.answer().contains("## Goal"),
                "Unrouted requests must keep legacy rendering");
        assertTrue(response.structuredData().isEmpty(),
                "Unrouted requests must not expose planning structured data");
    }

    @Test
    public void testPlanningWithoutCompletedStateIsNotRendered() {

        PipelineExecutionState state =
                new PipelineExecutionState(List.<ExecutionStage>of());
        state.addMetadata("goalAnalysis", sampleGoalAnalysis());

        SynthesizedResponse response = synthesizer.synthesize(
                planningContext("CREATE_PLAN"),
                state);

        assertFalse(response.answer().contains("## Subtasks"),
                "Without a completed planning result, default rendering applies");
    }
}
