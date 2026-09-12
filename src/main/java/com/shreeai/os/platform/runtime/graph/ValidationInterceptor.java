package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.Phase;

/**
 * <b>ValidationInterceptor</b>
 *
 * <p>Governance interceptor that runs <strong>after</strong> every node
 * execution and validates the produced {@link ExecutionNodeResult}.</p>
 *
 * <p>This interceptor is NOT a graph node. Validation is a runtime-level
 * concern that applies to every node result.</p>
 *
 * <p>Validation rules (deterministic):</p>
 * <ul>
 *   <li>A COMPLETED node must produce a non-blank output — otherwise the
 *       result is downgraded to FAILED.</li>
 *   <li>Failed / skipped / blocked results pass through unchanged.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Graph Executor</p>
 */
public final class ValidationInterceptor implements RuntimeInterceptor {

    @Override
    public String name() {
        return "ValidationInterceptor";
    }

    @Override
    public Phase phase() {
        return Phase.AFTER;
    }

    @Override
    public ExecutionNodeResult afterNode(ExecutionNode node, ExecutionNodeResult result) {
        if (result == null) {
            return ExecutionNodeResult.failed(
                    node.nodeId(), node.capability(),
                    "validation: node produced no result", 0L);
        }
        if (result.isSuccess() && (result.output() == null || result.output().isBlank())) {
            return ExecutionNodeResult.failed(
                    node.nodeId(), node.capability(),
                    "validation: completed node must produce non-blank output",
                    result.durationMillis());
        }
        return result;
    }
}
