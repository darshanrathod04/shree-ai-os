package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Hypothesis</b>
 *
 * <p>Represents a reasoning hypothesis.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates a candidate hypothesis for evaluation.</li>
 *   <li>Provides immutable hypothesis representation.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.
 * This represents a candidate hypothesis only and must never determine truth.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param id the unique identifier (must not be {@code null})
 * @param statement the hypothesis statement (must not be {@code null} or empty)
 * @param assumptions the assumptions underlying the hypothesis (must not be {@code null}, values may be {@code null})
 * @param supportingEvidenceReferences references to supporting evidence (must not be {@code null}, elements may be {@code null})
 * @param metadata additional hypothesis metadata (must not be {@code null}, values may be {@code null})
 * @param createdAt the timestamp when the hypothesis was created (must not be {@code null})
 */
public record Hypothesis(
        CognitiveId id,
        String statement,
        Map<String, Object> assumptions,
        List<String> supportingEvidenceReferences,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new Hypothesis with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param statement the hypothesis statement (must not be {@code null} or empty)
     * @param assumptions the assumptions underlying the hypothesis (must not be {@code null}, values may be {@code null})
     * @param supportingEvidenceReferences references to supporting evidence (must not be {@code null}, elements may be {@code null})
     * @param metadata additional hypothesis metadata (must not be {@code null}, values may be {@code null})
     * @param createdAt the timestamp when the hypothesis was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public Hypothesis {
        Objects.requireNonNull(id, "Hypothesis id must not be null");
        Objects.requireNonNull(statement, "Hypothesis statement must not be null");
        if (statement.isBlank()) {
            throw new IllegalArgumentException("Hypothesis statement must not be empty");
        }
        Objects.requireNonNull(assumptions, "Hypothesis assumptions must not be null");
        Objects.requireNonNull(supportingEvidenceReferences, "Hypothesis supportingEvidenceReferences must not be null");
        Objects.requireNonNull(metadata, "Hypothesis metadata must not be null");
        Objects.requireNonNull(createdAt, "Hypothesis createdAt must not be null");
    }

    /**
     * Returns the unique identifier of this hypothesis.
     *
     * @return the hypothesis identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the statement of this hypothesis.
     *
     * @return the hypothesis statement
     */
    public String statement() {
        return statement;
    }

    /**
     * Returns an unmodifiable view of the assumptions for this hypothesis.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the assumptions
     */
    public Map<String, Object> assumptions() {
        return Map.copyOf(assumptions);
    }

    /**
     * Returns an unmodifiable list of the supporting evidence references for this hypothesis.
     *
     * <p>The returned list is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable list of supporting evidence references
     */
    public List<String> supportingEvidenceReferences() {
        return List.copyOf(supportingEvidenceReferences);
    }

    /**
     * Returns an unmodifiable view of the metadata for this hypothesis.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this hypothesis was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns a string representation of this hypothesis.
     *
     * <p>Includes the identifier, statement, and creation timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "Hypothesis{" +
                "id=" + id +
                ", statement='" + statement + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}