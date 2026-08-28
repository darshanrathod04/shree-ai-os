package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ConversationContext</b>
 *
 * <p>Represents the runtime context for active user interaction and dialogue state.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates active user interaction state.</li>
 *   <li>Manages dialogue runtime state and conversational scope.</li>
 *   <li>Provides specialized context for conversation-related operations.</li>
 * </ul>
 *
 * <p><b>Context Type:</b> This is a specialized Context for conversation scenarios.</p>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-CTX-102</p>
 *
 * @param id the unique identifier (must not be null)
 * @param type the context type (must not be null, must be CONVERSATION)
 * @param state the current state (must not be null)
 * @param data the context data (must not be null, defensively copied)
 * @param createdAt when the context was created (must not be null)
 * @param updatedAt when the context was last updated (must not be null)
 * @param conversationId the conversation identifier (must not be null)
 * @param participantId the participant identifier (must not be null)
 * @param turnCount the current turn count (must not be negative)
 */
public record ConversationContext(
    ContextId id,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant createdAt,
    Instant updatedAt,
    String conversationId,
    String participantId,
    int turnCount
) {
    /**
     * Creates a new ConversationContext with validation.
     *
     * <p>All parameters are validated for null and business rules. The data map
     * is defensively copied to ensure immutability.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be CONVERSATION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, will be defensively copied)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param conversationId the conversation identifier (must not be null or blank)
     * @param participantId the participant identifier (must not be null or blank)
     * @param turnCount the current turn count (must not be negative)
     * @return a new ConversationContext instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not CONVERSATION, or if conversationId/participantId are blank, or if turnCount is negative
     */
    public static ConversationContext of(
        ContextId id,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant createdAt,
        Instant updatedAt,
        String conversationId,
        String participantId,
        int turnCount
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(participantId, "participantId must not be null");

        // Validate type is CONVERSATION
        if (type != ContextType.CONVERSATION) {
            throw new IllegalArgumentException("ConversationContext type must be CONVERSATION, got: " + type);
        }

        // Validate string fields are not blank
        if (conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        if (participantId.isBlank()) {
            throw new IllegalArgumentException("participantId must not be blank");
        }

        // Validate turn count is not negative
        if (turnCount < 0) {
            throw new IllegalArgumentException("turnCount must not be negative, got: " + turnCount);
        }

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new ConversationContext(id, type, state, unmodifiableData, createdAt, updatedAt,
            conversationId, participantId, turnCount);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be CONVERSATION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, must be unmodifiable)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param conversationId the conversation identifier (must not be null or blank)
     * @param participantId the participant identifier (must not be null or blank)
     * @param turnCount the current turn count (must not be negative)
     */
    public ConversationContext {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(participantId, "participantId must not be null");

        // Validate type is CONVERSATION
        if (type != ContextType.CONVERSATION) {
            throw new IllegalArgumentException("ConversationContext type must be CONVERSATION, got: " + type);
        }

        // Validate string fields are not blank
        if (conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        if (participantId.isBlank()) {
            throw new IllegalArgumentException("participantId must not be blank");
        }

        // Validate turn count is not negative
        if (turnCount < 0) {
            throw new IllegalArgumentException("turnCount must not be negative, got: " + turnCount);
        }
    }

    /**
     * Returns an unmodifiable view of the context data.
     *
     * <p>This method ensures that the internal data map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of context data
     */
    public Map<String, Object> data() {
        return data;
    }
}
