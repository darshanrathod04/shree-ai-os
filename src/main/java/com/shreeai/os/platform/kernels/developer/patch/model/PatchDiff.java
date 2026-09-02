package com.shreeai.os.platform.kernels.developer.patch.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>PatchDiff</b>
 *
 * <p>Immutable before/after diff record produced when a {@link PatchApplier}
 * applies a single {@link com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch}
 * to a source file. Captures the original source, the modified source,
 * the file path, and the status of the application.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class PatchDiff {

    /** Whether the patch was applied successfully. */
    public enum Status { SUCCESS, PARTIAL, FAILED, SKIPPED }

    private final String filePath;
    private final String before;
    private final String after;
    private final Status status;
    private final String message;
    private final Instant appliedAt;

    private PatchDiff(Builder b) {
        this.filePath = Objects.requireNonNull(b.filePath, "filePath");
        this.before = b.before == null ? "" : b.before;
        this.after = b.after == null ? "" : b.after;
        this.status = b.status == null ? Status.SUCCESS : b.status;
        this.message = b.message == null ? "" : b.message;
        this.appliedAt = b.appliedAt == null ? Instant.now() : b.appliedAt;
    }

    public String filePath() { return filePath; }
    public String before() { return before; }
    public String after() { return after; }
    public Status status() { return status; }
    public String message() { return message; }
    public Instant appliedAt() { return appliedAt; }

    /**
     * Returns true if the patch was applied successfully.
     */
    public boolean isSuccess() { return status == Status.SUCCESS; }

    /**
     * Returns the number of lines changed.
     */
    public int linesChanged() {
        int beforeLines = before.isEmpty() ? 0 : (int) before.lines().count();
        int afterLines = after.isEmpty() ? 0 : (int) after.lines().count();
        return Math.abs(afterLines - beforeLines);
    }

    /**
     * Returns a one-line summary.
     */
    public String summary() {
        return String.format("[%s] %s — %s", status, filePath, message);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String filePath;
        private String before;
        private String after;
        private Status status;
        private String message;
        private Instant appliedAt;

        public Builder filePath(String v) { this.filePath = v; return this; }
        public Builder before(String v) { this.before = v; return this; }
        public Builder after(String v) { this.after = v; return this; }
        public Builder status(Status v) { this.status = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder appliedAt(Instant v) { this.appliedAt = v; return this; }

        public PatchDiff build() { return new PatchDiff(this); }
    }
}
