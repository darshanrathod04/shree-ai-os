package com.shreeai.os.platform.kernels.developer.workflow.model;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchPlan;
import com.shreeai.os.platform.kernels.developer.codegen.model.ValidationResult;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DeveloperWorkflow</b>
 *
 * <p>Immutable container that aggregates all workflow components produced
 * during the autonomous developer workflow pipeline execution:</p>
 * <ul>
 *   <li>Structured intent ({@link DeveloperIntent})</li>
 *   <li>Project summary ({@link ProjectSummary})</li>
 *   <li>Workflow impact report ({@link WorkflowImpactReport})</li>
 *   <li>Patch plan ({@link PatchPlan})</li>
 *   <li>Validation result ({@link ValidationResult})</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class DeveloperWorkflow {

    private final DeveloperIntent intent;
    private final ProjectSummary projectSummary;
    private final WorkflowImpactReport workflowImpact;
    private final PatchPlan patchPlan;
    private final ValidationResult validationResult;
    private final Instant executedAt;

    private DeveloperWorkflow(Builder b) {
        this.intent = b.intent;
        this.projectSummary = b.projectSummary;
        this.workflowImpact = b.workflowImpact;
        this.patchPlan = b.patchPlan;
        this.validationResult = b.validationResult;
        this.executedAt = b.executedAt == null ? Instant.now() : b.executedAt;
    }

    public DeveloperIntent intent() { return intent; }
    public ProjectSummary projectSummary() { return projectSummary; }
    public WorkflowImpactReport workflowImpact() { return workflowImpact; }
    public PatchPlan patchPlan() { return patchPlan; }
    public ValidationResult validationResult() { return validationResult; }
    public Instant executedAt() { return executedAt; }

    /**
     * Returns a summary map for embedding in SDK responses.
     */
    public Map<String, Object> toMap() {
        return Map.of(
                "intent", intent != null ? intent.intent().name() : "UNKNOWN",
                "entity", intent != null ? intent.entity() : "",
                "confidence", intent != null ? intent.confidence() : 0.0,
                "affectedFiles", workflowImpact != null ? workflowImpact.affectedFiles() : 0,
                "riskLevel", workflowImpact != null ? workflowImpact.riskLevel().name() : "UNKNOWN",
                "estimatedChanges", workflowImpact != null ? workflowImpact.estimatedChanges() : 0,
                "patchCount", patchPlan != null ? patchPlan.totalPatches() : 0,
                "validationStatus", validationResult != null ? validationResult.overallStatus().name() : "UNKNOWN"
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private DeveloperIntent intent;
        private ProjectSummary projectSummary;
        private WorkflowImpactReport workflowImpact;
        private PatchPlan patchPlan;
        private ValidationResult validationResult;
        private Instant executedAt;

        public Builder intent(DeveloperIntent v) { this.intent = v; return this; }
        public Builder projectSummary(ProjectSummary v) { this.projectSummary = v; return this; }
        public Builder workflowImpact(WorkflowImpactReport v) { this.workflowImpact = v; return this; }
        public Builder patchPlan(PatchPlan v) { this.patchPlan = v; return this; }
        public Builder validationResult(ValidationResult v) { this.validationResult = v; return this; }
        public Builder executedAt(Instant v) { this.executedAt = v; return this; }

        public DeveloperWorkflow build() { return new DeveloperWorkflow(this); }
    }
}
