package platform.runtime.pipeline;

import java.util.List;

/**
 * Execution pipeline contract for the Runtime Layer.
 *
 * <p>The pipeline is the execution backbone of Shree AI OS Runtime.
 * It manages a sequence of execution stages and processes them in order.</p>
 *
 * <p>The pipeline:</p>
 * <ul>
 *   <li>Receives a PipelineContext</li>
 *   <li>Invokes the first stage</li>
 *   <li>Each stage can continue or short-circuit</li>
 *   <li>Returns a PipelineResult</li>
 * </ul>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * <p><strong>Shadow Mode:</strong> In production, the pipeline executes stages.
 * In shadow mode, the pipeline only logs the flow without executing.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A
 */
public interface ExecutionPipeline {

    /**
     * Execute the pipeline with the given context.
     *
     * <p>This method processes the context through all stages in order.
     * Stages can short-circuit the pipeline by returning a result.</p>
     *
     * <p>This method is thread-safe and can be called concurrently.</p>
     *
     * <p><strong>Shadow Mode:</strong> In shadow mode, this method logs the
     * pipeline flow without executing stages.</p>
     *
     * @param context the pipeline context (never null)
     * @return the pipeline result (never null)
     */
    PipelineResult execute(PipelineContext context);

    /**
     * Get the list of execution stages in order.
     *
     * <p>Stages are ordered by their @Order annotation priority.
     * Lower numbers execute first.</p>
     *
     * @return the ordered list of stages (never null, never empty)
     */
    List<ExecutionStage> getStages();
}