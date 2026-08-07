# Multi-Agent Kernel API Layer

## Overview

The Multi-Agent Kernel API Layer provides the public contracts for the Multi-Agent Kernel. The API defines how agents register, unregister, advertise capabilities, discover peers, manage lifecycle, and communicate. All coordination flows through the Chief Kernel.

## API Philosophy

The API Layer exists solely to define contracts. It does not implement any logic.

> **"What can agents do?"**

It does **not** answer:
- "How do agents register?"
- "How is discovery performed?"
- "How does communication work?"

Those responsibilities belong to other architectural layers:
- **Model Layer (MAGENT-102)**: defines data structures
- **Validation Layer (MAGENT-103)**: validates structure
- **Error Layer (MAGENT-104)**: represents failures
- **Service Layer (MAGENT-105)**: implements service logic
- **Engine Layer (MAGENT-106)**: performs computation
- **Chief Kernel**: coordinates all orchestration

## API Architecture

```
                    Applications

                          │

                          ▼

                Multi-Agent Public API

                          │

        ┌─────────────────────────────────┐

        ▼                                 ▼

 MultiAgentService                 Specialized Services

        │

        ├──────── AgentRegistryService

        ├──────── AgentDiscoveryService

        ├──────── CapabilityRegistryService

        ├──────── AgentLifecycleService

        └──────── AgentCommunicationService

                          │

                          ▼

                  Chief Kernel

                          │

                          ▼

              Remaining Platform Kernels
```

## Components

### MultiAgentService

**Primary façade.**

Responsibilities:
- Provides primary entry point for agent operations
- Coordinates specialized agent services
- Delegates to Chief Kernel for all coordination

Methods:
- `registerAgent(AgentRequest)` — registers a new agent
- `unregisterAgent(String)` — unregisters an agent
- `discoverAgents(AgentRequest)` — discovers agents
- `communicate(AgentCommunication)` — sends communication
- `getKernelHealth()` — retrieves kernel health

Properties:
- Interface-only
- No implementation
- No business logic

### AgentRegistryService

**Agent registration service.**

Responsibilities:
- Defines agent registration contracts
- Defines agent unregistration contracts
- Defines registration query contracts

Methods:
- `register(AgentRegistration)` — registers an agent
- `unregister(String)` — unregisters an agent
- `updateRegistration(AgentRegistration)` — updates registration
- `findRegistration(String)` — finds registration
- `listRegistrations()` — lists all registrations

Properties:
- Interface-only
- No implementation
- No business logic

### AgentDiscoveryService

**Agent discovery service.**

Responsibilities:
- Defines agent discovery contracts
- Defines capability-based discovery contracts
- Defines metadata-based discovery contracts

Methods:
- `discoverByCapability(AgentCapability)` — discovers by capability
- `discoverByStatus(AgentStatus)` — discovers by status
- `discoverByMetadata(Map)` — discovers by metadata
- `listAvailableAgents()` — lists all available agents

Properties:
- Interface-only
- No implementation
- No business logic

### CapabilityRegistryService

**Capability registry service.**

Responsibilities:
- Defines capability registration contracts
- Defines capability query contracts
- Defines capability validation contracts

Methods:
- `registerCapability(AgentCapability)` — registers capability
- `removeCapability(AgentCapability)` — removes capability
- `queryCapabilities(AgentRequest)` — queries capabilities
- `validateCapabilities(String)` — validates capabilities

Properties:
- Interface-only
- No implementation
- No business logic

### AgentLifecycleService

**Agent lifecycle service.**

Responsibilities:
- Defines agent lifecycle contracts
- Defines agent state transition contracts
- Defines lifecycle query contracts

Methods:
- `start(String)` — starts an agent
- `stop(String)` — stops an agent
- `pause(String)` — pauses an agent
- `resume(String)` — resumes an agent
- `getLifecycle(String)` — retrieves lifecycle state

Properties:
- Interface-only
- No implementation
- No business logic

### AgentCommunicationService

**Agent communication service.**

Responsibilities:
- Defines agent communication contracts
- Enforces Chief Kernel routing for all communication
- Defines communication status contracts

Methods:
- `send(AgentCommunication)` — sends communication
- `receive(String)` — receives communication
- `routeThroughChief(AgentCommunication)` — routes through Chief
- `getCommunicationStatus(String)` — retrieves communication status

Properties:
- Interface-only
- No implementation
- No business logic

### MultiAgentTypes

**Bootstrap types holder.**

Responsibilities:
- Provides immutable bootstrap records
- Contains shared API types
- No business logic

Records:
- `AgentRequest` — agent request
- `AgentResponse` — agent response
- `AgentDescriptor` — agent descriptor
- `AgentCapability` — agent capability
- `AgentRegistration` — agent registration
- `AgentStatus` — agent status
- `AgentCommunication` — agent communication
- `MultiAgentMetrics` — kernel metrics

Properties:
- Immutable records
- Constructor validation
- Defensive copying
- Value semantics

## Communication Architecture

All communication MUST flow through the Chief Kernel:

```
Agent A
   │
   ▼
Chief Kernel
   │
   ▼
Agent B
```

**Forbidden:**
```
Agent A
──────────────►
Agent B
```

Never define methods that enable direct agent-to-agent communication.

## Bootstrap Records

The API includes temporary bootstrap records that will be replaced during MAGENT-102:

**AgentRequest** — Immutable record representing an agent request
**AgentResponse** — Immutable record representing an agent response
**AgentDescriptor** — Immutable record describing an agent
**AgentCapability** — Immutable record representing an agent capability
**AgentRegistration** — Immutable record representing an agent registration
**AgentStatus** — Immutable record representing an agent status
**AgentCommunication** — Immutable record representing agent communication
**MultiAgentMetrics** — Immutable record representing kernel metrics

All records:
- Are immutable
- Validate required fields in constructors
- Use defensive copying for collections
- Provide value semantics (equals, hashCode, toString)

## Design Principles

### Contracts Only

The API Layer defines contracts only:
- No implementation logic
- No business logic
- No orchestration logic
- No networking logic
- No persistence logic

### Chief Kernel Coordination

All coordination flows through the Chief Kernel:
- All registration flows through Chief
- All discovery flows through Chief
- All lifecycle flows through Chief
- All communication flows through Chief

### Immutable Types

All bootstrap types are immutable:
- Records are immutable by default
- Constructor validation with Objects.requireNonNull()
- Defensive copying for collections
- No setters

### No Direct Communication

Agent-to-agent communication is forbidden:
- All communication must flow through Chief Kernel
- No direct agent-to-agent methods
- No peer-to-peer communication

## Usage Example

```java
// Create agent request
AgentRequest request = new AgentRequest(
    "agent-123",
    "worker",
    List.of(new AgentCapability("cap-1", "computation", "Computes tasks", Map.of())),
    Map.of("version", "1.0")
);

// Register agent
AgentResponse response = multiAgentService.registerAgent(request);

// Discover agents
List<AgentDescriptor> agents = multiAgentService.discoverAgents(request);

// Send communication through Chief
AgentCommunication communication = new AgentCommunication(
    "comm-123",
    "agent-123",
    "agent-456",
    "task",
    "Execute task",
    Instant.now(),
    Map.of()
);

AgentResponse commResponse = multiAgentService.communicate(communication);
```

## Architectural Boundaries

### Responsible For

The API Layer is responsible for:
- Defining service contracts
- Defining immutable bootstrap types
- Enforcing Chief Kernel coordination
- Preventing direct agent-to-agent communication

### Not Responsible For

The API Layer is **not** responsible for:
- Implementing services
- Implementing networking
- Implementing discovery
- Implementing lifecycle
- Implementing messaging
- Enabling direct agent-to-agent communication
- Accessing persistence
- Accessing runtime

## Separation from Other Layers

The API Layer is intentionally separated from other Multi-Agent Kernel layers:

```
API  ← You are here
 ↓
Model
 ↓
Validation
 ↓
Error
 ↓
Service
 ↓
Engine
 ↓
Chief Kernel
```

This separation ensures:
- API remains pure contract definitions
- No implementation logic in API layer
- Clear architectural boundaries
- Independent evolution of API and implementation

## Compliance

This implementation complies with:
- **Kernel Development Standard (EIO-ARCH-001)**
- **Multi-Agent Kernel Architecture (MAGENT-101)**

## Package Structure

```
platform.kernels.multiagent.api
├── MultiAgentService.java              # Primary façade
├── AgentRegistryService.java           # Agent registration
├── AgentDiscoveryService.java          # Agent discovery
├── CapabilityRegistryService.java      # Capability management
├── AgentLifecycleService.java          # Lifecycle management
├── AgentCommunicationService.java      # Communication
├── MultiAgentTypes.java                # Bootstrap types
├── package-info.java                   # Package documentation
└── README.md                           # This file
```

## Migration Note

These bootstrap records are temporary and will be replaced during MAGENT-102 with canonical domain models. Do not depend on these records in production code.

## Future Extensibility

The API architecture supports future extensibility through:
- **New service interfaces**: Add new interfaces for new capabilities
- **New bootstrap records**: Add new records for new types
- **New service methods**: Add new methods to existing interfaces
- **Contract evolution**: Extend contracts without breaking changes

## Version History

- **1.0** (2026-07-21): Initial implementation per MAGENT-101