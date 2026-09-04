package com.shreeai.os.platform.runtime.vector;

import java.util.List;
import java.util.Optional;

/**
 * <b>VectorStore</b>
 *
 * <p>SPI for persistent vector storage. Implementations are pluggable
 * adapters (in-memory, PostgreSQL + pgvector, ...) selected via
 * {@link VectorStoreProvider}; no kernel references a concrete store.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Implementations MUST be thread-safe.</li>
 *   <li>{@link #store} is an upsert keyed by {@link VectorRecord#id()}.</li>
 *   <li>Failures MUST be translated into {@link VectorRuntimeException}.</li>
 *   <li>Durability depends on the adapter; the platform degrades gracefully
 *       to the in-memory adapter when no database is configured.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface VectorStore {

    /**
     * Stores (upserts) a vector record.
     *
     * @param record the record to store (must not be null)
     * @throws VectorRuntimeException if persistence fails
     */
    void store(VectorRecord record);

    /**
     * Finds a record by id.
     *
     * @param id the record id (must not be null or blank)
     * @return the record, or empty when unknown
     * @throws VectorRuntimeException if lookup fails
     */
    Optional<VectorRecord> findById(String id);

    /**
     * Returns all stored records.
     *
     * @return immutable list of records (never null; may be empty)
     * @throws VectorRuntimeException if the scan fails
     */
    List<VectorRecord> all();

    /**
     * Deletes a record.
     *
     * @param id the record id (must not be null or blank)
     * @return {@code true} when a record was deleted
     * @throws VectorRuntimeException if deletion fails
     */
    boolean delete(String id);
}
