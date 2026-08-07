package com.shreeai.os.platform.runtime.execution;

/**
 * <b>ExecutionPipeline</b>
 *
 * <p>Interface for the execution pipeline that processes {@link ExecutionRequest} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for pipeline-based execution processing.</li>
 *   <li>Enables composable stage-by-stage execution.</li>
 *   <li>Provides a stable abstraction that can be implemented by different pipeline strategies.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 *
 * @see ExecutionRequest
 * @see ExecutionResult
 * @see ExecutionContext
 */
public interface ExecutionPipeline {

    /**
     * Executes the given request within the provided context.
     *
     * @param request the execution request to process
     * @param context the execution context for this execution
     * @return the result of the execution
     */
    ExecutionResult execute(ExecutionRequest request, ExecutionContext context);

    /**
     * Returns the name of this pipeline implementation.
     *
     * @return the pipeline name
     */
    String pipelineName();

    /**
     * Returns whether this pipeline is currently accepting new executions.
     *
     * @return true if accepting executions
     */
    boolean isAccepting();
}