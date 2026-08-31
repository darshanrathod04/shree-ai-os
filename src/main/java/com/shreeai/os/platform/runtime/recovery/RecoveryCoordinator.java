package com.shreeai.os.platform.runtime.recovery;

import com.shreeai.os.platform.runtime.cache.SessionCache;
import com.shreeai.os.platform.runtime.persistence.EpisodicMemoryRepository;
import com.shreeai.os.platform.runtime.persistence.MemoryVersionLedgerRepository;
import com.shreeai.os.platform.runtime.reflection.ReflectionRepository;

import java.util.Objects;

/**
 * Coordinates runtime recovery from L1 (cache) and L2 (PostgreSQL) sources.
 *
 * <p>On cache miss, rebuilds L1 from L2. On full restart, restores from snapshots.</p>
 */
public final class RecoveryCoordinator {

    private final SessionCache sessionCache;
    private final EpisodicMemoryRepository episodicMemoryRepository;
    private final MemoryVersionLedgerRepository ledgerRepository;
    private final ReflectionRepository reflectionRepository;

    public RecoveryCoordinator(
            SessionCache sessionCache,
            EpisodicMemoryRepository episodicMemoryRepository,
            MemoryVersionLedgerRepository ledgerRepository,
            ReflectionRepository reflectionRepository
    ) {
        this.sessionCache = Objects.requireNonNull(sessionCache, "sessionCache must not be null");
        this.episodicMemoryRepository = episodicMemoryRepository;
        this.ledgerRepository = ledgerRepository;
        this.reflectionRepository = reflectionRepository;
    }

    /**
     * Rebuilds L1 cache from L2 for a tenant.
     *
     * @param tenantId the tenant to rebuild cache for
     */
    public void rebuildCacheFromL2(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        // Load episodic memories from L2 into L1
        if (episodicMemoryRepository != null) {
            episodicMemoryRepository.findByTenantId(tenantId, 1000)
                    .forEach(memory -> sessionCache.putMemory(tenantId, memory));
        }

        // Load reflections from L2
        if (reflectionRepository != null) {
            reflectionRepository.findByTenantId(tenantId, 1000)
                    .forEach(reflection -> sessionCache.putReflection(tenantId, reflection));
        }
    }

    /**
     * Restores runtime state from a snapshot.
     *
     * @param snapshot the snapshot to restore from
     */
    public void restoreFromSnapshot(RuntimeSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        // Restore sessions
        snapshot.sessions().forEach(session ->
                sessionCache.putSession(snapshot.tenantId(), session));

        // Restore conversation states
        snapshot.conversationStates().forEach(state ->
                sessionCache.putConversationState(snapshot.tenantId(), state));

        // Restore execution contexts
        snapshot.executionContexts().forEach(ctx ->
                sessionCache.putExecutionContext(snapshot.tenantId(), ctx));
    }
}