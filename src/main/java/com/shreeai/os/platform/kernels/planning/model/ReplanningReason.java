package com.shreeai.os.platform.kernels.planning.model;

/** Explicit caller-supplied reason for incremental schedule repair. */
public enum ReplanningReason {
    DURATION_CHANGED,
    TASK_DELAYED,
    NEW_CONSTRAINT,
    MANUAL_REQUEST
}
