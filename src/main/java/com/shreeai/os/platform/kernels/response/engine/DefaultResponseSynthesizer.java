package com.shreeai.os.platform.kernels.response.engine;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.ResponseSection;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

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

        // Planning results render as a structured, human-readable plan.
        // Detection requires BOTH a completed planning result in the state
        // AND a routed planning operation on the request, so unrouted chat
        // requests keep the exact legacy rendering behavior.
        if (isPlanningResult(context, metadata)) {
            return synthesizePlanning(context, metadata);
        }

        return synthesizeDefault(metadata);
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
     * Renders a structured, human-readable plan from the Planning Kernel
     * outputs preserved in the pipeline state. Only renders information
     * that is actually present — nothing is invented.
     */
    private SynthesizedResponse synthesizePlanning(
            PipelineContext context,
            Map<String, Object> metadata
    ) {

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
}