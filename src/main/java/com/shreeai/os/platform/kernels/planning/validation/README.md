# Planning Kernel — Validation Layer

## Overview

The Planning Validation Layer provides structural validation for Planning domain
models. It verifies that planning structures are well-formed and internally
consistent before entering the Service and Engine layers.

## Validation Architecture

```
 Planning Request
       │
       ▼
 PlanningValidator  (entry point, coordinates validators)
       │
 ┌─────┼─────────────────────────────┐
 │     │      │      │        │       │
 ▼     ▼      ▼      ▼        ▼       ▼
Goal  Task  Schedule Priority Constraint ValidationCriteria
Val.  Val.  Val.     Val.     Val.      Val.
```

## Validation Pipeline

`PlanningValidator` serves as the entry point that coordinates all specialized
validators and aggregates their results into a unified `PlanningValidationResult`.

## Validator Responsibilities

| Validator | Validates | Never |
|-----------|-----------|-------|
| `PlanningValidator` | PlanningId, PlanningObjective; coordinates all validators | Performs planning |
| `GoalValidator` | Goal structure, identifiers, constraints | Decomposes goals |
| `TaskValidator` | Task structure, requirements, priority | Generates or sequences tasks |
| `ScheduleValidator` | Schedule structure, constraint references | Optimizes schedules |
| `PriorityValidator` | Priority fields, metadata | Computes priorities |
| `ConstraintValidator` | PlanningConstraints, GoalConstraints, SchedulingConstraints | Evaluates feasibility |
| `ValidationCriteriaValidator` | ValidationCriteria structure | Validates plans semantically |

## Separation from Planning Intelligence

The Validation Layer performs **structural validation only**:

- Verifies models are well-formed
- Checks null safety and identifier validity
- Ensures constructor invariants are satisfied
- Validates immutable collection integrity

The Validation Layer **must never**:

- Determine plan quality or optimality
- Evaluate scheduling efficiency
- Assess execution feasibility
- Compute priority correctness
- Validate resource allocation
- Optimize dependencies or constraints

## Stateless Validator Design

All validators adhere to the following design:

- **Final classes** — no inheritance
- **Static methods only** — utility class pattern
- **No mutable state** — all state passed as parameters
- **Thread-safe** — no shared mutable fields
- **Deterministic** — same inputs always produce the same output
- **Read-only** — never modify models

Do not instantiate validators.

## Validation Result

`PlanningValidationResult` is an immutable value object containing:

- `valid` — whether validation passed
- `violations` — list of violation messages
- `validatedAt` — timestamp of validation
- `metadata` — additional context

## Java Version

All validators are written in Java 21.

## Framework Independence

No framework dependencies (Spring, Lombok, JPA) are used.

## Ownership

**Planning Kernel** | Version 1.0

## Constitutional Authority

EIO-PLAN-103, EIO-ARCH-001