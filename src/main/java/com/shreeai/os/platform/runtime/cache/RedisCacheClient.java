package com.shreeai.os.platform.runtime.cache;

import java.util.Objects;
import java.util.Optional;

/**
 * <b>RedisCacheClient</b>
 *
 * <p>Redis-backed implementation of {@link CacheClient}. Uses the {@link CacheClient}
 * SPI so consumers never depend on Redis directly.</p>
 *
 * <p>This implementation wraps a Redis connection provider (Jedis or Lettuce)
 * behind the unified CacheClient interface. Connection lifecycle is managed
 * externally.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class RedisCacheClient implements CacheClient {

    private final RedisConnectionProvider connectionProvider;

    /**
     * Creates a Redis cache client.
     *
     * @param connectionProvider the Redis connection provider (never null)
     */
    public RedisCacheClient(RedisConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider,
                "connectionProvider must not be null");
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        try (var connection = connectionProvider.getConnection()) {
            if (ttlSeconds > 0) {
                connection.setex(key, ttlSeconds, value);
            } else {
                connection.set(key, value);
            }
        } catch (Exception e) {
            // Redis failures should not break the runtime.
            // The cache miss fallback will read from PostgreSQL.
        }
    }

    @Override
    public Optional<String> get(String key) {
        try (var connection = connectionProvider.getConnection()) {
            String value = connection.get(key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean evict(String key) {
        try (var connection = connectionProvider.getConnection()) {
            return connection.del(key) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean contains(String key) {
        try (var connection = connectionProvider.getConnection()) {
            return connection.exists(key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clear() {
        try (var connection = connectionProvider.getConnection()) {
            connection.flushDB();
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public long size() {
        try (var connection = connectionProvider.getConnection()) {
            return connection.dbSize();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public java.util.Set<String> keys(String prefix) {
        java.util.Objects.requireNonNull(prefix, "prefix must not be null");
        try (var connection = connectionProvider.getConnection()) {
            return connection.keys(prefix + "*");
        } catch (Exception e) {
            return java.util.Set.of();
        }
    }
}