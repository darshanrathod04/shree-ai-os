package com.shreeai.os.platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentStatus</b>
 *
 * <p>Represents the lifecycle state of an agent.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentStatus is metadata only. It contains no lifecycle transition logic.</p>
 *
 * <p>Common states include: REGISTERED, STARTING, RUNNING, PAUSED, STOPPED, UNREGISTERED</p>
 *
 * @param agentId   the agent identifier (must not be {@code null})
 * @param state     the agent state (must not be {@code null} or blank)
 * @param updatedAt when the status was updated (must not be {@code null})
 * @param metadata  additional status metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentStatus {
    private final String agentId;
    private final String state;
    private final Instant updatedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentStatus with the specified parameters.
     *
     * @param agentId   the agent identifier (must not be {@code null})
     * @param state     the agent state (must not be {@code null} or blank)
     * @param updatedAt when the status was updated (must not be {@code null})
     * @param metadata  additional status metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if state is blank
     * @since 1.0
     */
    public AgentStatus(String agentId, String state, Instant updatedAt, Map<String, Object> metadata) {
        this.agentId = Objects.requireNonNull(agentId, "AgentStatus agentId must not be null");
        this.state = validateState(state);
        this.updatedAt = Objects.requireNonNull(updatedAt, "AgentStatus updatedAt must not be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentStatus metadata must not be null"));
    }

    private static String validateState(String state) {
        Objects.requireNonNull(state, "AgentStatus state must not be null");
        if (state.isBlank()) {
            throw new IllegalArgumentException("AgentStatus state must not be blank");
        }
        return state;
    }

    /**
     * Returns the agent identifier.
     *
     * @return the agent identifier
     * @since 1.0
     */
    public String agentId() {
        return agentId;
    }

    /**
     * Returns the agent state.
     *
     * @return the agent state
     * @since 1.0
     */
    public String state() {
        return state;
    }

    /**
     * Returns when the status was updated.
     *
     * @return the update timestamp
     * @since 1.0
     */
    public Instant updatedAt() {
        return updatedAt;
    }

    /**
     * Returns the status metadata.
     *
     * @return an unmodifiable view of the status metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentStatuses are equal if they have the same agentId, state, updatedAt, and metadata.
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
        AgentStatus that = (AgentStatus) obj;
        return agentId.equals(that.agentId) &&
               state.equals(that.state) &&
               updatedAt.equals(that.updatedAt) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentStatus.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(agentId, state, updatedAt, metadata);
    }

    /**
     * Returns a string representation of the AgentStatus.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentStatus{" +
                "agentId='" + agentId + '\'' +
                ", state='" + state + '\'' +
                ", updatedAt=" + updatedAt +
                ", metadata=" + metadata +
                '}';
    }
}