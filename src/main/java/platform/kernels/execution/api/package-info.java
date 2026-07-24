/**
 * <b>Execution Kernel — API Layer</b>
 *
 * <p>Provides the public API contracts for the Execution Kernel.
 * This package defines the canonical interfaces through which the remainder
 * of Shree AI OS will interact with execution capabilities.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Exposes high-level execution operations for the platform.</li>
 *   <li>Defines contracts for action execution, workflow execution, and task execution.</li>
 *   <li>Defines contracts for execution monitoring and recovery.</li>
 *   <li>Provides stable API boundaries for execution capabilities.</li>
 *   <li>Delegates specialized execution tasks to subordinate services.</li>
 * </ul>
 *
 * <p><b>API Hierarchy:</b></p>
 * <pre>
 *                    ExecutionService
 *                  /      |      |      \
 *                 /       |      |       \
 *                /        |      |        \
 *      ActionExecution  WorkflowExecution  TaskExecution
 *                \         |         /
 *                 \        |        /
 *                  \ ExecutionMonitoring
 *                           |
 *                           |
 *                  ExecutionRecovery
 * </pre>
 *
 * <p><b>Core Components:</b></p>
 * <ul>
 *   <li>{@link ExecutionService} — Primary façade for execution operations.</li>
 *   <li>{@link ActionExecutionService} — Contracts for executing individual actions.</li>
 *   <li>{@link WorkflowExecutionService} — Contracts for workflow execution.</li>
 *   <li>{@link TaskExecutionService} — Contracts for execution of planned tasks.</li>
 *   <li>{@link ExecutionMonitoringService} — Runtime monitoring contracts.</li>
 *   <li>{@link ExecutionRecoveryService} — Execution recovery contracts.</li>
 *   <li>{@link ExecutionTypes} — Temporary immutable support types (to be migrated in EXEC-102).</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — all contracts are interfaces with no implementation.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only execution contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>What This Package Does NOT Contain:</b></p>
 * <ul>
 *   <li>No implementations.</li>
 *   <li>No default methods.</li>
 *   <li>No business logic.</li>
 *   <li>No execution algorithms.</li>
 *   <li>No planning algorithms.</li>
 *   <li>No reasoning logic.</li>
 *   <li>No persistence.</li>
 *   <li>No networking.</li>
 * </ul>
 *
 * <p><b>Temporary Types:</b></p>
 * <p>{@link ExecutionTypes} provides temporary immutable support records for API
 * construction. These types will be migrated to {@code platform.kernels.execution.model}
 * in EXEC-102 and replaced with canonical domain models.</p>
 *
 * <p><b>Ownership:</b> Execution Kernel — API Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.execution.api;