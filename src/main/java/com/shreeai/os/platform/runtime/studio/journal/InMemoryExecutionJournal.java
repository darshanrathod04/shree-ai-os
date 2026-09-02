package com.shreeai.os.platform.runtime.studio.journal;

import com.shreeai.os.platform.sdk.events.RuntimeEvent;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <b>InMemoryExecutionJournal</b>
 *
 * <p>Zero-infrastructure default {@link ExecutionJournalStore}. Thread-safe,
 * bounded, fully replayable in-process. Persists journals, the recent-event
 * feed, and tenant scope without any external infrastructure (Redis or
 * PostgreSQL).</p>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class InMemoryExecutionJournal implements ExecutionJournalStore {

    /** Bounded journal retention (oldest evicted first). */
    private static final int MAX_JOURNALS = 512;
    /** Bounded recent-event feed size. */
    private static final int MAX_RECENT_EVENTS = 512;

    private final Map<String, ExecutionJournal> byRequestId = new ConcurrentHashMap<>();
    private final Map<String, String> executionToRequest = new ConcurrentHashMap<>();
    private final Deque<RuntimeEvent> recentEvents = new ArrayDeque<>();
    private final Set<String> tenants = new ConcurrentSkipListSet<>();

    @Override
    public void openExecution(ExecutionJournal journal) {
        tenants.add(journal.tenantId());
        byRequestId.put(journal.requestId(), journal);
        if (journal.executionId() != null && !journal.executionId().isBlank()) {
            executionToRequest.put(journal.executionId(), journal.requestId());
        }
        trimJournals();
    }

    @Override
    public void append(String requestId, String tenantId, StageTimelineEntry entry) {
        byRequestId.computeIfPresent(requestId, (key, journal) -> {
            tenants.add(tenantId);
            List<StageTimelineEntry> timeline = new ArrayList<>(journal.stageTimeline());
            timeline.add(entry);
            return journal.withStageTimeline(timeline);
        });
    }

    @Override
    public void closeExecution(String requestId, String tenantId, JournalStatus status, Instant completedAt) {
        byRequestId.computeIfPresent(requestId, (key, journal) ->
                journal.withStatus(status, completedAt));
    }

    @Override
    public void recordEvent(RuntimeEvent event) {
        synchronized (recentEvents) {
            recentEvents.addFirst(event);
            while (recentEvents.size() > MAX_RECENT_EVENTS) {
                recentEvents.removeLast();
            }
        }
    }

    @Override
    public List<RuntimeEvent> recentEvents(int limit) {
        synchronized (recentEvents) {
            return List.copyOf(new ArrayList<>(recentEvents).subList(
                    0, Math.min(limit, recentEvents.size())));
        }
    }

    @Override
    public Optional<ExecutionJournal> readByRequestId(String requestId) {
        return Optional.ofNullable(byRequestId.get(requestId));
    }

    @Override
    public Optional<ExecutionJournal> readByExecutionId(String executionId) {
        String requestId = executionToRequest.get(executionId);
        return requestId == null ? Optional.empty() : Optional.ofNullable(byRequestId.get(requestId));
    }

        @Override
    public List<ExecutionJournal> findByTenant(String tenantId, int limit) {
        return byRequestId.values().stream()
                .filter(j -> tenantId == null || tenantId.equals(j.tenantId()))
                .sorted(Comparator.comparing(ExecutionJournal::createdAt).reversed())
                .limit(Math.max(0, limit))
                .collect(java.util.stream.Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    @Override
    public long count() {
        return byRequestId.size();
    }

    @Override
    public long countByTenant(String tenantId) {
        return byRequestId.values().stream()
                .filter(j -> tenantId.equals(j.tenantId()))
                .count();
    }

    @Override
    public long countFailedByTenant(String tenantId) {
        return byRequestId.values().stream()
                .filter(j -> tenantId.equals(j.tenantId()))
                .filter(j -> j.status() == JournalStatus.FAILED)
                .count();
    }

    @Override
    public List<StageTimelineEntry> stageEntriesByTenant(String tenantId, int limit) {
        List<StageTimelineEntry> entries = new ArrayList<>();
        byRequestId.values().stream()
                .filter(j -> tenantId.equals(j.tenantId()))
                .forEach(j -> entries.addAll(j.stageTimeline()));
        entries.sort(Comparator.comparing(StageTimelineEntry::beganAt).reversed());
        if (entries.size() > Math.max(0, limit)) {
            return List.copyOf(entries.subList(0, Math.max(0, limit)));
        }
        return List.copyOf(entries);
    }

    @Override
    public Set<String> tenantIds() {
        return Set.copyOf(tenants);
    }

    private void trimJournals() {
        if (byRequestId.size() <= MAX_JOURNALS) {
            return;
        }
        List<ExecutionJournal> sorted = new ArrayList<>(byRequestId.values());
        sorted.sort(Comparator.comparing(ExecutionJournal::createdAt));
        int excess = sorted.size() - MAX_JOURNALS;
        for (int i = 0; i < excess; i++) {
            ExecutionJournal eldest = sorted.get(i);
            byRequestId.remove(eldest.requestId());
            if (eldest.executionId() != null) {
                executionToRequest.remove(eldest.executionId());
            }
        }
    }
}