package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>IntentProfile</b>
 *
 * <p>Canonical intent object representing the detected primary intent of a user request.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Holds the primary detected intent and its confidence.</li>
 *   <li>Provides ranked alternative intents for fallback scenarios.</li>
 *   <li>Includes detection metadata for auditability.</li>
 *   <li>Single source of truth for intent during pipeline execution.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable. All fields are final.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-201</p>
 *
 * @param primaryIntent the detected primary intent (must not be null)
 * @param confidence the confidence score for the primary intent (0.0 to 1.0)
 * @param alternatives ranked list of alternative intents, sorted by confidence descending
 * @param detectedAt when the intent was detected (must not be null)
 * @param detectionMethod the method used for detection (must not be null)
 */
public record IntentProfile(
        PrimaryIntent primaryIntent,
        double confidence,
        List<IntentCandidate> alternatives,
        Instant detectedAt,
        String detectionMethod
) {
    /**
     * Creates a new IntentProfile with defensive copying and validation.
     *
     * @param primaryIntent the detected primary intent (must not be null)
     * @param confidence the confidence score for the primary intent (0.0 to 1.0)
     * @param alternatives ranked list of alternative intents
     * @param detectedAt when the intent was detected (must not be null)
     * @param detectionMethod the method used for detection (must not be null)
     * @return a new IntentProfile instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if confidence is out of range
     */
    public static IntentProfile of(PrimaryIntent primaryIntent, double confidence,
                                   List<IntentCandidate> alternatives, Instant detectedAt,
                                   String detectionMethod) {
        Objects.requireNonNull(primaryIntent, "primaryIntent must not be null");
        Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        Objects.requireNonNull(detectionMethod, "detectionMethod must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        List<IntentCandidate> safeAlternatives = alternatives != null
                ? List.copyOf(alternatives)
                : List.of();
        return new IntentProfile(primaryIntent, confidence, safeAlternatives, detectedAt, detectionMethod);
    }

    /**
     * Returns the primary intent.
     *
     * @return the primary intent
     */
    public PrimaryIntent primaryIntent() {
        return primaryIntent;
    }

    /**
     * Returns the confidence score.
     *
     * @return the confidence score
     */
    public double confidence() {
        return confidence;
    }

    /**
     * Returns the alternatives list.
     *
     * @return the alternatives list
     */
    public List<IntentCandidate> alternatives() {
        return alternatives;
    }

    /**
     * Returns the detected-at timestamp.
     *
     * @return the detected-at timestamp
     */
    public Instant detectedAt() {
        return detectedAt;
    }

    /**
     * Returns the detection method.
     *
     * @return the detection method
     */
    public String detectionMethod() {
        return detectionMethod;
    }

    /**
     * Creates an IntentProfile with UNKNOWN intent and zero confidence.
     *
     * @param detectionMethod the method used for detection
     * @return an IntentProfile representing unknown intent
     */
    public static IntentProfile unknown(String detectionMethod) {
        return new IntentProfile(
                PrimaryIntent.UNKNOWN,
                0.0,
                List.of(),
                Instant.now(),
                detectionMethod
        );
    }

    /**
     * Returns a string representation of this profile.
     *
     * @return formatted string with primary intent and confidence
     */
    @Override
    public String toString() {
        return String.format("IntentProfile{primaryIntent=%s, confidence=%.3f, alternatives=%d}",
                primaryIntent, confidence, alternatives.size());
    }
}