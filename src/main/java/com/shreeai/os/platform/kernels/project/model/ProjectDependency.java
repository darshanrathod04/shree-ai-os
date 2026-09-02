package com.shreeai.os.platform.kernels.project.model;

import java.util.Objects;

/**
 * <b>ProjectDependency</b> — represents a directed edge between two
 * project nodes (class-to-class, controller-to-service, etc.).
 */
public final class ProjectDependency {

    public enum Type {
        EXTENDS,            // class → superclass
        IMPLEMENTS,         // class → interface
        CALLS,              // method → method/type
        DEPENDS_ON,         // field/parameter type → type
        EXPOSES,            // controller method → endpoint
        INJECTS,            // field with @Autowired → type
        RETURNS,            // method → return type
        HAS_REPOSITORY,     // service → repository
        MAPS_TO_ENTITY,     // repository → entity
        HAS_ENTITY,         // entity → entity (relations)
        CONFIGURES          // @Configuration → @Bean methods
    }

    private final String source;
    private final String target;
    private final Type type;
    private final String context; // optional: method name, field name, etc.

    private ProjectDependency(Builder b) {
        this.source = Objects.requireNonNull(b.source);
        this.target = Objects.requireNonNull(b.target);
        this.type = Objects.requireNonNull(b.type);
        this.context = b.context == null ? "" : b.context;
    }

    public String source() { return source; }
    public String target() { return target; }
    public Type type() { return type; }
    public String context() { return context; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String source;
        private String target;
        private Type type;
        private String context;

        public Builder source(String v) { this.source = v; return this; }
        public Builder target(String v) { this.target = v; return this; }
        public Builder type(Type v) { this.type = v; return this; }
        public Builder context(String v) { this.context = v; return this; }

        public ProjectDependency build() { return new ProjectDependency(this); }
    }
}
