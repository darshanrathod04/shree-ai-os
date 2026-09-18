package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.engine.DefaultExecutablePlanningGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.*;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.stages.PlanningStage;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("P2.5: Executable planning graph integration")
class ExecutablePlanningGraphIntegrationTest {

    private static final PlanningTask ROOT = PlanningTask.create("Java", "Variables", TaskType.LEARNING, 1);
    private static final PlanningTask NEXT = PlanningTask.create("Java", "Loops", TaskType.LEARNING, 2);
    private static final TaskGraph GRAPH = TaskGraph.of(
            List.of(ROOT, NEXT),
            List.of(new TaskDependency(ROOT.taskId(), NEXT.taskId(), DependencyType.FINISH_TO_START)));

    private static ExecutionPlan plan(int day1, int day2) {
        var t1 = new ScheduledTask(ROOT.taskId(), day1, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 2)));
        var t2 = new ScheduledTask(NEXT.taskId(), day2, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 2)));
        return new ExecutionPlan(Math.max(day1, day2), 4, List.of(t1, t2));
    }

    private static ExecutablePlanningGraph builtGraph() {
        return new DefaultExecutablePlanningGraphEngine().build(GRAPH, plan(1, 2), null, null);
    }

    private static UserConstraints constraints(String duration) {
        return UserConstraints.of(duration, null, null, null, null, null, List.of());
    }

    @Nested
    @DisplayName("CognitiveState round trip")
    class RoundTrip {
        @Test @DisplayName("the engine-built graph is stored and retrieved")
        void storeAndRetrieve() {
            var graph = builtGraph();
            var state = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withExecutablePlanningGraph(graph);
            assertSame(graph, state.executablePlanningGraph());
            assertSame(GRAPH, state.taskGraph());
        }
        @Test @DisplayName("null graphs are rejected")
        void nullRejected() {
            assertThrows(NullPointerException.class,
                    () -> CognitiveState.empty().withExecutablePlanningGraph(null));
        }
        @Test @DisplayName("the original state stays untouched by the wither")
        void immutableOriginal() {
            var before = CognitiveState.empty();
            var after = before.withExecutablePlanningGraph(builtGraph());
            assertNotSame(before, after);
            assertNull(before.executablePlanningGraph());
            assertNotNull(after.executablePlanningGraph());
        }
        @Test @DisplayName("replacing the graph swaps cleanly")
        void replaceGraph() {
            var first = builtGraph();
            var second = new DefaultExecutablePlanningGraphEngine()
                    .build(GRAPH, plan(2, 3), null, null);
            var state = CognitiveState.empty().withExecutablePlanningGraph(first)
                    .withExecutablePlanningGraph(second);
            assertSame(second, state.executablePlanningGraph());
        }
        @Test @DisplayName("a fresh state carries no graph")
        void emptyStateHasNoGraph() {
            assertNull(CognitiveState.empty().executablePlanningGraph());
        }
    }

    @Nested
    @DisplayName("Wither preservation")
    class WitherPreservation {
        @Test @DisplayName("incrementReflection preserves the graph")
        void reflectionPreserves() {
            var graph = builtGraph();
            var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withExecutablePlanningGraph(graph).incrementReflection();
            assertSame(graph, before.executablePlanningGraph());
        }
        @TestFactory
        Stream<DynamicTest> everyArtifactWitherPreservesTheGraph() {
            return Arrays.stream(CognitiveState.class.getDeclaredMethods())
                    .filter(m -> m.getName().startsWith("with")
                            && !m.getName().equals("withExecutablePlanningGraph"))
                    .map(method -> DynamicTest.dynamicTest(method.getName(), () -> {
                        var graph = builtGraph();
                        var before = CognitiveState.empty().withTaskGraph(GRAPH)
                                .withExecutionPlan(plan(1, 2)).withExecutablePlanningGraph(graph);
                        Object[] args = Arrays.stream(method.getParameterTypes())
                                .map(type -> type == double.class ? (Object) 0.75 : mock(type)).toArray();
                        var after = (CognitiveState) method.invoke(before, args);
                        assertSame(graph, after.executablePlanningGraph());
                        assertSame(graph, before.executablePlanningGraph());
                    }));
        }
        @Test @DisplayName("every record component survives a copy")
        void recordComponentRoundTrip() throws Exception {
            var graph = builtGraph();
            var result = new ReplanningResult(plan(2, 3), ReplanningReason.TASK_DELAYED, 0);
            var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withReplanningResult(result).withExecutablePlanningGraph(graph);
            var after = before.withUserConstraints(constraints("10 days"));
            for (var component : CognitiveState.class.getRecordComponents()) {
                var expected = component.getName().equals("userConstraints")
                        ? after.userConstraints()
                        : component.getAccessor().invoke(before);
                assertEquals(expected, component.getAccessor().invoke(after), component.getName());
            }
            assertSame(graph, after.executablePlanningGraph());
        }
    }

    @Nested
    @DisplayName("Backward compatibility")
    class BackwardCompatibility {
        @Test @DisplayName("the legacy R4 constructor leaves the graph null")
        void legacyR4() {
            var legacy = new CognitiveState(null, null, null, null, 0, List.of(),
                    null, null, null, null, null, null, null, null, null, null);
            assertNull(legacy.executablePlanningGraph());
        }
        @Test @DisplayName("the P2.3 planning constructor leaves the graph null")
        void legacyP23() {
            var legacy = new CognitiveState(null, null, null, null, 0, List.of(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null,
                    GRAPH);
            assertNull(legacy.executablePlanningGraph());
            assertSame(GRAPH, legacy.taskGraph());
        }
        @Test @DisplayName("the P2.4 schedule constructor leaves the graph null")
        void legacyP24() {
            var legacy = new CognitiveState(null, null, null, null, 0, List.of(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null,
                    GRAPH, plan(1, 2));
            assertNull(legacy.executablePlanningGraph());
            assertEquals(plan(1, 2), legacy.executionPlan());
        }
    }

    @Nested
    @DisplayName("PlanningStage integration")
    class PlanningStageIntegration {
        @Test @DisplayName("replan preserves the stored executable planning graph")
        void replanPreservesGraph() {
            var stage = new PlanningStage(mock(PlanningService.class));
            var state = new PipelineExecutionState(List.of(stage));
            var graph = builtGraph();
            var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withUserConstraints(constraints("30 days")).withExecutablePlanningGraph(graph);
            state.setCognitiveState(before);
            stage.replan(state, ProgressSnapshot.empty(), constraints("1 day"), 4,
                    ReplanningReason.DURATION_CHANGED);
            var after = state.getCognitiveState();
            assertSame(graph, after.executablePlanningGraph());
            assertNotNull(after.replanningResult());
            assertNotEquals(before.executionPlan(), after.executionPlan());
            assertTrue(state.getMetadata().isEmpty());
        }
        @Test @DisplayName("a rejected replan leaves the graph untouched")
        void rejectedReplanKeepsGraph() {
            var stage = new PlanningStage();
            var state = new PipelineExecutionState(List.of(stage));
            var soloGraph = TaskGraph.of(List.of(ROOT), List.of());
            var soloPlan = new ExecutionPlan(3, 2, List.of(new ScheduledTask(
                    ROOT.taskId(), 3, 2, List.of(new ResourceAllocation(ResourceType.STUDY, 2)))));
            var before = CognitiveState.empty().withTaskGraph(soloGraph).withExecutionPlan(soloPlan)
                    .withExecutablePlanningGraph(
                            new DefaultExecutablePlanningGraphEngine().build(soloGraph, soloPlan, null, null));
            state.setCognitiveState(before);
            var impossible = new ProgressSnapshot(List.of(
                    new ProgressEntry(ROOT.taskId(), TaskProgress.COMPLETED)));
            assertThrows(IllegalArgumentException.class, () -> stage.replan(state, impossible,
                    constraints("1 day"), 2, ReplanningReason.TASK_DELAYED));
            assertSame(before, state.getCognitiveState());
            assertTrue(state.getMetadata().isEmpty());
        }
        @Test @DisplayName("the graph lives only in cognitive state, never in metadata")
        void noMetadataMirror() {
            var stage = new PlanningStage(mock(PlanningService.class));
            var state = new PipelineExecutionState(List.of(stage));
            var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withExecutablePlanningGraph(builtGraph());
            state.setCognitiveState(before);
            stage.replan(state, ProgressSnapshot.empty(), null, 3, ReplanningReason.MANUAL_REQUEST);
            var after = state.getCognitiveState();
            assertNotNull(after.executablePlanningGraph());
            assertTrue(state.getMetadata().keySet().stream().noneMatch(k -> k.contains("graph")));
            assertTrue(state.getMetadata().isEmpty());
        }
        @Test @DisplayName("replan with no graph stored leaves it absent")
        void replanWithoutGraph() {
            var stage = new PlanningStage();
            var state = new PipelineExecutionState(List.of(stage));
            var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(plan(1, 2))
                    .withUserConstraints(constraints("30 days"));
            state.setCognitiveState(before);
            stage.replan(state, ProgressSnapshot.empty(), null, 4, ReplanningReason.MANUAL_REQUEST);
            assertNull(state.getCognitiveState().executablePlanningGraph());
        }
    }

    @Nested
    @DisplayName("End-to-end engine-to-state determinism")
    class EndToEndDeterminism {
        @Test @DisplayName("identical inputs produce identical stored graphs")
        void identicalGraphs() {
            var first = builtGraph();
            var second = new DefaultExecutablePlanningGraphEngine().build(GRAPH, plan(1, 2), null, null);
            assertEquals(first, second);
            var state1 = CognitiveState.empty().withExecutablePlanningGraph(first);
            var state2 = CognitiveState.empty().withExecutablePlanningGraph(second);
            assertEquals(state1.executablePlanningGraph(), state2.executablePlanningGraph());
        }
        @Test @DisplayName("the graph matches its task graph and schedule")
        void graphMatchesInputs() {
            var graph = builtGraph();
            assertEquals(2, graph.nodes().size());
            assertEquals(1, graph.edges().size());
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(ROOT.taskId()).state());
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(NEXT.taskId()).state());
        }
        @Test @DisplayName("progress-driven rebuild yields the expected states")
        void progressRebuild() {
            var result = new ReplanningResult(plan(2, 3), ReplanningReason.TASK_DELAYED, 0);
            var snapshot = new ProgressSnapshot(List.of(
                    new ProgressEntry(ROOT.taskId(), TaskProgress.COMPLETED),
                    new ProgressEntry(NEXT.taskId(), TaskProgress.IN_PROGRESS)));
            var graph = new DefaultExecutablePlanningGraphEngine().build(GRAPH, plan(2, 3), result, snapshot);
            assertEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(ROOT.taskId()).state());
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(NEXT.taskId()).state());
        }
    }
}
