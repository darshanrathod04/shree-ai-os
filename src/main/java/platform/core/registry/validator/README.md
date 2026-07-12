# Kernel Registration Validation

## Package
`platform.core.registry.validator`

## Purpose
Validation layer for the Kernel Registry that ensures every Kernel satisfies the architectural requirements before registration.

The validator answers the question: *"Can this Kernel be registered?"*
It never answers: *"Register the Kernel."*

## Architectural Responsibility
- Ensures every Kernel satisfies the architectural requirements before registration.
- Returns structured validation results supporting multiple errors in a single execution.
- Enforces the architectural invariants defined in KERNEL-007.
- Remains independent of the registry implementation.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-005 — Kernel Registration
- KERNEL-007 — Kernel Invariants
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services

## Public Contracts

### `ValidationResult`
Structured result returned by the validator.

| Method | Returns | Description |
|--------|---------|-------------|
| `isValid()` | `boolean` | `true` if there are zero errors |
| `errors()` | `List<String>` | Unmodifiable list of error messages |
| `warnings()` | `List<String>` | Unmodifiable list of warning messages |
| `builder()` | `Builder` | Creates a new builder instance |

### `KernelRegistrationValidator`
Stateless validator for kernel registration readiness.

| Method | Returns | Description |
|--------|---------|-------------|
| `validate(RegisteredKernel)` | `ValidationResult` | Validates a kernel for registration readiness |
| `validateNoDuplicate(KernelId, Collection<KernelId>)` | `ValidationResult` | Validates that a KernelId is not already registered |

## Validation Rules

### `validate(RegisteredKernel)` performs:
| Rule | Type | Description |
|------|------|-------------|
| KernelId exists | Error | KernelId must not be null |
| KernelId format | Error | Must match pattern `^[a-zA-Z0-9-]+$` |
| KernelVersion exists | Error | KernelVersion must not be null |
| KernelMetadata exists | Error | KernelMetadata must not be null |
| Kernel name not blank | Error | Name must not be null or blank |
| Description not blank | Error | Description must not be null or blank |
| Category exists | Error | Category must not be null or blank |
| Metadata consistency | Warning | Name and category should not be identical |

### `validateNoDuplicate(KernelId, Collection<KernelId>)` performs:
| Rule | Type | Description |
|------|------|-------------|
| Duplicate KernelId | Error | KernelId must not already be registered |

## Design Constraints
- **Stateless** — all state is passed as method parameters
- **Deterministic** — same inputs always produce the same result
- **No business logic** — validation rules only
- **No model mutation** — models are never modified
- **No exceptions for expected failures** — uses structured `ValidationResult`
- **No Spring annotations** — framework-agnostic
- **No persistence**

## Relationship to the Registry
```
platform.core.registry.api.KernelRegistry<RegisteredKernel>
                            |
                            | uses
                            v
          platform.core.registry.validator.KernelRegistrationValidator
                            |
                            | validates
                            v
          platform.core.registry.model.RegisteredKernel
                            |
                            +--- KernelId
                            +--- KernelVersion
                            +--- KernelMetadata
                            |
                            | returns
                            v
          platform.core.registry.validator.ValidationResult
```

## Related Documents
- [EIO-101 — Kernel Registry Public API](../api/README.md)
- [EIO-102 — Kernel Registry Domain Models](../model/README.md)
- [KERNEL-005 — Kernel Registration](../../../../../../docs/architecture/kernel/KERNEL-005-KERNEL-REGISTRATION.md)
- [KERNEL-007 — Kernel Invariants](../../../../../../docs/architecture/kernel/KERNEL-007-KERNEL-INVARIANTS.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)