# Configuration Domain Models

## Package
`platform.core.configuration.model`

## Purpose
The Configuration Domain Models define the immutable Platform Language for the Configuration Service within Shree AI OS. These models provide type-safe, immutable value objects for configuration representation.

## Architectural Responsibility
- Defines the domain models for configuration management.
- Provides immutable value objects for type-safe configuration representation.
- Enables consistent configuration handling across Platform components.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Models

### `ConfigurationKey`
Immutable configuration key.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The key value |

**Invariant:** Value is non-null and non-blank.

**Equality:** Value-based equality with `equals()`, `hashCode()`, and `toString()`.

### `ConfigurationNamespace`
Immutable configuration namespace.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The namespace value |

**Invariant:** Value is non-null and non-blank.

**Equality:** Value-based equality with `equals()`, `hashCode()`, and `toString()`.

### `ConfigurationType`
Enumeration of supported configuration types.

| Value | Description |
|-------|-------------|
| `STRING` | String configuration value |
| `INTEGER` | Integer configuration value |
| `BOOLEAN` | Boolean configuration value |
| `DOUBLE` | Double configuration value |
| `LIST` | List configuration value |
| `MAP` | Map configuration value |

### `ConfigurationEntry`
Immutable configuration entry.

| Field | Type | Description |
|-------|------|-------------|
| `key()` | `ConfigurationKey` | The configuration key |
| `namespace()` | `ConfigurationNamespace` | The configuration namespace |
| `type()` | `ConfigurationType` | The configuration type |
| `value()` | `Object` | The configuration value (may be null) |
| `description()` | `String` | The configuration description |
| `readOnly()` | `boolean` | Whether the configuration is read-only |
| `createdAt()` | `Instant` | The creation timestamp |

**Invariant:** All required fields are non-null. Value may be null.

## Design Constraints
- **Immutable** — all models are immutable value objects
- **No setters** — all fields are final and set via constructor
- **No Lombok** — explicit constructors, getters, equals, hashCode, toString
- **No service logic** — models are pure data carriers
- **No validation logic** — validation belongs to the service layer
- **No persistence** — no ORM annotations
- **No Spring** — framework-agnostic
- **No Records** — uses explicit classes for compatibility

## Engineering Principle
**Platform Language belongs inside the model package. API packages expose contracts only.**

## Relationship to the Platform
```
ConfigurationService (API)
    |
    | uses
    v
ConfigurationEntry (Model)
    |
    | contains
    v
├── ConfigurationKey
├── ConfigurationNamespace
├── ConfigurationType
└── ConfigurationValue
```

## Consistency with Other Subsystems
This model architecture follows the same pattern as:
- **Event Bus** — `platform.core.eventbus.model`
- **Registry** — `platform.core.registry.model`
- **Lifecycle** — `platform.core.lifecycle.model`

All follow the pattern:
1. Immutable value objects
2. Constructor validation
3. No setters
4. equals/hashCode/toString
5. No business logic

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Service logic** — business logic belongs in the service layer
- **Validation** — validation logic belongs in the service layer
- **Persistence** — persistence belongs in the implementation
- **Spring** — framework-agnostic
- **Records** — uses explicit classes for compatibility

## Related Documents
- [EIO-501 — Configuration Service Public API](../../api/README.md)
- [EIO-502 — Configuration Domain Models (this document)](README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)