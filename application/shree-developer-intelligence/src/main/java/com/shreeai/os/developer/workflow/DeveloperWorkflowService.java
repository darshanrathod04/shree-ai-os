package com.shreeai.os.developer.workflow;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.platform.sdk.ProjectSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <b>DeveloperWorkflowService</b>
 *
 * <p>Module 3: Autonomous developer workflow orchestration.
 * Wraps {@code ProjectSDK.build()} and {@code ProjectSDK.apply()} with
 * session validation and error handling.</p>
 *
 * <p><b>SDK Composition:</b></p>
 * <pre>
 * build(path, instruction)  →  ProjectSDK.build(path, instruction) → DeveloperResult
 * apply(path, instruction)  →  ProjectSDK.apply(path, instruction) → DeveloperExecutionResult
 * </pre>
 *
 * <p><b>Application Layer Rule:</b> Uses only {@code ProjectSDK}.
 * No kernel imports.</p>
 *
 * @since Phase 2
 */
@Service
public class DeveloperWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(DeveloperWorkflowService.class);

    private final ProjectSDK projectSdk;

    public DeveloperWorkflowService(ProjectSDK projectSdk) {
        this.projectSdk = Objects.requireNonNull(projectSdk, "projectSdk");
    }

    /**
     * Executes the autonomous developer workflow.
     *
     * <p>Pipeline:</p>
     * <ol>
     *   <li>Intent analysis</li>
     *   <li>Project intelligence</li>
     *   <li>Impact analysis</li>
     *   <li>Code generation</li>
     *   <li>Validation</li>
     *   <li>Test skeleton generation</li>
     *   <li>Markdown summary</li>
     * </ol>
     *
     * <p>No files are written to disk.</p>
     *
     * @param projectPath root directory of the project (must not be null)
     * @param instruction natural-language developer instruction (must not be null)
     * @return DeveloperResult with artifacts, test skeletons, and markdown summary
     */
    public DeveloperResult build(String projectPath, String instruction) {
        Objects.requireNonNull(projectPath, "projectPath");
        Objects.requireNonNull(instruction, "instruction");

        log.info("Developer workflow build [path={}]: {}", projectPath, truncate(instruction, 60));
        DeveloperResult result = projectSdk.build(projectPath, instruction);
        log.info("Developer workflow complete: confidence={}, artifacts={}",
                result.confidence(), result.generatedArtifacts().size());
        return result;
    }

    /**
     * Executes the full autonomous patch application pipeline.
     *
     * <p>Pipeline:</p>
     * <ol>
     *   <li>Project analysis</li>
     *   <li>Impact computation</li>
     *   <li>Patch generation</li>
     *   <li>In-memory patch application</li>
     *   <li>Compile validation (static check)</li>
     *   <li>Rollback plan generation</li>
     *   <li>DeveloperExecutionResult</li>
     * </ol>
     *
     * <p>No files are written to disk. The result carries diffs and rollback
     * metadata for the UI to present.</p>
     *
     * @param projectPath root directory of the project (must not be null)
     * @param instruction natural-language developer instruction (must not be null)
     * @return DeveloperExecutionResult with applied diffs and rollback plan
     */
    public DeveloperExecutionResult apply(String projectPath, String instruction) {
        Objects.requireNonNull(projectPath, "projectPath");
        Objects.requireNonNull(instruction, "instruction");

        log.info("Developer apply [path={}]: {}", projectPath, truncate(instruction, 60));
        DeveloperExecutionResult result = projectSdk.apply(projectPath, instruction);
        log.info("Developer apply complete: status={}, applied={}/{}, rollback={}",
                result.status(), result.appliedCount(), result.appliedDiffs().size(),
                result.rollbackPlan() != null ? result.rollbackPlan().totalActions() : 0);
        return result;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
