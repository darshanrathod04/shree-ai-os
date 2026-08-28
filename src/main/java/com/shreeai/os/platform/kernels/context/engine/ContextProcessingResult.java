package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.Context;
import com.shreeai.os.platform.kernels.context.model.ContextSnapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ContextProcessingResult</b>
 *
 * <p>An immutable value object representing the result of Context processing operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of Context processing operations.</li>
 *   <li>Provides immutable processing results to the service layer.</li>
 *   <li>Contains success status, processed Context, snapshots, and metadata.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-106, EIO-ARCH-001</p>
 *
 * @param successful whether the processing was successful
 * @param context the processed Context (may be null if unsuccessful)
 * @param snapshot the created ContextSnapshot (may be null if not applicable)
 * @param processedAt when the processing occurred (must not be null)
 * @param metadata additional processing metadata (must not be null, defensively copied)
 */
public final class ContextProcessingResult {
    private final boolean successful;
    private final Context context;
    private final ContextSnapshot snapshot;
    private final Instant processedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new ContextProcessingResult with validation and defensive copying.
     *
     * <p>All parameters are validated. The metadata map is defensively copied
     * to ensure immutability.</p>
     *
     * @param successful whether the processing was successful
     * @param context the processed Context (may be null if unsuccessful)
     * @param snapshot the created ContextSnapshot (may be null if not applicable)
     * @param processedAt when the processing occurred (must not be null)
     * @param metadata additional processing metadata (must not be null, will be defensively copied)
     * @throws NullPointerException if {@code processedAt} or {@code metadata} is null
     */
    public ContextProcessingResult(
            boolean successful,
            Context context,
            ContextSnapshot snapshot,
            Instant processedAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(processedAt, "processedAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        this.successful = successful;
        this.context = context;
        this.snapshot = snapshot;
        this.processedAt = processedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether the processing was successful.
     *
     * @return {@code true} if processing was successful, {@code false} otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the processed Context.
     *
     * <p>This may be null if the processing was unsuccessful or if the operation
     * does not produce a Context.</p>
     *
     * @return the processed Context, or null if not applicable
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the created ContextSnapshot.
     *
     * <p>This may be null if the operation does not produce a snapshot.</p>
     *
     * @return the ContextSnapshot, or null if not applicable
     */
    public ContextSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Returns when the processing occurred.
     *
     * @return the processing timestamp
     */
    public Instant getProcessedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable map of processing metadata.
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
     * <p>Two ContextProcessingResult objects are equal if they have the same
     * successful, context, snapshot, processedAt, and metadata values.</p>
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
        ContextProcessingResult that = (ContextProcessingResult) o;
        return successful == that.successful &&
                Objects.equals(context, that.context) &&
                Objects.equals(snapshot, that.snapshot) &&
                Objects.equals(processedAt, that.processedAt) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, context, snapshot, processedAt, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ContextProcessingResult{" +
                "successful=" + successful +
                ", context=" + context +
                ", snapshot=" + snapshot +
                ", processedAt=" + processedAt +
                ", metadata=" + metadata +
                '}';
    }
}