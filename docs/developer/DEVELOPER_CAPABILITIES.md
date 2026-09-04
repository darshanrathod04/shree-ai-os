# Developer Capabilities — SDK Catalog

> **Every entry below is verified against the Java source.** The "Runtime path" column shows the exact code path from the SDK method to the implementation. If a method is not listed, it was not found in the public SDK surface.

---

## SDK Package

All SDK facades live in: `com.shreeai.os.platform.sdk`

You access them via:
- `ShreeAI.builder().build()` → returns a `ShreeAI` instance
- `shree.memory()`, `shree.knowledge()`, etc. → return SDK facades
- Or inject `ShreeAI` as a Spring bean

---

## 1. MemorySDK

**Package:** `com.shreeai.os.platform.sdk.MemorySDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `store(String key, Object value)` | `void` | `MemorySDK.store()` → `DefaultMemoryService.store(key, value)` |
| `recall(String query)` | `List<MemoryEntry>` | `MemorySDK.recall()` → `DefaultMemoryService.recall(query)` |
| `delete(String key)` | `void` | `MemorySDK.delete()` → `DefaultMemoryService.delete(key)` |
| `clear()` | `void` | `MemorySDK.clear()` → `DefaultMemoryService.clear()` |
| `size()` | `long` | `MemorySDK.size()` → `DefaultMemoryService.size()` |

**Status:** ✅ VERIFIED — `MemorySDK` delegates directly to `DefaultMemoryService`; both `RuntimeIntentRouter` (via `STORE_MEMORY` / `RECALL_MEMORY`) and the 11-stage pipeline (`MemoryRecallStage`, `MemoryStorageStage`) reach it.

**Example:**
```java
shree.memory().store("user-preference", "dark-mode");
List<MemoryEntry> memories = shree.memory().recall("preferences");
```

---

## 2. KnowledgeSDK

**Package:** `com.shreeai.os.platform.sdk.KnowledgeSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `ingest(String content)` | `KnowledgeEntry` | `KnowledgeSDK.ingest()` → `DefaultKnowledgeService.ingest(content, tenantId="default")` |
| `search(String query)` | `List<KnowledgeEntry>` | `KnowledgeSDK.search()` → `DefaultKnowledgeService.search(query)` |
| `getEntity(String entityId)` | `KnowledgeEntry` | `KnowledgeSDK.getEntity()` → `DefaultKnowledgeService.getEntity(entityId)` |
| `getGraph()` | `KnowledgeGraph` | `KnowledgeSDK.getGraph()` → `DefaultKnowledgeService.getGraph()` |

**Status:** ✅ VERIFIED — `KnowledgeSDK` delegates to `DefaultKnowledgeService`. `RuntimeIntentRouter` routes `SEARCH_KNOWLEDGE` / `QUERY_KNOWLEDGE` / `RETRIEVE_ENTITY` to the knowledge kernel. The 11-stage pipeline includes `KnowledgeRetrievalStage`.

**Example:**
```java
KnowledgeEntry entry = shree.knowledge().ingest("Java is a programming language.");
List<KnowledgeEntry> results = shree.knowledge().search("programming");
```

---

## 3. PlanningSDK

**Package:** `com.shreeai.os.platform.sdk.PlanningSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `createPlan(String, String, String)` | `SDKResponse` | `PlanningSDK.createPlan()` → `DefaultPlanningService` (string-routing fallback) |
| `createPlanTyped(String, String, PlanningScope, PlanningConstraints)` | `SDKResponse` | `PlanningSDK.createPlanTyped()` → `Runtime.planningService().createPlan()` → `DefaultPlanningService` |
| `refinePlan(String, String)` | `SDKResponse` | `PlanningSDK.refinePlan()` → `DefaultPlanningService` (string-routing) |
| `refinePlanTyped(String, String, PlanningConstraints)` | `SDKResponse` | `PlanningSDK.refinePlanTyped()` → `Runtime.planningService().refinePlan()` → `DefaultPlanningService` |
| `validatePlan(String)` | `SDKResponse` | `PlanningSDK.validatePlan()` → `DefaultPlanningService` (string-routing) |
| `validatePlanTyped(String, ValidationCriteria)` | `SDKResponse` | `PlanningSDK.validatePlanTyped()` → `Runtime.planningService().validatePlan()` → `DefaultPlanningService` |
| `planningService()` | `PlanningService` | Returns the typed `PlanningService` from the Runtime (or null if no Runtime) |

**Runtime path (Sprint-Release-6):**
```
PlanningSDK → Runtime.planningService()
  → DefaultPlanningService → PlanningProcessingEngine
  → PlanningIntelligenceEngine
```
When a Runtime is available, typed methods delegate directly to the kernel. Falls back to string-routed legacy path otherwise.

**Status:** ✅ VERIFIED — all methods are implemented with typed and legacy paths. See `PlanningSDK.java:114-295` and `DefaultRuntimeService.initializeStages()`.

**Example:**
```java
// Basic plan creation
SDKResponse plan = shree.planning().createPlan("obj-1", "Build a REST API", "project");

// Advanced typed plan creation with constraints
PlanningConstraints constraints = new PlanningConstraints(Map.of(), Map.of(), Map.of(), Map.of());
SDKResponse typed = shree.planning().createPlanTyped(
    "obj-2", "Add JWT auth", PlanningTypes.PlanningScope.SUBTASK, constraints);

// Plan validation with typed criteria
ValidationCriteria criteria = new ValidationCriteria(List.of(), List.of(), List.of());
SDKResponse validated = shree.planning().validatePlanTyped("plan-id-1", criteria);

// Direct access to the Planning Kernel service
PlanningService ps = shree.planning().planningService();
```

---

## 4. ReasoningSDK

**Package:** `com.shreeai.os.platform.sdk.ReasoningSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `reason(String premise)` | `ReasoningResult` | `ReasoningSDK.reason()` → `ReasoningEngine.reason(premise)` |

**Status:** ✅ VERIFIED — `ReasoningSDK` delegates to `ReasoningEngine`. The 11-stage pipeline includes `ReasoningStage`.

**Example:**
```java
ReasoningResult result = shree.reasoning().reason("If all humans are mortal, and Socrates is human...");
```

---

## 5. ReflectionSDK

**Package:** `com.shreeai.os.platform.sdk.ReflectionSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `reflect(String executionId)` | `SDKResponse` | `ReflectionSDK.reflect()` → `Runtime.reflectOnExecution()` → `AdaptiveReflectionEngine` → `DefaultReflectionEngine` |
| `getHistory(String tenantId, int limit)` | `SDKResponse` | `ReflectionSDK.getHistory()` → `Runtime.recentReflections()` → `InMemoryReflectionRepository` |
| `getAnalytics(String tenantId, int window)` | `SDKResponse` | `ReflectionSDK.getAnalytics()` → `Runtime.reflectionStatistics()` → `ReflectionStatistics` |
| `statistics(String tenantId, int window)` | `ReflectionStatistics` | Returns typed `ReflectionStatistics` from Runtime (null if no Runtime) |

**Runtime path (Phase 1.5):**
```
ReflectionSDK.reflect(executionId)
  → Runtime.reflectOnExecution(executionId, ...)
    → AdaptiveReflectionEngine (intelligence.reflection)
      → DefaultReflectionEngine (cognitive engine)
        → ReflectionAnalysis { verdict, score, lessons, importanceScore, memoryWorthy, retryAdvised }
  → SDKResponse with structuredPayload
```
`AdaptiveReflectionEngine` (v3.0) extends `DefaultReflectionEngine` with adaptive calibration: it observes outcome accuracy and tunes retry/memory thresholds dynamically.

**Status:** ✅ VERIFIED — typed path is wired when Runtime is available. See `ReflectionSDK.java:42-222`, `Runtime.java:156-243`, `AdaptiveReflectionEngine.java:1-201`.

**Example:**
```java
// Trigger reflection on a completed execution
SDKResponse reflection = shree.reflection().reflect("exec-abc-123");
Map<String, Object> payload = reflection.structuredPayload();
System.out.println("Verdict: " + payload.get("verdict"));
System.out.println("Score: " + payload.get("score"));

// Get reflection history for a tenant
SDKResponse history = shree.reflection().getHistory("tenant-1", 20);
System.out.println("Records: " + history.structuredPayload().get("count"));

// Get analytics summary
SDKResponse analytics = shree.reflection().getAnalytics("tenant-1", 50);
Map<String, Object> stats = analytics.structuredPayload();
System.out.println("Success rate: " + stats.get("successRate"));

// Direct typed access
ReflectionStatistics stats2 = shree.reflection().statistics("tenant-1", 50);
```

---

## 6. InferenceSDK

**Package:** `com.shreeai.os.platform.sdk.InferenceSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `infer(Object input)` | `InferenceResult` | `InferenceSDK.infer()` → `InferenceEngine.infer(input)` |

**Status:** ✅ VERIFIED — `InferenceSDK` delegates to `InferenceEngine`. The 11-stage pipeline includes `InferenceStage`.

**Example:**
```java
InferenceResult result = shree.inference().infer(someObject);
```

---

## 7. IdentitySDK

**Package:** `com.shreeai.os.platform.sdk.IdentitySDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `resolve(String identityId, String sessionId, String applicationId, String workspaceId)` | `SDKResponse` | `IdentitySDK.resolve()` → `Runtime.resolveIdentity()` → `IdentityService.resolveIdentity()` → `DefaultIdentityProcessingEngine` → `IdentityContext` |
| `createIdentity(String, String, Map)` | `SDKResponse` | `IdentitySDK.createIdentity()` → `Runtime` (legacy string-routing) |
| `getIdentity(String)` | `SDKResponse` | `IdentitySDK.getIdentity()` → `Runtime` (legacy string-routing) |
| `updateProfile(String, Map)` | `SDKResponse` | `IdentitySDK.updateProfile()` → `Runtime` (legacy string-routing) |

**Runtime path (verified):**
```
IdentitySDK.resolve(identityId, sessionId, applicationId, workspaceId)
  → Runtime.resolveIdentity() [DefaultRuntimeService.java:2192-2205]
    → IdentityService.resolveIdentity() [kernels.identity.api.IdentityService]
      → DefaultIdentityProcessingEngine.resolve()
        → IdentityContext { identityId, identityType, sessionId, applicationId, workspaceId, authenticated, resolvedAt }
  → SDKResponse with structuredPayload (`_identitySource: typed-runtime`)
```

**Status:** ✅ VERIFIED — typed `resolve()` is wired directly to the Runtime path. `createIdentity`, `getIdentity`, and `updateProfile` use the legacy string-routing path. See `IdentitySDK.java:47-94`, `Runtime.java:246-272`, `DefaultRuntimeService.java:2192-2205`.

**Example:**
```java
// Resolve an identity with full context
SDKResponse identity = shree.identity().resolve("user-123", "session-abc", "my-app", "workspace-1");
Map<String, Object> ctx = identity.structuredPayload();
System.out.println("Identity: " + ctx.get("identityId"));
System.out.println("Type: " + ctx.get("identityType"));
System.out.println("Authenticated: " + ctx.get("authenticated"));

// Create a new identity (legacy path)
SDKResponse created = shree.identity().createIdentity(
    "user-456", "AGENT", Map.of("role", "developer", "level", "senior"));

// Update profile
SDKResponse updated = shree.identity().updateProfile("user-456",
    Map.of("level", "principal", "team", "platform"));
```

---

## 8. ExecutionSDK

**Package:** `com.shreeai.os.platform.sdk.ExecutionSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `execute(Plan plan)` | `ExecutionResult` | `ExecutionSDK.execute()` → `DefaultExecutionService.execute(plan)` |

**Status:** ✅ VERIFIED — `ExecutionSDK` delegates to `DefaultExecutionService`. The 11-stage pipeline includes `ActionExecutionStage`.

**Example:**
```java
ExecutionResult result = shree.execution().execute(plan);
```

---

## 9. ProjectSDK

**Package:** `com.shreeai.os.platform.sdk.ProjectSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `analyze(Path projectRoot)` | `ProjectAnalysis` | `ProjectSDK.analyze()` → `ProjectIntelligenceService.analyze(projectRoot)` |
| `findClass(String className)` | `JavaClassInfo` | `ProjectSDK.findClass()` → `ProjectIntelligenceService.findClass(className)` |
| `findController(String name)` | `JavaClassInfo` | `ProjectSDK.findController()` → `ProjectIntelligenceService.findController(name)` |
| `findEntity(String name)` | `JavaClassInfo` | `ProjectSDK.findEntity()` → `ProjectIntelligenceService.findEntity(name)` |
| `summarize()` | `ProjectSummary` | `ProjectSDK.summarize()` → `ProjectIntelligenceService.summarize()` |
| `developerAgent()` | `DeveloperAgent` | `ProjectSDK.developerAgent()` → returns a `DeveloperAgent` instance |

**Status:** ✅ VERIFIED — `ProjectSDK` delegates to `ProjectIntelligenceService`. All methods are implemented and reachable.

**Example:**
```java
ProjectAnalysis analysis = shree.project().analyze(Path.of("./my-project"));
JavaClassInfo controller = shree.project().findController("UserController");
ProjectSummary summary = shree.project().summarize();
```

**Note:** `analyze()` takes `java.nio.file.Path`, not `String`. See `QUICKSTART_DEVELOPER_GUIDE.md` for the correct usage.

---

## 10. SettingsSDK (BYOK — Bring Your Own Key)

**Package:** `com.shreeai.os.platform.sdk.SettingsSDK`

| Method | Signature | Runtime Path |
|--------|-----------|--------------|
| `configureApiKey(ProviderType, String apiKey)` | `ProviderSettings` | `SettingsSDK.configureApiKey()` → `ByokSettingsService.save()` → `fireChange()` → `DefaultRuntimeService.rebuildLlmRouter()` |
| `save(ProviderType, String, String)` | `ProviderSettings` | `SettingsSDK.save()` → `ByokSettingsService.save()` |
| `delete(ProviderType)` | `boolean` | `SettingsSDK.delete()` → `ByokSettingsService.delete()` |
| `providers()` | `List<ProviderSettings>` | `SettingsSDK.providers()` → `ByokSettingsService.list()` |
| `provider(ProviderType)` | `Optional<ProviderSettings>` | `SettingsSDK.provider()` → `ByokSettingsService.get()` |
| `validate(ProviderType, String, String)` | `ValidationResult` | `SettingsSDK.validate()` → `ByokSettingsService.validate()` |
| `configureApiKey(ProviderType, String)` | `ProviderSettings` | Hot-reload path: triggers `rebuildLlmRouter()` |

**Hot-reload runtime path (Sprint-release-fix):**
```
SettingsSDK.configureApiKey(OPENAI, "sk-...")
  → ByokSettingsService.save()
    → fireChange()           [ByokSettingsService.java:119]
      → ChangeListener.onSettingsChanged()
        → DefaultRuntimeService.rebuildLlmRouter()   [DRS.java:645-678]
          → materializeProvider(OPENAI) → OpenAiProvider(key)
          → prepend to LLM chain
          → new LlmRouter(mergedChain)
  → LLM routing now uses the BYOK key on the next request
```

The wiring is: `ShreeAI` creates a shared `ByokSettingsService` instance (line 61), `ShreeClient.syncByokSettings(byok)` registers the Runtime listener (line 307), and `DefaultRuntimeService.setByokSettingsService()` subscribes to change events (line 408).

**Status:** ✅ VERIFIED — hot reload is wired and functional. API keys are masked before storage. See `SettingsSDK.java:79-91`, `ByokSettingsService.java:55-73`, `DefaultRuntimeService.java:408-413`.

**Example:**
```java
// Configure a BYOK OpenAI key — takes effect immediately (hot reload)
ProviderSettings openai = shree.settings().configureApiKey(ProviderType.OPENAI, "sk-...");
System.out.println("Key configured: " + openai.maskedKey());  // sk-****xyz

// Validate before saving
ValidationResult valid = shree.settings().validate(
    ProviderType.OLLAMA, "local-key", "http://localhost:11434");
System.out.println("Valid: " + valid.valid());

// List all configured providers
List<ProviderSettings> all = shree.settings().providers();
```

---

## 11. Multi-Agent Orchestration

**Entry point:** `ShreeClient.submit(ExecutionRequest)`

**Runtime path:** `ShreeClient.submit()` → `DefaultRuntimeService.submit()` → `IntentAnalyzer` (detects multi-intent payloads) → `MultiKernelOrchestrator.orchestrate()` (lazy-loaded, DRS lines 1664–1686) → fans out to multiple kernels in parallel → aggregates results.

**Status:** ✅ VERIFIED — `MultiKernelOrchestrator` is instantiated and invoked when the request payload contains multiple intents. The orchestrator coordinates multiple kernel calls and returns an aggregated `ExecutionResult`.

**Example:**
```java
ExecutionRequest request = ExecutionRequest.builder()
    .addIntent("MEMORY_RECALL", Map.of("query", "user preferences"))
    .addIntent("SEARCH_KNOWLEDGE", Map.of("query", "Java"))
    .build();

ExecutionResult result = shree.submit(request);
```

---

## 12. Event Bus

**Interface:** `com.shreeai.os.platform.runtime.event.RuntimeEventBus`

| Method | Signature |
|--------|-----------|
| `publish(RuntimeEvent event)` | `void` |
| `subscribe(EventType<S> type, EventListener<S> listener)` | `<S extends RuntimeEvent> void` |
| `unsubscribe(EventType<?> type, EventListener<?> listener)` | `void` |

**Access:** `shree.eventBus()` returns the `RuntimeEventBus` instance created by `ShreeAI`.

**Status:** ✅ VERIFIED — the bus is created in `ShreeAI`, bound to the runtime via `runtime.bindEventBus(eventBus)`, and internal consumers (e.g., `KnowledgeIngestionEventConsumer`) are registered.

**Example:**
```java
shree.eventBus().subscribe(EventType.KNOWLEDGE_INGESTED, event -> {
    System.out.println("Knowledge ingested: " + event.getEntryId());
});
```

---

## 13. Streaming Chat (Real Provider Token Streaming)

**Entry point:** `ShreeAI.chatStream(String message, StreamingListener listener)` or `ShreeClient.chatStream(...)`

**Runtime path (Sprint-release-fix):**
```
ShreeAI.chatStream(message, listener)
  → ShreeClient.chatStream(message, listener) [ShreeClient.java:209-247]
    → listener.onStart()
    → Runtime.streamText(message)   [DefaultRuntimeService.java:680-698]
      → LlmRequest.builder().stream(true)
      → llmRouter.stream(LlmRequest)   [LlmRouter.java:152-166]
        → for each provider in chain:
          → provider.stream(LlmRequest)
            - OpenAiProvider: SSE `data: {...}` parsing, fragments from `choices[0].delta.content`
            - GeminiProvider: SSE parsing of candidates JSON
            - OllamaProvider: NDJSON parsing
            - InMemoryLlmProvider: deterministic token list
    → for each token: listener.onToken(token)
    → listener.onComplete(complete)
```

The legacy `deliverSimulatedStream` word-splitting path is **only** used when no Runtime is available (test/stub contexts). Production calls always go through real provider streaming.

**Status:** ✅ VERIFIED — true LLM provider token streaming, not a simulation. Each provider's HTTP SSE/NDJSON stream is consumed lazily via a `Spliterator`. See `ShreeClient.java:209-273`, `DefaultRuntimeService.java:680-698`, `LlmProvider.stream()` implementations in each provider class.

**Example:**
```java
shree.chatStream("Tell me a story", new StreamingListener() {
    @Override public void onStart() { System.out.print(">>> "); }
    @Override public void onToken(String token) { System.out.print(token); }
    @Override public void onComplete(String complete) {
        System.out.println("\n[stream complete, " + complete.length() + " chars]");
    }
    @Override public void onError(Throwable t) { t.printStackTrace(); }
});
```

---

## 14. Diagnostics & Health

**Entry point:** `ShreeAI.status()` or `ShreeAI.metrics()`

**Runtime path:** `ShreeAI` exposes runtime status and metrics via `DefaultRuntimeService.getStatus()` and `ProviderHealthService`.

**Status:** ✅ VERIFIED — the runtime reports status (running/stopped), provider health, and basic metrics.

**Example:**
```java
RuntimeStatus status = shree.status();
System.out.println("Runtime state: " + status.getState());
```

---

## 15. Lifecycle Management

**Entry points:** `ShreeAI.start()`, `ShreeAI.stop()`, `ShreeAI.close()`

**Runtime path:** `ShreeAI` delegates to `DefaultRuntimeService.start()` / `stop()`. `close()` is an alias for `stop()`.

**Status:** ✅ VERIFIED — lifecycle methods are implemented and reachable.

**Example:**
```java
ShreeAI shree = ShreeAI.builder().build();
shree.start();
// ... use the runtime ...
shree.stop();
```

---

## Summary Table

| Capability | SDK / Entry Point | Status | Runtime Path |
|------------|-------------------|--------|--------------|
| Memory | `MemorySDK` | ✅ | → `DefaultMemoryService` |
| Knowledge | `KnowledgeSDK` | ✅ | → `DefaultKnowledgeService` |
| Planning (all operations) | `PlanningSDK` | ✅ | → `DefaultPlanningService` |
| Planning (create/refine/validate) | `PlanningSDK` (typed + legacy) | ✅ | → `DefaultPlanningService` |
| Reasoning | `ReasoningSDK` | ✅ | → `ReasoningEngine` |
| Reflection (Phase 1.5) | `ReflectionSDK` | ✅ | → `AdaptiveReflectionEngine` + `DefaultReflectionEngine` |
| Inference | `InferenceSDK` | ✅ | → `InferenceEngine` |
| Identity (typed + legacy) | `IdentitySDK` | ✅ | → `DefaultIdentityService` |
| Execution | `ExecutionSDK` | ✅ | → `DefaultExecutionService` |
| Project Intelligence | `ProjectSDK` | ✅ | → `ProjectIntelligenceService` |
| Settings / BYOK (hot reload) | `SettingsSDK` | ✅ | → `ByokSettingsService` → `rebuildLlmRouter()` |
| Multi-Agent | `ShreeClient.submit()` | ✅ | → `MultiKernelOrchestrator` |
| Event Bus | `ShreeAI.eventBus()` | ✅ | → `RuntimeEventBus` |
| Streaming (real provider tokens) | `ShreeAI.chatStream()` | ✅ | → `llmRouter.stream()` → LLM providers |
| Diagnostics | `ShreeAI.status()` | ✅ | → `DefaultRuntimeService.getStatus()` |
| Lifecycle | `ShreeAI.start()/.stop()` | ✅ | → `DefaultRuntimeService` |

**Legend:**
- ✅ VERIFIED — fully implemented and reachable from the entry point
- ⚠️ PARTIAL — implemented but with limitations (see `WORKING_STATUS.md` for details)

> **Note on this Developer Preview:** All 6 release blockers (BYOK hot reload, real token streaming, Reflection SDK Phase 1.5, Identity SDK typed path, tenant isolation enforcement, advanced Planning SDK) are now fully wired and production-reachable. The only remaining PARTIAL entries are documented in `WORKING_STATUS.md`.

---

*Next: see [WORKING_STATUS.md](WORKING_STATUS.md) for the complete verification report.*
