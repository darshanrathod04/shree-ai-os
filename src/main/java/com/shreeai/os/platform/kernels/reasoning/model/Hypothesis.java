package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>Hypothesis</b>
 *
 * <p>An immutable, deterministic hypothesis derived from one completed
 * multi-hop reasoning chain. Statements are produced from a locked template -
 * there is no natural-language creativity and there are no timestamps.</p>
 *
 * <p><b>Deterministic identity:</b> {@code hypothesisId} is derived as
 * {@code SHA-256(HYPOTHESIS | statement)} and is stable across identical
 * inputs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param hypothesisId          the deterministic hypothesis id (never null)
 * @param statement             the deterministic hypothesis statement (never
 *                              null or blank)
 * @param supportingEvidenceIds the chunk ids of the trusted evidence that
 *                              supports the chain (never null)
 * @param confidence            the deterministic confidence within
 *                              {@code [0.0, 1.0]}
 */
public record Hypothesis(
        String hypothesisId,
        String statement,
        List<String> supportingEvidenceIds,
        double confidence) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if id or statement is blank or the
     *                                  confidence is out of range
     */
    public Hypothesis {
        Objects.requireNonNull(hypothesisId, "hypothesisId must not be null");
        Objects.requireNonNull(statement, "statement must not be null");
        Objects.requireNonNull(supportingEvidenceIds, "supportingEvidenceIds must not be null");
        if (hypothesisId.isBlank()) {
            throw new IllegalArgumentException("hypothesisId must not be blank");
        }
        if (statement.isBlank()) {
            throw new IllegalArgumentException("statement must not be blank");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
        supportingEvidenceIds = List.copyOf(supportingEvidenceIds);
    }

    @Override
    public String toString() {
        return String.format("Hypothesis{statement=%s, confidence=%.4f, evidence=%d}",
                statement, confidence, supportingEvidenceIds.size());
    }
}
