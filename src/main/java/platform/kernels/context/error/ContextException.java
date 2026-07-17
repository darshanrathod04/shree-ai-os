package platform.kernels.context.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ContextException</b>
 *
 * <p>Base exception class for all Context Kernel exceptions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Serves as the root of the Context exception hierarchy.</li>
 *   <li>Encapsulates a ContextError for structured error reporting.</li>
 *   <li>Extends RuntimeException for unchecked exception handling.</li>
 *   <li>Provides consistent error access across all Context exceptions.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Encapsulates one immutable ContextError.</li>
 *   <li>Never duplicates primitive error fields.</li>
 *   <li>No business logic.</li>
 *   <li>No mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-104</p>
 *
 * @see ContextError
 */
public class ContextException extends RuntimeException {
    private final ContextError error;

    /**
     * Creates a new ContextException with the specified error.
     *
     * <p>The exception message is derived from the ContextError.</p>
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextException(ContextError error) {
        super(error.getMessage());
        Objects.requireNonNull(error, "error must not be null");
        this.error = error;
    }

    /**
     * Creates a new ContextException with the specified error and cause.
     *
     * <p>The exception message is derived from the ContextError.</p>
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextException(ContextError error, Throwable cause) {
        super(error.getMessage(), cause);
        Objects.requireNonNull(error, "error must not be null");
        this.error = error;
    }

    /**
     * Returns the encapsulated ContextError.
     *
     * <p>This provides access to the structured error information
     * including error code, timestamp, and metadata.</p>
     *
     * @return the ContextError
     */
    public ContextError getError() {
        return error;
    }

    /**
     * Returns the error code from the encapsulated ContextError.
     *
     * @return the error code
     */
    public ContextErrorCode getErrorCode() {
        return error.getCode();
    }

    /**
     * Returns when the error occurred from the encapsulated ContextError.
     *
     * @return the error timestamp
     */
    public Instant getOccurredAt() {
        return error.getOccurredAt();
    }

    /**
     * Returns the metadata from the encapsulated ContextError.
     *
     * <p>This method returns an unmodifiable view of the metadata map.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return error.getMetadata();
    }
}