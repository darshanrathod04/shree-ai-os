# Sprint 18: Autonomous Intelligence Layer — SPEC

## 1. Goal

Replace the current direct SDK-to-kernel dispatch in `ShreeAI.chat()` with a unified, multi-agent pipeline:

```
App → ShreeAI.chat()
     → ChiefIntelligenceAgent        (routes request, builds ExecutionPlan)
         → DiagnosisAgent             (checks workspace, memory, knowledge, project state)
         → EvidenceAgent              (extracts structured facts from kernel outputs)
         → VerificationAgent          (verifies evidence quality → confidence tier)
         → NaturalResponseAgent       (LLM generates natural text from verified evidence)
     → SynthesizedResponse
```

**Key principle:** Every kernel produces structured evidence (facts), not markdown. The LLM is invoked **only once** in `NaturalResponseAgent`, strictly for natural language generation. No kernel ever calls the LLM.

---

## 2. Confidence Tier System (ConfidenceCalculator)

| Tier              | Score | Condition                                            |
|-------------------|-------|------------------------------------------------------|
| `VERIFIED_PROJECT`| 0.95  | Project kernel verified the answer from actual code  |
| `VERIFIED_KB`     | 0.80  | Knowledge kernel verified the answer from graph       |
| `INFERRED`        | 0.60  | Reasoning/inference produced a plausible hypothesis  |
| `INSUFFICIENT`    | 0.15  | No evidence or diagnosis failed                     |

---

## 3. New Package Structure

```
src/main/java/com/shreeai/os/platform/runtime/
├── agents/
│   ├── ChiefIntelligenceAgent.java   — routes all requests; builds ExecutionPlan
│   ├── DiagnosisAgent.java           — workspace/memory/knowledge/project health
│   ├── EvidenceAgent.java            — converts kernel outputs → EvidenceBundle
│   ├── VerificationAgent.java        — validates evidence → VerificationReport
│   └── NaturalResponseAgent.java     — LLM synthesis from verified evidence
├── model/
│   ├── ExecutionPlan.java            — execution strategy produced by Chief
│   ├── EvidenceBundle.java           — structured facts from kernel outputs
│   ├── DiagnosticReport.java         — diagnosis output
│   ├── VerificationReport.java       — verification output with confidence tier
│   └── AgentDecision.java            — per-agent decision with rationale
└── confidence/
    └── ConfidenceCalculator.java     — 4-tier confidence computation
```

---

## 4. File Deliverables

### 4.1 Model Classes (5 files)

| File                    | Responsibility                                                              |
|-------------------------|-----------------------------------------------------------------------------|
| `ExecutionPlan.java`    | Immutable plan: requested kernels, execution order, skip-reason (if any)    |
| `EvidenceBundle.java`   | Immutable bundle: List<EvidenceItem>, source kernel, extraction timestamp    |
| `DiagnosticReport.java` | Diagnosis output: per-check status, actionable recommendations               |
| `VerificationReport.java` | Verification output: per-evidence status, confidence tier, citations      |
| `AgentDecision.java`    | Per-agent decision: action taken, rationale, confidence, metadata            |

### 4.2 ConfidenceCalculator (1 file)

`ConfidenceCalculator.java` — static utility with:
- `fromProjectEvidence()` → 0.95
- `fromKnowledgeEvidence()` → 0.80
- `fromReasoningEvidence()` → 0.60
- `fromInsufficient()` → 0.15
- `fromEvidenceBundle(EvidenceBundle)` — highest tier from bundle items

### 4.3 Agent Classes (5 files)

| Agent                  | Method                              | Returns                           |
|------------------------|-------------------------------------|-----------------------------------|
| `ChiefIntelligenceAgent` | `route(ExecutionRequest)`            | `ExecutionPlan` + `AgentDecision` |
| `DiagnosisAgent`        | `analyze(ExecutionPlan, context)`   | `DiagnosticReport`                |
| `EvidenceAgent`         | `extract(pipelineState)`            | `EvidenceBundle`                 |
| `VerificationAgent`     | `verify(EvidenceBundle)`            | `VerificationReport`              |
| `NaturalResponseAgent`  | `generate(VerificationReport)`      | `SynthesizedResponse`             |

### 4.4 Wiring Changes (2 files)

| File                           | Change                                                           |
|--------------------------------|------------------------------------------------------------------|
| `DefaultRuntimeService.java`    | Add `ChiefIntelligenceAgent` field; wire into `handleRequest()`   |
| `DefaultResponseSynthesizer.java` | Add `synthesizeFromEvidence(EvidenceBundle, confidence)` method |

---

## 5. ChiefIntelligenceAgent Routing Logic

```
Input: ExecutionRequest
Output: ExecutionPlan + AgentDecision

1. Extract user input from request
2. Run IntentAnalyzer.analyze(input) → IntentAnalysisResult
3. Determine required kernels from IntentAnalysisResult.requiredKernels()
4. Check workspace state (project path, analysis cache)
5. Check memory/knowledge availability
6. Build ExecutionPlan with ordered kernels + skip-reasons
7. Return ExecutionPlan + AgentDecision

Pre-execution gate:
  if (plan.hasBlockedKernels() && plan.allBlocked())
    → short-circuit with DiagnosticReport (no pipeline execution)
  else
    → proceed to pipeline
```

---

## 6. DiagnosisAgent Checks

```
1. PROJECT_CHECK — is project path available? has it been analyzed?
2. MEMORY_CHECK  — is memory accessible? any recent relevant entries?
3. KNOWLEDGE_CHECK — is knowledge graph accessible? any relevant nodes?
4. EXECUTION_CHECK — is execution environment available?
```

---

## 7. EvidenceAgent Extraction

Each kernel stage writes structured output to `PipelineExecutionState.metadata`. `EvidenceAgent` reads these:

| Metadata Key           | Evidence Type      | Extracted Fact                          |
|------------------------|--------------------|-----------------------------------------|
| `knowledgeResults`      | KNOWLEDGE          | KnowledgeNode.label + description       |
| `reasoningResult`       | REASONING          | ReasoningResult.conclusion               |
| `inferenceResult`       | INFERENCE          | InferenceResult.topHypothesis           |
| `planningResult`       | PLANNING           | PlanBlueprint.summary                   |
| `memoryResults`        | MEMORY             | Memory.content summary                  |
| `reflectionResult`     | REFLECTION         | ReflectionIntelligenceEngine.outcome     |
| `projectSummary`       | PROJECT            | ProjectSummary (from ProjectIntelligence kernel) |
| `executionResult`      | EXECUTION          | ExecutionResult                         |

---

## 8. VerificationAgent Validation

```
For each EvidenceItem in EvidenceBundle:
  1. Check source integrity (kernel produced it, not fabricated)
  2. Check freshness (timestamp within threshold)
  3. Check completeness (has required fields)
  4. Assign per-item verification status: VERIFIED / UNVERIFIED / FAILED
  5. Assign overall tier from ConfidenceCalculator

Build VerificationReport with:
  - per-item status list
  - overall confidence tier + score
  - list of citations (for VERIFIED_KB tier)
  - list of gaps (missing evidence for INSUFFICIENT)
```

---

## 9. NaturalResponseAgent Synthesis

```
Input: VerificationReport + original request
Output: SynthesizedResponse (calls LLM exactly once)

1. Build structured payload from VerificationReport
2. Call LLM with structured evidence → natural language
3. Wrap in SynthesizedResponse with:
   - confidence = VerificationReport.confidence()
   - style = ResponseStyle.PROFESSIONAL
   - structured payload = EvidenceBundle summary
```

---

## 10. Backward Compatibility

- `ShreeAI.chat()` signature is **unchanged** — existing apps work without modification
- `KnowledgeSDK`, `ProjectSDK`, `PlanningSDK` are **unchanged** — backward compatible
- Existing 11-stage pipeline is **unchanged** — EvidenceAgent reads from it
- Existing `IntentAnalyzer`, `MultiKernelOrchestrator` are **reused** (not rewritten)
- `DefaultResponseSynthesizer` gets a **new overload** — no existing behavior changed

---

## 11. Acceptance Tests

| # | Test                          | Criteria                                                    |
|---|-------------------------------|-------------------------------------------------------------|
| 1 | Chief routes to correct kernels | IntentAnalyzer + routing produces correct ExecutionPlan     |
| 2 | Diagnosis catches missing workspace | DiagnosticReport shows failed checks              |
| 3 | Evidence extracts from pipeline state | All 8 metadata keys → EvidenceBundle items |
| 4 | Verification assigns correct tier | project→0.95, knowledge→0.80, reasoning→0.60, none→0.15 |
| 5 | Confidence tiers match spec     | All 4 tiers return correct scores                            |
| 6 | Natural response calls LLM once | Single LLM invocation per request                           |
| 7 | Pipeline unchanged             | Existing 11-stage pipeline still runs                      |
| 8 | No regression                  | All 1,139 existing tests pass                               |

---

## 12. Non-Goals (Not in Sprint 18)

- No changes to SDK public API (`ShreeAI`, `KnowledgeSDK`, `ProjectSDK`, `PlanningSDK`)
- No changes to existing kernel engines
- No changes to the 11-stage pipeline
- No changes to `DefaultRuntimeService` lifecycle or kernel construction
- No standalone LLM calls from individual kernel engines
