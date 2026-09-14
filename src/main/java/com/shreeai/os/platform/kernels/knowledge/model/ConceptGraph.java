package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>ConceptGraph</b>
 *
 * <p>The canonical, immutable graph artifact produced by the Knowledge Graph
 * Builder: every concept discovered in a {@link KnowledgeDocument} together
 * with every deterministic relationship between them. Future kernels consume
 * this artifact instead of raw chunks.</p>
 *
 * <p><b>Canonical ordering:</b> concepts are ordered by canonical name (ties
 * broken by concept id) and relationships by source id, type and target id -
 * both orders are stable, so identical inputs produce structurally equal
 * graphs.</p>
 *
 * <p><b>Note:</b> the class name deliberately differs from the legacy
 * {@code KnowledgeGraph} (retrieval-oriented model), which remains untouched
 * for backward compatibility.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param concepts      the ordered immutable concept list (never null)
 * @param relationships the ordered immutable relationship list (never null)
 */
public record ConceptGraph(List<GraphConcept> concepts, List<ConceptRelationship> relationships) {

    /**
     * Compact constructor that validates, deterministically orders and
     * defensively copies both lists so the record is deeply immutable.
     *
     * @throws NullPointerException if concepts or relationships is null
     */
    public ConceptGraph {
        Objects.requireNonNull(concepts, "concepts must not be null");
        Objects.requireNonNull(relationships, "relationships must not be null");
        concepts = concepts.stream()
                .sorted(Comparator.comparing(GraphConcept::name)
                        .thenComparing(GraphConcept::conceptId))
                .toList();
        relationships = relationships.stream()
                .sorted(Comparator.comparing(ConceptRelationship::fromConcept)
                        .thenComparing(r -> r.type().name())
                        .thenComparing(ConceptRelationship::toConcept))
                .toList();
    }

    /**
     * Returns the number of concepts in the graph.
     *
     * @return the concept count (never negative)
     */
    public int conceptCount() {
        return concepts.size();
    }

    /**
     * Returns the number of relationships in the graph.
     *
     * @return the relationship count (never negative)
     */
    public int relationshipCount() {
        return relationships.size();
    }

    @Override
    public String toString() {
        return String.format("ConceptGraph{concepts=%d, relationships=%d}",
                concepts.size(), relationships.size());
    }
}
