package com.shreeai.os.platform.runtime.cache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>InMemoryCacheClient</b>
 *
 * <p>Thread-safe, in-memory implementation of {@link CacheClient} with TTL
 * support. Used as the default L1 cache when Redis is not configured.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class InMemoryCacheClient implements CacheClient {

    public static final int DEFAULT_MAX_CAPACITY = 10_000;

    private final ConcurrentHashMap<String, TimedValue> store = new ConcurrentHashMap<>();
    private final int maxCapacity;

    public InMemoryCacheClient() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public InMemoryCacheClient(int maxCapacity) {
        this.maxCapacity = Math.max(1, maxCapacity);
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        if (store.size() >= maxCapacity) {
            // Purge expired entries to relieve memory pressure
            store.entrySet().removeIf(e -> e.getValue().isExpired());
            if (store.size() >= maxCapacity) {
                // Evict the entry with the earliest expiry
                String oldestKey = null;
                long earliestExpiry = Long.MAX_VALUE;
                for (var entry : store.entrySet()) {
                    if (entry.getValue().expiryMillis() < earliestExpiry) {
                        earliestExpiry = entry.getValue().expiryMillis();
                        oldestKey = entry.getKey();
                    }
                }
                if (oldestKey != null) {
                    store.remove(oldestKey);
                }
            }
        }
        long now = System.currentTimeMillis();
        long expiry = (ttlSeconds <= 0 || ttlSeconds > (Long.MAX_VALUE - now) / 1000L)
                ? Long.MAX_VALUE
                : now + (ttlSeconds * 1000L);
        store.put(key, new TimedValue(value, expiry));
    }

    @Override
    public Optional<String> get(String key) {
        TimedValue tv = store.get(key);
        if (tv == null) {
            return Optional.empty();
        }
        if (tv.isExpired()) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(tv.value());
    }

    @Override
    public boolean evict(String key) {
        return store.remove(key) != null;
    }

    @Override
    public boolean contains(String key) {
        TimedValue tv = store.get(key);
        if (tv == null) {
            return false;
        }
        if (tv.isExpired()) {
            store.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public long size() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
        return store.size();
    }

    @Override
    public java.util.Set<String> keys(String prefix) {
        java.util.Objects.requireNonNull(prefix, "prefix must not be null");
        long now = System.currentTimeMillis();
        return store.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .filter(e -> now <= e.getValue().expiryMillis())
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    private record TimedValue(String value, long expiryMillis) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiryMillis;
        }
    }
}