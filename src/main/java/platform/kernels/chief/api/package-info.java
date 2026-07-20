/**
 * <b>Chief Kernel API</b>
 *
 * <p>This package provides the public API contracts for the Chief Kernel.
 * The Chief Kernel is responsible for strategic orchestration across the platform,
 * coordinating the completed kernels of Shree AI OS.</p>
 *
 * <p><b>Orchestration Responsibilities:</b></p>
 * <ul>
 *   <li>Strategic decision coordination</li>
 *   <li>Goal lifecycle management</li>
 *   <li>Task delegation</li>
 *   <li>Cross-kernel coordination</li>
 *   <li>Orchestration monitoring</li>
 * </ul>
 *
 * <p><b>Platform Position:</b></p>
 * <pre>
 * Knowledge
 *       ↓
 * Cognition
 *       ↓
 * Planning
 *       ↓
 * Execution
 *       ↓
 * Chief
 * </pre>
 *
 * <p><b>Kernel Responsibilities:</b></p>
 * <ul>
 *   <li>Knowledge → knows</li>
 *   <li>Cognition → reasons</li>
 *   <li>Planning → decides what should be done</li>
 *   <li>Execution → performs validated work</li>
 *   <li>Chief → coordinates the entire system</li>
 * </ul>
 *
 * <p><b>API Architecture:</b></p>
 * <pre>
 *                      ChiefService
 *                  /       |        |        \
 *                 /        |        |         \
 *          Decision   GoalManagement  TaskDelegation
 *                 \        |        /
 *                  \       |       /
 *            KernelCoordination
 *                      |
 *                      |
 *              ChiefMonitoringService
 * </pre>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <p>The Chief API may reference immutable models from completed kernels
 * where appropriate. It must never depend directly upon:</p>
 * <ul>
 *   <li>Repositories</li>
 *   <li>Persistence</li>
 *   <li>Networking</li>
 *   <li>Execution engines</li>
 *   <li>Planning engines</li>
 *   <li>Reasoning engines</li>
 *   <li>Framework-specific implementations</li>
 * </ul>
 *
 * <p><b>Service Contracts:</b></p>
 * <ul>
 *   <li>{@link platform.kernels.chief.api.ChiefService} — primary façade</li>
 *   <li>{@link platform.kernels.chief.api.DecisionService} — strategic decision coordination</li>
 *   <li>{@link platform.kernels.chief.api.GoalManagementService} — goal lifecycle management</li>
 *   <li>{@link platform.kernels.chief.api.TaskDelegationService} — task delegation</li>
 *   <li>{@link platform.kernels.chief.api.KernelCoordinationService} — cross-kernel coordination</li>
 *   <li>{@link platform.kernels.chief.api.ChiefMonitoringService} — strategic monitoring</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic</li>
 *   <li>Technology-agnostic — no framework dependencies</li>
 *   <li>Contract-focused — exposes only orchestration contracts</li>
 *   <li>Stateless — no mutable state</li>
 * </ul>
 *
 * <p><b>Migration Note:</b></p>
 * <p>{@link platform.kernels.chief.api.ChiefTypes} is temporary. In CHIEF-102,
 * all shared records shall migrate into {@code platform.kernels.chief.model}.</p>
 *
 * <p><b>Ownership:</b> Chief Kernel — API Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.chief.api;