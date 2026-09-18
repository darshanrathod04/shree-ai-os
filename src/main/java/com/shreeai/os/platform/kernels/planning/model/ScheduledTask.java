package com.shreeai.os.platform.kernels.planning.model;

import java.util.List;
import java.util.Objects;

/** A whole task assigned to a relative execution day, with one primary resource. */
public record ScheduledTask(String taskId, int day, int durationHours,
                            List<ResourceAllocation> resources) {
    public ScheduledTask {
        Objects.requireNonNull(taskId, "taskId must not be null");
        if (taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (day < 1 || durationHours < 1 || durationHours > 8) {
            throw new IllegalArgumentException("day must be >= 1 and durationHours must be 1..8");
        }
        resources = List.copyOf(resources);
        if (resources.size() != 1 || resources.getFirst().hours() != durationHours) {
            throw new IllegalArgumentException("one primary resource must cover the entire task");
        }
    }
}
