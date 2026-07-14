# Health Service Public API

## Package
`platform.core.health.api`

## Purpose
The Health Service Public API defines health monitoring contracts for the Platform within Shree AI OS. It enables health checking across all Platform components.

## Architectural Responsibility
- Defines the public contract for Platform health monitoring.
- Specifies WHAT the Health Service can do — implementations define HOW.
- Enables health checking across all Platform components.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contract

### `HealthService`
The public contract for Platform health monitoring.

| Method | Returns | Description |
|--------|---------|-------------|
| `register(HealthComponent)` | `boolean` | Registers a health component for monitoring |
| `check(HealthComponent)` | `Optional<HealthReport>` | Checks health of a specific component |
| `checkAll()` | `Collection<HealthReport>` | Checks health of all registered components |
| `unregister(HealthComponent)` | `boolean` | Unregisters a health component |
| `exists(HealthComponent)` | `boolean` | Returns whether a component is registered |

## Domain Models (Forward References for EIO-602)

### `HealthComponent`
Represents a platform component for health monitoring.

| Method | Returns | Description |
|--------|---------|-------------|
| `name()` | `String` | The component name |

**Note:** Value-based equality with `equals()`, `hashCode()`, and `toString()`.

### `HealthStatus`
Represents the health state of a platform component.

| Constant | Description |
|----------|-------------|
| `HEALTHY` | Component is healthy and operating normally |
| `DEGRADED` | Component is degraded but still operational |
| `UNHEALTHY` | Component is unhealthy and not operating correctly |
| `UNKNOWN` | Component health is unknown or has not been checked |

### `HealthReport`
Represents the result of a health check.

| Method | Returns | Description |
|--------|---------|-------------|
| `component()` | `HealthComponent` | The health component |
| `status()` | `HealthStatus` | The health status |
| `message()` | `String` | The health message |
| `timestamp()` | `Instant` | The report timestamp |

### `HealthCheck`
Represents a single health check request.

| Method | Returns | Description |
|--------|---------|-------------|
| `component()` | `HealthComponent` | The health component to check |
| `deep()` | `boolean` | Whether this is a deep health check |

## Design Constraints
- **Interface only** — no implementation classes
- **No business logic** — interface defines only the contract
- **No Spring annotations** — framework-agnostic
- **No storage** — health state storage belongs in the implementation
- **No validation** — validation logic belongs in the implementation
- **No monitoring** — monitoring logic belongs in the implementation
- **No scheduling** — health check scheduling belongs in the implementation
- **No threading** — threading model belongs in the implementation
- **No persistence** — persistence belongs in the implementation
- **No caching** — caching belongs in the implementation
- **No events** — event publishing belongs in the implementation

## Health Principle
**The API defines WHAT the Platform can do. Future services define HOW.**

## Relationship to the Platform
```
Platform Component
         |
         | uses
         v
  HealthService (API)
         |
         | implemented by
         v
  Health Service (Implementation)
         |
         | monitors
         v
  HealthComponent
         |
         | produces
         v
  ├── HealthReport
  ├── HealthStatus
  └── HealthCheck
```

## Expected Future Package Structure
```
platform.core.health
├── api        — Public contracts (this package)
│   ├── HealthService.java    — Public health service contract
│   ├── HealthComponent.java  — Forward-reference placeholder (EIO-602)
│   ├── HealthStatus.java     — Forward-reference placeholder (EIO-602)
│   ├── HealthReport.java     — Forward-reference placeholder (EIO-602)
│   ├── HealthCheck.java      — Forward-reference placeholder (EIO-602)
│   ├── package-info.java
│   └── README.md
├── model      — Domain models (EIO-602)
├── validator  — Validation logic
├── error      — Error types
├── engine     — Health check execution
├── service    — Implementation
└── tests      — Verification suite
```

## Forward References
The following domain models are forward-reference placeholders for EIO-602:
- `HealthComponent` — Full implementation in EIO-602
- `HealthStatus` — Full implementation in EIO-602
- `HealthReport` — Full implementation in EIO-602
- `HealthCheck` — Full implementation in EIO-602

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Implementation** — no implementation classes
- **Storage** — health state storage belongs in the implementation
- **Validation** — validation logic belongs in the implementation
- **Monitoring** — monitoring logic belongs in the implementation
- **Scheduling** — health check scheduling belongs in the implementation
- **Threading** — threading model belongs in the implementation
- **Persistence** — persistence belongs in the implementation
- **Caching** — caching belongs in the implementation
- **Events** — event publishing belongs in the implementation

## Related Documents
- [EIO-601 — Health Service Public API (this document)](./README.md)
- [EIO-602 — Health Domain Models (forthcoming)](./README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)