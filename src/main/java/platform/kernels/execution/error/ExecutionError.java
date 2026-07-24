package platform.kernels.execution.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionError</b>
 *
 * <p>Represents an immutable execution failure.
 * This value object encapsulates error information for execution failures.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution failure information.</li>
 *   <li>Provides immutable error descriptor.</li>
 *   <li>Classifies execution failures.</li>
 *   <li>Contains no recovery logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-104, EIO-ARCH-001</p>
 *
 * @param errorCode  the execution error code (must not be {@code null})
 * @param message    the error message (must not be {@code null})
 * @param occurredAt the timestamp when the error occurred (must not be {@code null})
 * @param metadata   additional error metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionError {

    private final ExecutionErrorCode errorCode;
    private final String message;
    private final Instant occurredAt;
    private final Map<String, Object> metadata;

    /**
     * Constructs an {@code ExecutionError} with the specified parameters.
     *
     * @param errorCode  the execution error code (must not be {@code null})
     * @param message    the error message (must not be {@code null})
     * @param occurredAt the timestamp when the error occurred (must not be {@code null})
     * @param metadata   additional error metadata (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ExecutionError(
            ExecutionErrorCode errorCode,
            String message,
            Instant occurredAt,
            Map<String, Object> metadata) {
        if (errorCode == null) {
            throw new IllegalArgumentException("ExecutionError errorCode must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("ExecutionError message must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("ExecutionError occurredAt must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ExecutionError metadata must not be null");
        }

        this.errorCode = errorCode;
        this.message = message;
        this.occurredAt = occurredAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns the execution error code.
     *
     * @return the error code
     */
    public ExecutionErrorCode errorCode() {
        return errorCode;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the timestamp when the error occurred.
     *
     * @return the occurrence timestamp
     */
    public Instant occurredAt() {
        return occurredAt;
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
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionError} instances are equal if they have the same
     * error code, message, timestamp, and metadata.</p>
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
        ExecutionError that = (ExecutionError) obj;
        return errorCode == that.errorCode &&
                Objects.equals(message, that.message) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for this {@code ExecutionError}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message, occurredAt, metadata);
    }

    /**
     * Returns a string representation of this {@code ExecutionError}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionError{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", occurredAt=" + occurredAt +
                ", metadata=" + metadata +
                '}';
    }
}