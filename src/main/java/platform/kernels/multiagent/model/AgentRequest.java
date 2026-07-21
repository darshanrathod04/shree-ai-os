package platform.kernels.multiagent.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentRequest</b>
 *
 * <p>Represents requests for registration, discovery, lifecycle, and communication.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentRequest is immutable and contains no processing logic.</p>
 *
 * @param agentId      the agent identifier (must not be {@code null} or blank)
 * @param agentType    the agent type (must not be {@code null} or blank)
 * @param capabilities the list of capabilities (must not be {@code null})
 * @param metadata     additional request metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentRequest {
    private final String agentId;
    private final String agentType;
    private final List<AgentCapability> capabilities;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentRequest with the specified parameters.
     *
     * @param agentId      the agent identifier (must not be {@code null} or blank)
     * @param agentType    the agent type (must not be {@code null} or blank)
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param metadata     additional request metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if agentId or agentType is blank
     * @since 1.0
     */
    public AgentRequest(
            String agentId,
            String agentType,
            List<AgentCapability> capabilities,
            Map<String, Object> metadata) {
        this.agentId = validateAgentId(agentId);
        this.agentType = validateAgentType(agentType);
        this.capabilities = List.copyOf(Objects.requireNonNull(capabilities, "AgentRequest capabilities must not be null"));
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentRequest metadata must not be null"));
    }

    private static String validateAgentId(String agentId) {
        Objects.requireNonNull(agentId, "AgentRequest agentId must not be null");
        if (agentId.isBlank()) {
            throw new IllegalArgumentException("AgentRequest agentId must not be blank");
        }
        return agentId;
    }

    private static String validateAgentType(String agentType) {
        Objects.requireNonNull(agentType, "AgentRequest agentType must not be null");
        if (agentType.isBlank()) {
            throw new IllegalArgumentException("AgentRequest agentType must not be blank");
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
     * Returns the request metadata.
     *
     * @return an unmodifiable view of the request metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentRequests are equal if they have the same agentId, agentType, capabilities, and metadata.
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
        AgentRequest that = (AgentRequest) obj;
        return agentId.equals(that.agentId) &&
               agentType.equals(that.agentType) &&
               capabilities.equals(that.capabilities) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentRequest.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(agentId, agentType, capabilities, metadata);
    }

    /**
     * Returns a string representation of the AgentRequest.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentRequest{" +
                "agentId='" + agentId + '\'' +
                ", agentType='" + agentType + '\'' +
                ", capabilities=" + capabilities +
                ", metadata=" + metadata +
                '}';
    }
}