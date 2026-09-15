package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>UncertaintyType</b>
 *
 * <p>Enum identifying the kind of certainty, uncertainty or knowledge-gap
 * issue discovered by R5 Uncertainty Modeling. Each type carries a locked,
 * deterministic impact value from {@code {0.25, 0.50, 0.75, 1.00}}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum UncertaintyType {

    /** The node is not backed by sufficient trusted evidence. */
    LOW_EVIDENCE("The node is not backed by sufficient trusted evidence.", 0.50),

    /** Supporting evidence disagrees or contradicts the node. */
    CONFLICTING_EVIDENCE("Supporting evidence disagrees or contradicts the node.", 0.75),

    /** A required supporting artifact or relationship is missing. */
    KNOWLEDGE_GAP("A required supporting artifact or relationship is missing.", 0.50),

    /** Verification is high but the supporting evidence trust is low. */
    LOW_RELIABILITY("Verification is high but the supporting evidence trust is low.", 0.50),

    /** The reasoning artifacts are incomplete for a full evaluation. */
    INCOMPLETE_REASONING("The reasoning artifacts are incomplete for a full evaluation.", 1.00);

    private final String description;
    private final double impact;

    UncertaintyType(String description, double impact) {
        this.description = description;
        this.impact = impact;
    }

    /**
     * Returns the locked default impact for this issue type.
     *
     * @return one of {@code {0.25, 0.50, 0.75, 1.00}}
     */
    public double impact() {
        return impact;
    }

    /**
     * Returns the human-readable description of this type.
     *
     * @return the locked description (never null)
     */
    public String description() {
        return description;
    }
}
