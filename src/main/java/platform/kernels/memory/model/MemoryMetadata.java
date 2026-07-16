package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import platform.kernels.identity.model.IdentityId;

/**
 * <b>MemoryMetadata</b>
 *
 * <p>Represents metadata associated with a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory metadata.</li>
 *   <li>Encapsulates tags, importance, and other attributes.</li>
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
 * @param memoryId the unique identifier of the memory
 * @param type the type of memory
 * @param status the current status
 * @param visibility the visibility level
 * @param owner the identity that owns this memory
 * @param tags set of tags for categorization
 * @param importance importance score (0.0 to 1.0)
 * @param confidence confidence score (0.0 to 1.0)
 * @param source the source of the memory
 * @param createdAt when the memory was created
 * @param updatedAt when the memory was last updated
 * @param accessedAt when the memory was last accessed
 * @param accessCount number of times accessed
 */
public record MemoryMetadata(
    MemoryId memoryId,
    MemoryType type,
    MemoryStatus status,
    MemoryVisibility visibility,
    IdentityId owner,
    Set<String> tags,
    double importance,
    double confidence,
    String source,
    Instant createdAt,
    Instant updatedAt,
    Instant accessedAt,
    long accessCount
) {
    /**
     * Creates a new MemoryMetadata with null validation and defensive copying.
     *
     * @param memoryId the unique identifier of the memory
     * @param type the type of memory
     * @param status the current status
     * @param visibility the visibility level
     * @param owner the identity that owns this memory
     * @param tags set of tags for categorization
     * @param importance importance score (0.0 to 1.0)
     * @param confidence confidence score (0.0 to 1.0)
     * @param source the source of the memory
     * @param createdAt when the memory was created
     * @param updatedAt when the memory was last updated
     * @param accessedAt when the memory was last accessed
     * @param accessCount number of times accessed
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryMetadata {
        Objects.requireNonNull(memoryId, "memoryId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(visibility, "visibility must not be null");
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(tags, "tags must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(accessedAt, "accessedAt must not be null");
        tags = Collections.unmodifiableSet(Set.copyOf(tags));
    }
}