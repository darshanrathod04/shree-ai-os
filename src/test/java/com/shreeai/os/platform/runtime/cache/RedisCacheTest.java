package com.shreeai.os.platform.runtime.cache;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link RedisCacheClient} against the {@link RedisConnectionProvider}
 * SPI with a mocked connection, plus graceful fallback when Redis is down.
 */
class RedisCacheTest {

    private final RedisConnection connection = mock(RedisConnection.class);
    private final RedisConnectionProvider provider = mock(RedisConnectionProvider.class);

    private RedisCacheClient newClient() {
        when(provider.getConnection()).thenReturn(connection);
        return new RedisCacheClient(provider);
    }

    @Test
    void putWithTtlUsesSetex() {
        RedisCacheClient client = newClient();
        client.put("shree:session:t1:s1", "{}", 60);
        verify(connection).setex("shree:session:t1:s1", 60, "{}");
    }

    @Test
    void putWithoutTtlUsesSet() {
        RedisCacheClient client = newClient();
        client.put("shree:session:t1:s1", "{}", 0);
        verify(connection).set("shree:session:t1:s1", "{}");
    }

    @Test
    void getReturnsStoredValue() {
        when(connection.get("key-1")).thenReturn("value-1");
        RedisCacheClient client = newClient();
        assertEquals("value-1", client.get("key-1").orElse("missing"));
    }

    @Test
    void getReturnsEmptyWhenMissing() {
        when(connection.get("key-1")).thenReturn(null);
        RedisCacheClient client = newClient();
        assertTrue(client.get("key-1").isEmpty());
    }

    @Test
    void evictReturnsTrueWhenDeleted() {
        when(connection.del("key-1")).thenReturn(1L);
        RedisCacheClient client = newClient();
        assertTrue(client.evict("key-1"));
    }

    @Test
    void containsDelegatesToExists() {
        when(connection.exists("key-1")).thenReturn(true);
        RedisCacheClient client = newClient();
        assertTrue(client.contains("key-1"));
    }

    @Test
    void keysReturnsMatchingSet() {
        when(connection.keys("shree:session:t1:*")).thenReturn(Set.of("a", "b"));
        RedisCacheClient client = newClient();
        assertEquals(Set.of("a", "b"), client.keys("shree:session:t1:"));
    }

    @Test
    void sizeDelegatesToDbSize() {
        when(connection.dbSize()).thenReturn(3L);
        RedisCacheClient client = newClient();
        assertEquals(3, client.size());
    }

    @Test
    void clearDelegatesToFlushDb() {
        RedisCacheClient client = newClient();
        client.clear();
        verify(connection).flushDB();
    }

    // ---- Fallback behaviour when Redis is unavailable ----

    @Test
    void putSwallowsConnectionFailure() {
        when(provider.getConnection()).thenThrow(new IllegalStateException("Redis down"));
        RedisCacheClient client = new RedisCacheClient(provider);
        assertDoesNotThrow(() -> client.put("k", "v", 60));
    }

    @Test
    void getReturnsEmptyOnConnectionFailure() {
        when(provider.getConnection()).thenThrow(new IllegalStateException("Redis down"));
        RedisCacheClient client = new RedisCacheClient(provider);
        assertTrue(client.get("k").isEmpty());
    }

    @Test
    void evictReturnsFalseOnConnectionFailure() {
        when(provider.getConnection()).thenThrow(new IllegalStateException("Redis down"));
        RedisCacheClient client = new RedisCacheClient(provider);
        assertFalse(client.evict("k"));
    }

    @Test
    void keysReturnsEmptyOnConnectionFailure() {
        when(provider.getConnection()).thenThrow(new IllegalStateException("Redis down"));
        RedisCacheClient client = new RedisCacheClient(provider);
        assertTrue(client.keys("shree:").isEmpty());
    }

    @Test
    void connectionClosedAfterOperation() {
        RedisCacheClient client = newClient();
        client.get("k");
        verify(connection).close();
    }
}