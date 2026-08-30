# PHASE 1 — ARCHITECTURE REVIEW & FILE IMPACT REPORT

**Phase:** 1 — Real Intelligence Engines (V2.2 → V3)
**Status:** REVIEW COMPLETE — AWAITING APPROVAL
**Scope of this document:** Architecture review only. No implementation code has been written.

---

## 1. ARCHITECTURE REVIEW OF EXISTING IMPLEMENTATION

### 1.1 Packages Reviewed

| Package | Exists? | Current State |
|---|---|---|
| `platform.kernels.knowledge` | YES | Full hexagonal kernel: `api` (5 frozen ports), `engine`, `model` (immutable DTOs), `validation`, `verification`, `error` |
| `platform.kernels.memory` | YES | Full kernel with `MemorySearchService.search(String)` contract, `MemoryVersionLedger` in engine |
| `platform.runtime.vector` | **NO — MUST BE CREATED** | No canonical vector runtime exists |
| `platform.runtime.storage` | **NO — MUST BE CREATED** | No canonical persistence runtime exists |

### 1.2 Verified Findings

**F-1. Vector intelligence is placeholder and trapped in legacy.**
`platform.legacy.memory.VectorMemoryStore` is an in-JVM `ArrayList` with cosine search;
`platform.legacy.memory.MemoryEmbedder` produces a naive 10-slot char-sum "embedding".
Per Constitutional Rule R1, canonical code must NOT import `platform.legacy`. All vector
capability must therefore be **re-hosted canonically** under `platform.runtime.vector`,
and legacy `MemoryEmbedder` is upgraded by **promote-and-delegate** (Rule R2).

**F-2. Knowledge storage is in-memory only.**
`DefaultKnowledgeService` holds a mutable `KnowledgeGraph` field
(`KnowledgeGraph.empty()` → reassigned on every create/update). This contradicts its own
"stateless" documentation and is **not thread-safe**. It is also constructed directly
inside `DefaultRuntimeService` (`new DefaultKnowledgeService(knowledgeEngine)`) with no
injection point for a store. This is the correct single seam for persistence.

**F-3. Search is lexical, not semantic.**
`DefaultKnowledgeSearchEngine` scores label/description/tag term overlap
(`LABEL_WEIGHT`, tag weights). `KnowledgeGroundingService` scores 50% term coverage +
50% evidence quality (node confidence/authority metadata). Both are deterministic and
unit-tested; neither uses embeddings. Grounding ≥ 0.90 is achievable today only for
trivially-matching queries — semantic grounding is required for the acceptance bar.

**F-4. The SDK dispatch contract is metadata-driven and already frozen.**
`KnowledgeSDK.query/retrieve/search` build `SDKRequest` with `metadata.operation`
(`QUERY_KNOWLEDGE`, `RETRIEVE_ENTITY`, `SEARCH_KNOWLEDGE`) → `ShreeClient.chat()` →
`DefaultRuntimeService` → `RuntimeIntentRouter.route()` (deterministic routing table)
→ Knowledge kernel. Adding ingestion therefore requires exactly one new routing entry —
no contract changes.

**F-5. There is no persistence infrastructure at all.**
`pom.xml` contains only web, security, jackson, okhttp, lombok, test. No JDBC/JPA driver,
no datasource configuration (`application.properties` has none), no Redis. Any storage
adapter must bring its own driver dependency and degrade gracefully when no database is
configured so that the existing green test suite (`mvnw clean test`) stays green.

**F-6. Graph abstraction exists and must be preserved.**
`KnowledgeGraphService` (createRelationship / removeRelationship / queryConnections /
traverseGraph / getEntityRelationships) is the graph port. A Neo4j adapter must sit
behind a **store SPI**, never hard-code a provider. Legacy `KnowledgeGraphEngine`
(legacy/graph, JSON-file backed) is quarantined and must not be imported.

**F-7. Two dispatch paths exist — logic must not be duplicated.**
(a) 10-stage canonical pipeline (`KnowledgeStage` in `runtime.pipeline.stages`), and
(b) capability dispatch (`KernelRegistry` + `ExecutionDispatcher` in `runtime.execution`).
Ingestion logic must live **once** in the Knowledge Kernel engine and both paths must
delegate to the same service.

**F-8. Reflection Kernel exists and is out of scope for Phase 1.**
`DefaultReflectionEngine` (kernels.cognitive.engine), `ReflectionKernelHandler`,
`AdaptiveReflectionEngine` (intelligence.reflection), `ReflectionStage`. Phase 1.5 will
extend these; Phase 1 must not touch them.

### 1.3 Target Architecture (Phase 1)

```
                SDK (frozen, additive only)
  KnowledgeSDK.ingest(title, content)   ← new method, additive
        │  metadata.operation = INGEST_KNOWLEDGE
        ▼
  ShreeClient ──► RuntimeIntentRouter ──► "INGEST_KNOWLEDGE" → KNOWLEDGE   (1 new entry)
        │
        ▼
  Knowledge Kernel (kernels.knowledge) — single source of ingestion logic
  ┌──────────────────────────────────────────────────────────────┐
  │ KnowledgeIngestionService (new api port)                     │
  │ DefaultKnowledgeIngestionEngine (chunk → embed → persist)    │
  │ DefaultKnowledgeService (delegates; graph state → store SPI) │
  │ KnowledgeGroundingService (semantic score, API preserved)    │
  └──────────────┬───────────────────────────────────────────────┘
                 │ ports (hexagon boundary)
  ┌──────────────▼───────────────────────────────────────────────┐
  │ runtime.vector  (SPI)                                        │
  │   VectorStore            → InMemoryVectorStore | PgVectorMemoryStore
  │   VectorSearchEngine     → InMemoryVectorSearchEngine | PgVectorSearchEngine
  │   EmbeddingProvider      → LocalDeterministicEmbedder | OpenAiCompatibleEmbedder
  │ runtime.storage (SPI)                                        │
  │   EmbeddingRepository    → PgEmbeddingRepository             │
  │   KnowledgeGraphStore    → InMemoryKnowledgeGraphStore | Neo4jKnowledgeGraphAdapter
  └──────────────────────────────────────────────────────────────┘
```

**Provider selection is configuration-driven** (`shree.vector.provider=in-memory|pgvector`,
`shree.knowledge.graph.provider=in-memory|neo4j`), defaulting to in-memory. No kernel code
references a concrete provider. Spring wiring stays out of the kernels (kernels remain
plain-Java, constructor-injected, thread-safe).

**Grounding score model (≥ 0.90 acceptance):**

```
groundingScore = 0.40 * semanticSimilarity   (cosine: query embedding vs. cited-node embeddings)
               + 0.30 * evidenceQuality      (existing node confidence/authority model)
               + 0.30 * termCoverage         (existing lexical model)
```

Semantic similarity is computed against embeddings persisted at ingestion time, so
ingested documents retrieve their own evidence at high cosine similarity → grounded
payloads exceed 0.90. The public `ground()` and `groundingScore(...)` signatures are
unchanged; a no-arg constructor is retained for full backward compatibility.

**Embedding dimension:** fixed contract (`EmbeddingProvider.dimensions()`, default 256
for the local deterministic provider; configurable when an external provider is used).
PgVector DDL uses `vector(<dim>)` aligned to the configured provider.

---

## 2. FILE IMPACT REPORT

### 2.1 Files to CREATE

| # | File | Purpose |
|---|---|---|
| C1 | `runtime/vector/VectorStore.java` | Port: store/list/delete vector records |
| C2 | `runtime/vector/VectorRecord.java` | Immutable record: id, text, embedding, metadata, createdAt |
| C3 | `runtime/vector/VectorSearchEngine.java` | Port: top-K similarity search |
| C4 | `runtime/vector/VectorSearchResult.java` | Immutable result (record id, score) |
| C5 | `runtime/vector/EmbeddingProvider.java` | Port: `embed(String) → double[]`, `dimensions()` |
| C6 | `runtime/vector/CosineSimilarity.java` | Stateless cosine util (single canonical implementation) |
| C7 | `runtime/vector/LocalDeterministicEmbedder.java` | Production-capable default embedder (hashed bag-of-ngrams → normalized dense vector; no external dependency) |
| C8 | `runtime/vector/InMemoryVectorStore.java` | Thread-safe (`ConcurrentHashMap`) default store |
| C9 | `runtime/vector/InMemoryVectorSearchEngine.java` | Brute-force cosine top-K |
| C10 | `runtime/vector/PgVectorMemoryStore.java` | JDBC adapter of `VectorStore` (pgvector) |
| C11 | `runtime/vector/PgVectorSearchEngine.java` | pgvector `<=>` KNN adapter of `VectorSearchEngine` |
| C12 | `runtime/vector/OpenAiCompatibleEmbedder.java` | Optional remote embedder via existing OkHttp dependency |
| C13 | `runtime/vector/VectorRuntimeException.java` | Adapter failure translation |
| C14 | `runtime/storage/EmbeddingRepository.java` | Port: persist/load embeddings keyed by document/node id |
| C15 | `runtime/storage/PgEmbeddingRepository.java` | JDBC implementation (table `shree_embedding`) |
| C16 | `runtime/storage/KnowledgeGraphStore.java` | Port behind `KnowledgeGraphService` abstraction (CRUD + traversal) |
| C17 | `runtime/storage/InMemoryKnowledgeGraphStore.java` | Default store extracted from current engine state |
| C18 | `runtime/storage/Neo4jKnowledgeGraphAdapter.java` | Neo4j driver adapter (lazy init; only active when configured) |
| C19 | `kernels/knowledge/api/KnowledgeIngestionService.java` | Additive kernel port: `ingest(title, content) → KnowledgeIngestionResult` |
| C20 | `kernels/knowledge/model/KnowledgeIngestionResult.java` | Immutable DTO (documentId, chunkCount, nodeIds) |
| C21 | `kernels/knowledge/engine/DefaultKnowledgeIngestionEngine.java` | THE single ingestion implementation: chunk → embed → VectorStore + EmbeddingRepository + KnowledgeGraphStore node creation |
| C22 | `runtime/routing/` routing entry | Additive: `INGEST_KNOWLEDGE → TargetKernel.KNOWLEDGE` (edit to C23-listed file, see 2.2) |
| C23 | `src/main/resources/db/pgvector-schema.sql` | DDL: `shree_vector_memory`, `shree_embedding` (tenant-ready columns reserved) |
| C24 | Tests (see 2.4) | Unit + integration suites |

### 2.2 Files to MODIFY (all changes additive or behavior-preserving)

| # | File | Change | Compatibility Impact |
|---|---|---|---|
| M1 | `sdk/KnowledgeSDK.java` | Add `ingest(String title, String content)` building `SDKRequest(message="KNOWLEDGE_INGEST", metadata={operation: INGEST_KNOWLEDGE, title, content})` | **None** — additive method on frozen class; existing methods untouched |
| M2 | `runtime/routing/RuntimeIntentRouter.java` | Add `INGEST_KNOWLEDGE → KNOWLEDGE` to the routing table | None — additive entry; existing routes and tests unchanged |
| M3 | `kernels/knowledge/service/DefaultKnowledgeService.java` | (a) graph state moves behind `KnowledgeGraphStore`; (b) `AtomicReference` for graph to fix F-2 thread-safety; (c) implements `KnowledgeIngestionService` by delegation to the engine; (d) new constructor accepting store + ingestion engine; **old constructor `DefaultKnowledgeService(processingEngine)` preserved** delegating with in-memory defaults | Existing tests constructing via old constructor keep working |
| M4 | `kernels/knowledge/engine/KnowledgeGroundingService.java` | New weighted score model (§1.3) with constructor-injected `EmbeddingProvider`; no-arg constructor preserved (lexical-only fallback) | Signatures unchanged; existing unit test values re-validated and updated where scores shift |
| M5 | `runtime/pipeline/stages/KnowledgeStage.java` | Additive branch: when operation = `INGEST_KNOWLEDGE`, invoke `KnowledgeIngestionService`; query/search behavior unchanged | None |
| M6 | `runtime/service/DefaultRuntimeService.java` | Wire ingestion service into the Knowledge stage/chain; optional injection seam for a configured store (defaults preserved) | Internal wiring only; no public contract |
| M7 | `legacy/memory/MemoryEmbedder.java` | Promote-and-delegate: delegates `embed()` to canonical `EmbeddingProvider` (Rule R2); signature and `@Component` retained | Legacy parity tests unaffected |
| M8 | `pom.xml` | Add `org.postgresql:postgresql` (runtime), `org.neo4j.driver:neo4j-java-driver` (runtime, optional use), `spring-boot-starter-jdbc`, test-scope Testcontainers (`postgresql`) | Build-only; no runtime behavior change when DB unconfigured |
| M9 | `application.properties` | Documented, default-off properties: `shree.vector.provider=in-memory`, `shree.knowledge.graph.provider=in-memory`, `shree.embedding.provider=local`, `shree.embedding.dimensions=256`, optional datasource block (commented) | Defaults keep current behavior |
| M10 | `kernels/knowledge/api/package-info.java` + kernel READMEs | Document new port and storage SPIs | Docs only |

### 2.3 Dependencies

* **New build deps:** postgresql JDBC driver, pgvector type handling via plain SQL
  (`?::vector` casting — no extra ORM), Neo4j Java driver, spring-jdbc (`JdbcClient`),
  Testcontainers (test scope only).
* **New internal dependency directions (enforced):**
  `kernels.knowledge → runtime.vector / runtime.storage` (kernels depend on SPIs only);
  `runtime.service → kernels.knowledge` (existing);
  `sdk → runtime` (existing); **no** `kernels.* → platform.legacy`, **no** `runtime.* → platform.legacy`.
* **Reuse:** OkHttp (remote embedder), Jackson (payload serialization), existing
  `KnowledgeValidator`, `KnowledgeProcessingEngine`, `KnowledgeRankingService`.

### 2.4 Tests to CREATE

| Test | Verifies |
|---|---|
| `runtime/vector/CosineSimilarityTest` | Orthogonal / identical / opposite / zero-norm vectors |
| `runtime/vector/InMemoryVectorStoreTest` | Store, list, thread-safety (concurrent writes) |
| `runtime/vector/LocalDeterministicEmbedderTest` | Determinism, dimension contract, normalization |
| `kernels/knowledge/DefaultKnowledgeIngestionEngineTest` | Ingest → chunk → embed → persisted; re-ingest idempotency |
| `kernels/knowledge/KnowledgeIngestionRetrievalTest` | Ingested document **becomes searchable semantically** (query terms ≠ document terms still matches) |
| `kernels/knowledge/KnowledgeGroundingSemanticTest` | Grounding score ≥ 0.90 for ingested-document evidence; unchanged API |
| `runtime/storage/Neo4jKnowledgeGraphAdapterTest` | Graph CRUD + traversal against the adapter (driver mocked; no hardcoded provider) |
| `runtime/vector/PgVectorIntegrationTest` | PgVector store/search round-trip — **Testcontainers, skipped when Docker unavailable** so CI stays green |
| `verification/KnowledgeIngestSdkIntegrationTest` | `ShreeAI.builder().apiKey("local").build().knowledge().ingest(...)` → `query(...)` returns grounded answer; existing routing integration test still passes |

### 2.5 Migration Strategy

1. **Step 1 — SPIs first:** create `runtime.vector` + `runtime.storage` ports with
   in-memory defaults. Zero behavioral change; full suite green.
2. **Step 2 — Kernel seam:** move `DefaultKnowledgeService` graph state behind
   `KnowledgeGraphStore`; preserve all public/legacy constructors. Suite green.
3. **Step 3 — Ingestion:** add `KnowledgeIngestionService`, routing entry, SDK method,
   stage branch. New tests green; old tests untouched.
4. **Step 4 — Grounding upgrade:** semantic score model with backward-compatible
   constructors. Re-validate grounding tests.
5. **Step 5 — Persistence adapters:** PgVector + Neo4j adapters behind configuration
   flags (off by default) + Testcontainers ITs.
6. **Step 6 — Legacy promote-and-delegate** for `MemoryEmbedder` (R2) and completion
   report `docs/PHASE_1_COMPLETION_REPORT.md`.

No data migration is required: existing stores are ephemeral (in-JVM / legacy JSON).

### 2.6 Risk Assessment

| Risk | Severity | Mitigation |
|---|---|---|
| Grounding ≥ 0.90 depends on embedding quality | HIGH | Local deterministic embedder is tuned for lexical overlap (guarantees retrieval of ingested docs); remote `OpenAiCompatibleEmbedder` optional via config; score formula weights ensure grounded evidence dominates |
| PgVector dimension mismatch | MEDIUM | Single source of truth: `EmbeddingProvider.dimensions()`; DDL parameterized; startup validation |
| Neo4j driver on classpath even when unused | LOW | Lazy driver init; adapter only instantiated when `shree.knowledge.graph.provider=neo4j` |
| Thread-safety regression in `DefaultKnowledgeService` refactor | MEDIUM | `AtomicReference`/immutable snapshot swap; concurrency unit test; existing verifier suites retained |
| Duplicate ingestion logic across the two dispatch paths | HIGH (architectural) | Ingestion implemented ONCE in `DefaultKnowledgeIngestionEngine`; stage and capability dispatch both delegate |
| Test suite requires Docker for PgVector IT | MEDIUM | Testcontainers tests tagged + auto-skipped when Docker absent; acceptance demonstrated locally and documented in completion report |
| Frozen SDK drift | HIGH | `KnowledgeSDK.ingest` is purely additive; `ShreeAI`, `SDKRequest`, `SDKResponse`, other SDKs untouched; `IntelligenceFoundationPreservationTest` + routing tests gate regressions |
| Legacy isolation (R1) | MEDIUM | Only allowed change in legacy is delegation out of `MemoryEmbedder` to canonical embedder (R2 promote-and-delegate); `CanonicalIsolationTest` continues to enforce |

### 2.7 Acceptance Criteria Mapping (Phase 1)

| Acceptance Criterion | Satisfied By |
|---|---|
| Document ingestion | C21 + M1 + M2 + M5; `KnowledgeIngestionRetrievalTest`, `KnowledgeIngestSdkIntegrationTest` |
| Semantic retrieval | `runtime.vector` SPI + cosine engines (C6–C11); retrieval test with non-lexical query match |
| Graph traversal | `KnowledgeGraphStore` port + `Neo4jKnowledgeGraphAdapter` (C16–C18) + adapter test |
| Grounding score ≥ 0.90 | M4 semantic score model + `KnowledgeGroundingSemanticTest` |
| Integration tests | §2.4 suite; `mvnw clean test` green with defaults |
| `docs/PHASE_1_COMPLETION_REPORT.md` | Generated at phase completion (Step 6) |

---

## 3. CONSTITUTIONAL COMPLIANCE CHECK

| Rule | Compliance |
|---|---|
| R1 — no canonical imports of `platform.legacy` | Vector capability is re-hosted canonically; legacy only delegates outward |
| R2 — promote-and-delegate; no duplicated logic | `MemoryEmbedder` delegates; ingestion implemented once; cosine implemented once in `CosineSimilarity` |
| R3 — frozen public API surfaces | `KnowledgeSDK.ingest` additive; all other SDK signatures byte-identical |
| R5 — phase ends green | In-memory defaults preserve current test behavior; DB features opt-in |
| Hexagonal architecture | Kernels depend on SPI ports; adapters (PgVector, Neo4j, in-memory) plug in from outside |
| Constructor injection | All new classes constructor-injected; no field injection, no service locator |
| Thread safety | `ConcurrentHashMap` stores, immutable DTOs, atomic graph snapshots |
| No hardcoded provider | Provider selection via configuration; kernels never reference adapters |

---

**END OF PHASE 1 ARCHITECTURE REVIEW — implementation will not begin until this document is approved.**
