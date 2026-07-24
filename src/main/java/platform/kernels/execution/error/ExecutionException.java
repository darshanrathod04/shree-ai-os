package platform.kernels.execution.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionException</b>
 *
 * <p>Base runtime exception for the Execution Kernel.
 * This exception encapsulates an immutable {@link ExecutionError} and serves as
 * the root of the Execution Kernel exception hierarchy.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution failures.</li>
 *   <li>Provides base exception for Execution Kernel.</li>
 *   <li>Maintains immutable error representation.</li>
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
public class ExecutionException extends RuntimeException {

    private final ExecutionError error;

    /**
     * Constructs an {@code ExecutionException} with the specified error.
     *
     * <p>The exception message is derived from the error's message.</p>
     *
     * @param error the execution error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ExecutionException(ExecutionError error) {
        super(Objects.requireNonNull(error, "ExecutionException error must not be null").message());
        this.error = error;
    }

    /**
     * Constructs an {@code ExecutionException} with the specified error and cause.
     *
     * <p>The exception message is derived from the error's message.</p>
     *
     * @param error the execution error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ExecutionException(ExecutionError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ExecutionException error must not be null").message(), cause);
        this.error = error;
    }

    /**
     * Returns the underlying execution error.
     *
     * <p>The returned error is immutable and represents the failure that caused
     * this exception.</p>
     *
     * @return the execution error
     */
    public ExecutionError error() {
        return error;
    }

    /**
     * Returns the execution error code.
     *
     * @return the error code
     */
    public ExecutionErrorCode errorCode() {
        return error.errorCode();
    }

    /**
     * Returns the timestamp when the error occurred.
     *
     * @return the occurrence timestamp
     */
    public Instant occurredAt() {
        return error.occurredAt();
    }

    /**
     * Returns an unmodifiable view of the error metadata.
     *
     * <p>The returned map is unmodifiable and reflects the metadata at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return error.metadata();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionException} instances are equal if they have the same
     * error and cause.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionException that = (ExecutionException) obj;
        return Objects.equals(error, that.error) &&
                Objects.equals(getCause(), that.getCause());
    }

    /**
     * Returns a hash code value for this {@code ExecutionException}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(error, getCause());
    }

    /**
     * Returns a string representation of this {@code ExecutionException}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionException{" +
                "error=" + error +
                ", cause=" + getCause() +
                '}';
    }
}