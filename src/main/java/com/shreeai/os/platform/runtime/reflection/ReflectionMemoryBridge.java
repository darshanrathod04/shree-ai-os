package com.shreeai.os.platform.runtime.reflection;

import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <b>ReflectionMemoryBridge</b>
 *
 * <p>Bridges the Reflection Intelligence Layer to the Memory Kernel.
 * Persists extracted lessons as OBSERVATION-type memories with embeddings
 * for semantic retrieval during future planning cycles.</p>
 *
 * <p>The bridge is deterministic and side-effect-free when memoryService is null.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ReflectionMemoryBridge {

    private final MemoryService memoryService;

    /**
     * Creates the bridge.
     *
     * @param memoryService the memory service (may be null; null means no-op)
     */
    public ReflectionMemoryBridge(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * Persists reflection lessons as a memory observation.
     *
     * @param tenantId    the tenant identifier
     * @param executionId the execution identifier
     * @param requestId   the original request identifier
     * @param verdict     the reflection verdict
     * @param score       the quality score
     * @param lessons     the extracted lessons
     * @return the memory id, or null when nothing was stored
     */
    public String storeLessons(
            String tenantId,
            String executionId,
            String requestId,
            String verdict,
            double score,
            List<String> lessons
    ) {
        if (memoryService == null || lessons == null || lessons.isEmpty()) {
            return null;
        }

        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");

        String lessonText = lessons.stream()
                .map(lesson -> "- " + lesson)
                .collect(Collectors.joining("\n"));

        String contentText = String.format(
                "Reflection lessons for execution %s (request %s) [%s | score %.2f]:%n%s",
                executionId, requestId, verdict, score, lessonText
        );

        MemoryContent content = new MemoryContent(
                contentText,
                null,
                Map.of(
                        "verdict", verdict,
                        "score", score,
                        "tenantId", tenantId,
                        "executionId", executionId,
                        "requestId", requestId,
                        "source", "reflection-bridge"
                ),
                Instant.now()
        );

        MemoryMetadata metadata = new MemoryMetadata(
                new MemoryId("reflection-" + executionId),
                MemoryType.OBSERVATION,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("sdk-local-user"),
                Set.of("reflection", "lesson", "auto-generated"),
                computeImportance(verdict, score),
                1.0,
                "reflection-memory-bridge",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L
        );

        try {
            MemoryId stored = memoryService.createMemory(
                    new CreateMemoryRequest(content, metadata, Instant.now())
            );
            return stored != null ? stored.value() : null;
        } catch (Exception e) {
            // Memory persistence must never break the reflection pipeline.
            // Log and return null.
            return null;
        }
    }

    private double computeImportance(String verdict, double score) {
        if ("FAILURE".equals(verdict)) {
            return 0.9;
        }
        if ("PARTIAL".equals(verdict)) {
            return 0.6;
        }
        return 0.4;
    }
}