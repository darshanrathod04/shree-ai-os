# Changelog

All notable changes to **Shree AI OS** will be documented in this file.

The project follows a **Developer Preview** release cadence leading up to Semantic Versioning (`MAJOR.MINOR.PATCH`) for stable releases.

---

## [v1.0.0-developer-preview] - 2026-09-05

The initial public Developer Preview release of Shree AI OS — an in-process, deterministic cognitive runtime substrate for Java 21.

### Added

#### Core Runtime & Orchestration
* **Deterministic Runtime Substrate:** Java 21-native in-process execution engine.
* **11-Stage Pipeline:** Formal stage orchestration (`Identity`, `Context`, `Memory`, `Knowledge`, `Reasoning`, `Inference`, `Planning`, `Execution`, `Reflection`, `Review`, `Synthesis`).
* **Intent Routing:** Deterministic `RuntimeIntentRouter` with capability-dispatch resolution.
* **Multi-Kernel Orchestrator:** Parallel fan-out and aggregation for multi-intent requests.
* **Event Bus:** In-process asynchronous pub/sub messaging via `RuntimeEventBus`.
* **Lifecycle Management:** Clean bootstrap, state monitoring, and graceful shutdown semantics.

#### Public SDK Surfaces (10-in-1 Substrate)
* `MemorySDK` — Episodic, semantic, procedural storage, ranking, and consolidation.
* `KnowledgeSDK` — Document ingestion, hybrid retrieval, and knowledge graph queries.
* `PlanningSDK` — Typed task DAG generation, constraint validation, and iterative refinement.
* `ExecutionSDK` — Deterministic workflow execution and plan dispatch.
* `ReasoningSDK` — Evidence-grounded deterministic premise evaluation.
* `ReflectionSDK` — Self-calibrating metacognitive evaluation and execution analytics.
* `InferenceSDK` — Structured schema extraction and classification.
* `IdentitySDK` — Request-scoped actor resolution and profile governance.
* `ProjectSDK` — Bytecode and AST analysis for Java workspaces.
* `SettingsSDK` — Dynamic runtime provider configuration and credential injection.

#### Substrate Kernels & Engines
* `MemoryKernel` supporting 10 distinct memory taxonomy models.
* `KnowledgeKernel` with sliding-window chunking and graph indexing.
* `PlanningKernel` with mathematical DAG task graphs and domain planners.
* `CognitiveKernels` (Reasoning Engine, Adaptive Reflection Engine, Inference Engine).
* `ExecutionKernel` with step-level rollback and retry policies.

#### LLM Adapters & Routing
* Multi-provider routing with automatic failover chain (`Google Gemini`, `OpenAI`, `Ollama`, `InMemoryLlmProvider`).
* Out-of-the-box in-memory deterministic fallback provider for testing without API keys.

#### Hybrid Retrieval-Augmented Generation (RAG)
* In-process local ONNX embedding provider (384-dimensional dense vectors).
* PostgreSQL + `pgvector` persistence support.
* Dual search indexing: HNSW vector search + GIN full-text keyword indexing.
* Reciprocal Rank Fusion (RRF) algorithm for combined result re-ranking.
* Citation-based grounded response synthesis.

#### Project Intelligence
* Java workspace structure indexing and AST parsing.
* Spring Controller, Entity, and Repository component discovery.
* Architectural drift and dependency graph summarization.

---

### Improved

#### Sprint Release Hardening
* **BYOK Dynamic Hot-Reload:** `SettingsSDK.configureApiKey()` triggers observer listeners to rebuild the active `LlmRouter` chain in real time without restarting the JVM.
* **True Token Streaming:** Rewrote `ShreeClient.chatStream()` to consume raw provider SSE/NDJSON token streams via `Runtime.streamText()`.
* **Advanced Reflection APIs:** Exposed typed `ReflectionReport` and `ReflectionStatistics` models backed by `AdaptiveReflectionEngine`.
* **Typed Identity Integration:** Connected `IdentitySDK.resolve()` directly to `DefaultIdentityProcessingEngine`.
* **Tenant Isolation Enforcement:** Wired `TenantIsolationEnforcer` at `DefaultRuntimeService.submit()` and query boundaries to guard against cross-tenant data leakage.
* **Typed Planning APIs:** Surfaced `createPlanTyped()`, `validatePlanTyped()`, and `refinePlanTyped()` in `PlanningSDK`.

#### Performance & Native Resource Management
* Singleton session reuse for in-process ONNX model inference.
* Persistent cache layer for extracted ONNX native libraries.
* Reduced heap and native off-heap memory consumption during high-concurrency test runs.

#### Repository Hygiene
* Moved internal working scratchpads and audit logs to `/internal`.
* Cleaned repository root and created a standardized governance layout.

---

### Fixed

* Resolved model resolution bugs in `GeminiProvider` when routing custom fine-tuned IDs.
* Fixed chunk truncation and URL generation in SSE token streams.
* Resolved intermittent ONNX model extraction failures on low-disk-space environments.
* Corrected tenant context fallback resolution in `ReflectionStage` during cold bootstraps.
* Fixed `Testcontainers` execution to gracefully skip PostgreSQL tests when Docker daemon is not active.

---

### Documentation Suite

Added canonical technical guides:
* `PLATFORM_IDENTITY.md` — 5-layer architecture, invariants, and pipeline specs.
* `DEVELOPER_CAPABILITIES.md` — Complete public SDK reference with line-level caller traces.
* `WORKING_STATUS.md` — Forensic verification audit and capability test matrix.
* `QUICKSTART_DEVELOPER_GUIDE.md` — 5-minute hands-on tutorial.
* `SECURITY.md` — Vulnerability disclosure policy and security invariants.
* `CONTRIBUTING.md` — Engineering guidelines for runtime contributions.
* `CODE_OF_CONDUCT.md` — Community collaboration standards.

---

### Test Suite Verification

* **Automated Tests:** 1,230+ architecture, unit, and integration tests
* **Pass Rate:** 100% (0 failures, 0 errors).
* **Integration Fallback:** Docker/pgvector integration suites automatically skip in environments lacking container runtimes.

---

### Release Notes & Legal Notice

**Stability:** Developer Preview (API is expected to evolve based on community feedback before v1.0.0 stable).
* This release is intended strictly for developer evaluation, benchmarking, and architectural feedback.
* **License:** Source-available under the *Shree AI OS Proprietary Evaluation License v1.0*. Not licensed for production deployment or commercial redistribution without prior written consent.