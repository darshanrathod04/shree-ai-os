package com.shreeai.os.platform.kernels.planning.error;

/**
 * <b>PlanningErrorCode</b>
 *
 * <p>Platform-standard error classification for Planning Kernel failures.
 * This strongly typed enumeration categorizes planning errors without
 * introducing any behavior or recovery logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies planning failures into stable categories.</li>
 *   <li>Provides framework-independent error identifiers.</li>
 *   <li>Maintains no behavior — pure classification only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an enum with no behavior. Each constant represents
 * a distinct category of planning failure.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum PlanningErrorCode {

    /**
     * General planning operation failure.
     */
    PLANNING_ERROR,

    /**
     * Goal planning operation failure.
     */
    GOAL_PLANNING_ERROR,

    /**
     * Task planning operation failure.
     */
    TASK_PLANNING_ERROR,

    /**
     * Scheduling operation failure.
     */
    SCHEDULING_ERROR,

    /**
     * Prioritization operation failure.
     */
    PRIORITIZATION_ERROR,

    /**
     * Plan validation operation failure.
     */
    VALIDATION_ERROR,

    /**
     * Invalid planning identifier provided.
     */
    INVALID_IDENTIFIER,

    /**
     * Invalid planning state encountered.
     */
    INVALID_STATE,

    /**
     * Invalid or malformed constraints.
     */
    INVALID_CONSTRAINTS,

    /**
     * Required data is missing.
     */
    MISSING_REQUIRED_DATA,

    /**
     * Immutable object violation or modification attempt.
     */
    IMMUTABLE_OBJECT_VIOLATION,

    /**
     * Internal processing failure.
     */
    INTERNAL_ERROR
}