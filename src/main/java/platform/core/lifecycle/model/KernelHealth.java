package platform.core.lifecycle.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KernelHealth</b>
 *
 * <p>Represents the runtime health status of a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides an immutable snapshot of a kernel's health status.</li>
 *   <li>Enables health queries through the LifecycleService API.</li>
 *   <li>Contains no business logic — pure data carrier.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Details map may be empty but never null.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-010</p>
 *
 * @see platform.core.lifecycle.api.LifecycleService
 */
public final class KernelHealth {

    private final String status;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Constructs a new {@code KernelHealth} with the given status and message.
     *
     * @param status  the health status (must not be null or blank)
     * @param message the health message (must not be null or blank)
     * @throws IllegalArgumentException if {@code status} or {@code message} is null or blank
     */
    public KernelHealth(String status, String message) {
        this(status, message, Instant.now(), Collections.emptyMap());
    }

    /**
     * Constructs a new {@code KernelHealth} with the given status, message, and timestamp.
     *
     * @param status    the health status (must not be null or blank)
     * @param message   the health message (must not be null or blank)
     * @param timestamp the instant when the health was checked (must not be null)
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public KernelHealth(String status, String message, Instant timestamp) {
        this(status, message, timestamp, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code KernelHealth} with the given status, message, timestamp, and details.
     *
     * @param status    the health status (must not be null or blank)
     * @param message   the health message (must not be null or blank)
     * @param timestamp the instant when the health was checked (must not be null)
     * @param details   optional details map (must not be null, may be empty)
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public KernelHealth(String status, String message, Instant timestamp, Map<String, Object> details) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("KernelHealth status must not be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("KernelHealth message must not be null or blank");
        }
        this.status = status;
        this.message = message;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.details = Collections.unmodifiableMap(Objects.requireNonNull(details, "Details must not be null"));
    }

    /**
     * Returns the health status.
     *
     * @return the health status
     */
    public String status() {
        return status;
    }

    /**
     * Returns the health message.
     *
     * @return the health message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the instant when the health was checked.
     *
     * @return the health timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns an unmodifiable map of optional health details.
     *
     * @return the details map (empty if no details provided)
     */
    public Map<String, Object> details() {
        return details;
    }

    /**
     * Compares this {@code KernelHealth} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code KernelHealth} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelHealth that = (KernelHealth) o;
        return status.equals(that.status)
                && message.equals(that.message)
                && timestamp.equals(that.timestamp)
                && details.equals(that.details);
    }

    /**
     * Returns the hash code for this {@code KernelHealth}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, message, timestamp, details);
    }

    /**
     * Returns a string representation of this {@code KernelHealth}.
     *
     * @return a string containing the status, message, and timestamp
     */
    @Override
    public String toString() {
        return "KernelHealth{"
                + "status='" + status + '\''
                + ", message='" + message + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}