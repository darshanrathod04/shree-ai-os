package com.shreeai.os.platform.kernels.inference.model;

/**
 * <b>ConfidenceLevel</b>
 *
 * <p>The locked, closed set of confidence band thresholds used by the I4
 * Confidence Calibration Engine to classify the calibrated confidence
 * score.</p>
 *
 * <p><b>Locked levels:</b></p>
 * <ul>
 *   <li>{@link #HIGH}   - score {@code >= 0.80}</li>
 *   <li>{@link #MEDIUM} - score {@code [0.60, 0.79]}</li>
 *   <li>{@link #LOW}    - score {@code < 0.60}</li>
 * </ul>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I4 Confidence Calibration</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ConfidenceLevel {

    /** Calibrated confidence is high (score >= 0.80). */
    HIGH(">=0.80"),

    /** Calibrated confidence is medium (score 0.60-0.79). */
    MEDIUM("0.60-0.79"),

    /** Calibrated confidence is low (score < 0.60). */
    LOW("<0.60");

    private final String range;

    ConfidenceLevel(String range) {
        this.range = range;
    }

    /**
     * Returns the locked confidence band range description for this level.
     *
     * @return the range description
     */
    public String range() {
        return range;
    }

    /**
     * Maps a locked confidence score to its deterministic level.
     *
     * @param confidence the calibrated score within {@code [0.0, 1.0]}
     * @return the matching confidence level (never null)
     */
    public static ConfidenceLevel of(double confidence) {
        if (confidence >= 0.80) {
            return HIGH;
        } else if (confidence >= 0.60) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}