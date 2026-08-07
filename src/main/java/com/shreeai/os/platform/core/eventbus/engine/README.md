# Event Dispatch Engine

## Package
`platform.core.eventbus.engine`

## Purpose
The Event Dispatch Engine executes event delivery within Shree AI OS. It remains independent from validation, subscriber registry, and service coordination.

## Architectural Responsibility
- Executes event delivery to subscribers.
- Records dispatch outcomes and collects failures.
- Remains independent from validation, subscriber registry, and service coordination.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Contracts

### `EventDispatchEngine`
Executes event delivery to subscribers.

| Method | Returns | Description |
|--------|---------|-------------|
| `dispatch(Event, Collection<EventSubscriber>)` | `DispatchResult` | Dispatches an event to the given subscribers |

**Note:** Throws `EventDispatchException` only for unexpected infrastructure failures.

### `DispatchResult`
Immutable result of a dispatch operation.

| Field | Type | Description |
|-------|------|-------------|
| `success()` | `boolean` | Whether the dispatch succeeded overall |
| `event()` | `Event` | The event that was dispatched |
| `subscribersAttempted()` | `int` | Number of subscribers attempted |
| `subscribersSucceeded()` | `int` | Number of subscribers that succeeded |
| `subscribersFailed()` | `int` | Number of subscribers that failed |
| `failureMessages()` | `List<String>` | List of failure messages (empty if no failures) |
| `timestamp()` | `Instant` | When the dispatch result was created |

**Invariant:** All fields are non-null. Failure messages list may be empty but never null.

## Dispatch Flow
```
Receive Event
    ↓
Receive Subscribers
    ↓
Invoke subscriber.onEvent(event) for each subscriber
    ↓
Collect results
    ↓
Create DispatchResult
    ↓
Return
```

## Dispatch Rules
- Continue dispatching even if one subscriber fails.
- One subscriber failure SHALL NOT stop other subscribers.
- Collect all failures.
- Never throw for expected subscriber failures.
- Unexpected infrastructure failures may throw EventDispatchException.

## Characteristics
- **Stateless** — no instance fields, no mutable state
- **Thread-safe** — can be called concurrently without synchronization
- **Never owns subscribers** — receives subscribers as parameters
- **Never validates events** — validation belongs to EventValidator
- **Never mutates events** — events are immutable
- **Never creates threads** — no thread creation
- **Never retries** — no retry logic
- **Never sleeps** — no Thread.sleep()
- **Never queues events** — no event queuing

## Engineering Principle
**Service coordinates. Engine executes. Validator protects. Models describe. Errors report. Responsibilities SHALL remain independent.**

## Relationship to the Platform
```
DefaultEventBusService (Service)
    |
    | coordinates
    v
EventDispatchEngine (Engine)
    |
    | executes
    v
DispatchResult
    |
    | contains
    v
Event + Subscriber outcomes
```

## Responsibilities

### Engine Responsibilities
- Execute dispatch
- Record dispatch outcome
- Collect failures
- Return DispatchResult

### Engine SHALL NOT Include
- Validation — validation belongs to EventValidator
- Subscriber Registry — registry belongs to DefaultEventBusService
- Queues — queue management belongs to the implementation
- Retry — retry logic belongs to the implementation
- Async Execution — async execution belongs to the implementation
- Scheduling — scheduling belongs to the implementation
- Persistence — event storage belongs to the implementation
- Filtering — event filtering belongs to the implementation
- Topic Lookup — topic lookup belongs to DefaultEventBusService

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Validation** — validation logic belongs to EventValidator
- **Subscriber Registry** — subscriber storage belongs to DefaultEventBusService
- **Queues** — queue management belongs to the implementation
- **Retry** — retry logic belongs to the implementation
- **Async Execution** — async execution belongs to the implementation
- **Scheduling** — scheduling belongs to the implementation
- **Persistence** — event storage belongs to the implementation
- **Filtering** — event filtering belongs to the implementation
- **Topic Lookup** — topic lookup belongs to DefaultEventBusService

## Related Documents
- [EIO-401 — Event Bus Public API](../api/README.md)
- [EIO-402 — Event Domain Models](../model/README.md)
- [EIO-403 — Event Validation](../validator/README.md)
- [EIO-404 — Event Error Architecture](../error/README.md)
- [EIO-405 — Default Event Bus Service](../service/README.md)
- [EIO-406 — Event Dispatch Engine (this document)](README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)