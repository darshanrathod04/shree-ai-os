package platform.kernels.knowledge.model;

/**
 * <b>KnowledgeRelationshipType</b>
 *
 * <p>Defines the semantic type of a relationship between knowledge entities within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible semantic relationship types.</li>
 *   <li>Provides type safety for relationship classification.</li>
 *   <li>Immutable enum.</li>
 * </ul>
 *
 * <p><b>Relationship Types:</b></p>
 * <ul>
 *   <li>IS_A - Specialization or subtype relationship</li>
 *   <li>PART_OF - Composition or aggregation relationship</li>
 *   <li>RELATED_TO - General semantic association</li>
 *   <li>DEPENDS_ON - Dependency relationship</li>
 *   <li>DERIVED_FROM - Derivation or inheritance relationship</li>
 *   <li>REFERENCES - Referential relationship</li>
 *   <li>CAUSES - Causal relationship</li>
 *   <li>SYNONYM_OF - Synonym or equivalent relationship</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 */
public enum KnowledgeRelationshipType {
    /**
     * Specialization or subtype relationship.
     */
    IS_A,

    /**
     * Composition or aggregation relationship.
     */
    PART_OF,

    /**
     * General semantic association.
     */
    RELATED_TO,

    /**
     * Dependency relationship.
     */
    DEPENDS_ON,

    /**
     * Derivation or inheritance relationship.
     */
    DERIVED_FROM,

    /**
     * Referential relationship.
     */
    REFERENCES,

    /**
     * Causal relationship.
     */
    CAUSES,

    /**
     * Synonym or equivalent relationship.
     */
    SYNONYM_OF
}