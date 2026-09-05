# SPRINT20 PHASE 5 â€” PLATFORM FORENSIC AUDIT
## Response Synthesis Layer: Ground Truth

**Date:** Sprint 20, Phase 5
**Scope:** Response synthesis, confidence computation, LLM boundary, duplicate generation
**Files audited:** 31 Java source files across response, runtime, SDK, and LLM layers

---

## BOTTOM LINE

The response synthesis layer is a **deterministic, template-based formatter**. It never calls the LLM. It runs three candidate generators per canonical request and discards two. The LLM SPI is complete but unwired.

---

## KEY FINDINGS

### F1: LLM Never Called (CRITICAL)
The `LlmProvider` SPI is fully implemented with OpenAI, Gemini, Ollama, and InMemory providers. `LlmRouter` is a working router with fail-over logic. But no `LlmRequest` is ever constructed in production. No provider is ever invoked. The `NaturalResponseAgent` documents this at line 90-92: "The LLM invocation slot is reserved here."

### F2: Three Generators Run Per Request (HIGH)
Path C (canonical CHAT) runs three candidate synthesis paths per request:
1. `CIA.route()` -> `NRA.generate()` (pre-pipeline, empty bundle, text DISCARDED)
2. `DRS.synthesize()` -> `DefaultResponseSynthesizer.synthesize()` (text REPLACED at DRS.java:1160)
3. `DRS.submit()` -> `NRA.generate()` (post-pipeline, FINAL)

Two of three outputs are discarded. The synthesizer output is replaced by the evidence-based NRA output at DRS.java:1160.

### F3: Context Lost in Transit (HIGH)
Kernel outputs are stringified at `EvidenceAgent.extract*()`:
- `ReasoningResult` -> only `conclusion` string survives (chain/citations lost)
- `InferenceResult` -> only `topHypothesis` string survives (ranking/citations lost)
- `PlanBlueprint` -> only `planSummary` string survives (phases/milestones/risks lost)
- `KnowledgeNode` -> only label and description survive (semantic metadata lost)

### F4: Confidence Set by SourceType, Not Content (MEDIUM)
`VerificationAgent` assigns 0.95/0.80/0.60/0.15 based on which SourceType is present as a boolean flag, not on the actual kernel confidence values. The `confidenceHint` per `EvidenceItem` is written by `EvidenceAgent` but never read by `VerificationAgent`.

### F5: No Prompt Builder Exists (CRITICAL)
Even if the LLM were wired, there is no `PromptBuilder` class. `LlmRequest` is never constructed. No context assembly from kernel outputs exists.

### F6: Two Synthesis Layers Exist (MEDIUM)
`DefaultResponseSynthesizer` (template engine with 6 hardcoded paths) and `NaturalResponseAgent` (evidence-to-markdown formatter) both render the same semantic content (knowledge citations, reasoning conclusions, planning summaries). Only one survives per path.

### F7: Pre-Pipeline Bundle Always Empty (LOW)
`EvidenceAgent.extract(request, diagnostics)` at CIA.java:127 always produces an empty bundle because it reads from `request.getMetadata()` before the pipeline runs. The metadata keys it looks for (knowledgeResults, reasoningConclusion, etc.) are not present yet.

### F8: Dead Code â€” 11 Classes (LOW)
`PlanningResponse`, `KnowledgeResponse`, `MemoryResponse`, `ConversationResponse`, `ResponseSynthesizerVerifier`, all four LLM providers, `LlmRequest`, `LlmResponse` â€” complete implementations with zero production callers.

---

## CONFIDENCE VALUES

| Value | Source | Used where | Final? |
|---|---|---|---|
| 0.95 | `ConfidenceCalculator.VERIFIED_PROJECT_SCORE` | Project evidence present | Yes |
| 0.80 | `ConfidenceCalculator.VERIFIED_KB_SCORE` | Knowledge evidence present | Yes |
| 0.60 | `ConfidenceCalculator.INFERRED_SCORE` | Reasoning/inference present | Yes |
| 0.15 | `ConfidenceCalculator.INSUFFICIENT_SCORE` | Empty bundle | Yes |
| 0.50 | Hardcoded in `CIA.java:224` | No-kernel diagnostic path | Yes (for that path) |
| 0.90 | Hardcoded in `DRS.java:124` | Execution synthesis | Discarded |
| 0.92 | Hardcoded in `DRS.java:1035` | Planning blueprint | Discarded |
| 1.00 | Hardcoded in `DRS.java:721` | Chat greeting | Discarded |

User-seen values: 0.95, 0.80, 0.60, 0.15, 0.50. Not 0.90, 0.92, or 1.00 in the canonical path.

---

## THREE EXECUTION PATHS

### Path A â€” Routed Operations (EXECUTE_TASK, ANALYZE_PROJECT, QUERY_KNOWLEDGE, CREATE_PLAN)
Owner: `DefaultResponseSynthesizer.synthesize*()`. No LLM. No NRA.

### Path B â€” Multi-Kernel Orchestrator
Owner: `DefaultResponseSynthesizer.synthesizeComposite()`. No LLM. No NRA.

### Path C â€” Canonical CHAT (everything else, the common case)
Owner: `NaturalResponseAgent.generate()` (post-pipeline). No LLM. Synthesizer output discarded.

---

## ARCHITECTURE VERDICT

The response synthesis layer is a well-structured, type-safe, deterministic template engine. It is not broken â€” it is unfinished. The LLM integration was designed but never wired. The NaturalResponseAgent placeholder became permanent.

The platform works correctly within its current design: intent routing, kernel execution, evidence verification, and response formatting all function as specified. But the response text is mechanical and template-driven because the LLM is not in the loop.

To produce natural language, the platform needs:
1. LLM wiring to `NaturalResponseAgent.generate()` or a new prompt builder
2. A `PromptBuilder` class to assemble kernel outputs into an LLM prompt
3. Context preservation (pass structured objects, not strings)
