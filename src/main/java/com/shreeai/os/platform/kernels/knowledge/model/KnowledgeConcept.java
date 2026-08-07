package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeConcept</b>
 *
 * <p>Represents a semantic concept within the knowledge graph. Specializes {@link KnowledgeNode}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents a semantic concept with conceptual meaning and classification.</li>
 *   <li>Encapsulates concept-specific metadata including synonyms and canonical form.</li>
 *   <li>Extends {@link KnowledgeNode} to inherit identity, type, state, and scope.</li>
 *   <li>Does not place graph responsibilities here — graph operations belong to {@link KnowledgeGraph}.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. The synonyms list is defensively copied.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 *
 * @see KnowledgeNode
 * @see KnowledgeGraph
 */
public final class KnowledgeConcept {

    private final KnowledgeNode node;
    private final String canonicalName;
    private final String[] synonyms;
    private final String domain;
    private final Map<String, Object> conceptMetadata;

    private KnowledgeConcept(
            KnowledgeNode node,
            String canonicalName,
            String[] synonyms,
            String domain,
            Map<String, Object> conceptMetadata) {
        this.node = node;
        this.canonicalName = canonicalName;
        this.synonyms = synonyms;
        this.domain = domain;
        this.conceptMetadata = conceptMetadata;
    }

    /**
     * Creates a new KnowledgeConcept with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The synonyms array and conceptMetadata
     * map are defensively copied to ensure immutability.</p>
     *
     * @param node            the underlying knowledge node (must not be null)
     * @param canonicalName   the canonical name for this concept (must not be null or empty)
     * @param synonyms        the array of synonym terms (must not be null, will be defensively copied)
     * @param domain          the domain or subject area (must not be null)
     * @param conceptMetadata the concept-specific metadata map (must not be null, will be defensively copied)
     * @return a new KnowledgeConcept instance
     * @throws NullPointerException     if any required parameter is null
     * @throws IllegalArgumentException if {@code canonicalName} is empty
     */
    public static KnowledgeConcept of(
            KnowledgeNode node,
            String canonicalName,
            String[] synonyms,
            String domain,
            Map<String, Object> conceptMetadata) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(canonicalName, "canonicalName must not be null");
        Objects.requireNonNull(synonyms, "synonyms must not be null");
        Objects.requireNonNull(domain, "domain must not be null");
        Objects.requireNonNull(conceptMetadata, "conceptMetadata must not be null");

        if (canonicalName.isBlank()) {
            throw new IllegalArgumentException("canonicalName must not be blank");
        }

        String[] synonymsCopy = new String[synonyms.length];
        System.arraycopy(synonyms, 0, synonymsCopy, 0, synonyms.length);

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(Map.copyOf(conceptMetadata));

        return new KnowledgeConcept(node, canonicalName, synonymsCopy, domain, unmodifiableMetadata);
    }

    /**
     * Returns the underlying knowledge node.
     *
     * @return the knowledge node (never null)
     */
    public KnowledgeNode getNode() {
        return node;
    }

    /**
     * Returns the unique identifier of this concept.
     *
     * @return the knowledge identifier (never null)
     */
    public KnowledgeId getId() {
        return node.getId();
    }

    /**
     * Returns the canonical name for this concept.
     *
     * @return the canonical name (never null)
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Returns a defensively copied array of synonym terms for this concept.
     *
     * @return a fresh copy of the synonyms array (never null)
     */
    public String[] getSynonyms() {
        String[] result = new String[synonyms.length];
        System.arraycopy(synonyms, 0, result, 0, synonyms.length);
        return result;
    }

    /**
     * Returns the domain or subject area of this concept.
     *
     * @return the domain (never null)
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns an unmodifiable view of the concept-specific metadata map.
     *
     * @return the unmodifiable concept metadata map (never null)
     */
    public Map<String, Object> getConceptMetadata() {
        return conceptMetadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeConcept instances are equal if they have the same underlying
     * knowledge node identifier.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeConcept that = (KnowledgeConcept) o;
        return node.getId().equals(that.node.getId());
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on the underlying node identifier
     */
    @Override
    public int hashCode() {
        return node.getId().hashCode();
    }

    /**
     * Returns a string representation of this knowledge concept.
     *
     * @return a string containing the identifier and canonical name
     */
    @Override
    public String toString() {
        return "KnowledgeConcept{" +
                "id=" + node.getId() +
                ", canonicalName='" + canonicalName + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}