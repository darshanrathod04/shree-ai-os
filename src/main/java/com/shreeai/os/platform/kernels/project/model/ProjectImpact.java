package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectImpact</b> — describes the impact of modifying a class.
 * Includes affected files, dependency depth, and impacted APIs.
 */
public final class ProjectImpact {

    private final String target;
    private final List<String> affectedClasses;
    private final List<ProjectEndpoint> affectedEndpoints;
    private final int dependencyDepth;

    private ProjectImpact(Builder b) {
        this.target = Objects.requireNonNull(b.target);
        this.affectedClasses = List.copyOf(b.affectedClasses == null ? List.of() : b.affectedClasses);
        this.affectedEndpoints = List.copyOf(b.affectedEndpoints == null ? List.of() : b.affectedEndpoints);
        this.dependencyDepth = b.dependencyDepth;
    }

    public String target() { return target; }
    public List<String> affectedClasses() { return affectedClasses; }
    public List<ProjectEndpoint> affectedEndpoints() { return affectedEndpoints; }
    public int dependencyDepth() { return dependencyDepth; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String target;
        private List<String> affectedClasses;
        private List<ProjectEndpoint> affectedEndpoints;
        private int dependencyDepth;

        public Builder target(String v) { this.target = v; return this; }
        public Builder affectedClasses(List<String> v) { this.affectedClasses = v; return this; }
        public Builder affectedEndpoints(List<ProjectEndpoint> v) { this.affectedEndpoints = v; return this; }
        public Builder dependencyDepth(int v) { this.dependencyDepth = v; return this; }

        public ProjectImpact build() { return new ProjectImpact(this); }
    }
}
