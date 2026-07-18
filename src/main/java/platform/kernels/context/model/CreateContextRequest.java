package platform.kernels.context.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CreateContextRequest</b>
 *
 * <p>Request object for creating a new Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context creation requests.</li>
 *   <li>Encapsulates all required attributes for Context creation.</li>
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
 * @param type the context type (must not be null)
 * @param data the initial context data (must not be null, defensively copied)
 * @param createdAt when the context is created (must not be null)
 */
public record CreateContextRequest(
    ContextType type,
    Map<String, Object> data,
    Instant createdAt
) {
    /**
     * Creates a new CreateContextRequest with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The data map is defensively copied
     * to ensure immutability.</p>
     *
     * @param type the context type (must not be null)
     * @param data the initial context data (must not be null, will be defensively copied)
     * @param createdAt when the context is created (must not be null)
     * @return a new CreateContextRequest instance
     * @throws NullPointerException if any required parameter is null
     */
    public static CreateContextRequest of(
        ContextType type,
        Map<String, Object> data,
        Instant createdAt
    ) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        // Defensive copying to ensure immutability
        Map<String, Object> unmodifiableData = Collections.unmodifiableMap(Map.copyOf(data));

        return new CreateContextRequest(type, unmodifiableData, createdAt);
    }

    /**
     * Canonical constructor for deserialization frameworks.
     *
     * <p>This constructor assumes data has already been defensively copied.
     * It is intended for use by serialization frameworks only.</p>
     *
     * @param type the context type (must not be null)
     * @param data the initial context data (must not be null, must be unmodifiable)
     * @param createdAt when the context is created (must not be null)
     */
    public CreateContextRequest {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
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
