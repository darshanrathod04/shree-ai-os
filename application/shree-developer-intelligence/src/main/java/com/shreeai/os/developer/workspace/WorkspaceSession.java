package com.shreeai.os.developer.workspace;

import com.shreeai.os.platform.kernels.project.model.ProjectSummary;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>WorkspaceSession</b>
 *
 * <p>An open project workspace. Created when the user opens a project
 * folder, populated when analysis completes, and persisted for the
 * lifetime of the application run.</p>
 *
 * <p>Carries:</p>
 * <ul>
 *   <li>Project summary from {@code shree.project().analyze(path)}</li>
 *   <li>Last impact analysis result (cached)</li>
 *   <li>Active session metadata</li>
 * </ul>
 */
public final class WorkspaceSession {

    private final String id;
    private final String projectPath;
    private final String projectName;
    private final ProjectSummary summary;
    private final String lastImpactTarget;
    private final Instant openedAt;
    private final List<String> history;

    private WorkspaceSession(Builder b) {
        this.id = b.id == null ? UUID.randomUUID().toString() : b.id;
        this.projectPath = Objects.requireNonNull(b.projectPath, "projectPath");
        this.projectName = b.projectName == null ? deriveName(b.projectPath) : b.projectName;
        this.summary = b.summary;
        this.lastImpactTarget = b.lastImpactTarget;
        this.openedAt = b.openedAt == null ? Instant.now() : b.openedAt;
        this.history = List.copyOf(b.history == null ? List.of() : b.history);
    }

    public String id() { return id; }
    public String projectPath() { return projectPath; }
    public String projectName() { return projectName; }
    public ProjectSummary summary() { return summary; }
    public String lastImpactTarget() { return lastImpactTarget; }
    public Instant openedAt() { return openedAt; }
    public List<String> history() { return history; }

    public boolean isAnalyzed() { return summary != null; }

    private static String deriveName(String path) {
        if (path == null || path.isBlank()) return "Untitled";
        String trimmed = path.endsWith("/") || path.endsWith("\\")
                ? path.substring(0, path.length() - 1)
                : path;
        int idx = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .projectPath(projectPath)
                .projectName(projectName)
                .summary(summary)
                .lastImpactTarget(lastImpactTarget)
                .openedAt(openedAt)
                .history(history);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id;
        private String projectPath;
        private String projectName;
        private ProjectSummary summary;
        private String lastImpactTarget;
        private Instant openedAt;
        private List<String> history;

        public Builder id(String v) { this.id = v; return this; }
        public Builder projectPath(String v) { this.projectPath = v; return this; }
        public Builder projectName(String v) { this.projectName = v; return this; }
        public Builder summary(ProjectSummary v) { this.summary = v; return this; }
        public Builder lastImpactTarget(String v) { this.lastImpactTarget = v; return this; }
        public Builder openedAt(Instant v) { this.openedAt = v; return this; }
        public Builder history(List<String> v) { this.history = v; return this; }
        public Builder addHistory(String entry) {
            if (this.history == null) this.history = new java.util.ArrayList<>();
            this.history.add(entry);
            return this;
        }

        public WorkspaceSession build() { return new WorkspaceSession(this); }
    }
}
