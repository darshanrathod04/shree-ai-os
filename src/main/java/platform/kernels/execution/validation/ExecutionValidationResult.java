package platform.kernels.execution.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionValidationResult</b>
 *
 * <p>Represents the immutable outcome of execution validation.
 * This value object encapsulates validation results and any violations found.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates validation outcome.</li>
 *   <li>Provides structural validation results.</li>
 *   <li>Contains violation details when validation fails.</li>
 *   <li>Contains no validation logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-103, EIO-ARCH-001</p>
 *
 * @param valid       whether the validation passed
 * @param violations  the list of validation violations (must not be {@code null})
 * @param validatedAt the timestamp when validation was performed (must not be {@code null})
 * @param metadata    additional validation metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionValidationResult {

    private final boolean valid;
    private final List<String> violations;
    private final Instant validatedAt;
    private final Map<String, Object> metadata;

    /**
     * Constructs an {@code ExecutionValidationResult} with the specified parameters.
     *
     * @param valid       whether the validation passed
     * @param violations  the list of validation violations (must not be {@code null})
     * @param validatedAt the timestamp when validation was performed (must not be {@code null})
     * @param metadata    additional validation metadata (must not be {@code null})
     * @throws IllegalArgumentException if violations, validatedAt, or metadata is {@code null}
     */
    public ExecutionValidationResult(
            boolean valid,
            List<String> violations,
            Instant validatedAt,
            Map<String, Object> metadata) {
        if (violations == null) {
            throw new IllegalArgumentException("ExecutionValidationResult violations must not be null");
        }
        if (validatedAt == null) {
            throw new IllegalArgumentException("ExecutionValidationResult validatedAt must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ExecutionValidationResult metadata must not be null");
        }

        this.valid = valid;
        this.violations = Collections.unmodifiableList(new java.util.ArrayList<>(violations));
        this.validatedAt = validatedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether the validation passed.
     *
     * @return {@code true} if validation passed, {@code false} otherwise
     */
    public boolean valid() {
        return valid;
    }

    /**
     * Returns an unmodifiable list of validation violations.
     *
     * <p>The returned list is unmodifiable and reflects the violations at the
     * time of this call. If validation passed, the list will be empty.</p>
     *
     * @return an unmodifiable list of violations
     */
    public List<String> violations() {
        return violations;
    }

    /**
     * Returns the timestamp when validation was performed.
     *
     * @return the validation timestamp
     */
    public Instant validatedAt() {
        return validatedAt;
    }

    /**
     * Returns an unmodifiable view of the validation metadata.
     *
     * <p>The returned map is unmodifiable and reflects the metadata at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionValidationResult} instances are equal if they have the same
     * validity, violations, timestamp, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionValidationResult that = (ExecutionValidationResult) obj;
        return valid == that.valid &&
                Objects.equals(violations, that.violations) &&
                Objects.equals(validatedAt, that.validatedAt) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for this {@code ExecutionValidationResult}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(valid, violations, validatedAt, metadata);
    }

    /**
     * Returns a string representation of this {@code ExecutionValidationResult}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionValidationResult{" +
                "valid=" + valid +
                ", violations=" + violations +
                ", validatedAt=" + validatedAt +
                ", metadata=" + metadata +
                '}';
    }
}