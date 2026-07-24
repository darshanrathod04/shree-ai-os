package platform.kernels.execution.error;

/**
 * <b>ExecutionErrorCode</b>
 *
 * <p>Represents execution-domain-specific error codes.
 * This enumeration provides a canonical classification of execution failures.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies execution failures.</li>
 *   <li>Provides canonical error codes.</li>
 *   <li>Enables error identification and handling.</li>
 *   <li>Contains no business logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum ExecutionErrorCode {

    /**
     * The execution request is structurally invalid.
     */
    INVALID_EXECUTION_REQUEST,

    /**
     * The execution state is invalid for the requested operation.
     */
    INVALID_EXECUTION_STATE,

    /**
     * The action is invalid or malformed.
     */
    INVALID_ACTION,

    /**
     * The workflow definition is invalid or malformed.
     */
    INVALID_WORKFLOW,

    /**
     * The task is invalid or malformed.
     */
    INVALID_TASK,

    /**
     * The recovery configuration is invalid or malformed.
     */
    INVALID_RECOVERY_CONFIGURATION,

    /**
     * The execution context is invalid or malformed.
     */
    INVALID_EXECUTION_CONTEXT,

    /**
     * The execution options are invalid or malformed.
     */
    INVALID_EXECUTION_OPTIONS,

    /**
     * The execution metrics are invalid or malformed.
     */
    INVALID_EXECUTION_METRICS,

    /**
     * Structural validation failed.
     */
    VALIDATION_FAILURE,

    /**
     * General execution failure.
     */
    EXECUTION_FAILURE,

    /**
     * Action execution failed.
     */
    ACTION_EXECUTION_FAILED,

    /**
     * Workflow execution failed.
     */
    WORKFLOW_EXECUTION_FAILED,

    /**
     * Task execution failed.
     */
    TASK_EXECUTION_FAILED,

    /**
     * Recovery operation failed.
     */
    RECOVERY_FAILED,

    /**
     * Execution was cancelled.
     */
    EXECUTION_CANCELLED,

    /**
     * Execution timed out.
     */
    EXECUTION_TIMEOUT,

    /**
     * Execution resource not available.
     */
    RESOURCE_UNAVAILABLE,

    /**
     * Execution dependency not satisfied.
     */
    DEPENDENCY_NOT_SATISFIED,

    /**
     * Unknown execution error.
     */
    UNKNOWN_ERROR
}