package platform.kernels.cognitive.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CognitiveError</b>
 *
 * <p>Immutable value object representing a cognitive failure.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates error classification and information</li>
 *   <li>Provides immutable error representation</li>
 *   <li>Contains no behavior - data carrier only</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object. All collections are unmodifiable.
 * Defensive copying is applied to all mutable inputs. This class is final with final fields.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * @param code the error classification code (must not be {@code null})
 * @param message the error message (must not be {@code null} or empty)
 * @param occurredAt the timestamp when the error occurred (must not be {@code null})
 * @param metadata additional error metadata (must not be {@code null}, values may be {@code null})
 */
public final class CognitiveError {

    private final CognitiveErrorCode code;
    private final String message;
    private final Instant occurredAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new CognitiveError with the specified parameters.
     *
     * <p>Performs defensive validation and creates immutable copies of all collections.</p>
     *
     * @param code the error classification code (must not be {@code null})
     * @param message the error message (must not be {@code null} or empty)
     * @param occurredAt the timestamp when the error occurred (must not be {@code null})
     * @param metadata additional error metadata (must not be {@code null}, values may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public CognitiveError(
            CognitiveErrorCode code,
            String message,
            Instant occurredAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(code, "CognitiveError code must not be null");
        Objects.requireNonNull(message, "CognitiveError message must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("CognitiveError message must not be empty");
        }
        Objects.requireNonNull(occurredAt, "CognitiveError occurredAt must not be null");
        Objects.requireNonNull(metadata, "CognitiveError metadata must not be null");

        this.code = code;
        this.message = message;
        this.occurredAt = occurredAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns the error classification code.
     *
     * @return the error code
     */
    public CognitiveErrorCode code() {
        return code;
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
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>The equality is based on all fields: code, message, occurredAt, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CognitiveError that = (CognitiveError) obj;
        return code == that.code &&
               Objects.equals(message, that.message) &&
               Objects.equals(occurredAt, that.occurredAt) &&
               Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, message, occurredAt, metadata);
    }

    /**
     * Returns a string representation of the error.
     *
     * <p>Includes the error code, message, and timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}