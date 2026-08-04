# Chief Kernel API

## Overview

The Chief Kernel is the strategic orchestration layer of Shree AI OS. It coordinates the completed kernels — Knowledge, Cognition, Planning, and Execution — while remaining independent of their internal implementations.

## Platform Position

```
Knowledge
      ↓
Cognition
      ↓
Planning
      ↓
Execution
      ↓
Chief
```

**Kernel Responsibilities:**
- **Knowledge** → knows
- **Cognition** → reasons
- **Planning** → decides what should be done
- **Execution** → performs validated work
- **Chief** → coordinates the entire system

## API Architecture

```
                     ChiefService
                 /       |        |        \
                /        |        |         \
         Decision   GoalManagement  TaskDelegation
                \        |        /
                 \       |       /
           KernelCoordination
                     |
                     |
             ChiefMonitoringService
```

## Service Contracts

### ChiefService

**Primary façade for the Chief Kernel.**

Responsibilities:
- Expose high-level strategic orchestration operations
- Coordinate cross-kernel service contracts
- Provide stable API boundaries for orchestration capabilities
- Delegate specialized orchestration tasks to subordinate services

Operations:
- `submitOrchestration(...)` — submits a strategic orchestration request
- `getOrchestrationStatus(...)` — retrieves orchestration status
- `cancelOrchestration(...)` — cancels an ongoing orchestration
- `getChiefHealth(...)` — retrieves Chief Kernel health status

### DecisionService

**Strategic decision coordination contracts.**

Operations:
- `evaluateCoordinationRequest(...)` — evaluates coordination requests
- `coordinateDecision(...)` — coordinates cross-kernel decisions
- `determineExecutionRouting(...)` — determines execution routing
- `selectParticipatingKernels(...)` — selects participating kernels

### GoalManagementService

**Goal lifecycle contracts.**

Operations:
- `createGoal(...)` — creates a strategic goal
- `updateGoal(...)` — updates an existing goal
- `prioritizeGoal(...)` — prioritizes a goal
- `retireGoal(...)` — retires a goal
- `queryGoalStatus(...)` — queries goal status

### TaskDelegationService

**Delegation contracts.**

Operations:
- `delegateTask(...)` — delegates a validated task
- `monitorDelegation(...)` — monitors delegation status
- `cancelDelegation(...)` — cancels a delegation
- `queryDelegationStatus(...)` — queries delegation status

### KernelCoordinationService

**Cross-kernel coordination contracts.**

Operations:
- `submitCoordinationRequest(...)` — submits coordination request
- `routeToKernel(...)` — routes request to appropriate kernel
- `coordinateDependencies(...)` — coordinates kernel dependencies
- `getOrchestrationState(...)` — retrieves orchestration state

### ChiefMonitoringService

**Strategic monitoring contracts.**

Operations:
- `getOrchestrationHealth(...)` — retrieves orchestration health
- `getCoordinationMetrics(...)` — retrieves coordination metrics
- `getSystemCoordinationState(...)` — retrieves system coordination state
- `getActiveOrchestrations(...)` — retrieves active orchestrations

## ChiefTypes

Temporary shared type container for API construction.

**Records:**
- `ChiefRequest` — strategic orchestration request
- `ChiefResponse` — orchestration response
- `DecisionContext` — decision coordination context
- `DecisionResult` — decision coordination result
- `GoalDescriptor` — goal lifecycle descriptor
- `DelegationResult` — task delegation result
- `CoordinationState` — cross-kernel coordination state
- `ChiefMetrics` — strategic orchestration metrics

**Migration Note:**
In CHIEF-102, all records shall migrate into `platform.kernels.chief.model`.

## Coordination Philosophy

The Chief Kernel is not another planning engine and not another execution engine. It coordinates the capabilities of the completed kernels while remaining independent of their internal implementations.

### What the Chief Kernel Does

- Strategic decision coordination
- Goal lifecycle management
- Task delegation
- Cross-kernel coordination
- Orchestration monitoring

### What the Chief Kernel Does NOT Do

- Knowledge processing
- Reasoning
- Planning computation
- Execution processing
- Persistence
- Networking

## Dependency Boundaries

The Chief API may reference immutable models from completed kernels where appropriate.

It must never depend directly upon:
- Repositories
- Persistence
- Networking
- Execution engines
- Planning engines
- Reasoning engines
- Framework-specific implementations

Interactions occur exclusively through public kernel contracts.

## Design Principles

### Interface-Only

All service contracts are interfaces with:
- No implementation logic
- No default methods
- No mutable state
- Comprehensive JavaDocs

### Technology-Agnostic

All contracts are framework-independent:
- No Spring annotations
- No Lombok
- No JPA
- No framework-specific dependencies

### Contract-Focused

All contracts expose only:
- Orchestration contracts
- Coordination contracts
- Monitoring contracts
- Never implementation details

### Stateless

All interfaces are stateless:
- No mutable fields
- No caches
- No shared mutable state
- Thread-safe by design

## Roadmap

### Future Implementation Layers

The Chief Kernel will follow the established architectural progression:

| Layer | Engineering Order | Status |
|-------|------------------|--------|
| API | CHIEF-101 | ✅ Complete |
| Model | CHIEF-102 | Future |
| Validation | CHIEF-103 | Future |
| Error | CHIEF-104 | Future |
| Service | CHIEF-105 | Future |
| Engine | CHIEF-106 | Future |
| Verification | CHIEF-107 | Future |

### Migration Strategy

ChiefTypes.java is temporary. In CHIEF-102:
1. Create `platform.kernels.chief.model` package
2. Migrate all records to canonical domain models
3. Update API interfaces to reference canonical domain models
4. Remove obsolete temporary types

This mirrors the API → Model evolution established in every completed kernel.

## Compliance

This implementation complies with:
- **Kernel Development Standard (EIO-ARCH-001)**
- **Chief Kernel Architecture (EIO-CHIEF-101)**

## Package Structure

```
platform.kernels.chief.api
├── ChiefService.java              # Primary façade
├── DecisionService.java           # Decision coordination
├── GoalManagementService.java     # Goal lifecycle
├── TaskDelegationService.java     # Task delegation
├── KernelCoordinationService.java # Cross-kernel coordination
├── ChiefMonitoringService.java    # Strategic monitoring
├── ChiefTypes.java                # Temporary shared types
├── package-info.java              # Package documentation
└── README.md                      # This file
```

## Version History

- **1.0** (2026-07-20): Initial implementation per EIO-CHIEF-101