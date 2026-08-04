package com.shreeai.os.platform.kernels.context.api;

import com.shreeai.os.platform.kernels.context.model.Context;
import com.shreeai.os.platform.kernels.context.model.ContextId;

import java.util.List;

/**
 * <b>ContextQueryService</b>
 *
 * <p>Defines the contract for querying Context data within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context read operations.</li>
 *   <li>Provides methods to query and retrieve Context data.</li>
 *   <li>Read-only operations that never modify Context state.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently query Context data.</p>
 *
 * <p><b>Immutability:</b> All returned Context objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-ARCH-001</p>
 *
 * @see ContextService
 * @see ContextSnapshotService
 * @see ContextLifecycleService
 */
public interface ContextQueryService {

    /**
     * Finds a Context by its identifier.
     *
     * <p>Retrieves a Context by its unique identifier. Returns an empty Optional
     * if the Context does not exist.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an Optional containing the Context, or empty if not found
     */
    java.util.Optional<Context> findById(ContextId id);

    /**
     * Finds all active Contexts.
     *
     * <p>Retrieves all Contexts that are currently active. Returns an immutable
     * list that may be empty if no active Contexts exist.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @return an immutable list of active Contexts (never null, may be empty)
     */
    List<Context> findActiveContexts();

    /**
     * Checks if a Context exists.
     *
     * <p>Returns true if a Context with the specified identifier exists,
     * false otherwise.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return {@code true} if the Context exists, {@code false} otherwise
     */
    boolean exists(ContextId id);
}