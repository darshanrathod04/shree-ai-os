# Event Bus Public API

## Package
`platform.core.eventbus.api`

## Purpose
The Event Bus API defines how Platform components publish and subscribe to events within Shree AI OS. It enables decoupled communication between Platform components through contracts, not direct dependencies.

## Architectural Responsibility
- Defines the public contract for Platform event communication.
- Specifies WHAT the Event Bus can do — implementations define HOW.
- Enables decoupled communication between Platform components.
- Ensures components communicate through contracts, not direct dependencies.

## Ownership
**Platform Core**

## Constitutional Authority
- ADD-PLT-202 — Platform Core
- ADD-PLT-205 — Platform Core Services
- ADD-PLT-206 — Kernel Orchestration

## Public Contracts

### `EventBus`
The public contract for Platform event communication.

| Method | Returns | Description |
|--------|---------|-------------|
| `publish(Event)` | `void` | Publishes an event to the Event Bus |
| `subscribe(EventTopic, EventSubscriber)` | `void` | Subscribes a subscriber to a topic |
| `unsubscribe(EventTopic, EventSubscriber)` | `void` | Unsubscribes a subscriber from a topic |
| `hasSubscribers(EventTopic)` | `boolean` | Returns whether there are subscribers for a topic |
| `registeredTopics()` | `Collection<EventTopic>` | Returns all currently registered topics |

## Domain Models

### `Event`
Represents a platform event.

| Field | Type | Description |
|-------|------|-------------|
| `topic()` | `EventTopic` | The event topic |
| `payload()` | `Object` | The event payload |
| `timestamp()` | `Instant` | The instant when the event was created |

### `EventTopic`
Represents a topic to which events can be published and subscribers can subscribe.

| Method | Returns | Description |
|--------|---------|-------------|
| `value()` | `String` | The topic name |

**Note:** `EventTopic` is value-based with `equals()`, `hashCode()`, and `toString()`.

### `EventSubscriber`
A functional interface for receiving events from the Event Bus.

| Method | Description |
|--------|-------------|
| `onEvent(Event)` | Called when an event is published to a subscribed topic |

## Design Constraints
- **Public interface only** — no implementation classes
- **No business logic** — interface defines only the contract
- **No Spring annotations** — framework-agnostic
- **No storage** — event storage belongs in the implementation
- **No dispatching** — event routing belongs in the implementation
- **No threading** — threading model belongs in the implementation

## Event Bus Principle
**Platform components communicate through contracts. Implementations remain hidden.**

## Relationship to the Platform
```
Platform Component A                Platform Component B
         |                                  |
         | publishes                        | subscribes
         v                                  |
  EventBus.publish(event)                   |
         |                                  |
         +---- EventBus ----+--------------->+
                            |                 |
                            |                 v
                            |          EventSubscriber.onEvent(event)
                            |
                            +---- EventBus.subscribe(topic, subscriber)
```

## Forward References
The following domain models are forward-reference placeholders for EIO-402:
- `Event` — Full implementation in EIO-402
- `EventTopic` — Full implementation in EIO-402
- `EventSubscriber` — Full implementation in EIO-402

## Out of Scope
The following concerns are explicitly **out of scope** for this package:
- **Storage** — event storage belongs in the implementation
- **Dispatching** — event routing belongs in the implementation
- **Validation** — validation logic belongs in the implementation
- **Errors** — error handling belongs in the implementation
- **Subscribers** — subscriber lifecycle belongs in the implementation
- **Threading** — threading model belongs in the implementation
- **Event Engine** — event processing engine belongs in the implementation

## Related Documents
- [EIO-402 — Event Bus Domain Models (forthcoming)](../model/README.md)
- [STD-003 — Platform Core Engineering Standard](../../../../../../docs/engineering/standards/STD-003-PLATFORM-CORE-ENGINEERING-STANDARD.md)