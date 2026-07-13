package platform.core.eventbus.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EventMetadata</b>
 *
 * <p>Represents the metadata associated with an event within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides immutable metadata for event tracking and correlation.</li>
 *   <li>Contains publisher information, priority, correlation ID, and optional attributes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Attributes map may be empty but never null.</p>
 */
public final class EventMetadata {

    private final String publisher;
    private final EventPriority priority;
    private final String correlationId;
    private final Map<String, Object> attributes;

    /**
     * Constructs a new {@code EventMetadata} with the given publisher, priority, and correlation ID.
     *
     * @param publisher     the event publisher (must not be null or blank)
     * @param priority      the event priority (must not be null)
     * @param correlationId the correlation ID (must not be null or blank)
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public EventMetadata(String publisher, EventPriority priority, String correlationId) {
        this(publisher, priority, correlationId, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code EventMetadata} with the given publisher, priority, correlation ID, and attributes.
     *
     * @param publisher     the event publisher (must not be null or blank)
     * @param priority      the event priority (must not be null)
     * @param correlationId the correlation ID (must not be null or blank)
     * @param attributes    optional attributes map (must not be null, may be empty)
     * @throws IllegalArgumentException if any required parameter is null or blank
     */
    public EventMetadata(String publisher, EventPriority priority, String correlationId, Map<String, Object> attributes) {
        if (publisher == null || publisher.isBlank()) {
            throw new IllegalArgumentException("Publisher must not be null or blank");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority must not be null");
        }
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("CorrelationId must not be null or blank");
        }
        this.publisher = publisher;
        this.priority = priority;
        this.correlationId = correlationId;
        this.attributes = Collections.unmodifiableMap(Objects.requireNonNull(attributes, "Attributes must not be null"));
    }

    /**
     * Returns the event publisher.
     *
     * @return the publisher
     */
    public String publisher() {
        return publisher;
    }

    /**
     * Returns the event priority.
     *
     * @return the priority
     */
    public EventPriority priority() {
        return priority;
    }

    /**
     * Returns the correlation ID.
     *
     * @return the correlation ID
     */
    public String correlationId() {
        return correlationId;
    }

    /**
     * Returns an unmodifiable map of optional attributes.
     *
     * @return the attributes map (empty if no attributes provided)
     */
    public Map<String, Object> attributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMetadata that = (EventMetadata) o;
        return publisher.equals(that.publisher)
                && priority == that.priority
                && correlationId.equals(that.correlationId)
                && attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisher, priority, correlationId, attributes);
    }

    @Override
    public String toString() {
        return "EventMetadata{"
                + "publisher='" + publisher + '\''
                + ", priority=" + priority
                + ", correlationId='" + correlationId + '\''
                + '}';
    }
}