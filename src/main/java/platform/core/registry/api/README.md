# Kernel Registry Public API

## Package
`platform.core.registry.api`

## Purpose
The Kernel Registry is the central registration authority for all kernels within Shree AI OS. It provides the public contract for kernel registration, unregistration, and discovery.

## Architectural Responsibility
- Provides the public contract for kernel registration and discovery.
- Enforces the principle that no kernel participates in the platform until it is formally registered (KERNEL-005).
- Maintains a registry of known kernels and their associated metadata.
- Enables platform awareness, dependency validation, and contract discovery.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-005 — Kernel Registration
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services

## Public Contracts

### `KernelRegistry<T>`
The central registration interface parameterized over the registration entry type `T`.

| Method | Description |
|--------|-------------|
| `register(String kernelId, T entry)` | Registers a kernel with the platform. |
| `unregister(String kernelId)` | Unregisters a kernel from the platform. |
| `find(String kernelId)` | Finds a registered kernel by its unique identifier. |
| `findAll()` | Returns all currently registered kernels. |
| `exists(String kernelId)` | Checks whether a kernel is currently registered. |

## Out of Scope
The following concerns are explicitly **out of scope** for this package and are delegated to the implementation layer:

- **Models** — Registration entry types are defined by the implementation.
- **Validation** — Validation logic belongs in the implementation layer.
- **Exceptions** — Exception types are defined by the implementation.
- **Lifecycle** — Lifecycle management is handled by the implementation.
- **Event Bus** — Event publishing is handled by the implementation.
- **Tests** — Testing is handled by the implementation.

## Design Constraints
- No implementation classes — this package contains only the public contract.
- No business logic — the interface defines only the contract, not behavior.
- No Spring annotations — the API is framework-agnostic.
- No persistence — storage concerns are handled by the implementation.

## Future Evolution
The registration model supports the following extensions without redesign:
- Dynamic kernel loading
- Plugin registration
- Remote kernel registration
- Distributed platform nodes
- Marketplace extensions

## Related Documents
- [KERNEL-005 — Kernel Registration](../../../../../../docs/architecture/kernel/KERNEL-005-KERNEL-REGISTRATION.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)
- [ADD-PLT-202 — Platform Core](../../../../../../docs/architecture/platform/ADD-PLT-202-PLATFORM-CORE.md)
- [ADD-PLT-205 — Platform Core Services](../../../../../../docs/architecture/platform/ADD-PLT-205-PLATFORM-CORE-SERVICES.md)