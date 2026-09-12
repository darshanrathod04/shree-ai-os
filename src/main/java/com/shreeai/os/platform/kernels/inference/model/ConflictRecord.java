package com.shreeai.os.platform.kernels.inference.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>ConflictRecord</b>
 *
 * <p>Immutable record of a single conflict resolution event.
 * Losing facts are never deleted - they are preserved in the
 * {@code rejectedFacts} field for full auditability.</p>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 *
 * @param topic             the subject area where conflict occurred
 * @param winningFact       the fact that won the conflict
 * @param rejectedFacts     facts rejected by this resolution (never deleted)
 * @param resolutionReason  deterministic explanation of why the winner was chosen
 * @param credibilitySummary credibility scores for the resolution
 * @param resolvedAt        instant when the conflict was resolved
 */
public record ConflictRecord(
        String topic,
        String winningFact,
        List<String> rejectedFacts,
        String resolutionReason,
        CredibilityScore credibilitySummary,
        Instant resolvedAt
) {
    /**
     * Creates a new ConflictRecord with validation.
     *
     * @param topic              the conflict topic (must not be null)
     * @param winningFact        the winning fact (must not be null)
     * @param rejectedFacts      rejected facts (must not be null)
     * @param resolutionReason   deterministic explanation (must not be null)
     * @param credibilitySummary credibility scores (must not be null)
     * @param resolvedAt         resolution instant (must not be null)
     * @throws NullPointerException if any required parameter is null
     */
    public ConflictRecord {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(winningFact, "winningFact must not be null");
        Objects.requireNonNull(rejectedFacts, "rejectedFacts must not be null");
        Objects.requireNonNull(resolutionReason, "resolutionReason must not be null");
        Objects.requireNonNull(credibilitySummary, "credibilitySummary must not be null");
        Objects.requireNonNull(resolvedAt, "resolvedAt must not be null");
        rejectedFacts = List.copyOf(rejectedFacts);
    }
}
