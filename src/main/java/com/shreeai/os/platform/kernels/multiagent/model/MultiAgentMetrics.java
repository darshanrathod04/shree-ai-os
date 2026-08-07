package com.shreeai.os.platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiAgentMetrics</b>
 *
 * <p>Represents runtime metrics for the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>MultiAgentMetrics is metadata only. It contains no calculations or monitoring logic.</p>
 *
 * <p>Examples include: total registrations, active agents, communication count</p>
 *
 * @param totalRegistrations  the total number of registrations
 * @param activeAgents        the number of active agents
 * @param communicationCount  the total number of communications
 * @param measuredAt          when the metrics were measured (must not be {@code null})
 * @param metadata            additional metrics metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class MultiAgentMetrics {
    private final int totalRegistrations;
    private final int activeAgents;
    private final int communicationCount;
    private final Instant measuredAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new MultiAgentMetrics with the specified parameters.
     *
     * @param totalRegistrations  the total number of registrations
     * @param activeAgents        the number of active agents
     * @param communicationCount  the total number of communications
     * @param measuredAt          when the metrics were measured (must not be {@code null})
     * @param metadata            additional metrics metadata (must not be {@code null})
     * @throws NullPointerException if measuredAt or metadata is {@code null}
     * @since 1.0
     */
    public MultiAgentMetrics(
            int totalRegistrations,
            int activeAgents,
            int communicationCount,
            Instant measuredAt,
            Map<String, Object> metadata) {
        this.totalRegistrations = totalRegistrations;
        this.activeAgents = activeAgents;
        this.communicationCount = communicationCount;
        this.measuredAt = Objects.requireNonNull(measuredAt, "MultiAgentMetrics measuredAt must not be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "MultiAgentMetrics metadata must not be null"));
    }

    /**
     * Returns the total number of registrations.
     *
     * @return the total registrations
     * @since 1.0
     */
    public int totalRegistrations() {
        return totalRegistrations;
    }

    /**
     * Returns the number of active agents.
     *
     * @return the active agents count
     * @since 1.0
     */
    public int activeAgents() {
        return activeAgents;
    }

    /**
     * Returns the total number of communications.
     *
     * @return the communication count
     * @since 1.0
     */
    public int communicationCount() {
        return communicationCount;
    }

    /**
     * Returns when the metrics were measured.
     *
     * @return the measurement timestamp
     * @since 1.0
     */
    public Instant measuredAt() {
        return measuredAt;
    }

    /**
     * Returns the metrics metadata.
     *
     * @return an unmodifiable view of the metrics metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MultiAgentMetrics are equal if they have the same totalRegistrations, activeAgents, communicationCount, measuredAt, and metadata.
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
        MultiAgentMetrics that = (MultiAgentMetrics) obj;
        return totalRegistrations == that.totalRegistrations &&
               activeAgents == that.activeAgents &&
               communicationCount == that.communicationCount &&
               measuredAt.equals(that.measuredAt) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the MultiAgentMetrics.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(totalRegistrations, activeAgents, communicationCount, measuredAt, metadata);
    }

    /**
     * Returns a string representation of the MultiAgentMetrics.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "MultiAgentMetrics{" +
                "totalRegistrations=" + totalRegistrations +
                ", activeAgents=" + activeAgents +
                ", communicationCount=" + communicationCount +
                ", measuredAt=" + measuredAt +
                ", metadata=" + metadata +
                '}';
    }
}