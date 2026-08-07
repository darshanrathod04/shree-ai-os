package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.memory.model.*;

/**
 * <b>MemoryProcessingEngine</b>
 *
 * <p>Defines the contract for Memory processing operations within the Memory Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Prepares processing results for Memory operations.</li>
 *   <li>Normalizes processing input and coordinates internal processing workflow.</li>
 *   <li>Never stores data, validates requests, or performs business logic.</li>
 *   <li>Never accesses repositories, databases, filesystems, or networks.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no instance fields, no mutable state.</li>
 *   <li>Thread-safe — safe for concurrent access.</li>
 *   <li>Deterministic — same input always produces same output.</li>
 *   <li>Side-effect free — no external interactions.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-MEM-106</p>
 *
 * @see MemoryProcessingResult
 * @see DefaultMemoryProcessingEngine
 */
public interface MemoryProcessingEngine {

    /**
     * Processes a Memory creation request.
     *
     * <p>Prepares the processing result for creating a new Memory. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param request the creation request (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult processCreate(CreateMemoryRequest request);

    /**
     * Processes a Memory update request.
     *
     * <p>Prepares the processing result for updating an existing Memory. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param request the update request (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult processUpdate(UpdateMemoryRequest request);

    /**
     * Processes a Memory deletion request.
     *
     * <p>Prepares the processing result for deleting a Memory. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param id the Memory identifier (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult processDelete(MemoryId id);

    /**
     * Processes a Memory archive request.
     *
     * <p>Prepares the processing result for archiving a Memory. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param id the Memory identifier (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult processArchive(MemoryId id);

    /**
     * Processes a Memory restore request.
     *
     * <p>Prepares the processing result for restoring an archived Memory. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param id the Memory identifier (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult processRestore(MemoryId id);

    /**
     * Prepares a search operation.
     *
     * <p>Prepares the processing result for a search request. This method
     * normalizes input and creates processing metadata without executing the search.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not execute searches or modify state.</p>
     *
     * @param request the search request (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult prepareSearch(MemorySearchRequest request);

    /**
     * Prepares an import operation.
     *
     * <p>Prepares the processing result for an import request. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param request the import request (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult prepareImport(MemoryImportRequest request);

    /**
     * Prepares an export operation.
     *
     * <p>Prepares the processing result for an export request. This method
     * normalizes input and creates processing metadata without persisting anything.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not store data or modify state.</p>
     *
     * @param request the export request (must not be null)
     * @return the processing result
     */
    MemoryProcessingResult prepareExport(MemoryExportRequest request);
}