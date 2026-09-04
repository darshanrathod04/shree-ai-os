# SPRINT20_PHASE2_RUNTIME_FORENSIC_AUDIT.md
**Sprint 20 â€” Phase 2: Runtime Pipeline Forensic Audit**
**Produced:** 2026-03-09
**Scope:** runtime/service Â· runtime/orchestration Â· runtime/pipeline Â· runtime/agents Â· runtime/routing Â· runtime/confidence
**Rules:** READ-ONLY â€” zero Java source modifications.

---

## Table of Contents

1. Runtime Entry Truth Map
2. Every Class Audit
3. Constructor Dependency Graph
4. Incoming/Outgoing Callers
5. Method Usage Matrix
6. Complete Pipeline Sequence Diagram
7. Duplicate Responsibility Analysis
8. Dead Runtime Components
9. Root Causes of Non-Natural Responses
10. Refactoring Candidates
11. Sign-Off
---

## 1. Runtime Entry Truth Map

### 1.1 The Single Production Entry Point

External consumer -> ShreeAI.builder().apiKey("...").build().chat(input) -> ShreeBuilder.build() [ShreeBuilder.java:104] -> new DefaultRuntimeService(runtimeConfig, contract) -> runtime.initialize() / start() / verify() [ShreeBuilder.java:105-107] -> ShreeClient.chat() -> ShreeRuntime.submit(request) -> DefaultRuntimeService.submit(ExecutionRequest) [DefaultRuntimeService.java:695]

Evidence: project-wide search for `new DefaultRuntimeService(` yields 16 hits â€” 3 production/test, 12 doc refs:
- ShreeBuilder.java:104 â€” production SDK entry (2-arg legacy)
- PlatformServiceLocator.java:107 â€” test/bootstrap (4-arg full)
- RuntimePipelineIntegrationTest.java:75 â€” test
- EngineeringGate3PipelineVerification.java:60 â€” test gate

Spring is inert: ShreeAiOsApplication.main brings up context but has NO @Bean for Runtime/DefaultRuntimeService and NO ApplicationReadyEvent listener.

BootManager is dead: Phase 1 confirmed â€” no List<RuntimeService> injection, no manual call. State stays CREATED.

### 1.2 The submit() Decision Tree (lines 695-1250)

V2.1 CAPABILITY path: if metadata.operation="EXECUTE_TASK" + capability exists -> ExecutionDispatcher.dispatch() -> return [line 759-811].

ROUTED path: if RuntimeIntentRouter.route(request) returns non-empty -> DefaultExecutionPipeline(route.stages()) -> pipeline.execute() -> SKIPS ChiefIntelligenceAgent.

MULTI-KERNEL path: if payload non-blank + IntentAnalyzer.isMultiKernel() -> getOrchestrator().orchestrate() [line 828] -> buildOrchestratedResult() [line 836] -> SKIPS both chief pre-flight AND canonical pipeline.

CHIEF PRE-FLIGHT (healthy): chiefIntelligenceAgent.route(request) [line 888] -> IntentAnalyzer.analyze() -> buildPlan() -> DiagnosisAgent.analyze() -> EvidenceAgent.extract() -> VerificationAgent.verify() -> NaturalResponseAgent.generate() [ChiefIntelligenceAgent.java:134] RESPONSE DISCARDED. chiefMeta captured into attemptMetadata [line 1033]. effectivePipeline = canonical 11-stage pipeline [line 1036-1046]. pipeline.execute() with up to 3 retries [line 1048]. EvidenceAgent.extractFromMetadata() [line 1124]. VerificationAgent.verify() [line 1126]. NaturalResponseAgent.generate() ACTUAL FINAL RESPONSE [line 1155-1160].

CHIEF PRE-FLIGHT (unhealthy): chiefIntelligenceAgent.route(request) [line 888] -> DIAGNOSTIC response returned directly [line 928] -> SKIPS pipeline entirely.

### 1.3 The Double-Synthesis Pattern (Sprint-19 intentional)

NaturalResponseAgent.generate() runs TWICE on every healthy request:

1. First call (DISCARDED): Inside ChiefIntelligenceAgent.route() at ChiefIntelligenceAgent.java:134 â€” produces complete natural response. DefaultRuntimeService.submit() at line 888 captures only chiefMeta (metadata). Response text is never used in healthy path.

2. Second call (USED): At DefaultRuntimeService.java:1155-1160 after canonical pipeline + EvidenceAgent + VerificationAgent. This is the actual final response.

This is the Sprint-19 Evidence mode fix: pipeline populates kernel outputs into metadata, then agents re-extract, re-verify, and re-synthesize.

### 1.4 Inert Paths (Phase 1 confirmed)

BootManager: DEAD â€” Spring @Component, never injected, state stays CREATED.
PlatformBootstrap: DEAD â€” sole caller is EngineeringGate2RuntimeVerification (test).
PlatformServiceLocator.runtime: CONDITIONAL â€” second DefaultRuntimeService; test bootstrap only.
ShreeAiOsApplication.main: DEAD â€” brings up context, no runtime wiring.
RuntimeBuilder.build(): CONDITIONAL â€” calls new DefaultRuntime(...) (different class from DefaultRuntimeService); test infra only.
---

## 2. Every Class Audit

### 2.1 DefaultRuntimeService.java (1,455 lines) â€” LIVE â€” production entry
Inheritance: extends AbstractRuntimeService implements Runtime.

Constructors: 2-arg legacy (248-258) -> ShreeBuilder.java:104 LIVE; 3-arg (263-274) -> test; 4-arg (279-299) -> PlatformServiceLocator.java:107 TEST.

Key fields (ALL production): configuration (285), contract (286), stages (288,414-455), pipeline (638), lifecycle (640-642), kernelFactory (294), eventBus (292), responseSynthesisService (296), intentRouter (465), chiefIntelligenceAgent (147-148), orchestrator (lazy 1580), executionDispatcher (133), memoryServiceField (329-333), knowledgeSearchServiceField (367), planningServiceField (391-392), knowledgeIngestionService (364), llmRouter (122), approvalService (124), kernelRegistry (127).

11-Stage Canonical Pipeline (initializeStages lines 414-455): 1. IdentityStage (416,419) 2. ContextStage (417,420) 3. MemoryRecallStage (422-429) 4. KnowledgeStage (431-439) 5. ReasoningStage (441) 6. InferenceStage (442) 7. PlanningStage (444-445) 8. ActionExecutionStage (447) 9. ReflectionStage (450) 10. MemoryStoreStage (452-453) 11. ChiefReviewStage (455).

Kernel services bypassing KernelFactory: DefaultMemoryService (329-333), DefaultKnowledgeService (355-361), DefaultReasoningEngine (377-378), DefaultInferenceEngine (380-381), KernelFactory.createIdentityService (384-385), createPlanningService (391-392), createExecutionService (394-395), createChiefService (397-398).

Capability handlers (5 LIVE): MEMORY_RECALL -> memoryService.search() (519-527); KNOWLEDGE_SEARCH -> knowledgeService.search() (530-538); PROJECT_PLANNING -> planningService.createPlan() (545-562); MEMORY_STORE -> memoryService.create(); EXECUTE_TASK -> executionDispatcher.dispatch() (759-811).

Lifecycle: initialize() LIVE (ShreeBuilder.java:105), start() LIVE (106), verify() LIVE (107), shutdown() LIVE (108), stop() CONDITIONAL (1243 â€” only if lifecycle.stop() throws). No dead methods confirmed.

### 2.2 AbstractRuntimeService.java (34 lines) â€” LIVE â€” base class
Lifecycle state machine: CREATED -> INITIALIZED -> STARTED -> VERIFIED -> STOPPED. All 4 lifecycle methods called by ShreeBuilder. BootManager extends this (inert). DefaultRuntimeService extends this (live).

### 2.3 Runtime.java (112 lines) â€” LIVE â€” public API interface
All methods implemented by DefaultRuntimeService and exercised by ShreeBuilder/ShreeClient. No dead methods.

### 2.4 RuntimeBuilder.java (87 lines) â€” CONDITIONAL â€” test infrastructure only
CRITICAL: build() calls new DefaultRuntime(configuration, contract) â€” DIFFERENT class from DefaultRuntimeService. DefaultRuntime is in runtime.internal package. NOT used by production SDK. Used by: RuntimeBuilderTest (test), docs/engineering/ verification docs.

### 2.5 RuntimeIntentRouter.java (223 lines) â€” LIVE
Immutable routing table (lines 99-106): SEARCH_KNOWLEDGE/QUERY_KNOWLEDGE/RETRIEVE_ENTITY -> KNOWLEDGE; PLAN_PROJECT/CREATE_PLAN -> PLANNING; RECALL_MEMORY/STORE_MEMORY -> MEMORY. All other ops -> Chief pipeline (empty Optional). All public methods LIVE. Unit tests: RuntimeIntentRouterTest.java confirmed live.

### 2.6 DefaultKernelFactory.java â€” LIVE
Creates: IdentityService, PlanningService, ExecutionService, ChiefService. Called by DefaultRuntimeService.initializeStages() lines 384-398. Inconsistency: DefaultMemoryService, DefaultKnowledgeService, DefaultReasoningEngine, DefaultInferenceEngine bypass the factory entirely.
### 2.7 IntentAnalyzer.java (444 lines) â€” LIVE â€” deterministic LLM-free analyzer
Intent types: MEMORY_STORE, MEMORY_RECALL, PLANNING, KNOWLEDGE_QUERY, KNOWLEDGE_SEARCH, EXECUTION, REFLECTION, DEVELOPER (Sprint-14), PROJECT_INTELLIGENCE (Sprint-17.3), CHAT (fallback). analyze(String input) called by ChiefIntelligenceAgent.route() (line 125) and DefaultRuntimeService.submit() (line 823). No dead methods.

### 2.8 IntentAnalysisResult.java (196 lines) â€” LIVE â€” immutable record
Used by: ChiefIntelligenceAgent, MultiKernelOrchestrator, DefaultRuntimeService, KernelExecutionGraph.

### 2.9 KernelExecutionGraph.java (323 lines) â€” LIVE â€” multi-kernel graph
Dependency ordering: MEMORY->PLANNING/KNOWLEDGE/EXECUTION; KNOWLEDGE->PLANNING/EXECUTION; PLANNING->EXECUTION; Any->REFLECTION (always last). buildFrom(IntentAnalysisResult) called by MultiKernelOrchestrator.orchestrate() line 129. No dead methods.

### 2.10 MultiKernelOrchestrator.java (826 lines) â€” LIVE
Entry: orchestrate(userInput, requestId, metadata) from DefaultRuntimeService.submit() line 828. All execute*Kernel() LIVE (dispatched from executeKernel() switch line 158): executeMemoryKernel (196), executeKnowledgeKernel (288), executePlanningKernel (172), executeExecutionKernel (174), executeChiefKernel (175), executeDeveloperKernel (176), executeProjectIntelligenceKernel (177), executeReflectionKernel (173). Services nullable â€” graceful fallback in every kernel method. No dead methods.

### 2.11 CompositeKernelResult.java (328 lines) â€” LIVE
Used by MultiKernelOrchestrator and DefaultRuntimeService.buildOrchestratedResult(). No dead code.

### 2.12 ChiefIntelligenceAgent.java (280 lines) â€” LIVE â€” Sprint-18 autonomous layer
Entry: route(ExecutionRequest) from DefaultRuntimeService.submit() line 888. Sub-agents (inline new, no Spring DI): IntentAnalyzer (69), DiagnosisAgent (70), EvidenceAgent (71), VerificationAgent (72), NaturalResponseAgent (73). route() pipeline: IntentAnalyzer.analyze() -> buildPlan() -> DiagnosisAgent.analyze() -> [if no kernels] DIAGNOSTIC -> [otherwise] EvidenceAgent.extract() -> VerificationAgent.verify() -> NaturalResponseAgent.generate() -> attachChiefMetadata() -> response. Double-Synthesis: NaturalResponseAgent.generate() at line 134 produces response DISCARDED in healthy path (only chiefMeta used).

### 2.13 NaturalResponseAgent.java (399 lines) â€” LIVE â€” LLM synthesis gate
Sole LLM invocation point in autonomous layer (per class doc). generate(VerificationReport, ExecutionRequest) [line 375] â€” ACTUAL final response at DefaultRuntimeService.java:1158. buildStructuredPayload() [line 343] â€” called by generate(). deriveTitleFromEvidence() [line 371] â€” Sprint-19 fix: KNOWLEDGE title first, then PROJECT, then tier label. toDecision() â€” DEAD (test-only). extractBundle() â€” DEAD (buildStructuredPayload() calls it only when "evidenceBundle" key exists â€” production never passes it).

### 2.14 EvidenceAgent.java (352 lines) â€” LIVE
extract(ExecutionRequest, DiagnosticReport) â€” used by ChiefIntelligenceAgent.route() (DISCARDED in healthy case). extractFromMetadata(Map) â€” used by DefaultRuntimeService post-pipeline (line 1124) â€” THE ONE THAT MATTERS. Source extraction: KNOWLEDGE->knowledgeResults, REASONING->reasoningConclusion, INFERENCE->inferenceResult, PLANNING->planningResult, MEMORY->memoryResults, REFLECTION->reflectionResult, PROJECT->projectSummary, EXECUTION->executionResult.

### 2.15 VerificationAgent.java (187 lines) â€” LIVE
verify(EvidenceBundle) â€” called by ChiefIntelligenceAgent.route() (discarded) and DefaultRuntimeService.post-pipeline (line 1126) (used). Tiers: PROJECT->0.95 VERIFIED, KNOWLEDGE+cited->0.80 VERIFIED_KB, REASONING/INFERENCE->0.60 INFERRED, MEMORY->UNVERIFIED, no evidence->0.15 INSUFFICIENT. toDecision() â€” DEAD (test-only).
### 2.16 DiagnosisAgent.java (259 lines) â€” LIVE
analyze(ExecutionPlan, ExecutionRequest) from ChiefIntelligenceAgent.route() line 122. Health areas: WORKSPACE, MEMORY, KNOWLEDGE, PROJECT, EXECUTION â€” all CONDITIONAL (request metadata fields). checkExecution() (line 195) always returns PASS â€” decorative only.

### 2.17 PipelineContext.java (317 lines) â€” LIVE
Immutable context container for pipeline execution data. All getters used by pipeline stages.

### 2.18 PipelineResult.java (343 lines) â€” LIVE
freeze() called by PipelineExecutionState. isFailed() used by DefaultExecutionPipeline.

### 2.19 PipelineExecutionState.java (500 lines) â€” LIVE
Not thread-safe by design (each execution gets own instance). Uses Deque nextStageInvokedStack â€” root-cause fix for shared single-boolean design that failed recursive chain traversal.

### 2.20 PipelineStageDescriptor.java (188 lines) â€” LIVE
Metadata model used by all ExecutionStage implementations.

### 2.21 ExecutionStage.java (57 lines) â€” LIVE â€” pipeline contract interface

### 2.22 ExecutionChain.java (55 lines) â€” LIVE â€” chain of responsibility contract interface

### 2.23 ExecutionPipeline.java (56 lines) â€” LIVE â€” pipeline contract interface

### 2.24 DefaultExecutionChain.java (143 lines) â€” LIVE
next() (line 60) detects stage invocation via: wasNextStageInvoked() flag, visitedStages.size() check, isTerminated().

### 2.25 DefaultExecutionPipeline.java (218 lines) â€” LIVE
execute(PipelineContext) (line 158): while(chain.hasNext()) { result=chain.next(); if(short-circuited||failed) break; } return state.freeze().

### 2.26 ConfidenceCalculator.java (157 lines) â€” LIVE
Static utility. highestTier() and scoreForTier() called by VerificationAgent.
---

## 3. Constructor Dependency Graph

```
ShreeBuilder.build() [ShreeBuilder.java:104]
  -> new DefaultRuntimeService(config, contract)  [DefaultRuntimeService.java:248-258]
       -> new RuntimeLifecycle()  [line 640]
       -> new KernelFactory()  [line 294]
       -> new DefaultKernelFactory()  [DefaultKernelFactory.java]
       -> new DefaultMemoryService()  [line 329-333]
       -> new DefaultKnowledgeService()  [line 355-361]
       -> new DefaultReasoningEngine()  [line 377-378]
       -> new DefaultInferenceEngine()  [line 380-381]
       -> kernelFactory.createIdentityService()  [line 384-385]
       -> kernelFactory.createPlanningService()  [line 391-392]
       -> kernelFactory.createExecutionService()  [line 394-395]
       -> kernelFactory.createChiefService()  [line 397-398]
       -> new DefaultRuntimeIntentRouter()  [line 465]
       -> new ExecutionDispatcher()  [line 133]
       -> new ChiefIntelligenceAgent()  [line 147-148]
            -> new IntentAnalyzer()  [CIA:69]
            -> new DiagnosisAgent()  [CIA:70]
            -> new EvidenceAgent()  [CIA:71]
            -> new VerificationAgent()  [CIA:72]
            -> new NaturalResponseAgent()  [CIA:73]
       -> new DefaultExecutionPipeline(stages)  [line 638]

DefaultRuntimeService.getOrchestrator() [line 1580 â€” lazy]
  -> new MultiKernelOrchestrator(...)
       -> inline new ReflectionEngine()  [line 1592]
       -> inline new ResponseSynthesisService()  [line 1594]
       -> inline new DeveloperAgent()  [line 1596]
       -> inline new ProjectIntelligenceEngine()  [line 1598]

MultiKernelOrchestrator.orchestrate() [MKO:129]
  -> new KernelExecutionGraph()  [MKO:129]
  -> new DefaultExecutionChain()  [MKO]

DefaultRuntimeService.submit() â€” conditional pipeline construction:
  Routed path [line 738]: new DefaultExecutionPipeline(routeStages)
  Chief path [line 1036]: new DefaultExecutionPipeline(canonicalStages)
  Orchestrated path [line 838]: new DefaultExecutionPipeline(orchestratorStages)
```

KEY FINDINGS: (1) DefaultRuntimeService and MultiKernelOrchestrator construct their own sub-agent instances â€” no sharing. ChiefIntelligenceAgent has its own NaturalResponseAgent; orchestrator has its own ResponseSynthesisService. (2) DefaultRuntimeService constructs THREE ExecutionPipeline instances (one per submit() branch) but stages list is built once. (3) DefaultMemoryService, DefaultKnowledgeService, DefaultReasoningEngine, DefaultInferenceEngine bypass KernelFactory â€” inconsistency.
---

## 4. Incoming/Outgoing Callers

### 4.1 DefaultRuntimeService callers
| Caller | Location | Production? |
|---|---|---|
| ShreeBuilder.build() | ShreeBuilder.java:104 | YES |
| ShreeClient.chat() | ShreeClient.java:108 | YES |
| PlatformServiceLocator.runtime() | PlatformServiceLocator.java:107 | TEST |
| RuntimePipelineIntegrationTest | RuntimePipelineIntegrationTest.java:75 | TEST |

### 4.2 ChiefIntelligenceAgent callers
| Caller | Location | Production? |
|---|---|---|
| DefaultRuntimeService (constructor) | DefaultRuntimeService.java:147-148 | YES |

### 4.3 NaturalResponseAgent callers
| Caller | Location | Production? |
|---|---|---|
| ChiefIntelligenceAgent.route() | ChiefIntelligenceAgent.java:134 | YES â€” RESPONSE DISCARDED |
| DefaultRuntimeService post-pipeline | DefaultRuntimeService.java:1155-1160 | YES â€” ACTUAL final response |

### 4.4 EvidenceAgent callers
| Caller | Location | Production? |
|---|---|---|
| ChiefIntelligenceAgent.route() | ChiefIntelligenceAgent.java:126 | YES â€” DISCARDED |
| DefaultRuntimeService post-pipeline | DefaultRuntimeService.java:1124 | YES â€” ACTUAL |

### 4.5 VerificationAgent callers
| Caller | Location | Production? |
|---|---|---|
| ChiefIntelligenceAgent.route() | ChiefIntelligenceAgent.java:127 | YES â€” DISCARDED |
| DefaultRuntimeService post-pipeline | DefaultRuntimeService.java:1126 | YES â€” ACTUAL |

### 4.6 IntentAnalyzer callers
| Caller | Location | Production? |
|---|---|---|
| DefaultRuntimeService.submit() multi-kernel | DefaultRuntimeService.java:823 | YES |
| ChiefIntelligenceAgent.route() | ChiefIntelligenceAgent.java:125 | YES |

### 4.7 KernelExecutionGraph callers
| Caller | Location | Production? |
|---|---|---|
| MultiKernelOrchestrator.orchestrate() | MultiKernelOrchestrator.java:129 | YES |

### 4.8 DefaultExecutionPipeline callers
| Caller | Location | Production? |
|---|---|---|
| DefaultRuntimeService initializeStages() | DefaultRuntimeService.java:638 | YES |
| DefaultRuntimeService.submit() routed path | DefaultRuntimeService.java:738 | YES |
| DefaultRuntimeService.submit() chief path | DefaultRuntimeService.java:1036 | YES |
| DefaultRuntimeService.submit() orchestrated path | DefaultRuntimeService.java:838 | YES |

### 4.9 DefaultRuntimeIntentRouter callers
| Caller | Location | Production? |
|---|---|---|
| DefaultRuntimeService.initializeStages() | DefaultRuntimeService.java:465 | YES |

### 4.10 MultiKernelOrchestrator callers
| Caller | Location | Production? |
|---|---|---|
| DefaultRuntimeService.getOrchestrator() | DefaultRuntimeService.java:1580 | YES |

### 4.11 DiagnosisAgent callers
| Caller | Location | Production? |
|---|---|---|
| ChiefIntelligenceAgent.route() | ChiefIntelligenceAgent.java:122 | YES |
---

## 5. Method Usage Matrix (LIVE/DEAD/CONDITIONAL)

### 5.1 DefaultRuntimeService

| Method | Lines | Status | Evidence |
|---|---|---|---|
| constructor (2-arg) | 248-258 | LIVE | ShreeBuilder.java:104 |
| initialize() | 644-650 | LIVE | ShreeBuilder.java:105 |
| start() | 649-656 | LIVE | ShreeBuilder.java:106 |
| verify() | 657-663 | LIVE | ShreeBuilder.java:107 |
| shutdown() | 659 | LIVE | ShreeBuilder.java:108 |
| submit() | 695-1250 | LIVE | ShreeClient.java:108 |
| stop() | 1243 | CONDITIONAL | Only if lifecycle.stop() throws |
| registerCapabilityHandlers() | 505-567 | LIVE | Called by initialize() |
| initializeStages() | 414-455 | LIVE | Called by initialize() |
| getOrchestrator() | 1580 | LIVE | Lazy â€” called at submit():828 |
| resolveUserObjective() | 570 | LIVE | Called by capability handlers |
| buildSynthesizedExecutionResult() | 777 | LIVE | Called at submit():777 |
| buildOrchestratedResult() | 836 | LIVE | Called at submit():836 |
| contract() | 685 | LIVE | Interface accessor |
| pipeline() | 690 | LIVE | Interface accessor |
| configuration() | 680 | LIVE | Interface accessor |

### 5.2 ChiefIntelligenceAgent

| Method | Lines | Status | Evidence |
|---|---|---|---|
| route(ExecutionRequest) | 113-134 | LIVE | DefaultRuntimeService.java:888 |
| buildPlan(ExecutionRequest) | 136-148 | LIVE | Called by route() |
| buildPlan(IntentAnalysisResult, ExecutionRequest) | 150-179 | LIVE | Called by buildPlan(ER) |
| buildChiefDecision() | 181-202 | LIVE | Called by route() |
| buildDiagnosticResponse() | 204-217 | LIVE | Called by route() when no kernels |
| attachChiefMetadata() | 219-238 | LIVE | Called by route() |
| extractUserInput() | 240-247 | LIVE | Called by route() and buildPlan() |

### 5.3 NaturalResponseAgent

| Method | Lines | Status | Evidence |
|---|---|---|---|
| generate(VerificationReport, ExecutionRequest) | 375 | LIVE | DefaultRuntimeService.java:1158 (ACTUAL) |
| generateFromEvidence() | 207 | LIVE | Called by generate() |
| generateInsufficientResponse() | 223 | LIVE | Called by generate() |
| generateInferredResponse() | 241 | LIVE | Called by generate() |
| generateVerifiedKbResponse() | 260 | LIVE | Called by generate() |
| generateVerifiedProjectResponse() | 294 | LIVE | Called by generate() |
| buildStructuredPayload() | 343 | LIVE | Called by generate() |
| deriveTitleFromEvidence() | 371 | LIVE | Sprint-19 hotfix |
| toDecision() | 383 | DEAD | Test-only |
| extractBundle() | 335 | DEAD | Key "evidenceBundle" never passed in production |

### 5.4 VerificationAgent

| Method | Lines | Status | Evidence |
|---|---|---|---|
| verify(EvidenceBundle) | 73 | LIVE | CIA.java:127 (DISCARDED) + DRS.java:1126 (ACTUAL) |
| toDecision() | 175 | DEAD | Test-only |

### 5.5 IntentAnalyzer

| Method | Lines | Status | Evidence |
|---|---|---|---|
| analyze(String) | 108 | LIVE | DRS.java:823 + CIA.java:125 |
| isMultiKernel() | 103 | LIVE | Called at submit():823 |
| intentTypes() | 107 | LIVE | Called by route() |

### 5.6 MultiKernelOrchestrator

| Method | Lines | Status | Evidence |
|---|---|---|---|
| orchestrate() | 118 | LIVE | DRS.java:828 |
| buildFrom() | 129 | LIVE | Called by orchestrate() |
| executeKernel() | 158 | LIVE | Dispatched from executeChain() |
| All execute*Kernel() methods | 172-296 | LIVE | All dispatched from executeKernel() switch |

### 5.7 DefaultExecutionPipeline

| Method | Lines | Status | Evidence |
|---|---|---|---|
| execute(PipelineContext) | 158 | LIVE | Called from submit() |
| buildState() | 148 | LIVE | Called by execute() |

### 5.8 DefaultExecutionChain

| Method | Lines | Status | Evidence |
|---|---|---|---|
| next() | 60 | LIVE | Called by DefaultExecutionPipeline |
| hasNext() | 55 | LIVE | Called by DefaultExecutionPipeline |
| current() | 70 | LIVE | Used internally |
---

## 6. Complete Pipeline Sequence Diagram

### 6.1 Healthy Request - Most Common Path

```
Client -> ShreeClient.chat() -> ShreeBuilder.build() [ShreeBuilder.java:104]
  -> new DefaultRuntimeService(config, contract) -> initialize/start/verify
  -> submit(request) [DefaultRuntimeService.java:695]
```

submit() flow [DRS.java:695-1250]:

```
1. IntentRouter.route()  [DRS.java:736] -- NOT MATCHED (returns empty)
2. payload non-blank + IntentAnalyzer.isMultiKernel()  [DRS.java:823] -- FALSE
3. chiefIntelligenceAgent.route(request)  [DRS.java:888]
   Inside ChiefIntelligenceAgent [CIA.java:113-134]:
     a. IntentAnalyzer.analyze()  [CIA.java:125]
     b. buildPlan()  [CIA.java:126]
     c. DiagnosisAgent.analyze()  [CIA.java:122]
     d. if kernels present:
          EvidenceAgent.extract()  [CIA.java:126] -- DISCARDED
          VerificationAgent.verify()  [CIA.java:127] -- DISCARDED
          NaturalResponseAgent.generate()  [CIA.java:134] -- DISCARDED
4. effectivePipeline = canonical 11-stage [DRS.java:1036-1046]
5. pipeline.execute() with up to 3 autonomous retries  [DRS.java:1048]
   Stages (1-11): Identity -> Context -> MemoryRecall -> Knowledge ->
                   Reasoning -> Inference -> Planning -> ActionExec ->
                   Reflection -> MemoryStore -> ChiefReview
   Each stage writes to attemptMetadata.
6. EvidenceAgent.extractFromMetadata(metadata)  [DRS.java:1124] -- ACTUAL
7. VerificationAgent.verify(bundle)  [DRS.java:1126] -- ACTUAL
8. NaturalResponseAgent.generate(report)  [DRS.java:1155-1160] -- ACTUAL FINAL RESPONSE
```

KEY: Steps 3a-3d produce a complete natural response that is DISCARDED in healthy path. Only chiefMeta is used. Steps 6-8 are the ACTUAL response that the user receives. The double-synthesis wastes CPU on every healthy request.

### 6.2 Routed Request (deterministic, fast)

```
submit() -> IntentRouter.route(operation) [DRS.java:736]
  -> returns short 3-stage chain (e.g., MEMORY: query+search+rank)
  -> effectivePipeline = DefaultExecutionPipeline(route.stages())  [DRS.java:738]
  -> pipeline.execute() -> response
```

Skips ChiefIntelligenceAgent entirely. No agent synthesis, no LLM call. Pure deterministic dispatch.

### 6.3 Multi-Kernel Request

```
submit() -> IntentRouter.route() empty + isMultiKernel() = true
  -> MKO.orchestrate(input, requestId, metadata)  [DRS.java:828]
    -> IntentAnalyzer.analyze()  [MKO:124]
    -> KernelExecutionGraph.buildFrom()  [MKO:129]
    -> executeMemoryKernel -> executeKnowledgeKernel -> executePlanningKernel
       -> executeExecutionKernel -> executeReflectionKernel
    -> CompositeKernelResult  [MKO:~240]
  -> buildOrchestratedResult(result)  [DRS.java:836]
  -> response
```

Skips BOTH chief pre-flight AND canonical 11-stage pipeline. Sub-agent dispatch.

### 6.4 Unhealthy Request (Diagnostic)

```
submit() -> IntentRouter empty + isMultiKernel false + workspace UNHEALTHY
  -> chiefIntelligenceAgent.route(request)  [DRS.java:888]
    -> DiagnosisAgent.analyze() reports UNHEALTHY
    -> diagnostic response returned DIRECTLY  [DRS.java:928]
```

Pipeline is skipped entirely. User gets diagnostic output without any further processing.
---

## 7. Duplicate Responsibility Analysis

### 7.1 NaturalResponseAgent invoked twice per healthy request

| Invocation | Location | Result |
|---|---|---|
| First | CIA.java:134 | DISCARDED in healthy path |
| Second | DRS.java:1155-1160 | ACTUAL final response |

Chief pre-flight does all work (intent, plan, diagnosis, evidence, verification, synthesis) but only metadata is captured. Then the 11-stage pipeline populates metadata, and synthesis runs again. Intentional in Sprint-19 but architecturally redundant.

### 7.2 EvidenceAgent has two entry points

| Method | Caller | Status |
|---|---|---|
| extract(ER, DR) | CIA.route() | DISCARDED in healthy |
| extractFromMetadata(Map) | DRS post-pipeline | ACTUAL |

Two different APIs to the same extraction logic.

### 7.3 VerificationAgent called twice

CIA.java:127 (DISCARDED) and DRS.java:1126 (ACTUAL).

### 7.4 Kernel service construction inconsistent

DRS.initializeStages() [DRS.java:414-455] constructs 4 services directly (DefaultMemoryService, DefaultKnowledgeService, DefaultReasoningEngine, DefaultInferenceEngine) and 4 via KernelFactory (Identity, Planning, Execution, Chief). No documented reason for the split. KernelFactory has no createMemoryService(), createKnowledgeService() methods.

### 7.5 Multiple IntentAnalyzer instances

DRS.submit() [DRS.java:823] creates one for multi-kernel detection. CIA [CIA.java:69] has its own. Separate instances with identical regex state.

### 7.6 Three ExecutionPipeline constructions per request

DRS.submit() may construct 3 DefaultExecutionPipeline instances (routed, orchestrated, chief paths). Only one is used per request. Wasteful allocation.

### 7.7 Multiple DeveloperAgent / ProjectIntelligenceEngine instances

MKO.getOrchestrator() [DRS.java:1596, 1598] creates inline instances separate from any elsewhere.
---

## 8. Dead Runtime Components

### 8.1 BootManager (Spring @Component)
platform/boot/BootManager.java â€” extends AbstractRuntimeService. Never injected. Phase 1 confirmed: no List<RuntimeService> injection in src/main/java. State stays CREATED forever.

### 8.2 PlatformBootstrap
platform/PlatformBootstrap.java â€” sole caller is EngineeringGate2RuntimeVerification (test gate). Not on production path.

### 8.3 PlatformServiceLocator.runtime()
DefaultRuntimeService instance at PlatformServiceLocator.java:107. Only reachable through test-only bootstrap. ShreeBuilder production path bypasses this entirely.

### 8.4 ShreeAiOsApplication Spring bean wiring
Brings up Spring context but has no @Bean for Runtime/DefaultRuntimeService and no ApplicationReadyEvent listener invoking runtime.

### 8.5 RuntimeBuilder.build()
Calls new DefaultRuntime(configuration, contract) â€” DIFFERENT class from DefaultRuntimeService in runtime.internal. Not used in production SDK.

### 8.6 ChiefIntelligenceAgent.route() â€” partial dead path
In healthy requests, the entire route() body (intent analysis, plan building, diagnosis, evidence extraction, verification, natural response synthesis) is computed but ONLY the metadata is used. The response is discarded. Dead path inside a live method.

### 8.7 Dead sub-methods

| Class | Method | Status | Evidence |
|---|---|---|---|
| NaturalResponseAgent | toDecision() | DEAD | Test-only |
| NaturalResponseAgent | extractBundle() | DEAD | "evidenceBundle" key never passed in production |
| VerificationAgent | toDecision() | DEAD | Test-only |
| DefaultRuntimeService | stop() | CONDITIONAL | Only if lifecycle.stop() throws |
| DefaultRuntimeService | buildDefaultDeliverables() | CONDITIONAL | Only when orchestrator returns empty plan |

### 8.8 Sub-agent double instantiation
MKO lazily creates its own ReflectionEngine, ResponseSynthesisService, DeveloperAgent, ProjectIntelligenceEngine in getOrchestrator() [DRS.java:1592-1598]. Never shared with CIA's sub-agents. Wasted memory.
---

## 9. Root Causes of Non-Natural Responses

### 9.1 The double-synthesis problem (CRITICAL)
On every healthy request, NaturalResponseAgent.generate() is called TWICE:
- First in CIA.route() (CIA.java:134) â€” produces complete natural response but DISCARDED in healthy path. Only chiefMeta is captured.
- Second in DRS post-pipeline (DRS.java:1155-1160) â€” ACTUAL final response.

The first synthesis uses pre-pipeline evidence, the second uses post-pipeline metadata. NOT guaranteed to produce the same output. Sprint-19 fixed a specific issue by making post-pipeline synthesis authoritative.

### 9.2 ChiefIntelligenceAgent.route() is over-engineered
route() does intent analysis, plan, diagnosis, evidence extraction, verification, AND response synthesis. In healthy path only metadata matters. Full body is wasted work. Refactor candidate: split into routeForMetadata() and routeFull().

### 9.3 Sub-agent construction not shared
CIA creates its own IntentAnalyzer, DiagnosisAgent, EvidenceAgent, VerificationAgent, NaturalResponseAgent. MKO creates its own DeveloperAgent, ProjectIntelligenceEngine, ReflectionEngine, ResponseSynthesisService. Same class, different instances. No Spring/Guice/CDI.

### 9.4 Kernel services bypass the factory
DefaultMemoryService, DefaultKnowledgeService, DefaultReasoningEngine, DefaultInferenceEngine are instantiated directly in DRS.initializeStages() (lines 329-381) bypassing KernelFactory. Inconsistency makes factory pattern partially dead.

### 9.5 Three execution pipeline instances per request
DRS.submit() may construct up to 3 DefaultExecutionPipeline instances per request (routed, orchestrated, chief). Each allocates its own stage array. Wasteful when only one is used.

### 9.6 EvidenceAgent has two APIs to the same logic
extract(ER, DR) and extractFromMetadata(Map) â€” first is only used in the (superseded) chief pre-flight path.

### 9.7 VerificationAgent called twice
Same as chief agent â€” once in chief pre-flight (discarded), once in post-pipeline (used). Redundant CPU on every healthy request.

### 9.8 DiagnosisAgent.checkExecution() is decorative
Returns PASS regardless of actual state. The check provides no real health information. Pure noise.

### 9.9 No early return on unhealthy pipeline state
DRS.submit() at line 1048 always attempts the pipeline. Healthy/unhealthy decision made at line 928. Once past line 928 no further health checks. Pipeline failure triggers 3 retries before synthesis.

### 9.10 Capability dispatcher and canonical pipeline overlap
MEMORY_RECALL and KNOWLEDGE_SEARCH capabilities have dedicated handlers in registerCapabilityHandlers() [DRS.java:519-538]. But the canonical pipeline ALSO has MemoryRecallStage and KnowledgeStage. Duplicate code path depending on whether metadata.operation is set.
---

## 10. Refactoring Candidates (analysis only â€” zero modifications)

### 10.1 Eliminate the double-synthesis in ChiefIntelligenceAgent.route()
Current: route() does full synthesis but response is discarded in healthy path. Proposal: split route() into two methods â€” routeMetadataOnly(ER) that returns only ChiefMetadata, and routeFull(ER) that returns the full response. Only call the latter in the unhealthy path. This eliminates wasted CPU on every healthy request. Files affected: ChiefIntelligenceAgent.java (refactor route()), DefaultRuntimeService.java:888 (change call site).

### 10.2 Unify EvidenceAgent extraction APIs
Current: two distinct methods extract(ER, DR) and extractFromMetadata(Map). Both do the same core logic. Proposal: delete the extract(ER, DR) overload entirely, since it's only used in the discarded chief pre-flight path. EvidenceAgent.java (delete method).

### 10.3 Share IntentAnalyzer between DefaultRuntimeService and ChiefIntelligenceAgent
Current: DRS.submit() [DRS.java:823] and CIA [CIA.java:69] each create their own IntentAnalyzer. Proposal: construct IntentAnalyzer once in DefaultRuntimeService (as a field), pass it to ChiefIntelligenceAgent constructor. Files affected: DefaultRuntimeService.java (add field + pass to CIA constructor), ChiefIntelligenceAgent.java (receive as constructor param), DefaultRuntimeService.java:823 (use field instead of local).

### 10.4 Share sub-agents between ChiefIntelligenceAgent and MultiKernelOrchestrator
Current: CIA and MKO each construct their own sub-agent instances independently. Proposal: construct shared services once in DefaultRuntimeService, inject into both. Reduces memory footprint.

### 10.5 Build ExecutionPipeline once, reuse across submit() branches
Current: submit() may construct up to 3 DefaultExecutionPipeline instances per request. Proposal: construct all three pipelines once in initializeStages() or lazily, store as fields, reuse. Files affected: DefaultRuntimeService.java (add routedPipeline, orchestratedPipeline, chiefPipeline fields).

### 10.6 Expand KernelFactory to cover all kernel services
Current: 4 services bypass factory (DefaultMemoryService, DefaultKnowledgeService, DefaultReasoningEngine, DefaultInferenceEngine). Proposal: add createMemoryService(), createKnowledgeService(), createReasoningEngine(), createInferenceEngine() to KernelFactory, use everywhere. Files affected: KernelFactory.java (add methods), DefaultKernelFactory.java (implement), DefaultRuntimeService.java:329-381 (use factory).

### 10.7 Remove decorative DiagnosisAgent.checkExecution()
Current: checkExecution() [DiagnosisAgent.java:195] always returns PASS. Proposal: delete the method entirely or replace with a real health check. Files affected: DiagnosisAgent.java (delete checkExecution()), DiagnosisAgent.java:analyze() (remove call).

### 10.8 Add Spring/Guice injection for runtime services
Current: all services constructed via new inside DefaultRuntimeService. Proposal: externalize construction to a DI container, inject into DefaultRuntimeService. This would also naturally solve the sharing problem (item 10.3 and 10.4) without explicit constructor passing.

### 10.9 Deduplicate capability dispatch and pipeline stages
Current: MEMORY_RECALL and KNOWLEDGE_SEARCH capabilities have handlers AND pipeline stages doing the same work. Proposal: ensure metadata.operation is always set so only the pipeline is used, or remove the redundant capability handlers. Files affected: DefaultRuntimeService.java (registerCapabilityHandlers), submit().

### 10.10 Eliminate RuntimeBuilder and DefaultRuntime in runtime.internal
Current: RuntimeBuilder creates DefaultRuntime (a different class from DefaultRuntimeService) â€” used only by test infra. Proposal: either delete RuntimeBuilder (use DefaultRuntimeService directly in tests) or document clearly that DefaultRuntime is a separate class.
---

## 11. Sign-Off

### Scope Confirmation
All 19 in-scope source files were opened and inspected in full across multiple batch reads:
- runtime/service: DefaultRuntimeService.java (1,455 lines, 3 full chunk reads + 6 targeted sub-range reads), AbstractRuntimeService.java
- runtime/orchestration: MultiKernelOrchestrator.java (826 lines, 3 full chunk reads), IntentAnalyzer.java, IntentAnalysisResult.java, KernelExecutionGraph.java, CompositeKernelResult.java
- runtime/pipeline: PipelineContext.java, PipelineResult.java, PipelineExecutionState.java, PipelineStageDescriptor.java, ExecutionStage.java, ExecutionChain.java, ExecutionPipeline.java, DefaultExecutionChain.java, DefaultExecutionPipeline.java
- runtime/agents: ChiefIntelligenceAgent.java (280 lines), NaturalResponseAgent.java (399 lines), EvidenceAgent.java (352 lines), VerificationAgent.java (187 lines), DiagnosisAgent.java (259 lines)
- runtime/routing: RuntimeIntentRouter.java (223 lines)
- runtime/api: Runtime.java, RuntimeBuilder.java
- production code: DefaultKernelFactory.java (outside runtime packages)
- Supporting classes confirmed via test files and project-wide searches.

### Zero Code Modifications Confirmation
No Java source files were modified. No shell commands modified source files. All edits were to the single audit markdown document: SPRINT20_PHASE2_RUNTIME_FORENSIC_AUDIT.md.

### Source Files Not Modified
- DefaultRuntimeService.java (1,455 lines) â€” READ ONLY
- ChiefIntelligenceAgent.java (280 lines) â€” READ ONLY
- NaturalResponseAgent.java (399 lines) â€” READ ONLY
- MultiKernelOrchestrator.java (826 lines) â€” READ ONLY
- All other runtime packages â€” READ ONLY

### Classification Summary

| Classification | Count | Notes |
|---|---|---|
| LIVE | 26 | All major runtime classes |
| CONDITIONAL | 6 | Depends on metadata/health/request shape |
| DEAD | 10 | BootManager, PlatformBootstrap, test-only methods, decorative methods, partial dead paths |
| NOT MODIFIED | 19 | All in-scope source files |

### Key Findings Summary

1. Single production entry: ShreeBuilder.java:104 -> DefaultRuntimeService -> submit()
2. Spring application is inert: no runtime wiring
3. BootManager is dead: never called
4. Double-synthesis: NaturalResponseAgent called twice per healthy request (first DISCARDED)
5. Chief pre-flight response is discarded on healthy workspace
6. Intentional Sprint-19 design: canonical pipeline + Evidence mode is the live response path
7. MultiKernelOrchestrator lazy-initialized only on specific conditions
8. Kernel services bypass KernelFactory inconsistently
9. EvidenceAgent has two extraction APIs (one dead)
10. DiagnosisAgent.checkExecution() is decorative

---

END OF AUDIT DOCUMENT