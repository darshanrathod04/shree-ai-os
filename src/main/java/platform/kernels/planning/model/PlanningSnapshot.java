package platform.kernels.planning.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningSnapshot</b>
 *
 * <p>Represents an immutable snapshot of planning state within the Planning Kernel.
 * This model captures the current state of a plan including its objective, goals,
 * tasks, schedule, timestamp, and associated metadata.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines a point-in-time snapshot of planning state.</li>
 *   <li>Captures the planning objective, goals, tasks, and schedule.</li>
 *   <li>Records the timestamp when the snapshot was taken.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Defensive copying — collections are copied on construction.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — represents historical state only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @see PlanningObjective
 * @see Goal
 * @see Task
 * @see Schedule
 */
public final class PlanningSnapshot {

    private final PlanningObjective objective;
    private final List<Goal> goals;
    private final List<Task> tasks;
    private final Schedule schedule;
    private final Instant timestamp;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code PlanningSnapshot} with the specified parameters.
     *
     * @param objective the planning objective (must not be {@code null})
     * @param goals     the collection of goals (must not be {@code null})
     * @param tasks     the collection of tasks (must not be {@code null})
     * @param schedule  the execution schedule (must not be {@code null})
     * @param timestamp the snapshot timestamp (must not be {@code null})
     * @param metadata  additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public PlanningSnapshot(PlanningObjective objective,
                            List<Goal> goals,
                            List<Task> tasks,
                            Schedule schedule,
                            Instant timestamp,
                            Map<String, String> metadata) {
        this.objective = Objects.requireNonNull(objective, "objective must not be null");
        this.goals = List.copyOf(
                Objects.requireNonNull(goals, "goals must not be null"));
        this.tasks = List.copyOf(
                Objects.requireNonNull(tasks, "tasks must not be null"));
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns the planning objective at the time of this snapshot.
     *
     * @return the {@link PlanningObjective}
     */
    public PlanningObjective objective() {
        return objective;
    }

    /**
     * Returns an unmodifiable view of the goals in this snapshot.
     *
     * @return the list of {@link Goal}
     */
    public List<Goal> goals() {
        return goals;
    }

    /**
     * Returns an unmodifiable view of the tasks in this snapshot.
     *
     * @return the list of {@link Task}
     */
    public List<Task> tasks() {
        return tasks;
    }

    /**
     * Returns the schedule at the time of this snapshot.
     *
     * @return the {@link Schedule}
     */
    public Schedule schedule() {
        return schedule;
    }

    /**
     * Returns the timestamp when this snapshot was taken.
     *
     * @return the {@link Instant} timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanningSnapshot that)) return false;
        return objective.equals(that.objective)
                && goals.equals(that.goals)
                && tasks.equals(that.tasks)
                && schedule.equals(that.schedule)
                && timestamp.equals(that.timestamp)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = objective.hashCode();
        result = 31 * result + goals.hashCode();
        result = 31 * result + tasks.hashCode();
        result = 31 * result + schedule.hashCode();
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlanningSnapshot{"
                + "objective=" + objective
                + ", goals=" + goals
                + ", tasks=" + tasks
                + ", schedule=" + schedule
                + ", timestamp=" + timestamp
                + ", metadata=" + metadata
                + '}';
    }
}