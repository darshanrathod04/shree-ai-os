package com.shreeai.os.platform.runtime.reflection;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionVerdict;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReflectionPipelineIntegrationTest {

    private DefaultReflectionEngine reflectionEngine;
    private InMemoryReflectionRepository repository;
    private ReflectionImportanceScorer importanceScorer;
    private MemoryService memoryService;
    private ReflectionMemoryBridge memoryBridge;

    @BeforeEach
    void setUp() {
        reflectionEngine = new DefaultReflectionEngine();
        repository = new InMemoryReflectionRepository();
        importanceScorer = new ReflectionImportanceScorer();
        memoryService = mock(MemoryService.class);
        when(memoryService.createMemory(any(CreateMemoryRequest.class)))
                .thenReturn(new MemoryId("mem-bridge-1"));
        memoryBridge = new ReflectionMemoryBridge(memoryService);
    }

    @Test
    void fullPipelineExecutionSuccess() {
        ReflectionInput input = new ReflectionInput(
                "exec-1", "Process data", 3, "SUCCESS",
                true, "Data processed correctly", 0.85
        );
        ReflectionAnalysis analysis = reflectionEngine.reflect(input);
        assertEquals(ReflectionVerdict.SUCCESS, analysis.verdict());
        assertTrue(analysis.score() >= 0.75);
        assertTrue(analysis.memoryWorthy());

        int importance = importanceScorer.score(
                analysis.verdict().name(), analysis.score(),
                analysis.lessons(), List.of()
        );
        assertTrue(importance >= 0 && importance <= 100);

        ReflectionHistory history = new ReflectionHistory(
                "tenant-int", "tenant-int", "exec-1", "req-1",
                analysis.verdict().name(), analysis.score(),
                importance, analysis.lessons(), null,
                analysis.retryAdvised(), analysis.evaluatedAt()
        );
        repository.save(history);

        String bridgeId = memoryBridge.storeLessons(
                "tenant-int", "exec-1", "req-1",
                analysis.verdict().name(), analysis.score(),
                analysis.lessons()
        );

        assertNotNull(bridgeId);
        assertEquals("mem-bridge-1", bridgeId);
        assertEquals(1, repository.countByTenantId("tenant-int"));
        Optional<ReflectionHistory> found = repository.findByExecutionId("tenant-int", "exec-1");
        assertTrue(found.isPresent());
    }

    @Test
    void fullPipelineExecutionFailure() {
        ReflectionInput input = new ReflectionInput(
                "exec-fail-1", "Process payment", 0, "FAILED",
                false, "", 0.2
        );
        ReflectionAnalysis analysis = reflectionEngine.reflect(input);
        assertEquals(ReflectionVerdict.FAILURE, analysis.verdict());
        assertTrue(analysis.score() < 0.4);
        assertTrue(analysis.retryAdvised());

        int importance = importanceScorer.score(
                analysis.verdict().name(), analysis.score(),
                analysis.lessons(), List.of()
        );
        assertTrue(importance >= 50, "Failure importance should be high, got " + importance);

        ReflectionHistory history = new ReflectionHistory(
                "tenant-fail", "tenant-fail", "exec-fail-1", "req-fail",
                analysis.verdict().name(), analysis.score(),
                importance, analysis.lessons(),
                "Execution scored below threshold",
                analysis.retryAdvised(), analysis.evaluatedAt()
        );
        repository.save(history);

        String bridgeId = memoryBridge.storeLessons(
                "tenant-fail", "exec-fail-1", "req-fail",
                analysis.verdict().name(), analysis.score(),
                analysis.lessons()
        );

        assertNotNull(bridgeId);
        List<ReflectionHistory> failures = repository.findFailuresByTenantId("tenant-fail", 10);
        assertEquals(1, failures.size());
        assertTrue(failures.get(0).retryAdvised());
    }
}