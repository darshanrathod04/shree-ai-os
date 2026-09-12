package com.shreeai.os.platform.runtime.interceptor;

/**
 * <b>SafetyInterceptor</b>
 *
 * <p>Canonical runtime safety gate invoked exactly once per graph node,
 * <strong>before</strong> the node is handed to its {@link
 * com.shreeai.os.platform.runtime.graph.NodeExecutor}.</p>
 *
 * <p>Unlike the {@link
 * com.shreeai.os.platform.runtime.graph.RuntimeInterceptor} chain (which is a
 * generic, pluggable governance hook), this gate is the single, mandatory,
 * kernel-level security checkpoint. It is consulted by
 * {@link com.shreeai.os.platform.runtime.graph.DefaultGraphRuntimeExecutor}
 * before any node executes and its {@link InterceptorResult} is surfaced in the
 * structured runtime payload so callers can see why a request was blocked or why
 * an approval is pending.</p>
 *
 * <p>Implementations must be:</p>
 * <ul>
 *   <li><b>Deterministic</b> — the same (node, context) pair must yield the
 *       same outcome.</li>
 *   <li><b>Total</b> — {@link #before} must never return null; the default
 *       implementation yields {@link InterceptorResult#allow()} so gates are
 *       opt-in.</li>
 *   <li><b>Side-effect free on the request</b> — the gate may inspect the
 *       permission context but must not mutate the execution request.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Safety Gate)</p>
 * <p><b>Version:</b> 1.0</p>
 */
@FunctionalInterface
public interface SafetyInterceptor {

    /**
     * Evaluates whether the given node may execute under the supplied permission
     * context. The returned {@link InterceptorResult} is non-null and never
     * throws.
     *
     * @param node    the node about to execute (never null)
     * @param context the permission context (never null)
     * @return the interception outcome (never null; defaults to ALLOW)
     */
    InterceptorResult before(com.shreeai.os.platform.graph.ExecutionNode node,
                             PermissionContext context);
}
