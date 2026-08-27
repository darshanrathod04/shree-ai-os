# Kernel ID Contract Discovery Report
**Engineering Order:** EO-V1-G2-003  
**Phase:** 1 — Contract Discovery  
**Date:** 2026-07-29  

---

## Executive Summary

Repository-first investigation completed. The canonical KernelId contract has been identified from the existing validation implementation.

### Key Finding

**The registry validation contract is: `^[a-zA-Z0-9-]+$`**

This regex pattern allows ONLY:
- Alphanumeric characters (a-z, A-Z, 0-9)
- Hyphens (-)

This pattern explicitly **PROHIBITS**:
- Periods (.)
- Underscores (_)
- Special characters
- Whitespace

---

## Contract Source

### Validation Implementation
**File:** `src/main/java/com/shreeai/os/platform/core/registry/validator/KernelRegistrationValidator.java`

**Line 43:**
```java
private static final Pattern KERNEL_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
```

**Validation Logic (Lines 76-83):**
```java
String idValue = kernelId.value();
if (idValue == null || idValue.isBlank()) {
    builder.addError("KernelId value must not be null or blank");
} else if (!KERNEL_ID_PATTERN.matcher(idValue).matches()) {
    builder.addError("KernelId format is invalid: '" + idValue
            + "'. Must contain only alphanumeric characters and hyphens");
}
```

---

## Canonical KernelId Specification

### Allowed Characters
- **Letters:** a-z, A-Z
- **Digits:** 0-9
- **Hyphens:** -

### Prohibited Characters
- **Periods:** . (dots)
- **Underscores:** _
- **Special characters:** @, #, $, %, etc.
- **Whitespace:** spaces, tabs, etc.

### Naming Convention
- **Format:** `[a-zA-Z0-9-]+`
- **Length:** No explicit limit (validated by KernelId constructor for null/blank only)
- **Case Sensitivity:** Case-sensitive (preserves original case)
- **Reserved Prefixes:** None defined

### Required Format
```
^[a-zA-Z0-9-]+$
```

**Explanation:**
- `^` - Start of string
- `[a-zA-Z0-9-]+` - One or more alphanumeric characters or hyphens
- `$` - End of string

---

## KernelId Model Analysis

### File: `src/main/java/com/shreeai/os/platform/core/registry/model/KernelId.java`

**Constructor Validation (Lines 36-41):**
```java
public KernelId(String id) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("KernelId must not be null or blank");
    }
    this.id = id;
}
```

**Findings:**
- ✅ KernelId constructor does NOT enforce format validation
- ✅ Format validation is delegated to KernelRegistrationValidator
- ✅ Registry is the single source of truth for format validation
- ✅ Separation of concerns: model accepts, validator enforces

---

## Registry Contract Analysis

### File: `src/main/java/com/shreeai/os/platform/core/registry/service/DefaultKernelRegistry.java`

**Registration Flow (Lines 116-143):**
```java
public boolean register(String kernelId, RegisteredKernel entry) {
    // Step 1: Validate the kernel
    ValidationResult validationResult = validator.validate(entry);
    if (!validationResult.isValid()) {
        String errorMessage = String.join("; ", validationResult.errors());
        throw new InvalidKernelException(errorMessage);
    }
    
    // Step 2: Check for duplicate
    // Step 3: Store the kernel
    // Step 4: Return success
}
```

**Findings:**
- ✅ Registry enforces validation before registration
- ✅ InvalidKernelException thrown for validation failures
- ✅ Registry does not bypass or relax validation
- ✅ Registry is the source of truth for registration rules

---

## Bootstrap Registration Analysis

### File: `src/main/java/com/shreeai/os/platform/bootstrap/PlatformBootstrap.java`

**Current Kernel IDs (Violations):**
```java
registerKernel("Identity", "kernel.identity", ...);      // ❌ VIOLATION (dot)
registerKernel("Memory", "kernel.memory", ...);          // ❌ VIOLATION (dot)
registerKernel("Context", "kernel.context", ...);        // ❌ VIOLATION (dot)
registerKernel("Knowledge", "kernel.knowledge", ...);    // ❌ VIOLATION (dot)
registerKernel("Cognitive", "kernel.cognitive", ...);    // ❌ VIOLATION (dot)
registerKernel("Planning", "kernel.planning", ...);      // ❌ VIOLATION (dot)
registerKernel("Execution", "kernel.execution", ...);    // ❌ VIOLATION (dot)
registerKernel("MultiAgent", "kernel.multiagent", ...);  // ❌ VIOLATION (dot)
registerKernel("Chief", "kernel.chief", ...);            // ❌ VIOLATION (dot)
```

**Problem:** All kernel IDs use dots (periods) as separators, which violates the registry contract.

---

## Repository Evidence Summary

### Validation Contract
- **Source:** KernelRegistrationValidator.java
- **Pattern:** `^[a-zA-Z0-9-]+$`
- **Error Message:** "Must contain only alphanumeric characters and hyphens"
- **Enforcement:** Strict (no exceptions)

### Current Violations
- **Total Kernels:** 9
- **Violating Kernels:** 9 (100%)
- **Violation Type:** Dots (periods) in kernel IDs
- **Example:** `kernel.identity` → should be `kernel-identity`

### Correct Format Examples
- ✅ `kernel-identity`
- ✅ `kernel-memory`
- ✅ `kernel-context`
- ✅ `kernel-knowledge`
- ✅ `kernel-cognitive`
- ✅ `kernel-planning`
- ✅ `kernel-execution`
- ✅ `kernel-multiagent`
- ✅ `kernel-chief`

---

## Engineering Order Compliance

### Repository-First Rule
✅ **COMPLIED**
- Inspected KernelId implementation
- Inspected DefaultKernelRegistry
- Inspected validation implementation
- Identified existing validation pattern from repository
- Did not infer or invent naming rules

### No Registry Redesign
✅ **COMPLIED**
- Registry validation remains unchanged
- No regex modification
- No constraint relaxation
- Registry is source of truth

### Contract Alignment Approach
✅ **COMPLIED**
- Producer (bootstrap) will be aligned to existing contract
- Registry contract remains unchanged
- Validation logic remains unchanged
- Only kernel registration code will be modified

---

## Risk Assessment

**Risk Level:** LOW

**Reasoning:**
- Clear contract identified from repository
- Simple string replacement (dots → hyphens)
- No architectural changes required
- No validation changes required
- Minimal scope: only kernel ID strings in bootstrap

---

## Next Steps

1. **Phase 2:** Create kernel registration audit table
2. **Phase 3:** Update kernel IDs in PlatformBootstrap.java
3. **Phase 4:** Verify kernel registration succeeds
4. **Phase 5:** Re-run Engineering Gate 2 verification

---

**Discovery Complete:** 2026-07-29  
**Recommendation:** Proceed with Phase 2 — Kernel Registration Audit