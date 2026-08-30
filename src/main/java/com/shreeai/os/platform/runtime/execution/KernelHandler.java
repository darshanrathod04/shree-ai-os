package com.shreeai.os.platform.runtime.execution;

import java.util.Map;

/**
 * <b>KernelHandler</b>
 *
 * <p>Functional contract executed by the {@link KernelRegistry} for a
 * registered {@link ExecutionCapability}. Concrete handlers delegate to the
 * kernel that owns the capability and return a rich, structured result.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes the kernel that owns the capability.</li>
 *   <li>Returns a rich, structured result.</li>
 *   <li>Contains no routing or permission logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
@FunctionalInterface
public interface KernelHandler {

    /**
     * Executes the kernel that owns the given capability.
     *
     * @param capability the capability being dispatched (never null)
     * @param input      the request payload (never null; may be blank)
     * @param context    the execution context (never null; may be empty)
     * @return a rich execution result (never null)
     */
    RichExecutionResult handle(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context);
}
