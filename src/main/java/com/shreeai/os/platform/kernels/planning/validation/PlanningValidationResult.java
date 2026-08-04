package com.shreeai.os.platform.kernels.planning.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningValidationResult</b>
 *
 * <p>Immutable value object representing the result of a planning validation operation.
 * This result captures whether the validation passed, any violations found, the
 * timestamp of validation, and associated metadata.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a structured representation of planning validation outcomes.</li>
 *   <li>Supports multiple violations in a single validation execution.</li>
 *   <li>Enables callers to inspect validation failures without exception handling.</li>
 *   <li>Records the timestamp when validation was performed.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Defensive copying — collections are copied on construction.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-103, EIO-ARCH-001</p>
 */
public final class PlanningValidationResult {

    private final boolean valid;
    private final List<String> violations;
    private final Instant validatedAt;
    private final Map<String, Object> metadata;

    /**
     * Constructs a {@code PlanningValidationResult} with the specified parameters.
     *
     * @param valid       whether the validation passed
     * @param violations  the list of violation messages (must not be {@code null})
     * @param validatedAt the validation timestamp (must not be {@code null})
     * @param metadata    additional metadata (must not be {@code null})
     * @throws NullPointerException if {@code violations}, {@code validatedAt}, or {@code metadata} is {@code null}
     */
    public PlanningValidationResult(boolean valid,
                                    List<String> violations,
                                    Instant validatedAt,
                                    Map<String, Object> metadata) {
        this.valid = valid;
        this.violations = List.copyOf(
                Objects.requireNonNull(violations, "violations must not be null"));
        this.validatedAt = Objects.requireNonNull(validatedAt, "validatedAt must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns whether this validation result is valid.
     *
     * @return {@code true} if validation passed, {@code false} otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns an unmodifiable view of the violation messages.
     *
     * @return the list of violations (empty if validation passed)
     */
    public List<String> violations() {
        return violations;
    }

    /**
     * Returns the timestamp when validation was performed.
     *
     * @return the validation {@link Instant}
     */
    public Instant validatedAt() {
        return validatedAt;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanningValidationResult that)) return false;
        return valid == that.valid
                && violations.equals(that.violations)
                && validatedAt.equals(that.validatedAt)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = (valid ? 1 : 0);
        result = 31 * result + violations.hashCode();
        result = 31 * result + validatedAt.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlanningValidationResult{"
                + "valid=" + valid
                + ", violations=" + violations
                + ", validatedAt=" + validatedAt
                + ", metadata=" + metadata
                + '}';
    }
}