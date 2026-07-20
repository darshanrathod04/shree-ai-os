package platform.kernels.execution.error;

import java.util.Objects;

/**
 * <b>WorkflowExecutionException</b>
 *
 * <p>Represents failures associated with workflow execution.
 * This exception is thrown when a workflow execution fails.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents workflow execution failures.</li>
 *   <li>Encapsulates immutable ExecutionError.</li>
 *   <li>Classifies workflow-specific failures.</li>
 *   <li>Contains no recovery logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable after construction.</li>
 *   <li>Constructor validation.</li>
 *   <li>Encapsulates immutable ExecutionError.</li>
 *   <li>No mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-104, EIO-ARCH-001</p>
 *
 * @param error the execution error (must not be {@code null})
 *
 * @since 1.0
 */
public class WorkflowExecutionException extends ExecutionException {

    /**
     * Constructs a {@code WorkflowExecutionException} with the specified error.
     *
     * @param error the execution error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public WorkflowExecutionException(ExecutionError error) {
        super(Objects.requireNonNull(error, "WorkflowExecutionException error must not be null"));
    }

    /**
     * Constructs a {@code WorkflowExecutionException} with the specified error and cause.
     *
     * @param error the execution error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public WorkflowExecutionException(ExecutionError error, Throwable cause) {
        super(Objects.requireNonNull(error, "WorkflowExecutionException error must not be null"), cause);
    }
}