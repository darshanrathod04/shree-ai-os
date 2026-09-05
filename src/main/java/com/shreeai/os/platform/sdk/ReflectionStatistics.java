package com.shreeai.os.platform.sdk;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ReflectionStatistics</b>
 *
 * <p>SDK-facing analytics summary returned by {@link ReflectionSDK#statistics(String, int)}.
 * Aggregates reflection history into success rates, score averages, lesson counts,
 * and root cause frequency for a tenant.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param tenantId           the tenant identifier
 * @param totalRecords      total reflection records analyzed
 * @param successCount      number of SUCCESS records
 * @param partialCount      number of PARTIAL records
 * @param failureCount      number of FAILURE records
 * @param successRate       success rate (0.0–1.0)
 * @param averageScore      average quality score across records
 * @param averageImportance average importance score across records
 * @param totalLessons     total lessons extracted across all records
 * @param rootCauseFrequency frequency map of root causes (root cause → count)
 * @param computedAt        when the summary was computed
 */
public record ReflectionStatistics(
        String tenantId,
        long totalRecords,
        long successCount,
        long partialCount,
        long failureCount,
        double successRate,
        double averageScore,
        double averageImportance,
        long totalLessons,
        Map<String, Long> rootCauseFrequency,
        Instant computedAt
) {
    public ReflectionStatistics {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(rootCauseFrequency, "rootCauseFrequency must not be null");
        Objects.requireNonNull(computedAt, "computedAt must not be null");
        rootCauseFrequency = Map.copyOf(rootCauseFrequency);
        successRate = Math.max(0.0, Math.min(1.0, successRate));
        averageScore = Math.max(0.0, Math.min(1.0, averageScore));
        averageImportance = Math.max(0.0, Math.min(100.0, averageImportance));
    }

    /** Returns true if there are no reflection records for this tenant. */
    public boolean isEmpty() {
        return totalRecords == 0;
    }
}
