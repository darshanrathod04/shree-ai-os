/**
 * <b>Context Error Layer</b>
 *
 * <p>Provides structured error reporting for the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides consistent, immutable error reporting.</li>
 *   <li>Encapsulates errors in structured ContextError value objects.</li>
 *   <li>Maintains a standardized exception hierarchy.</li>
 *   <li>Reports failures only—never validates, processes, stores, or coordinates.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable error models.</li>
 *   <li>Standardized error codes.</li>
 *   <li>Kernel-specific exception hierarchy.</li>
 *   <li>No primitive error scattering.</li>
 *   <li>No business logic.</li>
 * </ul>
 *
 * <p><b>Error Architecture:</b></p>
 * <ul>
 *   <li>ContextError - Immutable value object encapsulating error information</li>
 *   <li>ContextErrorCode - Standardized error identifiers</li>
 *   <li>ContextException - Base exception class</li>
 *   <li>ContextValidationException - Validation failures</li>
 *   <li>ContextLifecycleException - Lifecycle operation failures</li>
 *   <li>ContextSnapshotException - Snapshot operation failures</li>
 *   <li>ContextNotFoundException - Context not found errors</li>
 * </ul>
 *
 * <p><b>What Errors May Do:</b></p>
 * <ul>
 *   <li>Describe failures.</li>
 *   <li>Encapsulate metadata.</li>
 *   <li>Provide standardized codes.</li>
 * </ul>
 *
 * <p><b>What Errors Must Never Do:</b></p>
 * <ul>
 *   <li>Perform validation.</li>
 *   <li>Modify Context objects.</li>
 *   <li>Access repositories.</li>
 *   <li>Access databases.</li>
 *   <li>Perform persistence.</li>
 *   <li>Invoke AI.</li>
 *   <li>Perform networking.</li>
 *   <li>Publish events.</li>
 *   <li>Create threads.</li>
 *   <li>Schedule work.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> All error objects are immutable and thread-safe.
 * Exceptions can be safely thrown and caught from multiple threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-104, EIO-ARCH-001</p>
 *
 * @see platform.kernels.context.error.ContextError
 * @see platform.kernels.context.error.ContextErrorCode
 * @see platform.kernels.context.error.ContextException
 * @see platform.kernels.context.error.ContextValidationException
 * @see platform.kernels.context.error.ContextLifecycleException
 * @see platform.kernels.context.error.ContextSnapshotException
 * @see platform.kernels.context.error.ContextNotFoundException
 */
package platform.kernels.context.error;