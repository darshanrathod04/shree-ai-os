package com.shreeai.os.platform.runtime.pipeline;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public final class DefaultExecutionPipeline implements com.shreeai.os.platform.runtime.execution.ExecutionPipeline, com.shreeai.os.platform.runtime.pipeline.ExecutionPipeline {

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

    public com.shreeai.os.platform.runtime.execution.ExecutionResult execute(com.shreeai.os.platform.runtime.execution.ExecutionRequest request, com.shreeai.os.platform.runtime.execution.ExecutionContext context) {
        // Bridge: Convert execution contract to pipeline contract
        if (request == null || context == null) {
            return com.shreeai.os.platform.runtime.execution.ExecutionResult.failure(
                    request != null ? request.requestId() : "unknown",
                    "Request and context must not be null"
            );
        }

        try {
            // Convert runtime.execution.ExecutionRequest to V2 ExecutionRequest for PipelineContext
            com.shreeai.os.platform.kernels.execution.model.ExecutionRequest pipelineRequest =
                com.shreeai.os.platform.kernels.execution.model.ExecutionRequest.builder()
                    .requestId(request.requestId())
                    .requestType(request.requestType())
                    .parameters(request.metadata() != null ? new java.util.HashMap<>(request.metadata()) : new java.util.HashMap<>())
                    .build();
            // Convert ExecutionRequest to PipelineContext
            PipelineContext.Builder contextBuilder =
                    PipelineContext.builder()
                            .executionRequest(pipelineRequest);

// EO-V1.2 : propagate SDK metadata
            if (request.metadata() != null) {
                contextBuilder.addAttribute(
                        "requestMetadata",
                        request.metadata()
                );
            }

            PipelineContext pipelineContext = contextBuilder.build();

            // Execute using pipeline contract
            com.shreeai.os.platform.runtime.pipeline.PipelineResult pipelineResult = execute(pipelineContext);

            // Convert PipelineResult to ExecutionResult
            if (pipelineResult != null && pipelineResult.isSuccess()) {
                String output = pipelineResult.getMessages().isEmpty() 
                        ? "Pipeline completed successfully" 
                        : String.join("; ", pipelineResult.getMessages());
                return com.shreeai.os.platform.runtime.execution.ExecutionResult.success(
                        request.requestId(), 
                        output
                );
            } else {
                String error = pipelineResult != null && pipelineResult.getMessages() != null
                        ? String.join("; ", pipelineResult.getMessages())
                        : "Pipeline execution failed";
                return com.shreeai.os.platform.runtime.execution.ExecutionResult.failure(
                        request.requestId(), 
                        error
                );
            }
        } catch (Exception e) {
            return com.shreeai.os.platform.runtime.execution.ExecutionResult.failure(
                    request.requestId(),
                    "Pipeline execution error: " + e.getMessage()
            );
        }
    }
    
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
            // If the stage short-circuited, failed, or requested another
            // reasoning pass, stop execution.
            if (state.isShortCircuited() || state.isFailed() || state.requiresReReason()) {
                break;
            }
        }

        // P0.1 — Reflection Loop Execution.
        //
        // When ReflectionStage set requiresReReason, only the cognitive
        // segment (Reasoning → Inference → Planning → Reflection) is
        // re-executed. Identity, Context, Memory and Knowledge results
        // from the first pass are preserved and reused.
        //
        // The loop is bounded by the state's max reflection iterations, so
        // an infinite loop is impossible even if a custom reflection stage
        // ignores the limit.
        boolean reflectionLoopOccurred = false;
        int reflectionLoops = 0;
        while (state.requiresReReason()
                && !state.isFailed()
                && !state.isShortCircuited()
                && reflectionLoops < state.getMaxReflectionIterations()) {
            reflectionLoops++;
            reflectionLoopOccurred = true;
            state.clearRequiresReReason();
            result = executeStages(context, state, reflectionRewindStages());
        }

        // Resume the downstream stages after Reflection (e.g. MemoryStore,
        // ChiefReview) so the final response is still produced after the
        // reflection loops. This only runs when a loop interrupted the main
        // chain before those stages were reached.
        if (reflectionLoopOccurred
                && !state.isFailed()
                && !state.isShortCircuited()) {
            // The last rewind pass may have completed its segment chain and
            // marked the state as terminated; clear it so downstream stages
            // can run. The loop-request flag is consumed here too — once the
            // iteration bound is exhausted the loop must not continue, even
            // if a stage ignored the bound.
            state.resetTerminated();
            state.clearRequiresReReason();
            result = executeStages(context, state, downstreamStagesAfterReflection());
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
    public boolean isAccepting() {
        // Pipeline is always accepting unless explicitly shut down
        return true;
    }
    
    @Override
    public String pipelineName() {
        return "DefaultExecutionPipeline";
    }
    
    @Override
    public List<ExecutionStage> getStages() {
        return stages;
    }

    // =====================================================
    // P0.1 — REFLECTION LOOP HELPERS
    // =====================================================

    /**
     * Stage names of the cognitive segment that is re-executed when
     * reflection requests another reasoning pass. Identified by name so that
     * the rewind survives priority reshuffling of the stage list.
     */
    private static final Set<String> REFLECTION_REWIND_STAGE_NAMES =
            Set.of("Reasoning", "Inference", "Planning", "Reflection");

    /**
     * Executes the given stages in order through a fresh chain, reusing the
     * existing execution state.
     *
     * <p>Stops when a stage short-circuits, fails, or requests another
     * reflection pass.</p>
     *
     * @param context the pipeline context (never null)
     * @param state   the shared execution state (never null)
     * @param segment the segment stages to execute (never null, may be empty)
     * @return the last pipeline result produced, or null when the segment is empty
     */
    private PipelineResult executeStages(
            PipelineContext context,
            PipelineExecutionState state,
            List<ExecutionStage> segment) {
        DefaultExecutionChain segmentChain = new DefaultExecutionChain(segment);
        PipelineResult segmentResult = null;
        while (segmentChain.hasNext(context, state)) {
            segmentResult = segmentChain.next(context, state);
            if (state.isShortCircuited() || state.isFailed() || state.requiresReReason()) {
                break;
            }
        }
        return segmentResult;
    }

    /**
     * Returns the cognitive segment stages (Reasoning, Inference, Planning,
     * Reflection) in their original pipeline order.
     *
     * @return the rewind stages (never null, may be empty)
     */
    private List<ExecutionStage> reflectionRewindStages() {
        return stages.stream()
                .filter(stage -> REFLECTION_REWIND_STAGE_NAMES.contains(
                        stage.getDescriptor().getStageName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the stages that follow Reflection in the original pipeline
     * order (e.g. MemoryStore, ChiefReview).
     *
     * <p>These stages were not reached when the main chain was interrupted
     * by a reflection loop request. Executed once after the loops complete.</p>
     *
     * @return the downstream stages (never null, may be empty)
     */
    private List<ExecutionStage> downstreamStagesAfterReflection() {
        for (int i = 0; i < stages.size(); i++) {
            ExecutionStage stage = stages.get(i);
            if ("Reflection".equals(stage.getDescriptor().getStageName())) {
                return stages.subList(i + 1, stages.size());
            }
        }
        return List.of();
    }
}
