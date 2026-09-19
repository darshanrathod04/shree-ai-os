package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.graph.ExecutionNodeResult;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor;

/**
 * <b>ValidationInterceptor</b>
 *
 * <p>Post-execution quality-verification interceptor for the graph runtime.</p>
 *
 * <p>Validation runs <strong>after</strong> every node execution and evaluates
 * the produced {@link ExecutionNodeResult}. It is <strong>NOT</strong> a graph
 * node — it is a runtime-level governance concern applied to every node result.</p>
 *
 * <p>Execution flow:</p>
 * <pre>
 * Execute Node → ValidationInterceptor.validate(context)
 * </pre>
 *
 * <p>Possible verdicts:</p>
 * <ul>
 *   <li>{@link ValidationResult.Outcome#PASS} — output is acceptable, continue.</li>
 *   <li>{@link ValidationResult.Outcome#RETRY} — output is questionable, retry
 *       the node (subject to the executor's retry policy).</li>
 *   <li>{@link ValidationResult.Outcome#FAIL} — output is unacceptable, stop
 *       the graph and produce a structured failure.</li>
 * </ul>
 *
 * <p>This interceptor never generates new answers and never calls an LLM. It
 * performs deterministic, structural and heuristic quality checks only.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Validation Interceptor</p>
 */
@FunctionalInterface
public interface ValidationInterceptor extends RuntimeInterceptor {

    /**
     * Validates the result of a node execution.
     *
     * @param context an immutable snapshot of the node, result, request and
     *                shared execution context (never null)
     * @return the validation verdict (never null)
     */
    ValidationResult validate(ValidationContext context);

    @Override
    default String name() {
        return "ValidationInterceptor";
    }

    @Override
    default Phase phase() {
        return Phase.AFTER;
    }

    @Override
    default ExecutionNodeResult afterNode(ExecutionNode node, ExecutionNodeResult result) {
        // The executor calls validate() directly with a full context; this
        // default is a no-op pass-through for the generic interceptor chain.
        return result;
    }
}
