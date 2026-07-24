package platform.kernels.execution.model;

import java.util.Objects;

/**
 * <b>ExecutionStatus</b>
 *
 * <p>Represents execution lifecycle state.
 * This enumeration defines all possible execution states.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines execution lifecycle states.</li>
 *   <li>Provides clear status semantics.</li>
 *   <li>Enables status-based decision making.</li>
 *   <li>Contains no lifecycle transitions.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum ExecutionStatus {

    /**
     * Execution has been created but not started.
     */
    PENDING,

    /**
     * Execution is currently in progress.
     */
    RUNNING,

    /**
     * Execution completed successfully.
     */
    COMPLETED,

    /**
     * Execution failed with an error.
     */
    FAILED,

    /**
     * Execution was cancelled by request.
     */
    CANCELLED,

    /**
     * Execution was paused.
     */
    PAUSED,

    /**
     * Execution is waiting for a dependency.
     */
    WAITING,

    /**
     * Execution is being retried after failure.
     */
    RETRYING
}