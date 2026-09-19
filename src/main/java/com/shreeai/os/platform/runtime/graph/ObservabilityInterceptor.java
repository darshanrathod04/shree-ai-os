package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.Phase;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <b>ObservabilityInterceptor</b>
 *
 * <p>Governance interceptor that observes <strong>every</strong> node event:
 * before execution, after execution and on errors.</p>
 *
 * <p>This interceptor is NOT a graph node. Observability is a runtime-level
 * concern that applies to every node execution.</p>
 *
 * <p>Thread-safe: recorded events are appended to a synchronized list that
 * can be inspected for diagnostics and tests.</p>
 *
 * <p><b>Ownership:</b> Runtime Graph Executor</p>
 */
public final class ObservabilityInterceptor implements RuntimeInterceptor {

    private final List<String> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public String name() {
        return "ObservabilityInterceptor";
    }

    @Override
    public Phase phase() {
        return Phase.ALWAYS;
    }

    @Override
    public InterceptionDecision beforeNode(ExecutionNode node, ExecutionRequest request) {
        events.add("BEFORE " + node.nodeId());
        return InterceptionDecision.proceed();
    }

    @Override
    public ExecutionNodeResult afterNode(ExecutionNode node, ExecutionNodeResult result) {
        events.add("AFTER " + node.nodeId() + " -> " + result.status());
        return result;
    }

    @Override
    public void onNodeError(ExecutionNode node, RuntimeException error) {
        events.add("ERROR " + node.nodeId() + " -> " + error.getMessage());
    }

    /**
     * Returns an immutable snapshot of the observed events.
     *
     * @return observed events in occurrence order (never null)
     */
    public List<String> events() {
        synchronized (events) {
            return List.copyOf(events);
        }
    }

    /** Clears the recorded events. */
    public void reset() {
        events.clear();
    }
}
