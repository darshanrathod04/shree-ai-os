package com.shreeai.os.platform.runtime.reflection;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <b>ReflectionAnalyticsService</b>
 *
 * <p>Aggregates reflection history into actionable analytics: success rates,
 * average scores, lesson counts, root cause frequency, and trend detection.</p>
 *
 * <p>All computations are deterministic and read-only.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ReflectionAnalyticsService {

    private final ReflectionRepository repository;

    /**
     * Creates the analytics service.
     *
     * @param repository the reflection repository (never null)
     */
    public ReflectionAnalyticsService(ReflectionRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Computes analytics summary for a tenant.
     *
     * @param tenantId the tenant identifier
     * @param window   number of recent records to analyze
     * @return the analytics summary
     */
    public ReflectionAnalyticsSummary analyze(String tenantId, int window) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        List<ReflectionHistory> records = repository.findByTenantId(tenantId, window);

        if (records.isEmpty()) {
            return ReflectionAnalyticsSummary.empty(tenantId);
        }

        long total = records.size();
        long successes = records.stream().filter(r -> "SUCCESS".equals(r.verdict())).count();
        long partials = records.stream().filter(r -> "PARTIAL".equals(r.verdict())).count();
        long failures = records.stream().filter(r -> "FAILURE".equals(r.verdict())).count();

        DoubleSummaryStatistics scoreStats = records.stream()
                .mapToDouble(ReflectionHistory::score)
                .summaryStatistics();

        DoubleSummaryStatistics importanceStats = records.stream()
                .mapToDouble(r -> r.importanceScore())
                .summaryStatistics();

        long totalLessons = records.stream()
                .mapToLong(r -> r.lessons().size())
                .sum();

        Map<String, Long> rootCauseFrequency = records.stream()
                .filter(r -> r.rootCause() != null && !r.rootCause().isBlank())
                .collect(Collectors.groupingBy(
                        ReflectionHistory::rootCause,
                        Collectors.counting()
                ));

        double successRate = total > 0 ? successes / (double) total : 0.0;

        return new ReflectionAnalyticsSummary(
                tenantId,
                total,
                successes,
                partials,
                failures,
                successRate,
                scoreStats.getAverage(),
                importanceStats.getAverage(),
                totalLessons,
                rootCauseFrequency,
                Instant.now()
        );
    }

    /**
     * Immutable analytics summary for a tenant.
     *
     * @param tenantId           the tenant identifier
     * @param totalRecords       total records analyzed
     * @param successCount       number of SUCCESS records
     * @param partialCount       number of PARTIAL records
     * @param failureCount       number of FAILURE records
     * @param successRate        success rate (0.0–1.0)
     * @param averageScore       average quality score
     * @param averageImportance  average importance score
     * @param totalLessons       total lessons extracted
     * @param rootCauseFrequency frequency map of root causes
     * @param computedAt         when the summary was computed
     */
    public record ReflectionAnalyticsSummary(
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
        public ReflectionAnalyticsSummary {
            Objects.requireNonNull(tenantId, "tenantId must not be null");
            Objects.requireNonNull(rootCauseFrequency, "rootCauseFrequency must not be null");
            Objects.requireNonNull(computedAt, "computedAt must not be null");
            rootCauseFrequency = Map.copyOf(rootCauseFrequency);
        }

        public static ReflectionAnalyticsSummary empty(String tenantId) {
            return new ReflectionAnalyticsSummary(
                    tenantId, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0, Map.of(), Instant.now()
            );
        }
    }
}