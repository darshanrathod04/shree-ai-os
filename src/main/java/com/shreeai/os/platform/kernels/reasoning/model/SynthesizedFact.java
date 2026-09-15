package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>SynthesizedFact</b>
 *
 * <p>An immutable, deterministic fact synthesized from one evidence cluster.
 * The statement is a locked template - there is no natural-language
 * creativity, no summarization and no timestamps. Provenance is fully
 * preserved through {@code supportingEvidenceIds}.</p>
 *
 * <p><b>Deterministic identity:</b> {@code factId} is derived as
 * {@code SHA-256(FACT | statement)} and is stable across identical
 * inputs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param factId               the deterministic fact id (never null)
 * @param statement            the deterministic fact statement (never null
 *                             or blank)
 * @param supportingEvidenceIds the chunk ids of the evidence supporting this
 *                             fact, stably ordered (never null)
 * @param confidence           the aggregated confidence within {@code [0.0,
 *                             1.0]}
 */
public record SynthesizedFact(
        String factId,
        String statement,
        List<String> supportingEvidenceIds,
        double confidence) {

    /**
     * Compact constructor that validates, canonically orders and defensively
     * copies all fields.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if id or statement is blank or the
     *                                  confidence is out of range
     */
    public SynthesizedFact {
        Objects.requireNonNull(factId, "factId must not be null");
        Objects.requireNonNull(statement, "statement must not be null");
        Objects.requireNonNull(supportingEvidenceIds, "supportingEvidenceIds must not be null");
        if (factId.isBlank()) {
            throw new IllegalArgumentException("factId must not be blank");
        }
        if (statement.isBlank()) {
            throw new IllegalArgumentException("statement must not be blank");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
        supportingEvidenceIds = supportingEvidenceIds.stream()
                .distinct()
                .sorted(java.util.Comparator.naturalOrder())
                .toList();
    }

    @Override
    public String toString() {
        return String.format("SynthesizedFact{statement=%s, confidence=%.4f}",
                statement, confidence);
    }
}
