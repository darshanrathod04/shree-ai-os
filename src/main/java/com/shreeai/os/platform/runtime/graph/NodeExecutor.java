package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

import java.util.Map;

/**
 * <b>NodeExecutor</b>
 *
 * <p>Executes a single {@link ExecutionNode} of the Universal Execution Graph
 * by delegating to the existing runtime / kernel service that owns the
 * node's capability.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Maps one CapabilityType to one existing runtime service.</li>
 *   <li>Contains NO orchestration logic — ordering and dependency resolution
 *       are owned by the {@link DefaultGraphRuntimeExecutor}.</li>
 *   <li>Never throws: failures are reported as failed
 *       {@link ExecutionNodeResult}s.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Graph Execution)</p>
 */
public interface NodeExecutor {

    /**
     * Executes the given node.
     *
     * @param node    the node to execute (never null)
     * @param request the originating runtime request (never null)
     * @param context shared mutable execution context carrying upstream node
     *                outputs keyed by {@code "node:<capability>"} (never null)
     * @return the node result (never null)
     */
    ExecutionNodeResult execute(
            ExecutionNode node,
            ExecutionRequest request,
            Map<String, Object> context);
}
