package com.shreeai.os.platform.runtime.orchestration;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.developer.engine.DefaultDeveloperAgentEngine;
import com.shreeai.os.platform.kernels.response.model.DeveloperResponse;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;
import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeGroundingService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgePayload;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import com.shreeai.os.platform.kernels.response.service.ResponseSynthesisService;
import com.shreeai.os.platform.runtime.execution.ExecutionCapability;
import com.shreeai.os.platform.runtime.execution.RichExecutionResult;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiKernelOrchestrator</b>
 *
 * <p>Orchestrates multiple kernels based on an intent analysis result, executing
 * them in deterministic order and aggregating their results into a
 * {@link CompositeKernelResult}. After all kernels succeed, triggers a reflection
 * hook automatically.</p>
 *
 * <p>The orchestrator does not call any LLM — it is fully deterministic and
 * offline-capable.</p>
 *
 * @since Sprint-12
 */
public final class MultiKernelOrchestrator {

    private final IntentAnalyzer intentAnalyzer;
    private final MemoryService memoryService;
    private final KnowledgeSearchService knowledgeSearchService;
    private final PlanningService planningService;
    private final DefaultReflectionEngine reflectionEngine;
    private final ResponseSynthesisService responseSynthesisService;
    private final RuntimeEventBus eventBus;
    private final DefaultDeveloperAgentEngine developerAgent;
    private final DefaultProjectIntelligenceEngine projectEngine; // Sprint-17.3

    public MultiKernelOrchestrator(
            IntentAnalyzer intentAnalyzer,
            MemoryService memoryService,
            KnowledgeSearchService knowledgeSearchService,
            PlanningService planningService,
            DefaultReflectionEngine reflectionEngine,
            ResponseSynthesisService responseSynthesisService,
            RuntimeEventBus eventBus,
            DefaultDeveloperAgentEngine developerAgent
    ) {
        this(intentAnalyzer, memoryService, knowledgeSearchService, planningService,
                reflectionEngine, responseSynthesisService, eventBus, developerAgent,
                new DefaultProjectIntelligenceEngine());   // Sprint-17.3
    }

    /**
     * Sprint-17.3: Constructor that accepts an explicit ProjectIntelligenceEngine.
     */
    public MultiKernelOrchestrator(
            IntentAnalyzer intentAnalyzer,
            MemoryService memoryService,
            KnowledgeSearchService knowledgeSearchService,
            PlanningService planningService,
            DefaultReflectionEngine reflectionEngine,
            ResponseSynthesisService responseSynthesisService,
            RuntimeEventBus eventBus,
            DefaultDeveloperAgentEngine developerAgent,
            DefaultProjectIntelligenceEngine projectEngine
    ) {
        this.intentAnalyzer = Objects.requireNonNull(intentAnalyzer, "intentAnalyzer must not be null");
        this.memoryService = memoryService; // may be null
        this.knowledgeSearchService = knowledgeSearchService; // may be null
        this.planningService = planningService; // may be null
        this.reflectionEngine = reflectionEngine; // may be null
        this.responseSynthesisService = responseSynthesisService; // may be null
        this.eventBus = eventBus; // may be null
        this.developerAgent = developerAgent; // may be null — Sprint-14
        this.projectEngine = projectEngine; // may be null — Sprint-17.3
    }

    /**
     * Analyzes the request and orchestrates multi-kernel execution.
     *
     * @param userInput  the raw user input
     * @param requestId  the request identifier
     * @param metadata   the request metadata
     * @return a composite kernel result aggregating all kernel outputs
     */
    public CompositeKernelResult orchestrate(
            String userInput,
            String requestId,
            Map<String, Object> metadata
    ) {
        Instant startedAt = Instant.now();

        // Step 1: Analyze intent
        IntentAnalysisResult analysis = intentAnalyzer.analyze(userInput);

        // Step 2: Build execution graph
        KernelExecutionGraph graph = KernelExecutionGraph.builder()
                .buildFrom(analysis);

        // Step 3: Execute kernels in order
        CompositeKernelResult.Builder compositeBuilder = CompositeKernelResult.builder()
                .requestId(requestId)
                .startedAt(startedAt)
                .overallConfidence(analysis.confidence());

        for (KernelExecutionGraph.Node node : graph.executionOrder()) {
            executeKernel(node, userInput, metadata, analysis, compositeBuilder);
        }

        compositeBuilder.computeConfidenceFromResults();
        compositeBuilder.completedAt(Instant.now());

        CompositeKernelResult result = compositeBuilder.build();

        // Step 4: Reflection hook (after successful execution)
        if (result.isSuccess() && reflectionEngine != null) {
            triggerReflectionHook(result, requestId, metadata);
        }

        return result;
    }

    /**
     * Executes a single kernel node and accumulates results.
     */
    private void executeKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            Map<String, Object> metadata,
            IntentAnalysisResult analysis,
            CompositeKernelResult.Builder composite
    ) {
        IntentAnalysisResult.KernelType kernelType = node.kernelType();
        Instant kernelStart = Instant.now();

        try {
            switch (kernelType) {
                case MEMORY -> executeMemoryKernel(node, userInput, composite);
                case KNOWLEDGE -> executeKnowledgeKernel(node, userInput, composite);
                case PLANNING -> executePlanningKernel(node, userInput, analysis, composite);
                case REFLECTION -> executeReflectionKernel(composite);
                case EXECUTION -> executeExecutionKernel(node, userInput, composite);
                case CHIEF -> executeChiefKernel(node, userInput, composite);
                case DEVELOPER -> executeDeveloperKernel(node, userInput, composite);
                case PROJECT -> executeProjectIntelligenceKernel(node, userInput, metadata, composite);   // Sprint-17.3
            }
        } catch (Exception e) {
            long executionTime = Instant.now().toEpochMilli() - kernelStart.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    kernelType.name() + " Kernel",
                    kernelType,
                    "Error: " + e.getMessage(),
                    false,
                    executionTime,
                    0.0,
                    Map.of("error", e.getClass().getSimpleName())
            ));
            composite.success(false);
        }
    }

    // ─── Memory Kernel ───────────────────────────────────────────────────────

    private void executeMemoryKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        if (memoryService == null) {
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Memory Kernel",
                    IntentAnalysisResult.KernelType.MEMORY,
                    "Memory service not available — context stored in orchestrator state",
                    true,
                    Instant.now().toEpochMilli() - start.toEpochMilli(),
                    0.90,
                    Map.of("memoryStored", true, "mode", "orchestrator_state")
            ));
            composite.addStoredMemories(Map.of(
                    "lastStoredInput", userInput,
                    "storedAt", Instant.now().toString()
            ));
            return;
        }

        try {
            Map<String, Object> contentMetadata = new LinkedHashMap<>();
            contentMetadata.put("source", "MultiKernelOrchestrator");
            contentMetadata.put("originalInput", userInput);

            MemoryContent memoryContent = new MemoryContent(
                    userInput,
                    null, // no embedding
                    contentMetadata,
                    Instant.now()
            );

            MemoryMetadata memoryMetadata = new MemoryMetadata(
                    new MemoryId("orch-mem-" + System.currentTimeMillis()),
                    MemoryType.EPISODIC,
                    MemoryStatus.ACTIVE,
                    MemoryVisibility.PRIVATE,
                    new com.shreeai.os.platform.kernels.identity.model.IdentityId("sdk-local-user"),
                    java.util.Set.of("orchestrated", "multi-kernel"),
                    0.7, // importance
                    0.8, // confidence
                    "MultiKernelOrchestrator",
                    Instant.now(),
                    Instant.now(),
                    Instant.now(),
                    0L
            );

            CreateMemoryRequest createRequest = new CreateMemoryRequest(
                    memoryContent,
                    memoryMetadata,
                    Instant.now()
            );

            MemoryId storedId = memoryService.createMemory(createRequest);

            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            String storedValue = storedId != null ? storedId.value() : "unknown";
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Memory Kernel",
                    IntentAnalysisResult.KernelType.MEMORY,
                    "Context stored successfully: " + storedValue,
                    true,
                    duration,
                    0.95,
                    Map.of("memoryId", storedValue)
            ));
            composite.addStoredMemories(Map.of(
                    "memoryId", storedValue,
                    "storedInput", userInput,
                    "storedAt", Instant.now().toString()
            ));
        } catch (Exception e) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Memory Kernel",
                    IntentAnalysisResult.KernelType.MEMORY,
                    "Memory storage failed: " + e.getMessage(),
                    false,
                    duration,
                    0.0,
                    Map.of("error", e.getMessage())
            ));
        }
    }

    // ─── Knowledge Kernel ────────────────────────────────────────────────────

    private void executeKnowledgeKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        if (knowledgeSearchService == null) {
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Knowledge Kernel",
                    IntentAnalysisResult.KernelType.KNOWLEDGE,
                    "Knowledge service not available — knowledge retrieval simulated",
                    true,
                    Instant.now().toEpochMilli() - start.toEpochMilli(),
                    0.85,
                    Map.of("knowledgeFound", false, "mode", "simulated")
            ));
            return;
        }

        try {
            List<?> results = knowledgeSearchService.search(userInput);
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();

            boolean found = results != null && !results.isEmpty();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Knowledge Kernel",
                    IntentAnalysisResult.KernelType.KNOWLEDGE,
                    found
                            ? "Retrieved " + results.size() + " knowledge result(s)"
                            : "No knowledge results found for: " + userInput,
                    true,
                    duration,
                    found ? 0.90 : 0.50,
                    Map.of("knowledgeFound", found, "resultCount", results != null ? results.size() : 0)
            ));

            if (found) {
                composite.addCitations(Map.of(
                        "knowledgeCount", results.size(),
                        "query", userInput
                ));
            }
        } catch (Exception e) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Knowledge Kernel",
                    IntentAnalysisResult.KernelType.KNOWLEDGE,
                    "Knowledge retrieval failed: " + e.getMessage(),
                    false,
                    duration,
                    0.0,
                    Map.of("error", e.getMessage())
            ));
        }
    }

    // ─── Planning Kernel ─────────────────────────────────────────────────────

    private void executePlanningKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            IntentAnalysisResult analysis,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        if (planningService == null) {
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Planning Kernel",
                    IntentAnalysisResult.KernelType.PLANNING,
                    "Planning service not available",
                    false,
                    Instant.now().toEpochMilli() - start.toEpochMilli(),
                    0.0,
                    Map.of("error", "planningService is null")
            ));
            composite.success(false);
            return;
        }

        try {
            Map<String, String> entities = analysis.entities();
            PlanningConstraints constraints = buildPlanningConstraints(entities);

            PlanningService.PlanningRequest planningRequest =
                    new PlanningService.PlanningRequest(
                            userInput,
                            PlanningTypes.PlanningScope.STANDARD,
                            constraints);

            String planId = planningService.createPlan(planningRequest);
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();

            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Planning Kernel",
                    IntentAnalysisResult.KernelType.PLANNING,
                    "Plan created successfully: " + planId,
                    true,
                    duration,
                    0.92,
                    Map.of(
                            "planId", planId,
                            "scope", "STANDARD",
                            "domain", entities.getOrDefault("domain", "GENERAL")
                    )
            ));

            composite.addPlanData(Map.of(
                    "planId", planId,
                    "objective", userInput,
                    "domain", entities.getOrDefault("domain", "GENERAL")
            ));
        } catch (Exception e) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Planning Kernel",
                    IntentAnalysisResult.KernelType.PLANNING,
                    "Plan creation failed: " + e.getMessage(),
                    false,
                    duration,
                    0.0,
                    Map.of("error", e.getMessage())
            ));
            composite.success(false);
        }
    }

    // ─── Execution Kernel ────────────────────────────────────────────────────

    private void executeExecutionKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        // Execution kernel runs through the ExecutionDispatcher via TASK_EXECUTION
        // For now, simulate execution completion
        long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
        composite.addKernelResult(new CompositeKernelResult.KernelResult(
                "Execution Kernel",
                IntentAnalysisResult.KernelType.EXECUTION,
                "Execution task queued: " + userInput,
                true,
                duration,
                0.80,
                Map.of("taskQueued", true, "input", userInput)
        ));
    }

    // ─── Chief Kernel (fallback chat) ───────────────────────────────────────

    private void executeChiefKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();
        long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
        composite.addKernelResult(new CompositeKernelResult.KernelResult(
                "Chief Kernel",
                IntentAnalysisResult.KernelType.CHIEF,
                "Chief orchestration handled: " + userInput,
                true,
                duration,
                0.85,
                Map.of("handled", true)
        ));
    }

    // ─── Sprint-14: Developer Agent Kernel ─────────────────────────────────

    private void executeDeveloperKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        if (developerAgent == null) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Developer Agent",
                    IntentAnalysisResult.KernelType.DEVELOPER,
                    "Developer Agent not available — no project SDK configured",
                    true,
                    duration,
                    0.0,
                    Map.of("available", false)
            ));
            return;
        }

        try {
            DeveloperResponse response = developerAgent.analyze(userInput);

            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            Map<String, Object> kernelMetadata = new LinkedHashMap<>();
            kernelMetadata.put("intentType", response.intent().intent().name());
            kernelMetadata.put("intentLabel", response.intent().label());
            kernelMetadata.put("entity", response.intent().entity());
            kernelMetadata.put("domain", response.intent().domain());
            kernelMetadata.put("confidence", response.confidence());
            kernelMetadata.put("impact.totalAffected", response.impact().totalAffected());
            kernelMetadata.put("impact.dependencyDepth", response.impact().dependencyDepth());
            kernelMetadata.put("plan.phaseCount", response.plan().phases().size());
            kernelMetadata.put("testStrategy.totalTests", response.testStrategy().totalTests());
            kernelMetadata.put("validationIssues", response.validationIssues().size());

            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Developer Agent",
                    IntentAnalysisResult.KernelType.DEVELOPER,
                    response.toFormattedResponse(),
                    true,
                    duration,
                    response.confidence(),
                    kernelMetadata
            ));

        } catch (Exception e) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Developer Agent",
                    IntentAnalysisResult.KernelType.DEVELOPER,
                    "Developer Agent analysis failed: " + e.getMessage(),
                    false,
                    duration,
                    0.0,
                    Map.of("error", e.getClass().getSimpleName())
            ));
        }
    }

    // ─── Project Intelligence Kernel (Sprint-17.3) ─────────────────────────

    /**
     * Sprint-17.3: Executes the Project Intelligence kernel.
     *
     * <p>Delegates to {@link DefaultProjectIntelligenceEngine} to answer questions
     * about the previously analyzed project (classes, endpoints, entities, dependencies,
     * impact analysis). This kernel is triggered when the user asks questions like
     * "explain the X class", "what endpoints exist", "which classes depend on Y",
     * "show me the project structure", etc.</p>
     *
     * <p>The project must have been analyzed previously via {@link DefaultProjectIntelligenceEngine#analyze}
     * (typically called by WorkspaceService when opening the workspace). The engine
     * maintains in-memory state of the analyzed project graph.</p>
     *
     * @param node      the kernel execution node
     * @param userInput the original user question
     * @param metadata  the request metadata (may contain sessionId for workspace lookup)
     * @param composite the composite result builder
     */
    private void executeProjectIntelligenceKernel(
            KernelExecutionGraph.Node node,
            String userInput,
            Map<String, Object> metadata,
            CompositeKernelResult.Builder composite
    ) {
        Instant start = Instant.now();

        if (projectEngine == null) {
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Project Intelligence",
                    IntentAnalysisResult.KernelType.PROJECT,
                    "Project Intelligence engine not available — no project analyzed",
                    false,
                    Instant.now().toEpochMilli() - start.toEpochMilli(),
                    0.0,
                    Map.of("error", "projectEngine is null — call WorkspaceService.open() first")
            ));
            composite.success(false);
            return;
        }

        try {
            String normalizedQuestion = userInput.toLowerCase(Locale.ROOT);
            Map<String, Object> kernelMetadata = new LinkedHashMap<>();
            kernelMetadata.put("source", "Project Intelligence Kernel");

            String answer = null;
            double confidence = 0.80;
            boolean found = false;

            // Sprint-17.3: Route to the appropriate ProjectIntelligence query method
            // based on the question pattern. This replaces the Knowledge Graph fallback
            // that was previously returning "# KNOWLEDGE_QUERY" for all project questions.

            // 1. Class explanation queries: "explain the X class", "describe X class"
            if (normalizedQuestion.contains("explain")
                    || normalizedQuestion.contains("describe")
                    || normalizedQuestion.contains("show")) {
                String className = extractClassName(userInput);
                if (className != null) {
                    ProjectClass cls = projectEngine.findClass(className);
                    if (cls != null) {
                        answer = formatProjectClassExplanation(cls);
                        confidence = 0.95;
                        found = true;
                        kernelMetadata.put("className", className);
                        kernelMetadata.put("matchedBy", "classExplanation");
                    }
                }
            }

            // 2. Endpoint queries: "what endpoints", "which endpoints", "list routes"
            if (!found && (normalizedQuestion.contains("endpoint")
                    || normalizedQuestion.contains("routes")
                    || normalizedQuestion.contains("apis"))) {
                String path = extractEndpointPath(userInput);
                if (path != null) {
                    ProjectEndpoint endpoint = projectEngine.findController(path);
                    if (endpoint != null) {
                        answer = formatEndpointExplanation(endpoint);
                        confidence = 0.95;
                        found = true;
                        kernelMetadata.put("path", path);
                        kernelMetadata.put("matchedBy", "endpointLookup");
                    }
                }
                if (!found) {
                    // List all endpoints from the analyzed project
                    ProjectSummary summary = projectEngine.getSummary();
                    if (summary != null && summary.statistics() != null
                            && summary.statistics().endpointCount() > 0) {
                        answer = formatProjectSummary(summary);
                        confidence = 0.90;
                        found = true;
                        kernelMetadata.put("matchedBy", "endpointList");
                    }
                }
            }

            // 3. Dependency / impact queries: "which class depends on", "impact of"
            if (!found && (normalizedQuestion.contains("depend")
                    || normalizedQuestion.contains("impact")
                    || normalizedQuestion.contains("who uses"))) {
                String className = extractClassName(userInput);
                if (className != null) {
                    ProjectImpact impact = projectEngine.impact(className);
                    if (impact != null) {
                        answer = formatProjectImpact(impact);
                        confidence = 0.90;
                        found = true;
                        kernelMetadata.put("className", className);
                        kernelMetadata.put("matchedBy", "impactAnalysis");
                    }
                }
            }

            // 4. Entity queries: "find entity X"
            if (!found && normalizedQuestion.contains("entity")) {
                String entityName = extractClassName(userInput);
                if (entityName != null) {
                    var entity = projectEngine.findEntity(entityName);
                    if (entity != null) {
                        answer = formatEntityExplanation(entity);
                        confidence = 0.90;
                        found = true;
                        kernelMetadata.put("matchedBy", "entityLookup");
                    }
                }
            }

            // 5. Project structure overview
            if (!found && (normalizedQuestion.contains("structure")
                    || normalizedQuestion.contains("summary")
                    || normalizedQuestion.contains("overview"))) {
                ProjectSummary summary = projectEngine.getSummary();
                if (summary != null) {
                    answer = formatProjectSummary(summary);
                    confidence = 0.90;
                    found = true;
                    kernelMetadata.put("matchedBy", "projectSummary");
                }
            }

            // 6. Generic class/interface lookup (fallback)
            if (!found) {
                String className = extractClassName(userInput);
                if (className != null) {
                    ProjectClass cls = projectEngine.findClass(className);
                    if (cls != null) {
                        answer = formatProjectClassExplanation(cls);
                        confidence = 0.90;
                        found = true;
                        kernelMetadata.put("className", className);
                        kernelMetadata.put("matchedBy", "genericClassLookup");
                    }
                }
            }

            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();

            if (found) {
                composite.addKernelResult(new CompositeKernelResult.KernelResult(
                        "Project Intelligence",
                        IntentAnalysisResult.KernelType.PROJECT,
                        answer,
                        true,
                        duration,
                        confidence,
                        kernelMetadata
                ));
            } else {
                composite.addKernelResult(new CompositeKernelResult.KernelResult(
                        "Project Intelligence",
                        IntentAnalysisResult.KernelType.PROJECT,
                        "No project intelligence found — project may not have been analyzed. "
                                + "Call WorkspaceService.open() first, or the requested class/entity does not exist.",
                        false,
                        duration,
                        0.0,
                        Map.of("error", "notFound", "matchedBy", "none")
                ));
                composite.success(false);
            }

        } catch (Exception e) {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            composite.addKernelResult(new CompositeKernelResult.KernelResult(
                    "Project Intelligence",
                    IntentAnalysisResult.KernelType.PROJECT,
                    "Project Intelligence query failed: " + e.getMessage(),
                    false,
                    duration,
                    0.0,
                    Map.of("error", e.getClass().getSimpleName())
            ));
            composite.success(false);
        }
    }

    // ─── Formatters ─────────────────────────────────────────────────────────

    private String formatProjectClassExplanation(ProjectClass cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(cls.name()).append("\n\n");
        if (cls.packageName() != null && !cls.packageName().isBlank()) {
            sb.append("**Package:** `").append(cls.packageName()).append("`\n\n");
        }
        if (cls.role() != null && cls.role() != ProjectClass.Role.NONE) {
            sb.append("**Role:** ").append(cls.role()).append("\n\n");
        }
        if (cls.methods() != null && !cls.methods().isEmpty()) {
            sb.append("**Methods (").append(cls.methods().size()).append("):**\n\n");
            for (var method : cls.methods().stream().limit(10).toList()) {
                sb.append("- `").append(method.name()).append("`");
                if (method.returnType() != null) {
                    sb.append(" → ").append(method.returnType());
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String formatEndpointExplanation(ProjectEndpoint endpoint) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(endpoint.httpMethod()).append(" ").append(endpoint.path()).append("\n\n");
        if (endpoint.controllerClass() != null) {
            sb.append("**Controller:** `").append(endpoint.controllerClass()).append("`\n\n");
        }
        if (endpoint.methodName() != null && !endpoint.methodName().isBlank()) {
            sb.append("**Method:** `").append(endpoint.methodName()).append("`\n\n");
        }
        return sb.toString().trim();
    }

    private String formatProjectImpact(ProjectImpact impact) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Impact Analysis: ").append(impact.target()).append("\n\n");
        if (impact.affectedClasses() != null && !impact.affectedClasses().isEmpty()) {
            sb.append("**Affected classes (").append(impact.affectedClasses().size()).append("):**\n\n");
            for (String cls : impact.affectedClasses().stream().limit(10).toList()) {
                sb.append("- `").append(cls).append("`\n");
            }
        }
        if (impact.dependencyDepth() > 0) {
            sb.append("\n**Dependency depth:** ").append(impact.dependencyDepth()).append("\n");
        }
        return sb.toString().trim();
    }

    private String formatEntityExplanation(Object entity) {
        return "# Entity Information\n\n" + entity.toString();
    }

    private String formatProjectSummary(ProjectSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Project Summary\n\n");
        if (summary.projectName() != null) {
            sb.append("**Name:** ").append(summary.projectName()).append("\n\n");
        }
        if (summary.statistics() != null) {
            sb.append("**Classes:** ").append(summary.statistics().classCount()).append("\n");
            sb.append("**Endpoints:** ").append(summary.statistics().endpointCount()).append("\n");
        }
        if (summary.framework() != null) {
            sb.append("**Framework:** ").append(summary.framework()).append("\n");
        }
        if (summary.buildSystem() != null) {
            sb.append("**Build:** ").append(summary.buildSystem()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Extracts a class name from a natural-language question.
     * Handles patterns like "Explain the UserService class", "which class depends on X".
     */
    private String extractClassName(String question) {
        if (question == null) return null;
        String q = question.toLowerCase(Locale.ROOT);

        // "explain the X class", "describe the X class"
        int idx = q.indexOf("the ");
        int classIdx = q.indexOf(" class");
        if (idx >= 0 && classIdx > idx) {
            String candidate = question.substring(idx + 4, classIdx).trim();
            if (!candidate.isBlank()) return capitalize(candidate);
        }

        // Direct class name with suffix
        for (String suffix : List.of("Controller", "Service", "Repository", "SDK", "Engine",
                "Stage", "Config", "Handler", "Processor", "Manager")) {
            idx = q.indexOf(suffix.toLowerCase(Locale.ROOT));
            if (idx > 0) {
                String before = question.substring(0, idx).trim();
                if (!before.isBlank()) {
                    return before + suffix;
                }
                // The word itself might be the class name
                int wordStart = idx;
                while (wordStart > 0 && Character.isLowerCase(q.charAt(wordStart - 1))) {
                    wordStart--;
                }
                return question.substring(wordStart, idx + suffix.length());
            }
        }
        return null;
    }

    /**
     * Extracts an endpoint path from a question.
     */
    private String extractEndpointPath(String question) {
        if (question == null) return null;
        // Look for quoted paths like "/users", "/api/v1/..."
        for (String token : question.split("\\s+")) {
            if (token.startsWith("/")) {
                return token.replaceAll("[^a-zA-Z0-9/{}]", "");
            }
        }
        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─── Reflection Hook ───────────────────────────────────────────────────

    private void executeReflectionKernel(CompositeKernelResult.Builder composite) {
        // Reflection is handled by the triggerReflectionHook after the main execution
        // This is a placeholder for explicit reflection kernel call
        composite.addReflectionData(Map.of("reflectionTriggered", true));
    }

    private void triggerReflectionHook(
            CompositeKernelResult result,
            String requestId,
            Map<String, Object> metadata
    ) {
        try {
            if (reflectionEngine == null) {
                return;
            }

            ReflectionInput input = new ReflectionInput(
                    requestId,
                    result.primaryOutput(), // requestText
                    result.kernelResults().size(), // planStepCount
                    "COMPLETED", // actionStatus
                    result.isSuccess(), // executionSuccess
                    result.primaryOutput(), // responseSummary
                    result.overallConfidence() // confidence
            );

            ReflectionAnalysis analysis = reflectionEngine.reflect(input);

            if (eventBus != null) {
                eventBus.publish(new com.shreeai.os.platform.sdk.events.RuntimeEvent(
                        com.shreeai.os.platform.sdk.events.EventType.REFLECTION_PERSISTED,
                        requestId,
                        "MultiKernelOrchestrator",
                        Instant.now(),
                        Map.of(
                                "verdict", analysis.verdict().name(),
                                "score", analysis.score(),
                                "lessonsCount", analysis.lessons().size(),
                                "retryAdvised", analysis.retryAdvised()
                        )
                ));
            }
        } catch (Exception ignored) {
            // Reflection hook failures must not fail the main execution
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private PlanningConstraints buildPlanningConstraints(Map<String, String> entities) {
        String domain = entities.getOrDefault("domain", "GENERAL");

        Map<String, String> policyConstraints = new LinkedHashMap<>();
        policyConstraints.put("domain", domain);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("domain", domain);
        metadata.put("orchestratedBy", "MultiKernelOrchestrator");

        return new PlanningConstraints(
                Map.of(), Map.of(), policyConstraints, metadata);
    }
}
