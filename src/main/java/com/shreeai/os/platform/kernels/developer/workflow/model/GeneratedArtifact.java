package com.shreeai.os.platform.kernels.developer.workflow.model;

import java.util.Objects;

/**
 * <b>GeneratedArtifact</b>
 *
 * <p>Immutable descriptor for a single generated file artifact (Java source,
 * test, or configuration) produced by the developer workflow engine.
 * Holds the path, artifact type, source content, and package name.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class GeneratedArtifact {

    /** Category of the generated artifact. */
    public enum Type { JAVA, TEST, CONFIG }

    private final String path;
    private final Type type;
    private final String source;
    private final String packageName;

    private GeneratedArtifact(Builder b) {
        this.path = Objects.requireNonNull(b.path, "path must not be null");
        this.type = b.type == null ? Type.JAVA : b.type;
        this.source = b.source == null ? "" : b.source;
        this.packageName = b.packageName == null ? "" : b.packageName;
    }

    public String path() { return path; }
    public Type type() { return type; }
    public String source() { return source; }
    public String packageName() { return packageName; }

    /**
     * Returns the number of source lines in this artifact.
     */
    public int lineCount() {
        if (source == null || source.isEmpty()) return 0;
        return (int) source.lines().count();
    }

    /**
     * Returns the simple file name (last path segment).
     */
    public String fileName() {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String path;
        private Type type;
        private String source;
        private String packageName;

        public Builder path(String v) { this.path = v; return this; }
        public Builder type(Type v) { this.type = v; return this; }
        public Builder source(String v) { this.source = v; return this; }
        public Builder packageName(String v) { this.packageName = v; return this; }

        public GeneratedArtifact build() { return new GeneratedArtifact(this); }
    }
}
