package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.error.PlanValidationException;
import com.shreeai.os.platform.kernels.planning.model.DependencyType;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.TaskDependency;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.2: TaskGraph model, validation & immutability")
class TaskGraphModelTest {

    private static final String TASK_1 = "9e8bfb76782a36a15c238ecde1c5b5b0db789064ee40716ccb4887c329a64982";
    private static final String TASK_2 = "3e60b55c9900c81de4a459b1fd6be9316e0043d08e67edb83ec9a683eb8edf1e";
    private static final String TASK_3 = "168757d7d7922cbe17b0cbcfc6b6c37f0dbf74f9d4c1f103dbdfd7daeb0cc1b3";

    private PlanningTask v1() { return PlanningTask.create("Java Basics", "Variables", TaskType.LEARNING, 1); }
    private PlanningTask v2() { return PlanningTask.create("Java Basics", "Loops", TaskType.LEARNING, 2); }
    private PlanningTask v3() { return PlanningTask.create("Java Basics", "Methods", TaskType.LEARNING, 3); }
    private TaskDependency dep(PlanningTask from, PlanningTask to) {
        return new TaskDependency(from.taskId(), to.taskId(), DependencyType.FINISH_TO_START);
    }

    @Nested
    @DisplayName("empty / factory")
    class EmptyAndFactory {
        @Test @DisplayName("empty() has no tasks, no edges")
        void emptyHasNothing() {
            TaskGraph g = TaskGraph.empty();
            assertTrue(g.tasks().isEmpty());
            assertTrue(g.dependencies().isEmpty());
            assertEquals(0, g.taskCount());
            assertEquals(0, g.dependencyCount());
            assertTrue(g.isEmpty());
        }
        @Test @DisplayName("of() with empty tasks returns an empty graph")
        void ofEmptyReturnsEmpty() {
            assertTrue(TaskGraph.of(List.of(), List.of()).isEmpty());
        }
        @Test @DisplayName("of() builds an acyclic graph from acyclic input")
        void ofAcyclic() {
            PlanningTask a = v1();
            PlanningTask b = v2();
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
            assertEquals(2, g.taskCount());
            assertEquals(1, g.dependencyCount());
        }
        @Test @DisplayName("null dependency list is accepted as empty")
        void nullDependenciesAccepted() {
            PlanningTask a = v1();
            TaskGraph g = TaskGraph.of(List.of(a), null);
            assertEquals(0, g.dependencyCount());
        }
    }

    @Nested
    @DisplayName("task validation")
    class TaskValidation {
        @Test @DisplayName("duplicate task id is rejected")
        void duplicateTask() {
            PlanningTask a = v1();
            PlanningTask b = PlanningTask.create("Java Basics", "Variables", TaskType.LEARNING, 1);
            assertEquals(a.taskId(), b.taskId());
            assertThrows(IllegalArgumentException.class,
                    () -> TaskGraph.of(List.of(a, b), List.of()));
        }
        @Test @DisplayName("task with invalid id format is rejected at construction")
        void invalidTaskId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask("deadbeef", "T", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("null task list is tolerated as an empty graph")
        void nullTaskList() {
            assertTrue(TaskGraph.of(null, List.of()).isEmpty());
        }
        @Test @DisplayName("null task element is rejected")
        void nullTaskElement() {
            assertThrows(NullPointerException.class,
                    () -> TaskGraph.of(new ArrayList<>(List.of(v1(), null)), List.of()));
        }
    }

    @Nested
    @DisplayName("dependency validation")
    class DependencyValidation {
        @Test @DisplayName("dangling dependency endpoint is rejected")
        void danglingEndpoint() {
            PlanningTask a = v1();
            TaskDependency d = new TaskDependency(a.taskId(), TASK_2, DependencyType.FINISH_TO_START);
            assertThrows(IllegalArgumentException.class,
                    () -> TaskGraph.of(List.of(a), List.of(d)));
        }
        @Test @DisplayName("dependency referencing unknown tasks is rejected")
        void unknownTasks() {
            TaskDependency d = new TaskDependency(TASK_1, TASK_2, DependencyType.FINISH_TO_START);
            assertThrows(IllegalArgumentException.class,
                    () -> TaskGraph.of(List.of(v3()), List.of(d)));
        }
        @Test @DisplayName("self-dependency is rejected at the edge level")
        void selfEdge() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TaskDependency(TASK_1, TASK_1, DependencyType.FINISH_TO_START));
        }
        @Test @DisplayName("duplicate edge is rejected")
        void duplicateEdge() {
            PlanningTask a = v1(), b = v2();
            TaskDependency d = dep(a, b);
            assertThrows(IllegalArgumentException.class,
                    () -> TaskGraph.of(List.of(a, b), List.of(d, d)));
        }
        @Test @DisplayName("reverse edge with same type forms a cycle and is rejected")
        void reverseEdgeCycles() {
            PlanningTask a = v1(), b = v2();
            TaskDependency d1 = dep(a, b);
            TaskDependency d2 = dep(b, a);
            assertThrows(PlanValidationException.class,
                    () -> TaskGraph.of(List.of(a, b), List.of(d1, d2)));
        }
        @Test @DisplayName("null dependency endpoint is rejected")
        void nullEndpoint() {
            assertThrows(NullPointerException.class,
                    () -> new TaskDependency(null, TASK_2, DependencyType.FINISH_TO_START));
        }
    }

    @Nested
    @DisplayName("cycle detection")
    class CycleDetection {
        @Test @DisplayName("2-node cycle is rejected with the participants listed")
        void twoNodeCycle() {
            PlanningTask a = v1(), b = v2();
            assertThrows(PlanValidationException.class,
                    () -> TaskGraph.of(List.of(a, b), List.of(dep(a, b), dep(b, a))));
        }
        @Test @DisplayName("3-node cycle is rejected")
        void threeNodeCycle() {
            PlanningTask a = v1(), b = v2(), c = v3();
            assertThrows(PlanValidationException.class,
                    () -> TaskGraph.of(List.of(a, b, c),
                            List.of(dep(a, b), dep(b, c), dep(c, a))));
        }
        @Test @DisplayName("acyclic graph reports an empty cycle set")
        void acyclicCycleEmpty() {
            PlanningTask a = v1(), b = v2();
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
            assertTrue(TaskGraph.findCyclicTaskIds(g.tasks(), g.dependencies()).isEmpty());
        }
        @Test @DisplayName("a cycle lists every participant in lexicographic order")
        void cycleListsParticipants() {
            PlanningTask a = v1(), b = v2(), c = v3();
            List<String> cyclic;
            try {
                TaskGraph.of(List.of(a, b, c), List.of(dep(a, b), dep(b, c), dep(c, a)));
                cyclic = List.of();
            } catch (PlanValidationException e) {
                cyclic = TaskGraph.findCyclicTaskIds(
                        List.of(a, b, c), List.of(dep(a, b), dep(b, c), dep(c, a)));
            }
            assertEquals(Set.of(a.taskId(), b.taskId(), c.taskId()), Set.copyOf(cyclic));
            assertEquals(new ArrayList<>(cyclic), new ArrayList<>(cyclic).stream().sorted().toList());
        }
    }

    @Nested
    @DisplayName("query, topological order & ordering")
    class QueriesAndTopo {
        @Test @DisplayName("topologicalOrder includes every task exactly once")
        void topoAllTasks() {
            PlanningTask a = v1(), b = v2(), c = v3();
            TaskGraph g = TaskGraph.of(List.of(a, b, c), List.of(dep(a, b), dep(b, c)));
            List<PlanningTask> topo = TaskGraph.topologicalOrder(g.tasks(), g.dependencies());
            assertEquals(3, topo.size());
            assertEquals(Set.of(a.taskId(), b.taskId(), c.taskId()),
                    topo.stream().map(PlanningTask::taskId).collect(java.util.stream.Collectors.toSet()));
            assertEquals(0, indexOf(topo, a.taskId()));
            assertEquals(1, indexOf(topo, b.taskId()));
            assertEquals(2, indexOf(topo, c.taskId()));
        }
        @Test @DisplayName("topologicalOrder is valid for a diamond")
        void topoDiamond() {
            PlanningTask a = v1();
            PlanningTask b = PlanningTask.create("Java Basics", "Loops", TaskType.LEARNING, 2);
            PlanningTask c = PlanningTask.create("Java Basics", "Streams", TaskType.LEARNING, 2);
            PlanningTask d = PlanningTask.create("Java Basics", "Methods", TaskType.LEARNING, 3);
            List<TaskDependency> deps = List.of(dep(a, b), dep(a, c), dep(b, d), dep(c, d));
            TaskGraph g = TaskGraph.of(List.of(a, b, c, d), deps);
            List<String> ids = TaskGraph.topologicalOrder(g.tasks(), g.dependencies())
                    .stream().map(PlanningTask::taskId).toList();
            assertTrue(ids.indexOf(a.taskId()) < ids.indexOf(b.taskId()));
            assertTrue(ids.indexOf(a.taskId()) < ids.indexOf(c.taskId()));
            assertTrue(ids.indexOf(b.taskId()) < ids.indexOf(d.taskId()));
            assertTrue(ids.indexOf(c.taskId()) < ids.indexOf(d.taskId()));
        }
        @Test @DisplayName("topologicalOrder is deterministic across repeated calls")
        void topoDeterministic() {
            PlanningTask a = v1(), b = v2();
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of(dep(a, b)));
            assertEquals(g.topologicallyOrdered(), g.topologicallyOrdered());
        }
        @Test @DisplayName("topologicalOrder falls back to canonical order on ties")
        void topoCanonicalTiebreak() {
            PlanningTask a = v1(); // order=1
            PlanningTask b = v2(); // order=2
            TaskGraph g = TaskGraph.of(List.of(b, a), List.of()); // reversed input
            List<PlanningTask> topo = TaskGraph.topologicalOrder(g.tasks(), g.dependencies());
            assertEquals(a.taskId(), topo.get(0).taskId());
            assertEquals(b.taskId(), topo.get(1).taskId());
        }
        @Test @DisplayName("outgoing / incoming adjacency is correct")
        void adjacency() {
            PlanningTask a = v1(), b = v2(), c = v3();
            TaskGraph g = TaskGraph.of(List.of(a, b, c), List.of(dep(a, b), dep(b, c)));
            assertEquals(1, g.outgoing(a.taskId()).size());
            assertEquals(0, g.incoming(a.taskId()).size());
            assertEquals(1, g.incoming(c.taskId()).size());
            assertEquals(b.taskId(), g.outgoing(a.taskId()).get(0).toTaskId());
            assertEquals(b.taskId(), g.incoming(c.taskId()).get(0).fromTaskId());
        }
        @Test @DisplayName("taskById returns the task or empty; containsTask reflects membership")
        void lookups() {
            PlanningTask a = v1(), b = v2();
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of());
            assertEquals(a, g.taskById(a.taskId()).orElseThrow());
            assertTrue(g.taskById(TASK_3).isEmpty());
            assertTrue(g.containsTask(a.taskId()));
            assertFalse(g.containsTask(TASK_3));
            assertEquals(Set.of(a.taskId(), b.taskId()), g.taskIds());
            assertEquals(2, g.taskCount());
        }
        @Test @DisplayName("dependencies() returns an unmodifiable snapshot")
        void dependenciesSnapshot() {
            PlanningTask a = v1(), b = v2();
            TaskDependency d = dep(a, b);
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of(d));
            List<TaskDependency> deps = g.dependencies();
            assertThrows(UnsupportedOperationException.class, () -> deps.add(d));
            assertEquals(1, deps.size());
        }
        @Test @DisplayName("dependencyCount reflects the edge set")
        void dependencyCount() {
            PlanningTask a = v1(), b = v2(), c = v3();
            TaskGraph g = TaskGraph.of(List.of(a, b, c), List.of(dep(a, b), dep(b, c)));
            assertEquals(2, g.dependencyCount());
        }
    }

    @Nested
    @DisplayName("deep immutability")
    class DeepImmutability {
        @Test @DisplayName("tasks() returns an unmodifiable list")
        void tasksUnmodifiable() {
            PlanningTask a = v1();
            TaskGraph g = TaskGraph.of(List.of(a), List.of());
            assertThrows(UnsupportedOperationException.class, () -> g.tasks().add(a));
        }
        @Test @DisplayName("dependencies() returns an unmodifiable list")
        void depsUnmodifiable() {
            PlanningTask a = v1(), b = v2();
            TaskDependency d = dep(a, b);
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of(d));
            assertThrows(UnsupportedOperationException.class, () -> g.dependencies().add(d));
        }
        @Test @DisplayName("mutating the caller's input list does not change the graph")
        void callerMutationIsolated() {
            PlanningTask a = v1(), b = v2();
            List<PlanningTask> tasks = new ArrayList<>(List.of(a, b));
            TaskGraph g = TaskGraph.of(tasks, List.of());
            tasks.add(v3());
            assertEquals(2, g.taskCount());
        }
        @Test @DisplayName("tasksInCanonicalOrder is an unmodifiable defensive copy")
        void canonicalOrderIsCopy() {
            PlanningTask a = v2(), b = v1();
            TaskGraph g = TaskGraph.of(List.of(a, b), List.of());
            List<PlanningTask> canonical = g.tasksInCanonicalOrder();
            assertThrows(UnsupportedOperationException.class, () -> canonical.add(v3()));
            assertEquals(2, g.taskCount());
        }
    }

    private static int indexOf(List<PlanningTask> topo, String id) {
        for (int i = 0; i < topo.size(); i++) {
            if (topo.get(i).taskId().equals(id)) return i;
        }
        throw new AssertionError("missing " + id);
    }
}
