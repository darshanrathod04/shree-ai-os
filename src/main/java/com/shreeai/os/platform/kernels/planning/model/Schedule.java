package com.shreeai.os.platform.kernels.planning.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Schedule</b>
 *
 * <p>Represents an immutable execution schedule within the Planning Kernel.
 * This model captures a planned sequence of tasks, scheduling constraints,
 * resource allocation references, and associated metadata without performing
 * any scheduling algorithm.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines an execution schedule with a planned task sequence.</li>
 *   <li>Captures scheduling constraints and resource allocation references.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Defensive copying — collections are copied on construction.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no scheduling algorithm.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 *
 * @see Task
 * @see SchedulingConstraints
 * @see ResourceAvailability
 */
public final class Schedule {

    private final List<Task> plannedSequence;
    private final SchedulingConstraints schedulingConstraints;
    private final List<String> resourceAllocationReferences;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code Schedule} with the specified parameters.
     *
     * @param plannedSequence              the ordered list of planned tasks (must not be {@code null})
     * @param schedulingConstraints        the scheduling constraints (must not be {@code null})
     * @param resourceAllocationReferences the resource allocation references (must not be {@code null})
     * @param metadata                     additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public Schedule(List<Task> plannedSequence,
                    SchedulingConstraints schedulingConstraints,
                    List<String> resourceAllocationReferences,
                    Map<String, String> metadata) {
        this.plannedSequence = List.copyOf(
                Objects.requireNonNull(plannedSequence, "plannedSequence must not be null"));
        this.schedulingConstraints = Objects.requireNonNull(
                schedulingConstraints, "schedulingConstraints must not be null");
        this.resourceAllocationReferences = List.copyOf(
                Objects.requireNonNull(resourceAllocationReferences,
                        "resourceAllocationReferences must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the planned task sequence.
     *
     * @return the list of {@link Task} in planned order
     */
    public List<Task> plannedSequence() {
        return plannedSequence;
    }

    /**
     * Returns the scheduling constraints for this schedule.
     *
     * @return the {@link SchedulingConstraints}
     */
    public SchedulingConstraints schedulingConstraints() {
        return schedulingConstraints;
    }

    /**
     * Returns an unmodifiable view of the resource allocation references.
     *
     * @return the list of resource allocation reference identifiers
     */
    public List<String> resourceAllocationReferences() {
        return resourceAllocationReferences;
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
        if (!(o instanceof Schedule schedule)) return false;
        return plannedSequence.equals(schedule.plannedSequence)
                && schedulingConstraints.equals(schedule.schedulingConstraints)
                && resourceAllocationReferences.equals(schedule.resourceAllocationReferences)
                && metadata.equals(schedule.metadata);
    }

    @Override
    public int hashCode() {
        int result = plannedSequence.hashCode();
        result = 31 * result + schedulingConstraints.hashCode();
        result = 31 * result + resourceAllocationReferences.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Schedule{"
                + "plannedSequence=" + plannedSequence
                + ", schedulingConstraints=" + schedulingConstraints
                + ", resourceAllocationReferences=" + resourceAllocationReferences
                + ", metadata=" + metadata
                + '}';
    }
}