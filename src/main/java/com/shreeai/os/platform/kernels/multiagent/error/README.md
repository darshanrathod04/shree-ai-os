# Multi-Agent Kernel — Error Layer

## Package Purpose

This package contains the canonical exception hierarchy and immutable error model for the Multi-Agent Kernel of Shree AI OS. The Error Layer provides a consistent representation of failures across registration, discovery, capabilities, lifecycle, communication, and validation.

## Exception Hierarchy

```
MultiAgentException (base runtime exception)
├── AgentRegistrationException
├── AgentDiscoveryException
├── CapabilityException
├── LifecycleException
├── CommunicationException
└── MultiAgentValidationException
```

This hierarchy is canonical and fixed. Do not modify it.

## Components

### MultiAgentErrorCode

Canonical enumeration of error codes:

- `REGISTRATION_ERROR` — Agent registration errors
- `DISCOVERY_ERROR` — Agent discovery errors
- `CAPABILITY_ERROR` — Capability management errors
- `LIFECYCLE_ERROR` — Agent lifecycle errors
- `COMMUNICATION_ERROR` — Agent communication errors
- `VALIDATION_ERROR` — Validation failures
- `MULTI_AGENT_ERROR` — General Multi-Agent Kernel errors

### MultiAgentError

Immutable value object representing an error:

- `errorCode` — The error category (MultiAgentErrorCode)
- `message` — Human-readable error message
- `agentId` — Associated agent identifier (optional)
- `timestamp` — When the error occurred
- `details` — Additional error context (immutable map)

### MultiAgentException

Base runtime exception that wraps a MultiAgentError:

- Wraps immutable MultiAgentError
- Exposes error information through accessor methods
- No recovery logic
- Base class for all specialized exceptions

### Specialized Exceptions

Each specialized exception extends MultiAgentException and is associated with a specific error code:

- **AgentRegistrationException** — REGISTRATION_ERROR
- **AgentDiscoveryException** — DISCOVERY_ERROR
- **CapabilityException** — CAPABILITY_ERROR
- **LifecycleException** — LIFECYCLE_ERROR
- **CommunicationException** — COMMUNICATION_ERROR
- **MultiAgentValidationException** — VALIDATION_ERROR

## Immutable Error Model

All error objects are immutable value objects:

```java
public final class MultiAgentError {
    private final MultiAgentErrorCode errorCode;
    private final String message;
    private final String agentId;
    private final Instant timestamp;
    private final Map<String, Object> details;
    
    // No setters
    // No mutation methods
    // Defensive copying with Map.copyOf()
}
```

### Constructor Validation

All constructors validate arguments:

```java
public MultiAgentError(
        MultiAgentErrorCode errorCode,
        String message,
        String agentId,
        Instant timestamp,
        Map<String, Object> details) {
    this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    this.message = validateMessage(message);
    this.agentId = agentId;
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    this.details = Map.copyOf(Objects.requireNonNull(details, "details must not be null"));
}
```

### Value Semantics

MultiAgentError implements value semantics:

```java
@Override
public boolean equals(Object obj) {
    // Value-based equality
}

@Override
public int hashCode() {
    // Consistent with equals
}

@Override
public String toString() {
    // Human-readable representation
}
```

## Usage Examples

### Creating an Error

```java
// Create error details
Map<String, Object> details = Map.of(
    "reason", "Agent already registered",
    "attemptedAt", Instant.now()
);

// Create error
MultiAgentError error = new MultiAgentError(
    MultiAgentErrorCode.REGISTRATION_ERROR,
    "Agent registration failed",
    "agent-123",
    Instant.now(),
    details
);
```

### Throwing an Exception

```java
// Throw registration exception
throw new AgentRegistrationException(error);

// Throw with cause
throw new AgentRegistrationException(error, cause);
```

### Catching Exceptions

```java
try {
    // Registration operation
    registerAgent(request);
} catch (AgentRegistrationException e) {
    // Access error information
    MultiAgentError error = e.error();
    MultiAgentErrorCode code = e.errorCode();
    String message = e.getMessage();
    String agentId = e.agentId();
    Instant timestamp = e.timestamp();
    Map<String, Object> details = e.details();
    
    // Handle error
    System.err.println("Registration error: " + message);
}
```

### Catching by Base Exception

```java
try {
    // Multi-Agent operation
} catch (MultiAgentException e) {
    // Handle any Multi-Agent error
    MultiAgentError error = e.error();
    
    // Check error code
    switch (e.errorCode()) {
        case REGISTRATION_ERROR:
            // Handle registration error
            break;
        case DISCOVERY_ERROR:
            // Handle discovery error
            break;
        // ... other cases
    }
}
```

### Creating Validation Exception

```java
// Create validation error
MultiAgentError validationError = new MultiAgentError(
    MultiAgentErrorCode.VALIDATION_ERROR,
    "Agent request validation failed",
    null,  // No specific agent
    Instant.now(),
    Map.of("validationIssues", List.of("agentId is null", "agentType is blank"))
);

// Throw validation exception
throw new MultiAgentValidationException(validationError);
```

## Architectural Boundaries

### What Error Layer Can Do

✓ Represent failures as immutable objects
✓ Categorize errors with error codes
✓ Throw typed exceptions
✓ Expose error information

### What Error Layer Cannot Do

✗ Implement retry logic
✗ Implement recovery logic
✗ Perform routing
✗ Perform scheduling
✗ Register agents
✗ Discover agents
✗ Manage lifecycle
✗ Execute agents
✗ Maintain mutable state

## Error Flow

```
Error Occurs
    │
    ▼
Create MultiAgentError (immutable)
    │
    ▼
Wrap in MultiAgentException (or subclass)
    │
    ▼
Throw Exception
    │
    ▼
Catch and Handle
```

## Architectural Position

The Error Layer sits between the Validation Layer and the Service Layer:

```
Applications
    │
    ▼
Multi-Agent API
    │
    ▼
Domain Models
    │
    ▼
Validation Layer
    │
    ▼
Error Layer
    │
    ▼
Future Service Layer
    │
    ▼
Future Processing Engine
```

The Error Layer represents failures only. It never performs processing.

## Dependencies

### Allowed

```
platform.kernels.multiagent.model.*
java.util.*
java.time.*
```

### Forbidden

```
service
validation
engine
runtime
repository
database
network
memory
planning
knowledge
reasoning
framework annotations
```

## Code Quality

All error classes follow:
- **SOLID** principles
- **Clean Code** practices
- **Immutability** — All fields are final
- **Defensive Programming** — Constructor validation, null checks
- **Value Object Pattern** — equals, hashCode, toString

## Thread Safety

All error objects are thread-safe because they are:
- Immutable (no modification methods)
- Final classes (cannot be extended)
- Thread-safe value objects

## Constitutional Authority

This package is defined by:
- **MAGENT-104** — Multi-Agent Error Architecture (this document)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Related Documentation

- **MAGENT-101** — Multi-Agent Public API
- **MAGENT-102** — Multi-Agent Domain Models
- **MAGENT-103** — Multi-Agent Validation Layer
- **MAGENT-105** — Default Multi-Agent Service (next)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Version History

- **1.0** — Initial error architecture (MAGENT-104)