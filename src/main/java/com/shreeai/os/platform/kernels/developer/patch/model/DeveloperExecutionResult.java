package com.shreeai.os.platform.kernels.developer.patch.model;

import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperWorkflow;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DeveloperExecutionResult</b>
 *
 * <p>The complete structured output of the Sprint-17 autonomous patch
 * application pipeline. Carries the workflow, applied diffs, rollback plan,
 * compile diagnostics, and overall execution status.</p>
 *
 * <p>This is the entry point returned by {@code ProjectSDK.apply()}.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class DeveloperExecutionResult {

    /** Overall execution status. */
    public enum Status { SUCCESS, PARTIAL_SUCCESS, FAILED, SKIPPED }

    private final String executionId;
    private final Status status;
    private final DeveloperWorkflow workflow;
    private final List<PatchDiff> appliedDiffs;
    private final RollbackPlan rollbackPlan;
    private final CompileReport compileReport;
    private final double confidence;
    private final Instant executedAt;

    private DeveloperExecutionResult(Builder b) {
        this.executionId = b.executionId == null ? "exec-" + System.nanoTime() : b.executionId;
        this.status = b.status == null ? Status.SKIPPED : b.status;
        this.workflow = b.workflow;
        this.appliedDiffs = List.copyOf(b.appliedDiffs == null ? List.of() : b.appliedDiffs);
        this.rollbackPlan = b.rollbackPlan;
        this.compileReport = b.compileReport;
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.executedAt = b.executedAt == null ? Instant.now() : b.executedAt;
    }

    public String executionId() { return executionId; }
    public Status status() { return status; }
    public DeveloperWorkflow workflow() { return workflow; }
    public List<PatchDiff> appliedDiffs() { return appliedDiffs; }
    public RollbackPlan rollbackPlan() { return rollbackPlan; }
    public CompileReport compileReport() { return compileReport; }
    public double confidence() { return confidence; }
    public Instant executedAt() { return executedAt; }

    public boolean isSuccess() {
        return status == Status.SUCCESS || status == Status.PARTIAL_SUCCESS;
    }

    public int appliedCount() {
        return (int) appliedDiffs.stream().filter(PatchDiff::isSuccess).count();
    }

    public String summary() {
        return String.format(
                "DeveloperExecutionResult[id=%s, status=%s, applied=%d/%d, confidence=%.2f]",
                executionId, status, appliedCount(), appliedDiffs.size(), confidence
        );
    }

    public Map<String, Object> toPayload() {
        return Map.of(
                "executionId", executionId,
                "status", status.name(),
                "appliedPatches", appliedCount(),
                "totalPatches", appliedDiffs.size(),
                "rollbackActions", rollbackPlan != null ? rollbackPlan.totalActions() : 0,
                "compileStatus", compileReport != null ? compileReport.status().name() : "N/A",
                "confidence", confidence
        );
    }

    public static Builder builder() { return new Builder(); }

    /**
     * <b>CompileReport</b> — Maven compile diagnostics.
     */
    public static final class CompileReport {
        private final CompileStatus status;
        private final int filesCompiled;
        private final int errors;
        private final int warnings;
        private final List<String> diagnostics;

        public CompileReport(CompileStatus status, int filesCompiled, int errors, int warnings, List<String> diagnostics) {
            this.status = status;
            this.filesCompiled = filesCompiled;
            this.errors = errors;
            this.warnings = warnings;
            this.diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
        }

        public CompileStatus status() { return status; }
        public int filesCompiled() { return filesCompiled; }
        public int errors() { return errors; }
        public int warnings() { return warnings; }
        public List<String> diagnostics() { return diagnostics; }

        public static CompileReport success(int filesCompiled, int warnings, List<String> diagnostics) {
            return new CompileReport(CompileStatus.SUCCESS, filesCompiled, 0, warnings, diagnostics);
        }

        public static CompileReport failure(int errors, int warnings, List<String> diagnostics) {
            return new CompileReport(CompileStatus.FAILURE, 0, errors, warnings, diagnostics);
        }

        public static CompileReport skipped(String reason) {
            return new CompileReport(CompileStatus.SKIPPED, 0, 0, 0, List.of(reason));
        }

        public enum CompileStatus { SUCCESS, FAILURE, SKIPPED }
    }

    public static final class Builder {
        private String executionId;
        private Status status;
        private DeveloperWorkflow workflow;
        private List<PatchDiff> appliedDiffs;
        private RollbackPlan rollbackPlan;
        private CompileReport compileReport;
        private double confidence = 0.8;
        private Instant executedAt;

        public Builder executionId(String v) { this.executionId = v; return this; }
        public Builder status(Status v) { this.status = v; return this; }
        public Builder workflow(DeveloperWorkflow v) { this.workflow = v; return this; }
        public Builder appliedDiffs(List<PatchDiff> v) { this.appliedDiffs = v; return this; }
        public Builder rollbackPlan(RollbackPlan v) { this.rollbackPlan = v; return this; }
        public Builder compileReport(CompileReport v) { this.compileReport = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder executedAt(Instant v) { this.executedAt = v; return this; }

        public DeveloperExecutionResult build() { return new DeveloperExecutionResult(this); }
    }
}
