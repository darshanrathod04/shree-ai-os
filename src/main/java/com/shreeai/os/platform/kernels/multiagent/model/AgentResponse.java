package com.shreeai.os.platform.kernels.multiagent.model;

import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentResponse</b>
 *
 * <p>Represents immutable responses returned by the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentResponse is immutable and contains no mutable state or business logic.</p>
 *
 * @param success  whether the operation succeeded
 * @param message  the response message (must not be {@code null} or blank)
 * @param agentId  the agent identifier (may be {@code null})
 * @param metadata additional response metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentResponse {
    private final boolean success;
    private final String message;
    private final String agentId;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentResponse with the specified parameters.
     *
     * @param success  whether the operation succeeded
     * @param message  the response message (must not be {@code null} or blank)
     * @param agentId  the agent identifier (may be {@code null})
     * @param metadata additional response metadata (must not be {@code null})
     * @throws NullPointerException     if message or metadata is {@code null}
     * @throws IllegalArgumentException if message is blank
     * @since 1.0
     */
    public AgentResponse(boolean success, String message, String agentId, Map<String, Object> metadata) {
        this.success = success;
        this.message = validateMessage(message);
        this.agentId = agentId;
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentResponse metadata must not be null"));
    }

    private static String validateMessage(String message) {
        Objects.requireNonNull(message, "AgentResponse message must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("AgentResponse message must not be blank");
        }
        return message;
    }

    /**
     * Returns whether the operation succeeded.
     *
     * @return {@code true} if the operation succeeded
     * @since 1.0
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the response message.
     *
     * @return the response message
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
     * Returns the response metadata.
     *
     * @return an unmodifiable view of the response metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentResponses are equal if they have the same success, message, agentId, and metadata.
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
        AgentResponse that = (AgentResponse) obj;
        return success == that.success &&
               message.equals(that.message) &&
               Objects.equals(agentId, that.agentId) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentResponse.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(success, message, agentId, metadata);
    }

    /**
     * Returns a string representation of the AgentResponse.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}