package com.shreeai.os.platform.kernels.project.engine;

import com.shreeai.os.platform.kernels.project.analyzer.DependencyGraphBuilder;
import com.shreeai.os.platform.kernels.project.analyzer.SpringAnalyzer;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;
import com.shreeai.os.platform.kernels.project.model.ProjectDependency;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;
import com.shreeai.os.platform.kernels.project.model.ProjectGraph;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectModule;
import com.shreeai.os.platform.kernels.project.model.ProjectStatistics;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import com.shreeai.os.platform.kernels.project.parser.JavaAstParser;
import com.shreeai.os.platform.kernels.project.scanner.RepositoryScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>DefaultProjectIntelligenceEngine</b>
 *
 * <p>The top-level orchestrator for the Project Intelligence Kernel.
 * Coordinates:
 * <ol>
 *   <li>Repository scanning</li>
 *   <li>AST parsing</li>
 *   <li>Spring analysis</li>
 *   <li>Dependency graph construction</li>
 *   <li>Summary + impact generation</li>
 * </ol></p>
 *
 * <p><b>Ownership:</b> Project Intelligence (Sprint-13)</p>
 */
public final class DefaultProjectIntelligenceEngine {

    private final JavaAstParser astParser;
    private ProjectGraph lastGraph;
    private ProjectSummary lastSummary;
    private List<ProjectClass> lastClasses;
    private List<ProjectEndpoint> lastEndpoints;
    private List<ProjectEntity> lastEntities;
    private Path lastAnalyzedPath;

    public DefaultProjectIntelligenceEngine() {
        this.astParser = new JavaAstParser();
    }

    /**
     * Analyzes a project at the given path and returns a {@link ProjectSummary}.
     * The result is cached internally for subsequent queries
     * (findClass, findController, findEntity, impact, etc.).
     *
     * @param projectPath root directory of the project
     * @return ProjectSummary
     * @throws IOException if the project cannot be read
     */
    public ProjectSummary analyze(Path projectPath) throws IOException {
        if (projectPath == null) {
            throw new IllegalArgumentException("projectPath must not be null");
        }
        Path root = projectPath.toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IOException("Not a directory: " + root);
        }

        RepositoryScanner scanner = new RepositoryScanner(root);

        // 1. Scan
        List<Path> javaFiles = scanner.findJavaFiles();
        List<Path> configFiles = scanner.findConfigFiles();

        // 2. Parse
        List<ProjectClass> classes = new ArrayList<>();
        for (Path javaFile : javaFiles) {
            try {
                ProjectClass parsed = astParser.parse(javaFile);
                if (parsed != null) {
                    classes.add(parsed);
                }
            } catch (Exception e) {
                // Skip malformed files but continue
            }
        }

        // 3. Spring analysis
        SpringAnalyzer springAnalyzer = new SpringAnalyzer(classes);
        boolean isSpringBoot = springAnalyzer.isSpringBoot();
        List<ProjectEndpoint> endpoints = springAnalyzer.extractEndpoints();
        List<ProjectEntity> entities = springAnalyzer.extractEntities();
        int beanCount = springAnalyzer.countBeans();

        // Link entities to repositories
        linkEntitiesToRepositories(classes, entities);

        // 4. Build dependency graph
        DependencyGraphBuilder graphBuilder = new DependencyGraphBuilder(classes);
        List<ProjectDependency> edges = graphBuilder.buildEdges(endpoints);
        ProjectGraph graph = new ProjectGraph(classes, edges);

        // 5. Build modules
        List<ProjectModule> modules = discoverModules(root, classes);

        // 6. Build statistics
        ProjectStatistics stats = computeStatistics(classes, endpoints, beanCount);

        // 7. Build risks
        List<String> risks = detectRisks(classes, graph);

        // 8. Build summary
        String projectName = root.getFileName().toString();
        String buildSystem = detectBuildSystem(configFiles);
        String framework = isSpringBoot ? "SPRING_BOOT" : "PLAIN_JAVA";

        ProjectSummary summary = ProjectSummary.builder()
                .projectName(projectName)
                .projectPath(root.toString())
                .buildSystem(buildSystem)
                .framework(framework)
                .modules(modules)
                .statistics(stats)
                .risks(risks)
                .build();

        // Cache for query API
        this.lastGraph = graph;
        this.lastSummary = summary;
        this.lastClasses = classes;
        this.lastEndpoints = endpoints;
        this.lastEntities = entities;
        this.lastAnalyzedPath = root;

        return summary;
    }

    public ProjectGraph getGraph() {
        return lastGraph;
    }

    public ProjectSummary getSummary() {
        return lastSummary;
    }

    public Path getLastAnalyzedPath() {
        return lastAnalyzedPath;
    }

    /**
     * Returns the cached class matching the simple name (first match).
     */
    public ProjectClass findClass(String simpleName) {
        if (lastGraph == null) return null;
        List<ProjectClass> matches = lastGraph.findByName(simpleName);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * Returns the cached controller that exposes the given endpoint path.
     */
    public ProjectEndpoint findController(String path) {
        if (lastEndpoints == null || path == null) return null;
        for (ProjectEndpoint e : lastEndpoints) {
            if (path.equals(e.path()) || e.path().contains(path)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns the cached entity matching the given simple name.
     */
    public ProjectEntity findEntity(String simpleName) {
        if (lastClasses == null) return null;
        for (ProjectClass c : lastClasses) {
            if (c.role() == Role.ENTITY && simpleName.equals(c.name())) {
                return ProjectEntity.builder()
                        .name(c.name())
                        .fullyQualifiedName(c.fullyQualifiedName())
                        .fields(c.fields())
                        .build();
            }
        }
        return null;
    }

    /**
     * Returns the impact analysis of modifying a class with the given
     * simple name.
     */
    public ProjectImpact impact(String simpleName) {
        if (lastGraph == null || lastClasses == null) {
            return ProjectImpact.builder()
                    .target(simpleName == null ? "" : simpleName)
                    .build();
        }
        // Find FQN
        String fqn = null;
        for (ProjectClass c : lastClasses) {
            if (simpleName.equals(c.name())) {
                fqn = c.fullyQualifiedName();
                break;
            }
        }
        if (fqn == null) {
            return ProjectImpact.builder().target(simpleName).build();
        }

        List<String> affected = lastGraph.impactOf(fqn);
        List<ProjectEndpoint> affectedEndpoints = new ArrayList<>();
        for (ProjectEndpoint e : lastEndpoints) {
            if (affected.contains(e.controllerClass())
                    || e.service() != null && affected.contains(e.service())
                    || e.repository() != null && affected.contains(e.repository())
                    || e.entity() != null && affected.contains(e.entity())) {
                affectedEndpoints.add(e);
            }
        }
        int depth = computeDepth(fqn, lastGraph);

        return ProjectImpact.builder()
                .target(simpleName)
                .affectedClasses(affected)
                .affectedEndpoints(affectedEndpoints)
                .dependencyDepth(depth)
                .build();
    }

    private int computeDepth(String fqn, ProjectGraph graph) {
        // BFS over incoming edges
        Set<String> visited = new HashSet<>();
        Set<String> current = new HashSet<>();
        current.add(fqn);
        int depth = 0;
        while (!current.isEmpty() && depth < 100) {
            Set<String> next = new HashSet<>();
            for (String node : current) {
                for (ProjectDependency edge : graph.incoming(node)) {
                    if (visited.add(edge.source())) {
                        next.add(edge.source());
                    }
                }
            }
            current = next;
            depth++;
        }
        return depth - 1;
    }

    private void linkEntitiesToRepositories(List<ProjectClass> classes, List<ProjectEntity> entities) {
        // The entities have already been extracted; nothing more needed.
        // The graph builder uses the @Entity role to wire MAPS_TO_ENTITY.
    }

    private List<ProjectModule> discoverModules(Path root, List<ProjectClass> classes) {
        // Derive modules from top-level package groups under com.shreeai.os.platform.*
        Set<String> topLevel = new java.util.TreeSet<>();
        for (ProjectClass c : classes) {
            String pkg = c.packageName();
            if (pkg.startsWith("com.shreeai.os.platform.")) {
                String[] parts = pkg.split("\\.");
                if (parts.length >= 4) {
                    topLevel.add(parts[3]);
                }
            } else if (pkg.contains(".")) {
                String[] parts = pkg.split("\\.");
                topLevel.add(parts[0]);
            }
        }
        List<ProjectModule> modules = new ArrayList<>();
        for (String name : topLevel) {
            List<String> subs = new ArrayList<>();
            for (ProjectClass c : classes) {
                if (c.packageName().contains("." + name + ".")) {
                    String[] parts = c.packageName().split("\\.");
                    if (parts.length >= 5) {
                        subs.add(parts[4]);
                    }
                }
            }
            modules.add(ProjectModule.of(name, ProjectModule.Kind.ROOT_PACKAGE,
                    "com.shreeai.os.platform." + name,
                    subs.stream().distinct().toList()));
        }
        return modules;
    }

    private ProjectStatistics computeStatistics(List<ProjectClass> classes,
                                               List<ProjectEndpoint> endpoints,
                                               int beanCount) {
        int classCount = 0, interfaceCount = 0, enumCount = 0, recordCount = 0;
        int controllerCount = 0, serviceCount = 0, repositoryCount = 0;
        int entityCount = 0, configurationCount = 0, methodCount = 0;

        for (ProjectClass c : classes) {
            switch (c.kind()) {
                case CLASS -> classCount++;
                case INTERFACE -> interfaceCount++;
                case ENUM -> enumCount++;
                case RECORD -> recordCount++;
            }
            switch (c.role()) {
                case CONTROLLER, REST_CONTROLLER -> controllerCount++;
                case SERVICE -> serviceCount++;
                case REPOSITORY -> repositoryCount++;
                case ENTITY -> entityCount++;
                case CONFIGURATION -> configurationCount++;
            }
            methodCount += c.methods().size();
        }

        return ProjectStatistics.builder()
                .classCount(classCount)
                .interfaceCount(interfaceCount)
                .enumCount(enumCount)
                .recordCount(recordCount)
                .controllerCount(controllerCount)
                .serviceCount(serviceCount)
                .repositoryCount(repositoryCount)
                .entityCount(entityCount)
                .configurationCount(configurationCount)
                .beanCount(beanCount)
                .endpointCount(endpoints.size())
                .methodCount(methodCount)
                .build();
    }

    private List<String> detectRisks(List<ProjectClass> classes, ProjectGraph graph) {
        List<String> risks = new ArrayList<>();
        // Cyclic dependencies
        var cycles = graph.detectCycles();
        if (!cycles.isEmpty()) {
            risks.add("Circular dependency detected (" + cycles.size() + " cycle(s))");
        }
        // Large controllers (more than 10 HTTP methods)
        for (ProjectClass c : classes) {
            if (c.role() == Role.CONTROLLER) {
                long httpMethods = c.methods().stream()
                        .filter(m -> m.httpMethod() != null)
                        .count();
                if (httpMethods > 10) {
                    risks.add("Large controller: " + c.name() + " has " + httpMethods + " endpoints");
                }
            }
        }
        // God classes (>50 methods)
        for (ProjectClass c : classes) {
            if (c.methods().size() > 50) {
                risks.add("God class: " + c.name() + " has " + c.methods().size() + " methods");
            }
        }
        return risks;
    }

    private String detectBuildSystem(List<Path> configFiles) {
        for (Path p : configFiles) {
            String name = p.getFileName().toString();
            if (name.equals("pom.xml")) return "MAVEN";
            if (name.startsWith("build.gradle")) return "GRADLE";
        }
        return "UNKNOWN";
    }

    // ─── Sprint-14: Getters for Developer Agent ───────────────────────────────

    /**
     * Returns the dependency graph from the most recent analysis.
     * Used by the Developer Agent to perform impact analysis.
     *
     * @return the last computed ProjectGraph, or null if no analysis has run
     */
    public ProjectGraph getLastGraph() {
        return lastGraph;
    }

    /**
     * Returns the list of all classes from the most recent analysis.
     *
     * @return list of ProjectClass, or empty list if no analysis has run
     */
    public List<ProjectClass> getLastClasses() {
        return lastClasses != null ? List.copyOf(lastClasses) : List.of();
    }

    /**
     * Returns the list of all endpoints from the most recent analysis.
     *
     * @return list of ProjectEndpoint, or empty list if no analysis has run
     */
    public List<ProjectEndpoint> getLastEndpoints() {
        return lastEndpoints != null ? List.copyOf(lastEndpoints) : List.of();
    }

    /**
     * Returns the list of all JPA entities from the most recent analysis.
     *
     * @return list of ProjectEntity, or empty list if no analysis has run
     */
    public List<ProjectEntity> getLastEntities() {
        return lastEntities != null ? List.copyOf(lastEntities) : List.of();
    }
}
