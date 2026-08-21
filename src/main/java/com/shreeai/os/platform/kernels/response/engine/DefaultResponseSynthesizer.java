package com.shreeai.os.platform.kernels.response.engine;

import com.shreeai.os.platform.kernels.response.api.ResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.model.ResponseSection;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

        List<ResponseSection> sections = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        String summary = extractSummary(metadata);
        String conclusion = string(metadata.get("reasoningConclusion"));
        String plan = string(metadata.get("planSummary"));

        // Executive Summary
        sections.add(new ResponseSection(
                "Executive Summary",
                List.of(summary)
        ));

        // Findings
        List<String> findings = new ArrayList<>();

        addIfPresent(findings, metadata, "reasoningSummary");
        addIfPresent(findings, metadata, "reasoningConclusion");
        addIfPresent(findings, metadata, "planSummary");

        if (!findings.isEmpty()) {
            sections.add(new ResponseSection("Key Findings", findings));
        }

        // Recommendations
        if (!plan.isBlank()) {
            sections.add(new ResponseSection(
                    "Recommended Next Step",
                    List.of(plan)
            ));
        }

        // Evidence
        addIfPresent(evidence, metadata, "memoryId");
        addIfPresent(evidence, metadata, "knowledgeId");
        addIfPresent(evidence, metadata, "reasoningId");
        addIfPresent(evidence, metadata, "planId");

        double confidence = confidence(metadata);

        String answer = buildAnswer(summary, conclusion, plan);

        return new SynthesizedResponse(
                answer,
                summary,
                ResponseStyle.ANALYSIS,
                sections,
                evidence,
                confidence,
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

        StringBuilder builder = new StringBuilder();

        builder.append(summary);

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
}