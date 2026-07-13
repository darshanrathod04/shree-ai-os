# Event Validation Layer

## Package
`platform.core.eventbus.validator`

## Purpose
The Event Validation Layer verifies the structural correctness of Event Bus domain models within Shree AI OS. It remains completely independent from dispatching and service execution.

## Architectural Responsibility
- Verifies the structural correctness of Event Bus domain models.
- Remains completely independent from dispatching and service execution.
- Protects the Platform Language by ensuring all models meet invariants.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contract

### `EventValidator`
A stateless utility class that provides validation methods for Event Bus domain models.

| Method | Returns | Description |
|--------|---------|-------------|
| `validateEvent(Event)` | `ValidationResult` | Validates an Event |
| `validateEventId(EventId)` | `ValidationResult` | Validates an EventId |
| `validateTopic(EventTopic)` | `ValidationResult` | Validates an EventTopic |
| `validateMetadata(EventMetadata)` | `ValidationResult` | Validates EventMetadata |
| `validateSubscriber(EventSubscriber)` | `ValidationResult` | Validates an EventSubscriber |
| `validatePayload(Object)` | `ValidationResult` | Validates an event payload |

## Validation Rules

### Event
- ✓ Exists (not null)
- ✓ EventId exists and is valid
- ✓ Topic exists and is valid
- ✓ Metadata exists and is valid
- ✓ Payload exists (not null)
- ✓ Timestamp exists (not null)

### EventId
- ✓ Exists (not null)
- ✓ UUID is valid (not null)

### EventTopic
- ✓ Exists (not null)
- ✓ Not blank
- ✓ No leading/trailing spaces
- ✓ Length <= 128 characters

### EventMetadata
- ✓ Exists (not null)
- ✓ Publisher exists and is not blank
- ✓ Priority exists (not null)
- ✓ CorrelationId exists and is not blank
- ✓ Attributes map is not null

### EventSubscriber
- ✓ Exists (not null)

### Payload
- ✓ Exists (not null)
- Expected payload failures return `ValidationResult`, never exceptions

## Characteristics
- **Stateless** — no instance fields, no mutable state
- **Deterministic** — same inputs always produce same outputs
- **Thread-safe** — can be called concurrently without synchronization
- **Pure validation** — never dispatches events, never mutates models

## Design Constraints
- **Reuses ValidationResult** — uses `platform.core.registry.validator.ValidationResult`
- **No dispatching** — event routing belongs in the implementation
- **No threading** — threading model belongs in the implementation
- **No queues** — queue management belongs in the implementation
- **No retry** — retry logic belongs in the implementation
- **No persistence** — event storage belongs in the implementation
- **No service** — service layer belongs in the implementation
- **No errors** — error handling belongs in the implementation
- **No engine** — event processing engine belongs in the implementation

## Validation Principle
**Validation protects Platform Language. Validation never dispatches events. Validation never mutates models.**

## Relationship to the Platform
```
Event Model
    |
    | validateEvent()
    v
EventValidator
    |
    | returns
    v
ValidationResult
    |
    | isValid()?
    v
EventBus (API)
    |
    | if valid, dispatch
    v
Event Engine (Implementation)
```

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Dispatching** — event routing belongs in the implementation
- **Threading** — threading model belongs in the implementation
- **Queues** — queue management belongs in the implementation
- **Retry** — retry logic belongs in the implementation
- **Persistence** — event storage belongs in the implementation
- **Service** — service layer belongs in the implementation
- **Errors** — error handling belongs in the implementation
- **Engine** — event processing engine belongs in the implementation

## Related Documents
- [EIO-401 — Event Bus Public API](../api/README.md)
- [EIO-402 — Event Domain Models](../model/README.md)
- [EIO-403 — Event Validation (this document)](./README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)