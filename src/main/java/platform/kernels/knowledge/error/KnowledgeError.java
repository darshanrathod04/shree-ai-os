package platform.kernels.knowledge.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeError</b>
 *
 * <p>An immutable value object representing a structured error in the Knowledge Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates error information in a structured, immutable format.</li>
 *   <li>Provides standardized error codes for consistent error identification.</li>
 *   <li>Contains metadata for debugging and audit purposes.</li>
 *   <li>Serves as the error model for all Knowledge exceptions.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-104, EIO-ARCH-001</p>
 */
public final class KnowledgeError {

    private final KnowledgeErrorCode code;
    private final String message;
    private final Instant occurredAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new KnowledgeError with validation and defensive copying.
     *
     * <p>All parameters are validated for null. The metadata map is defensively copied
     * to ensure immutability.</p>
     *
     * @param code       the standardized error code (must not be null)
     * @param message    the error message (must not be null)
     * @param occurredAt when the error occurred (must not be null)
     * @param metadata   additional error metadata (must not be null, will be defensively copied)
     * @throws NullPointerException if any required parameter is null
     */
    public KnowledgeError(
            KnowledgeErrorCode code,
            String message,
            Instant occurredAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        this.code = code;
        this.message = message;
        this.occurredAt = occurredAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns the standardized error code.
     *
     * @return the error code
     */
    public KnowledgeErrorCode getCode() {
        return code;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns when the error occurred.
     *
     * @return the error timestamp
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * Returns an unmodifiable map of error metadata.
     *
     * <p>This method ensures that the internal metadata map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeError objects are equal if they have the same
     * code, message, occurredAt timestamp, and metadata.</p>
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KnowledgeError that = (KnowledgeError) o;
        return code == that.code
                && Objects.equals(message, that.message)
                && Objects.equals(occurredAt, that.occurredAt)
                && Objects.equals(metadata, that.metadata);
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
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "KnowledgeError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", occurredAt=" + occurredAt +
                ", metadata=" + metadata +
                '}';
    }
}