# Default Discovery Service

## Package
`platform.core.discovery.service`

## Purpose
Reference implementation of the Discovery Service that provides capability resolution by consulting the Kernel Registry.

The Registry remains the single source of truth for kernel information. Discovery owns capability resolution. Ownership shall never overlap.

## Architectural Responsibility
- Provides the reference implementation of capability resolution.
- Resolves capabilities by consulting the Kernel Registry — it does not maintain its own registry.
- Ensures the Registry remains the single source of truth for kernel information.
- Never duplicates Registry state or creates separate storage structures.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `DefaultDiscoveryService`
Reference implementation of `DiscoveryService`.

| Method | Description |
|--------|-------------|
| `resolveByCapability(CapabilityId)` | Resolves a kernel by its capability |
| `resolveByContract(ContractId)` | Resolves a kernel by its contract type |
| `supports(CapabilityId)` | Determines whether a capability exists |
| `availableCapabilities()` | Lists all available capabilities |

## Implementation Details

### Constructor Injection
```java
public DefaultDiscoveryService(KernelRegistry kernelRegistry, DiscoveryValidator validator)
```

### Resolution Flow
```
resolveByCapability()
  ↓
DiscoveryValidator.validateCapabilityId()
  ↓
KernelRegistry.findAll()
  ↓
Capability Match (scan kernel metadata tags)
  ↓
DiscoveryResult
```

### Storage
- **No internal storage** — does not cache or store kernel data
- **No cache** — always queries Registry for current state
- **No persistence** — in-memory only via Registry
- **Registry is the single source of truth** — never duplicates Registry state

## Design Constraints
- **Constructor injection only** — no setter injection
- **Thread-safe** — uses concurrent-safe Registry operations
- **Stateless** — no internal state beyond injected dependencies
- **Never bypasses validator** — all requests validated before resolution
- **Never bypasses error architecture** — uses DiscoveryException hierarchy
- **Never duplicates Registry data** — Registry is the single source of truth
- **Never exposes mutable collections** — returns unmodifiable views
- **No internal storage, no cache, no persistence**

## Engineering Constraints
The following are explicitly **out of scope** for this implementation:
- **Boot** — boot sequence is handled by the platform
- **Lifecycle** — lifecycle management is handled by the implementation layer
- **Event Bus** — event publishing is handled by the implementation
- **Configuration** — configuration is handled by the implementation
- **Health** — health checks are handled by the implementation
- **Spring** — no Spring annotations or dependencies
- **Persistence** — no database or serialization
- **Caching** — no internal cache
- **Logging** — logging is handled by the implementation
- **Metrics** — metrics are handled by the implementation

## Relationship to the Platform
```
platform.core.discovery.api.DiscoveryService
                            ^
                            |
                            | implements
                            |
platform.core.discovery.service.DefaultDiscoveryService
                            |
                            | uses
                            +--- platform.core.registry.api.KernelRegistry
                            |     (single source of truth for kernels)
                            |
                            +--- platform.core.discovery.validator.DiscoveryValidator
                            |     (validates all requests)
                            |
                            +--- platform.core.discovery.error.DiscoveryException
                            |     (error handling)
                            |
                            +--- platform.core.discovery.model.DiscoveryResult
                                  (return type)
```

## Discovery Principle
**Registry owns kernel information. Discovery owns capability resolution. Ownership shall never overlap.**

## Related Documents
- [EIO-201 — Discovery Service Public API](../api/README.md)
- [EIO-202 — Discovery Domain Models](../model/README.md)
- [EIO-204 — Discovery Validation](../validator/README.md)
- [EIO-205 — Discovery Error Architecture](../error/README.md)
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [ADD-PLT-206 — Kernel Orchestration](../../../../../../docs/architecture/platform/ADD-PLT-206-KERNEL-ORCHESTRATION.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)