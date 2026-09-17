package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultTaskDependencyGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.DependencyType;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanMilestone;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.TaskDependency;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.2: DefaultTaskDependencyGraphEngine")
class TaskDependencyGraphEngineTest {

    private DefaultTaskDependencyGraphEngine engine;

    @BeforeEach
    void setUp() { engine = new DefaultTaskDependencyGraphEngine(); }

    private PlanMilestone m(String name) {
        return new PlanMilestone(name, List.of(), 1, "", Map.of());
    }

    @Nested
    @DisplayName("Template expansion (Stage 1)")
    class Expansion {
        @Test @DisplayName("Java Basics expands to Variables->Loops->Methods (orders 1-3)")
        void expandJavaBasics() {
            List<PlanningTask> tasks = engine.expandMilestone(m("Java Basics"), 1);
            assertEquals(3, tasks.size());
            assertEquals("Variables", tasks.get(0).title());
            assertEquals("Loops", tasks.get(1).title());
            assertEquals("Methods", tasks.get(2).title());
            assertSame(TaskType.LEARNING, tasks.get(0).type());
            assertEquals(1, tasks.get(0).order());
            assertEquals(2, tasks.get(1).order());
            assertEquals(3, tasks.get(2).order());
            // Independently-known digests (see shell sha256sum verification).
            assertEquals("9e8bfb76782a36a15c238ecde1c5b5b0db789064ee40716ccb4887c329a64982", tasks.get(0).taskId());
            assertEquals("3e60b55c9900c81de4a459b1fd6be9316e0043d08e67edb83ec9a683eb8edf1e", tasks.get(1).taskId());
            assertEquals("168757d7d7922cbe17b0cbcfc6b6c37f0dbf74f9d4c1f103dbdfd7daeb0cc1b3", tasks.get(2).taskId());
        }
        @Test @DisplayName("OOP expands to Class->Object->Inheritance (Class id at order 2 is independently known)")
        void expandOop() {
            List<PlanningTask> tasks = engine.expandMilestone(m("OOP"), 2);
            assertEquals(List.of("Class", "Object", "Inheritance"),
                    tasks.stream().map(PlanningTask::title).toList());
            assertEquals(2, tasks.get(0).order());
            assertEquals(3, tasks.get(1).order());
            assertEquals(4, tasks.get(2).order());
            assertEquals("a323c7f296d1b59e7439d49aac6222d08b924fdd5db50770059a46b38504bc3f", tasks.get(0).taskId());
            // Remaining ids verified against the deterministic formula.
            assertEquals(PlanningTask.computeTaskId("OOP", "Object", TaskType.LEARNING, 3), tasks.get(1).taskId());
            assertEquals(PlanningTask.computeTaskId("OOP", "Inheritance", TaskType.LEARNING, 4), tasks.get(2).taskId());
        }
        @Test @DisplayName("first-match-wins: 'JavaScript Basics' matches the javascript rule, not the java rule")
        void firstMatchWins() {
            List<PlanningTask> tasks = engine.expandMilestone(m("JavaScript Basics"), 1);
            assertEquals(List.of("Syntax Fundamentals", "Functions & Scope", "DOM Manipulation"),
                    tasks.stream().map(PlanningTask::title).toList());
            assertSame(TaskType.LEARNING, tasks.get(0).type());
            assertSame(TaskType.IMPLEMENTATION, tasks.get(2).type());
        }
        @Test @DisplayName("rules table contains the full locked set")
        void rulesLockedSet() {
            List<com.shreeai.os.platform.kernels.planning.engine.DefaultTaskDependencyGraphEngine.ExpansionRule> rules =
                    DefaultTaskDependencyGraphEngine.RULES;
            assertEquals(15, rules.size());
            assertEquals(3, rules.get(0).templates().size());
        }
        @Test @DisplayName("templatesFor matches 'java basics' to the Variables chain")
        void templatesForJavaBasics() {
                        List<? extends Record> templates = engine.templatesFor(m("Java Basics"));
            assertEquals(3, templates.size());
        }
        @Test @DisplayName("null milestone is rejected")
        void nullMilestone() {
            assertThrows(NullPointerException.class,
                    () -> engine.expandMilestone(null, 1));
        }
        @Test @DisplayName("firstOrder below 1 is rejected")
        void badFirstOrder() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.expandMilestone(m("Java Basics"), 0));
        }
        @Test @DisplayName("returned task list is defensively immutable")
        void expandImmutable() {
            List<PlanningTask> tasks = engine.expandMilestone(m("Java Basics"), 1);
            assertThrows(UnsupportedOperationException.class, () -> tasks.add(tasks.get(0)));
        }
    }

    @Nested
    @DisplayName("Intra-milestone chain (Stage 2)")
    class Chain {
        @Test @DisplayName("chainDependencies links adjacent tasks with FINISH_TO_START")
        void chainLinks() {
            List<PlanningTask> tasks = engine.expandMilestone(m("Java Basics"), 1);
            List<TaskDependency> deps = engine.chainDependencies(tasks);
            assertEquals(2, deps.size());
            assertEquals(tasks.get(0).taskId(), deps.get(0).fromTaskId());
            assertEquals(tasks.get(1).taskId(), deps.get(0).toTaskId());
            assertEquals(tasks.get(1).taskId(), deps.get(1).fromTaskId());
            assertEquals(tasks.get(2).taskId(), deps.get(1).toTaskId());
        }
        @Test @DisplayName("chainDependencies is all FINISH_TO_START")
        void chainType() {
            List<PlanningTask> tasks = engine.expandMilestone(m("Java Basics"), 1);
            engine.chainDependencies(tasks).forEach(d ->
                    assertEquals(DependencyType.FINISH_TO_START, d.type()));
        }
        @Test @DisplayName("chainDependencies on a single task yields no edges")
        void singleTaskNoEdges() {
            PlanningTask solo = PlanningTask.create("M", "Solo", TaskType.LEARNING, 1);
            assertTrue(engine.chainDependencies(List.of(solo)).isEmpty());
        }
        @Test @DisplayName("chainDependencies rejects null")
        void nullList() {
            assertThrows(NullPointerException.class, () -> engine.chainDependencies(null));
        }
    }

    @Nested
    @DisplayName("Cross-milestone links (Stage 3)")
    class CrossMilestone {
        @Test @DisplayName("links last task of previous milestone to first of next, FINISH_TO_START")
        void crossLink() {
            List<PlanningTask> prev = engine.expandMilestone(m("Java Basics"), 1);
            List<PlanningTask> next = engine.expandMilestone(m("OOP"), prev.size() + 1);
            TaskDependency cross = engine.crossMilestoneDependency(prev, next);
            assertEquals(prev.get(prev.size() - 1).taskId(), cross.fromTaskId());
            assertEquals(next.get(0).taskId(), cross.toTaskId());
            assertEquals(DependencyType.FINISH_TO_START, cross.type());
        }
        @Test @DisplayName("rejects an empty previous milestone")
        void emptyPrevious() {
            List<PlanningTask> next = engine.expandMilestone(m("Java Basics"), 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.crossMilestoneDependency(List.of(), next));
        }
        @Test @DisplayName("rejects an empty next milestone")
        void emptyNext() {
            List<PlanningTask> prev = engine.expandMilestone(m("Java Basics"), 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.crossMilestoneDependency(prev, List.of()));
        }
        @Test @DisplayName("rejects null arguments")
        void nullArgs() {
            assertThrows(NullPointerException.class,
                    () -> engine.crossMilestoneDependency(null, null));
        }
    }

    @Nested
    @DisplayName("buildTaskGraph")
    class Build {
        private PlanBlueprint twoMilestone() {
            return new PlanBlueprint("Learn Java", "", 8, List.of(),
                    List.of(m("Java Basics"), m("OOP")),
                    List.of(), List.of(), List.of(), Map.of());
        }
            private PlanBlueprint singleMilestoneBlueprint() {
            return new PlanBlueprint("Learn Java", "", 4, List.of(),
                    List.of(m("Java Basics")),
                    List.of(), List.of(), List.of(), Map.of());
        }

        @Test @DisplayName("null blueprint yields the shared empty graph")
        void nullBlueprint() {
            assertEquals(TaskGraph.empty(), engine.buildTaskGraph((PlanBlueprint) null));
            assertTrue(engine.buildTaskGraph((PlanBlueprint) null).isEmpty());
        }
        @Test @DisplayName("empty milestone list yields an empty graph")
        void emptyMilestones() {
            PlanBlueprint bp = new PlanBlueprint("P", "", 4, List.of(), List.of(),
                    List.of(), List.of(), List.of(), Map.of());
            assertTrue(engine.buildTaskGraph(bp).isEmpty());
        }
        @Test @DisplayName("null milestone list yields an empty graph")
        void nullMilestoneList() {
            assertTrue(engine.buildTaskGraph((List<PlanMilestone>) null).isEmpty());
        }
        @Test @DisplayName("single milestone: 3 tasks, 2 intra-milestone edges")
        void singleMilestone() {
                        TaskGraph g = engine.buildTaskGraph(singleMilestoneBlueprint());
            assertEquals(3, g.taskCount());
            assertEquals(2, g.dependencyCount());
            assertEquals(Set.of("9e8bfb76782a36a15c238ecde1c5b5b0db789064ee40716ccb4887c329a64982",
                    "3e60b55c9900c81de4a459b1fd6be9316e0043d08e67edb83ec9a683eb8edf1e",
                    "168757d7d7922cbe17b0cbcfc6b6c37f0dbf74f9d4c1f103dbdfd7daeb0cc1b3"), g.taskIds());
        }
        @Test @DisplayName("two milestones: 6 tasks, 5 edges, cross milestone link present")
        void twoMilestones() {
            TaskGraph g = engine.buildTaskGraph(twoMilestone());
            assertEquals(6, g.taskCount());
            assertEquals(5, g.dependencyCount());
            // Canonical order is 1..6 by composite order.
            List<String> canonical = g.topologicallyOrdered().tasks().stream()
                    .map(PlanningTask::taskId).toList();
            assertEquals(6, canonical.size());
            // Cross edge: Java Methods (order 3) -> OOP Class (order 4).
            String javaMethods = "168757d7d7922cbe17b0cbcfc6b6c37f0dbf74f9d4c1f103dbdfd7daeb0cc1b3";
            String oopClass = PlanningTask.computeTaskId("OOP", "Class", TaskType.LEARNING, 4);
            Set<String> edgeKeys = g.dependencies().stream()
                    .map(d -> d.fromTaskId() + "->" + d.toTaskId()).collect(
                            java.util.stream.Collectors.toSet());
            assertTrue(edgeKeys.contains(javaMethods + "->" + oopClass));
            // No dangling edges.
            Set<String> ids = g.taskIds();
            g.dependencies().forEach(d -> {
                assertTrue(ids.contains(d.fromTaskId()));
                assertTrue(ids.contains(d.toTaskId()));
            });
        }
        @Test @DisplayName("same blueprint always yields an identical, equal graph (determinism)")
        void determinism() {
            TaskGraph a = engine.buildTaskGraph(twoMilestone());
            TaskGraph b = engine.buildTaskGraph(twoMilestone());
            assertEquals(a, b);
            assertEquals(a.taskIds(), b.taskIds());
            assertEquals(a.dependencies(), b.dependencies());
        }
        @Test @DisplayName("output graph is always acyclic")
        void acyclic() {
            TaskGraph g = engine.buildTaskGraph(twoMilestone());
            assertTrue(TaskGraph.findCyclicTaskIds(g.tasks(), g.dependencies()).isEmpty());
            assertDoesNotThrow(() -> engine.buildTaskGraph(twoMilestone()));
        }
        @Test @DisplayName("tasks are stored in canonical (stable) order in the returned graph")
        void canonicalOrderStable() {
            TaskGraph g = engine.buildTaskGraph(twoMilestone());
            List<Integer> orders = g.tasks().stream().map(PlanningTask::order).toList();
            List<Integer> sorted = new java.util.ArrayList<>(orders);
            java.util.Collections.sort(sorted);
            assertEquals(sorted, orders);
        }
        @Test @DisplayName("returned graph is deeply immutable")
        void returnedGraphImmutable() {
            TaskGraph g = engine.buildTaskGraph(twoMilestone());
            assertThrows(UnsupportedOperationException.class, () -> g.tasks().add(g.tasks().get(0)));
            assertThrows(UnsupportedOperationException.class, () -> g.dependencies().add(g.dependencies().get(0)));
        }
        @Test @DisplayName("no null tasks or dependencies are emitted")
        void noNulls() {
            TaskGraph g = engine.buildTaskGraph(twoMilestone());
            g.tasks().forEach(t -> assertNotNull(t.taskId()));
            g.dependencies().forEach(d -> {
                assertNotNull(d.fromTaskId());
                assertNotNull(d.toTaskId());
            });
        }
        @Test @DisplayName("unknown keyword milestone falls back to the default rule and still expands")
        void unknownMilestoneFallback() {
            PlanBlueprint bp = new PlanBlueprint("X", "", 4, List.of(),
                    List.of(m("Quantum Physics")), List.of(), List.of(), List.of(), Map.of());
            TaskGraph g = engine.buildTaskGraph(bp);
            assertFalse(g.isEmpty());
            assertTrue(g.taskCount() >= 1);
            g.tasks().forEach(t -> assertTrue(PlanningTask.isValidTaskId(t.taskId())));
        }
    }

    @Nested
    @DisplayName("Determinism & stability")
    class Determinism {
        @Test @DisplayName("expansion ids are stable across engine instances")
        void stableAcrossInstances() {
            List<PlanningTask> a = engine.expandMilestone(m("Java Basics"), 1);
            List<PlanningTask> b = new DefaultTaskDependencyGraphEngine().expandMilestone(m("Java Basics"), 1);
            assertEquals(a.stream().map(PlanningTask::taskId).toList(),
                    b.stream().map(PlanningTask::taskId).toList());
        }
        @Test @DisplayName("buildTaskGraph does not mutate the input blueprint's milestone list")
        void inputNotMutated() {
            List<PlanMilestone> milestones = new java.util.ArrayList<>(
                    List.of(m("Java Basics"), m("OOP")));
            PlanBlueprint bp = new PlanBlueprint("L", "", 8, List.of(), milestones,
                    List.of(), List.of(), List.of(), Map.of());
            int before = milestones.size();
            engine.buildTaskGraph(bp);
            assertEquals(before, milestones.size());
        }
    }
}
