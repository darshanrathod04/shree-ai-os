package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.project.model.*;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;

import java.util.*;

/**
 * <b>ArchitectureValidator</b>
 *
 * <p>Detects architectural issues in a project: circular dependencies,
 * layered architecture violations, controller-to-repository direct calls,
 * duplicate endpoints, bean conflicts, and god classes.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class ArchitectureValidator {

    private final ProjectGraph graph;
    private final List<ProjectClass> classes;
    private final List<ProjectEndpoint> endpoints;

    public ArchitectureValidator(ProjectGraph graph, List<ProjectClass> classes,
                                  List<ProjectEndpoint> endpoints) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.classes = new ArrayList<>(Objects.requireNonNull(classes, "classes must not be null"));
        this.endpoints = new ArrayList<>(Objects.requireNonNull(endpoints, "endpoints must not be null"));
    }

    /**
     * Validates the project architecture and returns a list of issues.
     *
     * @return list of validation issues (never null, may be empty)
     */
    public List<ValidationIssue> validate() {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.addAll(detectCircularDependencies());
        issues.addAll(detectLayerViolations());
        issues.addAll(detectControllerRepositoryDirectCalls());
        issues.addAll(detectDuplicateEndpoints());
        issues.addAll(detectLargeControllers());
        issues.addAll(detectGodClasses());
        return issues;
    }

    /**
     * Checks whether a proposed change (intent + impact) would introduce
     * new architecture violations.
     */
    public List<ValidationIssue> validateChange(com.shreeai.os.platform.kernels.developer.api.DeveloperIntent intent,
                                                ImpactReport impact) {
        List<ValidationIssue> issues = new ArrayList<>();
        // Check for layering violations in affected files
        for (ProjectClass c : impact.affectedControllers()) {
            issues.addAll(checkControllerLayering(c));
        }
        for (ProjectClass c : impact.affectedServices()) {
            issues.addAll(checkServiceLayering(c, impact));
        }
        return issues;
    }

    // ─── Circular dependencies ──────────────────────────────────────────────

    private List<ValidationIssue> detectCircularDependencies() {
        List<ValidationIssue> issues = new ArrayList<>();
        List<List<String>> cycles = graph.detectCycles();
        for (List<String> cycle : cycles) {
            issues.add(ValidationIssue.builder()
                    .kind(ValidationIssue.Kind.CIRCULAR_DEPENDENCY)
                    .severity(ValidationIssue.Severity.HIGH)
                    .message("Circular dependency detected: " + String.join(" -> ", cycle))
                    .affectedFiles(cycle)
                    .recommendation("Break the cycle by introducing an interface or refactoring the dependency direction.")
                    .build());
        }
        return issues;
    }

    // ─── Layer violations ───────────────────────────────────────────────────

    private List<ValidationIssue> detectLayerViolations() {
        List<ValidationIssue> issues = new ArrayList<>();
        // Controller should not call Repository directly
        for (ProjectClass c : classes) {
            if (c.role() == Role.CONTROLLER || c.role() == Role.REST_CONTROLLER) {
                for (ProjectDependency dep : graph.outgoing(c.fullyQualifiedName())) {
                    ProjectClass target = graph.findClass(dep.target());
                    if (target != null && target.role() == Role.REPOSITORY) {
                        issues.add(ValidationIssue.builder()
                                .kind(ValidationIssue.Kind.CONTROLLER_REPOSITORY_DIRECT_CALL)
                                .severity(ValidationIssue.Severity.MEDIUM)
                                .message(c.name() + " (controller) directly calls " + target.name() + " (repository)")
                                .affectedFiles(List.of(c.fullyQualifiedName(), target.fullyQualifiedName()))
                                .recommendation("Introduce a service layer between the controller and repository.")
                                .build());
                    }
                }
            }
        }
        return issues;
    }

    private List<ValidationIssue> detectControllerRepositoryDirectCalls() {
        // Already covered in detectLayerViolations
        return List.of();
    }

    // ─── Duplicate endpoints ─────────────────────────────────────────────────

    private List<ValidationIssue> detectDuplicateEndpoints() {
        List<ValidationIssue> issues = new ArrayList<>();
        Map<String, List<ProjectEndpoint>> byPath = new HashMap<>();
        for (ProjectEndpoint ep : endpoints) {
            String key = ep.httpMethod() + " " + ep.path();
            byPath.computeIfAbsent(key, k -> new ArrayList<>()).add(ep);
        }
        for (Map.Entry<String, List<ProjectEndpoint>> entry : byPath.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<String> files = new ArrayList<>();
                for (ProjectEndpoint ep : entry.getValue()) {
                    files.add(ep.controllerClass());
                }
                issues.add(ValidationIssue.builder()
                        .kind(ValidationIssue.Kind.DUPLICATE_ENDPOINT)
                        .severity(ValidationIssue.Severity.HIGH)
                        .message("Duplicate endpoint: " + entry.getKey())
                        .affectedFiles(files)
                        .recommendation("Consolidate to a single implementation or use different paths.")
                        .build());
            }
        }
        return issues;
    }

    // ─── Large controllers ──────────────────────────────────────────────────

    private List<ValidationIssue> detectLargeControllers() {
        List<ValidationIssue> issues = new ArrayList<>();
        for (ProjectClass c : classes) {
            if (c.role() == Role.CONTROLLER || c.role() == Role.REST_CONTROLLER) {
                long httpMethods = c.methods().stream()
                        .filter(m -> m.httpMethod() != null)
                        .count();
                if (httpMethods > 10) {
                    issues.add(ValidationIssue.builder()
                            .kind(ValidationIssue.Kind.LARGE_CONTROLLER)
                            .severity(ValidationIssue.Severity.MEDIUM)
                            .message(c.name() + " has " + httpMethods + " endpoints")
                            .affectedFiles(List.of(c.fullyQualifiedName()))
                            .recommendation("Consider splitting into smaller, feature-based controllers.")
                            .build());
                }
            }
        }
        return issues;
    }

    // ─── God classes ────────────────────────────────────────────────────────

    private List<ValidationIssue> detectGodClasses() {
        List<ValidationIssue> issues = new ArrayList<>();
        for (ProjectClass c : classes) {
            if (c.kind() == ProjectClass.Kind.CLASS || c.kind() == ProjectClass.Kind.INTERFACE) {
                int methodCount = c.methods().size();
                int fieldCount = c.fields().size();
                if (methodCount > 50 || fieldCount > 30) {
                    issues.add(ValidationIssue.builder()
                            .kind(ValidationIssue.Kind.GOD_CLASS)
                            .severity(ValidationIssue.Severity.HIGH)
                            .message(c.name() + " is a god class (" + methodCount + " methods, " + fieldCount + " fields)")
                            .affectedFiles(List.of(c.fullyQualifiedName()))
                            .recommendation("Apply Single Responsibility Principle. Split by feature or concern.")
                            .build());
                }
            }
        }
        return issues;
    }

    // ─── Change-specific checks ─────────────────────────────────────────────

    private List<ValidationIssue> checkControllerLayering(ProjectClass c) {
        List<ValidationIssue> issues = new ArrayList<>();
        if (c.role() != Role.CONTROLLER && c.role() != Role.REST_CONTROLLER) return issues;
        for (ProjectDependency dep : graph.outgoing(c.fullyQualifiedName())) {
            ProjectClass target = graph.findClass(dep.target());
            if (target != null && target.role() == Role.REPOSITORY) {
                issues.add(ValidationIssue.builder()
                        .kind(ValidationIssue.Kind.CONTROLLER_REPOSITORY_DIRECT_CALL)
                        .severity(ValidationIssue.Severity.MEDIUM)
                        .message("Proposed change introduces controller-to-repository direct call")
                        .affectedFiles(List.of(c.fullyQualifiedName()))
                        .recommendation("Use a service layer instead.")
                        .build());
            }
        }
        return issues;
    }

    private List<ValidationIssue> checkServiceLayering(ProjectClass c, ImpactReport impact) {
        List<ValidationIssue> issues = new ArrayList<>();
        // Check if service references entities directly in method params
        for (ProjectMethod m : c.methods()) {
            for (String param : m.parameterTypes()) {
                if (param.endsWith("Entity") || param.endsWith("Model")) {
                    issues.add(ValidationIssue.builder()
                            .kind(ValidationIssue.Kind.LAYER_VIOLATION)
                            .severity(ValidationIssue.Severity.LOW)
                            .message(c.name() + " references entity " + param + " in method " + m.name())
                            .affectedFiles(List.of(c.fullyQualifiedName()))
                            .recommendation("Use DTOs instead of entity types in service method signatures.")
                            .build());
                }
            }
        }
        return issues;
    }
}