# Default Lifecycle Service

## Package
`platform.core.lifecycle.service`

## Purpose
Reference implementation of the Lifecycle Service that provides lifecycle orchestration for kernels within Shree AI OS.

The service coordinates lifecycle operations by delegating validation to LifecycleValidator. It never owns lifecycle transition rules.

## Architectural Responsibility
- Provides the reference implementation of lifecycle orchestration.
- Coordinates lifecycle operations by delegating validation to LifecycleValidator.
- Maintains current lifecycle state for registered kernels.
- Never owns lifecycle transition rules — validation is delegated.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-008 — Kernel Lifecycle Philosophy
- KERNEL-009 — Kernel Lifecycle Contract
- KERNEL-010 — Kernel State Model
- KERNEL-011 — Kernel Lifecycle Events
- KERNEL-012 — Kernel Lifecycle Invariants
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `DefaultLifecycleService`
Reference implementation of `LifecycleService`.

| Method | Description |
|--------|-------------|
| `initialize(KernelId)` | Initializes a kernel for operation |
| `start(KernelId)` | Starts a kernel |
| `stop(KernelId)` | Stops a kernel |
| `suspend(KernelId)` | Suspends a kernel |
| `resume(KernelId)` | Resumes a suspended kernel |
| `state(KernelId)` | Returns the current state of a kernel |
| `health(KernelId)` | Returns the current health of a kernel |

## Implementation Details

### Constructor Injection
```java
public DefaultLifecycleService(KernelRegistry kernelRegistry, LifecycleValidator validator)
```

### Operation Flow
```
initialize/start/stop/suspend/resume()
  ↓
Validate KernelId (LifecycleValidator.validateKernelId)
  ↓
Read current state from ConcurrentHashMap
  ↓
Check idempotency (already in target state → return true)
  ↓
Delegate transition legality to LifecycleValidator.validateTransition()
  ↓
Update state in ConcurrentHashMap
  ↓
Return result
```

### Internal Storage
| Map | Type | Purpose |
|-----|------|---------|
| `states` | `ConcurrentHashMap<KernelId, KernelState>` | Current lifecycle state for registered kernels |
| `healthStates` | `ConcurrentHashMap<KernelId, KernelHealth>` | Runtime health for registered kernels |

### Storage Constraints
- **No other storage** — only the two ConcurrentHashMaps above
- No persistence — state is in-memory only
- No cache — state is the authoritative record
- No event publishing — event integration is handled by the implementation layer

## Design Constraints
- **Constructor injection only** — no setter injection
- **Thread-safe** — uses `ConcurrentHashMap` for state storage, no `synchronized` blocks
- **Never contains transition rules** — delegates to `LifecycleValidator`
- **Never bypasses validator** — all requests validated before transition
- **Never bypasses error architecture** — uses `LifecycleException` hierarchy only
- **No `RuntimeException` thrown directly** — always uses `LifecycleException` or `IllegalArgumentException`
- **No event publishing** — not part of this implementation
- **No scheduling** — not part of this implementation
- **No persistence** — in-memory only

## Idempotency Guarantees

| Operation | Already In State | Returns |
|-----------|------------------|---------|
| `initialize()` | INITIALIZED or RUNNING | `true` |
| `start()` | RUNNING | `true` |
| `stop()` | STOPPED | `true` |
| `suspend()` | SUSPENDED | `true` |
| `resume()` | RUNNING | `true` |

## Lifecycle Principle
**LifecycleService coordinates. LifecycleValidator validates. Transition Engine decides transitions. Responsibilities SHALL remain separated.**

## Relationship to the Platform
```
platform.core.lifecycle.api.LifecycleService
                            ^
                            |
                            | implements
                            |
platform.core.lifecycle.service.DefaultLifecycleService
                            |
                            | validates
                            +--- platform.core.lifecycle.validator.LifecycleValidator
                            |
                            | manages state
                            +--- status in memory
                            |
                            | queries registry
                            +--- platform.core.registry.api.KernelRegistry
                            |
                            | errors via
                            +--- platform.core.lifecycle.error.LifecycleException
```

## Related Documents
- [EIO-301 — Lifecycle Public API](../api/README.md)
- [EIO-302 — Lifecycle Domain Models](../model/README.md)
- [EIO-303 — Lifecycle Validation](../validator/README.md)
- [EIO-304 — Lifecycle Error Architecture](../error/README.md)
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [ADD-PLT-208 — Lifecycle Architecture](../../../../../../docs/architecture/platform/ADD-PLT-208-LIFECYCLE-ARCHITECTURE.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)