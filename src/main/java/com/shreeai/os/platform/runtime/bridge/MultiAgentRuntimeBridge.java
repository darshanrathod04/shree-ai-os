package com.shreeai.os.platform.runtime.bridge;

import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Runtime Bridge between Chief Kernel and Multi-Agent Kernel.
 *
 * Constitutional Rules:
 * - Runtime never talks to agents directly.
 * - All communication flows through MultiAgentService.
 * - Every communication receives a unique correlationId.
 */
public final class MultiAgentRuntimeBridge {

    private final MultiAgentService multiAgentService;

    public MultiAgentRuntimeBridge(
            MultiAgentService multiAgentService
    ) {
        this.multiAgentService = Objects.requireNonNull(multiAgentService);
    }

    /**
     * Delegate work through the constitutional communication channel.
     */
    public AgentResponse delegate(
            String senderAgent,
            String receiverAgent,
            String message,
            PipelineExecutionState state
    ) {

        Map<String, Object> metadata = Map.copyOf(state.getMetadata());

        AgentCommunication communication =
                new AgentCommunication(
                        UUID.randomUUID().toString(), // correlationId
                        senderAgent,
                        receiverAgent,
                        Instant.now(),
                        metadata
                );

        return multiAgentService.communicate(communication);
    }
}