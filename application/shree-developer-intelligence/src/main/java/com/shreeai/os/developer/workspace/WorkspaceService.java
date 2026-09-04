package com.shreeai.os.developer.workspace;

import com.shreeai.os.platform.kernels.project.model.*;
import com.shreeai.os.platform.sdk.ProjectSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>WorkspaceService</b>
 *
 * <p>Application-layer service for managing project workspaces. This is the
 * only service in Module 1. All project intelligence flows through here.</p>
 *
 * <p><b>SDK Composition:</b></p>
 * <pre>
 * openWorkspace(path)  →  ProjectSDK.analyze(path)  →  WorkspaceSession
 * findClass(name)     →  ProjectSDK.findClass(name)
 * findEndpoint(path)  →  ProjectSDK.findController(path)
 * impact(name)        →  ProjectSDK.impact(name)
 * </pre>
 *
 * <p><b>Application Layer Rule:</b> Uses only public {@code ProjectSDK}
 * methods. No kernel imports. No reflection into private fields.</p>
 *
 * @since Phase 2
 */
@Service
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    private final ProjectSDK projectSdk;
    private final Map<String, WorkspaceSession> sessions = new ConcurrentHashMap<>();

    public WorkspaceService(ProjectSDK projectSdk) {
        this.projectSdk = Objects.requireNonNull(projectSdk, "projectSdk");
    }

    /**
     * Opens a project at the given path and runs analysis.
     *
     * <p>If a session already exists for this path, returns it (idempotent).
     * Otherwise creates a new session and runs {@code shree.project().analyze()}.</p>
     *
     * @param projectPath absolute path to the project root (must not be null)
     * @return WorkspaceSession with summary populated from the Project Intelligence Kernel
     */
    public WorkspaceSession openWorkspace(String projectPath) {
        Objects.requireNonNull(projectPath, "projectPath");

        // Idempotent: return existing session for this path
        Optional<WorkspaceSession> existing = sessions.values().stream()
                .filter(s -> s.projectPath().equals(projectPath))
                .findFirst();
        if (existing.isPresent()) {
            log.info("Reopening existing workspace session: {}", existing.get().id());
            return existing.get();
        }

        log.info("Opening workspace: {}", projectPath);
        WorkspaceSession session = WorkspaceSession.builder()
                .projectPath(projectPath)
                .build();

        try {
            Path path = Paths.get(projectPath);
            ProjectSummary summary = projectSdk.analyze(path);
            session = session.toBuilder().summary(summary).build();
            log.info("Analysis complete for {}: {} classes, {} endpoints",
                    summary.projectName(),
                    summary.statistics().classCount(),
                    summary.statistics().endpointCount());
        } catch (IOException e) {
            log.error("Failed to analyze project at {}: {}", projectPath, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Project SDK analysis error for {}: {}", projectPath, e.getMessage());
        }

        sessions.put(session.id(), session);
        return session;
    }

    public Optional<WorkspaceSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public List<WorkspaceSession> getAllSessions() {
        return List.copyOf(sessions.values());
    }

    public boolean closeSession(String sessionId) {
        return sessions.remove(sessionId) != null;
    }

    public Optional<ProjectSummary> getSummary(String sessionId) {
        WorkspaceSession session = sessions.get(sessionId);
        return Optional.ofNullable(session != null ? session.summary() : null);
    }

    public Optional<ProjectClass> findClass(String sessionId, String simpleName) {
        requireSession(sessionId);
        try {
            return Optional.ofNullable(projectSdk.findClass(simpleName));
        } catch (RuntimeException e) {
            log.warn("findClass({}) failed: {}", simpleName, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProjectEndpoint> findEndpoint(String sessionId, String path) {
        requireSession(sessionId);
        try {
            return Optional.ofNullable(projectSdk.findController(path));
        } catch (RuntimeException e) {
            log.warn("findController({}) failed: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProjectEntity> findEntity(String sessionId, String simpleName) {
        requireSession(sessionId);
        try {
            return Optional.ofNullable(projectSdk.findEntity(simpleName));
        } catch (RuntimeException e) {
            log.warn("findEntity({}) failed: {}", simpleName, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ProjectImpact> impact(String sessionId, String simpleName) {
        requireSession(sessionId);
        try {
            return Optional.ofNullable(projectSdk.impact(simpleName));
        } catch (RuntimeException e) {
            log.warn("impact({}) failed: {}", simpleName, e.getMessage());
            return Optional.empty();
        }
    }

    private void requireSession(String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("Unknown workspace session: " + sessionId);
        }
    }
}
