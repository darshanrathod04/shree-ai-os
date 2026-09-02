package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.kernels.developer.engine.DefaultDeveloperAgentEngine;
import com.shreeai.os.platform.kernels.developer.patch.DefaultPatchExecutionEngine;
import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.workflow.DefaultDeveloperWorkflowEngine;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperRequest;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ProjectSDK</b>
 *
 * <p>Developer-facing entry point for the Project Intelligence Kernel.
 * Provides project analysis, class discovery, and impact analysis.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * ShreeAI shree = ShreeAI.builder().apiKey("local").build();
 * ProjectSummary summary = shree.project().analyze("/path/to/project");
 * ProjectImpact impact = shree.project().impact("UserService");
 * </pre>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
public final class ProjectSDK {

    private final DefaultProjectIntelligenceEngine engine;

    ProjectSDK() {
        this.engine = new DefaultProjectIntelligenceEngine();
    }

    // ─── Sprint-16: Autonomous Developer Workflow ─────────────────────────────

    /**
     * Executes the complete autonomous developer workflow for the given
     * instruction and project path.
     *
     * <p>The workflow combines intent analysis, project intelligence,
     * impact analysis, code generation, validation, and test skeleton
     * generation into a single call. No files are written.</p>
     *
     * <p>Usage:</p>
     * <pre>
     * ShreeAI shree = ShreeAI.builder().apiKey("local").build();
     * DeveloperResult result = shree.project().build(
     *     "/workspace/demo",
     *     "Add JWT authentication with refresh tokens"
     * );
     * System.out.println(result.markdownSummary());
     * </pre>
     *
     * @param projectPath root directory of the project (must not be null)
     * @param instruction natural-language developer instruction (must not be null)
     * @return DeveloperResult with workflow, artifacts, test skeletons, and markdown summary
     * @throws IllegalArgumentException if projectPath or instruction is null
     * @since Sprint-16
     */
    public DeveloperResult build(String projectPath, String instruction) {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        Objects.requireNonNull(instruction, "instruction must not be null");
        DeveloperRequest request = new DeveloperRequest(projectPath, instruction, Map.of());
        return workflowEngine().execute(request);
    }

    /**
     * Executes the autonomous developer workflow with additional metadata.
     *
     * @param projectPath root directory of the project
     * @param instruction natural-language developer instruction
     * @param metadata    additional context passed to the workflow engine
     * @return DeveloperResult
     * @since Sprint-16
     */
    public DeveloperResult build(String projectPath, String instruction, Map<String, Object> metadata) {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        Objects.requireNonNull(instruction, "instruction must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        DeveloperRequest request = new DeveloperRequest(projectPath, instruction, metadata);
        return workflowEngine().execute(request);
    }

    /**
     * Returns the shared workflow engine instance.
     */
    private static DefaultDeveloperWorkflowEngine workflowEngine() {
        return new DefaultDeveloperWorkflowEngine();
    }

    // ─── Sprint-14: Developer Agent ───────────────────────────────────────────

    /**
     * Returns the Developer Agent engine for Sprint-14.
     *
     * <p>The Developer Agent provides deterministic, LLM-free implementation
     * planning: it analyzes a developer request, computes the impact on the
     * project graph, validates architecture, generates phased implementation
     * plans, and produces a testing checklist.</p>
     *
     * <p>Usage:</p>
     * <pre>
     * ShreeAI shree = ShreeAI.builder().apiKey("local").build();
     * DeveloperResponse response = shree.project().developerAgent().analyze(
     *     "Add JWT authentication to the login endpoint",
     *     "/path/to/project"
     * );
     * System.out.println(response.formattedPlan());
     * </pre>
     *
     * @return the Developer Agent engine
     * @since Sprint-14
     */
    public DefaultDeveloperAgentEngine developerAgent() {
        return new DefaultDeveloperAgentEngine();
    }

    /**
     * Analyzes a project at the given path and returns its summary.
     *
     * <p>The analysis is cached internally, enabling subsequent
     * {@link #findClass}, {@link #findController}, {@link #findEntity},
     * and {@link #impact} calls to operate on the same project.</p>
     *
     * @param projectPath root directory of the project (must not be null)
     * @return ProjectSummary with architecture, statistics, and risks
     * @throws IllegalArgumentException if projectPath is null
     * @throws java.io.IOException if the project cannot be read
     */
    public ProjectSummary analyze(String projectPath) throws java.io.IOException {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        return analyze(Path.of(projectPath));
    }

    /**
     * Analyzes a project at the given path and returns its summary.
     *
     * @param projectPath root directory of the project (must not be null)
     * @return ProjectSummary
     */
    public ProjectSummary analyze(Path projectPath) throws java.io.IOException {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        return engine.analyze(projectPath);
    }

    /**
     * Finds a class by simple name (first match) in the last analyzed project.
     *
     * <p>Requires {@link #analyze} to have been called first.</p>
     *
     * @param simpleName simple class name (e.g. "UserService")
     * @return ProjectClass or null if not found / no project analyzed
     */
    public ProjectClass findClass(String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName must not be null");
        return engine.findClass(simpleName);
    }

    /**
     * Finds the controller endpoint at the given path in the last analyzed project.
     *
     * @param path REST endpoint path (e.g. "/users")
     * @return ProjectEndpoint or null if not found
     */
    public ProjectEndpoint findController(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return engine.findController(path);
    }

    /**
     * Finds an entity by simple name in the last analyzed project.
     *
     * @param simpleName entity simple name (e.g. "User")
     * @return ProjectEntity or null if not found
     */
    public ProjectEntity findEntity(String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName must not be null");
        return engine.findEntity(simpleName);
    }

    /**
     * Analyzes the impact of modifying a class with the given simple name.
     * Returns affected files, endpoints, and dependency depth.
     *
     * <p>Requires {@link #analyze} to have been called first.</p>
     *
     * @param simpleName simple class name (e.g. "UserService")
     * @return ProjectImpact describing the change impact
     */
    public ProjectImpact impact(String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName must not be null");
        return engine.impact(simpleName);
    }

    // ─── Sprint-17: Autonomous Patch Application ──────────────────────────────

    /**
     * Executes the full autonomous patch application pipeline for the given
     * project and developer instruction. The pipeline:
     * <ol>
     *   <li>Analyzes the project</li>
     *   <li>Computes impact</li>
     *   <li>Generates patches</li>
     *   <li>Applies patches safely (in-memory)</li>
     *   <li>Validates compile (static check)</li>
     *   <li>Generates a rollback plan</li>
     *   <li>Returns a structured {@link DeveloperExecutionResult}</li>
     * </ol>
     *
     * <p>No files are written to disk by this method.</p>
     *
     * <p>Usage:</p>
     * <pre>
     * ShreeAI shree = ShreeAI.builder().apiKey("local").build();
     * DeveloperExecutionResult result = shree.project().apply(
     *     "/workspace/demo",
     *     "Add JWT authentication with refresh tokens"
     * );
     * </pre>
     *
     * @param projectPath root directory of the project (must not be null)
     * @param instruction natural-language developer instruction (must not be null)
     * @return DeveloperExecutionResult
     * @since Sprint-17
     */
    public DeveloperExecutionResult apply(String projectPath, String instruction) {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        Objects.requireNonNull(instruction, "instruction must not be null");
        return patchEngine().execute(projectPath, instruction);
    }

    /**
     * Returns the patch execution engine used by {@link #apply(String, String)}.
     * Lazily constructed.
     */
    private DefaultPatchExecutionEngine patchEngine() {
        return new DefaultPatchExecutionEngine();
    }
}
