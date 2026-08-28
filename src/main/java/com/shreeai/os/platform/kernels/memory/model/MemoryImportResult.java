package com.shreeai.os.platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemoryImportResult</b>
 *
 * <p>Represents the result of a Memory import operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory import results.</li>
 *   <li>Encapsulates success/failure status and details.</li>
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
 * @param success whether the import succeeded
 * @param memoryId the ID of the imported memory if successful
 * @param failureMessage the reason for failure if not successful
 * @param importedAt when the import was completed
 */
public record MemoryImportResult(
    boolean success,
    MemoryId memoryId,
    String failureMessage,
    Instant importedAt
) {
    /**
     * Creates a successful import result.
     *
     * @param memoryId the ID of the imported memory
     * @param importedAt when the import was completed
     * @return a successful MemoryImportResult
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public static MemoryImportResult success(MemoryId memoryId, Instant importedAt) {
        Objects.requireNonNull(memoryId, "memoryId must not be null");
        Objects.requireNonNull(importedAt, "importedAt must not be null");
        return new MemoryImportResult(true, memoryId, null, importedAt);
    }

    /**
     * Creates a failed import result.
     *
     * @param failureMessage the reason for failure
     * @param importedAt when the import was attempted
     * @return a failed MemoryImportResult
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public static MemoryImportResult failure(String failureMessage, Instant importedAt) {
        Objects.requireNonNull(failureMessage, "failureMessage must not be null");
        Objects.requireNonNull(importedAt, "importedAt must not be null");
        return new MemoryImportResult(false, null, failureMessage, importedAt);
    }
}