# Planning Kernel API Layer

## Overview

The Planning Kernel API provides the public service contracts for transforming cognitive intent into structured plans. This layer defines the interfaces through which the remainder of Shree AI OS interacts with planning capabilities.

This milestone defines contracts only. No implementations, business logic, scheduling algorithms, or execution behavior are introduced.

## Kernel Mission

The Planning Kernel is responsible for transforming cognitive intent into structured plans. It provides contracts for:

- Goal decomposition
- Task generation
- Dependency analysis
- Scheduling
- Prioritization
- Plan validation

The Planning Kernel does not perform execution, orchestration, persistence, or cognitive reasoning.

## API Architecture

The Planning API is composed of six service contracts organized in a hierarchical structure:

```
                    PlanningService
                  /    |     |      \
                 /     |     |       \
                /      |     |        \
    GoalPlanning  TaskPlanning  Scheduling
           \            |            /
            \           |           /
             \   Prioritization  /
                     |
                     |
            PlanValidationService
```

### Service Contracts

#### PlanningService

Primary façade for the Planning Kernel.

**Responsibilities:**
- Expose high-level planning operations
- Coordinate planning-related contracts
- Provide stable API boundaries

**Operations:**
- `createPlan(PlanningRequest)` — Creates a comprehensive plan from cognitive intent
- `refinePlan(PlanRefinementRequest)` — Refines an existing plan
- `validatePlan(PlanValidationRequest)` — Validates a plan for completeness and consistency

#### GoalPlanningService

Defines contracts for goal planning operations.

**Responsibilities:**
- Goal creation and management
- Goal decomposition into sub-goals
- Goal refinement
- Goal hierarchy management
- Planning objective definition

**Operations:**
- `createGoal(GoalCreationRequest)` — Creates a new planning goal
- `decomposeGoal(GoalDecompositionRequest)` — Decomposes a goal into sub-goals
- `refineGoal(GoalRefinementRequest)` — Refines a goal based on new information
- `establishGoalHierarchy(GoalHierarchyRequest)` — Establishes hierarchical relationships
- `definePlanningObjectives(ObjectiveDefinitionRequest)` — Defines planning objectives

#### TaskPlanningService

Defines contracts for task planning operations.

**Responsibilities:**
- Task generation from goals
- Task sequencing
- Task dependency description
- Task grouping
- Task refinement

**Operations:**
- `generateTasks(TaskGenerationRequest)` — Generates tasks from a goal
- `sequenceTasks(TaskSequencingRequest)` — Sequences tasks in execution order
- `describeTaskDependencies(DependencyDescriptionRequest)` — Describes dependencies between tasks
- `groupTasks(TaskGroupingRequest)` — Groups tasks into logical units
- `refineTask(TaskRefinementRequest)` — Refines a task based on new information

#### SchedulingService

Defines contracts for scheduling operations.

**Responsibilities:**
- Schedule generation
- Schedule optimization requests
- Timeline planning
- Constraint scheduling
- Schedule evaluation requests

**Operations:**
- `generateSchedule(ScheduleGenerationRequest)` — Generates a schedule for tasks
- `optimizeSchedule(ScheduleOptimizationRequest)` — Requests optimization of an existing schedule
- `planTimeline(TimelinePlanningRequest)` — Plans a timeline for goal achievement
- `scheduleWithConstraints(ConstraintSchedulingRequest)` — Performs constraint-based scheduling
- `evaluateSchedule(ScheduleEvaluationRequest)` — Requests evaluation of a schedule

#### PrioritizationService

Defines contracts for prioritization operations.

**Responsibilities:**
- Priority assignment requests
- Priority ordering
- Urgency classification
- Importance evaluation requests
- Prioritization policies

**Operations:**
- `assignPriorities(PriorityAssignmentRequest)` — Assigns priorities to goals or tasks
- `orderByPriority(PriorityOrderingRequest)` — Orders elements by priority
- `classifyUrgency(UrgencyClassificationRequest)` — Classifies urgency of planning elements
- `evaluateImportance(ImportanceEvaluationRequest)` — Evaluates importance of planning elements
- `applyPrioritizationPolicy(PolicyApplicationRequest)` — Applies prioritization policies

#### PlanValidationService

Defines contracts for validating plans.

**Responsibilities:**
- Plan validation requests
- Dependency validation
- Constraint verification
- Completeness verification

**Operations:**
- `validatePlan(PlanValidationRequest)` — Validates a plan for completeness and consistency
- `validateDependencies(DependencyValidationRequest)` — Validates dependencies within a plan
- `verifyConstraints(ConstraintVerificationRequest)` — Verifies constraints within a plan
- `verifyCompleteness(CompletenessVerificationRequest)` — Verifies completeness of a plan

## Architectural Boundaries

### What the Planning API Does

- Defines contracts for goal decomposition
- Specifies task planning interfaces
- Provides scheduling contracts
- Defines prioritization interfaces
- Establishes plan validation contracts
- Transforms cognitive intent into structured plans

### What the Planning API Never Does

- **Never implements planning algorithms** — contracts only
- **Never executes scheduling logic** — contracts only
- **Never performs prioritization** — contracts only
- **Never executes business logic** — contracts only
- **Never persists data** — no database or file operations
- **Never invokes networking** — no external calls
- **Never depends on execution engines** — independent of execution
- **Never depends on orchestration** — independent of orchestration
- **Never contains default methods** — pure interface contracts
- **Never contains implementation** — interface-only design

## Separation of Responsibilities

### Cognitive Kernel

Responsible for:
- Reasoning
- Decision support
- Reflection
- Cognitive state management

### Planning Kernel

Responsible for:
- Goal decomposition
- Task planning
- Dependency planning
- Scheduling
- Prioritization
- Plan validation

### Execution Kernel (Future)

Responsible for:
- Workflow execution
- Task execution
- Runtime operations

### Chief Kernel (Future)

Responsible for:
- Orchestration
- Coordination
- Multi-agent planning
- Strategic supervision

## Platform Standards Compliance

The Planning API complies with:

- **EIO-PLAN-101**: Planning Kernel API implementation specification
- **EIO-ARCH-001**: Kernel Development Standard
- **Java 21**: Uses modern Java features (records, pattern matching, etc.)
- **Interface-Only Design**: Pure contracts with no implementation
- **No External Frameworks**: Pure Java interfaces without dependencies

## Design Principles

### Interface-Only Design

All service contracts are defined as pure interfaces with:
- No implementation logic
- No default methods
- No mutable state
- No framework dependencies

### Contract-Focused

Each interface exposes only the contracts necessary for its domain:
- PlanningService — high-level planning coordination
- GoalPlanningService — goal-related contracts
- TaskPlanningService — task-related contracts
- SchedulingService — scheduling contracts
- PrioritizationService — prioritization contracts
- PlanValidationService — validation contracts

### Technology-Agnostic

The Planning API:
- Uses only Java 21 language features
- Contains no framework-specific annotations
- Contains no persistence annotations
- Contains no networking code
- Remains independent of implementation details

## Usage

### Example: Creating a Plan

```java
// Create a planning request
PlanningService.PlanningRequest request = new PlanningService.PlanningRequest(
    "objective-123",
    PlanningService.PlanningScope.DEEP,
    new PlanningConstraints(...)
);

// Create a plan
String planId = planningService.createPlan(request);
```

### Example: Decomposing a Goal

```java
// Create a decomposition request
GoalPlanningService.GoalDecompositionRequest request = 
    new GoalPlanningService.GoalDecompositionRequest(
        "goal-456",
        GoalPlanningService.DecompositionStrategy.HIERARCHICAL,
        GoalPlanningService.DecompositionDepth.DEEP
    );

// Decompose the goal
String decompositionId = goalPlanningService.decomposeGoal(request);
```

### Example: Validating a Plan

```java
// Create a validation request
PlanValidationService.PlanValidationRequest request = 
    new PlanValidationService.PlanValidationRequest(
        "plan-789",
        PlanValidationService.ValidationScope.COMPREHENSIVE,
        new ValidationCriteria(...)
    );

// Validate the plan
String validationId = planValidationService.validatePlan(request);
```

## Future Extensibility

The Planning API is designed for extensibility:

- New service contracts can be added to the hierarchy
- Additional operations can be defined within existing services
- Request/response types can be extended with new fields
- Enums can be expanded with new values
- Implementations will be provided in subsequent Engineering Orders

## Constitutional Authority

- **EIO-PLAN-101**: Planning Kernel API implementation specification
- **EIO-ARCH-001**: Kernel Development Standard

## Ownership

- **Kernel**: Planning Kernel
- **Version**: 1.0
- **Package**: `platform.kernels.planning.api`