package com.shreeai.os.platform.intelligence.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkflowEngine Tests")
class WorkflowEngineTest {

    private WorkflowEngine engine;

    @BeforeEach
    void setUp() {
        engine = new WorkflowEngine();
    }

    private Workflow buildSampleWorkflow() {
        return Workflow.builder()
                .id("wf-1")
                .name("Research Report")
                .description("Gather, analyze, report")
                .steps(List.of(
                        WorkflowStep.of("gather", "Gather", "gather", 1),
                        WorkflowStep.of("analyze", "Analyze", "analyze", 2),
                        WorkflowStep.of("report", "Report", "report", 3)))
                .build();
    }

    @Test
    @DisplayName("register adds a workflow")
    void registerAddsWorkflow() {
        engine.register(buildSampleWorkflow());
        assertTrue(engine.isRegistered("wf-1"));
        assertEquals(1, engine.size());
    }

    @Test
    @DisplayName("register validates duplicate step ids")
    void registerRejectsDuplicateStepIds() {
        Workflow invalid = Workflow.builder()
                .id("wf-bad")
                .name("Bad")
                .steps(List.of(
                        WorkflowStep.of("s", "One", "a", 1),
                        WorkflowStep.of("s", "Two", "b", 2)))
                .build();

        assertThrows(IllegalStateException.class, () -> engine.register(invalid));
    }

    @Test
    @DisplayName("register validates unknown dependency")
    void registerRejectsUnknownDependency() {
        Workflow invalid = Workflow.builder()
                .id("wf-bad2")
                .name("Bad")
                .steps(List.of(
                        WorkflowStep.builder().id("s1").name("One").action("a")
                                .order(1).dependsOn(java.util.Set.of("missing")).build()))
                .build();

        assertThrows(IllegalStateException.class, () -> engine.register(invalid));
    }

    @Test
    @DisplayName("findById returns matching workflow")
    void findByIdMatches() {
        engine.register(buildSampleWorkflow());
        Optional<Workflow> found = engine.findById("wf-1");
        assertTrue(found.isPresent());
        assertEquals("Research Report", found.get().name());
    }

    @Test
    @DisplayName("findById returns empty for unknown")
    void findByIdUnknown() {
        assertEquals(Optional.empty(), engine.findById("nope"));
    }

    @Test
    @DisplayName("execute runs all steps in order on success")
    void executeRunsAllStepsOnSuccess() {
        engine.register(buildSampleWorkflow());

        WorkflowResult result = engine.execute("wf-1", Map.of("topic", "AI"),
                (step, context) -> WorkflowResult.StepOutcome.success(
                        step.id(), step.name(), Map.of(step.id() + "_done", true)));

        assertTrue(result.isSuccessful());
        assertEquals(WorkflowResult.Status.COMPLETED, result.status());
        assertEquals(3, result.stepOutcomes().size());
        // Steps run in order
        assertEquals("gather", result.stepOutcomes().get(0).stepId());
        assertEquals("analyze", result.stepOutcomes().get(1).stepId());
        assertEquals("report", result.stepOutcomes().get(2).stepId());
    }

    @Test
    @DisplayName("execute short-circuits on first failing step")
    void executeShortCircuitsOnFailure() {
        engine.register(buildSampleWorkflow());

        WorkflowResult result = engine.execute("wf-1", Map.of(),
                (step, context) -> "analyze".equals(step.id())
                        ? WorkflowResult.StepOutcome.failure(step.id(), step.name(), "boom")
                        : WorkflowResult.StepOutcome.success(step.id(), step.name(), Map.of()));

        assertEquals(WorkflowResult.Status.FAILED, result.status());
        assertFalse(result.isSuccessful());
        // gather (pass) + analyze (fail); report never runs
        assertEquals(2, result.stepOutcomes().size());
    }

    @Test
    @DisplayName("execute returns NOT_FOUND for unknown workflow")
    void executeNotFound() {
        WorkflowResult result = engine.execute("nope", Map.of(),
                (step, context) -> WorkflowResult.StepOutcome.success(
                        step.id(), step.name(), Map.of()));

        assertEquals(WorkflowResult.Status.NOT_FOUND, result.status());
        assertFalse(result.isSuccessful());
        assertEquals(0, result.stepOutcomes().size());
    }

    @Test
    @DisplayName("step outputs are shared into the context")
    void stepOutputsSharedIntoContext() {
        engine.register(buildSampleWorkflow());

        StringBuilder seen = new StringBuilder();
        engine.execute("wf-1", Map.of("topic", "AI"),
                (step, context) -> {
                    if ("analyze".equals(step.id()) && context.get("gather_done") != null) {
                        seen.append("shared");
                    }
                    return WorkflowResult.StepOutcome.success(
                            step.id(), step.name(), Map.of(step.id() + "_done", true));
                });

        assertEquals("shared", seen.toString());
    }

    @Test
    @DisplayName("remove unregisters a workflow")
    void removeUnregisters() {
        engine.register(buildSampleWorkflow());
        assertTrue(engine.remove("wf-1"));
        assertEquals(0, engine.size());
        assertFalse(engine.remove("wf-1"));
    }

    @Test
    @DisplayName("null arguments throw")
    void nullArgumentsThrow() {
        engine.register(buildSampleWorkflow());
        assertThrows(NullPointerException.class, () -> engine.register(null));
        assertThrows(NullPointerException.class, () -> engine.findById(null));
        assertThrows(NullPointerException.class, () -> engine.isRegistered(null));
        assertThrows(NullPointerException.class,
                () -> engine.execute("wf-1", Map.of(), null));
    }

    @Test
    @DisplayName("workflow without id throws on build")
    void workflowWithoutIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Workflow.builder().name("No id").build());
    }
}
