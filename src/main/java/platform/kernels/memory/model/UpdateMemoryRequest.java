package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>UpdateMemoryRequest</b>
 *
 * <p>Request object for updating an existing Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory update requests.</li>
 *   <li>Encapsulates all attributes that can be updated in a Memory.</li>
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
 * @param memoryId the unique identifier of the memory to update
 * @param content the updated memory content (optional)
 * @param metadata the updated memory metadata (optional)
 * @param updatedAt the timestamp when the update occurred
 */
public record UpdateMemoryRequest(
    MemoryId memoryId,
    MemoryContent content,
    MemoryMetadata metadata,
    Instant updatedAt
) {
    /**
     * Creates a new UpdateMemoryRequest with null validation.
     *
     * @param memoryId the unique identifier of the memory to update
     * @param content the updated memory content (optional)
     * @param metadata the updated memory metadata (optional)
     * @param updatedAt the timestamp when the update occurred
     * @throws NullPointerException if {@code memoryId} or {@code updatedAt} is {@code null}
     */
    public UpdateMemoryRequest {
        Objects.requireNonNull(memoryId, "memoryId must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}