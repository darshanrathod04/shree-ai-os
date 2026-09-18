package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/**
 * <b>ExecutionNode</b>
 *
 * <p>One scheduled task of an {@link ExecutionPlan}, prepared for execution
 * by the P2.5 Executable Planning Graph engine. Exactly one node exists per
 * scheduled task.</p>
 *
 * <p><b>Deterministic identity:</b> the {@code nodeId} is a lower-case
 * SHA-256 hex digest over a canonical key derived from the task identity,
 * scheduled day, and title. The same input therefore always produces the
 * byte-identical identifier on every JVM, every run, and every machine.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - every component is a {@code String}, primitive,
 *       or enum.</li>
 *   <li>Constructor validation - rejects null, blank, non-SHA-256 node
 *       identifiers, and non-positive days.</li>
 *   <li>Data-only - contains no execution or scheduling logic. The engine
 *       never executes tasks; it only prepares execution-ready state
 *       transitions.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodeId the deterministic lower-case SHA-256 hex identifier
 *               (must be 64 characters, must not be null)
 * @param taskId the identifier of the originating planning task (must not
 *               be null or blank)
 * @param title  the human-readable task title (must not be null or blank)
 * @param day    the relative execution day copied from the schedule
 *               (must be {@code >= 1})
 * @param state  the execution readiness classification (must not be null)
 *
 * @since P2.5
 * @see ExecutionState
 * @see ExecutablePlanningGraph
 */
public record ExecutionNode(
        String nodeId,
        String taskId,
        String title,
        int day,
        ExecutionState state) {

    /**
     * Creates a validated, deeply-immutable execution node.
     *
     * @throws NullPointerException     if {@code nodeId}, {@code taskId},
     *                                  {@code title}, or {@code state} is null
     * @throws IllegalArgumentException if {@code taskId} or {@code title} is
     *                                  blank, if {@code day} is below 1, or if
     *                                  {@code nodeId} is not a 64-character
     *                                  lower-case SHA-256 hex digest
     */
    public ExecutionNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        taskId = Objects.requireNonNull(taskId, "taskId must not be null").trim();
        title = Objects.requireNonNull(title, "title must not be null").trim();
        Objects.requireNonNull(state, "state must not be null");
        if (taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (day < 1) {
            throw new IllegalArgumentException("day must be >= 1");
        }
        if (!isValidNodeId(nodeId)) {
            throw new IllegalArgumentException(
                    "nodeId must be a 64-character lower-case SHA-256 hex digest");
        }
    }

    /**
     * Reports whether the candidate is a valid {@link #nodeId()} value.
     *
     * @param candidate the candidate identifier (may be null)
     * @return {@code true} when the candidate is a 64-character lower-case
     *         SHA-256 hex digest
     */
    public static boolean isValidNodeId(String candidate) {
        return PlanningTask.isValidTaskId(candidate);
    }

    @Override
    public String toString() {
        return "ExecutionNode{day=" + day
                + ", state=" + state
                + ", taskId=" + taskId.substring(0, Math.min(12, taskId.length()))
                + ", nodeId=" + nodeId.substring(0, Math.min(12, nodeId.length()))
                + ", title='" + title + "'}";
    }
}
