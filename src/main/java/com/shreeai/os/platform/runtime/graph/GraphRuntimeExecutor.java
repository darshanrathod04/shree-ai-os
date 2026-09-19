package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;

/**
 * <b>GraphRuntimeExecutor</b>
 *
 * <p>Contract for executing a {@link ExecutionGraph} produced by the
 * Universal Execution Graph builder (Task-003).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Finds executable nodes from the graph.</li>
 *   <li>Respects node dependencies and executes in topological order.</li>
 *   <li>Runs governance interceptors (Safety before, Validation after,
 *       Observability always) around each node execution.</li>
 *   <li>Produces per-node {@link ExecutionNodeResult}s.</li>
 *   <li>Stops on critical failures.</li>
 *   <li>Returns the resulting {@link ExecutionSession}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Graph Execution)</p>
 *
 * @see DefaultGraphRuntimeExecutor
 * @see RuntimeInterceptor
 * @see NodeExecutor
 */
public interface GraphRuntimeExecutor {

    /**
     * Executes the given graph for the given request and returns the session
     * carrying the aggregated result.
     *
     * @param graph   the execution graph to execute (never null)
     * @param request the originating runtime request (never null)
     * @return the execution session with status and result populated
     */
    ExecutionSession execute(ExecutionGraph graph, ExecutionRequest request);
}
