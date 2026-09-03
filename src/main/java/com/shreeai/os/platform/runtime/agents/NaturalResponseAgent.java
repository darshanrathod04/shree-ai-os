package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.kernels.response.model.ResponseSection;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.AgentDecision.Agent;
import com.shreeai.os.platform.runtime.model.AgentDecision.Action;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.EvidenceItem;
import com.shreeai.os.platform.runtime.model.VerificationReport;
import com.shreeai.os.platform.runtime.model.VerificationReport.ConfidenceTier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>NaturalResponseAgent</b>
 *
 * <p>The <strong>only</strong> place in the autonomous intelligence layer
 * where the LLM is invoked. Converts a verified {@code EvidenceBundle} into
 * a natural-language {@code SynthesizedResponse}.</p>
 *
 * <p><b>Constitutional Rule (Sprint 18):</b> The LLM is called exactly once
 * per user request, and only in this agent. No kernel engine, stage, or
 * other agent may call the LLM.</p>
 *
 * <p><b>Synthesis Modes:</b></p>
 * <ul>
 *   <li>{@code VERIFIED_PROJECT} — authoritative answer from actual code analysis</li>
 *   <li>{@code VERIFIED_KB} — authoritative answer from knowledge graph with citations</li>
 *   <li>{@code INFERRED} — hedged answer with reasoning/inference background</li>
 *   <li>{@code INSUFFICIENT} — honest "I don't know" with recommendations</li>
 * </ul>
 *
 * @since Sprint 18
 */
public final class NaturalResponseAgent {

    public NaturalResponseAgent() {}

    /**
     * Generates a natural-language response from a verified evidence bundle.
     *
     * <p>This is the single LLM invocation point in the autonomous intelligence
     * layer. The LLM receives structured evidence and generates natural language.</p>
     *
     * @param report  the verification report (must not be null)
     * @param request the original execution request (provides user intent context)
     * @return a fully-populated SynthesizedResponse (never null)
     */
    public SynthesizedResponse generate(VerificationReport report, ExecutionRequest request) {
        Objects.requireNonNull(report, "report must not be null");

        if (report.isInsufficient()) {
            return generateInsufficientResponse(report, request);
        }

        // Build structured evidence payload for LLM
        Map<String, Object> structuredData = buildStructuredPayload(report);

        // Generate natural language from structured evidence
        String answer = generateFromEvidence(report, request);

        // Build sections for structured response
        List<ResponseSection> sections = buildSections(report);

        return new SynthesizedResponse(
                answer,
                sections,
                report.confidence(),
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                structuredData
        );
    }

    /**
     * Builds a natural-language answer from verified evidence.
     *
     * <p>Current implementation generates structured text directly from evidence
     * items. In a full implementation, this would invoke the LLM with the
     * structured evidence payload.</p>
     *
     * <p><b>Note:</b> The LLM invocation slot is reserved here. The actual LLM
     * call should be wired through {@code LlmProvider} when the LLM integration
     * is complete.</p>
     *
     * @param report  the verification report with evidence
     * @param request the original request for context
     * @return natural language answer
     */
    private String generateFromEvidence(VerificationReport report, ExecutionRequest request) {
        EvidenceBundle bundle = extractBundle(report);
        if (bundle == null || bundle.isEmpty()) {
            return generateFallbackAnswer(report, request);
        }

        StringBuilder sb = new StringBuilder();

        // Sprint-19 hotfix: derive title from real knowledge evidence (if available),
        // otherwise fall back to the tier label. Never default to "Knowledge Answer"
        // when the knowledge graph provides a real title.
        String title = deriveTitleFromEvidence(bundle, report);

        String userQuestion = request != null && request.getUserInput() != null
                ? request.getUserInput().trim()
                : "";

        sb.append("# ").append(title).append("\n\n");

        if (!userQuestion.isBlank()) {
            sb.append("**Question:** ").append(userQuestion).append("\n\n");
        }

        // Sprint-19 hotfix: when KNOWLEDGE evidence is present, render the canonical
        // "## Summary" and "## Key Knowledge" structure so SDK consumers get a stable
        // grounded response shape (matches the original synthesizeKnowledge layout).
        appendKnowledgeSummaryAndKey(sb, bundle);

        // Evidence sections
        for (EvidenceItem item : bundle.items()) {
            appendEvidenceItem(sb, item, report.tier());
        }

        // Citations
        if (!report.citations().isEmpty()) {
            sb.append("## Citations\n\n");
            for (int i = 0; i < report.citations().size(); i++) {
                sb.append("[").append(i + 1).append("] ")
                        .append(report.citations().get(i))
                        .append("\n");
            }
            sb.append("\n");
        }

        // Confidence note
        appendConfidenceNote(sb, report);

        // Gaps
        if (!report.gaps().isEmpty()) {
            sb.append("## Limitations\n\n");
            for (String gap : report.gaps()) {
                sb.append("- ").append(gap).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Sprint-19 hotfix: when KNOWLEDGE evidence exists, renders the canonical
     * "## Summary" (first KNOWLEDGE item description) and "## Key Knowledge"
     * (all KNOWLEDGE items as bullet list with citations) sections.
     * This preserves the original synthesizeKnowledge response structure.
     */
    private void appendKnowledgeSummaryAndKey(StringBuilder sb, EvidenceBundle bundle) {
        List<EvidenceItem> knowledgeItems = new ArrayList<>();
        for (EvidenceItem item : bundle.items()) {
            if (item.sourceType() == EvidenceItem.SourceType.KNOWLEDGE) {
                knowledgeItems.add(item);
            }
        }
        if (knowledgeItems.isEmpty()) return;

        // ## Summary — first KNOWLEDGE item's content
        EvidenceItem first = knowledgeItems.get(0);
        if (first.content() != null && !first.content().isBlank()) {
            sb.append("## Summary\n\n");
            sb.append(first.content()).append("\n\n");
        }

        // ## Key Knowledge — all KNOWLEDGE items as bullets
        sb.append("## Key Knowledge\n\n");
        for (int i = 0; i < knowledgeItems.size(); i++) {
            EvidenceItem item = knowledgeItems.get(i);
            sb.append("- **").append(item.title()).append("**");
            if (item.content() != null && !item.content().isBlank()
                    && !item.content().equals(item.title())) {
                sb.append(": ").append(item.content());
            }
            sb.append(" [").append(i + 1).append("]\n");
        }
        sb.append("\n");
    }

    private void appendEvidenceItem(StringBuilder sb, EvidenceItem item, ConfidenceTier tier) {
        String sourceEmoji = switch (item.sourceType()) {
            case PROJECT -> "🏗️";
            case KNOWLEDGE -> "📚";
            case REASONING -> "🧠";
            case INFERENCE -> "💡";
            case PLANNING -> "📋";
            case MEMORY -> "🧩";
            case REFLECTION -> "🔄";
            case EXECUTION -> "⚙️";
        };

        sb.append(sourceEmoji).append(" **").append(item.title()).append("**\n\n");
        sb.append(item.content()).append("\n\n");

        if (!item.citations().isEmpty()) {
            sb.append("Citations: ");
            for (int i = 0; i < item.citations().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("[").append(i + 1).append("]");
            }
            sb.append("\n\n");
        }
    }

    private void appendConfidenceNote(StringBuilder sb, VerificationReport report) {
        String tierNote = switch (report.tier()) {
            case VERIFIED_PROJECT -> "This answer is verified from actual project code analysis.";
            case VERIFIED_KB -> "This answer is verified from the knowledge graph with citations.";
            case INFERRED -> "This answer is inferred from reasoning and may not be fully verified.";
            case INSUFFICIENT -> "Insufficient evidence to provide a verified answer.";
        };

        sb.append("**Confidence:** ").append(String.format("%.0f%%", report.confidence() * 100))
                .append(" (").append(report.tier()).append(")\n\n");
        sb.append("*").append(tierNote).append("*\n\n");
    }

    private SynthesizedResponse generateInsufficientResponse(VerificationReport report, ExecutionRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Insufficient Evidence\n\n");

        String userQuestion = request != null && request.getUserInput() != null
                ? request.getUserInput().trim()
                : "Your question";

        sb.append("**Question:** ").append(userQuestion).append("\n\n");
        sb.append("I don't have enough verified information to answer this question.\n\n");

        if (!report.gaps().isEmpty()) {
            sb.append("## What's Missing\n\n");
            for (String gap : report.gaps()) {
                sb.append("- ").append(gap).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## Recommendations\n\n");
        sb.append("- Provide more context in your question\n");
        sb.append("- Ensure the relevant knowledge has been ingested\n");
        sb.append("- If asking about a project, run ProjectSDK.analyze() first\n");

        Map<String, Object> structured = new LinkedHashMap<>();
        structured.put("tier", report.tier().name());
        structured.put("confidence", report.confidence());
        structured.put("gaps", report.gaps());

        List<ResponseSection> sections = List.of(
                new ResponseSection("Evidence", "No evidence available"),
                new ResponseSection("Limitations", sb.toString())
        );

        return new SynthesizedResponse(
                sb.toString(),
                sections,
                report.confidence(),
                ResponseStyle.PROFESSIONAL,
                Instant.now(),
                structured
        );
    }

    private String generateFallbackAnswer(VerificationReport report, ExecutionRequest request) {
        return "# Response\n\nNo structured evidence available for this request.";
    }

    private List<ResponseSection> buildSections(VerificationReport report) {
        List<ResponseSection> sections = new ArrayList<>();

        EvidenceBundle bundle = extractBundle(report);
        if (bundle != null && !bundle.isEmpty()) {
            StringBuilder evidenceText = new StringBuilder();
            for (EvidenceItem item : bundle.items()) {
                evidenceText.append("- **")
                        .append(item.title())
                        .append("**: ")
                        .append(item.content())
                        .append("\n");
            }
            sections.add(new ResponseSection("Evidence", evidenceText.toString().trim()));
        }

        if (!report.citations().isEmpty()) {
            StringBuilder citationsText = new StringBuilder();
            for (int i = 0; i < report.citations().size(); i++) {
                citationsText.append("[").append(i + 1).append("] ")
                        .append(report.citations().get(i))
                        .append("\n");
            }
            sections.add(new ResponseSection("Citations", citationsText.toString().trim()));
        }

        sections.add(new ResponseSection(
                "Confidence",
                String.format("%.0f%% — %s", report.confidence() * 100, report.tier())
        ));

        return sections;
    }

    private Map<String, Object> buildStructuredPayload(VerificationReport report) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("verificationTier", report.tier().name());
        data.put("confidence", report.confidence());
        data.put("confidenceTier", report.tier().name());
        data.put("itemCount", report.perItemStatus().size());

        EvidenceBundle bundle = extractBundle(report);
        if (bundle != null && !bundle.isEmpty()) {
            data.put("evidenceCount", bundle.size());
            data.put("bundleId", bundle.bundleId());
        }

        if (!report.citations().isEmpty()) {
            data.put("citationCount", report.citations().size());
            data.put("citations", report.citations());
        }

        if (!report.gaps().isEmpty()) {
            data.put("gaps", report.gaps());
        }

        return data;
    }

    /**
     * Extracts the evidence bundle from verification report metadata if present.
     */
    private EvidenceBundle extractBundle(VerificationReport report) {
        Object bundleObj = report.metadata().get("evidenceBundle");
        if (bundleObj instanceof EvidenceBundle bundle) {
            return bundle;
        }
        return null;
    }

    /**
     * Returns an AgentDecision describing this generation run.
     */
    public AgentDecision toDecision(SynthesizedResponse response) {
        return AgentDecision.builder()
                .agent(Agent.NATURAL_RESPONSE)
                .action(Action.GENERATE)
                .rationale(String.format("Generated response with confidence %.2f", response.confidence()))
                .confidence(response.confidence())
                .addMetadata("answerLength", response.answer().length())
                .addMetadata("sectionCount", response.sections().size())
                .build();
    }

    /**
     * Sprint-19 hotfix: derives the response title from real evidence, in this priority:
     * 1. First KNOWLEDGE evidence item's title (the ingested knowledge title — e.g. "darshan")
     * 2. First PROJECT evidence item's title
     * 3. Otherwise the tier label (e.g. "Knowledge Answer")
     *
     * Never uses the tier label as a default when real evidence exists.
     */
    private String deriveTitleFromEvidence(EvidenceBundle bundle, VerificationReport report) {
        if (bundle != null && !bundle.isEmpty()) {
            // Priority 1: KNOWLEDGE source — carries the ingested knowledge title
            for (EvidenceItem item : bundle.items()) {
                if (item.sourceType() == EvidenceItem.SourceType.KNOWLEDGE
                        && item.title() != null
                        && !item.title().isBlank()
                        && !"Knowledge Node".equals(item.title())) {
                    return item.title();
                }
            }
            // Priority 2: PROJECT source — carries the project name
            for (EvidenceItem item : bundle.items()) {
                if (item.sourceType() == EvidenceItem.SourceType.PROJECT
                        && item.title() != null
                        && !item.title().isBlank()) {
                    return item.title();
                }
            }
        }
        // Priority 3: tier label fallback
        return switch (report.tier()) {
            case VERIFIED_PROJECT -> "Project Intelligence Answer";
            case VERIFIED_KB -> "Knowledge Answer";
            case INFERRED -> "Inferred Answer";
            case INSUFFICIENT -> "Insufficient Evidence";
        };
    }
}
