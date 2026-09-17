package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>OptimizedDecision</b>
 *
 * <p>The canonical, immutable output artifact of the I3 Decision
 * Optimization Engine: one optimized decision with complete
 * explainability for a verified problem.</p>
 *
 * <p>The decision <em>selects</em> — it picks the single best candidate
 * from the {@link TradeoffAnalysisSet} using deterministic weighted
 * optimization. The justifications explain exactly why that candidate
 * was chosen, with one justification per contributing dimension.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the
 * justification list is defensively copied and unmodifiable, and every
 * justification is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param candidateId     the deterministic id of the selected candidate
 * @param strategy        the strategy category of the selected candidate
 * @param optimizationScore the weighted optimization score within
 *                          {@code [0.0, 1.0]}, rounded to 4 decimals
 * @param justifications  the deterministic explainability justifications
 *                          (never null, at least one)
 */
public record OptimizedDecision(
        String candidateId,
        AlternativeType strategy,
        double optimizationScore,
        List<DecisionJustification> justifications) {

    /**
     * Creates a validated, deeply-immutable optimized decision.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if candidateId is blank, the
     *                                  justification list is empty, or
     *                                  optimizationScore is outside
     *                                  {@code [0.0, 1.0]}
     */
    public OptimizedDecision {
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");
        Objects.requireNonNull(justifications, "justifications must not be null");
        if (candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (justifications.isEmpty()) {
            throw new IllegalArgumentException("justifications must not be empty");
        }
        if (optimizationScore < 0.0 || optimizationScore > 1.0) {
            throw new IllegalArgumentException(
                    "optimizationScore must be within [0.0, 1.0]: " + optimizationScore);
        }
        justifications = List.copyOf(justifications);
    }

    @Override
    public String toString() {
        return String.format("OptimizedDecision{candidateId='%s', strategy=%s, score=%.4f, justifications=%d}",
                candidateId, strategy, optimizationScore, justifications.size());
    }
}