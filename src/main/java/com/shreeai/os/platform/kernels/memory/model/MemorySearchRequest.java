package com.shreeai.os.platform.kernels.memory.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>MemorySearchRequest</b>
 *
 * <p>Request object for searching Memories within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory search requests.</li>
 *   <li>Encapsulates search criteria and parameters.</li>
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
 * @param query the search query string
 * @param from the start date for date range search (optional)
 * @param to the end date for date range search (optional)
 * @param tags the tags to filter by (optional)
 */
public record MemorySearchRequest(
    String query,
    Instant from,
    Instant to,
    java.util.Set<String> tags
) {
    /**
     * Creates a new MemorySearchRequest with null validation.
     *
     * @param query the search query string
     * @param from the start date for date range search (optional)
     * @param to the end date for date range search (optional)
     * @param tags the tags to filter by (optional)
     * @throws NullPointerException if {@code query} is {@code null}
     */
    public MemorySearchRequest {
        Objects.requireNonNull(query, "query must not be null");
    }
}