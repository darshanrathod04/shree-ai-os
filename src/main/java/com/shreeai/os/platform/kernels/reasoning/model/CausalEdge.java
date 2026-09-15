package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * An immutable directed causal edge linking a cause to an effect.
 *
 * <p>The {@code strength} value is deterministic and locked to a small
 * set of constants. It is never the result of probability or ML.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record CausalEdge(
        String fromNode,
        String toNode,
        double strength) {

    public CausalEdge {
        Objects.requireNonNull(fromNode, "fromNode must not be null");
        Objects.requireNonNull(toNode, "toNode must not be null");
        if (fromNode.isBlank()) {
            throw new IllegalArgumentException("fromNode must not be blank");
        }
        if (toNode.isBlank()) {
            throw new IllegalArgumentException("toNode must not be blank");
        }
        if (strength < 0.0 || strength > 1.0) {
            throw new IllegalArgumentException(
                    "strength must be in [0,1], was: " + strength);
        }
    }
}
