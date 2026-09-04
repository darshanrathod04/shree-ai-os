# Platform Identity — What Shree AI OS Is

> **Status:** Developer reference. Every claim in this document is backed by a specific Java source file and line range. If a section says "wired", that means the code path was traced from the entry point to the implementation.

---

## 1. Plain-English Identity

Shree AI OS is a **Java 21 runtime library** that lets any Java application treat an LLM as part of a deterministic software stack rather than as a chat endpoint. It ships as a single Maven artifact (`com.shreeai:shree-ai-os:1.0.0`) and is built on Spring Boot 4.0.2.

When you call `ShreeAI.builder().apiKey("...").build().chat()` the runtime:

1. Accepts a natural-language or structured request through `ShreeClient`.
2. Resolves which kernel should handle the request (Memory, Knowledge, Planning, Execution, etc.).
3. Runs an 11-stage orchestration pipeline that handles identity checks, context loading, memory recall, knowledge retrieval, reasoning, inference, planning, action execution, reflection, and memory persistence.
4. Generates the final natural-language response via an LLM router that can chain multiple providers with automatic fallback.

The platform's design goal is **deterministic AI orchestration**: the LLM is the last step, not the first. The runtime decides what to do before it ever calls a model.

---

## 2. Five-Layer Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│ Layer 5 — APPLICATION                                              │
│   Your Java app, REST controller, or reference app                  │
│   (shree-playground / shree-developer-intelligence)                │
├─────────────────────────────────────────────────────────────────────┤
│ Layer 4 — SDK FACADE                                               │
│   com.shreeai.os.platform.sdk.*                                    │
│   MemorySDK, KnowledgeSDK, PlanningSDK, ReasoningSDK,               │
│   ReflectionSDK, InferenceSDK, IdentitySDK, ExecutionSDK,          │
│   ProjectSDK, SettingsSDK                                          │
├─────────────────────────────────────────────────────────────────────┤
│ Layer 3 — RUNTIME ORCHESTRATION                                    │
│   ShreeClient → DefaultRuntimeService                              │
│   • RuntimeIntentRouter (deterministic operation routing)         │
│   • ChiefIntelligenceAgent (11-stage pipeline driver)              │
│   • MultiKernelOrchestrator (multi-intent fan-out)                │
│   • LlmRouter (provider chain with auto-fallback)                 │
│   • RuntimeEventBus (subscribe / publish / unsubscribe)            │
├─────────────────────────────────────────────────────────────────────┤
│ Layer 2 — KERNEL SERVICES                                          │
│   com.shreeai.os.platform.kernels.*                                │
│   memory, knowledge, planning, cognitive (reasoning/reflection/   │
│   inference), identity, execution, project, chief                 │
├─────────────────────────────────────────────────────────────────────┤
│ Layer 1 — PROVIDERS & STORAGE                                      │
│   LLM: OpenAiProvider, GeminiProvider, OllamaProvider,             │
│        InMemoryLlmProvider, OpenAiCompatibleProvider               │
│   Storage: in-memory repositories (Memory, Knowledge,              │
│   Conversation, Identity, Execution plans)                        │
└─────────────────────────────────────────────────────────────────────┘
```

### What each layer does

| Layer | Responsibility | Entry Points |
|-------|----------------|--------------|
| 5 — Application | Calls the SDK or injects `ShreeAI` as a Spring bean | `ShreeAI.builder()` |
| 4 — SDK Facade | Provides 10 focused APIs; one per capability | `MemorySDK`, `KnowledgeSDK`, etc. |
| 3 — Runtime | Decides routing, runs the 11-stage pipeline, manages LLM, publishes events | `DefaultRuntimeService` |
| 2 — Kernels | Domain-specific engines (memory storage, knowledge graph, planning, etc.) | `DefaultMemoryService`, `DefaultKnowledgeService`, `DefaultPlanningService` |
| 1 — Providers | External integrations (LLM APIs) and in-process storage | `LlmProvider` implementations |

---

## 3. Platform Map (Mermaid)

```mermaid
flowchart TB
    subgraph L5["Layer 5 — Application"]
        APP["Your Java app<br/>or shree-playground /<br/>shree-developer-intelligence"]
    end

    subgraph L4["Layer 4 — SDK Facade (com.shreeai.os.platform.sdk.*)"]
        SDK1["MemorySDK"]
        SDK2["KnowledgeSDK"]
        SDK3["PlanningSDK"]
        SDK4["ReasoningSDK"]
        SDK5["ReflectionSDK"]
        SDK6["InferenceSDK"]
        SDK7["IdentitySDK"]
        SDK8["ExecutionSDK"]
        SDK9["ProjectSDK"]
        SDK10["SettingsSDK"]
    end

    subgraph L3["Layer 3 — Runtime Orchestration"]
        CLIENT["ShreeClient<br/>(chat, chatStream, submit)"]
        RUNTIME["DefaultRuntimeService<br/>• submit()<br/>• bindEventBus()"]
        ROUTER["RuntimeIntentRouter<br/>(deterministic routing)"]
        CHIEF["ChiefIntelligenceAgent<br/>(11-stage pipeline driver)"]
        LLMROUTER["LlmRouter<br/>(provider chain)"]
        BUS["RuntimeEventBus<br/>(subscribe / publish)"]
    end

    subgraph L2["Layer 2 — Kernel Services"]
        K_MEM["Memory Kernel<br/>DefaultMemoryService"]
        K_KNOW["Knowledge Kernel<br/>DefaultKnowledgeService"]
        K_PLAN["Planning Kernel<br/>DefaultPlanningService"]
        K_COG["Cognitive Kernels<br/>Reasoning, Inference, Reflection"]
        K_ID["Identity Kernel<br/>DefaultIdentityService"]
        K_EXE["Execution Kernel<br/>DefaultExecutionService"]
        K_PROJ["Project Kernel<br/>ProjectIntelligenceService"]
    end

    subgraph L1["Layer 1 — Providers & Storage"]
        P_OPENAI["OpenAiProvider"]
        P_GEMINI["GeminiProvider"]
        P_OLLAMA["OllamaProvider"]
        P_INMEM["InMemoryLlmProvider"]
        P_COMPAT["OpenAiCompatibleProvider"]
        STORE["In-memory repositories<br/>(Memory, Knowledge, Identity,<br/>Execution, Conversation)"]
    end

    APP -->|ShreeAI.builder()| CLIENT
    APP --> SDK1 & SDK2 & SDK3 & SDK4 & SDK5 & SDK6 & SDK7 & SDK8 & SDK9 & SDK10
    SDK1 & SDK2 & SDK3 & SDK4 & SDK5 & SDK6 & SDK7 & SDK8 & SDK9 & SDK10 --> RUNTIME
    CLIENT --> RUNTIME
    RUNTIME --> ROUTER
    ROUTER -->|deterministic intent| K_MEM & K_KNOW & K_PLAN & K_COG & K_ID & K_EXE & K_PROJ
    RUNTIME --> CHIEF
    CHIEF -->|orchestrates| K_MEM & K_KNOW & K_PLAN & K_COG & K_ID & K_EXE & K_PROJ
    K_MEM & K_KNOW & K_PLAN & K_COG & K_ID & K_EXE & K_PROJ --> STORE
    RUNTIME --> LLMROUTER
    LLMROUTER --> P_OPENAI & P_GEMINI & P_OLLAMA & P_INMEM & P_COMPAT
    RUNTIME -.->|publish / subscribe| BUS
    BUS -.->|notify| APP
```

**How to read this diagram:**
- Solid arrows = direct method calls (verified in code).
- Dashed arrows = event bus subscriptions.
- Layer 4 SDKs all delegate to the same `DefaultRuntimeService`; they are thin facades.
- Layer 1 LLM providers are selected by `LlmRouter` at runtime; the router can chain multiple providers and falls back automatically.

---

## 4. Entry Points (Verified)

### Maven coordinates
```xml
<dependency>
    <groupId>com.shreeai</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Requirements:** Java 21, Spring Boot 4.0.2 (transitive).

### Minimal startup
```java
ShreeAI shree = ShreeAI.builder()
    .apiKey("sk-...")                          // optional, enables OpenAI
    .configuration(RuntimeConfiguration.defaults())
    .build();

ChatResponse reply = shree.chat("Plan a 30-minute workout");
shree.close();
```

### What happens internally
1. `ShreeAI.builder()` returns a `ShreeBuilder`.
2. `.build()` creates a `DefaultRuntimeService` (if no runtime was injected), calls `initialize()` then `start()`.
3. `ShreeAI` constructs a `ShreeClient` with the runtime and a fresh `RuntimeEventBus`.
4. `ShreeClient` calls `runtime.bindEventBus(eventBus)` to wire event consumers.
5. `.chat()` builds an `IntelligenceContextBuilder` payload, calls `runtime.submit()`, and returns the `ChatResponse`.

All five steps are confirmed in `ShreeAI.java` and `ShreeClient.java`.

---

## 5. The 11-Stage Pipeline

When the runtime cannot route a request deterministically (i.e., it is not a simple `STORE_MEMORY` or `PLAN_PROJECT` operation), it falls through to the **ChiefIntelligenceAgent** which orchestrates these stages in order:

1. **IdentityResolutionStage** — resolves the actor / tenant.
2. **ContextLoadingStage** — loads conversation context.
3. **MemoryRecallStage** — recalls relevant past interactions.
4. **KnowledgeRetrievalStage** — searches the knowledge graph.
5. **ReasoningStage** — applies rule-based reasoning.
6. **InferenceStage** — runs type / contract inference.
7. **PlanningStage** — generates an action plan.
8. **ActionExecutionStage** — executes the plan (may call other kernels).
9. **ReflectionStage** — evaluates the outcome and may trigger a retry loop.
10. **MemoryStorageStage** — persists new memories.
11. **ChiefReviewStage** — final verification, builds the `VerificationReport`.

After the pipeline completes, `DefaultRuntimeService` invokes **`new NaturalResponseAgent(llmRouter).generate(...)`** to produce the natural-language reply. The LLM is the final step, not the first.

**Reflection-driven retry:** if the `VerificationReport` reports a failure, the agent loops back to the failing stage (typically Planning or ActionExecution) and re-runs the tail of the pipeline. This is implemented in `ChiefIntelligenceAgent`.

---

## 6. LLM Provider Layer

`LlmRouter` chains multiple `LlmProvider` implementations. Providers are registered by `buildDefaultLlmRouter()`:

| Provider | Activated when |
|----------|----------------|
| `InMemoryLlmProvider` | Always (deterministic fallback) |
| `OllamaProvider` | `shree.llm.ollama.enabled=true` or `SHREE_LLM_OLLAMA=true` |
| `OpenAiProvider` | `OPENAI_API_KEY` env var is set |
| `GeminiProvider` | `shree.ai.api-key`, `GEMINI_API_KEY`, or `GOOGLE_API_KEY` is set |
| `OpenAiCompatibleProvider` | `shree.llm.openai-compatible.base-url` is set |

**Chain configuration:** read from `shree.llm.chain` / `SHREE_LLM_CHAIN` / `LLM_CHAIN` (comma-separated provider names). If no chain is set, the router auto-selects:
- `gemini,in-memory` if Gemini is available
- `openai,in-memory` if OpenAI is available
- `in-memory` otherwise

**Fallback behavior:** if the primary provider fails, the router tries the next provider in the chain. `InMemoryLlmProvider` is always last and always succeeds (it returns a templated response).

**Real token streaming:** `LlmProvider.stream(LlmRequest)` is the streaming SPI. Each provider implementation parses its native transport (SSE for OpenAI/Gemini, NDJSON for Ollama) and returns a `Stream<String>`:
- `OpenAiProvider.stream()` at `OpenAiProvider.java:77-148` — SSE `data: {...}` parsing via `ChunkSpliterator`, terminates on `data: [DONE]`. Each fragment is the `choices[0].delta.content` text.
- `GeminiProvider.stream()` at `GeminiProvider.java:72-112` — SSE parsing of Gemini candidates JSON.
- `OllamaProvider.stream()` at `OllamaProvider.java:79-112` — newline-delimited JSON parsing.
- `InMemoryLlmProvider.stream()` at `InMemoryLlmProvider.java:41-55` — returns `"modelName echoes: prompt"` as a token stream (deterministic local fallback).

`Runtime.streamText(String)` at `DefaultRuntimeService.java:680-698` invokes `llmRouter.stream(LlmRequest)`, and `ShreeClient.chatStream()` at `ShreeClient.java:209-247` consumes the returned token stream, forwarding each fragment to the `StreamingListener.onToken` callback in real time. **This is true provider streaming, not a word-splitting simulation.**

---

## 7. Tenant Model (Request-Scoped with Wired Enforcement)

Shree AI OS uses a **thread-local tenant context** with wired enforcement in the runtime:

- `TenantContext` (`src/main/java/com/shreeai/os/platform/runtime/tenant/TenantContext.java`) is a thread-local record holding `tenantId` and `organizationId`. Defaults to `"system"`.
- `DefaultTenantResolver` (`DefaultTenantResolver.java`) reads from `TenantContext.current()`.
- `TenantIsolationEnforcer` (`TenantIsolationEnforcer.java`) validates tenant access. Throws `TenantIsolationException` on cross-tenant access.
- `DefaultRuntimeService` initializes `TenantIsolationEnforcer` at line 598 and calls `enforceTenantBoundary()` on every reflected operation:
  - `enforceTenantBoundaryFromMetadata()` in `submit()` at line 929
  - `enforceTenantBoundary(tenantId)` in `recentReflections()` at line 2005
  - `enforceTenantBoundary(tenantId)` in `searchReflections()` at line 2016
- `RuntimeRecoveryService.recoverTenant()` sets `TenantContext.setCurrentTenant(tenantId, tenantId)` at line 69 before recovery, then clears it in `finally`.
- `KnowledgeIngestionEventConsumer` operates within the tenant scope set by `RuntimeRecoveryService`.

**Runtime path (verified):**
```
Application boundary → TenantContext.setCurrentTenant()
  → DefaultRuntimeService.submit() → enforceTenantBoundaryFromMetadata()
  → DefaultRuntimeService.recentReflections() → enforceTenantBoundary()
  → TenantIsolationEnforcer.validateAccess()
  → throws TenantIsolationException on violation
```

**Implication:** Multi-tenant isolation is enforced at the runtime boundary. Operations within the same tenant are unrestricted. Cross-tenant access is blocked with a structured exception.

---

## 8. Event Bus

`RuntimeEventBus` is a simple pub/sub interface:

```java
void publish(RuntimeEvent event);
<S extends RuntimeEvent> void subscribe(EventType<S> type, EventListener<S> listener);
void unsubscribe(EventType<?> type, EventListener<?> listener);
```

The bus is created by `ShreeAI` and bound to the runtime via `runtime.bindEventBus(eventBus)`. The runtime registers internal consumers (e.g., `KnowledgeIngestionEventConsumer`). You can subscribe to events from your application code.

---

## 9. What Shree AI OS Is Not

To avoid confusion:

- **Not a vector database.** Knowledge storage is in-memory and graph-based, not embedding-based.
- **Not a fine-tuning platform.** It orchestrates LLMs; it does not train them.
- **Not a multi-tenant SaaS.** Tenant isolation is enforced at the runtime boundary via `TenantIsolationEnforcer`; see §7.
- **Not a Spring-only framework.** It works in any Java 21 application; Spring Boot is the dependency container, not a requirement.
- **Not a chat-only API.** The 11-stage pipeline can execute plans, store memories, and run multi-kernel orchestration without any LLM call.

---

## 10. Summary

Shree AI OS is a **deterministic AI orchestration runtime** for Java. It:

- Provides 10 SDK facades for capability-based access.
- Routes requests through a 11-stage pipeline with reflection-driven retry.
- Chains multiple LLM providers with automatic fallback.
- Persists state in-memory (Memory, Knowledge, Identity, Execution, Conversation).
- Publishes events through a simple pub/sub bus.

**The LLM is invoked only in `NaturalResponseAgent`, as the final step.** Everything before it is deterministic Java code.

---

*Next: see [DEVELOPER_CAPABILITIES.md](DEVELOPER_CAPABILITIES.md) for the complete SDK catalog.*
