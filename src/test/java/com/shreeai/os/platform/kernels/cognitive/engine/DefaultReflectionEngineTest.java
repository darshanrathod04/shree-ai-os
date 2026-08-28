package com.shreeai.os.platform.kernels.cognitive.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the {@link DefaultReflectionEngine}. */
class DefaultReflectionEngineTest {

    private final DefaultReflectionEngine engine = new DefaultReflectionEngine();

    @Test
    void successfulExecutionScoresHigh() {
        ReflectionInput input = new ReflectionInput(
                "req-1", "search knowledge", 3, "COMPLETED", true, "grounded summary", 0.9);

        ReflectionAnalysis analysis = engine.reflect(input);

        assertEquals(ReflectionVerdict.SUCCESS, analysis.verdict());
        assertTrue(analysis.score() >= 0.75);
        assertFalse(analysis.retryAdvised());
        assertTrue(analysis.memoryWorthy());
        assertFalse(analysis.lessons().isEmpty());
    }

    @Test
    void failedExecutionScoresLowAndAdvisesRetry() {
        ReflectionInput input = new ReflectionInput(
                "req-2", "risky operation", 0, "FAILED", false, "", 0.2);

        ReflectionAnalysis analysis = engine.reflect(input);

        assertEquals(ReflectionVerdict.FAILURE, analysis.verdict());
        assertTrue(analysis.score() < 0.4);
        assertTrue(analysis.retryAdvised());
        assertTrue(analysis.lessons().stream().anyMatch(l -> l.contains("did not complete")));
        assertTrue(analysis.lessons().stream().anyMatch(l -> l.contains("No knowledge")));
    }

    @Test
    void partialExecutionFallsInBetween() {
        ReflectionInput input = new ReflectionInput(
                "req-3", "some request", 2, "COMPLETED", true, "", 0.2);

        ReflectionAnalysis analysis = engine.reflect(input);

        assertEquals(ReflectionVerdict.PARTIAL, analysis.verdict());
        assertTrue(analysis.score() >= 0.4 && analysis.score() < 0.75);
    }

    @Test
    void largePlansGenerateDecompositionLesson() {
        ReflectionInput input = new ReflectionInput(
                "req-4", "big plan", 12, "COMPLETED", true, "summary", 1.0);

        ReflectionAnalysis analysis = engine.reflect(input);

        assertTrue(analysis.lessons().stream().anyMatch(l -> l.contains("decomposing large plans")));
    }

    @Test
    void analysisClampsScore() {
        ReflectionInput input = new ReflectionInput(
                "req-5", "edge", 1, "COMPLETED", true, "s", 42.0);

        assertTrue(engine.score(input) <= 1.0);
    }
}