package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeNode</b>
 *
 * <p>Represents a semantic entity within the knowledge graph.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides identity for a semantic entity via {@link KnowledgeId}.</li>
 *   <li>Encapsulates semantic metadata including type, state, scope, and descriptive attributes.</li>
 *   <li>Provides classification via {@link KnowledgeType}.</li>
 *   <li>Serves as the base node type in the knowledge graph hierarchy.</li>
 *   <li>Remains generic and does not embed concept-specific behavior.</li>
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
 */
public final class KnowledgeNode {

    private final KnowledgeId id;
    private final KnowledgeType type;
    private final KnowledgeState state;
    private final KnowledgeScope scope;
    private final String label;
    private final String description;
    private final Map<String, Object> metadata;
    private final Instant createdAt;
    private final Instant updatedAt;

    private KnowledgeNode(
            KnowledgeId id,
            KnowledgeType type,
            KnowledgeState state,
            KnowledgeScope scope,
            String label,
            String description,
            Map<String, Object> metadata,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.scope = scope;
        this.label = label;
        this.description = description;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new KnowledgeNode with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param id          the unique identifier (must not be null)
     * @param type        the knowledge type (must not be null)
     * @param state       the knowledge state (must not be null)
     * @param scope       the knowledge scope (must not be null)
     * @param label       the human-readable label (must not be null)
     * @param description the description (must not be null)
     * @param metadata    the metadata map (must not be null, will be defensively copied)
     * @param createdAt   when the node was created (must not be null)
     * @param updatedAt   when the node was last updated (must not be null)
     * @return a new KnowledgeNode instance
     * @throws NullPointerException if any required parameter is null
     */
    public static KnowledgeNode of(
            KnowledgeId id,
            KnowledgeType type,
            KnowledgeState state,
            KnowledgeScope scope,
            String label,
            String description,
            Map<String, Object> metadata,
            Instant createdAt,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(Map.copyOf(metadata));

        return new KnowledgeNode(id, type, state, scope, label, description, unmodifiableMetadata, createdAt, updatedAt);
    }

    /**
     * Returns the unique identifier of this knowledge node.
     *
     * @return the knowledge identifier (never null)
     */
    public KnowledgeId getId() {
        return id;
    }

    /**
     * Returns the type classification of this knowledge node.
     *
     * @return the knowledge type (never null)
     */
    public KnowledgeType getType() {
        return type;
    }

    /**
     * Returns the lifecycle state of this knowledge node.
     *
     * @return the knowledge state (never null)
     */
    public KnowledgeState getState() {
        return state;
    }

    /**
     * Returns the visibility scope of this knowledge node.
     *
     * @return the knowledge scope (never null)
     */
    public KnowledgeScope getScope() {
        return scope;
    }

    /**
     * Returns the human-readable label of this knowledge node.
     *
     * @return the label (never null)
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the description of this knowledge node.
     *
     * @return the description (never null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns an unmodifiable view of the metadata map.
     *
     * @return the unmodifiable metadata map (never null)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Returns when this knowledge node was created.
     *
     * @return the creation timestamp (never null)
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns when this knowledge node was last updated.
     *
     * @return the last updated timestamp (never null)
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeNode instances are equal if they have the same identifier.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeNode that = (KnowledgeNode) o;
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
     * Returns a string representation of this knowledge node.
     *
     * @return a string containing the identifier and label
     */
    @Override
    public String toString() {
        return "KnowledgeNode{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", state=" + state +
                '}';
    }
}