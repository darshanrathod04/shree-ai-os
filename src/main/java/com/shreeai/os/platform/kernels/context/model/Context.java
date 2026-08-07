package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Context</b>
 *
 * <p>Represents the base runtime context abstraction within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates common runtime properties shared by all context types.</li>
 *   <li>Provides a stable data contract for Context operations.</li>
 *   <li>Immutable value object serving as the foundation for specialized contexts.</li>
 * </ul>
 *
 * <p><b>Context Principles:</b></p>
 * <ul>
 *   <li>Context is runtime, temporary, and lightweight.</li>
 *   <li>Context provides situational awareness, not persistent memory.</li>
 *   <li>Context is mutable as a concept through replacement, not mutable objects.</li>
 * </ul>
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
 * @param type the context type (must not be null)
 * @param state the current state (must not be null)
 * @param data the context data (must not be null, defensively copied)
 * @param createdAt when the context was created (must not be null)
 * @param updatedAt when the context was last updated (must not be null)
 */
public record Context(
    ContextId id,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * Creates a new Context with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The data map is defensively copied
     * to ensure immutability.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, will be defensively copied)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     * @return a new Context instance
     * @throws NullPointerException if any required parameter is null
     */
    public static Context of(
        ContextId id,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant createdAt,
        Instant updatedAt
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new Context(id, type, state, unmodifiableData, createdAt, updatedAt);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique identifier (must not be null)
     * @param type the context type (must not be null)
     * @param state the current state (must not be null)
     * @param data the context data (must not be null, must be unmodifiable)
     * @param createdAt when the context was created (must not be null)
     * @param updatedAt when the context was last updated (must not be null)
     */
    public Context {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
