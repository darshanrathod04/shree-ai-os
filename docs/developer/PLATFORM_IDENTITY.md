# Platform Identity

> **What Shree AI OS Is — Developer Preview v1.0.6**

**Shree AI OS is a JVM-native cognitive operating system runtime with Bring-Your-Own-Key (BYOK) model routing.** It enables enterprise Java applications to embed a complete cognitive brain — orchestrating memory, hybrid vector retrieval, deterministic planning, structural reasoning, adaptive reflection, multi-tenant isolation, and safe code execution before any language model generates a response.

The platform is anchored in one immutable architectural principle:

> **The LLM is the final natural-language response generator, not the decision maker.**

---

## 1. Core Platform Identity

Modern AI integration in enterprise environments often fails due to hallucination, unpredictable agent loops, security boundary violations, and vendor lock-in. Shree AI OS resolves these challenges by embedding an in-process, deterministic cognitive architecture:

1. **Native Java 21+ Runtime:** Zero Python sidecars, zero external agent frameworks, zero out-of-process RPC overhead. Runs natively inside your Spring Boot or JVM process.
2. **Deterministic Infrastructure First:** Goal decomposition, context loading, memory recall, knowledge synthesis, policy evaluation, and outcome verification are deterministic Java code.
3. **Bring-Your-Own-Key (BYOK) Multi-Provider Routing:** The runtime is model-agnostic. Route seamlessly between Google Gemini, OpenAI, Ollama (local), and OpenAI-compatible endpoints with dynamic hot-reload and deterministic in-memory fallbacks.
4. **Privacy-First & Multi-Tenant Isolated:** Strict boundary enforcement ensures cross-tenant data leaks are physically impossible at both the caching and database vector tiers.

---

## 2. Five-Layer Architecture

Shree AI OS organizes cognitive capabilities across five distinct, decoupled layers:

```text
+-------------------------------------------------------------------------+
|                           Application Layer                             |
|          Spring Boot Services, Microservices, Enterprise Systems        |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                                SDK Layer                                |
|  Chat • Memory • Knowledge • Planning • Execution • Reflection •        |
|  Identity • Project • Developer • Multi-Agent • Settings • Diagnostics  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                          Runtime Orchestration                          |
|  11-Stage Cognitive Pipeline • Intent Router • Fail-Closed Auth Gate    |
|  Event Bus • Multi-Agent Chief Orchestrator • Tenant Isolation Enforcer |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                             Kernel Services                             |
|  Episodic Memory • pgvector Hybrid Retrieval • Topological Planning •   |
|  In-Memory AST Patch Engine • Adaptive Reflection • Context Detector    |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                        LLM Provider Layer (BYOK)                        |
|        Google Gemini • OpenAI • Ollama • Deterministic In-Memory        |
+-------------------------------------------------------------------------+
```

---

## 3. The 11-Stage Cognitive Execution Pipeline

Every execution submitted to the platform passes through the hardened 11-stage cognitive execution pipeline:

```text
User Request
     ↓
1. IdentityStage          (Actor resolution, tenant context validation)
     ↓
2. ContextStage           (Domain detection, constraint & goal extraction)
     ↓
3. MemoryRecallStage      (Episodic memory & preferences retrieval)
     ↓
4. KnowledgeStage         (Hybrid RRF vector search + K0.6 Acquisition)
     ↓
5. ReasoningStage         (Fact extraction, conflict resolution, evidence graph)
     ↓
6. InferenceStage         (Deterministic candidate scoring & tradeoff analysis)
     ↓
7. PlanningStage          (Topological DAG task decomposition)
     ↓
8. ActionExecutionStage   (Fail-closed tool & patch execution gate)
     ↓
9. ReflectionStage        (Adaptive calibration, outcome scoring, bias audit)
     ↓
10. MemoryStoreStage      (Episodic memory commit & vector indexing)
     ↓
11. ChiefReviewStage      (Constitutional verification & governance sign-off)
     ↓
Dual-Mode Synthesis       (NaturalResponseAgent: Strict RAG or Fallback)
```

By enforcing this pipeline, the runtime can identify flaws, trigger reflection-driven retries, or block unauthorized tool invocations **before** model synthesis occurs.

---

## 4. BYOK Multi-Model Routing

Shree AI OS treats large language models as swappable commodities. With the built-in `LlmRouter` and `SettingsSDK`, platform operators can configure and hot-swap models at runtime:

- **Google Gemini:** Direct streaming and generation via Google GenAI REST API (e.g. `gemini-2.5-flash`).
- **OpenAI:** GPT-4o / GPT-3.5 with full SSE delta-token streaming.
- **Ollama:** Private on-premise inference with Llama 3 / Mistral / DeepSeek via local NDJSON streams.
- **In-Memory Fallback:** Deterministic zero-latency fallback ensuring test suites and offline airgapped environments never fail.

### Failover Resilience
```text
Primary (Gemini / OpenAI) ──[HTTP 429/503 Exponential Backoff]──> Fallback (Ollama / In-Memory)
```
Providers catch transient rate limits and service outages, retrying with exponential backoff before cleanly delegating to secondary providers.

---

## 5. Enterprise Multi-Tenancy & Security

Security and tenancy in Shree AI OS are not afterthoughts — they are hard structural boundaries:

- **Fail-Closed Authorization Gate:** `graphPermissionManager` strictly rejects unknown, unmapped, or null parameters with `PermissionDecision.DENY`.
- **Tenant-Scoped Vector Stores:** All vector tables in PostgreSQL enforce `tenant_id TEXT` partitioning, preventing cross-tenant information leakage.
- **Thread-Safe Runtime:** Memory and diagnostic counters utilize `AtomicInteger` and `LongAdder` structures, verified under 1,000 concurrent threads.

---

## 6. Official Maven Coordinates

Add Shree AI OS to your Maven project:

```xml
<dependency>
    <groupId>io.github.darshanrathod04</groupId>
    <artifactId>shree-ai-os</artifactId>
    <version>1.0.6-developer-preview</version>
</dependency>
```

Initialize the runtime:

```java
ShreeAI shree = ShreeAI.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();

SDKResponse response = shree.chat("Explain the cognitive pipeline");
System.out.println(response.answer());

shree.close();
```

---

## 7. What Shree AI OS Is NOT

To maintain architectural clarity:
- **NOT a LangChain or LlamaIndex clone:** It is written entirely in native Java with strict compile-time types, deterministic lifecycle, and zero external script dependencies.
- **NOT an opaque prompt wrapper:** Every step is inspected, logged, and audited through structured Java records and events.
- **NOT a vector database:** It integrates cleanly with standard enterprise PostgreSQL (`pgvector`) instances.
- **NOT restricted to Spring Boot:** Runs in any modern JVM application (Java 21+).

---

Platform: **Shree AI OS**  
Document: **Platform Identity**  
Version: **1.0.6-developer-preview**  
Author: **Darshan Rathod**
