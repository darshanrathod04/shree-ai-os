package com.shreeai.os.platform.kernels.inference.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>EvidencePackage</b>
 *
 * <p>Canonical immutable evidence consumed by the Inference Engine
 * after deterministic conflict resolution. All conflicting evidence
 * from the reasoning segment is resolved before inference executes.</p>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *     <li>Immutable - no method mutates this object.</li>
 *     <li>Thread-safe - all fields are deeply immutable.</li>
 *     <li>Never deletes discarded evidence; retains full conflict history.</li>
 * </ul>
 *
 * @param resolvedFacts     canonical accepted facts after conflict resolution
 * @param supportingEvidence evidence IDs supporting the winning facts
 * @param discardedEvidence  rejected evidence IDs (never deleted, preserved for audit)
 * @param conflicts         full conflict history across all resolved topics
 * @param overallConfidence aggregate confidence across the entire package
 * @param provenance        source lineage tracking where each fact originated
 */
public record EvidencePackage(
        List<ResolvedFact> resolvedFacts,
        List<String> supportingEvidence,
        List<String> discardedEvidence,
        List<ConflictRecord> conflicts,
        double overallConfidence,
        List<String> provenance
) {
    /**
     * Creates a new EvidencePackage with validation.
     *
     * @param resolvedFacts     canonical accepted facts (must not be null)
     * @param supportingEvidence evidence IDs supporting winners (must not be null)
     * @param discardedEvidence  rejected evidence IDs (must not be null)
     * @param conflicts         full conflict history (must not be null)
     * @param provenance        source lineage (must not be null)
     * @throws NullPointerException if any required parameter is null
     */
    public EvidencePackage {
        Objects.requireNonNull(resolvedFacts, "resolvedFacts must not be null");
        Objects.requireNonNull(supportingEvidence, "supportingEvidence must not be null");
        Objects.requireNonNull(discardedEvidence, "discardedEvidence must not be null");
        Objects.requireNonNull(conflicts, "conflicts must not be null");
        Objects.requireNonNull(provenance, "provenance must not be null");
        resolvedFacts = List.copyOf(resolvedFacts);
        supportingEvidence = List.copyOf(supportingEvidence);
        discardedEvidence = List.copyOf(discardedEvidence);
        conflicts = List.copyOf(conflicts);
        provenance = List.copyOf(provenance);
    }
}
