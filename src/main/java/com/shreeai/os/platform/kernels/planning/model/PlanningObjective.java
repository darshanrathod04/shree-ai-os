package com.shreeai.os.platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningObjective</b>
 *
 * <p>Represents the objective that drives planning within the Planning Kernel.
 * This model captures the intent and scope of a planning effort without performing
 * any planning itself.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the objective that motivates planning activity.</li>
 *   <li>Captures planning scope and descriptive intent.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no planning algorithms or business logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @see PlanningId
 */
public final class PlanningObjective {

    private final PlanningId planningId;
    private final String description;
    private final String scope;
    private final Map<String, Object> metadata;

    /**
     * Constructs a {@code PlanningObjective} with the specified parameters.
     *
     * @param planningId  the unique planning identity (must not be {@code null})
     * @param description the objective description (must not be {@code null})
     * @param scope       the planning scope (must not be {@code null})
     * @param metadata    additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public PlanningObjective(
            PlanningId planningId,
            String description,
            String scope,
            Map<String, Object> metadata
    ) {
        this.planningId = Objects.requireNonNull(planningId);
        this.description = Objects.requireNonNull(description);
        this.scope = Objects.requireNonNull(scope);
        this.metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    /**
     * Returns the unique identity of this planning objective.
     *
     * @return the {@link PlanningId}
     */
    public PlanningId planningId() {
        return planningId;
    }

    /**
     * Returns the objective description.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the planning scope.
     *
     * @return the scope
     */
    public String scope() {
        return scope;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanningObjective that)) return false;
        return planningId.equals(that.planningId)
                && description.equals(that.description)
                && scope.equals(that.scope)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = planningId.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + scope.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlanningObjective{"
                + "planningId=" + planningId
                + ", description='" + description + '\''
                + ", scope='" + scope + '\''
                + ", metadata=" + metadata
                + '}';
    }
}