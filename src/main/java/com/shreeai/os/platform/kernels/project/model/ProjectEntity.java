package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectEntity</b> — represents a JPA entity (or any class annotated
 * with {@code @Entity}, {@code @Document}, etc.).
 */
public final class ProjectEntity {

    private final String name;
    private final String fullyQualifiedName;
    private final String tableName;       // derived from @Table if present
    private final List<ProjectField> fields;
    private final String repositoryClass;

    private ProjectEntity(Builder b) {
        this.name = Objects.requireNonNull(b.name);
        this.fullyQualifiedName = Objects.requireNonNull(b.fullyQualifiedName);
        this.tableName = b.tableName;
        this.fields = List.copyOf(b.fields == null ? List.of() : b.fields);
        this.repositoryClass = b.repositoryClass;
    }

    public String name() { return name; }
    public String fullyQualifiedName() { return fullyQualifiedName; }
    public String tableName() { return tableName; }
    public List<ProjectField> fields() { return fields; }
    public String repositoryClass() { return repositoryClass; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String name;
        private String fullyQualifiedName;
        private String tableName;
        private List<ProjectField> fields;
        private String repositoryClass;

        public Builder name(String v) { this.name = v; return this; }
        public Builder fullyQualifiedName(String v) { this.fullyQualifiedName = v; return this; }
        public Builder tableName(String v) { this.tableName = v; return this; }
        public Builder fields(List<ProjectField> v) { this.fields = v; return this; }
        public Builder repositoryClass(String v) { this.repositoryClass = v; return this; }

        public ProjectEntity build() { return new ProjectEntity(this); }
    }
}
