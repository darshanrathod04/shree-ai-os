package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultAdaptiveReplanningEngine;
import com.shreeai.os.platform.kernels.planning.engine.DefaultResourceTimeAllocationEngine;
import com.shreeai.os.platform.kernels.planning.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class AdaptiveReplanningEngineTest {
    @Test
    void delayedMethodsPreservesCompletedWorkAndShiftsOnlyUnfinishedTasks() {
        var names = List.of("Variables", "Loops", "Methods", "Classes", "OOP");
        var tasks = IntStream.range(0, names.size())
                .mapToObj(i -> PlanningTask.create("Java", names.get(i), TaskType.LEARNING, i + 1)).toList();
        var edges = IntStream.range(1, tasks.size())
                .mapToObj(i -> new TaskDependency(tasks.get(i - 1).taskId(), tasks.get(i).taskId(),
                        DependencyType.FINISH_TO_START)).toList();
        var graph = TaskGraph.of(tasks, edges);
        var original = new DefaultResourceTimeAllocationEngine().allocate(graph, null);
        var progress = new ProgressSnapshot(List.of(
                new ProgressEntry(tasks.get(0).taskId(), TaskProgress.COMPLETED),
                new ProgressEntry(tasks.get(1).taskId(), TaskProgress.COMPLETED),
                new ProgressEntry(tasks.get(2).taskId(), TaskProgress.IN_PROGRESS)));
        var engine = new DefaultAdaptiveReplanningEngine();
        var result = engine.replan(original, graph, progress, null, 4, ReplanningReason.TASK_DELAYED);
        assertEquals(List.of(1, 2, 4, 5, 6), result.executionPlan().scheduledTasks().stream()
                .map(ScheduledTask::day).toList());
        assertEquals(3, result.shiftedTasks());
        assertEquals(ReplanningReason.TASK_DELAYED, result.reason());
        assertEquals(6, result.executionPlan().totalDays());
        assertEquals(15, result.executionPlan().totalHours());
        assertEquals(List.of(1, 2, 3, 4, 5), original.scheduledTasks().stream().map(ScheduledTask::day).toList());
        for (int i = 0; i < tasks.size(); i++) {
            var before = original.scheduledTasks().get(i);
            var after = result.executionPlan().scheduledTasks().get(i);
            assertEquals(before.taskId(), after.taskId());
            assertEquals(before.durationHours(), after.durationHours());
            assertEquals(before.resources(), after.resources());
            if (i < 2) assertSame(before, after);
        }
        assertEquals(result, engine.replan(original, graph, progress, null, 4, ReplanningReason.TASK_DELAYED));
    }
}
