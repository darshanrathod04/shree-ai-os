package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>TradeoffAnalysis</b>
 *
 * <p>One immutable, deterministic comparison matrix for a single
 * {@link AlternativeCandidate}. It carries the candidate id, one score per
 * locked {@link TradeoffDimension} and a deterministic template summary.</p>
 *
 * <p>The analysis <em>compares</em> - it never selects a winner. Winner
 * selection belongs to I3 Decision Optimization.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the score list is
 * defensively copied and unmodifiable, and every score is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I2 Trade-off Analysis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param candidateId the deterministic id of the analyzed candidate
 * @param scores      one immutable score per locked dimension in locked order
 * @param summary     the deterministic template summary text
 */
public record TradeoffAnalysis(
        String candidateId,
        List<TradeoffScore> scores,
        String summary) {

    /**
     * Creates a validated, deeply-immutable trade-off analysis.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if candidateId is blank or the score
     *                                  list is empty
     */
    public TradeoffAnalysis {
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(scores, "scores must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        if (candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (scores.isEmpty()) {
            throw new IllegalArgumentException("scores must not be empty");
        }
        scores = List.copyOf(scores);
    }

    @Override
    public String toString() {
        return String.format("TradeoffAnalysis{candidateId='%s', scores=%d, summary='%s'}",
                candidateId, scores.size(), summary);
    }
}