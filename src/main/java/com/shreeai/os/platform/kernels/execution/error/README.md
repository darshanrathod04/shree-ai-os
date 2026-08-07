# Execution Error Layer

## Overview

The Execution Error Layer provides a canonical, immutable representation of execution failures and a consistent exception hierarchy for the Execution Kernel. It classifies failures but never attempts to recover from them.

## Error Philosophy

The Error Layer exists solely to answer one question:

> **"What failed?"**

It does **not** answer:
- "How should the failure be handled?"
- "Should the execution be retried?"
- "What recovery strategy should be applied?"

Those responsibilities belong to later architectural layers:
- **Service Layer (EXEC-105)**: orchestrates requests, delegates validation, and translates failures
- **Engine Layer (EXEC-106)**: performs deterministic execution processing
- **Verification Layer (EXEC-107)**: certifies architectural compliance

## Error Architecture

```
RuntimeException
       │
       ▼
ExecutionException
       │
┌──────┼──────────────┬────────────────────┬────────────────────┐
▼      ▼              ▼                    ▼                    ▼
ActionExecution   WorkflowExecution   TaskExecution      Recovery
Exception         Exception           Exception          Exception
       │
       ▼
ExecutionValidationException
```

## Exception Hierarchy

### ExecutionException

**Base runtime exception for the Execution Kernel.**

Responsibilities:
- Encapsulates immutable ExecutionError
- Provides base exception for all Execution Kernel failures
- Maintains consistent exception behavior
- Exposes error code, message, and metadata

Design:
- Extends RuntimeException
- Immutable after construction
- Constructor validation
- No mutable state

### ActionExecutionException

Represents failures associated with action execution.

**Use for:**
- Action execution failures
- Action state transition failures
- Action-specific errors

### WorkflowExecutionException

Represents failures associated with workflow execution.

**Use for:**
- Workflow execution failures
- Workflow orchestration failures
- Workflow-specific errors

### TaskExecutionException

Represents failures associated with planned task execution.

**Use for:**
- Task execution failures
- Task prerequisite failures
- Task-specific errors

### RecoveryException

Represents failures associated with recovery configuration or recovery requests.

**Use for:**
- Recovery configuration failures
- Recovery request failures
- Recovery-specific errors

### ExecutionValidationException

Represents structural validation failures originating from the Validation Layer.

**Use for:**
- Validation failures
- Structural integrity failures
- Validation-specific errors

## ExecutionErrorCode

Strongly typed enumeration representing Execution-specific failures.

**Categories:**
- **Invalid Request/State**: INVALID_EXECUTION_REQUEST, INVALID_EXECUTION_STATE
- **Invalid Components**: INVALID_ACTION, INVALID_WORKFLOW, INVALID_TASK, INVALID_RECOVERY_CONFIGURATION
- **Invalid Context/Options**: INVALID_EXECUTION_CONTEXT, INVALID_EXECUTION_OPTIONS, INVALID_EXECUTION_METRICS
- **Validation**: VALIDATION_FAILURE
- **Execution**: EXECUTION_FAILURE, ACTION_EXECUTION_FAILED, WORKFLOW_EXECUTION_FAILED, TASK_EXECUTION_FAILED
- **Recovery**: RECOVERY_FAILED
- **Control**: EXECUTION_CANCELLED, EXECUTION_TIMEOUT
- **Resources**: RESOURCE_UNAVAILABLE, DEPENDENCY_NOT_SATISFIED
- **General**: UNKNOWN_ERROR

## ExecutionError

Immutable value object representing an execution failure.

**Fields:**
- `ExecutionErrorCode errorCode` — the error code
- `String message` — the error message
- `Instant occurredAt` — when the error occurred
- `Map<String, Object> metadata` — additional error metadata

**Properties:**
- Final class
- Final fields
- Constructor validation
- Defensive copying
- Unmodifiable collections
- No setters
- Value semantics
- equals(), hashCode(), toString()

## Architectural Responsibilities

### Responsible For

The Error Layer is responsible for:
- Failure classification
- Immutable failure representation
- Exception hierarchy
- Propagation of execution failures

### Not Responsible For

The Error Layer is **not** responsible for:
- Retry logic
- Rollback
- Compensation
- Workflow continuation
- Execution algorithms
- Recovery strategies
- Persistence
- Networking
- Error handling decisions

## Design Principles

### Immutability

All errors and exceptions are immutable:
- ExecutionError has final fields
- ExecutionError collections are defensively copied
- ExecutionError collections are wrapped as unmodifiable
- Exceptions encapsulate immutable ExecutionError
- No mutation after construction

### Constructor Validation

All constructors validate:
- Null arguments
- Required fields
- Structural invariants

### Defensive Copying

All mutable collections are:
- Copied on construction
- Wrapped with Collections.unmodifiable*()
- Protected from external modification

### Value Semantics

ExecutionError implements:
- equals() — based on all fields
- hashCode() — based on all fields
- toString() — human-readable representation

## Separation from Other Layers

The Error Layer is intentionally separated from other Execution Kernel layers:

```
API
 ↓
Model
 ↓
Validation
 ↓
Error  ← You are here
 ↓
Service
 ↓
Engine
 ↓
Verification
```

This separation ensures:
- Error classification remains pure
- No side effects in error handling
- Clear architectural boundaries
- Independent evolution of error and execution logic

## Usage Example

```java
// Create an error
ExecutionError error = new ExecutionError(
    ExecutionErrorCode.ACTION_EXECUTION_FAILED,
    "Action execution failed due to invalid state",
    Instant.now(),
    Map.of("actionId", "action-123", "state", "FAILED")
);

// Throw specialized exception
try {
    // Some execution logic
    throw new ActionExecutionException(error);
} catch (ActionExecutionException e) {
    // Access error details
    ExecutionErrorCode code = e.errorCode();
    String message = e.getMessage();
    Instant occurredAt = e.occurredAt();
    Map<String, Object> metadata = e.metadata();
}

// Throw with cause
try {
    // Some execution logic
    throw new WorkflowExecutionException(error, cause);
} catch (WorkflowExecutionException e) {
    // Handle workflow failure
    Throwable cause = e.getCause();
}
```

## Exception Design Rules

Every exception shall:
- Encapsulate an immutable ExecutionError
- Perform constructor validation
- Expose no mutable state
- Remain immutable after creation

Exceptions must not:
- Modify the underlying error
- Trigger recovery
- Perform retry
- Execute workflows
- Invoke services
- Invoke engines

## Thread Safety

All errors and exceptions are thread-safe because:
- ExecutionError is immutable
- No mutable state in exceptions
- All fields are final
- Defensive copying of collections
- Safe for concurrent access

## Future Extensibility

The error architecture supports future extensibility through:
- **New error codes**: Add error codes for new failure scenarios
- **New exception types**: Add specialized exceptions for new domains
- **Metadata enrichment**: Add error metadata without breaking changes
- **Error handlers**: Service layer can implement error handling strategies

## Compliance

This implementation complies with:
- **Kernel Development Standard (EIO-ARCH-001)**
- **Execution Kernel Architecture (EIO-EXEC-104)**

## Package Structure

```
platform.kernels.execution.error
├── ExecutionErrorCode.java              # Error code enumeration
├── ExecutionError.java                  # Immutable error value object
├── ExecutionException.java              # Base runtime exception
├── ActionExecutionException.java        # Action execution failures
├── WorkflowExecutionException.java      # Workflow execution failures
├── TaskExecutionException.java          # Task execution failures
├── RecoveryException.java               # Recovery failures
├── ExecutionValidationException.java    # Validation failures
├── package-info.java                    # Package documentation
└── README.md                            # This file
```

## Best Practices

### Creating Errors

```java
// Create error with metadata
ExecutionError error = new ExecutionError(
    ExecutionErrorCode.EXECUTION_FAILED,
    "Execution failed: " + reason,
    Instant.now(),
    Map.of(
        "executionId", executionId,
        "actionId", actionId,
        "durationMs", duration
    )
);
```

### Throwing Exceptions

```java
// Throw with error
throw new ExecutionException(error);

// Throw specialized exception
throw new ActionExecutionException(error);

// Throw with cause
throw new WorkflowExecutionException(error, cause);
```

### Catching Exceptions

```java
try {
    // Execution logic
} catch (ExecutionException e) {
    // Access error details
    ExecutionError error = e.error();
    ExecutionErrorCode code = e.errorCode();
    
    // Handle based on error code
    switch (code) {
        case ACTION_EXECUTION_FAILED:
            // Handle action failure
            break;
        case WORKFLOW_EXECUTION_FAILED:
            // Handle workflow failure
            break;
        default:
            // Handle other failures
    }
}
```

## Version History

- **1.0** (2026-07-20): Initial implementation per EIO-EXEC-104