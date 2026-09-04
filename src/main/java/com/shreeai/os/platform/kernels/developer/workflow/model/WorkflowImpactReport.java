package com.shreeai.os.platform.kernels.developer.workflow.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>WorkflowImpactReport</b>
 *
 * <p>Immutable impact analysis report produced by the Sprint-16
 * {@link com.shreeai.os.platform.kernels.developer.workflow.ImpactIntelligenceEngine}.
 * Contains affected files, impacted classes, risk level, estimated change count,
 * and dependency warnings.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class WorkflowImpactReport {

    /** Risk level of the proposed change. */
    public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    private final int totalFiles;
    private final List<String> affectedFiles;
    private final List<String> impactedClasses;
    private final RiskLevel riskLevel;
    private final int estimatedChanges;
    private final List<String> dependencyWarnings;

    private WorkflowImpactReport(Builder b) {
        this.totalFiles = b.totalFiles;
        this.affectedFiles = List.copyOf(b.affectedFiles == null ? List.of() : b.affectedFiles);
        this.impactedClasses = List.copyOf(b.impactedClasses == null ? List.of() : b.impactedClasses);
        this.riskLevel = b.riskLevel == null ? RiskLevel.MEDIUM : b.riskLevel;
        this.estimatedChanges = b.estimatedChanges;
        this.dependencyWarnings = List.copyOf(b.dependencyWarnings == null ? List.of() : b.dependencyWarnings);
    }

    public int totalFiles() { return totalFiles; }
    public List<String> affectedFiles() { return affectedFiles; }
    public List<String> impactedClasses() { return impactedClasses; }
    public RiskLevel riskLevel() { return riskLevel; }
    public int estimatedChanges() { return estimatedChanges; }
    public List<String> dependencyWarnings() { return dependencyWarnings; }

    /**
     * Returns the number of affected files.
     */
    public int affectedFileCount() { return affectedFiles.size(); }

    /**
     * Returns the number of impacted classes.
     */
    public int impactedClassCount() { return impactedClasses.size(); }

    /**
     * Returns true if there are any dependency warnings.
     */
    public boolean hasWarnings() { return !dependencyWarnings.isEmpty(); }

    /**
     * Returns all data as an immutable map for embedding in SDK responses.
     */
    public Map<String, Object> toMap() {
        return Map.of(
                "totalFiles", totalFiles,
                "affectedFiles", affectedFiles,
                "impactedClasses", impactedClasses,
                "riskLevel", riskLevel.name(),
                "estimatedChanges", estimatedChanges,
                "dependencyWarnings", dependencyWarnings
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int totalFiles;
        private List<String> affectedFiles;
        private List<String> impactedClasses;
        private RiskLevel riskLevel;
        private int estimatedChanges;
        private List<String> dependencyWarnings;

        public Builder totalFiles(int v) { this.totalFiles = v; return this; }
        public Builder affectedFiles(List<String> v) { this.affectedFiles = v; return this; }
        public Builder impactedClasses(List<String> v) { this.impactedClasses = v; return this; }
        public Builder riskLevel(RiskLevel v) { this.riskLevel = v; return this; }
        public Builder estimatedChanges(int v) { this.estimatedChanges = v; return this; }
        public Builder dependencyWarnings(List<String> v) { this.dependencyWarnings = v; return this; }

        public WorkflowImpactReport build() { return new WorkflowImpactReport(this); }
    }
}
