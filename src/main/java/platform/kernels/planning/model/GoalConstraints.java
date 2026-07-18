package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>GoalConstraints</b>
 *
 * <p>Represents immutable constraints associated with a goal within the Planning Kernel.
 * This model captures completion constraints, dependency limits, resource limits,
 * and associated metadata without performing any validation logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the constraints that bound goal achievement.</li>
 *   <li>Captures completion criteria, dependency, and resource limitations.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no validation logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class GoalConstraints {

    private final Map<String, String> completionConstraints;
    private final Map<String, String> dependencyLimits;
    private final Map<String, String> resourceLimits;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code GoalConstraints} with the specified parameters.
     *
     * @param completionConstraints the completion-related constraints (must not be {@code null})
     * @param dependencyLimits      the dependency-related limits (must not be {@code null})
     * @param resourceLimits        the resource-related limits (must not be {@code null})
     * @param metadata              additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public GoalConstraints(Map<String, String> completionConstraints,
                           Map<String, String> dependencyLimits,
                           Map<String, String> resourceLimits,
                           Map<String, String> metadata) {
        this.completionConstraints = Collections.unmodifiableMap(
                Objects.requireNonNull(completionConstraints, "completionConstraints must not be null"));
        this.dependencyLimits = Collections.unmodifiableMap(
                Objects.requireNonNull(dependencyLimits, "dependencyLimits must not be null"));
        this.resourceLimits = Collections.unmodifiableMap(
                Objects.requireNonNull(resourceLimits, "resourceLimits must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the completion constraints.
     *
     * @return the completion constraints map
     */
    public Map<String, String> completionConstraints() {
        return completionConstraints;
    }

    /**
     * Returns an unmodifiable view of the dependency limits.
     *
     * @return the dependency limits map
     */
    public Map<String, String> dependencyLimits() {
        return dependencyLimits;
    }

    /**
     * Returns an unmodifiable view of the resource limits.
     *
     * @return the resource limits map
     */
    public Map<String, String> resourceLimits() {
        return resourceLimits;
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
        if (!(o instanceof GoalConstraints that)) return false;
        return completionConstraints.equals(that.completionConstraints)
                && dependencyLimits.equals(that.dependencyLimits)
                && resourceLimits.equals(that.resourceLimits)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = completionConstraints.hashCode();
        result = 31 * result + dependencyLimits.hashCode();
        result = 31 * result + resourceLimits.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GoalConstraints{"
                + "completionConstraints=" + completionConstraints
                + ", dependencyLimits=" + dependencyLimits
                + ", resourceLimits=" + resourceLimits
                + ", metadata=" + metadata
                + '}';
    }
}