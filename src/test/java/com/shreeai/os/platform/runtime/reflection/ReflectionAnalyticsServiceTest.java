package com.shreeai.os.platform.runtime.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReflectionAnalyticsService}.
 */
class ReflectionAnalyticsServiceTest {

    private InMemoryReflectionRepository repository;
    private ReflectionAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReflectionRepository();
        analyticsService = new ReflectionAnalyticsService(repository);
    }

    @Test
    void emptyAnalytics() {
        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("tenant-1", 50);

        assertEquals("tenant-1", summary.tenantId());
        assertEquals(0, summary.totalRecords());
        assertEquals(0.0, summary.successRate());
        assertTrue(summary.rootCauseFrequency().isEmpty());
    }

    @Test
    void computesSuccessRate() {
        repository.save(makeHistory("t-1", "e-1", "SUCCESS", 0.9, 30));
        repository.save(makeHistory("t-1", "e-2", "SUCCESS", 0.85, 40));
        repository.save(makeHistory("t-1", "e-3", "FAILURE", 0.2, 90));

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 50);

        assertEquals(3, summary.totalRecords());
        assertEquals(2, summary.successCount());
        assertEquals(1, summary.failureCount());
        assertEquals(2.0 / 3.0, summary.successRate(), 0.001);
    }

    @Test
    void computesAverageScore() {
        repository.save(makeHistory("t-1", "e-1", "SUCCESS", 0.9, 30));
        repository.save(makeHistory("t-1", "e-2", "FAILURE", 0.3, 80));

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 50);

        assertEquals(0.6, summary.averageScore(), 0.001);
        assertEquals(55.0, summary.averageImportance(), 0.001);
    }

    @Test
    void countsTotalLessons() {
        repository.save(makeHistory("t-1", "e-1", "SUCCESS", 0.9, 30, List.of("A", "B")));
        repository.save(makeHistory("t-1", "e-2", "PARTIAL", 0.5, 60, List.of("C")));

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 50);

        assertEquals(3, summary.totalLessons());
    }

    @Test
    void aggregatesRootCauses() {
        repository.save(makeHistory("t-1", "e-1", "FAILURE", 0.2, 90, "Timeout"));
        repository.save(makeHistory("t-1", "e-2", "FAILURE", 0.3, 85, "Timeout"));
        repository.save(makeHistory("t-1", "e-3", "FAILURE", 0.1, 95, "Auth error"));

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 50);

        Map<String, Long> rootCauses = summary.rootCauseFrequency();
        assertEquals(2, rootCauses.size());
        assertEquals(2L, rootCauses.get("Timeout"));
        assertEquals(1L, rootCauses.get("Auth error"));
    }

    @Test
    void respectsWindowLimit() {
        for (int i = 0; i < 20; i++) {
            repository.save(makeHistory("t-1", "e-" + i, "SUCCESS", 0.9, 30));
        }

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 5);

        assertEquals(5, summary.totalRecords());
    }

    @Test
    void analyticsIsImmutable() {
        repository.save(makeHistory("t-1", "e-1", "FAILURE", 0.2, 90, "Error"));
        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze("t-1", 50);

        assertThrows(UnsupportedOperationException.class, () ->
                summary.rootCauseFrequency().put("New", 1L));
    }

    private ReflectionHistory makeHistory(
            String tenantId, String executionId, String verdict, double score, int importance) {
        return makeHistory(tenantId, executionId, verdict, score, importance, List.of("Lesson"));
    }

    private ReflectionHistory makeHistory(
            String tenantId, String executionId, String verdict,
            double score, int importance, String rootCause) {
        return new ReflectionHistory(
                tenantId, tenantId, executionId, "req-" + executionId,
                verdict, score, importance, List.of("Lesson for " + executionId),
                rootCause, "FAILURE".equals(verdict), Instant.now()
        );
    }

    private ReflectionHistory makeHistory(
            String tenantId, String executionId, String verdict,
            double score, int importance, List<String> lessons) {
        return new ReflectionHistory(
                tenantId, tenantId, executionId, "req-" + executionId,
                verdict, score, importance, lessons, null, false, Instant.now()
        );
    }
}