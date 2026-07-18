package platform.kernels.memory.api;

import platform.kernels.memory.model.CreateMemoryRequest;
import platform.kernels.memory.model.MemoryId;
import platform.kernels.memory.model.MemoryResult;
import platform.kernels.memory.model.UpdateMemoryRequest;

/**
 * <b>MemoryService</b>
 *
 * <p>Responsible for write operations on Memory entities within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Memory state-modifying operations.</li>
 *   <li>Commands request changes — they do not execute them.</li>
 *   <li>Enforces separation between mutation requests and read operations.</li>
 *   <li>Provides a stable contract for other kernels to request Memory changes.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently request Memory operations.</p>
 *
 * <p><b>Immutability:</b> All returned Memory objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Commands never modify state directly. They define
 * the contract for requesting changes. Implementations handle execution.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see platform.kernels.memory.api.MemoryQueryService
 * @see platform.kernels.memory.api.MemorySearchService
 */
public interface MemoryService {

    /**
     * Creates a new Memory within the platform.
     *
     * <p>This command requests the creation of a new Memory with the
     * specified content and metadata. The Memory becomes part of the
     * platform's memory model upon successful creation.</p>
     *
     * <p>The created Memory SHALL be assigned a unique identifier that
     * remains stable for the lifetime of the Memory.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the memory creation request containing all required attributes
     * @return the unique identifier assigned to the newly created Memory
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    MemoryId createMemory(CreateMemoryRequest request);

    /**
     * Updates an existing Memory within the platform.
     *
     * <p>This command requests an update to an existing Memory. Updates
     * preserve the Memory's continuity while allowing attribute modification.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the memory update request containing the updated attributes
     * @return a {@link MemoryResult} indicating success or failure
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    MemoryResult updateMemory(UpdateMemoryRequest request);

    /**
     * Deletes a Memory from the platform.
     *
     * <p>This command requests the deletion of a Memory. The Memory is
     * marked for deletion and will be permanently removed according to
     * the platform's lifecycle policies.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to delete
     * @return a {@link MemoryResult} indicating success or failure
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    MemoryResult deleteMemory(MemoryId id);

    /**
     * Archives a Memory within the platform.
     *
     * <p>This command requests the archival of a Memory. Archived memories
     * are preserved but not actively accessible through normal queries.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to archive
     * @return a {@link MemoryResult} indicating success or failure
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    MemoryResult archiveMemory(MemoryId id);

    /**
     * Restores an archived Memory to active status.
     *
     * <p>This command requests the restoration of an archived Memory.
     * Restored memories become actively accessible again.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to restore
     * @return a {@link MemoryResult} indicating success or failure
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    MemoryResult restoreMemory(MemoryId id);
}