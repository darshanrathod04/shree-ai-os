package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>DecisionWeight</b>
 *
 * <p>One immutable weight per {@link DecisionDimension} used by the
 * I3 Decision Optimization Engine. Weights are normalized internally so
 * that they always sum to {@code 1.0}. The weight represents the
 * importance of that dimension in the optimization formula.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param dimension the locked dimension being weighted (never null)
 * @param weight    the raw importance value within {@code [0.0, 1.0]}
 */
public record DecisionWeight(
        DecisionDimension dimension,
        double weight) {

    /**
     * Creates a validated immutable decision weight.
     *
     * @throws NullPointerException     if dimension is null
     * @throws IllegalArgumentException if weight is outside {@code [0.0, 1.0]}
     */
    public DecisionWeight {
        Objects.requireNonNull(dimension, "dimension must not be null");
        if (Double.isNaN(weight) || weight < 0.0 || weight > 1.0) {
            throw new IllegalArgumentException(
                    "weight must be within [0.0, 1.0]: " + weight);
        }
    }

    @Override
    public String toString() {
        return String.format("DecisionWeight{%s=%.4f}", dimension, weight);
    }
}