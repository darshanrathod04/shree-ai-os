# Duplicate Kernel Registration Investigation
**Engineering Order:** EO-V1-G2-003 (Post-Completion Analysis)  
**Type:** READ-ONLY Investigation  
**Date:** 2026-08-04  

---

## Executive Summary

Investigation completed. The duplicate kernel registration is caused by **test infrastructure design**, not a production code defect. The root cause is the combination of a singleton PlatformServiceLocator with multiple test methods each calling bootstrap.start().

---

## Investigation Findings

### 1. PlatformServiceLocator Singleton Pattern

**File:** `src/main/java/com/shreeai/os/platform/bootstrap/integration/PlatformServiceLocator.java`

**Evidence:**
```java
public class PlatformServiceLocator {
    private static PlatformServiceLocator instance;
    
    private PlatformServiceLocator() {
        // Initialization code
    }
    
    public static synchronized PlatformServiceLocator getInstance() {
        if (instance == null) {
            instance = new PlatformServiceLocator();
        }
        return instance;
    }
}
```

**Finding:** ✅ **YES, PlatformServiceLocator is a singleton**

**Implications:**
- Only one instance exists per JVM
- All services (including KernelRegistry) are created once
- State persists across test executions
- No reset between tests

---

### 2. KernelRegistry Sharing Across Test Executions

**File:** `src/main/java/com/shreeai/os/platform/bootstrap/integration/PlatformServiceLocator.java`

**Evidence:**
```java
private final KernelRegistry<?> kernelRegistry;

private PlatformServiceLocator() {
    // ...
    this.kernelRegistry = new DefaultKernelRegistry(registryValidator);
    // ...
}
```

**Finding:** ✅ **YES, KernelRegistry is shared across test executions**

**Reasoning:**
- PlatformServiceLocator is a singleton
- KernelRegistry is created once in PlatformServiceLocator constructor
- KernelRegistry is never reset or cleared
- All tests share the same KernelRegistry instance
- Kernels registered in test #1 persist for test #2, #3, etc.

---

### 3. PlatformBootstrap.start() Call Count

**File:** `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java`

**Evidence - Test Methods:**

1. **phase1_BootstrapVerification()** (line 100)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

2. **phase2_RuntimeVerification()** (line 160-161)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

3. **phase3_KernelVerification()** (line 234-235)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

4. **phase4_PlatformServiceVerification()** (line 312)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

5. **phase5_ShutdownVerification()** (line 460)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

6. **phase6_FailureRecoveryVerification()** (line 526)
   ```java
   bootstrap = PlatformBootstrap.create();
   bootstrapReport = bootstrap.start();
   ```

**Finding:** ✅ **PlatformBootstrap.start() is called 6 times**

**Details:**
- Each test method calls bootstrap.start() once
- Total: 6 test methods × 1 call each = 6 calls
- Each call attempts to register all 9 kernels
- First call succeeds
- Second call fails with DuplicateKernelException

---

### 4. Spring Auto-Startup Check

**File:** `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java`

**Evidence:**
```java
@SpringBootTest
class EngineeringGate2RuntimeVerification {
    // ...
}
```

**Finding:** ⚠️ **Spring Boot test context is loaded, but no automatic bootstrap**

**Reasoning:**
- `@SpringBootTest` loads Spring context
- No `@PostConstruct` or `@EventListener` in PlatformBootstrap
- No Spring-managed PlatformBootstrap bean
- Bootstrap is NOT automatically started by Spring
- All bootstrap is manual via test code

---

### 5. Manual Bootstrap in Tests

**File:** `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java`

**Evidence:**
Every test method manually creates and starts bootstrap:

```java
// Pattern in all 6 test methods:
bootstrap = PlatformBootstrap.create();
bootstrapReport = bootstrap.start();
```

**Finding:** ✅ **YES, bootstrap is manually started in every test**

**Details:**
- No automatic startup
- Each test explicitly calls bootstrap.start()
- No cleanup between tests
- No reset of PlatformServiceLocator

---

## Call Graph: Duplicate Kernel Registration

### First Test Execution (phase1_BootstrapVerification)

```
1. @BeforeEach setUp()
   ↓
2. phase1_BootstrapVerification()
   ↓
3. PlatformBootstrap.create()
   ↓
4. bootstrap.start()
   ↓
5. initializeKernels()
   ↓
6. registerKernel("Identity", "kernel-identity", ...)
   ↓
7. registry.register("kernel-identity", ...)
   ↓
8. ✅ SUCCESS - Kernel registered
```

**Result:** All 9 kernels registered successfully in shared KernelRegistry

---

### Second Test Execution (phase2_RuntimeVerification)

```
1. @BeforeEach setUp()
   ↓
2. phase2_RuntimeVerification()
   ↓
3. PlatformBootstrap.create()
   ↓
4. bootstrap.start()
   ↓
5. initializeKernels()
   ↓
6. registerKernel("Identity", "kernel-identity", ...)
   ↓
7. registry.register("kernel-identity", ...)
   ↓
8. ❌ FAILURE - DuplicateKernelException: "kernel-identity" already registered
```

**Result:** DuplicateKernelException because KernelRegistry still contains "kernel-identity" from test #1

---

## Root Cause Analysis

### Primary Cause
**PlatformServiceLocator singleton pattern with persistent state**

### Contributing Factors
1. **Singleton PlatformServiceLocator** - One instance per JVM
2. **Persistent KernelRegistry** - Created once, never cleared
3. **Multiple test methods** - 6 tests each calling bootstrap.start()
4. **No test isolation** - No reset between tests
5. **No cleanup** - Kernels remain registered after each test

### Exact Failure Sequence

**Test #1 (phase1_BootstrapVerification):**
- PlatformServiceLocator.getInstance() → creates new instance
- bootstrap.start() → registers 9 kernels
- Test completes
- PlatformServiceLocator instance retained in static field
- KernelRegistry retains all 9 kernels

**Test #2 (phase2_RuntimeVerification):**
- PlatformServiceLocator.getInstance() → returns existing instance
- bootstrap.start() → attempts to register 9 kernels
- registerKernel("Identity", "kernel-identity", ...) → ❌ DuplicateKernelException
- Test fails

---

## Evidence Summary

### Singleton Pattern Evidence
**Location:** PlatformServiceLocator.java, lines 27-28, 47-49
```java
private static PlatformServiceLocator instance;

public static synchronized PlatformServiceLocator getInstance() {
    if (instance == null) {
        instance = new PlatformServiceLocator();
    }
    return instance;
}
```

### Shared Registry Evidence
**Location:** PlatformServiceLocator.java, line 35
```java
private final KernelRegistry<?> kernelRegistry;
```
Created once in constructor, never reset.

### Multiple Bootstrap Calls Evidence
**Location:** EngineeringGate2RuntimeVerification.java
- Line 100: phase1_BootstrapVerification
- Line 161: phase2_RuntimeVerification
- Line 235: phase3_KernelVerification
- Line 312: phase4_PlatformServiceVerification
- Line 460: phase5_ShutdownVerification
- Line 526: phase6_FailureRecoveryVerification

### Duplicate Registration Error Evidence
**From test output:**
```
Caused by: com.shreeai.os.platform.core.registry.error.DuplicateKernelException: 
Kernel with id 'kernel-identity' is already registered
```

---

## Conclusion

### Root Cause
**Test infrastructure design issue, not a production code defect**

The duplicate kernel registration occurs because:
1. PlatformServiceLocator is a singleton (by design)
2. KernelRegistry persists across test executions (by design)
3. Each test method calls bootstrap.start() (test design)
4. No cleanup between tests (test design)

### Production Code Status
✅ **Production code is correct**
- Kernel ID contract alignment is successful
- Kernel registration works correctly
- No InvalidKernelException
- Bootstrap reaches READY state

### Test Infrastructure Issue
⚠️ **Test infrastructure needs improvement**
- PlatformServiceLocator has a `reset()` method but it's not used
- Tests need to call PlatformServiceLocator.reset() in @BeforeEach
- Or use separate test configurations

### Exact Source of Duplicate Registration
**Second call to bootstrap.start() in phase2_RuntimeVerification (line 161)**

The duplicate originates from:
- Test #1 (phase1) registers kernels successfully
- Test #2 (phase2) attempts to register same kernels
- KernelRegistry still contains kernels from test #1
- DuplicateKernelException thrown

---

## Recommendations

### For Test Infrastructure (Separate Engineering Order)
1. Call `PlatformServiceLocator.reset()` in @BeforeEach
2. Or create test-specific PlatformServiceLocator
3. Or use @DirtiesContext for test isolation
4. Or implement test cleanup in @AfterEach

### For Production Code
✅ **No changes required**
- Production code is working correctly
- Singleton pattern is appropriate for production
- Kernel registration is functioning as designed

---

**Investigation Complete:** 2026-08-04  
**Root Cause:** Test infrastructure (singleton state persistence)  
**Production Code Status:** ✅ Correct  
**Test Infrastructure Status:** ⚠️ Needs improvement