package com.shreeai.os.platform.core.eventbus.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>Event</b>
 *
 * <p>Represents a platform event within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides an immutable representation of a platform event.</li>
 *   <li>Contains event identity, topic, metadata, payload, and timestamp.</li>
 *   <li>Enables decoupled communication between Platform components.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null and validated at construction time.</p>
 */
public final class Event {

    private final EventId id;
    private final EventTopic topic;
    private final EventMetadata metadata;
    private final Object payload;
    private final Instant timestamp;

    /**
     * Constructs a new {@code Event} with the given id, topic, metadata, and payload.
     *
     * @param id       the event identifier (must not be null)
     * @param topic    the event topic (must not be null)
     * @param metadata the event metadata (must not be null)
     * @param payload  the event payload (may be null)
     * @throws NullPointerException if any required parameter is null
     */
    public Event(EventId id, EventTopic topic, EventMetadata metadata, Object payload) {
        this.id = Objects.requireNonNull(id, "EventId must not be null");
        this.topic = Objects.requireNonNull(topic, "EventTopic must not be null");
        this.metadata = Objects.requireNonNull(metadata, "EventMetadata must not be null");
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    /**
     * Returns the event identifier.
     *
     * @return the event identifier
     */
    public EventId id() {
        return id;
    }

    /**
     * Returns the event topic.
     *
     * @return the event topic
     */
    public EventTopic topic() {
        return topic;
    }

    /**
     * Returns the event metadata.
     *
     * @return the event metadata
     */
    public EventMetadata metadata() {
        return metadata;
    }

    /**
     * Returns the event payload.
     *
     * @return the event payload (may be null)
     */
    public Object payload() {
        return payload;
    }

    /**
     * Returns the instant when the event was created.
     *
     * @return the event timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id.equals(event.id)
                && topic.equals(event.topic)
                && metadata.equals(event.metadata)
                && Objects.equals(payload, event.payload)
                && timestamp.equals(event.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topic, metadata, payload, timestamp);
    }

    @Override
    public String toString() {
        return "Event{"
                + "id=" + id
                + ", topic=" + topic
                + ", metadata=" + metadata
                + ", timestamp=" + timestamp
                + '}';
    }
}