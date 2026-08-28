package com.shreeai.os.platform.core.plugin.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <b>VerificationResult</b>
 *
 * <p>Immutable result of a plugin verification within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a complete verification report.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see VerificationIssue
 * @see VerificationSeverity
 * @see PluginVerifier
 */
public final class VerificationResult {

    private final boolean valid;
    private final List<VerificationIssue> issues;

    private VerificationResult(boolean valid, List<VerificationIssue> issues) {
        this.valid = valid;
        this.issues = issues != null
                ? Collections.unmodifiableList(new ArrayList<>(issues))
                : Collections.emptyList();
    }

    /**
     * Returns {@code true} if the plugin is valid (no ERROR issues).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns an unmodifiable list of all verification issues.
     *
     * @return the list of issues
     */
    public List<VerificationIssue> issues() {
        return issues;
    }

    /**
     * Returns the number of ERROR issues.
     *
     * @return error count
     */
    public int errorCount() {
        return (int) issues.stream()
                .filter(i -> i.severity() == VerificationSeverity.ERROR)
                .count();
    }

    /**
     * Returns the number of WARNING issues.
     *
     * @return warning count
     */
    public int warningCount() {
        return (int) issues.stream()
                .filter(i -> i.severity() == VerificationSeverity.WARNING)
                .count();
    }

    /**
     * Returns the number of INFO issues.
     *
     * @return info count
     */
    public int infoCount() {
        return (int) issues.stream()
                .filter(i -> i.severity() == VerificationSeverity.INFO)
                .count();
    }

    /**
     * Creates a new {@code Builder} for constructing {@code VerificationResult} instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerificationResult that = (VerificationResult) obj;
        return valid == that.valid && issues.equals(that.issues);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(valid);
        result = 31 * result + issues.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VerificationResult{" +
                "valid=" + valid +
                ", issues=" + issues +
                '}';
    }

    /**
     * <b>Builder</b>
     *
     * <p>Builder for constructing {@code VerificationResult} instances.</p>
     */
    public static final class Builder {
        private final List<VerificationIssue> issues = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an issue to the result.
         *
         * @param severity the severity
         * @param message  the message
         * @return this builder
         */
        public Builder addIssue(VerificationSeverity severity, String message) {
            issues.add(new VerificationIssue(severity, message));
            return this;
        }

        /**
         * Adds an ERROR issue to the result.
         *
         * @param message the error message
         * @return this builder
         */
        public Builder addError(String message) {
            return addIssue(VerificationSeverity.ERROR, message);
        }

        /**
         * Adds a WARNING issue to the result.
         *
         * @param message the warning message
         * @return this builder
         */
        public Builder addWarning(String message) {
            return addIssue(VerificationSeverity.WARNING, message);
        }

        /**
         * Adds an INFO issue to the result.
         *
         * @param message the info message
         * @return this builder
         */
        public Builder addInfo(String message) {
            return addIssue(VerificationSeverity.INFO, message);
        }

        /**
         * Builds the {@code VerificationResult}.
         *
         * @return a new verification result
         */
        public VerificationResult build() {
            boolean hasErrors = issues.stream()
                    .anyMatch(i -> i.severity() == VerificationSeverity.ERROR);
            return new VerificationResult(!hasErrors, issues);
        }
    }
}