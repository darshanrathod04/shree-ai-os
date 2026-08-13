package com.shreeai.os.platform.runtime.pipeline;

import com.shreeai.os.platform.cognition.CognitiveDecision;
import com.shreeai.os.platform.execution.ExecutionMetadata;
import com.shreeai.os.platform.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.pipeline.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Runtime Pipeline (Sprint 6.2A-R1).
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>Pipeline creation and stage ordering</li>
 *   <li>Chain progression and stage invocation</li>
 *   <li>Execution state recording (visited stages, completed stages)</li>
 *   <li>Immutability and thread safety</li>
 *   <li>Duplicate priority detection</li>
 *   <li>Startup validation</li>
 *   <li>Null safety</li>
 *   <li>Shadow mode</li>
 *   <li>PipelineResult immutability</li>
 *   <li>Short circuit detection</li>
 *   <li>Failure handling</li>
 *   <li>Timing</li>
 *   <li>State isolation</li>
 *   <li>Execution history</li>
 * </ul>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
class RuntimePipelineTest {

    // =====================================================
    // PIPELINE STAGE DESCRIPTOR TESTS
    // =====================================================

    @Test
    void testPipelineStageDescriptor_Builder_AllFields() {
        PipelineStageDescriptor descriptor = PipelineStageDescriptor.builder()
                .stageName("ValidationStage")
                .priority(1)
                .enabled(true)
                .version("1.0")
                .description("Validates execution requests")
                .build();

        assertEquals("ValidationStage", descriptor.getStageName());
        assertEquals(1, descriptor.getPriority());
        assertTrue(descriptor.isEnabled());
        assertEquals("1.0", descriptor.getVersion());
        assertEquals("Validates execution requests", descriptor.getDescription());
    }

    @Test
    void testPipelineStageDescriptor_Builder_DefaultValues() {
        PipelineStageDescriptor descriptor = PipelineStageDescriptor.builder()
                .stageName("TestStage")
                .build();

        assertEquals("TestStage", descriptor.getStageName());
        assertEquals(0, descriptor.getPriority());
        assertTrue(descriptor.isEnabled());
        assertEquals("1.0", descriptor.getVersion());
        assertNull(descriptor.getDescription());
    }

    @Test
    void testPipelineStageDescriptor_Builder_MissingStageName() {
        assertThrows(IllegalStateException.class, () -> {
            PipelineStageDescriptor.builder()
                    .priority(1)
                    .build();
        });
    }

    @Test
    void testPipelineStageDescriptor_EqualsAndHashCode() {
        PipelineStageDescriptor descriptor1 = PipelineStageDescriptor.builder()
                .stageName("Stage1")
                .priority(1)
                .enabled(true)
                .build();

        PipelineStageDescriptor descriptor2 = PipelineStageDescriptor.builder()
                .stageName("Stage1")
                .priority(1)
                .enabled(true)
                .build();

        PipelineStageDescriptor descriptor3 = PipelineStageDescriptor.builder()
                .stageName("Stage2")
                .priority(2)
                .build();

        assertEquals(descriptor1, descriptor2);
        assertEquals(descriptor1.hashCode(), descriptor2.hashCode());
        assertNotEquals(descriptor1, descriptor3);
    }

    @Test
    void testPipelineStageDescriptor_ToString() {
        PipelineStageDescriptor descriptor = PipelineStageDescriptor.builder()
                .stageName("TestStage")
                .priority(5)
                .enabled(false)
                .version("2.0")
                .description("Test description")
                .build();

        String toString = descriptor.toString();
        assertTrue(toString.contains("stageName='TestStage'"));
        assertTrue(toString.contains("priority=5"));
        assertTrue(toString.contains("enabled=false"));
        assertTrue(toString.contains("version='2.0'"));
    }

    // =====================================================
    // PIPELINE CONTEXT TESTS
    // =====================================================

    @Test
    void testPipelineContext_Builder_DefaultValues() {
        PipelineContext context = PipelineContext.builder().build();

        assertNotNull(context.getPipelineId());
        assertNull(context.getExecutionRequest());
        assertNull(context.getDecision());
        assertNull(context.getValidationResult());
        assertNull(context.getExecutionMetadata());
        assertNull(context.getResolvedContext());
        assertTrue(context.getAttributes().isEmpty());
        assertNotNull(context.getTimestamp());
    }

    @Test
    void testPipelineContext_Builder_AllFields() {
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .build();

        CognitiveDecision decision = new CognitiveDecision(
                CognitiveDecision.Action.RESPOND, "test"
        );

        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .build();

        Instant now = Instant.now();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");

        PipelineContext context = PipelineContext.builder()
                .pipelineId("pipe-123")
                .executionRequest(request)
                .decision(decision)
                .validationResult(null)
                .executionMetadata(metadata)
                .resolvedContext(null)
                .addAttribute("key1", "value1")
                .timestamp(now)
                .build();

        assertEquals("pipe-123", context.getPipelineId());
        assertSame(request, context.getExecutionRequest());
        assertSame(decision, context.getDecision());
        assertNull(context.getValidationResult());
        assertSame(metadata, context.getExecutionMetadata());
        assertNull(context.getResolvedContext());
        assertEquals("value1", context.getAttribute("key1"));
        assertEquals(now, context.getTimestamp());
    }

    @Test
    void testPipelineContext_Immutability() {
        PipelineContext context = PipelineContext.builder().build();

        // Verify collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            context.getAttributes().put("key", "value");
        });
    }

    @Test
    void testPipelineContext_EqualsAndHashCode() {
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .build();

        PipelineContext context1 = PipelineContext.builder()
                .pipelineId("pipe-1")
                .executionRequest(request)
                .build();

        PipelineContext context2 = PipelineContext.builder()
                .pipelineId("pipe-1")
                .executionRequest(request)
                .build();

        PipelineContext context3 = PipelineContext.builder()
                .pipelineId("pipe-2")
                .executionRequest(request)
                .build();

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
        assertNotEquals(context1, context3);
    }

    @Test
    void testPipelineContext_ToString() {
        PipelineContext context = PipelineContext.builder()
                .pipelineId("pipe-123")
                .build();

        String toString = context.toString();
        assertTrue(toString.contains("pipelineId='pipe-123'"));
    }

    // =====================================================
    // PIPELINE RESULT TESTS
    // =====================================================

    @Test
    void testPipelineResult_Builder_DefaultValues() {
        PipelineResult result = PipelineResult.builder().build();

        assertNotNull(result.getResultId());
        assertFalse(result.isSuccess());
        assertEquals("UNKNOWN", result.getStatus());
        assertNull(result.getCurrentStage());
        assertTrue(result.getCompletedStages().isEmpty());
        assertEquals(0, result.getProcessingTime());
        assertTrue(result.getMessages().isEmpty());
        assertNull(result.getMetadata());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testPipelineResult_Builder_AllFields() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();
        Instant now = Instant.now();

        PipelineResult result = PipelineResult.builder()
                .resultId("result-123")
                .success(true)
                .status("SUCCESS")
                .currentStage("AuditStage")
                .addCompletedStage("ValidationStage")
                .addCompletedStage("ExecutionStage")
                .processingTime(150)
                .addMessage("Stage 1 completed")
                .addMessage("Stage 2 completed")
                .metadata(metadata)
                .timestamp(now)
                .build();

        assertEquals("result-123", result.getResultId());
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("AuditStage", result.getCurrentStage());
        assertEquals(2, result.getCompletedStages().size());
        assertEquals(150, result.getProcessingTime());
        assertTrue(result.hasMessages());
        assertSame(metadata, result.getMetadata());
        assertFalse(result.isFailed());
    }

    @Test
    void testPipelineResult_Immutability() {
        PipelineResult result = PipelineResult.builder()
                .addCompletedStage("Stage1")
                .build();

        // Verify collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            result.getCompletedStages().add("Stage2");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            result.getMessages().add("message");
        });
    }

    @Test
    void testPipelineResult_EqualsAndHashCode() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();

        PipelineResult result1 = PipelineResult.builder()
                .resultId("result-1")
                .success(true)
                .status("SUCCESS")
                .addCompletedStage("Stage1")
                .metadata(metadata)
                .build();

        PipelineResult result2 = PipelineResult.builder()
                .resultId("result-1")
                .success(true)
                .status("SUCCESS")
                .addCompletedStage("Stage1")
                .metadata(metadata)
                .build();

        PipelineResult result3 = PipelineResult.builder()
                .resultId("result-2")
                .success(false)
                .build();

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    void testPipelineResult_ToString() {
        PipelineResult result = PipelineResult.builder()
                .resultId("result-123")
                .success(true)
                .status("SUCCESS")
                .currentStage("TestStage")
                .processingTime(100)
                .build();

        String toString = result.toString();
        assertTrue(toString.contains("resultId='result-123'"));
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("status='SUCCESS'"));
        assertTrue(toString.contains("currentStage='TestStage'"));
        assertTrue(toString.contains("processingTime=100ms"));
    }

    // =====================================================
    // DEFAULT EXECUTION CHAIN TESTS
    // =====================================================

    @Test
    void testDefaultExecutionChain_EmptyStages() {
        List<ExecutionStage> stages = Collections.emptyList();
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);

        PipelineContext context = PipelineContext.builder().build();
        PipelineExecutionState state = new PipelineExecutionState(stages);
        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void testDefaultExecutionChain_SingleStage() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);

        PipelineContext context = PipelineContext.builder().build();
        PipelineExecutionState state = new PipelineExecutionState(stages);
        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess());
        assertEquals(1, state.getCompletedStages().size());
        assertTrue(state.getCompletedStages().contains("Stage1"));
        assertEquals(1, state.getVisitedStages().size());
        assertTrue(state.getVisitedStages().contains("Stage1"));
    }

    @Test
    void testDefaultExecutionChain_MultipleStages() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new TestExecutionStage("Stage2", 2),
                new TestExecutionStage("Stage3", 3)
        );

        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = PipelineContext.builder().build();
        PipelineExecutionState state = new PipelineExecutionState(stages);
        PipelineResult result = chain.next(context, state);

        assertTrue(result.isSuccess());
        assertEquals(3, state.getCompletedStages().size());
        assertTrue(state.getCompletedStages().contains("Stage1"));
        assertTrue(state.getCompletedStages().contains("Stage2"));
        assertTrue(state.getCompletedStages().contains("Stage3"));
        assertEquals(3, state.getVisitedStages().size());
    }

    @Test
    void testDefaultExecutionChain_StageShortCircuit() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new ShortCircuitStage("Stage2", 2),
                new TestExecutionStage("Stage3", 3)
        );

        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        PipelineContext context = PipelineContext.builder().build();
        PipelineExecutionState state = new PipelineExecutionState(stages);
        PipelineResult result = chain.next(context, state);

        assertFalse(result.isSuccess());
        assertEquals("SHORT_CIRCUIT", result.getStatus());
        assertEquals(1, state.getCompletedStages().size());
        assertTrue(state.getCompletedStages().contains("Stage1"));
        assertEquals(2, state.getVisitedStages().size());
        assertTrue(state.getVisitedStages().contains("Stage1"));
        assertTrue(state.getVisitedStages().contains("Stage2"));
        assertTrue(state.isShortCircuited());
    }

    @Test
    void testDefaultExecutionChain_Immutability() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);

        // Verify stages list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            chain.getStages().add(new TestExecutionStage("Stage2", 2));
        });
    }

    // =====================================================
    // DEFAULT EXECUTION PIPELINE TESTS
    // =====================================================

    @Test
    void testDefaultExecutionPipeline_EmptyStages_ShadowMode() {
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(Collections.emptyList());

        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("SHADOW", result.getStatus());
        assertTrue(result.getCompletedStages().isEmpty());
    }

    @Test
    void testDefaultExecutionPipeline_SingleStage() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getCompletedStages().size());
        assertTrue(result.getCompletedStages().contains("Stage1"));
        assertTrue(result.getProcessingTime() >= 0);
    }

    @Test
    void testDefaultExecutionPipeline_MultipleStages() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new TestExecutionStage("Stage2", 2),
                new TestExecutionStage("Stage3", 3)
        );

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals(3, result.getCompletedStages().size());
        assertTrue(result.getProcessingTime() >= 0);
    }

    @Test
    void testDefaultExecutionPipeline_StageOrdering() {
        List<ExecutionStage> stages = new ArrayList<>();
        stages.add(new TestExecutionStage("Stage3", 3));
        stages.add(new TestExecutionStage("Stage1", 1));
        stages.add(new TestExecutionStage("Stage2", 2));

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        List<ExecutionStage> orderedStages = pipeline.getStages();

        assertEquals(3, orderedStages.size());
        assertEquals("Stage1", orderedStages.get(0).getDescriptor().getStageName());
        assertEquals("Stage2", orderedStages.get(1).getDescriptor().getStageName());
        assertEquals("Stage3", orderedStages.get(2).getDescriptor().getStageName());
    }

    @Test
    void testDefaultExecutionPipeline_DuplicatePriorities() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new TestExecutionStage("Stage2", 1) // Duplicate priority
        );

        assertThrows(IllegalStateException.class, () -> {
            new DefaultExecutionPipeline(stages);
        });
    }

    @Test
    void testDefaultExecutionPipeline_NullStages() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultExecutionPipeline(null);
        });
    }

    @Test
    void testDefaultExecutionPipeline_Immutability() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        // Verify stages list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            pipeline.getStages().add(new TestExecutionStage("Stage2", 2));
        });
    }

    // =====================================================
    // EXECUTION STATE TESTS
    // =====================================================

    @Test
    void testPipelineExecutionState_VisitedAndCompletedStages() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new TestExecutionStage("Stage2", 2),
                new ShortCircuitStage("Stage3", 3)
        );

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        // Verify execution history is recorded
        assertFalse(result.isSuccess());
        assertEquals("SHORT_CIRCUIT", result.getStatus());
        assertEquals(2, result.getCompletedStages().size());
        assertTrue(result.getCompletedStages().contains("Stage1"));
        assertTrue(result.getCompletedStages().contains("Stage2"));
    }

    @Test
    void testPipelineExecutionState_Timing() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.getProcessingTime() >= 0);
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().getCustomValues().containsKey("duration"));
    }

    @Test
    void testPipelineExecutionState_Freeze() {
        List<ExecutionStage> stages = List.of(
                new TestExecutionStage("Stage1", 1),
                new TestExecutionStage("Stage2", 2)
        );

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = PipelineContext.builder().build();
        PipelineResult result = pipeline.execute(context);

        // Verify PipelineResult is immutable snapshot
        assertTrue(result.isSuccess());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(2, result.getCompletedStages().size());
        assertTrue(result.getProcessingTime() >= 0);
    }

    @Test
    void testPipelineExecutionState_StateIsolation() {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        // Execute pipeline twice
        PipelineContext context1 = PipelineContext.builder().build();
        PipelineResult result1 = pipeline.execute(context1);

        PipelineContext context2 = PipelineContext.builder().build();
        PipelineResult result2 = pipeline.execute(context2);

        // Verify results are independent
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertNotEquals(result1.getResultId(), result2.getResultId());
    }

    @Test
    void testPipelineExecutionState_ThreadSafety() throws InterruptedException {
        List<ExecutionStage> stages = List.of(new TestExecutionStage("Stage1", 1));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];
        String[] resultIds = new String[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                PipelineContext context = PipelineContext.builder().build();
                PipelineResult result = pipeline.execute(context);
                results[index] = result.isSuccess();
                resultIds[index] = result.getResultId();
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all threads succeeded
        for (boolean result : results) {
            assertTrue(result, "All threads should complete successfully");
        }

        // Verify all result IDs are unique (state isolation)
        for (int i = 0; i < threadCount; i++) {
            for (int j = i + 1; j < threadCount; j++) {
                assertNotEquals(resultIds[i], resultIds[j],
                        "Result IDs should be unique: " + resultIds[i] + " == " + resultIds[j]);
            }
        }
    }

    // =====================================================
    // PERFORMANCE TESTS
    // =====================================================

    @Test
    void testPipelinePerformance() {
        // Create 10 stages
        List<ExecutionStage> stages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            stages.add(new TestExecutionStage("Stage" + i, i));
        }

        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineContext context = PipelineContext.builder().build();

        long startTime = System.nanoTime();
        PipelineResult result = pipeline.execute(context);
        long endTime = System.nanoTime();

        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        assertTrue(result.isSuccess());
        assertEquals(10, result.getCompletedStages().size());
        assertTrue(processingTime < 100, "Pipeline should complete in < 100ms, but took " + processingTime + "ms");
    }

    // =====================================================
    // HELPER CLASSES
    // =====================================================

    /**
     * Test implementation of ExecutionStage.
     */
    private static class TestExecutionStage implements ExecutionStage {
        private final String stageName;
        private final int priority;

        TestExecutionStage(String stageName, int priority) {
            this.stageName = stageName;
            this.priority = priority;
        }

        @Override
        public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
            state.markNextStageInvoked();
            return chain.next(context, state);
        }

        @Override
        public PipelineStageDescriptor getDescriptor() {
            return PipelineStageDescriptor.builder()
                    .stageName(stageName)
                    .priority(priority)
                    .build();
        }
    }

    /**
     * Test stage that short-circuits the pipeline.
     */
    private static class ShortCircuitStage implements ExecutionStage {
        private final String stageName;
        private final int priority;

        ShortCircuitStage(String stageName, int priority) {
            this.stageName = stageName;
            this.priority = priority;
        }

        @Override
        public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
            return PipelineResult.builder()
                    .success(false)
                    .status("SHORT_CIRCUIT")
                    .addMessage("Short-circuited at " + stageName)
                    .build();
        }

        @Override
        public PipelineStageDescriptor getDescriptor() {
            return PipelineStageDescriptor.builder()
                    .stageName(stageName)
                    .priority(priority)
                    .build();
        }
    }
}