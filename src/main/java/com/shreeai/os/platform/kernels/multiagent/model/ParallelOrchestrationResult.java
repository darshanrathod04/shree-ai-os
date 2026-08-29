package com.shreeai.os.platform.kernels.multiagent.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ParallelOrchestrationResult</b> — Immutable aggregated result of a
 * parallel multi-agent orchestration.
 *
 * <p>Captures the collection of agent responses produced concurrently along
 * with aggregate success/failure metadata.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param objective      the orchestration objective
 * @param responses      the per-agent responses (must not be {@code null})
 * @param totalAgents    the total number of agents dispatched
 * @param succeededAgents the number of successful agents
 * @param failedAgents   the number of failed agents
 * @param startedAt      the orchestration start timestamp
 * @param completedAt    the orchestration completion timestamp
 * @param metadata       additional orchestration metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ParallelOrchestrationResult {

    private final String objective;
    private final List<AgentResponse> responses;
    private final int totalAgents;
    private final long succeededAgents;
    private final long failedAgents;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new {@code ParallelOrchestrationResult}.
     *
     * @param objective      the orchestration objective
     * @param responses      the per-agent responses
     * @param totalAgents    the total number of agents dispatched
     * @param succeededAgents the number of successful agents
     * @param failedAgents   the number of failed agents
     * @param startedAt      the start timestamp
     * @param completedAt    the completion timestamp
     * @param metadata       additional metadata
     */
    public ParallelOrchestrationResult(
            String objective,
            List<AgentResponse> responses,
            int totalAgents,
            long succeededAgents,
            long failedAgents,
            Instant startedAt,
            Instant completedAt,
            Map<String, Object> metadata) {
        this.objective = objective;
        this.responses = responses == null ? List.of() : List.copyOf(responses);
        this.totalAgents = totalAgents;
        this.succeededAgents = succeededAgents;
        this.failedAgents = failedAgents;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt must not be null");
        this.metadata = metadata == null ? Collections.emptyMap() : Map.copyOf(metadata);
    }

    public String objective() { return objective; }
    public List<AgentResponse> responses() { return responses; }
    public int totalAgents() { return totalAgents; }
    public long succeededAgents() { return succeededAgents; }
    public long failedAgents() { return failedAgents; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Map<String, Object> metadata() { return metadata; }

    /**
     * Returns whether every dispatched agent succeeded.
     *
     * @return {@code true} if there were no failures
     */
    public boolean allSucceeded() {
        return failedAgents == 0;
    }
}
