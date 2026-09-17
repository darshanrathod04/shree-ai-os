package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>DecisionJustification</b>
 *
 * <p>One immutable, deterministic explanation for why the I3 engine
 * selected a particular candidate on a specific {@link DecisionDimension}.
 * Every {@link OptimizedDecision} carries one or more justifications
 * — the engine never selects without explaining why.</p>
 *
 * <p>The summary is a deterministic template based on the score
 * threshold for that dimension — never LLM-generated.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param reason     the deterministic template reason text (never null or blank)
 * @param dimension  the dimension that contributed to the decision (never null)
 * @param contribution the normalized contribution of this dimension
 *                     to the final optimization score (clamped to [0.0, 1.0])
 */
public record DecisionJustification(
        String reason,
        DecisionDimension dimension,
        double contribution) {

    /**
     * Creates a validated, immutable decision justification.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if reason is blank or contribution
     *                                  is outside {@code [0.0, 1.0]}
     */
    public DecisionJustification {
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(dimension, "dimension must not be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (Double.isNaN(contribution) || contribution < 0.0 || contribution > 1.0) {
            throw new IllegalArgumentException(
                    "contribution must be within [0.0, 1.0]: " + contribution);
        }
    }

    @Override
    public String toString() {
        return String.format("DecisionJustification{reason='%s', dimension=%s, contribution=%.4f}",
                reason, dimension, contribution);
    }
}