package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.project.model.*;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;

import java.util.*;

/**
 * <b>ImpactAnalyzer</b>
 *
 * <p>Uses the existing {@link ProjectGraph} to identify the ordered set of
 * affected classes (direct and indirect), endpoints, controllers, services,
 * repositories, and entities for a given developer request.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class ImpactAnalyzer {

    private final ProjectGraph graph;
    private final List<ProjectClass> classes;
    private final List<ProjectEndpoint> endpoints;
    private final List<ProjectEntity> entities;

    public ImpactAnalyzer(ProjectGraph graph, List<ProjectClass> classes,
                          List<ProjectEndpoint> endpoints, List<ProjectEntity> entities) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.classes = new ArrayList<>(Objects.requireNonNull(classes, "classes must not be null"));
        this.endpoints = new ArrayList<>(Objects.requireNonNull(endpoints, "endpoints must not be null"));
        this.entities = new ArrayList<>(Objects.requireNonNull(entities, "entities must not be null"));
    }

    /**
     * Analyzes the impact of the given developer intent on the project.
     *
     * @param intent the parsed developer intent
     * @return ImpactReport with all affected artifacts
     */
    public ImpactReport analyze(com.shreeai.os.platform.kernels.developer.api.DeveloperIntent intent) {
        List<String> targets = resolveTargets(intent);
        List<String> directlyAffected = new ArrayList<>();
        List<String> indirectlyAffected = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String target : targets) {
            List<String> impacted = graph.impactOf(target);
            for (String fqn : impacted) {
                if (seen.add(fqn)) {
                    directlyAffected.add(fqn);
                }
            }
        }

        // Split direct from indirect
        Set<String> directSet = new HashSet<>(directlyAffected);
        List<String> computedIndirect = new ArrayList<>();
        for (String fqn : directlyAffected) {
            for (ProjectDependency edge : graph.outgoing(fqn)) {
                if (directSet.contains(edge.target())) {
                    if (!computedIndirect.contains(fqn)) {
                        computedIndirect.add(fqn);
                    }
                }
            }
        }

        // Sort for determinism
        List<String> sortedDirect = new ArrayList<>(directSet);
        Collections.sort(sortedDirect);
        List<String> sortedIndirect = new ArrayList<>(computedIndirect);
        Collections.sort(sortedIndirect);

        // Build role-based buckets
        List<ProjectClass> controllers = filterByRole(sortedDirect, Role.CONTROLLER, Role.REST_CONTROLLER);
        List<ProjectClass> services = filterByRole(sortedDirect, Role.SERVICE);
        List<ProjectClass> repositories = filterByRole(sortedDirect, Role.REPOSITORY);
        List<ProjectClass> configurations = filterByRole(sortedDirect, Role.CONFIGURATION);
        List<ProjectEntity> affectedEntities = filterEntities(sortedDirect);
        List<ProjectEndpoint> affectedEndpoints = filterEndpoints(sortedDirect);

        // Dependency chain (simple BFS from each target)
        Map<String, Object> chain = buildDependencyChain(targets);

        int depth = computeMaxDepth(targets);

        return ImpactReport.builder()
                .targetClass(targets.isEmpty() ? "" : targets.get(0))
                .directlyAffected(sortedDirect)
                .indirectlyAffected(sortedIndirect)
                .affectedEndpoints(affectedEndpoints)
                .affectedControllers(controllers)
                .affectedServices(services)
                .affectedRepositories(repositories)
                .affectedConfigurations(configurations)
                .affectedEntities(affectedEntities)
                .dependencyDepth(depth)
                .dependencyChain(chain)
                .build();
    }

    /**
     * Analyzes impact for a specific class name.
     */
    public ImpactReport analyzeClass(String simpleName) {
        List<String> targets = resolveTargetByName(simpleName);
        if (targets.isEmpty()) {
            return ImpactReport.builder()
                    .targetClass(simpleName)
                    .directlyAffected(List.of())
                    .indirectlyAffected(List.of())
                    .affectedEndpoints(List.of())
                    .affectedControllers(List.of())
                    .affectedServices(List.of())
                    .affectedRepositories(List.of())
                    .affectedConfigurations(List.of())
                    .affectedEntities(List.of())
                    .dependencyDepth(0)
                    .build();
        }
        List<String> directlyAffected = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String target : targets) {
            for (String fqn : graph.impactOf(target)) {
                if (seen.add(fqn)) directlyAffected.add(fqn);
            }
        }
        Collections.sort(directlyAffected);

        List<ProjectClass> controllers = filterByRole(directlyAffected, Role.CONTROLLER, Role.REST_CONTROLLER);
        List<ProjectClass> services = filterByRole(directlyAffected, Role.SERVICE);
        List<ProjectClass> repositories = filterByRole(directlyAffected, Role.REPOSITORY);
        List<ProjectClass> configurations = filterByRole(directlyAffected, Role.CONFIGURATION);
        List<ProjectEntity> affectedEntities = filterEntities(directlyAffected);
        List<ProjectEndpoint> affectedEndpoints = filterEndpoints(directlyAffected);
        Map<String, Object> chain = buildDependencyChain(targets);
        int depth = computeMaxDepth(targets);

        return ImpactReport.builder()
                .targetClass(targets.get(0))
                .directlyAffected(directlyAffected)
                .indirectlyAffected(List.of())
                .affectedEndpoints(affectedEndpoints)
                .affectedControllers(controllers)
                .affectedServices(services)
                .affectedRepositories(repositories)
                .affectedConfigurations(configurations)
                .affectedEntities(affectedEntities)
                .dependencyDepth(depth)
                .dependencyChain(chain)
                .build();
    }

    // ─── Target resolution ────────────────────────────────────────────────────

    private List<String> resolveTargets(com.shreeai.os.platform.kernels.developer.api.DeveloperIntent intent) {
        List<String> targets = new ArrayList<>();
        // Use tokens from intent
        for (String token : intent.tokens()) {
            targets.addAll(resolveTargetByName(token));
        }
        // Fallback: entity name
        if (targets.isEmpty() && !intent.entity().isEmpty()) {
            targets.addAll(resolveTargetByName(intent.entity()));
        }
        // Fallback: intent domain keyword
        if (targets.isEmpty()) {
            targets.addAll(resolveTargetByName(intent.domain()));
        }
        return targets;
    }

    private List<String> resolveTargetByName(String name) {
        List<String> found = new ArrayList<>();
        if (name == null || name.isBlank()) return found;
        ProjectClass c = graph.findClass(name);
        if (c != null) found.add(c.fullyQualifiedName());
        List<ProjectClass> byName = graph.findByName(name);
        for (ProjectClass pc : byName) {
            if (!found.contains(pc.fullyQualifiedName())) {
                found.add(pc.fullyQualifiedName());
            }
        }
        return found;
    }

    // ─── Filtering helpers ────────────────────────────────────────────────────

    private List<ProjectClass> filterByRole(List<String> fqns, Role... roles) {
        Set<Role> roleSet = Set.of(roles);
        List<ProjectClass> result = new ArrayList<>();
        for (String fqn : fqns) {
            ProjectClass c = graph.findClass(fqn);
            if (c != null && roleSet.contains(c.role())) {
                result.add(c);
            }
        }
        return result;
    }

    private List<ProjectEntity> filterEntities(List<String> fqns) {
        Set<String> fqnSet = new HashSet<>(fqns);
        List<ProjectEntity> result = new ArrayList<>();
        for (ProjectEntity e : entities) {
            if (fqnSet.contains(e.fullyQualifiedName())) {
                result.add(e);
            }
        }
        return result;
    }

    private List<ProjectEndpoint> filterEndpoints(List<String> fqns) {
        Set<String> fqnSet = new HashSet<>(fqns);
        List<ProjectEndpoint> result = new ArrayList<>();
        for (ProjectEndpoint ep : endpoints) {
            if (fqnSet.contains(ep.controllerClass())) {
                result.add(ep);
            }
        }
        return result;
    }

    // ─── Dependency chain ───────────────────────────────────────────────────

    private Map<String, Object> buildDependencyChain(List<String> targets) {
        Map<String, Object> chain = new LinkedHashMap<>();
        List<String> ordered = new ArrayList<>();
        for (String target : targets) {
            ProjectClass c = graph.findClass(target);
            if (c != null) {
                ordered.add(c.name());
                List<String> deps = new ArrayList<>();
                for (ProjectDependency edge : graph.outgoing(target)) {
                    deps.add(graph.findClass(edge.target()) != null
                            ? graph.findClass(edge.target()).name() : edge.target());
                }
                chain.put(c.name(), deps);
            }
        }
        chain.put("__orderedTargets__", ordered);
        return chain;
    }

    private int computeMaxDepth(List<String> targets) {
        int max = 0;
        for (String target : targets) {
            int depth = computeDepth(target, new HashSet<>());
            if (depth > max) max = depth;
        }
        return max;
    }

    private int computeDepth(String fqn, Set<String> visited) {
        if (!visited.add(fqn)) return 0;
        int max = 0;
        for (ProjectDependency edge : graph.outgoing(fqn)) {
            int child = computeDepth(edge.target(), visited);
            if (child > max) max = child;
        }
        return max + 1;
    }
}