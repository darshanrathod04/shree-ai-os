package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectField</b> — represents a field declaration in a class.
 */
public final class ProjectField {

    private final String name;
    private final String type;
    private final List<String> annotations;

    private ProjectField(Builder b) {
        this.name = Objects.requireNonNull(b.name);
        this.type = b.type == null ? "Object" : b.type;
        this.annotations = List.copyOf(b.annotations == null ? List.of() : b.annotations);
    }

    public String name() { return name; }
    public String type() { return type; }
    public List<String> annotations() { return annotations; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name;
        private String type;
        private List<String> annotations;

        public Builder name(String v) { this.name = v; return this; }
        public Builder type(String v) { this.type = v; return this; }
        public Builder annotations(List<String> v) { this.annotations = v; return this; }

        public ProjectField build() { return new ProjectField(this); }
    }
}
