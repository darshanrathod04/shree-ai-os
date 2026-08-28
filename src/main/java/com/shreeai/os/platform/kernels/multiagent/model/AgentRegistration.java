package com.shreeai.os.platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentRegistration</b>
 *
 * <p>Represents the registration state of an agent.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentRegistration is metadata only. It contains no registration logic.</p>
 *
 * @param agentId      the agent identifier (must not be {@code null})
 * @param agentType    the agent type (must not be {@code null} or blank)
 * @param capabilities the list of capabilities (must not be {@code null})
 * @param registeredAt when the agent was registered (must not be {@code null})
 * @param metadata     additional registration metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentRegistration {
    private final String agentId;
    private final String agentType;
    private final List<AgentCapability> capabilities;
    private final Instant registeredAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentRegistration with the specified parameters.
     *
     * @param agentId      the agent identifier (must not be {@code null})
     * @param agentType    the agent type (must not be {@code null} or blank)
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param registeredAt when the agent was registered (must not be {@code null})
     * @param metadata     additional registration metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if agentType is blank
     * @since 1.0
     */
    public AgentRegistration(
            String agentId,
            String agentType,
            List<AgentCapability> capabilities,
            Instant registeredAt,
            Map<String, Object> metadata) {
        this.agentId = Objects.requireNonNull(agentId, "AgentRegistration agentId must not be null");
        this.agentType = validateAgentType(agentType);
        this.capabilities = List.copyOf(Objects.requireNonNull(capabilities, "AgentRegistration capabilities must not be null"));
        this.registeredAt = Objects.requireNonNull(registeredAt, "AgentRegistration registeredAt must not be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentRegistration metadata must not be null"));
    }

    private static String validateAgentType(String agentType) {
        Objects.requireNonNull(agentType, "AgentRegistration agentType must not be null");
        if (agentType.isBlank()) {
            throw new IllegalArgumentException("AgentRegistration agentType must not be blank");
        }
        return agentType;
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
     * Returns the agent type.
     *
     * @return the agent type
     * @since 1.0
     */
    public String agentType() {
        return agentType;
    }

    /**
     * Returns the list of capabilities.
     *
     * @return an unmodifiable list of capabilities
     * @since 1.0
     */
    public List<AgentCapability> capabilities() {
        return capabilities;
    }

    /**
     * Returns when the agent was registered.
     *
     * @return the registration timestamp
     * @since 1.0
     */
    public Instant registeredAt() {
        return registeredAt;
    }

    /**
     * Returns the registration metadata.
     *
     * @return an unmodifiable view of the registration metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentRegistrations are equal if they have the same agentId, agentType, capabilities, registeredAt, and metadata.
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
        AgentRegistration that = (AgentRegistration) obj;
        return agentId.equals(that.agentId) &&
               agentType.equals(that.agentType) &&
               capabilities.equals(that.capabilities) &&
               registeredAt.equals(that.registeredAt) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentRegistration.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(agentId, agentType, capabilities, registeredAt, metadata);
    }

    /**
     * Returns a string representation of the AgentRegistration.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentRegistration{" +
                "agentId='" + agentId + '\'' +
                ", agentType='" + agentType + '\'' +
                ", capabilities=" + capabilities +
                ", registeredAt=" + registeredAt +
                ", metadata=" + metadata +
                '}';
    }
}