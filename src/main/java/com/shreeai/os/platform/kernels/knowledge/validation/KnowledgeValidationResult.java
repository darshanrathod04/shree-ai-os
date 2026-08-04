package com.shreeai.os.platform.kernels.knowledge.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeValidationResult</b>
 *
 * <p>An immutable value object representing the result of a Knowledge validation operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates validation outcomes without side effects.</li>
 *   <li>Provides immutable validation metadata for audit and debugging.</li>
 *   <li>Serves as the sole return type for all Knowledge validators.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-103, EIO-ARCH-001</p>
 */
public final class KnowledgeValidationResult {

    private final boolean valid;
    private final List<String> violations;
    private final Instant validatedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new KnowledgeValidationResult with validation and defensive copying.
     *
     * <p>All parameters are validated for null. Collections are defensively copied
     * to ensure immutability.</p>
     *
     * @param valid      whether the validation passed
     * @param violations list of validation violations (must not be null, will be defensively copied)
     * @param validatedAt when the validation was performed (must not be null)
     * @param metadata   additional validation metadata (must not be null, will be defensively copied)
     * @throws NullPointerException if violations, validatedAt, or metadata is null
     */
    public KnowledgeValidationResult(
            boolean valid,
            List<String> violations,
            Instant validatedAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(violations, "violations must not be null");
        Objects.requireNonNull(validatedAt, "validatedAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        this.valid = valid;
        this.violations = Collections.unmodifiableList(List.copyOf(violations));
        this.validatedAt = validatedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether the validation passed.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns an unmodifiable list of validation violations.
     *
     * <p>This method ensures that the internal violations list cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable list of violations (empty if valid)
     */
    public List<String> getViolations() {
        return violations;
    }

    /**
     * Returns when the validation was performed.
     *
     * @return the validation timestamp
     */
    public Instant getValidatedAt() {
        return validatedAt;
    }

    /**
     * Returns an unmodifiable map of validation metadata.
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
     * <p>Two KnowledgeValidationResult objects are equal if they have the same
     * valid flag, violations, validatedAt timestamp, and metadata.</p>
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
        KnowledgeValidationResult that = (KnowledgeValidationResult) o;
        return valid == that.valid
                && Objects.equals(violations, that.violations)
                && Objects.equals(validatedAt, that.validatedAt)
                && Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(valid, violations, validatedAt, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "KnowledgeValidationResult{" +
                "valid=" + valid +
                ", violations=" + violations +
                ", validatedAt=" + validatedAt +
                ", metadata=" + metadata +
                '}';
    }
}