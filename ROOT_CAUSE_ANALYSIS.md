# ROOT_CAUSE_ANALYSIS

**Task:** EO-V1-REL1-BUGFIX-001 — Runtime Pipeline Test Restoration
**Date:** 2026-08-08

---

## 1. Introduction

This document analyzes the root cause of the `RuntimePipelineTest` failures that occurred after the repository cleanup ("Repository cleanup & Release Freeze", commit `ceb4d06`).

## 2. Investigation Summary

The investigation followed these steps:
1. Ran `mvn clean test` to reproduce the failures
2. Identified `RuntimePipelineTest` as the failing suite (7 failures)
3. Examined the test expectations vs. the implementation
4. Verified the canonical V1 implementation via `PIPELINE_CANONICALIZATION_NOTE.md`
5. Compared the committed implementation against the working-directory (uncommitted) changes
6. Isolated the root causes in the runtime pipeline execution state tracking

## 3. Root Cause 1: `freeze()` Status Precedence Bug

### Symptom
Tests expected `COMPLETED`/success for normal pipeline completion but received failure/`TERMINATED`:
- `testDefaultExecutionPipeline_SingleStage` — `expected: <true> but was: <false>`
- `testDefaultExecutionPipeline_MultipleStages` — `expected: <true> but was: <false>`
- `testPipelineExecutionState_Freeze` — `expected: <true> but was: <false>`
- `testPipelineExecutionState_StateIsolation`, `testPipelineExecutionState_ThreadSafety`, `testPipelineExecutionState_Timing`, `testPipelinePerformance` — all `expected: <true> but was: <false>`

### Analysis
`DefaultExecutionChain.next()` marks the state as `terminated` when it exhausts the stage list during **normal** progression:

```java
if (currentIndex >= stages.size()) {
    state.markTerminated();  // <-- normal completion path
    return PipelineResult.builder().success(true).status("COMPLETED")...build();
}
```

However, `PipelineExecutionState.freeze()` checked `terminated` **before** checking whether all stages were visited:

```java
} else if (terminated) {
    status = "TERMINATED";
    success = false;          // <-- masked normal completion
} else if (visitedStages.size() >= stages.size()) {
    status = "COMPLETED";
    success = true;
}
```

This caused normal completions (which mark `terminated` via the chain) to be reported as `TERMINATED`/failure.

### Fix
Reordered the precedence in `freeze()` so that **normal completion** (`visitedStages.size() >= stages.size()`) is checked **before** the `terminated` flag:

```java
} else if (visitedStages.size() >= stages.size()) {
    // All stages visited - normal completion.
    status = "COMPLETED";
    success = true;
} else if (terminated) {
    status = "TERMINATED";
    success = false;
}
```

This preserves the V1 lifecycle semantics: `terminated` remains a valid state for genuinely interrupted executions, but normal full-stage traversal is correctly reported as `COMPLETED`.

## 4. Root Cause 2: Shared `nextStageInvoked` Boolean Clobbered During Recursion

### Symptom
Short-circuit and completion tracking failed:
- `testDefaultExecutionChain_StageShortCircuit` — `expected: <1> but was: <0>` (Stage1 not marked completed)
- `testPipelineExecutionState_VisitedAndCompletedStages` — `expected: <2> but was: <0>`

### Analysis
The V1 model uses **recursive chain traversal**. `DefaultExecutionChain.next()` invokes a stage, passing a `nextChain`; the stage calls `nextChain.next()` to continue. The runtime detects short-circuit by inspecting a single shared boolean `nextStageInvoked` on `PipelineExecutionState`.

**The bug:** With a single shared boolean, a deeper frame's `markStageStarted()` (which resets the flag) clobbers the outer frame's state.

**Trace of `testDefaultExecutionChain_StageShortCircuit`** (stages: Stage1, ShortCircuitStage(Stage2), Stage3):
- **Frame 0** (index 0): `markStageStarted("Stage1")` resets flag → Stage1.process calls `markNextStageInvoked()` (flag=true) → `nextChain.next()` → recursion
- **Frame 1** (index 1): `markStageStarted("Stage2")` **resets flag to false** → ShortCircuitStage doesn't call next → Frame 1 inspects `wasNextStageInvoked()` = false → `markShortCircuit()` ✓
- **Back in Frame 0**: `wasNextStageInvoked()` = **false** (was reset by Frame 1!) → Stage1 is **never marked completed** → `completedStages` is empty → FAIL

The single boolean cannot track nesting depth; it is fundamentally unable to represent per-frame state during recursion.

### Fix
Replaced the single boolean with a **per-frame flag stack** (`Deque<Boolean>`) in `PipelineExecutionState`:
- `markStageStarted()` pushes a fresh frame
- `markNextStageInvoked()` sets the current (top) frame's flag
- `wasNextStageInvoked()` reads the current (top) frame's flag
- `popStageFrame()` (called by the chain after inspection) pops the frame, restoring the caller's flag

`DefaultExecutionChain.next()` now calls `state.popStageFrame()` after inspecting the flag, so each frame's state is correctly isolated and the caller's flag is restored.

This preserves the V1 recursive chain architecture and the public API — it only fixes the internal state-tracking mechanism.

## 5. Why the Runtime Pipeline Is the Canonical V1 Implementation

Per `PIPELINE_CANONICALIZATION_NOTE.md`:
- **Canonical:** `com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline` (production-ready, 145 lines)
- **Legacy:** `com.shreeai.os.platform.runtime.internal.DefaultExecutionPipeline` (skeleton, 44 lines)

The `DefaultExecutionPipeline` in `runtime.pipeline` is verified as the active V1 implementation. The runtime pipeline tests (`RuntimePipelineIntegrationTest`, `RuntimeBuilderTest`, `ExecutionContractTest`) all pass with the fix, confirming no integration regression.

## 6. Root Cause 3 (Pre-existing, Out of Scope): Persisted Runtime State Pollution

### Symptom (pre-existing)
Unrelated integration tests fail due to persisted runtime state:
- `AutonomousPlanningTests` / `ExecutionAuditTests`: `expected: <Become Java Backend Developer> but was: <Get Internship>`
- `ConversationContinuityTests`: `expected: <CONTINUE> but was: <FOLLOW_UP>`
- `ChiefOfStaffTests`: `testSurvivesRestart` fails on state-dependent data

### Analysis
These `@SpringBootTest` integration tests load persisted runtime state files (`goals.json`, `conversation_state.json`, `execution_plans.json`, `chief_of_staff.json`, etc.) from the working directory. `GoalManager.createGoal()` returns early when an existing incomplete goal exists (`BLOCKED - already working on: Get Internship`), blocking new plan generation. Conversation continuity resumes stale conversation state.

### Verification
These failures were **present in the very first `mvn clean test` run** before any changes were made in this task. They are **not related to the runtime pipeline** and are outside the scope of the pipeline test restoration.

### Conclusion
Documented here for completeness. These require a separate investigation into test-state isolation for the cognitive/planning integration tests, and are explicitly out of scope for EO-V1-REL1-BUGFIX-001 (runtime pipeline restoration).

---

## 7. Summary of Root Causes

| # | Root Cause | Impact | Fix | In Scope |
|---|-----------|--------|-----|----------|
| 1 | `freeze()` reported normal completion as `TERMINATED` | 7 pipeline tests | Reordered status precedence | ✅ Yes |
| 2 | Shared `nextStageInvoked` boolean clobbered during recursion | 2 pipeline tests | Per-frame flag stack | ✅ Yes |
| 3 | Persisted runtime state pollutes `@SpringBootTest` context | 7 non-pipeline tests | Separate state-isolation work | ❌ No (pre-existing) |

---

*Root cause analysis generated as part of EO-V1-REL1-BUGFIX-001.*