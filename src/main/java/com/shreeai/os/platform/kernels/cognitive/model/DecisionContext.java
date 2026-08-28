package com.shreeai.os.platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DecisionContext</b>
 *
 * <p>Represents the context in which a decision will be evaluated.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the context for decision evaluation.</li>
 *   <li>Provides immutable decision context parameters.</li>
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
 * @param availableAlternatives the list of available alternatives (must not be {@code null}, elements may be {@code null})
 * @param assumptions the decision assumptions (must not be {@code null}, values may be {@code null})
 * @param constraints the decision constraints (must not be {@code null}, values may be {@code null})
 * @param metadata additional context metadata (must not be {@code null}, values may be {@code null})
 * @param createdAt the timestamp when the context was created (must not be {@code null})
 */
public record DecisionContext(
        CognitiveId id,
        List<String> availableAlternatives,
        Map<String, Object> assumptions,
        Map<String, Object> constraints,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new DecisionContext with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param availableAlternatives the list of available alternatives (must not be {@code null}, elements may be {@code null})
     * @param assumptions the decision assumptions (must not be {@code null}, values may be {@code null})
     * @param constraints the decision constraints (must not be {@code null}, values may be {@code null})
     * @param metadata additional context metadata (must not be {@code null}, values may be {@code null})
     * @param createdAt the timestamp when the context was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public DecisionContext {
        Objects.requireNonNull(id, "DecisionContext id must not be null");
        Objects.requireNonNull(availableAlternatives, "DecisionContext availableAlternatives must not be null");
        Objects.requireNonNull(assumptions, "DecisionContext assumptions must not be null");
        Objects.requireNonNull(constraints, "DecisionContext constraints must not be null");
        Objects.requireNonNull(metadata, "DecisionContext metadata must not be null");
        Objects.requireNonNull(createdAt, "DecisionContext createdAt must not be null");
    }

    /**
     * Returns the unique identifier of this decision context.
     *
     * @return the decision context identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns an unmodifiable list of the available alternatives for this decision context.
     *
     * <p>The returned list is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable list of available alternatives
     */
    public List<String> availableAlternatives() {
        return List.copyOf(availableAlternatives);
    }

    /**
     * Returns an unmodifiable view of the assumptions for this decision context.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the assumptions
     */
    public Map<String, Object> assumptions() {
        return Map.copyOf(assumptions);
    }

    /**
     * Returns an unmodifiable view of the constraints for this decision context.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the constraints
     */
    public Map<String, Object> constraints() {
        return Map.copyOf(constraints);
    }

    /**
     * Returns an unmodifiable view of the metadata for this decision context.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this decision context was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns a string representation of this decision context.
     *
     * <p>Includes the identifier, number of alternatives, and creation timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "DecisionContext{" +
                "id=" + id +
                ", availableAlternatives=" + availableAlternatives +
                ", createdAt=" + createdAt +
                '}';
    }
}