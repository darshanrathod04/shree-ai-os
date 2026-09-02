package com.shreeai.os.platform.runtime.studio.journal;

import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.sdk.events.RuntimeEventListener;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>JournalSubscriber</b>
 *
 * <p>Read-only observer that attaches to a {@link RuntimeEventBus} and records
 * the deterministic stage timeline of every execution into an
 * {@link ExecutionJournalStore}.</p>
 *
 * <p>Capture mechanism: the runtime publishes {@code PIPELINE_*} events on its
 * internal bus and each pipeline stage publishes a {@code *_COMPLETED} event
 * (Identity, Context, MemoryRecall, Knowledge, Reasoning, Inference, Planning,
 * ActionExecution, MemoryStore, ChiefReview, Reflection). {@code RuntimeEvent}
 * already carries {@code requestId}, {@code stage}, {@code timestamp} and
 * {@code metadata}, so the timeline is reconstructed WITHOUT touching the
 * frozen publisher surface or any SDK contract.</p>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class JournalSubscriber implements RuntimeEventListener {

    /** Stage completion events that form the timeline. */
    private static final Set<EventType> STAGE_TYPES = EnumSet.of(
            EventType.IDENTITY_COMPLETED,
            EventType.CONTEXT_COMPLETED,
            EventType.MEMORY_RECALL_COMPLETED,
            EventType.KNOWLEDGE_COMPLETED,
            EventType.REASONING_COMPLETED,
            EventType.INFERENCE_COMPLETED,
            EventType.PLANNING_COMPLETED,
            EventType.EXECUTION_COMPLETED,
            EventType.MEMORY_STORE_COMPLETED,
            EventType.CHIEF_REVIEW_COMPLETED,
            EventType.REFLECTION_COMPLETED);

    private static final String DEFAULT_TENANT = "default";

    private final RuntimeEventBus eventBus;
    private final ExecutionJournalStore store;

    /** requestId → (stageName → latest sequence). */
    private final Map<String, Map<String, Integer>> sequenceByRequest = new ConcurrentHashMap<>();
    /** requestId → (stageName → beganAt). */
    private final Map<String, Map<String, Instant>> beganByRequest = new ConcurrentHashMap<>();

    /**
     * Attaches this subscriber to the given bus for all pipeline and stage
     * event types and records into the given store.
     *
     * @param eventBus the runtime event bus (never null)
     * @param store    the journal store (never null)
     */
    public JournalSubscriber(RuntimeEventBus eventBus, ExecutionJournalStore store) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
        this.store = Objects.requireNonNull(store, "store must not be null");
        eventBus.subscribe(EventType.PIPELINE_STARTED, this);
        eventBus.subscribe(EventType.PIPELINE_COMPLETED, this);
        eventBus.subscribe(EventType.PIPELINE_FAILED, this);
        eventBus.subscribe(EventType.REFLECTION_PERSISTED, this);
        eventBus.subscribe(EventType.KNOWLEDGE_INGEST_REQUESTED, this);
        eventBus.subscribe(EventType.KNOWLEDGE_INGEST_COMPLETED, this);
        for (EventType type : STAGE_TYPES) {
            eventBus.subscribe(type, this);
        }
    }

    /** Detaches all subscriptions (best-effort cleanup). */
    public void unsubscribe() {
        eventBus.unsubscribe(EventType.PIPELINE_STARTED, this);
        eventBus.unsubscribe(EventType.PIPELINE_COMPLETED, this);
        eventBus.unsubscribe(EventType.PIPELINE_FAILED, this);
        eventBus.unsubscribe(EventType.REFLECTION_PERSISTED, this);
        eventBus.unsubscribe(EventType.KNOWLEDGE_INGEST_REQUESTED, this);
        eventBus.unsubscribe(EventType.KNOWLEDGE_INGEST_COMPLETED, this);
        for (EventType type : STAGE_TYPES) {
            eventBus.unsubscribe(type, this);
        }
    }

    @Override
    public void onEvent(RuntimeEvent event) {
        store.recordEvent(event);
        switch (event.type()) {
            case PIPELINE_STARTED -> handleStarted(event);
            case PIPELINE_COMPLETED -> handleClosed(event, JournalStatus.COMPLETED);
            case PIPELINE_FAILED -> handleClosed(event, JournalStatus.FAILED);
            default -> handleStage(event);
        }
    }

    private void handleStarted(RuntimeEvent event) {
        String requestId = event.requestId();
        String executionId = metadataString(event, "executionId");
        String traceId = metadataString(event, "traceId");
        String summary = metadataString(event, "requestSummary", "");
        store.openExecution(ExecutionJournal.begin(
                requestId, executionId, tenantOf(event), traceId, summary));
        sequenceByRequest.put(requestId, new ConcurrentHashMap<>());
        beganByRequest.put(requestId, new ConcurrentHashMap<>());
    }

        private void handleStage(RuntimeEvent event) {
        if (!STAGE_TYPES.contains(event.type())) {
            return;
        }
        String requestId = event.requestId();
        Map<String, Integer> sequence = sequenceByRequest.get(requestId);
        Map<String, Instant> began = beganByRequest.get(requestId);
        if (sequence == null) {
            // Stage event arrived before PIPELINE_STARTED (route-specific
            // dispatch) — treat this event as the implicit execution start.
            handleStarted(prefixStartedEvent(event));
            // Fall through so the stage entry is also appended.
            sequence = sequenceByRequest.get(requestId);
            began = beganByRequest.get(requestId);
        }
        String stage = stageName(event);
        int seq = sequence.merge(stage, 1, Integer::sum);
        Instant beganAt = began.computeIfAbsent(stage, s -> event.timestamp());
        Instant completedAt = event.timestamp();
        long durationMs = Math.max(0, Duration.between(beganAt, completedAt).toMillis());
        String outcome = metadataString(event, "status", "SUCCESS");
        store.append(requestId, tenantOf(event),
                StageTimelineEntry.builder()
                        .stageName(stage)
                        .sequence(seq)
                        .beganAt(beganAt)
                        .completedAt(completedAt)
                        .durationMs(durationMs)
                        .outcome(outcome)
                        .detailRef(metadataString(event, "executionId"))
                        .build());
    }

    private void handleClosed(RuntimeEvent event, JournalStatus status) {
        String requestId = event.requestId();
        store.closeExecution(requestId, tenantOf(event), status, event.timestamp());
        sequenceByRequest.remove(requestId);
        beganByRequest.remove(requestId);
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private RuntimeEvent prefixStartedEvent(RuntimeEvent event) {
        return new RuntimeEvent(
                EventType.PIPELINE_STARTED,
                event.requestId(),
                event.stage(),
                event.timestamp(),
                event.metadata());
    }

    private static String stageName(RuntimeEvent event) {
        String stage = event.stage();
        if (stage == null || stage.isBlank()) {
            String type = event.type().name();
            return type.endsWith("_COMPLETED") && !"PIPELINE_COMPLETED".equals(type)
                    ? type.substring(0, type.length() - "_COMPLETED".length())
                    : type;
        }
        return stage;
    }

    private static String tenantOf(RuntimeEvent event) {
        Object value = event.metadata() == null ? null : event.metadata().get("tenantId");
        return value == null || value.toString().isBlank() ? DEFAULT_TENANT : value.toString();
    }

    private static String metadataString(RuntimeEvent event, String key) {
        return metadataString(event, key, null);
    }

    private static String metadataString(RuntimeEvent event, String key, String fallback) {
        if (event.metadata() == null) {
            return fallback;
        }
        Object value = event.metadata().get(key);
        return value == null ? fallback : value.toString();
    }
}