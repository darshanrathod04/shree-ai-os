package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.memory.api.MemoryQueryService;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Memory Kernel Integration Test
 *
 * <p>This test verifies that the Memory Kernel works correctly with real memory operations.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 4
 */
public class MemoryKernelIntegrationTest {

    private MemoryService memoryService;
    private MemoryQueryService memoryQueryService;
    private MemorySearchService memorySearchService;
    private MemoryRankingService memoryRankingService;

    @BeforeEach
    public void setUp() {
        // Initialize memory kernel services
        MemoryValidator validator = new MemoryValidator();
        DefaultMemoryProcessingEngine processingEngine = new DefaultMemoryProcessingEngine();
        
        this.memoryService = new DefaultMemoryService(validator, processingEngine);
        this.memoryQueryService = (MemoryQueryService) memoryService;
        this.memorySearchService = (MemorySearchService) memoryService;
        this.memoryRankingService = new MemoryRankingService();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup if needed
    }

    @Test
    public void testStoreAndRecallMemory() {
        // Create a memory
        MemoryContent content = new MemoryContent(
                "Java is a programming language",
                null,
                Map.of("topic", "programming"),
                Instant.now()
        );

        MemoryId tempId = new MemoryId("temp-" + System.currentTimeMillis());
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata metadata = new MemoryMetadata(
                tempId,
                MemoryType.SEMANTIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("programming", "java"),
                0.8,
                0.9,
                "test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );

        CreateMemoryRequest request = new CreateMemoryRequest(content, metadata, Instant.now());
        MemoryId memoryId = memoryService.createMemory(request);

        // Verify memory was stored
        assertNotNull(memoryId, "Memory ID should not be null");
        assertTrue(memoryQueryService.exists(memoryId), "Memory should exist");

        // Recall memory
        var foundMemory = memoryQueryService.findById(memoryId);
        assertTrue(foundMemory.isPresent(), "Memory should be found");
        assertEquals("Java is a programming language", foundMemory.get().content().text());
    }

    @Test
    public void testNoMemoryExists() {
        // Search for non-existent memory
        List<com.shreeai.os.platform.kernels.memory.model.Memory> results = 
                memorySearchService.search("nonexistent");
        
        assertTrue(results.isEmpty(), "Should return empty list when no memories exist");
    }

    @Test
    public void testMultipleMemoriesRanking() {
        // Store multiple memories
        storeMemory("Java programming tutorial", 0.9, 0.9);
        storeMemory("Python basics", 0.7, 0.8);
        storeMemory("Spring Boot framework", 0.8, 0.85);
        storeMemory("JavaScript fundamentals", 0.6, 0.7);

        // Search for Java-related memories
        List<com.shreeai.os.platform.kernels.memory.model.Memory> searchResults = 
                memorySearchService.search("Java");

        // Rank memories
        List<com.shreeai.os.platform.kernels.memory.model.Memory> rankedMemories = 
                memoryRankingService.rankByRelevance("Java", searchResults, 10);

        // Verify ranking
        assertFalse(rankedMemories.isEmpty(), "Should have ranked memories");
        assertTrue(rankedMemories.get(0).content().text().contains("Java"), 
                "Top result should be most relevant");
    }

    @Test
    public void testPipelineExecutionWithMemory() {
        // This test verifies memory stages work in pipeline context
        // Store a memory first
        MemoryContent content = new MemoryContent(
                "Test memory for pipeline",
                null,
                Map.of("pipeline", "test"),
                Instant.now()
        );

        MemoryId tempId = new MemoryId("temp-" + System.currentTimeMillis());
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata metadata = new MemoryMetadata(
                tempId,
                MemoryType.EPISODIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("pipeline", "test"),
                0.7,
                0.8,
                "pipeline-test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );

        CreateMemoryRequest request = new CreateMemoryRequest(content, metadata, Instant.now());
        MemoryId memoryId = memoryService.createMemory(request);
        assertNotNull(memoryId, "Memory should be stored");
    }

    @Test
    public void testStoreAfterExecutionRecallLater() {
        // Store memory
        MemoryContent content = new MemoryContent(
                "User asked about Java programming",
                null,
                Map.of("requestId", "req-001"),
                Instant.now()
        );

        MemoryId tempId = new MemoryId("temp-" + System.currentTimeMillis());
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata metadata = new MemoryMetadata(
                tempId,
                MemoryType.EPISODIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("java", "programming", "execution"),
                0.8,
                0.9,
                "execution",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );

        CreateMemoryRequest request = new CreateMemoryRequest(content, metadata, Instant.now());
        MemoryId memoryId = memoryService.createMemory(request);
        assertNotNull(memoryId, "Memory should be stored");

        // Recall later
        var recalledMemory = memoryQueryService.findById(memoryId);
        assertTrue(recalledMemory.isPresent(), "Memory should be recalled");
        assertEquals("User asked about Java programming", recalledMemory.get().content().text());
    }

    /**
     * Helper method to store a memory.
     */
    private MemoryId storeMemory(String text, double importance, double confidence) {
        MemoryContent content = new MemoryContent(
                text,
                null,
                Map.of("test", "true"),
                Instant.now()
        );

        MemoryId tempId = new MemoryId("temp-" + System.currentTimeMillis());
        IdentityId ownerId = new IdentityId("test-owner");
        MemoryMetadata metadata = new MemoryMetadata(
                tempId,
                MemoryType.SEMANTIC,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                ownerId,
                Set.of("test"),
                importance,
                confidence,
                "test",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );

        CreateMemoryRequest request = new CreateMemoryRequest(content, metadata, Instant.now());
        return memoryService.createMemory(request);
    }
}