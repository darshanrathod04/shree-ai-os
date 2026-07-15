# Plugin Lifecycle Engine

## Overview

The Plugin Lifecycle Engine is a stateless, thread-safe component that governs plugin state transitions within Shree AI OS. It implements a deterministic state machine with five states and five allowed transitions.

## State Diagram

```
        ┌─────────────────────────────────────┐
        │                                     │
        v                                     │
   ┌─────────┐  load   ┌────────┐  start  ┌─────────┐
   │UNLOADED │───────→│ LOADED │───────→│ STARTED │
   └─────────┘        └────────┘        └─────────┘
        ↑                                    │
        │         ┌──────────┐               │ stop
        └─────────│  STOPPED │←──────────────┘
                  └──────────┘
                       │
                       │ start
                       v
                  ┌─────────┐
                  │ STARTED │
                  └─────────┘
```

## Transition Table

| From       | To         | Method       |
|------------|------------|--------------|
| UNLOADED   | LOADED     | `load()`     |
| LOADED     | STARTED    | `start()`    |
| STARTED    | STOPPED    | `stop()`     |
| STOPPED    | STARTED    | `start()`    |
| STOPPED    | UNLOADED   | `unload()`   |

### Rejected Transitions

| From       | To         | Reason                        |
|------------|------------|-------------------------------|
| STARTED    | LOADED     | Must stop before unloading    |
| UNLOADED   | STARTED    | Must load before starting     |
| FAILED     | STARTED    | Must be recovered first       |
| FAILED     | LOADED     | Must be recovered first       |
| (any)      | FAILED     | Not a valid target state      |

## Engine Responsibilities

The `PluginLifecycleEngine` is responsible **only** for:

- **Deciding** whether a requested state transition is valid
- **Returning** a `PluginTransitionResult` indicating success or failure

The engine does **NOT**:

- Store or cache plugins
- Validate plugin descriptors or JAR files
- Load JAR files or classloaders
- Execute any plugin code
- Publish or emit events
- Persist state to any database

## Core Components

| Class                      | Description                                          |
|----------------------------|------------------------------------------------------|
| `PluginTransitionResult`   | Immutable result capturing transition outcome         |
| `PluginLifecycleEngine`    | Stateless engine that validates transitions           |
| `PluginState` (model)      | Enum of possible plugin states (5 states)             |
| `PluginDescriptor` (model) | Immutable descriptor with plugin identity & metadata  |

## Usage Examples

### Starting a plugin

```java
PluginLifecycleEngine engine = new PluginLifecycleEngine();

// Assume descriptor is in UNLOADED state
// Load: UNLOADED -> LOADED
PluginTransitionResult loadResult = engine.load(descriptor);
// loadResult.success() == true

// Start: LOADED -> STARTED
PluginTransitionResult startResult = engine.start(descriptor);
// startResult.success() == true
```

### Stopping and restarting

```java
// Stop: STARTED -> STOPPED
PluginTransitionResult stopResult = engine.stop(descriptor);
// stopResult.success() == true

// Restart: STOPPED -> STARTED
PluginTransitionResult restartResult = engine.start(descriptor);
// restartResult.success() == true

// Unload: STOPPED -> UNLOADED
PluginTransitionResult unloadResult = engine.unload(descriptor);
// unloadResult.success() == true
```

### Handling rejected transitions

```java
// Any transition from FAILED state is rejected
PluginTransitionResult result = engine.start(descriptor);
// result.success() == false
// result.failureMessage() contains explanation
// result.currentState() == descriptor.state() (unchanged)
```

## Integration with DefaultPluginService

The `DefaultPluginService` coordinates lifecycle operations. It delegates to the `PluginLifecycleEngine`:

```
DefaultPluginService
    │
    ├── tracks plugin state (in-memory map)
    ├── delegates to engine.load(descriptor)
    ├── delegates to engine.start(descriptor)
    ├── delegates to engine.stop(descriptor)
    └── delegates to engine.unload(descriptor)
```

The service coordinates; the engine executes transitions.

## Engineering Principles

- **Stateless** — The engine holds no mutable state.
- **Thread-safe** — All classes are immutable or effectively immutable.
- **Deterministic** — Given the same inputs, the engine always produces the same output.
- **No persistence** — No database, filesystem, or I/O operations.
- **No plugin loading** — Does not load JARs, classloaders, or execute plugin code.
- **No framework coupling** — Pure Java 21 with zero dependencies on Spring, Lombok, or JPA.