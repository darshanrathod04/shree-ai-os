package com.shreeai.os.platform.runtime.service;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.legacy.execution.ExecutionMetadata;
import com.shreeai.os.platform.runtime.AbstractRuntimeService;
import com.shreeai.os.platform.runtime.RuntimeState;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionContext;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ChiefReviewStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ActionExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.stages.IdentityStage;
import com.shreeai.os.platform.runtime.pipeline.stages.InferenceStage;
import com.shreeai.os.platform.runtime.pipeline.stages.KnowledgeStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryRecallStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryStoreStage;
import com.shreeai.os.platform.runtime.pipeline.stages.PlanningStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ReasoningStage;
import com.shreeai.os.platform.kernels.memory.api.MemoryQueryService;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.factory.KernelFactory;
import com.shreeai.os.platform.kernels.factory.DefaultKernelFactory;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.execution.api.ExecutionService;
import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultRuntimeService</b>
 *
 * <p>Default implementation of the Runtime that extends {@link AbstractRuntimeService}
 * and implements the {@link Runtime} contract.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the concrete Runtime implementation for bootstrap integration.</li>
 *   <li>Extends AbstractRuntimeService for lifecycle state management.</li>
 *   <li>Implements the Runtime API contract.</li>
 *   <li>Delegates execution to the canonical ExecutionPipeline.</li>
 *   <li>Coordinates lifecycle only — no platform business logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Service</p>
 * <p><b>Lifecycle:</b> CREATED → INITIALIZED → STARTED → VERIFIED → STOPPED</p>
 */
public final class DefaultRuntimeService extends AbstractRuntimeService implements Runtime {


    private final RuntimeConfiguration configuration;
    private final RuntimeContract contract;
    private final List<ExecutionStage> stages;
    private RuntimeLifecycle lifecycle;
    private com.shreeai.os.platform.runtime.pipeline.ExecutionPipeline pipeline;
    private final KernelFactory kernelFactory;

    /**
     * Constructs a new DefaultRuntimeService with the given configuration and contract.
     *
     * @param configuration the runtime configuration (must not be null)
     * @param contract      the runtime contract (must not be null)
     */
    public DefaultRuntimeService(RuntimeConfiguration configuration, RuntimeContract contract) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");
        this.stages = new ArrayList<>();

        this.kernelFactory = new DefaultKernelFactory();

        // Initialize with real kernel execution stages
        initializeStages();
    }

    /**
     * Constructs a new DefaultRuntimeService with the given configuration, contract, and stages.
     *
     * @param configuration the runtime configuration (must not be null)
     * @param contract      the runtime contract (must not be null)
     * @param stages        the execution pipeline stages (must not be null)
     */
    public DefaultRuntimeService(
            RuntimeConfiguration configuration,
            RuntimeContract contract,
            List<ExecutionStage> stages
    ) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");
        this.stages = new ArrayList<>(Objects.requireNonNull(stages));

        this.kernelFactory = new DefaultKernelFactory();
    }
    
    /**
     * Initialize the real kernel execution pipeline stages.
     *
     * <p>This method builds the 10-stage kernel execution pipeline:</p>
     * <ol>
     *   <li>IdentityStage - Resolves agent identity</li>
     *   <li>ContextStage - Builds execution context</li>
     *   <li>MemoryRecallStage - Recalls relevant memories</li>
     *   <li>KnowledgeStage - Retrieves knowledge</li>
     *   <li>ReasoningStage - Performs cognitive reasoning</li>
     *   <li>InferenceStage - Generates hypotheses</li>
     *   <li>PlanningStage - Creates execution plan</li>
     *   <li>ExecutionStage - Executes planned actions</li>
     *   <li>MemoryStoreStage - Stores results in memory</li>
     *   <li>ChiefReviewStage - Final review and approval</li>
     * </ol>
     */
    private void initializeStages() {

        // ==========================================================
        // Memory Kernel
        // ==========================================================

        MemoryValidator memoryValidator = new MemoryValidator();
        DefaultMemoryProcessingEngine memoryProcessingEngine =
                new DefaultMemoryProcessingEngine();

        DefaultMemoryService memoryService =
                new DefaultMemoryService(
                        memoryValidator,
                        memoryProcessingEngine
                );

        MemoryQueryService memoryQueryService = memoryService;
        MemorySearchService memorySearchService = memoryService;
        MemoryRankingService memoryRankingService =
                new MemoryRankingService();

        // ==========================================================
        // Knowledge Kernel
        // ==========================================================

        DefaultKnowledgeProcessingEngine knowledgeEngine =
                new DefaultKnowledgeProcessingEngine();

        DefaultKnowledgeService knowledgeService =
                new DefaultKnowledgeService(knowledgeEngine);

        KnowledgeQueryService knowledgeQueryService = knowledgeService;
        KnowledgeSearchService knowledgeSearchService = knowledgeService;
        KnowledgeRankingService knowledgeRankingService =
                new KnowledgeRankingService();

        // ==========================================================
        // Cognitive Kernels
        // ==========================================================

        DefaultReasoningEngine reasoningEngine =
                new DefaultReasoningEngine();

        DefaultInferenceEngine inferenceEngine =
                new DefaultInferenceEngine();


        IdentityService identityService =
                kernelFactory.createIdentityService();
        // ==========================================================
        // Kernel Composition Root
        // Runtime NEVER constructs Planning/Execution/Chief directly.
        // ==========================================================

        PlanningService planningService =
                kernelFactory.createPlanningService();

        ExecutionService executionService =
                kernelFactory.createExecutionService();

        ChiefService chiefService =
                kernelFactory.createChiefService();

        // ==========================================================
        // Canonical 10-Stage Runtime Pipeline
        // ==========================================================

        stages.clear();

        stages.add(new IdentityStage(identityService));
        stages.add(new ContextStage());

        stages.add(
                new MemoryRecallStage(
                        memoryQueryService,
                        memorySearchService,
                        memoryRankingService
                )
        );

        stages.add(
                new KnowledgeStage(
                        knowledgeQueryService,
                        knowledgeSearchService,
                        knowledgeRankingService
                )
        );

        stages.add(new ReasoningStage(reasoningEngine));
        stages.add(new InferenceStage(inferenceEngine));
        stages.add(new PlanningStage(planningService));
        stages.add(new ActionExecutionStage(executionService));
        stages.add(new MemoryStoreStage(memoryService));
        stages.add(new ChiefReviewStage(chiefService));
    }

    @Override
    public void initialize() {
        super.initialize();
        // Create the canonical execution pipeline
        this.pipeline = new DefaultExecutionPipeline(stages);
        // Create lifecycle (uses DefaultRuntime lifecycle)
        com.shreeai.os.platform.runtime.lifecycle.RuntimeState runtimeState = 
            com.shreeai.os.platform.runtime.lifecycle.RuntimeState.INITIALIZING;
        this.lifecycle = new com.shreeai.os.platform.runtime.internal.DefaultRuntimeLifecycle();
    }

    @Override
    public void start() {
        super.start();
        if (lifecycle != null) {
            lifecycle.start();
        }
    }

    @Override
    public void verify() {
        super.verify();
    }

    @Override
    public void shutdown() {
        if (lifecycle != null) {
            try {
                lifecycle.stop();
            } catch (Exception e) {
                lifecycle.shutdown();
            }
        }
        super.shutdown();
    }

    // ========================================================================
    // Runtime API Implementation
    // ========================================================================

    @Override
    public RuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public RuntimeLifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public RuntimeContract contract() {
        return contract;
    }

    @Override
    public com.shreeai.os.platform.runtime.execution.ExecutionPipeline pipeline() {
        return (com.shreeai.os.platform.runtime.execution.ExecutionPipeline) pipeline;
    }

    @Override
    public com.shreeai.os.platform.runtime.execution.ExecutionSession submit(com.shreeai.os.platform.runtime.execution.ExecutionRequest request) {
        if (lifecycle == null || !lifecycle.isAcceptingRequests()) {
            throw new IllegalStateException(
                    "Runtime is not accepting requests. State: " + getState());
        }

        if (request == null) {
            throw new IllegalArgumentException("ExecutionRequest must not be null");
        }

        try {
            // Create execution session
            com.shreeai.os.platform.runtime.execution.ExecutionSession session = 
                com.shreeai.os.platform.runtime.execution.ExecutionSession.builder()
                    .requestId(request.requestId())
                    .status(com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.ACTIVE)
                    .build();

            // Create execution context
            ExecutionContext context = ExecutionContext.builder()
                    .session(session)
                    .configuration(configuration)
                    .contract(contract)
                    .build();

            // Execute the canonical pipeline exactly once
            com.shreeai.os.platform.runtime.pipeline.PipelineResult pipelineResult = null;
            if (pipeline != null) {
                // Convert runtime.execution.ExecutionRequest to execution.ExecutionRequest for PipelineContext,
                // preserving context and metadata so downstream stages receive the full payload
                ExecutionMetadata pipelineMetadata =
                    ExecutionMetadata.builder()
                        .executionSource("SDK")
                        .sessionId(request.requestId())
                        .customValues(request.metadata())
                        .build();

                com.shreeai.os.platform.legacy.execution.ExecutionRequest pipelineRequest =
                    com.shreeai.os.platform.legacy.execution.ExecutionRequest.builder()
                        .requestId(request.requestId())
                        .decisionId("sdk-chat-decision")
                        .capabilityName("CHAT")
                        .intent("CHAT_REQUEST")
                        .userInput(request.payload())
                        .metadata(pipelineMetadata)
                        .build();
                
                // Store ExecutionContext in pipeline context attributes for stage access
                com.shreeai.os.platform.runtime.pipeline.PipelineContext pipelineContext = 
                    com.shreeai.os.platform.runtime.pipeline.PipelineContext.builder()
                        .executionRequest(pipelineRequest)
                        .addAttribute("executionContext", context)
                        .addAttribute("executionSession", session)
                        .addAttribute("requestContext", request.context())
                        .addAttribute("requestMetadata", request.metadata())
                        .build();
                
                // Execute the canonical pipeline via the pipeline contract
                pipelineResult = pipeline.execute(pipelineContext);
            }

            // Convert PipelineResult to ExecutionResult, preserving any structured
            // intelligence context supplied with the request so rich data survives
            // the runtime → SDK boundary without being flattened into strings.
            com.shreeai.os.platform.runtime.execution.ExecutionResult result;
            if (pipelineResult != null && pipelineResult.isSuccess()) {
                String output = pipelineResult.getMessages().isEmpty() 
                        ? "Pipeline completed successfully" 
                        : String.join("; ", pipelineResult.getMessages());
                result = com.shreeai.os.platform.runtime.execution.ExecutionResult.builder()
                        .requestId(request.requestId())
                        .success(true)
                        .output(output)
                        .structuredPayload(buildStructuredPayload(request))
                        .build();
            } else {
                String error = pipelineResult != null && pipelineResult.getMessages() != null
                        ? String.join("; ", pipelineResult.getMessages())
                        : "Pipeline execution failed";
                result = com.shreeai.os.platform.runtime.execution.ExecutionResult.failure(
                        request.requestId(), error);
            }

            // Return session with the actual execution result attached
            return com.shreeai.os.platform.runtime.execution.ExecutionSession.builder()
                    .sessionId(session.sessionId())
                    .requestId(session.requestId())
                    .status(result.isSuccess() 
                            ? com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.COMPLETED
                            : com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.FAILED)
                    .result(result)
                    .createdAt(session.createdAt())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the structured payload map for an execution result.
     *
     * <p>Extracts the {@link IntelligenceContext} carried in the request metadata
     * (placed there by the SDK) and exposes it under the reserved key so the SDK
     * can return it to the developer application without information loss.</p>
     *
     * @param request the execution request
     * @return the structured payload map (never null, may be empty)
     */
    private java.util.Map<String, Object> buildStructuredPayload(
            com.shreeai.os.platform.runtime.execution.ExecutionRequest request) {
        if (request == null || request.metadata() == null) {
            return java.util.Map.of();
        }
        Object contextValue = request.metadata().get("intelligenceContext");
        if (contextValue instanceof IntelligenceContext intelligenceContext) {
            return java.util.Map.of("intelligenceContext", intelligenceContext);
        }
        return java.util.Map.of();
    }

    @Override
    public void stop() {
        if (lifecycle != null) {
            lifecycle.stop();
        }
    }

    @Override
    public String getName() {
        return "DefaultRuntimeService";
    }

    /**
     * Returns the current runtime state.
     *
     * @return current runtime state
     */
    public RuntimeState getRuntimeState() {
        return getState();
    }
}