package com.darshan.agent.runtime.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of ExecutionPipeline.
 *
 * <p>This component manages execution stages and processes them in order.
 * Stages are ordered by their descriptor priority.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Receive PipelineContext</li>
 *   <li>Create PipelineExecutionState</li>
 *   <li>Record timing</li>
 *   <li>Invoke first stage</li>
 *   <li>Freeze state to PipelineResult</li>
 *   <li>Return immutable PipelineResult</li>
 * </ul>
 *
 * <p>This class performs NO execution itself. It only orchestrates the pipeline.
 * Execution state is owned and managed by the Runtime.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * Do not modify without careful consideration of backward compatibility.</p>
 *
 * <p><strong>Shadow Mode:</strong> In production, the pipeline executes stages.
 * In shadow mode, the pipeline only logs the flow without executing.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
public final class DefaultExecutionPipeline implements ExecutionPipeline {

    private final List<ExecutionStage> stages;

    /**
     * Create a new DefaultExecutionPipeline with the given stages.
     *
     * <p>Stages are sorted by descriptor priority. Lower numbers execute first.</p>
     *
     * <p>This constructor validates stage ordering and fails fast if duplicate priorities exist.</p>
     *
     * @param stages the list of stages (never null, can be empty for shadow mode)
     */
    public DefaultExecutionPipeline(List<ExecutionStage> stages) {
        if (stages == null) {
            throw new IllegalArgumentException("stages cannot be null");
        }

        // Sort stages by descriptor priority (lower numbers execute first)
        List<ExecutionStage> sortedStages = new ArrayList<>(stages);
        sortedStages.sort((a, b) -> {
            int priorityA = a.getDescriptor().getPriority();
            int priorityB = b.getDescriptor().getPriority();
            return Integer.compare(priorityA, priorityB);
        });

        // Validate ordering - fail fast on duplicate priorities
        validateStageOrdering(sortedStages);

        this.stages = Collections.unmodifiableList(sortedStages);
    }

    /**
     * Validate stage ordering and fail fast on duplicate priorities.
     *
     * @param stages the sorted stages list
     * @throws IllegalStateException if duplicate priorities are detected
     */
    private void validateStageOrdering(List<ExecutionStage> stages) {
        Map<Integer, List<ExecutionStage>> stagesByPriority = stages.stream()
                .collect(Collectors.groupingBy(
                        stage -> stage.getDescriptor().getPriority()
                ));

        List<Integer> duplicatePriorities = stagesByPriority.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicatePriorities.isEmpty()) {
            throw new IllegalStateException(
                    "Duplicate stage priorities detected: " + duplicatePriorities +
                    ". Each stage must have a unique priority."
            );
        }
    }

    @Override
    public PipelineResult execute(PipelineContext context) {
        if (stages.isEmpty()) {
            // Shadow mode - no stages to execute
            return PipelineResult.builder()
                    .success(true)
                    .status("SHADOW")
                    .addMessage("Pipeline in shadow mode - no stages configured")
                    .build();
        }

        // Create execution state (Runtime owns execution state)
        PipelineExecutionState state = new PipelineExecutionState(stages);

        // Record start time
        state.markStartTime();

        // Create the execution chain
        DefaultExecutionChain chain = new DefaultExecutionChain(stages);
        
        // Execute stages one by one until completion or short-circuit
        PipelineResult result = null;
        while (chain.hasNext(context, state)) {
            result = chain.next(context, state);
            // If the stage short-circuited or failed, stop execution
            if (state.isShortCircuited() || state.isFailed()) {
                break;
            }
        }
        
        // If no result was created (empty pipeline), create a default one
        if (result == null) {
            result = PipelineResult.builder()
                    .success(true)
                    .status("COMPLETED")
                    .addMessage("No stages to execute")
                    .build();
        }

        // Record end time and calculate duration
        state.markEndTime();

        // Freeze state to immutable PipelineResult (created exactly once)
        return state.freeze();
    }

    @Override
    public List<ExecutionStage> getStages() {
        return stages;
    }
}