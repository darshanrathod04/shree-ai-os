package com.shreeai.os.platform.intelligence.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>WorkflowEngine</b>
 *
 * <p>V3: Reusable orchestration engine that registers, validates, and
 * executes {@link Workflow}s. Steps run in execution order; a failing step
 * halts the run and marks the workflow as failed.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Maintains a thread-safe registry of named workflows.</li>
 *   <li>Validates workflow structure (unique step ids, valid dependencies).</li>
 *   <li>Executes steps in order, short-circuiting on failure.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Intelligence — Workflow Engine</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class WorkflowEngine {

    /**
     * Executes a single workflow step against shared input/output context.
     *
     * <p>Implementations mutate {@code context} to share state between steps
     * and return the step's own output plus a success flag.</p>
     */
    @FunctionalInterface
    public interface StepExecutor {
        WorkflowResult.StepOutcome execute(WorkflowStep step, Map<String, Object> context);
    }

    private final Map<String, Workflow> workflowsById = new LinkedHashMap<>();

    /**
     * Registers a workflow, replacing any existing workflow with the same id.
     *
     * @param workflow the workflow (never null)
     */
    public synchronized void register(Workflow workflow) {
        Objects.requireNonNull(workflow, "workflow must not be null");
        workflow.validate();
        workflowsById.put(workflow.id(), workflow);
    }

    /**
     * Returns whether a workflow with the given id is registered.
     *
     * @param workflowId the workflow id (never null)
     * @return true when registered
     */
    public synchronized boolean isRegistered(String workflowId) {
        Objects.requireNonNull(workflowId, "workflowId must not be null");
        return workflowsById.containsKey(workflowId);
    }

    /**
     * Looks up a workflow by id.
     *
     * @param workflowId the workflow id (never null)
     * @return the workflow, or empty
     */
    public synchronized Optional<Workflow> findById(String workflowId) {
        Objects.requireNonNull(workflowId, "workflowId must not be null");
        return Optional.ofNullable(workflowsById.get(workflowId));
    }

    /**
     * Removes a workflow by id.
     *
     * @param workflowId the workflow id (never null)
     * @return whether a workflow was removed
     */
    public synchronized boolean remove(String workflowId) {
        Objects.requireNonNull(workflowId, "workflowId must not be null");
        return workflowsById.remove(workflowId) != null;
    }

    /**
     * @return the number of registered workflows
     */
    public synchronized int size() {
        return workflowsById.size();
    }

    /**
     * Executes a registered workflow, running each step in execution order
     * against a shared context.
     *
     * <p>The passed {@code input} is copied into the shared context under the
     * key {@code "input"} before steps run, so steps can read it.</p>
     *
     * @param workflowId the workflow id (never null)
     * @param input      the execution input (may be empty)
     * @param executor   the step executor (never null)
     * @return the workflow result (never null)
     */
    public WorkflowResult execute(String workflowId, Map<String, Object> input,
                                  StepExecutor executor) {
        Objects.requireNonNull(workflowId, "workflowId must not be null");
        Objects.requireNonNull(executor, "executor must not be null");

        Workflow workflow;
        synchronized (this) {
            workflow = workflowsById.get(workflowId);
        }
        if (workflow == null) {
            return WorkflowResult.builder()
                    .workflowId(workflowId)
                    .status(WorkflowResult.Status.NOT_FOUND)
                    .build();
        }

        Map<String, Object> context = new LinkedHashMap<>();
        if (input != null) {
            context.put("input", input);
        }

        WorkflowResult.Builder resultBuilder = WorkflowResult.builder()
                .workflowId(workflowId)
                .status(WorkflowResult.Status.COMPLETED);

        for (WorkflowStep step : workflow.steps()) {
            WorkflowResult.StepOutcome outcome = executor.execute(step, context);
            resultBuilder.stepOutcome(outcome);
            if (!outcome.isSuccess()) {
                resultBuilder.status(WorkflowResult.Status.FAILED);
                break;
            }
            if (outcome.output() != null) {
                context.putAll(outcome.output());
            }
        }

        return resultBuilder.build();
    }
}
