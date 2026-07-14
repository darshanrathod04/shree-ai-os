package platform.core.plugin.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PluginError</b>
 *
 * <p>Immutable error model for the Plugin subsystem within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates error information for plugin operations.</li>
 *   <li>Provides structured error data with code, message, timestamp, and details.</li>
 *   <li>Enables consistent error reporting across the Plugin subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Details map may be empty but never null.</p>
 */
public final class PluginError {

    private final PluginErrorCode code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Constructs a new {@code PluginError} with the given parameters.
     *
     * @param code      the error code (must not be null)
     * @param message   the error message (must not be null or blank)
     * @param timestamp the error timestamp (must not be null)
     * @param details   the error details (must not be null)
     * @throws IllegalArgumentException if any required parameter is null or blank
     */
    public PluginError(PluginErrorCode code,
                       String message,
                       Instant timestamp,
                       Map<String, Object> details) {
        this.code = Objects.requireNonNull(code, "PluginErrorCode must not be null");
        this.message = Objects.requireNonNull(message, "Message must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.details = Collections.unmodifiableMap(new HashMap<>(details != null ? details : new HashMap<>()));
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public PluginErrorCode code() {
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
     * Returns the error details.
     *
     * @return an unmodifiable map of error details (empty if no details)
     */
    public Map<String, Object> details() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginError that = (PluginError) o;
        return code == that.code
                && message.equals(that.message)
                && timestamp.equals(that.timestamp)
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, timestamp, details);
    }

    @Override
    public String toString() {
        return "PluginError{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", timestamp=" + timestamp
                + ", details=" + details
                + '}';
    }
}