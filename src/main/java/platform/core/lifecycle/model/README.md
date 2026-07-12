 # Lifecycle Domain Models

## Package
`platform.core.lifecycle.model`

## Purpose
Domain models defining the Platform language for kernel lifecycle management. These immutable value types represent kernel state, health, transitions, and transition results.

## Architectural Responsibility
- Defines the immutable domain objects used by the Lifecycle subsystem.
- Provides the type-safe language that the `platform.core.lifecycle.api` package uses for lifecycle operations.
- Ensures all lifecycle information is validated at construction time.

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

## Domain Models

### `KernelState` (Enum)
Represents the lifecycle state of a Kernel.

| State | Description |
|-------|-------------|
| `CREATED` | Kernel has been created but not initialized |
| `INITIALIZED` | Kernel has been initialized but not started |
| `RUNNING` | Kernel is running and operational |
| `SUSPENDED` | Kernel is suspended (state preserved for later resumption) |
| `STOPPED` | Kernel has been stopped |
| `FAILED` | Kernel has encountered an error |
| `TERMINATED` | Kernel has been permanently terminated |

### `KernelHealth`
Represents the runtime health status of a Kernel.

| Method | Returns | Description |
|--------|---------|-------------|
| `status()` | `String` | The health status |
| `message()` | `String` | The health message |
| `timestamp()` | `Instant` | The instant when the health was checked |
| `details()` | `Map<String, Object>` | Optional health details (unmodifiable) |

- Immutable by design (final class, final fields)
- Validates non-null and non-blank for status and message at construction
- Implements `equals()`, `hashCode()`, `toString()`

### `LifecycleTransition`
Represents one state transition of a Kernel.

| Method | Returns | Description |
|--------|---------|-------------|
| `kernelId()` | `KernelId` | The kernel identifier |
| `previousState()` | `KernelState` | The previous state |
| `currentState()` | `KernelState` | The current state |
| `timestamp()` | `Instant` | The instant when the transition occurred |

- Immutable by design (final class, final fields)
- Validates non-null for all fields at construction via `Objects.requireNonNull`
- Implements `equals()`, `hashCode()`, `toString()`

### `TransitionResult`
Represents the result of a lifecycle transition.

| Method | Returns | Description |
|--------|---------|-------------|
| `success()` | `boolean` | Whether the transition succeeded |
| `transition()` | `LifecycleTransition` | The lifecycle transition |
| `failureMessage()` | `String` | The failure message (null if successful) |

- Immutable by design (final class, final fields)
- Validates non-null for transition at construction
- Validates failure message is non-null/blank for failed transitions
- Implements `equals()`, `hashCode()`, `toString()`

## Design Constraints
- All models are **immutable** — no setters, no mutable state
- **No business logic** — models are pure data carriers
- **No Spring annotations** — framework-agnostic
- **No persistence** — no JPA, Hibernate, or database annotations
- **No Lombok** — all boilerplate is hand-written
- **No service logic** — pure domain models only

## Relationship to the API
```
platform.core.lifecycle.api.LifecycleService
                                    |
                                    | uses
                                    v
          platform.core.lifecycle.model.KernelState
          platform.core.lifecycle.model.KernelHealth
          platform.core.lifecycle.model.LifecycleTransition
          platform.core.lifecycle.model.TransitionResult
```

## Migration Note
The placeholder `KernelState` and `KernelHealth` classes from `platform.core.lifecycle.api` 
have been migrated to this package as proper domain models. The API package now imports
these models from the model package.

## Related Documents
- [EIO-301 — Lifecycle Public API](../api/README.md)
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [KERNEL-009 — Kernel Lifecycle Contract](../../../../../../docs/architecture/kernel/KERNEL-009-KERNEL-LIFECYCLE-CONTRACT.md)
- [KERNEL-010 — Kernel State Model](../../../../../../docs/architecture/kernel/KERNEL-010-KERNEL-STATE-MODEL.md)
- [KERNEL-011 — Kernel Lifecycle Events](../../../../../../docs/architecture/kernel/KERNEL-011-KERNEL-LIFECYCLE-EVENTS.md)
- [KERNEL-012 — Kernel Lifecycle Invariants](../../../../../../docs/architecture/kernel/KERNEL-012-KERNEL-LIFECYCLE-INVARIANTS.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)