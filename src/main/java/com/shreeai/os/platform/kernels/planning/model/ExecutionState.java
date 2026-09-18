package com.shreeai.os.platform.kernels.planning.model;

/**
 * <b>ExecutionState</b>
 *
 * <p>Execution readiness of a single {@link ExecutionNode} inside an
 * {@link ExecutablePlanningGraph}. This is a pure classification enum - it
 * carries no behavior and never transitions itself.</p>
 *
 * <p><b>Locked lifecycle (P2.5):</b></p>
 * <ul>
 *   <li>{@link #PENDING} - reserved for downstream orchestration; the P2.5
 *       engine never assigns it.</li>
 *   <li>{@link #READY} - no predecessors, or progress reported
 *       {@code IN_PROGRESS} after replanning.</li>
 *   <li>{@link #BLOCKED} - has unfinished predecessors in the task graph.</li>
 *   <li>{@link #COMPLETED} - progress reported {@code COMPLETED} after
 *       replanning.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Enum with no behavior - classification only.</li>
 *   <li>No clocks, no inference, no execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.5
 * @see ExecutionNode
 * @see ExecutablePlanningGraph
 */
public enum ExecutionState {
    /** Not yet prepared for execution; reserved for downstream orchestration. */
    PENDING,

    /** No predecessors, or replanning progress reported the task as active. */
    READY,

    /** At least one predecessor in the task graph is not COMPLETED. */
    BLOCKED,

    /** Replanning progress reported the task as fully completed. */
    COMPLETED
}
