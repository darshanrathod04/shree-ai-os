package com.shreeai.os.platform.intelligence.reflection;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionVerdict;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * <b>AdaptiveReflectionEngine</b>
 *
 * <p>V3: Extends the deterministic {@link DefaultReflectionEngine} with an
 * adaptive calibration layer. It observes the historical relationship
 * between predicted conclusion confidence and actual outcome correctness,
 * then adjusts reflection thresholds so the engine self-tunes over time.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Delegates base scoring/verdict/lessons to {@link DefaultReflectionEngine}.</li>
 *   <li>Learns recent accuracy from fed-back outcomes.</li>
 *   <li>Calibrates the retry and memory-gating thresholds from that history.</li>
 *   <li>Provides a {@link #recordOutcome(double, boolean)} feedback hook so
 *       downstream stages can close the loop.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Intelligence — Reflection</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class AdaptiveReflectionEngine {

    private static final int DEFAULT_HISTORY_SIZE = 50;
    private static final double BASE_RETRY_THRESHOLD = 0.40;
    private static final double BASE_MEMORY_THRESHOLD = 0.50;
    private static final double CALIBRATION_MAGNITUDE = 0.15;

    private final DefaultReflectionEngine delegate;
    private final int maxHistorySize;
    private final Deque<Boolean> outcomeHistory = new ArrayDeque<>();

    /**
     * Creates an adaptive engine with default history size.
     */
    public AdaptiveReflectionEngine() {
        this(DEFAULT_HISTORY_SIZE);
    }

    /**
     * Creates an adaptive engine with a specified history size.
     *
     * @param maxHistorySize maximum number of fed-back outcomes retained
     */
    public AdaptiveReflectionEngine(int maxHistorySize) {
        this(new DefaultReflectionEngine(), maxHistorySize);
    }

    /**
     * Creates an adaptive engine wrapping the given delegate.
     *
     * @param delegate        base reflection engine (never null)
     * @param maxHistorySize  maximum number of fed-back outcomes retained
     */
    public AdaptiveReflectionEngine(DefaultReflectionEngine delegate, int maxHistorySize) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.maxHistorySize = Math.max(1, maxHistorySize);
    }

    /**
     * Reflects on a completed execution, applying adaptive calibration.
     *
     * @param input the reflection input (never null)
     * @return the adaptively-calibrated analysis (never null)
     */
    public ReflectionAnalysis reflect(ReflectionInput input) {
        Objects.requireNonNull(input, "input must not be null");

        ReflectionAnalysis base = delegate.reflect(input);
        double calibratedScore = clamp(base.score() + calibrationBias());
        ReflectionVerdict verdict = delegate.score(input) >= retryThreshold()
                ? base.verdict()
                : relaxVerdict(base.verdict());

        // Retry less aggressively when the engine has been accurate recently.
        boolean retryAdvised = base.verdict() == ReflectionVerdict.FAILURE
                && verdict != ReflectionVerdict.FAILURE;
        boolean memoryWorthy = base.memoryWorthy()
                && base.score() >= memoryThreshold();

        String summary = base.summary() + " [adaptive]";

        return new ReflectionAnalysis(
                verdict,
                calibratedScore,
                base.lessons(),
                summary,
                memoryWorthy,
                retryAdvised,
                Instant.now());
    }

    /**
     * Feeds back an observed outcome for adaptive learning.
     *
     * @param predictedConfidence the confidence that was predicted (0.0-1.0)
     * @param correct             whether the outcome was actually correct
     */
    public synchronized void recordOutcome(double predictedConfidence, boolean correct) {
        if (correct) {
            outcomeHistory.addLast(true);
            if (outcomeHistory.size() > maxHistorySize) {
                outcomeHistory.removeFirst();
            }
        } else {
            outcomeHistory.addLast(false);
            if (outcomeHistory.size() > maxHistorySize) {
                outcomeHistory.removeFirst();
            }
        }
    }

    /**
     * @return the number of recorded outcomes
     */
    public synchronized int historySize() {
        return outcomeHistory.size();
    }

    /**
     * @return the recent outcome accuracy (0.0-1.0), or 0.5 when no history
     */
    public synchronized double recentAccuracy() {
        if (outcomeHistory.isEmpty()) {
            return 0.5;
        }
        long successes = outcomeHistory.stream().filter(Boolean::booleanValue).count();
        return (double) successes / outcomeHistory.size();
    }

    /**
     * @return the adaptively-tuned retry threshold in [0.0, 1.0]
     */
    public synchronized double retryThreshold() {
        double accuracy = recentAccuracy();
        // High accuracy -> we trust results -> retry less (lower threshold).
        double bias = (accuracy - 0.5) * CALIBRATION_MAGNITUDE;
        return clamp(BASE_RETRY_THRESHOLD - bias);
    }

    /**
     * @return the adaptively-tuned memory-gating threshold in [0.0, 1.0]
     */
    public synchronized double memoryThreshold() {
        double accuracy = recentAccuracy();
        // High accuracy -> reinforce success -> persist more aggressively.
        double bias = (accuracy - 0.5) * CALIBRATION_MAGNITUDE;
        return clamp(BASE_MEMORY_THRESHOLD - bias);
    }

    /** @return the maximum retained history size */
    public int maxHistorySize() {
        return maxHistorySize;
    }

    /**
     * Clears the outcome history.
     */
    public synchronized void reset() {
        outcomeHistory.clear();
    }

    // ==========================================================
    // Internal
    // ==========================================================

    /**
     * Computes a small confidence bias from recent accuracy so that a
     * well-calibrated engine nudges scores up and a poorly-calibrated one
     * nudges them down.
     *
     * @return bias in [-CALIBRATION_MAGNITUDE, CALIBRATION_MAGNITUDE]
     */
    private synchronized double calibrationBias() {
        return (recentAccuracy() - 0.5) * CALIBRATION_MAGNITUDE;
    }

    private ReflectionVerdict relaxVerdict(ReflectionVerdict verdict) {
        if (verdict == ReflectionVerdict.FAILURE) {
            return ReflectionVerdict.PARTIAL;
        }
        return verdict;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
