package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/** Immutable progress for one existing scheduled task. */
public record ProgressEntry(String taskId, TaskProgress status) {
    public ProgressEntry {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
    }
}
