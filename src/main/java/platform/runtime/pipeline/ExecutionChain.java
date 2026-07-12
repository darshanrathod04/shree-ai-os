package platform.runtime.pipeline;

/**
 * Execution chain for pipeline stage progression.
 *
 * <p>The chain maintains the current stage index and invokes stages sequentially.
 * It provides a way for stages to continue to the next stage in the pipeline.</p>
 *
 * <p>The chain updates the PipelineExecutionState directly to record execution history.
 * Execution history is never inferred from PipelineResult.</p>
 *
 * <p>This interface is thread-safe and immutable in its progression state.
 * Each call to next() advances the pipeline by one stage.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
public interface ExecutionChain {

    /**
     * Check if there are more stages to execute.
     *
     * <p>This method returns true if the pipeline has not yet completed all stages.</p>
     *
     * @param context the pipeline context (never null)
     * @param state the execution state (never null)
     * @return true if there are more stages to execute
     */
    boolean hasNext(PipelineContext context, PipelineExecutionState state);

    /**
     * Continue to the next stage in the pipeline.
     *
     * <p>This method advances the pipeline to the next stage and processes it.
     * If all stages have been completed, it returns a completed PipelineResult.</p>
     *
     * <p>This method updates the PipelineExecutionState directly to record execution history.
     * Execution history is never inferred from PipelineResult.</p>
     *
     * <p>This method is thread-safe and can be called multiple times,
     * though typically each stage calls it once.</p>
     *
     * <p>Stages should call this method to continue pipeline execution.
     * The runtime uses this to detect short-circuit behavior.</p>
     *
     * @param context the pipeline context (never null)
     * @param state the execution state to update (never null)
     * @return the pipeline result from the next stage or completion (never null)
     */
    PipelineResult next(PipelineContext context, PipelineExecutionState state);
}
