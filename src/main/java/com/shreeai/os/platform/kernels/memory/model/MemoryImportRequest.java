package com.shreeai.os.platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemoryImportRequest</b>
 *
 * <p>Request object for importing Memories into the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory import requests.</li>
 *   <li>Encapsulates import parameters and source information.</li>
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
 * @param source the source system or kernel
 * @param format the import format version
 * @param requestedAt when the import is requested
 */
public record MemoryImportRequest(
    String source,
    String format,
    Instant requestedAt
) {
    /**
     * Creates a new MemoryImportRequest with null validation.
     *
     * @param source the source system or kernel
     * @param format the import format version
     * @param requestedAt when the import is requested
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryImportRequest {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }
}