package platform.kernels.chief.api;

import platform.kernels.chief.model.ChiefMetrics;
import platform.kernels.chief.model.CoordinationState;

/**
 * <b>ChiefMonitoringService</b>
 *
 * <p>Defines strategic monitoring contracts for the Chief Kernel.
 * This interface provides contracts for monitoring orchestration health,
 * coordination metrics, and system coordination state without implementing
 * any monitoring logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines strategic monitoring contracts.</li>
 *   <li>Provides orchestration health monitoring.</li>
 *   <li>Provides coordination metrics and system state.</li>
 *   <li>Contains no monitoring implementation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only monitoring contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface ChiefMonitoringService {

    /**
     * Retrieves the current orchestration health status.
     *
     * <p>This operation provides strategic health information about the
     * Chief Kernel's orchestration capabilities.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @return the chief metrics representing orchestration health
     */
    ChiefMetrics getOrchestrationHealth();

    /**
     * Retrieves current coordination metrics.
     *
     * <p>This operation provides metrics about cross-kernel coordination
     * activities.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @return the chief metrics representing coordination metrics
     */
    ChiefMetrics getCoordinationMetrics();

    /**
     * Retrieves the current system coordination state.
     *
     * <p>This operation provides the current state of system-wide
     * coordination across all kernels.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @return the coordination state representing system coordination
     */
    CoordinationState getSystemCoordinationState();

    /**
     * Retrieves the list of active orchestrations.
     *
     * <p>This operation provides information about currently active
     * orchestration operations.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @return the chief metrics with active orchestration information
     */
    ChiefMetrics getActiveOrchestrations();
}