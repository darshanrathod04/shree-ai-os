package com.shreeai.os.platform.intelligence.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Workflow</b>
 *
 * <p>Immutable definition of a reusable orchestration workflow: a named,
 * ordered {@link List} of {@link WorkflowStep}s with validations for unique
 * step ids and well-formed dependency references.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Workflow Engine</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class Workflow {

    private final String id;
    private final String name;
    private final String description;
    private final List<WorkflowStep> steps;

    private Workflow(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.steps = Collections.unmodifiableList(
                builder.steps.stream()
                        .sorted(Comparator.comparingInt(WorkflowStep::order))
                        .toList());
    }

    /** @return the workflow id (never null) */
    public String id() {
        return id;
    }

    /** @return the workflow name (never null) */
    public String name() {
        return name;
    }

    /** @return the workflow description (never null) */
    public String description() {
        return description;
    }

    /** @return steps in execution order (never null) */
    public List<WorkflowStep> steps() {
        return steps;
    }

    /**
     * Looks up a step by id.
     *
     * @param stepId the step id
     * @return the matching step, or empty
     */
    public Optional<WorkflowStep> findStep(String stepId) {
        return steps.stream().filter(s -> s.id().equals(stepId)).findFirst();
    }

    /**
     * Validates the workflow structure: unique step ids and dependencies
     * that reference existing steps.
     *
     * @throws IllegalStateException if the workflow is malformed
     */
    public void validate() {
        Set<String> ids = new LinkedHashSet<>();
        for (WorkflowStep step : steps) {
            if (!ids.add(step.id())) {
                throw new IllegalStateException(
                        "Duplicate step id '" + step.id() + "' in workflow '" + id + "'");
            }
        }
        for (WorkflowStep step : steps) {
            for (String dependency : step.dependsOn()) {
                if (!ids.contains(dependency)) {
                    throw new IllegalStateException(
                            "Step '" + step.id() + "' depends on unknown step '" + dependency + "'");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Workflow{id='" + id + "', name='" + name + "', steps=" + steps.size() + '}';
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link Workflow}. */
    public static final class Builder {

        private String id;
        private String name;
        private String description = "";
        private List<WorkflowStep> steps = new ArrayList<>();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id must not be null");
            return this;
        }

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder steps(List<WorkflowStep> steps) {
            this.steps = steps == null ? new ArrayList<>() : new ArrayList<>(steps);
            return this;
        }

        public Builder step(WorkflowStep step) {
            steps.add(step);
            return this;
        }

        public Workflow build() {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            return new Workflow(this);
        }
    }
}
