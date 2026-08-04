package com.shreeai.os.platform.kernels.execution.error;

import java.util.Objects;

/**
 * <b>ExecutionValidationException</b>
 *
 * <p>Represents structural validation failures originating from the Validation Layer.
 * This exception is thrown when execution request validation fails.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents validation failures.</li>
 *   <li>Encapsulates immutable ExecutionError.</li>
 *   <li>Classifies validation-specific failures.</li>
 *   <li>Contains no validation logic.</li>
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
public class ExecutionValidationException extends ExecutionException {

    /**
     * Constructs an {@code ExecutionValidationException} with the specified error.
     *
     * @param error the execution error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ExecutionValidationException(ExecutionError error) {
        super(Objects.requireNonNull(error, "ExecutionValidationException error must not be null"));
    }

    /**
     * Constructs an {@code ExecutionValidationException} with the specified error and cause.
     *
     * @param error the execution error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ExecutionValidationException(ExecutionError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ExecutionValidationException error must not be null"), cause);
    }
}