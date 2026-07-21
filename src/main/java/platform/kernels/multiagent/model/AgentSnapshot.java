package platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentSnapshot</b>
 *
 * <p>Immutable snapshot of one agent at a point in time.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentSnapshot represents state at one point in time. It contains no behavior.</p>
 *
 * @param agentId      the agent identifier (must not be {@code null})
 * @param agentType    the agent type (must not be {@code null} or blank)
 * @param status       the agent status (must not be {@code null})
 * @param capabilities the list of capabilities (must not be {@code null})
 * @param priority     the agent priority (must not be {@code null} or blank)
 * @param tags         the list of tags (must not be {@code null})
 * @param metadata     additional metadata (must not be {@code null})
 * @param capturedAt   when the snapshot was captured (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentSnapshot {
    private final String agentId;
    private final String agentType;
    private final AgentStatus status;
    private final List<AgentCapability> capabilities;
    private final String priority;
    private final List<String> tags;
    private final Map<String, Object> metadata;
    private final Instant capturedAt;

    /**
     * Creates a new AgentSnapshot with the specified parameters.
     *
     * @param agentId      the agent identifier (must not be {@code null})
     * @param agentType    the agent type (must not be {@code null} or blank)
     * @param status       the agent status (must not be {@code null})
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param priority     the agent priority (must not be {@code null} or blank)
     * @param tags         the list of tags (must not be {@code null})
     * @param metadata     additional metadata (must not be {@code null})
     * @param capturedAt   when the snapshot was captured (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if agentType or priority is blank
     * @since 1.0
     */
    public AgentSnapshot(
            String agentId,
            String agentType,
            AgentStatus status,
            List<AgentCapability> capabilities,
            String priority,
            List<String> tags,
            Map<String, Object> metadata,
            Instant capturedAt) {
        this.agentId = Objects.requireNonNull(agentId, "AgentSnapshot agentId must not be null");
        this.agentType = validateAgentType(agentType);
        this.status = Objects.requireNonNull(status, "AgentSnapshot status must not be null");
        this.capabilities = List.copyOf(Objects.requireNonNull(capabilities, "AgentSnapshot capabilities must not be null"));
        this.priority = validatePriority(priority);
        this.tags = List.copyOf(Objects.requireNonNull(tags, "AgentSnapshot tags must not be null"));
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentSnapshot metadata must not be null"));
        this.capturedAt = Objects.requireNonNull(capturedAt, "AgentSnapshot capturedAt must not be null");
    }

    private static String validateAgentType(String agentType) {
        Objects.requireNonNull(agentType, "AgentSnapshot agentType must not be null");
        if (agentType.isBlank()) {
            throw new IllegalArgumentException("AgentSnapshot agentType must not be blank");
        }
        return agentType;
    }

    private static String validatePriority(String priority) {
        Objects.requireNonNull(priority, "AgentSnapshot priority must not be null");
        if (priority.isBlank()) {
            throw new IllegalArgumentException("AgentSnapshot priority must not be blank");
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
     * Returns the agent status.
     *
     * @return the agent status
     * @since 1.0
     */
    public AgentStatus status() {
        return status;
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
     * Returns when the snapshot was captured.
     *
     * @return the capture timestamp
     * @since 1.0
     */
    public Instant capturedAt() {
        return capturedAt;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentSnapshots are equal if they have the same agentId, agentType, status, capabilities, priority, tags, metadata, and capturedAt.
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
        AgentSnapshot that = (AgentSnapshot) obj;
        return agentId.equals(that.agentId) &&
               agentType.equals(that.agentType) &&
               status.equals(that.status) &&
               capabilities.equals(that.capabilities) &&
               priority.equals(that.priority) &&
               tags.equals(that.tags) &&
               metadata.equals(that.metadata) &&
               capturedAt.equals(that.capturedAt);
    }

    /**
     * Returns a hash code value for the AgentSnapshot.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(agentId, agentType, status, capabilities, priority, tags, metadata, capturedAt);
    }

    /**
     * Returns a string representation of the AgentSnapshot.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentSnapshot{" +
                "agentId='" + agentId + '\'' +
                ", agentType='" + agentType + '\'' +
                ", status=" + status +
                ", capabilities=" + capabilities +
                ", priority='" + priority + '\'' +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", capturedAt=" + capturedAt +
                '}';
    }
}