package platform.kernels.cognitive.engine;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import platform.kernels.cognitive.model.CognitiveState;

/**
 * <b>CognitiveProcessingResult</b>
 *
 * <p>Immutable value object representing the result of cognitive processing.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates processing outcomes</li>
 *   <li>Provides immutable result representation</li>
 *   <li>Contains no behavior - data carrier only</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object. All collections are unmodifiable.
 * Defensive copying is applied to all mutable inputs. This class is final with final fields.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-106, EIO-ARCH-001</p>
 *
 * @param successful indicates whether processing succeeded ({@code true}) or failed ({@code false})
 * @param processedAt the timestamp when processing was completed (must not be {@code null})
 * @param metadata processing metadata (must not be {@code null}, values may be {@code null})
 * @param result the processing result (may be {@code null})
 * @param updatedState the updated cognitive state (may be {@code null})
 * @since 1.0
 */
public final class CognitiveProcessingResult {

    private final boolean successful;
    private final Instant processedAt;
    private final Map<String, Object> metadata;
    private final Object result;
    private final CognitiveState updatedState;

    /**
     * Creates a new CognitiveProcessingResult with the specified parameters.
     *
     * <p>Performs defensive validation and creates immutable copies of all collections.</p>
     *
     * @param successful indicates whether processing succeeded ({@code true}) or failed ({@code false})
     * @param processedAt the timestamp when processing was completed (must not be {@code null})
     * @param metadata processing metadata (must not be {@code null}, values may be {@code null})
     * @param result the processing result (may be {@code null})
     * @param updatedState the updated cognitive state (may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public CognitiveProcessingResult(
            boolean successful,
            Instant processedAt,
            Map<String, Object> metadata,
            Object result,
            CognitiveState updatedState) {
        Objects.requireNonNull(processedAt, "CognitiveProcessingResult processedAt must not be null");
        Objects.requireNonNull(metadata, "CognitiveProcessingResult metadata must not be null");

        this.successful = successful;
        this.processedAt = processedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.result = result;
        this.updatedState = updatedState;
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
     * Returns the timestamp when processing was completed.
     *
     * @return the processing timestamp
     */
    public Instant processedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable view of the processing metadata.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns the processing result.
     *
     * @return the processing result, or {@code null} if no result
     */
    public Object result() {
        return result;
    }

    /**
     * Returns the updated cognitive state.
     *
     * @return the updated cognitive state, or {@code null} if no state update
     */
    public CognitiveState updatedState() {
        return updatedState;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>The equality is based on all fields: successful, processedAt, metadata, result, and updatedState.</p>
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

        CognitiveProcessingResult that = (CognitiveProcessingResult) obj;
        return successful == that.successful &&
               Objects.equals(processedAt, that.processedAt) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(result, that.result) &&
               Objects.equals(updatedState, that.updatedState);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, processedAt, metadata, result, updatedState);
    }

    /**
     * Returns a string representation of the processing result.
     *
     * <p>Includes the success status, timestamp, and result summary.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveProcessingResult{" +
                "successful=" + successful +
                ", processedAt=" + processedAt +
                ", result=" + result +
                ", updatedState=" + updatedState +
                '}';
    }
}