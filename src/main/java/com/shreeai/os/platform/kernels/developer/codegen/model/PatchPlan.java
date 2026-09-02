package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PatchPlan</b> — an ordered, deterministic set of {@link FilePatch} entries
 * produced by the {@link com.shreeai.os.platform.kernels.developer.codegen.PatchPlanner}.
 *
 * <p>A {@code PatchPlan} is the structured contract between the
 * <i>analysis</i> layer and the <i>code generation</i> layer: each patch is
 * fully described, carries its own reason and dependencies, and is ready to
 * be turned into actual source code by
 * {@link com.shreeai.os.platform.kernels.developer.codegen.JavaCodeGenerator}.</p>
 *
 * <p>The plan itself contains no source code; the actual code is produced
 * lazily by the code generator when the user (or the SDK) asks for it.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class PatchPlan {

    /** Status of the plan. */
    public enum Status { DRAFT, READY, APPLIED, REJECTED }

    private final String request;            // original developer request
    private final String intent;             // DeveloperIntentType.name()
    private final String entity;             // entity from the intent
    private final List<FilePatch> patches;
    private final List<String> newFiles;     // convenience: target files that are new
    private final List<String> modifiedFiles; // convenience: target files that are modified
    private final List<String> testFiles;    // convenience: test files to be generated
    private final Status status;
    private final Instant createdAt;
    private final Map<String, Object> metadata;

    private PatchPlan(Builder b) {
        this.request = Objects.requireNonNull(b.request, "request");
        this.intent = Objects.requireNonNull(b.intent, "intent");
        this.entity = b.entity == null ? "" : b.entity;
        this.patches = List.copyOf(b.patches == null ? List.of() : b.patches);
        this.newFiles = List.copyOf(b.newFiles == null ? List.of() : b.newFiles);
        this.modifiedFiles = List.copyOf(b.modifiedFiles == null ? List.of() : b.modifiedFiles);
        this.testFiles = List.copyOf(b.testFiles == null ? List.of() : b.testFiles);
        this.status = b.status == null ? Status.DRAFT : b.status;
        this.createdAt = b.createdAt == null ? Instant.now() : b.createdAt;
        this.metadata = Map.copyOf(b.metadata == null ? Map.of() : b.metadata);
    }

    public String request() { return request; }
    public String intent() { return intent; }
    public String entity() { return entity; }
    public List<FilePatch> patches() { return patches; }
    public List<String> newFiles() { return newFiles; }
    public List<String> modifiedFiles() { return modifiedFiles; }
    public List<String> testFiles() { return testFiles; }
    public Status status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Map<String, Object> metadata() { return metadata; }

    public int totalPatches() { return patches.size(); }
    public int totalOperations() {
        return patches.stream().mapToInt(p -> p.operations().size()).sum();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String request;
        private String intent;
        private String entity;
        private List<FilePatch> patches;
        private List<String> newFiles;
        private List<String> modifiedFiles;
        private List<String> testFiles;
        private Status status;
        private Instant createdAt;
        private Map<String, Object> metadata;

        public Builder request(String v) { this.request = v; return this; }
        public Builder intent(String v) { this.intent = v; return this; }
        public Builder entity(String v) { this.entity = v; return this; }
        public Builder patches(List<FilePatch> v) { this.patches = v; return this; }
        public Builder newFiles(List<String> v) { this.newFiles = v; return this; }
        public Builder modifiedFiles(List<String> v) { this.modifiedFiles = v; return this; }
        public Builder testFiles(List<String> v) { this.testFiles = v; return this; }
        public Builder status(Status v) { this.status = v; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public Builder metadata(Map<String, Object> v) { this.metadata = v; return this; }

        public PatchPlan build() { return new PatchPlan(this); }
    }
}
