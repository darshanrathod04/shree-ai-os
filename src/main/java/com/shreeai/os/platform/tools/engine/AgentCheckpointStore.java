package com.shreeai.os.platform.tools.engine;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>AgentCheckpointStore</b>
 *
 * <p>LangGraph / OpenAI Agents SDK style stateful checkpoint store for
 * {@link ToolCallingAgent}. Captures execution transcripts, pending
 * tool invocations, and step history so human-in-the-loop (HITL)
 * approvals can pause and resume without losing state.</p>
 */
public final class AgentCheckpointStore {

    public enum Status { PAUSED_APPROVAL, COMPLETED, REJECTED }

    private final Map<String, AgentCheckpoint> store = new ConcurrentHashMap<>();

    public AgentCheckpoint save(
            String model,
            String transcript,
            String pendingToolId,
            Map<String, Object> pendingArguments,
            List<ToolCallingAgent.ToolStep> steps,
            int nextStep,
            int maxSteps,
            Status status) {
        String id = "chk-" + UUID.randomUUID().toString().substring(0, 12);
        AgentCheckpoint cp = new AgentCheckpoint(
                id,
                Objects.requireNonNull(model),
                Objects.requireNonNull(transcript),
                pendingToolId,
                pendingArguments == null ? Map.of() : Map.copyOf(pendingArguments),
                List.copyOf(steps),
                nextStep,
                maxSteps,
                status,
                Instant.now()
        );
        store.put(id, cp);
        return cp;
    }

    public Optional<AgentCheckpoint> find(String checkpointId) {
        return Optional.ofNullable(store.get(checkpointId));
    }

    public void updateStatus(String checkpointId, Status newStatus) {
        store.computeIfPresent(checkpointId, (k, cp) -> new AgentCheckpoint(
                cp.checkpointId(),
                cp.model(),
                cp.transcript(),
                cp.pendingToolId(),
                cp.pendingArguments(),
                cp.steps(),
                cp.nextStep(),
                cp.maxSteps(),
                newStatus,
                Instant.now()
        ));
    }

    public record AgentCheckpoint(
            String checkpointId,
            String model,
            String transcript,
            String pendingToolId,
            Map<String, Object> pendingArguments,
            List<ToolCallingAgent.ToolStep> steps,
            int nextStep,
            int maxSteps,
            Status status,
            Instant updatedAt
    ) {}
}
