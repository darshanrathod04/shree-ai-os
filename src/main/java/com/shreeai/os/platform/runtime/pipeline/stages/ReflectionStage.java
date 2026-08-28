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

    /**
     * Creates a new ReflectionStage.
     *
     * @param memoryService the memory service used to persist lessons (may be null)
     */
    public ReflectionStage(MemoryService memoryService) {
        this.memoryService = memoryService;
        this.reflectionEngine = new DefaultReflectionEngine();
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

            state.addMessage("Reflection: " + analysis.summary());
            publishReflectionEvent(context, requestId, analysis);

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