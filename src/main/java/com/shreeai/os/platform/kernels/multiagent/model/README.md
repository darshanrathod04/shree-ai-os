# Multi-Agent Kernel — Domain Model

## Package Purpose

This package contains the canonical domain models for the Multi-Agent Kernel of Shree AI OS. These models define the vocabulary and language of the multi-agent system.

## Canonical Domain Language

The domain models in this package represent:

- **AgentId** — Canonical identity value object for agents
- **AgentRequest** — Immutable requests for registration, discovery, lifecycle, and communication
- **AgentResponse** — Immutable responses returned by the Multi-Agent Kernel
- **AgentDescriptor** — Metadata describing an agent (identity, capabilities, status, priority, tags)
- **AgentCapability** — One advertised capability of an agent
- **AgentRegistration** — Registration state of an agent
- **AgentStatus** — Lifecycle state of an agent
- **AgentCommunication** — Communication metadata between agents
- **MultiAgentMetrics** — Runtime metrics for the Multi-Agent Kernel
- **AgentSnapshot** — Immutable snapshot of one agent at a point in time

## Metadata vs Runtime

**Critical Architectural Distinction:**

- **Domain Models** (this package) = Metadata only
- **Runtime Behavior** = Future Service and Processing Engine layers

Domain models describe *what* exists. They do not perform *how* it works.

### Example Separation

```
AgentDescriptor
    │
    │ describes
    ▼
Agent Runtime
    │
    │ executes
    ▼
Chief Kernel
```

- `AgentDescriptor` contains metadata (identity, capabilities, priority)
- `Agent Runtime` contains execution logic
- `Chief Kernel` is the orchestration layer

## Migration from Bootstrap Records

These canonical models replace the temporary bootstrap records introduced in MAGENT-101 (package: `platform.kernels.multiagent.api`).

### What Changed

| Bootstrap Record (MAGENT-101) | Canonical Model (MAGENT-102) |
|-------------------------------|-------------------------------|
| `api.AgentRequest` (record) | `model.AgentRequest` (class) |
| `api.AgentResponse` (record) | `model.AgentResponse` (class) |
| `api.AgentDescriptor` (record) | `model.AgentDescriptor` (class) |
| `api.AgentCapability` (record) | `model.AgentCapability` (class) |
| `api.AgentRegistration` (record) | `model.AgentRegistration` (class) |
| `api.AgentStatus` (record) | `model.AgentStatus` (class) |
| `api.AgentCommunication` (record) | `model.AgentCommunication` (class) |
| `api.MultiAgentMetrics` (record) | `model.MultiAgentMetrics` (class) |
| *(not present)* | `model.AgentId` (NEW) |
| *(not present)* | `model.AgentSnapshot` (NEW) |

### Migration Transparency

The migration is **transparent to API consumers**. All API interfaces now reference the canonical models in the `model` package. The public API behavior remains unchanged.

## Architectural Boundaries

### Allowed Dependencies

```
java.util.*
java.time.*
platform.common.*
```

### Forbidden Dependencies

```
service
validation
engine
runtime
network
repository
database
memory
planning
knowledge
reasoning
framework annotations
```

### What Domain Models Can Do

✓ Define immutable data structures
✓ Validate constructor arguments
✓ Implement value semantics (equals, hashCode, toString)
✓ Represent state and metadata

### What Domain Models Cannot Do

✗ Perform business logic
✗ Execute validation rules (future Validation Layer)
✗ Access databases or repositories
✗ Communicate over networks
✗ Schedule or orchestrate work
✗ Access runtime state
✗ Implement services or processing

## Value Object Implementation

All domain models follow strict value object patterns:

### Immutability

```java
public final class AgentId {
    private final String value;
    // No setters
    // No mutation methods
}
```

### Constructor Validation

```java
public AgentId(String value) {
    this.value = Objects.requireNonNull(value, "AgentId value must not be null");
    if (value.isBlank()) {
        throw new IllegalArgumentException("AgentId value must not be blank");
    }
}
```

### Defensive Copying

```java
this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata must not be null"));
this.capabilities = List.copyOf(Objects.requireNonNull(capabilities, "capabilities must not be null"));
```

### Value Semantics

```java
@Override
public boolean equals(Object obj) {
    // Value-based equality
}

@Override
public int hashCode() {
    // Consistent with equals
}

@Override
public String toString() {
    // Human-readable representation
}
```

## Communication Model

All agent communication is **Chief-mediated**. The `AgentCommunication` model represents metadata only:

- correlationId — Links related messages
- senderId — Originating agent
- receiverId — Destination agent
- timestamp — When communication occurred
- metadata — Additional context

**No transport, networking, or routing logic** is included in the domain model.

## Lifecycle States

Common agent lifecycle states (represented in `AgentStatus`):

- `REGISTERED` — Agent is registered but not yet started
- `STARTING` — Agent is in the process of starting
- `RUNNING` — Agent is actively running
- `PAUSED` — Agent is temporarily paused
- `STOPPED` — Agent has been stopped
- `UNREGISTERED` — Agent is no longer registered

**Note:** These are string values. The domain model does not enforce state transitions. State transition logic belongs to the future Service Layer.

## Metrics

`MultiAgentMetrics` represents runtime metrics as data:

- totalRegistrations — Cumulative registrations
- activeAgents — Currently active agents
- communicationCount — Total communications
- measuredAt — When metrics were captured
- metadata — Additional metrics context

**No calculations or monitoring logic** is included. Metrics are collected by the future Service Layer and represented by this model.

## Snapshots

`AgentSnapshot` provides an immutable point-in-time view of an agent:

```java
AgentSnapshot snapshot = new AgentSnapshot(
    agentId,
    agentType,
    status,
    capabilities,
    priority,
    tags,
    metadata,
    Instant.now()
);
```

Snapshots are useful for:
- State inspection
- Audit trails
- Debugging
- Historical analysis

## Constitutional Authority

These models are defined by:
- **MAGENT-102** — Multi-Agent Domain Models (this document)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Related Documentation

- **MAGENT-101** — Multi-Agent Public API (previous, bootstrap records)
- **MAGENT-103** — Multi-Agent Validation Layer (next)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Version History

- **1.0** — Initial canonical domain models (MAGENT-102)