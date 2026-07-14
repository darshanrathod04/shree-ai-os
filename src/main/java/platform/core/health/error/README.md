# Health Error Architecture

## Architecture

```
platform.core.health.error
├── HealthErrorCode.java              — Error code enum
├── HealthError.java                  — Immutable error model
├── HealthException.java              — Base runtime exception
├── HealthComponentNotFoundException.java — Component not found
├── HealthCheckFailedException.java   — Check failed
├── InvalidHealthComponentException.java — Invalid component
├── package-info.java
└── README.md
```

## Architectural Responsibility

The Health Error Architecture provides **structured error handling** for the Health subsystem. It follows the exact pattern established by Registry, Discovery, Lifecycle, Event Bus, and Configuration modules.

### What the Error Architecture Does

- ✅ Defines error codes for all Health error conditions
- ✅ Provides immutable error model (HealthError)
- ✅ Provides base exception (HealthException)
- ✅ Provides concrete exceptions for specific error scenarios
- ✅ Enables consistent error handling across Health subsystem
- ✅ Wraps structured error information in exceptions

### What the Error Architecture Does NOT Do

- ❌ Monitor health (that's the Health Engine's job)
- ❌ Evaluate health status
- ❌ Execute health checks
- ❌ Validate models (that's the HealthValidator's job)
- ❌ Access Registry, Lifecycle, Event Bus, Configuration
- ❌ Access databases or external services
- ❌ Create threads or schedule tasks
- ❌ Publish events
- ❌ Contain business logic

## Error Flow

```
Health Operation
    │
    ▼
Error Occurs
    │
    ▼
HealthError (immutable model)
    │
    ▼
HealthException (runtime exception)
    │
    ▼
Caller Handles Exception
```

## Error Codes

### HEALTH_COMPONENT_NOT_FOUND
Thrown when a requested health component is not found in the Health registry.

**Exception:** `HealthComponentNotFoundException`

**Example:**
```java
throw new HealthComponentNotFoundException(component);
```

### HEALTH_CHECK_FAILED
Thrown when a health check fails to complete successfully.

**Exception:** `HealthCheckFailedException`

**Example:**
```java
throw new HealthCheckFailedException(component, "Connection timeout");
```

### HEALTH_INVALID_COMPONENT
Thrown when a health component fails validation.

**Exception:** `InvalidHealthComponentException`

**Example:**
```java
throw new InvalidHealthComponentException(component, "Name is blank");
```

### HEALTH_VALIDATION_FAILED
Thrown when health validation fails.

**Exception:** `HealthException` (with HEALTH_VALIDATION_FAILED code)

**Example:**
```java
throw new HealthException(HealthError.builder()
    .code(HealthErrorCode.HEALTH_VALIDATION_FAILED)
    .message("Validation failed")
    .timestamp(Instant.now())
    .details(Map.of("field", "name"))
    .build());
```

### HEALTH_ALREADY_REGISTERED
Thrown when attempting to register a component that is already registered.

**Exception:** `HealthException` (with HEALTH_ALREADY_REGISTERED code)

### HEALTH_NOT_REGISTERED
Thrown when attempting to operate on a component that is not registered.

**Exception:** `HealthException` (with HEALTH_NOT_REGISTERED code)

### HEALTH_ENGINE_FAILURE
Thrown when the health engine encounters a failure.

**Exception:** `HealthException` (with HEALTH_ENGINE_FAILURE code)

## Exception Hierarchy

```
HealthException (base runtime exception)
│
├── HealthComponentNotFoundException
│   └── Thrown when component not found
│
├── HealthCheckFailedException
│   └── Thrown when health check fails
│
└── InvalidHealthComponentException
    └── Thrown when component is invalid
```

## Error Model

### HealthError

Immutable error model with the following fields:

| Field | Type | Description |
|-------|------|-------------|
| code | HealthErrorCode | Error code enum |
| message | String | Human-readable error message |
| timestamp | Instant | When the error occurred |
| details | Map<String, Object> | Additional error details (unmodifiable) |

**Example:**
```java
HealthError error = new HealthError(
    HealthErrorCode.HEALTH_CHECK_FAILED,
    "Health check failed for component: Event Bus - Connection timeout",
    Instant.now(),
    Map.of(
        "componentId", "event-bus",
        "componentName", "Event Bus",
        "componentCategory", "Infrastructure",
        "reason", "Connection timeout"
    )
);
```

## Usage Examples

### Example 1: Throwing HealthComponentNotFoundException

```java
public Optional<HealthReport> check(HealthComponent component) {
    if (!registeredComponents.containsKey(component.id())) {
        throw new HealthComponentNotFoundException(component);
    }
    // Perform health check...
}
```

### Example 2: Throwing HealthCheckFailedException

```java
public HealthReport performCheck(HealthComponent component) {
    try {
        // Perform health check...
    } catch (Exception e) {
        throw new HealthCheckFailedException(component, e.getMessage());
    }
}
```

### Example 3: Throwing InvalidHealthComponentException

```java
public void register(HealthComponent component) {
    ValidationResult result = HealthValidator.validateComponent(component);
    if (!result.isValid()) {
        throw new InvalidHealthComponentException(component, 
            String.join(", ", result.errors()));
    }
    // Register component...
}
```

### Example 4: Catching Health Exceptions

```java
try {
    HealthReport report = healthService.check(component);
} catch (HealthComponentNotFoundException e) {
    log.error("Component not found: {}", e.code());
    // Handle not found
} catch (HealthCheckFailedException e) {
    log.error("Check failed: {}", e.getMessage());
    // Handle check failure
} catch (HealthException e) {
    log.error("Health error: {} - {}", e.code(), e.getMessage());
    // Handle other health errors
}
```

### Example 5: Inspecting Error Details

```java
try {
    healthService.check(component);
} catch (HealthException e) {
    HealthError error = e.error();
    log.error("Error code: {}", error.code());
    log.error("Message: {}", error.message());
    log.error("Timestamp: {}", error.timestamp());
    log.error("Details: {}", error.details());
    
    // Access specific details
    String componentId = (String) error.details().get("componentId");
    String reason = (String) error.details().get("reason");
}
```

## Design Principles

1. **Immutable** — All error models are immutable value objects
2. **Structured** — Errors contain code, message, timestamp, and details
3. **Consistent** — Follows the same pattern as other Platform Core modules
4. **Runtime Exceptions** — All exceptions extend RuntimeException
5. **No Business Logic** — Error definitions only
6. **No Monitoring** — Errors only, no health evaluation
7. **Framework-Agnostic** — No Spring or other framework dependencies

## Constitutional Authority

- ADD-PLT-202: Platform Language immutability requirements
- ADD-PLT-205: Domain model package structure
- ADD-PLT-206: Platform Language standards

## Ownership

**Platform Core** — Health Error Architecture is owned and maintained by the Platform Core team.

## Version

1.0