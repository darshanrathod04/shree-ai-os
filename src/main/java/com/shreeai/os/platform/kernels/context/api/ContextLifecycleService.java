package com.shreeai.os.platform.kernels.context.api;

import com.shreeai.os.platform.kernels.context.error.ContextNotFoundException;
import com.shreeai.os.platform.kernels.context.model.ContextId;

/**
 * <b>ContextLifecycleService</b>
 *
 * <p>Defines the contract for managing Context lifecycle operations within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Context lifecycle management.</li>
 *   <li>Provides methods to activate, deactivate, expire, and archive Contexts.</li>
 *   <li>Encapsulates all operations that control Context state transitions.</li>
 * </ul>
 *
 * <p><b>Lifecycle States:</b></p>
 * <ul>
 *   <li>Active - Context is active and can be modified</li>
 *   <li>Suspended - Context is preserved but cannot be modified</li>
 *   <li>Expired - Context has reached its end of life</li>
 *   <li>Archived - Context is archived for historical reference</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently manage Context lifecycles.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-ARCH-001</p>
 *
 * @see ContextService
 * @see ContextQueryService
 * @see ContextSnapshotService
 */
public interface ContextLifecycleService {

    /**
     * Activates a Context.
     *
     * <p>Activates the specified Context, allowing it to be used for operations.
     * A Context can only be activated if it is not already active.</p>
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
     * <p>Deactivates the specified Context, preventing further use. The Context
     * state is preserved but the Context cannot be modified.</p>
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
     * <p>Expires the specified Context, marking it as no longer usable. Expired
     * Contexts cannot be reactivated or modified.</p>
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
     * <p>Archives the specified Context for historical reference. Archived Contexts
     * are preserved but cannot be modified or reactivated.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param id the Context identifier (must not be null)
     * @throws ContextNotFoundException if the Context does not exist
     */
    void archive(ContextId id);
}