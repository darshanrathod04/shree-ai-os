package com.shreeai.os.platform.sdk;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>ReflectionReport</b>
 *
 * <p>SDK-facing record returned by {@link ReflectionSDK#reflect(String)}.
 * Captures the outcome of post-execution reflection: verdict, quality score,
 * importance score, extracted lessons, and retry advice.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param executionId    the execution identifier reflected upon
 * @param verdict       the outcome verdict (SUCCESS / PARTIAL / FAILURE)
 * @param score         the quality score (0.0–1.0)
 * @param importanceScore the importance score (0–100)
 * @param lessons       actionable lessons extracted from the execution
 * @param summary       human-readable reflection summary
 * @param memoryWorthy  whether lessons should be persisted to memory
 * @param retryAdvised  whether a retry with adjusted planning is advised
 * @param evaluatedAt   when the reflection was produced
 */
public record ReflectionReport(
        String executionId,
        String verdict,
        double score,
        int importanceScore,
        List<String> lessons,
        String summary,
        boolean memoryWorthy,
        boolean retryAdvised,
        Instant evaluatedAt
) {
    public ReflectionReport {
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(verdict, "verdict must not be null");
        Objects.requireNonNull(lessons, "lessons must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");
        lessons = List.copyOf(lessons);
        score = Math.max(0.0, Math.min(1.0, score));
        importanceScore = Math.max(0, Math.min(100, importanceScore));
    }

    /** Returns true if the verdict is SUCCESS. */
    public boolean isSuccess() {
        return "SUCCESS".equals(verdict);
    }

    /** Returns true if the verdict is PARTIAL. */
    public boolean isPartial() {
        return "PARTIAL".equals(verdict);
    }

    /** Returns true if the verdict is FAILURE. */
    public boolean isFailure() {
        return "FAILURE".equals(verdict);
    }
}
