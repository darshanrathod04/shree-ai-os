package com.shreeai.os.platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <b>MultiAgentValidationResult</b>
 *
 * <p>Immutable value object representing the result of Multi-Agent validation.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>MultiAgentValidationResult aggregates validation issues and warnings
 * from all validators in the validation layer.</p>
 *
 * @param valid   whether the validation passed
 * @param issues  list of validation issues (must not be {@code null})
 * @param warnings list of validation warnings (must not be {@code null})
 *
 * @since 1.0
 */
public final class MultiAgentValidationResult {
    private final boolean valid;
    private final List<String> issues;
    private final List<String> warnings;

    /**
     * Creates a new MultiAgentValidationResult with the specified parameters.
     *
     * @param valid    whether the validation passed
     * @param issues   list of validation issues (must not be {@code null})
     * @param warnings list of validation warnings (must not be {@code null})
     * @throws NullPointerException if issues or warnings is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult(boolean valid, List<String> issues, List<String> warnings) {
        this.valid = valid;
        this.issues = List.copyOf(Objects.requireNonNull(issues, "MultiAgentValidationResult issues must not be null"));
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "MultiAgentValidationResult warnings must not be null"));
    }

    /**
     * Returns whether the validation passed.
     *
     * @return {@code true} if validation passed
     * @since 1.0
     */
    public boolean valid() {
        return valid;
    }

    /**
     * Returns the list of validation issues.
     *
     * @return unmodifiable list of validation issues
     * @since 1.0
     */
    public List<String> issues() {
        return issues;
    }

    /**
     * Returns the list of validation warnings.
     *
     * @return unmodifiable list of validation warnings
     * @since 1.0
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * Creates a successful validation result with no issues or warnings.
     *
     * @return a successful validation result
     * @since 1.0
     */
    public static MultiAgentValidationResult success() {
        return new MultiAgentValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Creates a failed validation result with the specified issue.
     *
     * @param issue the validation issue (must not be {@code null} or blank)
     * @return a failed validation result
     * @throws NullPointerException     if issue is {@code null}
     * @throws IllegalArgumentException if issue is blank
     * @since 1.0
     */
    public static MultiAgentValidationResult failure(String issue) {
        Objects.requireNonNull(issue, "Validation issue must not be null");
        if (issue.isBlank()) {
            throw new IllegalArgumentException("Validation issue must not be blank");
        }
        List<String> issues = new ArrayList<>();
        issues.add(issue);
        return new MultiAgentValidationResult(false, issues, Collections.emptyList());
    }

    /**
     * Creates a validation result with multiple issues.
     *
     * @param issues the list of validation issues (must not be {@code null})
     * @return a failed validation result
     * @throws NullPointerException if issues is {@code null}
     * @since 1.0
     */
    public static MultiAgentValidationResult failure(List<String> issues) {
        Objects.requireNonNull(issues, "Validation issues must not be null");
        return new MultiAgentValidationResult(false, issues, Collections.emptyList());
    }

    /**
     * Adds a warning to this validation result.
     *
     * @param warning the warning to add (must not be {@code null} or blank)
     * @return a new validation result with the warning added
     * @throws NullPointerException     if warning is {@code null}
     * @throws IllegalArgumentException if warning is blank
     * @since 1.0
     */
    public MultiAgentValidationResult withWarning(String warning) {
        Objects.requireNonNull(warning, "Validation warning must not be null");
        if (warning.isBlank()) {
            throw new IllegalArgumentException("Validation warning must not be blank");
        }
        List<String> newWarnings = new ArrayList<>(this.warnings);
        newWarnings.add(warning);
        return new MultiAgentValidationResult(this.valid, this.issues, newWarnings);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MultiAgentValidationResults are equal if they have the same valid, issues, and warnings.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MultiAgentValidationResult that = (MultiAgentValidationResult) obj;
        return valid == that.valid &&
               issues.equals(that.issues) &&
               warnings.equals(that.warnings);
    }

    /**
     * Returns a hash code value for the MultiAgentValidationResult.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(valid, issues, warnings);
    }

    /**
     * Returns a string representation of the MultiAgentValidationResult.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "MultiAgentValidationResult{" +
                "valid=" + valid +
                ", issues=" + issues +
                ", warnings=" + warnings +
                '}';
    }
}