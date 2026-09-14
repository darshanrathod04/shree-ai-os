package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>ConceptRelationship</b>
 *
 * <p>One immutable, directed, typed edge of a {@link ConceptGraph}. Edges are
 * produced exclusively by the deterministic rules of the Knowledge Graph
 * Builder - there is no probabilistic inference and there are no timestamps.</p>
 *
 * <p><b>Deterministic identity:</b> {@code relationshipId} is derived by the
 * graph builder as {@code SHA-256(fromConcept + type + toConcept)} and is
 * stable across identical inputs.</p>
 *
 * <p><b>Deterministic confidence:</b> {@code confidence} is one of the locked
 * constant tiers of the builder (title {@code 1.00}, heading {@code 0.95},
 * explicit prerequisite {@code 0.90}, section co-occurrence {@code 0.80},
 * paragraph relation {@code 0.70}) - never a computed probability.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param relationshipId the deterministic relationship id (never null)
 * @param fromConcept    the concept id of the edge source (never null)
 * @param toConcept      the concept id of the edge target (never null)
 * @param type           the locked relationship kind (never null)
 * @param confidence     the locked deterministic confidence tier, in
 *                       {@code [0.0, 1.0]}
 */
public record ConceptRelationship(
        String relationshipId,
        String fromConcept,
        String toConcept,
        RelationshipType type,
        double confidence) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if ids are blank, both endpoints are
     *                                  equal or confidence is out of range
     */
    public ConceptRelationship {
        Objects.requireNonNull(relationshipId, "relationshipId must not be null");
        Objects.requireNonNull(fromConcept, "fromConcept must not be null");
        Objects.requireNonNull(toConcept, "toConcept must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (relationshipId.isBlank()) {
            throw new IllegalArgumentException("relationshipId must not be blank");
        }
        if (fromConcept.isBlank()) {
            throw new IllegalArgumentException("fromConcept must not be blank");
        }
        if (toConcept.isBlank()) {
            throw new IllegalArgumentException("toConcept must not be blank");
        }
        if (fromConcept.equals(toConcept)) {
            throw new IllegalArgumentException("self relationships are not allowed");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
    }

    /**
     * Creates a validated, immutable concept relationship.
     *
     * @param relationshipId deterministic relationship id (must not be null)
     * @param fromConcept    source concept id (must not be null)
     * @param toConcept      target concept id (must not be null)
     * @param type           relationship kind (must not be null)
     * @param confidence     deterministic confidence within {@code [0.0, 1.0]}
     * @return a new immutable ConceptRelationship (never null)
     */
    public static ConceptRelationship of(String relationshipId,
                                        String fromConcept,
                                        String toConcept,
                                        RelationshipType type,
                                        double confidence) {
        return new ConceptRelationship(relationshipId, fromConcept, toConcept, type, confidence);
    }

    @Override
    public String toString() {
        return String.format("ConceptRelationship{type=%s, confidence=%.2f}", type, confidence);
    }
}
