# Event Domain Models

## Package
`platform.core.eventbus.model`

## Purpose
The Event Domain Models define the immutable Platform language for the Event Bus subsystem within Shree AI OS. These models provide the type-safe data structures that the `platform.core.eventbus.api` package uses for event operations.

## Architectural Responsibility
- Defines the immutable domain objects used by the Event Bus subsystem.
- Provides the type-safe language that the `platform.core.eventbus.api` package uses for event operations.
- Ensures all event information is validated at construction time.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Domain Models

### `Event`
Represents a platform event.

| Field | Type | Description |
|-------|------|-------------|
| `id()` | `EventId` | Unique event identifier |
| `topic()` | `EventTopic` | Event topic |
| `metadata()` | `EventMetadata` | Event metadata |
| `payload()` | `Object` | Event payload (may be null) |
| `timestamp()` | `Instant` | Event creation timestamp |

**Invariant:** All fields except payload are non-null and validated at construction time.

### `EventId`
Represents a unique event identifier.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `UUID` | The UUID value |

**Invariant:** Always non-null. Generated automatically or constructed from a valid UUID string.

### `EventTopic`
Represents a topic to which events can be published and subscribers can subscribe.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The topic name |

**Invariant:** Name must not be null or blank. Value-based equality.

### `EventSubscriber`
A functional interface for receiving events from the Event Bus.

| Method | Description |
|--------|-------------|
| `onEvent(Event)` | Called when an event is published to a subscribed topic |

**Invariant:** Must handle all events delivered to subscribed topics.

### `EventMetadata`
Represents the metadata associated with an event.

| Field | Type | Description |
|-------|------|-------------|
| `publisher()` | `String` | Event publisher |
| `priority()` | `EventPriority` | Event priority |
| `correlationId()` | `String` | Correlation ID |
| `attributes()` | `Map<String, Object>` | Optional attributes |

**Invariant:** All required fields are non-null. Attributes map may be empty but never null.

### `EventPriority`
Represents the priority level of an event.

| Value | Description |
|-------|-------------|
| `LOW` | Processed when resources are available |
| `NORMAL` | Standard event processing |
| `HIGH` | Processed before normal events |
| `CRITICAL` | Processed immediately |

## Design Constraints
- **Immutable** — all models are immutable value objects
- **No business logic** — models are pure data carriers
- **No Spring annotations** — framework-agnostic
- **No persistence annotations** — no ORM mappings
- **No Lombok** — explicit getters and constructors
- **Constructor validation** — all invariants enforced at construction time

## Platform Language Principle
**Platform Language belongs inside the model package. API packages expose contracts only.**

## Relationship to the API
```
Platform Component
         |
         | uses
         v
  EventBus (API)
         |
         | publishes/subscribes
         v
  Event (Model)
         |
         | contains
         v
  ├── EventId (Model)
  ├── EventTopic (Model)
  ├── EventMetadata (Model)
  └── EventPriority (Model)
```

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Dispatching** — event routing belongs in the implementation
- **Storage** — event storage belongs in the implementation
- **Validation** — validation logic belongs in the implementation
- **Errors** — error handling belongs in the implementation
- **Subscribers** — subscriber lifecycle belongs in the implementation
- **Service** — service layer belongs in the implementation
- **Event Engine** — event processing engine belongs in the implementation

## Related Documents
- [EIO-401 — Event Bus Public API](../api/README.md)
- [EIO-402 — Event Domain Models (this document)](README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)