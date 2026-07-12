package platform.core.registry.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <b>ValidationResult</b>
 *
 * <p>Structured result returned by the {@link KernelRegistrationValidator}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a structured representation of validation outcomes.</li>
 *   <li>Supports multiple errors and warnings in a single validation execution.</li>
 *   <li>Enables callers to inspect validation failures without exception handling.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> A result is considered valid only when it has zero errors.
 * Warnings do not affect validity.</p>
 *
 * @see KernelRegistrationValidator
 */
public final class ValidationResult {

    private final List<String> errors;
    private final List<String> warnings;

    private ValidationResult(List<String> errors, List<String> warnings) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    /**
     * Creates a new {@code Builder} for constructing a {@code ValidationResult}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether this validation result is valid.
     *
     * <p>A result is valid only when it contains zero errors.
     * Warnings do not affect validity.</p>
     *
     * @return {@code true} if there are no errors, {@code false} otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Returns an unmodifiable list of validation error messages.
     *
     * @return the list of errors (empty if validation passed)
     */
    public List<String> errors() {
        return errors;
    }

    /**
     * Returns an unmodifiable list of validation warning messages.
     *
     * @return the list of warnings (empty if no warnings)
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * <b>ValidationResult.Builder</b>
     *
     * <p>Builder for constructing a {@code ValidationResult} incrementally.</p>
     */
    public static final class Builder {

        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an error message to the validation result.
         *
         * @param message the error message (must not be null)
         * @return this builder for chaining
         * @throws NullPointerException if {@code message} is null
         */
        public Builder addError(String message) {
            errors.add(Objects.requireNonNull(message, "Error message must not be null"));
            return this;
        }

        /**
         * Adds a warning message to the validation result.
         *
         * @param message the warning message (must not be null)
         * @return this builder for chaining
         * @throws NullPointerException if {@code message} is null
         */
        public Builder addWarning(String message) {
            warnings.add(Objects.requireNonNull(message, "Warning message must not be null"));
            return this;
        }

        /**
         * Builds the {@code ValidationResult}.
         *
         * @return a new immutable validation result
         */
        public ValidationResult build() {
            return new ValidationResult(errors, warnings);
        }
    }

    /**
     * Compares this {@code ValidationResult} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code ValidationResult} with the same
     *         errors and warnings
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return errors.equals(that.errors) && warnings.equals(that.warnings);
    }

    /**
     * Returns the hash code for this {@code ValidationResult}.
     *
     * @return the hash code based on errors and warnings
     */
    @Override
    public int hashCode() {
        return Objects.hash(errors, warnings);
    }

    /**
     * Returns a string representation of this {@code ValidationResult}.
     *
     * @return a string indicating validity and counts of errors and warnings
     */
    @Override
    public String toString() {
        return "ValidationResult{"
                + "valid=" + isValid()
                + ", errorCount=" + errors.size()
                + ", warningCount=" + warnings.size()
                + '}';
    }
}