package platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EvaluationCriteria</b>
 *
 * <p>Represents immutable decision evaluation criteria.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates evaluation criteria for decision assessment.</li>
 *   <li>Provides immutable criteria parameters.</li>
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
 * @param criterionName the name of the criterion (must not be {@code null} or empty)
 * @param weight the weight of this criterion (must not be {@code null})
 * @param priority the priority level (must not be {@code null} or empty)
 * @param metadata additional criteria metadata (must not be {@code null}, values may be {@code null})
 * @param createdAt the timestamp when the criteria was created (must not be {@code null})
 */
public record EvaluationCriteria(
        CognitiveId id,
        String criterionName,
        Double weight,
        String priority,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new EvaluationCriteria with the specified parameters.
     *
     * <p>Performs defensive validation to ensure all required fields are non-null
     * and meet validity constraints.</p>
     *
     * @param id the unique identifier (must not be {@code null})
     * @param criterionName the name of the criterion (must not be {@code null} or empty)
     * @param weight the weight of this criterion (must not be {@code null})
     * @param priority the priority level (must not be {@code null} or empty)
     * @param metadata additional criteria metadata (must not be {@code null}, values may be {@code null})
     * @param createdAt the timestamp when the criteria was created (must not be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public EvaluationCriteria {
        Objects.requireNonNull(id, "EvaluationCriteria id must not be null");
        Objects.requireNonNull(criterionName, "EvaluationCriteria criterionName must not be null");
        if (criterionName.isBlank()) {
            throw new IllegalArgumentException("EvaluationCriteria criterionName must not be empty");
        }
        Objects.requireNonNull(weight, "EvaluationCriteria weight must not be null");
        if (weight < 0.0 || weight > 1.0) {
            throw new IllegalArgumentException("EvaluationCriteria weight must be between 0.0 and 1.0");
        }
        Objects.requireNonNull(priority, "EvaluationCriteria priority must not be null");
        if (priority.isBlank()) {
            throw new IllegalArgumentException("EvaluationCriteria priority must not be empty");
        }
        Objects.requireNonNull(metadata, "EvaluationCriteria metadata must not be null");
        Objects.requireNonNull(createdAt, "EvaluationCriteria createdAt must not be null");
    }

    /**
     * Returns the unique identifier of this evaluation criteria.
     *
     * @return the evaluation criteria identifier
     */
    public CognitiveId id() {
        return id;
    }

    /**
     * Returns the name of this evaluation criterion.
     *
     * @return the criterion name
     */
    public String criterionName() {
        return criterionName;
    }

    /**
     * Returns the weight of this evaluation criterion.
     *
     * @return the criterion weight (between 0.0 and 1.0)
     */
    public Double weight() {
        return weight;
    }

    /**
     * Returns the priority level of this evaluation criterion.
     *
     * @return the priority level
     */
    public String priority() {
        return priority;
    }

    /**
     * Returns an unmodifiable view of the metadata for this evaluation criteria.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Returns the timestamp when this evaluation criteria was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns a string representation of this evaluation criteria.
     *
     * <p>Includes the identifier, criterion name, weight, and priority.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "EvaluationCriteria{" +
                "id=" + id +
                ", criterionName='" + criterionName + '\'' +
                ", weight=" + weight +
                ", priority='" + priority + '\'' +
                '}';
    }
}