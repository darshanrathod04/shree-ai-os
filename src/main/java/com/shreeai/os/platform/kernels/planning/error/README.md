# Planning Kernel — Error Layer

## Overview

The Planning Error Architecture standardizes how planning failures are represented
and communicated throughout the kernel. It provides immutable error objects, a
strongly typed error code system, and a domain-specific exception hierarchy.

## Error Architecture

```
RuntimeException
    │
    ▼
PlanningException
    │
    ├── GoalPlanningException
    ├── TaskPlanningException
    ├── SchedulingException
    ├── PriorityException
    └── PlanValidationException
```

## Exception Hierarchy

| Exception | Domain Concern | Classification |
|-----------|---------------|----------------|
| `PlanningException` | Root exception for all planning failures | General planning errors |
| `GoalPlanningException` | Goal planning operations | Goal creation, decomposition, refinement, hierarchy |
| `TaskPlanningException` | Task planning operations | Task generation, sequencing, dependencies, grouping |
| `SchedulingException` | Scheduling operations | Schedule generation, optimization, timeline planning |
| `PriorityException` | Prioritization operations | Priority assignment, ordering, urgency classification |
| `PlanValidationException` | Plan validation operations | Validation, dependency checking, constraint verification |

## PlanningError Lifecycle

1. **Creation** — A `PlanningError` is created with an error code, message,
   timestamp, and metadata.
2. **Encapsulation** — The error is encapsulated within a `PlanningException`
   or one of its specialized subclasses.
3. **Propagation** — The exception propagates across Service and Engine layers.
4. **Handling** — Callers inspect the immutable error for classification and
   diagnostic information.

## Error Propagation Philosophy

- Errors classify failures — they never attempt to recover from them.
- Exceptions communicate failures across architectural layers.
- No retry, recovery, or fallback logic exists in the Error Layer.
- Recovery, retries, and fallback strategies belong to future Service and
  Engine Engineering Orders.

## PlanningErrorCode Categories

| Error Code | Description |
|------------|-------------|
| `PLANNING_ERROR` | General planning operation failure |
| `GOAL_PLANNING_ERROR` | Goal planning operation failure |
| `TASK_PLANNING_ERROR` | Task planning operation failure |
| `SCHEDULING_ERROR` | Scheduling operation failure |
| `PRIORITIZATION_ERROR` | Prioritization operation failure |
| `VALIDATION_ERROR` | Plan validation operation failure |
| `INVALID_IDENTIFIER` | Invalid planning identifier provided |
| `INVALID_STATE` | Invalid planning state encountered |
| `INVALID_CONSTRAINTS` | Invalid or malformed constraints |
| `MISSING_REQUIRED_DATA` | Required data is missing |
| `IMMUTABLE_OBJECT_VIOLATION` | Immutable object violation or modification attempt |
| `INTERNAL_ERROR` | Internal processing failure |

## Immutability and Thread Safety

All error objects are:

- **Immutable** — state cannot change after construction
- **Thread-safe** — safely shareable across threads without synchronization
- **Defensive copying** — mutable inputs are copied on construction
- **Value-based equality** — equals(), hashCode(), and toString() implemented

## Future Integration

The Error Layer will be consumed by:

- **Service Layer** — wraps planning operation failures in typed exceptions.
- **Engine Layer** — reports processing failures through the error hierarchy.
- **Verification Layer** — verifies error classification and propagation.

## Java Version

All error classes are written in Java 21.

## Framework Independence

No framework dependencies (Spring, Lombok, JPA) are used.

## Ownership

**Planning Kernel** | Version 1.0

## Constitutional Authority

EIO-PLAN-104, EIO-ARCH-001