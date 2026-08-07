 # Event Bus Error Architecture

## Package
`platform.core.eventbus.error`

## Purpose
The Event Bus Error Architecture defines all standard errors used by the Event Bus subsystem within Shree AI OS. No dispatching or service logic is implemented.

## Architectural Responsibility
- Provides a complete error architecture for the Event Bus subsystem.
- Ensures consistent error reporting across all Event Bus operations.
- Follows the Platform-wide error pattern established by other subsystems.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Error Architecture

### `EventErrorCode`
Enumeration of all possible Event Bus error conditions.

| Code | Description |
|------|-------------|
| `EVENT_INVALID` | An event failed structural validation |
| `EVENT_VALIDATION_FAILED` | Event validation failed due to missing or invalid fields |
| `EVENT_NO_SUBSCRIBERS` | No subscribers found for the event topic |
| `EVENT_DISPATCH_FAILED` | Event dispatch failed due to an unexpected error |
| `EVENT_TOPIC_NOT_FOUND` | The event topic was not found in the registry |
| `EVENT_SUBSCRIBER_FAILED` | A subscriber failed to process the event |
| `EVENT_PUBLISH_FAILED` | Event publishing failed due to an unexpected error |

### `EventError`
Immutable error model containing error code, message, timestamp, and optional details.

| Field | Type | Description |
|-------|------|-------------|
| `code()` | `EventErrorCode` | The error code |
| `message()` | `String` | The error message |
| `timestamp()` | `Instant` | The error timestamp |
| `details()` | `Map<String, Object>` | Optional error details |

**Invariant:** All fields are non-null. Details map may be empty but never null.

### `EventBusException`
Base runtime exception for all Event Bus errors.

| Method | Returns | Description |
|--------|---------|-------------|
| `error()` | `EventError` | The event error |
| `code()` | `EventErrorCode` | The error code |
| `getMessage()` | `String` | The error message |

**Note:** This SHALL become the ONLY base exception for the Event Bus subsystem.

### `InvalidEventException`
Thrown when an Event fails structural validation.

| Error Code | Description |
|------------|-------------|
| `EVENT_INVALID` | An event failed structural validation |
| `EVENT_VALIDATION_FAILED` | Event validation failed due to missing or invalid fields |

### `NoSubscribersException`
Thrown when publishing to a topic without subscribers.

| Error Code | Description |
|------------|-------------|
| `EVENT_NO_SUBSCRIBERS` | No subscribers found for the event topic |

**Note:** Implementation may choose whether this is exceptional or informational.

### `EventDispatchException`
Thrown only for unexpected dispatch failures.

| Error Code | Description |
|------------|-------------|
| `EVENT_DISPATCH_FAILED` | Event dispatch failed due to an unexpected error |

**Note:** Should only be thrown for truly exceptional circumstances.

## Exception Hierarchy
```
EventBusException (base)
├── InvalidEventException
├── NoSubscribersException
└── EventDispatchException
```

## Design Constraints
- **Immutable error model** — all error models are immutable value objects
- **No business logic** — errors are pure data carriers
- **No Spring annotations** — framework-agnostic
- **No persistence annotations** — no ORM mappings
- **No Lombok** — explicit constructors and getters
- **Constructor validation** — all invariants enforced at construction time

## Error Principle
**Every Platform Core Service owns its own Error Architecture. All Error Architectures SHALL follow one Platform pattern.**

## Relationship to the Platform
```
Event Model
    |
    | validateEvent()
    v
EventValidator
    |
    | if invalid
    v
InvalidEventException
    |
    | contains
    v
EventError
    |
    | contains
    v
EventErrorCode
```

## Consistency with Other Subsystems
This error architecture follows the same pattern as:
- **Registry** — `platform.core.registry.error`
- **Lifecycle** — `platform.core.lifecycle.error`
- **Discovery** — `platform.core.discovery.error`

All follow the pattern:
1. `*ErrorCode` enum
2. `*Error` immutable model
3. `*Exception` base runtime exception
4. Concrete exceptions extending the base

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Dispatching** — event routing belongs in the implementation
- **Queues** — queue management belongs in the implementation
- **Thread pools** — threading model belongs in the implementation
- **Subscribers** — subscriber lifecycle belongs in the implementation
- **Validation** — validation logic belongs in the implementation
- **Service** — service layer belongs in the implementation
- **Dispatch Engine** — event processing engine belongs in the implementation
- **Tests** — test classes belong in the test package

## Related Documents
- [EIO-401 — Event Bus Public API](../api/README.md)
- [EIO-402 — Event Domain Models](../model/README.md)
- [EIO-403 — Event Validation](../validator/README.md)
- [EIO-404 — Event Error Architecture (this document)](README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)