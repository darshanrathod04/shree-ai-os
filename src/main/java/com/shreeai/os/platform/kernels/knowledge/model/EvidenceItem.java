package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>EvidenceItem</b>
 *
 * <p>One immutable, provenance-preserving retrieval hit: the original chunk
 * content together with the deterministic relevance score and the graph
 * concepts that matched. Content is never rewritten and never summarized -
 * synthesis is the responsibility of Reasoning, not of retrieval.</p>
 *
 * <p><b>Note:</b> the class name is scoped to the knowledge kernel package;
 * the inference and runtime layers keep their own unrelated
 * {@code EvidenceItem} types untouched.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K4 Retrieval and Ranking</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param chunkId         the deterministic chunk id of the original chunk
 *                        (never null)
 * @param documentId      the deterministic document id of the parent document
 *                        (never null)
 * @param content         the original chunk content, preserved verbatim (never
 *                        null)
 * @param relevanceScore  the deterministic relevance score in {@code [0.0,
 *                        1.0]}
 * @param matchedConcepts the canonically sorted concept names that matched
 *                        (never null)
 */
public record EvidenceItem(
        String chunkId,
        String documentId,
        String content,
        double relevanceScore,
        List<String> matchedConcepts) {

    /**
     * Compact constructor that validates every field, canonically sorts the
     * matched concepts and defensively copies them.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if ids are blank or the score is out of
     *                                  range
     */
    public EvidenceItem {
        Objects.requireNonNull(chunkId, "chunkId must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(matchedConcepts, "matchedConcepts must not be null");
        if (chunkId.isBlank()) {
            throw new IllegalArgumentException("chunkId must not be blank");
        }
        if (documentId.isBlank()) {
            throw new IllegalArgumentException("documentId must not be blank");
        }
        if (relevanceScore < 0.0 || relevanceScore > 1.0) {
            throw new IllegalArgumentException(
                    "relevanceScore must be within [0.0, 1.0]: " + relevanceScore);
        }
        matchedConcepts = matchedConcepts.stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Override
    public String toString() {
        return String.format("EvidenceItem{chunkId=%s, documentId=%s, score=%.4f, concepts=%s}",
                chunkId, documentId, relevanceScore, matchedConcepts);
    }
}
