package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ImpactReport</b>
 *
 * <p>Immutable report produced by {@link ImpactAnalyzer}. Contains the ordered
 * graph of affected classes (direct and indirect), affected REST endpoints,
 * affected entities, affected repositories, and affected services.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class ImpactReport {

    private final String targetClass;
    private final List<String> directlyAffected;
    private final List<String> indirectlyAffected;
    private final List<ProjectEndpoint> affectedEndpoints;
    private final List<ProjectClass> affectedControllers;
    private final List<ProjectClass> affectedServices;
    private final List<ProjectClass> affectedRepositories;
    private final List<ProjectEntity> affectedEntities;
    private final List<ProjectClass> affectedConfigurations;
    private final int dependencyDepth;
    private final Map<String, Object> dependencyChain;

    private ImpactReport(Builder b) {
        this.targetClass = Objects.requireNonNull(b.targetClass, "targetClass");
        this.directlyAffected = List.copyOf(b.directlyAffected == null ? List.of() : b.directlyAffected);
        this.indirectlyAffected = List.copyOf(b.indirectlyAffected == null ? List.of() : b.indirectlyAffected);
        this.affectedEndpoints = List.copyOf(b.affectedEndpoints == null ? List.of() : b.affectedEndpoints);
        this.affectedControllers = List.copyOf(b.affectedControllers == null ? List.of() : b.affectedControllers);
        this.affectedServices = List.copyOf(b.affectedServices == null ? List.of() : b.affectedServices);
        this.affectedRepositories = List.copyOf(b.affectedRepositories == null ? List.of() : b.affectedRepositories);
        this.affectedEntities = List.copyOf(b.affectedEntities == null ? List.of() : b.affectedEntities);
        this.affectedConfigurations = List.copyOf(b.affectedConfigurations == null ? List.of() : b.affectedConfigurations);
        this.dependencyDepth = b.dependencyDepth;
        this.dependencyChain = Map.copyOf(b.dependencyChain == null ? Map.of() : b.dependencyChain);
    }

    public String targetClass() { return targetClass; }
    public List<String> directlyAffected() { return directlyAffected; }
    public List<String> indirectlyAffected() { return indirectlyAffected; }
    public List<ProjectEndpoint> affectedEndpoints() { return affectedEndpoints; }
    public List<ProjectClass> affectedControllers() { return affectedControllers; }
    public List<ProjectClass> affectedServices() { return affectedServices; }
    public List<ProjectClass> affectedRepositories() { return affectedRepositories; }
    public List<ProjectEntity> affectedEntities() { return affectedEntities; }
    public List<ProjectClass> affectedConfigurations() { return affectedConfigurations; }
    public int dependencyDepth() { return dependencyDepth; }
    public Map<String, Object> dependencyChain() { return dependencyChain; }

    public int totalAffected() {
        return directlyAffected.size() + indirectlyAffected.size();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String targetClass;
        private List<String> directlyAffected;
        private List<String> indirectlyAffected;
        private List<ProjectEndpoint> affectedEndpoints;
        private List<ProjectClass> affectedControllers;
        private List<ProjectClass> affectedServices;
        private List<ProjectClass> affectedRepositories;
        private List<ProjectEntity> affectedEntities;
        private List<ProjectClass> affectedConfigurations;
        private int dependencyDepth;
        private Map<String, Object> dependencyChain;

        public Builder targetClass(String v) { this.targetClass = v; return this; }
        public Builder directlyAffected(List<String> v) { this.directlyAffected = v; return this; }
        public Builder indirectlyAffected(List<String> v) { this.indirectlyAffected = v; return this; }
        public Builder affectedEndpoints(List<ProjectEndpoint> v) { this.affectedEndpoints = v; return this; }
        public Builder affectedControllers(List<ProjectClass> v) { this.affectedControllers = v; return this; }
        public Builder affectedServices(List<ProjectClass> v) { this.affectedServices = v; return this; }
        public Builder affectedRepositories(List<ProjectClass> v) { this.affectedRepositories = v; return this; }
        public Builder affectedEntities(List<ProjectEntity> v) { this.affectedEntities = v; return this; }
        public Builder affectedConfigurations(List<ProjectClass> v) { this.affectedConfigurations = v; return this; }
        public Builder dependencyDepth(int v) { this.dependencyDepth = v; return this; }
        public Builder dependencyChain(Map<String, Object> v) { this.dependencyChain = v; return this; }

        public ImpactReport build() { return new ImpactReport(this); }
    }
}
