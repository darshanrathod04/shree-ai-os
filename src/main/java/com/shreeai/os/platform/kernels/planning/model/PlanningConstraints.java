package com.shreeai.os.platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningConstraints</b>
 *
 * <p>Represents immutable planning constraints within the Planning Kernel.
 * This model captures time constraints, dependency constraints, policy constraints,
 * and associated metadata without performing any constraint validation or enforcement.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the constraints that bound planning activity.</li>
 *   <li>Captures temporal, dependency, and policy limitations.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no validation or enforcement logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class PlanningConstraints {

    private final Map<String, String> timeConstraints;
    private final Map<String, String> dependencyConstraints;
    private final Map<String, String> policyConstraints;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code PlanningConstraints} with the specified parameters.
     *
     * @param timeConstraints       the time-related constraints (must not be {@code null})
     * @param dependencyConstraints the dependency-related constraints (must not be {@code null})
     * @param policyConstraints     the policy-related constraints (must not be {@code null})
     * @param metadata              additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public PlanningConstraints(Map<String, String> timeConstraints,
                               Map<String, String> dependencyConstraints,
                               Map<String, String> policyConstraints,
                               Map<String, String> metadata) {
        this.timeConstraints = Collections.unmodifiableMap(
                Objects.requireNonNull(timeConstraints, "timeConstraints must not be null"));
        this.dependencyConstraints = Collections.unmodifiableMap(
                Objects.requireNonNull(dependencyConstraints, "dependencyConstraints must not be null"));
        this.policyConstraints = Collections.unmodifiableMap(
                Objects.requireNonNull(policyConstraints, "policyConstraints must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the time constraints.
     *
     * @return the time constraints map
     */
    public Map<String, String> timeConstraints() {
        return timeConstraints;
    }

    /**
     * Returns an unmodifiable view of the dependency constraints.
     *
     * @return the dependency constraints map
     */
    public Map<String, String> dependencyConstraints() {
        return dependencyConstraints;
    }

    /**
     * Returns an unmodifiable view of the policy constraints.
     *
     * @return the policy constraints map
     */
    public Map<String, String> policyConstraints() {
        return policyConstraints;
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
        if (!(o instanceof PlanningConstraints that)) return false;
        return timeConstraints.equals(that.timeConstraints)
                && dependencyConstraints.equals(that.dependencyConstraints)
                && policyConstraints.equals(that.policyConstraints)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = timeConstraints.hashCode();
        result = 31 * result + dependencyConstraints.hashCode();
        result = 31 * result + policyConstraints.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlanningConstraints{"
                + "timeConstraints=" + timeConstraints
                + ", dependencyConstraints=" + dependencyConstraints
                + ", policyConstraints=" + policyConstraints
                + ", metadata=" + metadata
                + '}';
    }
}