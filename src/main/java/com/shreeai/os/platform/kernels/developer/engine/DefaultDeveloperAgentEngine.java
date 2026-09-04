package com.shreeai.os.platform.kernels.developer.engine;

import com.shreeai.os.platform.kernels.developer.analyzer.*;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.DefaultCodeGenerationEngine;
import com.shreeai.os.platform.kernels.developer.codegen.model.CodeGenerationResult;
import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.project.model.*;
import com.shreeai.os.platform.kernels.response.model.DeveloperResponse;

import java.nio.file.Path;
import java.util.*;

/**
 * <b>DefaultDeveloperAgentEngine</b>
 *
 * <p>The top-level orchestrator for the Developer Agent (Sprint-14).
 * Coordinates the full analysis pipeline.</p>
 *
 * <p><b>Production rules:</b></p>
 * <ul>
 *   <li>DO NOT modify any project files</li>
 *   <li>DO NOT generate Java source code</li>
 *   <li>DO NOT call any LLM</li>
 *   <li>All analysis is deterministic and offline-capable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class DefaultDeveloperAgentEngine {

    private final DeveloperIntentAnalyzer intentAnalyzer;
    private final DefaultProjectIntelligenceEngine projectEngine;
    private final DefaultCodeGenerationEngine codeGenEngine;
    private final Map<String, ProjectSummary> projectCache;
    private final Map<String, ProjectGraph> graphCache;
    /** Sprint-15: cached classes from the last analyze/analyzeWithCode call. */
    private volatile List<ProjectClass> lastClasses = List.of();

    public DefaultDeveloperAgentEngine() {
        this(new DefaultCodeGenerationEngine());
    }

    public DefaultDeveloperAgentEngine(DefaultCodeGenerationEngine codeGenEngine) {
        this.intentAnalyzer = new DeveloperIntentAnalyzer();
        this.projectEngine = new DefaultProjectIntelligenceEngine();
        this.codeGenEngine = codeGenEngine == null ? new DefaultCodeGenerationEngine() : codeGenEngine;
        this.projectCache = new LinkedHashMap<>();
        this.graphCache = new LinkedHashMap<>();
    }

    /**
     * Analyzes the developer request and returns a structured DeveloperResponse.
     *
     * @param request     the developer request string
     * @param projectPath optional path to the project root (null to skip project analysis)
     * @return DeveloperResponse with all analysis components
     */
    public DeveloperResponse analyze(String request, String projectPath) {
        Objects.requireNonNull(request, "request must not be null");

        DeveloperIntent intent = intentAnalyzer.analyze(request);

        ProjectSummary summary = null;
        ProjectGraph graph = null;
        List<ProjectClass> classes = List.of();
        List<ProjectEndpoint> endpoints = List.of();
        List<ProjectEntity> entities = List.of();

        if (projectPath != null && !projectPath.isBlank()) {
            String cacheKey = projectPath.trim();
            if (graphCache.containsKey(cacheKey)) {
                graph = graphCache.get(cacheKey);
                summary = projectCache.get(cacheKey);
            } else {
                try {
                    summary = projectEngine.analyze(Path.of(cacheKey));
                    graph = summary != null ? projectEngine.getLastGraph() : null;
                    if (graph != null) {
                        graphCache.put(cacheKey, graph);
                        projectCache.put(cacheKey, summary);
                    }
                } catch (java.io.IOException e) {
                    // Project analysis failed — continue without it
                }
            }
            if (summary != null) {
                classes = projectEngine.getLastClasses();
                endpoints = projectEngine.getLastEndpoints();
                entities = projectEngine.getLastEntities();
            }
        }

        ImpactReport impact = buildImpact(intent, graph, classes, endpoints, entities);
        List<ValidationIssue> issues = validate(graph, classes, endpoints, intent, impact);
        ImplementationPlan plan = plan(intent, classes, impact, issues);
        TestStrategyGenerator.TestStrategy testStrategy = generateTests(intent, impact, plan);
        double confidence = computeOverallConfidence(intent, impact, issues);

        // Sprint-15: cache for downstream code generation
        this.lastClasses = List.copyOf(classes);

        return DeveloperResponse.builder()
                .request(request)
                .intent(intent)
                .impact(impact)
                .validationIssues(issues)
                .plan(plan)
                .testStrategy(testStrategy)
                .confidence(confidence)
                .timestamp(java.time.Instant.now())
                .build();
    }

    public DeveloperResponse analyze(String request) {
        return analyze(request, null);
    }

    /**
     * <b>Sprint-15:</b> Analyzes the developer request AND generates a complete
     * {@link CodeGenerationResult} (patch plan, source code, validation, test
     * skeletons). Never writes any files.
     *
     * @param request     the developer request
     * @param projectPath optional project root path (null to skip project analysis)
     * @return a DeveloperResponse with {@code codeGeneration} populated
     * @since Sprint-15
     */
    public DeveloperResponse analyzeWithCode(String request, String projectPath) {
        DeveloperResponse base = analyze(request, projectPath);
        if (base.intent() == null) {
            return base;
        }
        CodeGenerationResult codeGen = codeGenEngine.generate(
                base.intent(), base.impact(), lastClasses);
        return DeveloperResponse.builder()
                .request(base.request())
                .intent(base.intent())
                .impact(base.impact())
                .validationIssues(base.validationIssues())
                .plan(base.plan())
                .testStrategy(base.testStrategy())
                .confidence(base.confidence())
                .timestamp(base.timestamp())
                .codeGeneration(codeGen)
                .build();
    }

    /**
     * <b>Sprint-15:</b> Same as {@link #analyzeWithCode(String, String)} but
     * without a project path.
     * @since Sprint-15
     */
    public DeveloperResponse analyzeWithCode(String request) {
        return analyzeWithCode(request, null);
    }

    private ImpactReport buildImpact(DeveloperIntent intent,
                                      ProjectGraph graph,
                                      List<ProjectClass> classes,
                                      List<ProjectEndpoint> endpoints,
                                      List<ProjectEntity> entities) {
        if (graph == null || graph.size() == 0) {
            return ImpactReport.builder()
                    .targetClass(intent.entity())
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
        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph, classes, endpoints, entities);
        return analyzer.analyze(intent);
    }

    private List<ValidationIssue> validate(ProjectGraph graph,
                                             List<ProjectClass> classes,
                                             List<ProjectEndpoint> endpoints,
                                             DeveloperIntent intent,
                                             ImpactReport impact) {
        if (graph == null || graph.size() == 0) return List.of();
        ArchitectureValidator validator = new ArchitectureValidator(graph, classes, endpoints);
        List<ValidationIssue> existing = validator.validate();
        List<ValidationIssue> changeIssues = validator.validateChange(intent, impact);
        List<ValidationIssue> all = new ArrayList<>(existing);
        for (ValidationIssue issue : changeIssues) {
            if (all.stream().noneMatch(i -> i.message().equals(issue.message()))) {
                all.add(issue);
            }
        }
        return all;
    }

    private ImplementationPlan plan(DeveloperIntent intent,
                                     List<ProjectClass> classes,
                                     ImpactReport impact,
                                     List<ValidationIssue> issues) {
        ImplementationPlanner planner = new ImplementationPlanner(classes);
        return planner.plan(intent, impact, issues);
    }

    private TestStrategyGenerator.TestStrategy generateTests(DeveloperIntent intent,
                                                               ImpactReport impact,
                                                               ImplementationPlan plan) {
        return new TestStrategyGenerator().generate(intent, impact, plan);
    }

    private double computeOverallConfidence(DeveloperIntent intent,
                                             ImpactReport impact,
                                             List<ValidationIssue> issues) {
        double base = intent.confidence();
        long highSeverity = issues.stream()
                .filter(i -> i.severity() == ValidationIssue.Severity.HIGH)
                .count();
        if (highSeverity > 0) base -= 0.1 * highSeverity;
        if (impact.totalAffected() > 15) base -= 0.05;
        return Math.max(0.1, Math.min(0.95, base));
    }

    public void clearCache() {
        projectCache.clear();
        graphCache.clear();
    }

    public int cacheSize() {
        return projectCache.size();
    }
}