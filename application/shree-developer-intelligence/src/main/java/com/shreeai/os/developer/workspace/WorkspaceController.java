package com.shreeai.os.developer.workspace;

import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <b>WorkspaceController</b> — REST surface for Module 1: Project Workspace.
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * POST   /api/developer/workspace/open              — open a project path
 * GET    /api/developer/workspace/sessions          — list all open sessions
 * GET    /api/developer/workspace/{id}              — get session details
 * GET    /api/developer/workspace/{id}/summary      — get project summary
 * GET    /api/developer/workspace/{id}/class?name=  — find class by name
 * GET    /api/developer/workspace/{id}/endpoint?p=  — find endpoint by path
 * GET    /api/developer/workspace/{id}/impact?n=    — compute impact for class
 * DELETE /api/developer/workspace/{id}              — close session
 * </pre>
 */
@RestController
@RequestMapping("/api/developer/workspace")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * Opens a project at the given path.
     */
    @PostMapping("/open")
    public ResponseEntity<?> open(@RequestBody OpenRequest request) {
        if (request == null || request.path() == null || request.path().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Path is required"));
        }
        WorkspaceSession session = workspaceService.openWorkspace(request.path());
        return ResponseEntity.ok(toMap(session));
    }

    /**
     * Lists all open workspace sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<Map<String, Object>>> sessions() {
        return ResponseEntity.ok(
                workspaceService.getAllSessions().stream().map(this::toMap).toList()
        );
    }

    /**
     * Returns a single session by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> session(@PathVariable String id) {
        return workspaceService.getSession(id)
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(toMap(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns the project summary for a session.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> summary(@PathVariable String id) {
        return workspaceService.getSummary(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Finds a class by simple name.
     */
    @GetMapping("/{id}/class")
    public ResponseEntity<?> findClass(@PathVariable String id, @RequestParam String name) {
        return workspaceService.findClass(id, name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Finds a controller endpoint by path.
     */
    @GetMapping("/{id}/endpoint")
    public ResponseEntity<?> findEndpoint(@PathVariable String id, @RequestParam String p) {
        return workspaceService.findEndpoint(id, p)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Computes the impact of modifying a class.
     */
    @GetMapping("/{id}/impact")
    public ResponseEntity<?> impact(@PathVariable String id, @RequestParam String n) {
        return workspaceService.impact(id, n)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Closes a workspace session.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> close(@PathVariable String id) {
        boolean closed = workspaceService.closeSession(id);
        if (!closed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(WorkspaceSession s) {
        ProjectSummary summary = s.summary();
        return Map.of(
                "id", s.id(),
                "projectPath", s.projectPath(),
                "projectName", s.projectName(),
                "analyzed", s.isAnalyzed(),
                "openedAt", s.openedAt().toString(),
                "buildSystem", summary != null ? summary.buildSystem() : "UNKNOWN",
                "framework", summary != null ? summary.framework() : "UNKNOWN",
                "moduleCount", summary != null ? summary.modules().size() : 0,
                "classCount", summary != null ? summary.statistics().classCount() : 0,
                "endpointCount", summary != null ? summary.statistics().endpointCount() : 0
        );
    }

    public record OpenRequest(String path) {}
}
