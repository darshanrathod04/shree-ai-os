package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MemoryStatistics</b>
 *
 * <p>Represents statistics about Memory usage within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory statistics.</li>
 *   <li>Encapsulates aggregate memory metrics.</li>
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
 * @param totalMemories total number of memories
 * @param activeMemories number of active memories
 * @param archivedMemories number of archived memories
 * @param memoriesByType breakdown by memory type
 * @param memoriesByStatus breakdown by status
 * @param totalAccessCount total number of memory accesses
 * @param averageImportance average importance score
 * @param averageConfidence average confidence score
 * @param lastUpdated when statistics were last calculated
 */
public record MemoryStatistics(
    long totalMemories,
    long activeMemories,
    long archivedMemories,
    Map<MemoryType, Long> memoriesByType,
    Map<MemoryStatus, Long> memoriesByStatus,
    long totalAccessCount,
    double averageImportance,
    double averageConfidence,
    Instant lastUpdated
) {
    /**
     * Creates a new MemoryStatistics with null validation and defensive copying.
     *
     * @param totalMemories total number of memories
     * @param activeMemories number of active memories
     * @param archivedMemories number of archived memories
     * @param memoriesByType breakdown by memory type
     * @param memoriesByStatus breakdown by status
     * @param totalAccessCount total number of memory accesses
     * @param averageImportance average importance score
     * @param averageConfidence average confidence score
     * @param lastUpdated when statistics were last calculated
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryStatistics {
        Objects.requireNonNull(memoriesByType, "memoriesByType must not be null");
        Objects.requireNonNull(memoriesByStatus, "memoriesByStatus must not be null");
        Objects.requireNonNull(lastUpdated, "lastUpdated must not be null");
        memoriesByType = Collections.unmodifiableMap(Map.copyOf(memoriesByType));
        memoriesByStatus = Collections.unmodifiableMap(Map.copyOf(memoriesByStatus));
    }
}