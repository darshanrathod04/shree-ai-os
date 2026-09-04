package com.shreeai.os.platform.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>PersistenceStore</b>
 *
 * <p>Abstraction for the storage layer used by Memory and Knowledge kernels.
 * Implementations range from in-memory maps to SQLite and PostgreSQL.</p>
 *
 * <p>All implementations must be thread-safe.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public interface PersistenceStore {

    /**
     * Stores a key-value entry.
     */
    void put(String key, String value);

    /**
     * Retrieves a value by key.
     */
    Optional<String> get(String key);

    /**
     * Deletes a key.
     */
    void delete(String key);

    /**
     * Returns true if the key exists.
     */
    boolean exists(String key);

    /**
     * Returns all keys matching the prefix (e.g., "memory:", "knowledge:").
     */
    List<String> keys(String prefix);

    /**
     * Returns all entries matching the prefix.
     */
    Map<String, String> entries(String prefix);

    /**
     * Returns the total number of entries (optionally scoped by prefix).
     */
    long size(String prefix);

    /**
     * Clears all entries (optionally scoped by prefix).
     */
    void clear(String prefix);

    /**
     * Returns the store name for diagnostics.
     */
    String name();

    /**
     * Returns true if the underlying store is connected and writable.
     */
    default boolean isHealthy() { return true; }
}
