# Discovery Service Public API

## Package
`platform.core.discovery.api`

## Purpose
The Discovery Service enables capability resolution within Shree AI OS without creating compile-time dependencies between kernels. This package defines the official Platform contract for discovery.

## Architectural Responsibility
- Defines the official Platform contract for capability resolution.
- Enables kernels to discover other kernels by capability or contract.
- Hides deployment details from requesting kernels.
- Ensures discovery remains independent of registry implementation.
- Supports version compatibility resolution (KD-004).

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `DiscoveryService`
The official Platform contract for capability resolution.

| Method | Returns | Description |
|--------|---------|-------------|
| `resolveByCapability(CapabilityId)` | `Optional<DiscoveryResult>` | Resolves a kernel by its capability |
| `resolveByContract(ContractId)` | `Optional<DiscoveryResult>` | Resolves a kernel by its contract type |
| `supports(CapabilityId)` | `boolean` | Determines whether a capability exists |
| `availableCapabilities()` | `Collection<CapabilityId>` | Lists all available capabilities |

## Discovery Principles

### Kernels Discover Capabilities, Not Implementations
Kernels should never know where another kernel lives. They should simply request the capability they need. The platform is responsible for locating the appropriate kernel.

### Discovery Rules (from KERNEL-006)
| Rule | Description |
|------|-------------|
| KD-001 | Only registered kernels may be discovered |
| KD-002 | Discovery returns contracts, not implementations |
| KD-003 | Discovery shall remain independent of deployment location |
| KD-004 | Discovery shall support version compatibility |
| KD-005 | Discovery failures shall be explicit |

## Design Constraints
- **No implementation** — this package contains only the public contract
- **No business logic** — the interface defines only the contract, not behavior
- **No Spring annotations** — the API is framework-agnostic
- **No persistence** — storage concerns are handled by the implementation
- **No registry implementation** — discovery is independent of the registry

## Domain Models Used

| Model | Description |
|-------|-------------|
| `CapabilityId` | Unique capability identity |
| `ContractId` | Discoverable contract identity |
| `DiscoveryResult` | Composed discovery result with status |
| `ResolutionStatus` | Enum: FOUND, NOT_FOUND, INCOMPATIBLE, UNAVAILABLE |

## Relationship to the Registry
```
platform.core.discovery.api.DiscoveryService
                            |
                            | uses (via implementation)
                            v
          platform.core.registry.api.KernelRegistry
                            |
                            | reads
                            v
              RegisteredKernel metadata
                            |
                            | returns
                            v
          platform.core.discovery.model.DiscoveryResult
                            |
                            +--- CapabilityId
                            +--- ContractId
                            +--- ResolutionStatus
                            +--- KernelId

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Models** — capability and contract types are defined by the implementation
- **Validation** — validation logic belongs in the implementation layer
- **Errors** — exception types are defined by the implementation
- **Service** — implementation classes are not part of this package
- **Tests** — testing is handled by the implementation

## Related Documents
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [EIO-101 — Kernel Registry Public API](../../registry/api/README.md)
- [ADD-PLT-202 — Platform Core](../../../../../../docs/architecture/platform/ADD-PLT-202-PLATFORM-CORE.md)
- [ADD-PLT-205 — Platform Core Services](../../../../../../docs/architecture/platform/ADD-PLT-205-PLATFORM-CORE-SERVICES.md)
- [ADD-PLT-206 — Kernel Orchestration](../../../../../../docs/architecture/platform/ADD-PLT-206-KERNEL-ORCHESTRATION.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)