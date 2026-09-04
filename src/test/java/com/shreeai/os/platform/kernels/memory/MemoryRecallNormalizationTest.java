package com.shreeai.os.platform.kernels.memory;

import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryLifecycleService;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint-9 acceptance tests: Memory Search and Memory Recall must handle
 * natural-language queries (e.g. "who is darshan") by applying the shared
 * QueryNormalizer so that interrogative prefixes are stripped before retrieval.
 */
class MemoryRecallNormalizationTest {

    private Memory createMemory(String title, String content) {
        MemoryId id = new MemoryId("mem-" + System.nanoTime());
        Instant now = Instant.now();
        MemoryContent memoryContent = new MemoryContent(content, null, Map.of(), now);
        IdentityId ownerId = new IdentityId("sprint-9-test");
        MemoryMetadata metadata = new MemoryMetadata(
                id, MemoryType.EPISODIC, MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE, ownerId, Set.of("test"),
                0.8, 0.9, title,
                now, now, now, 0L
        );
        return new Memory(id, memoryContent, metadata, now, now);
    }

    private DefaultMemoryService newService() {
        return new DefaultMemoryService(
                new MemoryValidator(),
                new DefaultMemoryProcessingEngine(),
                new MemoryLifecycleService());
    }

    @Test
    void searchWithWhoIsPrefixReturnsMemory() {
        DefaultMemoryService service = newService();
        Memory memory = createMemory("Darshan", "Founder of Shree AI OS");
        service.getMemoriesForTest().put(memory.id(), memory);

        List<Memory> results = service.search("who is darshan");
        assertFalse(results.isEmpty(),
                "search('who is darshan') should find the Darshan memory after normalization");
        assertEquals("Darshan", results.getFirst().metadata().source());
    }

    @Test
    void searchWithWhatIsPrefixReturnsMemory() {
        DefaultMemoryService service = newService();
        Memory memory = createMemory("Java", "Programming language");
        service.getMemoriesForTest().put(memory.id(), memory);

        List<Memory> results = service.search("what is java");
        assertFalse(results.isEmpty());
        assertEquals("Java", results.getFirst().metadata().source());
    }

    @Test
    void searchWithBareQueryStillWorks() {
        DefaultMemoryService service = newService();
        Memory memory = createMemory("Darshan", "Founder of Shree AI OS");
        service.getMemoriesForTest().put(memory.id(), memory);

        List<Memory> results = service.search("darshan");
        assertFalse(results.isEmpty(),
                "search('darshan') should find the Darshan memory");
    }

    @Test
    void searchWithUnknownQueryReturnsEmpty() {
        DefaultMemoryService service = newService();
        Memory memory = createMemory("Darshan", "Founder of Shree AI OS");
        service.getMemoriesForTest().put(memory.id(), memory);

        List<Memory> results = service.search("who is python");
        assertTrue(results.isEmpty());
    }

    @Test
    void searchMatchesContentNotJustTitle() {
        DefaultMemoryService service = newService();
        Memory memory = createMemory("Person", "Darshan is the founder of Shree AI OS");
        service.getMemoriesForTest().put(memory.id(), memory);

        List<Memory> results = service.search("who is darshan");
        assertFalse(results.isEmpty(),
                "search('who is darshan') should find via content match");
    }

    @Test
    void rankByRelevanceWithInterrogativePrefix() {
        MemoryRankingService ranking = new MemoryRankingService();
        List<Memory> memories = List.of(
                createMemory("Darshan", "Founder of Shree AI OS"),
                createMemory("Java", "Programming language")
        );

        List<Memory> ranked = ranking.rankByRelevance("who is darshan", memories, 10);
        assertFalse(ranked.isEmpty());
        assertEquals("Darshan", ranked.getFirst().metadata().source());
    }

    @Test
    void rankByRelevanceWithEmptyQueryReturnsEmpty() {
        MemoryRankingService ranking = new MemoryRankingService();
        List<Memory> memories = List.of(
                createMemory("Darshan", "Founder of Shree AI OS")
        );

        List<Memory> ranked = ranking.rankByRelevance("", memories, 10);
        assertTrue(ranked.isEmpty());
    }

    @Test
    void rankByRelevanceWithNullQueryReturnsEmpty() {
        MemoryRankingService ranking = new MemoryRankingService();
        List<Memory> memories = List.of(
                createMemory("Darshan", "Founder of Shree AI OS")
        );

        List<Memory> ranked = ranking.rankByRelevance(null, memories, 10);
        assertTrue(ranked.isEmpty());
    }
}
