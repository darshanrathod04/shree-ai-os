package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>GeneratedPatch</b> — the rendered output of a {@link FilePatch}: an
 * actual, deterministic Java source string, plus the metadata that explains it.
 *
 * <p>The {@code source} field is the complete content of the target file
 * <i>after</i> applying the patch. For new files it is the entire new content.
 * For modified files it is the full reconstructed source.</p>
 *
 * <p>The generated source is purely descriptive — it is never written to disk
 * by the Developer Agent.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class GeneratedPatch {

    private final String targetFile;
    private final boolean newFile;
    private final String source;            // the rendered source code
    private final List<String> addedImports;
    private final List<String> addedMethods;
    private final String reason;
    private final List<String> dependencies;

    private GeneratedPatch(Builder b) {
        this.targetFile = Objects.requireNonNull(b.targetFile, "targetFile");
        this.newFile = b.newFile;
        this.source = Objects.requireNonNull(b.source, "source");
        this.addedImports = List.copyOf(b.addedImports == null ? List.of() : b.addedImports);
        this.addedMethods = List.copyOf(b.addedMethods == null ? List.of() : b.addedMethods);
        this.reason = b.reason == null ? "" : b.reason;
        this.dependencies = List.copyOf(b.dependencies == null ? List.of() : b.dependencies);
    }

    public String targetFile() { return targetFile; }
    public boolean isNewFile() { return newFile; }
    public String source() { return source; }
    public List<String> addedImports() { return addedImports; }
    public List<String> addedMethods() { return addedMethods; }
    public String reason() { return reason; }
    public List<String> dependencies() { return dependencies; }

    public int lineCount() {
        if (source.isEmpty()) return 0;
        return source.split("\n", -1).length;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String targetFile;
        private boolean newFile;
        private String source;
        private List<String> addedImports;
        private List<String> addedMethods;
        private String reason;
        private List<String> dependencies;

        public Builder targetFile(String v) { this.targetFile = v; return this; }
        public Builder newFile(boolean v) { this.newFile = v; return this; }
        public Builder source(String v) { this.source = v; return this; }
        public Builder addedImports(List<String> v) { this.addedImports = v; return this; }
        public Builder addedMethods(List<String> v) { this.addedMethods = v; return this; }
        public Builder reason(String v) { this.reason = v; return this; }
        public Builder dependencies(List<String> v) { this.dependencies = v; return this; }

        public GeneratedPatch build() { return new GeneratedPatch(this); }
    }
}
