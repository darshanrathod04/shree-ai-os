package com.shreeai.os.platform.runtime.execution;

/**
 * <b>PermissionPolicy</b>
 *
 * <p>Contract evaluated by the Runtime before any capability-driven execution
 * is dispatched to a kernel. Implementations decide whether the requested
 * capability may execute immediately, requires approval, or must be denied.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Evaluates a {@link PermissionDecision} for a capability.</li>
 *   <li>Gates execution before any kernel handler is invoked.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public interface PermissionPolicy {

    /**
     * Evaluates the permission decision for the given capability.
     *
     * @param capability the capability being dispatched (never null)
     * @return the permission decision (never null)
     */
    PermissionDecision evaluate(ExecutionCapability capability);
}
