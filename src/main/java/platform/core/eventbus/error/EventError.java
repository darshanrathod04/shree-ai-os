package platform.core.eventbus.error;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EventError</b>
 *
 * <p>Represents a structured error within the Event Bus subsystem
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides an immutable representation of an Event Bus error.</li>
 *   <li>Contains error code, message, timestamp, and optional details.</li>
 *   <li>Enables consistent error reporting across the Event Bus subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Details map may be empty but never null.</p>
 */
public final class EventError {

    private final EventErrorCode code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Constructs a new {@code EventError} with the given code and message.
     *
     * @param code    the error code (must not be null)
     * @param message the error message (must not be null or blank)
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public EventError(EventErrorCode code, String message) {
        this(code, message, Instant.now(), Collections.emptyMap());
    }

    /**
     * Constructs a new {@code EventError} with the given code, message, and details.
     *
     * @param code     the error code (must not be null)
     * @param message  the error message (must not be null or blank)
     * @param details  optional details map (must not be null, may be empty)
     * @throws IllegalArgumentException if any required parameter is null or blank
     */
    public EventError(EventErrorCode code, String message, Map<String, Object> details) {
        this(code, message, Instant.now(), details);
    }

    /**
     * Constructs a new {@code EventError} with all fields.
     *
     * @param code      the error code (must not be null)
     * @param message   the error message (must not be null or blank)
     * @param timestamp the error timestamp (must not be null)
     * @param details   optional details map (must not be null, may be empty)
     * @throws IllegalArgumentException if any required parameter is null or blank
     */
    public EventError(EventErrorCode code, String message, Instant timestamp, Map<String, Object> details) {
        if (code == null) {
            throw new IllegalArgumentException("EventErrorCode must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("EventError message must not be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("EventError timestamp must not be null");
        }
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.details = Collections.unmodifiableMap(Objects.requireNonNull(details, "Details must not be null"));
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public EventErrorCode code() {
        return code;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the error timestamp.
     *
     * @return the error timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns an unmodifiable map of optional error details.
     *
     * @return the details map (empty if no details provided)
     */
    public Map<String, Object> details() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventError eventError = (EventError) o;
        return code == eventError.code
                && message.equals(eventError.message)
                && timestamp.equals(eventError.timestamp)
                && details.equals(eventError.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, timestamp, details);
    }

    @Override
    public String toString() {
        return "EventError{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}