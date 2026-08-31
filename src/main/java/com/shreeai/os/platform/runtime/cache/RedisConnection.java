package com.shreeai.os.platform.runtime.cache;

/**
 * <b>RedisConnection</b>
 *
 * <p>Minimal Redis command interface. Implementations delegate to the
 * actual Redis client (Jedis, Lettuce, etc.).</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface RedisConnection extends AutoCloseable {

    /**
     * Sets a key-value pair.
     */
    void set(String key, String value);

    /**
     * Sets a key-value pair with TTL in seconds.
     */
    void setex(String key, long seconds, String value);

    /**
     * Gets a value by key.
     *
     * @return the value, or null if not found
     */
    String get(String key);

    /**
     * Deletes a key.
     *
     * @return number of keys deleted
     */
    long del(String key);

    /**
     * Checks if a key exists.
     */
    boolean exists(String key);

    /**
     * Returns the number of keys in the current database.
     */
    long dbSize();

    /**
     * Finds all keys matching the given glob-style pattern (e.g. {@code prefix:*}).
     *
     * @param pattern the Redis glob pattern (never null)
     * @return the matching keys (never null, may be empty)
     */
    java.util.Set<String> keys(String pattern);

    /**
     * Flushes the current database.
     */
    void flushDB();

    @Override
    void close();
}