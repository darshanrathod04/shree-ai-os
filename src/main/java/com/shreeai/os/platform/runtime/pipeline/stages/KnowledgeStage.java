package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeGroundingService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgePayload;
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
 * KnowledgeStage - Retrieves relevant knowledge for the current request.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Querying the knowledge graph for relevant information</li>
 *   <li>Retrieving domain-specific knowledge</li>
 *   <li>Ranking knowledge by relevance</li>
 *   <li>Injecting knowledge into context for reasoning</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class KnowledgeStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Knowledge")
            .priority(4)
            .enabled(true)
            .version("1.0")
            .description("Retrieves relevant knowledge for the current request")
            .build();

    private final KnowledgeQueryService knowledgeQueryService;
    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeRankingService knowledgeRankingService;
    private final KnowledgeGroundingService knowledgeGroundingService =
            new KnowledgeGroundingService();

    /**
     * Creates a new KnowledgeStage with real knowledge kernel services.
     *
     * @param knowledgeQueryService the knowledge query service
     * @param knowledgeSearchService the knowledge search service
     * @param knowledgeRankingService the knowledge ranking service
     */
    public KnowledgeStage(
            KnowledgeQueryService knowledgeQueryService,
            KnowledgeSearchService knowledgeSearchService,
            KnowledgeRankingService knowledgeRankingService) {
        this.knowledgeQueryService = knowledgeQueryService;
        this.knowledgeSearchService = knowledgeSearchService;
        this.knowledgeRankingService = knowledgeRankingService;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses null services (will fail gracefully).
     */
    public KnowledgeStage() {
        this(null, null, null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve memory information from previous stage
            String memoryId = (String) state.getMetadata().get("memoryId");
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

            // Check if knowledge services are available
            if (knowledgeQueryService == null || knowledgeSearchService == null || knowledgeRankingService == null) {
                // Fallback to simulated behavior if services not injected
                String knowledgeId = "kwn-" + requestId;
                state.addMetadata("knowledgeId", knowledgeId);
                state.addMetadata("knowledgeFound", false);
                state.addMetadata("knowledgeCount", 0);
                state.addMetadata("rankedKnowledge", List.of());
                state.addMetadata("knowledgeConfidence", 0.0);
                state.addMessage("Knowledge retrieval skipped: services not available");
                publishKnowledgeEvent(context, requestId, 0);
                return chain.next(context, state);
            }

            // Get the request text for knowledge search
            String requestText = "";

            Object value = context.getAttribute("requestMetadata");

            if (value instanceof Map<?, ?> requestMetadata) {

                Object keyword = requestMetadata.get("keyword");

                if (keyword != null && !keyword.toString().isBlank()) {
                    requestText = keyword.toString();
                }
            }

            if (requestText.isBlank()
                    && context.getExecutionRequest() != null) {

                requestText = context.getExecutionRequest().getUserInput();
            }

            // Search for relevant knowledge
            List<KnowledgeNode> allKnowledge = knowledgeSearchService.search(requestText);
            
            // Rank knowledge by relevance
            List<KnowledgeNode> rankedKnowledge = knowledgeRankingService.rankByRelevance(
                    requestText, 
                    allKnowledge, 
                    10 // Top 10 knowledge items
            );

            // Store knowledge information in state
            int knowledgeCount = rankedKnowledge.size();
            String knowledgeId = knowledgeCount > 0 ? rankedKnowledge.get(0).getId().value() : "none";
            double knowledgeConfidence = knowledgeCount > 0 ? extractConfidence(rankedKnowledge.get(0)) : 0.0;

            state.addMetadata("knowledgeId", knowledgeId);
            state.addMetadata("knowledgeFound", knowledgeCount > 0);
            state.addMetadata("knowledgeCount", knowledgeCount);
            state.addMetadata("rankedKnowledge", rankedKnowledge);
            state.addMetadata("knowledgeConfidence", knowledgeConfidence);

// EO-V1.2 Structured Knowledge Payload
            state.addMetadata("routedKernel", "Knowledge Kernel");
            state.addMetadata("knowledgeResults", rankedKnowledge);

// EO-V1.3 Grounding, Citations and Structured Payload
            KnowledgePayload knowledgePayload = knowledgeGroundingService.ground(
                    requestText != null ? requestText : "",
                    rankedKnowledge,
                    null);

            state.addMetadata("knowledgePayload", knowledgePayload);
            state.addMetadata("knowledgeCitations", knowledgePayload.getCitations());
            state.addMetadata("knowledgeGroundingScore", knowledgePayload.getGroundingScore());

            if (!rankedKnowledge.isEmpty()) {

                KnowledgeNode top = rankedKnowledge.getFirst();

                String title = top.getLabel();

                if (title == null || title.isBlank()) {
                    title = requestText;
                }

                state.addMetadata("knowledgeTitle", title);
                state.addMetadata("knowledgeSummary", top.getDescription());
                state.addMetadata("knowledgeMetadata", top.getMetadata());
            }

            state.addMessage("Knowledge retrieved: " + knowledgeCount + " items for memory " + memoryId);
            publishKnowledgeEvent(
                    context,
                    requestId,
                    knowledgeCount
            );

            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Knowledge retrieval failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("KNOWLEDGE_FAILED")
                    .addMessage("Knowledge stage failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Extracts confidence value from knowledge node metadata.
     *
     * @param node the knowledge node
     * @return confidence value (0.0-1.0)
     */
    private double extractConfidence(KnowledgeNode node) {
        if (node.getMetadata() != null && node.getMetadata().containsKey("confidence")) {
            Object value = node.getMetadata().get("confidence");
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        }
        return 0.0;
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private void publishKnowledgeEvent(
            PipelineContext context,
            String requestId,
            int knowledgeCount
    ) {

        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.KNOWLEDGE_COMPLETED,
                        requestId,
                        "Knowledge",
                        Instant.now(),
                        Map.of(
                                "knowledgeResults", knowledgeCount
                        )
                )
        );
    }
}