package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemoryExportRequest</b>
 *
 * <p>Request object for exporting Memories from the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory export requests.</li>
 *   <li>Encapsulates export parameters and format information.</li>
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
 * @param format the export format version
 * @param requestedAt when the export is requested
 */
public record MemoryExportRequest(
    String format,
    Instant requestedAt
) {
    /**
     * Creates a new MemoryExportRequest with null validation.
     *
     * @param format the export format version
     * @param requestedAt when the export is requested
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryExportRequest {
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }
}