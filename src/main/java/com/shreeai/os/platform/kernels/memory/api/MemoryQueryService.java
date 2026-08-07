package com.shreeai.os.platform.kernels.memory.api;

import java.util.List;
import java.util.Optional;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;

/**
 * <b>MemoryQueryService</b>
 *
 * <p>Defines read-only operations for retrieving Memory data within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Memory read-only operations.</li>
 *   <li>Queries retrieve data — they never modify state.</li>
 *   <li>Enforces strict separation between read and write operations.</li>
 *   <li>Provides stable contracts for other kernels to retrieve Memory information.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently query Memory data.</p>
 *
 * <p><b>Immutability:</b> All returned Memory objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Queries never modify state. They are pure read operations
 * that return data without side effects.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see MemoryService
 * @see MemorySearchService
 */
public interface MemoryQueryService {

    /**
     * Finds a Memory by its unique identifier.
     *
     * <p>Returns the Memory if found, or an empty Optional if no Memory
     * exists with the given identifier.</p>
     *
     * <p>This is the primary method for verifying Memory existence and
     * retrieving Memory information.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to find
     * @return an {@link Optional} containing the Memory if found,
     *         or an empty Optional if not found
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    Optional<Memory> findById(MemoryId id);

    /**
     * Finds all Memories of a specific type.
     *
     * <p>Returns all Memories matching the specified type. The returned
     * list is immutable and may be empty if no matches are found.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param type the Memory type to search for
     * @return an immutable {@link List} of Memories of the specified type
     * @throws IllegalArgumentException if {@code type} is {@code null}
     */
    List<Memory> findByType(MemoryType type);

    /**
     * Finds all Memories owned by a specific Identity.
     *
     * <p>Returns all Memories owned by the specified Identity. The returned
     * list is immutable and may be empty if no matches are found.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param owner the unique identifier of the owning Identity
     * @return an immutable {@link List} of Memories owned by the Identity
     * @throws IllegalArgumentException if {@code owner} is {@code null}
     */
    List<Memory> findByOwner(IdentityId owner);

    /**
     * Retrieves the most recent Memories.
     *
     * <p>Returns the most recently created or updated Memories, up to
     * the specified limit. The returned list is immutable and may be empty.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param limit the maximum number of recent Memories to return
     * @return an immutable {@link List} of recent Memories
     * @throws IllegalArgumentException if {@code limit} is negative
     */
    List<Memory> getRecent(int limit);

    /**
     * Checks if a Memory exists.
     *
     * <p>Returns true if a Memory with the specified identifier exists,
     * false otherwise. This is a lightweight operation for existence checks.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to check
     * @return {@code true} if the Memory exists, {@code false} otherwise
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    boolean exists(MemoryId id);
}