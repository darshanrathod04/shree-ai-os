package platform.kernels.planning.model;

import java.util.Objects;

/**
 * <b>PlanningId</b>
 *
 * <p>Represents the unique identity of a planning entity within the Planning Kernel.
 * This is the canonical identity value object for all Planning aggregate roots.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable, immutable identifier for planning entities.</li>
 *   <li>Ensures type-safe identity references across the platform.</li>
 *   <li>Encapsulates planning entity identification.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — this is a value object with no setters.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Constructor validation — rejects {@code null} values.</li>
 * </ul>
 *
 * <p><b>Consistency:</b> Maintains identical architectural style to
 * {@code IdentityId}, {@code MemoryId}, {@code ContextId}, {@code KnowledgeId},
 * and {@code CognitiveId}.</p>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @param value the unique identifier value (must not be {@code null})
 */
public record PlanningId(String value) {

    /**
     * Constructs a {@code PlanningId} with the given value.
     *
     * @param value the unique identifier value (must not be {@code null})
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public PlanningId {
        Objects.requireNonNull(value, "PlanningId value must not be null");
    }
}