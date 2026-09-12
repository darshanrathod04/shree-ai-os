package com.shreeai.os.platform.runtime.pipeline;

import com.shreeai.os.platform.runtime.pipeline.stages.ReflectionStage;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the P0.1 Reflection Loop Execution.
 *
 * <p>Verifies that {@code DefaultExecutionPipeline} re-executes ONLY the
 * cognitive segment (Reasoning → Inference → Planning → Reflection) when
 * {@code ReflectionStage} requests another reasoning pass, that retrieval
 * results (Memory / Knowledge) are preserved and reused, and that an
 * infinite reflection loop is impossible.</p>
 */
class ReflectionLoopExecutionTest {

    // ─── Test doubles ──────────────────────────────────────────────────────

    /**
     * Base test stage that counts invocations and exposes a descriptor.
     */
    private abstract static class CountingStage implements ExecutionStage {

        private final String stageName;
        private final int priority;
        private final AtomicInteger calls = new AtomicInteger();

        CountingStage(String stageName, int priority) {
            this.stageName = stageName;
            this.priority = priority;
        }

        /** Number of times this stage was invoked. */
        int calls() {
            return calls.get();
        }

        @Override
        public final PipelineResult process(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            calls.incrementAndGet();
            return execute(context, chain, state);
        }

        protected abstract PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state);

        @Override
        public PipelineStageDescriptor getDescriptor() {
            return PipelineStageDescriptor.builder()
                    .stageName(stageName)
                    .priority(priority)
                    .enabled(true)
                    .version("1.0")
                    .description("test stub for " + stageName)
                    .build();
        }
    }

    /**
     * Retrieval stub that stores a sentinel in state metadata exactly once.
     */
    private static final class MemoryRecallStub extends CountingStage {

        static final String SENTINEL = "memory-sentinel";

        MemoryRecallStub() {
            super("MemoryRecall", 3);
        }

        @Override
        protected PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            state.addMetadata("rankedMemories", SENTINEL);
            return chain.next(context, state);
        }
    }

    /**
     * Reasoning stub which upgrades the execution metadata to a high-quality
     * state on every call at or after {@code upgradeOnCall}. A value of 0
     * never upgrades (reflection always FAILS); 1 upgrades immediately
     * (reflection always SUCCEEDS); 2 upgrades only on the second pass
     * (quality improves across a loop).
     */
    private static final class ReasoningStub extends CountingStage {

        private final int upgradeOnCall;

        ReasoningStub(int upgradeOnCall) {
            super("Reasoning", 5);
            this.upgradeOnCall = upgradeOnCall;
        }

        @Override
        protected PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            if (upgradeOnCall > 0 && calls() >= upgradeOnCall) {
                state.addMetadata("executionCompleted", true);
                state.addMetadata("executionStatus", "COMPLETED");
                state.addMetadata("planStepCount", 1);
                state.addMetadata("knowledgeSummary", "Java is an object oriented language");
                state.addMetadata("knowledgeConfidence", 0.9);
            }
            return chain.next(context, state);
        }
    }

    /**
     * No-op passthrough stage used for Inference / Planning.
     */
    private static final class PassThroughStub extends CountingStage {

        PassThroughStub(String stageName, int priority) {
            super(stageName, priority);
        }

        @Override
        protected PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            return chain.next(context, state);
        }
    }

    /**
     * Terminal response stub. Called at most once per pipeline execution.
     */
    private static final class ResponseStub extends CountingStage {

        ResponseStub() {
            super("Response", 10);
        }

        @Override
        protected PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            return chain.next(context, state);
        }
    }

    /**
     * Hostile reasoning stub that ignores the reflection iteration bound and
     * always requests another reasoning pass. Used to prove the pipeline's
     * own defensive loop guard cannot be bypassed.
     */
    private static final class FlakyReasoningStub extends CountingStage {

        FlakyReasoningStub() {
            super("Reasoning", 5);
        }

        @Override
        protected PipelineResult execute(
                PipelineContext context,
                ExecutionChain chain,
                PipelineExecutionState state) {
            state.markNextStageInvoked();
            state.setRequiresReReason(true);
            return PipelineResult.builder()
                    .success(true)
                    .status("REFLECTION_LOOP_REQUESTED")
                    .addMessage("flaky stage requested re-reason")
                    .build();
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private List<ExecutionStage> buildPipelineStages(int reasoningUpgradeOnCall) {
        return List.of(
                new MemoryRecallStub(),
                new ReasoningStub(reasoningUpgradeOnCall),
                new PassThroughStub("Inference", 6),
                new PassThroughStub("Planning", 7),
                new ReflectionStage(null),
                new ResponseStub());
    }

    private PipelineContext newContext() {
        return PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }

    private static PipelineExecutionState stateOf(PipelineResult result) {
        PipelineExecutionState state = result.getExecutionState();
        assertNotNull(state, "execute must attach the execution state");
        return state;
    }
// ─── State unit tests ──────────────────────────────────────────────────

    @Test
    void stateDefaultsToZeroIterations() {
        PipelineExecutionState state = new PipelineExecutionState(List.of());

        assertEquals(0, state.getReflectionIteration());
        assertEquals(2, state.getMaxReflectionIterations());
        assertFalse(state.requiresReReason());
        assertTrue(state.getPreviousQualityScores().isEmpty());
    }

    @Test
    void stateTracksReflectionIterationAndScores() {
        PipelineExecutionState state = new PipelineExecutionState(List.of());

        state.recordQualityScore(0.25);
        state.incrementReflectionIteration();
        state.recordQualityScore(0.97);
        state.incrementReflectionIteration();

        assertEquals(2, state.getReflectionIteration());
        assertEquals(List.of(0.25, 0.97), state.getPreviousQualityScores());

        state.setRequiresReReason(true);
        assertTrue(state.requiresReReason());
        state.clearRequiresReReason();
        assertFalse(state.requiresReReason());

        state.resetReflectionIteration();
        assertEquals(0, state.getReflectionIteration());
        assertTrue(state.getPreviousQualityScores().isEmpty());
        assertFalse(state.requiresReReason());
    }

    // ─── Scenario A: no loop ───────────────────────────────────────────────

    @Test
    void noLoopWhenReflectionDoesNotAdviseRetry() {
        List<ExecutionStage> stages = buildPipelineStages(1);
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineResult result = pipeline.execute(newContext());

        assertNotNull(result);
        assertTrue(result.isSuccess(), "pipeline should succeed without a loop");
        assertEquals("COMPLETED", result.getStatus());

        PipelineExecutionState state = stateOf(result);
        assertEquals(1, state.getReflectionIteration(),
                "reflection should have run exactly once");
        assertFalse(state.requiresReReason(), "no re-reason request expected");
        assertEquals(0.97, state.getCognitiveState().reflection().score(), 0.0001,
                "reflection should evaluate the upgraded metadata immediately");

        assertEquals(1, reasoningOf(stages).calls(), "reasoning must run exactly once");
        assertEquals(1, responseOf(stages).calls(), "response must run exactly once");
    }
// ─── Scenario B: exactly one loop, quality improves → exit ─────────────

    @Test
    void singleRewindWhenQualityImproves() {
        List<ExecutionStage> stages = buildPipelineStages(2);
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineResult result = pipeline.execute(newContext());

        assertNotNull(result);
        assertTrue(result.isSuccess(), "pipeline should succeed after one rewind");
        assertEquals("COMPLETED", result.getStatus());

        PipelineExecutionState state = stateOf(result);
        assertEquals(2, state.getReflectionIteration(),
                "reflection should have run twice (initial + one rewind)");
        assertEquals(List.of(0.0, 0.97), state.getPreviousQualityScores(),
                "quality should improve from the first to the second pass");
        assertEquals(0.97, state.getCognitiveState().reflection().score(), 0.0001,
                "final response must use the second reasoning result");
        assertFalse(state.requiresReReason());

        assertEquals(2, reasoningOf(stages).calls(), "reasoning must run twice");
        assertEquals(1, responseOf(stages).calls(), "response must run exactly once");
    }

    // ─── Scenario C: reflection always TRUE → bounded termination ──────────

    @Test
    void terminatesSafelyAfterMaxIterations() {
        List<ExecutionStage> stages = buildPipelineStages(0);
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        // If an infinite loop existed, this call would never return.
        PipelineResult result = pipeline.execute(newContext());

        assertNotNull(result);
        assertTrue(result.isSuccess(), "pipeline should terminate successfully");
        assertEquals("COMPLETED", result.getStatus());

        PipelineExecutionState state = stateOf(result);
        assertEquals(2, state.getReflectionIteration(),
                "reflection must stop after the max iterations (2)");
        assertEquals(2, state.getPreviousQualityScores().size());
        assertFalse(state.requiresReReason());

        assertEquals(2, reasoningOf(stages).calls(),
                "reasoning must run at most twice — never a third time");
        assertEquals(1, responseOf(stages).calls(),
                "response must still be produced after the bounded loop");
    }

    // ─── Memory / Knowledge preservation across the loop ───────────────────

    @Test
    void preservesRetrievalResultsAcrossRewind() {
        List<ExecutionStage> stages = buildPipelineStages(2);
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineResult result = pipeline.execute(newContext());
        assertTrue(result.isSuccess());

        PipelineExecutionState state = stateOf(result);
        assertEquals(MemoryRecallStub.SENTINEL, state.getMetadata().get("rankedMemories"),
                "retrieval results must be preserved across the reflection loop");
        assertEquals(1, memoryOf(stages).calls(),
                "MemoryRecall must run exactly once — it is NOT re-executed");
    }

    // ─── Defense in depth: a stage that ignores the bound ─────────────────

    @Test
    void pipelineStopsEvenIfStageNeverClearsLoopRequest() {
        FlakyReasoningStub flaky = new FlakyReasoningStub();
        ResponseStub response = new ResponseStub();
        List<ExecutionStage> stages = List.of(
                flaky,
                new PassThroughStub("Inference", 6),
                new PassThroughStub("Planning", 7),
                new PassThroughStub("Reflection", 9),
                response);
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineResult result = pipeline.execute(newContext());

        assertNotNull(result, "pipeline must terminate, never hang");

        // The rogue stage requests a re-reason pass on every invocation and
        // never lets the main chain reach the remaining stages, so freeze()
        // honestly reports TERMINATED rather than COMPLETED. The guarantees
        // that matter are: the loop is BOUNDED, the downstream response is
        // still produced, and no stage is allowed to loop forever.
        assertEquals(3, flaky.calls(),
                "the pipeline guard caps rewinds at max iterations "
                        + "(1 main + 2 capped rewinds)");
        assertEquals(1, response.calls(),
                "the downstream response must still run after the capped loop");
        assertFalse(stateOf(result).requiresReReason());
    }

    // ─── Stage lookups ─────────────────────────────────────────────────────

    private static ReasoningStub reasoningOf(List<ExecutionStage> stages) {
        return stages.stream()
                .filter(stage -> stage instanceof ReasoningStub)
                .map(stage -> (ReasoningStub) stage)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ReasoningStub missing"));
    }

    private static MemoryRecallStub memoryOf(List<ExecutionStage> stages) {
        return stages.stream()
                .filter(stage -> stage instanceof MemoryRecallStub)
                .map(stage -> (MemoryRecallStub) stage)
                .findFirst()
                .orElseThrow(() -> new AssertionError("MemoryRecallStub missing"));
    }

    private static ResponseStub responseOf(List<ExecutionStage> stages) {
        return stages.stream()
                .filter(stage -> stage instanceof ResponseStub)
                .map(stage -> (ResponseStub) stage)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ResponseStub missing"));
    }
}