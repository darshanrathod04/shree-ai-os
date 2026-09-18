package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>CalibratedDecision</b>
 *
 * <p>The canonical, immutable output artifact of the I4 Confidence
 * Calibration Engine: the calibrated confidence of an {@link OptimizedDecision},
 * expressed as a deterministic score, level and ordered list of explainable
 * factors.</p>
 *
 * <p>The calibration <em>never changes</em> the decision — it only measures
 * how trustworthy the optimized decision is, using only verified evidence
 * and uncertainty. The candidate id is echoed from the source decision so
 * the calibrated result is always attributable.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the factor list
 * is defensively copied and unmodifiable, and every factor is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I4 Confidence Calibration</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param candidateId the deterministic id of the calibrated candidate (never
 *                    null or blank)
 * @param confidence  the calibrated confidence within {@code [0.0, 1.0]},
 *                    rounded to 4 decimals
 * @param level       the locked confidence level (never null)
 * @param factors     the ordered deterministic contribution factors (never
 *                    null, at least one)
 */
public record CalibratedDecision(
        String candidateId,
        double confidence,
        ConfidenceLevel level,
        List<ConfidenceFactor> factors
) {

    /**
     * Creates a validated, deeply-immutable calibrated decision.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if candidateId is blank, confidence is
     *                                  outside {@code [0.0, 1.0]}, or the factor
     *                                  list is empty
     */
    public CalibratedDecision {
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(factors, "factors must not be null");
        if (candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (Double.isNaN(confidence) || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
        if (factors.isEmpty()) {
            throw new IllegalArgumentException("factors must not be empty");
        }
        factors = List.copyOf(factors);
    }

    /**
     * Returns the confidence rounded to four decimals.
     *
     * @return the rounded confidence
     */
    public double confidence() {
        return Math.round(confidence * 10000.0) / 10000.0;
    }

    @Override
    public String toString() {
        return String.format(
                "CalibratedDecision{candidateId='%s', confidence=%.4f, level=%s, factors=%d}",
                candidateId, confidence(), level, factors.size());
    }
}