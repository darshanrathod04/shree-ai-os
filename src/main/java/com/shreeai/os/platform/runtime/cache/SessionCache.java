package com.shreeai.os.platform.runtime.cache;

import com.shreeai.os.platform.runtime.recovery.RuntimeSnapshot;
import com.shreeai.os.platform.runtime.reflection.ReflectionHistory;

import java.util.List;
import java.util.Optional;

/**
 * <b>SessionCache</b>
 *
 * <p>High-level session cache abstraction for persisting runtime state
 * (SessionContext, ConversationState, ExecutionContext). Delegates to
 * a {@link CacheClient} implementation for storage.</p>
 *
 * <p>Supports TTL-based expiration, cache eviction, and tenant-isolated keys.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State (L1)</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface SessionCache {

    /**
     * Stores a session context.
     *
     * @param sessionId   the session identifier (never null)
     * @param tenantId    the tenant identifier (never null)
     * @param contextJson serialized session context (never null)
     * @param ttlSeconds  TTL in seconds, or 0 for default
     */
    void storeSession(String sessionId, String tenantId, String contextJson, long ttlSeconds);

    /**
     * Retrieves a session context.
     *
     * @param sessionId the session identifier
     * @param tenantId  the tenant identifier
     * @return the serialized session context, or empty if not found
     */
    Optional<String> getSession(String sessionId, String tenantId);

    /**
     * Stores conversation state.
     *
     * @param conversationId the conversation identifier (never null)
     * @param tenantId       the tenant identifier (never null)
     * @param stateJson      serialized conversation state (never null)
     * @param ttlSeconds     TTL in seconds, or 0 for default
     */
    void storeConversation(String conversationId, String tenantId, String stateJson, long ttlSeconds);

    /**
     * Retrieves conversation state.
     *
     * @param conversationId the conversation identifier
     * @param tenantId       the tenant identifier
     * @return the serialized conversation state, or empty if not found
     */
    Optional<String> getConversation(String conversationId, String tenantId);

    /**
     * Stores execution context.
     *
     * @param executionId the execution identifier (never null)
     * @param tenantId    the tenant identifier (never null)
     * @param contextJson serialized execution context (never null)
     * @param ttlSeconds  TTL in seconds, or 0 for default
     */
    void storeExecution(String executionId, String tenantId, String contextJson, long ttlSeconds);

    /**
     * Retrieves execution context.
     *
     * @param executionId the execution identifier
     * @param tenantId    the tenant identifier
     * @return the serialized execution context, or empty if not found
     */
    Optional<String> getExecution(String executionId, String tenantId);

    /**
     * Evicts a session entry.
     *
     * @param sessionId the session identifier
     * @param tenantId  the tenant identifier
     * @return true if an entry was removed
     */
    boolean evictSession(String sessionId, String tenantId);

    /**
     * Stores an episodic memory in the L1 cache for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @param content  the serialized memory content (never null)
     */
    void putMemory(String tenantId, String content);

    /**
     * Stores a reflection history record in the L1 cache for a tenant.
     *
     * @param tenantId   the tenant identifier (never null)
     * @param reflection the reflection history record (never null)
     */
    void putReflection(String tenantId, ReflectionHistory reflection);

    /**
     * Stores a serialized session in the L1 cache for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @param session  the serialized session (never null)
     */
    void putSession(String tenantId, RuntimeSnapshot.SerializableSession session);

    /**
     * Stores a serialized conversation state in the L1 cache for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @param state    the serialized conversation state (never null)
     */
    void putConversationState(String tenantId, RuntimeSnapshot.SerializableConversationState state);

    /**
     * Stores a serialized execution context in the L1 cache for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @param context  the serialized execution context (never null)
     */
    void putExecutionContext(String tenantId, RuntimeSnapshot.SerializableExecutionContext context);

    /**
     * Returns all serialized sessions for a tenant (cache enumeration).
     *
     * @param tenantId the tenant identifier (never null)
     * @return the sessions (never null, may be empty)
     */
    List<RuntimeSnapshot.SerializableSession> getSessions(String tenantId);

    /**
     * Returns all serialized conversation states for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @return the conversation states (never null, may be empty)
     */
    List<RuntimeSnapshot.SerializableConversationState> getConversationStates(String tenantId);

    /**
     * Returns all serialized execution contexts for a tenant.
     *
     * @param tenantId the tenant identifier (never null)
     * @return the execution contexts (never null, may be empty)
     */
    List<RuntimeSnapshot.SerializableExecutionContext> getExecutionContexts(String tenantId);

    /**
     * Returns the underlying cache client for size/clear operations.
     *
     * @return the cache client
     */
    CacheClient getCacheClient();
}