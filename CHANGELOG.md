# Changelog

All notable changes to **Shree AI OS** are documented in this file.

This project follows **Semantic Versioning** for stable releases. Current releases are distributed as **Developer Preview** builds.

---

## [1.0.7-developer-preview] - 2026-11-26

Runtime wiring hardening, fail-closed capability closure, grounded streaming, and deterministic CI stabilization release.

### Added
- **`ShreeAI` `AutoCloseable` Support**: Implemented `AutoCloseable.close()` on `ShreeAI` delegating cleanly to `runtime.stop()` so `try-with-resources` and `shree.close()` work out-of-the-box.
- **ONNX Degradation Observability**: Added `isDegraded()` and `degradationReason()` on `OnnxEmbeddingProvider` and verified real 384-dim `all-MiniLM-L6-v2` semantic inference.
- **GitHub Actions CI**: Added `.github/workflows/ci.yml` for automated JDK 21 test execution on push/PR.
- **Runtime Wiring Verification Suites**: Added `RuntimeWiringStep2VerificationTest`, `RuntimeWiringStep2Part2VerificationTest`, and `RuntimeWiringStep2Part3VerificationTest` (2,807 total tests passing).

### Fixed
- **Builder `apiKey()` Wiring**: Wired `ShreeAI.builder().apiKey(...)` through `ByokSettingsService` into `DefaultRuntimeService.rebuildLlmRouter()` so programmatic API keys activate `OpenAI` / `Gemini` providers immediately without requiring env vars.
- **Fail-Closed `graphPermissionManager()` Closure**: Closed fail-open gap on unmapped high-risk capabilities (`TOOLS`, `MODELS`, `AGENTS`) in `DefaultRuntimeService.graphPermissionManager()` so they strictly return `PermissionDecision.DENY`.
- **Grounded Streaming (`chatStream` / `streamText`)**: Routed `DefaultRuntimeService.streamText()` through the cognitive pipeline, `EvidenceAgent`, `VerificationAgent`, and `NaturalResponseAgent.buildStreamingRequest(...)` so streaming enforces RAG grounding and prompt-injection guardrails.
- **General Assistance Mode & Model Config**: Updated `NaturalResponseAgent` to serve direct general-assistance answers via configured LLM providers when KB evidence is empty (falling back to the deterministic template only when no live LLM is available), and to respect `shree.llm.model`.
- **Knowledge Ingestion Error Surfacing**: Replaced silent exception swallowing in `KnowledgeStage` with explicit `knowledgeIngestionErrors` metadata and state messages.
- **Sub-App Dependency Alignment & Surefire Speedup**: Aligned `application/shree-playground/pom.xml` (Spring Boot `4.0.2`, `onnxruntime 1.29.0`, `tokenizers 0.36.0`) and `shree-developer-intelligence/pom.xml` to `1.0.7-developer-preview`; configured deterministic `shree.llm.chain=in-memory` default in Surefire (~4.7x faster full test sweep).

---

## [1.0.6-developer-preview] - 2026-09-20

Hardened enterprise developer preview release featuring Phase 5 static analysis remediation, fail-closed authorization, robust multi-provider LLM failover with exponential backoff retries, and comprehensive adversarial verification.

### Added

#### Verification & Reliability Suites
- Added `Phase5StaticRemediationVerificationTest`: Comprehensive regression test suite validating all P0/P1 static analysis remediations under 1,000 concurrent operations.
- Added `PlatformAdversarialChaosIntegrationTest`: Stress testing runtime resilience against thread interruption, malformed inputs, out-of-order execution, and memory pressure.
- Added `ProductionBugSweepVerificationTest`: Regression suite sweeping for race conditions, deadlock vectors, and concurrency leaks.
- 56/56 automated test suites passing with 100% green verification.

#### Cognitive Pipeline & Knowledge Architecture
- **Dual-Mode Synthesis Strategy**: Implemented dual-path generation in `NaturalResponseAgent`:
  - *Mode A (Strict RAG Grounding)*: Verifiable citation tracking (`chunkId`, `title`, `excerpt`, `score`) with non-grounded speculative assertions filtered out.
  - *Mode B (General Assistance Fallback)*: Deterministic conversational assistance when no domain knowledge matches or queries are open-ended, preventing hallucinations while maintaining dialogue continuity.
- **K0.6 Autonomous Knowledge Acquisition Engine**:
  - Implemented query domain isolation in `DefaultKnowledgeContentResolver`: strictly blocks cross-domain leakage between Java, JavaScript, Python, and Healthcare.
  - Deterministic canonical domain generators providing verified architectural specifications when external retrieval is offline or unavailable.
- Fully wired the hardened 11-stage cognitive execution pipeline (`IdentityStage` through `ChiefReviewStage`).

### Fixed

- **P0 Fail-Closed Authorization Gate**: Remediated authorization evaluation in `DefaultRuntimeService.graphPermissionManager` to strictly return `PermissionDecision.DENY` on `NullPointerException`, `IllegalArgumentException`, or unmapped capabilities.
- **P0 Subprocess Stream Pipe Deadlocks**: Fixed OS pipe buffer saturation and thread blocking in `GitTool` and `TerminalTool` by consuming `stdout` and `stderr` asynchronously on separate worker threads.
- **UTF-8 Charset Enforcement**: Enforced explicit `StandardCharsets.UTF_8` across all `InputStreamReader` and stream-decoding operations in `TerminalTool`, `GitTool`, and `DefaultKnowledgeContentResolver`, eliminating platform-default encoding bugs.
- **Thread-Safe Memory & Diagnostic Counters**: Replaced racy volatile compound increments with lock-free `AtomicInteger` and `LongAdder` counters in `SdkDiagnosticsService`.
- **Playground Concurrency**: Wrapped conversation history traversals in `PlaygroundSessionService` with synchronized snapshot blocks, preventing `ConcurrentModificationException` during concurrent appends.
- **Token Truncation**: Resolved SSE token fragment truncation in provider stream listeners.
- **Knowledge Deduplication**: Fixed document chunk re-indexing and deduplication in `DefaultDocumentIngestionEngine` and `MemoryRecallNormalization`.
- **Exception Serialization**: Added `serialVersionUID` and transient modifiers on non-serializable fields across all core and kernel exceptions (`ConfigurationException`, `PlatformException`, `ValidationException`, etc.).
- **SDK Null Client Validation**: Added strict `Objects.requireNonNull(client)` guards across `IdentitySDK`, `PlanningSDK`, and `ReflectionSDK` constructors.
- **Memory Leak in Validation**: Replaced double-brace initialization capturing enclosing outer instances in `CognitiveValidator` with explicit mutable `HashMap` instances.

### Changed

- **Model Routing & LLM Resilience**:
  - Updated default model routing to `gemini-3.6-flash` (supporting `gemini-2.5-flash` / `gemini-1.5-flash` overrides).
  - Integrated automatic retry loops with exponential backoff on HTTP 429 (Rate Limit) and HTTP 503 (High Demand / Service Unavailable) errors in `GeminiProvider`.
  - Configured deterministic `InMemoryLlmProvider` as the zero-failure terminal fallback across the provider chain.
- **Version Coordinates**: Promoted project coordinates across `pom.xml` and documentation to `1.0.6-developer-preview`.
- **Documentation Suite**: Completely refreshed and synchronized all five documents in `docs/developer/` (`COGNITIVE_RUNTIME_ARCHITECTURE.md`, `DEVELOPER_CAPABILITIES.md`, `PLATFORM_IDENTITY.md`, `QUICKSTART_DEVELOPER_GUIDE.md`, and `WORKING_STATUS.md`).

---

## [1.0.0-developer-preview] - 2026-09-05

Initial public developer preview of **Shree AI OS**, an in-process deterministic cognitive runtime for Java 21.

### Added

#### Runtime
- Deterministic runtime substrate for Java 21
- 11-stage cognitive orchestration pipeline
- RuntimeIntentRouter for capability-based routing
- MultiKernelOrchestrator for parallel multi-intent execution
- RuntimeEventBus for publish/subscribe workflows
- Runtime lifecycle management (`start()`, `stop()`, `status()`)

#### Public SDK
- MemorySDK, KnowledgeSDK, PlanningSDK, ExecutionSDK
- ReasoningSDK, ReflectionSDK, InferenceSDK, IdentitySDK
- ProjectSDK, SettingsSDK

#### Kernels
- Memory Kernel, Knowledge Kernel, Planning Kernel
- Execution Kernel, Identity Kernel, Reasoning Engine
- Inference Engine, Adaptive Reflection Engine

#### LLM Platform
- OpenAI provider, Gemini provider, Ollama provider
- OpenAI-compatible provider, In-memory deterministic provider
- Automatic provider fallback chain

#### Hybrid RAG
- Local ONNX embedding engine (384 dimensions)
- PostgreSQL + pgvector integration (HNSW + GIN FTS fused via RRF)
- Citation-based grounded retrieval

---

## Versioning

| Version | Status | Release Date | Notes |
|---|---|---|---|
| `1.0.7-developer-preview` | **Active** | 2026-11-26 | Runtime wiring hardening, grounded streaming, AutoCloseable SDK, deterministic CI |
| `1.0.6-developer-preview` | Deprecated | 2026-09-20 | Superseded by 1.0.7 |
| `1.0.5-developer-preview` | Deprecated | 2026-09-12 | Superseded by 1.0.6 |
| `1.0.0-developer-preview` | Deprecated | 2026-09-05 | Initial preview release |