package com.shreeai.os.platform.kernels.cognitive.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CognitiveException</b>
 *
 * <p>Root exception for the Cognitive Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates a CognitiveError for consistent failure reporting</li>
 *   <li>Preserves the original cause where applicable</li>
 *   <li>Provides standard exception constructors</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable exception wrapper. The CognitiveError
 * reference is immutable and never modified after construction.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *     │
 *     ▼
 * CognitiveException
 *     │
 *     ├── ReasoningException
 *     ├── DecisionException
 *     ├── ReflectionException
 *     └── CognitiveStateException
 * </pre>
 *
 * @since 1.0
 */
public class CognitiveException extends RuntimeException {

    private final CognitiveError error;

    /**
     * Creates a new CognitiveException with the specified error.
     *
     * <p>The exception encapsulates the CognitiveError and preserves immutable
     * error information for consistent failure reporting.</p>
     *
     * @param error the cognitive error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public CognitiveException(CognitiveError error) {
        super(error.message());
        Objects.requireNonNull(error, "CognitiveException error must not be null");
        this.error = error;
    }

    /**
     * Creates a new CognitiveException with the specified error and cause.
     *
     * <p>The exception encapsulates the CognitiveError and preserves the original
     * cause for debugging purposes.</p>
     *
     * @param error the cognitive error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public CognitiveException(CognitiveError error, Throwable cause) {
        super(error.message(), cause);
        Objects.requireNonNull(error, "CognitiveException error must not be null");
        this.error = error;
    }

    /**
     * Returns the encapsulated CognitiveError.
     *
     * <p>The returned error is immutable and safe to share.</p>
     *
     * @return the cognitive error
     */
    public CognitiveError error() {
        return error;
    }

    /**
     * Returns the error code from the encapsulated CognitiveError.
     *
     * @return the error code
     */
    public CognitiveErrorCode errorCode() {
        return error.code();
    }

    /**
     * Returns the error message from the encapsulated CognitiveError.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return error.message();
    }

    /**
     * Returns the occurrence timestamp from the encapsulated CognitiveError.
     *
     * @return the occurrence timestamp
     */
    public Instant occurredAt() {
        return error.occurredAt();
    }

    /**
     * Returns an unmodifiable view of the error metadata from the encapsulated CognitiveError.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return error.metadata();
    }

    /**
     * Returns a string representation of this exception.
     *
     * <p>Includes the error code, message, and occurrence timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveException{" +
                "error=" + error +
                '}';
    }
}