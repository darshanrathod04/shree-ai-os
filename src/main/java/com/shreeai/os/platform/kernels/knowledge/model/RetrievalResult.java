package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>RetrievalResult</b>
 *
 * <p>The canonical retrieval artifact of the K4 pipeline: the ranked,
 * immutable evidence list plus observability counters. Reasoning consumes
 * this artifact - never raw documents.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K4 Retrieval and Ranking</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param evidence          the ranked, immutable evidence list (never null;
 *                          ordered by the locked stable-ordering strategy)
 * @param searchedDocuments the number of documents scanned (never negative)
 * @param matchedChunks     the number of chunks that became evidence (never
 *                          negative; always equals {@code evidence.size()})
 */
public record RetrievalResult(
        List<EvidenceItem> evidence,
        int searchedDocuments,
        int matchedChunks) {

    /**
     * Compact constructor that validates every field and defensively copies
     * the evidence list.
     *
     * @throws NullPointerException     if evidence is null
     * @throws IllegalArgumentException if counters are negative, inconsistent
     *                                  with the evidence list, or out of range
     */
    public RetrievalResult {
        Objects.requireNonNull(evidence, "evidence must not be null");
        evidence = List.copyOf(evidence);
        if (searchedDocuments < 0) {
            throw new IllegalArgumentException(
                    "searchedDocuments must be >= 0: " + searchedDocuments);
        }
        if (matchedChunks < 0) {
            throw new IllegalArgumentException(
                    "matchedChunks must be >= 0: " + matchedChunks);
        }
        if (matchedChunks != evidence.size()) {
            throw new IllegalArgumentException(String.format(
                    "matchedChunks must equal the evidence size: %d != %d",
                    matchedChunks, evidence.size()));
        }
    }

    /**
     * Returns the number of evidence items.
     *
     * @return the evidence count (never negative)
     */
    public int evidenceCount() {
        return evidence.size();
    }

    /**
     * Returns the highest-scoring evidence item, or empty when nothing
     * matched.
     *
     * @return the best evidence item, or empty (never null)
     */
    public java.util.Optional<EvidenceItem> best() {
        return evidence.stream()
                .max(Comparator.comparingDouble(EvidenceItem::relevanceScore));
    }

    @Override
    public String toString() {
        return String.format("RetrievalResult{evidence=%d, searchedDocuments=%d}",
                evidence.size(), searchedDocuments);
    }
}
