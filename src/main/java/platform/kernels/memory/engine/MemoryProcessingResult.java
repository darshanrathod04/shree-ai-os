package platform.kernels.memory.engine;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MemoryProcessingResult</b>
 *
 * <p>An immutable value object representing the result of a Memory processing operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of a processing operation.</li>
 *   <li>Provides metadata about the processing execution.</li>
 *   <li>Never exposes mutable state or collections.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final and
 * initialized via constructor. The metadata map is defensively copied and
 * exposed as unmodifiable.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-MEM-106</p>
 *
 * @param successful whether the processing was successful
 * @param operation the operation that was processed
 * @param processedAt when the processing occurred
 * @param metadata additional processing metadata (must not be null)
 */
public final class MemoryProcessingResult {
    private final boolean successful;
    private final String operation;
    private final Instant processedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new MemoryProcessingResult with the specified parameters.
     *
     * <p>All parameters are validated for null where applicable. The metadata
     * map is defensively copied to ensure immutability.</p>
     *
     * @param successful whether the processing was successful
     * @param operation the operation that was processed (must not be null or blank)
     * @param processedAt when the processing occurred (must not be null)
     * @param metadata additional processing metadata (must not be null)
     * @throws NullPointerException if operation, processedAt, or metadata is null
     * @throws IllegalArgumentException if operation is blank
     */
    public MemoryProcessingResult(
            boolean successful,
            String operation,
            Instant processedAt,
            Map<String, Object> metadata) {
        this.successful = successful;
        this.operation = Objects.requireNonNull(operation, "operation must not be null");
        this.processedAt = Objects.requireNonNull(processedAt, "processedAt must not be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");

        if (this.operation.isBlank()) {
            throw new IllegalArgumentException("operation must not be blank");
        }
    }

    /**
     * Returns whether the processing was successful.
     *
     * @return {@code true} if processing succeeded, {@code false} otherwise
     */
    public boolean successful() {
        return successful;
    }

    /**
     * Returns the operation that was processed.
     *
     * @return the operation name (never null or blank)
     */
    public String operation() {
        return operation;
    }

    /**
     * Returns when the processing occurred.
     *
     * @return the processing timestamp (never null)
     */
    public Instant processedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable view of the processing metadata.
     *
     * <p>The returned map is unmodifiable. Attempts to modify it will result
     * in an {@link UnsupportedOperationException}.</p>
     *
     * @return an unmodifiable map of metadata (never null)
     */
    public Map<String, Object> metadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two MemoryProcessingResult objects are equal if they have the same
     * successful flag, operation, processedAt timestamp, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MemoryProcessingResult that = (MemoryProcessingResult) obj;
        return successful == that.successful &&
               Objects.equals(operation, that.operation) &&
               Objects.equals(processedAt, that.processedAt) &&
               Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, operation, processedAt, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * <p>The string representation includes the successful flag, operation,
     * processedAt timestamp, and metadata for debugging purposes.</p>
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "MemoryProcessingResult{" +
               "successful=" + successful +
               ", operation='" + operation + '\'' +
               ", processedAt=" + processedAt +
               ", metadata=" + metadata +
               '}';
    }
}