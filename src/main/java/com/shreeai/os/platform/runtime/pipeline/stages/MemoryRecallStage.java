package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;
import com.shreeai.os.platform.kernels.memory.api.MemoryQueryService;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;

import java.time.Instant;
import java.util.Map;

import java.util.List;

/**
 * MemoryRecallStage - Recalls relevant memories for the current request.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Querying memory for relevant past interactions</li>
 *   <li>Retrieving semantic and episodic memories</li>
 *   <li>Ranking memories by relevance</li>
 *   <li>Injecting recalled memories into context</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class MemoryRecallStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("MemoryRecall")
            .priority(3)
            .enabled(true)
            .version("1.0")
            .description("Recalls relevant memories for the current request")
            .build();

    private final MemoryQueryService memoryQueryService;
    private final MemorySearchService memorySearchService;
    private final MemoryRankingService memoryRankingService;

    /**
     * Creates a new MemoryRecallStage with real memory kernel services.
     *
     * @param memoryQueryService   the memory query service
     * @param memorySearchService  the memory search service
     * @param memoryRankingService the memory ranking service
     */
    public MemoryRecallStage(
            MemoryQueryService memoryQueryService,
            MemorySearchService memorySearchService,
            MemoryRankingService memoryRankingService) {
        this.memoryQueryService = memoryQueryService;
        this.memorySearchService = memorySearchService;
        this.memoryRankingService = memoryRankingService;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses null services (will fail gracefully).
     */
    public MemoryRecallStage() {
        this(null, null, null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve context information from previous stage
            String contextId = (String) state.getMetadata().get("contextId");
            String requestId = context.getExecutionRequest() != null
                    ? context.getExecutionRequest().getRequestId()
                    : "unknown";

            // Check if memory services are available
            if (memoryQueryService == null || memorySearchService == null || memoryRankingService == null) {
                // Fallback to simulated behavior if services not injected
                state.addMetadata("memoryId", "mem-" + requestId);
                state.addMetadata("memoriesRecalled", 0);
                state.addMetadata("memoryRecalled", false);
                state.addMessage("Memory recall skipped: services not available");
                publishMemoryEvent(context, requestId, 0);
                return chain.next(context, state);
            }

            // Get the request text for memory search
            // Sprint-9: use getUserInput() (raw user payload) instead of
            // toString() — the previous .toString() call returned an
            // SDKRequest toString representation (e.g. "SDKRequest@abc123"),
            // which never matched any real memory and produced HTTP 500s
            // when downstream processors tried to interpret it.
            String requestText = context.getExecutionRequest() != null
                    && context.getExecutionRequest().getUserInput() != null
                    ? context.getExecutionRequest().getUserInput()
                    : "";

            // Sprint-9: Normalize the query so natural-language inputs
            // (e.g. "who is darshan") reach the memory store as the
            // canonical entity ("darshan") and can match stored memories.
            String normalizedQuery = QueryNormalizer.normalize(requestText);

            // Search for relevant memories
            List<Memory> allMemories = memorySearchService.search(normalizedQuery);

            // Rank memories by relevance
            List<Memory> rankedMemories = memoryRankingService.rankByRelevance(
                    normalizedQuery,
                    allMemories,
                    10 // Top 10 memories
            );

            // Store memory information in state
            int memoriesRecalled = rankedMemories.size();
            String memoryId = memoriesRecalled > 0
                    ? rankedMemories.get(0).metadata().memoryId().value()
                    : "none";

            state.addMetadata("memoryId", memoryId);
            state.addMetadata("memoriesRecalled", memoriesRecalled);
            state.addMetadata("memoryRecalled", memoriesRecalled > 0);
            state.addMetadata("rankedMemories", rankedMemories);
            state.addMessage("Memory recalled: " + memoriesRecalled + " memories for context " + contextId);

// Publish SDK runtime event
            publishMemoryEvent(
                    context,
                    requestId,
                    memoriesRecalled
            );

// Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Memory recall failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("MEMORY_RECALL_FAILED")
                    .addMessage("Memory recall stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private void publishMemoryEvent(
            PipelineContext context,
            String requestId,
            int memoriesFound
    ) {
        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.MEMORY_RECALL_COMPLETED,
                        requestId,
                        "MemoryRecall",
                        Instant.now(),
                        Map.of(
                                "memoriesFound", memoriesFound
                        )
                )
        );
    }
}