package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultExecutablePlanningGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.5: DefaultExecutablePlanningGraphEngine pipeline")
class DefaultExecutablePlanningGraphEngineTest {

    private final DefaultExecutablePlanningGraphEngine engine = new DefaultExecutablePlanningGraphEngine();

    private static PlanningTask t(String title, int order) {
        return PlanningTask.create("Learn Java", title, TaskType.LEARNING, order);
    }

    private static TaskDependency dep(PlanningTask from, PlanningTask to) {
        return new TaskDependency(from.taskId(), to.taskId(), DependencyType.FINISH_TO_START);
    }

    private static ScheduledTask s(PlanningTask task, int day) {
        return new ScheduledTask(task.taskId(), day, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 2)));
    }

    private static ExecutionPlan plan(List<ScheduledTask> tasks) {
        int days = tasks.stream().mapToInt(ScheduledTask::day).max().orElse(0);
        int hours = tasks.stream().mapToInt(ScheduledTask::durationHours).sum();
        return new ExecutionPlan(days, hours, List.copyOf(tasks));
    }

    private static ProgressSnapshot progress(Object... pairs) {
        List<ProgressEntry> entries = new ArrayList<>();
        for (int i = 0; i < pairs.length; i += 2) {
            entries.add(new ProgressEntry(((PlanningTask) pairs[i]).taskId(), (TaskProgress) pairs[i + 1]));
        }
        return new ProgressSnapshot(entries);
    }

    /* Two-task chain: A(order 1, day 1) -> B(order 2, day 2). */
    private static TaskGraph chain() {
        PlanningTask a = t("Variables", 1);
        PlanningTask b = t("Loops", 2);
        return TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
    }

    private static ExecutionPlan chainPlan() {
        return plan(List.of(s(t("Variables", 1), 1), s(t("Loops", 2), 2)));
    }

    @Nested
    @DisplayName("Stage 1 - scheduled tasks become execution nodes")
    class NodeGeneration {
        @Test @DisplayName("exactly one node exists per scheduled task")
        void oneNodePerTask() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals(2, graph.nodes().size());
        }
        @Test @DisplayName("tasks without predecessors start READY")
        void readyWithoutPredecessors() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(t("Variables", 1).taskId()).state());
        }
        @Test @DisplayName("tasks with predecessors start BLOCKED")
        void blockedWithPredecessors() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(t("Loops", 2).taskId()).state());
        }
        @Test @DisplayName("titles come from the task graph, not the schedule")
        void titlesFromGraph() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals("Variables", graph.nodeByTaskId(t("Variables", 1).taskId()).title());
            assertEquals("Loops", graph.nodeByTaskId(t("Loops", 2).taskId()).title());
        }
        @Test @DisplayName("days come from the schedule")
        void daysFromPlan() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals(1, graph.nodeByTaskId(t("Variables", 1).taskId()).day());
            assertEquals(2, graph.nodeByTaskId(t("Loops", 2).taskId()).day());
        }
        @Test @DisplayName("nodeId is the deterministic SHA-256 of taskId|day|title")
        void deterministicNodeId() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            var task = t("Variables", 1);
            var node = graph.nodeByTaskId(task.taskId());
            assertEquals(DefaultExecutablePlanningGraphEngine.nodeId(task.taskId(), 1, "Variables"),
                    node.nodeId());
            assertEquals(64, node.nodeId().length());
            assertTrue(node.nodeId().matches("[0-9a-f]{64}"));
        }
        @Test @DisplayName("same task on different days yields different nodeIds")
        void dayChangesIdentity() {
            var task = t("Variables", 1);
            assertNotEquals(DefaultExecutablePlanningGraphEngine.nodeId(task.taskId(), 1, "Variables"),
                    DefaultExecutablePlanningGraphEngine.nodeId(task.taskId(), 2, "Variables"));
        }
        @Test @DisplayName("a scheduled task missing from the graph is rejected")
        void unknownScheduledTask() {
            var stranger = PlanningTask.create("Other", "Stranger", TaskType.LEARNING, 1);
            var badPlan = plan(List.of(s(stranger, 1)));
            assertThrows(IllegalArgumentException.class,
                    () -> engine.build(chain(), badPlan, null, null));
        }
        @Test @DisplayName("null taskGraph and null executionPlan are rejected")
        void nullArguments() {
            assertThrows(IllegalArgumentException.class, () -> engine.build(null, chainPlan(), null, null));
            assertThrows(IllegalArgumentException.class, () -> engine.build(chain(), null, null, null));
        }
    }

    @Nested
    @DisplayName("Stage 2 - dependencies become execution edges")
    class EdgeGeneration {
        @Test @DisplayName("one edge per dependency, never inferred")
        void oneEdgePerDependency() {
            var graph = engine.build(chain(), chainPlan(), null, null);
            assertEquals(1, graph.edges().size());
            var edge = graph.edges().get(0);
            assertEquals(graph.nodeByTaskId(t("Variables", 1).taskId()).nodeId(), edge.fromNodeId());
            assertEquals(graph.nodeByTaskId(t("Loops", 2).taskId()).nodeId(), edge.toNodeId());
        }
        @Test @DisplayName("a graph without dependencies produces no edges")
        void noDependenciesNoEdges() {
            var task = t("Solo", 1);
            var g = TaskGraph.of(List.of(task), List.of());
            var graph = engine.build(g, plan(List.of(s(task, 1))), null, null);
            assertTrue(graph.edges().isEmpty());
        }
        @Test @DisplayName("several dependencies all materialize")
        void multipleDependencies() {
            var a = t("A", 1);
            var b = t("B", 2);
            var c = t("C", 3);
            var g = TaskGraph.of(List.of(a, b, c), List.of(dep(a, b), dep(a, c), dep(b, c)));
            var p = plan(List.of(s(a, 1), s(b, 2), s(c, 3)));
            var graph = engine.build(g, p, null, null);
            assertEquals(3, graph.edges().size());
        }
        @Test @DisplayName("a dependency whose endpoint is unscheduled is rejected")
        void danglingDependencyRejected() {
            var a = t("A", 1);
            var b = t("B", 2);
            var g = TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
            var p = plan(List.of(s(a, 1)));
            assertThrows(IllegalArgumentException.class, () -> engine.build(g, p, null, null));
        }
    }

    @Nested
    @DisplayName("Stage 3 - progress applied only with a replanning result")
    class ProgressApplication {
        @Test @DisplayName("null replanningResult ignores progress entirely")
        void nullResultIgnoresProgress() {
            var a = t("Variables", 1);
            var b = t("Loops", 2);
            var g = chain();
            var p = chainPlan();
            var snap = progress(a, TaskProgress.COMPLETED, b, TaskProgress.IN_PROGRESS);
            var graph = engine.build(g, p, null, snap);
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(a.taskId()).state());
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(b.taskId()).state());
        }
        @Test @DisplayName("COMPLETED marks the node COMPLETED")
        void completed() {
            var a = t("Variables", 1);
            var g = chain();
            var graph = engine.build(g, chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0),
                    progress(a, TaskProgress.COMPLETED));
            assertEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(a.taskId()).state());
        }
        @Test @DisplayName("IN_PROGRESS makes the node READY even when blocked")
        void inProgress() {
            var b = t("Loops", 2);
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0),
                    progress(b, TaskProgress.IN_PROGRESS));
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(b.taskId()).state());
        }
        @Test @DisplayName("NOT_STARTED on a blocked node stays BLOCKED")
        void notStartedBlocked() {
            var b = t("Loops", 2);
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0),
                    progress(b, TaskProgress.NOT_STARTED));
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(b.taskId()).state());
        }
        @Test @DisplayName("NOT_STARTED on an unblocked node stays READY")
        void notStartedReady() {
            var a = t("Variables", 1);
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0),
                    progress(a, TaskProgress.NOT_STARTED));
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(a.taskId()).state());
        }
        @Test @DisplayName("a missing progress entry leaves the initial state untouched")
        void missingEntryIgnored() {
            var a = t("Variables", 1);
            var b = t("Loops", 2);
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0),
                    progress(a, TaskProgress.COMPLETED));
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(b.taskId()).state());
        }
        @Test @DisplayName("a non-null result with a null snapshot keeps initial states")
        void resultWithoutSnapshot() {
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.TASK_DELAYED, 0), null);
            assertEquals(ExecutionState.READY, graph.nodeByTaskId(t("Variables", 1).taskId()).state());
            assertEquals(ExecutionState.BLOCKED, graph.nodeByTaskId(t("Loops", 2).taskId()).state());
        }
        @Test @DisplayName("COMPLETED applies regardless of predecessors")
        void completedRootTask() {
            var a = t("Variables", 1);
            var graph = engine.build(chain(), chainPlan(),
                    new ReplanningResult(chainPlan(), ReplanningReason.MANUAL_REQUEST, 0),
                    progress(a, TaskProgress.COMPLETED));
            assertEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(a.taskId()).state());
        }
        @Test @DisplayName("only the three locked transitions ever apply")
        void noOtherTransitions() {
            var a = t("Variables", 1);
            var b = t("Loops", 2);
            var g = chain();
            var p = chainPlan();
            var result = new ReplanningResult(p, ReplanningReason.TASK_DELAYED, 0);
            var graph = engine.build(g, p, result, progress(a, TaskProgress.IN_PROGRESS));
            assertNotEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(a.taskId()).state());
            assertNotEquals(ExecutionState.COMPLETED, graph.nodeByTaskId(b.taskId()).state());
        }
    }

    @Nested
    @DisplayName("Stages 4 + 5 - validation and canonical ordering")
    class ValidationAndOrdering {
        @Test @DisplayName("disconnected subgraphs are accepted")
        void disconnectedAccepted() {
            var a = t("A", 1);
            var b = t("B", 2);
            var g = TaskGraph.of(List.of(a, b), List.of());
            var graph = engine.build(g, plan(List.of(s(a, 1), s(b, 2))), null, null);
            assertEquals(2, graph.nodes().size());
            assertTrue(graph.edges().isEmpty());
        }
        @Test @DisplayName("nodes are canonically ordered by day, then order, then title")
        void nodeOrdering() {
            var early = t("B task", 3);
            var late = t("A task", 1);
            var g = TaskGraph.of(List.of(late, early), List.of());
            var p = plan(List.of(s(early, 1), s(late, 2)));
            var graph = engine.build(g, p, null, null);
            assertEquals("B task", graph.nodes().get(0).title());
            assertEquals("A task", graph.nodes().get(1).title());
            assertEquals(1, graph.nodes().get(0).day());
        }
        @Test @DisplayName("same-day nodes order by composite task order")
        void sameDayOrderByOrder() {
            var first = t("Zebra", 1);
            var second = t("Apple", 2);
            var g = TaskGraph.of(List.of(second, first), List.of());
            var p = plan(List.of(s(second, 1), s(first, 1)));
            var graph = engine.build(g, p, null, null);
            assertEquals("Zebra", graph.nodes().get(0).title());
            assertEquals("Apple", graph.nodes().get(1).title());
        }
        @Test @DisplayName("edges are canonically ordered by from then to")
        void edgeOrdering() {
            var a = t("A", 1);
            var b = t("B", 2);
            var c = t("C", 3);
            var g = TaskGraph.of(List.of(a, b, c),
                    List.of(dep(b, c), dep(a, c), dep(a, b)));
            var p = plan(List.of(s(a, 1), s(b, 2), s(c, 3)));
            var graph = engine.build(g, p, null, null);
            var keys = graph.edges().stream().map(ExecutionEdge::edgeKey).collect(Collectors.toList());
            assertEquals(keys.stream().sorted().collect(Collectors.toList()), keys);
            assertEquals(3, graph.edges().size());
        }
        @Test @DisplayName("the same inputs always produce the byte-identical graph")
        void sameInputIdenticalGraph() {
            var g = chain();
            var p = chainPlan();
            assertEquals(engine.build(g, p, null, null), engine.build(g, p, null, null));
            assertEquals(engine.build(g, p, null, null).hashCode(),
                    engine.build(g, p, null, null).hashCode());
        }
        @Test @DisplayName("input schedule order does not change the ordered graph")
        void inputOrderIrrelevant() {
            var a = t("Variables", 1);
            var b = t("Loops", 2);
            var g1 = TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
            var g2 = TaskGraph.of(List.of(b, a), List.of(dep(a, b)));
            var graph1 = engine.build(g1, plan(List.of(s(a, 1), s(b, 2))), null, null);
            var graph2 = engine.build(g2, plan(List.of(s(a, 1), s(b, 2))), null, null);
            assertEquals(graph1, graph2);
        }
        @Test @DisplayName("a deep chain builds without recursion limits")
        void deepChain() {
            int depth = 200;
            List<PlanningTask> tasks = new ArrayList<>(depth);
            List<TaskDependency> deps = new ArrayList<>(depth - 1);
            List<ScheduledTask> scheduled = new ArrayList<>(depth);
            for (int i = 0; i < depth; i++) {
                var task = t("Step " + i, i + 1);
                tasks.add(task);
                scheduled.add(s(task, i / 4 + 1));
                if (i > 0) {
                    deps.add(dep(tasks.get(i - 1), task));
                }
            }
            var graph = engine.build(TaskGraph.of(tasks, deps), plan(scheduled), null, null);
            assertEquals(depth, graph.nodes().size());
            assertEquals(depth - 1, graph.edges().size());
        }
        @Test @DisplayName("a shared engine instance is safe under concurrent builds")
        void threadSafe() throws Exception {
            var g = chain();
            var p = chainPlan();
            var expected = engine.build(g, p, null, null);
            try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
                List<Future<ExecutablePlanningGraph>> futures = new ArrayList<>();
                for (int i = 0; i < 16; i++) {
                    futures.add(pool.submit((Callable<ExecutablePlanningGraph>) () -> engine.build(g, p, null, null)));
                }
                for (Future<ExecutablePlanningGraph> future : futures) {
                    assertEquals(expected, future.get());
                }
            }
        }
    }
}
