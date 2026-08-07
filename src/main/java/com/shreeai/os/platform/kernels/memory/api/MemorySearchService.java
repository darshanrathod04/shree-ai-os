package com.shreeai.os.platform.kernels.memory.api;

import java.util.List;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.model.Memory;

/**
 * <b>MemorySearchService</b>
 *
 * <p>Defines search operations for Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for all Memory search operations.</li>
 *   <li>Enables flexible querying of Memory content and metadata.</li>
 *   <li>Provides stable contracts for other kernels to search Memories.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently search Memory data.</p>
 *
 * <p><b>Immutability:</b> All returned Memory objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Search operations never modify state. They are pure
 * read operations that return data without side effects.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see MemoryQueryService
 * @see MemoryService
 */
public interface MemorySearchService {

    /**
     * Searches for Memories by text query.
     *
     * <p>Performs a full-text search across all Memories matching the query.
     * The search is case-insensitive and matches against Memory content.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param query the search query string
     * @return an immutable {@link List} of matching Memories
     * @throws IllegalArgumentException if {@code query} is {@code null} or blank
     */
    List<Memory> search(String query);

    /**
     * Searches for Memories by tags.
     *
     * <p>Returns all Memories that have at least one of the specified tags.
     * The returned list is immutable and may be empty if no matches are found.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param tags the set of tags to search for
     * @return an immutable {@link List} of Memories matching the tags
     * @throws IllegalArgumentException if {@code tags} is {@code null} or empty
     */
    List<Memory> searchByTags(java.util.Set<String> tags);

    /**
     * Searches for Memories by date range.
     *
     * <p>Returns all Memories created within the specified date range.
     * The returned list is immutable and may be empty if no matches are found.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param from the start of the date range (inclusive)
     * @param to the end of the date range (inclusive)
     * @return an immutable {@link List} of Memories within the date range
     * @throws IllegalArgumentException if {@code from} or {@code to} is {@code null}
     */
    List<Memory> searchByDate(java.time.Instant from, java.time.Instant to);

    /**
     * Searches for Memories by similarity to a text.
     *
     * <p>Returns Memories with content similar to the specified text,
     * ordered by similarity score (most similar first). The returned
     * list is immutable and may be empty if no matches are found.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param text the reference text for similarity search
     * @return an immutable {@link List} of similar Memories, ordered by relevance
     * @throws IllegalArgumentException if {@code text} is {@code null} or blank
     */
    List<Memory> searchBySimilarity(String text);

    /**
     * Searches for Memories by owner.
     *
     * <p>Returns all Memories owned by the specified Identity. This is
     * a convenience method that delegates to {@link MemoryQueryService#findByOwner(IdentityId)}.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param ownerId the unique identifier of the owning Identity
     * @return an immutable {@link List} of Memories owned by the Identity
     * @throws IllegalArgumentException if {@code ownerId} is {@code null} or blank
     */
    List<Memory> searchByOwner(IdentityId ownerId);
}