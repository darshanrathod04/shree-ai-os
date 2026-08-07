package com.shreeai.os.platform.kernels.multiagent.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentDescriptor</b>
 *
 * <p>Represents metadata describing an agent.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentDescriptor is metadata only. It contains no execution logic, planning,
 * networking, memory access, orchestration, or runtime state.</p>
 *
 * <p>Architectural invariant: AgentDescriptor describes Agent Runtime, which executes Chief Kernel.</p>
 *
 * @param agentId      the agent identifier (must not be {@code null})
 * @param agentType    the agent type (must not be {@code null} or blank)
 * @param capabilities the list of capabilities (must not be {@code null})
 * @param priority     the agent priority (must not be {@code null} or blank)
 * @param tags         the list of tags (must not be {@code null})
 * @param metadata     additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentDescriptor {
    private final String agentId;
    private final String agentType;
    private final List<AgentCapability> capabilities;
    private final String priority;
    private final List<String> tags;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentDescriptor with the specified parameters.
     *
     * @param agentId      the agent identifier (must not be {@code null})
     * @param agentType    the agent type (must not be {@code null} or blank)
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param priority     the agent priority (must not be {@code null} or blank)
     * @param tags         the list of tags (must not be {@code null})
     * @param metadata     additional metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if agentType or priority is blank
     * @since 1.0
     */
    public AgentDescriptor(
            String agentId,
            String agentType,
            List<AgentCapability> capabilities,
            String priority,
            List<String> tags,
            Map<String, Object> metadata) {
        this.agentId = Objects.requireNonNull(agentId, "AgentDescriptor agentId must not be null");
        this.agentType = validateAgentType(agentType);
        this.capabilities = List.copyOf(Objects.requireNonNull(capabilities, "AgentDescriptor capabilities must not be null"));
        this.priority = validatePriority(priority);
        this.tags = List.copyOf(Objects.requireNonNull(tags, "AgentDescriptor tags must not be null"));
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentDescriptor metadata must not be null"));
    }

    private static String validateAgentType(String agentType) {
        Objects.requireNonNull(agentType, "AgentDescriptor agentType must not be null");
        if (agentType.isBlank()) {
            throw new IllegalArgumentException("AgentDescriptor agentType must not be blank");
        }
        return agentType;
    }

    private static String validatePriority(String priority) {
        Objects.requireNonNull(priority, "AgentDescriptor priority must not be null");
        if (priority.isBlank()) {
            throw new IllegalArgumentException("AgentDescriptor priority must not be blank");
        }
        return priority;
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
     * Returns the agent priority.
     *
     * @return the agent priority
     * @since 1.0
     */
    public String priority() {
        return priority;
    }

    /**
     * Returns the list of tags.
     *
     * @return an unmodifiable list of tags
     * @since 1.0
     */
    public List<String> tags() {
        return tags;
    }

    /**
     * Returns the metadata.
     *
     * @return an unmodifiable view of the metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentDescriptors are equal if they have the same agentId, agentType, capabilities, priority, tags, and metadata.
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
        AgentDescriptor that = (AgentDescriptor) obj;
        return agentId.equals(that.agentId) &&
               agentType.equals(that.agentType) &&
               capabilities.equals(that.capabilities) &&
               priority.equals(that.priority) &&
               tags.equals(that.tags) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentDescriptor.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(agentId, agentType, capabilities, priority, tags, metadata);
    }

    /**
     * Returns a string representation of the AgentDescriptor.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentDescriptor{" +
                "agentId='" + agentId + '\'' +
                ", agentType='" + agentType + '\'' +
                ", capabilities=" + capabilities +
                ", priority='" + priority + '\'' +
                ", tags=" + tags +
                ", metadata=" + metadata +
                '}';
    }
}