package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Task</b>
 *
 * <p>Represents an individual planned task within the Planning Kernel.
 * This model captures a task with its description, requirements, priority,
 * and metadata without performing any execution logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines an individual task within a plan.</li>
 *   <li>Captures task description, requirements, and priority.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @see PlanningId
 * @see TaskRequirements
 * @see Priority
 */
public final class Task {

    private final PlanningId planningId;
    private final String description;
    private final TaskRequirements requirements;
    private final Priority priority;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code Task} with the specified parameters.
     *
     * @param planningId   the unique planning identity (must not be {@code null})
     * @param description  the task description (must not be {@code null})
     * @param requirements the task requirements (must not be {@code null})
     * @param priority     the task priority (must not be {@code null})
     * @param metadata     additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public Task(PlanningId planningId,
                String description,
                TaskRequirements requirements,
                Priority priority,
                Map<String, String> metadata) {
        this.planningId = Objects.requireNonNull(planningId, "planningId must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.requirements = Objects.requireNonNull(requirements, "requirements must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns the unique identity of this task.
     *
     * @return the {@link PlanningId}
     */
    public PlanningId planningId() {
        return planningId;
    }

    /**
     * Returns the task description.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the requirements for this task.
     *
     * @return the {@link TaskRequirements}
     */
    public TaskRequirements requirements() {
        return requirements;
    }

    /**
     * Returns the priority of this task.
     *
     * @return the {@link Priority}
     */
    public Priority priority() {
        return priority;
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
        if (!(o instanceof Task task)) return false;
        return planningId.equals(task.planningId)
                && description.equals(task.description)
                && requirements.equals(task.requirements)
                && priority.equals(task.priority)
                && metadata.equals(task.metadata);
    }

    @Override
    public int hashCode() {
        int result = planningId.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + requirements.hashCode();
        result = 31 * result + priority.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Task{"
                + "planningId=" + planningId
                + ", description='" + description + '\''
                + ", requirements=" + requirements
                + ", priority=" + priority
                + ", metadata=" + metadata
                + '}';
    }
}