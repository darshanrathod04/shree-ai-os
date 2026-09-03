package com.shreeai.os.platform.runtime.confidence;

import com.shreeai.os.platform.runtime.model.VerificationReport.ConfidenceTier;

/**
 * <b>ConfidenceCalculator</b>
 *
 * <p>Static utility that computes the 4-tier confidence score for a user answer.
 * This is the single authoritative source for confidence tier assignment in
 * Sprint 18.</p>
 *
 * <p><b>Constitutional Rule:</b> Confidence reflects evidence quality, not
 * LLM persuasiveness. A confident-sounding answer with no evidence scores 0.15.</p>
 *
 * <p><b>Tier Scale (Sprint 18):</b></p>
 * <table>
 *   <tr><th>Tier              </th><th>Score</th><th>Condition</th></tr>
 *   <tr><td>VERIFIED_PROJECT  </td><td>0.95</td><td>Project kernel verified the answer from actual code</td></tr>
 *   <tr><td>VERIFIED_KB      </td><td>0.80</td><td>Knowledge kernel verified the answer from the knowledge graph</td></tr>
 *   <tr><td>INFERRED          </td><td>0.60</td><td>Reasoning/inference produced a plausible hypothesis</td></tr>
 *   <tr><td>INSUFFICIENT      </td><td>0.15</td><td>No evidence, workspace closed, or diagnosis failed</td></tr>
 * </table>
 *
 * @since Sprint 18
 */
public final class ConfidenceCalculator {

    private static final double VERIFIED_PROJECT_SCORE = 0.95;
    private static final double VERIFIED_KB_SCORE = 0.80;
    private static final double INFERRED_SCORE = 0.60;
    private static final double INSUFFICIENT_SCORE = 0.15;

    private ConfidenceCalculator() {}

    /**
     * Returns 0.95 when project intelligence kernel verified the answer.
     */
    public static double fromProjectEvidence() {
        return VERIFIED_PROJECT_SCORE;
    }

    /**
     * Returns 0.80 when knowledge kernel verified the answer.
     */
    public static double fromKnowledgeEvidence() {
        return VERIFIED_KB_SCORE;
    }

    /**
     * Returns 0.60 when reasoning or inference produced a plausible hypothesis.
     */
    public static double fromReasoningEvidence() {
        return INFERRED_SCORE;
    }

    /**
     * Returns 0.15 when no evidence is available or diagnosis failed.
     */
    public static double fromInsufficient() {
        return INSUFFICIENT_SCORE;
    }

    /**
     * Returns the appropriate confidence score for an evidence source type.
     */
    public static double fromSourceType(com.shreeai.os.platform.runtime.model.EvidenceItem.SourceType sourceType) {
        return switch (sourceType) {
            case PROJECT -> VERIFIED_PROJECT_SCORE;
            case KNOWLEDGE -> VERIFIED_KB_SCORE;
            case REASONING, INFERENCE, PLANNING, REFLECTION -> INFERRED_SCORE;
            case MEMORY, EXECUTION -> INFERRED_SCORE; // Memory recall is useful but not independently verified
        };
    }

    /**
     * Returns the confidence tier for an evidence source type.
     */
    public static ConfidenceTier tierFromSourceType(com.shreeai.os.platform.runtime.model.EvidenceItem.SourceType sourceType) {
        return switch (sourceType) {
            case PROJECT -> ConfidenceTier.VERIFIED_PROJECT;
            case KNOWLEDGE -> ConfidenceTier.VERIFIED_KB;
            case REASONING, INFERENCE, PLANNING, REFLECTION, MEMORY, EXECUTION -> ConfidenceTier.INFERRED;
        };
    }

    /**
     * Returns the highest-confidence score from a bundle of evidence.
     *
     * @param itemCount   number of evidence items in the bundle
     * @param hasProject  true if any item is of type PROJECT
     * @param hasKnowledge true if any item is of type KNOWLEDGE
     * @param hasReasoning true if any item is of type REASONING or INFERENCE
     * @return the appropriate confidence score
     */
    public static double fromBundleComposition(
            int itemCount,
            boolean hasProject,
            boolean hasKnowledge,
            boolean hasReasoning
    ) {
        if (itemCount == 0) {
            return INSUFFICIENT_SCORE;
        }
        if (hasProject) {
            return VERIFIED_PROJECT_SCORE;
        }
        if (hasKnowledge) {
            return VERIFIED_KB_SCORE;
        }
        if (hasReasoning) {
            return INFERRED_SCORE;
        }
        return INSUFFICIENT_SCORE;
    }

    /**
     * Returns the highest-confidence tier from a bundle of evidence items.
     *
     * @param items non-null (may be empty) list of evidence items
     * @return the highest applicable tier
     */
    public static ConfidenceTier highestTier(java.util.List<? extends com.shreeai.os.platform.runtime.model.EvidenceItem> items) {
        if (items == null || items.isEmpty()) {
            return ConfidenceTier.INSUFFICIENT;
        }
        boolean hasProject = false;
        boolean hasKnowledge = false;
        boolean hasReasoning = false;

        for (var item : items) {
            switch (item.sourceType()) {
                case PROJECT -> hasProject = true;
                case KNOWLEDGE -> hasKnowledge = true;
                case REASONING, INFERENCE, PLANNING, REFLECTION -> hasReasoning = true;
                default -> {}
            }
        }

        if (hasProject) return ConfidenceTier.VERIFIED_PROJECT;
        if (hasKnowledge) return ConfidenceTier.VERIFIED_KB;
        if (hasReasoning) return ConfidenceTier.INFERRED;
        return ConfidenceTier.INSUFFICIENT;
    }

    /**
     * Returns the confidence score for a given tier.
     */
    public static double scoreForTier(ConfidenceTier tier) {
        if (tier == null) return INSUFFICIENT_SCORE;
        return switch (tier) {
            case VERIFIED_PROJECT -> VERIFIED_PROJECT_SCORE;
            case VERIFIED_KB -> VERIFIED_KB_SCORE;
            case INFERRED -> INFERRED_SCORE;
            case INSUFFICIENT -> INSUFFICIENT_SCORE;
        };
    }
}
