/**
 * <b>Execution Error Layer</b>
 *
 * <p>This package provides a canonical, immutable representation of execution failures
 * and a consistent exception hierarchy for the Execution Kernel.</p>
 *
 * <p><b>Error Philosophy:</b></p>
 * <ul>
 *   <li><b>Failure classification</b> — categorizes execution failures</li>
 *   <li><b>Immutable representation</b> — errors cannot be modified after creation</li>
 *   <li><b>Exception hierarchy</b> — provides typed exceptions for different failure domains</li>
 *   <li><b>Propagation only</b> — classifies failures, does not handle them</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *        │
 *        ▼
 * ExecutionException
 *        │
 * ┌──────┼──────────────┬────────────────────┬────────────────────┐
 * ▼      ▼              ▼                    ▼                    ▼
 * ActionExecution   WorkflowExecution   TaskExecution      Recovery
 * Exception         Exception           Exception          Exception
 *        │
 *        ▼
 * ExecutionValidationException
 * </pre>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <p>The Error Layer is responsible for:</p>
 * <ul>
 *   <li>Failure classification</li>
 *   <li>Immutable failure representation</li>
 *   <li>Exception hierarchy</li>
 *   <li>Propagation of execution failures</li>
 * </ul>
 *
 * <p>The Error Layer is <b>not</b> responsible for:</p>
 * <ul>
 *   <li>Retry logic</li>
 *   <li>Rollback</li>
 *   <li>Compensation</li>
 *   <li>Workflow continuation</li>
 *   <li>Execution algorithms</li>
 *   <li>Recovery strategies</li>
 *   <li>Persistence</li>
 *   <li>Networking</li>
 * </ul>
 *
 * <p><b>Error Classification:</b></p>
 * <ul>
 *   <li>{@link platform.kernels.execution.error.ExecutionErrorCode} — execution-domain error codes</li>
 *   <li>{@link platform.kernels.execution.error.ExecutionError} — immutable error representation</li>
 *   <li>{@link platform.kernels.execution.error.ExecutionException} — base exception</li>
 *   <li>{@link platform.kernels.execution.error.ActionExecutionException} — action failures</li>
 *   <li>{@link platform.kernels.execution.error.WorkflowExecutionException} — workflow failures</li>
 *   <li>{@link platform.kernels.execution.error.TaskExecutionException} — task failures</li>
 *   <li>{@link platform.kernels.execution.error.RecoveryException} — recovery failures</li>
 *   <li>{@link platform.kernels.execution.error.ExecutionValidationException} — validation failures</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Immutability</b> — errors cannot be modified after creation</li>
 *   <li><b>Constructor validation</b> — rejects null arguments</li>
 *   <li><b>Defensive copying</b> — protects mutable collections</li>
 *   <li><b>Value semantics</b> — implements equals, hashCode, toString</li>
 *   <li><b>Framework independence</b> — no framework dependencies</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package platform.kernels.execution.error;