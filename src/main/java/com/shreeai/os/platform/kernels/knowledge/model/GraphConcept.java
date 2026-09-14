package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>GraphConcept</b>
 *
 * <p>One immutable, canonically named concept of a {@link ConceptGraph}.
 * Names are canonicalized by the builder's technology dictionary - aliases
 * such as {@code springboot}, {@code spring boot} or {@code Spring Boot} all
 * collapse into the single canonical concept {@code Spring Boot}. There are
 * no mutable aliases.</p>
 *
 * <p><b>Deterministic identity:</b> {@code conceptId} is derived by the graph
 * builder as {@code SHA-256(type + canonicalName)} and is stable across
 * identical inputs.</p>
 *
 * <p><b>Note:</b> the class name deliberately differs from the legacy
 * {@code KnowledgeConcept} (retrieval-oriented model), which remains
 * untouched for backward compatibility.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param conceptId the deterministic concept id (never null)
 * @param name      the canonical concept name (never null or blank)
 * @param type      the deterministic concept family (never null)
 */
public record GraphConcept(String conceptId, String name, ConceptType type) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if any field is null
     * @throws IllegalArgumentException if conceptId or name is blank
     */
    public GraphConcept {
        Objects.requireNonNull(conceptId, "conceptId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (conceptId.isBlank()) {
            throw new IllegalArgumentException("conceptId must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    /**
     * Creates a validated, immutable graph concept.
     *
     * @param conceptId deterministic concept id (must not be null or blank)
     * @param name      canonical concept name (must not be null or blank)
     * @param type      concept family (must not be null)
     * @return a new immutable GraphConcept (never null)
     */
    public static GraphConcept of(String conceptId, String name, ConceptType type) {
        return new GraphConcept(conceptId, name, type);
    }

    @Override
    public String toString() {
        return String.format("GraphConcept{name=%s, type=%s}", name, type);
    }
}
