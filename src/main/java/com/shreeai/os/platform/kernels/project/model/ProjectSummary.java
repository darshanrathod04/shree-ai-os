package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ProjectSummary</b>
 *
 * <p>Deterministic project metadata produced by the Project Intelligence
 * Kernel. Contains high-level architecture, modules, and statistics.</p>
 *
 * <p><b>Ownership:</b> Project Intelligence (Sprint-13)</p>
 */
public final class ProjectSummary {

    private final String projectName;
    private final String projectPath;
    private final String buildSystem;     // "MAVEN" | "GRADLE" | "UNKNOWN"
    private final String framework;        // "SPRING_BOOT" | "PLAIN_JAVA" | "UNKNOWN"
    private final List<ProjectModule> modules;
    private final ProjectStatistics statistics;
    private final List<String> risks;     // ["Circular dependency", "Large controller", ...]

    private ProjectSummary(Builder b) {
        this.projectName = Objects.requireNonNull(b.projectName, "projectName");
        this.projectPath = Objects.requireNonNull(b.projectPath, "projectPath");
        this.buildSystem = b.buildSystem == null ? "UNKNOWN" : b.buildSystem;
        this.framework = b.framework == null ? "UNKNOWN" : b.framework;
        this.modules = List.copyOf(b.modules == null ? List.of() : b.modules);
        this.statistics = b.statistics == null ? ProjectStatistics.empty() : b.statistics;
        this.risks = List.copyOf(b.risks == null ? List.of() : b.risks);
    }

    public String projectName() { return projectName; }
    public String projectPath() { return projectPath; }
    public String buildSystem() { return buildSystem; }
    public String framework() { return framework; }
    public List<ProjectModule> modules() { return modules; }
    public ProjectStatistics statistics() { return statistics; }
    public List<String> risks() { return risks; }

    public Map<String, Object> toMap() {
        return Map.of(
                "projectName", projectName,
                "projectPath", projectPath,
                "buildSystem", buildSystem,
                "framework", framework,
                "moduleCount", modules.size(),
                "modules", modules.stream().map(ProjectModule::name).toList(),
                "statistics", Map.of(
                        "classes", statistics.classCount(),
                        "controllers", statistics.controllerCount(),
                        "services", statistics.serviceCount(),
                        "repositories", statistics.repositoryCount(),
                        "entities", statistics.entityCount(),
                        "restApis", statistics.endpointCount(),
                        "beans", statistics.beanCount()
                ),
                "risks", risks
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String projectName;
        private String projectPath;
        private String buildSystem;
        private String framework;
        private List<ProjectModule> modules;
        private ProjectStatistics statistics;
        private List<String> risks;

        public Builder projectName(String v) { this.projectName = v; return this; }
        public Builder projectPath(String v) { this.projectPath = v; return this; }
        public Builder buildSystem(String v) { this.buildSystem = v; return this; }
        public Builder framework(String v) { this.framework = v; return this; }
        public Builder modules(List<ProjectModule> v) { this.modules = v; return this; }
        public Builder statistics(ProjectStatistics v) { this.statistics = v; return this; }
        public Builder risks(List<String> v) { this.risks = v; return this; }

        public ProjectSummary build() { return new ProjectSummary(this); }
    }
}
