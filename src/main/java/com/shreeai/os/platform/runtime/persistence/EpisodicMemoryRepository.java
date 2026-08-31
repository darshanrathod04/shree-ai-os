package com.shreeai.os.platform.runtime.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persisting EpisodicMemory entities to L2 storage.
 */
public interface EpisodicMemoryRepository {

    /**
     * Saves an episodic memory.
     *
     * @param tenantId the tenant identifier
     * @param memoryId the memory identifier
     * @param content  the memory content
     * @param metadata the memory metadata as key-value pairs
     * @return true if saved successfully
     */
    boolean save(String tenantId, String memoryId, String content, java.util.Map<String, String> metadata);

    /**
     * Finds an episodic memory by tenant and memory ID.
     */
    Optional<String> findById(String tenantId, String memoryId);

    /**
     * Finds all episodic memories for a tenant.
     */
    List<String> findByTenantId(String tenantId, int limit);

    /**
     * Returns all tenant IDs in the system.
     */
    java.util.Set<String> findAllTenantIds();

    /**
     * Deletes an episodic memory.
     */
    boolean delete(String tenantId, String memoryId);
}