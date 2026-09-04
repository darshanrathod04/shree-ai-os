package com.shreeai.os.platform.runtime.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ReflectionAnalyticsService with repository.
 */
class ReflectionAnalyticsIntegrationTest {

    private InMemoryReflectionRepository repository;
    private ReflectionAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReflectionRepository();
        analyticsService = new ReflectionAnalyticsService(repository);
    }

    @Test
    void fullPipelineAnalyticsAndHistory() {
        String tenant = "tenant-analytics";

        repository.save(new ReflectionHistory(
                tenant, tenant, "e-1", "r-1", "SUCCESS", 0.95, 30,
                List.of("L1"), null, false, Instant.now()
        ));
        repository.save(new ReflectionHistory(
                tenant, tenant, "e-2", "r-2", "FAILURE", 0.15, 95,
                List.of("L2", "L3"), "Timeout", true, Instant.now()
        ));

        ReflectionAnalyticsService.ReflectionAnalyticsSummary summary =
                analyticsService.analyze(tenant, 50);

        assertEquals(2, summary.totalRecords());
        assertEquals(1, summary.successCount());
        assertEquals(1, summary.failureCount());
        assertEquals(0.5, summary.successRate(), 0.001);
        assertEquals(3, summary.totalLessons());
        assertEquals(1, summary.rootCauseFrequency().size());

        List<ReflectionHistory> all = repository.findByTenantId(tenant, 50);
        assertEquals(2, all.size());
    }
}