package platform.kernels.context.api;

import platform.kernels.context.model.CreateContextRequest;
import platform.kernels.context.model.Context;
import platform.kernels.context.model.ContextId;
import platform.kernels.context.model.UpdateContextRequest;

/**
 * <b>ContextService</b>
 *
 * <p>Defines the contract for managing Context lifecycle operations within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context creation, updates, and lifecycle management.</li>
 *   <li>Encapsulates all operations that modify Context state.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Context Responsibilities:</b></p>
 * <ul>
 *   <li>Active conversation context</li>
 *   <li>Runtime execution context</li>
 *   <li>Current task context</li>
 *   <li>Session context</li>
 *   <li>Temporary working context</li>
 *   <li>Environmental context</li>
 *   <li>Context lifecycle</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently access and modify Context data.</p>
 *
 * <p><b>Immutability:</b> All returned Context objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-ARCH-001</p>
 *
 * @see platform.kernels.context.api.ContextQueryService
 * @see platform.kernels.context.api.ContextSnapshotService
 * @see platform.kernels.context.api.ContextLifecycleService
 */
public interface ContextService {

    /**
     * Creates a new Context.
     *
     * <p>Creates a new Context with the specified parameters. The Context
     * represents temporary runtime state and is not persistent.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the creation request (must not be null)
     * @return the created Context (never null)
     * @throws IllegalArgumentException if the request is invalid
     */
    Context createContext(CreateContextRequest request);

    /**
     * Updates an existing Context.
     *
     * <p>Updates the specified Context with new values. Only non-null fields
     * in the request will be updated.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param request the update request (must not be null)
     * @return the updated Context (never null)
     * @throws platform.kernels.context.error.ContextNotFoundException if the Context does not exist
     * @throws IllegalArgumentException if the request is invalid
     */
    Context updateContext(UpdateContextRequest request);

    /**
     * Clears a Context.
     *
     * <p>Clears all data from the specified Context, resetting it to its
     * initial state. The Context remains active but empty.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws platform.kernels.context.error.ContextNotFoundException if the Context does not exist
     */
    void clearContext(ContextId contextId);

    /**
     * Suspends a Context.
     *
     * <p>Suspends the specified Context, preserving its state but preventing
     * further modifications until resumed.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws platform.kernels.context.error.ContextNotFoundException if the Context does not exist
     */
    void suspendContext(ContextId contextId);

    /**
     * Resumes a suspended Context.
     *
     * <p>Resumes the specified Context, allowing modifications to continue.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @throws platform.kernels.context.error.ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is not suspended
     */
    void resumeContext(ContextId contextId);
}