package com.shreeai.os.platform.runtime.recovery;

import com.shreeai.os.platform.runtime.cache.CacheClient;
import com.shreeai.os.platform.runtime.cache.DefaultSessionCache;
import com.shreeai.os.platform.runtime.cache.InMemoryCacheClient;
import com.shreeai.os.platform.runtime.cache.RedisCacheClient;
import com.shreeai.os.platform.runtime.cache.RedisConnection;
import com.shreeai.os.platform.runtime.cache.RedisConnectionProvider;
import com.shreeai.os.platform.runtime.persistence.EpisodicMemoryRepository;
import com.shreeai.os.platform.runtime.persistence.MemoryVersionLedgerRepository;
import com.shreeai.os.platform.runtime.reflection.InMemoryReflectionRepository;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;
import com.shreeai.os.platform.runtime.reflection.ReflectionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link RuntimeRecoveryService} and {@link RecoveryCoordinator}:
 * snapshot creation, snapshot restoration, and L1-from-L2 cache rebuild.
 */
class RuntimeRecoveryTest {

    private final DefaultSessionCache sessionCache =
            new DefaultSessionCache(new InMemoryCacheClient());

    private RuntimeRecoveryService newRecoveryService(
            EpisodicMemoryRepository episodic,
            MemoryVersionLedgerRepository ledger,
            ReflectionRepository reflections
    ) {
        return new RuntimeRecoveryService(sessionCache, episodic, ledger, reflections);
    }

    @Test
    void createSnapshotCapturesSessionsConversationsAndExecutions() {
        sessionCache.storeSession("s1", "t1", sessionJson("s1"), 0);
        sessionCache.storeConversation("conv-1", "t1", conversationJson("conv-1"), 0);
        sessionCache.storeExecution("exec-1", "t1", executionJson("exec-1", "req-1", "SUCCESS"), 0);

        RuntimeSnapshot snapshot =
                newRecoveryService(null, null, null).createSnapshot("t1");

        assertEquals("t1", snapshot.tenantId());
        assertEquals(1, snapshot.sessions().size());
        assertEquals(1, snapshot.conversationStates().size());
        assertEquals(1, snapshot.executionContexts().size());
        assertEquals("s1", snapshot.sessions().get(0).sessionId());
    }

    @Test
    void createSnapshotExcludesOtherTenants() {
        sessionCache.storeSession("s1", "t1", sessionJson("s1"), 0);
        sessionCache.storeSession("s2", "t2", sessionJson("s2"), 0);

        RuntimeSnapshot snapshot =
                newRecoveryService(null, null, null).createSnapshot("t1");

        assertEquals(1, snapshot.sessions().size());
        assertEquals("s1", snapshot.sessions().get(0).sessionId());
    }

    private static String sessionJson(String sessionId) {
        return "{\"sessionId\":\"" + sessionId + "\",\"userId\":\"u1\",\"attributes\":{},"
                + "\"lastAccessedAt\":\"2026-08-31T10:00:00Z\"}";
    }

    private static String conversationJson(String sessionId) {
        return "{\"sessionId\":\"" + sessionId + "\",\"messageHistory\":[],\"metadata\":{}}";
    }

    private static String executionJson(String executionId, String requestId, String status) {
        return "{\"executionId\":\"" + executionId + "\",\"requestId\":\"" + requestId
                + "\",\"status\":\"" + status + "\",\"metadata\":{}}";
    }

    @Test
    void restoreFromSnapshotRepopulatesCache() {
        // Clear any state
        sessionCache.getCacheClient().clear();

        RuntimeSnapshot snapshot = new RuntimeSnapshot(
                "t9", "t9",
                List.of(new RuntimeSnapshot.SerializableSession(
                        "s9", "u9", Map.of("k", "v"), Instant.now())),
                List.of(new RuntimeSnapshot.SerializableConversationState(
                        "conv-9", List.of("m1"), Map.of())),
                List.of(new RuntimeSnapshot.SerializableExecutionContext(
                        "exec-9", "req-9", "SUCCESS", Map.of())),
                Instant.now()
        );

        RecoveryCoordinator coordinator =
                new RecoveryCoordinator(sessionCache, null, null, null);
        coordinator.restoreFromSnapshot(snapshot);

        assertTrue(sessionCache.getSession("s9", "t9").isPresent());
        assertTrue(sessionCache.getConversation("conv-9", "t9").isPresent());
        assertTrue(sessionCache.getExecution("exec-9", "t9").isPresent());
    }

    @Test
    void recoverTenantRebuildsL1CacheFromL2() {
        EpisodicMemoryRepository episodic = mock(EpisodicMemoryRepository.class);
        when(episodic.findByTenantId("t1", 1000)).thenReturn(List.of("memory-a", "memory-b"));
        when(episodic.findByTenantId("t2", 1000)).thenReturn(List.of("memory-x"));

        InMemoryReflectionRepository reflections = new InMemoryReflectionRepository();
        ReflectionHistory history = new ReflectionHistory(
                "t1", "org-1", "exec-1", "req-1",
                "SUCCESS", 0.9, 40, List.of("Lesson"), null, false, Instant.now()
        );
        reflections.save(history);

        RuntimeRecoveryService service = newRecoveryService(episodic, null, reflections);
        service.recoverTenant("t1");

        CacheClient cacheClient = sessionCache.getCacheClient();
        assertFalse(cacheClient.keys("shree:memory:t1:").isEmpty(),
                "Memories should be rebuilt into L1 after recovery");
        assertTrue(cacheClient.contains("shree:reflection:t1:exec-1"),
                "Reflection history should be rebuilt into L1 after recovery");
    }

    @Test
    void recoverAllIteratesEveryTenant() {
        EpisodicMemoryRepository episodic = mock(EpisodicMemoryRepository.class);
        when(episodic.findAllTenantIds()).thenReturn(java.util.Set.of("t1", "t2"));
        when(episodic.findByTenantId(anyString(), anyInt())).thenReturn(List.of("memory"));

        newRecoveryService(episodic, null, null).recoverAll();

        verify(episodic).findByTenantId("t1", 1000);
        verify(episodic).findByTenantId("t2", 1000);
    }

    @Test
    void recoverTenantClearsThreadTenantContext() {
        EpisodicMemoryRepository episodic = mock(EpisodicMemoryRepository.class);
        when(episodic.findByTenantId(anyString(), anyInt())).thenReturn(List.of());

        newRecoveryService(episodic, null, null).recoverTenant("t1");

        assertEquals("system", com.shreeai.os.platform.runtime.tenant.TenantContext.current().tenantId());
    }
}