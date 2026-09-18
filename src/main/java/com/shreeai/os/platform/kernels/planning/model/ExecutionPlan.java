package com.shreeai.os.platform.kernels.planning.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Canonical immutable schedule. Totals describe actual work, not the soft deadline. */
public record ExecutionPlan(int totalDays, int totalHours, List<ScheduledTask> scheduledTasks) {
    public ExecutionPlan {
        scheduledTasks = List.copyOf(scheduledTasks);
        Set<String> ids = new HashSet<>();
        Map<Integer, Integer> hoursByDay = new HashMap<>();
        int hours = 0;
        int days = 0;
        int previousDay = 0;
        for (ScheduledTask task : scheduledTasks) {
            if (!ids.add(task.taskId())) {
                throw new IllegalArgumentException("duplicate scheduled task: " + task.taskId());
            }
            if (task.day() < previousDay) {
                throw new IllegalArgumentException("scheduledTasks must be ordered by day");
            }
            previousDay = task.day();
            days = Math.max(days, task.day());
            hours = Math.addExact(hours, task.durationHours());
            if (hoursByDay.merge(task.day(), task.durationHours(), Integer::sum) > 8) {
                throw new IllegalArgumentException("daily capacity exceeds 8 hours");
            }
        }
        if (totalDays != days || totalHours != hours) {
            throw new IllegalArgumentException("totals must equal actual scheduled days and hours");
        }
    }

    public static ExecutionPlan empty() {
        return new ExecutionPlan(0, 0, List.of());
    }
}
