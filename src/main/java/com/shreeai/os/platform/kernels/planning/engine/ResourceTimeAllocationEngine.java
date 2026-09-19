package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;

/** Creates a deterministic schedule without executing tasks or replanning. */
public interface ResourceTimeAllocationEngine {
    /**
     * Packs whole tasks into eight-hour days, respecting every DAG edge.
     * Null constraints use the default 30-day soft horizon; null graphs are rejected.
     * Overruns are allowed and totals always describe the actual schedule.
     * Invalid explicit durations are rejected, including for empty graphs.
     */
    ExecutionPlan allocate(TaskGraph graph, UserConstraints constraints);
}
