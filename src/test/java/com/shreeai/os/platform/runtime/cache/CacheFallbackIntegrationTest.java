package com.shreeai.os.platform.runtime.cache;

import com.shreeai.os.platform.runtime.persistence.EpisodicMemoryRepository;
import com.shreeai.os.platform.runtime.persistence.MemoryVersionLedgerRepository;
import com.shreeai.os.platform.runtime.recovery.RecoveryCoordinator;
import com.shreeai.os.platform.runtime.reflection.InMemoryReflectionRepository;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * End-to-end cache fallback verification:
 * L1 (Redis) unavailable → cache miss → L2 (PostgreSQL) rebuild → cache hit.
 */
class CacheFallbackIntegrationTest {

    @Test
    void redisDownProducesCacheMiss() {
        RedisConnectionProvider downProvider = new RedisConnectionProvider() {
            @Override
            public RedisConnection getConnection() {
                throw new IllegalStateException("Redis unavailable");
            }

            @Override
            public void close() {
            }
        };

        DefaultSessionCache sessionCache = new DefaultSessionCache(new RedisCacheClient(downProvider));
        sessionCache.storeSession("s1", "t1", "{}", 0);

        assertTrue(sessionCache.getSession("s1", "t1").isEmpty(),
                "When Redis is down, reads must fall back to cache-miss semantics");
    }

    @Test
    void cacheMissFallsBackToL2Rebuild() {
        // L1 totally empty cache (simulating fresh start / Redis eviction)
        DefaultSessionCache sessionCache = new DefaultSessionCache(new InMemoryCacheClient());

        // L2 layer holds data
        EpisodicMemoryRepository episodic = mock(EpisodicMemoryRepository.class);
        when(episodic.findByTenantId("t1", 1000)).thenReturn(List.of("memory-content"));

        InMemoryReflectionRepository reflections = new InMemoryReflectionRepository();
        reflections.save(new ReflectionHistory(
                "t1", "org-1", "exec-1", "req-1",
                "FAILURE", 0.2, 95, List.of("Lesson"), "timeout", true, Instant.now()
        ));

        // Before rebuild: cache miss
        assertTrue(sessionCache.getCacheClient().keys("shree:").isEmpty());

        // Rebuild L1 from L2
        RecoveryCoordinator coordinator = new RecoveryCoordinator(
                sessionCache, episodic, mock(MemoryVersionLedgerRepository.class), reflections);
        coordinator.rebuildCacheFromL2("t1");

        // After rebuild: cache hit
        assertFalse(sessionCache.getCacheClient().keys("shree:memory:t1:").isEmpty(),
                "Memories should be repopulated in L1 from L2 after cache miss");
        assertTrue(sessionCache.getCacheClient().contains("shree:reflection:t1:exec-1"),
                "Reflections should be repopulated in L1 from L2 after cache miss");
    }

    @Test
    void inMemoryProviderSelectionIsConfigurationDriven() {
        CacheClient client = CacheProviderFactory.create("in-memory");
        assertInstanceOf(InMemoryCacheClient.class, client);

        CacheClient clientViaEnum = CacheProviderFactory.create(CacheProviderFactory.CacheProvider.IN_MEMORY);
        assertInstanceOf(InMemoryCacheClient.class, clientViaEnum);
    }

    @Test
    void redisProviderSelectionReturnsRedisClient() {
        CacheClient client = CacheProviderFactory.create("REDIS");
        assertInstanceOf(RedisCacheClient.class, client);
    }

    @Test
    void unknownProviderThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CacheProviderFactory.create("memcached"));
    }

    @Test
    void redisRoundTripThroughSessionCache() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisConnectionProvider provider = mock(RedisConnectionProvider.class);
        when(provider.getConnection()).thenReturn(connection);
        when(connection.get("shree:session:t1:s1")).thenReturn("{\"session\":\"s1\"}");

        DefaultSessionCache sessionCache = new DefaultSessionCache(new RedisCacheClient(provider));
        sessionCache.storeSession("s1", "t1", "{\"session\":\"s1\"}", 120);

        assertEquals("{\"session\":\"s1\"}", sessionCache.getSession("s1", "t1").orElse(""));
        verify(connection).setex("shree:session:t1:s1", 120, "{\"session\":\"s1\"}");
    }
}