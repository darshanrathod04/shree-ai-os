package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

import java.time.Instant;
import java.util.Map;

/**
 * MemoryStoreStage - Stores execution results in memory.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Storing execution results in memory</li>
 *   <li>Updating episodic and semantic memory</li>
 *   <li>Persisting interaction history</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class MemoryStoreStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("MemoryStore")
            .priority(10)
            .enabled(true)
            .version("1.0")
            .description("Stores execution results in memory")
            .build();

    private final MemoryService memoryService;

    /**
     * Creates a new MemoryStoreStage with real memory kernel service.
     *
     * @param memoryService the memory service
     */
    public MemoryStoreStage(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses null service (will fail gracefully).
     */
    public MemoryStoreStage() {
        this(null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve execution information from previous stage
            String executionId = (String) state.getMetadata().get("executionId");
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

            // Check if memory service is available
            if (memoryService == null) {
                // Fallback to simulated behavior if service not injected
                state.addMetadata("storedMemoryId", "stored-mem-" + requestId);
                state.addMetadata("memoryStored", false);
                state.addMessage("Memory storage skipped: service not available");
                return chain.next(context, state);
            }

            // Build memory content from execution
            String requestText = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().toString() 
                    : "Unknown request";
            
            String responseText = "Response for: " + requestText;
            
            String memoryText = String.format(
                    "Request: %s\nResponse: %s\nExecution ID: %s",
                    requestText,
                    responseText,
                    executionId
            );

            // Use a HashMap instead of Map.of() because executionId may be null
            // when the upstream ActionExecutionStage failed gracefully and
            // continued without setting an execution identifier.
            // MemoryContent uses Map.copyOf() internally which rejects null values,
            // so a non-null fallback is required.
            String safeExecutionId = executionId != null ? executionId : "none";
            java.util.Map<String, Object> contentMetadata = new java.util.HashMap<>();
            contentMetadata.put("requestId", requestId);
            contentMetadata.put("executionId", safeExecutionId);
            contentMetadata.put("topics", extractTopics(requestText));
            contentMetadata.put("concepts", extractConcepts(requestText));

            MemoryContent memoryContent = new MemoryContent(
                    memoryText,
                    null, // No embedding
                    contentMetadata,
                    Instant.now()
            );

            MemoryMetadata memoryMetadata = new MemoryMetadata(
                    new MemoryId("pending-" + requestId), // ID will be replaced by service
                    MemoryType.EPISODIC,
                    MemoryStatus.ACTIVE,
                    MemoryVisibility.PRIVATE,
                    new IdentityId("sdk-local-user"), // owner
                    java.util.Set.of(), // empty tags
                    0.7, // importance
                    0.8, // confidence
                    "pipeline-execution",
                    Instant.now(),
                    Instant.now(),
                    Instant.now(),
                    0L // access count
            );

            CreateMemoryRequest createRequest = new CreateMemoryRequest(
                    memoryContent,
                    memoryMetadata,
                    Instant.now()
            );

            // Store memory
            MemoryId storedMemoryId = memoryService.createMemory(createRequest);
            boolean memoryStored = storedMemoryId != null;

            // Store memory information in state
            state.addMetadata("storedMemoryId", storedMemoryId != null ? storedMemoryId.value() : "none");
            state.addMetadata("memoryStored", memoryStored);
            state.addMetadata("memoryType", MemoryType.EPISODIC);
            state.addMessage("Memory stored: " + (storedMemoryId != null ? storedMemoryId.value() : "none") + " for execution " + executionId);

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Memory storage failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("MEMORY_STORE_FAILED")
                    .addMessage("Memory store stage failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Extracts topics from text (simple implementation).
     *
     * @param text the text to analyze
     * @return comma-separated topics
     */
    private String extractTopics(String text) {
        // Simple topic extraction - in real implementation, use NLP
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder topics = new StringBuilder();
        for (String word : words) {
            if (word.length() > 4 && !isStopWord(word)) {
                if (topics.length() > 0) {
                    topics.append(", ");
                }
                topics.append(word);
            }
        }
        return topics.toString();
    }

    /**
     * Extracts concepts from text (simple implementation).
     *
     * @param text the text to analyze
     * @return comma-separated concepts
     */
    private String extractConcepts(String text) {
        // Simple concept extraction - in real implementation, use NLP
        return extractTopics(text); // Same as topics for now
    }

    /**
     * Checks if a word is a stop word.
     *
     * @param word the word to check
     * @return true if stop word
     */
    private boolean isStopWord(String word) {
        return switch (word) {
            case "the", "is", "at", "which", "on", "and", "or", "but", "in", "with", "a", "an", "to", "for", "of", "as" -> true;
            default -> false;
        };
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}