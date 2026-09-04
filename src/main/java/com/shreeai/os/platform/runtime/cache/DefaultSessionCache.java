package com.shreeai.os.platform.runtime.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shreeai.os.platform.runtime.recovery.RuntimeSnapshot;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <b>DefaultSessionCache</b>
 *
 * <p>Implementation of {@link SessionCache} that delegates to a {@link CacheClient}.
 * Keys are prefixed and tenant-isolated to prevent cross-tenant cache access.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State (L1)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultSessionCache implements SessionCache {

    private static final long DEFAULT_TTL_SECONDS = 3600; // 1 hour
    private static final String SESSION_PREFIX = "shree:session:";
    private static final String CONVERSATION_PREFIX = "shree:conversation:";
    private static final String EXECUTION_PREFIX = "shree:execution:";
    private static final String MEMORY_PREFIX = "shree:memory:";
    private static final String REFLECTION_PREFIX = "shree:reflection:";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final CacheClient cacheClient;
    private final long defaultTtlSeconds;

    /**
     * Creates a DefaultSessionCache with default TTL.
     *
     * @param cacheClient the cache client (never null)
     */
    public DefaultSessionCache(CacheClient cacheClient) {
        this(cacheClient, DEFAULT_TTL_SECONDS);
    }

    /**
     * Creates a DefaultSessionCache with a specified default TTL.
     *
     * @param cacheClient      the cache client (never null)
     * @param defaultTtlSeconds the default TTL in seconds
     */
    public DefaultSessionCache(CacheClient cacheClient, long defaultTtlSeconds) {
        this.cacheClient = Objects.requireNonNull(cacheClient, "cacheClient must not be null");
        this.defaultTtlSeconds = Math.max(0, defaultTtlSeconds);
    }

    @Override
    public void storeSession(String sessionId, String tenantId, String contextJson, long ttlSeconds) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(contextJson, "contextJson must not be null");
        String key = sessionKey(sessionId, tenantId);
        cacheClient.put(key, contextJson, ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds);
    }

    @Override
    public Optional<String> getSession(String sessionId, String tenantId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        String key = sessionKey(sessionId, tenantId);
        return cacheClient.get(key);
    }

    @Override
    public void storeConversation(String conversationId, String tenantId, String stateJson, long ttlSeconds) {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(stateJson, "stateJson must not be null");
        String key = conversationKey(conversationId, tenantId);
        cacheClient.put(key, stateJson, ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds);
    }

    @Override
    public Optional<String> getConversation(String conversationId, String tenantId) {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        String key = conversationKey(conversationId, tenantId);
        return cacheClient.get(key);
    }

    @Override
    public void storeExecution(String executionId, String tenantId, String contextJson, long ttlSeconds) {
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(contextJson, "contextJson must not be null");
        String key = executionKey(executionId, tenantId);
        cacheClient.put(key, contextJson, ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds);
    }

    @Override
    public Optional<String> getExecution(String executionId, String tenantId) {
        Objects.requireNonNull(executionId, "executionId must not be null");
        String key = executionKey(executionId, tenantId);
        return cacheClient.get(key);
    }

    @Override
    public boolean evictSession(String sessionId, String tenantId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        String key = sessionKey(sessionId, tenantId);
        return cacheClient.evict(key);
    }

    @Override
    public CacheClient getCacheClient() {
        return cacheClient;
    }

    @Override
    public void putMemory(String tenantId, String content) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        String key = MEMORY_PREFIX + tenantId + ":" + UUID.randomUUID();
        cacheClient.put(key, content, defaultTtlSeconds);
    }

    @Override
    public void putReflection(String tenantId, ReflectionHistory reflection) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(reflection, "reflection must not be null");
        String json = toJson(reflection);
        if (json != null) {
            String key = REFLECTION_PREFIX + tenantId + ":" + reflection.executionId();
            cacheClient.put(key, json, defaultTtlSeconds);
        }
    }

    @Override
    public void putSession(String tenantId, RuntimeSnapshot.SerializableSession session) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(session, "session must not be null");
        String json = toJson(session);
        if (json != null) {
            String key = sessionKey(session.sessionId(), tenantId);
            cacheClient.put(key, json, defaultTtlSeconds);
        }
    }

    @Override
    public void putConversationState(String tenantId, RuntimeSnapshot.SerializableConversationState state) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(state, "state must not be null");
        String json = toJson(state);
        if (json != null) {
            String key = conversationKey(state.sessionId(), tenantId);
            cacheClient.put(key, json, defaultTtlSeconds);
        }
    }

    @Override
    public void putExecutionContext(String tenantId, RuntimeSnapshot.SerializableExecutionContext context) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(context, "context must not be null");
        String json = toJson(context);
        if (json != null) {
            String key = executionKey(context.executionId(), tenantId);
            cacheClient.put(key, json, defaultTtlSeconds);
        }
    }

    @Override
    public List<RuntimeSnapshot.SerializableSession> getSessions(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        String prefix = SESSION_PREFIX + tenantId + ":";
        return cacheClient.keys(prefix).stream()
                .map(cacheClient::get)
                .flatMap(Optional::stream)
                .map(json -> fromJson(json, RuntimeSnapshot.SerializableSession.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<RuntimeSnapshot.SerializableConversationState> getConversationStates(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        String prefix = CONVERSATION_PREFIX + tenantId + ":";
        return cacheClient.keys(prefix).stream()
                .map(cacheClient::get)
                .flatMap(Optional::stream)
                .map(json -> fromJson(json, RuntimeSnapshot.SerializableConversationState.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<RuntimeSnapshot.SerializableExecutionContext> getExecutionContexts(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        String prefix = EXECUTION_PREFIX + tenantId + ":";
        return cacheClient.keys(prefix).stream()
                .map(cacheClient::get)
                .flatMap(Optional::stream)
                .map(json -> fromJson(json, RuntimeSnapshot.SerializableExecutionContext.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    private String sessionKey(String sessionId, String tenantId) {
        return SESSION_PREFIX + tenantId + ":" + sessionId;
    }

    private String conversationKey(String conversationId, String tenantId) {
        return CONVERSATION_PREFIX + tenantId + ":" + conversationId;
    }

    private String executionKey(String executionId, String tenantId) {
        return EXECUTION_PREFIX + tenantId + ":" + executionId;
    }
}