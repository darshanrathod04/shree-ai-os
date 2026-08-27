package com.shreeai.os.platform.runtime.service;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.legacy.execution.ExecutionMetadata;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.AbstractRuntimeService;
import com.shreeai.os.platform.runtime.RuntimeState;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionContext;
import com.shreeai.os.platform.runtime.internal.DefaultRuntimeLifecycle;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
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
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.kernels.response.engine.DefaultResponseSynthesizer;
import com.shreeai.os.platform.kernels.response.service.ResponseSynthesisService;
import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.runtime.routing.RuntimeIntentRouter;

import java.time.Instant;
import java.util.*;

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
    private final RuntimeEventBus eventBus;
    private final ResponseSynthesisService responseSynthesisService;
    private RuntimeIntentRouter intentRouter;
    /**
     * Constructs a new DefaultRuntimeService with the given configuration and contract.
     *
     * @param configuration the runtime configuration (must not be null)
     * @param contract      the runtime contract (must not be null)
     */
    /**
     * Legacy constructor (Backward Compatibility)
     */
    public DefaultRuntimeService(
            RuntimeConfiguration configuration,
            RuntimeContract contract
    ) {
        this(
                configuration,
                contract,
                List.of(),
                new RuntimeEventBus()
        );
    }

    /**
     * Canonical constructor
     */
    public DefaultRuntimeService(
            RuntimeConfiguration configuration,
            RuntimeContract contract,
            List<ExecutionStage> stages
    ) {
        this(
                configuration,
                contract,
                stages,
                new RuntimeEventBus()
        );
    }

    /**
     * Full constructor with RuntimeEventBus
     */
    public DefaultRuntimeService(
            RuntimeConfiguration configuration,
            RuntimeContract contract,
            List<ExecutionStage> stages,
            RuntimeEventBus eventBus
    ) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");

        this.stages = new ArrayList<>();
        this.pipeline = new DefaultExecutionPipeline(stages);
        this.lifecycle = new DefaultRuntimeLifecycle();

        this.eventBus = Objects.requireNonNull(eventBus);

        this.kernelFactory = new DefaultKernelFactory();

        this.responseSynthesisService = new ResponseSynthesisService();

        initializeStages();
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

        IdentityStage identityStage = new IdentityStage(identityService);
        ContextStage contextStage = new ContextStage();

        stages.add(identityStage);
        stages.add(contextStage);

        MemoryRecallStage memoryRecallStage =
                new MemoryRecallStage(
                        memoryQueryService,
                        memorySearchService,
                        memoryRankingService
                );

        stages.add(memoryRecallStage);

        KnowledgeStage knowledgeStage =
                new KnowledgeStage(
                        knowledgeQueryService,
                        knowledgeSearchService,
                        knowledgeRankingService
                );

        stages.add(knowledgeStage);

        stages.add(new ReasoningStage(reasoningEngine));
        stages.add(new InferenceStage(inferenceEngine));

        PlanningStage planningStage = new PlanningStage(planningService);
        stages.add(planningStage);

        stages.add(new ActionExecutionStage(executionService));

        MemoryStoreStage memoryStoreStage = new MemoryStoreStage(memoryService);
        stages.add(memoryStoreStage);

        stages.add(new ChiefReviewStage(chiefService));

        // ==========================================================
        // Deterministic Intent Router
        // Reuses the canonical kernel stage instances above; requests
        // carrying a known metadata.operation execute only the owning
        // kernel's stage chain, everything else keeps the canonical
        // Chief orchestration pipeline.
        // ==========================================================

        this.intentRouter = new RuntimeIntentRouter(
                identityStage,
                contextStage,
                knowledgeStage,
                planningStage,
                memoryRecallStage,
                memoryStoreStage
        );
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
        eventBus.publish(
                new RuntimeEvent(
                        EventType.PIPELINE_STARTED,
                        request.requestId(),
                        "Pipeline",
                        Instant.now(),
                        Map.of(
                                "requestType", request.requestType()
                        )
                )
        );

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


            // Resolve deterministic kernel routing for this request.
            // Requests carrying a known metadata.operation execute only the
            // owning kernel's stage chain; all other requests keep the
            // canonical Chief orchestration pipeline.
            RuntimeIntentRouter.ExecutionRoute route =
                    intentRouter != null
                            ? intentRouter.route(request).orElse(null)
                            : null;

            com.shreeai.os.platform.runtime.pipeline.ExecutionPipeline effectivePipeline = pipeline;
            if (route != null) {
                effectivePipeline = new DefaultExecutionPipeline(route.stages());
            }

            // Execute the canonical pipeline exactly once
            com.shreeai.os.platform.runtime.pipeline.PipelineContext pipelineContext = null;
            PipelineResult pipelineResult = null;

            if (effectivePipeline != null) {

                pipelineContext =
                        com.shreeai.os.platform.runtime.pipeline.PipelineContext.builder()
                                .executionRequest(request)
                                .addAttribute("executionContext", context)
                                .addAttribute("executionSession", session)
                                .addAttribute("requestContext", request.context())
                                .addAttribute("requestMetadata", request.metadata())
                                .addAttribute("runtimeEventBus", eventBus)
                                .build();

                pipelineResult = effectivePipeline.execute(pipelineContext);
            }

            // Convert PipelineResult to ExecutionResult, preserving any structured
            // intelligence context supplied with the request so rich data survives
            // the runtime → SDK boundary without being flattened into strings.
            com.shreeai.os.platform.runtime.execution.ExecutionResult result;
            if (pipelineResult != null && pipelineResult.isSuccess()) {

                SynthesizedResponse response =
                        responseSynthesisService.synthesize(
                                pipelineContext,
                                pipelineResult.getExecutionState()
                        );

                Map<String, Object> structured = new LinkedHashMap<>();

                structured.put("response", response);
                structured.putAll(buildStructuredPayload(request));

                // Additive, backward-compatible routing evidence so callers can
                // observe which kernel handled the request.
                if (route != null) {
                    structured.put("routedOperation", route.operation());
                    structured.put("routedKernel", route.kernelName());
                    structured.put("routedStages", route.stageNames());
                }

                Map<String, Object> payload = Map.copyOf(structured);

                result = ExecutionResult.builder()
                        .requestId(request.requestId())
                        .success(true)
                        .output(response.answer())
                        .structuredPayload(payload)
                        .build();

            } else {
                String error = pipelineResult != null && pipelineResult.getMessages() != null
                        ? String.join("; ", pipelineResult.getMessages())
                        : "Pipeline execution failed";
                result = com.shreeai.os.platform.runtime.execution.ExecutionResult.failure(
                        request.requestId(), error);
            }

            eventBus.publish(
                    new RuntimeEvent(
                            EventType.PIPELINE_COMPLETED,
                            request.requestId(),
                            "Pipeline",
                            Instant.now(),
                            Map.of(
                                    "status", "SUCCESS"
                            )
                    )
            );

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
            eventBus.publish(
                    new RuntimeEvent(
                            EventType.PIPELINE_FAILED,
                            request.requestId(),
                            "Pipeline",
                            Instant.now(),
                            Map.of(
                                    "reason", e.getMessage()
                            )
                    )
            );
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