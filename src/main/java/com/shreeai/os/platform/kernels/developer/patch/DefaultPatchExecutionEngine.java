package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchPlan;
import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult.CompileReport;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.RollbackEntry;
import com.shreeai.os.platform.kernels.developer.workflow.DefaultDeveloperWorkflowEngine;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperRequest;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperWorkflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultPatchExecutionEngine</b>
 *
 * <p>The top-level orchestrator for the Sprint-17 autonomous patch application
 * pipeline. Combines:</p>
 * <ol>
 *   <li>Workflow analysis (Sprint-16)</li>
 *   <li>Code generation (Sprint-15)</li>
 *   <li>Patch application (Sprint-17)</li>
 *   <li>Compile validation (Sprint-17)</li>
 *   <li>Rollback plan generation (Sprint-17)</li>
 * </ol>
 *
 * <p>The engine never writes files to disk by default. File writes only
 * happen if {@code applyWithFileWrites} is called.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class DefaultPatchExecutionEngine {

    private final DefaultDeveloperWorkflowEngine workflowEngine;
    private final PatchApplier patchApplier;
    private final CompileValidationService compileService;

    public DefaultPatchExecutionEngine() {
        this(new DefaultDeveloperWorkflowEngine(), new PatchApplier(), new CompileValidationService());
    }

    public DefaultPatchExecutionEngine(DefaultDeveloperWorkflowEngine workflowEngine,
                                       PatchApplier patchApplier,
                                       CompileValidationService compileService) {
        this.workflowEngine = Objects.requireNonNull(workflowEngine, "workflowEngine");
        this.patchApplier = Objects.requireNonNull(patchApplier, "patchApplier");
        this.compileService = Objects.requireNonNull(compileService, "compileService");
    }

    /**
     * Executes the full pipeline: workflow → patch application → compile
     * validation → rollback plan. Does not write files to disk.
     *
     * @param projectPath the project root directory
     * @param instruction the developer instruction
     * @return the structured execution result
     */
    public DeveloperExecutionResult execute(String projectPath, String instruction) {
        Objects.requireNonNull(projectPath, "projectPath must not be null");
        Objects.requireNonNull(instruction, "instruction must not be null");

        // 1) Run the workflow
        DeveloperRequest request = DeveloperRequest.builder()
                .projectPath(projectPath)
                .instruction(instruction)
                .build();
        DeveloperResult workflowResult = workflowEngine.execute(request);
        DeveloperWorkflow workflow = workflowResult.workflow();

        // 2) Apply patches in-memory
        List<PatchDiff> appliedDiffs = new ArrayList<>();
        List<RollbackEntry> rollbackEntries = new ArrayList<>();
        Map<String, String> workingSources = new LinkedHashMap<>();

        PatchPlan plan = workflow.patchPlan();
        if (plan != null && plan.patches() != null) {
            for (FilePatch patch : plan.patches()) {
                String source = workingSources.getOrDefault(patch.targetFile(), "");
                PatchApplier.ApplyResult r = patchApplier.apply(patch, source);
                appliedDiffs.add(r.diff());
                workingSources.put(patch.targetFile(), r.diff().after());
                if (r.rollbackEntry() != null) {
                    rollbackEntries.add(r.rollbackEntry());
                }
            }
        }

        // 3) Run a static check (no Maven invocation by default)
        List<String> sourceFiles = new ArrayList<>(workingSources.values());
        CompileReport compileReport = compileService.staticCheck(sourceFiles);

        // 4) Build rollback plan
        RollbackPlan rollbackPlan = RollbackPlan.builder()
                .planId("rb-" + System.nanoTime())
                .entries(rollbackEntries)
                .createdAt(Instant.now())
                .build();

        // 5) Compute status and confidence
        int successCount = (int) appliedDiffs.stream().filter(PatchDiff::isSuccess).count();
        int total = appliedDiffs.size();
        DeveloperExecutionResult.Status status;
        if (total == 0) {
            status = DeveloperExecutionResult.Status.SKIPPED;
        } else if (successCount == total && compileReport.status() == CompileReport.CompileStatus.SUCCESS) {
            status = DeveloperExecutionResult.Status.SUCCESS;
        } else if (successCount > 0) {
            status = DeveloperExecutionResult.Status.PARTIAL_SUCCESS;
        } else {
            status = DeveloperExecutionResult.Status.FAILED;
        }

        double confidence = computeConfidence(workflowResult.confidence(), successCount, total, compileReport);

        return DeveloperExecutionResult.builder()
                .executionId("exec-" + System.nanoTime())
                .status(status)
                .workflow(workflow)
                .appliedDiffs(appliedDiffs)
                .rollbackPlan(rollbackPlan)
                .compileReport(compileReport)
                .confidence(confidence)
                .executedAt(Instant.now())
                .build();
    }

    /**
     * Computes a confidence score from the workflow confidence and patch
     * application results.
     */
    private double computeConfidence(double base, int success, int total, CompileReport report) {
        double c = base;
        if (total > 0) {
            double successRatio = (double) success / total;
            c = c * 0.5 + 0.5 * successRatio;
        }
        if (report != null) {
            switch (report.status()) {
                case FAILURE -> c -= 0.20;
                case SUCCESS -> c += 0.05;
                case SKIPPED -> { /* no adjustment */ }
            }
        }
        return Math.max(0.1, Math.min(0.95, c));
    }
}
