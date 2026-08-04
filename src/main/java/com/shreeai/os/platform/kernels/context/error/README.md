# Context Error Layer

## Overview

The Context Error Layer provides structured, immutable error reporting for the Context Kernel. It implements a platform-wide error pattern that ensures consistent error handling across all kernels.

## Constitutional Authority

- **EIO-CTX-104**: Context Error Architecture Implementation
- **EIO-ARCH-001**: Kernel Development Standard

## Error Philosophy

The Error Layer follows a strict principle: **report failures only—never validate, process, store, or coordinate**.

### What Errors Do

- Describe failures in a structured format
- Encapsulate metadata for debugging and audit
- Provide standardized error codes for consistent identification
- Maintain immutability for thread-safe error handling

### What Errors Never Do

- Perform validation
- Modify Context objects
- Access repositories or databases
- Perform persistence operations
- Invoke AI or business logic
- Perform networking or filesystem operations
- Publish events
- Create threads or schedule work

## Architecture Overview

```
┌─────────────────────────────────────────┐
│         ContextError                     │
│  - Immutable value object                │
│  - ContextErrorCode code                 │
│  - String message                        │
│  - Instant occurredAt                    │
│  - Map<String, Object> metadata          │
└──────────────┬──────────────────────────┘
               │
               │ encapsulated by
               │
               ▼
┌─────────────────────────────────────────┐
│      ContextException                     │
│  - Extends RuntimeException               │
│  - Contains one ContextError              │
│  - Provides error accessors               │
└──────────────┬──────────────────────────┘
               │
       ┌───────┼─────────────┐
       ▼       ▼             ▼
Validation  Lifecycle  Snapshot  NotFound
Exception   Exception  Exception  Exception
```

## Exception Hierarchy

### ContextException (Base)

Root exception class for all Context Kernel exceptions.

**Responsibilities:**
- Extends RuntimeException for unchecked exception handling
- Encapsulates one immutable ContextError
- Provides access to structured error information
- Never duplicates primitive error fields

**Methods:**
- `getError()` - Returns the encapsulated ContextError
- `getErrorCode()` - Returns the error code
- `getOccurredAt()` - Returns when the error occurred
- `getMetadata()` - Returns error metadata

### ContextValidationException

Thrown when Context validation fails.

**Usage:**
```java
ContextError error = new ContextError(
    ContextErrorCode.VALIDATION_FAILED,
    "Context validation failed: invalid state",
    Instant.now(),
    Map.of("contextId", contextId.getValue())
);

throw new ContextValidationException(error);
```

### ContextLifecycleException

Thrown when Context lifecycle operations fail.

**Usage:**
```java
ContextError error = new ContextError(
    ContextErrorCode.LIFECYCLE_FAILED,
    "Failed to transition context state",
    Instant.now(),
    Map.of("currentState", context.getState().name())
);

throw new ContextLifecycleException(error);
```

### ContextSnapshotException

Thrown when Context snapshot operations fail.

**Usage:**
```java
ContextError error = new ContextError(
    ContextErrorCode.SNAPSHOT_FAILED,
    "Failed to create context snapshot",
    Instant.now(),
    Map.of("contextId", contextId.getValue())
);

throw new ContextSnapshotException(error);
```

### ContextNotFoundException

Thrown when a Context is not found.

**Usage:**
```java
ContextError error = new ContextError(
    ContextErrorCode.CONTEXT_NOT_FOUND,
    "Context not found",
    Instant.now(),
    Map.of("contextId", contextId.getValue())
);

throw new ContextNotFoundException(error);
```

## ContextError Structure

The `ContextError` is an immutable value object containing:

- **code** (`ContextErrorCode`): Standardized error identifier
- **message** (`String`): Human-readable error description
- **occurredAt** (`Instant`): When the error occurred
- **metadata** (`Map<String, Object>`): Additional error metadata

### Example ContextError

```java
Map<String, Object> metadata = Map.of(
    "contextId", "ctx-123",
    "contextType", "CONVERSATION",
    "operation", "validate"
);

ContextError error = new ContextError(
    ContextErrorCode.INVALID_STATE,
    "Context state transition not allowed",
    Instant.now(),
    metadata
);
```

## ContextErrorCode Values

Standardized error identifiers for the Context Kernel:

- **CONTEXT_NOT_FOUND** - Requested context does not exist
- **INVALID_CONTEXT** - Context structure is invalid
- **INVALID_STATE** - Context state is invalid
- **INVALID_PRIORITY** - Context priority is invalid
- **INVALID_SCOPE** - Context scope is invalid
- **SNAPSHOT_FAILED** - Context snapshot operation failed
- **LIFECYCLE_FAILED** - Context lifecycle operation failed
- **VALIDATION_FAILED** - Context validation failed
- **UNKNOWN_ERROR** - Unknown or unspecified error

## Design Principles

### Immutability

All error objects are immutable:
- ContextError has final fields
- Collections are defensively copied
- Collections are wrapped with `Collections.unmodifiableMap`
- No setters or mutation methods exist

### No Primitive Scattering

Errors never duplicate primitive fields:
- ContextException encapsulates one ContextError
- All error information is accessed through the ContextError
- No redundant getters for primitive fields

### Standardized Codes

Error codes are standardized identifiers:
- Not user-facing messages
- Consistent across the platform
- Enables programmatic error handling
- Part of the platform-wide error pattern

### Exception Chaining

Exceptions support cause chaining:
```java
try {
    // Some operation
} catch (IOException e) {
    ContextError error = new ContextError(
        ContextErrorCode.LIFECYCLE_FAILED,
        "Failed to persist context",
        Instant.now(),
        Map.of()
    );
    throw new ContextLifecycleException(error, e);
}
```

## Usage Examples

### Basic Error Creation and Throwing

```java
import com.shreeai.os.platform.kernels.context.error.ContextError;
import com.shreeai.os.platform.kernels.context.error.ContextErrorCode;

// Create an error
ContextError error = new ContextError(
        ContextErrorCode.CONTEXT_NOT_FOUND,
        "Context not found: ctx-123",
        Instant.now(),
        Map.of("contextId", "ctx-123")
);

// Throw exception
throw new

        ContextNotFoundException(error);
```

### Catching and Handling Exceptions

```java
try {
    Context context = contextService.findById(contextId);
    // Process context
} catch (ContextNotFoundException e) {
    // Access structured error information
    ContextErrorCode errorCode = e.getErrorCode();
    Instant occurredAt = e.getOccurredAt();
    Map<String, Object> metadata = e.getMetadata();
    
    // Handle specific error
    if (errorCode == ContextErrorCode.CONTEXT_NOT_FOUND) {
        String contextId = (String) metadata.get("contextId");
        // Handle not found case
    }
}
```

### Error Handling by Type

```java
try {
    // Some Context operation
} catch (ContextValidationException e) {
    // Handle validation errors
    System.err.println("Validation failed: " + e.getError().getMessage());
} catch (ContextLifecycleException e) {
    // Handle lifecycle errors
    System.err.println("Lifecycle operation failed: " + e.getError().getMessage());
} catch (ContextSnapshotException e) {
    // Handle snapshot errors
    System.err.println("Snapshot failed: " + e.getError().getMessage());
} catch (ContextException e) {
    // Handle all other Context errors
    System.err.println("Context error: " + e.getError().getMessage());
}
```

### Preserving Exception Chains

```java
public void processContext(ContextId contextId) {
    try {
        Context context = loadContext(contextId);
        validateContext(context);
    } catch (ValidationException e) {
        ContextError error = new ContextError(
            ContextErrorCode.VALIDATION_FAILED,
            "Context validation failed",
            Instant.now(),
            Map.of("contextId", contextId.getValue())
        );
        throw new ContextValidationException(error, e);
    }
}
```

## Thread Safety

All error objects are immutable and thread-safe:

```java
// Error objects can be safely shared across threads
ContextError sharedError = new ContextError(
    ContextErrorCode.UNKNOWN_ERROR,
    "Shared error",
    Instant.now(),
    Map.of()
);

// Multiple threads can safely throw exceptions with the same error
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        throw new ContextException(sharedError);
    });
}
executor.shutdown();
```

## Integration Points

The Error Layer integrates with other Context Kernel components:

- **Validation Layer**: Returns ContextValidationResult, throws ContextValidationException
- **API Layer**: Catches exceptions and converts to error responses
- **Model Layer**: Never throws exceptions, only data structures
- **Future Service**: Catches and handles exceptions
- **Future Engine**: Uses exceptions for error propagation

## Best Practices

1. **Always create ContextError first**: Create the immutable error object before throwing
2. **Include relevant metadata**: Add context information to metadata map
3. **Use specific exception types**: Use the most specific exception type available
4. **Preserve exception chains**: Pass cause when wrapping exceptions
5. **Don't catch and ignore**: Always handle or propagate ContextException
6. **Don't modify errors**: ContextError is immutable—don't attempt to modify it
7. **Use error codes programmatically**: Check error codes, not messages

## Kernel Standard Compliance

All error classes comply with the Kernel Development Standard (EIO-ARCH-001):

✅ Immutable error model
✅ Standardized error codes
✅ Kernel-specific exception hierarchy
✅ Platform Language compliant
✅ No primitive error scattering
✅ No business logic
✅ No persistence
✅ No side effects
✅ No repository access
✅ No database access
✅ No event publishing
✅ No AI logic
✅ No networking
✅ No filesystem operations
✅ No reflection
✅ No mutable static state
✅ Defensive copying
✅ Comprehensive JavaDocs

## Package Structure

```
platform.kernels.context.error
├── ContextError.java
├── ContextErrorCode.java
├── ContextException.java
├── ContextValidationException.java
├── ContextLifecycleException.java
├── ContextSnapshotException.java
├── ContextNotFoundException.java
├── package-info.java
└── README.md
```

## Platform-Wide Consistency

This error pattern is consistent with other kernels:

- **Identity Kernel**: Uses the same ContextError pattern
- **Memory Kernel**: Uses the same ContextError pattern
- **Future Kernels**: Knowledge, Planning, Execution, Cognitive, Chief, Multi-Agent

This consistency ensures that every kernel exposes failures through the same architectural pattern, making the platform more maintainable and predictable.

## Version History

- **1.0** (EIO-CTX-104): Initial implementation of Context Error Layer

## Ownership

**Context Kernel** - This package is owned and maintained by the Context Kernel team.

## Constitutional Authority

- **EIO-CTX-104**: Context Error Architecture Implementation Specification
- **EIO-ARCH-001**: Kernel Development Standard