# Pipeline Canonicalization Note

**Sprint:** V1-P1-003
**Date:** 2026-07-23
**Authority:** Chief Engineering Order EO-V1-P1-003

---

## Chief Decision

Per Chief Engineering Order EO-V1-P1-003, Task 5:

---

## Canonical Pipeline

**Package:** `com.shreeai.os.platform.runtime.pipeline`

**Interface:** `ExecutionPipeline` (runtime.pipeline)

**Implementation:** `DefaultExecutionPipeline`

**File:** `src/main/java/com/shreeai/os/platform/runtime/pipeline/DefaultExecutionPipeline.java`

**Maturity:** Production-ready (145 lines of code)

**Status:** ✅ CANONICAL — Use this implementation

**Characteristics:**
- Stage ordering by priority
- Duplicate priority validation
- Execution state management
- Shadow mode support
- Short-circuit execution
- Timing measurement
- Immutable PipelineResult
- Stage chain execution

---

## Legacy Pipeline

**Package:** `com.shreeai.os.platform.runtime.internal`

**Interface:** `ExecutionPipeline` (runtime.execution — different interface)

**Implementation:** `DefaultExecutionPipeline`

**File:** `src/main/java/com/shreeai/os/platform/runtime/internal/DefaultExecutionPipeline.java`

**Maturity:** Skeleton/placeholder (44 lines of code)

**Status:** ⚠️ LEGACY — Do not use for new development

**Characteristics:**
- No stage execution
- No ordering or validation
- No state management
- Placeholder implementation only
- States "not yet implemented" in documentation

---

## Current Usage

### Canonical Pipeline Usage
- Used by: Runtime framework
- Dependencies: PipelineContext, PipelineResult, PipelineState, ExecutionStage
- Ready for: Production use

### Legacy Pipeline Usage
- Used by: None (skeleton placeholder)
- Dependencies: Different interface (runtime.execution.ExecutionPipeline)
- Ready for: Deprecation

---

## Recommendation

### Immediate Actions (No Code Changes)

1. **Documentation Only:**
   - This note serves as the official documentation of the canonicalization decision
   - No code modifications made per EO constraints

2. **Bootstrap Reference:**
   - PlatformBootstrap references the canonical pipeline via `PlatformServiceLocator`
   - No direct reference to legacy pipeline in bootstrap

### Future Actions (When Permitted)

1. **Deprecate Legacy Implementation:**
   - Add `@Deprecated` annotation to `runtime.internal.DefaultExecutionPipeline`
   - Add deprecation notice in Javadoc pointing to canonical implementation

2. **Consolidate Interfaces:**
   - Review `runtime.execution.ExecutionPipeline` for deprecation
   - Migrate any users to `runtime.pipeline.ExecutionPipeline`

3. **Remove Legacy Implementation:**
   - Remove `runtime.internal.DefaultExecutionPipeline`
   - Remove `runtime.execution.ExecutionPipeline` (if safe to do so)
   - Remove `runtime.execution.ExecutionRequest`
   - Remove `runtime.execution.ExecutionResult`
   - Remove `runtime.execution.ExecutionContext`
   - Clean up all references

### Risk Assessment

| Factor | Assessment |
|--------|------------|
| Likelihood of confusion | HIGH — two similarly named classes in different packages |
| Impact on bootstrap | LOW — bootstrap does not use legacy pipeline |
| Impact on developers | MEDIUM — unclear which implementation to extend |
| Migration effort | LOW — legacy is only 44 lines, skeleton only |

---

## Summary

| Aspect | Canonical | Legacy |
|--------|-----------|--------|
| Package | `runtime.pipeline` | `runtime.internal` |
| Interface | `runtime.pipeline.ExecutionPipeline` | `runtime.execution.ExecutionPipeline` |
| Implementation | `DefaultExecutionPipeline` | `DefaultExecutionPipeline` |
| Lines of code | 145 | 44 |
| Maturity | Production-ready | Skeleton |
| Usage | Active | None |
| Status | ✅ CANONICAL | ⚠️ LEGACY |

---

*No code modifications performed. This is a documentation-only decision per EO constraints.*

**Decision Date:** 2026-07-23
**Sprint:** V1-P1-003
**Authority:** Chief Engineering Order