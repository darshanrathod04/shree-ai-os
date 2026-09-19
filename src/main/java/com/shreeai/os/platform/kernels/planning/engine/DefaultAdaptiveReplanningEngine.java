package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.ProgressSnapshot;
import com.shreeai.os.platform.kernels.planning.model.ReplanningReason;
import com.shreeai.os.platform.kernels.planning.model.ReplanningResult;
import com.shreeai.os.platform.kernels.planning.model.ScheduledTask;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stateless, order-preserving forward repair of an existing execution plan.
 * Original list order (including milestone order) is retained. Within this order,
 * no unfinished task moves earlier, and each is assigned its earliest feasible day.
 * Thus no task moves unless keeping its old day is infeasible under these rules.
 * Completed work reserves capacity before any repair. Inconsistent snapshots fail
 * atomically. No clocks, task generation, duration estimates, or external services.
 */
public final class DefaultAdaptiveReplanningEngine implements AdaptiveReplanningEngine {
    @Override
    public ReplanningResult replan(ExecutionPlan existing, TaskGraph graph, ProgressSnapshot progress,
            UserConstraints constraints, int currentDay, ReplanningReason reason) {
        Objects.requireNonNull(existing, "existing must not be null");
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(progress, "progress must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (currentDay < 1) {
            throw new IllegalArgumentException("currentDay must start at 1");
        }
        // P2.3's approved soft horizon: validate, but never compress effort or cap an overrun.
        new DefaultResourceTimeAllocationEngine().determineAvailableDays(
                constraints == null ? null : constraints.duration());
        Map<String, ScheduledTask> original = new HashMap<>();
        for (ScheduledTask task : existing.scheduledTasks()) {
            original.put(task.taskId(), task);
        }
        if (graph.tasks().size() != original.size()
                || graph.tasks().stream().anyMatch(task -> !original.containsKey(task.taskId()))) {
            throw new IllegalArgumentException("graph and existing schedule must contain exactly the same tasks");
        }
        Map<String, List<String>> predecessors = new HashMap<>();
        for (var edge : graph.dependencies()) {
            if (original.get(edge.fromTaskId()).day() >= original.get(edge.toTaskId()).day()) {
                throw new IllegalArgumentException("existing schedule violates a dependency");
            }
            predecessors.computeIfAbsent(edge.toTaskId(), key -> new ArrayList<>()).add(edge.fromTaskId());
        }
        Map<String, TaskProgress> statuses = new HashMap<>();
        for (var entry : progress.progress()) {
            if (!original.containsKey(entry.taskId())) {
                throw new IllegalArgumentException("unknown progress task: " + entry.taskId());
            }
            statuses.put(entry.taskId(), entry.status());
        }
        Map<Integer, Integer> usedHours = new HashMap<>();
        for (ScheduledTask task : existing.scheduledTasks()) {
            if (statuses.get(task.taskId()) == TaskProgress.COMPLETED) {
                if (task.day() > currentDay) {
                    throw new IllegalArgumentException("completed task lies after currentDay");
                }
                for (String predecessor : predecessors.getOrDefault(task.taskId(), List.of())) {
                    if (statuses.get(predecessor) != TaskProgress.COMPLETED) {
                        throw new IllegalArgumentException("completed task has an unfinished predecessor");
                    }
                }
                usedHours.merge(task.day(), task.durationHours(), Integer::sum);
            }
        }
        Map<String, Integer> assignedDays = new HashMap<>();
        List<ScheduledTask> repaired = new ArrayList<>();
        int previousDay = 0;
        int shifted = 0;
        for (ScheduledTask task : existing.scheduledTasks()) {
            int earliest = previousDay;
            for (String predecessor : predecessors.getOrDefault(task.taskId(), List.of())) {
                earliest = Math.max(earliest, Math.incrementExact(assignedDays.get(predecessor)));
            }
            int day = task.day();
            if (statuses.get(task.taskId()) == TaskProgress.COMPLETED) {
                if (day < earliest) {
                    throw new IllegalArgumentException("completed task conflicts with preserved schedule order");
                }
            } else {
                day = Math.max(Math.max(day, currentDay), earliest);
                while (usedHours.getOrDefault(day, 0) + task.durationHours() > 8) {
                    day = Math.incrementExact(day);
                }
                usedHours.merge(day, task.durationHours(), Integer::sum);
            }
            assignedDays.put(task.taskId(), day);
            previousDay = day;
            if (day == task.day()) {
                repaired.add(task);
            } else {
                shifted++;
                repaired.add(new ScheduledTask(task.taskId(), day, task.durationHours(), task.resources()));
            }
        }
        ExecutionPlan plan = shifted == 0 ? existing
                : new ExecutionPlan(previousDay, existing.totalHours(), repaired);
        return new ReplanningResult(plan, reason, shifted);
    }
}
