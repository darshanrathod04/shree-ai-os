# Lifecycle Error Architecture

## Package
`platform.core.lifecycle.error`

## Purpose
Standardized error model for the Lifecycle Service that provides a consistent error architecture following the Platform pattern.

## Architectural Responsibility
- Defines the standard error model used by all Lifecycle services.
- Provides a consistent error architecture following the Platform pattern.
- Ensures all lifecycle errors are consistent, typed, and documented.

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

## Error Model

### `LifecycleErrorCode`
Standardized error codes for lifecycle operations.

| Code | Description |
|------|-------------|
| `LIFECYCLE_INVALID_TRANSITION` | A state transition violates the Lifecycle State Model |
| `LIFECYCLE_KERNEL_NOT_INITIALIZED` | Execution was attempted before kernel initialization |
| `LIFECYCLE_KERNEL_ALREADY_RUNNING` | Attempted to start an already running kernel |
| `LIFECYCLE_KERNEL_ALREADY_STOPPED` | Attempted to stop an already stopped kernel |
| `LIFECYCLE_KERNEL_ALREADY_SUSPENDED` | Attempted to suspend an already suspended kernel |
| `LIFECYCLE_KERNEL_TERMINATED` | Attempted to operate on a terminated kernel |
| `LIFECYCLE_VALIDATION_FAILED` | Lifecycle validation failed |

### `LifecycleError`
Immutable error description containing code, message, timestamp, and optional details.

| Method | Returns | Description |
|--------|---------|-------------|
| `code()` | `LifecycleErrorCode` | The error code |
| `message()` | `String` | The human-readable error message |
| `timestamp()` | `Instant` | The instant when the error occurred |
| `details()` | `Map<String, Object>` | Optional error details (unmodifiable) |

### `LifecycleException`
Base exception for all Lifecycle Service errors. Extends `RuntimeException`.

| Method | Returns | Description |
|--------|---------|-------------|
| `error()` | `LifecycleError` | The associated lifecycle error |
| `code()` | `LifecycleErrorCode` | The error code |
| `getMessage()` | `String` | The error message |
| `timestamp()` | `Instant` | The error timestamp |
| `details()` | `Map<String, Object>` | The error details |

**All future lifecycle exceptions SHALL extend this class.**

### Exception Hierarchy

| Exception | Extends | Error Code | Description |
|-----------|---------|------------|-------------|
| `InvalidTransitionException` | `LifecycleException` | `LIFECYCLE_INVALID_TRANSITION` | Thrown when a state transition violates the Lifecycle State Model |
| `KernelNotInitializedException` | `LifecycleException` | `LIFECYCLE_KERNEL_NOT_INITIALIZED` | Thrown when execution is attempted before initialization |
| `KernelAlreadyRunningException` | `LifecycleException` | `LIFECYCLE_KERNEL_ALREADY_RUNNING` | Thrown when attempting to start an already running kernel |

## Design Constraints
- **LifecycleException is the ONLY base exception** — all future exceptions extend it
- **Immutable where applicable** — LifecycleError is immutable
- **No business logic** — error definitions only
- **No Spring annotations** — framework-agnostic
- **No persistence** — no database or serialization annotations

## Relationship to Other Error Architectures
```
platform.core.registry.error                    platform.core.discovery.error                    platform.core.lifecycle.error
├── RegistryErrorCode                     ↔    ├── DiscoveryErrorCode                     ↔    ├── LifecycleErrorCode
├── RegistryError                         ↔    ├── DiscoveryError                         ↔    ├── LifecycleError
├── RegistryException                     ↔    ├── DiscoveryException                     ↔    ├── LifecycleException
├── DuplicateKernelException              ↔    ├── CapabilityNotFoundException            ↔    ├── InvalidTransitionException
├── KernelNotFoundException               ↔    ├── ContractNotFoundException              ↔    ├── KernelNotInitializedException
└── InvalidKernelException                ↔    └── InvalidDiscoveryRequestException       ↔    └── KernelAlreadyRunningException
```

## Usage Example
```java
// Throwing an exception
throw new InvalidTransitionException(KernelState.RUNNING, KernelState.CREATED);

// Catching and handling
try {
    lifecycleService.start(kernelId);
} catch (InvalidTransitionException e) {
    System.err.println("Invalid transition: " + e.code());
    System.err.println("Error at: " + e.timestamp());
}
```

## Related Documents
- [EIO-301 — Lifecycle Public API](../api/README.md)
- [EIO-302 — Lifecycle Domain Models](../model/README.md)
- [EIO-303 — Lifecycle Validation](../validator/README.md)
- [EIO-104 — Registry Error Architecture](../../registry/error/README.md)
- [EIO-205 — Discovery Error Architecture](../../discovery/error/README.md)
- [KERNEL-008 — Kernel Lifecycle Philosophy](../../../../../../docs/architecture/kernel/KERNEL-008-KERNEL-LIFECYCLE-PHILOSOPHY.md)
- [KERNEL-009 — Kernel Lifecycle Contract](../../../../../../docs/architecture/kernel/KERNEL-009-KERNEL-LIFECYCLE-CONTRACT.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)