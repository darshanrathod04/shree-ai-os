package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.model.ConflictRecord;
import com.shreeai.os.platform.kernels.inference.model.CredibilityScore;
import com.shreeai.os.platform.kernels.inference.model.EvidencePackage;
import com.shreeai.os.platform.kernels.inference.model.ResolvedFact;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultEvidenceConflictResolver</b>
 *
 * <p>Deterministic, stateless conflict resolution engine that
 * converts raw, potentially conflicting evidence into a single
 * immutable {@link EvidencePackage}.</p>
 *
 * <p><b>Determinism Rules (LOCKED):</b></p>
 * <ul>
 *     <li>Same input → identical output</li>
 *     <li>Stable ordering - no randomness</li>
 *     <li>No UUID influence</li>
 *     <li>No timestamps affecting ranking</li>
 *     <li>No LLM, embeddings, or Bayesian inference</li>
 *     <li>100% reproducible</li>
 * </ul>
 *
 * <p><b>Credibility Formula (LOCKED):</b></p>
 * <pre>Final = (Source × 0.40) + (Evidence × 0.30) + (Recency × 0.20) + (Consensus × 0.10)</pre>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 */
public final class DefaultEvidenceConflictResolver {

    /** Source type priority scores (deterministic, no timestamps). */
    private static final double SOURCE_REASONING = 0.90;
    private static final double SOURCE_KNOWLEDGE = 0.80;
    private static final double SOURCE_MEMORY = 0.70;
    private static final double SOURCE_INFERENCE = 0.60;
    private static final double SOURCE_PLANNING = 0.50;
    private static final double SOURCE_REFLECTION = 0.40;
    private static final double SOURCE_DEFAULT = 0.50;

    private DefaultEvidenceConflictResolver() {
        // Prevent instantiation - stateless utility
    }

    /**
     * Resolves conflicting evidence from a reasoning result into
     * a single canonical {@link EvidencePackage}.
     *
     * @param reasoningResult the upstream reasoning result (must not be null)
     * @return an immutable evidence package (never null)
     * @throws NullPointerException if reasoningResult is null
     */
    public static EvidencePackage resolve(ReasoningResult reasoningResult) {
        return resolve(reasoningResult, List.of(), List.of());
    }

    /**
     * Resolves conflicting evidence from multiple sources - reasoning
     * result, memories, and knowledge nodes - into a single canonical
     * {@link EvidencePackage}.
     *
     * @param reasoningResult the upstream reasoning result (must not be null)
     * @param memories        recalled memory evidence (must not be null, may be empty)
     * @param knowledgeNodes  retrieved knowledge evidence (must not be null, may be empty)
     * @return an immutable evidence package (never null)
     * @throws NullPointerException if reasoningResult, memories, or knowledgeNodes is null
     */
    public static EvidencePackage resolve(
            ReasoningResult reasoningResult,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {
        Objects.requireNonNull(reasoningResult, "reasoningResult must not be null");
        Objects.requireNonNull(memories, "memories must not be null");
        Objects.requireNonNull(knowledgeNodes, "knowledgeNodes must not be null");

        List<EvidenceItem> allItems = collectEvidence(reasoningResult, memories, knowledgeNodes);

        if (allItems.isEmpty()) {
            return emptyPackage(reasoningResult);
        }

        Map<String, List<EvidenceItem>> topicGroups = groupByTopic(allItems);

        List<ResolvedFact> resolvedFacts = new ArrayList<>();
        List<String> supportingEvidence = new ArrayList<>();
        List<String> discardedEvidence = new ArrayList<>();
        List<ConflictRecord> conflicts = new ArrayList<>();
        double totalConfidence = 0.0;
        int resolvedCount = 0;

        for (Map.Entry<String, List<EvidenceItem>> entry : topicGroups.entrySet()) {
            List<EvidenceItem> items = entry.getValue();
            List<ScoredEvidence> scored = scoreItems(items);
            scored.sort((a, b) -> {
                int cmp = Double.compare(b.score(), a.score());
                if (cmp != 0) return cmp;
                return a.item().id().compareTo(b.item().id());
            });

            EvidenceItem winner = scored.get(0).item();
            double winnerScore = scored.get(0).score();

            ResolvedFact fact = new ResolvedFact(
                    entry.getKey(),
                    winner.content(),
                    winnerScore,
                    winner.sourceType(),
                    extractSupportingIds(scored)
            );
            resolvedFacts.add(fact);
            supportingEvidence.add(winner.id());
            totalConfidence += winnerScore;
            resolvedCount++;

            for (int i = 1; i < scored.size(); i++) {
                discardedEvidence.add(scored.get(i).item().id());
            }

            if (items.size() > 1) {
                List<String> rejected = new ArrayList<>();
                for (int i = 1; i < scored.size(); i++) {
                    rejected.add(scored.get(i).item().content());
                }
                CredibilityScore credibility = computeCredibilityScore(winner, items, winnerScore);
                ConflictRecord record = new ConflictRecord(
                        entry.getKey(),
                        winner.content(),
                        rejected,
                        buildResolutionReason(winner, credibility),
                        credibility,
                        Instant.now()
                );
                conflicts.add(record);
            }
        }

        double overallConfidence = resolvedCount > 0 ? totalConfidence / resolvedCount : 0.0;
        List<String> provenance = buildProvenance(reasoningResult, memories, knowledgeNodes);

        return new EvidencePackage(
                resolvedFacts,
                supportingEvidence,
                discardedEvidence,
                conflicts,
                clamp(overallConfidence),
                provenance
        );
    }

    /**
     * Collects evidence items from all sources in deterministic order.
     */
    private static List<EvidenceItem> collectEvidence(
            ReasoningResult reasoningResult,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {
        List<EvidenceItem> allItems = new ArrayList<>();

        List<String> reasoningEvidence = reasoningResult.evidence();
        if (reasoningEvidence != null) {
            for (int i = 0; i < reasoningEvidence.size(); i++) {
                String content = reasoningEvidence.get(i);
                allItems.add(new EvidenceItem(
                        "reasoning-" + i,
                        extractTopic(content),
                        content,
                        "reasoning",
                        clamp(reasoningResult.confidence())
                ));
            }
        }

        for (int i = 0; i < memories.size(); i++) {
            Memory mem = memories.get(i);
            if (mem == null || mem.content() == null) continue;
            String text = mem.content().text();
            double confidence = mem.metadata() != null ? mem.metadata().importance() : 0.5;
            allItems.add(new EvidenceItem(
                    "memory-" + i,
                    extractTopic(text),
                    text,
                    "memory",
                    clamp(confidence)
            ));
        }

        for (int i = 0; i < knowledgeNodes.size(); i++) {
            KnowledgeNode kn = knowledgeNodes.get(i);
            if (kn == null) continue;
            String text = kn.getLabel() != null ? kn.getLabel() : kn.getDescription();
            double confidence = 0.5;
            if (kn.getMetadata() != null) {
                Object confObj = kn.getMetadata().get("confidence");
                if (confObj instanceof Number number) {
                    confidence = number.doubleValue();
                }
            }
            allItems.add(new EvidenceItem(
                    "knowledge-" + i,
                    extractTopic(text),
                    text,
                    "knowledge",
                    clamp(confidence)
            ));
        }

        return allItems;
    }

    /**
     * Groups evidence items by topic (deterministic).
     */
    private static Map<String, List<EvidenceItem>> groupByTopic(List<EvidenceItem> items) {
        Map<String, List<EvidenceItem>> groups = new LinkedHashMap<>();
        for (EvidenceItem item : items) {
            groups.computeIfAbsent(item.topic(), k -> new ArrayList<>()).add(item);
        }
        return groups;
    }

    /**
     * Extracts a deterministic topic from evidence content.
     */
    private static String extractTopic(String content) {
        if (content == null || content.isBlank()) return "unknown";
        String trimmed = content.trim();
        int spaceIndex = trimmed.indexOf(' ');
        return spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
    }

    /**
     * Scores each evidence item using the locked credibility formula.
     */
    private static List<ScoredEvidence> scoreItems(List<EvidenceItem> items) {
        List<ScoredEvidence> scored = new ArrayList<>();
        int totalItems = items.size();
        int maxContentLength = items.stream()
                .mapToInt(item -> item.content() != null ? item.content().length() : 0)
                .max().orElse(1);

        for (int i = 0; i < items.size(); i++) {
            EvidenceItem item = items.get(i);
            double sourceScore = sourceScore(item.sourceType());
            double evidenceScore = computeEvidenceScore(item, maxContentLength);
            double recencyScore = recencyScore(i, totalItems);
            double consensusScore = consensusScore(totalItems);

            double finalScore = (sourceScore * 0.40)
                    + (evidenceScore * 0.30)
                    + (recencyScore * 0.20)
                    + (consensusScore * 0.10);

            scored.add(new ScoredEvidence(item, clamp(finalScore)));
        }
        return scored;
    }

    /**
     * Computes the evidence score from source confidence and content length.
     */
    private static double computeEvidenceScore(EvidenceItem item, int maxLength) {
        double confidence = item.sourceConfidence();
        int length = item.content() != null ? item.content().length() : 0;
        double lengthScore = maxLength > 0 ? (double) length / maxLength : 0.0;
        return (confidence * 0.50) + (lengthScore * 0.50);
    }

    /**
     * Returns the source priority score for a given source type.
     */
    private static double sourceScore(String sourceType) {
        if (sourceType == null) return SOURCE_DEFAULT;
        return switch (sourceType.toLowerCase()) {
            case "reasoning" -> SOURCE_REASONING;
            case "knowledge" -> SOURCE_KNOWLEDGE;
            case "memory" -> SOURCE_MEMORY;
            case "inference" -> SOURCE_INFERENCE;
            case "planning" -> SOURCE_PLANNING;
            case "reflection" -> SOURCE_REFLECTION;
            default -> SOURCE_DEFAULT;
        };
    }

    /**
     * Computes the recency score based on position (deterministic, no timestamps).
     */
    private static double recencyScore(int index, int total) {
        if (total <= 1) return 1.0;
        return 1.0 - ((double) index / (total - 1));
    }

    /**
     * Computes the consensus score based on group size.
     */
    private static double consensusScore(int groupSize) {
        return Math.min(1.0, (double) groupSize / 5.0);
    }

    /**
     * Computes a detailed credibility score for a conflict record.
     */
    private static CredibilityScore computeCredibilityScore(
            EvidenceItem winner,
            List<EvidenceItem> allItems,
            double finalScore) {
        double sourceScore = sourceScore(winner.sourceType());
        int maxLength = allItems.stream()
                .mapToInt(item -> item.content() != null ? item.content().length() : 0)
                .max().orElse(1);
        double evidenceScore = computeEvidenceScore(winner, maxLength);
        int winnerIndex = allItems.indexOf(winner);
        double recencyScore = recencyScore(winnerIndex, allItems.size());
        double consensusScore = consensusScore(allItems.size());

        String explanation = String.format(
                "Source=%.2f (type=%s), Evidence=%.2f (conf=%.2f, len=%d), " +
                "Recency=%.2f (pos=%d), Consensus=%.2f (group=%d)",
                sourceScore, winner.sourceType(),
                evidenceScore, winner.sourceConfidence(),
                winner.content() != null ? winner.content().length() : 0,
                recencyScore, winnerIndex,
                consensusScore, allItems.size()
        );

        return new CredibilityScore(
                finalScore, sourceScore, evidenceScore,
                recencyScore, consensusScore, explanation
        );
    }

    /**
     * Extracts IDs of all supporting evidence items.
     */
    private static List<String> extractSupportingIds(List<ScoredEvidence> scored) {
        List<String> ids = new ArrayList<>();
        for (ScoredEvidence s : scored) {
            ids.add(s.item().id());
        }
        return ids;
    }

    /**
     * Builds a deterministic resolution reason string.
     */
    private static String buildResolutionReason(EvidenceItem winner, CredibilityScore score) {
        return String.format(
                "Winner selected by credibility formula: final=%.4f " +
                "(source=%.2f×0.40 + evidence=%.2f×0.30 + recency=%.2f×0.20 + consensus=%.2f×0.10)",
                score.finalScore(), score.sourceScore(), score.evidenceScore(),
                score.recencyScore(), score.consensusScore()
        );
    }

    /**
     * Builds provenance list from all evidence sources.
     */
    private static List<String> buildProvenance(
            ReasoningResult reasoningResult,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {
        List<String> provenance = new ArrayList<>();
        provenance.add("reasoning:" + reasoningResult.reasoningId());
        provenance.add("type:" + reasoningResult.reasoningType());
        provenance.add("steps:" + reasoningResult.reasoningSteps());
        provenance.add("memories:" + memories.size());
        provenance.add("knowledge:" + knowledgeNodes.size());
        return Collections.unmodifiableList(provenance);
    }

    /**
     * Returns an empty EvidencePackage for reasoning results with no evidence.
     */
    private static EvidencePackage emptyPackage(ReasoningResult reasoningResult) {
        return new EvidencePackage(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0.0,
                List.of("reasoning:" + reasoningResult.reasoningId())
        );
    }

    /**
     * Clamps a value to the [0.0, 1.0] range.
     */
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /**
     * Internal immutable representation of an evidence item.
     */
    private record EvidenceItem(
            String id,
            String topic,
            String content,
            String sourceType,
            double sourceConfidence
    ) {}

    /**
     * Internal wrapper for scored evidence items.
     */
    private record ScoredEvidence(
            EvidenceItem item,
            double score
    ) {}
}
