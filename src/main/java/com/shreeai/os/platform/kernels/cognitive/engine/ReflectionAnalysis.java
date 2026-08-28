package com.shreeai.os.platform.kernels.cognitive.engine;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>ReflectionAnalysis</b>
 *
 * <p>The result of post-execution reflection: a verdict, a quality score,
 * actionable lessons, and whether the lessons are worth persisting to memory.</p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 *
 * @param verdict      the outcome verdict
 * @param score        the quality score (0.0-1.0)
 * @param lessons      actionable lessons extracted from the execution
 * @param summary      human-readable reflection summary
 * @param memoryWorthy whether lessons should be persisted to memory
 * @param retryAdvised whether a retry with adjusted planning is advised
 * @param evaluatedAt  when the reflection was produced
 */
public record ReflectionAnalysis(
        ReflectionVerdict verdict,
        double score,
        List<String> lessons,
        String summary,
        boolean memoryWorthy,
        boolean retryAdvised,
        Instant evaluatedAt) {

    /**
     * Creates a ReflectionAnalysis with validation and defensive copying.
     *
     * @throws NullPointerException if verdict, lessons, summary or evaluatedAt is null
     */
    public ReflectionAnalysis {
        Objects.requireNonNull(verdict, "verdict must not be null");
        Objects.requireNonNull(lessons, "lessons must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");
        lessons = List.copyOf(lessons);
        score = Math.max(0.0, Math.min(1.0, score));
    }
}