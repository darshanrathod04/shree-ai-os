# Plugin Service

## Purpose

The Plugin Service package provides the default in-memory implementation of the PluginService contract within Shree AI OS.

## Architecture

```
DefaultPluginService (implements PluginService)
    │
    ├── register(Plugin) → validate → store → return true
    ├── get(Plugin) → validate → lookup → Optional<PluginDescriptor>
    ├── list() → unmodifiable collection
    ├── unregister(Plugin) → validate → remove → boolean
    └── exists(Plugin) → lookup → boolean
```

## Storage

- **ConcurrentHashMap<PluginId, PluginDescriptor>** — Thread-safe in-memory storage
- No persistence, no database, no filesystem

## Dependency Graph

```
DefaultPluginService
    ├── PluginValidator (injected)
    └── PluginLifecycleEngine (injected)
```

## Examples

### Creating a Service

```java
PluginValidator validator = new PluginValidator();
PluginLifecycleEngine engine = new PluginLifecycleEngine();
PluginService service = new DefaultPluginService(validator, engine);
```

### Registering a Plugin

```java
PluginId id = new PluginId("my-plugin");
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");
boolean registered = service.register(plugin);
```

### Retrieving a Plugin

```java
Optional<PluginDescriptor> descriptor = service.get(plugin);
if (descriptor.isPresent()) {
    System.out.println(descriptor.get().state());
}
```

## Design Constraints

- ✅ No Spring
- ✅ No Lombok
- ✅ No JPA
- ✅ No filesystem
- ✅ No plugin loading
- ✅ No threads (uses ConcurrentHashMap)
- ✅ No events
- ✅ No scheduler

## Ownership

**Platform Core**

## Version

1.0 — EIO-705