# Default Event Bus Service

## Package
`platform.core.eventbus.service`

## Purpose
The Default Event Bus Service provides the default in-memory implementation of the EventBus contract within Shree AI OS. It coordinates publishing and subscriber management, and delegates actual event delivery to the Event Dispatch Engine.

## Architectural Responsibility
- Coordinates publishing and subscriber management.
- Owns the subscriber registry.
- Delegates actual event delivery to the Event Dispatch Engine.
- Ensures all events are validated before processing.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Implementation

### `DefaultEventBusService`
The default in-memory implementation of the EventBus contract.

| Method | Description |
|--------|-------------|
| `publish(Event)` | Validates event, checks subscribers, delegates to EventDispatchEngine |
| `subscribe(EventTopic, EventSubscriber)` | Validates topic and subscriber, registers subscriber |
| `unsubscribe(EventTopic, EventSubscriber)` | Validates, removes subscriber, cleans up empty topics |
| `hasSubscribers(EventTopic)` | Read-only lookup for subscribers |
| `registeredTopics()` | Returns unmodifiable collection of all registered topics |

### Constructor Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| `EventValidator` | `platform.core.eventbus.validator.EventValidator` | Validates events, topics, and subscribers |
| `LifecycleService` | `platform.core.lifecycle.api.LifecycleService` | Lifecycle service (future compatibility) |
| `LifecycleTransitionEngine` | `platform.core.lifecycle.engine.LifecycleTransitionEngine` | Lifecycle transition engine (future compatibility) |
| `EventDispatchEngine` | `platform.core.eventbus.service.EventDispatchEngine` | Delegates event delivery |

## Internal Storage

### Subscriber Registry
- **Storage:** `ConcurrentHashMap<EventTopic, Set<EventSubscriber>>`
- **Thread-safe:** Uses `ConcurrentHashMap` and `ConcurrentHashMap.newKeySet()`
- **No synchronized blocks:** Relies on concurrent collections
- **Cleanup:** Removes empty topic sets on unsubscribe

## Flow Diagrams

### publish(Event)
```
Event
  |
  | validateEvent()
  v
EventValidator
  |
  | if invalid
  v
InvalidEventException
  |
  | if valid
  v
Check subscribers exist
  |
  | if none
  v
NoSubscribersException
  |
  | if exist
  v
EventDispatchEngine.dispatch()
  |
  v
Return
```

### subscribe(EventTopic, EventSubscriber)
```
Topic + Subscriber
  |
  | validateTopic()
  v
EventValidator
  |
  | if invalid
  v
InvalidEventException
  |
  | validateSubscriber()
  v
EventValidator
  |
  | if invalid
  v
InvalidEventException
  |
  | if valid
  v
Register in ConcurrentHashMap
  |
  v
Return
```

### unsubscribe(EventTopic, EventSubscriber)
```
Topic + Subscriber
  |
  | validateTopic()
  v
EventValidator
  |
  | if invalid
  v
InvalidEventException
  |
  | if valid
  v
Remove from ConcurrentHashMap
  |
  | if empty
  v
Remove topic key
  |
  v
Return
```

## Design Constraints
- **Constructor injection only** — all dependencies injected via constructor
- **Thread-safe** — uses ConcurrentHashMap for all storage
- **Validation always used** — never bypasses EventValidator
- **EventDispatchEngine delegation** — never dispatches directly
- **Never exposes mutable collections** — returns unmodifiable views
- **Never creates threads** — no thread creation
- **Never sleeps** — no Thread.sleep()
- **Never retries** — no retry logic
- **Never caches events** — no event caching

## Engineering Principle
**Service coordinates. Engine executes. Validator protects. Responsibilities SHALL remain separated.**

## Relationship to the Platform
```
Platform Component
         |
         | uses
         v
  EventBus (API)
         |
         | implemented by
         v
  DefaultEventBusService (Service)
         |
         | coordinates
         v
  ├── EventValidator (validates)
  ├── EventDispatchEngine (executes)
  └── LifecycleService (future)
```

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Dispatching** — event routing belongs in EventDispatchEngine
- **Retry** — retry logic belongs in EventDispatchEngine
- **Async execution** — async execution belongs in EventDispatchEngine
- **Queues** — queue management belongs in EventDispatchEngine
- **Persistence** — event storage belongs in the implementation
- **Scheduling** — scheduling belongs in EventDispatchEngine
- **Filtering** — event filtering belongs in EventDispatchEngine

## Related Documents
- [EIO-401 — Event Bus Public API](../api/README.md)
- [EIO-402 — Event Domain Models](../model/README.md)
- [EIO-403 — Event Validation](../validator/README.md)
- [EIO-404 — Event Error Architecture](../error/README.md)
- [EIO-405 — Default Event Bus Service (this document)](README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)