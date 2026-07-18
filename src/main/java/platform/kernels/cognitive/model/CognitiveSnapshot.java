package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CognitiveSnapshot</b>
 *
 * <p>Represents an immutable snapshot of the cognitive state.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates an immutable historical record of cognitive state.</li>
 *   <li>Provides point-in-time snapshots for analysis and audit.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.
 * Snapshots are immutable historical records.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param id the unique identifier (must not be {@code null})
 * @param cognitiveState the cognitive state at the time of snapshot (must not be {@code null})
 * @param snapshotTimestamp the timestamp when the snapshot was taken (must not be {@code null})
 * @param metadata additional snapshot metadata (must not be {@code null}, values may be {@code null})
 */
public record CognitiveSnapshot(
        CognitiveId id,
        CognitiveState cognitiveState,
        Instant snapshotTimestamp,
        Map<String, Object> metadata
) {

    /**
     * Creates a new CognitiveSnapshot with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param cognitiveState the cognitive state at the time of snapshot (must not be {@code null})
     * @param snapshotTimestamp the timestamp when the snapshot was taken (must not be {@code null})
     * @param metadata additional snapshot metadata (must not be {@code null}, values may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public CognitiveSnapshot {
        Objects.requireNonNull(id, "CognitiveSnapshot id must not be null");
        Objects.requireNonNull(cognitiveState, "CognitiveSnapshot cognitiveState must not be null");
        Objects.requireNonNull(snapshotTimestamp, "CognitiveSnapshot snapshotTimestamp must not be null");
        Objects.requireNonNull(metadata, "CognitiveSnapshot metadata must not be null");
    }

    /**
     * Returns the unique identifier of this cognitive snapshot.
     *
     * @return the cognitive snapshot identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the cognitive state captured in this snapshot.
     *
     * @return the cognitive state
     */
    public CognitiveState cognitiveState() {
        return cognitiveState;
    }

    /**
     * Returns the timestamp when this snapshot was taken.
     *
     * @return the snapshot timestamp
     */
    public Instant snapshotTimestamp() {
        return snapshotTimestamp;
    }

    /**
     * Returns an unmodifiable view of the metadata for this cognitive snapshot.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns a string representation of this cognitive snapshot.
     *
     * <p>Includes the identifier, snapshot timestamp, and cognitive state identifier.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveSnapshot{" +
                "id=" + id +
                ", cognitiveState=" + cognitiveState.id() +
                ", snapshotTimestamp=" + snapshotTimestamp +
                '}';
    }
}