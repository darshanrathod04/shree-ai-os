package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ValidationCriteria</b>
 *
 * <p>Represents immutable plan validation criteria within the Planning Kernel.
 * This model captures validation rules, required conditions, completeness
 * requirements, and associated metadata without performing any validation behavior.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the criteria used to validate a plan.</li>
 *   <li>Captures validation rules, required conditions, and completeness requirements.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no validation behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class ValidationCriteria {

    private final Map<String, String> validationRules;
    private final Map<String, String> requiredConditions;
    private final Map<String, String> completenessRequirements;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code ValidationCriteria} with the specified parameters.
     *
     * @param validationRules         the validation rules (must not be {@code null})
     * @param requiredConditions      the required conditions (must not be {@code null})
     * @param completenessRequirements the completeness requirements (must not be {@code null})
     * @param metadata                additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public ValidationCriteria(Map<String, String> validationRules,
                              Map<String, String> requiredConditions,
                              Map<String, String> completenessRequirements,
                              Map<String, String> metadata) {
        this.validationRules = Collections.unmodifiableMap(
                Objects.requireNonNull(validationRules, "validationRules must not be null"));
        this.requiredConditions = Collections.unmodifiableMap(
                Objects.requireNonNull(requiredConditions, "requiredConditions must not be null"));
        this.completenessRequirements = Collections.unmodifiableMap(
                Objects.requireNonNull(completenessRequirements,
                        "completenessRequirements must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the validation rules.
     *
     * @return the validation rules map
     */
    public Map<String, String> validationRules() {
        return validationRules;
    }

    /**
     * Returns an unmodifiable view of the required conditions.
     *
     * @return the required conditions map
     */
    public Map<String, String> requiredConditions() {
        return requiredConditions;
    }

    /**
     * Returns an unmodifiable view of the completeness requirements.
     *
     * @return the completeness requirements map
     */
    public Map<String, String> completenessRequirements() {
        return completenessRequirements;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationCriteria that)) return false;
        return validationRules.equals(that.validationRules)
                && requiredConditions.equals(that.requiredConditions)
                && completenessRequirements.equals(that.completenessRequirements)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = validationRules.hashCode();
        result = 31 * result + requiredConditions.hashCode();
        result = 31 * result + completenessRequirements.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ValidationCriteria{"
                + "validationRules=" + validationRules
                + ", requiredConditions=" + requiredConditions
                + ", completenessRequirements=" + completenessRequirements
                + ", metadata=" + metadata
                + '}';
    }
}