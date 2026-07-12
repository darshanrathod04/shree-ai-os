# Lifecycle Public API

## Package
`platform.core.lifecycle.api`

## Purpose
The Lifecycle API defines the Platform contract for Kernel lifecycle management within Shree AI OS.

This API specifies WHAT the Platform can do. Future services define HOW the Platform does it.

## Architectural Responsibility
- Defines the official Platform contract for kernel lifecycle operations.
- Specifies WHAT the Platform can do — future services define HOW.
- Ensures lifecycle management is independent of implementation details.
- Provides a stable API for kernel state and health queries.

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

### `LifecycleService`
The official Platform contract for kernel lifecycle management.

| Method | Returns | Description |
|--------|---------|-------------|
| `initialize(KernelId)` | `boolean` | Initializes a kernel for operation |
| `start(KernelId)` | `boolean` | Starts a kernel |
| `stop(KernelId)` | `boolean` | Stops a kernel |
| `suspend(KernelId)` | `boolean` | Suspends a kernel |
| `resume(KernelId)` | `boolean` | Resumes a suspended kernel |
| `state(KernelId)` | `KernelState` | Returns the current state of a kernel |
| `health(KernelId)` | `KernelHealth` | Returns the current health of a kernel |

## Lifecycle States

The Lifecycle API manages kernels through the following states:

| State | Description |
|-------|-------------|
| `UNINITIALIZED` | Kernel has not been initialized |
| `INITIALIZED` | Kernel has been initialized but not started |
| `RUNNING` | Kernel is running and operational |
| `STOPPED` | Kernel has been stopped |
| `SUSPENDED` | Kernel is suspended (state preserved) |
| `FAILED` | Kernel has encountered an error |

## Lifecycle Operations

### initialize(KernelId)
Prepares the kernel for execution by performing one-time setup operations.

**Idempotent:** Calling it multiple times on an already initialized kernel returns `true` without side effects.

**Prerequisites:** None

**Postconditions:** Kernel is in `INITIALIZED` state

### start(KernelId)
Transitions the kernel from its current state to the `RUNNING` state.

**Idempotent:** Calling it on an already running kernel returns `true` without side effects.

**Prerequisites:** Kernel must be `INITIALIZED`

**Postconditions:** Kernel is in `RUNNING` state

### stop(KernelId)
Transitions the kernel from its current state to the `STOPPED` state.

**Idempotent:** Calling it on an already stopped kernel returns `true` without side effects.

**Prerequisites:** Kernel must be `RUNNING`

**Postconditions:** Kernel is in `STOPPED` state

### suspend(KernelId)
Transitions the kernel from its current state to the `SUSPENDED` state.

**Idempotent:** Calling it on an already suspended kernel returns `true` without side effects.

**Prerequisites:** Kernel must be `RUNNING`

**Postconditions:** Kernel is in `SUSPENDED` state (state preserved)

### resume(KernelId)
Transitions the kernel from the `SUSPENDED` state to the `RUNNING` state.

**Idempotent:** Calling it on a running kernel returns `true` without side effects.

**Prerequisites:** Kernel must be `SUSPENDED`

**Postconditions:** Kernel is in `RUNNING` state (state preserved from before suspension)

### state(KernelId)
Returns the current state of a kernel.

**Returns:** Immutable `KernelState` object

**Note:** `KernelState` is defined in EIO-302.

### health(KernelId)
Returns the current health status of a kernel.

**Returns:** Immutable `KernelHealth` object

**Note:** `KernelHealth` is defined in EIO-302.

## Design Constraints
- **Public interface only** — no implementation classes
- **No business logic** — interface defines only the contract
- **No Spring annotations** — framework-agnostic
- **No persistence** — state management is handled by the implementation
- **No exceptions** — exception types are defined by the implementation
- **No models** — `KernelState` and `KernelHealth` are defined in EIO-302

## Lifecycle Principle
**The Lifecycle API defines WHAT the Platform can do. Future services define HOW the Platform does it.**

## Relationship to the Registry
```
platform.core.lifecycle.api.LifecycleService
                            |
                            | uses
                            v
          platform.core.registry.api.KernelRegistry
                            |
                            | reads
                            v
              RegisteredKernel metadata
```

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Models** — `KernelState` and `KernelHealth` are defined in EIO-302
- **Implementation** — no implementation classes
- **Exceptions** — exception types are defined by the implementation
- **Validation** — validation logic belongs in the implementation layer
- **Tests** — testing is handled by the implementation
- **Lifecycle Manager** — not part of this API
- **Lifecycle Engine** — not part of this API
- **Lifecycle Repository** — not part of this API
- **Lifecycle Storage** — not part of this API
- **Lifecycle Validator** — not part of this API

## Related Documents
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [KERNEL-009 — Kernel Lifecycle Contract](../../../../../../docs/architecture/kernel/KERNEL-009-KERNEL-LIFECYCLE-CONTRACT.md)
- [KERNEL-010 — Kernel State Model](../../../../../../docs/architecture/kernel/KERNEL-010-KERNEL-STATE-MODEL.md)
- [KERNEL-011 — Kernel Lifecycle Events](../../../../../../docs/architecture/kernel/KERNEL-011-KERNEL-LIFECYCLE-EVENTS.md)
- [KERNEL-012 — Kernel Lifecycle Invariants](../../../../../../docs/architecture/kernel/KERNEL-012-KERNEL-LIFECYCLE-INVARIANTS.md)
- [ADD-PLT-208 — Lifecycle Architecture](../../../../../../docs/architecture/platform/ADD-PLT-208-LIFECYCLE-ARCHITECTURE.md)
- [ADD-PLT-209 — Lifecycle Services](../../../../../../docs/architecture/platform/ADD-PLT-209-LIFECYCLE-SERVICES.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)