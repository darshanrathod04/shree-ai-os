package com.shreeai.os.platform.runtime.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link InMemoryReflectionRepository}.
 */
class InMemoryReflectionRepositoryTest {

    private InMemoryReflectionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReflectionRepository();
    }

    @Test
    void saveAndFindByExecutionId() {
        ReflectionHistory history = makeHistory("tenant-1", "exec-1", "SUCCESS", 0.85, 70);
        repository.save(history);

        Optional<ReflectionHistory> found = repository.findByExecutionId("tenant-1", "exec-1");
        assertTrue(found.isPresent());
        assertEquals("exec-1", found.get().executionId());
    }

    @Test
    void findByExecutionIdReturnsEmptyForWrongTenant() {
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.8, 60));
        assertTrue(repository.findByExecutionId("tenant-2", "exec-1").isEmpty());
    }

    @Test
    void findByTenantIdReturnsRecordsNewestFirst() {
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.8, 50));
        repository.save(makeHistory("tenant-1", "exec-2", "FAILURE", 0.2, 90));

        List<ReflectionHistory> records = repository.findByTenantId("tenant-1", 10);
        assertEquals(2, records.size());
        assertTrue(records.get(0).evaluatedAt().isAfter(records.get(1).evaluatedAt())
                || records.get(0).evaluatedAt().equals(records.get(1).evaluatedAt()));
    }

    @Test
    void tenantIsolationIsEnforced() {
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.8, 50));
        repository.save(makeHistory("tenant-2", "exec-1", "FAILURE", 0.2, 90));

        assertEquals(1, repository.countByTenantId("tenant-1"));
        assertEquals(1, repository.countByTenantId("tenant-2"));
        assertTrue(repository.findByExecutionId("tenant-1", "exec-1").isPresent());
        assertTrue(repository.findByExecutionId("tenant-2", "exec-1").isPresent());
        // Tenant 1 should NOT see tenant 2's records
        assertEquals(0, repository.findByTenantId("tenant-1", 10).stream()
                .filter(r -> r.verdict().equals("FAILURE")).count());
    }

    @Test
    void findRecentReturnsGlobalTimeline() {
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.8, 50));
        repository.save(makeHistory("tenant-2", "exec-2", "PARTIAL", 0.5, 60));

        List<ReflectionHistory> recent = repository.findRecent(10);
        assertEquals(2, recent.size());
    }

    @Test
    void findFailuresByTenantId() {
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.9, 30));
        repository.save(makeHistory("tenant-1", "exec-2", "FAILURE", 0.2, 90));
        repository.save(makeHistory("tenant-1", "exec-3", "FAILURE", 0.3, 85));

        List<ReflectionHistory> failures = repository.findFailuresByTenantId("tenant-1", 10);
        assertEquals(2, failures.size());
        assertTrue(failures.stream().allMatch(r -> r.verdict().equals("FAILURE")));
    }

    @Test
    void countByTenantId() {
        assertEquals(0, repository.countByTenantId("tenant-1"));
        repository.save(makeHistory("tenant-1", "exec-1", "SUCCESS", 0.8, 50));
        assertEquals(1, repository.countByTenantId("tenant-1"));
    }

    private ReflectionHistory makeHistory(
            String tenantId, String executionId, String verdict, double score, int importance) {
        return new ReflectionHistory(
                tenantId, tenantId, executionId, "req-" + executionId,
                verdict, score, importance, List.of("Lesson for " + executionId),
                "FAILURE".equals(verdict) ? "Root cause" : null,
                "FAILURE".equals(verdict),
                Instant.now()
        );
    }
}