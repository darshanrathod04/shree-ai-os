# Planning Kernel — Service Layer

## Overview

The Planning Service Layer provides orchestration for planning requests. It coordinates
validation, delegates computation to the Processing Engine, and translates failures
into the standardized Planning exception hierarchy.

## Service Architecture

```
Planning API
      │
      ▼
DefaultPlanningService
      │
      ├──────────────► PlanningValidator
      │
      ▼
PlanningProcessingEngine
      │
      ▼
PlanningException Translation
```

## Request Flow

1. **Receive Request** — API layer passes planning request to service.
2. **Validate** — Service delegates to PlanningValidator for structural validation.
3. **Reject if Invalid** — If validation fails, translate to PlanValidationException.
4. **Delegate to Engine** — If valid, delegate to PlanningProcessingEngine.
5. **Translate Exceptions** — Convert engine failures to PlanningException hierarchy.
6. **Return Result** — Return processing result to API layer.

## Orchestration Responsibilities

| Responsibility | Implementation |
|----------------|----------------|
| Receive planning requests | Public service methods |
| Coordinate validation | Delegate to PlanningValidator |
| Delegate computation | Delegate to PlanningProcessingEngine |
| Translate failures | Convert to PlanningException hierarchy |
| Return results | Pass through engine results |

## Validation Delegation

The service never duplicates validation logic. All structural validation is
delegated to the Planning Validation Layer:

- `PlanningValidator.validatePlanningObjective(...)`
- `PlanningValidator.validateGoal(...)`
- `PlanningValidator.validateTask(...)`
- `PlanningValidator.validateValidationCriteria(...)`

## Engine Delegation

The service never performs planning computation. All computation is delegated
to the PlanningProcessingEngine:

- `processGoalPlanning(...)`
- `processTaskPlanning(...)`
- `processScheduling(...)`
- `processPrioritization(...)`
- `processPlanValidation(...)`

## Exception Translation

Internal failures are translated into the Planning exception hierarchy:

| Operation | Exception |
|-----------|-----------|
| Goal planning | `GoalPlanningException` |
| Task planning | `TaskPlanningException` |
| Scheduling | `SchedulingException` |
| Prioritization | `PriorityException` |
| Plan validation | `PlanValidationException` |

## Separation from Engine

- **Service Layer** — orchestrates, validates, delegates, translates exceptions.
- **Engine Layer** — performs deterministic planning computation (PLAN-106).

The `PlanningProcessingEngine` interface is temporary in the Service package.
In PLAN-106, it will migrate to `platform.kernels.planning.engine`.

## Stateless Design

`DefaultPlanningService` is:

- **Stateless** — no mutable instance fields
- **Thread-safe** — immutable dependencies after construction
- **Deterministic** — same inputs produce same outputs
- **Read-only** — never modifies domain models

Dependencies are injected via constructor injection only.

## What the Service Does NOT Do

- Does not perform planning computation
- Does not execute scheduling algorithms
- Does not evaluate priorities
- Does not allocate resources
- Does not mutate domain models
- Does not access persistence
- Does not invoke networking
- Does not create threads

## Future Integration

The Service Layer will be consumed by:

- **Planning API** — public interface for planning operations
- **Engine Layer** — concrete processing implementations (PLAN-106)
- **Verification Layer** — verifies service contracts (PLAN-107)

## Java Version

All service classes are written in Java 21.

## Framework Independence

No framework dependencies (Spring, Lombok, JPA) are used.

## Ownership

**Planning Kernel** | Version 1.0

## Constitutional Authority

EIO-PLAN-105, EIO-ARCH-001