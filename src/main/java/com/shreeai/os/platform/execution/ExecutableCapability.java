package com.shreeai.os.platform.execution;

/**
 * Core contract for all executable capabilities in Shree AI OS.
 *
 * <p>Every capability that can be executed by the Runtime Layer must implement
 * this interface. This defines the stable ABI (Application Binary Interface)
 * for capability execution.</p>
 *
 * <p>Implementations must be thread-safe and stateless, or properly handle
 * concurrent access. No mutable static state is allowed.</p>
 *
 * <p>This is part of the stable execution contract (ABI) for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
public interface ExecutableCapability {

    /**
     * Get the unique name of this capability.
     *
     * <p>The name must be unique across all capabilities and is used for
     * routing and identification purposes.</p>
     *
     * @return the capability name (never null or empty)
     */
    String getName();

    /**
     * Get the execution type for this capability.
     *
     * <p>The execution type determines how the capability should be executed
     * by the Runtime Layer (synchronous, asynchronous, background, streaming).</p>
     *
     * @return the execution type (never null)
     */
    ExecutionType getExecutionType();

    /**
     * Check if this capability supports the given execution request.
     *
     * <p>This method allows the Runtime Layer to determine if a capability
     * can handle a specific request before attempting execution. Implementations
     * should check intent, input validity, and any other relevant criteria.</p>
     *
     * <p>This method must be fast and side-effect free. It should not perform
     * any actual execution or I/O operations.</p>
     *
     * @param request the execution request to check
     * @return true if this capability can handle the request
     */
    boolean supports(ExecutionRequest request);

    /**
     * Execute the capability with the given request.
     *
     * <p>This is the main execution method. Implementations should perform
     * the actual capability logic and return the result.</p>
     *
     * <p>Implementations must:</p>
     * <ul>
     *   <li>Be thread-safe</li>
     *   <li>Handle all error conditions gracefully</li>
     *   <li>Return appropriate ExecutionStatus in the result</li>
     *   <li>Set execution time in the result metadata</li>
     *   <li>Never throw unchecked exceptions (catch and return FAILED status)</li>
     * </ul>
     *
     * <p>This method should not call other capabilities directly.
     * Use the ExecutionContext for coordination if needed.</p>
     *
     * @param request the execution request
     * @return the execution result (never null)
     */
    ExecutionResult execute(ExecutionRequest request);
}