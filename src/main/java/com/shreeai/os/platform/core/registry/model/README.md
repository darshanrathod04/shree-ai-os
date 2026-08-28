# Kernel Registry Domain Models

## Package
`platform.core.registry.model`

## Purpose
Domain models defining the Platform language used by the Kernel Registry API. These immutable value types represent kernel identity, version, metadata, and registration state.

## Architectural Responsibility
- Defines the immutable value types that represent kernel identity, version, metadata, and registration state.
- Provides the type-safe language that the `platform.core.registry.api` package uses for registration and discovery.
- Ensures all kernel identity and version information is validated at construction time.

## Ownership
**Platform Core**

## Constitutional Authority
- CONST-001
- KERNEL-005 — Kernel Registration
- KERNEL-006 — Kernel Discovery
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services

## Domain Models

### `KernelId`
Represents the unique identity of a Kernel.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The underlying identifier string |

- Immutable by design (final class, final field)
- Validates non-null and non-blank at construction
- Implements `equals()`, `hashCode()`, `toString()`

### `KernelVersion`
Represents the semantic version of a Kernel (Major.Minor.Patch).

| Method | Returns | Description |
|--------|---------|-------------|
| `major()` | `int` | The major version number |
| `minor()` | `int` | The minor version number |
| `patch()` | `int` | The patch version number |
| `compareTo(KernelVersion)` | `int` | Compare versions for ordering |

- Immutable by design (final class, final fields)
- Validates non-negative version numbers at construction
- Implements `Comparable<KernelVersion>` for version ordering
- Implements `equals()`, `hashCode()`, `toString()`
- String representation: `"major.minor.patch"`

### `KernelMetadata`
Represents descriptive information about a Kernel.

| Method | Returns | Description |
|--------|---------|-------------|
| `name()` | `String` | The kernel name |
| `description()` | `String` | The kernel description |
| `author()` | `String` | The kernel author |
| `tags()` | `Set<String>` | Unmodifiable set of tags |
| `category()` | `String` | The kernel category |
| `createdTimestamp()` | `Instant` | The creation timestamp |

- Immutable by design (final class, final fields, defensive copy of `Set`)
- Validates non-null for all fields, non-blank for name at construction
- Implements `equals()`, `hashCode()`, `toString()`

### `RegisteredKernel`
Represents a registered Kernel. Composes identity, version, and metadata.

| Method | Returns | Description |
|--------|---------|-------------|
| `kernelId()` | `KernelId` | The unique kernel identity |
| `version()` | `KernelVersion` | The kernel version |
| `metadata()` | `KernelMetadata` | The kernel metadata |

- Immutable by design (final class, final fields)
- Validates non-null for all fields at construction via `Objects.requireNonNull`
- Implements `equals()`, `hashCode()`, `toString()`

## Design Constraints
- All models are **immutable** — no setters, no mutable state
- **No business logic** — models are pure data carriers
- **No Spring annotations** — framework-agnostic
- **No persistence** — no JPA, Hibernate, or database annotations
- **No Lombok** — all boilerplate is hand-written
- **No services, validators, or exceptions** — pure domain models only

## Relationship to the API
```
platform.core.registry.api.KernelRegistry<RegisteredKernel>
                                    |
                                    | uses
                                    v
                    platform.core.registry.model.RegisteredKernel
                                    |
                                    +--- KernelId
                                    +--- KernelVersion
                                    +--- KernelMetadata
```

## Related Documents
- [EIO-101 — Kernel Registry Public API](../api/README.md)
- [KERNEL-005 — Kernel Registration](../../../../../../docs/architecture/kernel/KERNEL-005-KERNEL-REGISTRATION.md)
- [KERNEL-006 — Kernel Discovery](../../../../../../docs/architecture/kernel/KERNEL-006-KERNEL-DISCOVERY.md)
- [STD-002 — Kernel Development Standard](../../../../../../docs/engineering/standards/STD-002-KERNEL-DEVELOPMENT-STANDARD.MD)