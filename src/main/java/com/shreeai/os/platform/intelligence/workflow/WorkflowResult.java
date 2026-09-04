package com.shreeai.os.platform.intelligence.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>WorkflowResult</b>
 *
 * <p>Outcome of a {@link WorkflowEngine} execution: the workflow id, the
 * overall terminal status, and the per-step execution results in execution
 * order.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Workflow Engine</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class WorkflowResult {

    /** Terminal status of a workflow execution. */
    public enum Status {
        COMPLETED, FAILED, NOT_FOUND
    }

    private final String workflowId;
    private final Status status;
    private final List<StepOutcome> stepOutcomes;

    private WorkflowResult(Builder builder) {
        this.workflowId = builder.workflowId;
        this.status = builder.status;
        this.stepOutcomes = Collections.unmodifiableList(
                new ArrayList<>(builder.stepOutcomes));
    }

    /** @return the workflow id (never null) */
    public String workflowId() {
        return workflowId;
    }

    /** @return the overall status (never null) */
    public Status status() {
        return status;
    }

    /** @return per-step outcomes in execution order (never null) */
    public List<StepOutcome> stepOutcomes() {
        return stepOutcomes;
    }

    /** @return whether all steps completed successfully */
    public boolean isSuccessful() {
        return status == Status.COMPLETED;
    }

    @Override
    public String toString() {
        return "WorkflowResult{workflowId='" + workflowId + "', status=" + status
                + ", stepOutcomes=" + stepOutcomes.size() + '}';
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link WorkflowResult}. */
    public static final class Builder {

        private String workflowId;
        private Status status;
        private List<StepOutcome> stepOutcomes = new ArrayList<>();

        private Builder() {
        }

        public Builder workflowId(String workflowId) {
            this.workflowId = Objects.requireNonNull(workflowId, "workflowId must not be null");
            return this;
        }

        public Builder status(Status status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
            return this;
        }

        public Builder stepOutcome(StepOutcome outcome) {
            stepOutcomes.add(outcome);
            return this;
        }

        public WorkflowResult build() {
            if (workflowId == null) {
                throw new IllegalArgumentException("workflowId must not be null");
            }
            if (status == null) {
                throw new IllegalArgumentException("status must not be null");
            }
            return new WorkflowResult(this);
        }
    }

    /** Immutable outcome of a single executed step. */
    public static final class StepOutcome {

        private final String stepId;
        private final String stepName;
        private final boolean success;
        private final Map<String, Object> output;

        private StepOutcome(String stepId, String stepName, boolean success,
                            Map<String, Object> output) {
            this.stepId = stepId;
            this.stepName = stepName;
            this.success = success;
            this.output = Map.copyOf(output);
        }

        /** @return the step id (never null) */
        public String stepId() {
            return stepId;
        }

        /** @return the step name (never null) */
        public String stepName() {
            return stepName;
        }

        /** @return whether the step succeeded */
        public boolean isSuccess() {
            return success;
        }

        /** @return the step output (never null) */
        public Map<String, Object> output() {
            return output;
        }

        /** Creates a successful step outcome. */
        public static StepOutcome success(String stepId, String stepName,
                                          Map<String, Object> output) {
            return new StepOutcome(stepId, stepName, true,
                    output == null ? Map.of() : output);
        }

        /** Creates a failed step outcome. */
        public static StepOutcome failure(String stepId, String stepName, String error) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("error", error == null ? "step failed" : error);
            return new StepOutcome(stepId, stepName, false, out);
        }
    }
}
