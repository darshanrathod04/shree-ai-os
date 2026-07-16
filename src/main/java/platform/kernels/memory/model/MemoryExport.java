package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemoryExport</b>
 *
 * <p>Represents an exported Memory for transfer between systems.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory export data.</li>
 *   <li>Encapsulates all data needed to reconstruct a Memory.</li>
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
 * @param memory the exported memory
 * @param exportedAt when the export was created
 * @param format the export format version
 */
public record MemoryExport(
    Memory memory,
    Instant exportedAt,
    String format
) {
    /**
     * Creates a new MemoryExport with null validation.
     *
     * @param memory the exported memory
     * @param exportedAt when the export was created
     * @param format the export format version
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryExport {
        Objects.requireNonNull(memory, "memory must not be null");
        Objects.requireNonNull(exportedAt, "exportedAt must not be null");
        Objects.requireNonNull(format, "format must not be null");
    }
}