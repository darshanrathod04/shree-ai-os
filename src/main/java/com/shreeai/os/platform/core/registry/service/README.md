# Default Kernel Registry Service

## Package
`platform.core.registry.service`

## Purpose
Provides the default in-memory implementation of the Kernel Registry. This implementation serves as the official reference implementation for the Platform Foundation.

## Architectural Responsibility
- Provides the default in-memory implementation of the `KernelRegistry` interface.
- Serves as the official reference implementation for the Platform Foundation.
- Owns the registry storage and enforces all registration, unregistration, and lookup operations.
- Ensures thread-safe access to registered kernels.
- Never bypasses validation or error architecture.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-005 — Kernel Registration
- KERNEL-007 — Kernel Invariants
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `DefaultKernelRegistry`
Default in-memory implementation of `KernelRegistry<RegisteredKernel>`.

| Method | Description |
|--------|-------------|
| `register(String kernelId, RegisteredKernel entry)` | Registers a kernel after validation and duplicate check |
| `unregister(String kernelId)` | Unregisters a kernel by identifier |
| `find(String kernelId)` | Finds a registered kernel by identifier |
| `findAll()` | Returns all registered kernels (unmodifiable) |
| `exists(String kernelId)` | Checks if a kernel is registered |

## Registration Flow
```
register()
  ↓
Validate (via KernelRegistrationValidator)
  ↓
Duplicate Check (via ConcurrentHashMap)
  ↓
Store (in ConcurrentHashMap)
  ↓
Return true
```

## Unregistration Flow
```
unregister()
  ↓
Exists? (via ConcurrentHashMap.containsKey)
  ↓
Remove (via ConcurrentHashMap.remove)
  ↓
Return true/false
```

## Lookup Flow
```
find()
  ↓
Lookup (via ConcurrentHashMap.get)
  ↓
Optional Result
```

## Design Constraints
- **Thread-safe** — uses `ConcurrentHashMap` for all storage operations
- **Constructor injection only** — no setter injection
- **Never bypasses validation** — all registrations validated via `KernelRegistrationValidator`
- **Never bypasses error architecture** — uses `RegistryException` hierarchy
- **Never modifies RegisteredKernel** — models are immutable
- **Never exposes internal collections** — returns unmodifiable views via `Collections.unmodifiableCollection`
- **In-memory only** — no persistence, no caching, no serialization

## Storage
```java
ConcurrentHashMap<KernelId, RegisteredKernel>
```

## Engineering Constraints
The following are explicitly **out of scope** for this implementation:
- **Discovery** — discovery is handled by a separate service
- **Lifecycle** — lifecycle management is handled by the implementation layer
- **Boot** — boot sequence is handled by the platform
- **Event Bus** — event publishing is handled by the implementation
- **Configuration** — configuration is handled by the implementation
- **Health** — health checks are handled by the implementation
- **Spring** — no Spring annotations or dependencies
- **Persistence** — in-memory only, no database
- **Logging** — logging is handled by the implementation
- **Metrics** — metrics are handled by the implementation

## Relationship to the Registry
```
platform.core.registry.api.KernelRegistry<RegisteredKernel>
                            ^
                            |
                            | implements
                            |
platform.core.registry.service.DefaultKernelRegistry
                            |
                            | uses
                            +--- platform.core.registry.validator.KernelRegistrationValidator
                            +--- platform.core.registry.model.RegisteredKernel
                            +--- platform.core.registry.error.RegistryException
                            |     +--- DuplicateKernelException
                            |     +--- InvalidKernelException
                            |     +--- KernelNotFoundException
                            |
                            | stores
                            v
              ConcurrentHashMap<KernelId, RegisteredKernel>
```

## Related Documents
- [EIO-101 — Kernel Registry Public API](../api/README.md)
- [EIO-102 — Kernel Registry Domain Models](../model/README.md)
- [EIO-103 — Kernel Registration Validation](../validator/README.md)
- [EIO-104 — Registry Error Architecture](../error/README.md)
- [KERNEL-005 — Kernel Registration](../../../../../../docs/architecture/kernel/KERNEL-005-KERNEL-REGISTRATION.md)
- [KERNEL-007 — Kernel Invariants](../../../../../../docs/architecture/kernel/KERNEL-007-KERNEL-INVARIANTS.md)
- [ADD-PLT-206 — Kernel Orchestration](../../../../../../docs/architecture/platform/ADD-PLT-206-KERNEL-ORCHESTRATION.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)