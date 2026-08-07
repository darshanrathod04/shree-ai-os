package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CreateKnowledgeRequest</b>
 *
 * <p>Represents a request to create a new knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates all parameters required to create a knowledge entity.</li>
 *   <li>Provides immutable request contract for knowledge creation.</li>
 *   <li>Includes constructor validation for required parameters.</li>
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
public final class CreateKnowledgeRequest {

    private final KnowledgeType type;
    private final KnowledgeState state;
    private final KnowledgeScope scope;
    private final String label;
    private final String description;
    private final Map<String, Object> metadata;

    private CreateKnowledgeRequest(
            KnowledgeType type,
            KnowledgeState state,
            KnowledgeScope scope,
            String label,
            String description,
            Map<String, Object> metadata) {
        this.type = type;
        this.state = state;
        this.scope = scope;
        this.label = label;
        this.description = description;
        this.metadata = metadata;
    }

    /**
     * Creates a new CreateKnowledgeRequest with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param type        the knowledge type (must not be null)
     * @param state       the knowledge state (must not be null)
     * @param scope       the knowledge scope (must not be null)
     * @param label       the human-readable label (must not be null)
     * @param description the description (must not be null)
     * @param metadata    the metadata map (must not be null, will be defensively copied)
     * @return a new CreateKnowledgeRequest instance
     * @throws NullPointerException if any required parameter is null
     */
    public static CreateKnowledgeRequest of(
            KnowledgeType type,
            KnowledgeState state,
            KnowledgeScope scope,
            String label,
            String description,
            Map<String, Object> metadata) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(Map.copyOf(metadata));

        return new CreateKnowledgeRequest(type, state, scope, label, description, unmodifiableMetadata);
    }

    /**
     * Returns the knowledge type of the entity to create.
     *
     * @return the knowledge type (never null)
     */
    public KnowledgeType getType() {
        return type;
    }

    /**
     * Returns the knowledge state of the entity to create.
     *
     * @return the knowledge state (never null)
     */
    public KnowledgeState getState() {
        return state;
    }

    /**
     * Returns the knowledge scope of the entity to create.
     *
     * @return the knowledge scope (never null)
     */
    public KnowledgeScope getScope() {
        return scope;
    }

    /**
     * Returns the label of the entity to create.
     *
     * @return the label (never null)
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the description of the entity to create.
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
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two CreateKnowledgeRequest instances are equal if they have the same
     * type, label, and description.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateKnowledgeRequest that = (CreateKnowledgeRequest) o;
        return type == that.type
                && scope == that.scope
                && label.equals(that.label)
                && description.equals(that.description);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on type, scope, label, and description
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, scope, label, description);
    }

    /**
     * Returns a string representation of this request.
     *
     * @return a string containing the type and label
     */
    @Override
    public String toString() {
        return "CreateKnowledgeRequest{" +
                "type=" + type +
                ", label='" + label + '\'' +
                ", scope=" + scope +
                '}';
    }
}