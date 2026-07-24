# Execution Kernel — API Layer

## Overview

The Execution Kernel API provides the public contracts through which the remainder
of Shree AI OS will interact with execution capabilities. This milestone defines
contracts only — no implementations, execution logic, workflow engines, scheduling
behavior, recovery strategies, or orchestration algorithms are introduced.

The Execution Kernel is responsible for transforming validated planning outputs
into controlled runtime execution. It provides contracts for:

- Action execution
- Workflow execution
- Task execution
- Execution monitoring
- Execution recovery
- Execution lifecycle management

## API Architecture

The Execution API consists of six service contracts organized in a hierarchical
structure:

```
                    ExecutionService
                  /      |      |      \
                 /       |      |       \
                /        |      |        \
      ActionExecution  WorkflowExecution  TaskExecution
                \         |         /
                 \        |        /
                  \ ExecutionMonitoring
                           |
                           |
                  ExecutionRecovery
```

All interfaces are public contracts. No interface depends on implementation classes.

## Core Components

### ExecutionService

Primary façade for the Execution Kernel, providing high-level execution operations
and coordinating execution-related contracts.

**Responsibilities:**
- Expose high-level execution operations for the platform
- Coordinate execution-related service contracts
- Provide stable API boundaries for execution capabilities
- Delegate specialized execution tasks to subordinate services

**Operations:**
- `executeAction(ExecutionRequest)` — Execute an action
- `executeWorkflow(ExecutionRequest)` — Execute a workflow
- `executeTask(ExecutionRequest)` — Execute a planned task
- `getExecutionStatus(String)` — Retrieve execution status
- `cancelExecution(String)` — Cancel an ongoing execution
- `recoverExecution(String, RecoveryStrategy)` — Initiate recovery for failed execution

### ActionExecutionService

Defines contracts for executing individual actions.

**Responsibilities:**
- Define action execution contracts
- Define action cancellation contracts
- Define action retry contracts
- Define action status query contracts
- Provide stable API boundaries for action execution

**Operations:**
- `executeAction(ExecutionRequest)` — Execute an action
- `cancelAction(String)` — Cancel an ongoing action
- `retryAction(String)` — Retry a failed action
- `getActionStatus(String)` — Query action status
- `getActionResult(String)` — Retrieve action execution result
- `getActiveActions()` — List all active actions

### WorkflowExecutionService

Defines contracts for workflow execution.

**Responsibilities:**
- Define workflow execution contracts
- Define workflow lifecycle management contracts
- Define workflow control contracts (pause, resume, stop)
- Provide stable API boundaries for workflow execution

**Operations:**
- `executeWorkflow(ExecutionRequest)` — Execute a workflow
- `pauseWorkflow(String)` — Pause a workflow
- `resumeWorkflow(String)` — Resume a paused workflow
- `stopWorkflow(String)` — Stop a workflow
- `getWorkflowState(String)` — Retrieve workflow state
- `getWorkflowResult(String)` — Retrieve workflow execution result
- `getActiveWorkflows()` — List all active workflows

### TaskExecutionService

Defines contracts for execution of Planning tasks.

**Responsibilities:**
- Define task execution contracts
- Define task lifecycle management contracts
- Define task control contracts (execute, skip, complete, fail)
- Provide stable API boundaries for task execution

**Operations:**
- `executeTask(ExecutionRequest)` — Execute a planned task
- `skipTask(String)` — Skip a task execution
- `completeTask(String, Map<String, Object>)` — Mark task as completed
- `failTask(String, String, Map<String, Object>)` — Mark task as failed
- `getTaskStatus(String)` — Retrieve task status
- `getTaskResult(String)` — Retrieve task execution result
- `getActiveTasks()` — List all active tasks

### ExecutionMonitoringService

Defines runtime monitoring contracts.

**Responsibilities:**
- Define execution state monitoring contracts
- Define execution progress monitoring contracts
- Define execution metrics contracts
- Define active executions monitoring contracts
- Define execution health monitoring contracts
- Provide stable API boundaries for execution monitoring

**Operations:**
- `getExecutionState(String)` — Retrieve current execution state
- `getExecutionProgress(String)` — Retrieve execution progress
- `getExecutionMetrics(String)` — Retrieve execution metrics
- `getActiveExecutions()` — List all active executions
- `getExecutionHealth()` — Check execution system health
- `getExecutionHistory(String)` — Retrieve execution history

### ExecutionRecoveryService

Defines execution recovery contracts.

**Responsibilities:**
- Define retry request contracts
- Define rollback request contracts
- Define compensation request contracts
- Define recovery strategy selection contracts
- Provide stable API boundaries for execution recovery

**Operations:**
- `retryExecution(String, RecoveryStrategy)` — Retry a failed execution
- `rollbackExecution(String)` — Rollback a failed execution
- `compensateExecution(String)` — Execute compensation logic
- `selectRecoveryStrategy(String)` — Select recovery strategy
- `getRecoveryOptions(String)` — Retrieve recovery options
- `isRecoverable(String)` — Check if execution is recoverable
- `getRecoverableExecutions()` — List recoverable executions

## ExecutionTypes (Temporary)

ExecutionTypes provides temporary immutable support records for API construction.
These types exist solely to unblock API development and will be migrated to
`platform.kernels.execution.model` in EXEC-102.

### Records

- **ExecutionRequest** — Represents a request to execute an action, task, or workflow
- **ExecutionResult** — Represents the result of an execution operation
- **ExecutionMetrics** — Represents execution performance metrics
- **ExecutionContext** — Represents the context in which execution occurs
- **ExecutionOptions** — Represents configurable options for execution

### Enums

- **ExecutionStatus** — Represents execution lifecycle states (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, PAUSED, WAITING, RETRYING)
- **RecoveryStrategy** — Represents recovery strategies (RETRY, ROLLBACK, COMPENSATE, SKIP, FAIL, DEFAULT)
- **WorkflowState** — Represents workflow lifecycle states (CREATED, RUNNING, PAUSED, COMPLETED, FAILED, STOPPED, WAITING)
- **ActionState** — Represents action lifecycle states (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, RETRYING, SKIPPED)

## Architectural Boundaries

### What Execution Does

- Transforms validated planning outputs into controlled runtime execution
- Provides contracts for action execution, workflow execution, and task execution
- Provides contracts for execution monitoring and recovery
- Manages execution lifecycle

### What Execution Does NOT Do

- Does not perform reasoning
- Does not perform planning
- Does not perform knowledge processing
- Does not perform persistence
- Does not perform networking
- Does not perform AI inference

### Separation of Responsibilities

**Knowledge Kernel:**
- Knowledge representation
- Knowledge management

**Cognitive Kernel:**
- Reasoning
- Reflection
- Decision support

**Planning Kernel:**
- Goal decomposition
- Task planning
- Scheduling
- Prioritization

**Execution Kernel:**
- Action execution
- Workflow execution
- Runtime monitoring
- Execution lifecycle
- Execution recovery

**Chief Kernel (future):**
- Orchestration
- Strategic supervision
- Autonomous coordination

## Platform Boundaries

The Execution API may reference immutable platform models from previous kernels
as needed. It must never depend directly upon:

- Persistence
- Repositories
- Networking
- Execution engine implementations
- UI components
- Framework-specific implementations

## Design Principles

### Interface-Only Design

All contracts are defined as interfaces with:
- No implementation logic
- No default methods
- No mutable state
- No framework dependencies

### Immutable Support Types

All temporary types in ExecutionTypes are:
- Immutable records or enums
- Constructor validation
- Value-based equality
- No business logic

### Comprehensive JavaDocs

Every interface, record, enum, and public method includes comprehensive JavaDocs
describing:
- Architectural responsibility
- Design principles
- Thread safety
- Determinism
- Parameter validation
- Return values
- Exceptions

## Migration Strategy

ExecutionTypes.java is temporary. In EXEC-102:

1. All shared records will be migrated to `platform.kernels.execution.model`
2. API interfaces will be updated to reference canonical domain models
3. ExecutionTypes.java will be removed

This mirrors the API → Model evolution established by the Knowledge, Cognitive,
and Planning kernels.

## Usage Example

```java
// Execute an action
ExecutionRequest request = new ExecutionRequest(
    "exec-123",
    "action-456",
    new ExecutionContext("exec-123", "plan-789", "obj-101", Map.of(), 0),
    new ExecutionOptions(30000, 3, 1000, false, false, Map.of()),
    Map.of("param1", "value1")
);

String executionId = executionService.executeAction(request);

// Monitor execution
ExecutionStatus status = executionMonitoringService.getExecutionState(executionId);
ExecutionMetrics metrics = executionMonitoringService.getExecutionMetrics(executionId);

// Recover if failed
if (status == ExecutionStatus.FAILED) {
    RecoveryStrategy strategy = executionRecoveryService.selectRecoveryStrategy(executionId);
    String retryId = executionService.recoverExecution(executionId, strategy);
}
```

## Constitutional Authority

- EIO-EXEC-101: Execution Kernel Public API
- EIO-ARCH-001: Kernel Development Standard

## Ownership

**Execution Kernel** — API Layer | Version 1.0