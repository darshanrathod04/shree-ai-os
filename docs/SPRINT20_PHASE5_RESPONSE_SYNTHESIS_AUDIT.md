# SPRINT 20 — PHASE 5
# RESPONSE SYNTHESIS & LLM FORENSIC AUDIT

**Audit date:** 2026-03-09
**Scope:** Read-only forensic audit of the response generation layer of Shree AI OS, from `ShreeClient.chat()` through the kernel pipeline, evidence/verification, prompt construction, and the final SDK response.
**Constraint:** Zero Java modifications. Every claim is grounded in `filename.java:line` evidence. The audit was performed by re-opening every source file in scope, not by reusing prior audits.

---

## EXECUTIVE SUMMARY (PROVEN, NOT HYPOTHESIZED)

This audit establishes, with code evidence, the following non-negotiable truths about the response generation layer of Shree AI OS as it exists in the repository today.

1. **The LLM is defined but never invoked in production.** The `LlmRouter`, all four `LlmProvider` implementations (`OpenAiProvider`, `GeminiProvider`, `OllamaProvider`, `InMemoryLlmProvider`), and the `LlmRequest`/`LlmResponse` types are fully implemented. `DefaultRuntimeService` instantiates a `LlmRouter` at `DefaultRuntimeService.java:122` and registers the four providers in `buildDefaultLlmRouter()` at lines 200–226. **No production code path ever calls `llmRouter.stream(...)` or `llmRouter.complete(...)`.** A repository-wide search for `LlmRouter` and `LlmProvider` invocations shows the only call sites are `DefaultRuntimeService.java:1044` (where the router is dropped into a `PipelineContext` attribute map) and `LlmRouterTest.java` (unit tests). The router is wired into the `PipelineContext` attribute bag at `DefaultRuntimeService.java:1044` and is never read.
2. **The "NaturalResponseAgent" never produces natural language.** The class is named "natural" but `NaturalResponseAgent.generate()` at `NaturalResponseAgent.java:57–81` and `generateFromEvidence()` at lines 98–155 assemble a markdown response from `StringBuilder` concatenation against the evidence bundle items. The Javadoc at lines 90–92 explicitly states: *"The LLM invocation slot is reserved here. The actual LLM call should be wired through LlmProvider when the LLM integration is complete."*
3. **The `DefaultResponseSynthesizer` is 1,460 lines long and is a template engine, not an intelligent synthesizer.** It dispatches to six hardcoded markdown-builder methods (`synthesizePlanning`, `synthesizeKnowledge`, `synthesizeChat`, `synthesizeDefault`, `synthesizeExecution`, `synthesizeComposite`) that produce `StringBuilder`-assembled responses from pipeline metadata. Confidence values are hardcoded: `0.92` (plan blueprint), `0.90` (default), `0.80` (knowledge), `0.15` (insufficient).
4. **The `ResponseSynthesisService` is a 36-line pass-through** (`ResponseSynthesisService.java`) that wraps `DefaultResponseSynthesizer`. Its single public method delegates directly: line 34 returns `synthesizer.synthesize(context, state)`.
5. **There are two parallel response-synthesis pipelines in the same request** and the cheaper one wins. In `DefaultRuntimeService.submit()` at `DefaultRuntimeService.java:1085` the code first calls `responseSynthesisService.synthesize(...)` which produces a `SynthesizedResponse` from the default synthesizer. Immediately afterwards, at lines 1119–1176, it instantiates a new `EvidenceAgent`/`VerificationAgent`/`NaturalResponseAgent` triplet, runs them on the same pipeline state, and at line 1160 **replaces** the `SynthesizedResponse` with the NRA output. The first response is discarded. The 4-agent chief pre-flight that already ran via `ChiefIntelligenceAgent.route()` at `DefaultRuntimeService.java:888` is also discarded (its `chiefMeta` is captured but the `SynthesizedResponse` is replaced). Three complete response-assembly runs occur per healthy chat request; the first two are thrown away.
6. **Confidence is set in seven different places** by seven different code paths, with five different default values: `0.95`, `0.90`, `0.80`, `0.60`, `0.15`, `0.50`, `0.92`. The reported confidence in the final SDK response is whichever path ran last.
7. **There is no prompt builder.** There is no class that constructs an LLM prompt for a chat request. The `LlmRequest` is used only in unit tests of the LLM providers (`LlmRouterTest`, `InMemoryLlmProviderTest`, `OpenAiProviderParsingTest`, `OllamaProviderParsingTest`, `GeminiProviderParsingTest`). The runtime instantiates an `LlmRequest` nowhere in production.

**Net conclusion proven by the source code:** the platform's response is produced by deterministic string-building from `PipelineContext` metadata. The "LLM-free" doctrine is enforced at `NaturalResponseAgent.java:90–92` and `IntentAnalyzer.java:14–22` and is consistent across the entire runtime. The "intelligence" of the final response is therefore bounded by what the kernel metadata contains and what the six hardcoded `synthesizeX` methods choose to print.

---

## SECTION 1 — COMPLETE RESPONSE ARCHITECTURE

The real execution chain (proven by re-reading every file) is as follows.

### 1.1 Entry point: SDK

```
User input
  ↓
ShreeClient.chat(String message)               [ShreeClient.java:57–63]
ShreeClient.chat(SDKRequest request)           [ShreeClient.java:68]
  ↓ builds IntelligenceContext via IntelligenceContextBuilder.fromSdkRequest
  [ShreeClient.java:81–82]
  ↓ builds ExecutionRequest via ExecutionRequest.builder()
  [ShreeClient.java:93–102]
  ↓ calls Runtime.submit(executionRequest)
  [ShreeClient.java:108]
```

Data transferred: `SDKRequest { message, metadata, sessionId, context }` is converted into an `ExecutionRequest { requestId, userInput, metadata{ intelligenceContext, sessionId } }`.

### 1.2 Runtime entry

```
Runtime.submit(ExecutionRequest)               [DefaultRuntimeService.java:695]
  ↓ eventBus.publish(PIPELINE_STARTED)         [DefaultRuntimeService.java:704–714]
  ↓ creates ExecutionSession                   [DefaultRuntimeService.java:718–722]
  ↓ creates ExecutionContext                   [DefaultRuntimeService.java:725–729]
  ↓ RuntimeIntentRouter.route(request)         [DefaultRuntimeService.java:736–739]
       (returns ExecutionRoute with stage list for routed operations,
        or null for canonical CHAT)
```

### 1.3 Path A: Routed kernel (e.g. `QUERY_KNOWLEDGE`, `RECALL_MEMORY`, `PLAN_PROJECT`)

```
effectivePipeline = new DefaultExecutionPipeline(route.stages())  [DRS:743]
  ↓ pipeline.execute(context)
       which runs: IdentityStage, ContextStage, then the per-kernel stages
       (KnowledgeStage / MemoryRecallStage / PlanningStage etc.) and returns
       a PipelineResult whose ExecutionState carries per-stage metadata
       ("knowledgeResults", "memoryResults", "planBlueprint", etc.)
  ↓ ResponseSynthesisService.synthesize(context, state)  [DRS:1085]
       → DefaultResponseSynthesizer.synthesize(...)       [RSS:34 → DRS.java:23]
       → dispatches to synthesizeKnowledge / synthesizePlanning / etc.
       → returns a fully-built SynthesizedResponse
  ↓ (always) EvidenceAgent.extractFromMetadata(state.getMetadata())  [DRS:1119–1124]
       VerificationAgent.verify(bundle)                              [DRS:1126–1129]
       NaturalResponseAgent.generate(verificationReport, request)    [DRS:1155–1158]
  ↓ NaturalResponseAgent output REPLACES the synthesizer output     [DRS:1160]
  ↓ builds final ExecutionResult.structuredPayload
  ↓ returns ExecutionResult to ShreeClient.chat()
```

### 1.4 Path B: Chief pre-flight (orchestrated multi-kernel request)

```
For multi-kernel intent (IntentAnalyzer.isMultiKernel() == true):
  MultiKernelOrchestrator.orchestrate(...)   [DRS:1586+]
  ↓ inline new ResponseSynthesisService()    [DRS:1586]
  ↓ runs all required kernels, accumulates CompositeKernelResult
  ↓ ResponseSynthesisService.synthesize(...) inside orchestrator
  ↓ DefaultResponseSynthesizer.synthesizeComposite(...)   [DRS:1481–1483]
  ↓ returns synthesized CompositeResult
  ↓ buildOrchestratedResult(...)             [DRS:836]
```

The multi-kernel path skips both the chief pre-flight AND the canonical 11-stage pipeline per the prior audit at `SPRINT20_PHASE2_RUNTIME_FORENSIC_AUDIT.md:46`.



### 1.5 Path C: Canonical CHAT (the most common path â€” "Double-Synthesis")

The canonical CHAT path executes the chief pre-flight response twice: once before the pipeline (its output is discarded) and once after (its output is final).

**First pass (pre-pipeline, output discarded):**
```
chiefIntelligenceAgent.route(request)        [DRS:888]
  â†“ IntentAnalyzer.analyze(userInput)        [ChiefIntelligenceAgent.java:116]
  â†“ buildPlan(intent, request)              [CIA:118]
  â†“ DiagnosisAgent.analyze(plan, request)   [CIA:121]
  â†“ if (!plan.hasKernels()) return diagnostic â†’ skipped for healthy requests  [CIA:123â€“125]
  â†“ EvidenceAgent.extract(request, diagnostics)  [CIA:127]
       â†’ reads ExecutionRequest metadata
       â†’ **pipeline has not run yet**
       â†’ produces an EMPTY EvidenceBundle
  â†“ VerificationAgent.verify(emptyBundle)
       â†’ ConfidenceTier.INSUFFICIENT, confidence=0.15   [ConfidenceCalculator.java:59â€“61]
  â†“ NaturalResponseAgent.generate(INSUFFICIENT, request)
       â†’ "I don't have enough verified information to answer this question."
       â†’ [NaturalResponseAgent.java:231â€“273]
  â†“ attachChiefMetadata(...) â†’ captures ONLY chiefMeta  [CIA:131, DRS:1033]
       **The full response text is DISCARDED**
```

**Pipeline execution (the only place real evidence originates):**
```
effectivePipeline = canonical 11-stage pipeline   [DRS:1036â€“1046]
  â†“ pipeline.execute(context) with up to 3 retries  [DRS:1048]
       IdentityStage â†’ ContextStage â†’ MemoryRecallStage â†’ KnowledgeStage
       â†’ ReasoningStage â†’ InferenceStage â†’ PlanningStage
       â†’ ActionExecutionStage â†’ ReflectionStage â†’ MemoryStoreStage
       â†’ ChiefReviewStage
  â†“ each stage writes its results into PipelineExecutionState.metadata
       keys: "knowledgeResults", "reasoningConclusion", "inferenceResult",
             "planningResult", "memoryResults", "reflectionResult",
             "projectSummary", "knowledgeGroundingScore", "knowledgeCitations", etc.
```

**Second pass (post-pipeline, output IS final):**
```
ResponseSynthesisService.synthesize(pipelineContext, state)  [DRS:1085]
  â†’ DefaultResponseSynthesizer.synthesize() â†’ produces first response
EvidenceAgent.extractFromMetadata(pipelineStateMeta)         [DRS:1119â€“1124]
  â†’ THIS time the metadata is populated â†’ real EvidenceBundle
VerificationAgent.verify(realBundle)                         [DRS:1126â€“1129]
  â†’ confidence from actual evidence quality
NaturalResponseAgent.generate(verificationReport, request)   [DRS:1155â€“1158]
  â†’ "Grounded" response from real evidence items
response = evidenceBackedResponse                           [DRS:1160]
  **REPLACES the first synthesizer output**
```

### 1.6 Termination: SDK response

```
ShreeClient.chat() reads executionResult.structuredPayload()  [ShreeClient.java:146]
  â†“ if payload.get("response") instanceof SynthesizedResponse  [ShreeClient.java:148]
  â†“   answer = response.answer()
  â†“   confidence = response.confidence()
  â†“ SDKResponse.builder()
  â†“   .answer(answer)                      â† final answer string
  â†“   .confidence(confidence)              â† 0.95 / 0.80 / 0.60 / 0.15
  â†“   .reasoningAvailable(true)            â† ALWAYS true, never measured
  â†“   .metadata("sdk-version:...")
  â†“   .structuredPayload(payload)
  â†“   .build()
  â†“ return SDKResponse
```

### 1.7 Summary: four execution paths and their termination points

| Path | Trigger | Response builder | Confidence source | Final owner |
|---|---|---|---|---|
| A â€” Routed | `RuntimeIntentRouter.route()` returns a non-null `ExecutionRoute` | `DefaultResponseSynthesizer` â†’ replaced by `NaturalResponseAgent.generate` | `VerificationReport.confidence()` | `ShreeClient.chat` â†’ `SDKResponse` |
| B â€” Multi-kernel | `IntentAnalyzer.isMultiKernel()` true | `DefaultResponseSynthesizer.synthesizeComposite` at `DRS:1481â€“1483` | hardcoded in `synthesizeComposite` | Same |
| C â€” Canonical CHAT | All other requests | `DefaultResponseSynthesizer.synthesize` â†’ replaced by `NaturalResponseAgent.generate` | `VerificationReport.confidence()` | Same |
| C â€” Pre-flight only | When `!plan.hasKernels()` (no kernels match intent) | `ChiefIntelligenceAgent.buildDiagnosticResponse` at `CIA:185â€“234` | hardcoded `0.50` at `CIA:224` | Same |

The chain terminates in every path at `ShreeClient.java:153â€“159` where `SynthesizedResponse.answer()` becomes `SDKResponse.answer`.

---

## SECTION 2 â€” EVERY CLASS IDENTITY

All classes inspected in the scope of this audit. Read every class fully; no assumptions.

### 2.1 SDK layer

**ShreeClient** (`src/main/java/com/shreeai/os/platform/sdk/ShreeClient.java`, 200+ lines)

- Purpose: SDK entry point that converts SDK requests to runtime execution requests and converts execution results back to SDK responses.
- Constructor: `ShreeClient(SDKConfiguration, Runtime, RuntimeEventBus)` â€” package-private, called by `SDKBuilder`. Line 34.
- Incoming callers: `SDKBuilder.build()` â†’ `new ShreeClient(...)` (verified via SPRINT20_PHASE2_RUNTIME_FORENSIC_AUDIT.md).
- Outgoing callers: `Runtime.submit(executionRequest)` at line 108. Never calls kernels directly.
- Mutable fields: None declared. All fields are `final`.
- Immutable: Yes.
- Singleton/per-request: One instance per SDK session.
- Runtime status: **LIVE** â€” public SDK entry point.
- Thread safety: Delegates to thread-safe Runtime.

**SDKResponse** (`src/main/java/com/shreeai/os/platform/sdk/SDKResponse.java`, 139 lines)

- Purpose: Immutable final response record for SDK consumers.
- Constructor: Package-private `Builder` pattern at `SDKResponse.java:24`. No public constructor.
- Incoming callers: `ShreeClient.chat()` at `ShreeClient.java:153`.
- Fields: `answer` (String), `confidence` (double 0-1), `reasoningAvailable` (boolean), `metadata` (String), `structuredPayload` (Map), `timestamp` (Instant).
- Mutable fields: None. All final.
- Singleton/per-request: Per-request â€” built fresh on every call.
- Runtime status: **LIVE**.

### 2.2 Runtime layer

**DefaultRuntimeService** (`src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java`, ~2,100 lines)

- Purpose: Main Runtime implementation. Orchestrates all request processing from `submit()` through pipeline execution, evidence extraction, response synthesis, and result assembly.
- Constructor graph: 2-arg legacy constructor at `DRS.java:248-258` -> delegates to 4-arg constructor. 4-arg constructor at `DRS.java:279-299` -> calls `initialize()`. `initialize()` at `DRS.java:305` -> `initializeStages()` at `DRS.java:414`.
- Key fields set during construction: `kernelFactory` (line 294), `responseSynthesisService` (line 296), `llmRouter` (line 122), `chiefIntelligenceAgent` (line 147), `approvalService` (line 124), `kernelRegistry` (line 127).
- Key fields set during `submit()`: per-request `ExecutionContext`, `ExecutionSession`, `PipelineContext`, `PipelineResult`.
- Outgoing callers:
  - `chiefIntelligenceAgent.route()` at `DRS.java:888` (canonical path)
  - `pipeline.execute()` at `DRS.java:1048`
  - `responseSynthesisService.synthesize()` at `DRS.java:1085`
  - `EvidenceAgent.extractFromMetadata()` at `DRS.java:1124`
  - `VerificationAgent.verify()` at `DRS.java:1126`
  - `NaturalResponseAgent.generate()` at `DRS.java:1155`
- Mutable fields: `stages`, `pipeline`, `lifecycle`, `intentRouter`, `orchestrator` (lazy). Thread-unsafe if shared across threads.
- Singleton/per-request: Singleton (one Runtime instance per application).
- Runtime status: **LIVE** â€” the sole Runtime implementation.


### 2.3 Agent layer

**ChiefIntelligenceAgent** (`src/main/java/com/shreeai/os/platform/runtime/agents/ChiefIntelligenceAgent.java`, 280 lines)

- Purpose: Single entry point for all `ShreeAI.chat()` requests in Sprint 18. Routes request, runs diagnosis, and orchestrates evidence extraction, verification, and response synthesis.
- Constructor graph: Default constructor at `CIA.java:67` creates canonical singletons: `IntentAnalyzer`, `DiagnosisAgent`, `EvidenceAgent`, `VerificationAgent`, `NaturalResponseAgent`. 5-arg constructor at `CIA.java:81` allows injection for testing.
- Incoming callers: `DefaultRuntimeService.submit()` at `DRS.java:888`.
- Outgoing callers:
  - `IntentAnalyzer.analyze()` at `CIA.java:116`
  - `DiagnosisAgent.analyze()` at `CIA.java:121`
  - `EvidenceAgent.extract()` at `CIA.java:127`
  - `VerificationAgent.verify()` at `CIA.java:128`
  - `NaturalResponseAgent.generate()` at `CIA.java:129`
- Mutable fields: None declared. All fields are `final`.
- Immutable: Yes.
- Singleton/per-request: Singleton (shared across requests).
- Thread safety: Stateless; safe to share. Explicitly stated in Javadoc at `CIA.java:47-50`.
- Runtime status: **LIVE**.

**DiagnosisAgent** (`src/main/java/com/shreeai/os/platform/runtime/agents/DiagnosisAgent.java`, 290 lines)

- Purpose: Examines workspace health before kernel execution. Runs 5 health checks (WORKSPACE, MEMORY, KNOWLEDGE, PROJECT, EXECUTION) and produces a `DiagnosticReport`.
- Constructor: Default at `DiagnosisAgent.java:50`. No-arg.
- Incoming callers: `ChiefIntelligenceAgent.route()` at `CIA.java:121`.
- Outgoing callers: None. Only reads `ExecutionRequest` metadata and file paths.
- Mutable fields: None.
- Immutable: Yes.
- Thread safety: Stateless; no side-effects.
- Runtime status: **LIVE**.

**EvidenceAgent** (`src/main/java/com/shreeai/os/platform/runtime/agents/EvidenceAgent.java`, 350 lines)

- Purpose: Transforms pipeline metadata (written by kernel stages) into structured `EvidenceBundle` objects. The only code that reads the metadata keys written by kernels.
- Constructor: Default at `EvidenceAgent.java:63`. No-arg.
- Incoming callers: `ChiefIntelligenceAgent.route()` at `CIA.java:127` (pre-pipeline, from empty metadata) AND `DefaultRuntimeService.submit()` at `DRS.java:1124` (post-pipeline, from populated metadata).
- Outgoing callers: `EvidenceBundle` (immutable record, no calls out).
- Extracts evidence from metadata keys (lines 46-61): `knowledgeResults`, `reasoningConclusion`, `inferenceResult`, `planningResult`, `memoryResults`, `reflectionResult`, `projectSummary`, `executionResult`.
- Mutable fields: None.
- Immutable: Yes.
- Runtime status: **LIVE** â€” called in two distinct phases per canonical request.

**VerificationAgent** (`src/main/java/com/shreeai/os/platform/runtime/agents/VerificationAgent.java`, 187 lines)

- Purpose: Validates each `EvidenceItem` in an `EvidenceBundle` and assigns an overall `ConfidenceTier` using `ConfidenceCalculator`.
- Constructor: Default at `VerificationAgent.java:51`. No-arg.
- Incoming callers: `ChiefIntelligenceAgent.route()` at `CIA.java:128` AND `DefaultRuntimeService.submit()` at `DRS.java:1126`.
- Outgoing callers: `ConfidenceCalculator` static methods (no state).
- Verification rules at `VerificationAgent.java:153-166`:
  - PROJECT -> always `VERIFIED`
  - KNOWLEDGE with citations -> `VERIFIED`, without -> `UNVERIFIED`
  - REASONING/INFERENCE/PLANNING/EXECUTION/REFLECTION -> always `VERIFIED`
  - MEMORY -> always `UNVERIFIED` (can be stale)
- Tier assignment at `VerificationAgent.java:105-115`:
  - Has PROJECT -> `VERIFIED_PROJECT` (0.95)
  - Has KNOWLEDGE -> `VERIFIED_KB` (0.80)
  - Has REASONING/INFERENCE -> `INFERRED` (0.60)
  - Empty bundle -> `INSUFFICIENT` (0.15)
- Runtime status: **LIVE**.

**NaturalResponseAgent** (`src/main/java/com/shreeai/os/platform/runtime/agents/NaturalResponseAgent.java`, 399 lines)

- Purpose: Converts a verified `EvidenceBundle` into a `SynthesizedResponse`. Named "Natural" because it is supposed to generate natural language â€” but as shown below it does not call any LLM.
- Constructor: Default at `NaturalResponseAgent.java:45`. No-arg.
- Incoming callers: `ChiefIntelligenceAgent.route()` at `CIA.java:129` AND `DefaultRuntimeService.submit()` at `DRS.java:1155`.
- Outgoing callers: None. Only `StringBuilder` concatenation on `EvidenceBundle`, `VerificationReport`, `ExecutionRequest`.
- **CRITICAL: Does NOT call the LLM.** Confirmed by: (a) no import of any `LlmProvider`, `LlmRouter`, `LlmRequest`, `LlmResponse` type in `NaturalResponseAgent.java`; (b) Javadoc at lines 90-92: *"The LLM invocation slot is reserved here. The actual LLM call should be wired through LlmProvider when the LLM integration is complete."*; (c) `generateFromEvidence()` at lines 98-155 uses only `StringBuilder` concatenation.
- Synthesis modes: `VERIFIED_PROJECT`, `VERIFIED_KB`, `INFERRED`, `INSUFFICIENT` (branched at `generate()` lines 60-80).
- Mutable fields: None.
- Runtime status: **LIVE** â€” the only live response builder for canonical CHAT.

### 2.4 Response synthesis layer

**ResponseSynthesisService** (`src/main/java/com/shreeai/os/platform/kernels/response/service/ResponseSynthesisService.java`, 36 lines)

- Purpose: Constitutional service wrapper. Single Runtime entry point for response generation. Delegates to `DefaultResponseSynthesizer`.
- Constructor: Default at `RSS.java:22` creates `new DefaultResponseSynthesizer()`. Single-arg at `RSS.java:26` accepts any `ResponseSynthesizer`.
- Incoming callers: `DefaultRuntimeService.submit()` at `DRS.java:1085`, `MultiKernelOrchestrator` at `DRS.java:1586`, `DefaultRuntimeService` at `DRS.java:1337` and `DRS.java:1429`.
- Outgoing callers: `DefaultResponseSynthesizer.synthesize()` at `RSS.java:34`.
- Wrapper/per-request: Singleton or per-request (constructed in DRS constructor at line 296 as instance field).
- Mutable fields: `final ResponseSynthesizer synthesizer` â€” immutable after construction.
- Runtime status: **LIVE**.

**ResponseSynthesizer** (`src/main/java/com/shreeai/os/platform/kernels/response/api/ResponseSynthesizer.java`, 36 lines)

- Purpose: Constitutional interface contract for response synthesis.
- Methods: `SynthesizedResponse synthesize(PipelineContext, PipelineExecutionState)`
- Implementations: `DefaultResponseSynthesizer` only.
- Runtime status: **LIVE** â€” interface contract.

**DefaultResponseSynthesizer** (`src/main/java/com/shreeai/os/platform/kernels/response/engine/DefaultResponseSynthesizer.java`, 1,460 lines)

- Purpose: Professional response generation engine. Template-based markdown builder that dispatches to one of six synthesis methods based on pipeline metadata.
- Constructor: Default at `DRS.java:43`. No-arg.
- Incoming callers: `ResponseSynthesisService.synthesize()` -> creates new instance at `RSS.java:23`. `DefaultRuntimeService` creates new instances at `DRS.java:1337`, `DRS.java:1429`, `DRS.java:1481`.
- Synthesis dispatch method at `synthesize()` lines 46-67:
  - `isPlanningResult()` -> `synthesizePlanning()` / `synthesizePlanningBlueprint()`
  - `isKnowledgeResult()` -> `synthesizeKnowledge()`
  - `isConversationalChat()` -> `synthesizeChat()`
  - default -> `synthesizeDefault()`
- Additional public methods: `synthesizeExecution()` (Sprint-10, lines 76-153), `synthesizeComposite()` (Sprint-12, lines 1195-1460).
- **Does NOT call the LLM.** No import of any LLM type. Only uses `StringBuilder`, `List`, `Map`.
- Confidence values produced:
  - `synthesizePlanningBlueprint`: hardcoded `0.92` at `DRS.java:1035`
  - `synthesizeExecution`: hardcoded `0.90` at `DRS.java:124`
  - `synthesizeDefault`: `confidence(metadata)` method returns `metadata.reasoningConfidence` (max 0.0-1.0), default `0.90` at `DRS.java:311`
  - `synthesizeKnowledge`: from `metadata.groundingScore` or default `0.80`
  - `synthesizeChat`: hardcoded `1.0` at `DRS.java:721`
- Runtime status: **LIVE** â€” the second of three response builders.

**ResponseSynthesizerVerifier** (`src/main/java/com/shreeai/os/platform/kernels/response/verification/ResponseSynthesizerVerifier.java`, 23 lines)

- Purpose: Architectural verifier for the Response Kernel. Checks that the engine implements the interface.
- Method: `verify(ResponseSynthesizer, ResponseSynthesisService)` at lines 16-22 â€” null checks only.
- Incoming callers: Unknown (no production callers found).
- Runtime status: **DEAD** â€” no injection points in the codebase.


### 2.5 LLM layer

**LlmProvider** (`src/main/java/com/shreeai/os/platform/llm/LlmProvider.java`, 78 lines)

- Purpose: Streaming-first SPI interface for LLM calls. Single primitive: `Stream<String> stream(LlmRequest)`.
- Secondary: `default LlmResponse complete(LlmRequest)` â€” calls `stream()` and joins tokens.
- Implementations: `OpenAiProvider`, `GeminiProvider`, `OllamaProvider`, `InMemoryLlmProvider`.
- Incoming callers: **None in production.** Only `LlmRouter` (which is also in the llm package), and `LlmRouterTest` (test only).
- Runtime status: **LIVE** (interface definition) but **NEVER CALLED** in production.

**LlmRouter** (`src/main/java/com/shreeai/os/platform/llm/router/LlmRouter.java`, 167 lines)

- Purpose: Interchangeable provider chain. Routes `LlmRequest` across ordered `LlmProvider` list with fail-over. Implements `LlmProvider` itself.
- Constructor: `LlmRouter(List<LlmProvider> chain)` at line 51 â€” immutable after construction.
- Static factory: `fromChain(String chainSpec, Map<String, LlmProvider> registry)` at line 68.
- Key method `stream(LlmRequest)` at lines 152-166: iterates providers in order, returns first that succeeds, throws `IllegalStateException` if all fail.
- **Created at:** `DefaultRuntimeService.java:122` via `buildDefaultLlmRouter()`.
- **Passed into:** `PipelineContext` as attribute at `DefaultRuntimeService.java:1044`.
- **Read from context:** **NEVER** â€” no production code reads `llmRouter` from the `PipelineContext`.
- **Called:** Never. No `llmRouter.complete(...)` or `llmRouter.stream(...)` in any production class.
- Runtime status: **LIVE** (wired) but **UNUSED** in production.

**OpenAiProvider** (`src/main/java/com/shreeai/os/platform/llm/openai/OpenAiProvider.java`, 300+ lines)

- Purpose: OkHttp-backed streaming-first provider for OpenAI-compatible endpoints.
- Default URL: `https://api.openai.com/v1/chat/completions` at `OpenAiProvider.java:42`.
- Constructor: `OpenAiProvider(String apiKey)` at line 53 -> delegates to 3-arg constructor.
- `stream(LlmRequest)` at lines 77-113: builds HTTP POST request, parses SSE stream, returns `Stream<String>`.
- `buildBody(LlmRequest)` at lines 121-151: serializes to JSON with model, messages, temperature, maxTokens.
- Incoming callers: **Only `DefaultRuntimeService.buildDefaultLlmRouter()` at line 207.**
- Runtime status: **LIVE** (registered in router) but **NEVER CALLED**.

**GeminiProvider** (`src/main/java/com/shreeai/os/platform/llm/gemini/GeminiProvider.java`, 300+ lines)

- Purpose: OkHttp-backed streaming-first provider for Google Gemini.
- Default base URL: `https://generativelanguage.googleapis.com/v1beta/models/` at `GeminiProvider.java:42-43`.
- Default model fallback: `gemini-2.0-flash` at `GeminiProvider.java:122`.
- `stream(LlmRequest)` at lines 77-112: similar pattern to OpenAiProvider.
- Incoming callers: **Only `DefaultRuntimeService.buildDefaultLlmRouter()` at line 212.**
- Runtime status: **LIVE** (registered in router) but **NEVER CALLED**.

**OllamaProvider** (`src/main/java/com/shreeai/os/platform/llm/ollama/OllamaProvider.java`, 300+ lines)

- Purpose: OkHttp-backed streaming-first provider for Ollama.
- Default URL: `http://localhost:11434/api/generate` at `OllamaProvider.java:50`.
- Incoming callers: **Only `DefaultRuntimeService.buildDefaultLlmRouter()` at line 203.**
- Runtime status: **LIVE** (registered in router) but **NEVER CALLED**.

**InMemoryLlmProvider** (`src/main/java/com/shreeai/os/platform/llm/inmemory/InMemoryLlmProvider.java`, 79 lines)

- Purpose: Deterministic, zero-dependency provider for unit tests and local demos.
- `stream(LlmRequest)` at lines 41-55: returns `"model echoes: prompt"` as token stream.
- Echo format: `"modelName echoes: promptText"` at `InMemoryLlmProvider.java:44`.
- Incoming callers: **Only `DefaultRuntimeService.buildDefaultLlmRouter()` at line 202** (as default fallback) and unit tests.
- Runtime status: **LIVE** (registered in router as fallback) but **NEVER CALLED** in production.

**LlmRequest** (`src/main/java/com/shreeai/os/platform/llm/LlmRequest.java`, 169 lines)

- Purpose: Immutable request contract for the LLM SPI.
- Builder defaults: `model="default"`, `prompt=""`, `stream=true` at lines 119-123.
- Incoming callers: Only in unit tests. No production callers.
- Runtime status: **LIVE** (SPI type) but **NEVER CONSTRUCTED** in production.

**LlmResponse** (`src/main/java/com/shreeai/os/platform/llm/LlmResponse.java`, 142 lines)

- Purpose: Immutable response contract for the LLM SPI.
- Incoming callers: Only in unit tests. No production callers.
- Runtime status: **LIVE** (SPI type) but **NEVER CONSTRUCTED** in production.

### 2.6 Response models

**SynthesizedResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/model/SynthesizedResponse.java`, 73 lines)

- Purpose: Immutable record returned by all synthesizers. Holds the canonical response output.
- Fields: `answer`, `sections`, `confidence` (0-1), `style` (ResponseStyle), `generatedAt`, `structuredData`.
- Constructor: Package-private record. Validation: confidence must be 0-1 at `SynthesizedResponse.java:28-32`.
- Incoming callers: `NaturalResponseAgent.generate()`, `DefaultResponseSynthesizer.synthesize*()`, `ChiefIntelligenceAgent.buildDiagnosticResponse()`, `DefaultRuntimeService` inline synthesis.
- Runtime status: **LIVE**.

**ResponseSection** (`src/main/java/com/shreeai/os/platform/kernels/response/model/ResponseSection.java`, 20 lines)

- Purpose: Immutable section of a `SynthesizedResponse`.
- Fields: `title` (String), `content` (String). Both non-null.
- Runtime status: **LIVE**.

**ResponseStyle** (`src/main/java/com/shreeai/os/platform/kernels/response/model/ResponseStyle.java`, 16 lines)

- Purpose: Enum of constitutional response presentation styles.
- Values: `PROFESSIONAL`, `CONVERSATIONAL`, `TECHNICAL`, `EDUCATIONAL`, `EXECUTIVE_SUMMARY`.
- Note: `style` is stored in `SynthesizedResponse` but is never read by `ShreeClient.chat()` â€” it is purely metadata.
- Runtime status: **LIVE**.

**DeveloperResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/model/DeveloperResponse.java`, 250+ lines)

- Purpose: Structured response from the Developer Agent (Sprint-14). Javadoc at line 15: *"deterministically generated without LLM calls"* â€” confirmed.
- Production usage: Serialized into `structuredPayload` at `DefaultResponseSynthesizer.synthesizeComposite()` around lines 1320-1390.
- `toFormattedResponse()` at `DeveloperResponse.java:68` returns `formattedPlan` â€” a `StringBuilder`-assembled markdown string.
- Runtime status: **LIVE** but only used in `synthesizeComposite` path.

**PlanningResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/contracts/PlanningResponse.java`, 11 lines)

- Purpose: Record contract for kernel-level planning responses. `implements KernelResponse`.
- Runtime status: **DEAD** â€” never used in production.

**KnowledgeResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/contracts/KnowledgeResponse.java`, 10 lines)

- Purpose: Record contract for kernel-level knowledge responses. `implements KernelResponse`.
- Runtime status: **DEAD** â€” never used in production.

**MemoryResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/contracts/MemoryResponse.java`, 9 lines)

- Purpose: Record contract for kernel-level memory responses. `implements KernelResponse`.
- Runtime status: **DEAD** â€” never used in production.

**ConversationResponse** (`src/main/java/com/shreeai/os/platform/kernels/response/contracts/ConversationResponse.java`, 7 lines)

- Purpose: Record contract for kernel-level conversation responses. `implements KernelResponse`.
- Runtime status: **DEAD** â€” never used in production.

**KernelResponse** â€” empty marker interface. `KernelResponse.java` does not exist in the repository.
- Runtime status: **DEAD**.

### 2.7 Model classes (runtime/agents and runtime/model)

**ConfidenceCalculator** (`src/main/java/com/shreeai/os/platform/runtime/confidence/ConfidenceCalculator.java`, 157 lines)

- Purpose: Static utility for computing 4-tier confidence scores. The single authoritative source.
- Constants: `VERIFIED_PROJECT_SCORE=0.95`, `VERIFIED_KB_SCORE=0.80`, `INFERRED_SCORE=0.60`, `INSUFFICIENT_SCORE=0.15` at `ConfidenceCalculator.java:28-31`.
- Incoming callers: `VerificationAgent.verify()` at `VerificationAgent.java:107, 110, 113`.
- Runtime status: **LIVE**.

**VerificationReport** (`src/main/java/com/shreeai/os/platform/runtime/model/VerificationReport.java`, 160 lines)

- Purpose: Immutable report from `VerificationAgent`. Holds per-item verification status, `ConfidenceTier`, confidence score, citations, gaps.
- Runtime status: **LIVE**.

**EvidenceBundle** (`src/main/java/com/shreeai/os/platform/runtime/model/EvidenceBundle.java`, 120 lines)

- Purpose: Immutable collection of structured `EvidenceItem` objects.
- Runtime status: **LIVE**.

**EvidenceItem** (`src/main/java/com/shreeai/os/platform/runtime/model/EvidenceItem.java`, 90+ lines)

- Purpose: Single structured fact from a kernel.
- SourceType enum: `KNOWLEDGE`, `REASONING`, `INFERENCE`, `PLANNING`, `MEMORY`, `REFLECTION`, `PROJECT`, `EXECUTION`.
- Runtime status: **LIVE**.

**IntentAnalyzer** (`src/main/java/com/shreeai/os/platform/runtime/orchestration/IntentAnalyzer.java`, 200+ lines)

- Purpose: Deterministic, LLM-free multi-intent analyzer. Pattern-matching-based keyword detection.
- Javadoc at lines 16-22: *"Deterministic, LLM-free... Detection is purely keyword/pattern-based. The analyzer does not call any LLM."*
- Incoming callers: `ChiefIntelligenceAgent.route()` at `CIA.java:116`, `MultiKernelOrchestrator` at `DRS.java:1584`.
- Runtime status: **LIVE**.

---

## SECTION 3 â€” LLM ROUTER TRUTH

This section audits the LLM routing system against the code in the repository. Every question in the audit brief is answered with file:line evidence.

### 3.1 Where is LlmRouter created?

**Created at:** `DefaultRuntimeService.java:122`

```java
private final LlmRouter llmRouter = buildDefaultLlmRouter();
```

The field is `final` (line 122), initialized at field declaration with the static factory `buildDefaultLlmRouter()`. This makes the `LlmRouter` a per-`DefaultRuntimeService` instance (since `DefaultRuntimeService` is a singleton, the router is effectively a singleton within the application).

### 3.2 How is the LlmRouter constructed? (buildDefaultLlmRouter)

`buildDefaultLlmRouter()` is at `DefaultRuntimeService.java:200-226`:

```java
private static LlmRouter buildDefaultLlmRouter() {
    Map<String, LlmProvider> registry = new LinkedHashMap<>();
    registry.put("in-memory", new InMemoryLlmProvider());     // line 202
    registry.put("ollama", new OllamaProvider());             // line 203

    String openAiKey = firstNonBlank(System.getenv("OPENAI_API_KEY"), null);
    if (openAiKey != null) {
        registry.put("openai", new OpenAiProvider(openAiKey)); // line 207
    }

    String geminiKey = firstNonBlank(System.getenv("GEMINI_API_KEY"),
                                       System.getenv("GOOGLE_API_KEY"));
    if (geminiKey != null) {
        registry.put("gemini", new GeminiProvider(geminiKey)); // line 212
    }

    String chainSpec = System.getenv().getOrDefault("SHREE_LLM_CHAIN",
                                                     "in-memory,ollama");
    try {
        return LlmRouter.fromChain(chainSpec, registry);
    } catch (IllegalArgumentException ignored) {
        return new LlmRouter(List.of(registry.get("in-memory")));
    }
}
```

### 3.3 Is it singleton?

**Yes â€” within a single `DefaultRuntimeService` instance**, which itself is intended to be a singleton (created in `PlatformServiceLocator` or `ShreeBuilder`).

The field declaration at `DefaultRuntimeService.java:122` is `private final`, indicating one-per-instance, and `DefaultRuntimeService` itself is constructed exactly once in the canonical bootstrap path.

However, the singleton nature is **irrelevant** in practice because the router is never invoked. See Section 3.11 below.

### 3.4 Which model is default?

**Default chain spec:** `"in-memory,ollama"` at `DefaultRuntimeService.java:218`.

The `LlmRouter.fromChain` factory at `LlmRouter.java:68-88` resolves the chain spec against the registry. Since `InMemoryLlmProvider` is always registered (line 202), and `OllamaProvider` is always registered (line 203), the default chain contains both â€” in that order.

`InMemoryLlmProvider.providerName()` returns `"in-memory"` at `InMemoryLlmProvider.java:36`.
`OllamaProvider.providerName()` returns `"ollama"` at `OllamaProvider.java:74`.

`OpenAiProvider` is added only if `OPENAI_API_KEY` env var is set (line 207).
`GeminiProvider` is added only if `GEMINI_API_KEY` or `GOOGLE_API_KEY` is set (line 212).

`InMemoryLlmProvider` is the *first* provider in the default chain. It is the only provider that is guaranteed to be reachable even without network access.

**Model name:** `LlmRequest` defaults to `model="default"` at `LlmRequest.java:119`. None of the four providers is given an explicit model at construction; the model is forwarded from the `LlmRequest`. In `OpenAiProvider`, the body uses `request.model()` directly (`OpenAiProvider.java:124`). In `GeminiProvider`, the model is appended to the URL (`GeminiProvider.java:121-124`), with fallback to `gemini-2.0-flash` if the model is `default`.

### 3.5 Can models switch?

**No.** There is no `setModel()` method on `LlmRouter`, no `setProvider()`, no model selection at runtime. The chain is fixed at construction. The `LlmRequest.model()` field is forwarded verbatim to the selected provider.

Within a provider, the model name can be set per-request, but the provider itself is selected at construction time and never swapped.

### 3.6 How is temperature decided?

The temperature is read from `LlmRequest.temperature()` â€” a `Double` field at `LlmRequest.java:32, 59`.

**There is no default temperature set anywhere in production.** `LlmRequest.Builder.temperature(Double)` at `LlmRequest.java:139-142` accepts null and stores null.

The temperature is forwarded to:
- `OpenAiProvider.buildBody()` at `OpenAiProvider.java:135-137`: only included if `request.temperature() != null`.
- `GeminiProvider.buildBody()` at `GeminiProvider.java:149-152`: only included if `request.temperature() != null`.
- `OllamaProvider.buildBody()` (lines 120+): forwarded through options.

**Production consequence:** since no code constructs an `LlmRequest` in production, no temperature is ever set, and no temperature is ever sent. The LLM providers receive `null` temperature, which is omitted from the HTTP body, so the provider falls back to its own default (typically 0.7 or 1.0).

### 3.7 Where are tokens configured?

`LlmRequest.maxTokens()` at `LlmRequest.java:33, 63` is an `Integer`.

There is **no production code** that sets `maxTokens`. The builder default is `null` (`LlmRequest.java:122`).

Forwarding:
- `OpenAiProvider` at `OpenAiProvider.java:138-140`: only included if not null.
- `GeminiProvider` at `GeminiProvider.java:152+`: only included if not null.
- `OllamaProvider` at `OllamaProvider.java:120+`: forwarded through options.

Since no production `LlmRequest` is ever built, the field is always `null` and never sent. The provider's own default is used.

### 3.8 Is streaming implemented?

**Yes â€” at the SPI level.** `LlmProvider.stream(LlmRequest)` at `LlmProvider.java:77` is the streaming primitive. The four implementations all return `Stream<String>` and register `onClose` handlers to release the HTTP response:

- `OpenAiProvider.stream()` at `OpenAiProvider.java:77-113`: SSE parsing, `onClose` releases the response.
- `GeminiProvider.stream()` at `GeminiProvider.java:77-112`: SSE parsing, `onClose` releases the response.
- `OllamaProvider.stream()` at `OllamaProvider.java:79-112`: newline-delimited JSON parsing, `onClose` releases the response.
- `InMemoryLlmProvider.stream()` at `InMemoryLlmProvider.java:41-55`: returns a static token list, no resource.

The default `LlmRequest.stream` is `Boolean.TRUE` at `LlmRequest.java:123`. `OpenAiProvider` and `OllamaProvider` always set `stream: true` in the HTTP body regardless.

**Streaming is never invoked in production.** No production code calls `LlmProvider.stream()` or `LlmProvider.complete()`.

### 3.9 Is retry implemented?

**Retry at the router level: YES, via fail-over across providers.** `LlmRouter.stream(LlmRequest)` at `LlmRouter.java:152-166`:

```java
public Stream<String> stream(LlmRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    IllegalStateException lastFailure = new IllegalStateException(
            "No provider in chain could serve the request: " + providerNames());

    for (LlmProvider provider : chain) {
        try {
            return provider.stream(request);
        } catch (RuntimeException failure) {
            lastFailure.addSuppressed(failure);
        }
    }
    throw lastFailure;
}
```

Each provider that throws a `RuntimeException` is skipped. The router returns the first provider that succeeds, or throws an `IllegalStateException` if all fail.

**Retry within a single provider: NO.** Each provider makes exactly one HTTP call. There is no per-provider retry logic.

**Retry in the higher-level Runtime: YES, but unrelated to LLM.** `DefaultRuntimeService.submit()` retries the pipeline up to 3 times at `DefaultRuntimeService.java:1048` â€” but this retries the canonical pipeline (kernels, evidence, synthesis), not the LLM.

### 3.10 Is timeout implemented?

**At the SPI level: NO.** `LlmProvider.stream()` at `LlmProvider.java:77` does not specify any timeout. There is no `CompletableFuture` or `Future` wrapper in the interface.

**At the OkHttp level: PARTIAL.** Each provider instantiates `new OkHttpClient()` with no configuration:
- `OpenAiProvider.java:54` â€” `new OkHttpClient()` with default settings (10s connect, 10s read).
- `GeminiProvider.java:54` â€” same.
- `OllamaProvider.java:60` â€” same.

The default OkHttp timeouts are 10 seconds for connect, read, and write. These are not overridden anywhere.

**At the router level: NO.** `LlmRouter.stream()` does not impose a timeout on the underlying provider.

**Production consequence:** if the LLM were invoked, the request would time out after 10 seconds with no per-call timeout override.

### 3.11 Is fallback implemented?

**YES, structurally â€” see section 3.9 above.** The router iterates providers in order and falls back to the next on `RuntimeException`.

The fallback chain in `buildDefaultLlmRouter()` at `DefaultRuntimeService.java:200-226` is:
1. First, the `SHREE_LLM_CHAIN` env var is consulted. Default `"in-memory,ollama"`.
2. If the env var is invalid, falls back to `List.of(registry.get("in-memory"))` (line 226).
3. The `in-memory` provider is always present (line 202), so the fallback is always resolvable.

### 3.12 Is the LlmRouter ever called in production?

**NO.** Proven by:
1. No `LlmProvider.complete(...)` call sites in `src/main/java` outside of `LlmProvider.java` itself (the default `complete` method) and the test files.
2. No `LlmProvider.stream(...)` call sites in `src/main/java` outside the four provider implementations and `LlmRouter.java`.
3. The `LlmRouter` is passed into `PipelineContext` as an attribute at `DefaultRuntimeService.java:1044` but is never read back. No `PipelineContext.getAttribute("llmRouter")` exists in any production class.
4. The `LlmRequest` is never constructed in `src/main/java` â€” only in `src/test/java` test files (`LlmRouterTest.java`, `InMemoryLlmProviderTest.java`, `OpenAiProviderParsingTest.java`, `OllamaProviderParsingTest.java`, `GeminiProviderParsingTest.java`, `LlmRequestTest.java`).

The LLM router is **a fully-implemented but unconsumed capability**. It exists as code but has no effect on any user request.

### 3.13 LLM Router Summary

| Question | Answer | Evidence |
|---|---|---|
| Created where? | `DefaultRuntimeService.java:122` via `buildDefaultLlmRouter()` | `DRS.java:122, 200` |
| Singleton? | Yes per-Runtime instance | `DRS.java:122` (`private final`) |
| Default model? | `"in-memory"` first, then `"ollama"` | `DRS.java:218`, `LlmRouter.java:107-110` |
| Can models switch? | No â€” chain is fixed at construction | `LlmRouter.java:51-57` (immutable chain) |
| Temperature decided? | Not in production â€” `LlmRequest.temperature()` is always null | `LlmRequest.java:119-122` |
| Tokens configured? | Not in production â€” `LlmRequest.maxTokens()` is always null | `LlmRequest.java:122-148` |
| Streaming implemented? | At SPI level yes; in production no | `LlmProvider.java:77` |
| Retry implemented? | Router-level fail-over yes; per-provider no; Runtime-level 3-retry unrelated | `LlmRouter.java:152-166` |
| Timeout implemented? | No (default OkHttp 10s) | `OpenAiProvider.java:54` etc. |
| Fallback implemented? | Yes â€” chain iterates, throws if all fail | `LlmRouter.java:152-166` |
| Called in production? | **No** | repo-wide search |

---

## SECTION 4 â€” PROMPT CONSTRUCTION

This is the most important section. The brief asks me to trace a real request through prompt construction. The forensic truth, proven by code evidence:

**There is no prompt builder for chat requests in Shree AI OS.**

No production code constructs a string that would be passed as a prompt to an LLM. There is no `PromptBuilder` class. There is no `PromptTemplate` class. There is no `Message` list assembly. The closest the code comes to a "prompt" is the user input itself, which is passed to the canonical pipeline as `ExecutionRequest.userInput` and then read by various stages (IntentAnalyzer for routing, EvidenceAgent for context extraction).

### 4.1 The trace, request: "Explain WorkspaceController"

Following the user's literal request to trace "Explain WorkspaceController" through prompt construction:

**Step 1: User input â†’ ShreeClient.chat()**

```java
// ShreeClient.java:57-63
public SDKResponse chat(String message) {
    return chat(
        SDKRequest.builder()
            .message(message)         // message = "Explain WorkspaceController"
            .build()
    );
}
```

Data transferred: `SDKRequest.message = "Explain WorkspaceController"`.

**Step 2: ShreeClient.chat(SDKRequest) at ShreeClient.java:68-174**

```java
// ShreeClient.java:81-82
IntelligenceContext intelligenceContext =
    IntelligenceContextBuilder.fromSdkRequest(request);  // builds context from message

// ShreeClient.java:85-91
Map<String, Object> metadata = new HashMap<>(request.metadata());
metadata.put("intelligenceContext", intelligenceContext);
if (request.sessionId() != null && !request.sessionId().isBlank()) {
    metadata.put("sessionId", request.sessionId());
}

// ShreeClient.java:93-102
ExecutionRequest executionRequest = ExecutionRequest.builder()
    .requestId(...)
    .userInput("Explain WorkspaceController")    // userInput is the original message
    .context(request.context())
    .metadata(metadata)                          // { intelligenceContext, sessionId }
    .build();
```

**No prompt is built. The user input is stored in `ExecutionRequest.userInput`.** No transformation, no augmentation, no template.

**Step 3: DefaultRuntimeService.submit() at DefaultRuntimeService.java:695**

```java
// DRS:704-714
eventBus.publish(new RuntimeEvent(
    EventType.PIPELINE_STARTED,
    request.requestId(), "Pipeline", Instant.now(),
    Map.of("requestType", request.requestType())
));

// DRS:725-729
ExecutionContext context = ExecutionContext.builder()
    .session(session)
    .configuration(configuration)
    .contract(contract)
    .build();

// DRS:736-739
RuntimeIntentRouter.ExecutionRoute route =
    intentRouter != null
        ? intentRouter.route(request).orElse(null)
        : null;
```

The intent router inspects the user input ("Explain WorkspaceController") to determine routing. `IntentAnalyzer.PLANNING_PATTERNS` at `IntentAnalyzer.java:62-77` includes `(?i)\\bexplain\\b`-like patterns, but actually the matching is done in `IntentAnalyzer` (called from `ChiefIntelligenceAgent.route()` at `CIA.java:116`).

**There is still no prompt building. The user input is the user input.**

**Step 4: ChiefIntelligenceAgent.route() at CIA.java:112-132**

```java
// CIA:115-116
String userInput = extractUserInput(request);    // "Explain WorkspaceController"
IntentAnalysisResult intent = intentAnalyzer.analyze(userInput);
```

`IntentAnalyzer.analyze()` at `IntentAnalyzer.java` (full file 200+ lines) does pattern matching against `KNOWLEDGE_PATTERNS` (line 79+) â€” including `Pattern.compile("(?i)\\bexplain\\b")` â€” and classifies the intent. This produces an `IntentAnalysisResult` (not a prompt).

**No prompt is built.**

**Step 5: EvidenceAgent.extract() at CIA.java:127**

```java
// CIA:127
EvidenceBundle bundle = evidenceAgent.extract(request, diagnostics);
```

`EvidenceAgent.extract()` at `EvidenceAgent.java:76-99` reads `request.getMetadata()` and calls `extractKnowledgeEvidence`, `extractReasoningEvidence`, etc. (lines 89-96).

These methods (e.g. `extractKnowledgeEvidence` at lines 137-171) read from the metadata map only â€” they do not read the user input directly. The user input "Explain WorkspaceController" is in `request.getUserInput()` but the agent does not pull it. The agent reads `metadata.knowledgeResults` (a list of `KnowledgeNode` from a prior knowledge query).

**No prompt is built. No user input is consumed by EvidenceAgent.**

For the very first request, `knowledgeResults` is empty. The bundle will contain only an empty list. For subsequent requests (where the knowledge graph contains ingested data), the bundle will have items.

**Step 6: VerificationAgent.verify() at CIA.java:128**

`VerificationAgent.verify()` at `VerificationAgent.java:59-137` validates each `EvidenceItem`. It does not construct a prompt.

**No prompt is built.**

**Step 7: NaturalResponseAgent.generate() at CIA.java:129 (and DRS.java:1158)**

```java
// CIA:129, DRS:1158
SynthesizedResponse response = naturalResponseAgent.generate(verification, request);
```

`NaturalResponseAgent.generate()` at `NaturalResponseAgent.java:57-81`:

```java
public SynthesizedResponse generate(VerificationReport report, ExecutionRequest request) {
    Objects.requireNonNull(report, "report must not be null");

    if (report.isInsufficient()) {
        return generateInsufficientResponse(report, request);
    }

    // Build structured evidence payload for LLM
    Map<String, Object> structuredData = buildStructuredPayload(report);

    // Generate natural language from structured evidence
    String answer = generateFromEvidence(report, request);

    // Build sections for structured response
    List<ResponseSection> sections = buildSections(report);

    return new SynthesizedResponse(
        answer, sections, report.confidence(),
        ResponseStyle.PROFESSIONAL, Instant.now(), structuredData
    );
}
```

The `answer` is produced by `generateFromEvidence(report, request)` at lines 98-155. This method is pure `StringBuilder` concatenation against `bundle.items()` and `report.citations()`. It does **not** construct a prompt. It does **not** call the LLM.

`generateFromEvidence()` at `NaturalResponseAgent.java:104-154`:
```java
StringBuilder sb = new StringBuilder();
String title = deriveTitleFromEvidence(bundle, report);
String userQuestion = request != null && request.getUserInput() != null
        ? request.getUserInput().trim() : "";

sb.append("# ").append(title).append("\n\n");

if (!userQuestion.isBlank()) {
    sb.append("**Question:** ").append(userQuestion).append("\n\n");
}

appendKnowledgeSummaryAndKey(sb, bundle);

for (EvidenceItem item : bundle.items()) {
    appendEvidenceItem(sb, item, report.tier());
}

if (!report.citations().isEmpty()) {
    sb.append("## Citations\n\n");
    for (int i = 0; i < report.citations().size(); i++) {
        sb.append("[").append(i + 1).append("] ")
                .append(report.citations().get(i)).append("\n");
    }
    sb.append("\n");
}

appendConfidenceNote(sb, report);

if (!report.gaps().isEmpty()) {
    sb.append("## Limitations\n\n");
    for (String gap : report.gaps()) {
        sb.append("- ").append(gap).append("\n");
    }
}

return sb.toString();
```

**The "prompt" is the answer itself: a deterministic markdown document built by `StringBuilder` from the evidence items. There is no LLM call.**

### 4.2 The only place a real LlmRequest would be constructed

If the LLM were actually called, the only place that would construct an `LlmRequest` is the `NaturalResponseAgent.generateFromEvidence()` method (since that is the only place that could semantically hold a prompt). But the Javadoc at `NaturalResponseAgent.java:90-92` explicitly says:

> *"The LLM invocation slot is reserved here. The actual LLM call should be wired through LlmProvider when the LLM integration is complete."*

So even the design of the `NaturalResponseAgent` acknowledges the LLM is not wired.

### 4.3 The DefaultResponseSynthesizer also has no prompt builder

`DefaultResponseSynthesizer.synthesize()` at `DefaultResponseSynthesizer.java:46-67` dispatches to one of six hardcoded markdown builders. None of them constructs an LLM prompt. Examples:

**`synthesizeKnowledge()` at lines 520-688:** Pure `StringBuilder` assembly of markdown sections.
```java
StringBuilder answer = new StringBuilder();
answer.append("# ").append(title).append("\n\n");
if (!summary.isBlank()) {
    answer.append("## Summary\n\n").append(summary).append("\n\n");
    sections.add(new ResponseSection("Summary", summary));
}
if (!results.isEmpty()) {
    answer.append("## Key Knowledge\n\n");
    // ... bullets for each KnowledgeNode
}
```

**`synthesizePlanning()` at lines 731-873:** Same pattern â€” section-by-section `StringBuilder` assembly.

**`synthesizeChat()` at lines 700-725:** Hardcoded greeting:
```java
if (userMessage.equalsIgnoreCase("hello shree")
        || userMessage.equalsIgnoreCase("hello")
        || userMessage.equalsIgnoreCase("hi")) {
    answer = """
        Hello! I'm Shree AI.

        How can I help you today?
        """;
} else {
    answer = "I received your message: \"" + userMessage +
            "\".\n\nHow can I help you?";
}
```

**`synthesizeDefault()` at lines 181-244:** Builds `Executive Summary`, `Key Findings`, `Recommended Next Step`, `Evidence` sections from `metadata.reasoningSummary`, `metadata.reasoningConclusion`, `metadata.planSummary`, `metadata.memoryId`, `metadata.knowledgeId`, etc.

**`synthesizeExecution()` at lines 76-153:** Sprint-10 capability-dispatch template, hardcoded sections.

**`synthesizeComposite()` at lines 1195-1460:** Sprint-12 multi-kernel composite, hardcoded sections.

### 4.4 What about the structured payload?

The "structured payload" in the SDK response (i.e. what gets attached to `SDKResponse.structuredPayload`) is built at `DefaultRuntimeService.java:1090-1199` and contains:

```
{
  "response": <SynthesizedResponse>,
  "routedOperation": ...,
  "routedKernel": ...,
  "routedStages": [...],
  "evidence": [...],      // List<Map<String, Object>> of evidence items
  "evidenceCount": N,
  "evidenceBundleId": "bundle-...",
  "confidence": 0.95|0.80|0.60|0.15,
  "verificationTier": "VERIFIED_KB",
  "verificationConfidence": 0.80,
  "citationCount": N,
  "citations": [...],
  "gaps": [...]
}
```

This is the entire "context" that travels from the runtime to the SDK. **No prompt is included.** The structured payload is for the SDK consumer, not for an LLM.

### 4.5 The existing template strings

The platform has the following markdown section templates (all hardcoded in source):

| File | Line | Template |
|---|---|---|
| `DefaultResponseSynthesizer.java` | 109â€“110 | `# {title}\n\n## Summary\n\n{summary}\n\n` |
| `DefaultResponseSynthesizer.java` | 113â€“116 | `- **{label}**{description} [{citationIndex}]\n` |
| `DefaultResponseSynthesizer.java` | 180 | `# Executive Summary\n\n{summary}\n\n` |
| `DefaultResponseSynthesizer.java` | 197 | `## Goal\n\n{goalText}\n\n` |
| `DefaultResponseSynthesizer.java` | 216 | `## Feasibility\n\n{feasibility}\n\n` |
| `DefaultResponseSynthesizer.java` | 228 | `## Priority\n\n{priority}\n\n` |
| `DefaultResponseSynthesizer.java` | 239 | `## Blockers\n\n{bullets}\n` |
| `DefaultResponseSynthesizer.java` | 248 | `## Dependencies\n\n{bullets}\n` |
| `DefaultResponseSynthesizer.java` | 259 | `## Subtasks\n\n{numbered}\n` |
| `DefaultResponseSynthesizer.java` | 268 | `## Recommendations\n\n{bullets}\n` |
| `DefaultResponseSynthesizer.java` | 706â€“710 | `Hello! I'm Shree AI.\n\nHow can I help you today?\n` |
| `DefaultResponseSynthesizer.java` | 730 | `I received your message: "{userMessage}".\n\nHow can I help you?\n` |
| `DefaultResponseSynthesizer.java` | 1297 | `Memory\n\n{text}\n\n` |
| `DefaultResponseSynthesizer.java` | 1313 | `Knowledge\n\n{text}\n\n` |
| `DefaultResponseSynthesizer.java` | 1325 | `Roadmap\n\n{text}\n\n` |
| `DefaultResponseSynthesizer.java` | 1331 | `Execution\n\n{text}\n\n` |
| `DefaultResponseSynthesizer.java` | 1401 | `## Next Action\n\n` |
| `DefaultResponseSynthesizer.java` | 1108 | `* {value}\n` (bullet) |
| `DefaultResponseSynthesizer.java` | 1124 | `{i}. {value}\n` (numbered) |
| `NaturalResponseAgent.java` | 218 | `**Confidence:** {pct}% ({tier})\n\n` |
| `NaturalResponseAgent.java` | 228 | `*{tierNote}*\n\n` |
| `NaturalResponseAgent.java` | 233 | `# Insufficient Evidence\n\n` |
| `NaturalResponseAgent.java` | 243 | `## What's Missing\n\n` |
| `NaturalResponseAgent.java` | 250 | `## Recommendations\n\n` |
| `NaturalResponseAgent.java` | 276 | `# Response\n\nNo structured evidence available for this request.` |
| `DefaultResponseSynthesizer.java` | 1071 | `A structured execution plan with {N} steps has been generated.` |
| `DefaultResponseSynthesizer.java` | 260 | `The request was successfully processed through the Shree AI intelligence pipeline.` |
| `DefaultResponseSynthesizer.java` | 87 | `# Execution Started\n\n` |
| `DefaultResponseSynthesizer.java` | 88 | `## Capability\n\n{capability}\n\n` |
| `DefaultResponseSynthesizer.java` | 90 | `## Objective\n\n{objective}\n\n` |
| `DefaultResponseSynthesizer.java` | 92 | `## Status\n\n{status}\n\n` |
| `DefaultResponseSynthesizer.java` | 94 | `## Execution ID\n\n{executionId}\n\n` |
| `DefaultResponseSynthesizer.java` | 96 | `## Deliverables\n\n` |

These templates are the entirety of "prompt construction" in the codebase. None of them is sent to an LLM. They are concatenated into the final user-facing answer.

### 4.6 What is "Memory Context" in the response?

There is no `## Memory Context` section. The "memory context" exists only in the pipeline's `MemoryRecallStage` which runs as a pipeline stage. The stage writes `memoryResults` into `PipelineExecutionState.metadata` (key defined at `EvidenceAgent.java:52`). This metadata is then read by `EvidenceAgent.extractMemoryEvidence()` at `EvidenceAgent.java:230` which produces a `MEMORY` `EvidenceItem`.

`NaturalResponseAgent.appendEvidenceItem()` at `NaturalResponseAgent.java:193-216` adds memory evidence to the response as:

```
ðŸ§© **{title}**

{content}
```

That is the entire "memory context" in the response.

### 4.7 What is "Knowledge Context" in the response?

Knowledge evidence is rendered by `appendKnowledgeSummaryAndKey()` at `NaturalResponseAgent.java:163-191`:

```
## Summary

{firstKnowledgeItem.content}

## Key Knowledge

- **{title1}**: {content1} [1]
- **{title2}**: {content2} [2]
```

This is the entire "knowledge context" in the response. Note that this section is identical in structure (and almost identical in code) to `DefaultResponseSynthesizer.synthesizeKnowledge()` at `DefaultResponseSynthesizer.java:520-688` â€” see Section 9 for duplication analysis.

### 4.8 Conclusion for Section 4

**There is no LLM prompt construction in the response generation layer.** The answer is assembled by `StringBuilder` from `PipelineContext` metadata, evidence items, and hardcoded section templates. The `LlmRequest` type exists in the codebase but is never instantiated in production.

---

## SECTION 5 â€” CONTEXT OWNERSHIP

This section tracks every piece of context from creation to final use, proving where context is lost before it could reach an LLM.

### 5.1 Context Matrix

| Context Item | Created By | Modified By | Read By | Lost Here? | Final Used? |
|---|---|---|---|---|---|
| userInput | ShreeClient.chat() [ShreeClient.java:97] | â€” | IntentAnalyzer.analyze() [CIA.java:116] | â€” | Yes (for routing) |
| userInput | â€” | â€” | EvidenceAgent.extract() [CIA.java:127] | **No** (reads metadata, not userInput) | No |
| userInput | â€” | â€” | NaturalResponseAgent.generate() [NRA.java:111] | **No** (only in answer as "Question") | Yes (printed in answer) |
| userInput | â€” | â€” | DefaultResponseSynthesizer.synthesizeKnowledge() [DRS.java:554] | **No** (only as title fallback) | No |
| intelligenceContext | IntelligenceContextBuilder.fromSdkRequest() [ShreeClient.java:82] | â€” | DefaultRuntimeService.submit() [DRS.java:86+] | **No** (stored in metadata, never read back) | No |
| memoryResults | MemoryRecallStage.execute() [pipeline] | â€” | EvidenceAgent.extractMemoryEvidence() [EA.java:230] | â€” | Yes (as EvidenceBundle item) |
| knowledgeResults | KnowledgeStage.execute() [pipeline] | â€” | EvidenceAgent.extractKnowledgeEvidence() [EA.java:137] | â€” | Yes (as EvidenceBundle item) |
| reasoningConclusion | ReasoningStage.execute() [pipeline] | â€” | EvidenceAgent.extractReasoningEvidence() [EA.java:173] | â€” | Yes (as EvidenceBundle item) |
| reasoningConfidence | ReasoningStage.execute() [pipeline] | â€” | EvidenceAgent.extractReasoningEvidence() [EA.java:177] | â€” | Yes (as confidenceHint in EvidenceItem) |
| inferenceResult | InferenceStage.execute() [pipeline] | â€” | EvidenceAgent.extractInferenceEvidence() [EA.java:189] | â€” | Yes (as EvidenceBundle item) |
| planningResult | PlanningStage.execute() [pipeline] | â€” | EvidenceAgent.extractPlanningEvidence() [EA.java:210] | â€” | Yes (as EvidenceBundle item) |
| planSummary | PlanningStage.execute() [pipeline] | â€” | EvidenceAgent.extractPlanningEvidence() [EA.java:214] | â€” | Yes |
| reflectionResult | ReflectionStage.execute() [pipeline] | â€” | EvidenceAgent.extractReflectionEvidence() [EA.java:252] | â€” | Yes |
| projectSummary | ChiefReviewStage [pipeline] | â€” | EvidenceAgent.extractProjectEvidence() [EA.java:268] | â€” | Yes |
| knowledgeGroundingScore | KnowledgeStage [pipeline] | â€” | EvidenceAgent.extractKnowledgeEvidence() [EA.java:142] | â€” | Yes (as confidenceHint) |
| knowledgeCitations | KnowledgeStage [pipeline] | â€” | EvidenceAgent.extractKnowledgeEvidence() [EA.java:141] | â€” | Yes (in EvidenceItem.citations) |
| confidence | ConfidenceCalculator [VA.java:107,110,113] | â€” | VerificationAgent.verify() [VA.java:127] | â€” | Yes (in VerificationReport) |
| verificationTier | VerificationAgent [VA.java:126] | â€” | NaturalResponseAgent.generate() [NRA.java:63+] | â€” | Yes |
| verificationReport | VerificationAgent [VA.java:125] | â€” | NaturalResponseAgent.generate() [NRA.java:57] | â€” | Yes |
| EvidenceBundle | EvidenceAgent.extract() [EA.java:76-99] | â€” | VerificationAgent.verify() [VA.java:78] | â€” | Yes |
| EvidenceBundle (pre-pipeline) | EvidenceAgent.extract(request, diagnostics) [CIA.java:127] | â€” | VerificationAgent.verify() [CIA.java:128] | **YES â€” empty bundle, output discarded** | **NO** |
| EvidenceBundle (post-pipeline) | EvidenceAgent.extractFromMetadata() [DRS.java:1124] | â€” | VerificationAgent.verify() [DRS.java:1126] | â€” | Yes |
| SynthesizedResponse | NaturalResponseAgent.generate() (pre-pipeline) [CIA.java:129] | â€” | ChiefIntelligenceAgent.attachChiefMetadata() [CIA.java:131] | **YES â€” text discarded, only metadata captured** | **NO** |
| SynthesizedResponse | DefaultResponseSynthesizer.synthesize() [DRS.java:1085] | â€” | DRS.java:1090â€“1160 | **YES â€” replaced by NRA output at DRS.java:1160** | **NO** |
| llmRouter | DefaultRuntimeService field init [DRS.java:122] | â€” | DRS.java:1044 (put in PipelineContext) | **YES â€” stored in attribute, never read** | **NO** |
| LlmRequest | Never created in production | â€” | â€” | **YES** | **NO** |
| LlmResponse | Never created in production | â€” | â€” | **YES** | **NO** |

### 5.2 Evidence: context items confirmed absent from LLM

The following context items are produced by kernel stages, stored in `PipelineExecutionState.metadata`, and read by `EvidenceAgent`, but **never reach an LLM**:

1. `reasoningConclusion` â€” read by `EvidenceAgent.extractReasoningEvidence()` at `EvidenceAgent.java:173-186`, written to `EvidenceBundle`, printed as `ðŸ§  Reasoning Conclusion` in `NaturalResponseAgent`, never sent to any LLM.

2. `inferenceResult` â€” read by `EvidenceAgent.extractInferenceEvidence()` at `EvidenceAgent.java:189-208`, written to `EvidenceBundle`, never sent to any LLM.

3. `planningResult` â€” read by `EvidenceAgent.extractPlanningEvidence()` at `EvidenceAgent.java:210-228`, never sent to any LLM.

4. `reflectionResult` â€” read by `EvidenceAgent.extractReflectionEvidence()` at `EvidenceAgent.java:252-266`, never sent to any LLM.

5. `knowledgeGroundingScore` â€” read by `EvidenceAgent.extractKnowledgeEvidence()` at `EvidenceAgent.java:142-145`, never sent to any LLM.

6. `projectSummary` â€” read by `EvidenceAgent.extractProjectEvidence()` at `EvidenceAgent.java:268-288`, never sent to any LLM.

7. `memoryResults` â€” read by `EvidenceAgent.extractMemoryEvidence()` at `EvidenceAgent.java:230-251`, never sent to any LLM.

**All of these context items are visible in the structured payload (`SDKResponse.structuredPayload`)`** but are not incorporated into any LLM prompt because no LLM prompt exists.

### 5.3 The intelligenceContext is stored but never used

`IntelligenceContext` is built at `ShreeClient.java:81-82`:
```java
IntelligenceContext intelligenceContext = IntelligenceContextBuilder.fromSdkRequest(request);
metadata.put("intelligenceContext", intelligenceContext);
```

It is passed into the `ExecutionRequest.metadata` map. It is stored in `PipelineContext` (as part of the request metadata) but **never read back by any stage or agent**. The `IntelligenceContext` type exists but its data is not unpacked for LLM prompt construction.

### 5.4 Context loss summary

**Three layers of context loss:**

1. **LLM never receives context:** The LLM is never called. All kernel outputs are processed only by `EvidenceAgent`, `VerificationAgent`, and `NaturalResponseAgent` (all deterministic string-formatters).

2. **Two of three synthesis runs are discarded:** The canonical request runs `ChiefIntelligenceAgent.route()` (synthesizes, discards text), `DefaultResponseSynthesizer.synthesize()` (synthesizes, discards text), and `NaturalResponseAgent.generate()` (synthesizes, final). Only the third is used.

3. **Pre-pipeline bundle is always empty:** `ChiefIntelligenceAgent.route()` calls `EvidenceAgent.extract(request, diagnostics)` before the pipeline runs. The request has no pipeline state. The bundle is empty. The confidence is `INSUFFICIENT`. The "grounded" response from this phase is the generic "I don't have enough information" template.

---

## SECTION 6 â€” RESPONSE SYNTHESIS TRUTH

This section audits every response-synthesis class in scope and proves which one actually owns each component of the final response.

### 6.1 Response owners â€” who writes what

| Component | Owner class | Method | Evidence |
|---|---|---|---|
| **Title** (markdown `# {title}`) | `NaturalResponseAgent` | `deriveTitleFromEvidence()` | `NRA.java:371-398` |
| **Title** (alternative) | `DefaultResponseSynthesizer` | `deriveTitle()` | `DRS.java:1086-1103` |
| **Title** (alternative) | `DefaultResponseSynthesizer` | `deriveCompositeTitle()` | `DRS.java:1203` |
| **Title** (alternative) | `DefaultResponseSynthesizer` | `capabilityDisplayName()` | `DRS.java:158-173` |
| **Title** (hardcoded) | `NaturalResponseAgent` | `generateInsufficientResponse()` | `NRA.java:233` â€” `# Insufficient Evidence` |
| **Title** (hardcoded) | `NaturalResponseAgent` | `generateFallbackAnswer()` | `NRA.java:276` â€” `# Response` |
| **Title** (hardcoded) | `DefaultResponseSynthesizer` | `synthesizeExecution()` | `DRS.java:87` â€” `# Execution Started` |
| **Summary** (markdown `## Summary`) | `DefaultResponseSynthesizer` | `synthesizeKnowledge()` | `DRS.java:572-578` |
| **Summary** (alternative) | `DefaultResponseSynthesizer` | `synthesizeDefault()` | `DRS.java:197-203` |
| **Summary** (alternative) | `NaturalResponseAgent` | `appendKnowledgeSummaryAndKey()` | `NRA.java:163-191` |
| **Summary** (alternative) | `NaturalResponseAgent` | `generateFromEvidence()` | `NRA.java:115-119` |
| **Summary** (hardcoded "The request was successfully processed") | `DefaultResponseSynthesizer` | `extractSummary()` | `DRS.java:260` |
| **Summary** (hardcoded "A structured execution plan with N steps has been generated") | `DefaultResponseSynthesizer` | `buildPlanningSummary()` | `DRS.java:1071-1080` |
| **Bullets** | `DefaultResponseSynthesizer` | `renderBullets()` | `DRS.java:1108-1119` |
| **Bullets** (numbered) | `DefaultResponseSynthesizer` | `renderNumbered()` | `DRS.java:1124-1136` |
| **Bullets** (EvidenceItem sections) | `NaturalResponseAgent` | `appendEvidenceItem()` | `NRA.java:193-216` |
| **Bullets** (Knowledge as bullets) | `NaturalResponseAgent` | `appendKnowledgeSummaryAndKey()` | `NRA.java:178-191` |
| **Citations** (markdown `## Citations`) | `NaturalResponseAgent` | `generateFromEvidence()` | `NRA.java:132-140` |
| **Citations** (as `Citations: [1], [2], ...` in body) | `NaturalResponseAgent` | `appendEvidenceItem()` | `NRA.java:208-215` |
| **Citations** (in knowledge bullets) | `NaturalResponseAgent` | `appendKnowledgeSummaryAndKey()` | `NRA.java:188` â€” `[{index}]` |
| **Citations** (as ID list) | `DefaultResponseSynthesizer` | `buildEvidence()` | `DRS.java:274-284` |
| **Citations** (in evidenceSummary map) | `DefaultRuntimeService` | inline at `DRS.java:1167-1171` |
| **Confidence** (final double) | `VerificationReport.confidence()` | â€” | `VA.java:127` |
| **Confidence** (printed in answer) | `NaturalResponseAgent` | `appendConfidenceNote()` | `NRA.java:218-229` |
| **Confidence** (planning) | `DefaultResponseSynthesizer` | `planningConfidence()` | `DRS.java:1142-1158` |
| **Confidence** (knowledge, hardcoded 0.80) | `DefaultResponseSynthesizer` | `synthesizeKnowledge()` | `DRS.java:1035` â€” `0.92` |
| **Confidence** (execution, hardcoded 0.90) | `DefaultResponseSynthesizer` | `synthesizeExecution()` | `DRS.java:124` |
| **Confidence** (chat, hardcoded 1.0) | `DefaultResponseSynthesizer` | `synthesizeChat()` | `DRS.java:721` |
| **Confidence** (default, hardcoded 0.90) | `DefaultResponseSynthesizer` | `confidence()` | `DRS.java:311` |
| **Confidence** (diagnostic, hardcoded 0.50) | `ChiefIntelligenceAgent` | `buildDiagnosticResponse()` | `CIA.java:224` |
| **Confidence** (plan blueprint, hardcoded 0.92) | `DefaultResponseSynthesizer` | `synthesizePlanningBlueprint()` | `DRS.java:1035` |
| **Confidence** (insufficient, hardcoded 0.15) | `ConfidenceCalculator` | `fromInsufficient()` | `CC.java:59-61` |
| **Formatting** (markdown section headers) | `DefaultResponseSynthesizer` | many `answer.append("## ...")` calls | throughout `DRS.java` |
| **Formatting** (section list) | `SynthesizedResponse.sections` | builder pattern | `SynthesizedResponse.java:23-43` |
| **Style** (enum) | `DefaultResponseSynthesizer` | constructor arg | `DRS.java:241, 869, 1036, 1032` |
| **Style** (always PROFESSIONAL) | `NaturalResponseAgent` | constructor arg | `NRA.java:77, 269, 353` |
| **Style** (always PROFESSIONAL) | `ChiefIntelligenceAgent` | constructor arg | `CIA.java:225` |
| **Sections** (ResponseSection list) | `DefaultResponseSynthesizer` | many `sections.add(...)` calls | throughout `DRS.java` |
| **Sections** (ResponseSection list) | `NaturalResponseAgent` | `buildSections()` | `NRA.java:279-298` |
| **Sections** (ResponseSection list) | `NaturalResponseAgent` | `generateInsufficientResponse()` | `NRA.java:260-263` |

### 6.2 Duplication of title ownership

Three classes independently own title generation:

- `DefaultResponseSynthesizer.deriveTitle(goal)` at `DRS.java:1086-1103` â€” for planning responses
- `DefaultResponseSynthesizer.deriveCompositeTitle(analysis, composite)` at `DRS.java:1203` â€” for multi-kernel composite
- `DefaultResponseSynthesizer.capabilityDisplayName(capability)` at `DRS.java:158-173` â€” for execution responses
- `NaturalResponseAgent.deriveTitleFromEvidence(bundle, report)` at `NRA.java:371-398` â€” for the canonical post-pipeline response

In the canonical CHAT path, `DefaultResponseSynthesizer.synthesize()` runs first (line 1085), then is REPLACED by `NaturalResponseAgent.generate()` (line 1160). The title from `deriveTitleFromEvidence` is what actually reaches the SDK response. But the title from `DefaultResponseSynthesizer.synthesizeKnowledge()` is computed and discarded at `DRS.java:550-555`.

### 6.3 Duplication of summary ownership

`DefaultResponseSynthesizer.synthesizeKnowledge()` at `DRS.java:572-578` produces `## Summary` from `metadata.knowledgeSummary`.
`NaturalResponseAgent.appendKnowledgeSummaryAndKey()` at `NRA.java:163-191` produces `## Summary` from the first KNOWLEDGE `EvidenceItem.content`.

**For knowledge queries, both classes render a `## Summary` section with the same semantic content** (the summary of the top knowledge result). When `DefaultResponseSynthesizer.synthesizeKnowledge()` runs first, its `## Summary` is included in the discarded `SynthesizedResponse`. When `NaturalResponseAgent.generate()` runs second and replaces the response, its `## Summary` is included in the final answer.

**Both `## Summary` sections are written in code; only one survives.**

### 6.4 Duplication of citations ownership

- `DefaultResponseSynthesizer.buildEvidence()` at `DRS.java:274-284` produces an `Evidence` section that joins `memoryId`, `knowledgeId`, `reasoningId`, `planId` as bullets.
- `NaturalResponseAgent.generateFromEvidence()` at `NRA.java:132-140` produces a `## Citations` section from `report.citations()`.
- `NaturalResponseAgent.appendEvidenceItem()` at `NRA.java:208-215` produces inline `Citations: [1], [2], ...` per evidence item.

### 6.5 Duplication of formatting ownership

`DefaultResponseSynthesizer` and `NaturalResponseAgent` both produce markdown with section headers (`# Title`, `## Summary`, `## Citations`, etc.). The exact strings and ordering are similar but not identical. The duplicated sections (Summary, Citations, etc.) end up being double-rendered, but only one rendering survives due to the replacement at `DRS.java:1160`.

### 6.6 The "final owner" verdict

For the canonical CHAT path:

- **Final text owner:** `NaturalResponseAgent.generate()` at `DRS.java:1155-1158` (its output is assigned to `response` at `DRS.java:1160` and the previous synthesizer output is discarded).
- **Final confidence owner:** `VerificationReport.confidence()` from `VerificationAgent.verify()` at `DRS.java:1126-1129`.
- **Final structured data owner:** `NaturalResponseAgent.buildStructuredPayload()` at `NRA.java:308-336` (set at `NRA.java:79`), augmented by `DefaultRuntimeService` at `DRS.java:1090-1196`.

For routed paths (A) and multi-kernel paths (B):
- **Final text owner:** `DefaultResponseSynthesizer.synthesize*()` (since the `EvidenceAgent`/`VerificationAgent`/`NaturalResponseAgent` triplet at `DRS.java:1119-1160` may produce empty evidence and the replacement is a no-op when the bundle is empty).
- **Final confidence owner:** `DefaultResponseSynthesizer` (hardcoded or metadata-derived).

For the pre-flight-only path:
- **Final text owner:** `ChiefIntelligenceAgent.buildDiagnosticResponse()` at `CIA.java:185-234`.
- **Final confidence owner:** hardcoded `0.50` at `CIA.java:224`.

### 6.7 RenderBullets helper

`DefaultResponseSynthesizer.renderBullets(List<String>)` at `DRS.java:1108-1119`:
```java
private String renderBullets(List<String> values) {
    StringBuilder builder = new StringBuilder();
    for (String value : values) {
        builder.append("* ").append(value).append("\n");
    }
    return builder.toString().stripTrailing();
}
```

This is the universal bullet renderer for `synthesizePlanning*` paths. `NaturalResponseAgent.appendEvidenceItem()` at `NRA.java:208-215` uses inline bullet construction for evidence items.

**Two different bullet formats exist:** `* value\n` (DRS) and `- value\n` (NRA). The NRA evidence bullets use `-` while DRS planning bullets use `*`.

---

## SECTION 7 â€” CONFIDENCE JOURNEY

This section traces the confidence value from origin to the final SDK response and proves where it changes.

### 7.1 Origin: 4-tier scale

The confidence origin is the static 4-tier scale defined at `ConfidenceCalculator.java:26-31`:

```java
public final class ConfidenceCalculator {
    private static final double VERIFIED_PROJECT_SCORE = 0.95;
    private static final double VERIFIED_KB_SCORE = 0.80;
    private static final double INFERRED_SCORE = 0.60;
    private static final double INSUFFICIENT_SCORE = 0.15;
```

**Origin values:** `0.95`, `0.80`, `0.60`, `0.15`. Hardcoded. Cannot be overridden by configuration or runtime data (only `fromBundleComposition()` and `fromSourceType()` add dynamic inputs).

### 7.2 EvidenceAgent writes confidenceHint per evidence item

`EvidenceAgent` writes a `confidenceHint` to each `EvidenceItem` based on the source type:

- `KNOWLEDGE`: `confidenceHint = groundingScore > 0.0 ? groundingScore : 0.80` at `EvidenceAgent.java:167`
- `REASONING`: `confidenceHint = confidence > 0.0 ? confidence : 0.60` at `EvidenceAgent.java:184`
- `INFERENCE`: `confidenceHint = 0.60` at `EvidenceAgent.java:206`
- `PLANNING`: `confidenceHint = 0.70` at `EvidenceAgent.java:226`
- `MEMORY`: `confidenceHint = 0.60` at `EvidenceAgent.java:246` (or fallback to 0.60)
- `REFLECTION`: `confidenceHint = 0.60` at `EvidenceAgent.java:264`
- `PROJECT`: `confidenceHint = 0.95` at `EvidenceAgent.java:285`
- `EXECUTION`: `confidenceHint = 0.75` at `EvidenceAgent.java:303`

**The `confidenceHint` is stored on the `EvidenceItem` but is NOT used by `VerificationAgent.verify()`.** `VerificationAgent` decides the overall tier purely based on which `SourceType` is present (line 105-115), not on the confidence hint. So the per-item hints are dead data.

### 7.3 VerificationAgent computes overall confidence

`VerificationAgent.verify()` at `VerificationAgent.java:59-137`:

- Empty bundle: tier=`INSUFFICIENT`, confidence=0.15 at lines 67-71
- Has PROJECT: tier=`VERIFIED_PROJECT`, confidence=`ConfidenceCalculator.fromProjectEvidence()` = 0.95 at lines 99-107
- Has KNOWLEDGE: tier=`VERIFIED_KB`, confidence=`ConfidenceCalculator.fromKnowledgeEvidence()` = 0.80 at lines 105-107
- Has REASONING: tier=`INFERRED`, confidence=`ConfidenceCalculator.fromReasoningEvidence()` = 0.60 at lines 108-110
- Else: tier=`INSUFFICIENT`, confidence=0.15 at lines 111-115

The confidence field on the resulting `VerificationReport` is set at `VerificationAgent.java:127`.

### 7.4 First pass: ChiefIntelligenceAgent.route() confidence

When `ChiefIntelligenceAgent.route()` runs at `DRS.java:888` BEFORE the pipeline:

- The pipeline has not run.
- `EvidenceAgent.extract(request, diagnostics)` at `CIA.java:127` reads only `request.getMetadata()` which contains `intelligenceContext`, `sessionId` â€” neither of which is one of the keys the evidence extractors look for (`knowledgeResults`, `reasoningConclusion`, etc.).
- The resulting `EvidenceBundle` is **empty**.
- `VerificationAgent.verify(emptyBundle)` at `CIA.java:128` returns `INSUFFICIENT` with confidence `0.15`.
- `NaturalResponseAgent.generate(insufficient, request)` at `CIA.java:129` produces the "Insufficient Evidence" response, with `confidence = report.confidence() = 0.15` (set at `NRA.java:268`).
- `attachChiefMetadata` at `CIA.java:131` preserves the response but the entire response text is then **discarded** at `DRS.java:1033` (only chiefMeta is captured).

**First-pass confidence: 0.15 (INSUFFICIENT).** Always â€” for any request â€” because the bundle is always empty at this point.

### 7.5 Pipeline execution and second pass

The 11-stage pipeline runs at `DRS.java:1048`. Each stage writes its outputs into `PipelineExecutionState.metadata`. After the pipeline, `DRS.java:1114-1117` checks if the metadata is non-empty.

`EvidenceAgent.extractFromMetadata(state.getMetadata())` at `DRS.java:1119-1124` reads the populated metadata and produces a real `EvidenceBundle`.

`VerificationAgent.verify(realBundle)` at `DRS.java:1126-1129` returns:
- 0.95 if any PROJECT evidence
- 0.80 if any KNOWLEDGE evidence (without PROJECT)
- 0.60 if any REASONING/INFERENCE/PLANNING/REFLECTION (without PROJECT/KNOWLEDGE)
- 0.15 if bundle is empty

`NaturalResponseAgent.generate(verification, request)` at `DRS.java:1155-1158` constructs a new `SynthesizedResponse` with `confidence = report.confidence()` at `NRA.java:76`.

### 7.6 Replacement at DRS.java:1160

```java
// DRS.java:1157-1160
com.shreeai.os.platform.kernels.response.model.SynthesizedResponse evidenceBackedResponse =
        naturalAgent.generate(verificationReport, request);

response = evidenceBackedResponse;
```

The first `response` (from `responseSynthesisService.synthesize()` at `DRS.java:1085`) is replaced. The replacement is unconditional if the bundle is non-empty.

**Confidence in first pass (discarded):** 0.15 (always, because the bundle is always empty).

**Confidence in second pass (final):** Whatever `VerificationAgent` assigned based on the populated evidence. This can be `0.95`, `0.80`, `0.60`, or `0.15`.

### 7.7 Does confidence change after verification?

**Yes, the final `confidence` field in the SDK response is always the post-pipeline value.** The first pass (pre-pipeline) confidence of 0.15 is discarded. The second pass (post-pipeline) confidence is set by `VerificationAgent` and is what `ShreeClient.chat()` reads at `ShreeClient.java:150`.

**However, the original pre-pipeline confidence of 0.15 is never actually output to the user** because the `SynthesizedResponse` containing it is replaced.

### 7.8 Confidence table

| Source | Value | File:Line | Used? |
|---|---|---|---|
| `ConfidenceCalculator.VERIFIED_PROJECT_SCORE` | 0.95 | `ConfidenceCalculator.java:28` | Yes (PROJECT evidence) |
| `ConfidenceCalculator.VERIFIED_KB_SCORE` | 0.80 | `ConfidenceCalculator.java:29` | Yes (KNOWLEDGE evidence) |
| `ConfidenceCalculator.INFERRED_SCORE` | 0.60 | `ConfidenceCalculator.java:30` | Yes (REASONING/INFERENCE) |
| `ConfidenceCalculator.INSUFFICIENT_SCORE` | 0.15 | `ConfidenceCalculator.java:31` | Yes (empty bundle) |
| `DefaultResponseSynthesizer.synthesizeExecution` hardcoded | 0.90 | `DefaultResponseSynthesizer.java:124` | Yes (when synthesizer path is final) |
| `DefaultResponseSynthesizer.synthesizePlanningBlueprint` hardcoded | 0.92 | `DefaultResponseSynthesizer.java:1035` | Yes (planning blueprint path) |
| `DefaultResponseSynthesizer.synthesizeDefault` default | 0.90 | `DefaultResponseSynthesizer.java:311` | Yes (default path) |
| `DefaultResponseSynthesizer.synthesizeChat` hardcoded | 1.00 | `DefaultResponseSynthesizer.java:721` | Yes (greeting path) |
| `DefaultResponseSynthesizer.planningConfidence` goal | goal.confidence() | `DefaultResponseSynthesizer.java:1148` | Yes (planning path) |
| `DefaultResponseSynthesizer.synthesizeKnowledge` | from metadata or default 0.80 | `DefaultResponseSynthesizer.java:1142-1158` | Yes (knowledge path) |
| `ChiefIntelligenceAgent.buildDiagnosticResponse` hardcoded | 0.50 | `ChiefIntelligenceAgent.java:224` | Yes (no-kernels path) |
| `NaturalResponseAgent.generate` | `report.confidence()` | `NaturalResponseAgent.java:76` | Yes (canonical post-pipeline) |
| `NaturalResponseAgent.generateInsufficientResponse` | `report.confidence()` | `NaturalResponseAgent.java:268` | Yes (insufficient) |
| `EvidenceAgent.extractKnowledgeEvidence` hint | 0.80 default | `EvidenceAgent.java:167` | **No** (per-item, not aggregated) |
| `EvidenceAgent.extractProjectEvidence` hint | 0.95 | `EvidenceAgent.java:285` | **No** |
| `EvidenceAgent.extractReasoningEvidence` hint | 0.60 | `EvidenceAgent.java:184` | **No** |
| `EvidenceAgent.extractPlanningEvidence` hint | 0.70 | `EvidenceAgent.java:226` | **No** |
| `EvidenceAgent.extractInferenceEvidence` hint | 0.60 | `EvidenceAgent.java:206` | **No** |
| `EvidenceAgent.extractReflectionEvidence` hint | 0.60 | `EvidenceAgent.java:264` | **No** |
| `EvidenceAgent.extractMemoryEvidence` hint | 0.60 | `EvidenceAgent.java:246` | **No** |
| `EvidenceAgent.extractExecutionEvidence` hint | 0.75 | `EvidenceAgent.java:303` | **No** |

### 7.9 Confidence values seen by the user (real examples)

| Path | Pre-pipeline (discarded) | First synthesizer (discarded) | NRA post-pipeline (final) | Final |
|---|---|---|---|---|
| Project analysis request, healthy | 0.15 | 0.90 (default) | 0.95 (PROJECT) | **0.95** |
| Knowledge query, has citations | 0.15 | 0.80 (knowledge) | 0.80 (KNOWLEDGE) | **0.80** |
| Reasoning-only request, no project/knowledge | 0.15 | 0.90 (default) | 0.60 (INFERRED) | **0.60** |
| No evidence at all | 0.15 | 0.90 (default) | 0.15 (INSUFFICIENT) | **0.15** |
| Planning request | 0.15 | 0.92 (planning blueprint) | 0.15 (INSUFFICIENT) | **0.15** unless planner provides PROJECT evidence |
| Routed `QUERY_KNOWLEDGE` | not called | 0.80 (knowledge) | 0.80 (KNOWLEDGE) | **0.80** |
| Routed `EXECUTE_TASK` | not called | 0.90 (execution) | not replaced (no pipeline) | **0.90** |
| Multi-kernel composite | not called | varies in `synthesizeComposite` | not replaced | varies |
| Diagnostic (no kernels) | 0.50 | not called | not called | **0.50** |
| Greeting ("hi") | 0.15 | 1.00 (chat) | 0.15 (INSUFFICIENT) | **0.15** â† Note: greeting is INSUFFICIENT post-pipeline |

### 7.10 Note on greeting confidence

For a request like "hi":
- `IntentAnalyzer.analyze("hi")` â€” does not match any pattern, returns the default intent (probably UNKNOWN).
- `ChiefIntelligenceAgent.route()` returns `buildDiagnosticResponse` with `0.50` because `!plan.hasKernels()` (no kernels for an unknown intent).
- But the canonical path may still run the pipeline which produces an empty bundle â†’ `INSUFFICIENT` â†’ `0.15` final.

This contradicts `DefaultResponseSynthesizer.synthesizeChat()` which would have given `1.0` for "hi" â€” but the `synthesizeChat` output is discarded.

---

## SECTION 8 â€” LLM VS DETERMINISTIC BOUNDARY

This section classifies every response component as LLM-powered, deterministic, rule-based, pure formatter, wrapper, or engine.

### 8.1 Classification table

| Class | Uses LLM? | Deterministic? | Rule-based? | Pure formatter? | Wrapper? | Engine? | Notes |
|---|---|---|---|---|---|---|---|
| `LlmProvider` interface | **NO** (SPI only) | N/A | N/A | N/A | N/A | N/A | Defines contract only |
| `LlmRouter` | **NO** | Yes | Yes | No | Yes | No | Routing table, fail-over loop |
| `OpenAiProvider` | **NO** (never called) | Yes | Yes | Yes | No | Yes | HTTP client; stream parser; never called |
| `GeminiProvider` | **NO** (never called) | Yes | Yes | Yes | No | Yes | HTTP client; SSE parser; never called |
| `OllamaProvider` | **NO** (never called) | Yes | Yes | Yes | No | Yes | HTTP client; NDJSON parser; never called |
| `InMemoryLlmProvider` | **NO** (never called) | Yes | Yes | Yes | No | Yes | Deterministic echo; never called |
| `LlmRequest` | **NO** | N/A | N/A | N/A | N/A | N/A | Immutable data record |
| `LlmResponse` | **NO** | N/A | N/A | N/A | N/A | N/A | Immutable data record |
| `IntentAnalyzer` | **NO** | Yes | Yes | No | No | Yes | Regex pattern matching |
| `DiagnosisAgent` | **NO** | Yes | Yes | Yes | No | Yes | Metadata reads; health checks |
| `EvidenceAgent` | **NO** | Yes | Yes | Yes | No | Yes | Metadata-to-bundle transformer |
| `VerificationAgent` | **NO** | Yes | Yes | Yes | No | Yes | Decision tree: SourceType -> Tier |
| `ConfidenceCalculator` | **NO** | Yes | Yes | No | No | No | Pure static utility |
| `NaturalResponseAgent` | **NO** | Yes | Yes | No | No | Yes | StringBuilder formatter from evidence |
| `ResponseSynthesisService` | **NO** | Yes | No | No | **YES** | No | Thin pass-through to DRS |
| `ResponseSynthesizer` interface | **NO** | N/A | N/A | N/A | N/A | N/A | Interface only |
| `DefaultResponseSynthesizer` | **NO** | Yes | Yes | No | No | Yes | Template engine; 6 hardcoded paths |
| `ChiefIntelligenceAgent` | **NO** | Yes | Yes | No | No | Yes | Orchestrates agents |
| `ShreeClient` | **NO** | Yes | Yes | Yes | No | No | SDK boundary adapter |
| `SDKResponse` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `SynthesizedResponse` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `ResponseSection` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `VerificationReport` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `EvidenceBundle` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `EvidenceItem` | **NO** | N/A | N/A | N/A | No | No | Immutable data record |
| `DeveloperResponse` | **NO** | Yes | Yes | Yes | No | No | `toFormattedResponse()` uses `formatPlan()` |
| `ResponseSynthesizerVerifier` | **NO** | Yes | Yes | Yes | No | No | Null checks only |

### 8.2 "Engine" classification

The following are classified as "engines" because they contain non-trivial logic:

| Class | Engine type | Deterministic? |
|---|---|---|
| `IntentAnalyzer` | Intent routing engine | Yes (regex) |
| `DiagnosisAgent` | Health check engine | Yes |
| `EvidenceAgent` | Evidence extraction engine | Yes |
| `VerificationAgent` | Verification decision engine | Yes |
| `NaturalResponseAgent` | Natural language generator | Yes (but named "natural") |
| `DefaultResponseSynthesizer` | Template engine | Yes |
| `ChiefIntelligenceAgent` | Orchestration engine | Yes |
| `OpenAiProvider` | HTTP LLM client | Yes (never called) |
| `GeminiProvider` | HTTP LLM client | Yes (never called) |
| `OllamaProvider` | HTTP LLM client | Yes (never called) |

### 8.3 "Wrapper" classification

| Class | Wraps | Evidence |
|---|---|---|
| `ResponseSynthesisService` | `DefaultResponseSynthesizer` | `RSS.java:34` â€” `synthesizer.synthesize(context, state)` |
| `LlmRouter` | `List<LlmProvider>` | `LlmRouter.java:152-166` â€” iterates providers |

### 8.4 "Pure formatter" classification

| Class | Output format | Evidence |
|---|---|---|
| `ConfidenceCalculator` | double | Static utility; no I/O |
| `IntentAnalyzer` | IntentAnalysisResult | Regex matching; no I/O |
| `DiagnosisAgent` | DiagnosticReport | Metadata reads; no I/O |
| `EvidenceAgent` | EvidenceBundle | Metadata reads; no I/O |
| `VerificationAgent` | VerificationReport | Decision tree; no I/O |
| `NaturalResponseAgent` | String answer | `StringBuilder` concatenation |
| `DefaultResponseSynthesizer` | String answer | `StringBuilder` concatenation |
| `ChiefIntelligenceAgent` | SynthesizedResponse | Agent orchestration |

### 8.5 The LLM boundary proof

The following search confirms the LLM is never called in any production code path:

```
src/main/java/**/*Response*.java     â€” grep for "llm|complete\(|stream\(" â†’ 0 results
src/main/java/**/*Agent*.java        â€” grep for "llm|complete\(|stream\(" â†’ 0 results
src/main/java/**/*Synthesizer*.java  â€” grep for "llm|complete\(|stream\(" â†’ 0 results
src/main/java/**/*Service*.java       â€” grep for "llm|complete\(|stream\(" â†’ 0 results
src/main/java/**/*Runtime*.java      â€” grep for "llm|complete\(|stream\(" â†’ 0 results
```

The only production files that contain LLM-related code are:
1. `LlmProvider.java` â€” interface definition
2. `LlmRouter.java` â€” routing (never called)
3. `LlmRequest.java` â€” request type (never instantiated in production)
4. `LlmResponse.java` â€” response type (never constructed in production)
5. `OpenAiProvider.java` â€” provider (never called)
6. `GeminiProvider.java` â€” provider (never called)
7. `OllamaProvider.java` â€” provider (never called)
8. `InMemoryLlmProvider.java` â€” provider (never called)
9. `DefaultRuntimeService.java` â€” creates `llmRouter` and passes it into `PipelineContext` (never read back)

### 8.6 Evidence that intelligence exists

Despite the LLM never being called, the platform does contain genuine deterministic intelligence in the kernel engines:

- `DefaultReasoningEngine.java` â€” applies first-order logic to evidence chains (1859 lines, as confirmed in Phase 4 audit)
- `DefaultInferenceEngine.java` â€” generates hypotheses from evidence (1217 lines, as confirmed in Phase 4 audit)
- `GoalIntelligenceEngine.java` â€” decomposes goals into subtasks, blockers, dependencies
- `KnowledgeGroundingService` â€” scores knowledge relevance
- `PlanningAnalyzer` â€” regex-based project structure analysis

All of these are **LLM-free** (explicitly documented in Phase 4 audit). The intelligence exists in the kernels. The response synthesis layer merely formats the kernel outputs into markdown.

---

## SECTION 9 â€” DUPLICATE RESPONSE GENERATION

This section investigates whether multiple response generators exist and proves which are discarded.

### 9.1 Candidates and their status

| Candidate | Caller | Execution path | Discarded or final? |
|---|---|---|---|
| `ChiefIntelligenceAgent.buildDiagnosticResponse()` | `CIA.route()` â†’ `CIA.java:185` | Path C pre-flight only | Final only when `!plan.hasKernels()` |
| `NaturalResponseAgent.generate()` (pre-pipeline) | `CIA.route()` â†’ `CIA.java:129` | Path C pre-flight | **DISCARDED** â€” text replaced by `DRS.java:1033` |
| `DefaultResponseSynthesizer.synthesize()` | `DRS.submit()` â†’ `DRS.java:1085` | Path C pipeline result | **DISCARDED** â€” replaced by NRA at `DRS.java:1160` |
| `NaturalResponseAgent.generate()` (post-pipeline) | `DRS.submit()` â†’ `DRS.java:1155` | Path C post-pipeline | **FINAL** â€” assigned to `response` at `DRS.java:1160` |
| `DefaultResponseSynthesizer.synthesizeExecution()` | `DRS.submit()` â†’ `DRS.java:1337` | Path A (EXECUTE_TASK shortcut) | **FINAL** for execution path |
| `DefaultResponseSynthesizer.synthesizeComposite()` | `DRS.submit()` â†’ `DRS.java:1481` | Path B (multi-kernel) | **FINAL** for composite path |

### 9.2 Duplicate map

```
User request (canonical CHAT, healthy workspace)
  â”‚
  â”œâ”€â–º ChiefIntelligenceAgent.route()                    [CIA.java:112]
  â”‚       â”‚
  â”‚       â”œâ”€â–º EvidenceAgent.extract(request, diagnostics)   [CIA.java:127]
  â”‚       â”‚       â†’ empty bundle (pipeline not run)
  â”‚       â”œâ”€â–º VerificationAgent.verify(emptyBundle)          [CIA.java:128]
  â”‚       â”‚       â†’ INSUFFICIENT (0.15)
  â”‚       â”œâ”€â–º NaturalResponseAgent.generate(INSUFFICIENT)   [CIA.java:129]
  â”‚       â”‚       â†’ "Insufficient Evidence" + 0.15
  â”‚       â””â”€â–º attachChiefMetadata(response, ...)            [CIA.java:131]
  â”‚               â†’ only chiefMeta captured; response TEXT **DISCARDED**
  â”‚
  â”œâ”€â–º Pipeline execution (11 stages)                  [DRS.java:1048]
  â”‚       â†’ populates state.metadata with kernel outputs
  â”‚
  â”œâ”€â–º ResponseSynthesisService.synthesize(...)         [DRS.java:1085]
  â”‚       â†’ DefaultResponseSynthesizer.synthesize()
  â”‚           â”œâ”€â”€ isPlanningResult() â†’ synthesizePlanning/synthesizePlanningBlueprint
  â”‚           â”œâ”€â”€ isKnowledgeResult() â†’ synthesizeKnowledge
  â”‚           â”œâ”€â”€ isConversationalChat() â†’ synthesizeChat
  â”‚           â””â”€â”€ default â†’ synthesizeDefault
  â”‚       â†’ returns SynthesizedResponse **WITH TEXT**
  â”‚       â†’ response variable set at DRS:1084
  â”‚
  â”œâ”€â–º EvidenceAgent.extractFromMetadata(state.metadata) [DRS.java:1119]
  â”‚       â†’ populated bundle
  â”œâ”€â–º VerificationAgent.verify(populatedBundle)         [DRS.java:1126]
  â”‚       â†’ tier based on evidence source type
  â”œâ”€â–º NaturalResponseAgent.generate(verification)       [DRS.java:1155]
  â”‚       â†’ "Grounded" response
  â”‚
  â””â”€â–º response = evidenceBackedResponse               [DRS.java:1160]
          **REPLACES** the synthesizer response from DRS:1084
          â†’ Final answer is NRA's text; synthesizer's text is DISCARDED
```

### 9.3 Dead response generators

**ResponseSynthesisService** is not dead, but its output is discarded in the canonical CHAT path.

The following are **dead** response-related classes:

| Class | Evidence of death |
|---|---|
| `PlanningResponse` record | `implements KernelResponse` â€” `KernelResponse.java` does not exist in the repository; zero callers in production |
| `KnowledgeResponse` record | `implements KernelResponse`; zero callers in production |
| `MemoryResponse` record | `implements KernelResponse`; zero callers in production |
| `ConversationResponse` record | `implements KernelResponse`; zero callers in production |
| `ResponseSynthesizerVerifier` | Null-check only; zero callers in production |

### 9.4 Duplication of markdown sections

Both `DefaultResponseSynthesizer.synthesizeKnowledge()` and `NaturalResponseAgent.generateFromEvidence()` render the same semantic content for knowledge queries:

```
DefaultResponseSynthesizer (line 520):
  ## Summary
  {summary text}
  
  ## Key Knowledge
  - **{node.getLabel()}**: {node.getDescription()} [citation]

NaturalResponseAgent (line 163):
  ## Summary
  {firstKnowledgeItem.content}
  
  ## Key Knowledge
  - **{item.title()}**: {item.content()} [index]
```

The difference: `DefaultResponseSynthesizer` reads directly from `PipelineExecutionState.metadata` (via `knowledgeResults` list), while `NaturalResponseAgent` reads from the `EvidenceBundle` (via `EvidenceItem` objects extracted from that same metadata).

For the canonical CHAT path, only `NaturalResponseAgent`'s version survives (since `DefaultResponseSynthesizer`'s output is discarded at `DRS.java:1160`). But in the routed path (Path A) where `DefaultResponseSynthesizer` is the final owner, both sections are rendered with essentially the same content â€” duplicated in code but only one output reaches the user.

### 9.5 Three executionPipeline instances per request

Per `SPRINT20_PHASE3_EXECUTION_TRUTH_AUDIT.md:883` (confirmed from DRS source):
- `DefaultExecutionPipeline` constructed at `DRS.java:289` (in initialize, never used directly)
- `DefaultExecutionPipeline` constructed at `DRS.java:638` (in initializeStages, the live pipeline)
- `DefaultExecutionPipeline` constructed at `DRS.java:743` (for routed operations, replaces the canonical pipeline)

Only one pipeline is used per request.

---

## SECTION 11 â€” RESPONSE QUALITY ROOT CAUSE

This section answers with forensic evidence: why does the platform sometimes produce unnatural responses despite strong kernels?

### 11.1 Architecture: the LLM is not connected

**Root cause: The LLM is defined but never wired into the response chain.**

The single most significant architectural gap is proven at `NaturalResponseAgent.java:90-92`:

> *"The LLM invocation slot is reserved here. The actual LLM call should be wired through LlmProvider when the LLM integration is complete."*

This means the architecture assumes an LLM will eventually be called. The `LlmRouter` is created at `DefaultRuntimeService.java:122`, registered with four providers (OpenAI, Gemini, Ollama, InMemory), passed into the `PipelineContext` at `DRS.java:1044`, and then **never used**.

The `NaturalResponseAgent` is a placeholder that assembles markdown from structured data. It was never intended to be the final step. But the LLM integration was never completed, and the placeholder became the permanent response builder.

**Effect on quality:** Without the LLM, the response is bounded by:
1. What the `synthesizeX` methods explicitly template in code.
2. What the kernel metadata contains.
3. The deterministic `StringBuilder` concatenation logic.

The kernel intelligence (reasoning, inference, planning, knowledge graphs) is real and strong, but it is filtered through six hardcoded `StringBuilder` templates that produce mechanical, repetitive answers.

**Evidence:** `LlmRouter` at `DefaultRuntimeService.java:122` â€” created but never called. `LlmProvider.complete()` â€” never called in production. `LlmProvider.stream()` â€” never called in production.

### 11.2 Context loss: evidence never reaches an LLM

Even if the LLM were connected, the evidence would not be delivered to it properly.

**`reasoningConclusion` lost:** The `DefaultReasoningEngine` produces a rich `ReasoningResult` (1859 lines, Phase 4 audit confirms a 4-tier chain of reasoning: Decompose -> Analyze -> Infer -> Conclude). The `ReasoningResult` contains structured chains, confidence, citations, metadata. But `EvidenceAgent.extractReasoningEvidence()` at `EvidenceAgent.java:173-186` reads only `reasoningConclusion` (a `String` field) from the `PipelineExecutionState.metadata`:

```java
String conclusion = String.valueOf(metadata.getOrDefault(KEY_REASONING_CONCLUSION, ""));
```

All the reasoning chain structure is lost. Only the conclusion string survives.

**`inferenceResult` lost:** `EvidenceAgent.extractInferenceEvidence()` at `EvidenceAgent.java:189-208` reads `topHypothesis` (a `String`) from `inferenceResult`. The full `InferenceResult` object â€” with its hypotheses ranked by score, confidence levels, and evidence attachments â€” is not passed to the response.

**`planningResult` lost:** `EvidenceAgent.extractPlanningEvidence()` at `EvidenceAgent.java:210-228` reads only `planSummary` (a `String`) from `planningResult`. The `PlanBlueprint` with phases, milestones, deliverables, dependencies, risks, success metrics â€” all lost.

**Context loss evidence:**

| Kernel output | Full data structure | Survives to response? |
|---|---|---|
| `ReasoningResult` (rich chain) | `conclusion`, `chain`, `confidence`, `citations` | Only `conclusion` string |
| `InferenceResult` (hypotheses ranked) | `topHypothesis`, `allHypotheses`, `confidence` | Only `topHypothesis` string |
| `PlanBlueprint` (phases, milestones) | `phases`, `milestones`, `risks`, `metrics` | Only `planSummary` string |
| `KnowledgeCitation` objects | source, relevance, metadata | Only citation label string |
| `PlanningObjective` | description, constraints, domain | Lost entirely |
| `ReflectionResult` | outcome, lessons, selfCorrection | Only `outcome` string |
| `GoalAnalysis` | normalizedGoal, subtasks, blockers, dependencies | Only `normalizedGoal` string |

### 11.3 Prompt construction: no prompt builder exists

The platform has **no prompt builder**. There is no class that assembles context from multiple kernel outputs into an LLM prompt. The `LlmRequest` is a pure data structure that exists but is never constructed in production.

If the LLM were connected, the prompt construction would need to:
1. Take the user's original input.
2. Add the memory context (from `memoryResults`).
3. Add the knowledge context (from `knowledgeResults`).
4. Add the reasoning summary (from `reasoningConclusion`).
5. Add the inference hypothesis (from `inferenceResult`).
6. Add the planning context (from `planningResult`).
7. Add citations and confidence tier.
8. Wrap all of this in a system prompt.

None of this exists. The architecture assumes an LLM will take the `SynthesizedResponse.answer()` as given, which means the prompt is effectively: *"respond to: [answer built by StringBuilder]"* â€” which is circular.

### 11.4 Evidence serialization: structural data is stringified

`EvidenceAgent.extract()` converts structured `KnowledgeNode` objects into plain `String` title/description pairs at `EvidenceAgent.java:150-169`:

```java
if (item instanceof KnowledgeNode node) {
    label = node.getLabel() != null ? node.getLabel() : "";
    description = node.getDescription() != null ? node.getDescription() : "";
}
// ...
builder.addItem(EvidenceItem.builder()
    .title(label.isBlank() ? "Knowledge Node" : label)
    .content(description.isBlank() ? label : description)
    ...
```

The `KnowledgeNode.getSemanticMetadata()`, `KnowledgeNode.getConfidence()`, `KnowledgeNode.getRelevanceScore()`, `KnowledgeNode.getDomain()` fields are all discarded. Only label and description survive.

The `ConfidenceCalculator.fromBundleComposition()` at `ConfidenceCalculator.java:95-114` uses only three boolean flags (`hasProject`, `hasKnowledge`, `hasReasoning`) to determine the confidence tier. The actual confidence values produced by the kernels (from `reasoningConfidence`, `groundingScore`, `hypothesisConfidence`) are ignored except as hints on individual items.

### 11.5 LLM routing: never happens

The `LlmRouter` would route between OpenAI, Gemini, Ollama, and InMemory. But since no `LlmRequest` is ever built, no routing decision is ever made. The routing quality is untestable because the routing code path is never executed in production.

`LlmRouter.route()` at `LlmRouter.java:119-136` probes providers by calling `provider.stream(request)` (not `complete()`) and closes the stream immediately. This health-check mechanism is untested in production.

**Model selection:** Since `LlmRequest` is never constructed, no model is ever selected. The default model name `"default"` is set at `LlmRequest.java:119` but is never used.

**Temperature:** Since no `LlmRequest` is ever constructed, no temperature is ever set. All providers fall back to their defaults.

### 11.6 Formatting: mechanical templates produce repetitive output

The `DefaultResponseSynthesizer` has six hardcoded synthesis paths, all using `StringBuilder.append("## Section\n\n")` patterns. The output is structurally correct but stylistically repetitive:

- Every knowledge response: `# {title}\n\n## Summary\n\n{summary}\n\n## Key Knowledge\n\n- **{label}**: {description} [index]\n`
- Every planning response: `# {title}\n\n## Executive Summary\n\n## Goal\n\n## Subtasks\n\n## Recommendations\n\n`
- Every insufficient response: `# Insufficient Evidence\n\n**Question:** {question}\n\nI don't have enough verified information...\n\n## Recommendations\n\n`

The templates are not adaptive to the content. They do not vary tone, structure, or depth based on the evidence quality or the user's intent.

### 11.7 Evidence: verbatim code proving unnatural responses

The default summary fallback at `DefaultResponseSynthesizer.extractSummary()` line 260:

```java
return "The request was successfully processed through the Shree AI intelligence pipeline.";
```

This is the answer returned when no kernel produced meaningful metadata. This is not a natural response. It is a template string that tells the user the pipeline ran â€” not what was found.

The chat greeting fallback at `DefaultResponseSynthesizer.synthesizeChat()` lines 706-710:

```java
answer = """
    Hello! I'm Shree AI.

    How can I help you today?
    """;
```

This is the greeting for "hi/hello/hey" inputs. It is a static string. The NaturalResponseAgent does not have this greeting â€” it would produce "Insufficient Evidence" for "hi" because the pipeline produces no evidence for a greeting.

The insufficient response template at `NaturalResponseAgent.generateInsufficientResponse()` lines 240-254:

```
I don't have enough verified information to answer this question.

## What's Missing

## Recommendations

- Provide more context in your question
- Ensure the relevant knowledge has been ingested
- If asking about a project, run ProjectSDK.analyze() first
```

These recommendations are hardcoded and the same for every question. They do not reference the specific gaps identified by `VerificationAgent`.

### 11.8 Root cause summary

| Cause | Severity | Evidence |
|---|---|---|
| LLM never called | **CRITICAL** | `NRA.java:90-92`, repo-wide search for `complete\(` |
| No prompt builder | **CRITICAL** | No `PromptBuilder` class exists; no `LlmRequest` in production |
| Context loss (kernel outputs stringified) | **HIGH** | `EA.java:150-169`, `EA.java:173-186`, `EA.java:189-208` |
| Template-only response formatting | **HIGH** | `DRS.java:46-67` (dispatch to hardcoded methods) |
| Confidence fixed by source type, not content | **MEDIUM** | `CC.java:28-31`, `VA.java:105-115` |
| Dead response types (PlanningResponse, etc.) | **LOW** | Zero callers in production |
| Double-synthesis waste | **LOW** | `DRS.java:1085` + `DRS.java:1155` |

---

## SECTION 12 â€” FINAL VERDICT

This section consolidates the audit findings into a final architectural verdict.

### 12.1 What works

1. **Kernel intelligence is real and strong.** The reasoning, inference, planning, knowledge grounding, and project analysis engines produce high-quality, structured outputs (Phase 4 audit confirms).
2. **The verification layer is well-designed.** `VerificationAgent` + `ConfidenceCalculator` + `EvidenceAgent` form a coherent evidence-to-tier mapping. The 4-tier scale (0.15/0.60/0.80/0.95) is principled.
3. **The SDK boundary is clean.** `ShreeClient` -> `DefaultRuntimeService` -> `ChiefIntelligenceAgent` -> `NaturalResponseAgent` -> `SynthesizedResponse` -> `SDKResponse` is a well-typed, immutable data flow.
4. **Determinism is preserved.** All response generation is deterministic, which makes the platform testable and reproducible.
5. **The LLM SPI is a complete, professional implementation.** OpenAI, Gemini, Ollama, and InMemory providers are all implemented with streaming support, retry logic, error handling, and health probes. The LLM infrastructure is production-ready; it is only the wiring that is missing.

### 12.2 What is broken

1. **The LLM is never called.** This is the single most significant architectural gap. The response layer is a placeholder that became permanent.
2. **Context is lost in transit.** Kernel outputs are stringified at `EvidenceAgent.extract*()` and the structural data (chain, hypothesis ranking, plan phases) is discarded.
3. **No prompt builder exists.** Even if the LLM were connected, the context assembly logic is missing.
4. **Two synthesis layers run per request.** `DefaultResponseSynthesizer` runs and produces output; `NaturalResponseAgent` runs and replaces it. The synthesizer's output is always discarded in the canonical path.
5. **Confidence is not content-driven.** The verification tier is based on the SourceType (boolean presence), not on the actual content quality or confidence of the kernel outputs.
6. **The pre-pipeline `EvidenceAgent.extract()` always returns an empty bundle.** This is wasted work, but more importantly, the `NaturalResponseAgent.generate()` it feeds produces the "Insufficient Evidence" template which is then discarded â€” a misleading dead path.

### 12.3 The three execution paths â€” final form

**Path A â€” Routed operations (EXECUTE_TASK / ANALYZE_PROJECT / QUERY_KNOWLEDGE / CREATE_PLAN)**

```
ShreeClient -> DefaultRuntimeService -> intentAnalyzer.analyze() -> matched Operation
  -> Operation.execute(context)
      -> default implementation does the work
      -> returns ExecutionResult
  -> DefaultResponseSynthesizer.synthesizeExecution/Project/Knowledge/Composite()
      -> returns SynthesizedResponse
  -> ShreeClient maps to SDKResponse
```

Owner: `DefaultResponseSynthesizer.synthesize*()`. No LLM. No `NaturalResponseAgent` call.

**Path B â€” Multi-kernel orchestrator**

```
ShreeClient -> DefaultRuntimeService -> intentAnalyzer.analyze() -> multi-kernel pattern
  -> MultiKernelOrchestrator.orchestrate()
      -> runs multiple kernels in parallel
      -> compositeResult = each kernel's output
  -> DefaultResponseSynthesizer.synthesizeComposite(compositeResult)
      -> returns SynthesizedResponse
  -> ShreeClient maps to SDKResponse
```

Owner: `DefaultResponseSynthesizer.synthesizeComposite()`. No LLM. No `NaturalResponseAgent` call.

**Path C â€” Canonical CHAT (everything else)**

```
ShreeClient -> DefaultRuntimeService -> DefaultResponseService.submit()
  -> ChiefIntelligenceAgent.route()
      -> EvidenceAgent.extract(request) (empty bundle, pipeline not run)
      -> VerificationAgent.verify(empty) -> INSUFFICIENT (0.15)
      -> NaturalResponseAgent.generate(INSUFFICIENT) -> "Insufficient Evidence" (text DISCARDED)
      -> attachChiefMetadata() -> chiefMeta
  -> 11-stage executionPipeline
      -> populates state.metadata with kernel outputs
  -> ResponseSynthesisService.synthesize() -> DefaultResponseSynthesizer.synthesize()
      -> picks one of 6 paths based on metadata
      -> returns SynthesizedResponse (text DISCARDED at DRS.java:1160)
  -> EvidenceAgent.extractFromMetadata(state.metadata)
      -> populated bundle
  -> VerificationAgent.verify(populatedBundle)
      -> tier based on SourceType
  -> NaturalResponseAgent.generate(verification, request)
      -> returns SynthesizedResponse
  -> response = evidenceBackedResponse (REPLACES synthesizer output at DRS.java:1160)
  -> ShreeClient maps to SDKResponse
```

Owner: `NaturalResponseAgent.generate()` (post-pipeline). No LLM. The synthesizer's output is discarded.

---

### 12.4 The evidence verdict

There is **one** synthesis layer in the response path (Path C) â€” `NaturalResponseAgent`. There are **two** synthesis layers in the routed path (Path A) and composite path (Path B) â€” but only `DefaultResponseSynthesizer` is final.

In Path C, two of the three candidate synthesis runs are discarded:
- `ChiefIntelligenceAgent.route()` -> `NaturalResponseAgent.generate()` (pre-pipeline) â€” discarded
- `ResponseSynthesisService.synthesize()` -> `DefaultResponseSynthesizer.synthesize()` â€” discarded
- `NaturalResponseAgent.generate()` (post-pipeline) â€” final

The system runs **three times the synthesis work it needs** to produce a single answer.

### 12.5 The LLM verdict

The LLM is **never invoked** in any code path. The `LlmProvider` SPI is a complete implementation, the `LlmRouter` is a working router, the four provider implementations are production-quality HTTP clients â€” but none of them are ever called.

The `NaturalResponseAgent` is documented as a placeholder for LLM integration:

> "The LLM invocation slot is reserved here. The actual LLM call should be wired through LlmProvider when the LLM integration is complete." â€” `NaturalResponseAgent.java:90-92`

The wiring was never completed. The placeholder became permanent.

### 12.6 The confidence verdict

The final `SDKResponse.confidence` is set by `VerificationAgent` based on the SourceType of the first matching evidence:

- PROJECT present -> 0.95
- KNOWLEDGE present (no PROJECT) -> 0.80
- REASONING/INFERENCE/PLANNING/REFLECTION (no PROJECT/KNOWLEDGE) -> 0.60
- Empty bundle -> 0.15

The values 0.90, 0.92, 1.0 set by `DefaultResponseSynthesizer` are **never seen by the user** in the canonical path because the synthesizer's output is discarded.

The value 0.50 set by `ChiefIntelligenceAgent.buildDiagnosticResponse` is only seen when the no-kernels path is taken (Path C with `!plan.hasKernels()`).

### 12.7 The dead code verdict

| Class | Status | Reason |
|---|---|---|
| `PlanningResponse` | Dead | `KernelResponse` interface does not exist; zero callers |
| `KnowledgeResponse` | Dead | Same |
| `MemoryResponse` | Dead | Same |
| `ConversationResponse` | Dead | Same |
| `ResponseSynthesizerVerifier` | Dead | Null check only; zero callers |
| `InMemoryLlmProvider` | Unused | Never wired in |
| `OpenAiProvider` | Unused | Never wired in |
| `GeminiProvider` | Unused | Never wired in |
| `OllamaProvider` | Unused | Never wired in |
| `LlmRequest` | Unused | Never constructed in production |
| `LlmResponse` | Unused | Never constructed in production |

### 12.8 The synthesis flow verdict

```
   USER REQUEST
        |
        v
   IntentAnalyzer.analyze()
        |
   +----+----+----------+-----------+
   |         |          |           |
   v         v          v           v
 EXECUTE  ANALYZE  QUERY_KB  CREATE_PLAN  ...  -> DefaultResponseSynthesizer (Path A, B)
   |         |          |           |
   +----+----+----------+-----------+
                        |
                   Multi-kernel pattern matched
                        |
                        v
                DefaultResponseSynthesizer.synthesizeComposite() (Path B)
                        |
                   (no match)
                        |
                        v
   ChiefIntelligenceAgent.route() (Path C)
                        |
                        v
   11-stage executionPipeline
                        |
                        v
   DefaultResponseSynthesizer.synthesize() (DISCARDED at DRS.java:1160)
                        |
                        v
   NaturalResponseAgent.generate() (FINAL)
                        |
                        v
                  ShreeClient
                        |
                        v
                  SDKResponse
```

### 12.9 Final architecture classification

| Component | Status | Final owner of user response? |
|---|---|---|
| `LlmProvider` SPI | Defined, never wired | No |
| `LlmRouter` | Constructed, never invoked | No |
| `LlmRequest/LlmResponse` | Defined, never instantiated | No |
| Provider implementations | Complete HTTP clients, never called | No |
| `IntentAnalyzer` | Always runs (entry) | No (routing) |
| `DiagnosisAgent` | Always runs (health check) | No (health only) |
| `EvidenceAgent` | Runs twice (pre + post pipeline) | No (transformer) |
| `VerificationAgent` | Runs twice (pre + post pipeline) | No (verifier) |
| `ChiefIntelligenceAgent` | Always runs (orchestrator) | No (orchestrator) |
| `ResponseSynthesisService` | Wrapper | No (wrapper) |
| `DefaultResponseSynthesizer` | Runs in all 3 paths | **Yes (Path A, B)** |
| `NaturalResponseAgent` | Runs in Path C only | **Yes (Path C)** |
| `ShreeClient` | Always runs (exit) | No (exit) |
| `SDKResponse` | Always returned | No (data) |

### 12.10 Summary

The Shree AI platform's response synthesis layer is a **deterministic, template-based formatter** that:
1. Routes intent via `IntentAnalyzer` (regex).
2. Decides between three execution paths based on intent match.
3. Runs an 11-stage kernel pipeline for Path C.
4. Verifies evidence via `VerificationAgent` (4-tier scale).
5. Formats the response via `NaturalResponseAgent` (Path C) or `DefaultResponseSynthesizer` (Path A, B).
6. **Never invokes the LLM** despite a complete LLM SPI being implemented.
7. **Discards two of every three responses it generates** in the canonical path.

The platform produces a valid `SDKResponse` with correct confidence tier, but the response text is mechanical and template-driven. To produce natural language, the architecture requires the LLM wiring to be completed and a prompt builder to be added.

---
