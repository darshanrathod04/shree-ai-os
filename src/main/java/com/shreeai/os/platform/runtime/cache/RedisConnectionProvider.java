package com.shreeai.os.platform.runtime.cache;

import java.io.IOException;
import java.util.Objects;

/**
 * <b>RedisConnectionProvider</b>
 *
 * <p>Abstraction over Redis connection lifecycle. Implementations wrap
 * Jedis, Lettuce, or any Redis client library.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface RedisConnectionProvider {

    /**
     * Gets a Redis connection for the current operation.
     *
     * @return a connection instance (must be closed by caller or implement AutoCloseable)
     */
    RedisConnection getConnection();

    /**
     * Closes the connection provider and releases resources.
     */
    void close();

    /**
     * Creates a default provider configured from system properties:
     * <ul>
     *   <li>{@code shree.redis.host} — default {@code localhost}</li>
     *   <li>{@code shree.redis.port} — default {@code 6379}</li>
     * </ul>
     *
     * <p>The default provider uses the built-in {@link SocketRedisConnection}
     * RESP client so no external Redis library is required on the classpath.
     * Providers can be injected instead for enterprise Redis clients.</p>
     *
     * @return a default Redis connection provider (never null)
     */
    static RedisConnectionProvider defaultProvider() {
        return new RedisConnectionProvider() {
            @Override
            public RedisConnection getConnection() {
                String host = System.getProperty("shree.redis.host", "localhost");
                int port = Integer.getInteger("shree.redis.port", 6379);
                try {
                    return new SocketRedisConnection(host, port);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Cannot connect to Redis at " + host + ":" + port, e);
                }
            }

            @Override
            public void close() {
                // Nothing to release for socket-per-connection flow.
            }
        };
    }

    /**
     * Wraps a concrete provider instance, guarding against nulls.
     *
     * @param delegate the provider to wrap (never null)
     * @return a non-null provider
     */
    static RedisConnectionProvider from(RedisConnectionProvider delegate) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        return delegate;
    }
}