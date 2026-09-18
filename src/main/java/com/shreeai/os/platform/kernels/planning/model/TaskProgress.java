package com.shreeai.os.platform.kernels.planning.model;

/** Execution status supplied by the caller; never inferred from wall-clock time. */
public enum TaskProgress {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
