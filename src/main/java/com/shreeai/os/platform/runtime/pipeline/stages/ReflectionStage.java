package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionVerdict;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.runtime.reflection.ReflectionAnalyticsService;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import com.shreeai.os.platform.runtime.reflection.ReflectionImportanceScorer;
import com.shreeai.os.platform.runtime.reflection.ReflectionMemoryBridge;
import com.shreeai.os.platform.runtime.reflection.ReflectionRepository;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReflectionStage - Evaluates the execution outcome after the fact.
 *
 * <p>EO-V1.5 Reflection Kernel: after {@code ActionExecutionStage} completes,
 * this stage scores the execution quality, assigns a verdict, extracts
 * actionable lessons, persists memory-worthy lessons through the memory
 * kernel, and advises on retry for the chief layer.</p>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since EO-V1.5
 */
public final class ReflectionStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Reflection")
            .priority(9)
            .enabled(true)
            .version("1.0")
            .description("Evaluates execution outcome and stores lessons")
            .build();

    private final DefaultReflectionEngine reflectionEngine;
    private final MemoryService memoryService;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionImportanceScorer importanceScorer;
    private final ReflectionMemoryBridge memoryBridge;
    private final ReflectionAnalyticsService analyticsService;

    /**
     * Creates a new ReflectionStage with full reflection intelligence (Phase 1.5).
     *
     * @param memoryService       the memory service used to persist lessons (may be null)
     * @param reflectionRepository the reflection repository (may be null)
     * @param importanceScorer     the importance scorer (may be null)
     * @param memoryBridge         the memory bridge (may be null)
     * @param analyticsService     the analytics service (may be null)
     */
    public ReflectionStage(
            MemoryService memoryService,
            ReflectionRepository reflectionRepository,
            ReflectionImportanceScorer importanceScorer,
            ReflectionMemoryBridge memoryBridge,
            ReflectionAnalyticsService analyticsService
    ) {
        this.memoryService = memoryService;
        this.reflectionRepository = reflectionRepository;
        this.importanceScorer = importanceScorer != null ? importanceScorer : new ReflectionImportanceScorer();
        this.memoryBridge = memoryBridge;
        this.analyticsService = analyticsService;
        this.reflectionEngine = new DefaultReflectionEngine();
    }

    /**
     * Creates a new ReflectionStage (backward-compatible).
     *
     * @param memoryService the memory service used to persist lessons (may be null)
     */
    public ReflectionStage(MemoryService memoryService) {
        this(memoryService, null, null, null, null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            String requestId = context.getExecutionRequest() != null
                    ? context.getExecutionRequest().getRequestId()
                    : "unknown";
            String requestText = context.getExecutionRequest() != null
                    ? context.getExecutionRequest().getUserInput()
                    : "";

            Map<String, Object> metadata = state.getMetadata();

            String actionStatus = stringOf(metadata, "executionStatus");
            boolean executionCompleted = Boolean.TRUE.equals(metadata.get("executionCompleted"));

            ReflectionInput input = new ReflectionInput(
                    requestId,
                    requestText,
                    intOf(metadata, "planStepCount"),
                    actionStatus,
                    executionCompleted,
                    stringOf(metadata, "knowledgeSummary"),
                    doubleOf(metadata, "knowledgeConfidence"));

            ReflectionAnalysis analysis = reflectionEngine.reflect(input);

            state.addMetadata("reflectionVerdict", analysis.verdict().name());
            state.addMetadata("reflectionScore", analysis.score());
            state.addMetadata("reflectionLessons", analysis.lessons());
            state.addMetadata("reflectionSummary", analysis.summary());
            state.addMetadata("reflectionRetryAdvised", analysis.retryAdvised());

            String lessonId = storeLesson(context, requestId, analysis);

            if (lessonId != null) {
                state.addMetadata("reflectionLessonMemoryId", lessonId);
            }

            // Phase 1.5: Compute importance score and persist to repository
            int importanceScore = computeImportanceScore(context, analysis);
            String memoryBridgeId = persistReflection(context, requestId, analysis, importanceScore);

            if (memoryBridgeId != null) {
                state.addMetadata("reflectionMemoryId", memoryBridgeId);
            }
            state.addMetadata("reflectionImportanceScore", importanceScore);

            state.addMessage("Reflection: " + analysis.summary());
            publishReflectionEvent(context, requestId, analysis);
            publishReflectionPersistedEvent(context, requestId, analysis, importanceScore, memoryBridgeId);

            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Reflection failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("REFLECTION_FAILED")
                    .addMessage("Reflection stage failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Persists memory-worthy lessons through the memory kernel.
     *
     * @return the stored lesson memory id, or null when nothing was stored
     */
    private String storeLesson(PipelineContext context, String requestId, ReflectionAnalysis analysis) {
        if (memoryService == null || !analysis.memoryWorthy()) {
            return null;
        }

        String lessonText = analysis.lessons().stream()
                .map(lesson -> "- " + lesson)
                .collect(Collectors.joining("\n"));

        MemoryContent content = new MemoryContent(
                "Reflection lessons for request " + requestId + " ["
                        + analysis.verdict() + "]:\n" + lessonText,
                null,
                Map.of("verdict", analysis.verdict().name(), "score", analysis.score()),
                Instant.now());

        MemoryMetadata lessonMetadata = new MemoryMetadata(
                new MemoryId("pending-reflection-" + requestId),
                MemoryType.OBSERVATION,
                MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE,
                new IdentityId("sdk-local-user"),
                java.util.Set.of("reflection", "lesson"),
                analysis.verdict() == ReflectionVerdict.FAILURE ? 0.9 : 0.5,
                1.0,
                "reflection-stage",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                0L);

        MemoryId stored = memoryService.createMemory(new CreateMemoryRequest(content, lessonMetadata, Instant.now()));
        return stored != null ? stored.value() : null;
    }

    private void publishReflectionEvent(
            PipelineContext context,
            String requestId,
            ReflectionAnalysis analysis
    ) {
        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.REFLECTION_COMPLETED,
                        requestId,
                        "Reflection",
                        Instant.now(),
                        Map.of(
                                "verdict", analysis.verdict().name(),
                                "score", analysis.score(),
                                "retryAdvised", analysis.retryAdvised()
                        )
                )
        );
    }

    /**
     * Phase 1.5: Computes importance score for the reflection.
     */
    private int computeImportanceScore(PipelineContext context, ReflectionAnalysis analysis) {
        String tenantId = resolveTenantId(context);
        List<List<String>> previousLessons = resolvePreviousLessons(tenantId);
        return importanceScorer.score(
                analysis.verdict().name(),
                analysis.score(),
                analysis.lessons(),
                previousLessons
        );
    }

    /**
     * Phase 1.5: Persists reflection to repository and memory bridge.
     */
    private String persistReflection(
            PipelineContext context,
            String requestId,
            ReflectionAnalysis analysis,
            int importanceScore
    ) {
        String tenantId = resolveTenantId(context);
        String executionId = resolveExecutionId(context, requestId);
        String rootCause = analysis.verdict() == ReflectionVerdict.FAILURE
                ? "Execution scored below threshold"
                : null;

        // Save to repository
        if (reflectionRepository != null) {
            ReflectionHistory history = new ReflectionHistory(
                    tenantId,
                    tenantId,
                    executionId,
                    requestId,
                    analysis.verdict().name(),
                    analysis.score(),
                    importanceScore,
                    analysis.lessons(),
                    rootCause,
                    analysis.retryAdvised(),
                    analysis.evaluatedAt()
            );
            reflectionRepository.save(history);
        }

        // Bridge to memory kernel
        if (memoryBridge != null) {
            return memoryBridge.storeLessons(
                    tenantId,
                    executionId,
                    requestId,
                    analysis.verdict().name(),
                    analysis.score(),
                    analysis.lessons()
            );
        }

        return null;
    }

    /**
     * Phase 1.5: Publishes REFLECTION_PERSISTED event.
     */
    private void publishReflectionPersistedEvent(
            PipelineContext context,
            String requestId,
            ReflectionAnalysis analysis,
            int importanceScore,
            String memoryBridgeId
    ) {
        Object value = context.getAttribute("runtimeEventBus");
        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.REFLECTION_PERSISTED,
                        requestId,
                        "Reflection",
                        Instant.now(),
                        Map.of(
                                "executionId", resolveExecutionId(context, requestId),
                                "tenantId", resolveTenantId(context),
                                "verdict", analysis.verdict().name(),
                                "score", analysis.score(),
                                "importanceScore", importanceScore,
                                "reflectionMemoryId", memoryBridgeId != null ? memoryBridgeId : "none"
                        )
                )
        );
    }

    private String resolveTenantId(PipelineContext context) {
        Object value = context.getAttribute("tenantId");
        return value instanceof String s && !s.isBlank() ? s : "default";
    }

    private String resolveExecutionId(PipelineContext context, String fallback) {
        Object value = context.getAttribute("executionId");
        return value instanceof String s && !s.isBlank() ? s : fallback;
    }

    private List<List<String>> resolvePreviousLessons(String tenantId) {
        if (reflectionRepository == null) {
            return List.of();
        }
        return reflectionRepository.findByTenantId(tenantId, 5).stream()
                .map(ReflectionHistory::lessons)
                .collect(Collectors.toList());
    }

    private static String stringOf(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : "";
    }

    private static double doubleOf(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    private static int intOf(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof List<?> list) {
            return list.size();
        }
        return 0;
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}