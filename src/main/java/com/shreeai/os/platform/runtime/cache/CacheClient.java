package com.shreeai.os.platform.runtime.cache;

import java.util.Optional;

/**
 * <b>CacheClient</b>
 *
 * <p>SPI for cache operations. Decouples cache consumers from the underlying
 * cache implementation (in-memory, Redis, or any other provider).</p>
 *
 * <p>All operations are thread-safe. Implementations MUST support TTL-based
 * expiration and atomic put-if-absent semantics.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface CacheClient {

    /**
     * Stores a value with an optional TTL.
     *
     * @param key   the cache key (never null)
     * @param value the value to store (never null)
     * @param ttlSeconds TTL in seconds, or 0 for no expiration
     */
    void put(String key, String value, long ttlSeconds);

    /**
     * Retrieves a value by key.
     *
     * @param key the cache key (never null)
     * @return the value, or empty if not found or expired
     */
    Optional<String> get(String key);

    /**
     * Removes a value by key.
     *
     * @param key the cache key (never null)
     * @return true if a value was removed
     */
    boolean evict(String key);

    /**
     * Checks if a key exists and is not expired.
     *
     * @param key the cache key (never null)
     * @return true if the key exists
     */
    boolean contains(String key);

    /**
     * Clears all cached values.
     */
    void clear();

    /**
     * Returns the number of entries in the cache.
     *
     * @return the entry count
     */
    long size();

    /**
     * Returns all keys that begin with the given prefix.
     *
     * <p>This is the SPI-level enumeration hook used for snapshotting,
     * recovery, and tenant-scoped cache scans.</p>
     *
     * @param prefix the key prefix (never null)
     * @return the matching unexpired keys (never null, may be empty)
     */
    java.util.Set<String> keys(String prefix);
}