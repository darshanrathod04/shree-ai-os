# Plugin Model

## Purpose

The Plugin Model package defines the immutable Platform Language for the Plugin Framework within Shree AI OS. It contains only data structures — no business logic, no persistence, no services.

## Architecture

```
PluginId (identity)
    │
    ▼
Plugin (entity)
    │
    ▼
PluginDescriptor (metadata)
    │
    ▼
PluginRequest (operation)
```

## Model Responsibilities

### PluginId

Immutable value object representing a unique plugin identifier.

**Responsibilities:**
- Provides unique identity for plugins
- Validates non-null, non-blank values
- Value equality based on string value

**Fields:**
- `String value` — the plugin identifier

**Methods:**
- `String value()` — returns the identifier

### Plugin

Immutable value object representing a plugin entity.

**Responsibilities:**
- Represents a plugin with identity, name, and version
- Validates all fields on construction
- Value equality based on all fields

**Fields:**
- `PluginId id` — unique identifier
- `String name` — human-readable name
- `String version` — semantic version

**Methods:**
- `PluginId id()` — returns the identifier
- `String name()` — returns the name
- `String version()` — returns the version

### PluginDescriptor

Immutable value object describing a registered plugin.

**Responsibilities:**
- Provides metadata about a registered plugin
- Tracks plugin state and load timestamp
- Identifies the plugin provider

**Fields:**
- `Plugin plugin` — the plugin
- `PluginState state` — current lifecycle state
- `Instant loadedAt` — timestamp when loaded
- `String provider` — plugin provider name

**Methods:**
- `Plugin plugin()` — returns the plugin
- `PluginState state()` — returns the state
- `Instant loadedAt()` — returns the load timestamp
- `String provider()` — returns the provider

### PluginState

Enumeration of plugin lifecycle states.

**Values:**
- `LOADED` — Plugin loaded into memory, not started
- `UNLOADED` — Plugin unloaded from memory
- `STARTED` — Plugin running and operational
- `STOPPED` — Plugin stopped but remains loaded
- `FAILED` — Plugin encountered an error

### PluginRequest

Immutable value object for plugin operation requests.

**Responsibilities:**
- Encapsulates parameters for plugin operations
- Indicates whether operation should be forced

**Fields:**
- `Plugin plugin` — the plugin
- `boolean force` — whether to force the operation

**Methods:**
- `Plugin plugin()` — returns the plugin
- `boolean force()` — returns the force flag

## Immutability Rules

All models in this package follow strict immutability rules:

1. **Final classes** — All classes are declared `final` to prevent extension
2. **Final fields** — All fields are declared `final` to prevent reassignment
3. **No setters** — No setter methods exist; state cannot be modified after construction
4. **Constructor validation** — All constructors validate inputs (non-null, non-blank)
5. **Value equality** — `equals()` and `hashCode()` based on field values
6. **Defensive copies** — Mutable fields are copied in constructors (if applicable)
7. **No business logic** — Models contain only data and validation

## Future Package Layout

```
platform.core.plugin
├── api                    # Public contracts
│   ├── PluginService.java
│   └── ...
├── model                  # Domain models (this package)
│   ├── PluginId.java
│   ├── Plugin.java
│   ├── PluginDescriptor.java
│   ├── PluginState.java
│   └── PluginRequest.java
├── service                # Service implementation (future)
│   ├── DefaultPluginService.java
│   └── package-info.java
├── engine                 # Plugin engine (future)
│   ├── PluginEngine.java
│   ├── PluginLoader.java
│   └── package-info.java
├── error                  # Error handling (future)
│   ├── PluginErrorCode.java
│   ├── PluginException.java
│   └── package-info.java
└── validator              # Validation (future)
    ├── PluginValidator.java
    └── package-info.java
```

## Design Constraints

This package follows strict architectural rules:

- ✅ Immutable — final classes, final fields
- ✅ Constructor validation
- ✅ equals(), hashCode(), toString()
- ✅ No setters
- ✅ No business logic
- ✅ No persistence
- ✅ No services
- ✅ No validation
- ✅ No events
- ✅ No threading
- ✅ No monitoring
- ✅ No Lombok
- ✅ No Spring
- ✅ No JPA

## Examples

### Creating a PluginId

```java
PluginId id = new PluginId("my-plugin");
String value = id.value();  // "my-plugin"
```

### Creating a Plugin

```java
PluginId id = new PluginId("my-plugin");
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");

PluginId pluginId = plugin.id();        // PluginId instance
String name = plugin.name();            // "My Plugin"
String version = plugin.version();      // "1.0.0"
```

### Creating a PluginDescriptor

```java
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");
PluginDescriptor descriptor = new PluginDescriptor(
    plugin,
    PluginState.STARTED,
    Instant.now(),
    "Platform Core"
);

Plugin plugin = descriptor.plugin();      // Plugin instance
PluginState state = descriptor.state();   // PluginState.STARTED
Instant loadedAt = descriptor.loadedAt(); // timestamp
String provider = descriptor.provider();  // "Platform Core"
```

### Creating a PluginRequest

```java
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");
PluginRequest request = new PluginRequest(plugin, true);

Plugin plugin = request.plugin();  // Plugin instance
boolean force = request.force();   // true
```

### Value Equality

```java
PluginId id1 = new PluginId("my-plugin");
PluginId id2 = new PluginId("my-plugin");

assertEquals(id1, id2);  // true — same value
assertEquals(id1.hashCode(), id2.hashCode());  // true — same hash
```

### Immutability Guarantee

```java
PluginId id = new PluginId("my-plugin");
// id.value() returns "my-plugin"
// No setter exists — value cannot be changed
// PluginId is final — cannot be extended
// Field is final — cannot be reassigned
```

## Constitutional Authority

- **ADD-PLT-301**: Plugin Framework definition
- **STD-003**: Platform Language standards
- **ADR-001**: Interface-first design

## Ownership

**Platform Core** — This package is owned and maintained by the Platform Core team. No external modifications without review.

## Version

1.0 — Initial model definition (EIO-702)