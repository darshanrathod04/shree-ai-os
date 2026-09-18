package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultTaskDependencyGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanMilestone;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.2: TaskGraph integration into CognitiveState")
class TaskGraphCognitiveStateTest {

    private PlanMilestone m(String name) {
        return new PlanMilestone(name, List.of(), 1, "", Map.of());
    }

    private PlanBlueprint twoMilestoneBlueprint() {
        return new PlanBlueprint("Learn Java", "", 8, List.of(),
                List.of(m("Java Basics"), m("OOP")),
                List.of(), List.of(), List.of(), Map.of());
    }

    @Nested
    @DisplayName("Engine -> CognitiveState round trip")
    class RoundTrip {
        @Test @DisplayName("engine-built graph is stored and retrieved from the immutable cognitive state")
        void storeAndRetrieve() {
            TaskGraph graph = new DefaultTaskDependencyGraphEngine()
                    .buildTaskGraph(twoMilestoneBlueprint());
            assertFalse(graph.isEmpty());
            assertEquals(6, graph.taskCount());

            CognitiveState state = CognitiveState.empty().withTaskGraph(graph);
            assertSame(graph, state.taskGraph());
        }
        @Test @DisplayName("null graph is rejected by withTaskGraph")
        void nullGraphRejected() {
            assertThrows(NullPointerException.class,
                    () -> CognitiveState.empty().withTaskGraph(null));
        }
        @Test @DisplayName("withTaskGraph is immutable: the original state is left untouched")
        void immutableOriginal() {
            TaskGraph graph = new DefaultTaskDependencyGraphEngine()
                    .buildTaskGraph(twoMilestoneBlueprint());
            CognitiveState before = CognitiveState.empty();
            CognitiveState after = before.withTaskGraph(graph);
            assertNotSame(before, after);
            assertNull(before.taskGraph());
            assertSame(graph, after.taskGraph());
        }
        @Test @DisplayName("replacing the graph swaps the reference cleanly")
        void replaceGraph() {
            TaskGraph first = TaskGraph.empty();
            TaskGraph second = new DefaultTaskDependencyGraphEngine()
                    .buildTaskGraph(twoMilestoneBlueprint());
            CognitiveState state = CognitiveState.empty().withTaskGraph(first).withTaskGraph(second);
            assertSame(second, state.taskGraph());
            assertNotSame(first, state.taskGraph());
        }
    }

    @Nested
    @DisplayName("Determinism of the stored artifact")
    class Determinism {
        @Test @DisplayName("the same blueprint yields a graph equal to every other build")
        void repeatedBuildsEqual() {
            DefaultTaskDependencyGraphEngine engine = new DefaultTaskDependencyGraphEngine();
            TaskGraph a = engine.buildTaskGraph(twoMilestoneBlueprint());
            TaskGraph b = engine.buildTaskGraph(twoMilestoneBlueprint());
            assertEquals(a, b);
            assertEquals(a.dependencies(), b.dependencies());
        }
        @Test @DisplayName("the stored graph is acyclic by construction")
        void storedGraphAcyclic() {
            TaskGraph graph = new DefaultTaskDependencyGraphEngine()
                    .buildTaskGraph(twoMilestoneBlueprint());
            assertTrue(TaskGraph.findCyclicTaskIds(graph.tasks(), graph.dependencies()).isEmpty());
        }
    }
}
