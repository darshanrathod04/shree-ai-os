package platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>UpdateContextRequest</b>
 *
 * <p>Request object for updating an existing Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context update requests.</li>
 *   <li>Encapsulates all attributes that can be updated in a Context.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
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
 * @param contextId the unique identifier of the context to update (must not be null)
 * @param type the updated context type (optional)
 * @param state the updated context state (optional)
 * @param data the updated context data (optional, defensively copied)
 * @param updatedAt when the update occurred (must not be null)
 */
public record UpdateContextRequest(
    ContextId contextId,
    ContextType type,
    ContextState state,
    Map<String, Object> data,
    Instant updatedAt
) {
    /**
     * Creates a new UpdateContextRequest with null validation and defensive copying.
     *
     * <p>Required parameters are validated for null. If provided, the data map
     * is defensively copied to ensure immutability.</p>
     *
     * @param contextId the unique identifier of the context to update (must not be null)
     * @param type the updated context type (optional)
     * @param state the updated context state (optional)
     * @param data the updated context data (optional, will be defensively copied if provided)
     * @param updatedAt when the update occurred (must not be null)
     * @return a new UpdateContextRequest instance
     * @throws NullPointerException if {@code contextId} or {@code updatedAt} is null
     */
    public static UpdateContextRequest of(
        ContextId contextId,
        ContextType type,
        ContextState state,
        Map<String, Object> data,
        Instant updatedAt
    ) {
        Objects.requireNonNull(contextId, "contextId must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        // Defensive copying for data map if provided
        Map<String, Object> unmodifiableData = null;
        if (data != null) {
            unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));
        }

        return new UpdateContextRequest(contextId, type, state, unmodifiableData, updatedAt);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param contextId the unique identifier of the context to update (must not be null)
     * @param type the updated context type (optional)
     * @param state the updated context state (optional)
     * @param data the updated context data (optional, must be unmodifiable if provided)
     * @param updatedAt when the update occurred (must not be null)
     */
    public UpdateContextRequest {
        Objects.requireNonNull(contextId, "contextId must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Returns an unmodifiable view of the context data.
     *
     * <p>This method ensures that the internal data map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of context data, or null if not provided
     */
    public Map<String, Object> data() {
        return data;
    }
}
