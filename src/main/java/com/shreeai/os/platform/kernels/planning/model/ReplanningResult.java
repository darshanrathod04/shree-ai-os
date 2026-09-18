package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/** Immutable result of repairing an existing schedule. */
public record ReplanningResult(ExecutionPlan executionPlan, ReplanningReason reason, int shiftedTasks) {
    public ReplanningResult {
        Objects.requireNonNull(executionPlan, "executionPlan must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (shiftedTasks < 0 || shiftedTasks > executionPlan.scheduledTasks().size()) {
            throw new IllegalArgumentException("shiftedTasks must be within the scheduled task count");
        }
    }
}
