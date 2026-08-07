# Multi-Agent Kernel — Engine Layer

## Package Purpose

This package contains the deterministic processing engine for the Multi-Agent Kernel of Shree AI OS. The Engine layer computes deterministic processing outcomes for validated Multi-Agent operations.

## Architecture Position

```
MultiAgentService
    │
    ▼
DefaultMultiAgentService
    │
    ├──── MultiAgentValidator
    │
    ▼
MultiAgentProcessingEngine
    │
    ▼
DefaultMultiAgentProcessingEngine
    │
    ▼
MultiAgentProcessingResult
```

## Processing Responsibilities

The Engine evaluates/computes. It does NOT perform infrastructure execution.

| Operation | Engine Responsibility | NOT Responsible For |
|-----------|----------------------|---------------------|
| registerAgent | Evaluate registration metadata | Persisting agents, starting agents |
| unregisterAgent | Evaluate unregistration | Mutating storage, terminating processes |
| discoverAgents | Evaluate discovery criteria | Querying databases, network discovery |
| communicate | Evaluate Chief-mediated communication | Sending messages, opening sockets |
| getKernelHealth | Evaluate health from deterministic info | Network probes, infrastructure inspection |

## Deterministic Design

The engine is **deterministic** — the same input always produces the same output.

```java
// Given the same registration input
AgentRegistration input = ...;

// The engine always produces the same outcome
AgentResponse response1 = engine.registerAgent(input);
AgentResponse response2 = engine.registerAgent(input);
// response1 == response2
```

## Statelessness

The engine maintains **no mutable state**:

- No hidden registries
- No in-memory caches
- No mutable collections
- No static mutable fields

```java
// FORBIDDEN — no mutable registries in the engine
private final Map<String, AgentDescriptor> agents = new HashMap<>();
```

## Thread Safety

Thread safety results from stateless design:

- No shared mutable state
- All methods are reentrant
- No synchronization required
- Safe for concurrent use

## Service/Engine Separation

```
Service (MAGENT-105)                    Engine (MAGENT-106)
─────────────────────                   ─────────────────────
Coordinates                             Computes
Delegates validation                    Processes deterministically
Translates exceptions                   Produces outcomes
Orchestrates flow                       No infrastructure execution
```

## Validation/Engine Separation

```
Validation (MAGENT-103)                 Engine (MAGENT-106)
─────────────────────                   ─────────────────────
Validates structure                     Computes outcomes
Null checks                             Deterministic processing
Required field checks                   No validation logic
Metadata validation
```

The engine does NOT recreate validation logic. Engine inputs are treated as structurally validated by the service pipeline.

## Chief-Mediated Communication Invariant

The engine preserves the critical invariant that all communication flows through the Chief Kernel:

```
Agent
   │
   ▼
Multi-Agent Kernel
   │
   ▼
Chief-mediated coordination
   │
   ▼
Target capability / agent
```

The engine evaluates communication metadata but does NOT transport communication.

## Components

### MultiAgentProcessingEngine (Interface)

Public processing contract defining operations:
- `registerAgent(AgentRegistration)` — Registration evaluation
- `unregisterAgent(String)` — Unregistration evaluation
- `discoverAgents(AgentRequest)` — Discovery evaluation
- `communicate(AgentCommunication)` — Communication evaluation
- `getKernelHealth()` — Health evaluation

### MultiAgentProcessingResult

Immutable value object representing processing outcome:
- `succeeded` — Whether processing succeeded
- `outcome` — Descriptive outcome
- `processedAt` — Processing timestamp
- `metadata` — Processing context

### DefaultMultiAgentProcessingEngine

Default implementation providing:
- Deterministic processing outcomes
- Stateless evaluation
- Infrastructure independence
- Thread-safe operation

## Processing Operations

### Registration Processing
```
Registration Input
    │
    ▼
Evaluate Metadata
    │
    ▼
Produce AgentResponse
```

**Does NOT:** Persist agents, maintain registry, start agents, contact external systems.

### Unregistration Processing
```
Unregistration Request
    │
    ▼
Evaluate Request
    │
    ▼
Produce AgentResponse
```

**Does NOT:** Mutate storage, terminate processes, perform cleanup, communicate externally.

### Discovery Processing
```
Discovery Criteria
    │
    ▼
Evaluate Criteria
    │
    ▼
Produce Descriptor List
```

**Does NOT:** Query databases, perform network discovery, call remote registries, maintain hidden registries.

### Communication Processing
```
Communication Metadata
    │
    ▼
Evaluate Chief-Mediated Invariant
    │
    ▼
Produce AgentResponse
```

**Does NOT:** Send messages, open sockets, call endpoints, use brokers, transport payloads.

### Health Evaluation
```
Health Request
    │
    ▼
Evaluate Deterministic State
    │
    ▼
Produce MultiAgentMetrics
```

**Does NOT:** Perform network probes, query databases, inspect infrastructure, use monitoring frameworks.

## Migration from MAGENT-105

The `MultiAgentProcessingEngine` interface was migrated from:
- **OLD:** `platform.kernels.multiagent.service.MultiAgentProcessingEngine`
- **NEW:** `platform.kernels.multiagent.engine.MultiAgentProcessingEngine`

`DefaultMultiAgentService` was updated to depend on the canonical engine-package contract.
The obsolete service-package interface was removed.

## Dependencies

### Allowed
```
platform.kernels.multiagent.model.*
java.util.*
java.time.*
```

### Forbidden
```
Persistence
Networking
Message transport
Scheduling
Agent execution
Framework annotations
```

## Code Quality

The Engine layer follows:
- **Deterministic Design** — Same input always produces same output
- **Statelessness** — No mutable state, hidden registries, or caches
- **Thread Safety** — Reentrant methods, no synchronization required
- **SOLID** principles
- **Clean Code** practices
- **Infrastructure Independence** — No external system dependencies

## Constitutional Authority

This package is defined by:
- **MAGENT-106** — Multi-Agent Processing Engine (this document)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Related Documentation

- **MAGENT-101** — Multi-Agent Public API
- **MAGENT-102** — Multi-Agent Domain Models
- **MAGENT-103** — Multi-Agent Validation Layer
- **MAGENT-104** — Multi-Agent Error Architecture
- **MAGENT-105** — Multi-Agent Service Layer
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Version History

- **1.0** — Initial processing engine (MAGENT-106)