package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemoryImport</b>
 *
 * <p>Represents a request to import a Memory into the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory import requests.</li>
 *   <li>Encapsulates exported memory data for import.</li>
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
 * @param memory the memory to import
 * @param importedAt when the import is requested
 * @param source the source system or kernel
 */
public record MemoryImport(
    Memory memory,
    Instant importedAt,
    String source
) {
    /**
     * Creates a new MemoryImport with null validation.
     *
     * @param memory the memory to import
     * @param importedAt when the import is requested
     * @param source the source system or kernel
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryImport {
        Objects.requireNonNull(memory, "memory must not be null");
        Objects.requireNonNull(importedAt, "importedAt must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}