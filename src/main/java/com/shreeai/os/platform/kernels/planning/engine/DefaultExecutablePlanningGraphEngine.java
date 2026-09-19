package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.ExecutionEdge;
import com.shreeai.os.platform.kernels.planning.model.ExecutionNode;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.ExecutionState;
import com.shreeai.os.platform.kernels.planning.model.ExecutablePlanningGraph;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.ProgressEntry;
import com.shreeai.os.platform.kernels.planning.model.ProgressSnapshot;
import com.shreeai.os.platform.kernels.planning.model.ReplanningResult;
import com.shreeai.os.platform.kernels.planning.model.ScheduledTask;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskProgress;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>DefaultExecutablePlanningGraphEngine</b>
 *
 * <p>Deterministic implementation of {@link ExecutablePlanningGraphEngine}:
 * converts an {@link ExecutionPlan} into an {@link ExecutablePlanningGraph}
 * through the locked five-stage pipeline.</p>
 *
 * <p><b>The engine does not execute tasks.</b> It prepares execution-ready
 * state transitions only.</p>
 *
 * <p><b>Deterministic identity:</b> each {@code nodeId} is the lower-case
 * SHA-256 hex digest over the canonical key {@code taskId|day|title} - the
 * same inputs always produce the byte-identical graph.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless and thread-safe - no mutable instance state.</li>
 *   <li>Deterministic - no randomness, no clocks, no graph libraries, no
 *       LLM, no I/O.</li>
 *   <li>No inference - exactly one node per scheduled task and exactly one
 *       edge per task-graph dependency.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.5
 * @see ExecutablePlanningGraphEngine
 * @see ExecutablePlanningGraph
 */
public final class DefaultExecutablePlanningGraphEngine implements ExecutablePlanningGraphEngine {

    @Override
    public ExecutablePlanningGraph build(
            TaskGraph taskGraph,
            ExecutionPlan executionPlan,
            ReplanningResult replanningResult,
            ProgressSnapshot progress) {
        if (taskGraph == null) {
            throw new IllegalArgumentException("taskGraph must not be null");
        }
        if (executionPlan == null) {
            throw new IllegalArgumentException("executionPlan must not be null");
        }
        Map<String, PlanningTask> tasksById = new LinkedHashMap<>();
        for (PlanningTask task : taskGraph.tasks()) {
            tasksById.put(task.taskId(), task);
        }
        Map<String, Set<String>> predecessors = predecessorsByTaskId(taskGraph);

        /* ------------------------------------------------------------
         * Stage 1 - Convert scheduled tasks into execution nodes.
         * READY when the task has no predecessors, BLOCKED otherwise.
         * ------------------------------------------------------------ */
        List<ExecutionNode> nodes = new ArrayList<>(executionPlan.scheduledTasks().size());
        for (ScheduledTask scheduled : executionPlan.scheduledTasks()) {
            PlanningTask planning = tasksById.get(scheduled.taskId());
            if (planning == null) {
                throw new IllegalArgumentException(
                        "scheduled task is not part of the task graph: " + scheduled.taskId());
            }
            boolean hasPredecessors =
                    !predecessors.getOrDefault(scheduled.taskId(), Set.of()).isEmpty();
            ExecutionState initial = hasPredecessors ? ExecutionState.BLOCKED : ExecutionState.READY;
            nodes.add(new ExecutionNode(
                    nodeId(scheduled.taskId(), scheduled.day(), planning.title()),
                    scheduled.taskId(),
                    planning.title(),
                    scheduled.day(),
                    initial));
        }

        /* ------------------------------------------------------------
         * Stage 2 - Build execution edges from task-graph dependencies.
         * One edge per dependency; never an inferred edge.
         * ------------------------------------------------------------ */
        Map<String, ExecutionNode> nodeByTask = new HashMap<>();
        for (ExecutionNode node : nodes) {
            nodeByTask.put(node.taskId(), node);
        }
        List<ExecutionEdge> edges = new ArrayList<>(taskGraph.dependencies().size());
        for (var dependency : taskGraph.dependencies()) {
            ExecutionNode from = nodeByTask.get(dependency.fromTaskId());
            ExecutionNode to = nodeByTask.get(dependency.toTaskId());
            if (from == null || to == null) {
                throw new IllegalArgumentException(
                        "dependency endpoint is not scheduled: "
                                + dependency.fromTaskId() + " -> " + dependency.toTaskId());
            }
            edges.add(new ExecutionEdge(from.nodeId(), to.nodeId()));
        }

        return applyProgressAndOrder(taskGraph, nodes, edges, replanningResult, progress);
    }

    /**
     * Stage 3 - applies the locked progress transitions only when a
     * replanning result exists; Stages 4 + 5 - delegates DAG validation to
     * the record constructor and returns the canonically ordered graph.
     *
     * @param taskGraph        the originating task graph (never null)
     * @param nodes            the stage-1/2 nodes (never null)
     * @param edges            the stage-2 edges (never null)
     * @param replanningResult the replanning artifact (may be null)
     * @param progress         the caller-supplied progress (may be null)
     * @return the validated, canonically ordered graph (never null)
     */
    private static ExecutablePlanningGraph applyProgressAndOrder(
            TaskGraph taskGraph,
            List<ExecutionNode> nodes,
            List<ExecutionEdge> edges,
            ReplanningResult replanningResult,
            ProgressSnapshot progress) {
        List<ExecutionNode> effectiveNodes = nodes;
        if (replanningResult != null) {
            ProgressSnapshot effective = progress == null ? ProgressSnapshot.empty() : progress;
            Map<String, TaskProgress> progressByTask = new LinkedHashMap<>();
            for (ProgressEntry entry : effective.progress()) {
                progressByTask.put(entry.taskId(), entry.status());
            }
            Map<String, Set<String>> predecessors = predecessorsByTaskId(taskGraph);
            effectiveNodes = new ArrayList<>(nodes.size());
            for (ExecutionNode node : nodes) {
                TaskProgress reported = progressByTask.get(node.taskId());
                ExecutionState updated = node.state();
                if (reported == TaskProgress.COMPLETED) {
                    updated = ExecutionState.COMPLETED;
                } else if (reported == TaskProgress.IN_PROGRESS) {
                    updated = ExecutionState.READY;
                } else if (reported == TaskProgress.NOT_STARTED
                        && !predecessors.getOrDefault(node.taskId(), Set.of()).isEmpty()) {
                    updated = ExecutionState.BLOCKED;
                }
                effectiveNodes.add(new ExecutionNode(
                        node.nodeId(), node.taskId(), node.title(), node.day(), updated));
            }
        }

        /* ------------------------------------------------------------
         * Stage 5 - canonical ordering. Nodes: day, task order, title.
         * Edges: from, to. Stable forever.
         * ------------------------------------------------------------ */
        Map<String, Integer> orderById = new HashMap<>();
        for (PlanningTask task : taskGraph.tasks()) {
            orderById.put(task.taskId(), task.order());
        }
        List<ExecutionNode> orderedNodes = new ArrayList<>(effectiveNodes);
        orderedNodes.sort(Comparator
                .comparingInt(ExecutionNode::day)
                .thenComparingInt(n -> orderById.getOrDefault(n.taskId(), Integer.MAX_VALUE))
                .thenComparing(ExecutionNode::title)
                .thenComparing(ExecutionNode::nodeId));
        List<ExecutionEdge> orderedEdges = new ArrayList<>(edges);
        orderedEdges.sort(Comparator
                .comparing(ExecutionEdge::fromNodeId)
                .thenComparing(ExecutionEdge::toNodeId));

        /* Stage 4 - the record constructor validates edges and acyclicity. */
        return new ExecutablePlanningGraph(orderedNodes, orderedEdges);
    }

    /**
     * Builds the predecessor map of the task graph: for every task the set
     * of tasks it depends on.
     *
     * @param taskGraph the task graph (must not be null)
     * @return the predecessor map (never null)
     */
    private static Map<String, Set<String>> predecessorsByTaskId(TaskGraph taskGraph) {
        Map<String, Set<String>> predecessors = new LinkedHashMap<>();
        for (PlanningTask task : taskGraph.tasks()) {
            predecessors.put(task.taskId(), new HashSet<>());
        }
        for (var dependency : taskGraph.dependencies()) {
            predecessors
                    .computeIfAbsent(dependency.toTaskId(), ignored -> new HashSet<>())
                    .add(dependency.fromTaskId());
        }
        return predecessors;
    }

    /**
     * Computes the deterministic node identifier: a lower-case SHA-256 hex
     * digest over {@code taskId|day|title}.
     *
     * @param taskId the planning task identifier (must not be null)
     * @param day    the scheduled day
     * @param title  the task title (must not be null)
     * @return the 64-character node identifier (never null)
     */
    public static String nodeId(String taskId, int day, String title) {
        return PlanningTask.sha256Hex(taskId + "|" + day + "|" + title);
    }
}
