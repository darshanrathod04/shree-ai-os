package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.ContextId;
import com.shreeai.os.platform.kernels.context.model.CreateContextRequest;
import com.shreeai.os.platform.kernels.context.model.UpdateContextRequest;

/**
 * <b>ContextProcessingEngine</b>
 *
 * <p>Defines the contract for Context runtime processing operations within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Performs runtime Context processing operations.</li>
 *   <li>Prepares Context instances for runtime use.</li>
 *   <li>Coordinates internal processing flow.</li>
 *   <li>Produces immutable processing results.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless - no mutable instance state.</li>
 *   <li>Thread-safe - immutable operations.</li>
 *   <li>No validation - validation is performed by the service layer.</li>
 *   <li>No persistence - pure runtime processing only.</li>
 *   <li>No business orchestration - processes only.</li>
 * </ul>
 *
 * <p><b>Processing Philosophy:</b></p>
 * <p>The engine performs pure processing operations. It receives validated inputs,
 * performs deterministic processing, and returns immutable results. The engine
 * never coordinates API requests, never validates inputs, and never accesses
 * persistence or repositories.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-106, EIO-ARCH-001</p>
 *
 * @see ContextProcessingResult
 * @see DefaultContextProcessingEngine
 */
public interface ContextProcessingEngine {

    /**
     * Processes Context creation.
     *
     * <p>Prepares a new Context instance for runtime use based on the creation request.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param request the creation request (must not be null, assumed valid)
     * @return the processing result (never null)
     */
    ContextProcessingResult processCreate(CreateContextRequest request);

    /**
     * Processes Context update.
     *
     * <p>Prepares an updated Context instance for runtime use based on the update request.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param request the update request (must not be null, assumed valid)
     * @return the processing result (never null)
     */
    ContextProcessingResult processUpdate(UpdateContextRequest request);

    /**
     * Processes Context clearing.
     *
     * <p>Prepares the Context clearing operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processClear(ContextId contextId);

    /**
     * Processes Context suspension.
     *
     * <p>Prepares the Context suspension operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processSuspend(ContextId contextId);

    /**
     * Processes Context resumption.
     *
     * <p>Prepares the Context resumption operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processResume(ContextId contextId);

    /**
     * Processes Context activation.
     *
     * <p>Prepares the Context activation operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processActivate(ContextId contextId);

    /**
     * Processes Context deactivation.
     *
     * <p>Prepares the Context deactivation operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processDeactivate(ContextId contextId);

    /**
     * Processes Context expiration.
     *
     * <p>Prepares the Context expiration operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processExpire(ContextId contextId);

    /**
     * Processes Context archival.
     *
     * <p>Prepares the Context archival operation.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result (never null)
     */
    ContextProcessingResult processArchive(ContextId contextId);

    /**
     * Processes Context snapshot creation.
     *
     * <p>Prepares a Context snapshot for runtime use.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param contextId the Context identifier (must not be null)
     * @return the processing result with the created snapshot (never null)
     */
    ContextProcessingResult processCreateSnapshot(ContextId contextId);
}