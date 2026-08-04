# Registry Error Architecture

## Package
`platform.core.registry.error`

## Purpose
Standardized error model for the Kernel Registry. Provides a consistent, typed error handling architecture that supports future Platform Core Services without redesign.

## Architectural Responsibility
- Provides a standardized error model for the Kernel Registry.
- Defines error codes, structured error descriptions, and a base exception hierarchy.
- Supports future Platform Core Services without redesign.
- Ensures all registry errors are consistent, typed, and documented.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-005 — Kernel Registration
- KERNEL-007 — Kernel Invariants
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services

## Public Contracts

### `RegistryErrorCode`
Enumeration of standardized registry error codes.

| Code | Description |
|------|-------------|
| `REGISTRY_DUPLICATE_KERNEL` | A kernel with the same identifier is already registered |
| `REGISTRY_INVALID_KERNEL` | The kernel data is structurally invalid or malformed |
| `REGISTRY_KERNEL_NOT_FOUND` | The requested kernel was not found in the registry |
| `REGISTRY_INVALID_VERSION` | The kernel version is invalid or incompatible |
| `REGISTRY_VALIDATION_FAILED` | Kernel validation failed — does not satisfy registration prerequisites |

### `RegistryError`
Immutable error description.

| Method | Returns | Description |
|--------|---------|-------------|
| `code()` | `RegistryErrorCode` | The error code |
| `message()` | `String` | Human-readable error message |
| `timestamp()` | `Instant` | When the error occurred |
| `details()` | `Map<String, Object>` | Optional error details |

### `RegistryException`
Base exception for all registry errors. Extends `RuntimeException`.

| Method | Returns | Description |
|--------|---------|-------------|
| `error()` | `RegistryError` | The associated registry error |
| `code()` | `RegistryErrorCode` | The error code |
| `message()` | `String` | The error message |
| `timestamp()` | `Instant` | When the error occurred |
| `details()` | `Map<String, Object>` | Optional error details |

### Exception Hierarchy

```
RegistryException (base)
├── DuplicateKernelException
├── KernelNotFoundException
└── InvalidKernelException
```

**Future exceptions SHALL extend `RegistryException`.**

### `DuplicateKernelException`
Thrown when attempting to register a kernel with an identifier that is already registered.

| Constructor | Description |
|-------------|-------------|
| `DuplicateKernelException(String kernelId)` | With kernel identifier |
| `DuplicateKernelException(String kernelId, String message)` | With identifier and custom message |
| `DuplicateKernelException(String kernelId, Throwable cause)` | With identifier and cause |
| `DuplicateKernelException(String kernelId, String message, Throwable cause)` | With identifier, message, and cause |

### `KernelNotFoundException`
Thrown when a requested kernel is not found in the registry.

| Constructor | Description |
|-------------|-------------|
| `KernelNotFoundException(String kernelId)` | With kernel identifier |
| `KernelNotFoundException(String kernelId, String message)` | With identifier and custom message |
| `KernelNotFoundException(String kernelId, Throwable cause)` | With identifier and cause |
| `KernelNotFoundException(String kernelId, String message, Throwable cause)` | With identifier, message, and cause |

### `InvalidKernelException`
Thrown when a kernel fails validation during registration.

| Constructor | Description |
|-------------|-------------|
| `InvalidKernelException(String message)` | With message |
| `InvalidKernelException(String message, String details)` | With message and details |
| `InvalidKernelException(String message, Throwable cause)` | With message and cause |
| `InvalidKernelException(String message, String details, Throwable cause)` | With message, details, and cause |

## Design Constraints
- **RegistryException is the ONLY base exception** — all future exceptions extend it
- **Immutable where applicable** — `RegistryError` is immutable
- **No business logic** — error definitions only
- **No Spring annotations** — framework-agnostic
- **No persistence**

## Relationship to the Registry
```
platform.core.registry.api.KernelRegistry<RegisteredKernel>
                            |
                            | throws
                            v
          platform.core.registry.error.RegistryException
                            |
                            +--- DuplicateKernelException
                            +--- KernelNotFoundException
                            +--- InvalidKernelException
                            |
                            | contains
                            v
          platform.core.registry.error.RegistryError
                            |
                            +--- RegistryErrorCode
                            +--- message
                            +--- timestamp
                            +--- details
```

## Related Documents
- [EIO-101 — Kernel Registry Public API](../api/README.md)
- [EIO-102 — Kernel Registry Domain Models](../model/README.md)
- [EIO-103 — Kernel Registration Validation](../validator/README.md)
- [KERNEL-005 — Kernel Registration](../../../../../../docs/architecture/kernel/KERNEL-005-KERNEL-REGISTRATION.md)
- [KERNEL-007 — Kernel Invariants](../../../../../../docs/architecture/kernel/KERNEL-007-KERNEL-INVARIANTS.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)