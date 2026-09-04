package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectClass</b> — represents a Java class/interface/enum/record
 * discovered in the project.
 */
public final class ProjectClass {

    public enum Kind { CLASS, INTERFACE, ENUM, RECORD, ANNOTATION }

    public enum Role {
        CONTROLLER, REST_CONTROLLER, SERVICE, REPOSITORY, COMPONENT,
        CONFIGURATION, ENTITY, BEAN, NONE
    }

    private final String name;
    private final String fullyQualifiedName;
    private final String packageName;
    private final String filePath;
    private final Kind kind;
    private final Role role;
    private final List<String> modifiers;
    private final List<String> annotations;
    private final List<ProjectMethod> methods;
    private final List<ProjectField> fields;
    private final String superClass;       // may be null
    private final List<String> interfaces; // may be empty

    private ProjectClass(Builder b) {
        this.name = Objects.requireNonNull(b.name);
        this.fullyQualifiedName = Objects.requireNonNull(b.fullyQualifiedName);
        this.packageName = b.packageName == null ? "" : b.packageName;
        this.filePath = b.filePath == null ? "" : b.filePath;
        this.kind = b.kind == null ? Kind.CLASS : b.kind;
        this.role = b.role == null ? Role.NONE : b.role;
        this.modifiers = List.copyOf(b.modifiers == null ? List.of() : b.modifiers);
        this.annotations = List.copyOf(b.annotations == null ? List.of() : b.annotations);
        this.methods = List.copyOf(b.methods == null ? List.of() : b.methods);
        this.fields = List.copyOf(b.fields == null ? List.of() : b.fields);
        this.superClass = b.superClass;
        this.interfaces = List.copyOf(b.interfaces == null ? List.of() : b.interfaces);
    }

    public String name() { return name; }
    public String fullyQualifiedName() { return fullyQualifiedName; }
    public String packageName() { return packageName; }
    public String filePath() { return filePath; }
    public Kind kind() { return kind; }
    public Role role() { return role; }
    public List<String> modifiers() { return modifiers; }
    public List<String> annotations() { return annotations; }
    public List<ProjectMethod> methods() { return methods; }
    public List<ProjectField> fields() { return fields; }
    public String superClass() { return superClass; }
    public List<String> interfaces() { return interfaces; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name;
        private String fullyQualifiedName;
        private String packageName;
        private String filePath;
        private Kind kind;
        private Role role;
        private List<String> modifiers;
        private List<String> annotations;
        private List<ProjectMethod> methods;
        private List<ProjectField> fields;
        private String superClass;
        private List<String> interfaces;

        public Builder name(String v) { this.name = v; return this; }
        public Builder fullyQualifiedName(String v) { this.fullyQualifiedName = v; return this; }
        public Builder packageName(String v) { this.packageName = v; return this; }
        public Builder filePath(String v) { this.filePath = v; return this; }
        public Builder kind(Kind v) { this.kind = v; return this; }
        public Builder role(Role v) { this.role = v; return this; }
        public Builder modifiers(List<String> v) { this.modifiers = v; return this; }
        public Builder annotations(List<String> v) { this.annotations = v; return this; }
        public Builder methods(List<ProjectMethod> v) { this.methods = v; return this; }
        public Builder fields(List<ProjectField> v) { this.fields = v; return this; }
        public Builder superClass(String v) { this.superClass = v; return this; }
        public Builder interfaces(List<String> v) { this.interfaces = v; return this; }

        public ProjectClass build() { return new ProjectClass(this); }
    }
}
