package com.shreeai.os.platform.kernels.multiagent.engine;

import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.model.*;

import java.time.Instant;
import java.util.*;

/**
 * Constitutional implementation of AgentOrchestrator.
 *
 * Uses the existing MultiAgentService only.
 * No direct agent-to-agent execution is allowed.
 */
public final class DefaultAgentOrchestrator implements AgentOrchestrator {

    private final MultiAgentService multiAgentService;

    public DefaultAgentOrchestrator(
            MultiAgentService multiAgentService
    ) {
        this.multiAgentService = Objects.requireNonNull(multiAgentService);
    }

    @Override
    public List<AgentResponse> orchestrate(
            String objective,
            Map<String, Object> context
    ) {

        AgentRequest discoveryRequest =
                new AgentRequest(
                        "chief",
                        "ORCHESTRATOR",
                        List.of(),
                        context == null ? Map.of() : Map.copyOf(context)
                );

        List<AgentDescriptor> agents =
                multiAgentService.discoverAgents(discoveryRequest);

        List<AgentResponse> responses = new ArrayList<>();

        for (AgentDescriptor agent : agents) {

            AgentCommunication communication =
                    new AgentCommunication(
                            UUID.randomUUID().toString(),
                            "chief",
                            agent.agentId(),
                            Instant.now(),
                            Map.of(
                                    "objective", objective,
                                    "context", context == null ? Map.of() : context
                            )
                    );

            AgentResponse response =
                    multiAgentService.communicate(communication);

            responses.add(response);
        }

        return List.copyOf(responses);
    }
}