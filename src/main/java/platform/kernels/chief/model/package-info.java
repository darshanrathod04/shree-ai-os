/**
 * <b>Chief Kernel Domain Model</b>
 *
 * <p>This package provides the immutable domain models for the Chief Kernel.
 * These models represent strategic orchestration concepts throughout Shree AI OS
 * and serve as the canonical vocabulary for the Chief Kernel.</p>
 *
 * <p><b>Model Architecture:</b></p>
 * <pre>
 * ChiefRequest
 *       │
 *       ▼
 * DecisionContext
 *       │
 *       ▼
 * GoalDescriptor
 *       │
 *       ▼
 * CoordinationState
 *      ┌────────┴────────┐
 *      ▼                 ▼
 * DecisionResult   DelegationResult
 *      └────────┬────────┘
 *               ▼
 *        ChiefResponse
 *               │
 *      ┌────────┴─────────┐
 *      ▼                   ▼
 * ChiefMetrics      ChiefSnapshot
 * </pre>
 *
 * <p><b>Model Responsibilities:</b></p>
 * <ul>
 *   <li>ChiefId — unique identity for orchestration instances</li>
 *   <li>ChiefRequest — strategic orchestration request</li>
 *   <li>ChiefResponse — immutable orchestration outcome</li>
 *   <li>DecisionContext — strategic coordination context</li>
 *   <li>DecisionResult — strategic decision outcome</li>
 *   <li>GoalDescriptor — immutable goal metadata</li>
 *   <li>DelegationResult — immutable delegation outcomes</li>
 *   <li>CoordinationState — immutable orchestration state</li>
 *   <li>ChiefMetrics — immutable orchestration metrics</li>
 *   <li>ChiefSnapshot — immutable orchestration snapshot</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all models are immutable value objects</li>
 *   <li>Constructor validation — rejects null or invalid arguments</li>
 *   <li>Defensive copying — protects mutable collections</li>
 *   <li>Value semantics — implements equals, hashCode, toString</li>
 *   <li>No behavior — models contain data only, no orchestration logic</li>
 * </ul>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <p>The Domain Model represents orchestration concepts only.
 * It must never contain:</p>
 * <ul>
 *   <li>Orchestration algorithms</li>
 *   <li>Decision-making logic</li>
 *   <li>Goal prioritization</li>
 *   <li>Delegation logic</li>
 *   <li>Kernel coordination behavior</li>
 *   <li>Validation logic</li>
 *   <li>Persistence</li>
 *   <li>Networking</li>
 *   <li>Thread management</li>
 *   <li>AI integrations</li>
 * </ul>
 *
 * <p><b>Behavior belongs to:</b></p>
 * <ul>
 *   <li>Validation Layer (CHIEF-103)</li>
 *   <li>Service Layer (CHIEF-105)</li>
 *   <li>Engine Layer (CHIEF-106)</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-102, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.chief.model;