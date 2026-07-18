package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>CreateMemoryRequest</b>
 *
 * <p>Request object for creating a new Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory creation requests.</li>
 *   <li>Encapsulates all required attributes for Memory creation.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @param content the memory content
 * @param metadata the memory metadata
 * @param createdAt the timestamp when the memory is created
 */
public record CreateMemoryRequest(
    MemoryContent content,
    MemoryMetadata metadata,
    Instant createdAt
) {
    /**
     * Creates a new CreateMemoryRequest with null validation.
     *
     * @param content the memory content
     * @param metadata the memory metadata
     * @param createdAt the timestamp when the memory is created
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public CreateMemoryRequest {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}