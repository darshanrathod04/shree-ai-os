/**
 * <b>Chief Kernel Error Layer</b>
 *
 * <p>This package provides the canonical error architecture for the Chief Kernel.
 * The Error Layer provides a unified, immutable representation of orchestration
 * failures across the Chief Kernel.</p>
 *
 * <p><b>Error Philosophy:</b></p>
 * <ul>
 *   <li><b>Representation only</b> — represents failures, does not resolve them</li>
 *   <li><b>Immutable</b> — all error objects are immutable</li>
 *   <li><b>Consistent</b> — unified error representation across the kernel</li>
 *   <li><b>Classified</b> — errors are classified by domain</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *        │
 *        ▼
 * ChiefException
 * ├────────────── DecisionException
 * ├────────────── GoalManagementException
 * ├────────────── TaskDelegationException
 * ├────────────── KernelCoordinationException
 * └────────────── ChiefValidationException
 * </pre>
 *
 * <p><b>Error Components:</b></p>
 * <ul>
 *   <li>{@link platform.kernels.chief.error.ChiefErrorCode} — canonical error identifiers</li>
 *   <li>{@link platform.kernels.chief.error.ChiefError} — immutable error information</li>
 *   <li>{@link platform.kernels.chief.error.ChiefException} — canonical base exception</li>
 *   <li>{@link platform.kernels.chief.error.DecisionException} — decision failures</li>
 *   <li>{@link platform.kernels.chief.error.GoalManagementException} — goal management failures</li>
 *   <li>{@link platform.kernels.chief.error.TaskDelegationException} — delegation failures</li>
 *   <li>{@link platform.kernels.chief.error.KernelCoordinationException} — coordination failures</li>
 *   <li>{@link platform.kernels.chief.error.ChiefValidationException} — validation failures</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Immutable</b> — all error objects are immutable</li>
 *   <li><b>Value semantics</b> — ChiefError implements equals, hashCode, toString</li>
 *   <li><b>Defensive copying</b> — mutable collections are protected</li>
 *   <li><b>No business logic</b> — errors represent failures only</li>
 * </ul>
 *
 * <p><b>Error Layer must never:</b></p>
 * <ul>
 *   <li>Resolve failures</li>
 *   <li>Retry operations</li>
 *   <li>Recover from errors</li>
 *   <li>Execute orchestration</li>
 *   <li>Delegate work</li>
 *   <li>Coordinate kernels</li>
 *   <li>Make decisions</li>
 *   <li>Prioritize goals</li>
 *   <li>Log errors</li>
 *   <li>Persist errors</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.chief.error;