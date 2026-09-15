package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * Verification status classification for a reasoning node.
 */
public enum VerificationStatus {
    VERIFIED("The node is fully supported by trusted evidence.", 1.0),
    PARTIALLY_VERIFIED("The node has some supporting evidence but with caveats.", 0.5),
    UNSUPPORTED("No trusted evidence supports this node.", 0.0),
    CONTRADICTED("This node conflicts with trusted evidence.", -0.5);

    private final String description;
    private final double score;

    VerificationStatus(String description, double score) {
        this.description = description;
        this.score = score;
    }

    /**
     * Returns the locked severity score for this status.
     */
    public double score() { return score; }
}
