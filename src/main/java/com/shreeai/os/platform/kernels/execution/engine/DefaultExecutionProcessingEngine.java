package com.shreeai.os.platform.kernels.execution.engine;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.execution.model.*;

/**
 * <b>DefaultExecutionProcessingEngine</b>
 *
 * <p>Canonical processing engine implementation for the Execution Kernel.
 * This class performs deterministic execution computation on validated Execution
 * domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Performs deterministic execution computation.</li>
 *   <li>Transforms validated Execution domain models.</li>
 *   <li>Constructs immutable processing results.</li>
 *   <li>Attaches execution and outcome-verification intelligence.</li>
 *   <li>Contains no orchestration, validation, or exception translation logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable state.</li>
 *   <li>Thread-safe — no shared mutable state.</li>
 *   <li>Deterministic execution transformation.</li>
 *   <li>Outcome verification is explicitly separated from execution completion.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Engine Layer</p>
 * <p><b>Version:</b> 2.0</p>
 *
 * @since 1.0
 */
public final class DefaultExecutionProcessingEngine
        implements ExecutionProcessingEngine {

    /**
     * Public constructor for service-layer instantiation.
     */
    public DefaultExecutionProcessingEngine() {
        // Stateless engine.
    }

    /**
     * Processes action execution deterministically.
     */
    @Override
    public ExecutionProcessingResult processActionExecution(
            ExecutionRequest executionRequest) {

        Objects.requireNonNull(
                executionRequest,
                "processActionExecution executionRequest must not be null"
        );

        Instant processedAt = Instant.now();
        Map<String, Object> metadata = new java.util.HashMap<>();

        addExecutionIntelligence(
                executionRequest,
                "ACTION",
                metadata
        );

        ExecutionResult executionResult =
                createExecutionResult(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ActionState actionState =
                createActionState(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionMetrics executionMetrics =
                createExecutionMetrics(
                        executionRequest,
                        processedAt,
                        metadata
                );

        WorkflowState workflowState =
                createWorkflowState(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionSnapshot executionSnapshot =
                createExecutionSnapshot(
                        executionRequest,
                        executionResult,
                        workflowState,
                        actionState,
                        processedAt,
                        metadata
                );

        metadata.put(
                "processingType",
                "ACTION_EXECUTION"
        );

        metadata.put(
                "actionId",
                executionRequest.actionId()
        );

        /*
         * Semantic verification is performed after the execution result
         * exists. COMPLETED does not automatically mean VERIFIED_SUCCESS.
         */
        addOutcomeVerification(
                executionRequest,
                executionResult,
                metadata
        );

        return new ExecutionProcessingResult(
                true,
                processedAt,
                metadata,
                executionRequest,
                executionResult,
                ExecutionStatus.COMPLETED,
                executionMetrics,
                null,
                actionState,
                executionSnapshot
        );
    }

    /**
     * Processes workflow execution deterministically.
     */
    @Override
    public ExecutionProcessingResult processWorkflowExecution(
            ExecutionRequest executionRequest) {

        Objects.requireNonNull(
                executionRequest,
                "processWorkflowExecution executionRequest must not be null"
        );

        Instant processedAt = Instant.now();
        Map<String, Object> metadata = new java.util.HashMap<>();

        addExecutionIntelligence(
                executionRequest,
                "WORKFLOW",
                metadata
        );

        ExecutionResult executionResult =
                createExecutionResult(
                        executionRequest,
                        processedAt,
                        metadata
                );

        WorkflowState workflowState =
                createWorkflowState(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionMetrics executionMetrics =
                createExecutionMetrics(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionSnapshot executionSnapshot =
                createExecutionSnapshot(
                        executionRequest,
                        executionResult,
                        workflowState,
                        null,
                        processedAt,
                        metadata
                );

        metadata.put(
                "processingType",
                "WORKFLOW_EXECUTION"
        );

        metadata.put(
                "workflowId",
                executionRequest.context().planId()
        );

        addOutcomeVerification(
                executionRequest,
                executionResult,
                metadata
        );

        return new ExecutionProcessingResult(
                true,
                processedAt,
                metadata,
                executionRequest,
                executionResult,
                ExecutionStatus.COMPLETED,
                executionMetrics,
                workflowState,
                null,
                executionSnapshot
        );
    }

    /**
     * Processes task execution deterministically.
     */
    @Override
    public ExecutionProcessingResult processTaskExecution(
            ExecutionRequest executionRequest) {

        Objects.requireNonNull(
                executionRequest,
                "processTaskExecution executionRequest must not be null"
        );

        Instant processedAt = Instant.now();
        Map<String, Object> metadata = new java.util.HashMap<>();

        addExecutionIntelligence(
                executionRequest,
                "TASK",
                metadata
        );

        ExecutionResult executionResult =
                createExecutionResult(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ActionState taskState =
                createTaskState(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionMetrics executionMetrics =
                createExecutionMetrics(
                        executionRequest,
                        processedAt,
                        metadata
                );

        WorkflowState workflowState =
                createWorkflowState(
                        executionRequest,
                        processedAt,
                        metadata
                );

        ExecutionSnapshot executionSnapshot =
                createExecutionSnapshot(
                        executionRequest,
                        executionResult,
                        workflowState,
                        taskState,
                        processedAt,
                        metadata
                );

        metadata.put(
                "processingType",
                "TASK_EXECUTION"
        );

        metadata.put(
                "taskId",
                executionRequest.actionId()
        );

        addOutcomeVerification(
                executionRequest,
                executionResult,
                metadata
        );

        return new ExecutionProcessingResult(
                true,
                processedAt,
                metadata,
                executionRequest,
                executionResult,
                ExecutionStatus.COMPLETED,
                executionMetrics,
                null,
                taskState,
                executionSnapshot
        );
    }

    /**
     * Processes execution monitoring deterministically.
     */
    @Override
    public ExecutionStatus processExecutionMonitoring(
            String executionId) {

        if (executionId == null
                || executionId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "processExecutionMonitoring executionId "
                            + "must not be null or empty"
            );
        }

        return ExecutionStatus.COMPLETED;
    }

    /**
     * Processes execution recovery deterministically.
     */
    @Override
    public ExecutionProcessingResult processExecutionRecovery(
            String executionId,
            RecoveryStrategy recoveryStrategy) {

        if (executionId == null
                || executionId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "processExecutionRecovery executionId "
                            + "must not be null or empty"
            );
        }

        if (recoveryStrategy == null) {
            throw new IllegalArgumentException(
                    "processExecutionRecovery recoveryStrategy "
                            + "must not be null"
            );
        }

        Instant processedAt = Instant.now();
        Map<String, Object> metadata =
                new java.util.HashMap<>();

        ExecutionIntelligenceEngine.RecoveryAnalysis
                recoveryAnalysis =
                new ExecutionIntelligenceEngine()
                        .analyzeRecovery(
                                executionId,
                                recoveryStrategy
                        );

        metadata.put(
                "executionIntelligence",
                recoveryAnalysis.metadata()
        );

        metadata.put(
                "recoveryConfidence",
                recoveryAnalysis.confidence()
        );

        metadata.put(
                "recoveryRecommendations",
                recoveryAnalysis.recommendations()
        );

        metadata.put(
                "processingType",
                "EXECUTION_RECOVERY"
        );

        metadata.put(
                "executionId",
                executionId
        );

        metadata.put(
                "recoveryStrategy",
                recoveryStrategy.name()
        );

        /*
         * Recovery uses the existing execution model.
         * No new recovery-specific execution contract is introduced.
         */
        ExecutionRequest recoveryRequest =
                new ExecutionRequest(
                        new ExecutionId(executionId),
                        "recovery-" + executionId,
                        new ExecutionContext(
                                new ExecutionId(executionId),
                                "recovery-plan",
                                "recovery-objective",
                                new java.util.HashMap<>(),
                                0
                        ),
                        new ExecutionOptions(
                                0,
                                0,
                                0,
                                false,
                                false,
                                new java.util.HashMap<>()
                        ),
                        new java.util.HashMap<>()
                );

        ExecutionResult executionResult =
                createExecutionResult(
                        recoveryRequest,
                        processedAt,
                        metadata
                );

        ExecutionMetrics executionMetrics =
                createExecutionMetrics(
                        recoveryRequest,
                        processedAt,
                        metadata
                );

        WorkflowState workflowState =
                createWorkflowState(
                        recoveryRequest,
                        processedAt,
                        metadata
                );

        ExecutionSnapshot executionSnapshot =
                createExecutionSnapshot(
                        recoveryRequest,
                        executionResult,
                        workflowState,
                        null,
                        processedAt,
                        metadata
                );

        addOutcomeVerification(
                recoveryRequest,
                executionResult,
                metadata
        );

        return new ExecutionProcessingResult(
                true,
                processedAt,
                metadata,
                recoveryRequest,
                executionResult,
                ExecutionStatus.COMPLETED,
                executionMetrics,
                null,
                null,
                executionSnapshot
        );
    }

    /**
     * Adds execution intelligence to processing metadata.
     */
    private static void addExecutionIntelligence(
            ExecutionRequest executionRequest,
            String executionType,
            Map<String, Object> metadata) {

        ExecutionIntelligenceEngine.ExecutionIntelligenceAnalysis
                intelligence =
                new ExecutionIntelligenceEngine()
                        .analyze(
                                executionRequest,
                                executionType
                        );

        metadata.put(
                "executionIntelligence",
                intelligence.metadata()
        );

        metadata.put(
                "executionReady",
                intelligence.executionReady()
        );

        metadata.put(
                "executionConfidence",
                intelligence.confidence()
        );
    }

    /**
     * Performs semantic outcome verification.
     *
     * <p>This method deliberately keeps outcome verification inside the
     * Execution Kernel while exposing the complete structured assessment
     * through the existing processing metadata contract.</p>
     */
    private static void addOutcomeVerification(
            ExecutionRequest executionRequest,
            ExecutionResult executionResult,
            Map<String, Object> metadata) {

        OutcomeVerificationIntelligenceEngine
                verificationEngine =
                new OutcomeVerificationIntelligenceEngine();

        OutcomeVerificationIntelligenceEngine.OutcomeAssessment
                assessment =
                verificationEngine.verify(
                        executionRequest,
                        executionResult
                );

        /*
         * Preserve the complete assessment for future intelligence layers.
         */
        metadata.put(
                "outcomeVerification",
                assessment
        );

        /*
         * Also expose stable scalar/list projections so downstream
         * services do not need to understand the OutcomeAssessment class.
         */
        metadata.put(
                "outcomeVerificationState",
                assessment.state().name()
        );

        metadata.put(
                "outcomeVerified",
                assessment.verified()
        );

        metadata.put(
                "outcomeVerificationConfidence",
                assessment.confidence()
        );

        metadata.put(
                "outcomeVerificationConfidenceBand",
                assessment.confidenceBand()
        );

        metadata.put(
                "outcomeVerificationVerdict",
                assessment.verdict()
        );

        metadata.put(
                "outcomeEvidence",
                assessment.evidence()
        );

        metadata.put(
                "outcomeFindings",
                assessment.findings()
        );

        metadata.put(
                "outcomeRisks",
                assessment.risks()
        );

        metadata.put(
                "outcomeMissingInformation",
                assessment.missingInformation()
        );

        metadata.put(
                "outcomeRecommendations",
                assessment.recommendations()
        );

        metadata.put(
                "outcomeVerificationPerformed",
                true
        );
    }

    /**
     * Creates an execution result from an execution request.
     */
    private static ExecutionResult createExecutionResult(
            ExecutionRequest executionRequest,
            Instant processedAt,
            Map<String, Object> metadata) {

        return new ExecutionResult(
                executionRequest.executionId(),
                ExecutionStatus.COMPLETED,
                new java.util.HashMap<>(),
                createExecutionMetrics(
                        executionRequest,
                        processedAt,
                        metadata
                ),
                processedAt
        );
    }

    /**
     * Creates execution metrics.
     */
    private static ExecutionMetrics createExecutionMetrics(
            ExecutionRequest executionRequest,
            Instant processedAt,
            Map<String, Object> metadata) {

        long durationMs = 0;

        return new ExecutionMetrics(
                processedAt,
                processedAt,
                durationMs,
                0,
                new java.util.HashMap<>()
        );
    }

    /**
     * Creates action state.
     */
    private static ActionState createActionState(
            ExecutionRequest executionRequest,
            Instant processedAt,
            Map<String, Object> metadata) {

        return new ActionState(
                executionRequest.actionId(),
                ExecutionStatus.COMPLETED,
                new java.util.HashMap<>(),
                new java.util.HashMap<>()
        );
    }

    /**
     * Creates task state.
     */
    private static ActionState createTaskState(
            ExecutionRequest executionRequest,
            Instant processedAt,
            Map<String, Object> metadata) {

        return new ActionState(
                executionRequest.actionId(),
                ExecutionStatus.COMPLETED,
                new java.util.HashMap<>(),
                new java.util.HashMap<>()
        );
    }

    /**
     * Creates workflow state.
     */
    private static WorkflowState createWorkflowState(
            ExecutionRequest executionRequest,
            Instant processedAt,
            Map<String, Object> metadata) {

        return new WorkflowState(
                executionRequest.context().planId(),
                "COMPLETED",
                new java.util.ArrayList<>(),
                new java.util.HashMap<>()
        );
    }

    /**
     * Creates an immutable execution snapshot.
     */
    private static ExecutionSnapshot createExecutionSnapshot(
            ExecutionRequest executionRequest,
            ExecutionResult executionResult,
            WorkflowState workflowState,
            ActionState actionState,
            Instant processedAt,
            Map<String, Object> metadata) {

        Map<String, ActionState> actionStates =
                new java.util.HashMap<>();

        if (actionState != null) {
            actionStates.put(
                    actionState.actionId(),
                    actionState
            );
        }

        return new ExecutionSnapshot(
                executionRequest,
                executionResult,
                workflowState,
                actionStates,
                processedAt,
                metadata
        );
    }
}