package com.shreeai.os.platform.runtime.pipeline;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of ExecutionChain.
 *
 * <p>This class maintains the current stage index and invokes stages sequentially.
 * It updates PipelineExecutionState directly to record execution history.
 * Execution history is never inferred from PipelineResult.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
public final class DefaultExecutionChain implements ExecutionChain {

    private final List<ExecutionStage> stages;
    private final int currentIndex;

    /**
     * Create a new DefaultExecutionChain.
     *
     * <p>This constructor is used by DefaultExecutionPipeline to create the initial chain.</p>
     *
     * @param stages the ordered list of stages (never null, never empty)
     */
    public DefaultExecutionChain(List<ExecutionStage> stages) {
        this.stages = Collections.unmodifiableList(stages);
        this.currentIndex = 0;
    }

    /**
     * Create a new DefaultExecutionChain with a specific index.
     *
     * <p>This constructor is used internally to advance the chain.</p>
     *
     * @param stages the ordered list of stages (never null, never empty)
     * @param currentIndex the current stage index
     */
    private DefaultExecutionChain(List<ExecutionStage> stages, int currentIndex) {
        this.stages = Collections.unmodifiableList(stages);
        this.currentIndex = currentIndex;
    }

    @Override
    public boolean hasNext(PipelineContext context, PipelineExecutionState state) {
        // Check if all stages have been completed
        return currentIndex < stages.size() &&
               !state.isTerminated() &&
               !state.isShortCircuited() &&
               !state.isFailed();
    }

    @Override
    public PipelineResult next(PipelineContext context, PipelineExecutionState state) {
        // Check if all stages have been completed
        if (currentIndex >= stages.size()) {
            // All stages completed - mark termination and return completion result
            state.markTerminated();
            return PipelineResult.builder()
                    .success(true)
                    .status("COMPLETED")
                    .addMessage("Pipeline completed successfully")
                    .build();
        }

        // Synchronize state's index with chain's index
        // The chain's index is the single source of truth for which stage to execute
        while (state.getCurrentStageIndex() < currentIndex) {
            state.advanceStage();
        }

        // Get the current stage
        ExecutionStage currentStage = stages.get(currentIndex);
        String stageName = currentStage.getDescriptor().getStageName();

        // Mark stage as started in state (Runtime records execution).
        // This pushes a new frame onto the state's next-stage-invoked stack.
        state.markStageStarted(stageName);

        // Create the next chain with incremented index
        DefaultExecutionChain nextChain = new DefaultExecutionChain(stages, currentIndex + 1);

        // Invoke the current stage, passing the next chain and state.
        // The stage will call nextChain.next() if it wants to continue.
        PipelineResult result = currentStage.process(context, nextChain, state);

        // Determine whether the stage continued to the next chain.
        //
        // The preferred signal is state.wasNextStageInvoked(), which stages
        // may set by calling state.markNextStageInvoked(). However, real stages
        // in the canonical pipeline (IdentityStage, ContextStage, etc.) only call
        // chain.next() without setting this flag. For those stages, we detect
        // continuation via the execution state:
        //   - If more stages were visited beyond this one, the stage called chain.next().
        //   - If the terminal stage called chain.next(), the chain exhausted the
        //     stage list and marked the state as terminated.
        boolean nextStageWasInvoked = state.wasNextStageInvoked()
                || state.getVisitedStages().size() > currentIndex + 1
                || state.isTerminated();

        if (!nextStageWasInvoked) {
            // Stage short-circuited - it returned without calling chain.next()
            state.markShortCircuit();
        } else {
            // Stage completed successfully (called chain.next())
            // Mark this stage as completed
            state.markStageCompleted(stageName);
        }

        // Pop the current frame so the caller's frame flag is restored.
        // This is essential for correct short-circuit and completion detection
        // during recursive chain traversal.
        state.popStageFrame();

        // Return the result from THIS stage
        // The pipeline will call next() again if there are more stages
        return result;
    }

    /**
     * Get the current stage index.
     *
     * @return the current stage index
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Get the stages list.
     *
     * @return the unmodifiable stages list
     */
    public List<ExecutionStage> getStages() {
        return stages;
    }
}