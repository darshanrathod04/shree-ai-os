package platform.kernels.execution.service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import platform.kernels.execution.api.ExecutionService;
import platform.kernels.execution.engine.ExecutionProcessingEngine;
import platform.kernels.execution.engine.ExecutionProcessingResult;
import platform.kernels.execution.error.ActionExecutionException;
import platform.kernels.execution.error.ExecutionError;
import platform.kernels.execution.error.ExecutionErrorCode;
import platform.kernels.execution.error.ExecutionException;
import platform.kernels.execution.error.ExecutionValidationException;
import platform.kernels.execution.error.RecoveryException;
import platform.kernels.execution.error.TaskExecutionException;
import platform.kernels.execution.error.WorkflowExecutionException;
import platform.kernels.execution.model.ExecutionRequest;
import platform.kernels.execution.model.ExecutionResult;
import platform.kernels.execution.model.ExecutionStatus;
import platform.kernels.execution.model.RecoveryStrategy;
import platform.kernels.execution.validation.ExecutionValidationResult;
import platform.kernels.execution.validation.ExecutionValidator;

/**
 * <b>DefaultExecutionService</b>
 *
 * <p>Canonical service implementation for the Execution Kernel.
 * This class orchestrates execution requests by delegating to the Validation
 * Layer and Processing Engine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Implements ExecutionService contract.</li>
 *   <li>Orchestrates execution requests.</li>
 *   <li>Delegates validation to ExecutionValidator.</li>
 *   <li>Delegates processing to ExecutionProcessingEngine.</li>
 *   <li>Translates failures into canonical ExecutionException hierarchy.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields.</li>
 *   <li>Thread-safe — immutable dependencies.</li>
 *   <li>Constructor injection — all dependencies provided through constructor.</li>
 *   <li>Delegation — orchestrates, never executes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Service Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-105, EIO-ARCH-001</p>
 *
 * @param validator          the execution validator (must not be {@code null})
 * @param processingEngine   the processing engine (must not be {@code null})
 *
 * @since 1.0
 */
public final class DefaultExecutionService implements ExecutionService {

    private final ExecutionValidator validator;
    private final ExecutionProcessingEngine processingEngine;

    /**
     * Constructs a {@code DefaultExecutionService} with the specified dependencies.
     *
     * <p>All dependencies are injected through the constructor and are immutable
     * after construction.</p>
     *
     * @param validator        the execution validator (must not be {@code null})
     * @param processingEngine the processing engine (must not be {@code null})
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public DefaultExecutionService(
            ExecutionValidator validator,
            ExecutionProcessingEngine processingEngine) {
        this.validator = Objects.requireNonNull(validator, "DefaultExecutionService validator must not be null");
        this.processingEngine = Objects.requireNonNull(processingEngine, "DefaultExecutionService processingEngine must not be null");
    }

    /**
     * Executes an action based on the provided execution request.
     *
     * <p>This method orchestrates action execution by:</p>
     * <ol>
     *   <li>Validating the execution request</li>
     *   <li>Delegating to the processing engine</li>
     *   <li>Translating any failures into canonical exceptions</li>
     * </ol>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return an execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     * @throws ExecutionValidationException if validation fails
     * @throws ActionExecutionException if action execution fails
     */
    @Override
    public String executeAction(ExecutionRequest executionRequest) {
        Objects.requireNonNull(executionRequest, "executeAction executionRequest must not be null");

        // Validate the request
        ExecutionValidationResult validationResult = validator.validate(executionRequest);
        if (!validationResult.valid()) {
            throw createValidationException(validationResult);
        }

        try {
            // Delegate to processing engine
            ExecutionProcessingResult processingResult = processingEngine.processActionExecution(executionRequest);
            ExecutionResult result = processingResult.executionResult();
            return result.executionId().value();
        } catch (ExecutionException e) {
            // Re-throw ExecutionException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception into ActionExecutionException
            throw createActionExecutionException(e);
        }
    }

    /**
     * Executes a workflow based on the provided execution request.
     *
     * <p>This method orchestrates workflow execution by:</p>
     * <ol>
     *   <li>Validating the execution request</li>
     *   <li>Delegating to the processing engine</li>
     *   <li>Translating any failures into canonical exceptions</li>
     * </ol>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a workflow execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     * @throws ExecutionValidationException if validation fails
     * @throws WorkflowExecutionException if workflow execution fails
     */
    @Override
    public String executeWorkflow(ExecutionRequest executionRequest) {
        Objects.requireNonNull(executionRequest, "executeWorkflow executionRequest must not be null");

        // Validate the request
        ExecutionValidationResult validationResult = validator.validate(executionRequest);
        if (!validationResult.valid()) {
            throw createValidationException(validationResult);
        }

        try {
            // Delegate to processing engine
            ExecutionProcessingResult processingResult = processingEngine.processWorkflowExecution(executionRequest);
            ExecutionResult result = processingResult.executionResult();
            return result.executionId().value();
        } catch (ExecutionException e) {
            // Re-throw ExecutionException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception into WorkflowExecutionException
            throw createWorkflowExecutionException(e);
        }
    }

    /**
     * Executes a planned task based on the provided execution request.
     *
     * <p>This method orchestrates task execution by:</p>
     * <ol>
     *   <li>Validating the execution request</li>
     *   <li>Delegating to the processing engine</li>
     *   <li>Translating any failures into canonical exceptions</li>
     * </ol>
     *
     * @param executionRequest the execution request parameters (must not be {@code null})
     * @return a task execution identifier
     * @throws IllegalArgumentException if executionRequest is {@code null}
     * @throws ExecutionValidationException if validation fails
     * @throws TaskExecutionException if task execution fails
     */
    @Override
    public String executeTask(ExecutionRequest executionRequest) {
        Objects.requireNonNull(executionRequest, "executeTask executionRequest must not be null");

        // Validate the request
        ExecutionValidationResult validationResult = validator.validate(executionRequest);
        if (!validationResult.valid()) {
            throw createValidationException(validationResult);
        }

        try {
            // Delegate to processing engine
            ExecutionProcessingResult processingResult = processingEngine.processTaskExecution(executionRequest);
            ExecutionResult result = processingResult.executionResult();
            return result.executionId().value();
        } catch (ExecutionException e) {
            // Re-throw ExecutionException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception into TaskExecutionException
            throw createTaskExecutionException(e);
        }
    }

    /**
     * Retrieves the current execution status for a given execution identifier.
     *
     * <p>This method orchestrates status retrieval by delegating to the
     * processing engine.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return the current execution status
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    @Override
    public ExecutionStatus getExecutionStatus(String executionId) {
        if (executionId == null || executionId.trim().isEmpty()) {
            throw new IllegalArgumentException("getExecutionStatus executionId must not be null or empty");
        }

        try {
            // Delegate to processing engine
            return processingEngine.processExecutionMonitoring(executionId);
        } catch (ExecutionException e) {
            // Re-throw ExecutionException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception into ExecutionException
            throw createExecutionException(e);
        }
    }

    /**
     * Cancels an ongoing execution.
     *
     * <p>This method orchestrates cancellation by delegating to the
     * processing engine.</p>
     *
     * @param executionId the execution identifier (must not be {@code null} or empty)
     * @return {@code true} if cancellation was requested successfully
     * @throws IllegalArgumentException if executionId is {@code null} or empty
     */
    @Override
    public boolean cancelExecution(String executionId) {
        if (executionId == null || executionId.trim().isEmpty()) {
            throw new IllegalArgumentException("cancelExecution executionId must not be null or empty");
        }

        // Note: Cancellation is not part of the ExecutionProcessingEngine interface
        // This will be implemented in EXEC-106 or handled by the Service layer
        // For now, return false as a placeholder
        return false;
    }

    /**
     * Initiates recovery for a failed execution.
     *
     * <p>This method orchestrates recovery by:</p>
     * <ol>
     *   <li>Validating the execution identifier</li>
     *   <li>Delegating to the processing engine</li>
     *   <li>Translating any failures into canonical exceptions</li>
     * </ol>
     *
     * @param executionId     the execution identifier (must not be {@code null} or empty)
     * @param recoveryStrategy the recovery strategy to apply (must not be {@code null})
     * @return a recovery execution identifier
     * @throws IllegalArgumentException if executionId or recoveryStrategy is {@code null}
     * @throws ExecutionValidationException if validation fails
     * @throws RecoveryException if recovery fails
     */
    @Override
    public String recoverExecution(String executionId, RecoveryStrategy recoveryStrategy) {
        if (executionId == null || executionId.trim().isEmpty()) {
            throw new IllegalArgumentException("recoverExecution executionId must not be null or empty");
        }
        if (recoveryStrategy == null) {
            throw new IllegalArgumentException("recoverExecution recoveryStrategy must not be null");
        }

        try {
            // Delegate to processing engine
            ExecutionProcessingResult processingResult = processingEngine.processExecutionRecovery(executionId, recoveryStrategy);
            ExecutionResult result = processingResult.executionResult();
            return result.executionId().value();
        } catch (ExecutionException e) {
            // Re-throw ExecutionException as-is
            throw e;
        } catch (Exception e) {
            // Translate any other exception into RecoveryException
            throw createRecoveryException(e);
        }
    }

    /**
     * Creates an ExecutionValidationException from a validation result.
     *
     * @param validationResult the validation result (must not be {@code null})
     * @return the execution validation exception
     */
    private ExecutionValidationException createValidationException(ExecutionValidationResult validationResult) {
        String message = "Validation failed: " + String.join(", ", validationResult.violations());
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.VALIDATION_FAILURE,
                message,
                validationResult.validatedAt(),
                validationResult.metadata()
        );
        return new ExecutionValidationException(error);
    }

    /**
     * Creates an ActionExecutionException from a generic exception.
     *
     * @param cause the cause (must not be {@code null})
     * @return the action execution exception
     */
    private ActionExecutionException createActionExecutionException(Exception cause) {
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.ACTION_EXECUTION_FAILED,
                "Action execution failed: " + cause.getMessage(),
                Instant.now(),
                Map.of("cause", cause.getClass().getName())
        );
        return new ActionExecutionException(error, cause);
    }

    /**
     * Creates a WorkflowExecutionException from a generic exception.
     *
     * @param cause the cause (must not be {@code null})
     * @return the workflow execution exception
     */
    private WorkflowExecutionException createWorkflowExecutionException(Exception cause) {
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.WORKFLOW_EXECUTION_FAILED,
                "Workflow execution failed: " + cause.getMessage(),
                Instant.now(),
                Map.of("cause", cause.getClass().getName())
        );
        return new WorkflowExecutionException(error, cause);
    }

    /**
     * Creates a TaskExecutionException from a generic exception.
     *
     * @param cause the cause (must not be {@code null})
     * @return the task execution exception
     */
    private TaskExecutionException createTaskExecutionException(Exception cause) {
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.TASK_EXECUTION_FAILED,
                "Task execution failed: " + cause.getMessage(),
                Instant.now(),
                Map.of("cause", cause.getClass().getName())
        );
        return new TaskExecutionException(error, cause);
    }

    /**
     * Creates a RecoveryException from a generic exception.
     *
     * @param cause the cause (must not be {@code null})
     * @return the recovery exception
     */
    private RecoveryException createRecoveryException(Exception cause) {
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.RECOVERY_FAILED,
                "Recovery failed: " + cause.getMessage(),
                Instant.now(),
                Map.of("cause", cause.getClass().getName())
        );
        return new RecoveryException(error, cause);
    }

    /**
     * Creates an ExecutionException from a generic exception.
     *
     * @param cause the cause (must not be {@code null})
     * @return the execution exception
     */
    private ExecutionException createExecutionException(Exception cause) {
        ExecutionError error = new ExecutionError(
                ExecutionErrorCode.EXECUTION_FAILURE,
                "Execution failed: " + cause.getMessage(),
                Instant.now(),
                Map.of("cause", cause.getClass().getName())
        );
        return new ExecutionException(error, cause);
    }
}