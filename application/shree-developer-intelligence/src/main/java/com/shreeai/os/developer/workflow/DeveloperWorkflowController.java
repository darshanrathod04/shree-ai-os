package com.shreeai.os.developer.workflow;

import com.shreeai.os.platform.kernels.developer.patch.model.*;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.developer.review.ReviewController;
import com.shreeai.os.developer.workspace.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>DeveloperWorkflowController</b> - REST surface for Module 3: Developer Workflow.
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * POST /api/developer/workflow/build   - generate workflow (no file writes)
 * POST /api/developer/workflow/apply   - generate and apply patch in-memory
 * </pre>
 */
@RestController
@RequestMapping("/api/developer/workflow")
@CrossOrigin(origins = "*")
public class DeveloperWorkflowController {

    private final DeveloperWorkflowService workflowService;
    private final WorkspaceService workspaceService;
    private final ReviewController reviewController;

    public DeveloperWorkflowController(
            DeveloperWorkflowService workflowService,
            WorkspaceService workspaceService,
            ReviewController reviewController) {
        this.workflowService = workflowService;
        this.workspaceService = workspaceService;
        this.reviewController = reviewController;
    }

    /**
     * Generates a developer workflow.
     */
    @PostMapping("/build")
    public ResponseEntity<?> build(@RequestBody BuildRequest request) {
        if (request == null) {
            return bad("Request body required");
        }

        String projectPath = resolveProjectPath(request);
        if (projectPath == null) {
            return bad("sessionId or projectPath is required");
        }
        if (request.instruction() == null || request.instruction().isBlank()) {
            return bad("instruction is required");
        }

        try {
            DeveloperResult result = workflowService.build(projectPath, request.instruction());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("instruction", request.instruction());
            response.put("projectPath", projectPath);
            response.put("confidence", result.confidence());
            response.put("markdownSummary", result.markdownSummary());
            response.put("artifactCount", result.generatedArtifacts().size());
            response.put("testSkeletonCount", result.testSkeletons().size());
            response.put("totalSourceLines", result.totalSourceLines());
            response.put("artifacts", result.generatedArtifacts().stream()
                    .map(a -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("type", a.type().name());
                        m.put("path", a.path());
                        m.put("fileName", a.fileName());
                        m.put("lines", a.lineCount());
                        m.put("package", a.packageName());
                        m.put("preview", a.source().length() > 200
                                ? a.source().substring(0, 200) + "..."
                                : a.source());
                        return m;
                    }).toList());
            response.put("testSkeletons", result.testSkeletons().stream()
                    .map(t -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("classUnderTest", t.classUnderTest());
                        m.put("testClassName", t.testClassName());
                        m.put("testClassFqn", t.testClassFqn());
                        m.put("framework", t.framework().name());
                        m.put("category", t.category().name());
                        m.put("methodCount", t.methodSignatures().size());
                        m.put("methodSignatures", t.methodSignatures());
                        return m;
                    }).toList());
            response.put("completedAt", result.completedAt().toString());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Workflow failed: " + e.getMessage()));
        }
    }

    /**
     * Generates and applies a patch in-memory.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody BuildRequest request) {
        if (request == null) {
            return bad("Request body required");
        }

        String projectPath = resolveProjectPath(request);
        if (projectPath == null) {
            return bad("sessionId or projectPath is required");
        }
        if (request.instruction() == null || request.instruction().isBlank()) {
            return bad("instruction is required");
        }

        try {
            DeveloperExecutionResult result = workflowService.apply(projectPath, request.instruction());

            // Store result for Safe Apply Review (Module 4) so diffs/rollback are queryable
            reviewController.storeResult(result);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("executionId", result.executionId());
            response.put("status", result.status().name());
            response.put("isSuccess", result.isSuccess());
            response.put("confidence", result.confidence());
            response.put("appliedCount", result.appliedCount());
            response.put("totalPatches", result.appliedDiffs().size());
            response.put("executedAt", result.executedAt().toString());
            response.put("riskLevel", deriveRiskLevel(result));

            DeveloperExecutionResult.CompileReport compile = result.compileReport();
            if (compile != null) {
                response.put("compile", Map.of(
                        "status", compile.status().name(),
                        "filesCompiled", compile.filesCompiled(),
                        "errors", compile.errors(),
                        "warnings", compile.warnings(),
                        "diagnostics", compile.diagnostics()
                ));
            }

            RollbackPlan rollback = result.rollbackPlan();
            if (rollback != null) {
                List<Map<String, Object>> entries = new ArrayList<>();
                for (var e : rollback.entries()) {
                    Map<String, Object> entryMap = new LinkedHashMap<>();
                    entryMap.put("filePath", e.filePath());
                    entryMap.put("actionCount", e.actions().size());
                    entryMap.put("originalContent", e.originalContent());
                    entryMap.put("actions", e.actions().stream()
                            .map(a -> {
                                Map<String, Object> aMap = new LinkedHashMap<>();
                                aMap.put("type", a.type().name());
                                aMap.put("target", a.target());
                                aMap.put("description", a.description());
                                return aMap;
                            }).toList());
                    entries.add(entryMap);
                }

                Map<String, Object> rollbackMap = new LinkedHashMap<>();
                rollbackMap.put("planId", rollback.planId());
                rollbackMap.put("fileCount", rollback.fileCount());
                rollbackMap.put("totalActions", rollback.totalActions());
                rollbackMap.put("isEmpty", rollback.isEmpty());
                rollbackMap.put("entries", entries);
                response.put("rollback", rollbackMap);
            }

            List<Map<String, Object>> diffs = new ArrayList<>();
            for (var d : result.appliedDiffs()) {
                Map<String, Object> dMap = new LinkedHashMap<>();
                dMap.put("filePath", d.filePath());
                dMap.put("status", d.status().name());
                dMap.put("isSuccess", d.isSuccess());
                dMap.put("linesChanged", d.linesChanged());
                dMap.put("message", d.message());
                dMap.put("before", d.before());
                dMap.put("after", d.after());
                diffs.add(dMap);
            }
            response.put("diffs", diffs);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Apply failed: " + e.getMessage()));
        }
    }

    private String deriveRiskLevel(DeveloperExecutionResult result) {
        if (result.appliedDiffs().isEmpty()) return "NONE";
        int totalChanges = result.appliedDiffs().stream()
                .mapToInt(PatchDiff::linesChanged)
                .sum();
        if (totalChanges == 0) return "NONE";
        if (totalChanges < 20) return "LOW";
        if (totalChanges < 100) return "MEDIUM";
        return "HIGH";
    }

    private String resolveProjectPath(BuildRequest request) {
        if (request.projectPath() != null && !request.projectPath().isBlank()) {
            return request.projectPath();
        }
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            return workspaceService.getSession(request.sessionId())
                    .map(s -> s.projectPath())
                    .orElse(null);
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    public record BuildRequest(
            String sessionId,
            String projectPath,
            String instruction
    ) {}
}
