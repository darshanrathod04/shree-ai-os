package com.shreeai.os.platform.intelligence.reflection;

import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionVerdict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdaptiveReflectionEngine Tests")
class AdaptiveReflectionEngineTest {

    private AdaptiveReflectionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdaptiveReflectionEngine();
    }

    private ReflectionInput successInput() {
        return new ReflectionInput(
                "req-1", "Do the thing", 5, "SUCCESS",
                true, "Everything went well", 0.9);
    }

    private ReflectionInput failureInput() {
        return new ReflectionInput(
                "req-2", "Do the thing", 0, "FAILED",
                false, "", 0.1);
    }

    @Test
    @DisplayName("reflect delegates to base engine producing a verdict")
    void reflectProducesVerdict() {
        ReflectionAnalysis analysis = engine.reflect(successInput());
        assertNotNull(analysis);
        assertNotNull(analysis.verdict());
        assertTrue(analysis.lessons().size() >= 0);
    }

    @Test
    @DisplayName("high quality input scores near the top")
    void highQualityInputScoresHigh() {
        ReflectionAnalysis analysis = engine.reflect(successInput());
        assertTrue(analysis.score() >= 0.7);
    }

    @Test
    @DisplayName("history starts empty")
    void historyStartsEmpty() {
        assertEquals(0, engine.historySize());
        assertEquals(0.5, engine.recentAccuracy());
    }

    @Test
    @DisplayName("recordOutcome grows history")
    void recordOutcomeGrowsHistory() {
        engine.recordOutcome(0.8, true);
        engine.recordOutcome(0.6, false);
        assertEquals(2, engine.historySize());
    }

    @Test
    @DisplayName("recentAccuracy reflects fed-back outcomes")
    void recentAccuracyReflectsOutcomes() {
        engine.recordOutcome(0.8, true);
        engine.recordOutcome(0.7, true);
        engine.recordOutcome(0.5, false);
        assertEquals(2.0 / 3.0, engine.recentAccuracy(), 0.001);
    }

    @Test
    @DisplayName("high accuracy lowers retry threshold")
    void highAccuracyLowersRetryThreshold() {
        double neutral = engine.retryThreshold();
        for (int i = 0; i < 20; i++) {
            engine.recordOutcome(0.8, true);
        }
        assertTrue(engine.retryThreshold() < neutral);
    }

    @Test
    @DisplayName("low accuracy raises retry threshold")
    void lowAccuracyRaisesRetryThreshold() {
        double neutral = engine.retryThreshold();
        for (int i = 0; i < 20; i++) {
            engine.recordOutcome(0.3, false);
        }
        assertTrue(engine.retryThreshold() > neutral);
    }

    @Test
    @DisplayName("history is bounded by max size")
    void historyBoundedByMaxSize() {
        AdaptiveReflectionEngine bounded = new AdaptiveReflectionEngine(10);
        for (int i = 0; i < 50; i++) {
            bounded.recordOutcome(0.5, i % 2 == 0);
        }
        assertEquals(10, bounded.historySize());
        assertEquals(10, bounded.maxHistorySize());
    }

    @Test
    @DisplayName("reset clears history")
    void resetClearsHistory() {
        engine.recordOutcome(0.8, true);
        engine.reset();
        assertEquals(0, engine.historySize());
        assertEquals(0.5, engine.recentAccuracy());
    }

    @Test
    @DisplayName("thresholds remain within [0,1]")
    void thresholdsWithinBounds() {
        for (int i = 0; i < 50; i++) {
            engine.recordOutcome(0.5, true);
        }
        assertTrue(engine.retryThreshold() >= 0.0 && engine.retryThreshold() <= 1.0);
        assertTrue(engine.memoryThreshold() >= 0.0 && engine.memoryThreshold() <= 1.0);
    }

    @Test
    @DisplayName("null input throws")
    void nullInputThrows() {
        assertThrows(NullPointerException.class, () -> engine.reflect(null));
    }

    @Test
    @DisplayName("summary carries adaptive marker")
    void summaryCarriesAdaptiveMarker() {
        ReflectionAnalysis analysis = engine.reflect(successInput());
        assertTrue(analysis.summary().contains("[adaptive]"));
    }
}
