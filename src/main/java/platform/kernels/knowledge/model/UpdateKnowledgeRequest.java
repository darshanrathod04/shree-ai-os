package platform.kernels.knowledge.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>UpdateKnowledgeRequest</b>
 *
 * <p>Represents a request to update an existing knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates all parameters required to update a knowledge entity.</li>
 *   <li>Provides immutable request contract for knowledge updates.</li>
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
public final class UpdateKnowledgeRequest {

    private final KnowledgeId knowledgeId;
    private final String label;
    private final String description;
    private final Map<String, Object> metadata;

    private UpdateKnowledgeRequest(
            KnowledgeId knowledgeId,
            String label,
            String description,
            Map<String, Object> metadata) {
        this.knowledgeId = knowledgeId;
        this.label = label;
        this.description = description;
        this.metadata = metadata;
    }

    /**
     * Creates a new UpdateKnowledgeRequest with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param knowledgeId the unique identifier of the knowledge entity to update (must not be null)
     * @param label       the updated human-readable label (must not be null)
     * @param description the updated description (must not be null)
     * @param metadata    the updated metadata map (must not be null, will be defensively copied)
     * @return a new UpdateKnowledgeRequest instance
     * @throws NullPointerException if any required parameter is null
     */
    public static UpdateKnowledgeRequest of(
            KnowledgeId knowledgeId,
            String label,
            String description,
            Map<String, Object> metadata) {
        Objects.requireNonNull(knowledgeId, "knowledgeId must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(Map.copyOf(metadata));

        return new UpdateKnowledgeRequest(knowledgeId, label, description, unmodifiableMetadata);
    }

    /**
     * Returns the unique identifier of the knowledge entity to update.
     *
     * @return the knowledge identifier (never null)
     */
    public KnowledgeId getKnowledgeId() {
        return knowledgeId;
    }

    /**
     * Returns the updated label.
     *
     * @return the label (never null)
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the updated description.
     *
     * @return the description (never null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns an unmodifiable view of the updated metadata map.
     *
     * @return the unmodifiable metadata map (never null)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two UpdateKnowledgeRequest instances are equal if they have the same
     * knowledge identifier.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateKnowledgeRequest that = (UpdateKnowledgeRequest) o;
        return knowledgeId.equals(that.knowledgeId);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on the knowledge identifier
     */
    @Override
    public int hashCode() {
        return knowledgeId.hashCode();
    }

    /**
     * Returns a string representation of this request.
     *
     * @return a string containing the identifier and label
     */
    @Override
    public String toString() {
        return "UpdateKnowledgeRequest{" +
                "knowledgeId=" + knowledgeId +
                ", label='" + label + '\'' +
                '}';
    }
}