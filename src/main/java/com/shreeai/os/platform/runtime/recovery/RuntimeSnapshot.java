package com.shreeai.os.platform.runtime.recovery;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot of runtime state for recovery.
 *
 * <p>Contains all state needed to restore a tenant's runtime after restart:
 * sessions, memories, reflections, and execution history.</p>
 *
 * @param tenantId       tenant identifier
 * @param organizationId organization identifier
 * @param sessions       serialized session contexts
 * @param conversationStates conversation states
 * @param executionContexts  execution contexts
 * @param createdAt      snapshot creation time
 */
public record RuntimeSnapshot(
        String tenantId,
        String organizationId,
        List<SerializableSession> sessions,
        List<SerializableConversationState> conversationStates,
        List<SerializableExecutionContext> executionContexts,
        Instant createdAt
) implements Serializable {
    public RuntimeSnapshot {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        sessions = sessions != null ? Collections.unmodifiableList(sessions) : List.of();
        conversationStates = conversationStates != null ? Collections.unmodifiableList(conversationStates) : List.of();
        executionContexts = executionContexts != null ? Collections.unmodifiableList(executionContexts) : List.of();
        createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public record SerializableSession(
            String sessionId,
            String userId,
            Map<String, Object> attributes,
            Instant lastAccessedAt
    ) implements Serializable {}

    public record SerializableConversationState(
            String sessionId,
            List<String> messageHistory,
            Map<String, Object> metadata
    ) implements Serializable {}

    public record SerializableExecutionContext(
            String executionId,
            String requestId,
            String status,
            Map<String, Object> metadata
    ) implements Serializable {}
}