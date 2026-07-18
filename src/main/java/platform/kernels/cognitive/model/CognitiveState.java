package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CognitiveState</b>
 *
 * <p>Represents the current cognitive condition of the reasoning engine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the current state of cognitive processes.</li>
 *   <li>Provides immutable snapshots of cognitive condition.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param id the unique identifier (must not be {@code null})
 * @param stateName the name/type of the cognitive state (must not be {@code null} or empty)
 * @param lifecycleStatus the current lifecycle status (must not be {@code null})
 * @param createdAt the timestamp when the state was created (must not be {@code null})
 * @param updatedAt the timestamp when the state was last updated (must not be {@code null})
 * @param metadata additional state metadata (must not be {@code null}, values may be {@code null})
 */
public record CognitiveState(
        CognitiveId id,
        String stateName,
        String lifecycleStatus,
        Instant createdAt,
        Instant updatedAt,
        Map<String, Object> metadata
) {

    /**
     * Creates a new CognitiveState with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param stateName the name/type of the cognitive state (must not be {@code null} or empty)
     * @param lifecycleStatus the current lifecycle status (must not be {@code null})
     * @param createdAt the timestamp when the state was created (must not be {@code null})
     * @param updatedAt the timestamp when the state was last updated (must not be {@code null})
     * @param metadata additional state metadata (must not be {@code null}, values may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public CognitiveState {
        Objects.requireNonNull(id, "CognitiveState id must not be null");
        Objects.requireNonNull(stateName, "CognitiveState stateName must not be null");
        if (stateName.isBlank()) {
            throw new IllegalArgumentException("CognitiveState stateName must not be empty");
        }
        Objects.requireNonNull(lifecycleStatus, "CognitiveState lifecycleStatus must not be null");
        Objects.requireNonNull(createdAt, "CognitiveState createdAt must not be null");
        Objects.requireNonNull(updatedAt, "CognitiveState updatedAt must not be null");
        Objects.requireNonNull(metadata, "CognitiveState metadata must not be null");

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("CognitiveState updatedAt must not be before createdAt");
        }
    }

    /**
     * Returns the unique identifier of this cognitive state.
     *
     * @return the cognitive state identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the name/type of this cognitive state.
     *
     * @return the state name
     */
    public String stateName() {
        return stateName;
    }

    /**
     * Returns the lifecycle status of this cognitive state.
     *
     * @return the lifecycle status
     */
    public String lifecycleStatus() {
        return lifecycleStatus;
    }

    /**
     * Returns the creation timestamp of this cognitive state.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns the last update timestamp of this cognitive state.
     *
     * @return the update timestamp
     */
    public Instant updatedAt() {
        return updatedAt;
    }

    /**
     * Returns an unmodifiable view of the metadata associated with this cognitive state.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns a string representation of this cognitive state.
     *
     * <p>Includes the identifier, state name, lifecycle status, and timestamps.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveState{" +
                "id=" + id +
                ", stateName='" + stateName + '\'' +
                ", lifecycleStatus='" + lifecycleStatus + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}