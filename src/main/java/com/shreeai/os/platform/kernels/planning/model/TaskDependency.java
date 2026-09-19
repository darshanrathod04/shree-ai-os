package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/**
 * <b>TaskDependency</b>
 *
 * <p>A single directed precedence edge between two {@link PlanningTask}
 * nodes of a {@link TaskGraph}.</p>
 *
 * <p><b>Deliberate omissions:</b> there are no weights, no probabilities,
 * no durations, and no lag values. P2.2 discovers relationships and
 * execution order only - cost, time, and resource models arrive in P2.3.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - every component is a {@code String} or enum.</li>
 *   <li>Constructor validation - rejects null components, non-SHA-256
 *       identifiers, and self-dependencies.</li>
 *   <li>Data-only - contains no scheduling or execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param fromTaskId the predecessor task identifier (must be a 64-character
 *                   lower-case SHA-256 hex digest)
 * @param toTaskId   the successor task identifier (must be a 64-character
 *                   lower-case SHA-256 hex digest)
 * @param type       the precedence relationship (must not be null)
 *
 * @since P2.2
 * @see DependencyType
 * @see TaskGraph
 */
public record TaskDependency(
        String fromTaskId,
        String toTaskId,
        DependencyType type) {

    /**
     * Creates a validated, deeply-immutable dependency edge.
     *
     * @throws NullPointerException     if any component is null
     * @throws IllegalArgumentException if either identifier is not a
     *                                  {@link PlanningTask#isValidTaskId(String)}
     *                                  digest, or if both identifiers are
     *                                  equal (a task may not depend on itself)
     */
    public TaskDependency {
        fromTaskId = requireTaskId(fromTaskId, "fromTaskId");
        toTaskId = requireTaskId(toTaskId, "toTaskId");
        Objects.requireNonNull(type, "type must not be null");
        if (fromTaskId.equals(toTaskId)) {
            throw new IllegalArgumentException(
                    "a task must not depend on itself: " + fromTaskId);
        }
    }

    /**
     * Creates a {@link DependencyType#FINISH_TO_START} edge, the canonical
     * precedence relationship produced by the P2.2 engine.
     *
     * @param fromTaskId the predecessor task identifier
     * @param toTaskId   the successor task identifier
     * @return a new finish-to-start dependency (never null)
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if either identifier is invalid or
     *                                  both are equal
     */
    public static TaskDependency finishToStart(String fromTaskId, String toTaskId) {
        return new TaskDependency(fromTaskId, toTaskId, DependencyType.FINISH_TO_START);
    }

    /**
     * Creates a {@link DependencyType#START_TO_START} edge. Start-to-start
     * precedence is modelled for P2.3 and beyond; the P2.2 deterministic
     * expansion emits finish-to-start edges only.
     *
     * @param fromTaskId the predecessor task identifier
     * @param toTaskId   the successor task identifier
     * @return a new start-to-start dependency (never null)
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if either identifier is invalid or
     *                                  both are equal
     */
    public static TaskDependency startToStart(String fromTaskId, String toTaskId) {
        return new TaskDependency(fromTaskId, toTaskId, DependencyType.START_TO_START);
    }

    /**
     * Returns the deterministic canonical key of this edge, used for
     * duplicate detection and stable diagnostics.
     *
     * @return the canonical edge key {@code from->to:type} (never null)
     */
    public String edgeKey() {
        return fromTaskId + "->" + toTaskId + ':' + type.name();
    }

    /**
     * Reports whether the given task participates in this edge, either as
     * predecessor or as successor.
     *
     * @param taskId the task identifier (may be null)
     * @return {@code true} when the identifier is an endpoint of this edge
     */
    public boolean involves(String taskId) {
        return fromTaskId.equals(taskId) || toTaskId.equals(taskId);
    }

    /**
     * Returns a short, stable representation of an endpoint identifier.
     *
     * @param taskId the identifier (must not be null)
     * @return the first twelve characters of the identifier
     */
    public static String shortId(String taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        return taskId.length() <= 12 ? taskId : taskId.substring(0, 12);
    }

    private static String requireTaskId(String candidate, String field) {
        String id = Objects.requireNonNull(candidate, field + " must not be null").trim();
        if (!PlanningTask.isValidTaskId(id)) {
            throw new IllegalArgumentException(
                    field + " must be a 64-character lower-case SHA-256 hex digest: " + id);
        }
        return id;
    }

    @Override
    public String toString() {
        return "TaskDependency{" + shortId(fromTaskId)
                + " -> " + shortId(toTaskId)
                + ", " + type + '}';
    }
}