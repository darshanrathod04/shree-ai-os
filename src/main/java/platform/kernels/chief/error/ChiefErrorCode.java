package platform.kernels.chief.error;

/**
 * <b>ChiefErrorCode</b>
 *
 * <p>Defines canonical error identifiers for the Chief Kernel.
 * This enum provides stable, unique error codes for orchestration failures.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides canonical error identifiers.</li>
 *   <li>Classifies orchestration failures.</li>
 *   <li>Ensures consistent error representation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — enum values are immutable.</li>
 *   <li>Unique — each error code is unique.</li>
 *   <li>Stable — error codes do not change.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum ChiefErrorCode {

    /**
     * Decision-related errors.
     */
    DECISION_ERROR("DECISION_ERROR", "Decision orchestration error"),

    /**
     * Goal management errors.
     */
    GOAL_MANAGEMENT_ERROR("GOAL_MANAGEMENT_ERROR", "Goal management error"),

    /**
     * Task delegation errors.
     */
    TASK_DELEGATION_ERROR("TASK_DELEGATION_ERROR", "Task delegation error"),

    /**
     * Kernel coordination errors.
     */
    KERNEL_COORDINATION_ERROR("KERNEL_COORDINATION_ERROR", "Kernel coordination error"),

    /**
     * Validation errors.
     */
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation error"),

    /**
     * Monitoring errors.
     */
    MONITORING_ERROR("MONITORING_ERROR", "Monitoring error"),

    /**
     * General orchestration errors.
     */
    ORCHESTRATION_ERROR("ORCHESTRATION_ERROR", "General orchestration error");

    private final String code;
    private final String description;

    ChiefErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public String code() {
        return code;
    }

    /**
     * Returns the error description.
     *
     * @return the error description
     */
    public String description() {
        return description;
    }
}