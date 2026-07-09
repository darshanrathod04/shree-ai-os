package com.darshan.agent.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a single validation rule execution.
 *
 * <p>Immutable outcome containing pass/fail status, optional warnings,
 * and metadata for observability.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>Immutable after construction. All collections are unmodifiable.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
public final class ValidationOutcome {

    private final boolean passed;
    private final List<String> warnings;
    private final List<String> errors;
    private final String message;

    /**
     * Create a successful outcome with no warnings.
     */
    public static ValidationOutcome success() {
        return new ValidationOutcome(true, Collections.emptyList(), Collections.emptyList(), "Passed");
    }

    /**
     * Create a successful outcome with warnings.
     */
    public static ValidationOutcome successWithWarnings(List<String> warnings, String message) {
        return new ValidationOutcome(true, warnings != null ? warnings : Collections.emptyList(),
                                    Collections.emptyList(), message);
    }

    /**
     * Create a failed outcome.
     */
    public static ValidationOutcome failure(List<String> errors, String message) {
        return new ValidationOutcome(false, Collections.emptyList(),
                                    errors != null ? errors : Collections.emptyList(), message);
    }

    public ValidationOutcome(boolean passed, List<String> warnings, List<String> errors, String message) {
        this.passed = passed;
        this.warnings = warnings != null ? Collections.unmodifiableList(new ArrayList<>(warnings)) : Collections.emptyList();
        this.errors = errors != null ? Collections.unmodifiableList(new ArrayList<>(errors)) : Collections.emptyList();
        this.message = message != null ? message : "";
    }

    public boolean isPassed() { return passed; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public String getMessage() { return message; }

    /**
     * Builder for ValidationOutcome.
     */
    public static class Builder {
        private boolean passed = true;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private String message = "";

        public Builder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder warning(String warning) {
            this.warnings.add(warning);
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings.addAll(warnings);
            return this;
        }

        public Builder error(String error) {
            this.errors.add(error);
            this.passed = false;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors.addAll(errors);
            this.passed = false;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationOutcome build() {
            return new ValidationOutcome(passed, warnings, errors, message);
        }
    }
}