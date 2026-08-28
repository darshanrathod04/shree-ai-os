# Execution Kernel — Domain Model Layer

## Overview

The Execution Kernel Domain Model provides the canonical immutable value objects
that represent execution concepts throughout Shree AI OS. These models become the
stable foundation upon which the Execution API, Validation, Service, and Engine
layers are built.

This layer defines **what can be executed**, not **how execution is performed**.
Execution algorithms, workflow orchestration, monitoring behavior, recovery
strategies, and lifecycle management belong to subsequent layers.

## Model Architecture

The Execution Domain Model is organized as a hierarchy of immutable value objects:

```
                ExecutionRequest
                        │
                        ▼
                ExecutionContext
                        │
                        ▼
                ExecutionOptions
                        │
                        ▼
                WorkflowState
                  /       \
                 /         \
                /           \
           ActionState  ExecutionStatus
                \           /
                 \         /
                  \       /
                        ▼
                ExecutionResult
                  /         \
                 /           \
                /             \
     ExecutionMetrics    RecoveryStrategy
                        │
                        ▼
               ExecutionSnapshot
```

## Core Models

### Identity

**ExecutionId**
- Represents the unique identity of an execution instance
- Immutable value object with constructor validation
- Consistent with IdentityId, MemoryId, ContextId, KnowledgeId, CognitiveId, PlanningId
- Value-based equality

### Execution Intent

**ExecutionRequest**
- Represents a request to execute work
- Contains execution identifier, action identifier, context, options, and parameters
- Immutable with defensive copying
- No execution behavior

**ExecutionContext**
- Represents the execution environment
- Contains execution identifier, plan identifier, objective identifier, context data, and priority
- Immutable with defensive copying of context data
- Links execution to planning and cognitive context

**ExecutionOptions**
- Represents configurable execution options
- Contains timeout, retry configuration, and execution preferences
- Immutable with defensive copying
- No execution logic

### Execution State

**ExecutionStatus**
- Represents execution lifecycle states
- Enumeration: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, PAUSED, WAITING, RETRYING
- No lifecycle transitions

**WorkflowState**
- Represents workflow execution state
- Contains workflow identifier, execution stage, task references, and metadata
- Immutable with defensive copying
- No orchestration logic

**ActionState**
- Represents action execution state
- Contains action identifier, lifecycle state, timestamps, and metadata
- Immutable with defensive copying
- No execution behavior

### Execution Outcome

**ExecutionResult**
- Represents the immutable outcome of execution
- Contains execution identifier, status, result data, metrics, and completion timestamp
- Immutable with defensive copying
- No processing logic

**ExecutionMetrics**
- Represents execution performance metrics
- Contains timing, duration, retry count, and resource usage
- Immutable with defensive copying
- No metric calculations

**RecoveryStrategy**
- Represents recovery strategy types
- Enumeration: RETRY, ROLLBACK, COMPENSATE, SKIP, FAIL, DEFAULT
- No recovery implementation

### Historical Representation

**ExecutionSnapshot**
- Represents an immutable snapshot of execution state at a point in time
- Contains execution request, result, workflow state, action states, timestamp, and metadata
- Immutable with defensive copying
- Historical representation only

## Design Principles

### Immutability

All domain models are immutable:
- All fields are final
- No setters
- No mutable state exposure
- Defensive copying of all mutable collections

### Constructor Validation

All models validate inputs in constructors:
- Null checks for all required parameters
- Empty string checks for string identifiers
- Clear error messages for validation failures

### Value Semantics

All models implement value semantics:
- `equals()` — value-based equality
- `hashCode()` — consistent with equals
- `toString()` — human-readable representation

### No Behavior

Domain models contain data only:
- No execution algorithms
- No workflow orchestration
- No recovery logic
- No monitoring logic
- No validation logic
- No persistence
- No networking
- No thread management
- No AI integrations

Behavior belongs to future Validation, Service, and Engine layers.

## Platform Consistency

All models follow canonical platform patterns:

**Identity Pattern:**
- ExecutionId follows the same pattern as IdentityId, MemoryId, ContextId, KnowledgeId, CognitiveId, PlanningId
- Immutable value object with constructor validation
- Value-based equality

**Value Object Pattern:**
- All value objects are final classes
- Constructor validation
- Defensive copying
- Value semantics

**Enumeration Pattern:**
- All enumerations represent states or types
- No behavior in enumerations
- Clear semantic meaning

## API Migration

The Execution API (EXEC-101) originally used temporary types from ExecutionTypes.java.
These have been replaced with canonical domain models:

| Temporary Type | Canonical Model |
|----------------|-----------------|
| ExecutionTypes.ExecutionRequest | ExecutionRequest |
| ExecutionTypes.ExecutionResult | ExecutionResult |
| ExecutionTypes.ExecutionStatus | ExecutionStatus |
| ExecutionTypes.ExecutionMetrics | ExecutionMetrics |
| ExecutionTypes.ExecutionContext | ExecutionContext |
| ExecutionTypes.ExecutionOptions | ExecutionOptions |
| ExecutionTypes.RecoveryStrategy | RecoveryStrategy |
| ExecutionTypes.WorkflowState | WorkflowState |
| ExecutionTypes.ActionState | ActionState |

All API interfaces now reference canonical domain models directly.

## Migration from ExecutionTypes

ExecutionTypes.java was a temporary bootstrap artifact used during EXEC-101.

**Migration completed:**
- All types moved to platform.kernels.execution.model
- All API interfaces updated to use canonical models
- ExecutionTypes.java removed

This mirrors the migration strategy established in Knowledge, Cognitive, and Planning kernels.

## Usage Example

```java
// Create execution identity
ExecutionId executionId = new ExecutionId("exec-123");

// Create execution context
ExecutionContext context = new ExecutionContext(
    executionId,
    "plan-456",
    "obj-789",
    Map.of("key1", "value1"),
    10
);

// Create execution options
ExecutionOptions options = new ExecutionOptions(
    30000,  // 30 second timeout
    3,      // max 3 retries
    1000,   // 1 second retry delay
    false,  // no partial execution
    false,  // don't continue on error
    Map.of("strategy", "aggressive")
);

// Create execution request
ExecutionRequest request = new ExecutionRequest(
    executionId,
    "action-abc",
    context,
    options,
    Map.of("param1", "value1")
);

// Create execution result
ExecutionResult result = new ExecutionResult(
    executionId,
    ExecutionStatus.COMPLETED,
    Map.of("output", "success"),
    new ExecutionMetrics(
        Instant.now().minusSeconds(5),
        Instant.now(),
        5000,
        0,
        Map.of("cpu", 0.5)
    ),
    Instant.now()
);

// Create execution snapshot
ExecutionSnapshot snapshot = new ExecutionSnapshot(
    request,
    result,
    new WorkflowState(
        "workflow-xyz",
        "stage-1",
        List.of("task-1", "task-2"),
        Map.of()
    ),
    Map.of(
        "action-1", new ActionState(
            "action-1",
            ExecutionStatus.COMPLETED,
            Map.of("start", Instant.now().minusSeconds(5), "end", Instant.now()),
            Map.of()
        )
    ),
    Instant.now(),
    Map.of("version", "1.0")
);
```

## Future Extensibility

This domain model provides the foundation for:

**Validation Layer (EXEC-103):**
- Execution request validation
- Execution constraint validation
- Execution state validation

**Service Layer (EXEC-104):**
- Execution services
- Monitoring services
- Recovery services

**Engine Layer (EXEC-105):**
- Execution engines
- Workflow engines
- Recovery engines

All future layers will build upon these immutable domain models.

## Constitutional Authority

- EIO-EXEC-102: Execution Kernel Domain Model
- EIO-ARCH-001: Kernel Development Standard

## Ownership

**Execution Kernel** — Domain Model Layer | Version 1.0