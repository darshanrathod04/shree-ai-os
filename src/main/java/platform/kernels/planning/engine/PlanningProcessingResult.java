package platform.kernels.planning.engine;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.planning.model.Goal;
import platform.kernels.planning.model.PlanningObjective;
import platform.kernels.planning.model.Priority;
import platform.kernels.planning.model.Schedule;
import platform.kernels.planning.model.Task;

/**
 * <b>PlanningProcessingResult</b>
 *
 * <p>Immutable value object representing the result of a planning processing operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates deterministic processing outcomes.</li>
 *   <li>Provides immutable result representation.</li>
 *   <li>Contains no behavior — data carrier only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object. All collections are unmodifiable.
 * Defensive copying is applied to all mutable inputs. This class is final with final fields.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-106, EIO-ARCH-001</p>
 *
 * @param successful     whether the processing succeeded
 * @param processedAt    the timestamp when processing occurred
 * @param metadata       additional processing metadata
 * @param objective      the planning objective (may be {@code null})
 * @param goals          the list of generated goals (may be {@code null})
 * @param tasks          the list of generated tasks (may be {@code null})
 * @param schedule       the generated schedule (may be {@code null})
 * @param priorities     the list of generated priorities (may be {@code null})
 */
public final class PlanningProcessingResult {

    private final boolean successful;
    private final Instant processedAt;
    private final Map<String, Object> metadata;
    private final PlanningObjective objective;
    private final List<Goal> goals;
    private final List<Task> tasks;
    private final Schedule schedule;
    private final List<Priority> priorities;

    /**
     * Creates a new {@code PlanningProcessingResult} with the specified parameters.
     *
     * <p>Performs defensive validation and creates immutable copies of all collections.</p>
     *
     * @param successful  whether the processing succeeded
     * @param processedAt the timestamp when processing occurred (must not be {@code null})
     * @param metadata    additional processing metadata (must not be {@code null})
     * @param objective   the planning objective (may be {@code null})
     * @param goals       the list of generated goals (may be {@code null})
     * @param tasks       the list of generated tasks (may be {@code null})
     * @param schedule    the generated schedule (may be {@code null})
     * @param priorities  the list of generated priorities (may be {@code null})
     * @throws IllegalArgumentException if any validation constraint is violated
     */
    public PlanningProcessingResult(
            boolean successful,
            Instant processedAt,
            Map<String, Object> metadata,
            PlanningObjective objective,
            List<Goal> goals,
            List<Task> tasks,
            Schedule schedule,
            List<Priority> priorities) {
        Objects.requireNonNull(processedAt, "PlanningProcessingResult processedAt must not be null");
        Objects.requireNonNull(metadata, "PlanningProcessingResult metadata must not be null");

        this.successful = successful;
        this.processedAt = processedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.objective = objective;
        this.goals = goals != null ? List.copyOf(goals) : null;
        this.tasks = tasks != null ? List.copyOf(tasks) : null;
        this.schedule = schedule;
        this.priorities = priorities != null ? List.copyOf(priorities) : null;
    }

    /**
     * Returns whether the processing was successful.
     *
     * @return {@code true} if processing succeeded, {@code false} otherwise
     */
    public boolean successful() {
        return successful;
    }

    /**
     * Returns the timestamp when processing occurred.
     *
     * @return the processing {@link Instant}
     */
    public Instant processedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable view of the processing metadata.
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns the planning objective, if present.
     *
     * @return the planning objective, or {@code null} if not present
     */
    public PlanningObjective objective() {
        return objective;
    }

    /**
     * Returns an unmodifiable list of generated goals, if present.
     *
     * @return the list of goals, or {@code null} if not present
     */
    public List<Goal> goals() {
        return goals;
    }

    /**
     * Returns an unmodifiable list of generated tasks, if present.
     *
     * @return the list of tasks, or {@code null} if not present
     */
    public List<Task> tasks() {
        return tasks;
    }

    /**
     * Returns the generated schedule, if present.
     *
     * @return the schedule, or {@code null} if not present
     */
    public Schedule schedule() {
        return schedule;
    }

    /**
     * Returns an unmodifiable list of generated priorities, if present.
     *
     * @return the list of priorities, or {@code null} if not present
     */
    public List<Priority> priorities() {
        return priorities;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>The equality is based on all fields.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlanningProcessingResult that)) return false;
        return successful == that.successful
                && Objects.equals(processedAt, that.processedAt)
                && Objects.equals(metadata, that.metadata)
                && Objects.equals(objective, that.objective)
                && Objects.equals(goals, that.goals)
                && Objects.equals(tasks, that.tasks)
                && Objects.equals(schedule, that.schedule)
                && Objects.equals(priorities, that.priorities);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, processedAt, metadata, objective, goals, tasks, schedule, priorities);
    }

    /**
     * Returns a string representation of the result.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "PlanningProcessingResult{"
                + "successful=" + successful
                + ", processedAt=" + processedAt
                + ", metadata=" + metadata
                + ", objective=" + objective
                + ", goals=" + goals
                + ", tasks=" + tasks
                + ", schedule=" + schedule
                + ", priorities=" + priorities
                + '}';
    }
}