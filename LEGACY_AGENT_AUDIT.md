# Legacy Agent & Society Audit Report

**Packages:** `platform/agents`, `platform/chief`, `platform/orchestrator`, `platform/debate`, `platform/approval`, `platform/capability`, `platform/intent`
**Comparison Targets:** `platform/kernels/chief`, `platform/kernels/multiagent`
**Audit Type:** READ-ONLY Architecture Analysis
**Date:** 2026-07-22

---

## Executive Summary

The `platform/agents`, `platform/chief`, `platform/orchestrator`, `platform/debate`, `platform/approval`, `platform/capability`, and `platform/intent` packages represent **legacy agent orchestration and multi-agent coordination** implementations that predate the modern `platform/kernels/chief` and `platform/kernels/multiagent` architectures.

**Key Findings:**
- **agents:** 5 files - Agent role definitions and base agent implementations
- **chief:** 2 files - Chief of staff executive coordination
- **orchestrator:** 1 file - Agent orchestration
- **debate:** 16 files - Debate engine and swarm intelligence
- **approval:** 1 file - Approval workflows
- **capability:** 10 files - Capability registry and routing
- **intent:** 2 files - Intent classification and routing
- **Total legacy:** 37 files (0 interfaces, 37 classes)
- **kernels/chief:** 40 files (8 interfaces) - Modern chief kernel
- **kernels/multiagent:** 43 files (9 interfaces) - Modern multiagent kernel
- **No class name overlaps** - completely separate implementations
- **Legacy packages** contain unique capabilities: debate engines, approval workflows, swarm intelligence, capability registries

---

## 1. Package Hierarchy

### platform/agents (5 files)
```
platform/agents/
├── AgentRole.java
├── BaseAgent.java
├── ExecutorAgent.java
├── PlannerAgent.java
└── ReviewerAgent.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Agent lifecycle (BaseAgent)
- ✅ Agent roles (AgentRole, ExecutorAgent, PlannerAgent, ReviewerAgent)
- ✅ Agent registry (implied)
- ❌ Capability routing (not present)
- ❌ Task delegation (not present)
- ❌ Debate (not present)
- ❌ Consensus (not present)
- ❌ Approval (not present)
- ❌ Intent routing (not present)
- ❌ Goal routing (not present)
- ❌ Tool selection (not present)
- ❌ Orchestration (not present)
- ❌ Chief governance (not present)
- ❌ Multi-agent coordination (not present)

### platform/chief (2 files)
```
platform/chief/
├── ChiefInsight.java
└── ChiefOfStaffEngine.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Chief governance (ChiefOfStaffEngine)
- ✅ Executive coordination (ChiefOfStaffEngine)
- ✅ Decision-making (ChiefInsight)
- ❌ Agent lifecycle (not present)
- ❌ Agent registry (not present)
- ❌ Capability routing (not present)
- ❌ Task delegation (not present)
- ❌ Debate (not present)
- ❌ Consensus (not present)
- ❌ Approval (not present)
- ❌ Intent routing (not present)
- ❌ Goal routing (not present)
- ❌ Tool selection (not present)
- ❌ Orchestration (not present)
- ❌ Multi-agent coordination (not present)

### platform/orchestrator (1 file)
```
platform/orchestrator/
└── AgentOrchestrator.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Orchestration (AgentOrchestrator)
- ❌ All other capabilities (not present)

### platform/debate (16 files)
```
platform/debate/
├── CriticAgent.java
├── DebateEngine.java
├── DebateMemory.java
├── DebateState.java
├── DebateTurn.java
├── JudgeAgent.java
├── ProposerAgent.java
├── RefinerAgent.java
├── ResearchAgent.java
└── swarm/
    ├── AdaptiveSwarmSelector.java
    ├── AgentPerformanceMemory.java
    ├── DebateSwarmEngine.java
    ├── JudgeAgent.java
    ├── SwarmJudge.java
    ├── SwarmResult.java
    └── SwarmWorkerAgent.java
```

**Structure:** Flat structure with 1 sub-package (swarm)

**Capabilities Identified:**
- ✅ Debate (DebateEngine, DebateTurn, DebateState)
- ✅ Consensus (implied through JudgeAgent, SwarmJudge)
- ✅ Agent negotiation (CriticAgent, ProposerAgent, RefinerAgent)
- ✅ Swarm intelligence (swarm/ sub-package)
- ✅ Voting (implied through SwarmJudge)
- ✅ Arbitration (JudgeAgent, SwarmJudge)
- ❌ Agent lifecycle (not present)
- ❌ Agent registry (not present)
- ❌ Capability routing (not present)
- ❌ Task delegation (not present)
- ❌ Approval (not present)
- ❌ Intent routing (not present)
- ❌ Goal routing (not present)
- ❌ Tool selection (not present)
- ❌ Orchestration (not present)
- ❌ Chief governance (not present)
- ❌ Multi-agent coordination (not present)

### platform/approval (1 file)
```
platform/approval/
└── ApprovalService.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Approval (ApprovalService)
- ❌ All other capabilities (not present)

### platform/capability (10 files)
```
platform/capability/
├── Capability.java
├── CapabilityConfig.java
├── CapabilityContext.java
├── CapabilityMatch.java
├── CapabilityMetadata.java
├── CapabilityRegistry.java
├── ChatCapability.java
├── LearningCapability.java
├── QuizCapability.java
└── RoadmapCapability.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Capability registry (CapabilityRegistry)
- ✅ Capability routing (CapabilityMatch, CapabilityContext)
- ✅ Tool selection (Capability, CapabilityConfig)
- ❌ Agent lifecycle (not present)
- ❌ Agent registry (not present)
- ❌ Task delegation (not present)
- ❌ Debate (not present)
- ❌ Consensus (not present)
- ❌ Approval (not present)
- ❌ Intent routing (not present)
- ❌ Goal routing (not present)
- ❌ Orchestration (not present)
- ❌ Chief governance (not present)
- ❌ Multi-agent coordination (not present)

### platform/intent (2 files)
```
platform/intent/
├── IntentClassifier.java
└── IntentType.java
```

**Structure:** Flat structure with no sub-packages

**Capabilities Identified:**
- ✅ Intent routing (IntentClassifier, IntentType)
- ❌ All other capabilities (not present)

### platform/kernels/chief (40 files) - For Comparison
```
platform/kernels/chief/
├── api/ (interfaces)
├── engine/ (engines)
├── error/ (exceptions)
├── model/ (models)
├── service/ (services)
├── validation/ (validators)
└── verification/ (verifiers)
```

**Structure:** Layered architecture with 7 sub-packages

**Capabilities Identified:**
- ✅ Chief governance
- ✅ Executive coordination
- ✅ Decision-making
- ✅ Cross-kernel orchestration
- ✅ Strategic planning
- ✅ Resource allocation

### platform/kernels/multiagent (43 files) - For Comparison
```
platform/kernels/multiagent/
├── api/ (interfaces)
├── engine/ (engines)
├── error/ (exceptions)
├── model/ (models)
├── service/ (services)
├── validation/ (validators)
└── verification/ (verifiers)
```

**Structure:** Layered architecture with 7 sub-packages

**Capabilities Identified:**
- ✅ Agent registry
- ✅ Agent communication
- ✅ Agent discovery
- ✅ Capability matching
- ✅ Multi-agent coordination
- ✅ Task delegation
- ✅ Agent lifecycle management

---

## 2. Responsibilities

### platform/agents

**Purpose:** Agent role definitions and base implementations

Responsible for:
- Agent role definitions (AgentRole)
- Base agent implementation (BaseAgent)
- Specialized agent types (ExecutorAgent, PlannerAgent, ReviewerAgent)

**Ownership:** Agent definitions subsystem

### platform/chief

**Purpose:** Executive coordination and decision-making

Responsible for:
- Chief of staff operations (ChiefOfStaffEngine)
- Executive insights and decisions (ChiefInsight)
- High-level coordination

**Ownership:** Executive governance subsystem

### platform/orchestrator

**Purpose:** Agent orchestration

Responsible for:
- Agent orchestration (AgentOrchestrator)
- Coordination of multiple agents

**Ownership:** Orchestration subsystem

### platform/debate

**Purpose:** Debate engine and swarm intelligence

Responsible for:
- Debate management (DebateEngine, DebateTurn, DebateState)
- Agent roles in debate (CriticAgent, ProposerAgent, RefinerAgent, JudgeAgent, ResearchAgent)
- Debate memory (DebateMemory)
- Swarm intelligence (swarm/ sub-package)
- Adaptive swarm selection (AdaptiveSwarmSelector)
- Agent performance tracking (AgentPerformanceMemory)

**Ownership:** Debate and swarm intelligence subsystem

### platform/approval

**Purpose:** Approval workflows

Responsible for:
- Approval management (ApprovalService)
- Approval workflows

**Ownership:** Approval subsystem

### platform/capability

**Purpose:** Capability registry and routing

Responsible for:
- Capability definitions (Capability)
- Capability configuration (CapabilityConfig)
- Capability metadata (CapabilityMetadata)
- Capability registry (CapabilityRegistry)
- Capability matching (CapabilityMatch)
- Capability context (CapabilityContext)
- Specific capabilities (ChatCapability, LearningCapability, QuizCapability, RoadmapCapability)

**Ownership:** Capability management subsystem

### platform/intent

**Purpose:** Intent classification and routing

Responsible for:
- Intent classification (IntentClassifier)
- Intent types (IntentType)
- Intent routing

**Ownership:** Intent management subsystem

### platform/kernels/chief (Comparison)

**Purpose:** Executive coordination and high-level decision-making

Responsible for:
- Executive coordination
- High-level decision-making
- Cross-kernel orchestration
- Strategic planning
- Resource allocation

**Ownership:** Chief kernel

### platform/kernels/multiagent (Comparison)

**Purpose:** Multi-agent coordination and communication

Responsible for:
- Agent registry
- Agent communication
- Agent discovery
- Capability matching
- Multi-agent coordination
- Task delegation
- Agent lifecycle management

**Ownership:** Multiagent kernel

---

## 3. Public APIs

### platform/agents

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **BaseAgent** - Base agent implementation
- **AgentRole** - Agent role enumeration
- **ExecutorAgent** - Executor agent
- **PlannerAgent** - Planner agent
- **ReviewerAgent** - Reviewer agent

#### Entry Points
- BaseAgent: Main entry point for agent operations

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- BaseAgent - Coordinates agent operations

### platform/chief

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **ChiefOfStaffEngine** - Chief of staff engine
- **ChiefInsight** - Chief insight model

#### Entry Points
- ChiefOfStaffEngine: Main entry point for chief operations

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- ChiefOfStaffEngine - Coordinates executive operations

### platform/orchestrator

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **AgentOrchestrator** - Agent orchestrator

#### Entry Points
- AgentOrchestrator: Main entry point for orchestration

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- AgentOrchestrator - Coordinates agent orchestration

### platform/debate

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **DebateEngine** - Debate engine
- **DebateState** - Debate state enumeration
- **DebateTurn** - Debate turn model
- **DebateMemory** - Debate memory
- **CriticAgent** - Critic agent
- **ProposerAgent** - Proposer agent
- **RefinerAgent** - Refiner agent
- **JudgeAgent** - Judge agent
- **ResearchAgent** - Research agent
- **AdaptiveSwarmSelector** - Adaptive swarm selector
- **AgentPerformanceMemory** - Agent performance memory
- **DebateSwarmEngine** - Debate swarm engine
- **SwarmJudge** - Swarm judge
- **SwarmResult** - Swarm result
- **SwarmWorkerAgent** - Swarm worker agent

#### Entry Points
- DebateEngine: Main entry point for debate operations
- DebateSwarmEngine: Main entry point for swarm operations

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- DebateEngine - Coordinates debate
- JudgeAgent - Coordinates judging
- AdaptiveSwarmSelector - Coordinates swarm selection

### platform/approval

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **ApprovalService** - Approval service

#### Entry Points
- ApprovalService: Main entry point for approval operations

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- ApprovalService - Coordinates approval workflows

### platform/capability

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **CapabilityRegistry** - Capability registry
- **Capability** - Capability base class
- **CapabilityConfig** - Capability configuration
- **CapabilityContext** - Capability context
- **CapabilityMatch** - Capability match
- **CapabilityMetadata** - Capability metadata
- **ChatCapability** - Chat capability
- **LearningCapability** - Learning capability
- **QuizCapability** - Quiz capability
- **RoadmapCapability** - Roadmap capability

#### Entry Points
- CapabilityRegistry: Main entry point for capability operations

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- CapabilityRegistry - Coordinates capability management

### platform/intent

#### Interfaces
- **None** (0 interfaces)

#### Public Classes
- **IntentClassifier** - Intent classifier
- **IntentType** - Intent type enumeration

#### Entry Points
- IntentClassifier: Main entry point for intent classification

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- IntentClassifier - Coordinates intent classification

### platform/kernels/chief (Comparison)

#### Interfaces (8 interfaces)
- **ChiefOfStaffEngine** - Main engine interface
- **ChiefInsight** - Interface for chief insights
- Plus 6 additional interfaces

#### Public Services
- ChiefOfStaffEngine - Default implementation

#### Entry Points
- ChiefOfStaffEngine: Main entry point for executive coordination

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- ChiefOfStaffEngine - Coordinates kernel interactions

### platform/kernels/multiagent (Comparison)

#### Interfaces (9 interfaces)
- **MultiAgentService** - Main multi-agent service
- **AgentRegistry** - Agent registry interface
- **AgentCommunication** - Agent communication interface
- **AgentDiscovery** - Agent discovery interface
- **CapabilityMatcher** - Capability matching interface
- Plus 4 additional interfaces

#### Public Services
- MultiAgentService - Default implementation
- AgentRegistry - Default implementation
- AgentCommunication - Default implementation
- AgentDiscovery - Default implementation
- CapabilityMatcher - Default implementation

#### Entry Points
- MultiAgentService: Main entry point for multi-agent operations
- AgentRegistry: Agent registration entry point
- AgentCommunication: Communication entry point
- AgentDiscovery: Discovery entry point
- CapabilityMatcher: Capability matching entry point

#### Factories
- None explicit

#### Builders
- None explicit

#### Coordinators
- AgentCoordinator - Coordinates agent operations
- CommunicationCoordinator - Coordinates communication

---

## 4. Internal Structure

### platform/agents

#### Models
- **AgentRole** - Agent role enumeration
- **BaseAgent** - Base agent model
- **ExecutorAgent** - Executor agent model
- **PlannerAgent** - Planner agent model
- **ReviewerAgent** - Reviewer agent model

#### Registries
- None explicit

#### Coordinators
- **BaseAgent** - Coordinates agent operations

#### Routers
- None explicit

#### Engines
- None explicit

#### Validators
- None (no validators)

#### Executors
- **ExecutorAgent** - Executes tasks

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/chief

#### Models
- **ChiefInsight** - Chief insight model
- **ChiefOfStaffEngine** - Chief engine model

#### Registries
- None explicit

#### Coordinators
- **ChiefOfStaffEngine** - Coordinates executive operations

#### Routers
- None explicit

#### Engines
- **ChiefOfStaffEngine** - Chief engine

#### Validators
- None (no validators)

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/orchestrator

#### Models
- **AgentOrchestrator** - Orchestrator model

#### Registries
- None explicit

#### Coordinators
- **AgentOrchestrator** - Coordinates orchestration

#### Routers
- None explicit

#### Engines
- None explicit

#### Validators
- None (no validators)

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/debate

#### Models
- **DebateState** - Debate state enumeration
- **DebateTurn** - Debate turn model
- **DebateMemory** - Debate memory model
- **SwarmResult** - Swarm result model

#### Registries
- None explicit

#### Coordinators
- **DebateEngine** - Coordinates debate
- **JudgeAgent** - Coordinates judging
- **AdaptiveSwarmSelector** - Coordinates swarm selection

#### Routers
- None explicit

#### Engines
- **DebateEngine** - Debate engine
- **DebateSwarmEngine** - Swarm debate engine

#### Validators
- None (no validators)

#### Executors
- **CriticAgent** - Critic executor
- **ProposerAgent** - Proposer executor
- **RefinerAgent** - Refiner executor
- **ResearchAgent** - Research executor
- **SwarmWorkerAgent** - Swarm worker executor

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- **AgentPerformanceMemory** - Agent performance tracking

### platform/approval

#### Models
- **ApprovalService** - Approval service model

#### Registries
- None explicit

#### Coordinators
- **ApprovalService** - Coordinates approval workflows

#### Routers
- None explicit

#### Engines
- None explicit

#### Validators
- None (no validators)

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/capability

#### Models
- **Capability** - Capability base model
- **CapabilityConfig** - Capability configuration model
- **CapabilityContext** - Capability context model
- **CapabilityMatch** - Capability match model
- **CapabilityMetadata** - Capability metadata model
- **CapabilityRegistry** - Capability registry model
- **ChatCapability** - Chat capability model
- **LearningCapability** - Learning capability model
- **QuizCapability** - Quiz capability model
- **RoadmapCapability** - Roadmap capability model

#### Registries
- **CapabilityRegistry** - Capability registry

#### Coordinators
- **CapabilityRegistry** - Coordinates capability management

#### Routers
- **CapabilityMatch** - Routes to capabilities

#### Engines
- None explicit

#### Validators
- None (no validators)

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/intent

#### Models
- **IntentClassifier** - Intent classifier model
- **IntentType** - Intent type enumeration

#### Registries
- None explicit

#### Coordinators
- **IntentClassifier** - Coordinates intent classification

#### Routers
- **IntentClassifier** - Routes based on intent

#### Engines
- None explicit

#### Validators
- None (no validators)

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions
- None (no exceptions)

#### Utilities
- None explicit

### platform/kernels/chief (Comparison)

#### Models
- **ChiefInsight** - Chief insight model
- **ChiefDecision** - Chief decision model
- **ChiefMetrics** - Chief metrics model

#### Registries
- None explicit (uses kernel registry)

#### Coordinators
- **ChiefOfStaffEngine** - Coordinates kernel interactions

#### Routers
- None explicit

#### Engines
- **ChiefOfStaffEngine** - Chief processing engine

#### Validators
- **ChiefValidator** - Validates chief decisions

#### Executors
- None explicit

#### Policies
- None explicit

#### Exceptions (6)
- **ChiefException** - Base chief exception
- **ChiefNotFoundException** - Chief not found
- **ChiefInitializationException** - Initialization error
- **ChiefExecutionException** - Execution error
- **InvalidChiefStateException** - Invalid state error
- **ChiefValidationException** - Validation error

#### Utilities
- None explicit

### platform/kernels/multiagent (Comparison)

#### Models
- **AgentDescriptor** - Agent descriptor model
- **AgentCapability** - Agent capability model
- **AgentCommunication** - Agent communication model
- **AgentRegistration** - Agent registration model
- **AgentRequest** - Agent request model
- **AgentResponse** - Agent response model
- **AgentStatus** - Agent status enumeration
- **AgentSnapshot** - Agent snapshot model
- **MultiAgentMetrics** - Multi-agent metrics model
- **AgentId** - Agent identifier

#### Registries
- **AgentRegistry** - Agent registry

#### Coordinators
- **AgentCoordinator** - Coordinates agent operations
- **CommunicationCoordinator** - Coordinates communication

#### Routers
- **CapabilityMatcher** - Routes to agents based on capabilities

#### Engines
- **MultiAgentEngine** - Multi-agent processing engine
- **AgentCoordinationEngine** - Coordination engine
- **AgentCommunicationEngine** - Communication engine

#### Validators (5)
- **AgentValidator** - Validates agent data
- **CapabilityValidator** - Validates capabilities
- **CommunicationValidator** - Validates communication
- **LifecycleValidator** - Validates agent lifecycle
- **MultiAgentCriteriaValidator** - Criteria validation

#### Executors
- None explicit (agents execute tasks)

#### Policies
- None explicit

#### Exceptions (7)
- **MultiAgentException** - Base multi-agent exception
- **AgentNotFoundException** - Agent not found
- **AgentRegistrationException** - Registration error
- **AgentCommunicationException** - Communication error
- **CapabilityNotFoundException** - Capability not found
- **MultiAgentValidationException** - Validation error
- **MultiAgentError** - Base error

#### Utilities
- **AgentDiscovery** - Agent discovery utility

---

## 5. Domain Models

### platform/agents

#### Agent Models
- **BaseAgent** - Base agent model
- **AgentRole** - Agent role enumeration
- **ExecutorAgent** - Executor agent
- **PlannerAgent** - Planner agent
- **ReviewerAgent** - Reviewer agent

### platform/chief

#### Chief Models
- **ChiefInsight** - Chief insight model
- **ChiefOfStaffEngine** - Chief engine model

### platform/orchestrator

#### Orchestrator Models
- **AgentOrchestrator** - Orchestrator model

### platform/debate

#### Debate Models
- **DebateState** - Debate state enumeration
- **DebateTurn** - Debate turn model
- **DebateMemory** - Debate memory model
- **SwarmResult** - Swarm result model

#### Agent Models
- **CriticAgent** - Critic agent
- **ProposerAgent** - Proposer agent
- **RefinerAgent** - Refiner agent
- **JudgeAgent** - Judge agent
- **ResearchAgent** - Research agent
- **SwarmWorkerAgent** - Swarm worker agent

#### Swarm Models
- **AdaptiveSwarmSelector** - Swarm selector model
- **AgentPerformanceMemory** - Performance memory model
- **DebateSwarmEngine** - Swarm engine model
- **SwarmJudge** - Swarm judge model

### platform/approval

#### Approval Models
- **ApprovalService** - Approval service model

### platform/capability

#### Capability Models
- **Capability** - Capability base model
- **CapabilityConfig** - Capability configuration model
- **CapabilityContext** - Capability context model
- **CapabilityMatch** - Capability match model
- **CapabilityMetadata** - Capability metadata model
- **CapabilityRegistry** - Capability registry model
- **ChatCapability** - Chat capability model
- **LearningCapability** - Learning capability model
- **QuizCapability** - Quiz capability model
- **RoadmapCapability** - Roadmap capability model

### platform/intent

#### Intent Models
- **IntentClassifier** - Intent classifier model
- **IntentType** - Intent type enumeration

### platform/kernels/chief (Comparison)

#### Chief Models
- **ChiefInsight** - Chief insight model
- **ChiefDecision** - Chief decision model
- **ChiefMetrics** - Chief metrics model

### platform/kernels/multiagent (Comparison)

#### Agent Models
- **AgentDescriptor** - Agent descriptor model
- **AgentCapability** - Agent capability model
- **AgentCommunication** - Agent communication model
- **AgentRegistration** - Agent registration model
- **AgentRequest** - Agent request model
- **AgentResponse** - Agent response model
- **AgentStatus** - Agent status enumeration
- **AgentSnapshot** - Agent snapshot model
- **MultiAgentMetrics** - Multi-agent metrics model
- **AgentId** - Agent identifier

---

## 6. Dependencies

### platform/agents

#### Internal Dependencies
- **None** (0 internal dependencies)
- All classes at root level

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
agents (standalone)
```

### platform/chief

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
chief (standalone)
```

### platform/orchestrator

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
orchestrator (standalone)
```

### platform/debate

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
debate (standalone)
```

### platform/approval

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
approval (standalone)
```

### platform/capability

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
capability (standalone)
```

### platform/intent

#### Internal Dependencies
- **None** (0 internal dependencies)

#### External Dependencies
- **None** (0 external platform dependencies)
- Fully self-contained

**Dependency Pattern:**
```
intent (standalone)
```

### platform/kernels/chief (Comparison)

#### Internal Dependencies
- **model** (extensive)
  - ChiefInsight, ChiefDecision, ChiefMetrics
- **core** (for eventbus, configuration, registry)
- **runtime** (for execution, lifecycle)
- **cognitive** (for reasoning)
- **planning** (for strategic planning)
- **knowledge** (for information access)
- **memory** (for historical context)

**Dependency Pattern:**
```
kernels/chief → core, runtime, cognitive, planning, knowledge, memory
```

### platform/kernels/multiagent (Comparison)

#### Internal Dependencies
- **model** (extensive)
  - AgentDescriptor, AgentCapability, AgentCommunication, etc.
- **core** (for eventbus, configuration, registry, discovery)
- **runtime** (for execution, lifecycle)
- **execution** (for task execution)
- **planning** (for multi-agent planning)
- **knowledge** (for capability knowledge)
- **memory** (for agent memory)

**Dependency Pattern:**
```
kernels/multiagent → core, runtime, execution, planning, knowledge, memory
```

---

## 7. Shared Concepts with kernels/chief and kernels/multiagent

The following concepts exist in both legacy packages and kernel packages, but are implemented separately:

### Legacy vs kernels/chief

| Legacy | Kernel | Concept |
|--------|--------|---------|
| ChiefOfStaffEngine | ChiefOfStaffEngine | Chief engine (same name, different impl) |
| ChiefInsight | ChiefInsight | Chief insight (same name, different impl) |
| AgentOrchestrator | ChiefOfStaffEngine | Orchestration/coordination |

### Legacy vs kernels/multiagent

| Legacy | Kernel | Concept |
|--------|--------|---------|
| BaseAgent | AgentDescriptor | Agent definition |
| AgentRole | AgentStatus | Agent role/status |
| CapabilityRegistry | CapabilityMatcher | Capability registry/matching |
| CapabilityMatch | CapabilityMatcher | Capability matching |
| IntentClassifier | AgentDiscovery | Intent/agent discovery |
| DebateEngine | AgentCommunication | Agent communication |
| JudgeAgent | AgentCoordinator | Agent coordination |
| ApprovalService | (not present) | Approval workflow |

**Key Observation:** The legacy packages contain foundational concepts that were later expanded in the kernel architecture. The class names ChiefOfStaffEngine and ChiefInsight appear in both chief packages, suggesting direct evolution.

**Evolution Pattern:**
```
platform/chief (foundation)
    ↓
platform/kernels/chief (expansion)

platform/agents + orchestrator + debate + capability + intent (foundation)
    ↓
platform/kernels/multiagent (expansion)
```

---

## 8. Unique Legacy Capabilities

### Unique to platform/agents (not in kernels/chief or multiagent)

#### Agent Roles
- **AgentRole** - Agent role enumeration
- **ExecutorAgent** - Executor agent specialization
- **PlannerAgent** - Planner agent specialization
- **ReviewerAgent** - Reviewer agent specialization

#### Base Agent
- **BaseAgent** - Base agent implementation

### Unique to platform/chief (not in kernels/chief or multiagent)

#### Minimal Implementation
- **ChiefInsight** - Simple insight model
- **ChiefOfStaffEngine** - Simple chief engine

**Note:** Very minimal implementation compared to kernel

### Unique to platform/orchestrator (not in kernels/chief or multiagent)

#### Orchestration
- **AgentOrchestrator** - Simple orchestrator

### Unique to platform/debate (not in kernels/chief or multiagent)

#### Debate Engine
- **DebateEngine** - Debate engine
- **DebateState** - Debate state enumeration
- **DebateTurn** - Debate turn model
- **DebateMemory** - Debate memory

#### Debate Agents
- **CriticAgent** - Critic agent
- **ProposerAgent** - Proposer agent
- **RefinerAgent** - Refiner agent
- **JudgeAgent** - Judge agent
- **ResearchAgent** - Research agent

#### Swarm Intelligence
- **DebateSwarmEngine** - Swarm debate engine
- **AdaptiveSwarmSelector** - Adaptive swarm selection
- **AgentPerformanceMemory** - Agent performance tracking
- **SwarmJudge** - Swarm judge
- **SwarmResult** - Swarm result
- **SwarmWorkerAgent** - Swarm worker agent

### Unique to platform/approval (not in kernels/chief or multiagent)

#### Approval Workflows
- **ApprovalService** - Approval service

### Unique to platform/capability (not in kernels/chief or multiagent)

#### Capability Registry
- **CapabilityRegistry** - Capability registry
- **CapabilityMatch** - Capability matching
- **CapabilityContext** - Capability context
- **CapabilityMetadata** - Capability metadata

#### Specific Capabilities
- **ChatCapability** - Chat capability
- **LearningCapability** - Learning capability
- **QuizCapability** - Quiz capability
- **RoadmapCapability** - Roadmap capability

### Unique to platform/intent (not in kernels/chief or multiagent)

#### Intent Classification
- **IntentClassifier** - Intent classifier
- **IntentType** - Intent type enumeration

### Unique to platform/kernels/chief (not in legacy packages)

#### Validation
- **ChiefValidator** - Chief decision validation

#### Verification
- **Chief verification** - Architecture verification

#### Error Handling
- **ChiefException** - Base chief exception
- **ChiefNotFoundException** - Chief not found
- **ChiefInitializationException** - Initialization error
- **ChiefExecutionException** - Execution error
- **InvalidChiefStateException** - Invalid state error
- **ChiefValidationException** - Validation error

#### Advanced Models
- **ChiefDecision** - Chief decision model
- **ChiefMetrics** - Chief metrics model

### Unique to platform/kernels/multiagent (not in legacy packages)

#### Agent Management
- **AgentRegistry** - Agent registry
- **AgentDescriptor** - Agent descriptor
- **AgentRegistration** - Agent registration
- **AgentSnapshot** - Agent snapshot
- **AgentId** - Agent identifier

#### Communication
- **AgentCommunication** - Agent communication
- **CommunicationCoordinator** - Communication coordinator

#### Discovery
- **AgentDiscovery** - Agent discovery

#### Validation
- **AgentValidator** - Agent validation
- **CapabilityValidator** - Capability validation
- **CommunicationValidator** - Communication validation
- **LifecycleValidator** - Lifecycle validation
- **MultiAgentCriteriaValidator** - Criteria validation

#### Verification
- **MultiAgent verification** - Architecture verification

#### Error Handling
- **MultiAgentException** - Base multi-agent exception
- **AgentNotFoundException** - Agent not found
- **AgentRegistrationException** - Registration error
- **AgentCommunicationException** - Communication error
- **CapabilityNotFoundException** - Capability not found
- **MultiAgentValidationException** - Validation error
- **MultiAgentError** - Base error

#### Advanced Models
- **AgentCapability** - Agent capability model
- **AgentRequest** - Agent request model
- **AgentResponse** - Agent response model
- **AgentStatus** - Agent status enumeration
- **MultiAgentMetrics** - Multi-agent metrics model

---

## 9. Architecture Observations

### Layering

#### Legacy Packages
**Flat Architecture:**
- No layered structure
- All classes at root level
- No interface-based design (0 interfaces across all packages)
- Direct implementation pattern

**Pattern:** Monolithic agent/orchestration system with specialized components

#### Kernel Packages
**Layered Architecture:**
- Clear layering: api → service → engine → model → validation → verification
- Interface-based design (8 interfaces in chief, 9 in multiagent)
- Validation layer
- Error layer
- Verification layer

**Pattern:** Enterprise-grade layered architecture

### Coupling

#### Legacy Packages
**No Coupling:**
- All packages are standalone (0 dependencies)
- No platform integration
- Fully self-contained

**Coupling Type:** Standalone

#### Kernel Packages
**High Coupling (Integration):**
- chief → core, runtime, cognitive, planning, knowledge, memory
- multiagent → core, runtime, execution, planning, knowledge, memory

**Coupling Type:** Deep platform integration

### Cohesion

#### Legacy Packages
**Medium Cohesion:**
- agents: Focused on agent roles
- chief: Focused on executive coordination
- orchestrator: Focused on orchestration
- debate: Focused on debate and swarm
- approval: Focused on approval
- capability: Focused on capabilities
- intent: Focused on intent classification

**Cohesion Score:** Medium - related but distinct concerns across 7 packages

#### Kernel Packages
**Very High Cohesion:**
- chief: Single responsibility - executive coordination
- multiagent: Single responsibility - multi-agent coordination

**Cohesion Score:** Very High - single domain focus per kernel

### Boundaries

#### Legacy Packages
**Unclear Boundaries:**
- No interface contracts
- Direct implementation exposure
- No API layer
- No validation or verification
- No cross-package integration

**Boundary Type:** Blurred boundaries

#### Kernel Packages
**Clear Boundaries:**
- Well-defined API layer
- Interface-based contracts
- Internal implementation hidden
- Clear dependency direction

**Boundary Type:** Well-defined boundaries

### Governance Lifecycle

#### Legacy Packages
**Distributed Governance:**
- Chief governance in platform/chief
- Agent roles in platform/agents
- Orchestration in platform/orchestrator
- Debate in platform/debate
- Approval in platform/approval

**Lifecycle Pattern:** Distributed across 7 packages

#### Kernel Packages
**Centralized Governance:**
- Chief governance in kernels/chief
- Multi-agent coordination in kernels/multiagent

**Lifecycle Pattern:** Centralized in 2 kernels

### Orchestration Flow

#### Legacy Packages
**Distributed Flow:**
```
intent (IntentClassifier)
    ↓
capability (CapabilityRegistry)
    ↓
orchestrator (AgentOrchestrator)
    ↓
agents (BaseAgent, ExecutorAgent, etc.)
    ↓
chief (ChiefOfStaffEngine)
    ↓
debate (DebateEngine) [if needed]
    ↓
approval (ApprovalService) [if needed]
```

**Flow Pattern:** Distributed across 7 packages

#### Kernel Packages
**Centralized Flow:**
```
kernels/multiagent (MultiAgentService)
    ↓
kernels/chief (ChiefOfStaffEngine)
```

**Flow Pattern:** Centralized with clear separation

### Delegation Flow

#### Legacy Packages
**No Centralized Delegation:**
- Delegation spread across packages
- No clear delegation mechanism
- Each package handles its own delegation

**Delegation Pattern:** Distributed delegation

#### Kernel Packages
**Centralized Delegation:**
- MultiAgentService handles delegation
- ChiefOfStaffEngine handles executive delegation
- Clear delegation flow

**Delegation Pattern:** Centralized delegation

### Extension Points

#### Legacy Packages
**Limited Extension:**
- No interfaces for extension
- Direct implementation
- Must modify existing classes

**Extension Type:** Limited

#### Kernel Packages
**Multiple Extension Points:**
- ChiefOfStaffEngine interface for custom implementations
- MultiAgentService interface for custom implementations
- AgentRegistry for custom agent registration
- CapabilityMatcher for custom capability matching
- Various validators for custom validation

**Extension Type:** Highly extensible

### Statistics

| Package | Files | Interfaces | Classes | Interfaces % |
|---------|-------|------------|---------|--------------|
| agents | 5 | 0 | 5 | 0% |
| chief | 2 | 0 | 2 | 0% |
| orchestrator | 1 | 0 | 1 | 0% |
| debate | 16 | 0 | 16 | 0% |
| approval | 1 | 0 | 1 | 0% |
| capability | 10 | 0 | 10 | 0% |
| intent | 2 | 0 | 2 | 0% |
| **Total legacy** | **37** | **0** | **37** | **0%** |
| kernels/chief | 40 | 8 | 32 | 25% |
| kernels/multiagent | 43 | 9 | 35 | 20.9% |

**Interface Adoption:**
- Legacy: 0% interface-based design
- Kernel chief: 25% interface-based design
- Kernel multiagent: 20.9% interface-based design

### Design Patterns

#### Legacy Packages
- **Direct Implementation** - No interfaces
- **Engine Pattern** - DebateEngine, ChiefOfStaffEngine
- **Registry Pattern** - CapabilityRegistry
- **Classifier Pattern** - IntentClassifier
- **Orchestrator Pattern** - AgentOrchestrator
- **Tight Coupling** - None (all standalone)

#### Kernel Packages
- **Interface-Based Design** - 8 interfaces in chief, 9 in multiagent
- **Engine Pattern** - ChiefOfStaffEngine, MultiAgentEngine
- **Service Pattern** - Service layer with interfaces
- **Registry Pattern** - AgentRegistry
- **Validator Pattern** - Multiple validators
- **Exception Hierarchy** - Comprehensive error handling
- **Verification Pattern** - Architecture verification
- **Coordinator Pattern** - AgentCoordinator, CommunicationCoordinator

### Strengths

#### Legacy Packages
1. **Debate Engine:** Sophisticated debate and swarm intelligence
2. **Capability Registry:** Comprehensive capability management
3. **Intent Classification:** Intent routing
4. **Approval Workflows:** Approval management
5. **Agent Roles:** Clear agent role definitions
6. **Self-Contained:** No dependencies
7. **Swarm Intelligence:** Adaptive swarm selection

#### Kernel Packages
1. **Interface-Based:** Highly extensible (8 interfaces in chief, 9 in multiagent)
2. **Validated:** Comprehensive validation layer
3. **Verified:** Architecture verification
4. **Error Handling:** Comprehensive exception hierarchy
5. **Platform Integration:** Deep platform integration
6. **Separation of Concerns:** Clear separation between chief and multiagent
7. **Agent Management:** Comprehensive agent lifecycle management

### Considerations

#### Legacy Packages
1. **No Interfaces:** Limited extensibility (0 interfaces)
2. **No Validation:** No input validation
3. **No Error Handling:** No exception hierarchy
4. **No Verification:** No architecture verification
5. **Flat Structure:** No layering
6. **Distributed Logic:** Logic spread across 7 packages
7. **No Integration:** No platform integration

---

## 10. Capability Mapping

### Legacy to Kernel Capability Mapping

**Evolution Path:**
```
platform/agents + orchestrator + debate + capability + intent (foundation)
    ↓
platform/kernels/multiagent (expansion)

platform/chief (foundation)
    ↓
platform/kernels/chief (expansion)
```

| Capability | Legacy Owner | Kernel Owner | Migration Status |
|------------|--------------|--------------|------------------|
| Agent Registry | agents (BaseAgent) | multiagent (AgentRegistry) | Evolved |
| Agent Lifecycle | agents (BaseAgent) | multiagent (AgentLifecycleService) | Evolved |
| Capability Routing | capability (CapabilityRegistry) | multiagent (CapabilityMatcher) | Evolved |
| Task Delegation | orchestrator (AgentOrchestrator) | multiagent (MultiAgentService) | Evolved |
| Debate | debate (DebateEngine) | chief (ChiefOfStaffEngine) | Evolved |
| Consensus | debate (JudgeAgent, SwarmJudge) | chief (ChiefOfStaffEngine) | Evolved |
| Approval | approval (ApprovalService) | chief (ChiefOfStaffEngine) | Evolved |
| Intent Routing | intent (IntentClassifier) | multiagent (AgentDiscovery) | Evolved |
| Goal Routing | Not present | multiagent (AgentDiscovery) | Added |
| Tool Selection | capability (CapabilityMatch) | multiagent (CapabilityMatcher) | Evolved |
| Orchestration | orchestrator (AgentOrchestrator) | chief (ChiefOfStaffEngine) | Evolved |
| Chief Governance | chief (ChiefOfStaffEngine) | chief (ChiefOfStaffEngine) | Evolved |
| Multi-Agent Coordination | debate (DebateEngine) | multiagent (MultiAgentService) | Evolved |
| Swarm Intelligence | debate (swarm/) | Not present | Not migrated |
| Agent Negotiation | debate (CriticAgent, ProposerAgent) | multiagent (AgentCommunication) | Evolved |
| Voting | debate (SwarmJudge) | Not present | Not migrated |
| Arbitration | debate (JudgeAgent) | chief (ChiefOfStaffEngine) | Evolved |
| Governance Rules | Not present | chief (ChiefValidator) | Added |
| Orchestration Strategies | Not present | chief (ChiefOfStaffEngine) | Added |
| Recursive Delegation | Not present | multiagent (MultiAgentService) | Added |

**Migration Status Legend:**
- **Evolved** - Capability exists in legacy and was expanded in kernel
- **Added** - Capability added in kernel architecture
- **Not migrated** - Capability exists in legacy but not in kernel
- **Not present** - Capability not found in either package

---

## Conclusion

The `platform/agents`, `platform/chief`, `platform/orchestrator`, `platform/debate`, `platform/approval`, `platform/capability`, and `platform/intent` packages represent **legacy agent orchestration and multi-agent coordination** implementations that have been superseded by the more structured `platform/kernels/chief` and `platform/kernels/multiagent` architectures.

**Key Observations:**

1. **Distributed Legacy Architecture:** The legacy system has 7 separate packages for agent/orchestration functionality, while the kernel architecture consolidates this into 2 kernels (chief and multiagent).

2. **Unique Legacy Capabilities:**
   - Debate engine with swarm intelligence
   - Approval workflows
   - Capability registry with specific capabilities
   - Intent classification
   - Agent role definitions

3. **Architecture Evolution:**
   - Legacy: Flat, 0% interface-based, 7 standalone packages
   - Kernel: Layered, 20-25% interface-based, 2 integrated kernels

4. **Direct Evolution:** The presence of ChiefOfStaffEngine and ChiefInsight in both platform/chief and kernels/chief indicates direct evolution.

5. **Missing in Kernel:** Some legacy capabilities are not present in kernels:
   - Swarm intelligence (debate/swarm)
   - Voting mechanisms
   - Approval workflows

6. **Platform Integration:** The kernel packages have deep platform integration (core, runtime, cognitive, planning, knowledge, memory), while legacy packages are fully standalone.

**Architecture Evolution Path:**
```
platform/agents + orchestrator + debate + capability + intent (37 files)
    ↓
platform/kernels/multiagent (43 files)

platform/chief (2 files)
    ↓
platform/kernels/chief (40 files)
```

**Implications:**
- The debate and swarm intelligence capabilities from platform/debate may need special consideration during any migration
- The capability registry from platform/capability provides a foundation for the kernel's capability matching
- The intent classification from platform/intent provides foundational concepts for agent discovery
- The approval workflows from platform/approval may need to be reimplemented in the kernel architecture if needed

---

*This audit was generated through automated static analysis. No files were modified during this analysis.*