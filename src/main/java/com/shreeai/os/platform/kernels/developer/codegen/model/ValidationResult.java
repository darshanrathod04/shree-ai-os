package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>ValidationResult</b> — result of running {@link PatchValidator} against
 * a {@link PatchPlan}.
 *
 * <p>Each patch is individually validated. The overall result is the most severe
 * status across all patches:</p>
 * <ul>
 *   <li>{@link Status#SAFE} — no issues found</li>
 *   <li>{@link Status#WARNING} — minor issues (e.g. unused import) but patch is usable</li>
 *   <li>{@link Status#INVALID} — hard errors (duplicate identifier, broken dependency)</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class ValidationResult {

    /** Overall validation status. */
    public enum Status { SAFE, WARNING, INVALID }

    /** Category of a single validation issue. */
    public enum IssueKind {
        DUPLICATE_IMPORT,
        DUPLICATE_METHOD,
        DUPLICATE_ENDPOINT,
        DUPLICATE_FIELD,
        CLASS_NOT_FOUND,
        MALFORMED_SIGNATURE,
        PACKAGE_MISMATCH,
        CYCLIC_DEPENDENCY,
        UNUSED_IMPORT,
        MISSING_DEPENDENCY
    }

    private final Status overallStatus;
    private final List<PatchResult> patchResults;
    private final List<String> errors;     // all INVALID issues across patches
    private final List<String> warnings;  // all WARNING issues across patches

    private ValidationResult(Builder b) {
        this.overallStatus = computeOverall(b.patchResults);
        this.patchResults = List.copyOf(b.patchResults == null ? List.of() : b.patchResults);
        this.errors = List.copyOf(b.errors == null ? List.of() : b.errors);
        this.warnings = List.copyOf(b.warnings == null ? List.of() : b.warnings);
    }

    private static Status computeOverall(List<PatchResult> results) {
        if (results == null || results.isEmpty()) return Status.SAFE;
        boolean hasInvalid = results.stream()
                .anyMatch(r -> r.status() == Status.INVALID);
        boolean hasWarning = results.stream()
                .anyMatch(r -> r.status() == Status.WARNING);
        if (hasInvalid) return Status.INVALID;
        if (hasWarning) return Status.WARNING;
        return Status.SAFE;
    }

    public Status overallStatus() { return overallStatus; }
    public List<PatchResult> patchResults() { return patchResults; }
    public List<String> errors() { return errors; }
    public List<String> warnings() { return warnings; }

    public boolean isSafe() { return overallStatus == Status.SAFE; }
    public boolean isWarning() { return overallStatus == Status.WARNING; }
    public boolean isInvalid() { return overallStatus == Status.INVALID; }

    public static Builder builder() { return new Builder(); }

    /**
     * <b>PatchResult</b> — validation result for a single {@link FilePatch}.
     */
    public static final class PatchResult {
        private final String targetFile;
        private final Status status;
        private final List<Issue> issues;

        public PatchResult(String targetFile, Status status, List<Issue> issues) {
            this.targetFile = Objects.requireNonNull(targetFile, "targetFile");
            this.status = status;
            this.issues = List.copyOf(issues == null ? List.of() : issues);
        }

        public String targetFile() { return targetFile; }
        public Status status() { return status; }
        public List<Issue> issues() { return issues; }
    }

    /**
     * <b>Issue</b> — a single validation finding.
     */
    public static final class Issue {
        private final IssueKind kind;
        private final String message;
        private final String targetFile;
        private final String operationSignature;
        private final Status severity;

        public Issue(IssueKind kind, String message, String targetFile,
                     String operationSignature, Status severity) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.message = Objects.requireNonNull(message, "message");
            this.targetFile = targetFile == null ? "" : targetFile;
            this.operationSignature = operationSignature == null ? "" : operationSignature;
            this.severity = Objects.requireNonNull(severity, "severity");
        }

        public IssueKind kind() { return kind; }
        public String message() { return message; }
        public String targetFile() { return targetFile; }
        public String operationSignature() { return operationSignature; }
        public Status severity() { return severity; }
    }

    public static final class Builder {
        private List<PatchResult> patchResults;
        private List<String> errors;
        private List<String> warnings;

        public Builder patchResults(List<PatchResult> v) { this.patchResults = v; return this; }
        public Builder addPatchResult(PatchResult r) {
            if (this.patchResults == null) this.patchResults = new ArrayList<>();
            this.patchResults.add(r);
            return this;
        }
        public Builder errors(List<String> v) { this.errors = v; return this; }
        public Builder warnings(List<String> v) { this.warnings = v; return this; }

        public ValidationResult build() { return new ValidationResult(this); }
    }
}
