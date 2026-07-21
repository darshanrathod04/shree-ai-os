package platform.kernels.multiagent.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiAgentError</b>
 *
 * <p>Immutable value object representing a Multi-Agent Kernel error.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>MultiAgentError provides a consistent representation of failures across
 * registration, discovery, capabilities, lifecycle, communication, and validation.</p>
 *
 * @param errorCode    the error code (must not be {@code null})
 * @param message      the error message (must not be {@code null} or blank)
 * @param agentId      the agent identifier (may be {@code null})
 * @param timestamp    when the error occurred (must not be {@code null})
 * @param details      additional error details (must not be {@code null})
 *
 * @since 1.0
 */
public final class MultiAgentError {
    private final MultiAgentErrorCode errorCode;
    private final String message;
    private final String agentId;
    private final Instant timestamp;
    private final Map<String, Object> details;

    /**
     * Creates a new MultiAgentError with the specified parameters.
     *
     * @param errorCode the error code (must not be {@code null})
     * @param message   the error message (must not be {@code null} or blank)
     * @param agentId   the agent identifier (may be {@code null})
     * @param timestamp when the error occurred (must not be {@code null})
     * @param details   additional error details (must not be {@code null})
     * @throws NullPointerException     if errorCode, message, timestamp, or details is {@code null}
     * @throws IllegalArgumentException if message is blank
     * @since 1.0
     */
    public MultiAgentError(
            MultiAgentErrorCode errorCode,
            String message,
            String agentId,
            Instant timestamp,
            Map<String, Object> details) {
        this.errorCode = Objects.requireNonNull(errorCode, "MultiAgentError errorCode must not be null");
        this.message = validateMessage(message);
        this.agentId = agentId;
        this.timestamp = Objects.requireNonNull(timestamp, "MultiAgentError timestamp must not be null");
        this.details = Map.copyOf(Objects.requireNonNull(details, "MultiAgentError details must not be null"));
    }

    private static String validateMessage(String message) {
        Objects.requireNonNull(message, "MultiAgentError message must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("MultiAgentError message must not be blank");
        }
        return message;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     * @since 1.0
     */
    public MultiAgentErrorCode errorCode() {
        return errorCode;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     * @since 1.0
     */
    public String message() {
        return message;
    }

    /**
     * Returns the agent identifier.
     *
     * @return the agent identifier, or {@code null} if not present
     * @since 1.0
     */
    public String agentId() {
        return agentId;
    }

    /**
     * Returns when the error occurred.
     *
     * @return the error timestamp
     * @since 1.0
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the error details.
     *
     * @return an unmodifiable view of the error details
     * @since 1.0
     */
    public Map<String, Object> details() {
        return details;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MultiAgentErrors are equal if they have the same errorCode, message, agentId, timestamp, and details.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MultiAgentError that = (MultiAgentError) obj;
        return errorCode == that.errorCode &&
               message.equals(that.message) &&
               Objects.equals(agentId, that.agentId) &&
               timestamp.equals(that.timestamp) &&
               details.equals(that.details);
    }

    /**
     * Returns a hash code value for the MultiAgentError.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message, agentId, timestamp, details);
    }

    /**
     * Returns a string representation of the MultiAgentError.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "MultiAgentError{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", agentId='" + agentId + '\'' +
                ", timestamp=" + timestamp +
                ", details=" + details +
                '}';
    }
}