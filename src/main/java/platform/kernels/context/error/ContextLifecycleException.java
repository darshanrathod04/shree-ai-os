package platform.kernels.context.error;

/**
 * <b>ContextLifecycleException</b>
 *
 * <p>Exception thrown when Context lifecycle operations fail.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that Context lifecycle operations have failed.</li>
 *   <li>Encapsulates a ContextError with LIFECYCLE_FAILED code.</li>
 *   <li>Extends ContextException for consistent error handling.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Encapsulates one immutable ContextError.</li>
 *   <li>No business logic.</li>
 *   <li>No mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-104</p>
 *
 * @see ContextException
 * @see ContextError
 */
public class ContextLifecycleException extends ContextException {
    /**
     * Creates a new ContextLifecycleException with the specified error.
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextLifecycleException(ContextError error) {
        super(error);
    }

    /**
     * Creates a new ContextLifecycleException with the specified error and cause.
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextLifecycleException(ContextError error, Throwable cause) {
        super(error, cause);
    }
}