package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.ResourceAllocation;
import com.shreeai.os.platform.kernels.planning.model.ResourceType;
import com.shreeai.os.platform.kernels.planning.model.ScheduledTask;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stateless deterministic scheduler with an eight-hour daily capacity.
 * Every edge requires its successor to start on a strictly later day.
 * Independent tasks fill the earliest remaining capacity. The requested duration
 * is a soft horizon: no padding, truncation or replanning occurs on an overrun.
 */
public final class DefaultResourceTimeAllocationEngine implements ResourceTimeAllocationEngine {
    private static final int HOURS_PER_DAY = 8;
    private static final Pattern DURATION = Pattern.compile(
            "([0-9]+)\\s+(days?|weeks?|months?)", Pattern.CASE_INSENSITIVE);

    /** Parses explicit whole days, weeks (7 days), or months (30 days). */
    public int determineAvailableDays(String duration) {
        if (duration == null) {
            return 30;
        }
        Matcher matcher = DURATION.matcher(duration.strip());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("duration must be a positive whole number of days, weeks or months");
        }
        try {
            int amount = Integer.parseInt(matcher.group(1));
            int multiplier = switch (Character.toLowerCase(matcher.group(2).charAt(0))) {
                case 'w' -> 7;
                case 'm' -> 30;
                default -> 1;
            };
            if (amount < 1) {
                throw new IllegalArgumentException("duration must be positive");
            }
            return Math.multiplyExact(amount, multiplier);
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("duration exceeds supported whole days", ex);
        }
    }

    @Override
    public ExecutionPlan allocate(TaskGraph graph, UserConstraints constraints) {
        Objects.requireNonNull(graph, "graph must not be null");
        // A soft horizon must still be explicitly validated, including for empty graphs.
        determineAvailableDays(constraints == null ? null : constraints.duration());
        Map<String, List<String>> predecessors = new HashMap<>();
        for (var dependency : graph.dependencies()) {
            predecessors.computeIfAbsent(dependency.toTaskId(), key -> new ArrayList<>())
                    .add(dependency.fromTaskId());
        }
        Map<String, Integer> assignedDays = new HashMap<>();
        Map<Integer, Integer> usedHours = new HashMap<>();
        List<ScheduledTask> scheduled = new ArrayList<>();
        int totalHours = 0;
        int totalDays = 0;
        for (PlanningTask task : TaskGraph.topologicalOrder(graph.tasks(), graph.dependencies())) {
            int day = 1;
            for (String predecessor : predecessors.getOrDefault(task.taskId(), List.of())) {
                day = Math.max(day, Math.addExact(assignedDays.get(predecessor), 1));
            }
            int hours = hoursFor(task.type());
            while (usedHours.getOrDefault(day, 0) + hours > HOURS_PER_DAY) {
                day = Math.incrementExact(day);
            }
            assignedDays.put(task.taskId(), day);
            usedHours.merge(day, hours, Integer::sum);
            scheduled.add(new ScheduledTask(task.taskId(), day, hours,
                    List.of(new ResourceAllocation(resourceFor(task.type()), hours))));
            totalHours = Math.addExact(totalHours, hours);
            totalDays = Math.max(totalDays, day);
        }
        // Stable sort retains the graph's canonical topological tie-break within each day.
        scheduled.sort(Comparator.comparingInt(ScheduledTask::day));
        return new ExecutionPlan(totalDays, totalHours, scheduled);
    }

    private static int hoursFor(TaskType type) {
        return switch (type) {
            case LEARNING, ASSESSMENT -> 3;
            case IMPLEMENTATION -> 4;
            case PROJECT -> 8;
            case REVIEW -> 2;
        };
    }

    private static ResourceType resourceFor(TaskType type) {
        return switch (type) {
            case LEARNING -> ResourceType.STUDY;
            case IMPLEMENTATION -> ResourceType.CODING;
            case PROJECT -> ResourceType.PROJECT;
            case REVIEW -> ResourceType.REVIEW;
            case ASSESSMENT -> ResourceType.ASSESSMENT;
        };
    }
}
