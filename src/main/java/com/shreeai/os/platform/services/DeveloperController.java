package com.shreeai.os.platform.services;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DeveloperController</b> — Unified REST surface for {@code shree-platform-web}
 * Developer Intelligence workspace ({@code /api/developer/*}).
 */
@RestController
@RequestMapping("/api/developer")
public class DeveloperController {

    private static final int MAX_INPUT_CHARS = 100_000;

    private final ShreeAI ai;
    private final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();
    private final Map<String, ProjectSummary> summaries = new ConcurrentHashMap<>();
    private final Map<String, DeveloperExecutionResult> executionResults = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> executions = new ConcurrentHashMap<>();

    public DeveloperController(ShreeAI ai) {
        this.ai = Objects.requireNonNull(ai, "ai must not be null");
    }

    @PostMapping("/workspace/open")
    public ResponseEntity<?> openWorkspace(@RequestBody Map<String, Object> body) {
        String rawPath = str(body, "path");
        if (rawPath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "path is required"));
        }
        try {
            Path validatedPath = resolveValidatedWorkspacePath(rawPath);
            ProjectSummary summary = ai.project().analyze(validatedPath);
            String id = "ws-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> session = new LinkedHashMap<>();
            session.put("id", id);
            session.put("projectPath", summary.projectPath());
            session.put("projectName", summary.projectName());
            session.put("analyzed", true);
            session.put("openedAt", Instant.now().toString());
            session.put("buildSystem", summary.buildSystem());
            session.put("framework", summary.framework());
            session.put("moduleCount", summary.modules().size());
            session.put("classCount", summary.statistics().classCount());
            session.put("endpointCount", summary.statistics().endpointCount());
            sessions.put(id, session);
            summaries.put(id, summary);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/workspace/sessions")
    public List<Map<String, Object>> listSessions() {
        return new ArrayList<>(sessions.values());
    }

    @GetMapping("/workspace/{id}")
    public ResponseEntity<?> getSession(@PathVariable String id) {
        Map<String, Object> session = sessions.get(id);
        return session == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(session);
    }

    @GetMapping("/workspace/{id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable String id) {
        ProjectSummary summary = summaries.get(id);
        return summary == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(summary.toMap());
    }

    @GetMapping("/workspace/{id}/class")
    public ResponseEntity<?> findClass(@PathVariable String id, @RequestParam String name) {
        if (!sessions.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        ProjectClass cls = ai.project().findClass(name);
        return cls == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(cls);
    }

    @GetMapping("/workspace/{id}/endpoint")
    public ResponseEntity<?> findEndpoint(@PathVariable String id, @RequestParam("p") String path) {
        if (!sessions.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        ProjectEndpoint ep = ai.project().findController(path);
        return ep == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(ep);
    }

    @GetMapping("/workspace/{id}/impact")
    public ResponseEntity<?> impact(@PathVariable String id, @RequestParam("n") String name) {
        if (!sessions.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        ProjectImpact impact = ai.project().impact(name);
        return impact == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(impact);
    }

    @DeleteMapping("/workspace/{id}")
    public ResponseEntity<Void> closeWorkspace(@PathVariable String id) {
        sessions.remove(id);
        summaries.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chat/ask")
    public ResponseEntity<?> chatAsk(@RequestBody Map<String, Object> body) {
        String sessionId = str(body, "sessionId");
        String question = str(body, "question");
        if (question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "question is required"));
        }
        if (question.length() > MAX_INPUT_CHARS) {
            return ResponseEntity.badRequest().body(Map.of("error", "question exceeds maximum length"));
        }
        SDKResponse response = ai.chat(question, Map.of("sessionId", sessionId));
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "question", question,
                "answer", response.answer() == null ? "" : response.answer(),
                "confidence", response.confidence(),
                "knowledgeUsed", true,
                "memoryUsed", true,
                "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/chat/remember")
    public ResponseEntity<?> chatRemember(@RequestBody Map<String, Object> body) {
        String sessionId = str(body, "sessionId");
        String title = str(body, "title");
        String content = str(body, "content");
        ai.memory().store(title, content);
        return ResponseEntity.ok(Map.of(
                "status", "stored",
                "sessionId", sessionId,
                "title", title
        ));
    }

    @PostMapping("/chat/recall")
    public ResponseEntity<?> chatRecall(@RequestBody Map<String, Object> body) {
        String query = str(body, "query");
        SDKResponse res = ai.memory().recall(query);
        return ResponseEntity.ok(Map.of(
                "answer", res.answer() == null ? "" : res.answer(),
                "confidence", res.confidence()
        ));
    }

    @PostMapping("/workflow/build")
    public ResponseEntity<?> workflowBuild(@RequestBody Map<String, Object> body) {
        String rawPath = str(body, "projectPath");
        String instruction = str(body, "instruction");
        if (rawPath.isBlank() || instruction.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "projectPath and instruction are required"));
        }
        try {
            Path validatedPath = resolveValidatedWorkspacePath(rawPath);
            var result = ai.project().build(validatedPath.toString(), instruction);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/workflow/apply")
    public ResponseEntity<?> workflowApply(@RequestBody Map<String, Object> body) {
        String rawPath = str(body, "projectPath");
        String instruction = str(body, "instruction");
        if (rawPath.isBlank() || instruction.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "projectPath and instruction are required"));
        }
        try {
            Path validatedPath = resolveValidatedWorkspacePath(rawPath);
            DeveloperExecutionResult result = ai.project().apply(validatedPath.toString(), instruction);
            return ResponseEntity.ok(recordExecution(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Records a {@link DeveloperExecutionResult} and maps it to the REST payload
     * expected by {@code shree-platform-web}.
     */
    public Map<String, Object> recordExecution(DeveloperExecutionResult result) {
        String execId = result.executionId();
        executionResults.put(execId, result);

        List<Map<String, Object>> diffs = mapDiffs(result.appliedDiffs());
        Map<String, Object> rollback = mapRollback(execId, result.rollbackPlan());
        Map<String, Object> compile = mapCompile(result.compileReport());

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("executionId", execId);
        record.put("status", result.status().name());
        record.put("isSuccess", result.isSuccess());
        record.put("confidence", result.confidence());
        record.put("appliedCount", result.appliedCount());
        record.put("totalPatches", result.appliedDiffs().size());
        record.put("riskLevel", result.appliedDiffs().isEmpty() ? "NONE" : "LOW");
        record.put("executedAt", result.executedAt().toString());
        record.put("compile", compile);
        record.put("rollback", rollback);
        record.put("diffs", diffs);
        executions.put(execId, record);
        return record;
    }

    @GetMapping("/review/executions")
    public List<Map<String, Object>> listExecutions() {
        return new ArrayList<>(executions.values());
    }

    @GetMapping("/review/{executionId}/diffs")
    public ResponseEntity<?> getDiffs(@PathVariable String executionId) {
        DeveloperExecutionResult result = executionResults.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        List<Map<String, Object>> diffs = mapDiffs(result.appliedDiffs());
        return ResponseEntity.ok(Map.of(
                "executionId", executionId,
                "totalDiffs", diffs.size(),
                "diffs", diffs
        ));
    }

    @GetMapping("/review/{executionId}/diff/{filePath}")
    public ResponseEntity<?> getDiff(@PathVariable String executionId, @PathVariable String filePath) {
        DeveloperExecutionResult result = executionResults.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return mapDiffs(result.appliedDiffs()).stream()
                .filter(d -> filePath.equals(d.get("filePath")) || String.valueOf(d.get("filePath")).endsWith(filePath))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/review/{executionId}/rollback")
    public ResponseEntity<?> getRollback(@PathVariable String executionId) {
        DeveloperExecutionResult result = executionResults.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapRollback(executionId, result.rollbackPlan()));
    }

    @PostMapping("/review/{executionId}/rollback/execute")
    public ResponseEntity<?> executeRollback(@PathVariable String executionId) {
        DeveloperExecutionResult result = executionResults.get(executionId);
        if (result == null || result.rollbackPlan() == null || result.rollbackPlan().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        int restored = 0;
        List<String> restoredFiles = new ArrayList<>();
        try {
            for (RollbackPlan.RollbackEntry entry : result.rollbackPlan().entries()) {
                Path target = Path.of(entry.filePath()).toAbsolutePath().normalize();
                boolean removeFile = entry.actions().stream()
                        .anyMatch(a -> a.type() == RollbackPlan.UndoType.REMOVE_FILE);
                if (removeFile) {
                    if (Files.deleteIfExists(target)) {
                        restored++;
                        restoredFiles.add(entry.filePath());
                    }
                } else {
                    if (target.getParent() != null) {
                        Files.createDirectories(target.getParent());
                    }
                    Files.writeString(target, entry.originalContent());
                    restored++;
                    restoredFiles.add(entry.filePath());
                }
            }
            return ResponseEntity.ok(Map.of(
                    "executionId", executionId,
                    "status", "ROLLED_BACK",
                    "restoredCount", restored,
                    "restoredFiles", restoredFiles
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private static List<Map<String, Object>> mapDiffs(List<PatchDiff> diffs) {
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (PatchDiff d : diffs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("filePath", d.filePath());
            item.put("status", d.status().name());
            item.put("isSuccess", d.isSuccess());
            item.put("linesChanged", d.linesChanged());
            item.put("message", d.message());
            item.put("appliedAt", d.appliedAt().toString());
            item.put("before", d.before());
            item.put("after", d.after());
            mapped.add(item);
        }
        return mapped;
    }

    private static Map<String, Object> mapRollback(String executionId, RollbackPlan plan) {
        if (plan == null) {
            return Map.of(
                    "executionId", executionId,
                    "hasRollback", false,
                    "planId", executionId,
                    "fileCount", 0,
                    "totalActions", 0,
                    "isEmpty", true,
                    "entries", List.of()
            );
        }
        List<Map<String, Object>> entries = new ArrayList<>();
        for (RollbackPlan.RollbackEntry entry : plan.entries()) {
            List<Map<String, Object>> actions = new ArrayList<>();
            for (RollbackPlan.UndoAction action : entry.actions()) {
                actions.add(Map.of(
                        "type", action.type().name(),
                        "description", action.description(),
                        "target", action.target()
                ));
            }
            entries.add(Map.of(
                    "filePath", entry.filePath(),
                    "originalContent", entry.originalContent(),
                    "actionCount", actions.size(),
                    "actions", actions
            ));
        }
        return Map.of(
                "executionId", executionId,
                "hasRollback", !plan.isEmpty(),
                "planId", plan.planId(),
                "fileCount", plan.fileCount(),
                "totalActions", plan.totalActions(),
                "isEmpty", plan.isEmpty(),
                "entries", entries
        );
    }

    private static Map<String, Object> mapCompile(DeveloperExecutionResult.CompileReport report) {
        if (report == null) {
            return Map.of(
                    "status", "SKIPPED",
                    "filesCompiled", 0,
                    "errors", 0,
                    "warnings", 0,
                    "diagnostics", List.of()
            );
        }
        return Map.of(
                "status", report.status().name(),
                "filesCompiled", report.filesCompiled(),
                "errors", report.errors(),
                "warnings", report.warnings(),
                "diagnostics", report.diagnostics()
        );
    }

    private static Path resolveValidatedWorkspacePath(String rawPath) {
        if (rawPath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Invalid path");
        }
        Path resolved = Path.of(rawPath).toAbsolutePath().normalize();
        String allowedRootProp = System.getProperty("shree.workspace.allowed-root",
                System.getenv("SHREE_WORKSPACE_ALLOWED_ROOT"));
        if (allowedRootProp != null && !allowedRootProp.isBlank()) {
            Path allowedRoot = Path.of(allowedRootProp).toAbsolutePath().normalize();
            if (!resolved.startsWith(allowedRoot)) {
                throw new SecurityException("Workspace path escapes configured allowed root");
            }
        }
        if (!Files.exists(resolved) || !Files.isDirectory(resolved)) {
            throw new IllegalArgumentException("Workspace path must be an existing directory: " + resolved);
        }
        return resolved;
    }

    private static String str(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return String.valueOf(body.get(key));
    }
}
