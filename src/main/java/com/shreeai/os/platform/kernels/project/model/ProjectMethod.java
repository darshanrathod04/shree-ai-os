package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectMethod</b> — represents a method signature within a class.
 * Captures the return type, parameters, and HTTP method mapping (if any).
 */
public final class ProjectMethod {

    private final String name;
    private final String returnType;
    private final List<String> parameterTypes;
    private final List<String> annotations;
    private final String httpMethod;   // "GET" | "POST" | ... or null
    private final String httpPath;     // "/users/{id}" or null

    private ProjectMethod(Builder b) {
        this.name = Objects.requireNonNull(b.name);
        this.returnType = b.returnType == null ? "void" : b.returnType;
        this.parameterTypes = List.copyOf(b.parameterTypes == null ? List.of() : b.parameterTypes);
        this.annotations = List.copyOf(b.annotations == null ? List.of() : b.annotations);
        this.httpMethod = b.httpMethod;
        this.httpPath = b.httpPath;
    }

    public String name() { return name; }
    public String returnType() { return returnType; }
    public List<String> parameterTypes() { return parameterTypes; }
    public List<String> annotations() { return annotations; }
    public String httpMethod() { return httpMethod; }
    public String httpPath() { return httpPath; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name;
        private String returnType;
        private List<String> parameterTypes;
        private List<String> annotations;
        private String httpMethod;
        private String httpPath;

        public Builder name(String v) { this.name = v; return this; }
        public Builder returnType(String v) { this.returnType = v; return this; }
        public Builder parameterTypes(List<String> v) { this.parameterTypes = v; return this; }
        public Builder annotations(List<String> v) { this.annotations = v; return this; }
        public Builder httpMethod(String v) { this.httpMethod = v; return this; }
        public Builder httpPath(String v) { this.httpPath = v; return this; }

        public ProjectMethod build() { return new ProjectMethod(this); }
    }
}
