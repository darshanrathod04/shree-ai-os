package com.shreeai.os.platform.runtime.reflection;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <b>ReflectionHistory</b>
 *
 * <p>Immutable, tenant-aware domain model representing a persisted reflection
 * record. Each entry captures the outcome of a post-execution reflection
 * including the verdict, score, importance score, extracted lessons, root
 * cause analysis, and the tenant/organization context for multi-tenant
 * isolation.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param tenantId         the tenant identifier (never null)
 * @param organizationId   the organization identifier (never null)
 * @param executionId      the execution identifier (never null)
 * @param requestId        the original request identifier (never null)
 * @param verdict          the reflection verdict (never null)
 * @param score            the quality score (0.0–1.0)
 * @param importanceScore  the importance score (0–100)
 * @param lessons          extracted lessons (never null, immutable)
 * @param rootCause        root cause when outcome was FAILURE or PARTIAL (may be null)
 * @param retryAdvised     whether a retry was advised
 * @param evaluatedAt      when the reflection was produced (never null)
 */
public record ReflectionHistory(
        String tenantId,
        String organizationId,
        String executionId,
        String requestId,
        String verdict,
        double score,
        int importanceScore,
        List<String> lessons,
        String rootCause,
        boolean retryAdvised,
        Instant evaluatedAt
) {
    /**
     * Creates a ReflectionHistory with validation and defensive copying.
     */
    public ReflectionHistory {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(verdict, "verdict must not be null");
        Objects.requireNonNull(lessons, "lessons must not be null");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");

        score = clamp(score, 0.0, 1.0);
        importanceScore = clamp(importanceScore, 0, 100);
        lessons = Collections.unmodifiableList(List.copyOf(lessons));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}