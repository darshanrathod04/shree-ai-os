package com.shreeai.os.platform.kernels.multiagent.engine;

import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.model.*;

import java.time.Instant;
import java.util.*;

/**
 * Constitutional implementation of AgentOrchestrator.
 *
 * Responsibilities:
 * - Discover suitable agents
 * - Create one shared intelligence context
 * - Delegate through MultiAgentService only
 * - Never communicate directly with agents
 */
public final class DefaultAgentOrchestrator
        implements AgentOrchestrator {

    private final MultiAgentService multiAgentService;

    public DefaultAgentOrchestrator(
            MultiAgentService multiAgentService
    ) {
        this.multiAgentService =
                Objects.requireNonNull(multiAgentService);
    }

    @Override
    public List<AgentResponse> orchestrate(
            String objective,
            Map<String, Object> context
    ) {

        Map<String, Object> metadata =
                context == null
                        ? Map.of()
                        : Map.copyOf(context);

        AgentRequest discoveryRequest =
                new AgentRequest(
                        "chief",
                        "ORCHESTRATOR",
                        List.of(),
                        metadata
                );

        List<AgentDescriptor> agents =
                multiAgentService.discoverAgents(discoveryRequest);

        AgentContext sharedContext =
                createSharedContext(objective, metadata);

        List<AgentResponse> responses = new ArrayList<>();

        for (AgentDescriptor agent : agents) {

            Map<String, Object> payload = new HashMap<>();

            payload.put("objective", objective);
            payload.put("agentContext", sharedContext);
            payload.putAll(metadata);

            AgentCommunication communication =
                    new AgentCommunication(
                            UUID.randomUUID().toString(),
                            "chief",
                            agent.agentId(),
                            Instant.now(),
                            payload
                    );

            AgentResponse response =
                    multiAgentService.communicate(communication);

            responses.add(response);
        }

        return List.copyOf(responses);
    }

    /**
     * Creates the shared intelligence package.
     */
    private AgentContext createSharedContext(
            String objective,
            Map<String, Object> metadata
    ) {

        List<String> memory =
                asStringList(metadata.get("memory"));

        List<String> knowledge =
                asStringList(metadata.get("knowledge"));

        return new AgentContext(
                objective,
                memory,
                knowledge,
                metadata
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object value) {

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .toList();
        }

        return List.of();
    }
}