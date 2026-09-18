package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>ConfidenceFactor</b>
 *
 * <p>One immutable, deterministic contribution factor used by the I4
 * Confidence Calibration Engine. Each factor is a named component of the
 * final calibrated confidence, carrying its locked weight contribution
 * within {@code [0.0, 1.0]}.</p>
 *
 * <p><b>Immutability:</b> This record is immutable. Defensive copying is
 * unnecessary because both fields are immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I4 Confidence Calibration</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param name        the deterministic factor name (never null or blank)
 * @param contribution the factor's contribution within {@code [0.0, 1.0]},
 *                     rounded to 4 decimals
 */
public record ConfidenceFactor(
        String name,
        double contribution
) {

    /**
     * Compact constructor validating all fields.
     *
     * @throws NullPointerException     if name is null
     * @throws IllegalArgumentException if name is blank or contribution is
     *                                  outside {@code [0.0, 1.0]}
     */
    public ConfidenceFactor {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (Double.isNaN(contribution) || contribution < 0.0 || contribution > 1.0) {
            throw new IllegalArgumentException(
                    "contribution must be within [0.0, 1.0]: " + contribution);
        }
    }

    /**
     * Returns the contribution rounded to four decimals.
     *
     * @return the rounded contribution
     */
    public double contribution() {
        return Math.round(contribution * 10000.0) / 10000.0;
    }
}