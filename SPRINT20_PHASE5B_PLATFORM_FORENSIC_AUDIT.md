# SPRINT20 PHASE 5B — PLATFORM FORENSIC AUDIT
## Remaining Platform Layers: Execution Pipeline, Kernels, SDK, Tenancy, Services

**Date:** Sprint 20, Phase 5B
**Scope:** Execution pipeline stages, kernel implementations, SDK facades, multi-tenancy, Spring wiring, bootstrap, platform services
**Files audited:** 80+ Java source files
**Preceding audit:** `SPRINT20_PHASE5_PLATFORM_FORENSIC_AUDIT.md` (response synthesis layer)

---

## BOTTOM LINE

The runtime execution pipeline is a **fully wired, stage-composed execution engine** that chains 12 stages sequentially. The event bus is a synchronous publish/subscribe mechanism with two production consumers (knowledge ingestion, journal subscriber). Multi-tenancy uses a thread-local store with an isolation enforcer. The SDK is a thin facade layer — all business logic lives in the runtime. The Spring application wires everything through `ShreeAiOsConfig`.

---

## PART I — EXECUTION PIPELINE

### 1.1 Pipeline Architecture

The execution pipeline (`DefaultExecutionPipeline`) runs 12 stages sequentially:

| # | Stage | Kernel | Purpose |
|---|---|---|---|
| 1 | `IdentityStage` | Identity | User/session resolution |
| 2 | `ContextStage` | Context | Conversation context injection |
| 3 | `MemoryRecallStage` | Memory | Episodic memory recall |
| 4 | `KnowledgeStage` | Knowledge | Knowledge graph grounding |
| 5 | `ReasoningStage` | Cognitive | Multi-step reasoning chain |
| 6 | `InferenceStage` | Inference | Hypothesis evaluation |
| 7 | `PlanningStage` | Planning | Execution plan generation |
| 8 | `ActionExecutionStage` | Execution | Capability dispatch |
| 9 | `MemoryStoreStage` | Memory | Memory consolidation |
| 10 | `ReflectionStage` | Cognitive | Post-execution reflection |
| 11 | `ChiefReviewStage` | Chief Intelligence | Chief review and routing |
| 12 | (implicit) | Response Synthesis | Evidence-to-response rendering |

The pipeline uses a `PipelineContext` (defensively copied between stages) to accumulate state. Each stage implements `ExecutionStage` with `process(request, context)` returning a `PipelineResult`.

### 1.2 Stage Contract

Every stage follows the same pattern:
```java
public PipelineResult process(ExecutionRequest request, PipelineContext context) {
    StageResult result = engine.process(request, context);
    enrichContext(context, result);
    return PipelineResult.success(request.requestId(), result);
}
```

**Key invariant:** The pipeline is additive — each stage reads prior state from `PipelineContext` and writes its own results back. No stage deletes or overwrites another stage's output.

### 1.3 Chief Intelligence Agent (CIA) — Pre-flight Router

The `ChiefIntelligenceAgent` runs before the pipeline (at `DefaultRuntimeService.java:865-882`). For every canonical (unrouted) chat request it:
1. Calls `route(request)` to classify into an `ExecutionPlan`
2. Runs `diagnose(workspace)` to check workspace health
3. If unhealthy → short-circuits with a diagnostic response
4. If healthy → enriches `attemptMetadata` with chief decision metadata

The CIA does NOT replace the pipeline. The canonical pipeline still runs when healthy. Its metadata (chief metadata) is injected into `PipelineContext.attemptMetadata` so downstream stages retain observability.

### 1.4 Multi-Kernel Orchestrator

`MultiKernelOrchestrator.orchestrate()` (invoked when intent is `MULTI_KERNEL`) runs `CompositeKernelExecutor.executeParallel()` over the selected kernel subset, then synthesizes a `CompositeKernelResult` from individual `KernelResult` objects.

The orchestrator is used for Path B (workspace long-lived sessions) and also invoked from the canonical chat path when `IntentAnalysisResult.isMultiKernel()` returns true.

---

## PART II — KERNEL IMPLEMENTATIONS

### 2.1 Memory Kernel

**SPI:** `MemoryQueryService`, `MemorySearchService`, `MemoryValidator`
**Engine:** `DefaultMemoryProcessingEngine`, `MemoryRankingService`
**Service:** `DefaultMemoryService` (delegates to engine)

The Memory Kernel handles episodic recall and storage:
- `MemoryValidator` enforces tenant-scoped access (uses `TenantContext.current()`)
- `MemoryRankingService` scores recalled memories by recency, relevance, importance
- In-memory default via `InMemoryMemoryStore`; PostgreSQL adapter via Phase 2
- All repository calls pass `TenantContext.current().tenantId()` as the first parameter

### 2.2 Knowledge Kernel

**SPI:** `KnowledgeQueryService`, `KnowledgeSearchService`, `KnowledgeIngestionService`
**Engine:** `DefaultKnowledgeProcessingEngine`, `KnowledgeGroundingService`, `KnowledgeRankingService`, `DefaultKnowledgeIngestionEngine`
**Service:** `DefaultKnowledgeService`

The Knowledge Kernel provides semantic search and document ingestion:
- Vector store abstraction via `VectorStoreProvider` SPI (defaults: in-memory)
- `EmbeddingProvider` SPI for embedding generation
- `KnowledgeGraphStore` SPI for graph persistence
- Every persisted chunk carries metadata-first schema: `documentId`, `tenantId`, `chunkIndex`, `embeddingVersion`, `title`, `source`

**Event-driven ingestion:** `KnowledgeSDK.ingest()` → `KNOWLEDGE_INGEST_REQUESTED` event → `KnowledgeIngestionEventConsumer` → `KnowledgeIngestionService` → `KNOWLEDGE_INGEST_COMPLETED` event. Synchronous dispatch ensures the document is searchable before the SDK call returns.

### 2.3 Cognitive Kernel (Reasoning + Reflection)

**Reasoning Engine:** `DefaultReasoningEngine`
- Chain-of-thought reasoning with configurable depth
- Extracts reasoning conclusions and citations
- Results stored as `ReasoningResult` with conclusion, chain, citations, confidence

**Reflection Engine:** `DefaultReflectionEngine`
- Phase 1.5 feature: post-execution reflection
- `ReflectionHistory` record: verdict, score, importanceScore, lessons, rootCause
- Stores to `ReflectionRepository` (in-memory default, PostgreSQL in Phase 2)
- `ReflectionMemoryBridge` stores lessons as memories with OBSERVATION type and "reflection" tag
- Publishes `REFLECTION_PERSISTED` event on completion
- `InMemoryReflectionRepository` uses per-tenant `CopyOnWriteArrayList`. All queries are tenant-scoped by default.

### 2.4 Inference Kernel

**Engine:** `DefaultInferenceEngine`
- Evaluates hypotheses against evidence
- Returns ranked hypotheses with confidence scores
- Supports multi-criteria evaluation

### 2.5 Planning Kernel

**Engine:** `DefaultPlanningEngine`
- Generates `PlanBlueprint` with phases, milestones, risks, dependencies
- `PlannerRegistry` SPI for pluggable planners
- Used by `PlanningStage` and `MultiKernelOrchestrator`

### 2.6 Execution Kernel

**Engine:** `DefaultExecutionEngine`
- Dispatches to registered `KernelHandler` implementations via `KernelRegistry`
- 5 capabilities: `PROJECT_PLANNING`, `WORKOUT_PLANNING`, `KNOWLEDGE_SEARCH`, `MEMORY_RECALL`, `TASK_EXECUTION`
- Each capability maps to a `KernelHandler` (functional interface)
- Registry starts empty; populated during bootstrap via `registerCapabilityHandlers()`

### 2.7 Project Intelligence Kernel

**Engine:** `DefaultProjectIntelligenceEngine`
- Analyzes project directory structure (Java class scanning)
- Builds `ProjectSummary` with class count, endpoint count, framework detection
- Tracks `ProjectClass`, `ProjectEndpoint`, `ProjectEntity`, `ProjectImpact`
- Lazily constructed by `MultiKernelOrchestrator` for Path B

**SDK usage pattern:** `ProjectSDK` (public facade) → `DefaultProjectIntelligenceEngine` (implementation). No direct kernel access in application code.

### 2.8 Developer Kernel

**Engines:** `DefaultDeveloperAgentEngine`, `DefaultPatchExecutionEngine`, `DefaultDeveloperWorkflowEngine`
**Models:** `DeveloperRequest`, `DeveloperResult`, `DeveloperExecutionResult`, `PatchPlan`

The Developer Kernel powers autonomous software development:
- `DeveloperWorkflowEngine.build()`: full workflow from instruction to generated artifacts
- `PatchExecutionEngine.apply()`: patch generation → application → validation → rollback plan
- No files written to disk by the engine (pure computation)

---

## PART III — SDK FACADE LAYER

### 3.1 SDK Architecture

The SDK is a **pure facade layer** — zero business logic, all calls delegate to `ShreeClient.chat()` which calls `Runtime.submit()`. This is by design (architectural constraint: application layer never calls kernels directly).

```
Developer Code
    ↓  (constructor-injected by Spring)
ShreeAiOsConfig (@Configuration)
    ↓  (@Bean)
ShreeAI / ProjectSDK / KnowledgeSDK / MemorySDK / ...
    ↓  (package-private ShreeClient)
SDKRequest → Runtime.submit()
    ↓
Pipeline → Stages → Kernels
```

### 3.2 SDK Class Inventory

| SDK Class | Delegates to | Operations |
|---|---|---|
| `IdentitySDK` | `ShreeClient` | `createIdentity`, `getIdentity`, `updateProfile` |
| `MemorySDK` | `ShreeClient` | `search`, `store`, `recall` |
| `KnowledgeSDK` | `ShreeClient` + events | `query`, `retrieve`, `search`, `ingest` |
| `PlanningSDK` | `ShreeClient` | `createPlan`, `refinePlan`, `validatePlan` |
| `ExecutionSDK` | `ShreeClient` | `execute`, `verify` |
| `ReflectionSDK` | `ShreeClient` | `reflect`, `getHistory`, `getAnalytics` |
| `ProjectSDK` | `DefaultProjectIntelligenceEngine` (direct) | `analyze`, `findClass`, `impact`, `build`, `apply` |
| `SettingsSDK` | `ByokSettingsService` | `providers`, `save`, `delete`, `validate` |
| `DiagnosticsSDK` | `SdkDiagnosticsService` | `report`, `reportAsString`, `provider`, `model`, `kernel` |

### 3.3 ProjectSDK — Direct Engine Call

`ProjectSDK` is the exception to the facade rule: it directly instantiates and calls `DefaultProjectIntelligenceEngine` (line 49, 79, 174, 263). This is intentional — the project kernel is a local analysis tool that doesn't need runtime orchestration. All other SDKs go through `ShreeClient`.

### 3.4 Event-Driven Contracts

Two SDK operations use the event bus for deterministic async-in-sync semantics:

**`KnowledgeSDK.ingest()`:**
1. Generate `requestId = UUID.randomUUID()`
2. Register `CompletableFuture` listener for `KNOWLEDGE_INGEST_COMPLETED` with matching `requestId`
3. Subscribe to `KNOWLEDGE_INGEST_COMPLETED` on SDK event bus
4. Publish `KNOWLEDGE_INGEST_REQUESTED` event
5. Wait up to 30 seconds for completion event
6. Unsubscribe listener in `finally` block
7. Return `SDKResponse` with `structuredPayload` containing `documentId`, `chunkCount`, `nodeIds`

**`ReflectionSDK.reflect()`:** Delegates through `ShreeClient` → pipeline → `ReflectionStage`. The `REFLECTION_PERSISTED` event is published by the pipeline stage, not the SDK.

---

## PART IV — EVENT SYSTEM

### 4.1 Event Bus Architecture

Two event bus implementations exist:
- **`sdk.events.RuntimeEventBus`** (SDK package): `ConcurrentHashMap<EventType, List<RuntimeEventListener>>`. Used by SDK for `KnowledgeSDK.ingest()` async pattern.
- **`runtime.studio.journal.JournalSubscriber`**: Subscribes to all pipeline stage events + `KNOWLEDGE_INGEST_*` + `REFLECTION_PERSISTED`. Writes to `ExecutionJournalStore`.

**Synchronous dispatch:** `RuntimeEventBus.publish()` iterates listeners and calls `listener.onEvent(event)` synchronously. No queue, no async dispatch.

### 4.2 Event Types

19 event types defined in `EventType.java`:
- Pipeline lifecycle: `PIPELINE_STARTED`, `PIPELINE_COMPLETED`, `PIPELINE_FAILED`
- Stage completion (9): `IDENTITY_COMPLETED`, `CONTEXT_COMPLETED`, `MEMORY_RECALL_COMPLETED`, `KNOWLEDGE_COMPLETED`, `REASONING_COMPLETED`, `INFERENCE_COMPLETED`, `PLANNING_COMPLETED`, `EXECUTION_COMPLETED`, `MEMORY_STORE_COMPLETED`
- Synthesis: `CHIEF_REVIEW_COMPLETED`, `REFLECTION_COMPLETED`, `REFLECTION_PERSISTED`
- Knowledge ingestion: `KNOWLEDGE_INGEST_REQUESTED`, `KNOWLEDGE_INGEST_COMPLETED`

### 4.3 Event Binding (`bindEventBus`)

`ShreeClient` constructor calls `runtime.bindEventBus(eventBus)` (line 46 of ShreeClient.java). The `Runtime.bindEventBus()` default implementation is a no-op. `DefaultRuntimeService` overrides it (line 488+):

```java
@Override
public void bindEventBus(RuntimeEventBus eventBus) {
    if (eventBus == null) return;
    eventBus.subscribe(
            EventType.KNOWLEDGE_INGEST_REQUESTED,
            new KnowledgeIngestionEventConsumer(
                    () -> knowledgeIngestionService, eventBus));
}
```

This connects the SDK event bus to the runtime knowledge ingestion service, enabling `KnowledgeSDK.ingest()` to work.

---

## PART V — MULTI-TENANCY

### 5.1 Tenant Architecture

```
TenantContext (ThreadLocal record)
    ↑
DefaultTenantResolver.resolveContext()
    ↑
TenantIsolationEnforcer.validateAccess(requestedTenantId)
    ↑
[Service layer — on every operation]
```

**Three components:**
1. `TenantContext` — immutable record `(tenantId, organizationId)`, backed by `ThreadLocal<TenantContext>`. Provides `setCurrent()`, `current()`, `currentOptional()`, `clear()`.
2. `TenantResolver` — SPI interface. `DefaultTenantResolver` delegates to `TenantContext.current()`.
3. `TenantIsolationEnforcer` — validates `requestedTenantId == currentTenant`, throws `TenantIsolationException` on mismatch.

### 5.2 Thread Isolation

`TenantContext` uses a `ThreadLocal` store. The test at `TenantIsolationTest.java:72` confirms each thread has its own tenant:

```java
Thread worker = new Thread(() -> otherThreadTenant.set(TenantContext.current().tenantId()));
worker.start();
worker.join();
// main thread still has its own tenant
assertEquals("main-tenant", TenantContext.current().tenantId());
assertEquals("system", otherThreadTenant.get());
```

### 5.3 Multi-Tenant Data Isolation

All per-tenant data stores scope by `TenantContext.current().tenantId()`:
- `InMemoryReflectionRepository`: per-tenant `CopyOnWriteArrayList`
- `InMemoryMemoryStore`: tenant-scoped keys
- Knowledge kernel: `tenantId` in every `VectorRecord` metadata
- Cache keys: prefixed with `tenantId:`

**Note:** Enforcement is service-level (each service calls `TenantIsolationEnforcer.validateAccess()` before operations). There is no platform-wide AOP enforcement.

### 5.4 Tenant Fallback

When no tenant is set, `TenantContext.current()` falls back to `system()` (default tenantId="system", organizationId="system"). `TenantIsolationEnforcer` also falls back to "system" if `resolveTenantId()` returns empty.

---

## PART VI — RUNTIME LIFECYCLE

### 6.1 State Machine

```
INITIALIZING → READY → ACTIVE → DRAINING → STOPPED
                 ↑        ↓
                 └─── IDLE ──┘

Any state → FAILED
```

Valid transitions:
- `start()`: INITIALIZING → READY
- `stop()`: READY/ACTIVE/IDLE → DRAINING → STOPPED (two-step)
- `shutdown()`: any non-STOPPED/FAILED → STOPPED
- `fail(cause)`: any → FAILED

`isAcceptingRequests()` returns true only for READY or IDLE.

### 6.2 Runtime Initialization

`DefaultRuntimeService.initialize()` calls:
1. `initializeKernelFactory()` — creates `DefaultKernelFactory`
2. `initializeProviders()` — sets up embedding and vector store providers
3. `initializeServices()` — creates all kernel services (memory, knowledge, cognitive, etc.)
4. `initializeStages()` — wires all 11 pipeline stages
5. `registerCapabilityHandlers()` — populates `KernelRegistry` with 5 capability handlers

The runtime transitions from CREATED → INITIALIZED → VERIFIED → STARTED.

---

## PART VII — SPRING WIRING (APPLICATION LAYER)

### 7.1 Bean Graph

`ShreeAiOsConfig` (`application/shree-developer-intelligence`) wires two SDK beans:

```java
@Bean public ShreeAI shreeAi() { ... }      // Used by AiChatService
@Bean public ProjectSDK projectSdk() { ... } // Used by WorkspaceService, DeveloperWorkflowService
```

These are injected via constructor into:
- `WorkspaceService(ProjectSDK)` — open/close/manage project sessions
- `AiChatService(ShreeAI, ProjectSDK)` — natural language chat grounded in project
- `DeveloperWorkflowService(ProjectSDK)` — autonomous developer workflow

**Rule:** Application layer never instantiates SDK classes outside `ShreeAiOsConfig`. All SDK usage is via constructor injection.

### 7.2 Workspace Session Management

`WorkspaceService` maintains an in-memory `ConcurrentHashMap<String, WorkspaceSession>` of all open project sessions. Each `WorkspaceSession` carries:
- `id` (UUID), `projectPath`, `projectName`, `summary` (from `ProjectSDK.analyze()`)
- `lastImpactTarget`, `openedAt`, `history`

Sessions are NOT persisted. They exist for the lifetime of the application run.

---

## PART VIII — PLATFORM SERVICES

### 8.1 BYOK Settings Service

`ByokSettingsService` manages LLM provider API keys:
- Validates keys before saving (min length 8, endpoint required for Ollama)
- Masks keys on save (`"sk-abc123xyz"` → `"sk-****xyz"`)
- Raw keys never returned from any getter
- In-memory `ConcurrentHashMap<ProviderType, ProviderSettings>`

### 8.2 SDK Diagnostics Service

`SdkDiagnosticsService` aggregates runtime diagnostics:
- `provider`, `model`, `activeKernel` (9 kernel types), `latencyMs`, `knowledgeHits`, `memoryUsed`, `routingSource`
- Fluent setter API for chaining configuration
- `startTimer()` returns a `Runnable` that records elapsed time

---

## PART IX — CROSSCUTTING FINDINGS

### X1: Knowledge Ingestion Uses Hardcoded "default" Tenant (LOW)

`KnowledgeSDK.ingest()` line 131:
```java
requestMetadata.put("tenantId", "default");
```

The tenant ID is hardcoded as "default" rather than reading from `TenantContext.current().tenantId()`. All ingested documents are attributed to the "default" tenant regardless of the actual caller.

**Impact:** Low — only affects knowledge ingestion. Memory and reflection use `TenantContext.current()`. Should be fixed to use the current tenant context.

### X2: ProjectSDK Bypasses Runtime Pipeline (LOW)

`ProjectSDK` directly instantiates `DefaultProjectIntelligenceEngine` rather than going through `Runtime.submit()`. This means:
- No pipeline stages run for project analysis
- No event publishing
- No Chief Intelligence Agent routing
- No reflection

**Impact:** Low — intentional for local analysis performance. But project analysis results are invisible to the studio/journal system.

### X3: Workspace Sessions Not Visible to Chat Path (MEDIUM)

`WorkspaceService` and `AiChatService` share the same `ProjectSDK` instance but not the analyzed project state:
- `AiChatService.ask()` calls `ProjectSDK.analyze()` on every chat request
- No `IntelligenceContext` is built with project evidence in the chat path
- The `sessionId` parameter in `AiChatService.ask()` is used only for memory keying, not for retrieving the analyzed project

**Impact:** Medium — project context is not carried into chat requests.

### X4: IntelligenceContext Defined But Not Populated (MEDIUM)

`IntelligenceContext` (with `contextId`, `request`, `project`, `evidence`, `memoryReferences`, `knowledgeReferences`, `tenantId`, `organizationId`) is defined in the `intelligence` package but is NOT built or populated in the canonical `DefaultRuntimeService.submit()` path. The structured response does include an `intelligenceContext` field, but it is empty (all fields null or default).

**Impact:** Medium — the intelligence aggregate exists but is never populated. The rich structured context is designed but unimplemented.

### X5: Two KernelRegistry Implementations (LOW)

Two `KernelRegistry` interfaces exist with the same name:
1. `platform.runtime.execution.KernelRegistry` — maps `ExecutionCapability → KernelHandler` (for execution dispatch)
2. `platform.core.registry.api.KernelRegistry<T>` — generic registration authority for kernels (for kernel discovery)

The first is runtime-internal. The second is the platform-wide kernel registry (referenced in architecture docs as `KR-005`).

**Impact:** Low — same name, different packages, no conflict. But potentially confusing for new developers.

### X6: bindEventBus Is Conditionally Executed (LOW)

`bindEventBus` is called from `ShreeClient` constructor only when `runtime != null`. If `runtime` is null (standalone SDK usage), the event bus is never connected. `KnowledgeSDK.ingest()` would timeout waiting for `KNOWLEDGE_INGEST_COMPLETED` if called without a bound runtime.

**Impact:** Low — `finally` block still unsubscribes the listener. The `ingest()` contract fails gracefully with a timeout.

### X7: Two Reflection Paths (LOW)

`ReflectionStage` (pipeline stage, `ReflectionStage.java:308`) and `MultiKernelOrchestrator` (orchestrator, `MultiKernelOrchestrator.java:883`) both publish `REFLECTION_PERSISTED` events. Only one runs per request (pipeline vs orchestrator). No data corruption.

**Impact:** Low — duplicate event types from different architectural paths.

### X8: SDK Error Handling Is Comprehensive (POSITIVE)

`KnowledgeSDK.ingest()` uses try/catch/finally with `CompletableFuture.get(timeout)`:
- `SDKException` — re-thrown
- `TimeoutException` — converted to SDKException with `KNOWLEDGE_FAILED` code
- `InterruptedException` / general `Exception` — wrapped as `UNKNOWN`

`ValidationException` from `sdk.exceptions` is thrown early for null/blank title/content before any network call. This is exemplary defensive programming.

---

## ARCHITECTURE VERDICT

The platform is a **well-structured, layered execution engine** with clear ownership boundaries:

1. **SDK Layer:** Pure facade — delegates to runtime, no business logic
2. **Runtime Layer:** Orchestrates pipeline stages, owns lifecycle, manages sessions
3. **Kernel Layer:** Self-contained execution units with SPI + engine + service pattern
4. **Application Layer:** Uses only SDKs, wired via Spring `@Configuration`
5. **Multi-Tenancy:** Thread-local with service-level enforcement

**Completeness assessment:**

| Layer | Status | Notes |
|---|---|---|
| Execution Pipeline | Complete | 12 stages, CIA pre-flight, orchestrator |
| Memory Kernel | Complete | In-memory + SPI for PostgreSQL |
| Knowledge Kernel | Complete | Vector store + graph + ingestion |
| Cognitive Kernel | Complete | Reasoning + Reflection |
| Inference Kernel | Complete | Hypothesis evaluation |
| Planning Kernel | Complete | Plan generation + registry |
| Execution Kernel | Complete | Capability dispatch |
| Project Kernel | Complete | Local analysis engine |
| Developer Kernel | Complete | Workflow + patch engine |
| SDK Facades | Complete | All 9 facades |
| Event System | Complete | Sync pub/sub, 2 consumers |
| Multi-Tenancy | Partial | Service-level enforcement; hardcoded "default" tenant in ingestion |
| Intelligence Context | Partial | Defined but not populated in canonical path |
| Spring Wiring | Complete | Clean bean graph, constructor injection |

**Remaining gaps (prioritized):**

| Priority | Finding | File | Fix |
|---|---|---|---|
| HIGH | `IntelligenceContext` not populated — workspace/project context lost in chat | `DefaultRuntimeService.submit()` | Build `IntelligenceContext` from pipeline results |
| MEDIUM | Hardcoded "default" tenant in knowledge ingestion | `KnowledgeSDK.ingest():131` | Use `TenantContext.current().tenantId()` |
| LOW | `ProjectSDK` bypasses pipeline — no events, no journal | `ProjectSDK.java` | Route through runtime or document as by-design |
| LOW | Two `KernelRegistry` classes with same name | `runtime.execution`, `core.registry.api` | Rename one or add package disambiguation |
| LOW | `bindEventBus` conditional — ingest times out without runtime | `ShreeClient:46` | Document contract or add SDK-side fallback |
