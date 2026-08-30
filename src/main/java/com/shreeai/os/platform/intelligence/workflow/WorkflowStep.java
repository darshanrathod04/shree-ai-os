package com.shreeai.os.platform.intelligence.workflow;

import java.util.Objects;
import java.util.Set;

/**
 * <b>WorkflowStep</b>
 *
 * <p>Immutable definition of a single step within a {@link Workflow}. Each
 * step declares a stable id, a name, the action to perform, an execution
 * order, and optional dependencies on other step ids.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Workflow Engine</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class WorkflowStep {

    private final String id;
    private final String name;
    private final String action;
    private final int order;
    private final Set<String> dependsOn;

    private WorkflowStep(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.action = builder.action;
        this.order = builder.order;
        this.dependsOn = builder.dependsOn == null
                ? Set.of()
                : Set.copyOf(builder.dependsOn);
    }

    /** @return the stable step id (never null) */
    public String id() {
        return id;
    }

    /** @return the step name (never null) */
    public String name() {
        return name;
    }

    /** @return the action this step performs (never null) */
    public String action() {
        return action;
    }

    /** @return the execution order (higher runs later) */
    public int order() {
        return order;
    }

    /** @return the ids of steps this step depends on (never null) */
    public Set<String> dependsOn() {
        return dependsOn;
    }

    @Override
    public String toString() {
        return "WorkflowStep{id='" + id + "', name='" + name + "', order=" + order + '}';
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Creates a step with the given id, name, action, and order. */
    public static WorkflowStep of(String id, String name, String action, int order) {
        return builder().id(id).name(name).action(action).order(order).build();
    }

    /** Fluent builder for {@link WorkflowStep}. */
    public static final class Builder {

        private String id;
        private String name;
        private String action;
        private int order;
        private Set<String> dependsOn;

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

        public Builder action(String action) {
            this.action = Objects.requireNonNull(action, "action must not be null");
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder dependsOn(Set<String> dependsOn) {
            this.dependsOn = dependsOn;
            return this;
        }

        public WorkflowStep build() {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            if (action == null) {
                throw new IllegalArgumentException("action must not be null");
            }
            return new WorkflowStep(this);
        }
    }
}
