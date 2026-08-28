package com.shreeai.os.platform.kernels.context.error;

/**
 * <b>ContextNotFoundException</b>
 *
 * <p>Exception thrown when a Context is not found.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a requested Context does not exist.</li>
 *   <li>Encapsulates a ContextError with CONTEXT_NOT_FOUND code.</li>
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
public class ContextNotFoundException extends ContextException {
    /**
     * Creates a new ContextNotFoundException with the specified error.
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextNotFoundException(ContextError error) {
        super(error);
    }

    /**
     * Creates a new ContextNotFoundException with the specified error and cause.
     *
     * @param error the ContextError to encapsulate (must not be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public ContextNotFoundException(ContextError error, Throwable cause) {
        super(error, cause);
    }
}