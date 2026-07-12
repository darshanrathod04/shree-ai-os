# Lifecycle State Transition Engine

## Package
`platform.core.lifecycle.engine`

## Purpose
The Transition Engine is the ONLY component responsible for executing Lifecycle State Transitions within Shree AI OS. No other component shall directly change Kernel lifecycle state.

## Architectural Responsibility
- Executes validated lifecycle state transitions.
- Creates `LifecycleTransition` and `TransitionResult` records.
- Never mutates external state — returns results for the caller to apply.
- Remains completely stateless — all state is passed as parameters.

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

### `LifecycleTransitionEngine`
Stateless engine for executing lifecycle state transitions.

| Method | Returns | Description |
|--------|---------|-------------|
| `transition(KernelId, KernelState, KernelState)` | `TransitionResult` | Executes a validated state transition and produces a result |

## Execution Flow

```
transition(kernelId, currentState, targetState)
  ↓
LifecycleValidator.validateTransition(currentState, targetState)
  ↓
Create LifecycleTransition(kernelId, currentState, targetState, Instant.now())
  ↓
Create TransitionResult(success = true/false, transition, failureMessage?)
  ↓
Return TransitionResult
```

## Transition Outcomes

| Validation Result | TransitionResult |
|-------------------|-----------------|
| Valid (allowed) | `TransitionResult(success = true)` |
| Invalid (rejected) | `TransitionResult(success = false, failureMessage)` |

## Design Constraints
- **Stateless** — no internal state, no maps, no caches, no persistence
- **Thread-safe** — no mutable state
- **Never mutates external state** — only creates results for callers to apply
- **Never accesses Registry** — isolated from other components
- **Never accesses Discovery** — isolated from other components
- **Never accesses Service** — isolated from other components
- **No event publishing** — not part of this implementation
- **No scheduling** — not part of this implementation
- **No Spring annotations** — framework-agnostic

## Dependency Model
```
LifecycleTransitionEngine
            |
            | uses
            v
LifecycleValidator
```

## Principle of Separation
```
LifecycleValidator          decides legality.
LifecycleTransitionEngine   executes transitions.
LifecycleService            coordinates orchestration.

These responsibilities SHALL remain independent forever.
```

## Relationship to the Platform
```
platform.core.lifecycle.service.DefaultLifecycleService
                            |
                            | delegates
                            v
          platform.core.lifecycle.engine.LifecycleTransitionEngine
                            |
                            | validates via
                            v
          platform.core.lifecycle.validator.LifecycleValidator
                            |
                            | produces
                            v
          platform.core.lifecycle.model.TransitionResult
                            |
                            +--- LifecycleTransition
                            +--- success flag
                            +--- failure message
```

## Related Documents
- [EIO-301 — Lifecycle Public API](../api/README.md)
- [EIO-302 — Lifecycle Domain Models](../model/README.md)
- [EIO-303 — Lifecycle Validation](../validator/README.md)
- [EIO-304 — Lifecycle Error Architecture](../error/README.md)
- [EIO-305 — Lifecycle Service Default Implementation](../service/README.md)
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [ADD-PLT-208 — Lifecycle Architecture](../../../../../../docs/architecture/platform/ADD-PLT-208-LIFECYCLE-ARCHITECTURE.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)