# Plugin API

## Purpose

The Plugin API defines the public contracts for the Plugin Framework within Shree AI OS. It specifies **WHAT** the Platform can do with plugins—not **HOW** it does it.

This package contains only interfaces and immutable value objects. No implementation, no business logic, no persistence.

## Architecture

```
PluginService (interface)
    │
    ├── register(Plugin) → boolean
    ├── get(Plugin) → Optional<PluginDescriptor>
    ├── list() → Collection<PluginDescriptor>
    ├── unregister(Plugin) → boolean
    └── exists(Plugin) → boolean
    │
    ▼
Plugin Models
    │
    ├── Plugin (identity)
    ├── PluginDescriptor (metadata)
    ├── PluginState (lifecycle)
    └── PluginRequest (operation)
    │
    ▼
Future Implementation
    (platform.core.plugin.service)
    (platform.core.plugin.engine)
    (platform.core.plugin.loader)
```

## PluginService

The central interface for plugin operations.

### Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `register(Plugin)` | Register a plugin | `true` if registered, `false` if exists |
| `get(Plugin)` | Retrieve plugin descriptor | `Optional<PluginDescriptor>` |
| `list()` | List all registered plugins | `Collection<PluginDescriptor>` |
| `unregister(Plugin)` | Unregister a plugin | `true` if unregistered, `false` if not found |
| `exists(Plugin)` | Check if plugin is registered | `true` if exists, `false` otherwise |

## Models

### Plugin

Immutable value object representing a plugin identity.

```java
Plugin plugin = new Plugin("my-plugin");
String value = plugin.value();  // "my-plugin"
```

### PluginDescriptor

Immutable value object describing a registered plugin.

```java
PluginDescriptor descriptor = new PluginDescriptor(
    plugin,
    PluginState.STARTED
);
Plugin plugin = descriptor.plugin();      // Plugin instance
PluginState state = descriptor.state();   // PluginState.STARTED
```

### PluginState

Enumeration of plugin lifecycle states.

| State | Description |
|-------|-------------|
| `LOADED` | Plugin loaded into memory, not started |
| `UNLOADED` | Plugin unloaded from memory |
| `STARTED` | Plugin running and operational |
| `STOPPED` | Plugin stopped but remains loaded |
| `FAILED` | Plugin encountered an error |

### PluginRequest

Immutable value object for plugin operation requests.

```java
PluginRequest request = new PluginRequest(plugin, true);
Plugin plugin = request.plugin();  // Plugin instance
boolean deep = request.deep();     // true
```

## Future Package Layout

```
platform.core.plugin
├── api                    # Public contracts (this package)
│   ├── PluginService.java
│   ├── Plugin.java
│   ├── PluginDescriptor.java
│   ├── PluginState.java
│   └── PluginRequest.java
├── model                  # Domain models (EIO-702)
│   ├── PluginManifest.java
│   ├── PluginDependency.java
│   └── PluginMetadata.java
├── service                # Service implementation (EIO-703)
│   ├── DefaultPluginService.java
│   └── package-info.java
├── engine                 # Plugin engine (EIO-704)
│   ├── PluginEngine.java
│   ├── PluginLoader.java
│   └── package-info.java
├── error                  # Error handling (EIO-705)
│   ├── PluginErrorCode.java
│   ├── PluginException.java
│   └── package-info.java
└── validator              # Validation (EIO-706)
    ├── PluginValidator.java
    └── package-info.java
```

## Design Constraints

This package follows strict architectural rules:

- ✅ Interface only — no implementation
- ✅ Immutable models — final classes, final fields
- ✅ Constructor validation
- ✅ No business logic
- ✅ No persistence
- ✅ No events
- ✅ No threading
- ✅ No monitoring
- ✅ No validation
- ✅ Framework agnostic
- ✅ No Spring
- ✅ No Lombok
- ✅ No JPA

## Examples

### Register a Plugin

```java
PluginService service = ...; // Implementation provided by Platform Core
Plugin plugin = new Plugin("my-plugin");

boolean registered = service.register(plugin);
// Returns true if registered, false if already exists
```

### Check Plugin Status

```java
Plugin plugin = new Plugin("my-plugin");

if (service.exists(plugin)) {
    PluginDescriptor descriptor = service.get(plugin).get();
    PluginState state = descriptor.state();
    // Handle based on state
}
```

### List All Plugins

```java
Collection<PluginDescriptor> plugins = service.list();

for (PluginDescriptor descriptor : plugins) {
    System.out.println(descriptor.plugin().value() + " - " + descriptor.state());
}
```

### Unregister a Plugin

```java
Plugin plugin = new Plugin("my-plugin");

boolean unregistered = service.unregister(plugin);
// Returns true if unregistered, false if not found
```

## Constitutional Authority

- **ADD-PLT-301**: Plugin Framework public API definition
- **STD-003**: Platform Language standards
- **ADR-001**: Interface-first design

## Ownership

**Platform Core** — This package is owned and maintained by the Platform Core team. No external modifications without review.

## Version

1.0 — Initial API definition (EIO-701)