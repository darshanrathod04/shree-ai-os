package platform.runtime.pipeline;

/**
 * Execution stage contract for the Runtime Pipeline.
 *
 * <p>Each runtime behavior (validation, retry, timeout, audit, metrics, etc.)
 * implements this interface as a pipeline stage.</p>
 *
 * <p>Stages are executed sequentially by the pipeline chain.
 * Each stage can:</p>
 * <ul>
 *   <li>Continue to the next stage by calling chain.next()</li>
 *   <li>Short-circuit the pipeline by returning a result</li>
 *   <li>Add messages to the result for observability</li>
 * </ul>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * <p><strong>Shadow Mode:</strong> In production, stages perform their actual logic.
 * In shadow mode, stages only log their flow without executing.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A
 */
public interface ExecutionStage {

    /**
     * Process the pipeline context and continue or short-circuit.
     *
     * <p>This method is called by the pipeline chain. Implementations should:</p>
     * <ul>
     *   <li>Perform their specific logic (validation, retry, etc.)</li>
     *   <li>Call chain.next(context, state) to continue to the next stage</li>
     *   <li>Return a PipelineResult to short-circuit the pipeline</li>
     * </ul>
     *
     * <p>Implementations must be thread-safe and stateless.</p>
     *
     * <p>The PipelineExecutionState is provided for read-only access to execution history.
     * Stages must NOT modify the state directly. The Runtime owns and manages all state mutations.</p>
     *
     * @param context the pipeline context (never null)
     * @param chain the execution chain to continue the pipeline (never null)
     * @param state the execution state with recorded execution history (never null)
     * @return the pipeline result (never null)
     */
    PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state);

    /**
     * Get the stage descriptor with metadata about this stage.
     *
     * @return the stage descriptor (never null)
     */
    PipelineStageDescriptor getDescriptor();
}