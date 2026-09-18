package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.ProgressSnapshot;
import com.shreeai.os.platform.kernels.planning.model.ReplanningReason;
import com.shreeai.os.platform.kernels.planning.model.ReplanningResult;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;

/** Incrementally repairs a schedule without generating new tasks or estimating new effort. */
public interface AdaptiveReplanningEngine {
    /**
     * Completed tasks are pinned. Unfinished tasks never move earlier than their original day
     * or currentDay. Every dependency requires a strictly later day; daily capacity remains 8h.
     * Duration is a validated soft horizon, consistent with P2.3. Missing progress is NOT_STARTED.
     * Inconsistent graph, progress, or pinned work is rejected, never silently regenerated.
     */
    ReplanningResult replan(ExecutionPlan existing, TaskGraph graph, ProgressSnapshot progress,
            UserConstraints constraints, int currentDay, ReplanningReason reason);
}
