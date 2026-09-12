package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.InterceptionDecision;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.Phase;

import java.util.List;

/**
 * <b>SafetyInterceptor</b>
 *
 * <p>Governance interceptor that runs <strong>before</strong> every node
 * execution and may block the node.</p>
 *
 * <p>This interceptor is NOT a graph node. Safety is a runtime-level concern
 * that applies to every node regardless of the resolved capability plan.</p>
 *
 * <p>Deterministic deny-list check on the request payload: payloads matching
 * a destructive pattern are blocked with a reason.</p>
 *
 * <p><b>Ownership:</b> Runtime Graph Executor</p>
 */
public final class SafetyInterceptor implements RuntimeInterceptor {

    /** Deterministic deny-list patterns (case-insensitive). */
    private static final List<String> DENY_PATTERNS = List.of(
            "rm -rf ",
            "drop table",
            "delete from ",
            "shutdown -",
            "format c:",
            "__import__('os').system");

    @Override
    public String name() {
        return "SafetyInterceptor";
    }

    @Override
    public Phase phase() {
        return Phase.BEFORE;
    }

    @Override
    public InterceptionDecision beforeNode(ExecutionNode node, ExecutionRequest request) {
        String payload = request.payload();
        if (payload == null) {
            return InterceptionDecision.proceed();
        }
        String normalized = payload.toLowerCase();
        for (String pattern : DENY_PATTERNS) {
            if (normalized.contains(pattern)) {
                return InterceptionDecision.block(
                        "safety: destructive pattern detected: " + pattern.trim());
            }
        }
        return InterceptionDecision.proceed();
    }
}
