# Plugin Validator

## Purpose

The Plugin Validator is a stateless validation layer that ensures every Plugin model satisfies the architectural requirements before being used by the Plugin Framework. It answers the question: "Is this Plugin model valid?" — it never loads, executes, or modifies plugins.

## Architecture

```
PluginValidator (stateless)
    │
    ├── validatePluginId(PluginId) → ValidationResult
    ├── validatePlugin(Plugin) → ValidationResult
    ├── validateDescriptor(PluginDescriptor) → ValidationResult
    ├── validateRequest(PluginRequest) → ValidationResult
    └── validateState(PluginState) → ValidationResult
    │
    ▼
ValidationResult (from registry.validator)
```

## Responsibilities

### What the Validator DOES:

- Validates structural integrity of Plugin models
- Ensures non-null and non-blank constraints
- Returns structured ValidationResult with multiple errors
- Enforces architectural invariants from ADD-PLT-301

### What the Validator DOES NOT do:

- ❌ Load plugins
- ❌ Execute plugins
- ❌ Start or stop plugins
- ❌ Modify plugin state
- ❌ Access Registry, Lifecycle, Event Bus, Configuration
- ❌ Persist data
- ❌ Log events
- ❌ Spawn threads

## Validation Rules

### PluginId

| Rule | Description |
|------|-------------|
| Not null | PluginId must not be null |
| Value not blank | Value must not be null or blank |

### Plugin

| Rule | Description |
|------|-------------|
| Not null | Plugin must not be null |
| Id not null | Plugin id must not be null |
| Name not blank | Plugin name must not be null or blank |
| Version not blank | Plugin version must not be null or blank |

### PluginDescriptor

| Rule | Description |
|------|-------------|
| Not null | PluginDescriptor must not be null |
| Plugin not null | PluginDescriptor plugin must not be null |
| State not null | PluginDescriptor state must not be null |
| LoadedAt not null | PluginDescriptor loadedAt must not be null |
| Provider not blank | PluginDescriptor provider must not be null or blank |

### PluginRequest

| Rule | Description |
|------|-------------|
| Not null | PluginRequest must not be null |
| Plugin not null | PluginRequest plugin must not be null |

**Note:** The force flag is not validated as it is a boolean primitive.

### PluginState

| Rule | Description |
|------|-------------|
| Not null | PluginState must not be null |

## Characteristics

### Stateless

All validation state is passed as method parameters. The validator maintains no internal state.

```java
PluginValidator validator = new PluginValidator();
// No configuration, no initialization
// Can be used immediately
```

### Deterministic

Same inputs always produce the same result.

```java
PluginId id = new PluginId("test");
ValidationResult result1 = PluginValidator.validatePluginId(id);
ValidationResult result2 = PluginValidator.validatePluginId(id);
// result1.equals(result2) == true
```

### Thread-Safe

No mutable state means safe for concurrent use.

```java
// Multiple threads can use the same validator instance
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        PluginValidator.validatePluginId(new PluginId("thread-safe"));
    });
}
```

### Pure Validation

No side effects, no external access, no mutation.

```java
Plugin plugin = new Plugin(id, "Test", "1.0.0");
ValidationResult result = PluginValidator.validatePlugin(plugin);
// plugin is unchanged
// No external systems accessed
// No events published
```

## Usage Examples

### Validate PluginId

```java
PluginId id = new PluginId("my-plugin");
ValidationResult result = PluginValidator.validatePluginId(id);

if (result.isValid()) {
    // PluginId is valid
} else {
    // Handle validation errors
    for (String error : result.errors()) {
        System.err.println(error);
    }
}
```

### Validate Plugin

```java
PluginId id = new PluginId("my-plugin");
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");

ValidationResult result = PluginValidator.validatePlugin(plugin);

if (result.isValid()) {
    // Plugin is valid
} else {
    // Handle validation errors
    result.errors().forEach(System.err::println);
}
```

### Validate PluginDescriptor

```java
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");
PluginDescriptor descriptor = new PluginDescriptor(
    plugin,
    PluginState.STARTED,
    Instant.now(),
    "Platform Core"
);

ValidationResult result = PluginValidator.validateDescriptor(descriptor);

if (result.isValid()) {
    // Descriptor is valid
} else {
    // Handle validation errors
}
```

### Validate PluginRequest

```java
Plugin plugin = new Plugin(id, "My Plugin", "1.0.0");
PluginRequest request = new PluginRequest(plugin, true);

ValidationResult result = PluginValidator.validateRequest(request);

if (result.isValid()) {
    // Request is valid
}
```

### Validate PluginState

```java
PluginState state = PluginState.STARTED;
ValidationResult result = PluginValidator.validateState(state);

if (result.isValid()) {
    // State is valid (always true for non-null enum)
}
```

### Handle Multiple Validation Errors

```java
// Create invalid plugin
Plugin plugin = new Plugin(null, "", null);

ValidationResult result = PluginValidator.validatePlugin(plugin);

if (!result.isValid()) {
    // Multiple errors can be present
    System.out.println("Errors: " + result.errors().size());
    result.errors().forEach(error -> System.out.println("  - " + error));
}
```

## Integration with PluginService

The validator is used by the PluginService implementation to validate models before processing:

```java
public class DefaultPluginService implements PluginService {
    private final PluginValidator validator = new PluginValidator();

    @Override
    public boolean register(Plugin plugin) {
        // Validate plugin first
        ValidationResult result = validator.validatePlugin(plugin);
        if (!result.isValid()) {
            throw new InvalidPluginException(result.errors());
        }

        // Proceed with registration
        // ...
    }
}
```

## Design Constraints

This package follows strict architectural rules:

- ✅ Stateless — no internal state
- ✅ Deterministic — same inputs = same outputs
- ✅ Thread-safe — no mutable state
- ✅ Pure validation — no side effects
- ✅ No business logic — validation rules only
- ✅ No persistence — no database operations
- ✅ No services — no business logic
- ✅ No plugin loading — never loads plugins
- ✅ No lifecycle management — never starts/stops plugins
- ✅ No events — no event publishing
- ✅ No threading — no thread creation
- ✅ No monitoring — no metrics or logging
- ✅ Framework agnostic — no Spring, no Lombok, no JPA

## Constitutional Authority

- **ADD-PLT-301**: Plugin Framework definition
- **STD-003**: Platform Language standards
- **ADR-001**: Interface-first design

## Ownership

**Platform Core** — This package is owned and maintained by the Platform Core team. No external modifications without review.

## Version

1.0 — Initial validator definition (EIO-703)