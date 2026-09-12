package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>CredibilityScore</b>
 *
 * <p>Deterministic credibility evaluation for evidence resolution.
 * Computed exclusively from the locked formula:</p>
 *
 * <pre>Final = (Source × 0.40) + (Evidence × 0.30) + (Recency × 0.20) + (Consensus × 0.10)</pre>
 *
 * <p><b>Determinism Guarantees:</b></p>
 * <ul>
 *     <li>Same input → identical output</li>
 *     <li>Stable ordering - no randomness</li>
 *     <li>No UUID influence</li>
 *     <li>No timestamps affecting ranking</li>
 *     <li>No LLM, embeddings, or Bayesian inference</li>
 * </ul>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 *
 * @param finalScore    the computed final credibility score
 * @param sourceScore   the source reliability component (0.0–1.0)
 * @param evidenceScore the evidence quantity/quality component (0.0–1.0)
 * @param recencyScore  the source priority component (0.0–1.0)
 * @param consensusScore the cross-source agreement component (0.0–1.0)
 * @param explanation   deterministic explanation of why the score was produced
 */
public record CredibilityScore(
        double finalScore,
        double sourceScore,
        double evidenceScore,
        double recencyScore,
        double consensusScore,
        String explanation
) {
    /**
     * Creates a new CredibilityScore with validation.
     *
     * @param finalScore    the final score (must be 0.0–1.0)
     * @param sourceScore   the source component (must be 0.0–1.0)
     * @param evidenceScore the evidence component (must be 0.0–1.0)
     * @param recencyScore  the recency component (must be 0.0–1.0)
     * @param consensusScore the consensus component (must be 0.0–1.0)
     * @param explanation   deterministic explanation (must not be null)
     * @throws IllegalArgumentException if any score is outside [0.0, 1.0]
     * @throws NullPointerException     if explanation is null
     */
    public CredibilityScore {
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (finalScore < 0.0 || finalScore > 1.0) {
            throw new IllegalArgumentException("finalScore must be between 0.0 and 1.0");
        }
        if (sourceScore < 0.0 || sourceScore > 1.0) {
            throw new IllegalArgumentException("sourceScore must be between 0.0 and 1.0");
        }
        if (evidenceScore < 0.0 || evidenceScore > 1.0) {
            throw new IllegalArgumentException("evidenceScore must be between 0.0 and 1.0");
        }
        if (recencyScore < 0.0 || recencyScore > 1.0) {
            throw new IllegalArgumentException("recencyScore must be between 0.0 and 1.0");
        }
        if (consensusScore < 0.0 || consensusScore > 1.0) {
            throw new IllegalArgumentException("consensusScore must be between 0.0 and 1.0");
        }
    }
}
