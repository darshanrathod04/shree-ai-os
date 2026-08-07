# Lifecycle Validation

## Package
`platform.core.lifecycle.validator`

## Purpose
Validation layer for the Lifecycle Service that enforces all lifecycle rules before any state transition occurs.

The validator answers the question: *"Is this transition allowed?"*
It never answers: *"Perform the transition."*

## Architectural Responsibility
- Enforces all lifecycle rules before any state transition occurs.
- Answers the question: "Is this transition allowed?" — it never performs the transition.
- Returns structured validation results supporting multiple errors in a single execution.
- Reuses the approved Registry validation architecture.

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

### `LifecycleValidator`
Stateless validator for lifecycle transition readiness.

| Method | Returns | Description |
|--------|---------|-------------|
| `validateKernelId(KernelId)` | `ValidationResult` | Validates a kernel identifier |
| `validateState(KernelState)` | `ValidationResult` | Validates a kernel state |
| `validateHealth(KernelHealth)` | `ValidationResult` | Validates kernel health |
| `validateTransition(KernelState, KernelState)` | `ValidationResult` | Validates a state transition |
| `validateTransitionResult(TransitionResult)` | `ValidationResult` | Validates a transition result |

## Allowed Transitions

| From | To | Allowed |
|------|----|---------|
| `CREATED` | `INITIALIZED` | ✓ |
| `INITIALIZED` | `RUNNING` | ✓ |
| `RUNNING` | `SUSPENDED` | ✓ |
| `RUNNING` | `STOPPED` | ✓ |
| `RUNNING` | `FAILED` | ✓ |
| `SUSPENDED` | `RUNNING` | ✓ |
| `FAILED` | `TERMINATED` | ✓ |
| `STOPPED` | `TERMINATED` | ✓ |

## Rejected Transitions

| From | To | Reason |
|------|----|--------|
| `CREATED` | `RUNNING` | Must initialize first |
| `INITIALIZED` | `TERMINATED` | Must run first |
| `FAILED` | `RUNNING` | Must terminate first |
| `TERMINATED` | ANY | Terminal state |
| `STOPPED` | `RUNNING` | Must terminate first |

## Validation Rules

### `validateKernelId(KernelId)` performs:
| Rule | Type | Description |
|------|------|-------------|
| KernelId exists | Error | KernelId must not be null |
| KernelId format | Error | KernelId value must not be null or blank |

### `validateState(KernelState)` performs:
| Rule | Type | Description |
|------|------|-------------|
| State exists | Error | KernelState must not be null |

### `validateHealth(KernelHealth)` performs:
| Rule | Type | Description |
|------|------|-------------|
| Health status | Error | KernelHealth status must not be null or blank |
| Health message | Error | KernelHealth message must not be null or blank |

### `validateTransition(KernelState, KernelState)` performs:
| Rule | Type | Description |
|------|------|-------------|
| Transition allowed | Error | Previous state can transition to next state |

### `validateTransitionResult(TransitionResult)` performs:
| Rule | Type | Description |
|------|------|-------------|
| Transition exists | Error | TransitionResult transition must not be null |
| KernelId present | Error | TransitionResult kernelId must not be null |
| Previous state present | Error | TransitionResult previousState must not be null |
| Current state present | Error | TransitionResult currentState must not be null |
| Timestamp present | Error | TransitionResult timestamp must not be null |
| Failure message | Error | Failed TransitionResult must have non-null, non-blank failure message |

## Design Constraints
- **Stateless** — all state is passed as method parameters
- **Deterministic** — same inputs always produce the same result
- **Thread-safe** — no mutable state
- **Reuses ValidationResult** — uses existing `platform.core.registry.validator.ValidationResult`
- **No business logic** — validation rules only
- **No state mutation** — validation never changes state
- **Never performs transitions** — validation only

## Relationship to the Lifecycle API
```
platform.core.lifecycle.api.LifecycleService
                            |
                            | uses (via implementation)
                            v
          platform.core.lifecycle.validator.LifecycleValidator
                            |
                            | validates
                            v
          platform.core.lifecycle.model.TransitionResult
                            |
                            +--- KernelId
                            +--- KernelState
                            +--- KernelHealth
                            |
                            | returns
                            v
          platform.core.registry.validator.ValidationResult
```

## Engineering Principle
**Validation decides whether a transition is allowed. Validation never performs the transition.**

## Related Documents
- [EIO-301 — Lifecycle Public API](../api/README.md)
- [EIO-302 — Lifecycle Domain Models](../model/README.md)
- [EIO-103 — Kernel Registration Validation](../../registry/validator/README.md)
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [KERNEL-009 — Kernel Lifecycle Contract](../../../../../../docs/architecture/kernel/KERNEL-009-KERNEL-LIFECYCLE-CONTRACT.md)
- [KERNEL-010 — Kernel State Model](../../../../../../docs/architecture/kernel/KERNEL-010-KERNEL-STATE-MODEL.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)