package com.shreeai.os.platform.kernels.memory.service;

import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests verifying that {@link MemoryLifecycleService} is
 * properly wired into {@link DefaultMemoryService}.
 */
class DefaultMemoryServiceLifecycleWiringTest {

    private DefaultMemoryService memoryService;

    @BeforeEach
    void setUp() {
        MemoryValidator validator = new MemoryValidator();
        DefaultMemoryProcessingEngine processingEngine = new DefaultMemoryProcessingEngine();
        this.memoryService = new DefaultMemoryService(validator, processingEngine);
    }

    @Test
    void scoreImportanceReturnsZeroForAbsentMemory() {
        assertEquals(0.0,
                memoryService.scoreImportance(new MemoryId("nonexistent")));
    }

    @Test
    void scoreImportanceIsPositiveForStoredMemory() {
        MemoryId id = createWorkingMemory(0.5);
        double score = memoryService.scoreImportance(id);
        assertTrue(score > 0.0);
    }

    @Test
    void touchMemoryIncrementsAccessCount() {
        MemoryId id = createWorkingMemory(0.3);
        Memory before = memoryService.findById(id).orElseThrow();
        assertEquals(0L, before.metadata().accessCount());
        Optional<Memory> touched = memoryService.touchMemory(id);
        assertTrue(touched.isPresent());
        assertEquals(1L, touched.get().metadata().accessCount());
    }

    @Test
    void touchMemoryReturnsEmptyForAbsentMemory() {
        assertTrue(memoryService.touchMemory(new MemoryId("absent")).isEmpty());
    }

        @Test
    void promoteIfEligibleDoesNotPromoteLowAccessWorkingMemory() {
        MemoryId id = createWorkingMemory(0.1);
        assertTrue(memoryService.promoteIfEligible(id).isEmpty());
    }

    @Test
    void promoteIfEligibleDoesNotPromoteNonWorkingMemory() {
        MemoryId id = createMemory(MemoryType.SEMANTIC, 0.5);
        assertTrue(memoryService.promoteIfEligible(id).isEmpty());
    }

    @Test
    void promoteIfEligiblePromotesAfterEnoughTouches() {
        MemoryId id = createWorkingMemory(0.3);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);

        Optional<Memory> promoted = memoryService.promoteIfEligible(id);
        assertTrue(promoted.isPresent());
        assertEquals(MemoryType.SEMANTIC, promoted.get().metadata().type());
    }

        @Test
    void versionHistoryRecordsPromotion() {
        MemoryId id = createWorkingMemory(0.3);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);
        memoryService.promoteIfEligible(id);

        // Each touch creates a version snapshot (touched != existing),
        // plus one more from promotion = 4 total snapshots.
        List<Memory> history = memoryService.history(id);
        assertEquals(4, history.size(),
                "3 touches + 1 promotion should record 4 version snapshots");
        // History is oldest-first: the first entry is the original working memory.
        assertEquals(MemoryType.WORKING, history.get(0).metadata().type(),
                "History's oldest entry should be the original working memory");
    }

    @Test
    void consolidateMemoriesPromotesWorkingMemory() {
        MemoryId id = createWorkingMemory(0.3);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);
        memoryService.touchMemory(id);

        List<Memory> changed = memoryService.consolidateMemories();
        assertEquals(1, changed.size());
        Memory stored = memoryService.findById(id).orElseThrow();
        assertEquals(MemoryType.SEMANTIC, stored.metadata().type());
    }

    // --- Helpers ---

    private MemoryId createMemory(MemoryType type, double importance) {
        MemoryId id = new MemoryId("mem-" + System.nanoTime());
        Instant now = Instant.now();
        MemoryContent content = new MemoryContent("test content", null, Map.of(), now);
        MemoryMetadata metadata = new MemoryMetadata(
                id, type, MemoryStatus.ACTIVE, MemoryVisibility.PRIVATE,
                new IdentityId("test-owner"), Set.of("test"),
                importance, 0.9, "test", now, now, now, 0L);
        CreateMemoryRequest request = new CreateMemoryRequest(content, metadata, now);
        return memoryService.createMemory(request);
    }

    private MemoryId createWorkingMemory(double importance) {
        return createMemory(MemoryType.WORKING, importance);
    }
}
