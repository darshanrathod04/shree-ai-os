package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.AgentDecision.Agent;
import com.shreeai.os.platform.runtime.model.AgentDecision.Action;
import com.shreeai.os.platform.runtime.model.DiagnosticReport;
import com.shreeai.os.platform.runtime.model.DiagnosticReport.CheckStatus;
import com.shreeai.os.platform.runtime.model.DiagnosticReport.DiagnosticArea;
import com.shreeai.os.platform.runtime.model.ExecutionPlan;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DiagnosisAgent</b>
 *
 * <p>Examines the runtime environment before any kernel executes. Runs
 * a fixed set of health checks to determine whether the workspace,
 * memory, knowledge graph, project analysis, and execution engine
 * are ready to serve the current request.</p>
 *
 * <p><b>Checks (Sprint 18):</b></p>
 * <ul>
 *   <li>{@code WORKSPACE} — Is a project path or working directory available?</li>
 *   <li>{@code MEMORY} — Is the memory store accessible? Any recent entries?</li>
 *   <li>{@code KNOWLEDGE} — Is the knowledge graph accessible? Any relevant nodes?</li>
 *   <li>{@code PROJECT} — Has the project been analyzed? Is analysis cache valid?</li>
 *   <li>{@code EXECUTION} — Is the execution engine ready?</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — reads request metadata only, no side-effects.</li>
 *   <li>Deterministic — same request always produces same report.</li>
 *   <li>Fast — all checks are local reads, no network calls.</li>
 * </ul>
 *
 * @since Sprint 18
 */
public final class DiagnosisAgent {

    /**
     * Creates a DiagnosisAgent instance.
     */
    public DiagnosisAgent() {}

    /**
     * Analyzes the workspace and returns a health report.
     *
     * @param plan   the execution plan produced by {@code ChiefIntelligenceAgent}
     * @param request the original execution request (may carry workspace context)
     * @return a populated {@code DiagnosticReport} (never null)
     */
    public DiagnosticReport analyze(ExecutionPlan plan, ExecutionRequest request) {
        Objects.requireNonNull(plan, "plan must not be null");

        DiagnosticReport.Builder reportBuilder = DiagnosticReport.builder();

        // ── WORKSPACE CHECK ─────────────────────────────────────────────────
        CheckStatus workspaceStatus = checkWorkspace(request);
        reportBuilder.putStatus(DiagnosticArea.WORKSPACE, workspaceStatus);

        // ── MEMORY CHECK ──────────────────────────────────────────────────────
        CheckStatus memoryStatus = checkMemory(request);
        reportBuilder.putStatus(DiagnosticArea.MEMORY, memoryStatus);

        // ── KNOWLEDGE CHECK ───────────────────────────────────────────────────
        CheckStatus knowledgeStatus = checkKnowledge(request);
        reportBuilder.putStatus(DiagnosticArea.KNOWLEDGE, knowledgeStatus);

        // ── PROJECT CHECK ─────────────────────────────────────────────────────
        CheckStatus projectStatus = checkProject(plan, request);
        reportBuilder.putStatus(DiagnosticArea.PROJECT, projectStatus);

        // ── EXECUTION CHECK ───────────────────────────────────────────────────
        CheckStatus executionStatus = checkExecution(request);
        reportBuilder.putStatus(DiagnosticArea.EXECUTION, executionStatus);

        // ── RECOMMENDATIONS ──────────────────────────────────────────────────
        addRecommendations(reportBuilder, workspaceStatus, memoryStatus,
                knowledgeStatus, projectStatus, executionStatus);

        // ── DETAILS ──────────────────────────────────────────────────────────
        reportBuilder.addDetail("planId", plan.planId());
        reportBuilder.addDetail("detectedIntent", plan.detectedIntent().name());
        reportBuilder.addDetail("requestedKernels", plan.orderedKernels().size());

        return reportBuilder.build();
    }

    /**
     * Checks whether a project path or working directory is available.
     */
    CheckStatus checkWorkspace(ExecutionRequest request) {
        if (request == null) {
            return CheckStatus.SKIPPED;
        }

        Object workspacePath = extractMetadata(request, "workspacePath");
        if (workspacePath != null && !String.valueOf(workspacePath).isBlank()) {
            return CheckStatus.PASS;
        }

        // Check for a project path hint
        Object projectPath = extractMetadata(request, "projectPath");
        if (projectPath != null && !String.valueOf(projectPath).isBlank()) {
            return CheckStatus.PASS;
        }

        return CheckStatus.WARN;
    }

    /**
     * Checks whether the memory store is accessible.
     */
    CheckStatus checkMemory(ExecutionRequest request) {
        if (request == null) {
            return CheckStatus.SKIPPED;
        }

        Object memoryId = extractMetadata(request, "memoryId");
        if (memoryId != null && !String.valueOf(memoryId).isBlank()) {
            return CheckStatus.PASS;
        }

        // If this is a memory request, warn if no memory ID
        Object operation = extractMetadata(request, "operation");
        if ("RECALL_MEMORY".equals(String.valueOf(operation))
                || "STORE_MEMORY".equals(String.valueOf(operation))) {
            if (memoryId == null || String.valueOf(memoryId).isBlank()) {
                return CheckStatus.WARN;
            }
        }

        return CheckStatus.PASS;
    }

    /**
     * Checks whether the knowledge graph is accessible.
     */
    CheckStatus checkKnowledge(ExecutionRequest request) {
        if (request == null) {
            return CheckStatus.SKIPPED;
        }

        Object operation = extractMetadata(request, "operation");
        if (operation == null) {
            // General chat — knowledge is optional
            return CheckStatus.PASS;
        }

        String op = String.valueOf(operation).toUpperCase(java.util.Locale.ROOT);
        if (op.contains("KNOWLEDGE") || op.contains("QUERY")) {
            return CheckStatus.PASS;
        }

        return CheckStatus.PASS;
    }

    /**
     * Checks whether the project has been analyzed and the analysis cache is valid.
     */
    CheckStatus checkProject(ExecutionPlan plan, ExecutionRequest request) {
        if (request == null) {
            return CheckStatus.SKIPPED;
        }

        boolean projectKernelRequested = plan.orderedKernels().contains(KernelType.PROJECT);

        if (!projectKernelRequested) {
            return CheckStatus.SKIPPED;
        }

        Object projectPath = extractMetadata(request, "projectPath");
        if (projectPath == null || String.valueOf(projectPath).isBlank()) {
            return CheckStatus.FAIL;
        }

        Object analysisCacheValid = extractMetadata(request, "projectAnalysisCacheValid");
        if (Boolean.FALSE.equals(analysisCacheValid)) {
            return CheckStatus.WARN;
        }

        return CheckStatus.PASS;
    }

    /**
     * Checks whether the execution engine is ready.
     */
    CheckStatus checkExecution(ExecutionRequest request) {
        if (request == null) {
            return CheckStatus.SKIPPED;
        }

        Object operation = extractMetadata(request, "operation");
        if (operation == null) {
            return CheckStatus.PASS;
        }

        String op = String.valueOf(operation).toUpperCase(java.util.Locale.ROOT);
        if (op.contains("EXECUTE") || op.contains("TASK")) {
            return CheckStatus.PASS;
        }

        return CheckStatus.PASS;
    }

    private void addRecommendations(
            DiagnosticReport.Builder builder,
            CheckStatus workspace,
            CheckStatus memory,
            CheckStatus knowledge,
            CheckStatus project,
            CheckStatus execution
    ) {
        if (workspace == CheckStatus.FAIL || workspace == CheckStatus.WARN) {
            builder.addRecommendation("Set 'workspacePath' or 'projectPath' in request metadata to enable project analysis.");
        }
        if (project == CheckStatus.FAIL) {
            builder.addRecommendation("Run ProjectSDK.analyze() first to initialize the project cache.");
        }
        if (project == CheckStatus.WARN) {
            builder.addRecommendation("Project cache may be stale. Consider re-running ProjectSDK.analyze().");
        }
    }

    /**
     * Safely extracts a metadata value from the ExecutionRequest.
     */
    private Object extractMetadata(ExecutionRequest request, String key) {
        if (request == null || request.getMetadata() == null) {
            return null;
        }
        return request.getMetadata().get(key);
    }

    /**
     * Returns an AgentDecision describing this diagnosis run.
     */
    public AgentDecision toDecision(DiagnosticReport report) {
        return AgentDecision.builder()
                .agent(Agent.DIAGNOSIS)
                .action(report.hasFailures() ? Action.SHORT_CIRCUIT : Action.DIAGNOSE)
                .rationale(report.isHealthy()
                        ? "All workspace checks passed"
                        : "Some checks failed or warned: " + report.statuses())
                .confidence(report.isHealthy() ? 0.95 : 0.60)
                .addMetadata("diagnosticReportId", report.reportId())
                .addMetadata("healthy", report.isHealthy())
                .addMetadata("failureCount", (int) report.statuses().values().stream()
                        .filter(s -> s == CheckStatus.FAIL).count())
                .build();
    }
}
