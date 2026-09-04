package com.shreeai.os.platform.runtime.cache;

import java.util.Objects;

/**
 * <b>CacheProviderFactory</b>
 *
 * <p>Configuration-driven factory for creating {@link CacheClient} instances.
 * Supports IN_MEMORY and REDIS providers.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State (L1)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class CacheProviderFactory {

    public enum CacheProvider {
        IN_MEMORY,
        REDIS
    }

    private CacheProviderFactory() {
    }

    /**
     * Creates a CacheClient from the provider name.
     *
     * @param providerName the provider name (case-insensitive)
     * @return the cache client
     * @throws IllegalArgumentException if provider is unknown
     */
    public static CacheClient create(String providerName) {
        Objects.requireNonNull(providerName, "providerName must not be null");

        return switch (providerName.toUpperCase().trim().replace('-', '_')) {
            case "IN_MEMORY", "INMEMORY" -> new InMemoryCacheClient();
            case "REDIS", "REDIS_CACHE", "REDIS://" -> new RedisCacheClient(RedisConnectionProvider.defaultProvider());
            default -> throw new IllegalArgumentException(
                    "Unknown cache provider: " + providerName + ". Supported: IN_MEMORY, REDIS"
            );
        };
    }

    /**
     * Creates a CacheClient from the provider enum.
     *
     * @param provider the provider enum
     * @return the cache client
     */
    public static CacheClient create(CacheProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");

        return switch (provider) {
            case IN_MEMORY -> new InMemoryCacheClient();
            case REDIS -> new RedisCacheClient(RedisConnectionProvider.defaultProvider());
        };
    }
}