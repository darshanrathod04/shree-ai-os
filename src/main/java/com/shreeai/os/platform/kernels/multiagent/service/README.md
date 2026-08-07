# Multi-Agent Kernel — Service Layer

## Package Purpose

This package contains the thin orchestration façade for the Multi-Agent Kernel of Shree AI OS. The Service Layer coordinates validation, processing, and exception translation between the API layer and the Processing Engine.

## Delegation Flow

```
Applications
    │
    ▼
MultiAgentService (API)
    │
    ▼
DefaultMultiAgentService
    │
    ├──────── MultiAgentValidator
    │
    └──────── MultiAgentProcessingEngine
                    │
                    ▼
            Future Engine Layer
```

The Service Layer coordinates. The Processing Engine performs processing.

## Processing Flow

Every public operation follows this sequence:

```
Request
    │
    ▼
Validation
    │
    ▼
Processing Engine
    │
    ▼
Response
```

This flow is enforced for all operations:
- Agent registration
- Agent unregistration
- Agent discovery
- Agent communication
- Kernel health

## Components

### MultiAgentProcessingEngine (Interface)

Interface defining the processing contract for the Engine layer:

- `registerAgent(AgentRegistration)` — Register a new agent
- `unregisterAgent(String)` — Unregister an agent
- `discoverAgents(AgentRequest)` — Discover agents by criteria
- `communicate(AgentCommunication)` — Send communication
- `getKernelHealth()` — Retrieve kernel health metrics

**No implementation provided.** Implementation will be provided in MAGENT-106.

### DefaultMultiAgentService

Default implementation of `MultiAgentService` (API interface). It:

- **Validates** requests via `MultiAgentValidator` before processing
- **Delegates** all processing to `MultiAgentProcessingEngine`
- **Translates** failures into canonical exceptions
- **Remains stateless and thread-safe**

## Exception Translation

The Service Layer translates failures into the canonical exception hierarchy:

| Failure | Exception |
|---------|-----------|
| Validation failures | **MultiAgentValidationException** |
| Registration failures | **AgentRegistrationException** |
| Discovery failures | **AgentDiscoveryException** |
| Capability failures | **CapabilityException** |
| Lifecycle failures | **LifecycleException** |
| Communication failures | **CommunicationException** |
| Unexpected failures | **MultiAgentException** |

### Translation Flow

```
Error Occurs
    │
    ▼
Create MultiAgentError (immutable)
    │
    ▼
Wrap in Canonical Exception
    │
    ▼
Throw to Caller
```

## Service Rules

### What Service Layer Can Do

✓ Coordinate validation
✓ Delegate processing to engine
✓ Translate exceptions
✓ Maintain stateless coordination

### What Service Layer Cannot Do

✗ Select agents
✗ Rank capabilities
✗ Perform discovery
✗ Execute lifecycle transitions
✗ Route communications
✗ Schedule work
✗ Persist data
✗ Execute agents
✗ Implement business logic

## Dependencies

### Constructor Injection

All dependencies are injected through the constructor:

```java
public DefaultMultiAgentService(
        MultiAgentValidator validator,
        MultiAgentProcessingEngine processingEngine) {
    this.validator = validator;
    this.processingEngine = processingEngine;
}
```

**No setter injection.** **No mutable state.**

### Allowed Dependencies

```
platform.kernels.multiagent.api.*
platform.kernels.multiagent.model.*
platform.kernels.multiagent.validation.*
platform.kernels.multiagent.error.*
java.util.*
```

### Forbidden Dependencies

```
engine implementation
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
// Create dependencies
MultiAgentValidator validator = createValidator();
MultiAgentProcessingEngine engine = createProcessingEngine();

// Create service
DefaultMultiAgentService service = new DefaultMultiAgentService(validator, engine);

// Register an agent
AgentRequest request = new AgentRequest(
    "agent-123",
    "worker",
    capabilities,
    metadata
);

try {
    AgentResponse response = service.registerAgent(request);
    if (response.success()) {
        System.out.println("Agent registered: " + response.message());
    }
} catch (MultiAgentValidationException e) {
    System.err.println("Validation failed: " + e.getMessage());
} catch (AgentRegistrationException e) {
    System.err.println("Registration failed: " + e.getMessage());
}
```

## Code Quality

The Service Layer follows:
- **SOLID** principles
- **Clean Code** practices
- **Stateless Design** — No instance state beyond injected dependencies
- **Constructor Injection** — No setter injection
- **Thread Safety** — Stateless, immutable references

## Thread Safety

DefaultMultiAgentService is thread-safe because:
- All dependencies are immutable references
- No mutable state is maintained
- Validation and processing are delegated
- No shared mutable data

## Architectural Position

The Service Layer sits between the API and Engine layers:

```
Applications
    │
    ▼
Multi-Agent API ───────────────────────┐
    │                                   │
    ▼                                   │
Domain Models                          │
    │                                   │
    ▼                                   ▼
Validation Layer                    DefaultMultiAgentService
    │                                   │
    ▼                                   ├── MultiAgentValidator
Error Layer                             │
    │                                   └── MultiAgentProcessingEngine
    ▼                                           │
Service Layer ──────────────────────────────────┤
    │                                            │
    ▼                                            ▼
Future Processing Engine ────────────────────────┘
```

## Constitutional Authority

This package is defined by:
- **MAGENT-105** — Multi-Agent Service Layer (this document)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Related Documentation

- **MAGENT-101** — Multi-Agent Public API
- **MAGENT-102** — Multi-Agent Domain Models
- **MAGENT-103** — Multi-Agent Validation Layer
- **MAGENT-104** — Multi-Agent Error Architecture
- **MAGENT-106** — Multi-Agent Processing Engine (next)
- **EIO-ARCH-001** — Enterprise Integration Architecture

## Version History

- **1.0** — Initial service layer (MAGENT-105)