package com.shreeai.os.platform.runtime.persistence;

import java.util.List;

/**
 * Repository for persisting MemoryVersionLedger entries to L2 storage.
 */
public interface MemoryVersionLedgerRepository {

    /**
     * Records a memory version change.
     *
     * @param tenantId    tenant identifier
     * @param memoryId    memory identifier
     * @param version     version number
     * @param changeType  type of change (CREATE, UPDATE, DELETE)
     * @param snapshot    snapshot of the memory state
     * @return true if recorded successfully
     */
    boolean recordVersion(String tenantId, String memoryId, long version, String changeType, String snapshot);

    /**
     * Finds version history for a memory.
     */
    List<VersionInfo> findVersionHistory(String tenantId, String memoryId, int limit);

    /**
     * Finds the latest version for a memory.
     */
    VersionInfo findLatestVersion(String tenantId, String memoryId);

    /**
     * Record for a memory version.
     */
    record VersionInfo(
            String memoryId,
            long version,
            String changeType,
            String snapshot,
            java.time.Instant recordedAt
    ) {}
}