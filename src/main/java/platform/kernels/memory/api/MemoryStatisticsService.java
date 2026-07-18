package platform.kernels.memory.api;

import java.util.Map;
import platform.kernels.memory.model.MemoryStatistics;
import platform.kernels.memory.model.MemoryType;

/**
 * <b>MemoryStatisticsService</b>
 *
 * <p>Defines statistics operations for Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory statistics operations.</li>
 *   <li>Enables monitoring and analytics of Memory usage.</li>
 *   <li>Provides stable contracts for memory metrics.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently request Memory statistics.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Statistics operations never modify state. They are
 * pure read operations that return aggregated data.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see platform.kernels.memory.api.MemoryQueryService
 * @see platform.kernels.memory.api.MemoryStatistics
 */
public interface MemoryStatisticsService {

    /**
     * Retrieves comprehensive Memory statistics.
     *
     * <p>Returns aggregated statistics about Memory usage within the platform,
     * including counts, averages, and breakdowns by type and status.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @return a {@link MemoryStatistics} containing comprehensive memory metrics
     */
    MemoryStatistics getStatistics();

    /**
     * Counts Memories by type.
     *
     * <p>Returns a map of MemoryType to count, showing the distribution
     * of memories across different types.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @return an immutable {@link Map} of MemoryType to count
     */
    Map<MemoryType, Long> countByType();

    /**
     * Returns the total number of Memories.
     *
     * <p>Provides a lightweight operation to get the total count of all
     * Memories in the platform.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @return the total number of Memories
     */
    long totalMemoryCount();

    /**
     * Returns the number of archived Memories.
     *
     * <p>Provides a lightweight operation to get the count of archived
     * Memories in the platform.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @return the number of archived Memories
     */
    long archivedCount();
}