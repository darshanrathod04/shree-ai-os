package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.ExecutablePlanningGraph;
import com.shreeai.os.platform.kernels.planning.model.ProgressSnapshot;
import com.shreeai.os.platform.kernels.planning.model.ReplanningResult;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;

/**
 * <b>ExecutablePlanningGraphEngine</b>
 *
 * <p>Planning Kernel contract for converting an {@link ExecutionPlan} into
 * an {@link ExecutablePlanningGraph} - the canonical workflow artifact that
 * Chief Intelligence will later orchestrate.</p>
 *
 * <p><b>The engine does not execute tasks.</b> It prepares execution-ready
 * state transitions only.</p>
 *
 * <p><b>Locked execution pipeline:</b></p>
 * <ol>
 *   <li>Convert scheduled tasks - one {@code ExecutionNode} per
 *       {@code ScheduledTask}; READY when the task has no predecessors,
 *       BLOCKED otherwise.</li>
 *   <li>Build execution edges - one edge per task-graph dependency; no edge
 *       is ever inferred.</li>
 *   <li>Apply progress - only when a {@link ReplanningResult} exists:
 *       COMPLETED stays COMPLETED, IN_PROGRESS maps to READY, NOT_STARTED
 *       with predecessors maps to BLOCKED. No other transitions.</li>
 *   <li>Validate DAG - every edge references an existing node and the graph
 *       is acyclic; disconnected graphs are allowed.</li>
 *   <li>Canonical ordering - nodes by (day, task order, title); edges by
 *       (from, to). Stable forever.</li>
 * </ol>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless and thread-safe - implementations hold no mutable state.</li>
 *   <li>Deterministic - the same inputs always produce a byte-identical
 *       graph. No randomness, no clocks, no graph libraries, no LLM.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.5
 * @see DefaultExecutablePlanningGraphEngine
 * @see ExecutablePlanningGraph
 */
public interface ExecutablePlanningGraphEngine {

    /**
     * Converts the given execution plan into an executable planning graph
     * using the task graph for titles, composite order, and dependencies.
     *
     * @param taskGraph        the task graph carrying titles, order, and
     *                         dependencies (must not be null)
     * @param executionPlan    the canonical schedule to project (must not be
     *                         null)
     * @param replanningResult the replanning artifact whose presence enables
     *                         Stage 3 progress application (may be null,
     *                         meaning no progress is applied)
     * @param progress         the caller-supplied progress snapshot consumed
     *                         by Stage 3 when a replanning result exists
     *                         (may be null, meaning no entries)
     * @return the execution-ready graph (never null)
     */
    ExecutablePlanningGraph build(
            TaskGraph taskGraph,
            ExecutionPlan executionPlan,
            ReplanningResult replanningResult,
            ProgressSnapshot progress);
}
