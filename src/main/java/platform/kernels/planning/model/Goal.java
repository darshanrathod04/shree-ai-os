package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Goal</b>
 *
 * <p>Represents a planning goal within the Planning Kernel.
 * This model captures a goal with its associated objective, constraints,
 * and metadata without performing any decomposition or planning logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines a planning goal with its identity and objective.</li>
 *   <li>Captures goal-specific constraints.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no decomposition logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @see PlanningId
 * @see PlanningObjective
 * @see GoalConstraints
 */
public final class Goal {

    private final PlanningId planningId;
    private final PlanningObjective objective;
    private final GoalConstraints constraints;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code Goal} with the specified parameters.
     *
     * @param planningId  the unique planning identity (must not be {@code null})
     * @param objective   the planning objective (must not be {@code null})
     * @param constraints the goal constraints (must not be {@code null})
     * @param metadata    additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public Goal(PlanningId planningId,
                PlanningObjective objective,
                GoalConstraints constraints,
                Map<String, String> metadata) {
        this.planningId = Objects.requireNonNull(planningId, "planningId must not be null");
        this.objective = Objects.requireNonNull(objective, "objective must not be null");
        this.constraints = Objects.requireNonNull(constraints, "constraints must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns the unique identity of this goal.
     *
     * @return the {@link PlanningId}
     */
    public PlanningId planningId() {
        return planningId;
    }

    /**
     * Returns the planning objective associated with this goal.
     *
     * @return the {@link PlanningObjective}
     */
    public PlanningObjective objective() {
        return objective;
    }

    /**
     * Returns the constraints associated with this goal.
     *
     * @return the {@link GoalConstraints}
     */
    public GoalConstraints constraints() {
        return constraints;
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
        if (!(o instanceof Goal goal)) return false;
        return planningId.equals(goal.planningId)
                && objective.equals(goal.objective)
                && constraints.equals(goal.constraints)
                && metadata.equals(goal.metadata);
    }

    @Override
    public int hashCode() {
        int result = planningId.hashCode();
        result = 31 * result + objective.hashCode();
        result = 31 * result + constraints.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Goal{"
                + "planningId=" + planningId
                + ", objective=" + objective
                + ", constraints=" + constraints
                + ", metadata=" + metadata
                + '}';
    }
}