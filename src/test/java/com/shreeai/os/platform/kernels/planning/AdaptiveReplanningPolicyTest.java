package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.engine.DefaultAdaptiveReplanningEngine;
import com.shreeai.os.platform.kernels.planning.model.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveReplanningPolicyTest {
    private final DefaultAdaptiveReplanningEngine engine = new DefaultAdaptiveReplanningEngine();

    private static PlanningTask task(int i) {
        return PlanningTask.create("Milestone " + i, "Task " + i, TaskType.LEARNING, i + 1);
    }

    private static ScheduledTask scheduled(PlanningTask task, int day, int hours) {
        return new ScheduledTask(task.taskId(), day, hours,
                List.of(new ResourceAllocation(ResourceType.STUDY, hours)));
    }

    private static ExecutionPlan plan(ScheduledTask... tasks) {
        return new ExecutionPlan(tasks.length == 0 ? 0 : tasks[tasks.length - 1].day(),
                Stream.of(tasks).mapToInt(ScheduledTask::durationHours).sum(), List.of(tasks));
    }

    private static TaskDependency edge(PlanningTask a, PlanningTask b) {
        return new TaskDependency(a.taskId(), b.taskId(), DependencyType.FINISH_TO_START);
    }

    private static ProgressSnapshot completed(PlanningTask... tasks) {
        return new ProgressSnapshot(Stream.of(tasks)
                .map(t -> new ProgressEntry(t.taskId(), TaskProgress.COMPLETED)).toList());
    }

    private ReplanningResult repair(ExecutionPlan plan, TaskGraph graph, ProgressSnapshot progress, int day) {
        return engine.replan(plan, graph, progress, null, day, ReplanningReason.TASK_DELAYED);
    }

    @TestFactory
    Stream<DynamicTest> allWholeTaskCapacityPairs() {
        return IntStream.rangeClosed(1, 8).boxed().flatMap(aHours -> IntStream.rangeClosed(1, 8)
                .mapToObj(bHours -> DynamicTest.dynamicTest(aHours + "h followed by " + bHours + "h", () -> {
                    var a = task(0);
                    var b = task(1);
                    var original = plan(scheduled(a, 1, aHours), scheduled(b, 2, bHours));
                    var graph = TaskGraph.of(List.of(b, a), List.of());
                    var result = repair(original, graph, ProgressSnapshot.empty(), 3);
                    int bDay = aHours + bHours <= 8 ? 3 : 4;
                    assertEquals(plan(scheduled(a, 3, aHours), scheduled(b, bDay, bHours)), result.executionPlan());
                    assertEquals(2, result.shiftedTasks());
                    assertEquals(result, repair(original, graph, ProgressSnapshot.empty(), 3));
                    var repeat = repair(result.executionPlan(), graph, ProgressSnapshot.empty(), 3);
                    assertSame(result.executionPlan(), repeat.executionPlan());
                    assertEquals(0, repeat.shiftedTasks());
                })));
    }

    @TestFactory
    Stream<DynamicTest> pinnedPrefixAndDelayedSuffix() {
        return IntStream.rangeClosed(0, 5).boxed().flatMap(pinned -> IntStream.rangeClosed(1, 8)
                .mapToObj(delay -> DynamicTest.dynamicTest(pinned + " completed, delay " + delay, () -> {
                    var tasks = IntStream.range(0, 6).mapToObj(AdaptiveReplanningPolicyTest::task).toList();
                    var graph = TaskGraph.of(tasks, IntStream.range(1, 6)
                            .mapToObj(i -> edge(tasks.get(i - 1), tasks.get(i))).toList());
                    var original = plan(IntStream.range(0, 6)
                            .mapToObj(i -> scheduled(tasks.get(i), i + 1, 3)).toArray(ScheduledTask[]::new));
                    var progress = completed(tasks.subList(0, pinned).toArray(PlanningTask[]::new));
                    var result = repair(original, graph, progress, pinned + 1 + delay);
                    assertEquals(6 - pinned, result.shiftedTasks());
                    assertEquals(6 + delay, result.executionPlan().totalDays());
                    assertEquals(18, result.executionPlan().totalHours());
                    for (int i = 0; i < 6; i++) {
                        var before = original.scheduledTasks().get(i);
                        var after = result.executionPlan().scheduledTasks().get(i);
                        assertEquals(i < pinned ? i + 1 : i + 1 + delay, after.day());
                        assertEquals(before.taskId(), after.taskId());
                        assertEquals(before.resources(), after.resources());
                        if (i < pinned) assertSame(before, after);
                    }
                })));
    }

    @TestFactory
    Stream<DynamicTest> durationIsSoftHorizonAndReasonIsExplicit() {
        return Stream.of(ReplanningReason.values()).flatMap(reason -> Stream.of("1 day", "2 days", "30 days", "2 months")
                .map(duration -> DynamicTest.dynamicTest(reason + ": " + duration, () -> {
                    var a = task(0);
                    var original = plan(scheduled(a, 5, 3));
                    var constraints = UserConstraints.of(duration, null, null, null, null, null, List.of());
                    var result = engine.replan(original, TaskGraph.of(List.of(a), List.of()),
                            ProgressSnapshot.empty(), constraints, 1, reason);
                    assertSame(original, result.executionPlan());
                    assertEquals(0, result.shiftedTasks());
                    assertEquals(reason, result.reason());
                })));
    }

    @Test
    void rejectsInconsistentProgressAndGraphWithoutMutatingInput() {
        var a = task(0);
        var b = task(1);
        var original = plan(scheduled(a, 1, 3), scheduled(b, 2, 3));
        var graph = TaskGraph.of(List.of(a, b), List.of(edge(a, b)));
        assertThrows(IllegalArgumentException.class, () -> repair(original, graph, completed(b), 2));
        assertThrows(IllegalArgumentException.class, () -> repair(original, graph, completed(a, b), 1));
        assertThrows(IllegalArgumentException.class, () -> repair(original, graph, completed(task(2)), 3));
        assertThrows(IllegalArgumentException.class, () -> repair(original, TaskGraph.empty(), ProgressSnapshot.empty(), 2));
        assertThrows(IllegalArgumentException.class, () -> repair(original,
                TaskGraph.of(List.of(a, task(2)), List.of()), ProgressSnapshot.empty(), 2));
        assertThrows(IllegalArgumentException.class, () -> repair(original,
                TaskGraph.of(List.of(a, b), List.of(edge(b, a))), ProgressSnapshot.empty(), 2));
        assertEquals(plan(scheduled(a, 1, 3), scheduled(b, 2, 3)), original);
    }

    @Test
    void reservesPinnedCapacityAndRejectsImpossibleOrder() {
        var a = task(0);
        var b = task(1);
        var graph = TaskGraph.of(List.of(a, b), List.of());
        var fits = plan(scheduled(a, 1, 4), scheduled(b, 2, 4));
        var result = repair(fits, graph, completed(b), 2);
        assertEquals(plan(scheduled(a, 2, 4), scheduled(b, 2, 4)), result.executionPlan());
        assertSame(fits.scheduledTasks().get(1), result.executionPlan().scheduledTasks().get(1));
        var blocked = plan(scheduled(a, 1, 5), scheduled(b, 2, 4));
        assertThrows(IllegalArgumentException.class, () -> repair(blocked, graph, completed(b), 2));
        assertThrows(IllegalArgumentException.class, () -> repair(fits, graph, completed(b), 3));
    }

    @Test
    void boundaryDaysAndMissingStatuses() {
        var a = task(0);
        var b = task(1);
        var graph = TaskGraph.of(List.of(a), List.of());
        var original = plan(scheduled(a, 1, 3));
        assertEquals(Integer.MAX_VALUE, repair(original, graph, ProgressSnapshot.empty(), Integer.MAX_VALUE)
                .executionPlan().totalDays());
        for (int day : new int[]{Integer.MIN_VALUE, -1, 0}) {
            assertThrows(IllegalArgumentException.class, () -> repair(original, graph, ProgressSnapshot.empty(), day));
        }
        for (var status : List.of(TaskProgress.NOT_STARTED, TaskProgress.IN_PROGRESS)) {
            assertEquals(repair(original, graph, ProgressSnapshot.empty(), 3), repair(original, graph,
                    new ProgressSnapshot(List.of(new ProgressEntry(a.taskId(), status))), 3));
        }
        assertSame(original, repair(original, graph, completed(a), 10).executionPlan());
        assertEquals(ExecutionPlan.empty(), repair(ExecutionPlan.empty(), TaskGraph.empty(),
                ProgressSnapshot.empty(), 10).executionPlan());
        assertThrows(ArithmeticException.class, () -> repair(plan(scheduled(a, 1, 3), scheduled(b, 2, 3)),
                TaskGraph.of(List.of(a, b), List.of(edge(a, b))), ProgressSnapshot.empty(), Integer.MAX_VALUE));
    }

    @Test
    void progressAndResultAreValidatedAndDeeplyImmutable() {
        var entry = new ProgressEntry("task", TaskProgress.IN_PROGRESS);
        var entries = new ArrayList<>(List.of(entry));
        var snapshot = new ProgressSnapshot(entries);
        entries.clear();
        assertEquals(List.of(entry), snapshot.progress());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.progress().clear());
        assertThrows(IllegalArgumentException.class, () -> new ProgressSnapshot(List.of(entry, entry)));
        assertThrows(NullPointerException.class, () -> new ProgressSnapshot(null));
        assertThrows(NullPointerException.class, () -> new ProgressSnapshot(java.util.Arrays.asList((ProgressEntry) null)));
        assertThrows(NullPointerException.class, () -> new ProgressEntry(null, TaskProgress.COMPLETED));
        assertThrows(NullPointerException.class, () -> new ProgressEntry("task", null));
        assertThrows(IllegalArgumentException.class, () -> new ProgressEntry(" ", TaskProgress.COMPLETED));
        assertThrows(NullPointerException.class, () -> new ReplanningResult(null, ReplanningReason.MANUAL_REQUEST, 0));
        assertThrows(NullPointerException.class, () -> new ReplanningResult(ExecutionPlan.empty(), null, 0));
        for (int count : new int[]{-1, 1}) {
            assertThrows(IllegalArgumentException.class, () -> new ReplanningResult(ExecutionPlan.empty(),
                    ReplanningReason.MANUAL_REQUEST, count));
        }
    }
}


