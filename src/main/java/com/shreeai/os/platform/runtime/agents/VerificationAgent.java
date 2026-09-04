package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.runtime.confidence.ConfidenceCalculator;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.AgentDecision.Agent;
import com.shreeai.os.platform.runtime.model.AgentDecision.Action;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.EvidenceItem;
import com.shreeai.os.platform.runtime.model.EvidenceItem.SourceType;
import com.shreeai.os.platform.runtime.model.VerificationReport;
import com.shreeai.os.platform.runtime.model.VerificationReport.ConfidenceTier;
import com.shreeai.os.platform.runtime.model.VerificationReport.ItemStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>VerificationAgent</b>
 *
 * <p>Validates each evidence item in an {@code EvidenceBundle} and assigns
 * an overall confidence tier using {@code ConfidenceCalculator}. The result
 * is a {@code VerificationReport} that drives response quality.</p>
 *
 * <p><b>Verification Logic:</b></p>
 * <ul>
 *   <li>{@code PROJECT} items → always VERIFIED (code is the source of truth)</li>
 *   <li>{@code KNOWLEDGE} items with citations → VERIFIED</li>
 *   <li>{@code KNOWLEDGE} items without citations → UNVERIFIED</li>
 *   <li>{@code REASONING/INFERENCE} items → VERIFIED (kernel attests reasoning)</li>
 *   <li>{@code MEMORY} items → UNVERIFIED (memory can be stale)</li>
 *   <li>{@code EXECUTION} items → VERIFIED (kernel produced result)</li>
 *   <li>Empty bundle → INSUFFICIENT</li>
 * </ul>
 *
 * <p><b>Confidence Tier Assignment:</b></p>
 * <ul>
 *   <li>Has PROJECT evidence → VERIFIED_PROJECT (0.95)</li>
 *   <li>Has KNOWLEDGE evidence with citations → VERIFIED_KB (0.80)</li>
 *   <li>Has REASONING/INFERENCE evidence → INFERRED (0.60)</li>
 *   <li>No evidence → INSUFFICIENT (0.15)</li>
 * </ul>
 *
 * @since Sprint 18
 */
public final class VerificationAgent {

    /** Maximum age of evidence in milliseconds before it's considered stale (1 hour). */
    private static final long MAX_EVIDENCE_AGE_MS = 3_600_000L;

    public VerificationAgent() {}

    /**
     * Verifies all evidence items in the bundle and produces a verification report.
     *
     * @param bundle the evidence bundle to verify (must not be null)
     * @return a fully-populated VerificationReport (never null)
     */
    public VerificationReport verify(EvidenceBundle bundle) {
        Objects.requireNonNull(bundle, "bundle must not be null");

        VerificationReport.Builder reportBuilder = VerificationReport.builder();
        List<String> citations = new ArrayList<>();
        List<String> gaps = new ArrayList<>();

        if (bundle.isEmpty()) {
            return reportBuilder
                    .tier(ConfidenceTier.INSUFFICIENT)
                    .confidence(ConfidenceCalculator.fromInsufficient())
                    .addGap("No evidence available from any kernel")
                    .build();
        }

        boolean hasProject = false;
        boolean hasKnowledge = false;
        boolean hasReasoning = false;

        for (EvidenceItem item : bundle.items()) {
            ItemStatus status = verifyItem(item);
            reportBuilder.putItemStatus(item.itemId(), status);

            switch (item.sourceType()) {
                case PROJECT -> {
                    hasProject = true;
                    citations.addAll(item.citations());
                }
                case KNOWLEDGE -> {
                    hasKnowledge = true;
                    citations.addAll(item.citations());
                }
                case REASONING, INFERENCE, PLANNING, REFLECTION, MEMORY, EXECUTION -> {
                    hasReasoning = true;
                }
                default -> {}
            }
        }

        // Assign tier based on evidence composition
        ConfidenceTier tier;
        double confidence;

        if (hasProject) {
            tier = ConfidenceTier.VERIFIED_PROJECT;
            confidence = ConfidenceCalculator.fromProjectEvidence();
        } else if (hasKnowledge) {
            tier = ConfidenceTier.VERIFIED_KB;
            confidence = ConfidenceCalculator.fromKnowledgeEvidence();
        } else if (hasReasoning) {
            tier = ConfidenceTier.INFERRED;
            confidence = ConfidenceCalculator.fromReasoningEvidence();
        } else {
            tier = ConfidenceTier.INSUFFICIENT;
            confidence = ConfidenceCalculator.fromInsufficient();
            gaps.add("No verified or inferred evidence available");
        }

        // Check for stale evidence
        long now = System.currentTimeMillis();
        for (EvidenceItem item : bundle.items()) {
            if (item.producedAtMillis() > 0 && (now - item.producedAtMillis()) > MAX_EVIDENCE_AGE_MS) {
                gaps.add("Evidence item '" + item.itemId() + "' is stale (>1 hour old)");
            }
        }

        return reportBuilder
                .tier(tier)
                .confidence(confidence)
                .citations(citations)
                .gaps(gaps)
                .addMetadata("evidenceBundle", bundle)
                .addMetadata("bundleId", bundle.bundleId())
                .addMetadata("itemCount", bundle.size())
                .addMetadata("hasProject", hasProject)
                .addMetadata("hasKnowledge", hasKnowledge)
                .addMetadata("hasReasoning", hasReasoning)
                .build();
    }

    /**
     * Verifies a single evidence item.
     *
     * <p>Verification rules:</p>
     * <ul>
     *   <li>PROJECT → always VERIFIED (source of truth)</li>
     *   <li>KNOWLEDGE with citations → VERIFIED</li>
     *   <li>KNOWLEDGE without citations → UNVERIFIED</li>
     *   <li>REASONING/INFERENCE/PLANNING → VERIFIED (kernel attests)</li>
     *   <li>MEMORY → UNVERIFIED (can be stale)</li>
     *   <li>EXECUTION → VERIFIED</li>
     *   <li>REFLECTION → VERIFIED</li>
     * </ul>
     */
    ItemStatus verifyItem(EvidenceItem item) {
        Objects.requireNonNull(item, "item must not be null");

        if (item.content().isBlank() && item.title().isBlank()) {
            return ItemStatus.FAILED;
        }

        return switch (item.sourceType()) {
            case PROJECT -> ItemStatus.VERIFIED;
            case KNOWLEDGE -> item.citations().isEmpty() ? ItemStatus.UNVERIFIED : ItemStatus.VERIFIED;
            case REASONING, INFERENCE, PLANNING, REFLECTION, EXECUTION -> ItemStatus.VERIFIED;
            case MEMORY -> ItemStatus.UNVERIFIED;
        };
    }

    /**
     * Returns an AgentDecision describing this verification run.
     */
    public AgentDecision toDecision(VerificationReport report) {
        return AgentDecision.builder()
                .agent(Agent.VERIFICATION)
                .action(Action.VERIFY)
                .rationale(String.format(
                        "Verification complete: tier=%s, confidence=%.2f, items=%d",
                        report.tier(), report.confidence(), report.perItemStatus().size()))
                .confidence(report.confidence())
                .addMetadata("reportId", report.reportId())
                .addMetadata("tier", report.tier().name())
                .addMetadata("verifiedCount", (int) report.perItemStatus().values().stream()
                        .filter(s -> s == ItemStatus.VERIFIED).count())
                .addMetadata("unverifiedCount", (int) report.perItemStatus().values().stream()
                        .filter(s -> s == ItemStatus.UNVERIFIED).count())
                .build();
    }
}
