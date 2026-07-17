package platform.kernels.memory.engine;

import platform.kernels.memory.model.Memory;
import platform.kernels.memory.model.MemoryId;

import java.util.List;
import java.util.Set;
import platform.kernels.identity.model.IdentityId;

/**
 * <b>MemoryProcessingEngine</b>
 *
 * <p>Forward-reference interface for the Memory Processing Engine (EIO-MEM-106).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory processing operations.</li>
 *   <li>Performs search, similarity, and content processing.</li>
 *   <li>This is a forward reference only — implementation belongs to EIO-MEM-106.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 */
public interface MemoryProcessingEngine {

    /**
     * Searches for Memories by text query.
     *
     * @param query the search query string
     * @return a list of matching Memories
     */
    List<Memory> search(String query);

    /**
     * Searches for Memories by tags.
     *
     * @param tags the set of tags to search for
     * @return a list of matching Memories
     */
    List<Memory> searchByTags(Set<String> tags);

    /**
     * Searches for Memories by date range.
     *
     * @param from the start of the date range
     * @param to the end of the date range
     * @return a list of matching Memories
     */
    List<Memory> searchByDate(java.time.Instant from, java.time.Instant to);

    /**
     * Searches for Memories by similarity to a text.
     *
     * @param text the reference text for similarity search
     * @return a list of similar Memories
     */
    List<Memory> searchBySimilarity(String text);

    /**
     * Processes a Memory for storage.
     *
     * @param memory the memory to process
     * @return the processed memory
     */
    Memory processForStorage(Memory memory);

    /**
     * Processes a Memory for export.
     *
     * @param memory the memory to export
     * @return the processed memory
     */
    Memory processForExport(Memory memory);

    /**
     * Processes a Memory for import.
     *
     * @param memory the memory to import
     * @return the processed memory
     */
    Memory processForImport(Memory memory);
}