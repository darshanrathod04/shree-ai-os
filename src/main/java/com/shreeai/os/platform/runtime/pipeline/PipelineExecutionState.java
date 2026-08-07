package com.shreeai.os.platform.runtime.pipeline;

import com.shreeai.os.platform.execution.ExecutionMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal runtime execution state.
 *
 * <p>This class is the single source of truth for pipeline execution state.
 * It is owned exclusively by the Runtime and is never exposed publicly.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Track current stage index</li>
 *   <li>Record visited stages</li>
 *   <li>Record completed stages</li>
 *   <li>Collect messages</li>
 *   <li>Store metadata</li>
 *   <li>Track timing (start, end, duration)</li>
 *   <li>Track failure state</li>
 *   <li>Track short-circuit state</li>
 *   <li>Track termination state</li>
 * </ul>
 *
 * <p>This class is NOT thread-safe by design. It is runtime-local and never shared
 * between executions. Each pipeline execution gets its own state instance.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.
 * This class is internal to the runtime and should not be used by stages or external code.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
public final class PipelineExecutionState {

    // =====================================================
    // EXECUTION STATE
    // =====================================================

    private final List<ExecutionStage> stages;
    private final List<String> visitedStages;
    private final List<String> completedStages;
    private final List<String> messages;
    private final Map<String, Object> metadata;
    private Instant startTime;
    private Instant endTime;
    private long duration;
    private boolean failed;
    private boolean shortCircuited;
    private boolean terminated;
    private boolean nextStageInvoked;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    /**
     * Create a new execution state for the given stages.
     *
     * @param stages the ordered list of stages (never null, never empty)
     */
    public PipelineExecutionState(List<ExecutionStage> stages) {
        if (stages == null) {
            throw new IllegalArgumentException("stages cannot be null");
        }

        this.stages = new ArrayList<>(stages);
        this.visitedStages = new ArrayList<>();
        this.completedStages = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();
        this.failed = false;
        this.shortCircuited = false;
        this.terminated = false;
        this.nextStageInvoked = false;
    }

    // =====================================================
    // STAGE EXECUTION TRACKING
    // =====================================================

    /**
     * Mark the current stage as started.
     *
     * <p>This method is called by the runtime before invoking a stage.</p>
     *
     * @return the stage name that was started
     */
    public String markStageStarted(String stageName) {
        if (stageName == null) {
            throw new IllegalArgumentException("stageName cannot be null");
        }

        visitedStages.add(stageName);
        // Reset the flag for this new stage
        this.nextStageInvoked = false;
        return stageName;
    }

    /**
     * Advance to the next stage.
     *
     * <p>This method is called after a stage completes successfully.</p>
     */
    void advanceStage() {
        // No-op: kept for backward compatibility
        // The chain's index is the single source of truth
    }

    /**
     * Mark the current stage as completed.
     *
     * <p>This method is called by the runtime after a stage completes successfully.
     * Note: This does NOT advance the index. The chain's index tracks which stage to execute next.</p>
     *
     * @param stageName the name of the stage that completed
     */
    public void markStageCompleted(String stageName) {
        if (!completedStages.contains(stageName)) {
            completedStages.add(stageName);
        }
    }

    /**
     * Mark the pipeline as short-circuited.
     *
     * <p>This method is called when a stage returns a result without calling chain.next().</p>
     */
    void markShortCircuit() {
        this.shortCircuited = true;
    }

    /**
     * Mark the pipeline as failed.
     *
     * @param message the failure message (never null)
     */
    public void markFailure(String message) {
        this.failed = true;
        addMessage(message);
    }

    /**
     * Mark the pipeline as terminated.
     */
    void markTerminated() {
        this.terminated = true;
    }

    /**
     * Mark that the next stage was invoked.
     *
     * <p>This is called when chain.next() is invoked, indicating the stage
     * wants to continue to the next stage.</p>
     */
    public void markNextStageInvoked() {
        this.nextStageInvoked = true;
    }

    /**
     * Check if the next stage was invoked.
     *
     * @return true if chain.next() was called
     */
    public boolean wasNextStageInvoked() {
        return nextStageInvoked;
    }

    /**
     * Reset the next stage invoked flag.
     *
     * <p>This is called before invoking a stage to reset the flag for the next check.</p>
     */
    public void resetNextStageInvoked() {
        this.nextStageInvoked = false;
    }

    // =====================================================
    // MESSAGES AND METADATA
    // =====================================================

    /**
     * Add a message to the execution log.
     *
     * @param message the message to add (never null)
     */
    public void addMessage(String message) {
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Add metadata to the execution state.
     *
     * @param key the metadata key (never null)
     * @param value the metadata value (can be null)
     */
    public void addMetadata(String key, Object value) {
        if (key != null) {
            metadata.put(key, value);
        }
    }

    // =====================================================
    // TIMING
    // =====================================================

    /**
     * Mark the start time of pipeline execution.
     */
    void markStartTime() {
        this.startTime = Instant.now();
    }

    /**
     * Mark the end time of pipeline execution and calculate duration.
     */
    void markEndTime() {
        this.endTime = Instant.now();
        if (startTime != null) {
            this.duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    // =====================================================
    // STATE QUERIES
    // =====================================================

    /**
     * Check if there are more stages to execute.
     *
     * @return true if there are more stages
     */
    boolean hasNextStage() {
        // This method is deprecated - the chain controls stage progression
        // Kept for backward compatibility
        return !visitedStages.isEmpty() && visitedStages.size() < stages.size();
    }

    /**
     * Get the next stage to execute.
     *
     * @return the next stage, or null if no more stages
     */
    ExecutionStage getNextStage() {
        // This method is deprecated - the chain controls stage progression
        // Kept for backward compatibility
        int index = visitedStages.size();
        if (index >= stages.size()) {
            return null;
        }
        return stages.get(index);
    }

    /**
     * Get the current stage index.
     *
     * @return the current stage index
     */
    int getCurrentStageIndex() {
        // This method is deprecated - the chain controls stage progression
        // Kept for backward compatibility
        return visitedStages.size();
    }

    /**
     * Get the total number of stages.
     *
     * @return the total stage count
     */
    int getTotalStages() {
        return stages.size();
    }

    /**
     * Check if the pipeline failed.
     *
     * @return true if the pipeline failed
     */
    boolean isFailed() {
        return failed;
    }

    /**
     * Check if the pipeline was short-circuited.
     *
     * @return true if the pipeline was short-circuited
     */
    public boolean isShortCircuited() {
        return shortCircuited;
    }

    /**
     * Check if the pipeline was terminated.
     *
     * @return true if the pipeline was terminated
     */
    boolean isTerminated() {
        return terminated;
    }

    /**
     * Get the visited stages list.
     *
     * @return unmodifiable list of visited stage names
     */
    public List<String> getVisitedStages() {
        return Collections.unmodifiableList(visitedStages);
    }

    /**
     * Get the completed stages list.
     *
     * @return unmodifiable list of completed stage names
     */
    public List<String> getCompletedStages() {
        return Collections.unmodifiableList(completedStages);
    }

    /**
     * Get the messages list.
     *
     * @return unmodifiable list of messages
     */
    List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Get the metadata map.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Get the start time.
     *
     * @return the start time, or null if not started
     */
    Instant getStartTime() {
        return startTime;
    }

    /**
     * Get the end time.
     *
     * @return the end time, or null if not finished
     */
    Instant getEndTime() {
        return endTime;
    }

    /**
     * Get the duration in milliseconds.
     *
     * @return the duration in milliseconds
     */
    long getDuration() {
        return duration;
    }

    // =====================================================
    // FREEZE TO IMMUTABLE RESULT
    // =====================================================

    /**
     * Freeze the execution state into an immutable PipelineResult.
     *
     * <p>This method creates a PipelineResult snapshot from the current state.
     * After calling this method, the state should not be modified.</p>
     *
     * <p>This method is called exactly once at the end of pipeline execution.</p>
     *
     * @return an immutable PipelineResult snapshot
     */
    PipelineResult freeze() {
        String status;
        boolean success;

        if (failed) {
            status = "FAILED";
            success = false;
        } else if (shortCircuited) {
            status = "SHORT_CIRCUIT";
            success = false;
        } else if (terminated) {
            status = "TERMINATED";
            success = false;
        } else if (visitedStages.size() >= stages.size()) {
            status = "COMPLETED";
            success = true;
        } else {
            status = "INCOMPLETE";
            success = false;
        }

        String currentStage = null;
        if (!visitedStages.isEmpty() && visitedStages.size() < stages.size()) {
            currentStage = stages.get(visitedStages.size()).getDescriptor().getStageName();
        }

        return PipelineResult.builder()
                .success(success)
                .status(status)
                .currentStage(currentStage)
                .completedStages(completedStages)
                .processingTime(duration)
                .messages(messages)
                .metadata(buildExecutionMetadata())
                .timestamp(endTime != null ? endTime : Instant.now())
                .build();
    }

    /**
     * Build ExecutionMetadata from the state.
     *
     * @return the execution metadata
     */
    private ExecutionMetadata buildExecutionMetadata() {
        return ExecutionMetadata.builder()
                .executionSource("RuntimePipeline")
                .addCustomValue("visitedStages", visitedStages.size())
                .addCustomValue("completedStages", completedStages.size())
                .addCustomValue("failed", failed)
                .addCustomValue("shortCircuited", shortCircuited)
                .addCustomValue("terminated", terminated)
                .addCustomValue("duration", duration)
                .build();
    }

    // =====================================================
    // TO STRING
    // =====================================================

    @Override
    public String toString() {
        return "PipelineExecutionState{" +
                "currentStageIndex=" + visitedStages.size() +
                ", totalStages=" + stages.size() +
                ", visitedStages=" + visitedStages +
                ", completedStages=" + completedStages +
                ", failed=" + failed +
                ", shortCircuited=" + shortCircuited +
                ", terminated=" + terminated +
                ", duration=" + duration + "ms" +
                '}';
    }
}