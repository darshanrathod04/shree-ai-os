package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ReliabilityResult</b>
 *
 * <p>The canonical trust artifact of the Knowledge Intelligence phase: the
 * stably ordered, trust-annotated evidence list plus the mean overall trust.
 * Future Reasoning consumes this artifact - never raw retrieval results.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param evidence     the ordered immutable trust-annotated evidence (never
 *                     null)
 * @param overallTrust the mean overall trust across the evidence, within
 *                     {@code [0.0, 1.0]} (0.0 when empty)
 */
public record ReliabilityResult(List<TrustedEvidence> evidence, double overallTrust) {

    /**
     * Compact constructor that validates the trust range and defensively
     * copies the evidence list.
     *
     * @throws NullPointerException     if evidence is null
     * @throws IllegalArgumentException if overallTrust is outside
     *                                  {@code [0.0, 1.0]}
     */
    public ReliabilityResult {
        Objects.requireNonNull(evidence, "evidence must not be null");
        evidence = List.copyOf(evidence);
        if (overallTrust < 0.0 || overallTrust > 1.0) {
            throw new IllegalArgumentException(
                    "overallTrust must be within [0.0, 1.0]: " + overallTrust);
        }
    }

    /**
     * Returns the number of trust-annotated evidence items.
     *
     * @return the evidence count (never negative)
     */
    public int evidenceCount() {
        return evidence.size();
    }

    @Override
    public String toString() {
        return String.format("ReliabilityResult{evidence=%d, overallTrust=%.4f}",
                evidence.size(), overallTrust);
    }
}
