package com.shreeai.os.platform.runtime.reflection;

import java.util.List;
import java.util.Optional;

/**
 * <b>ReflectionRepository</b>
 *
 * <p>Persistence store for {@link ReflectionHistory} records. Supports
 * tenant-isolated storage and retrieval of reflection history.</p>
 *
 * <p>Implementations MUST be thread-safe and MUST enforce tenant isolation
 * — no tenant may access another tenant's reflection records.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ReflectionRepository {

    /**
     * Persists a reflection history record.
     *
     * @param history the reflection history to save (never null)
     * @return the saved record (never null)
     * @throws NullPointerException if history is null
     */
    ReflectionHistory save(ReflectionHistory history);

    /**
     * Finds a reflection history by execution identifier.
     *
     * @param tenantId    the tenant identifier (never null)
     * @param executionId the execution identifier (never null)
     * @return the matching record, or empty if not found
     */
    Optional<ReflectionHistory> findByExecutionId(String tenantId, String executionId);

    /**
     * Finds all reflection history for a tenant, ordered by evaluatedAt descending.
     *
     * @param tenantId the tenant identifier (never null)
     * @param limit    maximum number of records to return
     * @return list of records (never null, may be empty)
     */
    List<ReflectionHistory> findByTenantId(String tenantId, int limit);

    /**
     * Finds recent reflection history across all tenants (admin use).
     *
     * @param limit maximum number of records to return
     * @return list of records ordered by evaluatedAt descending
     */
    List<ReflectionHistory> findRecent(int limit);

    /**
     * Counts the number of reflection records for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @return the count (≥ 0)
     */
    long countByTenantId(String tenantId);

    /**
     * Finds all FAILED reflection records for a tenant within the given window.
     *
     * @param tenantId the tenant identifier (never null)
     * @param limit    maximum number of records to return
     * @return list of failed records ordered by evaluatedAt descending
     */
    List<ReflectionHistory> findFailuresByTenantId(String tenantId, int limit);
}