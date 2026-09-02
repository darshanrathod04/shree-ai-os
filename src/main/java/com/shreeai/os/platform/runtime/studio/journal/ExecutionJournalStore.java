package com.shreeai.os.platform.runtime.studio.journal;

import com.shreeai.os.platform.sdk.events.RuntimeEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <b>ExecutionJournalStore</b>
 *
 * <p>SPI for the Execution Journal engine (Phase 3). Implementations persist
 * and reconstruct {@link ExecutionJournal}s from stage-level rows, keep the
 * recent runtime event feed, and provide tenant-scoped read access.</p>
 *
 * <p>Contract obligations:</p>
 * <ul>
 *   <li>Thread-safe and tenant-isolated: reads scoped by {@code tenantId} SHALL
 *       never leak another tenant's journals.</li>
 *   <li>{@code openExecution}/{@code append}/{@code closeExecution} are the
 *       journal-collection entry points; {@code recordEvent} feeds the event
 *       ring used by observability and recording.</li>
 *   <li>Replay contract: {@link #readByRequestId(String)} must be able to
 *       reconstruct a full journal after process restart (L2-backed stores
 *       aggregate persisted rows).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Studio Execution Journal (Phase 3)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ExecutionJournalStore {

    /**
     * Registers a new IN_PROGRESS journal header.
     *
     * @param journal the journal header (never null)
     */
    void openExecution(ExecutionJournal journal);

    /**
     * Appends a stage entry to the journal identified by request id.
     *
     * @param requestId the request identifier (never null)
     * @param tenantId  the tenant identifier (never null)
     * @param entry     the stage timeline entry (never null)
     */
    void append(String requestId, String tenantId, StageTimelineEntry entry);

    /**
     * Closes the journal for a request with the given terminal status.
     *
     * @param requestId   the request identifier (never null)
     * @param tenantId    the tenant identifier (never null)
     * @param status      the terminal status (never null)
     * @param completedAt when the execution completed (never null)
     */
    void closeExecution(String requestId, String tenantId, JournalStatus status, Instant completedAt);

    /**
     * Records a raw runtime event into the recent-event feed.
     *
     * @param event the runtime event (never null)
     */
    void recordEvent(RuntimeEvent event);

    /**
     * Returns the most recent runtime events, newest first.
     *
     * @param limit maximum number of events to return
     * @return the events (never null, may be empty)
     */
    List<RuntimeEvent> recentEvents(int limit);

    /**
     * Reconstructs a journal by its request identifier (replay after restart).
     *
     * @param requestId the request identifier (never null)
     * @return the reconstructed journal, or empty when unknown
     */
    Optional<ExecutionJournal> readByRequestId(String requestId);

    /**
     * Reconstructs a journal by its execution identifier.
     *
     * @param executionId the execution identifier (never null)
     * @return the reconstructed journal, or empty when unknown
     */
    Optional<ExecutionJournal> readByExecutionId(String executionId);

    /**
     * Returns the most recent journals for a tenant, newest first.
     *
     * @param tenantId the tenant identifier (never null)
     * @param limit    maximum number of journals to return
     * @return the journals (never null, may be empty)
     */
    List<ExecutionJournal> findByTenant(String tenantId, int limit);

    /** @return total number of journals across all tenants */
    long count();

    /** @return total number of journals for a tenant */
    long countByTenant(String tenantId);

    /** @return total number of FAILED journals for a tenant */
    long countFailedByTenant(String tenantId);

    /**
     * Returns flattened stage entries for a tenant (heatmap + performance
     * analytics input).
     *
     * @param tenantId the tenant identifier (never null)
     * @param limit    maximum number of entries to return
     * @return the stage entries (never null, may be empty)
     */
    List<StageTimelineEntry> stageEntriesByTenant(String tenantId, int limit);

        /** @return all tenant ids that have touched the journal (never null) */
    Set<String> tenantIds();

    /**
     * Returns the most recent journals across ALL tenants, newest first.
     *
     * @param limit maximum number of journals to return
     * @return the journals (never null, may be empty)
     */
    default List<ExecutionJournal> latest(int limit) {
        return findByTenant(null, Math.max(0, limit));
    }
}