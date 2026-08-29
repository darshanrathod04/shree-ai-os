package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryKernelHandler Tests")
class MemoryKernelHandlerTest {

    private MockMemorySearchService mockSearchService;
    private MemoryKernelHandler handler;

    @BeforeEach
    void setUp() {
        mockSearchService = new MockMemorySearchService();
        handler = new MemoryKernelHandler(mockSearchService);
    }

    @Test
    @DisplayName("Handle memory recall returns SUCCESS with results")
    void handleMemoryRecallSuccess() {
        List<Memory> memories = List.of(
                createTestMemory("memory-1", "Important fact about AI"));

        mockSearchService.setResults(memories);

        RichExecutionResult result = handler.handle(
                ExecutionCapability.MEMORY_RECALL,
                "AI facts",
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.isSuccess());
        assertTrue(result.output().contains("Found 1 memory"));
        assertTrue(result.output().contains("Important fact about AI"));
        assertEquals(1, result.metadata().get("memoryCount"));
    }

    @Test
    @DisplayName("Handle empty memory results")
    void handleEmptyMemoryResults() {
        mockSearchService.setResults(List.of());

        RichExecutionResult result = handler.handle(
                ExecutionCapability.MEMORY_RECALL,
                "no results query",
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.output().contains("Found 0 memory"));
        assertEquals(0, result.metadata().get("memoryCount"));
        assertEquals(0.1, result.confidence(), 0.01);
    }

    @Test
    @DisplayName("Handle multiple memories formats output")
    void handleMultipleMemories() {
        List<Memory> memories = List.of(
                createTestMemory("memory-1", "First fact"),
                createTestMemory("memory-2", "Second fact"),
                createTestMemory("memory-3", "Third fact"));

        mockSearchService.setResults(memories);

        RichExecutionResult result = handler.handle(
                ExecutionCapability.MEMORY_RECALL,
                "all facts",
                Map.of());

        assertTrue(result.output().contains("Found 3 memory"));
        assertTrue(result.output().contains("First fact"));
        assertTrue(result.output().contains("Second fact"));
        assertTrue(result.output().contains("Third fact"));
    }

    @Test
    @DisplayName("Handle search exception returns failure")
    void handleSearchExceptionReturnsFailure() {
        mockSearchService.setException(new RuntimeException("Search failed"));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.MEMORY_RECALL,
                "error",
                Map.of());

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("Memory recall failed"));
    }

    @Test
    @DisplayName("Null input handled gracefully")
    void nullInputHandledGracefully() {
        mockSearchService.setResults(List.of());

        RichExecutionResult result = handler.handle(
                ExecutionCapability.MEMORY_RECALL,
                null,
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
    }

    private Memory createTestMemory(String id, String text) {
        MemoryId memoryId = new MemoryId(id);
        Instant now = Instant.now();
        MemoryContent content = new MemoryContent(
                text, null, Map.of(), now);
        IdentityId ownerId = new IdentityId("test-user");
                MemoryMetadata metadata = new MemoryMetadata(
                memoryId, MemoryType.WORKING, MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE, ownerId, Set.of("test"),
                0.8, 0.9, "test", now, now, now, 0);
        return new Memory(memoryId, content, metadata, now, now);
    }

    /**
     * Minimal MemorySearchService mock for testing.
     */
    private static final class MockMemorySearchService implements MemorySearchService {

        private List<Memory> results = List.of();
        private RuntimeException exception;

        void setResults(List<Memory> results) {
            this.results = results;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public List<Memory> search(String query) {
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            return results;
        }

        @Override
        public List<Memory> searchByTags(Set<String> tags) {
            return results;
        }

        @Override
        public List<Memory> searchByDate(Instant from, Instant to) {
            return results;
        }

        @Override
        public List<Memory> searchBySimilarity(String text) {
            return results;
        }

        @Override
        public List<Memory> searchByOwner(IdentityId ownerId) {
            return results;
        }
    }
}
