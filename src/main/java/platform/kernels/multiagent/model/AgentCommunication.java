package platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentCommunication</b>
 *
 * <p>Represents communication metadata between agents.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentCommunication is metadata only. It contains no transport, networking, or routing logic.
 * Communication is always Chief-mediated.</p>
 *
 * @param correlationId the correlation identifier (must not be {@code null} or blank)
 * @param senderId      the sender agent identifier (must not be {@code null} or blank)
 * @param receiverId    the receiver agent identifier (must not be {@code null} or blank)
 * @param timestamp     when the communication was created (must not be {@code null})
 * @param metadata      additional communication metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentCommunication {
    private final String correlationId;
    private final String senderId;
    private final String receiverId;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentCommunication with the specified parameters.
     *
     * @param correlationId the correlation identifier (must not be {@code null} or blank)
     * @param senderId      the sender agent identifier (must not be {@code null} or blank)
     * @param receiverId    the receiver agent identifier (must not be {@code null} or blank)
     * @param timestamp     when the communication was created (must not be {@code null})
     * @param metadata      additional communication metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if correlationId, senderId, or receiverId is blank
     * @since 1.0
     */
    public AgentCommunication(
            String correlationId,
            String senderId,
            String receiverId,
            Instant timestamp,
            Map<String, Object> metadata) {
        this.correlationId = validateCorrelationId(correlationId);
        this.senderId = validateSenderId(senderId);
        this.receiverId = validateReceiverId(receiverId);
        this.timestamp = Objects.requireNonNull(timestamp, "AgentCommunication timestamp must not be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentCommunication metadata must not be null"));
    }

    private static String validateCorrelationId(String correlationId) {
        Objects.requireNonNull(correlationId, "AgentCommunication correlationId must not be null");
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("AgentCommunication correlationId must not be blank");
        }
        return correlationId;
    }

    private static String validateSenderId(String senderId) {
        Objects.requireNonNull(senderId, "AgentCommunication senderId must not be null");
        if (senderId.isBlank()) {
            throw new IllegalArgumentException("AgentCommunication senderId must not be blank");
        }
        return senderId;
    }

    private static String validateReceiverId(String receiverId) {
        Objects.requireNonNull(receiverId, "AgentCommunication receiverId must not be null");
        if (receiverId.isBlank()) {
            throw new IllegalArgumentException("AgentCommunication receiverId must not be blank");
        }
        return receiverId;
    }

    /**
     * Returns the correlation identifier.
     *
     * @return the correlation identifier
     * @since 1.0
     */
    public String correlationId() {
        return correlationId;
    }

    /**
     * Returns the sender agent identifier.
     *
     * @return the sender agent identifier
     * @since 1.0
     */
    public String senderId() {
        return senderId;
    }

    /**
     * Returns the receiver agent identifier.
     *
     * @return the receiver agent identifier
     * @since 1.0
     */
    public String receiverId() {
        return receiverId;
    }

    /**
     * Returns when the communication was created.
     *
     * @return the communication timestamp
     * @since 1.0
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the communication metadata.
     *
     * @return an unmodifiable view of the communication metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentCommunications are equal if they have the same correlationId, senderId, receiverId, timestamp, and metadata.
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
        AgentCommunication that = (AgentCommunication) obj;
        return correlationId.equals(that.correlationId) &&
               senderId.equals(that.senderId) &&
               receiverId.equals(that.receiverId) &&
               timestamp.equals(that.timestamp) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentCommunication.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(correlationId, senderId, receiverId, timestamp, metadata);
    }

    /**
     * Returns a string representation of the AgentCommunication.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentCommunication{" +
                "correlationId='" + correlationId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", timestamp=" + timestamp +
                ", metadata=" + metadata +
                '}';
    }
}