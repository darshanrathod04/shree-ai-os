# Changelog

All notable changes to **Shree AI OS** are documented in this file.

This project follows **Semantic Versioning** for stable releases. Current releases are distributed as **Developer Preview** builds.

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
- MemorySDK
- KnowledgeSDK
- PlanningSDK
- ExecutionSDK
- ReasoningSDK
- ReflectionSDK
- InferenceSDK
- IdentitySDK
- ProjectSDK
- SettingsSDK

#### Kernels
- Memory Kernel
- Knowledge Kernel
- Planning Kernel
- Execution Kernel
- Identity Kernel
- Reasoning Engine
- Inference Engine
- Adaptive Reflection Engine

#### LLM Platform
- OpenAI provider
- Gemini provider
- Ollama provider
- OpenAI-compatible provider
- In-memory deterministic provider
- Automatic provider fallback chain

#### Hybrid RAG
- Local ONNX embedding engine (384 dimensions)
- PostgreSQL + pgvector integration
- HNSW vector indexing
- GIN full-text indexing
- Reciprocal Rank Fusion (RRF)
- Citation-based grounded retrieval

#### Developer Tooling
- Project Intelligence SDK
- Java AST analysis
- Spring component discovery
- Developer Agent API

### Improved

- Dynamic BYOK hot reload without JVM restart
- Real provider token streaming (SSE/NDJSON)
- Typed Planning APIs
- Typed Identity resolution
- Reflection analytics APIs
- Tenant isolation enforcement
- Reduced ONNX native memory usage
- Repository governance and documentation structure

### Fixed

- Gemini custom model routing
- SSE token truncation
- ONNX extraction reliability
- Reflection tenant fallback
- Docker-aware pgvector integration tests

### Documentation

Added official documentation suite:

- PLATFORM_IDENTITY.md
- DEVELOPER_CAPABILITIES.md
- WORKING_STATUS.md
- QUICKSTART_DEVELOPER_GUIDE.md
- SECURITY.md
- CONTRIBUTING.md
- CODE_OF_CONDUCT.md

### Verification

- 1,230+ automated tests
- 100% passing test suite
- Production runtime wiring verified
- Public SDK surface verified

---

## Versioning

| Version                 | Status  |
|-------------------------|---------|
| 1.0.0-developer-preview | Active  |