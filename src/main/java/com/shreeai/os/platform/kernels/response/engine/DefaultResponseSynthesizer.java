package com.shreeai.os.platform.kernels.response.engine;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.planning.model.Milestone;
import com.shreeai.os.platform.kernels.planning.model.Phase;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.ResponseSection;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeCitation;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.runtime.orchestration.CompositeKernelResult;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult;
import com.shreeai.os.platform.kernels.response.model.DeveloperResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DefaultResponseSynthesizer
 *
 * Professional response generation engine.
 *
 * Constitutional rules:
 * - Never invent facts.
 * - Never expose chain-of-thought.
 * - Only synthesize validated pipeline outputs.
 */
public final class DefaultResponseSynthesizer implements ResponseSynthesizer {

    @Override
    public SynthesizedResponse synthesize(
            PipelineContext context,
            PipelineExecutionState state
    ) {

        Map<String, Object> metadata = state.getMetadata();

        if (isPlanningResult(context, metadata)) {
            return synthesizePlanning(context, metadata);
        }

        if (isKnowledgeResult(context, metadata)) {
            return synthesizeKnowledge(context, metadata);
        }

        if (isConversationalChat(context, metadata)) {
            return synthesizeChat(context, metadata);
        }

        return synthesizeDefault(metadata);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Execution Synthesis  (Sprint-10)
    // Produces structured enterprise-grade responses for capability-dispatch
    // results coming through the EXECUTE_TASK shortcut in DefaultRuntimeService.
    // Never produces a Goal{...} toString dump.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Synthesizes a structured execution response for capability-dispatch
     * results routed through the {@code EXECUTE_TASK} shortcut in
     * {@link com.shreeai.os.platform.runtime.service.DefaultRuntimeService}.
     *
     * @param capability      the dispatched execution capability
     * @param objective       the user's original execution input
     * @param status          the terminal status string (e.g. "COMPLETED", "FAILED")
     * @param executionId     the unique execution identifier
     * @param planId          the kernel-produced plan/task identifier
     * @param kernel          the kernel that handled the execution
     * @param deliverables    ordered list of deliverables
     * @param executionTimeMs wall-clock execution time in milliseconds
     * @return a fully-populated {@link SynthesizedResponse}
     */
    public SynthesizedResponse synthesizeExecution(
            com.shreeai.os.platform.runtime.execution.ExecutionCapability capability,
            String objective,
            String status,
            String executionId,
            String planId,
            String kernel,
            List<String> deliverables,
            long executionTimeMs
    ) {
        String capabilityDisplay = capabilityDisplayName(capability);
        List<ResponseSection> sections = new ArrayList<>();

        sections.add(new ResponseSection("Capability",     capabilityDisplay));
        sections.add(new ResponseSection("Objective",      objective));
        sections.add(new ResponseSection("Status",         status));
        sections.add(new ResponseSection("Execution ID",   executionId));

        String deliverablesText = deliverables.isEmpty()
                ? "No deliverables generated."
                : renderNumbered(deliverables);
        sections.add(new ResponseSection("Deliverables", deliverablesText));

        String metadataText = String.format(
                "Execution Time: %d ms%nCapability: %s%nKernel: %s",
                executionTimeMs,
                capability.value(),
                kernel
        );
        sections.add(new ResponseSection("Metadata", metadataText));

        String answer = buildExecutionAnswer(
                capabilityDisplay, objective, status, executionId,
                deliverables, executionTimeMs, kernel);

        Map<String, Object> structuredData = new LinkedHashMap<>();
        structuredData.put("capability",      capabilityDisplay);
        structuredData.put("capabilityValue", capability.value());
        structuredData.put("objective",       objective);
        structuredData.put("status",          status);
        structuredData.put("executionId",     executionId);
        structuredData.put("executionTimeMs",  executionTimeMs);
        structuredData.put("kernel",          kernel);
        structuredData.put("planId",          planId);
        structuredData.put("deliverables",    List.copyOf(deliverables));

        return new SynthesizedResponse(
                answer, sections, 0.90,
                ResponseStyle.PROFESSIONAL,
                Instant.now(), structuredData);
    }

    /** Returns the human-readable display name for an execution capability. */
    private String capabilityDisplayName(
            com.shreeai.os.platform.runtime.execution.ExecutionCapability capability
    ) {
        if (capability == null) {
            return "Unknown";
        }
        return switch (capability) {
            case PROJECT_PLANNING -> "Project Planning";
            case WORKOUT_PLANNING -> "Workout Planning";
            case KNOWLEDGE_SEARCH -> "Knowledge Search";
            case MEMORY_RECALL    -> "Memory Recall";
            case TASK_EXECUTION   -> "Task Execution";
        };
    }

    /**
     * Builds the answer text for an execution response. The output is
     * the canonical markdown structure required by the Sprint-10 spec.
     */
    private String buildExecutionAnswer(
            String capability,
            String objective,
            String status,
            String executionId,
            List<String> deliverables,
            long executionTimeMs,
            String kernel
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Execution Started\n\n");
        sb.append("## Capability\n").append(capability).append("\n\n");
        sb.append("## Objective\n").append(objective).append("\n\n");
        sb.append("## Status\n").append(status).append("\n\n");
        sb.append("## Execution ID\n").append(executionId).append("\n\n");
        sb.append("## Deliverables\n");
        if (deliverables.isEmpty()) {
            sb.append("No deliverables generated.\n\n");
        } else {
            for (int i = 0; i < deliverables.size(); i++) {
                sb.append(i + 1).append(". ").append(deliverables.get(i)).append("\n");
            }
            sb.append("\n");
        }
        sb.append("## Metadata\n");
        sb.append("Execution Time: ").append(executionTimeMs).append(" ms\n");
        sb.append("Capability: ").append(capability).append("\n");
        sb.append("Kernel: ").append(kernel).append("\n");
        return sb.toString();
    }

    /**
     * Legacy rendering path — unchanged behavior for chat and all
     * non-planning payloads.
     */
    private SynthesizedResponse synthesizeDefault(Map<String, Object> metadata) {

        List<ResponseSection> sections = new ArrayList<>();

        String summary = extractSummary(metadata);
        String conclusion = string(metadata.get("reasoningConclusion"));
        String plan = string(metadata.get("planSummary"));

        // Executive Summary
        sections.add(new ResponseSection(
                "Executive Summary",
                summary
        ));

        // Key Findings
        String findings = buildFindings(metadata);

        if (!findings.isBlank()) {
            sections.add(new ResponseSection(
                    "Key Findings",
                    findings
            ));
        }

        // Recommendation
        if (!plan.isBlank()) {
            sections.add(new ResponseSection(
                    "Recommended Next Step",
                    plan
            ));
        }

        // Evidence
        String evidence = buildEvidence(metadata);

        if (!evidence.isBlank()) {
            sections.add(new ResponseSection(
                    "Evidence",
                    evidence
            ));
        }

        double confidence = confidence(metadata);

        String answer = buildAnswer(summary, conclusion, plan);

        return new SynthesizedResponse(
                answer,
                sections,
                confidence,
                ResponseStyle.PROFESSIONAL,
                Instant.now()
        );
    }

    private String extractSummary(Map<String, Object> metadata) {

        String reasoning = string(metadata.get("reasoningSummary"));

        if (!reasoning.isBlank()) {
            return reasoning;
        }

        String plan = string(metadata.get("planSummary"));

        if (!plan.isBlank()) {
            return plan;
        }

        return "The request was successfully processed through the Shree AI intelligence pipeline.";
    }

    private String buildFindings(Map<String, Object> metadata) {

        List<String> findings = new ArrayList<>();

        addIfPresent(findings, metadata, "reasoningSummary");
        addIfPresent(findings, metadata, "reasoningConclusion");
        addIfPresent(findings, metadata, "planSummary");

        return String.join("\n• ", prependBullet(findings));
    }

    private String buildEvidence(Map<String, Object> metadata) {

        List<String> evidence = new ArrayList<>();

        addIfPresent(evidence, metadata, "memoryId");
        addIfPresent(evidence, metadata, "knowledgeId");
        addIfPresent(evidence, metadata, "reasoningId");
        addIfPresent(evidence, metadata, "planId");

        return String.join("\n• ", prependBullet(evidence));
    }

    private List<String> prependBullet(List<String> values) {

        if (values.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        result.add("• " + values.get(0));

        for (int i = 1; i < values.size(); i++) {
            result.add(values.get(i));
        }

        return result;
    }

    private double confidence(Map<String, Object> metadata) {

        Object value = metadata.get("reasoningConfidence");

        if (value instanceof Number number) {
            double c = number.doubleValue();
            return Math.max(0.0, Math.min(1.0, c));
        }

        return 0.90;
    }

    private String buildAnswer(
            String summary,
            String conclusion,
            String plan
    ) {

        StringBuilder builder = new StringBuilder(summary);

        if (!conclusion.isBlank()) {
            builder.append("\n\nConclusion: ")
                    .append(conclusion);
        }

        if (!plan.isBlank()) {
            builder.append("\n\nNext Step: ")
                    .append(plan);
        }

        return builder.toString();
    }

    private void addIfPresent(
            List<String> list,
            Map<String, Object> metadata,
            String key
    ) {

        String value = string(metadata.get(key));

        if (!value.isBlank()) {
            list.add(value);
        }
    }

    private String string(Object value) {
        return value == null ? "" : value.toString().trim();
    }
/* ==========================================================
       Planning rendering (EO-V1-002)
       ========================================================== */

    /**
     * Detects a routable planning result.
     *
     * <p>Requires BOTH a completed planning result in the pipeline state
     * AND a routed planning operation ({@code PLAN_PROJECT} or
     * {@code CREATE_PLAN}) on the request. Unrouted chat requests that
     * happen to pass through the Planning stage of the canonical pipeline
     * keep the legacy rendering.</p>
     */
    private boolean isPlanningResult(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

        if (!Boolean.TRUE.equals(metadata.get("planningCompleted"))) {
            return false;
        }

        if (string(metadata.get("planId")).isBlank()) {
            return false;
        }

        return isRoutedPlanningOperation(context);
    }

    private boolean isConversationalChat(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

        if (context == null || context.getExecutionRequest() == null) {
            return false;
        }

        // Planning requests are never chat
        if (isRoutedPlanningOperation(context)) {
            return false;
        }

        // Knowledge requests are never chat
        if (isKnowledgeResult(context, metadata)) {
            return false;
        }

        String input = requestText(context).toLowerCase(Locale.ROOT).trim();

        return input.equals("hi")
                || input.equals("hello")
                || input.equals("hello shree")
                || input.equals("hey")
                || input.equals("hey shree");
    }

    /**
     * Checks whether the request was routed to the Planning Kernel.
     */
    private boolean isRoutedPlanningOperation(PipelineContext context) {

        if (context == null) {
            return false;
        }

        Object value = context.getAttribute("requestMetadata");

        if (!(value instanceof Map<?, ?> requestMetadata)) {
            return false;
        }

        Object operation = requestMetadata.get("operation");

        if (operation == null) {
            return false;
        }

        String normalized =
                String.valueOf(operation).trim().toUpperCase(Locale.ROOT);

        return "PLAN_PROJECT".equals(normalized)
                || "CREATE_PLAN".equals(normalized);
    }

    /**
     * Reads the developer-supplied objective from the request metadata
     * (set by the Planning SDK facade).
     */
    private String requestObjective(PipelineContext context) {

        Object value = context == null
                ? null
                : context.getAttribute("requestMetadata");

        if (value instanceof Map<?, ?> requestMetadata) {

            Object objective = requestMetadata.get("objective");

            if (objective != null
                    && !String.valueOf(objective).isBlank()) {

                return String.valueOf(objective).trim();
            }
        }

        return "";
    }

    /**
     * Reads the raw request payload text.
     */
    private String requestText(PipelineContext context) {

        if (context == null
                || context.getExecutionRequest() == null
                || context.getExecutionRequest().getUserInput() == null) {

            return "";
        }

        return context.getExecutionRequest().getUserInput().trim();
    }

    /**
     * Returns the first non-blank value.
     */
    private String firstNonBlank(String... values) {

        for (String value : values) {

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    /**
     * Checks whether the pipeline state carries knowledge results from
     * KnowledgeStage — either through the routed kernel path
     * (QUERY_KNOWLEDGE, SEARCH_KNOWLEDGE) or through the Chief-orchestrated
     * canonical pipeline (CHAT with knowledge-grounded input).
     *
     * <p>Both paths populate {@code knowledgeResults} in the pipeline metadata.
     * The {@code routedKernel} check handles routed requests; the
     * {@code knowledgeResults} check handles Chief-orchestrated CHAT requests
     * where no explicit routing operation is set.</p>
     */
    @SuppressWarnings("unchecked")
    private boolean isKnowledgeResult(
            PipelineContext context,
            Map<String, Object> metadata
    ) {
        // Routed knowledge path (QUERY_KNOWLEDGE, SEARCH_KNOWLEDGE, etc.)
        if ("Knowledge Kernel".equals(metadata.get("routedKernel"))) {
            return true;
        }
        // Chief-orchestrated path (CHAT) — KnowledgeStage populates
        // knowledgeResults even when no explicit operation is set.
        if (metadata.containsKey("knowledgeResults")
                && metadata.get("knowledgeResults") instanceof List<?>
                && !((List<?>) metadata.get("knowledgeResults")).isEmpty()) {
            return true;
        }
        return false;
    }

    private SynthesizedResponse synthesizeKnowledge(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

        Object value = context.getAttribute("requestMetadata");

        String keyword = "";

        if (value instanceof Map<?, ?> requestMetadata) {
            Object k = requestMetadata.get("keyword");
            if (k != null && !k.toString().isBlank()) {
                keyword = k.toString();
            }
        }

        // Sprint-17.3: Derive title from the real question in metadata, not the
        // SDK message literal (which was previously "KNOWLEDGE_QUERY"). The question
        // is stored at requestMetadata.question by KnowledgeSDK.query(). Falls back
        // to the request text (Sprint-12 behavior) when no explicit question was
        // provided — e.g., for Chief-orchestrated CHAT requests routed through
        // KnowledgeStage without an explicit "question" metadata field.
        String realQuestion = "";
        if (value instanceof Map<?, ?> requestMetadata) {
            Object q = requestMetadata.get("question");
            if (q != null && !q.toString().isBlank()) {
                realQuestion = q.toString();
            }
        }

        String title = firstNonBlank(
                string(metadata.get("knowledgeTitle")),
                keyword,
                realQuestion,
                requestText(context)   // Sprint-17.3: fallback to request text when no question metadata
        );

        String summary = string(metadata.get("knowledgeSummary"));

        @SuppressWarnings("unchecked")
        List<KnowledgeNode> results =
                metadata.get("knowledgeResults") instanceof List<?>
                        ? (List<KnowledgeNode>) metadata.get("knowledgeResults")
                        : List.of();

        List<ResponseSection> sections = new ArrayList<>();
        Map<String, Object> structured = new LinkedHashMap<>();

        StringBuilder answer = new StringBuilder();

        answer.append("# ").append(title).append("\n\n");

        if (!summary.isBlank()) {
            answer.append("## Summary\n\n")
                    .append(summary)
                    .append("\n\n");

            sections.add(new ResponseSection("Summary", summary));
        }

        if (!results.isEmpty()) {

            answer.append("## Key Knowledge\n\n");

            StringBuilder keyPoints = new StringBuilder();

            int citationIndex = 0;

            for (KnowledgeNode node : results) {

                citationIndex++;

                answer.append("- **")
                        .append(node.getLabel())
                        .append("**");

                if (node.getDescription() != null &&
                        !node.getDescription().isBlank()) {

                    answer.append(": ")
                            .append(node.getDescription());
                }

                answer.append(" [").append(citationIndex).append("]\n");

                keyPoints.append("• ")
                        .append(node.getLabel());

                if (node.getDescription() != null &&
                        !node.getDescription().isBlank()) {

                    keyPoints.append(" — ")
                            .append(node.getDescription());
                }

                keyPoints.append(" [").append(citationIndex).append("]\n");
            }

            sections.add(new ResponseSection(
                    "Key Knowledge",
                    keyPoints.toString().stripTrailing()
            ));

            structured.put("knowledgeCount", results.size());

            // EO-V1.3 Citations — verifiable references to the knowledge graph
            answer.append("\n## Citations\n\n");

            List<String> citationLines = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {

                KnowledgeCitation citation =
                        KnowledgeCitation.fromNode(i + 1, results.get(i));

                citationLines.add(citation.toMarkdownLine());

                answer.append(citation.toMarkdownLine()).append("\n");
            }

            sections.add(new ResponseSection(
                    "Citations",
                    String.join("\n", citationLines)
            ));

            structured.put("citations", citationLines);
        }

        structured.put("knowledgeTitle", title);

        // Sprint-17.3: Derive confidence from grounding score, not hard-coded 0.95.
        // Low/no grounding → low confidence; strong grounding → high confidence.
        double confidence = deriveKnowledgeConfidence(results, metadata);

        return new SynthesizedResponse(
                answer.toString().trim(),
                sections,
                confidence,
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                structured
        );
    }

    /**
     * Sprint-17.3: Derives confidence for knowledge synthesis based on:
     * 1. Whether results were found (empty graph → low confidence)
     * 2. The knowledgeGroundingScore if available (real grounding signal)
     *
     * @param results  the retrieved knowledge nodes (may be empty)
     * @param metadata the pipeline metadata
     * @return confidence in [0.0, 1.0]
     */
    private double deriveKnowledgeConfidence(
            List<KnowledgeNode> results,
            Map<String, Object> metadata
    ) {
        // No results found — low confidence (no evidence)
        if (results.isEmpty()) {
            return 0.15;  // Sprint-17.3: was 0.95 — now honestly reports no evidence
        }

        // Results found — check grounding score
        Object grounding = metadata.get("knowledgeGroundingScore");
        if (grounding instanceof Number groundingScore) {
            // Use the real grounding score as the confidence signal
            return Math.max(0.0, Math.min(1.0, groundingScore.doubleValue()));
        }

        // Results found but no grounding score — moderate confidence
        return 0.80;
    }

    private SynthesizedResponse synthesizeChat(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

        String userMessage = requestText(context);

        String answer;

        if (userMessage.equalsIgnoreCase("hello shree")
                || userMessage.equalsIgnoreCase("hello")
                || userMessage.equalsIgnoreCase("hi")) {

            answer = """
                Hello! I'm Shree AI.

                How can I help you today?
                """;

        } else {

            answer = "I received your message: \"" + userMessage +
                    "\".\n\nHow can I help you?";
        }

        return new SynthesizedResponse(
                answer.strip(),
                List.of(),
                1.0,
                ResponseStyle.CONVERSATIONAL,
                Instant.now()
        );
    }
/**
     * Renders a structured, human-readable plan from the Planning Kernel
     * outputs preserved in the pipeline state. Only renders information
     * that is actually present — nothing is invented.
     */
    private SynthesizedResponse synthesizePlanning(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

        // Sprint-11: If a PlanBlueprint is present (produced by the domain-aware
        // Planning Kernel), render the rich executive-grade plan with phases,
        // milestones, deliverables, dependencies, risks, and success metrics.
        if (metadata.get("planBlueprint") instanceof PlanBlueprint blueprint) {
            return synthesizePlanningBlueprint(context, metadata, blueprint);
        }

        GoalAnalysis goal =
                metadata.get("goalAnalysis") instanceof GoalAnalysis analysis
                        ? analysis
                        : null;

        PlanningObjective objective =
                metadata.get("planningObjective")
                        instanceof PlanningObjective planningObjective
                        ? planningObjective
                        : null;

        String goalText = firstNonBlank(
                requestObjective(context),
                objective != null ? objective.description() : "",
                goal != null ? goal.normalizedGoal() : "",
                requestText(context)
        );

        if (goalText.isBlank()) {
            goalText = "Plan";
        }

        List<String> subtasks =
                goal != null ? goal.subtasks() : List.of();

        List<String> blockers =
                goal != null ? goal.blockers() : List.of();

        List<String> dependencies =
                goal != null ? goal.dependencies() : List.of();

        List<String> recommendations =
                goal != null ? goal.recommendations() : List.of();

        String title = deriveTitle(goalText);
        String summary = buildPlanningSummary(subtasks);

        StringBuilder answer = new StringBuilder();
        List<ResponseSection> sections = new ArrayList<>();
        Map<String, Object> structuredData = new LinkedHashMap<>();

        // Title
        answer.append("# ")
                .append(title)
                .append("\n\n");

        // Executive summary
        answer.append("## Executive Summary\n\n")
                .append(summary)
                .append("\n\n");
        sections.add(new ResponseSection("Executive Summary", summary));
        structuredData.put("planningSummary", summary);

        // Goal
        answer.append("## Goal\n\n")
                .append(goalText)
                .append("\n\n");
        sections.add(new ResponseSection("Goal", goalText));

        // Feasibility (only when present)
        if (goal != null && goal.feasibility() != null) {
            answer.append("## Feasibility\n\n")
                    .append(goal.feasibility().name())
                    .append("\n\n");
            sections.add(
                    new ResponseSection(
                            "Feasibility",
                            goal.feasibility().name()));
        }

        // Priority (only when present)
        if (goal != null && goal.priority() != null) {
            answer.append("## Priority\n\n")
                    .append(goal.priority().name())
                    .append("\n\n");
            sections.add(
                    new ResponseSection(
                            "Priority",
                            goal.priority().name()));
        }

        // Blockers (only when present)
        if (!blockers.isEmpty()) {
            answer.append("## Blockers\n\n")
                    .append(renderBullets(blockers))
                    .append("\n");
            sections.add(new ResponseSection("Blockers", renderBullets(blockers)));
            structuredData.put("blockers", blockers);
        }

        // Dependencies (only when present)
        if (!dependencies.isEmpty()) {
            answer.append("## Dependencies\n\n")
                    .append(renderBullets(dependencies))
                    .append("\n");
            sections.add(
                    new ResponseSection(
                            "Dependencies",
                            renderBullets(dependencies)));
        }

        // Subtasks, numbered (only when present)
        if (!subtasks.isEmpty()) {
            answer.append("## Subtasks\n\n")
                    .append(renderNumbered(subtasks))
                    .append("\n");
            sections.add(new ResponseSection("Subtasks", renderNumbered(subtasks)));
            structuredData.put("subtasks", subtasks);
        }

        // Recommendations (only when present)
        if (!recommendations.isEmpty()) {
            answer.append("## Recommendations\n\n")
                    .append(renderBullets(recommendations))
                    .append("\n");
            sections.add(
                    new ResponseSection(
                            "Recommendations",
                            renderBullets(recommendations)));
            structuredData.put("recommendations", recommendations);
        }

        return new SynthesizedResponse(
                answer.toString().trim(),
                sections,
                planningConfidence(metadata, goal),
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                structuredData
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sprint-11 — Rich Plan Blueprint Synthesis
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders a rich, executive-grade plan from a Sprint-11
     * {@link PlanBlueprint}. The output is the canonical Sprint-11
     * planning markdown format with phases, milestones, deliverables,
     * dependencies, success criteria, risks, and success metrics.
     */
    private SynthesizedResponse synthesizePlanningBlueprint(
            PipelineContext context,
            Map<String, Object> metadata,
            PlanBlueprint blueprint
    ) {
        StringBuilder answer = new StringBuilder();
        List<ResponseSection> sections = new ArrayList<>();
        Map<String, Object> structuredData = new LinkedHashMap<>();

        String title = blueprint.title();
        String goal = blueprint.goal();
        int weeks = blueprint.timelineWeeks();

        // Title
        answer.append("# ").append(title).append("\n\n");
        sections.add(new ResponseSection("Title", title));
        structuredData.put("title", title);

        // Executive summary
        String summary = "A "
                + (weeks > 0 ? weeks + "-week " : "")
                + "structured plan with "
                + blueprint.phases().size() + " phases and "
                + blueprint.milestones().size() + " milestones.";
        answer.append("## Executive Summary\n\n").append(summary).append("\n\n");
        sections.add(new ResponseSection("Executive Summary", summary));
        structuredData.put("executiveSummary", summary);

        // Goal
        if (goal != null && !goal.isBlank()) {
            answer.append("## Goal\n\n").append(goal).append("\n\n");
            sections.add(new ResponseSection("Goal", goal));
        }

        // Timeline
        if (weeks > 0) {
            answer.append("## Timeline\n\n").append(weeks).append(" Weeks\n\n");
            sections.add(new ResponseSection("Timeline", weeks + " Weeks"));
            structuredData.put("timelineWeeks", weeks);
        }

        // Milestones
        if (!blueprint.milestones().isEmpty()) {
            answer.append("## Milestones\n\n");
            for (Milestone m : blueprint.milestones()) {
                answer.append("* Week ").append(m.estimatedWeek())
                        .append(" — ").append(m.name()).append("\n");
            }
            answer.append("\n");
            sections.add(new ResponseSection("Milestones", renderMilestones(blueprint.milestones())));
            structuredData.put("milestones",
                    blueprint.milestones().stream()
                            .map(Milestone::name)
                            .toList());
        }

        // Phases
        if (!blueprint.phases().isEmpty()) {
            int phaseIndex = 0;
            for (Phase phase : blueprint.phases()) {
                phaseIndex++;
                answer.append("## Phase ").append(phaseIndex)
                        .append(" — ").append(phase.title()).append("\n\n");

                if (phase.objective() != null && !phase.objective().isBlank()) {
                    answer.append("### Objective\n\n")
                            .append(phase.objective())
                            .append("\n\n");
                }

                if (phase.durationWeeks() > 0) {
                    answer.append("### Duration\n\n")
                            .append(phase.durationWeeks()).append(" weeks\n\n");
                }

                if (!phase.deliverables().isEmpty()) {
                    answer.append("### Deliverables\n\n");
                    for (String d : phase.deliverables()) {
                        answer.append("* ").append(d).append("\n");
                    }
                    answer.append("\n");
                }

                if (!phase.dependencies().isEmpty()) {
                    answer.append("### Dependencies\n\n");
                    for (String d : phase.dependencies()) {
                        answer.append("* ").append(d).append("\n");
                    }
                    answer.append("\n");
                }

                if (!phase.successCriteria().isEmpty()) {
                    answer.append("### Success Criteria\n\n");
                    for (String c : phase.successCriteria()) {
                        answer.append("* ").append(c).append("\n");
                    }
                    answer.append("\n");
                }

                sections.add(new ResponseSection("Phase " + phaseIndex + ": " + phase.title(),
                        buildPhaseContent(phase)));
            }
        }
        structuredData.put("phases",
                blueprint.phases().stream().map(Phase::title).toList());

        // Risks
        if (!blueprint.risks().isEmpty()) {
            answer.append("## Risks\n\n");
            for (String risk : blueprint.risks()) {
                answer.append("* ").append(risk).append("\n");
            }
            answer.append("\n");
            sections.add(new ResponseSection("Risks", renderBullets(blueprint.risks())));
            structuredData.put("risks", blueprint.risks());
        }

        // Success metrics
        if (!blueprint.successMetrics().isEmpty()) {
            answer.append("## Success Metrics\n\n");
            for (String metric : blueprint.successMetrics()) {
                answer.append("* ").append(metric).append("\n");
            }
            answer.append("\n");
            sections.add(new ResponseSection("Success Metrics",
                    renderBullets(blueprint.successMetrics())));
            structuredData.put("successMetrics", blueprint.successMetrics());
        }

        // Recommendations
        if (!blueprint.recommendations().isEmpty()) {
            answer.append("## Recommendations\n\n");
            for (String rec : blueprint.recommendations()) {
                answer.append("* ").append(rec).append("\n");
            }
            answer.append("\n");
            sections.add(new ResponseSection("Recommendations",
                    renderBullets(blueprint.recommendations())));
            structuredData.put("recommendations", blueprint.recommendations());
        }

        // Domain tag
        if (blueprint.metadata() != null) {
            structuredData.put("domain",
                    blueprint.metadata().getOrDefault("domain", "GENERAL"));
        }

        return new SynthesizedResponse(
                answer.toString().trim(),
                sections,
                0.92,
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                structuredData
        );
    }

    private String buildPhaseContent(Phase phase) {
        StringBuilder sb = new StringBuilder();
        if (phase.objective() != null && !phase.objective().isBlank()) {
            sb.append("Objective: ").append(phase.objective()).append("\n");
        }
        if (phase.durationWeeks() > 0) {
            sb.append("Duration: ").append(phase.durationWeeks()).append(" weeks\n");
        }
        if (!phase.deliverables().isEmpty()) {
            sb.append("Deliverables:\n").append(renderBullets(phase.deliverables()));
        }
        if (!phase.successCriteria().isEmpty()) {
            sb.append("Success Criteria:\n").append(renderBullets(phase.successCriteria()));
        }
        return sb.toString();
    }

    private String renderMilestones(List<Milestone> milestones) {
        StringBuilder sb = new StringBuilder();
        for (Milestone m : milestones) {
            sb.append("Week ").append(m.estimatedWeek()).append(" — ")
                    .append(m.name()).append("\n");
        }
        return sb.toString().stripTrailing();
    }

/**
     * Deterministic executive summary derived from actual plan data.
     */
    private String buildPlanningSummary(List<String> subtasks) {

        if (subtasks.isEmpty()) {
            return "A structured execution plan has been generated.";
        }

        return "A structured execution plan with "
                + subtasks.size()
                + " steps has been generated.";
    }

    /**
     * Derives a display title from the goal text by removing a leading
     * imperative phrase. The goal content itself is never altered.
     */
    private String deriveTitle(String goal) {

        String cleaned = goal.trim().replaceFirst(
                "(?i)^(create|build|make|design|develop|generate|prepare|plan|set\\s+up|organize|organise)\\s+(an?|the)?\\s*",
                ""
        ).trim();

        if (cleaned.isEmpty()) {
            cleaned = goal.trim();
        }

        if (!cleaned.isEmpty()) {
            cleaned = Character.toUpperCase(cleaned.charAt(0))
                    + cleaned.substring(1);
        }

        return cleaned;
    }

    /**
     * Renders list values as markdown bullets.
     */
    private String renderBullets(List<String> values) {

        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            builder.append("* ")
                    .append(value)
                    .append("\n");
        }

        return builder.toString().stripTrailing();
    }

    /**
     * Renders list values as a numbered markdown list.
     */
    private String renderNumbered(List<String> values) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            builder.append(i + 1)
                    .append(". ")
                    .append(values.get(i))
                    .append("\n");
        }

        return builder.toString().stripTrailing();
    }

    /**
     * Planning confidence from goal intelligence, falling back to the
     * legacy default.
     */
    private double planningConfidence(
            Map<String, Object> metadata,
            GoalAnalysis goal
    ) {

        if (goal != null) {
            return Math.max(0.0, Math.min(1.0, goal.confidence()));
        }

        Object value = metadata.get("goalIntelligenceConfidence");

        if (value instanceof Number number) {
            return Math.max(0.0, Math.min(1.0, number.doubleValue()));
        }

        return 0.90;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sprint-12: Multi-Kernel Composite Synthesis
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Synthesizes a professional response from a multi-kernel orchestration result.
     *
     * <p>Composites the outputs of all kernels into a single, well-structured response
     * with clearly labeled sections. This is called by the orchestrator when multiple
     * kernels have been executed for a single user request.</p>
     *
     * <p>Example output:</p>
     * <pre>
     * # Project Roadmap for AI Assistant
     *
     * ## Project Memory
     * Project has been saved successfully.
     *
     * ## Knowledge Context
     * Retrieved 3 relevant knowledge results about AI development.
     *
     * ## Roadmap
     * Phase 1 — Requirements
     * ...
     *
     * ## Next Action
     * Start with Phase 1: Requirements Analysis
     * </pre>
     *
     * @param context  the pipeline context (used for compatibility)
     * @param composite the composite result from the orchestrator
     * @param analysis the intent analysis result
     * @return a fully-populated SynthesizedResponse with multi-kernel sections
     * @since Sprint-12
     */
    public SynthesizedResponse synthesizeComposite(
            PipelineContext context,
            CompositeKernelResult composite,
            IntentAnalysisResult analysis
    ) {
        StringBuilder answer = new StringBuilder();

        // ── Title ──────────────────────────────────────────────────────────
        String title = deriveCompositeTitle(analysis, composite);
        answer.append("# ").append(title).append("\n\n");

        // ── Memory Section ───────────────────────────────────────────────
        renderMemorySection(answer, composite);

        // ── Knowledge Section ───────────────────────────────────────────
        renderKnowledgeSection(answer, composite);

        // ── Planning Section ────────────────────────────────────────────
        renderPlanningSection(answer, composite);

        // ── Execution Section ───────────────────────────────────────────
        renderExecutionSection(answer, composite);

        // ── Sprint-14: Developer Agent Section ────────────────────────────
        renderDeveloperAgentSection(answer, composite);

        // ── Reflection Section (if present) ─────────────────────────────
        renderReflectionSection(answer, composite);

        // ── Next Action ────────────────────────────────────────────────
        renderNextAction(answer, composite, analysis);

        // Build structured sections
        List<ResponseSection> sections = buildCompositeSections(composite, analysis);

        return new SynthesizedResponse(
                answer.toString().stripTrailing(),
                sections,
                composite.overallConfidence(),
                ResponseStyle.PROFESSIONAL,
                Instant.now()
        );
    }

    /**
     * Derives a display title from the composite result.
     */
    private String deriveCompositeTitle(
            IntentAnalysisResult analysis,
            CompositeKernelResult composite
    ) {
        Map<String, Object> planData = composite.planData();
        String objective = (String) planData.getOrDefault("objective",
                analysis.originalInput());

        if (objective != null && !objective.isBlank()) {
            return deriveTitle(objective);
        }

        return "Orchestrated Response";
    }

    /**
     * Renders the memory section if memory kernel executed.
     */
    private void renderMemorySection(StringBuilder answer, CompositeKernelResult composite) {
        Map<String, Object> memories = composite.storedMemories();
        if (memories == null || memories.isEmpty()) {
            return;
        }

        answer.append("## Project Memory\n\n");
        String storedId = (String) memories.get("memoryId");
        if (storedId != null) {
            answer.append("Project context has been stored successfully.\n\n");
        } else {
            answer.append("Context stored in session.\n\n");
        }
    }

    /**
     * Renders the knowledge section if knowledge kernel executed.
     */
    private void renderKnowledgeSection(StringBuilder answer, CompositeKernelResult composite) {
        Map<String, Object> citations = composite.citations();
        if (citations == null || citations.isEmpty()) {
            return;
        }

        answer.append("## Knowledge Context\n\n");
        Object count = citations.get("knowledgeCount");
        if (count != null) {
            answer.append("Retrieved ").append(count)
                    .append(" relevant knowledge result(s).\n\n");
        } else {
            answer.append("Knowledge context retrieved.\n\n");
        }
    }

    /**
     * Renders the planning section if planning kernel executed.
     */
    private void renderPlanningSection(StringBuilder answer, CompositeKernelResult composite) {
        Map<String, Object> planData = composite.planData();
        if (planData == null || planData.isEmpty()) {
            return;
        }

        answer.append("## Roadmap\n\n");

        String planId = (String) planData.get("planId");
        String domain = (String) planData.getOrDefault("domain", "GENERAL");

        if (planId != null) {
            answer.append("A structured ").append(domain)
                    .append(" roadmap has been generated.\n\n");
        } else {
            answer.append("A structured execution plan has been generated.\n\n");
        }
    }

    /**
     * Renders the execution section if execution kernel executed.
     */
    private void renderExecutionSection(StringBuilder answer, CompositeKernelResult composite) {
        for (CompositeKernelResult.KernelResult kr : composite.kernelResults()) {
            if (kr.kernelType() == IntentAnalysisResult.KernelType.EXECUTION) {
                answer.append("## Execution\n\n");
                answer.append(kr.output()).append("\n\n");
            }
        }
    }

    /**
     * Renders the reflection section if reflection was triggered.
     */
    private void renderReflectionSection(StringBuilder answer, CompositeKernelResult composite) {
        Map<String, Object> reflectionData = composite.reflectionData();
        if (reflectionData == null || reflectionData.isEmpty()) {
            return;
        }

        answer.append("## Reflection\n\n");
        answer.append("Execution was automatically reviewed and lessons recorded.\n\n");
    }

    // ─── Sprint-14: Developer Agent Synthesis ────────────────────────────────

    /**
     * Renders the Developer Agent section in the composite response.
     * Called from {@link #synthesizeComposite} when the Developer Agent kernel executed.
     *
     * @param answer   the StringBuilder to append to
     * @param composite the composite kernel result
     */
    private void renderDeveloperAgentSection(
            StringBuilder answer,
            CompositeKernelResult composite
    ) {
        // Find the Developer Agent kernel result
        for (CompositeKernelResult.KernelResult kr : composite.kernelResults()) {
            if (kr.kernelType() == IntentAnalysisResult.KernelType.DEVELOPER) {
                if (!kr.isSuccess()) {
                    return; // Error was already recorded in kernel output
                }

                answer.append("## Implementation Plan\n\n");
                answer.append(kr.output());
                answer.append("\n\n");

                // Extract and render key metrics
                Map<String, Object> meta = kr.metadata();
                if (meta != null && !meta.isEmpty()) {
                    answer.append("**Plan Summary**\n\n");
                    if (meta.get("intentLabel") != null) {
                        answer.append("* Intent: ").append(meta.get("intentLabel")).append("\n");
                    }
                    if (meta.get("entity") != null && !"".equals(meta.get("entity").toString())) {
                        answer.append("* Entity: ").append(meta.get("entity")).append("\n");
                    }
                    if (meta.get("impact.totalAffected") != null) {
                        answer.append("* Classes Affected: ").append(meta.get("impact.totalAffected")).append("\n");
                    }
                    if (meta.get("plan.phaseCount") != null) {
                        answer.append("* Implementation Phases: ").append(meta.get("plan.phaseCount")).append("\n");
                    }
                    if (meta.get("testStrategy.totalTests") != null) {
                        answer.append("* Recommended Tests: ").append(meta.get("testStrategy.totalTests")).append("\n");
                    }
                    Integer issueCount = (Integer) meta.get("validationIssues");
                    if (issueCount != null && issueCount > 0) {
                        answer.append("* Architecture Issues: ").append(issueCount).append(" (see full report)\n");
                    }
                    answer.append("\n\n");
                }
                break; // Only one developer agent result per request
            }
        }
    }

    /**
     * Renders the next action recommendation.
     */
    private void renderNextAction(
            StringBuilder answer,
            CompositeKernelResult composite,
            IntentAnalysisResult analysis
    ) {
        answer.append("## Next Action\n\n");

        // Determine the most useful next action based on kernel outcomes
        if (composite.planData().containsKey("planId")) {
            answer.append("Proceed with Phase 1 of the generated roadmap.\n");
        } else if (!composite.storedMemories().isEmpty()) {
            answer.append("Context is stored and ready for your next request.\n");
        } else if (!composite.citations().isEmpty()) {
            answer.append("Review the knowledge context above for next steps.\n");
        } else {
            answer.append("All requested operations completed successfully.\n");
        }
    }

    /**
     * Builds structured response sections from the composite result.
     */
    private List<ResponseSection> buildCompositeSections(
            CompositeKernelResult composite,
            IntentAnalysisResult analysis
    ) {
        List<ResponseSection> sections = new ArrayList<>();

        // Memory section
        if (!composite.storedMemories().isEmpty()) {
            String memoryId = (String) composite.storedMemories().get("memoryId");
            String content = memoryId != null
                    ? "Context stored: " + memoryId
                    : "Context stored in session";
            sections.add(new ResponseSection("Project Memory", content));
        }

        // Knowledge section
        if (!composite.citations().isEmpty()) {
            int count = ((Number) composite.citations()
                    .getOrDefault("knowledgeCount", 0)).intValue();
            sections.add(new ResponseSection(
                    "Knowledge Context",
                    "Retrieved " + count + " knowledge result(s)"
            ));
        }

        // Planning section
        if (!composite.planData().isEmpty()) {
            String planId = (String) composite.planData().getOrDefault("planId", "generated");
            sections.add(new ResponseSection("Roadmap", "Plan: " + planId));
        }

        // Execution section
        for (CompositeKernelResult.KernelResult kr : composite.kernelResults()) {
            if (kr.kernelType() == IntentAnalysisResult.KernelType.EXECUTION) {
                sections.add(new ResponseSection("Execution", kr.output()));
            }
        }

        return sections;
    }
}