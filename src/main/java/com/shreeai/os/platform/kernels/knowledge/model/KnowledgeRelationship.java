package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeRelationship</b>
 *
 * <p>Represents a semantic relationship between two knowledge nodes within the knowledge graph.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines a directed semantic connection from a source node to a target node.</li>
 *   <li>Encapsulates the relationship type via {@link KnowledgeRelationshipType}.</li>
 *   <li>Carries relationship-specific metadata for semantic context.</li>
 *   <li>Remains immutable — relationships are created and removed, never modified.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. The metadata map is defensively copied.</p>
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
 * @see KnowledgeRelationshipType
 */
public final class KnowledgeRelationship {

    private final KnowledgeId id;
    private final KnowledgeId sourceNodeId;
    private final KnowledgeId targetNodeId;
    private final KnowledgeRelationshipType type;
    private final String label;
    private final Map<String, Object> metadata;
    private final Instant createdAt;

    private KnowledgeRelationship(
            KnowledgeId id,
            KnowledgeId sourceNodeId,
            KnowledgeId targetNodeId,
            KnowledgeRelationshipType type,
            String label,
            Map<String, Object> metadata,
            Instant createdAt) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.type = type;
        this.label = label;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new KnowledgeRelationship with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param id            the unique identifier for this relationship (must not be null)
     * @param sourceNodeId  the identifier of the source knowledge node (must not be null)
     * @param targetNodeId  the identifier of the target knowledge node (must not be null)
     * @param type          the semantic relationship type (must not be null)
     * @param label         the human-readable label (must not be null)
     * @param metadata      the relationship metadata map (must not be null, will be defensively copied)
     * @param createdAt     when the relationship was created (must not be null)
     * @return a new KnowledgeRelationship instance
     * @throws NullPointerException if any required parameter is null
     */
    public static KnowledgeRelationship of(
            KnowledgeId id,
            KnowledgeId sourceNodeId,
            KnowledgeId targetNodeId,
            KnowledgeRelationshipType type,
            String label,
            Map<String, Object> metadata,
            Instant createdAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(sourceNodeId, "sourceNodeId must not be null");
        Objects.requireNonNull(targetNodeId, "targetNodeId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(Map.copyOf(metadata));

        return new KnowledgeRelationship(id, sourceNodeId, targetNodeId, type, label, unmodifiableMetadata, createdAt);
    }

    /**
     * Returns the unique identifier of this relationship.
     *
     * @return the relationship identifier (never null)
     */
    public KnowledgeId getId() {
        return id;
    }

    /**
     * Returns the identifier of the source knowledge node.
     *
     * @return the source node identifier (never null)
     */
    public KnowledgeId getSourceNodeId() {
        return sourceNodeId;
    }

    /**
     * Returns the identifier of the target knowledge node.
     *
     * @return the target node identifier (never null)
     */
    public KnowledgeId getTargetNodeId() {
        return targetNodeId;
    }

    /**
     * Returns the semantic type of this relationship.
     *
     * @return the relationship type (never null)
     */
    public KnowledgeRelationshipType getType() {
        return type;
    }

    /**
     * Returns the human-readable label of this relationship.
     *
     * @return the label (never null)
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns an unmodifiable view of the relationship metadata map.
     *
     * @return the unmodifiable metadata map (never null)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Returns when this relationship was created.
     *
     * @return the creation timestamp (never null)
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeRelationship instances are equal if they have the same identifier.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeRelationship that = (KnowledgeRelationship) o;
        return id.equals(that.id);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on the identifier
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns a string representation of this relationship.
     *
     * @return a string containing the identifier, source, target, and type
     */
    @Override
    public String toString() {
        return "KnowledgeRelationship{" +
                "id=" + id +
                ", source=" + sourceNodeId +
                ", target=" + targetNodeId +
                ", type=" + type +
                '}';
    }
}