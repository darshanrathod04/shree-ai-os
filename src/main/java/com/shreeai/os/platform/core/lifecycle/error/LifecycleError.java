package com.shreeai.os.platform.core.lifecycle.error;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>LifecycleError</b>
 *
 * <p>Immutable error description for lifecycle operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a structured, immutable error description.</li>
 *   <li>Contains error code, message, timestamp, and optional details.</li>
 *   <li>Supports the LifecycleException hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Details map may be empty but never null.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see LifecycleErrorCode
 * @see LifecycleException
 */
public final class LifecycleError {

    private final LifecycleErrorCode code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Constructs a new {@code LifecycleError} with the given code and message.
     *
     * @param code    the error code (must not be null)
     * @param message the error message (must not be null or blank)
     * @throws NullPointerException if {@code code} is null
     * @throws IllegalArgumentException if {@code message} is null or blank
     */
    public LifecycleError(LifecycleErrorCode code, String message) {
        this(code, message, Instant.now(), Collections.emptyMap());
    }

    /**
     * Constructs a new {@code LifecycleError} with the given code, message, and timestamp.
     *
     * @param code      the error code (must not be null)
     * @param message   the error message (must not be null or blank)
     * @param timestamp the instant when the error occurred (must not be null)
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if {@code message} is null or blank
     */
    public LifecycleError(LifecycleErrorCode code, String message, Instant timestamp) {
        this(code, message, timestamp, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code LifecycleError} with the given code, message, timestamp, and details.
     *
     * @param code      the error code (must not be null)
     * @param message   the error message (must not be null or blank)
     * @param timestamp the instant when the error occurred (must not be null)
     * @param details   optional details map (must not be null, may be empty)
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if {@code message} is null or blank
     */
    public LifecycleError(LifecycleErrorCode code, String message, Instant timestamp, Map<String, Object> details) {
        if (code == null) {
            throw new NullPointerException("LifecycleErrorCode must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("LifecycleError message must not be null or blank");
        }
        this.code = code;
        this.message = message;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.details = Collections.unmodifiableMap(Objects.requireNonNull(details, "Details must not be null"));
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public LifecycleErrorCode code() {
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
     * Compares this {@code LifecycleError} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code LifecycleError} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LifecycleError that = (LifecycleError) o;
        return code == that.code
                && message.equals(that.message)
                && timestamp.equals(that.timestamp)
                && details.equals(that.details);
    }

    /**
     * Returns the hash code for this {@code LifecycleError}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, message, timestamp, details);
    }

    /**
     * Returns a string representation of this {@code LifecycleError}.
     *
     * @return a string containing the code, message, and timestamp
     */
    @Override
    public String toString() {
        return "LifecycleError{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}