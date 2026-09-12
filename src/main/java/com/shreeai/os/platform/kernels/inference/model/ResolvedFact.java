package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ResolvedFact</b>
 *
 * <p>Represents a single canonical fact that survived deterministic
 * conflict resolution. Each fact records which source won and why.</p>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 *
 * @param fact                the fact statement
 * @param value               the resolved value for this fact
 * @param confidence          confidence in this resolved fact (0.0–1.0)
 * @param winningSource       identifier of the source that won this conflict
 * @param supportingEvidenceIds IDs of evidence items supporting the winner
 */
public record ResolvedFact(
        String fact,
        String value,
        double confidence,
        String winningSource,
        List<String> supportingEvidenceIds
) {
    /**
     * Creates a new ResolvedFact with validation.
     *
     * @param fact                 the fact statement (must not be null)
     * @param value                the resolved value (must not be null)
     * @param confidence           confidence score (must be 0.0–1.0)
     * @param winningSource        the winning source identifier (must not be null)
     * @param supportingEvidenceIds IDs of supporting evidence (must not be null)
     * @throws NullPointerException     if fact, value, or winningSource is null
     * @throws IllegalArgumentException if confidence is outside [0.0, 1.0]
     */
    public ResolvedFact {
        Objects.requireNonNull(fact, "fact must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(winningSource, "winningSource must not be null");
        Objects.requireNonNull(supportingEvidenceIds, "supportingEvidenceIds must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        supportingEvidenceIds = List.copyOf(supportingEvidenceIds);
    }
}
