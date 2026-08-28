package com.shreeai.os.platform.core.discovery.error;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DiscoveryError</b>
 *
 * <p>Immutable error description for the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a structured, immutable representation of a discovery error.</li>
 *   <li>Encapsulates error code, message, timestamp, and optional details.</li>
 *   <li>Enables consistent error reporting across the Discovery Service.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Details map may be empty but never null.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryErrorCode
 * @see DiscoveryException
 */
public final class DiscoveryError {

    private final DiscoveryErrorCode code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Constructs a new {@code DiscoveryError} with the given code, message, and timestamp.
     *
     * @param code      the error code (must not be null)
     * @param message   the human-readable error message (must not be null)
     * @param timestamp the instant when the error occurred (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DiscoveryError(DiscoveryErrorCode code, String message, Instant timestamp) {
        this(code, message, timestamp, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code DiscoveryError} with the given code, message, timestamp, and details.
     *
     * @param code      the error code (must not be null)
     * @param message   the human-readable error message (must not be null)
     * @param timestamp the instant when the error occurred (must not be null)
     * @param details   optional details map (must not be null, may be empty)
     * @throws NullPointerException if any parameter is null
     */
    public DiscoveryError(DiscoveryErrorCode code, String message, Instant timestamp, Map<String, Object> details) {
        this.code = Objects.requireNonNull(code, "Error code must not be null");
        this.message = Objects.requireNonNull(message, "Error message must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.details = Collections.unmodifiableMap(Objects.requireNonNull(details, "Details must not be null"));
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public DiscoveryErrorCode code() {
        return code;
    }

    /**
     * Returns the human-readable error message.
     *
     * @return the error message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the instant when the error occurred.
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

    /**
     * Compares this {@code DiscoveryError} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code DiscoveryError} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryError that = (DiscoveryError) o;
        return code == that.code
                && message.equals(that.message)
                && timestamp.equals(that.timestamp)
                && details.equals(that.details);
    }

    /**
     * Returns the hash code for this {@code DiscoveryError}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, message, timestamp, details);
    }

    /**
     * Returns a string representation of this {@code DiscoveryError}.
     *
     * @return a string containing the error code, message, and timestamp
     */
    @Override
    public String toString() {
        return "DiscoveryError{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}