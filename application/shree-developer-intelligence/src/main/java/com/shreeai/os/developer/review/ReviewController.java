package com.shreeai.os.developer.review;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <b>ReviewController</b> - REST surface for Module 4: Safe Apply.
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * GET  /api/developer/review/{executionId}/diffs     - get all diffs for an execution
 * GET  /api/developer/review/{executionId}/diff/{f}  - get single diff by file path
 * GET  /api/developer/review/{executionId}/rollback  - get rollback plan
 * GET  /api/developer/review/executions              - list all stored executions
 * </pre>
 *
 * <p>The diffs and rollback plan are returned from the last {@code apply()}
 * result, stored in-memory keyed by execution ID.</p>
 */
@RestController
@RequestMapping("/api/developer/review")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final Map<String, DeveloperExecutionResult> executions = new LinkedHashMap<>();

    public ReviewController() {}

    /**
     * Stores an execution result for later review.
     */
    public void storeResult(DeveloperExecutionResult result) {
        executions.put(result.executionId(), result);
    }

    /**
     * Returns all diffs for an execution.
     */
    @GetMapping("/{executionId}/diffs")
    public ResponseEntity<Map<String, Object>> getDiffs(@PathVariable String executionId) {
        DeveloperExecutionResult result = executions.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        List<Map<String, Object>> diffs = new ArrayList<>();
        for (PatchDiff d : result.appliedDiffs()) {
            diffs.add(toDiffMap(d));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionId", executionId);
        body.put("totalDiffs", diffs.size());
        body.put("diffs", diffs);
        return ResponseEntity.ok(body);
    }

    /**
     * Returns a single diff by file path (URL-encoded).
     */
    @GetMapping("/{executionId}/diff/{filePath:.+}")
    public ResponseEntity<Map<String, Object>> getDiff(
            @PathVariable String executionId,
            @PathVariable String filePath) {

        DeveloperExecutionResult result = executions.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        for (PatchDiff d : result.appliedDiffs()) {
            if (d.filePath().equals(filePath)) {
                return ResponseEntity.ok(toDiffMap(d));
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns the rollback plan for an execution.
     */
    @GetMapping("/{executionId}/rollback")
    public ResponseEntity<Map<String, Object>> getRollback(@PathVariable String executionId) {
        DeveloperExecutionResult result = executions.get(executionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        RollbackPlan rollback = result.rollbackPlan();
        if (rollback == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("executionId", executionId);
            body.put("hasRollback", false);
            body.put("message", "No rollback plan available");
            return ResponseEntity.ok(body);
        }

        List<Map<String, Object>> entries = new ArrayList<>();
        for (RollbackPlan.RollbackEntry e : rollback.entries()) {
            List<Map<String, Object>> actions = new ArrayList<>();
            for (RollbackPlan.UndoAction a : e.actions()) {
                Map<String, Object> aMap = new LinkedHashMap<>();
                aMap.put("type", a.type().name());
                aMap.put("description", a.description());
                aMap.put("target", a.target());
                actions.add(aMap);
            }
            Map<String, Object> entryMap = new LinkedHashMap<>();
            entryMap.put("filePath", e.filePath());
            entryMap.put("originalContent", e.originalContent());
            entryMap.put("actionCount", e.actions().size());
            entryMap.put("actions", actions);
            entries.add(entryMap);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionId", executionId);
        body.put("planId", rollback.planId());
        body.put("hasRollback", true);
        body.put("fileCount", rollback.fileCount());
        body.put("totalActions", rollback.totalActions());
        body.put("createdAt", rollback.createdAt().toString());
        body.put("entries", entries);
        return ResponseEntity.ok(body);
    }

    /**
     * Lists all stored execution IDs.
     */
    @GetMapping("/executions")
    public ResponseEntity<List<Map<String, Object>>> listExecutions() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeveloperExecutionResult r : executions.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("executionId", r.executionId());
            m.put("status", r.status().name());
            m.put("appliedCount", r.appliedCount());
            m.put("totalPatches", r.appliedDiffs().size());
            m.put("confidence", r.confidence());
            m.put("executedAt", r.executedAt().toString());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    private Map<String, Object> toDiffMap(PatchDiff d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("filePath", d.filePath());
        m.put("status", d.status().name());
        m.put("isSuccess", d.isSuccess());
        m.put("linesChanged", d.linesChanged());
        m.put("message", d.message());
        m.put("appliedAt", d.appliedAt().toString());
        m.put("before", d.before());
        m.put("after", d.after());
        return m;
    }
}
