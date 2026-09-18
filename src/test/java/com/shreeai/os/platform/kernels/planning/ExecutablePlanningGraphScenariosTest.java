package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultExecutablePlanningGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.5: Executable planning graph scenarios")
class ExecutablePlanningGraphScenariosTest {

    private final DefaultExecutablePlanningGraphEngine engine = new DefaultExecutablePlanningGraphEngine();

    private static PlanningTask t(String milestone, String title, int order) {
        return PlanningTask.create(milestone, title, TaskType.LEARNING, order);
    }

    private static TaskDependency dep(PlanningTask from, PlanningTask to) {
        return new TaskDependency(from.taskId(), to.taskId(), DependencyType.FINISH_TO_START);
    }

    private static ScheduledTask s(PlanningTask task, int day, int hours) {
        return new ScheduledTask(task.taskId(), day, hours,
                List.of(new ResourceAllocation(ResourceType.STUDY, hours)));
    }

    private static ExecutionPlan plan(List<ScheduledTask> tasks) {
        int days = tasks.stream().mapToInt(ScheduledTask::day).max().orElse(0);
        int hours = tasks.stream().mapToInt(ScheduledTask::durationHours).sum();
        return new ExecutionPlan(days, hours, List.copyOf(tasks));
    }

    /* Six-task curriculum across three days: 1->2, 2->3, 1->4, 4->5, 3->6. */
    private record Curriculum(TaskGraph graph, ExecutionPlan plan,
                              List<PlanningTask> tasks) {
    }

    private static Curriculum curriculum() {
        var a = t("Java", "Variables", 1);
        var b = t("Java", "Loops", 2);
        var c = t("Java", "Methods", 3);
        var d = t("Java", "Classes", 4);
        var e = t("Java", "Objects", 5);
        var f = t("Java", "Polymorphism", 6);
        TaskGraph graph = TaskGraph.of(
                List.of(a, b, c, d, e, f),
                List.of(dep(a, b), dep(b, c), dep(a, d), dep(d, e), dep(c, f)));
        ExecutionPlan plan = plan(List.of(
                s(a, 1, 2), s(d, 1, 2),
                s(b, 2, 2), s(e, 2, 2),
                s(c, 3, 2), s(f, 3, 2)));
        return new Curriculum(graph, plan, List.of(a, b, c, d, e, f));
    }

    @Test @DisplayName("the initial curriculum graph starts one root READY and the rest BLOCKED")
    void initialStates() {
        var c = curriculum();
        var graph = engine.build(c.graph(), c.plan(), null, null);
        assertEquals(ExecutionState.READY, graph.nodeByTaskId(c.tasks().get(0).taskId()).state());
        for (int i = 1; i < c.tasks().size(); i++) {
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(c.tasks().get(i).taskId()).state(),
                    "task " + i);
        }
    }

    @Test @DisplayName("the curriculum graph has six nodes and five edges")
    void structure() {
        var c = curriculum();
        var graph = engine.build(c.graph(), c.plan(), null, null);
        assertEquals(6, graph.nodes().size());
        assertEquals(5, graph.edges().size());
    }

    @Test @DisplayName("nodes appear in canonical day-then-order sequence")
    void canonicalSequence() {
        var c = curriculum();
        var graph = engine.build(c.graph(), c.plan(), null, null);
        List<String> expected = List.of("Variables", "Classes", "Loops", "Objects", "Methods", "Polymorphism");
        List<String> actual = graph.nodes().stream().map(ExecutionNode::title).toList();
        assertEquals(expected, actual);
    }

    @Test @DisplayName("a day-1 task completing unblocks nothing new beyond its dependents")
    void completedScenario() {
        var c = curriculum();
        var root = c.tasks().get(0);
        var snapshot = new ProgressSnapshot(List.of(new ProgressEntry(root.taskId(), TaskProgress.COMPLETED)));
        var graph = engine.build(c.graph(), c.plan(),
                new ReplanningResult(c.plan(), ReplanningReason.MANUAL_REQUEST, 0), snapshot);
        assertEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(root.taskId()).state());
        assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(c.tasks().get(1).taskId()).state());
        assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(c.tasks().get(3).taskId()).state());
    }

    @Test @DisplayName("an in-progress day-1 task becomes READY")
    void inProgressScenario() {
        var c = curriculum();
        var root = c.tasks().get(0);
        var snapshot = new ProgressSnapshot(List.of(new ProgressEntry(root.taskId(), TaskProgress.IN_PROGRESS)));
        var graph = engine.build(c.graph(), c.plan(),
                new ReplanningResult(c.plan(), ReplanningReason.TASK_DELAYED, 0), snapshot);
        assertEquals(ExecutionState.READY, graph.nodeByTaskId(root.taskId()).state());
    }

    @Test @DisplayName("mixed progress applies each locked transition independently")
    void mixedProgress() {
        var c = curriculum();
        var a = c.tasks().get(0);
        var d = c.tasks().get(3);
        var f = c.tasks().get(5);
        var snapshot = new ProgressSnapshot(List.of(
                new ProgressEntry(a.taskId(), TaskProgress.COMPLETED),
                new ProgressEntry(d.taskId(), TaskProgress.IN_PROGRESS),
                new ProgressEntry(f.taskId(), TaskProgress.NOT_STARTED)));
        var graph = engine.build(c.graph(), c.plan(),
                new ReplanningResult(c.plan(), ReplanningReason.NEW_CONSTRAINT, 0), snapshot);
        assertEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(a.taskId()).state());
        assertEquals(ExecutionState.READY, graph.nodeByTaskId(d.taskId()).state());
        assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(f.taskId()).state());
    }

    @Test @DisplayName("the same curriculum always builds an equal graph")
    void deterministicCurriculum() {
        assertEquals(engine.build(curriculum().graph(), curriculum().plan(), null, null),
                engine.build(curriculum().graph(), curriculum().plan(), null, null));
    }

    @Test @DisplayName("a shifted schedule changes node identity deterministically")
    void shiftedSchedule() {
        var c = curriculum();
        var graphDay1 = engine.build(c.graph(), c.plan(), null, null);
        var shifted = plan(List.of(
                s(c.tasks().get(0), 2, 2), s(c.tasks().get(3), 2, 2),
                s(c.tasks().get(1), 3, 2), s(c.tasks().get(4), 3, 2),
                s(c.tasks().get(2), 4, 2), s(c.tasks().get(5), 4, 2)));
        var graphDay2 = engine.build(c.graph(), shifted, null, null);
        assertNotEquals(graphDay1, graphDay2);
        assertEquals(graphDay1.nodes().size(), graphDay2.nodes().size());
        for (int i = 0; i < c.tasks().size(); i++) {
            assertNotEquals(graphDay1.nodeByTaskId(c.tasks().get(i).taskId()).nodeId(),
                    graphDay2.nodeByTaskId(c.tasks().get(i).taskId()).nodeId(), "task " + i);
        }
    }

    @Test @DisplayName("an empty graph and plan produce an empty executable graph")
    void emptyInputs() {
        var graph = engine.build(TaskGraph.empty(), ExecutionPlan.empty(), null, null);
        assertTrue(graph.nodes().isEmpty());
        assertTrue(graph.edges().isEmpty());
    }

    @Test @DisplayName("a wide fan-out keeps every child blocked")
    void wideFanOut() {
        var root = t("Java", "Root", 1);
        List<PlanningTask> children = new ArrayList<>();
        List<TaskDependency> deps = new ArrayList<>();
        List<ScheduledTask> scheduled = new ArrayList<>();
        scheduled.add(s(root, 1, 2));
        for (int i = 0; i < 3; i++) {
            var child = t("Java", "Child " + i, i + 2);
            children.add(child);
            deps.add(dep(root, child));
            scheduled.add(s(child, (i / 4) + 2, 2));
        }
        var g = TaskGraph.of(
                List.of(root, children.get(0), children.get(1), children.get(2)), deps);
        var graph = engine.build(g, plan(scheduled), null, null);
        assertEquals(ExecutionState.READY, graph.nodeByTaskId(root.taskId()).state());
        for (var child : children) {
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(child.taskId()).state());
        }
        assertEquals(3, graph.edges().size());
    }

    @Test @DisplayName("a six-node pipeline builds iteratively without deep recursion")
    void pipelineChain() {
        int depth = 30;
        List<PlanningTask> tasks = new ArrayList<>(depth);
        List<TaskDependency> deps = new ArrayList<>(depth - 1);
        List<ScheduledTask> scheduled = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            var task = t("Java", "Stage " + i, i + 1);
            tasks.add(task);
            scheduled.add(s(task, (i / 4) + 1, 2));
            if (i > 0) {
                deps.add(dep(tasks.get(i - 1), task));
            }
        }
        var graph = engine.build(TaskGraph.of(tasks, deps), plan(scheduled), null, null);
        assertEquals(ExecutionState.READY, graph.nodeByTaskId(tasks.get(0).taskId()).state());
        assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(tasks.get(depth - 1).taskId()).state());
    }
}
