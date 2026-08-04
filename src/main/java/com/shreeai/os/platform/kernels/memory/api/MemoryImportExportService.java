package com.shreeai.os.platform.kernels.memory.api;

import com.shreeai.os.platform.kernels.memory.model.MemoryExport;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryImport;
import com.shreeai.os.platform.kernels.memory.model.MemoryImportResult;

/**
 * <b>MemoryImportExportService</b>
 *
 * <p>Defines import and export operations for Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory import/export operations.</li>
 *   <li>Enables memory portability between systems.</li>
 *   <li>Provides stable contracts for memory transfer.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently import/export Memories.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Import/export operations never modify source memories.
 * They create independent copies.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see MemoryService
 * @see MemoryQueryService
 */
public interface MemoryImportExportService {

    /**
     * Exports a Memory for transfer.
     *
     * <p>Creates an exportable representation of a Memory that can be
     * transferred to other systems or kernels. The exported Memory is
     * independent of the original and can be safely modified.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the unique identifier of the Memory to export
     * @return a {@link MemoryExport} containing the exported Memory data
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    MemoryExport exportMemory(MemoryId id);

    /**
     * Imports a Memory into the platform.
     *
     * <p>Creates a new Memory from imported data. The imported Memory
     * is independent of the source and receives a new unique identifier.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the import request containing the Memory data
     * @return a {@link MemoryImportResult} indicating success or failure
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    MemoryImportResult importMemory(MemoryImport request);
}