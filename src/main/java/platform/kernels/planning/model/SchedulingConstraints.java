package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>SchedulingConstraints</b>
 *
 * <p>Represents immutable scheduling limitations within the Planning Kernel.
 * This model captures timing rules, ordering rules, dependency rules,
 * and associated metadata without performing any optimization logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the constraints that bound scheduling activity.</li>
 *   <li>Captures timing, ordering, and dependency rules.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no optimization logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class SchedulingConstraints {

    private final Map<String, String> timingRules;
    private final Map<String, String> orderingRules;
    private final Map<String, String> dependencyRules;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code SchedulingConstraints} with the specified parameters.
     *
     * @param timingRules     the timing-related rules (must not be {@code null})
     * @param orderingRules   the ordering-related rules (must not be {@code null})
     * @param dependencyRules the dependency-related rules (must not be {@code null})
     * @param metadata        additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public SchedulingConstraints(Map<String, String> timingRules,
                                 Map<String, String> orderingRules,
                                 Map<String, String> dependencyRules,
                                 Map<String, String> metadata) {
        this.timingRules = Collections.unmodifiableMap(
                Objects.requireNonNull(timingRules, "timingRules must not be null"));
        this.orderingRules = Collections.unmodifiableMap(
                Objects.requireNonNull(orderingRules, "orderingRules must not be null"));
        this.dependencyRules = Collections.unmodifiableMap(
                Objects.requireNonNull(dependencyRules, "dependencyRules must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the timing rules.
     *
     * @return the timing rules map
     */
    public Map<String, String> timingRules() {
        return timingRules;
    }

    /**
     * Returns an unmodifiable view of the ordering rules.
     *
     * @return the ordering rules map
     */
    public Map<String, String> orderingRules() {
        return orderingRules;
    }

    /**
     * Returns an unmodifiable view of the dependency rules.
     *
     * @return the dependency rules map
     */
    public Map<String, String> dependencyRules() {
        return dependencyRules;
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
        if (!(o instanceof SchedulingConstraints that)) return false;
        return timingRules.equals(that.timingRules)
                && orderingRules.equals(that.orderingRules)
                && dependencyRules.equals(that.dependencyRules)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = timingRules.hashCode();
        result = 31 * result + orderingRules.hashCode();
        result = 31 * result + dependencyRules.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SchedulingConstraints{"
                + "timingRules=" + timingRules
                + ", orderingRules=" + orderingRules
                + ", dependencyRules=" + dependencyRules
                + ", metadata=" + metadata
                + '}';
    }
}