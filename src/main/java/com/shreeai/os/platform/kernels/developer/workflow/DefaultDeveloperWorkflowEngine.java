package com.shreeai.os.platform.kernels.developer.workflow;

import com.shreeai.os.platform.kernels.developer.analyzer.DeveloperIntentAnalyzer;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.DefaultCodeGenerationEngine;
import com.shreeai.os.platform.kernels.developer.codegen.JavaCodeGenerator;
import com.shreeai.os.platform.kernels.developer.codegen.PatchPlanner;
import com.shreeai.os.platform.kernels.developer.codegen.PatchValidator;
import com.shreeai.os.platform.kernels.developer.codegen.TestSkeletonGenerator;
import com.shreeai.os.platform.kernels.developer.codegen.model.CodeGenerationResult;
import com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchPlan;
import com.shreeai.os.platform.kernels.developer.codegen.model.TestSkeleton;
import com.shreeai.os.platform.kernels.developer.codegen.model.ValidationResult;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperRequest;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperWorkflow;
import com.shreeai.os.platform.kernels.developer.workflow.model.GeneratedArtifact;
import com.shreeai.os.platform.kernels.developer.workflow.model.WorkflowImpactReport;
import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectGraph;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultDeveloperWorkflowEngine</b>
 *
 * <p>The top-level orchestrator for the Sprint-16 Autonomous Developer
 * Workflow Engine. Coordinates the full pipeline:</p>
 *
 * <pre>
 * DeveloperRequest
 *   ▼ IntentAnalyzer          (DeveloperIntentAnalyzer)
 *   ▼ ProjectIntelligence     (DefaultProjectIntelligenceEngine)
 *   ▼ ImpactAnalyzer          (ImpactIntelligenceEngine)
 *   ▼ PatchPlanner            (PatchPlanner)
 *   ▼ JavaCodeGenerator       (JavaCodeGenerator)
 *   ▼ PatchValidator          (PatchValidator)
 *   ▼ TestSkeletonGenerator   (TestSkeletonGenerator)
 *   ▼ MarkdownSummaryRenderer (MarkdownSummaryRenderer)
 *   ▶ DeveloperResult
 * </pre>
 *
 * <p><b>Production rules:</b></p>
 * <ul>
 *   <li>DO NOT modify any project files</li>
 *   <li>DO NOT write any generated code to disk</li>
 *   <li>DO NOT call any LLM</li>
 *   <li>All analysis is deterministic and offline-capable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class DefaultDeveloperWorkflowEngine {

    private final DeveloperIntentAnalyzer intentAnalyzer;
    private final DefaultProjectIntelligenceEngine projectEngine;
    private final ImpactIntelligenceEngine impactEngine;
    private final PatchPlanner patchPlanner;
    private final JavaCodeGenerator codeGenerator;
    private final PatchValidator patchValidator;
    private final TestSkeletonGenerator testGenerator;
    private final MarkdownSummaryRenderer markdownRenderer;

    public DefaultDeveloperWorkflowEngine() {
        this(
                new DeveloperIntentAnalyzer(),
                new DefaultProjectIntelligenceEngine(),
                new ImpactIntelligenceEngine(),
                new PatchPlanner(),
                new JavaCodeGenerator(),
                new PatchValidator(),
                new TestSkeletonGenerator(),
                new MarkdownSummaryRenderer()
        );
    }

    public DefaultDeveloperWorkflowEngine(DeveloperIntentAnalyzer intentAnalyzer,
                                          DefaultProjectIntelligenceEngine projectEngine,
                                          ImpactIntelligenceEngine impactEngine,
                                          PatchPlanner patchPlanner,
                                          JavaCodeGenerator codeGenerator,
                                          PatchValidator patchValidator,
                                          TestSkeletonGenerator testGenerator,
                                          MarkdownSummaryRenderer markdownRenderer) {
        this.intentAnalyzer = Objects.requireNonNull(intentAnalyzer, "intentAnalyzer");
        this.projectEngine = Objects.requireNonNull(projectEngine, "projectEngine");
        this.impactEngine = Objects.requireNonNull(impactEngine, "impactEngine");
        this.patchPlanner = Objects.requireNonNull(patchPlanner, "patchPlanner");
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "codeGenerator");
        this.patchValidator = Objects.requireNonNull(patchValidator, "patchValidator");
        this.testGenerator = Objects.requireNonNull(testGenerator, "testGenerator");
        this.markdownRenderer = Objects.requireNonNull(markdownRenderer, "markdownRenderer");
    }

    /**
     * Executes the complete autonomous developer workflow pipeline for the given request.
     *
     * <p>The pipeline:</p>
     * <ol>
     *   <li>Analyzes the developer intent from the instruction string</li>
     *   <li>Analyzes the project (if a valid project path is provided)</li>
     *   <li>Computes the impact analysis using deterministic rules</li>
     *   <li>Generates a patch plan for the code changes</li>
     *   <li>Renders the Java source code patches</li>
     *   <li>Validates the patches for correctness</li>
     *   <li>Generates test skeletons</li>
     *   <li>Produces a markdown summary</li>
     * </ol>
     *
     * <p>All output is in-memory. No files are written.</p>
     *
     * @param request the developer request (project path + instruction)
     * @return a non-null DeveloperResult with all workflow outputs
     * @throws IllegalArgumentException if request is null
     */
    public DeveloperResult execute(DeveloperRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // ── Step 1: Analyze Intent ────────────────────────────────────────────────
        DeveloperIntent intent = intentAnalyzer.analyze(request.instruction());

        // ── Step 2: Analyze Project ───────────────────────────────────────────────
        ProjectSummary projectSummary = analyzeProject(request.projectPath());

        // ── Step 3: Compute Impact ────────────────────────────────────────────────
        ProjectGraph graph = projectEngine.getLastGraph();
        List<ProjectClass> classes = projectEngine.getLastClasses();
        WorkflowImpactReport workflowImpact = impactEngine.compute(intent, graph, classes);

        // ── Step 4: Generate Patch Plan ──────────────────────────────────────────
        PatchPlan patchPlan = patchPlanner.plan(intent, null, classes);

        // ── Step 5: Generate Java Code ────────────────────────────────────────────
        List<GeneratedPatch> generatedPatches = codeGenerator.generatePatches(patchPlan);

        // ── Step 6: Validate ──────────────────────────────────────────────────────
        ValidationResult validation = patchValidator.validate(patchPlan);

        // ── Step 7: Generate Test Skeletons ──────────────────────────────────────
        List<TestSkeleton> testSkeletons = testGenerator.generate(intent, patchPlan, classes);

        // ── Step 8: Compute Overall Confidence ───────────────────────────────────
        double confidence = computeConfidence(intent, validation, generatedPatches.size());

        // ── Step 9: Build Artifacts ──────────────────────────────────────────────
        List<GeneratedArtifact> artifacts = buildArtifacts(generatedPatches, testSkeletons);

        // ── Step 10: Render Markdown Summary ─────────────────────────────────────
        String markdown = markdownRenderer.render(
                buildWorkflow(intent, projectSummary, workflowImpact, patchPlan, validation),
                buildCodeGenResult(intent, patchPlan, generatedPatches, validation, testSkeletons),
                request.instruction(),
                confidence
        );

        // ── Step 11: Build Final Result ──────────────────────────────────────────
        return DeveloperResult.builder()
                .workflow(buildWorkflow(intent, projectSummary, workflowImpact, patchPlan, validation))
                .generatedArtifacts(artifacts)
                .testSkeletons(testSkeletons)
                .confidence(confidence)
                .markdownSummary(markdown)
                .completedAt(Instant.now())
                .build();
    }

    /**
     * Analyzes the project at the given path. Returns null if the path is invalid
     * or the analysis fails.
     */
    private ProjectSummary analyzeProject(String projectPath) {
        if (projectPath == null || projectPath.isBlank()) return null;
        try {
            Path path = Path.of(projectPath.trim());
            return projectEngine.analyze(path);
        } catch (Exception e) {
            // Project analysis failed — continue without it
            return null;
        }
    }

    /**
     * Builds the DeveloperWorkflow from its components.
     */
    private DeveloperWorkflow buildWorkflow(DeveloperIntent intent,
                                            ProjectSummary projectSummary,
                                            WorkflowImpactReport workflowImpact,
                                            PatchPlan patchPlan,
                                            ValidationResult validation) {
        return DeveloperWorkflow.builder()
                .intent(intent)
                .projectSummary(projectSummary)
                .workflowImpact(workflowImpact)
                .patchPlan(patchPlan)
                .validationResult(validation)
                .executedAt(Instant.now())
                .build();
    }

    /**
     * Builds a CodeGenerationResult from generated patches (for the markdown renderer).
     */
    private CodeGenerationResult buildCodeGenResult(DeveloperIntent intent,
                                                    PatchPlan patchPlan,
                                                    List<GeneratedPatch> generatedPatches,
                                                    ValidationResult validation,
                                                    List<TestSkeleton> testSkeletons) {
        return CodeGenerationResult.builder()
                .request(intent.originalRequest())
                .intent(intent.intent().name())
                .entity(intent.entity())
                .patchPlan(patchPlan)
                .generatedPatches(generatedPatches)
                .validation(validation)
                .testSkeletons(testSkeletons)
                .confidence(intent.confidence())
                .generatedAt(Instant.now())
                .build();
    }

    /**
     * Converts generated patches and test skeletons into GeneratedArtifact objects.
     */
    private List<GeneratedArtifact> buildArtifacts(List<GeneratedPatch> patches,
                                                 List<TestSkeleton> testSkeletons) {
        List<GeneratedArtifact> artifacts = new ArrayList<>();

        // Convert patches
        for (GeneratedPatch patch : patches) {
            GeneratedArtifact.Type type = inferArtifactType(patch.targetFile());
            artifacts.add(GeneratedArtifact.builder()
                    .path(patch.targetFile())
                    .type(type)
                    .source(patch.source())
                    .packageName(extractPackage(patch.targetFile()))
                    .build());
        }

        // Convert test skeletons to artifacts
        for (TestSkeleton skeleton : testSkeletons) {
            String testPath = skeleton.testFilePath();
            if (testPath == null || testPath.isEmpty()) {
                // Synthesize a path from the class FQN
                String fqn = skeleton.testClassFqn();
                testPath = fqn.replace('.', '/') + ".java";
            }
            artifacts.add(GeneratedArtifact.builder()
                    .path(testPath)
                    .type(GeneratedArtifact.Type.TEST)
                    .source("")
                    .packageName(extractPackage(testPath))
                    .build());
        }

        return artifacts;
    }

    /**
     * Infers the artifact type from the file path.
     */
    private GeneratedArtifact.Type inferArtifactType(String filePath) {
        if (filePath == null) return GeneratedArtifact.Type.JAVA;
        String lower = filePath.toLowerCase();
        if (lower.contains("/test/") || lower.contains("\\test\\")
                || lower.contains("test.java") || lower.endsWith("test.java")) {
            return GeneratedArtifact.Type.TEST;
        }
        if (lower.contains("/resources/") || lower.contains("\\resources\\")
                || lower.endsWith(".properties") || lower.endsWith(".xml")
                || lower.endsWith(".yml") || lower.endsWith(".yaml")) {
            return GeneratedArtifact.Type.CONFIG;
        }
        return GeneratedArtifact.Type.JAVA;
    }

    /**
     * Extracts the package name from a file path.
     */
    private String extractPackage(String filePath) {
        if (filePath == null) return "";
        // e.g. "src/main/java/com/example/UserService.java" → "com.example"
        int srcIdx = Math.max(
                filePath.indexOf("src/main/java/"),
                filePath.indexOf("src/test/java/"));
        if (srcIdx >= 0) {
            String remaining = filePath.substring(srcIdx + "src/main/java/".length());
            int lastSlash = Math.max(remaining.lastIndexOf('/'), remaining.lastIndexOf('\\'));
            if (lastSlash > 0) {
                return remaining.substring(0, lastSlash).replace('/', '.');
            }
        }
        return "";
    }

    /**
     * Computes the overall confidence score.
     */
    private double computeConfidence(DeveloperIntent intent,
                                     ValidationResult validation,
                                     int patchCount) {
        double base = intent.confidence();
        if (validation != null) {
            switch (validation.overallStatus()) {
                case INVALID -> base -= 0.25;
                case WARNING -> base -= 0.10;
                case SAFE -> { /* no penalty */ }
            }
        }
        if (patchCount == 0) base -= 0.20;
        if (patchCount > 10) base -= 0.05;
        return Math.max(0.1, Math.min(0.95, base));
    }
}
