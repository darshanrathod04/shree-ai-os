# Chief Kernel Error Layer

## Overview

The Chief Kernel Error Layer provides a unified, immutable representation of orchestration failures across the Chief Kernel. The Error Layer represents failures consistently but does not resolve them.

## Error Philosophy

The Error Layer exists solely to answer one question:

> **"What went wrong during orchestration?"**

It does **not** answer:
- "How should this failure be resolved?"
- "Should this operation be retried?"
- "What is the recovery strategy?"

Those responsibilities belong to other architectural layers:
- **Service Layer (CHIEF-105)**: error handling and recovery orchestration
- **Engine Layer (CHIEF-106)**: deterministic error computation

## Exception Hierarchy

```
RuntimeException
       │
       ▼
ChiefException
├────────────── DecisionException
├────────────── GoalManagementException
├────────────── TaskDelegationException
├────────────── KernelCoordinationException
└────────────── ChiefValidationException
```

## Components

### ChiefErrorCode

**Canonical error identifiers enum.**

Error codes:
- `DECISION_ERROR` — Decision orchestration errors
- `GOAL_MANAGEMENT_ERROR` — Goal management errors
- `TASK_DELEGATION_ERROR` — Task delegation errors
- `KERNEL_COORDINATION_ERROR` — Kernel coordination errors
- `VALIDATION_ERROR` — Validation errors
- `MONITORING_ERROR` — Monitoring errors
- `ORCHESTRATION_ERROR` — General orchestration errors

Properties:
- Immutable enum
- Unique values
- Stable names
- No business logic

### ChiefError

**Immutable error value object.**

Fields:
- `ChiefErrorCode code` — the error code
- `String message` — the error message
- `String component` — the component where error occurred
- `String operation` — the operation that failed
- `Map<String, Object> metadata` — additional error metadata
- `Instant timestamp` — when the error occurred

Properties:
- Final class
- Final fields
- Constructor validation
- Defensive copying
- Unmodifiable collections
- No setters
- Value semantics
- equals(), hashCode(), toString()

### ChiefException

**Canonical base exception.**

Responsibilities:
- Wraps ChiefError for exception handling
- Provides base exception for all Chief Kernel exceptions
- Extends RuntimeException

Constructors:
- `ChiefException(ChiefError error)`
- `ChiefException(ChiefError error, Throwable cause)`

Properties:
- Immutable ChiefError
- No setters
- Getter for ChiefError

### DecisionException

**Decision-related failures.**

Represents failures related to:
- Decision context
- Decision execution
- Decision validation

Extends ChiefException. No additional behavior.

### GoalManagementException

**Goal management failures.**

Represents failures related to:
- Goal lifecycle
- Goal metadata
- Goal state

Extends ChiefException. No additional behavior.

### TaskDelegationException

**Task delegation failures.**

Represents failures related to:
- Delegation
- Routing
- Assignment

Extends ChiefException. No additional behavior.

### KernelCoordinationException

**Kernel coordination failures.**

Represents failures related to:
- Orchestration
- Coordination
- Dependency graph

Extends ChiefException. No additional behavior.

### ChiefValidationException

**Validation failures.**

Represents validation failures produced by the Validation Layer.

Extends ChiefException. No additional behavior.

## Error Model Rules

The Error Layer represents failures. It never resolves failures.

### Allowed

✔ Immutable objects
✔ Value semantics
✔ Constructor validation
✔ Defensive copying
✔ Exception hierarchy

### Forbidden

✘ Retry logic
✘ Recovery logic
✘ Orchestration
✘ Delegation
✘ Goal prioritization
✘ Decision algorithms
✘ Monitoring implementation
✘ Logging
✘ Persistence

## Design Principles

### Immutable

All error objects are immutable:
- ChiefErrorCode is an enum (immutable)
- ChiefError has final fields
- All exceptions wrap immutable ChiefError
- No setters
- Defensive copying

### Representation Only

The Error Layer only represents failures:
- Does not resolve failures
- Does not retry operations
- Does not recover from errors
- Does not execute orchestration
- Does not delegate work
- Does not coordinate kernels
- Does not make decisions
- Does not prioritize goals
- Does not log errors
- Does not persist errors

### Classified

Errors are classified by domain:
- Decision errors
- Goal management errors
- Task delegation errors
- Kernel coordination errors
- Validation errors
- Monitoring errors
- General orchestration errors

## Usage Example

```java
// Create an error
ChiefError error = new ChiefError(
    ChiefErrorCode.DECISION_ERROR,
    "Invalid decision context",
    "DecisionService",
    "evaluateCoordinationRequest",
    Map.of("decisionId", "dec-123"),
    Instant.now()
);

// Throw a DecisionException
throw new DecisionException(error);

// Catch and handle
try {
    // orchestration logic
} catch (ChiefException e) {
    ChiefError error = e.error();
    System.out.println("Error code: " + error.code());
    System.out.println("Error message: " + error.message());
    System.out.println("Component: " + error.component());
    System.out.println("Operation: " + error.operation());
}

// Catch specific exception
try {
    // decision logic
} catch (DecisionException e) {
    ChiefError error = e.error();
    // handle decision error
}
```

## Architectural Boundaries

### Responsible For

The Error Layer is responsible for:
- Representing orchestration failures
- Classifying errors by domain
- Providing immutable error information
- Wrapping errors in exceptions
- Providing error codes

### Not Responsible For

The Error Layer is **not** responsible for:
- Resolving failures
- Retrying operations
- Recovering from errors
- Executing orchestration
- Delegating work
- Coordinating kernels
- Making decisions
- Prioritizing goals
- Logging errors
- Persisting errors

## Separation from Other Layers

The Error Layer is intentionally separated from other Chief Kernel layers:

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
- Error representation remains pure
- No side effects in error handling
- Clear architectural boundaries
- Independent evolution of error representation and error handling

## Compliance

This implementation complies with:
- **Kernel Development Standard (EIO-ARCH-001)**
- **Chief Kernel Architecture (EIO-CHIEF-104)**

## Package Structure

```
platform.kernels.chief.error
├── ChiefErrorCode.java              # Canonical error identifiers
├── ChiefError.java                  # Immutable error value object
├── ChiefException.java              # Canonical base exception
├── DecisionException.java           # Decision failures
├── GoalManagementException.java     # Goal management failures
├── TaskDelegationException.java     # Delegation failures
├── KernelCoordinationException.java # Coordination failures
├── ChiefValidationException.java    # Validation failures
├── package-info.java                # Package documentation
└── README.md                        # This file
```

## Future Extensibility

The error architecture supports future extensibility through:
- **New error codes**: Add new ChiefErrorCode enum values
- **New exceptions**: Add new specialized exceptions for new domains
- **Metadata enrichment**: Add error metadata without breaking changes
- **Error categorization**: Extend error classification without changing hierarchy

## Version History

- **1.0** (2026-07-21): Initial implementation per EIO-CHIEF-104