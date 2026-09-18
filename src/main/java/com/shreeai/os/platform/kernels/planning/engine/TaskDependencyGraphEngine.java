package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanMilestone;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;

/**
 * <b>TaskDependencyGraphEngine</b>
 *
 * <p>Converts a {@link PlanBlueprint milestone plan} into an executable
 * {@link TaskGraph task dependency DAG}.</p>
 *
 * <p>The engine performs five locked pipeline stages:</p>
 * <ol>
 *   <li>Expand milestones into ordered tasks via deterministic templates.</li>
 *   <li>Build intra-milestone FINISH_TO_START chains.</li>
 *   <li>Link the last task of each milestone to the first task of the next.</li>
 *   <li>Topologically validate the graph (no cycles; valid endpoints).</li>
 *   <li>Apply canonical ordering for stable output.</li>
 * </ol>
 *
 * <p><b>Never does any of the following:</b> schedules dates, allocates
 * resources, calls an LLM, uses a graph library, or recurses on graph
 * traversal. Implementations must be stateless, thread-safe, and
 * deterministic.</p>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.2
 * @see TaskGraph
 * @see PlanBlueprint
 */
@FunctionalInterface
public interface TaskDependencyGraphEngine {

    /**
     * Converts a domain-aware milestone plan into a task dependency DAG.
     *
     * <p>A {@code null} blueprint yields an empty graph; an engine failure
     * on an otherwise valid blueprint never occurs under deterministic
     * templates.</p>
     *
     * @param blueprint the milestone plan (may be null)
     * @return a validated, canonical task graph (never null)
     * @throws com.shreeai.os.platform.kernels.planning.error.PlanValidationException
     *         if the produced graph contains a cycle (never happens for
     *         deterministic expansion of a non-empty blueprint)
     */
    TaskGraph buildTaskGraph(PlanBlueprint blueprint);
}
