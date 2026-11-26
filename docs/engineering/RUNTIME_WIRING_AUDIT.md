# Shree AI OS — Runtime Wiring & Logic Audit Report

> **Phase 1 Audit (Java / Spring Boot Runtime + Guardrails)**  
> **Date:** 2026-11-26  
> **Scope:** End-to-end verification of core execution flows in `src/main/java/com/shreeai/os/platform/` to identify why real-world runtime outputs are weak, disconnected, or non-workable.

---

## 1. Executive Diagnosis

The runtime has strong individual building blocks (11 pipeline stages, ONNX + `pgvector` support, `LlmRouter`, `ByokSettingsService`, `TenantIsolationEnforcer`), **but the end-to-end wiring between SDK entrypoints, kernel services, and LLM synthesis has 6 critical disconnects**:

1. **`ShreeAI.builder().apiKey(...)` is ignored by the runtime** (`ShreeBuilder.java`, `ShreeAI.java`).
2. **`chatStream(...)` completely bypasses the 11-stage pipeline, RAG, memory, and guardrails** (`DefaultRuntimeService.streamText`).
3. **`graphPermissionManager()` fails OPEN on unmapped `CapabilityType`s** (`DefaultRuntimeService.java:234-236`).
4. **`NaturalResponseAgent` hardcodes `"shree-default"` and drops into robotic template dumps / canned insufficient responses** (`NaturalResponseAgent.java`).
5. **Silent degradation in `OnnxEmbeddingProvider` and `DefaultRuntimeService`** hides missing models, failed retrieval, and configuration errors.
6. **Dependency & test isolation drift** (`shree-playground/pom.xml` version mismatch; `PlatformAdversarialChaosIntegrationTest` hitting live Gemini free-tier 429s).

---

## 2. Verified Flow-by-Flow Findings

### Finding 1 — `ShreeAI.builder().apiKey(...)` Does Not Wire Into `LlmRouter` (P0 Critical)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/sdk/ShreeBuilder.java:69-109`
  * `src/main/java/com/shreeai/os/platform/sdk/ShreeAI.java:45-66`
  * `src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java:330-396`
* **Root Cause:**
  * `ShreeBuilder.build()` puts `apiKey` into `SDKConfiguration`, then calls `createDefaultRuntime()` which constructs `DefaultRuntimeService(runtimeConfig, runtimeContract)` without passing `apiKey`.
  * `ShreeAI` constructs a fresh, empty `new ByokSettingsService()` and calls `client.syncByokSettings(byok)` — which does nothing because `byok` has no keys configured.
  * `DefaultRuntimeService.buildDefaultLlmRouter()` only reads `System.getenv(...)` / `System.getProperty(...)`.
* **Real-World Impact:** A Java developer following the Quickstart (`ShreeAI.builder().apiKey("AIza...").build()`) without setting env vars silently gets `in-memory` echo/fallback responses instead of real LLM output.
* **Phase 2 Fix:** Wire non-`"local"` `SDKConfiguration.apiKey()` (and provider/model options) directly into `ByokSettingsService` inside `ShreeAI` constructor before `syncByokSettings(byok)` so `rebuildLlmRouter()` activates the configured provider immediately.

---

### Finding 2 — `chatStream(...)` Bypasses the Entire Cognitive Pipeline & Guardrails (P0 Critical)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/sdk/ShreeClient.java:194-236`
  * `src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java:773-791`
* **Root Cause:**
  * `ShreeClient.chatStream(message, listener)` delegates directly to `runtime.streamText(message)`.
  * `DefaultRuntimeService.streamText(String message)` creates a bare `LlmRequest` (`model = "shree-streaming"`, `prompt = message`) and calls `llmRouter.stream(llmRequest)` directly.
  * **None** of the 11 pipeline stages (`IdentityStage`, `MemoryRecallStage`, `KnowledgeStage`, `ReasoningStage`, `ActionExecutionStage`, `ChiefReviewStage`) are executed!
* **Real-World Impact:** Streaming chat has zero RAG grounding, zero memory recall, zero tenant isolation, and zero prompt-injection/guardrail wrapping.
* **Phase 2 Fix:** Run the pipeline stages up to evidence/context preparation first, build the grounded `LlmRequest` (system guardrails + retrieved RAG/memory context + user query), and stream that prompt through `llmRouter.stream(...)` (while storing the completed turn into memory/reflection).

---

### Finding 3 — `graphPermissionManager()` Fails Open on Unmapped Capabilities (P0 Security)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java:229-263`
* **Root Cause:**
  * Lines 234-236:
    ```java
    ExecutionCapability execution = toExecutionCapability(capability);
    if (execution == null) {
        return PermissionDecision.ALLOW;
    }
    ```
  * `toExecutionCapability()` only maps `MEMORY`, `KNOWLEDGE`, `PLANNING`, and `EXECUTION`. Any other `CapabilityType` returns `null`, which is **explicitly allowed (`ALLOW`)** instead of denied (`DENY`).
* **Real-World Impact:** Violates the platform's core **Fail-Closed Security** promise for any new or unmapped capability type.
* **Phase 2 Fix:** Return `PermissionDecision.DENY` when `execution == null`, and explicitly map every valid `CapabilityType` in `toExecutionCapability()`.

---

### Finding 4 — `NaturalResponseAgent` Hardcoded Model & Unusable Fallback UX (P1 High)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/runtime/agents/NaturalResponseAgent.java:72, 126-131, 166-237, 257-281, 315-326`
  * `src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java:1583-1665`
* **Root Cause:**
  * `DEFAULT_LLM_MODEL = "shree-default"` is hardcoded instead of passing the configured model from `RuntimeConfiguration` / `ByokSettingsService`.
  * When `report.isInsufficient()` is true, `generate()` short-circuits at line 129 and never invokes the LLM — returning a canned insufficient template even for valid general queries.
  * When `LlmRouter` falls back to `InMemoryLlmProvider` (`" echoes: "`), `NaturalResponseAgent` renders raw internal debug sections (`# Title`, `**Question:**`, `## Citations`, `## Limitations`).
* **Real-World Impact:** Users receive robotic markdown dumps or "insufficient evidence" rejections on normal prompts instead of clean, helpful, properly grounded answers.
* **Phase 2 Fix:**
  * Distinguish **Strict Grounded Mode** (when RAG/project evidence is required or found) vs **General Conversational Mode** (when no KB match is needed, still enforcing safety guardrails).
  * Propagate configured model names properly; surface clear diagnostic errors when a real LLM provider fails instead of silently masking failures behind debug markdown.

---

### Finding 5 — Silent Degradation in Embeddings, Knowledge Retrieval & Error Handling (P1 High)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/runtime/embedding/OnnxEmbeddingProvider.java:58-89, 98-120`
  * `src/main/java/com/shreeai/os/platform/runtime/pipeline/stages/KnowledgeStage.java:148-159, 194-202, 239-241`
  * `src/main/java/com/shreeai/os/platform/runtime/service/DefaultRuntimeService.java:553-565, 1663-1665`
* **Root Cause:**
  * If ONNX model resources are missing or native library fails to load, `OnnxEmbeddingProvider` silently switches to `deterministicFallback(text)` (hash-based pseudo-vectors that have zero semantic meaning).
  * `KnowledgeStage` filters results by `KnowledgeRankingService.MIN_RELEVANCE_THRESHOLD` (`0.65`) and silently catches ingestion exceptions (`catch (Exception ignored)`).
  * `DefaultRuntimeService:1663` silently catches `evidenceError`.
* **Real-World Impact:** Developers think semantic RAG is running, when in reality it may be using hash vectors or silently dropping chunks — leading to poor/irrelevant search results with no clear warning.
* **Phase 2 Fix:**
  * Expose embedding mode (`ONNX_ACTIVE` vs `DEGRADED_FALLBACK`) in `DiagnosticsSDK` and startup health checks.
  * Verify `all-MiniLM-L6-v2` ONNX resource loading path in `src/main/resources` and add a health check test.
  * Replace silent `catch (Exception ignored)` blocks with structured diagnostic warnings/events on `RuntimeEventBus`.

---

### Finding 6 — Simulated Execution Branch & Sub-App / CI Drift (P1–P2)
* **Files & Lines:**
  * `src/main/java/com/shreeai/os/platform/runtime/pipeline/stages/ActionExecutionStage.java:87-110`
  * `application/shree-playground/pom.xml:11, 30, 35`
  * `src/test/java/com/shreeai/os/platform/verification/PlatformAdversarialChaosIntegrationTest.java`
* **Root Cause:**
  * `ActionExecutionStage` returns `"Execution completed (simulated)"` whenever `executionService == null`.
  * `shree-playground/pom.xml` uses Spring Boot `4.1.1`, `onnxruntime 1.17.1`, `tokenizers 0.28.0` vs root `4.0.2`, `1.29.0`, `0.36.0`.
  * `PlatformAdversarialChaosIntegrationTest` executes live Gemini API calls (causing 429 rate-limit failures and 128s+ test times).
* **Phase 2 Fix:**
  * Ensure `DefaultRuntimeService` always wires a real `ExecutionService` + fail-closed `PermissionManager`, and fail explicitly (or mark `SIMULATED` clearly in metadata) if null.
  * Align `application/` POM dependencies with root `pom.xml`.
  * Isolate live LLM tests under `@Tag("live")` so default `mvn test` is fast (`<20s`) and deterministic.

---

## 3. Recommended Phase 2 Remediation Order

| Step | Target | Goal |
|---|---|---|
| **2.1** | `ShreeBuilder`, `ShreeAI`, `ByokSettingsService` | Fix `apiKey()` + `SDKConfiguration` -> `LlmRouter` wiring |
| **2.2** | `DefaultRuntimeService.graphPermissionManager()` | Close fail-open hole for unmapped `CapabilityType`s |
| **2.3** | `DefaultRuntimeService.streamText()` + `ShreeClient.chatStream()` | Route streaming through pipeline context/RAG/guardrails |
| **2.4** | `NaturalResponseAgent` | Fix model propagation, conversational vs strict-RAG mode, and fallback clarity |
| **2.5** | `OnnxEmbeddingProvider` + `KnowledgeStage` | Verify real ONNX model loading & eliminate silent failure swallowing |
| **2.6** | `pom.xml` (sub-apps) + Test Isolation + Web Lint | Align versions, isolate live Gemini tests (`@Tag("live")`), fix `shree-platform-web` ESLint errors |
