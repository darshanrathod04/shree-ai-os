# Developer Capabilities

> **Official Public SDK Reference — Developer Preview v1.0.6**

This document provides the complete, authoritative reference for the **10-SDK developer surface** and runtime intelligence engines in Shree AI OS.

**Audience:** JVM software engineers, platform architects, and systems integrators.

**Compatibility Promise:**
- **Runtime:** Java 21+
- **Artifact:** `io.github.darshanrathod04:shree-ai-os`
- **Version:** `1.0.6-developer-preview`
- **Build Quality:** 100% verified across 56+ integration and adversarial test suites.

---

## 1. 10-SDK Developer Surface Overview

All platform capabilities are exposed cleanly through `ShreeAI` and `ShreeClient`. Whether accessed directly or injected into Spring Boot services, the SDK gives applications deterministic control over the cognitive runtime:

| SDK Surface | Entry Point | Core Focus |
|---|---|---|
| **1. Chat SDK** | `client.chat()` / `shree.chat()` | Synchronous, asynchronous, and true SSE token streaming |
| **2. Memory SDK** | `client.memory()` / `shree.memory()` | Episodic memory storage, recall, and tenant-scoped caching |
| **3. Knowledge SDK** | `client.knowledge()` / `shree.knowledge()` | Hybrid RRF vector retrieval, document ingestion, entity lookup |
| **4. Planning SDK** | `client.planning()` / `shree.planning()` | Typed DAG creation, constraint refinement, plan validation |
| **5. Execution SDK** | `client.execution()` / `shree.execution()` | Action plan dispatch, tool execution, safety boundaries |
| **6. Reflection SDK** | `client.reflection()` / `shree.reflection()` | Post-run outcome analysis, adaptive calibration, analytics |
| **7. Identity SDK** | `client.identity()` / `shree.identity()` | Multi-tenant context resolution, profile management, session tracking |
| **8. Project SDK** | `client.project()` / `shree.project()` | JavaParser AST structural analysis, class discovery, impact analysis |
| **9. Developer SDK** | `client.developer()` / `shree.developer()` | Autonomous patch generation, compile validation, rollback planning |
| **10. Multi-Agent SDK** | `client.multiAgent()` / `shree.multiAgent()` | Multi-agent discovery, peer communication, chief-of-staff coordination |

*Complementary Platform Facades:*
- **SettingsSDK (`shree.settings()`)**: BYOK credential storage with zero-downtime hot-reload.
- **RuntimeEventBus (`shree.eventBus()`)**: Decoupled pub/sub event subscription for all kernel lifecycle events.
- **DiagnosticsSDK (`shree.diagnostics()`)**: Real-time thread-safe metrics and provider health reports.

---

## 2. Core SDK Facades

### 1. Chat SDK (`client.chat()`)
**Package:** `com.shreeai.os.platform.sdk.ShreeClient`

Provides synchronous, asynchronous, and streaming entry points into the cognitive runtime.

```java
// Synchronous grounded chat
SDKResponse response = client.chat("How does vector search work?");
System.out.println(response.answer());

// Asynchronous execution
CompletableFuture<SDKResponse> asyncResponse = client.chatAsync("Analyze repo structure");

// True LLM Token Streaming (consumes SSE/NDJSON fragments directly from provider)
client.chatStream("Summarize changes", new StreamingListener() {
    @Override public void onStart() { System.out.print("Stream started: "); }
    @Override public void onToken(String token) { System.out.print(token); }
    @Override public void onComplete(String full) { System.out.println("\nDone (" + full.length() + " chars)"); }
    @Override public void onError(Throwable t) { t.printStackTrace(); }
});
```

---

### 2. Memory SDK (`client.memory()`)
**Package:** `com.shreeai.os.platform.sdk.MemorySDK`

Manages episodic memories with tenant-isolated caching and semantic normalization.

| Method | Signature | Description |
|---|---|---|
| `store(String key, Object value)` | `void` | Stores an episodic memory item under a key |
| `recall(String query)` | `List<MemoryEntry>` | Semantically recalls matching memories |
| `delete(String key)` | `void` | Evicts the specified memory key |
| `clear()` | `void` | Flushes all memories for the current tenant |
| `size()` | `long` | Returns total active memories |

```java
shree.memory().store("user-preferred-framework", "Spring Boot 3.4");
List<MemoryEntry> entries = shree.memory().recall("framework preferences");
```

---

### 3. Knowledge SDK (`client.knowledge()`)
**Package:** `com.shreeai.os.platform.sdk.KnowledgeSDK`

Entry point for ingesting documentation and querying the hybrid pgvector knowledge graph.

| Method | Signature | Description |
|---|---|---|
| `ingest(String content)` | `KnowledgeEntry` | Chunks, embeds, and indexes text into pgvector |
| `search(String query)` | `List<KnowledgeEntry>` | Executes hybrid RRF search (HNSW KNN + GIN FTS) |
| `getEntity(String entityId)` | `KnowledgeEntry` | Fetches an exact entity by identifier |
| `getGraph()` | `KnowledgeGraph` | Retrieves graph relationship topology |

```java
KnowledgeEntry entry = shree.knowledge().ingest("PostgreSQL 16 provides enhanced pgvector HNSW indexing.");
List<KnowledgeEntry> results = shree.knowledge().search("pgvector indexing");
```

---

### 4. Planning SDK (`client.planning()`)
**Package:** `com.shreeai.os.platform.sdk.PlanningSDK`

Provides deterministic goal decomposition into topological DAGs.

```java
// Create a plan with objective and constraints
SDKResponse planResponse = shree.planning().createPlan("obj-101", "Migrate database to PostgreSQL", "infrastructure");

// Advanced typed plan generation
PlanningConstraints constraints = new PlanningConstraints(Map.of("timeoutMs", 5000), Map.of(), Map.of(), Map.of());
SDKResponse typedPlan = shree.planning().createPlanTyped("obj-102", "Refactor authentication filter", PlanningScope.SUBTASK, constraints);
```

---

### 5. Execution SDK (`client.execution()`)
**Package:** `com.shreeai.os.platform.sdk.ExecutionSDK`

Dispatches validated action plans through the platform's execution engine.

```java
ExecutionResult result = shree.execution().execute(plan);
System.out.println("Execution state: " + result.getStatus());
```

---

### 6. Reflection SDK (`client.reflection()`)
**Package:** `com.shreeai.os.platform.sdk.ReflectionSDK`

Powered by the `AdaptiveReflectionEngine`. Inspects historical runs, extracts lessons, and tunes runtime calibration thresholds dynamically.

```java
// Reflect on a completed execution
SDKResponse reflection = shree.reflection().reflect("exec-8821");
Map<String, Object> data = reflection.structuredPayload();
System.out.println("Verdict: " + data.get("verdict"));
System.out.println("Adaptive Score: " + data.get("score"));

// Retrieve analytics window for tenant
ReflectionStatistics stats = shree.reflection().statistics("tenant-prod", 50);
System.out.println("Success Rate: " + stats.getSuccessRate());
```

---

### 7. Identity SDK (`client.identity()`)
**Package:** `com.shreeai.os.platform.sdk.IdentitySDK`

Enforces strict tenant scoping and actor identity across executions.

```java
SDKResponse identity = shree.identity().resolve("user-771", "session-44", "crm-app", "workspace-alpha");
Map<String, Object> ctx = identity.structuredPayload();
System.out.println("Authenticated: " + ctx.get("authenticated"));
```

---

### 8. Project SDK (`client.project()`)
**Package:** `com.shreeai.os.platform.sdk.ProjectSDK`

Structural static analysis of JVM projects using JavaParser.

```java
// Analyze project root directory
ProjectSummary summary = shree.project().analyze(Path.of("/workspace/my-service"));
System.out.println("Total Classes: " + summary.getClassCount());

// Discover controllers, entities, or services
ProjectClass controller = shree.project().findController("/api/v1/orders");
ProjectImpact impact = shree.project().impact("OrderService");
System.out.println("Impacted Endpoints: " + impact.getAffectedEndpoints());
```

---

### 9. Developer SDK (`client.developer()`)
**Package:** `com.shreeai.os.platform.kernels.developer`

Enables autonomous developer planning, code synthesis, safe patch application, and rollback planning.

```java
// Autonomous planning and code generation (Sprint-16)
DeveloperResult plan = shree.project().build(
    "/workspace/my-service",
    "Add Redis caching to OrderService.findById"
);
System.out.println(plan.markdownSummary());

// Autonomous patch application pipeline (Sprint-17)
DeveloperExecutionResult execution = shree.project().apply(
    "/workspace/my-service",
    "Add Redis caching to OrderService.findById"
);
System.out.println("Patches Applied: " + execution.getPatches().size());
System.out.println("Compilation Validated: " + execution.getCompileReport().isSuccess());
```

---

### 10. Multi-Agent SDK (`client.multiAgent()`)
**Package:** `com.shreeai.os.platform.kernels.multiagent`

Coordinates multi-agent discovery, communication, and intent orchestration.

```java
// Register an agent descriptor
AgentDescriptor agent = new AgentDescriptor("ReviewAgent", "SECURITY", List.of("CODE_AUDIT"));
shree.multiAgent().registerAgent(new AgentRegistrationRequest(agent));

// Direct agent communication
AgentResponse comm = shree.multiAgent().communicate(new AgentCommunication(
    "SecurityAuditor", "ReviewAgent", "Audit SQL bindings in repository layer"
));
```

---

## 3. AST Parsing & Automated Patch Generation Workflows

The developer intelligence engine uses `JavaParser` to perform deterministic, structural code manipulation without risking source corruption or syntax errors:

```
                      Natural Language Instruction
                                   |
                                   v
             [ ProjectSDK: JavaAstParser (Java 21 AST) ]
               - Parses CompilationUnit
               - Extracts @RestController, @Service, @Entity
               - Builds Class & Endpoint Dependency Graph
                                   |
                                   v
                      [ Structural Impact Analysis ]
               - Traverses call hierarchy
               - Computes blast radius across project
                                   |
                                   v
                  [ Code Generation (JavaCodeGenerator) ]
               - Generates new methods, imports, fields
               - Assembles PatchPlan and GeneratedPatch
                                   |
                                   v
                     [ PatchApplier & AST Validator ]
               - Applies modifications in-memory
               - Validates with StaticJavaParser.parse(modifiedSource)
               - Fails closed on any parse error
                                   |
                                   v
                        [ In-Memory Static Compile ]
               - Validates symbol resolution
               - Generates reversible RollbackPlan
                                   |
                                   v
                       DeveloperExecutionResult
```

### Key Workflow Highlights:
1. **`JavaAstParser`**: Configured with language level `JAVA_21`. Inspects classes, records, interfaces, fields, methods, and Spring stereotypes (`@RestController`, `@Service`, `@Repository`, `@Entity`, `@ConfigurationProperties`).
2. **In-Memory Safety**: Patches are generated and verified entirely in memory. Disk files are never touched without explicit opt-in (`applyWithFileWrites`).
3. **AST Validation Gate**: Before accepting any patch, `PatchApplier` parses the resulting code with `StaticJavaParser.parse(source)`. If the patch produces invalid syntax or unclosed blocks, the patch is discarded immediately.
4. **Reversible Rollbacks**: Every patch bundle generates a structured `RollbackPlan` containing the exact original source and file diffs (`PatchDiff`), ensuring deterministic zero-risk rollback.

---

## 4. Multi-Tenant RBAC & Vector Isolation Boundaries

Shree AI OS is built from the ground up for strict multi-tenant enterprise isolation:

```
                            Execution Request
                                   |
                        [ TenantIsolationEnforcer ]
               Validates active tenant against TenantContext
                                   |
                     +-------------+-------------+
                     |                           |
                     v                           v
         [ Fail-Closed Security Gate ]     [ Tenant-Isolated pgvector ]
         graphPermissionManager()          WHERE tenant_id = :tenantId
         Evaluates ExecutionCapability     Composite HNSW Index
         DENY on NPE / unmapped action     Isolated Namespaces
```

### 1. Fail-Closed Authorization Gate (`graphPermissionManager`)
All tool invocations and graph execution steps pass through the runtime's authorization gate (`DefaultRuntimeService.java`):
- Maps capabilities to `ExecutionCapability` (`MEMORY_RECALL`, `KNOWLEDGE_SEARCH`, `PROJECT_PLANNING`, `TASK_EXECUTION`).
- Evaluates against the configured `PermissionPolicy` (`ALLOW`, `REQUIRE_APPROVAL` / `ASK_USER`, `DENY`).
- **P0 Fail-Closed Contract**: If any parameter is `null`, malformed, or unmapped, the gate catches the exception and immediately returns `PermissionDecision.DENY`.

### 2. Tenant-Isolated Vector Namespaces
- **Database Schema**: The `pgvector` store includes a dedicated `tenant_id TEXT NOT NULL` column across document and vector tables.
- **Index Scoping**: Compound indices on `(tenant_id, document_id)` and vector similarity searches explicitly filter by tenant ID.
- **Cache Isolation**: `DefaultSessionCache` partitions sessions, conversation state, execution contexts, and memory recall using composite tenant keys (`sessionKey(sessionId, tenantId)`).
- **Zero Cross-Tenant Leakage**: A tenant cannot search, retrieve, or recall knowledge or episodic memory belonging to another tenant.

---

## 5. Bring-Your-Own-Key (BYOK) Hot-Reload

`SettingsSDK` enables dynamic configuration of model API keys without application restarts:

```java
// Hot-reload a BYOK OpenAI key
ProviderSettings settings = shree.settings().configureApiKey(ProviderType.OPENAI, "sk-proj-abc123xyz");
System.out.println("Configured key: " + settings.maskedKey()); // sk-****3xyz
```

**Hot-Reload Call Chain:**
1. `SettingsSDK.configureApiKey()` persists the credential in `ByokSettingsService`.
2. `ByokSettingsService.fireChange()` broadcasts a `ChangeEvent`.
3. `DefaultRuntimeService.rebuildLlmRouter()` catches the event, creates a new provider instance, prepends it to the routing chain, and updates `LlmRouter` atomically.
4. The very next chat request utilizes the newly configured provider without restarting the JVM.

---

## 6. Real Provider Token Streaming

Shree AI OS implements true asynchronous streaming directly from model APIs:

- **OpenAI:** Consumes Server-Sent Events (`text/event-stream`), streaming delta tokens lazily via `choices[0].delta.content`.
- **Gemini:** Parses SSE chunks of candidate content from the Google Gemini API.
- **Ollama:** Streams NDJSON (`application/x-ndjson`) token lines for local models.
- **In-Memory Fallback:** Deterministically yields tokens for development and offline testing.

Tokens are emitted to the caller's `StreamingListener.onToken(token)` with sub-millisecond dispatch overhead.

---

## 7. Diagnostics & Reliability Metrics

Track runtime health and throughput via `SdkDiagnosticsService`:

```java
Map<String, Object> report = shree.diagnostics().report();
System.out.println("Knowledge Cache Hits: " + report.get("knowledgeHits"));
System.out.println("Active Model: " + report.get("activeModel"));
```

All internal counters use `AtomicInteger` and `LongAdder` for thread-safe lock-free operation under heavy concurrency (verified under 1,000 concurrent threads).

---

Platform: **Shree AI OS**  
Document: **Developer Capabilities**  
Version: **1.0.6-developer-preview**  
Author: **Darshan Rathod**
