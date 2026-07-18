package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>Memory</b>
 *
 * <p>Represents a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the core Memory data contract.</li>
 *   <li>Encapsulates content and metadata.</li>
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
 * @param id the unique identifier for the memory
 * @param content the memory content
 * @param metadata the memory metadata
 * @param createdAt when the memory was created
 * @param updatedAt when the memory was last updated
 */
public record Memory(
    MemoryId id,
    MemoryContent content,
    MemoryMetadata metadata,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * Creates a new Memory with null validation.
     *
     * @param id the unique identifier for the memory
     * @param content the memory content
     * @param metadata the memory metadata
     * @param createdAt when the memory was created
     * @param updatedAt when the memory was last updated
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public Memory {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}