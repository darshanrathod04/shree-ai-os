package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.AgentDecision.Action;
import com.shreeai.os.platform.runtime.model.AgentDecision.Agent;
import com.shreeai.os.platform.runtime.model.DiagnosticReport;
import com.shreeai.os.platform.runtime.model.DiagnosticReport.CheckStatus;
import com.shreeai.os.platform.runtime.model.DiagnosticReport.DiagnosticArea;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.ExecutionPlan;
import com.shreeai.os.platform.runtime.model.VerificationReport;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalyzer;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefIntelligenceAgent</b>
 *
 * <p>The single entry point for all {@code ShreeAI.chat()} requests in Sprint 18.
 * Every request passes through this agent before any kernel executes.</p>
 *
 * <p><b>Pipeline:</b></p>
 * <ol>
 *   <li>{@link #route(ExecutionRequest)} → {@code ExecutionPlan}</li>
 *   <li>{@link DiagnosisAgent#analyze(ExecutionPlan)} → {@code DiagnosticReport}</li>
 *   <li>If workspace is healthy and kernels can run:</li>
 *   <ol type="a">
 *     <li>{@link EvidenceAgent#extract(com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState)} → {@code EvidenceBundle}</li>
 *     <li>{@link VerificationAgent#verify(EvidenceBundle)} → {@code VerificationReport}</li>
 *     <li>{@link NaturalResponseAgent#generate(VerificationReport, ExecutionRequest)} → {@code SynthesizedResponse}</li>
 *   </ol>
 *   <li>If workspace is unhealthy or all kernels blocked:</li>
 *   <ol type="a">
 *     <li>Return diagnostic response from {@code DiagnosticReport}</li>
 *   </ol>
 * </ol>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — all state is in the request or passed to sub-agents.</li>
 *   <li>No LLM calls — routing is deterministic via {@code IntentAnalyzer}.</li>
 *   <li>Immutable results — all models are immutable.</li>
 * </ul>
 *
 * @since Sprint 18
 */
public final class ChiefIntelligenceAgent {

    private final IntentAnalyzer intentAnalyzer;
    private final DiagnosisAgent diagnosisAgent;
    private final EvidenceAgent evidenceAgent;
    private final VerificationAgent verificationAgent;
    private final NaturalResponseAgent naturalResponseAgent;

    /**
     * Creates a fully-wired ChiefIntelligenceAgent with all required sub-agents.
     *
     * <p>Uses the canonical singleton instances from the runtime.</p>
     */
    public ChiefIntelligenceAgent() {
        this(
                new IntentAnalyzer(),
                new DiagnosisAgent(),
                new EvidenceAgent(),
                new VerificationAgent(),
                new NaturalResponseAgent()
        );
    }

    /**
     * Creates a ChiefIntelligenceAgent with injected dependencies.
     * Supports testing and alternate implementations.
     */
    public ChiefIntelligenceAgent(
            IntentAnalyzer intentAnalyzer,
            DiagnosisAgent diagnosisAgent,
            EvidenceAgent evidenceAgent,
            VerificationAgent verificationAgent,
            NaturalResponseAgent naturalResponseAgent
    ) {
        this.intentAnalyzer = Objects.requireNonNull(intentAnalyzer, "intentAnalyzer must not be null");
        this.diagnosisAgent = Objects.requireNonNull(diagnosisAgent, "diagnosisAgent must not be null");
        this.evidenceAgent = Objects.requireNonNull(evidenceAgent, "evidenceAgent must not be null");
        this.verificationAgent = Objects.requireNonNull(verificationAgent, "verificationAgent must not be null");
        this.naturalResponseAgent = Objects.requireNonNull(naturalResponseAgent, "naturalResponseAgent must not be null");
    }

    /**
     * Routes a single user request through the full autonomous intelligence pipeline.
     *
     * <p>This is the canonical public entry point for all {@code ShreeAI.chat()}
     * requests in Sprint 18.</p>
     *
     * <p>Pipeline:</p>
     * <ol>
     *   <li>Build ExecutionPlan via intent analysis</li>
     *   <li>Run DiagnosisAgent to check workspace health</li>
     *   <li>If blocked: return diagnostic response</li>
     *   <li>Otherwise: extract evidence → verify → synthesize</li>
     * </ol>
     *
     * @param request the execution request (never null)
     * @return a fully-synthesized response (never null)
     */
    public SynthesizedResponse route(ExecutionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String userInput = extractUserInput(request);
        IntentAnalysisResult intent = intentAnalyzer.analyze(userInput);

        ExecutionPlan plan = buildPlan(intent, request);
        AgentDecision chiefDecision = buildChiefDecision(plan, intent);

        DiagnosticReport diagnostics = diagnosisAgent.analyze(plan, request);

        if (!plan.hasKernels()) {
            return buildDiagnosticResponse(plan, diagnostics);
        }

        EvidenceBundle bundle = evidenceAgent.extract(request, diagnostics);
        VerificationReport verification = verificationAgent.verify(bundle);
        SynthesizedResponse response = naturalResponseAgent.generate(verification, request);

        return attachChiefMetadata(response, chiefDecision, plan, verification, diagnostics);
    }

    /**
     * Returns the execution plan without running the full pipeline.
     * Useful for observability and debugging.
     *
     * @param request the execution request (never null)
     * @return the execution plan
     */
    public ExecutionPlan buildPlan(ExecutionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        String userInput = extractUserInput(request);
        IntentAnalysisResult intent = intentAnalyzer.analyze(userInput);
        return buildPlan(intent, request);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private ExecutionPlan buildPlan(IntentAnalysisResult intent, ExecutionRequest request) {
        ExecutionPlan.Builder planBuilder = ExecutionPlan.builder()
                .detectedIntent(intent.primaryIntent())
                .routingMetadata(Map.of(
                        "originalInput", intent.originalInput(),
                        "intentConfidence", intent.confidence(),
                        "isMultiKernel", intent.isMultiKernel()
                ));

        for (KernelType kt : intent.requiredKernels()) {
            planBuilder.addKernel(kt);
        }

        return planBuilder.build();
    }

    private AgentDecision buildChiefDecision(ExecutionPlan plan, IntentAnalysisResult intent) {
        String rationale = String.format(
                "Detected intent=%s, kernels=%s, multiKernel=%s, intentConfidence=%.2f",
                intent.primaryIntent(),
                plan.orderedKernels(),
                intent.isMultiKernel(),
                intent.confidence()
        );
        return AgentDecision.builder()
                .agent(Agent.CHIEF_INTELLIGENCE)
                .action(Action.ROUTE)
                .rationale(rationale)
                .confidence(intent.confidence())
                .addMetadata("detectedIntent", intent.primaryIntent().name())
                .addMetadata("kernelCount", plan.orderedKernels().size())
                .addMetadata("isMultiKernel", intent.isMultiKernel())
                .build();
    }

    private SynthesizedResponse buildDiagnosticResponse(
            ExecutionPlan plan,
            DiagnosticReport diagnostics
    ) {
        StringBuilder answer = new StringBuilder();
        answer.append("# Diagnostic Report\n\n");
        answer.append("## Workspace Status\n\n");

        for (Map.Entry<DiagnosticArea, CheckStatus> entry : diagnostics.statuses().entrySet()) {
            String emoji = switch (entry.getValue()) {
                case PASS -> "✅";
                case WARN -> "⚠️";
                case FAIL -> "❌";
                case SKIPPED -> "➖";
            };
            answer.append(emoji)
                    .append(" ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        if (!diagnostics.recommendations().isEmpty()) {
            answer.append("\n## Recommendations\n\n");
            for (String rec : diagnostics.recommendations()) {
                answer.append("- ").append(rec).append("\n");
            }
        }

        var sections = java.util.List.of(
                new com.shreeai.os.platform.kernels.response.model.ResponseSection(
                        "Workspace Status", answer.toString()
                )
        );

        return new SynthesizedResponse(
                answer.toString(),
                sections,
                0.50,
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                Map.of(
                        "diagnosticReport", diagnostics.reportId(),
                        "hasFailures", diagnostics.hasFailures(),
                        "isHealthy", diagnostics.isHealthy(),
                        "criticalFailure", true
                )
        );
    }

    private SynthesizedResponse attachChiefMetadata(
            SynthesizedResponse response,
            AgentDecision chiefDecision,
            ExecutionPlan plan,
            VerificationReport verification,
            DiagnosticReport diagnostics
    ) {
        java.util.Map<String, Object> enrichedMetadata = new java.util.LinkedHashMap<>(response.structuredData());
        enrichedMetadata.put("chiefDecisionId", chiefDecision.decisionId());
        enrichedMetadata.put("executionPlanId", plan.planId());
        enrichedMetadata.put("verificationTier", verification.tier().name());
        enrichedMetadata.put("confidenceTier", verification.tier().name());
        enrichedMetadata.put("confidence", verification.confidence());
        enrichedMetadata.put("isHealthy", diagnostics.isHealthy());
        enrichedMetadata.put("hasFailures", diagnostics.hasFailures());
        // Sprint-18: criticalFailure means a hard pre-flight blocker — only when
        // the EXECUTION engine itself reports FAIL or no WORKSPACE is reachable.
        // Soft warnings (PROJECT, MEMORY, KNOWLEDGE) are NOT critical; the
        // canonical pipeline can still serve the request with reduced confidence.
        boolean criticalFailure = diagnostics.statusOf(DiagnosticArea.EXECUTION) == CheckStatus.FAIL
                || diagnostics.statusOf(DiagnosticArea.WORKSPACE) == CheckStatus.FAIL;
        enrichedMetadata.put("criticalFailure", criticalFailure);

        return new SynthesizedResponse(
                response.answer(),
                response.sections(),
                response.confidence(),
                response.style(),
                response.generatedAt(),
                enrichedMetadata
        );
    }

    private String extractUserInput(ExecutionRequest request) {
        if (request == null) return "";
        String input = request.getUserInput();
        return input != null ? input.trim() : "";
    }

    // Expose sub-agents for testing
    DiagnosisAgent diagnosisAgent() { return diagnosisAgent; }
    EvidenceAgent evidenceAgent() { return evidenceAgent; }
    VerificationAgent verificationAgent() { return verificationAgent; }
    NaturalResponseAgent naturalResponseAgent() { return naturalResponseAgent; }
}
