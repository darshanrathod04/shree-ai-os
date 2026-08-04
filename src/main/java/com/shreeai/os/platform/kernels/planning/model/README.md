# Planning Kernel — Domain Model

## Overview

The Planning Domain Model establishes the canonical value objects that represent
planning concepts throughout the platform. This is the second architectural layer
of the Planning Kernel, completing the transition from API contracts to stable,
immutable domain representations.

## Model Hierarchy

```
                 PlanningObjective
                        │
                        ▼
                     Goal
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
     GoalConstraints           Task
                                    │
                          ┌─────────┴─────────┐
                          ▼                   ▼
                 TaskRequirements      Priority
                          │
                      Schedule
                          │
               ┌─────────┴─────────┐
               ▼                   ▼
  SchedulingConstraints   ResourceAvailability
                          │
                          ▼
                 ValidationCriteria
                          │
                          ▼
                  PlanningSnapshot
```

## Identity Strategy

- `PlanningId` is the canonical identity value object for all Planning aggregate roots.
- It follows the same architectural style as `IdentityId`, `MemoryId`, `ContextId`,
  `KnowledgeId`, and `CognitiveId`.
- It is a Java 21 record with constructor validation and value-based equality.

## Immutability Principles

Every model in this package adheres to strict immutability:

1. **Final fields** — all fields are declared `final`.
2. **No setters** — state is provided exclusively through constructors.
3. **Constructor validation** — all arguments are validated for non-null.
4. **Defensive copying** — mutable collections are defensively copied via
   `Collections.unmodifiableMap(...)`, `List.copyOf(...)`, etc.
5. **Value-based equality** — `equals()`, `hashCode()`, and `toString()` are
   implemented for all models.

## API Interaction

- Service interfaces in `platform.kernels.planning.api` reference these stable
  domain model types.
- Domain models are distinct from API request/response records.
- The temporary planning abstractions introduced in PLAN-101 (`PlanningTypes`)
  are replaced by these canonical models where appropriate.

## Architectural Boundaries

This package defines **what** can be planned, not **how** planning is performed.

### Contains

- Immutable value objects
- Constructor validation
- Defensive copying
- Value-based equality

### Does Not Contain

- Planning algorithms
- Scheduling algorithms
- Prioritization algorithms
- Validation logic
- Execution logic
- Orchestration
- Persistence
- Networking
- AI provider integration

## Future Extensibility

This model layer is the canonical foundation upon which the remainder of the
Planning Kernel will be built:

- **Validation Layer** — validate plans against criteria and constraints.
- **Service Layer** — orchestrate planning operations.
- **Engine Layer** — execute planning, scheduling, and prioritization algorithms.
- **Verification Layer** — verify architectural compliance and contract adherence.

## Java Version

All models are written in Java 21, using records where appropriate and final
classes where richer validation is required.

## Framework Independence

No framework dependencies (Spring, Lombok, JPA) are used. These models are
pure Java value objects.

## Ownership

**Planning Kernel** | Version 1.0

## Constitutional Authority

EIO-PLAN-102, EIO-ARCH-001