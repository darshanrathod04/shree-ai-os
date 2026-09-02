package com.shreeai.os.platform.kernels.context.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>ConversationSession</b>
 *
 * <p>Lightweight V2 canonical representation of a conversation session.
 * Captures only the fields and lifecycle semantics needed by the
 * validation layer, the pipeline context, and the context kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Identifies a user-conversation scope via {@code sessionId}.</li>
 *   <li>Tracks session creation and last-access timestamps for expiry.</li>
 *   <li>Owns the active {@link ConversationContext} for the session.</li>
 *   <li>Contains no message history or learning-specific state.</li>
 * </ul>
 *
 */
public final class ConversationSession {

    /** Default session timeout: 24 hours. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofHours(24);

    private final String sessionId;
    private final String userId;
    private final ConversationContext context;
    private final Instant createdAt;
    private volatile Instant lastAccessedAt;

    public ConversationSession(String sessionId, String userId, ConversationContext context,
                               Instant createdAt, Instant lastAccessedAt) {
        Objects.requireNonNull(sessionId, "ConversationSession sessionId must not be null");
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("ConversationSession sessionId must not be blank");
        }
        Objects.requireNonNull(createdAt, "ConversationSession createdAt must not be null");
        Objects.requireNonNull(lastAccessedAt, "ConversationSession lastAccessedAt must not be null");
        this.sessionId = sessionId;
        this.userId = userId;
        this.context = context;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
    }

    public static ConversationSession create(String userId) {
        Instant now = Instant.now();
        return new ConversationSession(UUID.randomUUID().toString(), userId, null, now, now);
    }

    public static ConversationSession forTesting(String sessionId, String userId) {
        Instant now = Instant.now();
        return new ConversationSession(sessionId, userId, null, now, now);
    }

    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public ConversationContext getContext() { return context; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }

    public boolean isExpired() { return isExpired(DEFAULT_TIMEOUT); }
    public boolean isExpired(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        return Duration.between(lastAccessedAt, Instant.now()).compareTo(timeout) > 0;
    }

    public void touch() { this.lastAccessedAt = Instant.now(); }
}