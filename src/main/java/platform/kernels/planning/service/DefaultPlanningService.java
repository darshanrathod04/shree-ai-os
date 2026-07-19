package platform.kernels.planning.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import platform.kernels.planning.error.GoalPlanningException;
import platform.kernels.planning.error.PlanValidationException;
import platform.kernels.planning.error.PlanningError;
import platform.kernels.planning.error.PlanningErrorCode;
import platform.kernels.planning.error.PlanningException;
import platform.kernels.planning.error.PriorityException;
import platform.kernels.planning.error.SchedulingException;
import platform.kernels.planning.error.TaskPlanningException;
import platform.kernels.planning.engine.PlanningProcessingEngine;
import platform.kernels.planning.model.Goal;
import platform.kernels.planning.model.PlanningObjective;
import platform.kernels.planning.model.Priority;
import platform.kernels.planning.model.Schedule;
import platform.kernels.planning.model.Task;
import platform.kernels.planning.model.ValidationCriteria;
import platform.kernels.planning.validation.PlanningValidationResult;
import platform.kernels.planning.validation.PlanningValidator;

/**
 * <b>DefaultPlanningService</b>
 *
 * <p>Default orchestration service for the Planning Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates validation of planning domain models.</li>
 *   <li>Delegates processing to the planning processing engine.</li>
 *   <li>Translates failures into the PlanningException hierarchy.</li>
 *   <li>Returns processing results to the public API.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Service Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This class is stateless, thread-safe, deterministic, and read-only.
 * It maintains no mutable instance state and performs no planning computation.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-105, EIO-ARCH-001</p>
 *
 * <p><b>Orchestration Flow:</b></p>
 * <pre>
 * API Request
 *       │
 *       ▼
 * Validation
 *       │
 *       ▼
 * Processing Engine
 *       │
 *       ▼
 * Exception Translation
 *       │
 *       ▼
 * Response
 * </pre>
 *
 * @since 1.0
 */
public final class DefaultPlanningService {

    private final PlanningValidator validator;
    private final PlanningProcessingEngine processingEngine;

    /**
     * Creates a new {@code DefaultPlanningService} with the specified dependencies.
     *
     * <p>Uses constructor injection exclusively. All dependencies are immutable
     * and validated during construction.</p>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>PlanningValidator - for structural validation</li>
     *   <li>PlanningProcessingEngine - for planning processing delegation</li>
     * </ul>
     *
     * @param validator the planning validator (must not be {@code null})
     * @param processingEngine the planning processing engine (must not be {@code null})
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public DefaultPlanningService(
            PlanningValidator validator,
            PlanningProcessingEngine processingEngine) {
        Objects.requireNonNull(validator, "DefaultPlanningService validator must not be null");
        Objects.requireNonNull(processingEngine, "DefaultPlanningService processingEngine must not be null");

        this.validator = validator;
        this.processingEngine = processingEngine;
    }

    /**
     * Processes a goal planning request through the orchestration pipeline.
     *
     * <p>Follows the standard delegation flow:</p>
     * <ol>
     *   <li>Validate the planning objective</li>
     *   <li>Delegate to processing engine if valid</li>
     *   <li>Translate any exceptions to GoalPlanningException</li>
     *   <li>Return the processing result</li>
     * </ol>
     *
     * <p><b>Service Responsibilities:</b></p>
     * <ul>
     *   <li>Coordinate validation</li>
     *   <li>Delegate processing</li>
     *   <li>Translate exceptions</li>
     *   <li>Coordinate responses</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not decompose goals</li>
     *   <li>Does not evaluate goal achievement strategies</li>
     *   <li>Does not modify domain models</li>
     * </ul>
     *
     * @param objective the planning objective (must not be {@code null})
     * @return a list of processed {@link Goal} instances
     * @throws GoalPlanningException if goal planning processing fails
     * @throws PlanningException if validation fails
     */
    public List<Goal> processGoalPlanning(PlanningObjective objective) {
        Objects.requireNonNull(objective, "PlanningObjective must not be null");

        // Validate objective
        PlanningValidationResult validationResult = validator.validatePlanningObjective(objective);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult, PlanningErrorCode.GOAL_PLANNING_ERROR);
        }

        try {
            // Delegate to processing engine
            return processingEngine.processGoalPlanning(objective);
        } catch (PlanningException e) {
            // Re-throw PlanningException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception to GoalPlanningException
            throw new GoalPlanningException(createError(
                    PlanningErrorCode.GOAL_PLANNING_ERROR,
                    "Goal planning processing failed: " + e.getMessage(),
                    e
            ), e);
        }
    }

    /**
     * Processes a task planning request through the orchestration pipeline.
     *
     * <p>Follows the standard delegation flow:</p>
     * <ol>
     *   <li>Validate the goals</li>
     *   <li>Delegate to processing engine if valid</li>
     *   <li>Translate any exceptions to TaskPlanningException</li>
     *   <li>Return the processing result</li>
     * </ol>
     *
     * <p><b>Service Responsibilities:</b></p>
     * <ul>
     *   <li>Coordinate validation</li>
     *   <li>Delegate processing</li>
     *   <li>Translate exceptions</li>
     *   <li>Coordinate responses</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not generate or sequence tasks</li>
     *   <li>Does not evaluate task dependencies</li>
     *   <li>Does not modify domain models</li>
     * </ul>
     *
     * @param goals the list of goals to plan tasks for (must not be {@code null})
     * @return a list of processed {@link Task} instances
     * @throws TaskPlanningException if task planning processing fails
     * @throws PlanningException if validation fails
     */
    public List<Task> processTaskPlanning(List<Goal> goals) {
        Objects.requireNonNull(goals, "Goals must not be null");

        // Validate goals
        PlanningValidationResult validationResult = validator.validateGoal(goals.get(0));
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult, PlanningErrorCode.TASK_PLANNING_ERROR);
        }

        try {
            // Delegate to processing engine
            return processingEngine.processTaskPlanning(goals);
        } catch (PlanningException e) {
            // Re-throw PlanningException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception to TaskPlanningException
            throw new TaskPlanningException(createError(
                    PlanningErrorCode.TASK_PLANNING_ERROR,
                    "Task planning processing failed: " + e.getMessage(),
                    e
            ), e);
        }
    }

    /**
     * Processes a scheduling request through the orchestration pipeline.
     *
     * <p>Follows the standard delegation flow:</p>
     * <ol>
     *   <li>Validate the tasks</li>
     *   <li>Delegate to processing engine if valid</li>
     *   <li>Translate any exceptions to SchedulingException</li>
     *   <li>Return the processing result</li>
     * </ol>
     *
     * <p><b>Service Responsibilities:</b></p>
     * <ul>
     *   <li>Coordinate validation</li>
     *   <li>Delegate processing</li>
     *   <li>Translate exceptions</li>
     *   <li>Coordinate responses</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not optimize schedules</li>
     *   <li>Does not evaluate scheduling quality</li>
     *   <li>Does not modify domain models</li>
     * </ul>
     *
     * @param tasks the list of tasks to schedule (must not be {@code null})
     * @return a processed {@link Schedule} instance
     * @throws SchedulingException if scheduling processing fails
     * @throws PlanningException if validation fails
     */
    public Schedule processScheduling(List<Task> tasks) {
        Objects.requireNonNull(tasks, "Tasks must not be null");

        // Validate tasks
        PlanningValidationResult validationResult = validator.validateTask(tasks.get(0));
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult, PlanningErrorCode.SCHEDULING_ERROR);
        }

        try {
            // Delegate to processing engine
            return processingEngine.processScheduling(tasks);
        } catch (PlanningException e) {
            // Re-throw PlanningException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception to SchedulingException
            throw new SchedulingException(createError(
                    PlanningErrorCode.SCHEDULING_ERROR,
                    "Scheduling processing failed: " + e.getMessage(),
                    e
            ), e);
        }
    }

    /**
     * Processes a prioritization request through the orchestration pipeline.
     *
     * <p>Follows the standard delegation flow:</p>
     * <ol>
     *   <li>Validate the tasks</li>
     *   <li>Delegate to processing engine if valid</li>
     *   <li>Translate any exceptions to PriorityException</li>
     *   <li>Return the processing result</li>
     * </ol>
     *
     * <p><b>Service Responsibilities:</b></p>
     * <ul>
     *   <li>Coordinate validation</li>
     *   <li>Delegate processing</li>
     *   <li>Translate exceptions</li>
     *   <li>Coordinate responses</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not compute priorities</li>
     *   <li>Does not evaluate priority correctness</li>
     *   <li>Does not modify domain models</li>
     * </ul>
     *
     * @param tasks the list of tasks to prioritize (must not be {@code null})
     * @return a list of processed {@link Priority} instances
     * @throws PriorityException if prioritization processing fails
     * @throws PlanningException if validation fails
     */
    public List<Priority> processPrioritization(List<Task> tasks) {
        Objects.requireNonNull(tasks, "Tasks must not be null");

        // Validate tasks
        PlanningValidationResult validationResult = validator.validateTask(tasks.get(0));
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult, PlanningErrorCode.PRIORITIZATION_ERROR);
        }

        try {
            // Delegate to processing engine
            return processingEngine.processPrioritization(tasks);
        } catch (PlanningException e) {
            // Re-throw PlanningException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception to PriorityException
            throw new PriorityException(createError(
                    PlanningErrorCode.PRIORITIZATION_ERROR,
                    "Prioritization processing failed: " + e.getMessage(),
                    e
            ), e);
        }
    }

    /**
     * Processes a plan validation request through the orchestration pipeline.
     *
     * <p>Follows the standard delegation flow:</p>
     * <ol>
     *   <li>Validate the validation criteria</li>
     *   <li>Delegate to processing engine if valid</li>
     *   <li>Translate any exceptions to PlanValidationException</li>
     *   <li>Return the processing result</li>
     * </ol>
     *
     * <p><b>Service Responsibilities:</b></p>
     * <ul>
     *   <li>Coordinate validation</li>
     *   <li>Delegate processing</li>
     *   <li>Translate exceptions</li>
     *   <li>Coordinate responses</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate plans semantically</li>
     *   <li>Does not evaluate plan quality</li>
     *   <li>Does not modify domain models</li>
     * </ul>
     *
     * @param criteria the validation criteria (must not be {@code null})
     * @return a validation result
     * @throws PlanValidationException if plan validation processing fails
     * @throws PlanningException if validation fails
     */
    public Object processPlanValidation(ValidationCriteria criteria) {
        Objects.requireNonNull(criteria, "ValidationCriteria must not be null");

        // Validate criteria
        PlanningValidationResult validationResult = validator.validateValidationCriteria(criteria);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult, PlanningErrorCode.VALIDATION_ERROR);
        }

        try {
            // Delegate to processing engine
            return processingEngine.processPlanValidation(criteria);
        } catch (PlanningException e) {
            // Re-throw PlanningException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception to PlanValidationException
            throw new PlanValidationException(createError(
                    PlanningErrorCode.VALIDATION_ERROR,
                    "Plan validation processing failed: " + e.getMessage(),
                    e
            ), e);
        }
    }

    /**
     * Creates a PlanValidationException from validation failures.
     *
     * <p>Aggregates all validation violations into a single exception with
     * appropriate error classification.</p>
     *
     * @param validationResult the validation result
     * @param errorCode the error code
     * @return a PlanValidationException representing the validation failure
     */
    private PlanValidationException createValidationException(
            PlanningValidationResult validationResult,
            PlanningErrorCode errorCode) {
        StringBuilder message = new StringBuilder("Validation failed: ");
        message.append(validationResult.violations().size());
        message.append(" violation(s) found");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("violations", validationResult.violations());
        metadata.put("validator", "PlanningValidator");

        PlanningError error = new PlanningError(
                errorCode,
                message.toString(),
                Instant.now(),
                metadata
        );

        return new PlanValidationException(error);
    }

    /**
     * Creates a PlanningError with the specified parameters.
     *
     * @param errorCode the error code
     * @param message the error message
     * @param cause the original cause (may be {@code null})
     * @return a PlanningError instance
     */
    private PlanningError createError(PlanningErrorCode errorCode, String message, Throwable cause) {
        Map<String, Object> metadata = new HashMap<>();
        if (cause != null) {
            metadata.put("causeType", cause.getClass().getName());
            metadata.put("causeMessage", cause.getMessage());
        }

        return new PlanningError(
                errorCode,
                message,
                Instant.now(),
                metadata
        );
    }
}