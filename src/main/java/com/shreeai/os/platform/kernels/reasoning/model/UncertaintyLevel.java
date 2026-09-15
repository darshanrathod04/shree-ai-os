package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>UncertaintyLevel</b>
 *
 * <p>The deterministic certainty classification of a single reasoning node,
 * produced by R5 Uncertainty Modeling. The classification thresholds are
 * locked and never probabilistic:</p>
 * <ul>
 *   <li>{@link #CERTAIN} - certainty score at or above {@code 0.90}</li>
 *   <li>{@link #LIKELY} - certainty score in {@code [0.75, 0.90)}</li>
 *   <li>{@link #UNCERTAIN} - certainty score in {@code [0.50, 0.75)}</li>
 *   <li>{@link #UNKNOWN} - certainty score below {@code 0.50}</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum UncertaintyLevel {

    /** Certainty score at or above 0.90. */
    CERTAIN("Certainty score at or above 0.90"),

    /** Certainty score in [0.75, 0.90). */
    LIKELY("Certainty score in [0.75, 0.90)"),

    /** Certainty score in [0.50, 0.75). */
    UNCERTAIN("Certainty score in [0.50, 0.75)"),

    /** Certainty score below 0.50. */
    UNKNOWN("Certainty score below 0.50");

    private final String description;

    UncertaintyLevel(String description) {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this level.
     *
     * @return the locked description (never null)
     */
    public String description() {
        return description;
    }
}
