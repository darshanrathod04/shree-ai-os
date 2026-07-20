/**
 * <b>Execution Kernel — Domain Model Layer</b>
 *
 * <p>Provides the canonical immutable domain models for the Execution Kernel.
 * This package defines the stable vocabulary of execution concepts used
 * throughout Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines immutable execution domain models.</li>
 *   <li>Provides canonical identity and value objects.</li>
 *   <li>Encapsulates execution state representations.</li>
 *   <li>Maintains value semantics for all execution concepts.</li>
 * </ul>
 *
 * <p><b>Model Hierarchy:</b></p>
 * <pre>
 *                ExecutionRequest
 *                        │
 *                        ▼
 *                ExecutionContext
 *                        │
 *                        ▼
 *                ExecutionOptions
 *                        │
 *                        ▼
 *                WorkflowState
 *                  /       \
 *                 /         \
 *                /           \
 *           ActionState  ExecutionStatus
 *                \           /
 *                 \         /
 *                  \       /
 *                        ▼
 *                ExecutionResult
 *                  /         \
 *                 /           \
 *                /             \
 *     ExecutionMetrics    RecoveryStrategy
 *                        │
 *                        ▼
 *               ExecutionSnapshot
 * </pre>
 *
 * <p><b>Core Models:</b></p>
 * <ul>
 *   <li>{@link ExecutionId} — Canonical identity for execution instances.</li>
 *   <li>{@link ExecutionRequest} — Execution intent representation.</li>
 *   <li>{@link ExecutionResult} — Execution outcome representation.</li>
 *   <li>{@link ExecutionStatus} — Execution lifecycle states.</li>
 *   <li>{@link ExecutionMetrics} — Execution performance metrics.</li>
 *   <li>{@link ExecutionContext} — Execution environment context.</li>
 *   <li>{@link ExecutionOptions} — Execution configuration options.</li>
 *   <li>{@link RecoveryStrategy} — Recovery strategy types.</li>
 *   <li>{@link WorkflowState} — Workflow state representation.</li>
 *   <li>{@link ActionState} — Action state representation.</li>
 *   <li>{@link ExecutionSnapshot} — Historical execution snapshot.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all models are immutable value objects.</li>
 *   <li>Constructor validation — all models validate inputs.</li>
 *   <li>Defensive copying — all mutable collections are protected.</li>
 *   <li>Value semantics — all models implement equals, hashCode, toString.</li>
 *   <li>No behavior — models contain data only, no business logic.</li>
 * </ul>
 *
 * <p><b>What This Package Does NOT Contain:</b></p>
 * <ul>
 *   <li>No execution algorithms.</li>
 *   <li>No workflow orchestration.</li>
 *   <li>No recovery logic.</li>
 *   <li>No monitoring logic.</li>
 *   <li>No validation logic.</li>
 *   <li>No persistence.</li>
 *   <li>No networking.</li>
 *   <li>No thread management.</li>
 *   <li>No AI integrations.</li>
 * </ul>
 *
 * <p><b>Behavior Ownership:</b></p>
 * <p>Execution behavior belongs to future Validation, Service, and Engine layers.
 * This package defines what can be executed, not how execution is performed.</p>
 *
 * <p><b>Platform Consistency:</b></p>
 * <p>All models follow the canonical platform patterns established by:
 * Identity Kernel, Memory Kernel, Context Kernel, Knowledge Kernel,
 * Cognitive Kernel, and Planning Kernel.</p>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.execution.model;