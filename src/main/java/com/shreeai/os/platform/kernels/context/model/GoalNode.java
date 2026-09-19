package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>GoalNode</b>
 *
 * <p>Represents one identified goal from user input.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param title the goal title (preserves original wording)
 * @param confidence the confidence score (0.0 to 1.0)
 * @param evidence the evidence for this goal identification
 */
public record GoalNode(
        String title,
        double confidence,
        GoalEvidence evidence
) {
    /**
     * Creates a new GoalNode with validation.
     *
     * @param title the goal title (must not be null)
     * @param confidence the confidence score (0.0 to 1.0)
     * @param evidence the evidence for this goal identification (must not be null)
     * @return a new GoalNode instance
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if confidence is out of range
     */
    public static GoalNode of(String title, double confidence, GoalEvidence evidence) {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(evidence, "evidence must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        return new GoalNode(title, confidence, evidence);
    }

    public String title() {
        return title;
    }

    public double confidence() {
        return confidence;
    }

    public GoalEvidence evidence() {
        return evidence;
    }

    @Override
    public String toString() {
        return String.format("GoalNode{title='%s', confidence=%.3f}", title, confidence);
    }
}