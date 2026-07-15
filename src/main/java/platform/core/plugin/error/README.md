# Plugin Error

## Purpose

The Plugin Error package provides the error handling architecture for the Plugin Framework within Shree AI OS. It defines error codes, immutable error models, and a complete exception hierarchy for consistent error reporting and handling.

## Architecture

```
PluginErrorCode (enum)
    │
    ▼
PluginError (immutable model)
    │
    ▼
PluginException (base runtime exception)
    │
    ├── DuplicatePluginException
    ├── PluginNotFoundException
    └── InvalidPluginException
```

## Error Hierarchy

### PluginErrorCode

Enumeration of all possible Plugin subsystem error conditions.

| Code | Description |
|------|-------------|
| `PLUGIN_DUPLICATE` | A plugin with the same identifier is already registered |
| `PLUGIN_NOT_FOUND` | The requested plugin was not found |
| `PLUGIN_INVALID` | The plugin is invalid |
| `PLUGIN_VALIDATION_FAILED` | Plugin validation failed |
| `PLUGIN_ALREADY_STARTED` | The plugin is already started |
| `PLUGIN_ALREADY_STOPPED` | The plugin is already stopped |
| `PLUGIN_LIFECYCLE_FAILED` | The plugin lifecycle operation failed |

### PluginError

Immutable error model that encapsulates error information.

**Fields:**
- `PluginErrorCode code` — the error code
- `String message` — human-readable error message
- `Instant timestamp` — when the error occurred
- `Map<String, Object> details` — additional error context

**Methods:**
- `PluginErrorCode code()` — returns the error code
- `String message()` — returns the error message
- `Instant timestamp()` — returns the error timestamp
- `Map<String, Object> details()` — returns the error details (unmodifiable)

### PluginException

Base runtime exception for all Plugin errors.

**Methods:**
- `PluginError error()` — returns the associated PluginError
- `PluginErrorCode code()` — returns the error code
- `String getMessage()` — returns the error message

### Concrete Exceptions

#### DuplicatePluginException

Thrown when attempting to register a plugin that is already registered.

**Error Code:** `PLUGIN_DUPLICATE`

**Constructors:**
```java
DuplicatePluginException(Plugin plugin)
DuplicatePluginException(Plugin plugin, Map<String, Object> details)
```

**Error Details:**
- `pluginId` — the plugin identifier
- `pluginName` — the plugin name
- `pluginVersion` — the plugin version

#### PluginNotFoundException

Thrown when a requested plugin is not found in the registry.

**Error Code:** `PLUGIN_NOT_FOUND`

**Constructors:**
```java
PluginNotFoundException(Plugin plugin)
PluginNotFoundException(Plugin plugin, Map<String, Object> details)
```

**Error Details:**
- `pluginId` — the plugin identifier
- `pluginName` — the plugin name
- `pluginVersion` — the plugin version

#### InvalidPluginException

Thrown when a plugin fails validation.

**Error Code:** `PLUGIN_INVALID`

**Constructors:**
```java
InvalidPluginException(Plugin plugin, String reason)
InvalidPluginException(Plugin plugin, String reason, Map<String, Object> details)
```

**Error Details:**
- `pluginId` — the plugin identifier
- `pluginName` — the plugin name
- `pluginVersion` — the plugin version
- `reason` — the validation failure reason

## Usage Examples

### Throwing DuplicatePluginException

```java
public class DefaultPluginService implements PluginService {
    @Override
    public boolean register(Plugin plugin) {
        if (registry.containsKey(plugin.id())) {
            throw new DuplicatePluginException(plugin);
        }
        registry.put(plugin.id(), plugin);
        return true;
    }
}
```

### Throwing PluginNotFoundException

```java
@Override
public Optional<PluginDescriptor> get(Plugin plugin) {
    PluginDescriptor descriptor = registry.get(plugin.id());
    if (descriptor == null) {
        throw new PluginNotFoundException(plugin);
    }
    return Optional.of(descriptor);
}
```

### Throwing InvalidPluginException

```java
@Override
public boolean register(Plugin plugin) {
    ValidationResult result = validator.validatePlugin(plugin);
    if (!result.isValid()) {
        throw new InvalidPluginException(plugin, "Validation failed: " + result.errors());
    }
    // Proceed with registration
    return true;
}
```

### Catching PluginException

```java
try {
    service.register(plugin);
} catch (DuplicatePluginException e) {
    System.err.println("Plugin already registered: " + e.code());
    System.err.println("Details: " + e.error().details());
} catch (PluginNotFoundException e) {
    System.err.println("Plugin not found: " + e.code());
} catch (PluginException e) {
    System.err.println("Plugin error: " + e.getMessage());
    System.err.println("Error code: " + e.code());
}
```

### Accessing Error Details

```java
try {
    service.register(plugin);
} catch (PluginException e) {
    PluginError error = e.error();
    
    System.err.println("Code: " + error.code());
    System.err.println("Message: " + error.message());
    System.err.println("Timestamp: " + error.timestamp());
    System.err.println("Details: " + error.details());
    
    // Access specific details
    String pluginId = (String) error.details().get("pluginId");
    String pluginName = (String) error.details().get("pluginName");
}
```

## Design Principles

### Immutability

All error models are immutable:

```java
PluginError error = new PluginError(
    PluginErrorCode.PLUGIN_DUPLICATE,
    "Plugin already registered",
    Instant.now(),
    Map.of("pluginId", "my-plugin")
);

// error.details() returns unmodifiable map
// error.code() returns enum (immutable)
// error.message() returns string (immutable)
// error.timestamp() returns Instant (immutable)
```

### Structured Error Information

Every exception wraps a PluginError with structured data:

```java
catch (PluginException e) {
    // Access structured error information
    PluginErrorCode code = e.code();
    String message = e.getMessage();
    PluginError error = e.error();
    Map<String, Object> details = error.details();
    
    // Log or handle based on error code
    if (e.code() == PluginErrorCode.PLUGIN_DUPLICATE) {
        // Handle duplicate registration
    }
}
```

### No Business Logic

Error classes contain no business logic:

```java
// ❌ WRONG - error classes should not contain business logic
public class DuplicatePluginException extends PluginException {
    public boolean shouldRetry() {
        return false; // Business logic!
    }
}

// ✅ CORRECT - error classes only provide error information
public class DuplicatePluginException extends PluginException {
    // No business logic, only error data
}
```

## Future Extension Points

### Adding New Error Codes

To add a new error code:

1. Add the enum value to `PluginErrorCode`
2. Optionally create a concrete exception class
3. Document the error code in this README

### Adding New Concrete Exceptions

To add a new concrete exception:

1. Create a new class extending `PluginException`
2. Implement constructors that create appropriate `PluginError`
3. Add error details specific to the exception type
4. Document the exception in this README

## Integration with Validator

The validator returns `ValidationResult`, not exceptions. Exceptions are thrown by the service layer:

```java
// Validator - returns ValidationResult
ValidationResult result = validator.validatePlugin(plugin);

// Service - throws exceptions based on validation
if (!result.isValid()) {
    throw new InvalidPluginException(plugin, "Validation failed");
}
```

## Constitutional Authority

- **ADD-PLT-301**: Plugin Framework definition
- **STD-003**: Platform Language standards
- **ADR-001**: Interface-first design

## Ownership

**Platform Core** — This package is owned and maintained by the Platform Core team. No external modifications without review.

## Version

1.0 — Initial error architecture definition (EIO-704)