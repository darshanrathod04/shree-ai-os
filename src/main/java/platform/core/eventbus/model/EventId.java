package platform.core.eventbus.model;

import java.util.Objects;
import java.util.UUID;

/**
 * <b>EventId</b>
 *
 * <p>Represents a unique event identifier within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a unique, immutable identity for each event.</li>
 *   <li>Enables event tracking, correlation, and deduplication.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class EventId {

    private final UUID value;

    /**
     * Constructs a new {@code EventId} with a randomly generated UUID.
     */
    public EventId() {
        this.value = UUID.randomUUID();
    }

    /**
     * Constructs a new {@code EventId} with the given UUID string.
     *
     * @param uuid the UUID string (must not be null or blank)
     * @throws IllegalArgumentException if {@code uuid} is null or blank
     */
    public EventId(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("EventId UUID must not be null or blank");
        }
        this.value = UUID.fromString(uuid);
    }

    /**
     * Returns the UUID value.
     *
     * @return the UUID value
     */
    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventId eventId = (EventId) o;
        return value.equals(eventId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}