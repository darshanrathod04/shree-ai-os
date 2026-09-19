package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * Classification of a node within a {@link CausalGraph}.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum CausalNodeType {

    /** The origin concept in a cause-effect relationship. */
    CAUSE,

    /** The dependent concept in a cause-effect relationship. */
    EFFECT,

    /** A concept that is both cause and effect within a causal chain. */
    INTERMEDIATE
}
