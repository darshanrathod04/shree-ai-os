package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>TrustScore</b>
 *
 * <p>The immutable, deterministic trust evaluation of one piece of evidence:
 * the three component scores plus the locked weighted overall score and a
 * deterministic, human-readable explanation.</p>
 *
 * <p><b>Locked formula:</b> {@code overall = clamp(authority * 0.45 +
 * freshness * 0.35 + provenance * 0.20)}.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param overall     the locked weighted overall trust within {@code [0.0,
 *                    1.0]}
 * @param authority   the deterministic authority component within
 *                    {@code [0.0, 1.0]}
 * @param freshness   the deterministic freshness component within
 *                    {@code [0.0, 1.0]}
 * @param provenance  the deterministic provenance component within
 *                    {@code [0.0, 1.0]}
 * @param explanation the deterministic explanation of the evaluation (never
 *                    null)
 */
public record TrustScore(
        double overall,
        double authority,
        double freshness,
        double provenance,
        String explanation) {

    /**
     * Compact constructor that validates every score range defensively.
     *
     * @throws NullPointerException     if explanation is null
     * @throws IllegalArgumentException if any score is outside {@code [0.0,
     *                                  1.0]}
     */
    public TrustScore {
        Objects.requireNonNull(explanation, "explanation must not be null");
        requireRange(overall, "overall");
        requireRange(authority, "authority");
        requireRange(freshness, "freshness");
        requireRange(provenance, "provenance");
    }

    private static void requireRange(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be within [0.0, 1.0]: " + value);
        }
    }

    @Override
    public String toString() {
        return String.format("TrustScore{overall=%.4f, authority=%.4f, freshness=%.4f, provenance=%.4f}",
                overall, authority, freshness, provenance);
    }
}
