package com.shreeai.os.platform.kernels.multiagent.engine;

import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.model.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        Map<String, Object> metadata = copyMetadata(context);

        List<AgentDescriptor> agents =
                discoverAgents(metadata);

        AgentContext sharedContext =
                createSharedContext(objective, metadata);

        List<AgentResponse> responses = new ArrayList<>();

        for (AgentDescriptor agent : agents) {
            responses.add(
                    multiAgentService.communicate(
                            buildCommunication(agent, objective, sharedContext, metadata)
                    )
            );
        }

        return List.copyOf(responses);
    }

    @Override
    public ParallelOrchestrationResult parallelOrchestrate(
            String objective,
            Map<String, Object> context,
            ParallelExecutionPolicy policy
    ) {
        Objects.requireNonNull(policy, "ParallelExecutionPolicy must not be null");

        Map<String, Object> metadata = copyMetadata(context);

        List<AgentDescriptor> agents =
                discoverAgents(metadata);

        AgentContext sharedContext =
                createSharedContext(objective, metadata);

        Instant startedAt = Instant.now();

        ExecutorService executor =
                policy.isUnlimited()
                        ? Executors.newCachedThreadPool()
                        : Executors.newFixedThreadPool(policy.maxConcurrency());

        try {
            List<CompletableFuture<AgentResponse>> futures =
                    new java.util.concurrent.CopyOnWriteArrayList<>();
            java.util.concurrent.atomic.AtomicBoolean anyFailed =
                    new java.util.concurrent.atomic.AtomicBoolean(false);

            for (AgentDescriptor agent : agents) {
                CompletableFuture<AgentResponse> future =
                        CompletableFuture.supplyAsync(() ->
                                multiAgentService.communicate(
                                        buildCommunication(agent, objective, sharedContext, metadata)
                                ), executor)
                                .orTimeout(policy.timeoutMs(), TimeUnit.MILLISECONDS)
                                .exceptionally(ex -> failedResponse(agent, ex));

                if (policy.failFast()) {
                    future.whenComplete((res, ex) -> {
                        if (ex != null || (res != null && !res.success())) {
                            if (anyFailed.compareAndSet(false, true)) {
                                for (CompletableFuture<AgentResponse> f : futures) {
                                    f.cancel(true);
                                }
                            }
                        }
                    });
                }

                futures.add(future);
            }

            List<AgentResponse> responses = new ArrayList<>();
            for (CompletableFuture<AgentResponse> future : futures) {
                try {
                    responses.add(future.join());
                } catch (Exception ex) {
                    responses.add(failedResponse(null, ex));
                }
            }

            long succeeded = responses.stream()
                    .filter(r -> r != null && r.success())
                    .count();
            long failed = responses.size() - succeeded;

            return new ParallelOrchestrationResult(
                    objective,
                    responses,
                    agents.size(),
                    succeeded,
                    failed,
                    startedAt,
                    Instant.now(),
                    Map.of(
                            "policy", policy,
                            "dispatched", agents.size(),
                            "failFast", policy.failFast()
                    )
            );
        } finally {
            executor.shutdownNow();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Builds a Chief-mediated communication for a single agent.
     */
    private AgentCommunication buildCommunication(
            AgentDescriptor agent,
            String objective,
            AgentContext sharedContext,
            Map<String, Object> metadata) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("objective", objective);
        payload.put("agentContext", sharedContext);
        payload.putAll(metadata);

        return new AgentCommunication(
                UUID.randomUUID().toString(),
                "chief",
                agent.agentId(),
                Instant.now(),
                payload
        );
    }

    /**
     * Builds a failed {@link AgentResponse} for a dispatched agent that
     * threw or timed out.
     */
    private AgentResponse failedResponse(AgentDescriptor agent, Throwable ex) {
        String errorMsg = ex == null ? "timeout" : (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        return new AgentResponse(
                false,
                "Agent execution failed or timed out: " + errorMsg,
                agent == null ? "unknown" : agent.agentId(),
                Map.of("error", errorMsg)
        );
    }

    /**
     * Discovers the agents available for orchestration.
     */
    private List<AgentDescriptor> discoverAgents(Map<String, Object> metadata) {
        AgentRequest discoveryRequest =
                new AgentRequest(
                        "chief",
                        "ORCHESTRATOR",
                        List.of(),
                        metadata
                );
        return multiAgentService.discoverAgents(discoveryRequest);
    }

    private Map<String, Object> copyMetadata(Map<String, Object> context) {
        return context == null ? Map.of() : Map.copyOf(context);
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