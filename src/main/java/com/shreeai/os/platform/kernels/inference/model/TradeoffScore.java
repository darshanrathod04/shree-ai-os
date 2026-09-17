package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>TradeoffScore</b>
 *
 * <p>One immutable, deterministic score on a locked trade-off dimension for a
 * single {@link AlternativeCandidate}. The score is a comparison value within
 * {@code [0.0, 1.0]} - it is never a probability, never a prediction and never
 * a winner selection. Selection belongs to I3 Decision Optimization.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I2 Trade-off Analysis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param dimension the locked dimension this score measures (never null)
 * @param score     the deterministic comparison value within {@code [0.0, 1.0]}
 */
public record TradeoffScore(
        TradeoffDimension dimension,
        double score) {

    /**
     * Creates a validated immutable trade-off score.
     *
     * @throws NullPointerException     if dimension is null
     * @throws IllegalArgumentException if score is outside {@code [0.0, 1.0]}
     */
    public TradeoffScore {
        Objects.requireNonNull(dimension, "dimension must not be null");
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(
                    "score must be within [0.0, 1.0]: " + score);
        }
    }

    @Override
    public String toString() {
        return String.format("TradeoffScore{%s=%.4f}", dimension, score);
    }
}