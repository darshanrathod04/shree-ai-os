package com.shreeai.os.platform.runtime.reflection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * <b>InMemoryReflectionRepository</b>
 *
 * <p>Thread-safe, in-memory implementation of {@link ReflectionRepository}.
 * Enforces tenant isolation through per-tenant data partitioning.</p>
 *
 * <p>This is the default implementation for Phase 1.5. A PostgreSQL-backed
 * implementation will be provided in Phase 2.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class InMemoryReflectionRepository implements ReflectionRepository {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ReflectionHistory>> tenantStore =
            new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<ReflectionHistory> globalTimeline =
            new CopyOnWriteArrayList<>();

    @Override
    public ReflectionHistory save(ReflectionHistory history) {
        Objects.requireNonNull(history, "history must not be null");

        tenantStore.computeIfAbsent(
                history.tenantId(),
                k -> new CopyOnWriteArrayList<>()
        ).add(history);

        globalTimeline.add(history);

        return history;
    }

    @Override
    public Optional<ReflectionHistory> findByExecutionId(String tenantId, String executionId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(executionId, "executionId must not be null");

        CopyOnWriteArrayList<ReflectionHistory> records = tenantStore.get(tenantId);
        if (records == null) {
            return Optional.empty();
        }

        return records.stream()
                .filter(r -> r.executionId().equals(executionId))
                .findFirst();
    }

    @Override
    public List<ReflectionHistory> findByTenantId(String tenantId, int limit) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        CopyOnWriteArrayList<ReflectionHistory> records = tenantStore.get(tenantId);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .sorted(Comparator.comparing(ReflectionHistory::evaluatedAt).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<ReflectionHistory> findRecent(int limit) {
        if (globalTimeline.isEmpty()) {
            return Collections.emptyList();
        }

        return globalTimeline.stream()
                .sorted(Comparator.comparing(ReflectionHistory::evaluatedAt).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public long countByTenantId(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        CopyOnWriteArrayList<ReflectionHistory> records = tenantStore.get(tenantId);
        return records == null ? 0L : records.size();
    }

    @Override
    public List<ReflectionHistory> findFailuresByTenantId(String tenantId, int limit) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        CopyOnWriteArrayList<ReflectionHistory> records = tenantStore.get(tenantId);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .filter(r -> "FAILURE".equals(r.verdict()))
                .sorted(Comparator.comparing(ReflectionHistory::evaluatedAt).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toUnmodifiableList());
    }
}