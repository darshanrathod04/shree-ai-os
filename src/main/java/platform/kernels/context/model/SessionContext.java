package platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>SessionContext</b>
 *
 * <p>Represents the runtime context for current user session and session lifecycle.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates current user session state.</li>
 *   <li>Manages runtime session information and session lifecycle.</li>
 *   <li>Provides specialized context for session-related operations.</li>
 * </ul>
 *
 * <p><b>Context Type:</b> This is a specialized Context for session scenarios.</p>
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
 * @param type the context type (must not be null, must be SESSION)
 * @param state the current state (must not be null)
 * @param data the context data (must not be null, defensively copied)
 * @param createdAt when the context was created (must not be null)
 * @param updatedAt when the context was last updated (must not be null)
 * @param sessionId the session identifier (must not be null or blank)
 * @param userId the user identifier (must not be null or blank)
 * @param sessionStartTime when the session started (must not be null)
 */
public record SessionContext(
    ContextId id,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant createdAt,
    Instant updatedAt,
    String sessionId,
    String userId,
    Instant sessionStartTime
) {
    /**
     * Creates a new SessionContext with validation.
     *
     * <p>All parameters are validated for null and business rules. The data map
     * is defensively copied to ensure immutability.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be SESSION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, will be defensively copied)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param sessionId the session identifier (must not be null or blank)
     * @param userId the user identifier (must not be null or blank)
     * @param sessionStartTime when the session started (must not be null)
     * @return a new SessionContext instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is not SESSION, or if sessionId/userId are blank
     */
    public static SessionContext of(
        ContextId id,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant createdAt,
        Instant updatedAt,
        String sessionId,
        String userId,
        Instant sessionStartTime
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionStartTime, "sessionStartTime must not be null");

        // Validate type is SESSION
        if (type != ContextType.SESSION) {
            throw new IllegalArgumentException("SessionContext type must be SESSION, got: " + type);
        }

        // Validate string fields are not blank
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new SessionContext(id, type, state, unmodifiableData, createdAt, updatedAt,
            sessionId, userId, sessionStartTime);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null, must be SESSION)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, must be unmodifiable)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @param sessionId the session identifier (must not be null or blank)
     * @param userId the user identifier (must not be null or blank)
     * @param sessionStartTime when the session started (must not be null)
     */
    public SessionContext {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(sessionStartTime, "sessionStartTime must not be null");

        // Validate type is SESSION
        if (type != ContextType.SESSION) {
            throw new IllegalArgumentException("SessionContext type must be SESSION, got: " + type);
        }

        // Validate string fields are not blank
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
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
