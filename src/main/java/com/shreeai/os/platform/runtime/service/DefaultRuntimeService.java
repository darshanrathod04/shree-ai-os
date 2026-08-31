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
import com.shreeai.os.platform.runtime.pipeline.stages.ReflectionStage;
import com.shreeai.os.platform.kernels.memory.api.MemoryQueryService;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeIngestionService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeGroundingService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.runtime.embedding.EmbeddingProvider;
import com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder;
import com.shreeai.os.platform.runtime.execution.KnowledgeIngestionEventConsumer;
import com.shreeai.os.platform.runtime.storage.KnowledgeGraphStore;
import com.shreeai.os.platform.runtime.storage.KnowledgeGraphStores;
import com.shreeai.os.platform.runtime.vector.VectorStoreProvider;
import com.shreeai.os.platform.runtime.vector.VectorStoreProviders;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.factory.KernelFactory;
import com.shreeai.os.platform.kernels.factory.DefaultKernelFactory;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
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
import com.shreeai.os.platform.runtime.execution.ExecutionCapability;
import com.shreeai.os.platform.runtime.execution.ExecutionDispatcher;
import com.shreeai.os.platform.runtime.execution.KernelRegistry;
import com.shreeai.os.platform.runtime.execution.KernelHandler;
import com.shreeai.os.platform.runtime.execution.PermissionPolicy;
import com.shreeai.os.platform.runtime.execution.DefaultPermissionPolicy;
import com.shreeai.os.platform.runtime.execution.RichExecutionResult;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.inmemory.InMemoryLlmProvider;
import com.shreeai.os.platform.llm.ollama.OllamaProvider;
import com.shreeai.os.platform.llm.openai.OpenAiProvider;
import com.shreeai.os.platform.llm.gemini.GeminiProvider;
import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.engine.InMemoryApprovalService;

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
    /** Interchangeable LLM provider chain (GPT / Gemini / Ollama / in-memory). */
    private final LlmRouter llmRouter = buildDefaultLlmRouter();
    /** Approval gate backing autonomous retries and escalations. */
    private final ApprovalService approvalService = new InMemoryApprovalService();

    /** Thread-safe registry mapping capabilities to kernel handlers. */
    private final KernelRegistry kernelRegistry = new KernelRegistry();

    /** Permission policy evaluated before capability dispatch. */
    private final PermissionPolicy permissionPolicy = new DefaultPermissionPolicy();

    /** Capability-driven execution dispatcher. */
    private final ExecutionDispatcher executionDispatcher =
            new ExecutionDispatcher(kernelRegistry, permissionPolicy);

    /** Maximum autonomous re-executions advised by the reflection kernel. */
    private static final int MAX_AUTONOMOUS_RETRIES = 2;

    /**
     * Event-driven knowledge ingestion service, set during
     * {@link #initializeStages()} and consumed via {@link #bindEventBus(RuntimeEventBus)}.
     */
    private volatile KnowledgeIngestionService knowledgeIngestionService;

    /** Request metadata key carrying the requested operation. */
    private static final String OPERATION_KEY = "operation";

    /** The operation value that triggers capability-driven dispatch. */
    private static final String EXECUTE_TASK_OPERATION = "EXECUTE_TASK";

    /** Request metadata key carrying the requested capability. */
    private static final String CAPABILITY_KEY = "capability";

    /**
     * Builds the runtime's default LLM router from configuration.
     *
     * <p>Chain order comes from the {@code shree.llm.chain} system property or
     * the {@code LLM_CHAIN} environment variable (comma-separated provider
     * names, e.g. {@code openai,gemini,ollama,in-memory}); it defaults to the
     * deterministic in-memory provider so tests and offline runs stay stable.
     * Cloud providers register only when their API keys are present.</p>
     */
    private static LlmRouter buildDefaultLlmRouter() {
        Map<String, LlmProvider> registry = new LinkedHashMap<>();
        registry.put("in-memory", new InMemoryLlmProvider());
        registry.put("ollama", new OllamaProvider());

        String openAiKey = firstNonBlank(System.getenv("OPENAI_API_KEY"), null);
        if (openAiKey != null) {
            registry.put("openai", new OpenAiProvider(openAiKey));
        }

        String geminiKey = firstNonBlank(System.getenv("GEMINI_API_KEY"), System.getenv("GOOGLE_API_KEY"));
        if (geminiKey != null) {
            registry.put("gemini", new GeminiProvider(geminiKey));
        }

        String chain = firstNonBlank(
                System.getProperty("shree.llm.chain"),
                System.getenv("LLM_CHAIN"));
        if (chain == null) {
            chain = "in-memory";
        }

        try {
            return LlmRouter.fromChain(chain, registry);
        } catch (IllegalArgumentException ignored) {
            // Unknown provider names in the chain — fall back to a safe default.
            return new LlmRouter(List.of(registry.get("in-memory")));
        }
    }

    private static String firstNonBlank(String primary, String secondary) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (secondary != null && !secondary.isBlank()) {
            return secondary;
        }
        return null;
    }
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
     * <p>This method builds the 11-stage kernel execution pipeline:</p>
     * <ol>
     *   <li>IdentityStage - Resolves agent identity</li>
     *   <li>ContextStage - Builds execution context</li>
     *   <li>MemoryRecallStage - Recalls relevant memories</li>
     *   <li>KnowledgeStage - Retrieves knowledge</li>
     *   <li>ReasoningStage - Performs cognitive reasoning</li>
     *   <li>InferenceStage - Generates hypotheses</li>
     *   <li>PlanningStage - Creates execution plan</li>
     *   <li>ExecutionStage - Executes planned actions</li>
     *   <li>ReflectionStage - Evaluates execution outcome and stores lessons</li>
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
        // Storage providers are configuration-driven (PHASE-1):
        // in-memory by default; pgvector / neo4j when configured.
        // ==========================================================

        EmbeddingProvider embeddingProvider = new LocalDeterministicEmbedder(
                VectorStoreProviders.embeddingDimensions(System.getProperties()));

        KnowledgeGraphStore knowledgeGraphStore = KnowledgeGraphStores.selected();
        VectorStoreProvider vectorStoreProvider = VectorStoreProviders.selected();

        DefaultKnowledgeProcessingEngine knowledgeEngine =
                new DefaultKnowledgeProcessingEngine();

        DefaultKnowledgeService knowledgeService =
                new DefaultKnowledgeService(
                        knowledgeEngine,
                        knowledgeGraphStore,
                        vectorStoreProvider.vectorStore(),
                        vectorStoreProvider.searchEngine(),
                        embeddingProvider);

        // Expose the event-driven ingestion entry point for bindEventBus().
        this.knowledgeIngestionService = knowledgeService;

        KnowledgeQueryService knowledgeQueryService = knowledgeService;
        KnowledgeSearchService knowledgeSearchService = knowledgeService;
        KnowledgeRankingService knowledgeRankingService =
                new KnowledgeRankingService();
        KnowledgeGroundingService knowledgeGroundingService =
                new KnowledgeGroundingService(embeddingProvider);

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
        // V2.1 Capability-Driven Dispatch — register kernel handlers
        // ==========================================================
        registerCapabilityHandlers(
                memoryService,
                knowledgeService,
                planningService,
                executionService
        );

                // ==========================================================
        // Canonical 11-Stage Runtime Pipeline
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
                        knowledgeRankingService,
                        knowledgeGroundingService
                );

        stages.add(knowledgeStage);

        stages.add(new ReasoningStage(reasoningEngine));
        stages.add(new InferenceStage(inferenceEngine));

        PlanningStage planningStage = new PlanningStage(planningService);
        stages.add(planningStage);

        stages.add(new ActionExecutionStage(executionService));

        // EO-V1.5 Reflection Kernel — post-execution evaluation + lesson memory
        stages.add(new ReflectionStage(memoryService));

        MemoryStoreStage memoryStoreStage = new MemoryStoreStage(memoryService);
        stages.add(memoryStoreStage);

        stages.add(new ChiefReviewStage(chiefService, approvalService));

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

    /**
     * {@inheritDoc}
     *
     * <p>Subscribes the runtime-side event-driven kernel consumers on the
     * given SDK event bus. Currently binds the knowledge ingestion consumer
     * backing the event-driven {@code KnowledgeSDK.ingest(...)} contract.</p>
     */
    @Override
    public void bindEventBus(RuntimeEventBus eventBus) {
        if (eventBus == null) {
            return;
        }
        eventBus.subscribe(
                EventType.KNOWLEDGE_INGEST_REQUESTED,
                new KnowledgeIngestionEventConsumer(
                        () -> knowledgeIngestionService,
                        eventBus));
    }

    /**
     * Registers kernel handlers for the supported {@link ExecutionCapability}
     * values. Each handler delegates to the kernel that owns the capability
     * and produces a {@link RichExecutionResult}.
     *
     * <p>Registration is OCP-compliant: new capabilities can be added without
     * modifying the dispatcher.</p>
     *
     * @param memoryService    the memory kernel service (never null)
     * @param knowledgeService the knowledge kernel service (never null)
     * @param planningService  the planning kernel service (never null)
     * @param executionService the execution kernel service (never null)
     */
    private void registerCapabilityHandlers(
            DefaultMemoryService memoryService,
            DefaultKnowledgeService knowledgeService,
            PlanningService planningService,
            ExecutionService executionService) {

        // Memory Recall → Memory Kernel
        kernelRegistry.register(ExecutionCapability.MEMORY_RECALL,
                (capability, input, context) -> {
                    List<Memory> memories = memoryService.search(input);
                    String output = memories.stream()
                            .map(m -> m.content().text())
                            .collect(java.util.stream.Collectors.joining("\n"));
                    return RichExecutionResult.success(
                            capability, output, 0.85);
                });

        // Knowledge Search → Knowledge Kernel
        kernelRegistry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) -> {
                    List<KnowledgeNode> nodes = knowledgeService.search(input);
                    String output = nodes.stream()
                            .map(KnowledgeNode::getLabel)
                            .collect(java.util.stream.Collectors.joining("\n"));
                    return RichExecutionResult.success(
                            capability, output, 0.90);
                });

        // Project Planning → Planning Kernel
        kernelRegistry.register(ExecutionCapability.PROJECT_PLANNING,
                (capability, input, context) -> {
                    String planId = planningService.createPlan(
                            new PlanningService.PlanningRequest(
                                    input,
                                    PlanningTypes.PlanningScope.STANDARD,
                                    emptyPlanningConstraints()));
                    return RichExecutionResult.success(
                            capability, planId, 0.80);
                });

        // Workout Planning → Planning Kernel
        kernelRegistry.register(ExecutionCapability.WORKOUT_PLANNING,
                (capability, input, context) -> {
                    String planId = planningService.createPlan(
                            new PlanningService.PlanningRequest(
                                    input,
                                    PlanningTypes.PlanningScope.STANDARD,
                                    emptyPlanningConstraints()));
                    return RichExecutionResult.success(
                            capability, planId, 0.80);
                });

        // Task Execution → Execution Kernel
        kernelRegistry.register(ExecutionCapability.TASK_EXECUTION,
                (capability, input, context) -> {
                    String taskId = executionService.executeTask(
                            new com.shreeai.os.platform.kernels.execution.model.ExecutionRequest(
                                    new com.shreeai.os.platform.kernels.execution.model.ExecutionId(
                                            "exec-" + java.util.UUID.randomUUID()),
                                    input,
                                    new com.shreeai.os.platform.kernels.execution.model.ExecutionContext(
                                            new com.shreeai.os.platform.kernels.execution.model.ExecutionId(
                                                    "exec-" + java.util.UUID.randomUUID()),
                                            "dispatcher",
                                            "Task dispatched via ExecutionDispatcher",
                                            Map.of("input", input),
                                            1),
                                    new com.shreeai.os.platform.kernels.execution.model.ExecutionOptions(
                                            30000, 3, 1000, false, false,
                                            Map.of("source", "ExecutionDispatcher")),
                                    Map.of()));
                    return RichExecutionResult.success(
                            capability, taskId, 0.75);
                });
    }

    /**
     * Returns empty planning constraints for dispatcher-driven planning.
     *
     * @return empty planning constraints (never null)
     */
    private PlanningConstraints emptyPlanningConstraints() {
        return new PlanningConstraints(
                Map.of(), Map.of(), Map.of(), Map.of());
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

            // ================================================================
            // V2.1 Capability-Driven Dispatch
            // Requests carrying operation=EXECUTE_TASK with a capability field
            // are dispatched directly to the owning kernel handler through the
            // ExecutionDispatcher, bypassing the canonical pipeline entirely.
            // ================================================================
            String operation = request.metadata() != null
                    ? String.valueOf(request.metadata().getOrDefault(OPERATION_KEY, ""))
                    : "";
            String capabilityValue = request.metadata() != null
                    ? String.valueOf(request.metadata().getOrDefault(CAPABILITY_KEY, ""))
                    : "";

            if (EXECUTE_TASK_OPERATION.equals(operation) && !capabilityValue.isBlank()) {
                java.util.Optional<ExecutionCapability> capability =
                        ExecutionCapability.fromValue(capabilityValue);
                if (capability.isPresent()) {
                    RichExecutionResult richResult = executionDispatcher.dispatch(
                            capability.get(),
                            request.payload(),
                            request.metadata() != null ? request.metadata() : Map.of());

                    com.shreeai.os.platform.runtime.execution.ExecutionResult result =
                            richResult.toExecutionResult();

                    eventBus.publish(
                            new RuntimeEvent(
                                    EventType.PIPELINE_COMPLETED,
                                    request.requestId(),
                                    "CapabilityDispatch",
                                    Instant.now(),
                                    Map.of(
                                            "status", result.isSuccess() ? "SUCCESS" : "FAILED",
                                            "capability", capabilityValue)));

                    return com.shreeai.os.platform.runtime.execution.ExecutionSession.builder()
                            .sessionId(session.sessionId())
                            .requestId(session.requestId())
                            .status(result.isSuccess()
                                    ? com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.COMPLETED
                                    : com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.FAILED)
                            .result(result)
                            .createdAt(session.createdAt())
                            .build();
                }
            }

            // Execute the canonical pipeline with reflection-driven autonomous retry
            com.shreeai.os.platform.runtime.pipeline.PipelineContext pipelineContext = null;
            PipelineResult pipelineResult = null;

            if (effectivePipeline != null) {

                int maxAttempts = 1 + MAX_AUTONOMOUS_RETRIES;
                List<Object> retryLessons = List.of();

                for (int attempt = 1; attempt <= maxAttempts; attempt++) {

                    Map<String, Object> attemptMetadata = new LinkedHashMap<>();
                    if (request.metadata() != null) {
                        attemptMetadata.putAll(request.metadata());
                    }
                    if (attempt > 1) {
                        attemptMetadata.put("retryAttempt", attempt - 1);
                        attemptMetadata.put("reflectionLessons", retryLessons);
                    }

                    pipelineContext =
                            com.shreeai.os.platform.runtime.pipeline.PipelineContext.builder()
                                    .executionRequest(request)
                                    .addAttribute("executionContext", context)
                                    .addAttribute("executionSession", session)
                                    .addAttribute("requestContext", request.context())
                                    .addAttribute("requestMetadata", attemptMetadata)
                                    .addAttribute("runtimeEventBus", eventBus)
                                    .addAttribute("llmRouter", llmRouter)
                                    .addAttribute("approvalService", approvalService)
                                    .build();

                    pipelineResult = effectivePipeline.execute(pipelineContext);

                    boolean retryAdvised = pipelineResult != null
                            && pipelineResult.getExecutionState() != null
                            && Boolean.TRUE.equals(
                                    pipelineResult.getExecutionState().getMetadata()
                                            .get("reflectionRetryAdvised"));

                    if (!retryAdvised || attempt == maxAttempts) {
                        break;
                    }

                    // Carry the reflection lessons into the next attempt so the
                    // planning stage can adjust strategy.
                    Object lessons = pipelineResult.getExecutionState().getMetadata()
                            .get("reflectionLessons");
                    if (lessons instanceof List<?> lessonList) {
                        retryLessons = List.copyOf(lessonList);
                    }

                    eventBus.publish(new RuntimeEvent(
                            EventType.EXECUTION_COMPLETED,
                            request.requestId(),
                            "Runtime",
                            Instant.now(),
                            Map.of("autonomousRetry", attempt)
                    ));
                }
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