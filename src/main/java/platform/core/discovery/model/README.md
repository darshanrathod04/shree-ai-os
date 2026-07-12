# Discovery Domain Models

## Package
`platform.core.discovery.model`

## Purpose
Domain models defining the Platform language for capability discovery. These immutable value types represent capability identity, contract identity, resolution status, and discovery results.

## Architectural Responsibility
- Defines the immutable value types that represent capability identity, contract identity, resolution status, and discovery results.
- Provides the type-safe language that the `platform.core.discovery.api` package uses for capability resolution.
- Ensures all capability and contract information is validated at construction time.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Domain Models

### `CapabilityId`
Represents the unique identity of a platform capability.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The underlying identifier string |

- Immutable by design (final class, final field)
- Validates non-null and non-blank at construction
- Implements `equals()`, `hashCode()`, `toString()`

### `ContractId`
Represents a discoverable platform contract.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The underlying identifier string |

- Immutable by design (final class, final field)
- Validates non-null and non-blank at construction
- Implements `equals()`, `hashCode()`, `toString()`

### `ResolutionStatus`
Enumeration of capability resolution outcomes.

| Value | Description |
|-------|-------------|
| `FOUND` | The capability was found and is available |
| `NOT_FOUND` | The capability was not found in the platform |
| `INCOMPATIBLE` | The capability was found but is incompatible with the request |
| `UNAVAILABLE` | The capability was found but is currently unavailable |

### `DiscoveryResult`
Represents the result of capability discovery. Composes capability, kernel, contract, and status.

| Method | Returns | Description |
|--------|---------|-------------|
| `capabilityId()` | `CapabilityId` | The capability identifier |
| `kernelId()` | `KernelId` | The kernel identifier |
| `contractId()` | `ContractId` | The contract identifier |
| `status()` | `ResolutionStatus` | The resolution status |

- Immutable by design (final class, final fields)
- Validates non-null for all fields at construction via `Objects.requireNonNull`
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
platform.core.discovery.api.DiscoveryService<C, K>
                                    |
                                    | uses
                                    v
          platform.core.discovery.model.DiscoveryResult
                                    |
                                    +--- CapabilityId
                                    +--- ContractId
                                    +--- ResolutionStatus
                                    +--- KernelId (from registry model)
```

## Related Documents
- [EIO-201 — Discovery Service Public API](../api/README.md)
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [ADD-PLT-202 — Platform Core](../../../../../../docs/architecture/platform/ADD-PLT-202-PLATFORM-CORE.md)
- [ADD-PLT-205 — Platform Core Services](../../../../../../docs/architecture/platform/ADD-PLT-205-PLATFORM-CORE-SERVICES.md)
- [ADD-PLT-206 — Kernel Orchestration](../../../../../../docs/architecture/platform/ADD-PLT-206-KERNEL-ORCHESTRATION.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)