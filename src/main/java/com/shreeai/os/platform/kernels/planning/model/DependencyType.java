package com.shreeai.os.platform.kernels.planning.model;

/**
 * <b>DependencyType</b>
 *
 * <p>Defines the canonical relationship between two {@link PlanningTask}
 * nodes in a {@link TaskGraph}. Only the two precedence relationships
 * required by P2.2 are modelled.</p>
 *
 * <p>No weights, no probabilities, and no lag values are modelled: P2.2
 * discovers relationships and execution order only. Duration, resources,
 * and lag are deliberately deferred to P2.3.</p>
 *
 * <p><b>Semantics:</b></p>
 * <ul>
 *   <li>{@link #FINISH_TO_START} - the successor starts only after the
 *       predecessor finishes.</li>
 *   <li>{@link #START_TO_START} - the successor may start once the
 *       predecessor has started.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable - enum constants are inherently immutable.</li>
 *   <li>Deterministic - the declaration order is the canonical ordinal
 *       order used as a stable sort tie-break.</li>
 *   <li>Data-only - carries no scheduling or allocation logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.2
 * @see TaskDependency
 * @see TaskGraph
 */
public enum DependencyType {

    /** The successor may start only after the predecessor has finished. */
    FINISH_TO_START,

    /** The successor may start once the predecessor has started. */
    START_TO_START
}
