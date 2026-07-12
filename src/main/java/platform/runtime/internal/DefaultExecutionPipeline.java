package platform.runtime.internal;

import platform.runtime.execution.ExecutionContext;
import platform.runtime.execution.ExecutionPipeline;
import platform.runtime.execution.ExecutionRequest;
import platform.runtime.execution.ExecutionResult;

/**
 * <b>DefaultExecutionPipeline</b>
 *
 * <p>Default no-op implementation of the {@link ExecutionPipeline} interface.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a placeholder pipeline implementation for the Runtime skeleton.</li>
 *   <li>Returns a default result indicating the pipeline is not yet implemented.</li>
 *   <li>Will be replaced with a full pipeline implementation in Sprint 2.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Internal)</p>
 */
public final class DefaultExecutionPipeline implements ExecutionPipeline {

    private static final String PIPELINE_NAME = "default-skeleton-pipeline";

    @Override
    public ExecutionResult execute(ExecutionRequest request, ExecutionContext context) {
        // Skeleton implementation - no actual execution logic
        return ExecutionResult.success(
                request.requestId(),
                "Pipeline execution not yet implemented (Runtime Kernel Sprint 1 skeleton)"
        );
    }

    @Override
    public String pipelineName() {
        return PIPELINE_NAME;
    }

    @Override
    public boolean isAccepting() {
        return true;
    }
}