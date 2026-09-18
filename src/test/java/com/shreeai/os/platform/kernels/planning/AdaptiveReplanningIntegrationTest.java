package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.model.*;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.stages.PlanningStage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdaptiveReplanningIntegrationTest {
    private static final PlanningTask TASK = PlanningTask.create("Java", "Methods", TaskType.LEARNING, 1);
    private static final TaskGraph GRAPH = TaskGraph.of(List.of(TASK), List.of());
    private static final ExecutionPlan ORIGINAL = plan(3);

    private static ExecutionPlan plan(int day) {
        return new ExecutionPlan(day, 3, List.of(new ScheduledTask(TASK.taskId(), day, 3,
                List.of(new ResourceAllocation(ResourceType.STUDY, 3)))));
    }

    private static UserConstraints constraints(String duration) {
        return UserConstraints.of(duration, null, null, null, null, null, List.of());
    }

    @Test
    void stagePublishesResultScheduleAndConstraintsWithoutRegenerating() {
        var service = mock(PlanningService.class);
        var stage = new PlanningStage(service);
        var state = new PipelineExecutionState(List.of(stage));
        var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(ORIGINAL)
                .withUserConstraints(constraints("30 days"));
        state.setCognitiveState(before);
        var updatedConstraints = constraints("1 day");
        stage.replan(state, ProgressSnapshot.empty(), updatedConstraints, 4, ReplanningReason.DURATION_CHANGED);
        var after = state.getCognitiveState();
        assertEquals(plan(4), after.executionPlan());
        assertSame(after.executionPlan(), after.replanningResult().executionPlan());
        assertEquals(1, after.replanningResult().shiftedTasks());
        assertEquals(ReplanningReason.DURATION_CHANGED, after.replanningResult().reason());
        assertSame(updatedConstraints, after.userConstraints());
        assertSame(GRAPH, after.taskGraph());
        assertSame(ORIGINAL, before.executionPlan());
        assertNull(before.replanningResult());
        assertTrue(state.getMetadata().isEmpty());
        stage.replan(state, ProgressSnapshot.empty(), null, 4, ReplanningReason.MANUAL_REQUEST);
        assertSame(updatedConstraints, state.getCognitiveState().userConstraints());
        assertEquals(0, state.getCognitiveState().replanningResult().shiftedTasks());
        verifyNoInteractions(service);
    }

    @Test
    void rejectedUpdateLeavesEntireStateUntouched() {
        var stage = new PlanningStage();
        var state = new PipelineExecutionState(List.of(stage));
        var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(ORIGINAL);
        state.setCognitiveState(before);
        var impossible = new ProgressSnapshot(List.of(new ProgressEntry(TASK.taskId(), TaskProgress.COMPLETED)));
        assertThrows(IllegalArgumentException.class, () -> stage.replan(state, impossible,
                constraints("1 day"), 2, ReplanningReason.TASK_DELAYED));
        assertSame(before, state.getCognitiveState());
        assertTrue(state.getMetadata().isEmpty());
        assertThrows(NullPointerException.class, () -> stage.replan(new PipelineExecutionState(List.of()),
                ProgressSnapshot.empty(), null, 1, ReplanningReason.MANUAL_REQUEST));
    }

    @Test
    void resultSetterPreservesAllOtherRecordComponents() throws Exception {
        var before = CognitiveState.empty().withTaskGraph(GRAPH).withExecutionPlan(ORIGINAL).incrementReflection();
        var result = new ReplanningResult(plan(4), ReplanningReason.TASK_DELAYED, 1);
        var after = before.withReplanningResult(result);
        for (var component : CognitiveState.class.getRecordComponents()) {
            var expected = component.getName().equals("replanningResult") ? result : component.getAccessor().invoke(before);
            assertEquals(expected, component.getAccessor().invoke(after), component.getName());
        }
        assertThrows(NullPointerException.class, () -> before.withReplanningResult(null));
        assertSame(result, after.incrementReflection().replanningResult());
    }

    @TestFactory
    Stream<DynamicTest> everyArtifactCopyPreservesReplanningResult() {
        return Arrays.stream(CognitiveState.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("with") && !m.getName().equals("withReplanningResult"))
                .map(method -> DynamicTest.dynamicTest(method.getName(), () -> {
                    var result = new ReplanningResult(plan(4), ReplanningReason.TASK_DELAYED, 1);
                    var before = CognitiveState.empty().withReplanningResult(result);
                    Object[] args = Arrays.stream(method.getParameterTypes())
                            .map(type -> type == double.class ? (Object) 0.75 : mock(type)).toArray();
                    var after = (CognitiveState) method.invoke(before, args);
                    assertSame(result, after.replanningResult());
                    assertSame(result, before.replanningResult());
                }));
    }
}
