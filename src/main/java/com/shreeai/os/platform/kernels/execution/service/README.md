# Execution Service Layer

## Overview

The Execution Service Layer is the orchestration boundary of the Execution Kernel. It receives validated execution requests from the API, delegates structural verification to the Validation Layer, delegates deterministic execution processing to the Processing Engine, and translates failures into the canonical Execution exception hierarchy.

## Service Philosophy

The Service Layer exists solely to answer one question:

> **"How should this execution request be orchestrated?"**

It does **not** answer:
- "How should the execution be performed?"
- "What is the execution algorithm?"
- "How should failures be recovered?"

Those responsibilities belong to other architectural layers:
- **Validation Layer (EXEC-103)**: structural verification
- **Processing Engine (EXEC-106)**: deterministic execution computation
- **Service Layer (EXEC-105)**: orchestration (you are here)
- **Verification Layer (EXEC-107)**: architectural compliance

## Service Architecture

```
Execution API
       │
       ▼
DefaultExecutionService
       │
       ▼
ExecutionValidator
       │
       ▼
ExecutionProcessingEngine (temporary in service package)
       │
       ▼
ExecutionException Translation
```

## Orchestration Pipeline

The orchestration pipeline is fixed and follows this flow:

1. **Receive** execution request from API
2. **Validate** request structure via ExecutionValidator
3. **If validation fails**, throw ExecutionValidationException
4. **If validation passes**, delegate to ExecutionProcessingEngine
5. **Translate** any failures into canonical ExecutionException hierarchy
6. **Return** result or propagate exception

## Components

### ExecutionProcessingEngine

**Processing contract interface (temporary).**

Responsibilities:
- Defines processing contracts for execution operations
- Provides deterministic execution computation interface
- Delegates to Engine layer (EXEC-106) for implementation
- Contains no execution logic

Operations:
- `processActionExecution(...)` — processes action execution
- `processWorkflowExecution(...)` — processes workflow execution
- `processTaskExecution(...)` — processes task execution
- `processExecutionMonitoring(...)` — processes execution monitoring
- `processExecutionRecovery(...)` — processes execution recovery

**Temporary Architecture Note:**
This interface is temporarily located in the service package. During EXEC-106, it will be migrated to:
`platform.kernels.execution.engine`

### DefaultExecutionService

**Canonical service implementation.**

Responsibilities:
- Implements ExecutionService contract
- Orchestrates execution requests
- Delegates validation to ExecutionValidator
- Delegates processing to ExecutionProcessingEngine
- Translates failures into canonical ExecutionException hierarchy
- Contains no execution logic

Design:
- Stateless — no mutable fields
- Thread-safe — immutable dependencies
- Constructor injection — all dependencies provided through constructor
- Delegation — orchestrates, never executes

## Constructor Injection

All dependencies are injected through the constructor:

```java
public DefaultExecutionService(
        ExecutionValidator validator,
        ExecutionProcessingEngine processingEngine) {
    this.validator = Objects.requireNonNull(validator, 
        "DefaultExecutionService validator must not be null");
    this.processingEngine = Objects.requireNonNull(processingEngine, 
        "DefaultExecutionService processingEngine must not be null");
}
```

**Constructor Injection Policy:**
- All dependencies are final
- Dependencies provided through constructor only
- Constructor arguments are validated
- No field injection
- No setter injection
- No service locator
- No mutable dependency references

## Delegation Responsibilities

### Validation Delegation

The Service delegates validation to ExecutionValidator:

```java
ExecutionValidationResult validationResult = validator.validate(executionRequest);
if (!validationResult.valid()) {
    throw createValidationException(validationResult);
}
```

### Processing Delegation

The Service delegates processing to ExecutionProcessingEngine:

```java
ExecutionResult result = processingEngine.processActionExecution(executionRequest);
return result.executionId().value();
```

### Exception Translation

The Service translates failures into the canonical Execution exception hierarchy:

| Failure Type | Exception |
|--------------|-----------|
| Validation failure | ExecutionValidationException |
| Action execution failure | ActionExecutionException |
| Workflow execution failure | WorkflowExecutionException |
| Task execution failure | TaskExecutionException |
| Recovery failure | RecoveryException |
| General execution failure | ExecutionException |

## Exception Translation Philosophy

The Service Layer translates all failures into the canonical Execution exception hierarchy:

1. **Validation failures** are translated to ExecutionValidationException
2. **ExecutionException** subclasses are re-thrown as-is
3. **Other exceptions** are wrapped in the appropriate ExecutionException subclass
4. **All exceptions** encapsulate immutable ExecutionError objects

This ensures:
- Consistent exception handling across the platform
- Immutable error representation
- Clear failure classification
- No information loss during translation

## Architectural Responsibilities

### Responsible For

The Service Layer is responsible for:
- Request orchestration
- Validation delegation
- Engine delegation
- Exception translation

### Not Responsible For

The Service Layer is **not** responsible for:
- Execution algorithms
- Workflow execution
- Task dispatch
- Retry logic
- Rollback
- Compensation
- Monitoring implementation
- Persistence
- Networking

## Design Principles

### Statelessness

The Service Layer is stateless:
- No mutable fields
- No caches
- No shared mutable state
- No synchronization for business behavior
- Thread-safe by design

### Delegation

The Service Layer delegates:
- Validation → ExecutionValidator
- Processing → ExecutionProcessingEngine
- Never performs execution computation

### Constructor Injection

All dependencies are injected through the constructor:
- Immutable dependencies
- Validated at construction
- No mutation after construction

### Exception Translation

All failures are translated into canonical exceptions:
- Validation failures → ExecutionValidationException
- Action failures → ActionExecutionException
- Workflow failures → WorkflowExecutionException
- Task failures → TaskExecutionException
- Recovery failures → RecoveryException
- General failures → ExecutionException

## Separation from Other Layers

The Service Layer is intentionally separated from other Execution Kernel layers:

```
API
 ↓
Model
 ↓
Validation
 ↓
Error
 ↓
Service  ← You are here
 ↓
Engine
 ↓
Verification
```

This separation ensures:
- Service remains pure orchestration logic
- No side effects in service methods
- Clear architectural boundaries
- Independent evolution of service and execution logic

## Usage Example

```java
// Create dependencies
ExecutionValidator validator = new ExecutionValidator();
ExecutionProcessingEngine engine = new DefaultExecutionEngine();
DefaultExecutionService service = new DefaultExecutionService(validator, engine);

// Execute action
try {
    String executionId = service.executeAction(request);
    // Handle success
} catch (ExecutionValidationException e) {
    // Handle validation failure
    ExecutionError error = e.error();
    List<String> violations = e.metadata().get("violations");
} catch (ActionExecutionException e) {
    // Handle action execution failure
    ExecutionErrorCode code = e.errorCode();
} catch (ExecutionException e) {
    // Handle other execution failures
}

// Execute workflow
try {
    String workflowId = service.executeWorkflow(request);
} catch (WorkflowExecutionException e) {
    // Handle workflow failure
}

// Execute task
try {
    String taskId = service.executeTask(request);
} catch (TaskExecutionException e) {
    // Handle task failure
}

// Recover execution
try {
    String recoveryId = service.recoverExecution(executionId, RecoveryStrategy.RETRY);
} catch (RecoveryException e) {
    // Handle recovery failure
}
```

## Thread Safety

The Service Layer is thread-safe because:
- No mutable state
- All dependencies are immutable
- All methods are stateless
- No shared resources
- Safe for concurrent access

## Determinism

The Service Layer is deterministic because:
- Same input always produces same output
- No external dependencies
- No random or time-based logic
- No mutable state
- Pure orchestration logic

## Future Extensibility

The service architecture supports future extensibility through:
- **New operations**: Add new methods to ExecutionProcessingEngine interface
- **New exception types**: Add specialized exceptions for new failure domains
- **Additional validation**: Extend validation delegation without changing service logic
- **Engine migration**: Migrate ExecutionProcessingEngine to engine package in EXEC-106

## Migration Plan

### EXEC-106 Migration

During EXEC-106, the ExecutionProcessingEngine interface will be migrated:

1. Create `platform.kernels.execution.engine` package
2. Move ExecutionProcessingEngine to engine package
3. Update DefaultExecutionService to reference engine package
4. Remove temporary service-layer interface

This migration maintains architectural consistency with other kernels:
- Knowledge Kernel (KNW-105 → KNW-106)
- Cognitive Kernel (COG-105 → COG-106)
- Planning Kernel (PLAN-105 → PLAN-106)

## Compliance

This implementation complies with:
- **Kernel Development Standard (EIO-ARCH-001)**
- **Execution Kernel Architecture (EIO-EXEC-105)**

## Package Structure

```
platform.kernels.execution.service
├── ExecutionProcessingEngine.java      # Processing contract (temporary)
├── DefaultExecutionService.java        # Canonical service implementation
├── package-info.java                   # Package documentation
└── README.md                           # This file
```

## Best Practices

### Using the Service Layer

```java
// Inject dependencies
ExecutionValidator validator = ...;
ExecutionProcessingEngine engine = ...;
DefaultExecutionService service = new DefaultExecutionService(validator, engine);

// Always validate input
Objects.requireNonNull(request, "request must not be null");

// Handle exceptions appropriately
try {
    String executionId = service.executeAction(request);
} catch (ExecutionValidationException e) {
    // Handle validation errors
    log.validationFailed(e);
} catch (ActionExecutionException e) {
    // Handle action execution errors
    log.actionExecutionFailed(e);
} catch (ExecutionException e) {
    // Handle other execution errors
    log.executionFailed(e);
}
```

### Exception Handling

```java
// Catch specific exceptions
try {
    service.executeWorkflow(request);
} catch (WorkflowExecutionException e) {
    ExecutionError error = e.error();
    ExecutionErrorCode code = e.errorCode();
    // Handle workflow-specific failure
}

// Catch all execution exceptions
try {
    service.executeTask(request);
} catch (ExecutionException e) {
    // Handle any execution failure
    ExecutionError error = e.error();
}
```

## Version History

- **1.0** (2026-07-20): Initial implementation per EIO-EXEC-105