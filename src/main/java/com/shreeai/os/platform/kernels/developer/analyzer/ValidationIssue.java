package com.shreeai.os.platform.kernels.developer.analyzer;

import java.util.List;
import java.util.Objects;

/**
 * <b>ValidationIssue</b>
 *
 * <p>Represents a detected architecture violation or risk in the project.</p>
 *
 * @since Sprint-14
 */
public final class ValidationIssue {

    public enum Severity { LOW, MEDIUM, HIGH }

    public enum Kind {
        CIRCULAR_DEPENDENCY,
        LAYER_VIOLATION,
        CONTROLLER_REPOSITORY_DIRECT_CALL,
        MISSING_SERVICE_LAYER,
        DUPLICATE_ENDPOINT,
        BEAN_CONFLICT,
        LARGE_CLASS,
        LARGE_CONTROLLER,
        GOD_CLASS,
        MISSING_VALIDATION,
        CYCLIC_IMPORT,
        NAMING_VIOLATION
    }

    private final Kind kind;
    private final Severity severity;
    private final String message;
    private final List<String> affectedFiles;
    private final String recommendation;

    private ValidationIssue(Builder b) {
        this.kind = Objects.requireNonNull(b.kind);
        this.severity = Objects.requireNonNull(b.severity);
        this.message = Objects.requireNonNull(b.message);
        this.affectedFiles = List.copyOf(b.affectedFiles == null ? List.of() : b.affectedFiles);
        this.recommendation = b.recommendation == null ? "" : b.recommendation;
    }

    public Kind kind() { return kind; }
    public Severity severity() { return severity; }
    public String message() { return message; }
    public List<String> affectedFiles() { return affectedFiles; }
    public String recommendation() { return recommendation; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Kind kind;
        private Severity severity;
        private String message;
        private List<String> affectedFiles;
        private String recommendation;

        public Builder kind(Kind v) { this.kind = v; return this; }
        public Builder severity(Severity v) { this.severity = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder affectedFiles(List<String> v) { this.affectedFiles = v; return this; }
        public Builder recommendation(String v) { this.recommendation = v; return this; }

        public ValidationIssue build() { return new ValidationIssue(this); }
    }
}
