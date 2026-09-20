# Cognitive Runtime Architecture

> Technical Architecture Reference — Developer Preview v1.0.6

This document details the internal architecture of Shree AI OS, including the hardened 11-stage cognitive execution pipeline, the hybrid RAG retrieval engine, the ONNX embedding pipeline, pgvector integration, K0.6 autonomous knowledge acquisition, and resilient multi-provider LLM routing.

**Audience:** Platform architects, contributors, and enterprise JVM reliability engineers.

**In-Process, Privacy-First Cognitive Runtime & Deterministic Orchestration**

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-green.svg)](https://spring.io/projects/spring-boot)
[![pgvector](https://img.shields.io/badge/pgvector-0.7-blue.svg)](https://github.com/pgvector/pgvector)
[![Version](https://img.shields.io/badge/Version-1.0.6--developer--preview-orange.svg)](./pom.xml)
[![Build Status](https://img.shields.io/badge/Tests-56%2F56%20Green-brightgreen.svg)](./WORKING_STATUS.md)

---

## 1. What is Shree AI OS?

Shree AI OS is an **in-process, privacy-first cognitive operating system runtime** that embeds an entire AI reasoning brain directly within JVM applications — without leaking corporate data or relying on opaque external orchestrators. Operating as a native Java 21 library, the runtime enforces deterministic software control before invoking any language model:

| Capability | Implementation Mechanism |
|---|---|
| **Semantic Embeddings** | In-process ONNX model (`all-MiniLM-L6-v2`), 384-dimensional vector space, zero-network-latency inference |
| **Document Chunking** | Sentence-boundary-aware sliding window (600-char target, 80-char whitespace-aligned overlap) |
| **Hybrid Vector Search** | PostgreSQL + `pgvector`: HNSW (semantic KNN) + GIN full-text search (`tsvector`), fused via **Reciprocal Rank Fusion (RRF)** |
| **Dual-Mode Synthesis** | Strict RAG grounding with citation tracking vs. general assistance fallback via `NaturalResponseAgent` |
| **Autonomous Acquisition** | K0.6 engine with strict query domain isolation (Java, JavaScript, Python, Healthcare) |
| **Cognitive Pipeline** | Hardened 11-stage execution pipeline from Identity Resolution to Chief Review |
| **Fail-Closed Security** | Deterministic RBAC boundaries with fail-closed authorization gate denying unknown/malformed inputs |
| **Resilient Routing** | `LlmRouter` with exponential backoff on HTTP 429/503 and deterministic in-memory fallback |

---

## 2. Canonical Cognitive Execution Pipeline (11 Stages)

Every request entering Shree AI OS through the SDK or Application Gateway flows through the hardened 11-stage cognitive execution pipeline. The runtime guarantees deterministic stage execution order, enforcing tenant isolation, policy evaluations, and reflection prior to response synthesis.

```
                           +-------------------------------------------------------+
                           |               SDK / Application Gateway               |
                           +-------------------------------------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 1. IdentityStage          |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 2. ContextStage           |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 3. MemoryRecallStage      |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 4. KnowledgeStage         |
                                        |    (Dual-Channel RRF)     |
                                        |    (K0.6 Acquisition)     |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 5. ReasoningStage         |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 6. InferenceStage         |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 7. PlanningStage          |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 8. ActionExecutionStage   |
                                        |    (Fail-Closed Gate)     |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 9. ReflectionStage        |
                                        |    (Adaptive Calibration) |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 10. MemoryStoreStage      |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | 11. ChiefReviewStage      |
                                        +---------------------------+
                                                      |
                                                      v
                                        +---------------------------+
                                        | Dual-Mode Synthesis       |
                                        | (NaturalResponseAgent)    |
                                        +---------------------------+
```

### Stage Responsibilities:

1. **`IdentityStage`**: Resolves caller identity (`identityId`, `sessionId`, `applicationId`, `workspaceId`). Validates authentication tokens and binds credentials into the request context.
2. **`ContextStage`**: Extracts ambient execution parameters, detects primary domain (e.g. `TECHNOLOGY`, `MEDICAL`, `FINANCIAL`), identifies ambiguous goals, and establishes tenant boundaries.
3. **`MemoryRecallStage`**: Queries semantic episodic memory for historical conversations, user preferences, and tenant-scoped session facts.
4. **`KnowledgeStage`**: Executes hybrid RRF retrieval (HNSW semantic KNN + GIN full-text FTS) across local pgvector knowledge stores and invokes the K0.6 Autonomous Knowledge Acquisition engine when external or canonical specs are needed.
5. **`ReasoningStage`**: Performs structured fact extraction, checks premise consistency, resolves evidence conflicts, and builds the evidence graph.
6. **`InferenceStage`**: Generates deterministic hypotheses, conducts tradeoff analyses, and calibrates confidence tiers (`HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT`).
7. **`PlanningStage`**: Decomposes complex objectives into ordered topological Directed Acyclic Graphs (DAGs) with explicit preconditions and fallback steps.
8. **`ActionExecutionStage`**: Dispatches planned tool invocations, code modifications, or terminal commands. Guarded by the **fail-closed authorization gate** (`graphPermissionManager`), which strictly defaults to `PermissionDecision.DENY` on unmapped capabilities or unexpected exceptions.
9. **`ReflectionStage`**: Powered by `AdaptiveReflectionEngine`. Inspects action outcomes, calculates importance scores, updates retry thresholds, and flags memory-worthy operational learnings.
10. **`MemoryStoreStage`**: Persists new episodic memories, reflection lessons, and updated conversation embeddings into tenant-isolated storage.
11. **`ChiefReviewStage`**: Performs final constitutional compliance audits, governance gate checks, and safety assertions before clearing the execution bundle for response delivery.

---

## 3. Dual-Mode Synthesis Strategy

Shree AI OS separates deterministic knowledge verification from natural-language generation. Once the cognitive pipeline completes, response generation is handled by the `NaturalResponseAgent` using a strict **Dual-Mode Synthesis** strategy:

```
                            Execution Verification Report
                                          |
                        +-----------------+-----------------+
                        |                                   |
                Evidence Sufficient?                Evidence Insufficient?
                        |                                   |
                        v                                   v
             [Mode A: Strict RAG Grounding]      [Mode B: General Assistance]
             - Ingests verified chunks           - Deterministic fallback
             - Strict citation tracking          - Conversational continuity
             - Filters ungrounded claims         - Zero hallucinated facts
             - Chunk ID, title, excerpt          - Safe recommendation guidance
```

### Mode A: Strict RAG Grounding (Verified Knowledge)
- **Activation:** Triggered when the `VerificationReport` contains verified evidence items from project intelligence or knowledge graph nodes (`VERIFIED_PROJECT`, `VERIFIED_KB`).
- **Citation Tracking:** Every factual assertion in the synthesized response maps to a concrete citation payload containing:
  - `chunkId`: Unique UUID of the indexed chunk.
  - `title`: Document or file title.
  - `excerpt`: Verbatim text snippet used during synthesis.
  - `score`: Combined RRF similarity score (0.0 to 1.0).
- **Anti-Hallucination Gate:** Non-grounded claims not backed by evidence items are filtered out. If the LLM generates unsupported assertions, the runtime strips them prior to client delivery.

### Mode B: General Assistance Fallback (Insufficient / Conversational)
- **Activation:** Triggered when the query is open-ended, conversational (e.g., greetings, philosophical questions), or when no indexed knowledge meets confidence thresholds (`ConfidenceTier.INSUFFICIENT`).
- **Deterministic Continuity:** Rather than failing abruptly or inventing facts, the agent uses structured templates and conversational completions via `NaturalResponseAgent`.
- **Honest Boundary Enforcement:** For domain queries with insufficient context, the system explicitly states known limitations and suggests concrete acquisition paths (e.g. ingesting relevant documentation or refining project path).

---

## 4. K0.6 Autonomous Knowledge Acquisition Engine

When queries refer to external specifications, frameworks, or domain models not present in local memory, the **K0.6 Autonomous Knowledge Acquisition Engine** (`DefaultKnowledgeAcquisitionOrchestrator`, `DefaultKnowledgeContentResolver`) resolves canonical knowledge deterministically.

### Query Domain Isolation
To prevent cross-domain contamination in multi-tenant and multi-language environments, the acquisition engine enforces **strict domain isolation**:

```
                       User Query / Domain Detector
                                     |
         +-----------------+---------+---------+-----------------+
         |                 |                   |                 |
         v                 v                   v                 v
     [ Python ]     [ JavaScript ]      [ Healthcare ]        [ Java ]
         |                 |                   |                 |
         +--------+--------+---------+---------+                 |
                  |                  |                           |
                  v                  v                           v
         Strictly Blocks:      Strictly Blocks:           Isolated Java &
          Java / Spring         Java / Spring             Spring Sources
          Knowledge             Knowledge                 Preserved
```

- **Domain Isolation Rules:**
  - Queries targeting **Python**, **Healthcare**, **Domain Modeling**, or pure **JavaScript** strictly block Java and Spring knowledge sources (`isSourceCompatible()` check).
  - A healthcare diagnostic query will never be populated with Java Spring Boot boilerplate.
  - A Python asyncio query will never receive Java virtual thread or concurrency documentation.
- **Deterministic Canonical Generators:** When external network retrieval is disabled or offline, built-in domain generators provide verified architectural specifications for core domains without hallucination.

---

## 5. Failover Resilience in `LlmRouter`

The LLM is treated as a swappable, untrusted commodity layer. The runtime coordinates all model interactions through `LlmRouter`, guaranteeing high availability through multi-provider chaining and automatic retry policies:

```
                      LlmRouter Execution Chain
                                 |
                                 v
                     +-----------------------+
                     | 1. Primary Provider   |
                     |    (e.g., Gemini)     |
                     +-----------------------+
                                 |
                 HTTP 429/503?   +---> [Exponential Backoff Retry]
                 Exhausted?      |
                                 v
                     +-----------------------+
                     | 2. Secondary Provider |
                     |    (e.g., OpenAI)     |
                     +-----------------------+
                                 |
                 Failed/Offline? |
                                 v
                     +-----------------------+
                     | 3. In-Memory Provider |
                     |    (Deterministic)    |
                     +-----------------------+
```

### Exponential Backoff & Transient Fault Handling
- **HTTP 429 (Rate Limit) & HTTP 503 (High Demand / Service Unavailable):**
  - Providers (such as `GeminiProvider`) implement active retry loops with configurable exponential backoff (`retryBackoffMs` doubling per attempt, default 2000ms, up to 3 retries).
  - Prevents transient cloud rate spikes from bubbling up as client exceptions.
- **Multi-Provider Fallback Chain:**
  - If the primary provider fails completely, `LlmRouter` automatically falls through to the next configured provider in the chain (e.g. `gemini -> openai -> ollama -> in-memory`).
- **Deterministic In-Memory Fallback:**
  - The `InMemoryLlmProvider` sits at the end of the fallback chain, guaranteeing that airgapped environments, CI pipelines, and offline development environments always succeed without external network dependencies.

---

## 6. Hybrid Vector Search (PostgreSQL + pgvector)

Shree AI OS uses a dual-channel retrieval architecture combining dense semantic embeddings with sparse keyword indexing, fused via **Reciprocal Rank Fusion (RRF)**:

```
                                  Query Text
                                       |
                   +-------------------+-------------------+
                   |                                       |
                   v                                       v
         [ ONNX Embedder ]                       [ tsvector Parser ]
          384-dim vector                          English dictionary
                   |                                       |
                   v                                       v
         [ pgvector HNSW ]                       [ PostgreSQL GIN ]
         Cosine Distance <=>                     Full-Text Search @@
                   |                                       |
                   v                                       v
         Dense Rank (1..20)                      Sparse Rank (1..20)
                   |                                       |
                   +-------------------+-------------------+
                                       |
                                       v
                     Reciprocal Rank Fusion (k = 60)
                     Score = 1/(60 + Rank_dense) + 1/(60 + Rank_sparse)
                                       |
                                       v
                             Top-K Grounded Chunks
```

### Smart Document Chunking (`DocumentChunker`)
- Splits text on semantic sentence boundaries using regular expression heuristics.
- Target chunk size: **600 characters** with an **80-character whitespace-aligned overlap**.
- Guarantees tokens and sentences are never severed mid-word, maintaining vector quality.

---

## 7. Multi-Tenant RBAC & Isolation Boundaries

- **Tenant-Isolated Vector Store:** Every vector table in PostgreSQL includes a `tenant_id TEXT` column with compound indexes. All SQL queries enforce tenant filtering (`WHERE tenant_id = ?`).
- **Tenant Context Propagation:** Request metadata carries `tenantId`, enforced across `TenantContext` and validated by `TenantIsolationEnforcer`.
- **Fail-Closed Authorization Gate:** `DefaultRuntimeService.graphPermissionManager()` evaluates `ExecutionCapability` before dispatching any tool, subprocess, or terminal action. Any unmapped action or unexpected parameter results in an immediate, safe `PermissionDecision.DENY`.

---

## 8. 3-Minute Quickstart

### Prerequisites

| Requirement | Supported Version | Notes |
|---|---|---|
| **JDK** | 21+ | Eclipse Temurin / Adoptium recommended |
| **Docker** | 24+ | Required for PostgreSQL + pgvector |
| **Maven** | 3.9+ | Bundled `./mvnw` wrapper available |

### Step 1 — Start the pgvector Database

```bash
docker compose up -d
docker compose ps
# shree-postgres-vector  healthy (up)  0.0.0.0:5432->5432/tcp
```

### Step 2 — Configure Environment

```bash
export GEMINI_API_KEY=your_gemini_key_here
# Optional: export OPENAI_API_KEY=your_openai_key_here
```

### Step 3 — Launch the Application Playground

```bash
mvn clean spring-boot:run -pl application/shree-playground
```

The service boots on **http://localhost:7070**.

---

## 9. Ready-to-Use cURL Examples

### Ingest Knowledge into Vector Store

```bash
curl -s -X POST http://localhost:7070/api/playground/knowledge/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "title": "PostgreSQL pgvector Architecture",
    "content": "pgvector extends PostgreSQL with native vector similarity search. Using HNSW indexes, it calculates cosine distances over high-dimensional embeddings. Fused with GIN full-text indexes via RRF, it delivers robust hybrid retrieval."
  }' | jq .
```

### Grounded Chat Query with Citations

```bash
curl -s -X POST http://localhost:7070/api/playground/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How does Shree AI OS perform hybrid vector retrieval?"}' | jq .
```

**Verified Response Payload Structure:**
```json
{
  "status": "SUCCESS",
  "data": {
    "response": "Shree AI OS performs hybrid vector retrieval by executing dense KNN search via pgvector HNSW and sparse full-text search via PostgreSQL GIN in parallel, fusing both result rankings with Reciprocal Rank Fusion (RRF)...",
    "citations": [
      {
        "chunkId": "c4b3a120-f19e-4e88-b21b-86d149021e1a",
        "title": "PostgreSQL pgvector Architecture",
        "excerpt": "pgvector extends PostgreSQL with native vector similarity search. Using HNSW indexes...",
        "score": 0.965
      }
    ],
    "intent": "KNOWLEDGE_QUERY",
    "confidence": 0.95,
    "traceId": "trace-94a2-11ef"
  }
}
```

---

## 10. Repository & Architecture Boundaries

```
shree-ai-os/
|-- docker-compose.yml           # PostgreSQL 16 + pgvector 0.7
|-- application/
|   +-- shree-playground/       # REST API reference application (port 7070)
|-- src/
|   +-- main/java/com/shreeai/os/
|       +-- platform/
|           +-- core/           # Registry, lifecycle, discovery, event bus
|           +-- kernels/        # Identity, Memory, Knowledge, Planning,
|           |                   # Reasoning, Inference, Reflection, Developer
|           +-- runtime/        # 11-stage pipeline, pgvector store, agents
|           +-- sdk/            # Public 10-SDK developer facade
+-- docs/                       # Platform specifications and guides
```

---

## 11. Constitutional Governance Rules

1. **R1 (Isolation):** No canonical runtime code imports legacy or test code.
2. **R2 (Single Source of Truth):** `DefaultRuntimeService` and canonical kernels govern all execution state.
3. **R3 (Frozen Signatures):** Public SDK method signatures remain stable across the `1.0.x` preview series.
4. **R4 (Fail-Closed Security):** All authorization gates default to `DENY` upon ambiguity or exception.
5. **R5 (Continuous Green Verification):** Every phase and build passes all 56+ verification test suites without regressions.

---

Platform: **Shree AI OS**  
Document: **Cognitive Runtime Architecture**  
Version: **1.0.6-developer-preview**  
Author: **Darshan Rathod**