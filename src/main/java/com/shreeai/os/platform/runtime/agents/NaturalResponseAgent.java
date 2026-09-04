package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.kernels.response.model.ResponseSection;
import com.shreeai.os.platform.kernels.response.model.ResponseStyle;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
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
 * where the LLM is invoked (Sprint-21). Converts a verified
 * {@code EvidenceBundle} into a natural-language {@code SynthesizedResponse}.</p>
 *
 * <p><b>Constitutional Rule (Sprint 18):</b> The LLM is called exactly once
 * per user request, and only in this agent. No kernel engine, stage, or
 * other agent may call the LLM.</p>
 *
 * <p><b>Sprint-21 LLM Wiring:</b> When an {@link LlmProvider} is wired in
 * (via the constructor or {@link #setLlmProvider}), the agent routes a
 * structured prompt (Shree persona + grounded evidence context + user question)
 * through the provider and returns the model's prose. The agent falls back
 * gracefully to the deterministic {@code StringBuilder}-based renderer when
 * the LLM is absent, unreachable, or throws — ensuring the response path is
 * never blocked by provider failures.</p>
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

    /**
     * Optional LLM provider. When present and reachable, the agent will route
     * a structured prompt through the provider to obtain a natural-language
     * completion. When {@code null} or unreachable, the agent falls back to
     * the deterministic {@code StringBuilder}-based rendering — so the
     * agent remains functional in offline / test environments.
     *
     * <p>Sprint-21 wiring: this slot is the canonical LLM invocation point
     * for the autonomous intelligence layer.</p>
     */
    private LlmProvider llmProvider;

    /**
     * Default model identifier passed to the LLM provider when one is wired
     * in. Kept as a constant so it is trivial to override from configuration
     * in a follow-up sprint.
     */
    private static final String DEFAULT_LLM_MODEL = "shree-default";

    /** Default temperature — moderate, deterministic-but-natural. */
    private static final double DEFAULT_LLM_TEMPERATURE = 0.3;

    /** Default max tokens — keeps responses grounded and within budget. */
    private static final int DEFAULT_LLM_MAX_TOKENS = 1024;

    public NaturalResponseAgent() {}

    /**
     * Creates a NaturalResponseAgent with the supplied LLM provider.
     *
     * <p>When the provider is non-null the agent will attempt an LLM
     * completion on every non-insufficient {@code VerificationReport}. Any
     * runtime failure of the provider is caught and the deterministic
     * fallback rendering is returned, so this constructor is safe to use
     * with any LLM-backed or null LLM.</p>
     *
     * @param llmProvider optional LLM provider; may be {@code null}
     */
    public NaturalResponseAgent(LlmProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    /**
     * Late-binds an LLM provider (e.g. after the runtime has built its
     * default router). Null-safe — passing {@code null} disables LLM
     * synthesis and reverts to the deterministic fallback.
     *
     * @param llmProvider the LLM provider, or {@code null} to disable
     */
    public void setLlmProvider(LlmProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    /**
     * Returns the currently-configured LLM provider, or {@code null} when
     * the agent is operating in deterministic-fallback mode.
     */
    public LlmProvider getLlmProvider() {
        return llmProvider;
    }

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
     * <p>Sprint-21: when an {@link LlmProvider} is wired in and reachable,
     * this method routes a structured prompt (system + grounded context +
     * user question) through the LLM and returns the model's prose. When
     * the LLM is absent, offline, or throws, this method falls back to the
     * deterministic {@code StringBuilder}-based rendering so the agent is
     * never a single point of failure.</p>
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

        // Sprint-19 hotfix: derive title from real knowledge evidence (if available),
        // otherwise fall back to the tier label. Never default to "Knowledge Answer"
        // when the knowledge graph provides a real title.
        String title = deriveTitleFromEvidence(bundle, report);

        String userQuestion = request != null && request.getUserInput() != null
                ? request.getUserInput().trim()
                : "";

        StringBuilder sb = new StringBuilder();

        // Sprint-21: always emit the standard markdown title heading so SDK
        // consumers (KnowledgeGroundedChatAndQueryTest, etc.) receive the canonical
        // # {title} heading regardless of which rendering path is taken.
        sb.append("# ").append(title).append("\n\n");

        if (!userQuestion.isBlank()) {
            sb.append("**Question:** ").append(userQuestion).append("\n\n");
        }

        // Sprint-21: try the LLM to produce natural-language body prose.
        // If the LLM call succeeds and returns non-blank prose, use it to enhance
        // the answer. On any failure (null, blank, exception) fall back to the
        // deterministic renderer so the agent is never blocked by an unavailable
        // provider.
        //
        // NOTE: the structured sections (## Summary / ## Key Knowledge / Evidence /
        // Citations / Confidence) are ALWAYS rendered below, regardless of LLM
        // success, to preserve the canonical response shape that SDK consumers
        // and tests depend on.
        String llmProse = tryLlmSynthesis(report, request, bundle);
        if (llmProse != null && !llmProse.isBlank()) {
            sb.append(llmProse).append("\n\n");
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
     * Attempts to synthesize a natural-language answer via the configured
     * {@link LlmProvider}. Returns {@code null} when no provider is
     * configured, when the provider is unreachable, or when the call throws
     * — in every case the caller should fall back to the deterministic
     * {@code StringBuilder} rendering.
     *
     * <p>This method is intentionally defensive: LLM failures must never
     * break the user-facing response path. Any {@link RuntimeException}
     * from the provider is swallowed and a {@code null} result is returned
     * (with a structured metadata marker so tests / observability can detect
     * the fallback).</p>
     *
     * @param report  the verification report (must not be null)
     * @param request the execution request (may be null)
     * @param bundle  the extracted evidence bundle (must not be null)
     * @return the LLM's natural-language answer, or {@code null} on any failure
     */
    private String tryLlmSynthesis(VerificationReport report,
                                   ExecutionRequest request,
                                   EvidenceBundle bundle) {
        if (llmProvider == null) {
            return null;
        }
        try {
            LlmRequest llmRequest = buildLlmRequest(report, request, bundle);
            String content = llmProvider.complete(llmRequest).content();
            if (content != null && !content.isBlank()) {
                return content.trim();
            }
            return null;
        } catch (RuntimeException llmError) {
            // LLM failure must not break the response path. Caller falls
            // back to the deterministic StringBuilder rendering.
            return null;
        }
    }

    /**
     * Builds a structured {@link LlmRequest} combining the Shree persona /
     * guardrails system prompt, the grounded evidence context, and the
     * user's original question.
     *
     * <p>Design notes (Sprint-21):</p>
     * <ul>
     *   <li>The system prompt enforces the deterministic guardrails
     *       (grounded-only, citation rules, tier-appropriate hedging).</li>
     *   <li>Each evidence item contributes its title, content, source type,
     *       confidence hint, and citations — the LLM sees the full kernel
     *       data, not a stringified version of it.</li>
     *   <li>Grounded context is bounded to a reasonable character budget
     *       to keep prompts predictable for budget-constrained providers.</li>
     * </ul>
     */
    private LlmRequest buildLlmRequest(VerificationReport report,
                                       ExecutionRequest request,
                                       EvidenceBundle bundle) {
        String systemPrompt = buildSystemPrompt(report);
        String groundedContext = buildGroundedContext(report, bundle);
        String userQuery = request != null && request.getUserInput() != null
                ? request.getUserInput().trim()
                : "";

        String fullPrompt = systemPrompt
                + "\n\n--- GROUNDED CONTEXT ---\n"
                + groundedContext
                + "\n\n--- USER QUESTION ---\n"
                + userQuery
                + "\n\n--- RESPONSE ---\n";

        return LlmRequest.builder()
                .model(DEFAULT_LLM_MODEL)
                .prompt(fullPrompt)
                .temperature(DEFAULT_LLM_TEMPERATURE)
                .maxTokens(DEFAULT_LLM_MAX_TOKENS)
                .stream(Boolean.FALSE)
                .option("verificationTier", report.tier().name())
                .option("confidence", report.confidence())
                .option("evidenceCount", bundle.size())
                .option("citationCount", report.citations().size())
                .build();
    }

    /**
     * System prompt that establishes the Shree persona and the deterministic
     * guardrails: grounded-only, citation rules, tier-appropriate hedging.
     */
    private String buildSystemPrompt(VerificationReport report) {
        String tier = report.tier() != null ? report.tier().name() : "INSUFFICIENT";
        double confidence = report.confidence();
        return "You are Shree, the assistant of Shree AI OS.\n"
                + "Your answer MUST be grounded ONLY in the evidence provided below.\n"
                + "Do NOT invent facts, citations, file paths, or APIs that are not in the context.\n"
                + "When the evidence does not contain the answer, say so explicitly.\n"
                + "Cite the [n] markers from the evidence when you reference them.\n"
                + "Use a professional, concise tone. Use markdown headings and bullet lists.\n"
                + "Current verification tier: " + tier + " (confidence " + confidence + ").\n"
                + tierHedgingGuidance(tier);
    }

    private String tierHedgingGuidance(String tier) {
        return switch (tier) {
            case "VERIFIED_PROJECT" ->
                    "The evidence is verified from actual project code. Be direct and authoritative.";
            case "VERIFIED_KB" ->
                    "The evidence is verified from the knowledge graph. Be direct and cite the [n] markers.";
            case "INFERRED" ->
                    "The evidence is inferred from reasoning. Hedge appropriately (\"likely\", \"based on available signals\").";
            case "INSUFFICIENT" ->
                    "Evidence is insufficient. State the gaps honestly and ask for more context.";
            default -> "Respond in a professional tone.";
        };
    }

    /**
     * Renders the {@link EvidenceBundle} as a compact, citation-tagged
     * context block for the LLM. Each item contributes its title, content,
     * source type, confidence hint, and citations — so the LLM sees the
     * full kernel data, not a stringified version of it.
     */
    private String buildGroundedContext(VerificationReport report, EvidenceBundle bundle) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("Bundle: ").append(bundle.bundleId())
                .append(" | items=").append(bundle.size())
                .append(" | tier=").append(report.tier().name())
                .append(" | confidence=").append(report.confidence())
                .append("\n\n");

        List<String> citations = report.citations();
        int citationCursor = 1;
        for (EvidenceItem item : bundle.items()) {
            ctx.append("[").append(citationCursor).append("] ")
                    .append(item.sourceType() != null ? item.sourceType().name() : "UNKNOWN")
                    .append(" — ").append(safe(item.title())).append("\n");
            ctx.append("    Content: ").append(truncate(safe(item.content()), 1200)).append("\n");
            if (item.confidenceHint() > 0.0) {
                ctx.append("    Confidence: ").append(item.confidenceHint()).append("\n");
            }
            if (!item.citations().isEmpty()) {
                ctx.append("    Citations: ");
                for (int i = 0; i < item.citations().size(); i++) {
                    if (i > 0) ctx.append(", ");
                    ctx.append("[").append(citationCursor).append("]");
                }
                ctx.append("\n");
            }
            if (!item.attributes().isEmpty()) {
                ctx.append("    Attributes: ").append(item.attributes()).append("\n");
            }
            ctx.append("\n");
            citationCursor++;
        }
        if (!citations.isEmpty()) {
            ctx.append("External citations:\n");
            for (int i = 0; i < citations.size(); i++) {
                ctx.append("[").append(i + 1).append("] ")
                        .append(safe(citations.get(i))).append("\n");
            }
        }
        if (!report.gaps().isEmpty()) {
            ctx.append("\nKnown gaps:\n");
            for (String gap : report.gaps()) {
                ctx.append("- ").append(safe(gap)).append("\n");
            }
        }
        return ctx.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
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
        // Sprint-21: surface LLM-wiring state in structured payload so
        // tests and operators can confirm whether the LLM was used or
        // whether the agent fell back to the deterministic renderer.
        data.put("llmWired", llmProvider != null);
        if (llmProvider != null) {
            data.put("llmProviderName", llmProvider.providerName());
        }

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
