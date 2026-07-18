# Context Kernel API

## Purpose

The Context Kernel API defines the public contracts for managing temporary runtime state within the platform. The Context Kernel represents the current runtime state of the platform - it is not Memory, and it is not Identity. It maintains temporary execution context only.

## Architecture

The Context Kernel follows a layered architecture:

```
                   User
                     │
                     ▼
            Context Kernel
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
 Conversation   Execution     Session
   Context       Context      Context
        │            │            │
        └────────────┼────────────┘
                     ▼
              Context Snapshot
```

## API Philosophy

The Context Kernel API follows strict principles:

- **Interface Only** - All APIs are interfaces, no implementations
- **No Business Logic** - APIs define contracts only
- **Thread-Safe** - All operations must be thread-safe
- **Immutable Returns** - All returned objects must be immutable
- **Read-Only Queries** - Query operations never modify state

## Lifecycle

Context lifecycle states:

1. **Active** - Context is active and can be modified
2. **Suspended** - Context is preserved but cannot be modified
3. **Expired** - Context has reached its end of life
4. **Archived** - Context is archived for historical reference

## Responsibilities

The Context Kernel manages only:

- Active conversation context
- Runtime execution context
- Current task context
- Session context
- Temporary working context
- Environmental context
- Context lifecycle

The Context Kernel never stores long-term information. Long-term information belongs to the Memory Kernel.

## API Contracts

### ContextService

Manages Context lifecycle operations:

- `createContext(CreateContextRequest)` - Creates a new Context
- `updateContext(UpdateContextRequest)` - Updates an existing Context
- `clearContext(ContextId)` - Clears all data from a Context
- `suspendContext(ContextId)` - Suspends a Context
- `resumeContext(ContextId)` - Resumes a suspended Context

### ContextQueryService

Provides read-only query operations:

- `findById(ContextId)` - Finds a Context by identifier
- `findActiveContexts()` - Finds all active Contexts
- `exists(ContextId)` - Checks if a Context exists

### ContextSnapshotService

Manages Context snapshots:

- `createSnapshot(ContextId)` - Creates a snapshot of a Context
- `latestSnapshot(ContextId)` - Retrieves the latest snapshot
- `history(ContextId)` - Retrieves snapshot history

**Snapshot Principles:**
- Snapshots represent runtime state only
- Snapshots are not Memory
- Snapshots are temporary and lightweight
- Snapshots capture execution context at a point in time

### ContextLifecycleService

Manages Context lifecycle state transitions:

- `activate(ContextId)` - Activates a Context
- `deactivate(ContextId)` - Deactivates a Context
- `expire(ContextId)` - Expires a Context
- `archive(ContextId)` - Archives a Context

## Dependencies

### Allowed

- Platform Core
- Identity Kernel (read-only references only)
- Memory Kernel (read-only references only)

### Forbidden

- Planning Kernel
- Chief Kernel
- Knowledge Kernel
- Execution Kernel
- LLM integrations
- Networking
- Persistence
- UI

## Context vs Memory

**Context is:**
- Temporary
- Mutable
- Runtime-only
- Lightweight
- Fast

**Memory is:**
- Persistent
- Historical
- Searchable
- Semantic

Never mix Context responsibilities with Memory responsibilities.

## Kernel Standard Compliance

This API complies with the Kernel Development Standard (EIO-ARCH-001):

- Required lifecycle: API → Model → Validator → Error → Service → Engine → Verification
- No shortcuts
- No architectural deviations
- Interface-only design
- No implementations in API package
- No business logic in APIs
- Comprehensive JavaDocs
- Thread-safe contracts
- Immutable return values

## Thread Safety

All API contracts require thread-safe implementations. Multiple kernels may concurrently access and modify Context data.

## Immutability

All returned Context and ContextSnapshot objects must be immutable. Consumers must not modify returned objects.

## Future Roadmap

Future enhancements (not in this specification):

- Context search capabilities
- Context relationships
- Context metrics and analytics
- Context validation rules
- Context transformation operations

## JavaDocs

All public interfaces and methods are fully documented with JavaDocs. The documentation includes:

- Purpose and responsibility
- Thread safety guarantees
- Side effect declarations
- Parameter descriptions
- Return value descriptions
- Exception specifications
- Architectural context

## Constitutional Authority

This package is governed by:
- **EIO-CTX-101** - Context Kernel API contracts specification
- **EIO-ARCH-001** - Kernel Development Standard

## Usage Example

```java
// Create a Context
ContextService contextService = ...; // Injected
CreateContextRequest request = new CreateContextRequest(
    conversationId,
    taskId,
    Map.of("key", "value")
);
Context context = contextService.createContext(request);

// Query Context
ContextQueryService queryService = ...; // Injected
Optional<Context> found = queryService.findById(context.id());
List<Context> activeContexts = queryService.findActiveContexts();

// Create snapshot
ContextSnapshotService snapshotService = ...; // Injected
ContextSnapshot snapshot = snapshotService.createSnapshot(context.id());

// Manage lifecycle
ContextLifecycleService lifecycleService = ...; // Injected
lifecycleService.activate(context.id());
lifecycleService.suspendContext(context.id());
lifecycleService.resumeContext(context.id());
lifecycleService.deactivate(context.id());
lifecycleService.archive(context.id());