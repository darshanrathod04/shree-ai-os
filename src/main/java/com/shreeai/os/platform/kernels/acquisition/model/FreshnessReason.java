package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>FreshnessReason</b>
 *
 * <p>Enterprise-grade provenance for one {@link AcquisitionDecision}: the
 * selected source it applies to, the decision itself and the human-readable
 * explanation of exactly why that decision was reached.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Records the deterministic justification of a cache decision so it can
 *       be audited, explained and replayed.</li>
 *   <li>Names the exact registry source the decision applies to.</li>
 * </ul>
 *
 * <p><b>Examples of explanations:</b> {@code "Cache valid: age=10 days"},
 * {@code "Cache expired: age=290 days"}, {@code "Source missing"},
 * {@code "User requested latest"}.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param sourceId    the deterministic registry id of the source the decision
 *                    applies to (must not be null or blank)
 * @param decision    the locked cache decision (must not be null)
 * @param explanation the deterministic, human-readable justification (must not
 *                    be null or blank)
 */
public record FreshnessReason(
        String sourceId,
        AcquisitionDecision decision,
        String explanation) {

    /**
     * Creates a new FreshnessReason with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if sourceId or explanation is blank
     */
    public FreshnessReason {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
    }

    @Override
    public String toString() {
        return String.format("FreshnessReason{source='%s', decision=%s, explanation='%s'}",
                sourceId, decision, explanation);
    }
}