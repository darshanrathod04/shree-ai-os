package com.shreeai.os.platform.runtime.service;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
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
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.factory.KernelFactory;
import com.shreeai.os.platform.kernels.factory.DefaultKernelFactory;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.response.service.ResponseSynthesisService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalyzer;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult;
import com.shreeai.os.platform.runtime.orchestration.MultiKernelOrchestrator;
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
 *   <li>Coordinates lifecycle only Ã¢â‚¬â€ no platform business logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Service</p>
 * <p><b>Lifecycle:</b> CREATED Ã¢â€ â€™ INITIALIZED Ã¢â€ â€™ STARTED Ã¢â€ â€™ VERIFIED Ã¢â€ â€™ STOPPED</p>
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

    // ─── Sprint-18: Autonomous Intelligence Layer ──────────────────────────────

    /**
     * The single entry point for all {@code ShreeAI.chat()} requests in Sprint 18.
     * Routes every request, runs workspace diagnostics, and decides whether to
     * proceed to kernel execution or surface a diagnostic response. Stateless and
     * thread-safe; all state lives in the request or returned objects.
     */
    private final com.shreeai.os.platform.runtime.agents.ChiefIntelligenceAgent chiefIntelligenceAgent =
            new com.shreeai.os.platform.runtime.agents.ChiefIntelligenceAgent();

    /** Request metadata key for the chief decision id (Sprint 18). */
    private static final String CHIEF_DECISION_ID_KEY = "chiefDecisionId";

    /** Request metadata key for the chief execution plan id (Sprint 18). */
    private static final String CHIEF_PLAN_ID_KEY = "chiefPlanId";

    /** Request metadata key for the chief's primary kernel (Sprint 18). */
    private static final String CHIEF_PRIMARY_KERNEL_KEY = "chiefPrimaryKernel";

    /** Request metadata key for the chief's diagnostic status (Sprint 18). */
    private static final String CHIEF_DIAGNOSTIC_STATUS_KEY = "chiefDiagnosticStatus";

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

    // ─── Sprint-12: Multi-Kernel Orchestration ────────────────────────────────

    /** Stores the MemoryService for orchestrator access (initialized in initializeStages). */
    private com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService memoryServiceField;

    /** Stores the KnowledgeSearchService for orchestrator access. */
    private com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService knowledgeSearchServiceField;

    /** Stores the PlanningService for orchestrator access. */
    private com.shreeai.os.platform.kernels.planning.api.PlanningService planningServiceField;

    /** Lazy-initialized multi-kernel orchestrator. */
    private volatile com.shreeai.os.platform.runtime.orchestration.MultiKernelOrchestrator orchestrator;

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
            // Unknown provider names in the chain Ã¢â‚¬â€ fall back to a safe default.
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
        // V2.1 Capability-Driven Dispatch Ã¢â‚¬â€ register kernel handlers
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

        // EO-V1.5 Reflection Kernel Ã¢â‚¬â€ post-execution evaluation + lesson memory
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

        // Sprint-12: Store services for the multi-kernel orchestrator
        this.memoryServiceField = memoryService;
        this.knowledgeSearchServiceField = knowledgeSearchService;
        this.planningServiceField = planningService;
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

        // Memory Recall Ã¢â€ â€™ Memory Kernel
        kernelRegistry.register(ExecutionCapability.MEMORY_RECALL,
                (capability, input, context) -> {
                    List<Memory> memories = memoryService.search(input);
                    String output = memories.stream()
                            .map(m -> m.content().text())
                            .collect(java.util.stream.Collectors.joining("\n"));
                    return RichExecutionResult.success(
                            capability, output, 0.85);
                });

        // Knowledge Search Ã¢â€ â€™ Knowledge Kernel
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
        // Sprint-10: pull the user's actual objective from context.get("input")
        // (where ShreeClient placed it) rather than from the dispatcher's
        // `input` parameter, which is the SDK message placeholder
        // ("EXECUTION_RUN") and would otherwise produce a meaningless plan.
        kernelRegistry.register(ExecutionCapability.PROJECT_PLANNING,
                (capability, input, context) -> {
                    String userObjective = resolveUserObjective(input, context);
                    String planId = planningService.createPlan(
                            new PlanningService.PlanningRequest(
                                    userObjective,
                                    PlanningTypes.PlanningScope.STANDARD,
                                    emptyPlanningConstraints()));
                    String executionId = "exec-" + java.util.UUID.randomUUID();
                    java.util.Map<String, Object> meta = new java.util.HashMap<>();
                    meta.put("planId", planId);
                    meta.put("objective", userObjective);
                    meta.put("capabilityValue", capability.value());
                    meta.put("kernel", "Planning Kernel");
                    meta.put("inputSource", context.containsKey("input")
                            ? "context.input" : "dispatcher.input");
                    return RichExecutionResult.builder()
                            .executionId(executionId)
                            .capability(capability)
                            .status(com.shreeai.os.platform.runtime.execution.ExecutionStatus.SUCCESS)
                            .output(userObjective)
                            .confidence(0.90)
                            .metadata(meta)
                            .build();
                });

        // Workout Planning → Planning Kernel
        // Same Sprint-10 fix as PROJECT_PLANNING: use the user's actual
        // input from context, not the SDK message placeholder.
        kernelRegistry.register(ExecutionCapability.WORKOUT_PLANNING,
                (capability, input, context) -> {
                    String userObjective = resolveUserObjective(input, context);
                    String planId = planningService.createPlan(
                            new PlanningService.PlanningRequest(
                                    userObjective,
                                    PlanningTypes.PlanningScope.STANDARD,
                                    emptyPlanningConstraints()));
                    String executionId = "exec-" + java.util.UUID.randomUUID();
                    java.util.Map<String, Object> meta = new java.util.HashMap<>();
                    meta.put("planId", planId);
                    meta.put("objective", userObjective);
                    meta.put("capabilityValue", capability.value());
                    meta.put("kernel", "Planning Kernel");
                    meta.put("inputSource", context.containsKey("input")
                            ? "context.input" : "dispatcher.input");
                    return RichExecutionResult.builder()
                            .executionId(executionId)
                            .capability(capability)
                            .status(com.shreeai.os.platform.runtime.execution.ExecutionStatus.SUCCESS)
                            .output(userObjective)
                            .confidence(0.90)
                            .metadata(meta)
                            .build();
                });

        // Task Execution Ã¢â€ â€™ Execution Kernel
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

                // Sprint-10: pass the full metadata (including the user's actual
                // input from ExecutionSDK.execute()) to the dispatcher.
                java.util.Map<String, Object> dispatchContext =
                        request.metadata() != null
                                ? new java.util.HashMap<>(request.metadata())
                                : new java.util.HashMap<>();

                com.shreeai.os.platform.runtime.execution.ExecutionResult result;
                if (capability.isPresent()) {
                    // Known capability: dispatch to the registered handler and synthesize
                    RichExecutionResult richResult = executionDispatcher.dispatch(
                            capability.get(),
                            request.payload(),
                            dispatchContext);
                    result = buildSynthesizedExecutionResult(
                            capability.get(),
                            capabilityValue,
                            richResult,
                            dispatchContext);
                } else {
                    // Sprint-10: unknown capability — produce a structured failure
                    // without throwing, bypassing the canonical pipeline entirely.
                    result = buildSynthesizedUnknownCapabilityResult(
                            capabilityValue,
                            dispatchContext);
                }

                // Sprint-10: structured response is always returned — the raw handler
                // output (e.g. "Goal{...}") is NEVER returned directly as answer.
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

            // ================================================================
            // Sprint-12: Multi-Kernel Orchestrator
            // Requests that carry no explicit routed operation AND that the
            // deterministic intent analyzer flags as multi-kernel are
            // automatically orchestrated across the relevant kernels and
            // composed into a single response. Existing single-kernel and
            // unrouted Chief-pipeline flows are unchanged.
            // ================================================================
            if (operation.isBlank() && route == null && request.payload() != null
                    && !request.payload().isBlank()) {
                IntentAnalysisResult analysis = new IntentAnalyzer()
                        .analyze(request.payload());

                if (analysis.isMultiKernel()) {
                    com.shreeai.os.platform.runtime.orchestration.CompositeKernelResult composite =
                            getOrchestrator().orchestrate(
                                    request.payload(),
                                    request.requestId(),
                                    request.metadata()
                            );

                    // Build the synthesized result so the SDK contract is preserved
                    com.shreeai.os.platform.runtime.execution.ExecutionResult result =
                            buildOrchestratedResult(request, composite, analysis);

                    eventBus.publish(
                            new RuntimeEvent(
                                    EventType.PIPELINE_COMPLETED,
                                    request.requestId(),
                                    "MultiKernelOrchestrator",
                                    Instant.now(),
                                    Map.of(
                                            "status", result.isSuccess() ? "SUCCESS" : "FAILED",
                                            "primaryIntent", analysis.primaryIntent().name(),
                                            "kernelCount", composite.kernelResults().size(),
                                            "executionOrder", String.join(",", composite.executionOrder())
                                    )
                            )
                    );

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

            // ─── Sprint-18: Chief Intelligence Agent pre-flight ─────────────────
            // Every default (unrouted) chat request passes through the
            // ChiefIntelligenceAgent, which: (1) routes the request into an
            // ExecutionPlan, (2) diagnoses the workspace, and (3) either
            // short-circuits with a diagnostic response when unhealthy, or
            // enriches request metadata with the chief's decision so downstream
            // stages retain observability. The canonical pipeline still runs
            // when the workspace is healthy.

            com.shreeai.os.platform.kernels.response.model.SynthesizedResponse chiefResponse = null;
            java.util.Map<String, Object> chiefMeta = java.util.Map.of();
            // Sprint-18: chief metadata captured here is injected into the
            // pipeline context's attemptMetadata (request.metadata() is
            // unmodifiable). Keys use the CHIEF_*_KEY constants for consistency.
            java.util.Map<String, Object> chiefMetadataForPipeline = new java.util.LinkedHashMap<>();
            String chiefDecisionIdCaptured = null;

            if (operation.isBlank() && route == null && request.payload() != null
                    && !request.payload().isBlank()) {
                // Sprint-18: wrap the chief pre-flight in a try/catch so a transient
                // analysis error never blocks the canonical pipeline. The chief is
                // observability + diagnostic, not a hard pre-condition.
                try {
                    chiefResponse = chiefIntelligenceAgent.route(request);
                    chiefMeta = chiefResponse.structuredData() != null
                            ? chiefResponse.structuredData()
                            : java.util.Map.of();
                } catch (RuntimeException chiefError) {
                    String reason = chiefError.getMessage() != null
                            ? chiefError.getMessage()
                            : chiefError.getClass().getSimpleName();
                    eventBus.publish(new RuntimeEvent(
                            EventType.PIPELINE_FAILED,
                            request.requestId(),
                            "ChiefIntelligenceAgent",
                            Instant.now(),
                            Map.of(
                                    "status", "CHIEF_SKIPPED",
                                    "reason", reason
                            )
                    ));
                    chiefResponse = null;
                    chiefMeta = java.util.Map.of();
                }

                Object isHealthyObj = chiefMeta.get("isHealthy");
                boolean isHealthy = isHealthyObj instanceof Boolean b && b;
                Object hasFailuresObj = chiefMeta.get("hasFailures");
                boolean hasFailures = hasFailuresObj instanceof Boolean bf && bf;

                // Sprint-18: only short-circuit when a CRITICAL check has failed.
                // Soft warnings (WARN) and PROJECT/MEMORY unavailability are
                // recoverable — they are not pre-flight blockers, just signals
                // that the canonical pipeline may return lower-confidence
                // results. We only abort the request when EXECUTION is FAIL
                // (the engine itself is down) or WORKSPACE is FAIL (no
                // operating environment at all).
                Object criticalFailureObj = chiefMeta.get("criticalFailure");
                boolean criticalFailure = criticalFailureObj instanceof Boolean cf && cf;
                if (criticalFailure) {
                    // Workspace unhealthy — short-circuit with diagnostic response
                    java.util.Map<String, Object> chiefPayload = new java.util.LinkedHashMap<>();
                    chiefPayload.put("response", chiefResponse);
                    chiefPayload.put("source", "ChiefIntelligenceAgent");
                    chiefPayload.put("isHealthy", isHealthy);
                    chiefPayload.put("hasFailures", hasFailures);
                    Object capturedDecision = chiefMeta.get("chiefDecisionId");
                    if (capturedDecision != null) {
                        String v = String.valueOf(capturedDecision);
                        if (!v.isEmpty()) {
                            chiefPayload.put(CHIEF_DECISION_ID_KEY, v);
                        }
                    }
                    Object capturedPlan = chiefMeta.get("executionPlanId");
                    if (capturedPlan != null) {
                        String v = String.valueOf(capturedPlan);
                        if (!v.isEmpty()) {
                            chiefPayload.put(CHIEF_PLAN_ID_KEY, v);
                        }
                    }

                    com.shreeai.os.platform.runtime.execution.ExecutionResult diagResult =
                            com.shreeai.os.platform.runtime.execution.ExecutionResult.builder()
                                    .requestId(request.requestId())
                                    .success(false)
                                    .output(chiefResponse.answer())
                                    .structuredPayload(java.util.Map.copyOf(chiefPayload))
                                    .build();

                    eventBus.publish(new RuntimeEvent(
                            EventType.PIPELINE_COMPLETED,
                            request.requestId(),
                            "ChiefIntelligenceAgent",
                            Instant.now(),
                            Map.of(
                                    "status", "DIAGNOSTIC",
                                    "isHealthy", isHealthy,
                                    "hasFailures", hasFailures)));

                    return com.shreeai.os.platform.runtime.execution.ExecutionSession.builder()
                            .sessionId(session.sessionId())
                            .requestId(session.requestId())
                            .status(com.shreeai.os.platform.runtime.execution.ExecutionSession.SessionStatus.FAILED)
                            .result(diagResult)
                            .createdAt(session.createdAt())
                            .build();
                }

                // Healthy — capture chief routing decision into a deferred payload
                // for later injection into the pipeline context (request.metadata()
                // is unmodifiable, so we cannot mutate it directly here).
                if (!chiefMeta.isEmpty()) {
                    Object decisionObj = chiefMeta.get("chiefDecisionId");
                    Object planObj = chiefMeta.get("executionPlanId");
                    if (decisionObj != null) {
                        String v = String.valueOf(decisionObj);
                        chiefDecisionIdCaptured = v;
                        if (!v.isEmpty()) {
                            // Stash for the canonical pipeline metadata
                            chiefMetadataForPipeline.putIfAbsent(
                                    CHIEF_DECISION_ID_KEY, v);
                        }
                    }
                    if (planObj != null) {
                        String v = String.valueOf(planObj);
                        if (!v.isEmpty()) {
                            chiefMetadataForPipeline.putIfAbsent(
                                    CHIEF_PLAN_ID_KEY, v);
                        }
                    }
                    chiefMetadataForPipeline.putIfAbsent(
                            CHIEF_DIAGNOSTIC_STATUS_KEY,
                            isHealthy ? "HEALTHY" : "DEGRADED");
                    Object primaryKernel = chiefMeta.get("primaryKernel");
                    if (primaryKernel != null) {
                        String v = String.valueOf(primaryKernel);
                        if (!v.isEmpty()) {
                            chiefMetadataForPipeline.putIfAbsent(
                                    CHIEF_PRIMARY_KERNEL_KEY, v);
                        }
                    }
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

                    // Sprint-18: inject chief intelligence routing metadata into the
                    // pipeline context. chiefMetadataForPipeline is empty if the chief
                    // pre-flight was skipped, so this is a no-op in those cases.
                    if (!chiefMetadataForPipeline.isEmpty()) {
                        chiefMetadataForPipeline.forEach(attemptMetadata::putIfAbsent);
                    }

                    pipelineContext =
                            com.shreeai.os.platform.runtime.pipeline.PipelineContext.builder()
                                    .executionRequest(toV2ExecutionRequest(request))
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
            // the runtime Ã¢â€ â€™ SDK boundary without being flattened into strings.
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

                // ─── Phase 2.2: Evidence Mode ─────────────────────────────────────
                // Extract structured evidence from the populated pipeline state
                // and inject the evidence bundle + verification report into the
                // structured payload. This is the runtime-level Evidence Mode
                // requirement: every chat response must carry evidence so the
                // SDK surface can answer "what is this answer grounded in?"
                //
                // Root cause (pre-fix): ChiefIntelligenceAgent.route() was called
                // BEFORE the pipeline ran, so EvidenceAgent extracted from an
                // empty state. The generated response was then discarded.
                // Fix: extract evidence AFTER the pipeline populates the state.
                if (pipelineResult != null && pipelineResult.getExecutionState() != null) {
                    Map<String, Object> pipelineStateMeta =
                            pipelineResult.getExecutionState().getMetadata();
                    if (pipelineStateMeta != null && !pipelineStateMeta.isEmpty()) {
                        try {
                            com.shreeai.os.platform.runtime.agents.EvidenceAgent evidenceAgent =
                                    new com.shreeai.os.platform.runtime.agents.EvidenceAgent();
                            // Use extractFromMetadata() to read from the pipeline state
                            // which now contains knowledgeResults, reasoningConclusion, etc.
                            com.shreeai.os.platform.runtime.model.EvidenceBundle evidenceBundle =
                                    evidenceAgent.extractFromMetadata(pipelineStateMeta);
                            if (evidenceBundle != null && !evidenceBundle.isEmpty()) {
                                com.shreeai.os.platform.runtime.agents.VerificationAgent verificationAgent =
                                        new com.shreeai.os.platform.runtime.agents.VerificationAgent();
                                com.shreeai.os.platform.runtime.model.VerificationReport verificationReport =
                                        verificationAgent.verify(evidenceBundle);

                                // Build a serializable evidence summary for the API response
                                java.util.List<java.util.Map<String, Object>> evidenceSummary =
                                        new java.util.ArrayList<>();
                                for (com.shreeai.os.platform.runtime.model.EvidenceItem item
                                        : evidenceBundle.items()) {
                                    java.util.Map<String, Object> itemMap = new java.util.LinkedHashMap<>();
                                    itemMap.put("itemId", item.itemId());
                                    itemMap.put("sourceType", item.sourceType().name());
                                    itemMap.put("title", item.title());
                                    itemMap.put("content", item.content());
                                    itemMap.put("confidenceHint", item.confidenceHint());
                                    itemMap.put("citations", item.citations());
                                    itemMap.put("attributes", item.attributes());
                                    evidenceSummary.add(itemMap);
                                }

                                structured.put("evidence", evidenceSummary);
                                structured.put("evidenceCount", evidenceBundle.size());
                                structured.put("evidenceBundleId", evidenceBundle.bundleId());

                                // Phase 2.2: Re-synthesize the answer using
                                // NaturalResponseAgent so the response text
                                // is grounded in the actual evidence items,
                                // not the generic DefaultResponseSynthesizer output.
                                com.shreeai.os.platform.runtime.agents.NaturalResponseAgent naturalAgent =
                                        new com.shreeai.os.platform.runtime.agents.NaturalResponseAgent();
                                com.shreeai.os.platform.kernels.response.model.SynthesizedResponse evidenceBackedResponse =
                                        naturalAgent.generate(verificationReport, request);

                                response = evidenceBackedResponse;
                                structured.put("response", response);
                                structured.put("confidence", verificationReport.confidence());
                                structured.put("verificationTier",
                                        verificationReport.tier().name());
                                structured.put("verificationConfidence",
                                        verificationReport.confidence());
                                structured.put("citationCount",
                                        verificationReport.citations().size());
                                if (!verificationReport.citations().isEmpty()) {
                                    structured.put("citations",
                                            verificationReport.citations());
                                }
                                if (!verificationReport.gaps().isEmpty()) {
                                    structured.put("gaps",
                                            verificationReport.gaps());
                                }
                            }
                        } catch (RuntimeException evidenceError) {
                            // Evidence extraction is best-effort: never fail
                            // the request just because evidence grounding
                            // could not be applied. Log via event bus.
                            eventBus.publish(new RuntimeEvent(
                                    EventType.PIPELINE_FAILED,
                                    request.requestId(),
                                    "EvidenceMode",
                                    Instant.now(),
                                    Map.of(
                                            "status", "EVIDENCE_SKIPPED",
                                            "reason",
                                            evidenceError.getMessage() != null
                                                    ? evidenceError.getMessage()
                                                    : evidenceError.getClass().getSimpleName()
                                    )
                            ));
                        }
                    }
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
            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            eventBus.publish(
                    new RuntimeEvent(
                            EventType.PIPELINE_FAILED,
                            request.requestId(),
                            "Pipeline",
                            Instant.now(),
                            Map.of(
                                    "reason", reason
                            )
                    )
            );
            throw new RuntimeException("Execution failed: " + reason, e);
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

    // ─────────────────────────────────────────────────────────────────────────
    // Sprint-10 helper: resolves the user's actual objective.
    //
    // The dispatcher's `input` parameter is the SDK message placeholder
    // ("EXECUTION_RUN") because ExecutionSDK.execute() sets message="EXECUTION_RUN"
    // and ShreeClient.chat() uses request.message() as ExecutionRequest.payload.
    // The user's actual input is placed in metadata["input"] by ExecutionSDK.
    // ─────────────────────────────────────────────────────────────────────────
    private static String resolveUserObjective(
            String dispatcherInput,
            java.util.Map<String, Object> context
    ) {
        if (context != null) {
            Object input = context.get("input");
            if (input instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return dispatcherInput != null ? dispatcherInput : "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sprint-10: constructs an ExecutionResult whose structured payload carries
    // a SynthesizedResponse so ShreeClient.chat() picks it up and returns it
    // as SDKResponse.answer instead of the raw handler output.
    // ─────────────────────────────────────────────────────────────────────────
    private com.shreeai.os.platform.runtime.execution.ExecutionResult
            buildSynthesizedExecutionResult(
                    ExecutionCapability capability,
                    String capabilityValueRaw,
                    RichExecutionResult richResult,
                    java.util.Map<String, Object> context
            ) {
        String executionId = richResult.executionId();
        long executionTimeMs = richResult.durationMs();
        boolean success = richResult.isSuccess();

        String effectiveCapabilityValue = capability != null
                ? capability.value()
                : (capabilityValueRaw == null || capabilityValueRaw.isBlank()
                        ? "UNKNOWN" : capabilityValueRaw);

        String objective = extractString(richResult.metadata(), "objective",
                () -> extractString(context, "input", () -> richResult.output()));
        String kernel = extractString(richResult.metadata(), "kernel",
                () -> effectiveCapabilityValue + " Kernel");
        // Sprint-10: the Planning Kernel handler stores Goal.toString() as planId.
        // Detect that and replace with a clean synthetic planId to prevent the
        // Goal{...} Java object dump from leaking into the SDK payload.
        String rawPlanId = extractString(richResult.metadata(), "planId",
                () -> "plan-" + java.util.UUID.randomUUID());
        String planId = sanitizePlanId(rawPlanId);
        String status = success ? "COMPLETED" : "FAILED";

        java.util.List<String> deliverables =
                buildDefaultDeliverables(objective, capability);

        SynthesizedResponse synthesis = new DefaultResponseSynthesizer()
                .synthesizeExecution(
                        capability != null
                                ? capability
                                : ExecutionCapability.PROJECT_PLANNING,
                        objective, status, executionId,
                        planId, kernel, deliverables, executionTimeMs);

        java.util.Map<String, Object> enrichedPayload = new java.util.HashMap<>();
        enrichedPayload.put("response", synthesis);
        // Sprint-10: do not blindly copy handler metadata. The planId field
        // produced by Planning Kernel handlers is Goal.toString() (the
        // smoking-gun string). We expose it under a clean key instead of
        // letting it leak into the SDK payload.
        enrichedPayload.put("planId", planId);
        enrichedPayload.put("kernel", kernel);
        enrichedPayload.put("capabilityValue", effectiveCapabilityValue);
        enrichedPayload.put("objective", objective);
        enrichedPayload.put("status", status);
        enrichedPayload.put("executionId", executionId);
        enrichedPayload.put("executionTimeMs", executionTimeMs);
        enrichedPayload.put("deliverables", deliverables);

        return com.shreeai.os.platform.runtime.execution.ExecutionResult.builder()
                .requestId(executionId)
                .success(success)
                .output(synthesis.answer())
                .errorMessage(success ? null : richResult.output())
                .completedAt(richResult.completedAt())
                .structuredPayload(enrichedPayload)
                .build();
    }

    private String extractString(
            java.util.Map<String, Object> source,
            String key,
            java.util.function.Supplier<String> fallback
    ) {
        if (source != null) {
            Object value = source.get(key);
            if (value instanceof String s && !s.isBlank()) { return s; }
        }
        return fallback.get();
    }

    /**
     * Sprint-10: detects when a planId value is actually a Java object dump
     * (e.g. {@code Goal{planningId=..., objective=...}} produced by
     * {@code Goal.toString()}) and replaces it with a clean synthetic planId.
     *
     * <p>This is the surgical fix for the dispatcher's {@code Goal{...}} dump
     * bug. The Planning Kernel returns {@code Goal.toString()} as a String,
     * and the dispatcher previously propagated that to the SDK. We sanitize
     * it here at the boundary so the user-facing payload never contains a
     * Java object representation.</p>
     */
    private String sanitizePlanId(String planId) {
        if (planId == null) {
            return "plan-" + java.util.UUID.randomUUID();
        }
        // Heuristic: any value containing "Goal{" or "RichExecutionResult{"
        // is a Java toString() dump, not a real planId.
        if (planId.contains("Goal{")
                || planId.contains("RichExecutionResult{")
                || planId.contains("planningId=")
                || planId.contains("objective=")
                || planId.contains("constraints=")) {
            return "plan-" + java.util.UUID.randomUUID();
        }
        return planId;
    }

    /**
     * Sprint-10: builds a structured failure response for an unknown capability
     * (one not registered in the {@link ExecutionCapability} enum).
     *
     * <p>The SDK receives a structured, well-formed response — no exception,
     * no pipeline fallback, and no raw Java dumps in the answer or payload.</p>
     */
    private com.shreeai.os.platform.runtime.execution.ExecutionResult
            buildSynthesizedUnknownCapabilityResult(
                    String capabilityValue,
                    java.util.Map<String, Object> context
            ) {
        String executionId = "exec-" + java.util.UUID.randomUUID();
        String objective = context != null
                ? extractString(context, "input", () -> "")
                : "";
        String planId = "plan-" + java.util.UUID.randomUUID();
        String kernel = "Unknown";
        String status = "NOT_SUPPORTED";

        SynthesizedResponse synthesis = new DefaultResponseSynthesizer()
                .synthesizeExecution(
                        ExecutionCapability.PROJECT_PLANNING,
                        objective,
                        status,
                        executionId,
                        planId,
                        kernel,
                        java.util.List.of(),
                        0L);

        java.util.Map<String, Object> enrichedPayload = new java.util.HashMap<>();
        enrichedPayload.put("response", synthesis);
        enrichedPayload.put("planId", planId);
        enrichedPayload.put("kernel", kernel);
        enrichedPayload.put("capabilityValue",
                capabilityValue == null ? "UNKNOWN" : capabilityValue);
        enrichedPayload.put("objective", objective);
        enrichedPayload.put("status", status);
        enrichedPayload.put("executionId", executionId);
        enrichedPayload.put("executionTimeMs", 0L);
        enrichedPayload.put("deliverables", java.util.List.of());

        return com.shreeai.os.platform.runtime.execution.ExecutionResult.builder()
                .requestId(executionId)
                // Sprint-10: always return success=true for unknown capability so the
                // SDK returns the structured response instead of throwing SDKException.
                // The actual execution status (NOT_SUPPORTED) is encoded in the
                // structured payload so clients can distinguish the two outcomes.
                .success(true)
                .output(synthesis.answer())
                .errorMessage(null)
                .completedAt(Instant.now())
                .structuredPayload(enrichedPayload)
                .build();
    }

    /**
     * Builds a structured ExecutionResult for an orchestrated multi-kernel run.
     *
     * <p>The result preserves backward compatibility by returning the composite
     * outcome through the existing {@link com.shreeai.os.platform.runtime.execution.ExecutionResult}
     * contract. The structured payload carries the full composite kernel result,
     * intent analysis, and execution order for advanced clients.</p>
     */
    private com.shreeai.os.platform.runtime.execution.ExecutionResult buildOrchestratedResult(
            com.shreeai.os.platform.runtime.execution.ExecutionRequest request,
            com.shreeai.os.platform.runtime.orchestration.CompositeKernelResult composite,
            IntentAnalysisResult analysis
    ) {
        // Build the multi-kernel response through DefaultResponseSynthesizer's
        // new public method (added in Sprint-12)
        DefaultResponseSynthesizer synthesizer = new DefaultResponseSynthesizer();
        com.shreeai.os.platform.kernels.response.model.SynthesizedResponse synthesis =
                synthesizer.synthesizeComposite(
                        com.shreeai.os.platform.runtime.pipeline.PipelineContext.builder()
                                .executionRequest(toV2ExecutionRequest(request))
                                .build(),
                        composite,
                        analysis
                );

        java.util.Map<String, Object> enrichedPayload = new java.util.LinkedHashMap<>();
        enrichedPayload.put("response", synthesis);
        enrichedPayload.put("orchestrated", true);
        enrichedPayload.put("primaryIntent", analysis.primaryIntent().name());
        enrichedPayload.put("secondaryIntents",
                analysis.secondaryIntents().stream()
                        .map(Enum::name)
                        .toList());
        enrichedPayload.put("requiredKernels",
                analysis.requiredKernels().stream()
                        .map(Enum::name)
                        .toList());
        enrichedPayload.put("executionOrder", composite.executionOrder());
        enrichedPayload.put("kernelCount", composite.kernelResults().size());
        enrichedPayload.put("compositeResult", composite);
        enrichedPayload.put("intentConfidence", analysis.confidence());
        enrichedPayload.put("overallConfidence", composite.overallConfidence());
        enrichedPayload.put("durationMs", composite.durationMs());
        enrichedPayload.put("entities", analysis.entities());

        java.util.Map<String, Object> payload = java.util.Map.copyOf(enrichedPayload);

        return com.shreeai.os.platform.runtime.execution.ExecutionResult.builder()
                .requestId(request.requestId())
                .success(composite.isSuccess())
                .output(synthesis.answer())
                .structuredPayload(payload)
                .build();
    }

    private java.util.List<String> buildDefaultDeliverables(
            String objective,
            ExecutionCapability capability
    ) {
        if (objective == null || objective.isBlank()) {
            return java.util.List.of();
        }
        String lower = objective.toLowerCase(java.util.Locale.ROOT);
        if (capability == ExecutionCapability.PROJECT_PLANNING
                || capability == ExecutionCapability.WORKOUT_PLANNING) {
            if (lower.contains("dashboard")) {
                return java.util.List.of(
                        "Dashboard layout", "Navigation structure",
                        "Widget architecture", "Testing checklist");
            }
            if (lower.contains("api") || lower.contains("rest")) {
                return java.util.List.of(
                        "API specification", "Endpoint definitions",
                        "Request/response schemas", "Integration guide");
            }
            if (lower.contains("mobile") || lower.contains("android")) {
                return java.util.List.of(
                        "Screen wireframes", "Navigation flow",
                        "State management design", "Testing strategy");
            }
            return java.util.List.of(
                    "Requirements analysis", "Architecture outline",
                    "Implementation steps", "Quality checklist");
        }
        return java.util.List.of();
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

    /**
     * Returns the lazily-initialized multi-kernel orchestrator.
     * The orchestrator is created on first access after stage initialization.
     *
     * @return the multi-kernel orchestrator (never null after initialization)
     */
    private MultiKernelOrchestrator getOrchestrator() {
        if (orchestrator == null) {
            synchronized (this) {
                if (orchestrator == null) {
                    IntentAnalyzer analyzer = new IntentAnalyzer();
                    DefaultReflectionEngine reflectionEngine = new DefaultReflectionEngine();
                    ResponseSynthesisService synthesisService = new ResponseSynthesisService();

                    orchestrator = new MultiKernelOrchestrator(
                            analyzer,
                            memoryServiceField,
                            knowledgeSearchServiceField,
                            planningServiceField,
                            reflectionEngine,
                            synthesisService,
                            eventBus,
                            new com.shreeai.os.platform.kernels.developer.engine.DefaultDeveloperAgentEngine()
                    );
                }
            }
        }
        return orchestrator;
    }

    private static com.shreeai.os.platform.kernels.execution.model.ExecutionRequest toV2ExecutionRequest(
            com.shreeai.os.platform.runtime.execution.ExecutionRequest request) {
        // Preserve the V1 requestId as the V2 executionId so the pipeline
        // receives a stable, non-random identifier.  Without this the
        // ContextStage would see a blank requestId (since Builder.requestId()
        // only sets the actionId field, not the executionId field used by
        // build() to construct the default ExecutionContext).
        com.shreeai.os.platform.kernels.execution.model.ExecutionId execId =
                new com.shreeai.os.platform.kernels.execution.model.ExecutionId(request.requestId());
        // Build the base parameters from V1 metadata first (e.g. intelligenceContext,
        // sessionId).  After that, layer the canonical fields on top so they
        // survive the parameters(Map) call — addMetadata() mutates the already-set
        // map rather than creating a new one.
        java.util.HashMap<String, Object> baseParams = request.metadata() != null
                ? new java.util.HashMap<>(request.metadata())
                : new java.util.HashMap<>();
        return com.shreeai.os.platform.kernels.execution.model.ExecutionRequest.builder()
                .executionId(execId)
                .requestType(request.requestType())
                .parameters(baseParams)
                .addMetadata("payload", request.payload())
                .addMetadata("userInput", request.payload())
                .addMetadata("context", request.context())
                .build();
    }
}

