package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.engine.DefaultResourceTimeAllocationEngine;
import com.shreeai.os.platform.kernels.planning.model.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class ResourceTimeAllocationEngineTest {
    private final DefaultResourceTimeAllocationEngine engine = new DefaultResourceTimeAllocationEngine();
    private static final int[] HOURS = {3, 4, 8, 2, 3};
    private static final ResourceType[] RESOURCES = {
            ResourceType.STUDY, ResourceType.CODING, ResourceType.PROJECT,
            ResourceType.REVIEW, ResourceType.ASSESSMENT};

    private static UserConstraints constraints(String duration) {
        return UserConstraints.of(duration, null, null, null, null, null, List.of());
    }
    private static PlanningTask task(TaskType type, int order) {
        return PlanningTask.create("P2.3", "Task " + order, type, order);
    }
    private static TaskDependency edge(PlanningTask from, PlanningTask to) {
        return new TaskDependency(from.taskId(), to.taskId(), DependencyType.FINISH_TO_START);
    }

    @TestFactory
    Stream<DynamicTest> explicitDurationUnits() {
        return IntStream.rangeClosed(1, 10).boxed().flatMap(n -> Stream.of(
                DynamicTest.dynamicTest(n + " days", () -> assertEquals(n, engine.determineAvailableDays(n + " days"))),
                DynamicTest.dynamicTest(n + " weeks", () -> assertEquals(n * 7, engine.determineAvailableDays(n + " weeks"))),
                DynamicTest.dynamicTest(n + " months", () -> assertEquals(n * 30, engine.determineAvailableDays(n + " months")))));
    }

    @TestFactory
    Stream<DynamicTest> invalidExplicitDurations() {
        return Stream.of("", " ", "0 days", "-1 day", "+2 days", "1.5 weeks", "two weeks",
                "30", "30days", "1 year", "1 hour", "1 week 2 days", "about 30 days", "30 days later",
                "2147483648 days", "306783379 weeks", "71582789 months", "9999999999999999 days",
                "NaN days", "1e2 days", "0 months", "0 weeks", "1 fortnight", "1 d")
                .map(value -> DynamicTest.dynamicTest("reject [" + value + "]", () -> {
                    assertThrows(IllegalArgumentException.class, () -> engine.determineAvailableDays(value));
                    assertThrows(IllegalArgumentException.class, () -> engine.allocate(TaskGraph.empty(), constraints(value)));
                }));
    }

    @Test
    void durationNormalizationAndBoundaries() {
        assertEquals(30, engine.determineAvailableDays(null));
        assertEquals(1, engine.determineAvailableDays(" 1 DAY "));
        assertEquals(14, engine.determineAvailableDays("2\tWeEkS"));
        assertEquals(30, engine.determineAvailableDays("01 month"));
        assertEquals(Integer.MAX_VALUE, engine.determineAvailableDays("2147483647 days"));
        assertEquals(2147483646, engine.determineAvailableDays("306783378 weeks"));
        assertEquals(2147483640, engine.determineAvailableDays("71582788 months"));
    }

    @TestFactory
    Stream<DynamicTest> everyIndependentTypePair() {
        return Stream.of(TaskType.values()).flatMap(first -> Stream.of(TaskType.values()).map(second ->
                DynamicTest.dynamicTest(first + " with " + second, () -> {
                    var a = task(first, 1);
                    var b = task(second, 2);
                    var graph = TaskGraph.of(List.of(b, a), List.of());
                    var plan = engine.allocate(graph, constraints("30 days"));
                    int aHours = HOURS[first.ordinal()];
                    int bHours = HOURS[second.ordinal()];
                    int secondDay = aHours + bHours <= 8 ? 1 : 2;
                    assertEquals(List.of(
                            new ScheduledTask(a.taskId(), 1, aHours, List.of(new ResourceAllocation(RESOURCES[first.ordinal()], aHours))),
                            new ScheduledTask(b.taskId(), secondDay, bHours, List.of(new ResourceAllocation(RESOURCES[second.ordinal()], bHours)))), plan.scheduledTasks());
                    assertEquals(secondDay, plan.totalDays());
                    assertEquals(aHours + bHours, plan.totalHours());
                    assertEquals(plan, engine.allocate(graph, constraints("2 weeks")));
                })));
    }
    @TestFactory
    Stream<DynamicTest> chainsWithinAndBeyondSoftHorizons() {
        return IntStream.rangeClosed(1, 40).mapToObj(size -> DynamicTest.dynamicTest("chain length " + size, () -> {
            List<PlanningTask> tasks = new ArrayList<>();
            List<TaskDependency> edges = new ArrayList<>();
            int expectedHours = 0;
            for (int i = 0; i < size; i++) {
                TaskType type = TaskType.values()[i % 5];
                tasks.add(task(type, i + 1));
                expectedHours += HOURS[type.ordinal()];
                if (i > 0) edges.add(edge(tasks.get(i - 1), tasks.get(i)));
            }
            var graph = TaskGraph.of(tasks, edges);
            var plan = engine.allocate(graph, constraints("30 days"));
            assertEquals(size, plan.totalDays());
            assertEquals(expectedHours, plan.totalHours());
            for (int i = 0; i < size; i++) {
                assertEquals(i + 1, plan.scheduledTasks().get(i).day());
                assertEquals(tasks.get(i).taskId(), plan.scheduledTasks().get(i).taskId());
            }
            assertEquals(plan, engine.allocate(graph, constraints("2 weeks")));
            assertEquals(plan, engine.allocate(graph, null));
            Collections.reverse(tasks);
            Collections.reverse(edges);
            assertEquals(plan, engine.allocate(TaskGraph.of(tasks, edges), UserConstraints.empty()));
        }));
    }

    @Test
    void emptyAndNullInputs() {
        assertEquals(ExecutionPlan.empty(), engine.allocate(TaskGraph.empty(), null));
        assertEquals(ExecutionPlan.empty(), engine.allocate(TaskGraph.empty(), constraints("2 weeks")));
        assertThrows(NullPointerException.class, () -> engine.allocate(null, null));
    }

    @Test
    void diamondJoinAndBackfilling() {
        var a = task(TaskType.LEARNING, 1);
        var b = task(TaskType.IMPLEMENTATION, 2);
        var c = task(TaskType.PROJECT, 3);
        var d = task(TaskType.ASSESSMENT, 4);
        var independent = task(TaskType.REVIEW, 5);
        var graph = TaskGraph.of(List.of(a, b, c, d, independent),
                List.of(edge(a, b), edge(a, c), edge(b, d), edge(c, d)));
        var plan = engine.allocate(graph, null);
        assertEquals(List.of(a.taskId(), independent.taskId(), b.taskId(), c.taskId(), d.taskId()),
                plan.scheduledTasks().stream().map(ScheduledTask::taskId).toList());
        assertEquals(List.of(1, 1, 2, 3, 4), plan.scheduledTasks().stream().map(ScheduledTask::day).toList());
        assertEquals(20, plan.totalHours());
        assertEquals(4, plan.totalDays());
    }

    @TestFactory
    Stream<DynamicTest> allDependencyTypesRequireLaterDays() {
        return Stream.of(DependencyType.values()).map(type -> DynamicTest.dynamicTest(type.name(), () -> {
            var a = task(TaskType.REVIEW, 1);
            var b = task(TaskType.REVIEW, 2);
            var plan = engine.allocate(TaskGraph.of(List.of(b, a),
                    List.of(new TaskDependency(a.taskId(), b.taskId(), type))), null);
            assertEquals(List.of(1, 2), plan.scheduledTasks().stream().map(ScheduledTask::day).toList());
        }));
    }

    @Test
    void sharedEngineIsThreadSafe() throws Exception {
        var graph = TaskGraph.of(List.of(task(TaskType.PROJECT, 1), task(TaskType.REVIEW, 2)), List.of());
        var expected = engine.allocate(graph, null);
        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Callable<ExecutionPlan>> jobs = IntStream.range(0, 100)
                    .mapToObj(i -> (Callable<ExecutionPlan>) () -> engine.allocate(graph, constraints("2 weeks"))).toList();
            for (var result : pool.invokeAll(jobs)) assertEquals(expected, result.get());
        }
    }
}

