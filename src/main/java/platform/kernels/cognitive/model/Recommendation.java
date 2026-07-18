package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Recommendation</b>
 *
 * <p>Represents a recommendation produced by future reasoning.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates recommendation data from reasoning processes.</li>
 *   <li>Provides immutable recommendation representation.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.
 * This contains data only and performs no recommendation generation.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param id the unique identifier (must not be {@code null})
 * @param description the recommendation description (must not be {@code null} or empty)
 * @param rationale the rationale for the recommendation (must not be {@code null} or empty)
 * @param confidenceMetadata confidence metadata for the recommendation (must not be {@code null}, values may be {@code null})
 * @param supportingReferences references supporting the recommendation (must not be {@code null}, elements may be {@code null})
 * @param metadata additional recommendation metadata (must not be {@code null}, values may be {@code null})
 * @param createdAt the timestamp when the recommendation was created (must not be {@code null})
 */
public record Recommendation(
        CognitiveId id,
        String description,
        String rationale,
        Map<String, Object> confidenceMetadata,
        List<String> supportingReferences,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new Recommendation with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param description the recommendation description (must not be {@code null} or empty)
     * @param rationale the rationale for the recommendation (must not be {@code null} or empty)
     * @param confidenceMetadata confidence metadata for the recommendation (must not be {@code null}, values may be {@code null})
     * @param supportingReferences references supporting the recommendation (must not be {@code null}, elements may be {@code null})
     * @param metadata additional recommendation metadata (must not be {@code null}, values may be {@code null})
     * @param createdAt the timestamp when the recommendation was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public Recommendation {
        Objects.requireNonNull(id, "Recommendation id must not be null");
        Objects.requireNonNull(description, "Recommendation description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("Recommendation description must not be empty");
        }
        Objects.requireNonNull(rationale, "Recommendation rationale must not be null");
        if (rationale.isBlank()) {
            throw new IllegalArgumentException("Recommendation rationale must not be empty");
        }
        Objects.requireNonNull(confidenceMetadata, "Recommendation confidenceMetadata must not be null");
        Objects.requireNonNull(supportingReferences, "Recommendation supportingReferences must not be null");
        Objects.requireNonNull(metadata, "Recommendation metadata must not be null");
        Objects.requireNonNull(createdAt, "Recommendation createdAt must not be null");
    }

    /**
     * Returns the unique identifier of this recommendation.
     *
     * @return the recommendation identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the description of this recommendation.
     *
     * @return the recommendation description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the rationale for this recommendation.
     *
     * @return the recommendation rationale
     */
    public String rationale() {
        return rationale;
    }

    /**
     * Returns an unmodifiable view of the confidence metadata for this recommendation.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the confidence metadata
     */
    public Map<String, Object> confidenceMetadata() {
        return Map.copyOf(confidenceMetadata);
    }

    /**
     * Returns an unmodifiable list of the supporting references for this recommendation.
     *
     * <p>The returned list is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable list of supporting references
     */
    public List<String> supportingReferences() {
        return List.copyOf(supportingReferences);
    }

    /**
     * Returns an unmodifiable view of the metadata for this recommendation.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this recommendation was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns a string representation of this recommendation.
     *
     * <p>Includes the identifier, description, and creation timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "Recommendation{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}