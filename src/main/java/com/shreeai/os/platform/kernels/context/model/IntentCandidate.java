package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>IntentCandidate</b>
 *
 * <p>Represents a ranked alternative intent detected from user input.</p>
 *
 * <p><b>Immutability:</b> This record is immutable. All fields are final.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param intent the primary intent candidate (must not be null)
 * @param confidence the confidence score (0.0 to 1.0)
 */
public record IntentCandidate(
        PrimaryIntent intent,
        double confidence
) {
    /**
     * Creates a new IntentCandidate with validation.
     *
     * @param intent the primary intent candidate (must not be null)
     * @param confidence the confidence score (0.0 to 1.0)
     * @return a new IntentCandidate instance
     * @throws NullPointerException if intent is null
     * @throws IllegalArgumentException if confidence is out of range
     */
    public static IntentCandidate of(PrimaryIntent intent, double confidence) {
        Objects.requireNonNull(intent, "intent must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        return new IntentCandidate(intent, confidence);
    }

    /**
     * Returns the intent for this candidate.
     *
     * @return the intent
     */
    public PrimaryIntent intent() {
        return intent;
    }

    /**
     * Returns the confidence score for this candidate.
     *
     * @return the confidence score
     */
    public double confidence() {
        return confidence;
    }

    /**
     * Returns a string representation of this candidate.
     *
     * @return formatted string with intent and confidence
     */
    @Override
    public String toString() {
        return String.format("IntentCandidate{intent=%s, confidence=%.3f}", intent, confidence);
    }
}