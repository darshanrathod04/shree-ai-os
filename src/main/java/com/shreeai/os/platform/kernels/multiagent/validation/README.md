# Multi-Agent Kernel — Validation Layer

## Package Purpose

This package contains the structural validation layer for the Multi-Agent Kernel of Shree AI OS. The Validation Layer ensures that all Multi-Agent requests, registrations, capability definitions, lifecycle requests, discovery criteria, and Chief-mediated communication metadata are structurally valid before entering the Service Layer.

## Validation Flow

```
MultiAgentRequest
        │
        ▼
MultiAgentValidator
        │
 ┌──────┼─────────────────────────────────────────────┐
 ▼      ▼          ▼          ▼          ▼            ▼

Registration  Discovery  Capability  Lifecycle  Communication  Criteria

        │
        ▼
Aggregate Results
        │
        ▼
MultiAgentValidationResult
```

The `MultiAgentValidator` façade coordinates validation across all components. It delegates to specialized validators and aggregates their results into a single `MultiAgentValidationResult`.

## Validators

### MultiAgentValidator

The validation façade that coordinates all validators. It:
- Delegates validation to specialized validators
- Aggregates validation results
- Returns immutable `MultiAgentValidationResult`
- Does NOT perform validation logic directly

### AgentRegistrationValidator

Validates agent registration metadata:
- AgentId structure
- AgentType format
- Capability definitions
- Registration timestamps
- Ownership metadata

**Does NOT:** Register agents, update registry, or perform any registration logic.

### AgentDiscoveryValidator

Validates discovery criteria and metadata:
- Discovery criteria structure
- Filtering metadata
- Capability search parameters
- Status filters

**Does NOT:** Discover agents, execute searches, or perform filtering.

### CapabilityValidator

Validates capability definitions:
- Capability identifiers
- Capability metadata
- Capability uniqueness
- Version format

**Does NOT:** Register capabilities, modify capabilities, or validate implementations.

### LifecycleValidator

Validates lifecycle requests and transitions:
- Lifecycle request structure
- AgentStatus consistency
- State value validation
- Transition metadata

**Does NOT:** Start agents, stop agents, pause agents, resume agents, or execute lifecycle transitions.

### CommunicationValidator

Validates communication metadata and enforces Chief-mediated communication:
- Correlation identifiers
- Sender metadata
- Receiver metadata
- Routing metadata
- **Chief mediation invariant**

**Does NOT:** Perform routing, transport, or networking.

### MultiAgentCriteriaValidator

Validates shared criteria used across multiple operations:
- Registration criteria
- Discovery criteria
- Lifecycle criteria
- Communication criteria

**Contains NO business logic.**

## Architectural Boundaries

### What Validation Can Do

✓ Null checks
✓ Required field validation
✓ Immutable collection validation
✓ Identifier validation
✓ Metadata validation
✓ Lifecycle consistency checks
✓ Capability uniqueness checks
✓ Chief-mediated routing validation

### What Validation Cannot Do

✗ Register agents
✗ Discover agents
✗ Schedule work
✗ Route communications
✗ Perform networking
✗ Orchestrate agents
✗ Persist data
✗ Maintain mutable state
✗ Implement business logic

## Communication Invariant

The Validation Layer enforces the critical architectural invariant that all communication must flow through the Chief Kernel:

```
Agent A
    │
    ▼
Chief Kernel
    │
    ▼
Agent B
```

**Direct agent-to-agent communication is architecturally invalid.**

The `CommunicationValidator` rejects any structure representing direct communication:

```
Agent A
────────────►
Agent B
```

### Chief-Mediation Validation

The `CommunicationValidator` checks for:

1. **Direct communication indicators** — Rejects metadata with `direct=true`
2. **Missing Chief routing** — Warns if metadata lacks `chiefId` or `chiefKernelId`
3. **Transport/protocol indicators** — Warns if metadata contains transport logic
4. **Networking indicators** — Warns if metadata contains endpoint/address information

## Validation Rules

Validation is **STRUCTURAL ONLY**.

### Allowed Validations

- Null checks
- Required fields
- Immutable collections
- Identifier validation
- Metadata validation
- Lifecycle consistency
- Capability uniqueness
- Routing metadata validation

### Forbidden Operations

- Registration
- Discovery
- Scheduling
- Communication routing
- Networking
- Orchestration
- Persistence
- Mutable state

## Architectural Invariants

The Validation Layer enforces these critical distinctions:

```
AgentDescriptor != AgentRuntime
AgentCapability != CapabilityImplementation
AgentCommunication != MessageTransport
AgentRegistration != RegistrationLogic
AgentStatus != LifecycleExecution
```

Validation verifies **metadata only**. It never validates runtime execution.

## Value Object Implementation

### MultiAgentValidationResult

Immutable result object containing:
- `valid` — Whether validation passed
- `issues` — List of validation issues (failures)
- `warnings` — List of validation warnings (non-critical)

Factory methods:
- `success()` — Creates successful result
- `failure(String issue)` — Creates failed result with single issue
- `failure(List<String> issues)` — Creates failed result with multiple issues
- `withWarning(String warning)` — Adds warning to result (returns new instance)

### Stateless Validators

All validators are:
- **Stateless** — No mutable state
- **Immutable** — No modification after construction
- **Thread-safe** — Can be used concurrently
- **Final classes** — Cannot be extended

## Dependencies

### Allowed

```
platform.kernels.multiagent.api.*
platform.kernels.multiagent.model.*
java.util.*
```

### Forbidden

```
service
engine
runtime
repository
database
network
memory
planning
knowledge
reasoning
framework annotations
```

## Usage Example

```java
// Create validators
AgentRegistrationValidator registrationValidator = new AgentRegistrationValidator();
AgentDiscoveryValidator discoveryValidator = new AgentDiscoveryValidator();
CapabilityValidator capabilityValidator = new CapabilityValidator();
LifecycleValidator lifecycleValidator = new LifecycleValidator();
CommunicationValidator communicationValidator = new CommunicationValidator();
MultiAgentCriteriaValidator criteriaValidator = new MultiAgentCriteriaValidator();

// Create façade
MultiAgentValidator validator = new MultiAgentValidator(
    registrationValidator,
    discoveryValidator,
    capabilityValidator,
    lifecycleValidator,
    communicationValidator,
    criteriaValidator
);

// Validate registration
AgentRegistration registration = ...;
MultiAgentValidationResult result = validator.validateRegistration(registration);

if (result.valid()) {
    // Proceed to Service Layer
} else {
    // Handle validation failures
    for (String issue : result.issues()) {
        System.err.println("Validation issue: " + issue);
    }
}

// Check warnings
for (String warning : result.warnings()) {
    System.out.println("Validation warning: " + warning);
}
```

## Comprehensive Validation

```java
// Validate all components at once
MultiAgentValidationResult result = validator.validateAll(
    registration,   // May be null
    descriptor,     // May be null
    request,        // May be null
    communication   // May be null
);

if (result.valid()) {
    // All validations passed
}
```

## Lifecycle State Validation

The `LifecycleValidator` recognizes these states:

- `REGISTERED` — Agent is registered but not yet started
- `STARTING` — Agent is in the process of starting
- `RUNNING` — Agent is actively running
- `PAUSED` — Agent is temporarily paused
- `STOPPED` — Agent has been stopped
- `UNREGISTERED` — Agent is no longer registered

**Note:** These are string values. The Validation Layer does not enforce state transitions. State transition logic belongs to the future Service Layer.

## Code Quality

All validators follow:
- **SOLID** principles
- **Clean Code** practices
- **Stateless Design** — No mutable state
- **Immutability** — All fields are final
- **Defensive Programming** — Constructor validation, null checks

## Thread Safety

All validators are thread-safe because they are:
- Stateless (no instance variables)
- Immutable (no modification methods)
- Final classes (cannot be extended)

## Constitutional Authority

This package is defined by:
- **MAGENT-103** — Multi-Agent Validation Layer (this document)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Related Documentation

- **MAGENT-101** — Multi-Agent Public API
- **MAGENT-102** — Multi-Agent Domain Models
- **MAGENT-104** — Multi-Agent Error Architecture (next)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Version History

- **1.0** — Initial validation layer (MAGENT-103)