package platform.kernels.execution.error;

import java.util.Objects;

/**
 * <b>ActionExecutionException</b>
 *
 * <p>Represents failures associated with action execution.
 * This exception is thrown when an action execution fails.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents action execution failures.</li>
 *   <li>Encapsulates immutable ExecutionError.</li>
 *   <li>Classifies action-specific failures.</li>
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
public class ActionExecutionException extends ExecutionException {

    /**
     * Constructs an {@code ActionExecutionException} with the specified error.
     *
     * @param error the execution error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ActionExecutionException(ExecutionError error) {
        super(Objects.requireNonNull(error, "ActionExecutionException error must not be null"));
    }

    /**
     * Constructs an {@code ActionExecutionException} with the specified error and cause.
     *
     * @param error the execution error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ActionExecutionException(ExecutionError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ActionExecutionException error must not be null"), cause);
    }
}