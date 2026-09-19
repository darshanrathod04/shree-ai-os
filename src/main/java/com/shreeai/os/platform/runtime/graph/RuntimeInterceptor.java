package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

/**
 * <b>RuntimeInterceptor</b>
 *
 * <p>Governance interceptor contract for the graph runtime executor.</p>
 *
 * <p>Interceptors are <strong>NOT</strong> graph nodes. Safety, Validation and
 * Observability are implemented as interceptors so that governance always runs
 * around every node regardless of which capabilities a request resolved.</p>
 *
 * <p>Execution flow:</p>
 * <pre>
 * Before Node  → SafetyInterceptor.beforeNode(...)
 * Execute Node → NodeExecutor
 * After Node   → ValidationInterceptor.afterNode(...)
 * Always       → ObservabilityInterceptor (before + after + errors)
 * </pre>
 *
 * <p><b>Ownership:</b> Runtime Graph Executor</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface RuntimeInterceptor {

    /**
     * Returns the interceptor name (used for diagnostics and ordering).
     *
     * @return the interceptor name
     */
    String name();

    /**
     * Invoked before a node is executed. A {@link Phase#BEFORE} interceptor
     * (e.g. Safety) may block the node by returning a blocking decision.
     *
     * @param node    the node about to be executed
     * @param request the originating execution request
     * @return the interception decision (never null; defaults to proceed)
     */
    default InterceptionDecision beforeNode(ExecutionNode node, ExecutionRequest request) {
        return InterceptionDecision.proceed();
    }

    /**
     * Invoked after a node was executed. A {@link Phase#AFTER} interceptor
     * (e.g. Validation) may replace or enrich the node result.
     *
     * @param node   the node that was executed
     * @param result the raw node result
     * @return the (possibly modified) node result (never null)
     */
    default ExecutionNodeResult afterNode(ExecutionNode node, ExecutionNodeResult result) {
        return result;
    }

    /**
     * Invoked when node execution throws. Implementations must not throw.
     *
     * @param node  the node that threw
     * @param error the thrown error
     */
    default void onNodeError(ExecutionNode node, RuntimeException error) {
        // default: no-op
    }

    /**
     * The phase this interceptor participates in.
     */
    enum Phase {
        /** Runs before node execution (e.g. Safety). */
        BEFORE,
        /** Runs after node execution (e.g. Validation). */
        AFTER,
        /** Runs on every node event (e.g. Observability). */
        ALWAYS
    }

    /**
     * The phase this interceptor is registered for. Defaults to
     * {@link Phase#ALWAYS}.
     *
     * @return the phase
     */
    default Phase phase() {
        return Phase.ALWAYS;
    }

    /**
     * Decision returned by {@link #beforeNode(ExecutionNode, ExecutionRequest)}.
     */
    final class InterceptionDecision {

        private final boolean proceed;
        private final String blockReason;

        private InterceptionDecision(boolean proceed, String blockReason) {
            this.proceed = proceed;
            this.blockReason = blockReason;
        }

        /**
         * Allows node execution to continue.
         *
         * @return a proceed decision
         */
        public static InterceptionDecision proceed() {
            return new InterceptionDecision(true, null);
        }

        /**
         * Blocks node execution with the given reason.
         *
         * @param reason why the node is blocked
         * @return a blocking decision
         */
        public static InterceptionDecision block(String reason) {
            return new InterceptionDecision(false,
                    reason == null ? "blocked by interceptor" : reason);
        }

        public boolean isProceed() {
            return proceed;
        }

        public String blockReason() {
            return blockReason;
        }
    }
}
