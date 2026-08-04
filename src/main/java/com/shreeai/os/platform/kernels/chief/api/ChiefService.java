package com.shreeai.os.platform.kernels.chief.api;

import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.model.ChiefMetrics;

/**
 * <b>ChiefService</b>
 *
 * <p>Primary façade for the Chief Kernel, providing high-level strategic orchestration
 * operations and coordinating cross-kernel service contracts.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Exposes high-level strategic orchestration operations for the platform.</li>
 *   <li>Coordinates cross-kernel service contracts.</li>
 *   <li>Provides stable API boundaries for orchestration capabilities.</li>
 *   <li>Delegates specialized orchestration tasks to subordinate services.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only orchestration contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @see DecisionService
 * @see GoalManagementService
 * @see TaskDelegationService
 * @see KernelCoordinationService
 * @see ChiefMonitoringService
 *
 * @since 1.0
 */
public interface ChiefService {

    /**
     * Submits a strategic orchestration request to the Chief Kernel.
     *
     * <p>This operation initiates strategic orchestration and returns a response
     * containing the orchestration outcome.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to subordinate services
     * for specialized orchestration logic.</p>
     *
     * @param request the strategic orchestration request (must not be {@code null})
     * @return the orchestration response
     * @throws IllegalArgumentException if request is {@code null}
     */
    ChiefResponse submitOrchestration(ChiefRequest request);

    /**
     * Retrieves the current orchestration status for a given request identifier.
     *
     * <p>This operation provides real-time orchestration status information.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link ChiefMonitoringService} for status retrieval.</p>
     *
     * @param requestId the orchestration request identifier (must not be {@code null} or empty)
     * @return the orchestration response with current status
     * @throws IllegalArgumentException if requestId is {@code null} or empty
     */
    ChiefResponse getOrchestrationStatus(String requestId);

    /**
     * Cancels an ongoing orchestration.
     *
     * <p>This operation requests cancellation of the specified orchestration.
     * The orchestration may not cancel immediately if it is in a non-interruptible
     * state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to subordinate services
     * for cancellation logic.</p>
     *
     * @param requestId the orchestration request identifier (must not be {@code null} or empty)
     * @return {@code true} if cancellation was requested successfully
     * @throws IllegalArgumentException if requestId is {@code null} or empty
     */
    boolean cancelOrchestration(String requestId);

    /**
     * Retrieves the current health status of the Chief Kernel.
     *
     * <p>This operation provides strategic monitoring information about the
     * Chief Kernel's operational state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @return the chief metrics representing current health
     */
    ChiefMetrics getChiefHealth();
}