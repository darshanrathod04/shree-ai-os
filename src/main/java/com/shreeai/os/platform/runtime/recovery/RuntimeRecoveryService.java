package com.shreeai.os.platform.runtime.recovery;

import com.shreeai.os.platform.runtime.cache.SessionCache;
import com.shreeai.os.platform.runtime.persistence.EpisodicMemoryRepository;
import com.shreeai.os.platform.runtime.persistence.MemoryVersionLedgerRepository;
import com.shreeai.os.platform.runtime.reflection.ReflectionRepository;
import com.shreeai.os.platform.runtime.tenant.TenantContext;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service that restores runtime state after application restart.
 *
 * <p>Coordinates snapshot-based recovery and L1→L2 cache rebuild.</p>
 */
public final class RuntimeRecoveryService {

    private static final Logger LOGGER = Logger.getLogger(RuntimeRecoveryService.class.getName());

    private final SessionCache sessionCache;
    private final EpisodicMemoryRepository episodicMemoryRepository;
    private final MemoryVersionLedgerRepository ledgerRepository;
    private final ReflectionRepository reflectionRepository;
    private final RecoveryCoordinator recoveryCoordinator;

    public RuntimeRecoveryService(
            SessionCache sessionCache,
            EpisodicMemoryRepository episodicMemoryRepository,
            MemoryVersionLedgerRepository ledgerRepository,
            ReflectionRepository reflectionRepository
    ) {
        this.sessionCache = Objects.requireNonNull(sessionCache, "sessionCache must not be null");
        this.episodicMemoryRepository = episodicMemoryRepository;
        this.ledgerRepository = ledgerRepository;
        this.reflectionRepository = reflectionRepository;
        this.recoveryCoordinator = new RecoveryCoordinator(
                sessionCache, episodicMemoryRepository, ledgerRepository, reflectionRepository);
    }

    /**
     * Performs full runtime recovery for all tenants.
     */
    public void recoverAll() {
        LOGGER.info("Starting runtime recovery for all tenants");

        // Get all known tenant IDs from L2
        Set<String> tenantIds = episodicMemoryRepository != null
                ? episodicMemoryRepository.findAllTenantIds()
                : Set.of();

        for (String tenantId : tenantIds) {
            recoverTenant(tenantId);
        }

        LOGGER.info("Runtime recovery completed for " + tenantIds.size() + " tenants");
    }

    /**
     * Recovers runtime state for a single tenant.
     *
     * @param tenantId the tenant to recover
     */
    public void recoverTenant(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        try {
            TenantContext.setCurrentTenant(tenantId, tenantId);
            recoveryCoordinator.rebuildCacheFromL2(tenantId);
            LOGGER.info("Recovered runtime state for tenant: " + tenantId);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Creates a snapshot of current runtime state for a tenant.
     *
     * @param tenantId the tenant to snapshot
     * @return the runtime snapshot
     */
    public RuntimeSnapshot createSnapshot(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        return new RuntimeSnapshot(
                tenantId,
                tenantId,
                sessionCache.getSessions(tenantId),
                sessionCache.getConversationStates(tenantId),
                sessionCache.getExecutionContexts(tenantId),
                java.time.Instant.now()
        );
    }
}