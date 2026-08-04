package com.shreeai.os.platform.kernels.context.service;

import com.shreeai.os.platform.kernels.context.error.ContextNotFoundException;
import com.shreeai.os.platform.kernels.context.model.Context;
import com.shreeai.os.platform.kernels.context.model.ContextId;
import com.shreeai.os.platform.kernels.context.model.ContextSnapshot;
import com.shreeai.os.platform.kernels.context.model.CreateContextRequest;
import com.shreeai.os.platform.kernels.context.model.UpdateContextRequest;

import java.util.List;
import java.util.Optional;

/**
 * <b>ContextProcessingEngine</b>
 *
 * <p>Defines the contract for Context processing operations within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Performs all Context processing operations.</li>
 *   <li>Encapsulates business logic for Context management.</li>
 *   <li>Handles persistence and state mutations.</li>
 *   <li>Provides the implementation layer for Context operations.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Contains all business logic - service delegates to this layer.</li>
 *   <li>Stateless and thread-safe.</li>
 *   <li>No validation - validation is performed by the service layer.</li>
 *   <li>No API concerns - pure processing logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-105, EIO-ARCH-001</p>
 *
 * @see DefaultContextService
 */
public interface ContextProcessingEngine {

    /**
     * Creates a new Context.
     *
     * <p>Performs the actual creation logic including persistence and state initialization.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the creation request (must not be null, assumed valid)
     * @return the created Context (never null)
     */
    Context createContext(CreateContextRequest request);

    /**
     * Updates an existing Context.
     *
     * <p>Performs the actual update logic including persistence and state mutation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the update request (must not be null, assumed valid)
     * @return the updated Context (never null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    Context updateContext(UpdateContextRequest request);

    /**
     * Clears a Context.
     *
     * <p>Performs the actual clearing logic including state reset.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    void clearContext(ContextId contextId);

    /**
     * Suspends a Context.
     *
     * <p>Performs the actual suspension logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    void suspendContext(ContextId contextId);

    /**
     * Resumes a suspended Context.
     *
     * <p>Performs the actual resumption logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is not suspended
     */
    void resumeContext(ContextId contextId);

    /**
     * Finds a Context by its identifier.
     *
     * <p>Performs the actual retrieval logic from persistence.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an Optional containing the Context, or empty if not found
     */
    Optional<Context> findById(ContextId id);

    /**
     * Finds all active Contexts.
     *
     * <p>Performs the actual query logic to retrieve all active Contexts.</p>
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
     * <p>Performs the actual existence check.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return {@code true} if the Context exists, {@code false} otherwise
     */
    boolean exists(ContextId id);

    /**
     * Activates a Context.
     *
     * <p>Performs the actual activation logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is already active
     */
    void activate(ContextId id);

    /**
     * Deactivates a Context.
     *
     * <p>Performs the actual deactivation logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is not active
     */
    void deactivate(ContextId id);

    /**
     * Expires a Context.
     *
     * <p>Performs the actual expiration logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    void expire(ContextId id);

    /**
     * Archives a Context.
     *
     * <p>Performs the actual archival logic including state transition.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    void archive(ContextId id);

    /**
     * Creates a snapshot of a Context.
     *
     * <p>Performs the actual snapshot creation logic.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return the created ContextSnapshot (never null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    ContextSnapshot createSnapshot(ContextId id);

    /**
     * Retrieves the latest snapshot of a Context.
     *
     * <p>Performs the actual retrieval logic for the latest snapshot.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an Optional containing the latest ContextSnapshot, or empty if none exist
     */
    Optional<ContextSnapshot> latestSnapshot(ContextId id);

    /**
     * Retrieves the snapshot history of a Context.
     *
     * <p>Performs the actual retrieval logic for snapshot history.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This is a read-only operation.</p>
     *
     * @param id the Context identifier (must not be null)
     * @return an immutable list of ContextSnapshots (never null, may be empty)
     */
    List<ContextSnapshot> history(ContextId id);
}