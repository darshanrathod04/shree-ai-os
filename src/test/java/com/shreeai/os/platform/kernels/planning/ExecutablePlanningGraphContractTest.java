package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.DefaultExecutablePlanningGraphEngine;
import com.shreeai.os.platform.kernels.planning.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.5: Executable planning graph cross-run identity contract")
class ExecutablePlanningGraphContractTest {

    /*
     * Pinned SHA-256 digests: identical on every JVM, every machine, and
     * every run. If these tests ever fail, determinism is broken.
     */
    private static final String TASK_ID =
            "d35c4d1ee2267ad93dfe63b3a8f555da2ce40f5f9d433a50b6c9f07f2e42772c";
    private static final String NODE_ID =
            "9bd9cd588219de84bc7a8d5d5b38d32a6276572da533fa39534ca0feb0fcc253";

    @Test @DisplayName("the planning task id is pinned across JVM runs")
    void pinnedTaskId() {
        var task = PlanningTask.create("Learn Java", "Variables", TaskType.LEARNING, 1);
        assertEquals(TASK_ID, task.taskId());
    }

    @Test @DisplayName("the engine node id is pinned across JVM runs")
    void pinnedNodeId() {
        var task = PlanningTask.create("Learn Java", "Variables", TaskType.LEARNING, 1);
        assertEquals(NODE_ID,
                DefaultExecutablePlanningGraphEngine.nodeId(task.taskId(), 1, "Variables"));
    }

    @Test @DisplayName("a whole build pins every node id across JVM runs")
    void pinnedBuild() {
        var task = PlanningTask.create("Learn Java", "Variables", TaskType.LEARNING, 1);
        var graph = TaskGraph.of(List.of(task), List.of());
        var plan = new ExecutionPlan(1, 2, List.of(new ScheduledTask(task.taskId(), 1, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 2)))));
        var built = new DefaultExecutablePlanningGraphEngine().build(graph, plan, null, null);
        assertEquals(1, built.nodes().size());
        assertEquals(NODE_ID, built.nodes().get(0).nodeId());
        assertEquals(TASK_ID, built.nodes().get(0).taskId());
        assertEquals("Variables", built.nodes().get(0).title());
        assertEquals(ExecutionState.READY, built.nodes().get(0).state());
    }

    @Test @DisplayName("structure validation is exposed and deterministic")
    void validateStructureDirect() {
        assertDoesNotThrow(() -> ExecutablePlanningGraph.validateStructure(List.of(), List.of()));
        assertThrows(NullPointerException.class,
                () -> ExecutablePlanningGraph.validateStructure(null, null));
    }

    @Test @DisplayName("every execution state can label a node")
    void everyStateConstructs() {
        var taskId = PlanningTask.create("M", "T", TaskType.LEARNING, 1).taskId();
        for (ExecutionState state : ExecutionState.values()) {
            var node = new ExecutionNode(NODE_ID, taskId, "Title", 1, state);
            assertEquals(state, node.state());
        }
    }

    @Test @DisplayName("node lookup by id and taskId agree on identity")
    void lookupsAgree() {
        var taskId = PlanningTask.create("Learn Java", "Variables", TaskType.LEARNING, 1).taskId();
        var graph = ExecutablePlanningGraph.of(
                List.of(new ExecutionNode(NODE_ID, taskId, "Variables", 1, ExecutionState.READY)),
                List.of());
        assertSame(graph.nodeById(NODE_ID), graph.nodeByTaskId(taskId));
    }

    @Test @DisplayName("an engine built from a one-day plan never infers edges")
    void noInferredEdges() {
        var a = PlanningTask.create("M", "A", TaskType.LEARNING, 1);
        var b = PlanningTask.create("M", "B", TaskType.LEARNING, 2);
        var g = TaskGraph.of(List.of(a, b), List.of());
        var plan = new ExecutionPlan(1, 4, List.of(
                new ScheduledTask(a.taskId(), 1, 2, List.of(new ResourceAllocation(ResourceType.STUDY, 2))),
                new ScheduledTask(b.taskId(), 1, 2, List.of(new ResourceAllocation(ResourceType.STUDY, 2)))));
        var built = new DefaultExecutablePlanningGraphEngine().build(g, plan, null, null);
        assertTrue(built.edges().isEmpty());
        assertEquals(2, built.nodes().size());
    }

    @Test @DisplayName("day and title changes both change node identity")
    void identitySensitivity() {
        var taskId = PlanningTask.create("M", "A", TaskType.LEARNING, 1).taskId();
        String base = DefaultExecutablePlanningGraphEngine.nodeId(taskId, 1, "A");
        assertNotEquals(base, DefaultExecutablePlanningGraphEngine.nodeId(taskId, 2, "A"));
        assertNotEquals(base, DefaultExecutablePlanningGraphEngine.nodeId(taskId, 1, "B"));
        assertEquals(base, DefaultExecutablePlanningGraphEngine.nodeId(taskId, 1, "A"));
    }

    @Test @DisplayName("nodeId is a strict function: same key, same digest, always")
    void nodeIdFunctionStable() {
        String first = DefaultExecutablePlanningGraphEngine.nodeId(TASK_ID, 7, "Pipes");
        String second = DefaultExecutablePlanningGraphEngine.nodeId(TASK_ID, 7, "Pipes");
        assertEquals(first, second);
        assertEquals(NODE_ID.length(), first.length());
    }
}
