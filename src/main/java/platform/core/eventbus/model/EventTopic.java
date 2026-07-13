package platform.core.eventbus.model;

import java.util.Objects;

/**
 * <b>EventTopic</b>
 *
 * <p>Represents a topic to which events can be published and subscribers
 * can subscribe within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed, immutable topic identifier for event routing.</li>
 *   <li>Enables topic-based publish/subscribe semantics.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class EventTopic {

    private final String name;

    /**
     * Constructs a new {@code EventTopic} with the given name.
     *
     * @param name the topic name (must not be null or blank)
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public EventTopic(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("EventTopic name must not be null or blank");
        }
        this.name = name;
    }

    /**
     * Returns the topic name.
     *
     * @return the topic name
     */
    public String value() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventTopic that = (EventTopic) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}