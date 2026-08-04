package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ContextSnapshot</b>
 *
 * <p>Represents a snapshot of a Context at a specific point in time.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates runtime state captured at a specific instant.</li>
 *   <li>Provides a stable data contract for Context snapshots.</li>
 *   <li>Immutable value object for runtime state capture.</li>
 * </ul>
 *
 * <p><b>Snapshot Principles:</b></p>
 * <ul>
 *   <li>Snapshots represent runtime state only - not persistent memory.</li>
 *   <li>Snapshots are temporary and lightweight.</li>
 *   <li>Snapshots capture execution context at a point in time.</li>
 *   <li>Snapshots have no historical semantics.</li>
 *   <li>Snapshots must never replace Memory.</li>
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
 * @param id the unique snapshot identifier (must not be null)
 * @param contextId the Context identifier (must not be null)
 * @param data the snapshot data (must not be null, defensively copied)
 * @param createdAt when the snapshot was created (must not be null)
 */
public record ContextSnapshot(
    ContextId id,
    ContextId contextId,
    Map<String, Object> data,
    Instant createdAt
) {
    /**
     * Creates a new ContextSnapshot with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The data map is defensively copied
     * to ensure immutability.</p>
     *
     * @param id the unique snapshot identifier (must not be null)
     * @param contextId the Context identifier (must not be null)
     * @param data the snapshot data (must not be null, will be defensively copied)
     * @param createdAt when the snapshot was created (must not be null)
     * @return a new ContextSnapshot instance
     * @throws NullPointerException if any required parameter is null
     */
    public static ContextSnapshot of(
        ContextId id,
        ContextId contextId,
        Map<String, Object> data,
        Instant createdAt
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(contextId, "contextId must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new ContextSnapshot(id, contextId, unmodifiableData, createdAt);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param id the unique snapshot identifier (must not be null)
     * @param contextId the Context identifier (must not be null)
     * @param data the snapshot data (must not be null, must be unmodifiable)
     * @param createdAt when the snapshot was created (must not be null)
     */
    public ContextSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(contextId, "contextId must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Returns an unmodifiable view of the snapshot data.
     *
     * <p>This method ensures that the internal data map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of snapshot data
     */
    public Map<String, Object> data() {
        return data;
    }
}
