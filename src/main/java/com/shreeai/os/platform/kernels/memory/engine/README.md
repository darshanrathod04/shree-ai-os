 # Memory Processing Engine

## Purpose

The Memory Processing Engine is a pure processing component within the Memory Kernel. It prepares processing results for Memory operations without storing data, validating requests, or performing business logic.

## Architecture

The processing flow is fixed:

```
API
 │
 ▼
DefaultMemoryService
 │
 ▼
MemoryProcessingEngine
 │
 ▼
MemoryProcessingResult
```

The engine sits between the service layer and the processing result. The service calls the engine, and the engine returns a `MemoryProcessingResult`. The service decides what to do with the result.

## Dependencies

The Memory Processing Engine has minimal dependencies:

- **platform.kernels.memory.model** - Request and result models
- **java.time.Instant** - Timestamp handling
- **java.util.Map** - Metadata storage

The engine does NOT depend on:
- Storage or persistence layers
- Validation frameworks
- Business logic components
- External services or APIs

## Design Principles

### Stateless
The engine contains no instance fields and maintains no state between invocations. Each method call is independent and self-contained.

### Thread-Safe
All implementations must be thread-safe. The default implementation achieves this through immutability and lack of mutable state.

### Deterministic
Given the same input, the engine produces the same output. Operations are pure functions with no side effects.

### Side-Effect Free
The engine never:
- Stores data
- Validates requests
- Performs business logic
- Accesses repositories or databases
- Reads from or writes to filesystems
- Makes network calls
- Publishes events
- Performs AI reasoning or vector search

## Usage

### Basic Usage

```java
// Create the engine instance
MemoryProcessingEngine engine = new DefaultMemoryProcessingEngine();

// Process a create request
CreateMemoryRequest createRequest = new CreateMemoryRequest(
    content,
    metadata,
    Instant.now()
);
MemoryProcessingResult result = engine.processCreate(createRequest);

// Process an update request
UpdateMemoryRequest updateRequest = new UpdateMemoryRequest(
    memoryId,
    content,
    metadata,
    Instant.now()
);
MemoryProcessingResult result = engine.processUpdate(updateRequest);

// Process a delete request
MemoryProcessingResult result = engine.processDelete(memoryId);

// Process an archive request
MemoryProcessingResult result = engine.processArchive(memoryId);

// Process a restore request
MemoryProcessingResult result = engine.processRestore(memoryId);

// Prepare a search
MemorySearchRequest searchRequest = new MemorySearchRequest(
    "query",
    from,
    to,
    tags
);
MemoryProcessingResult result = engine.prepareSearch(searchRequest);

// Prepare an import
MemoryImportRequest importRequest = new MemoryImportRequest(
    "source",
    "format",
    Instant.now()
);
MemoryProcessingResult result = engine.prepareImport(importRequest);

// Prepare an export
MemoryExportRequest exportRequest = new MemoryExportRequest(
    "format",
    Instant.now()
);
MemoryProcessingResult result = engine.prepareExport(exportRequest);
```

### Service Integration

The service layer integrates with the engine as follows:

```java
public final class DefaultMemoryService implements MemoryService {
    private final MemoryProcessingEngine processingEngine;

    public DefaultMemoryService(
            MemoryValidator validator,
            MemoryProcessingEngine processingEngine) {
        this.processingEngine = processingEngine;
    }

    public MemoryId createMemory(CreateMemoryRequest request) {
        // 1. Validate the request
        ValidationResult validationResult = validator.validateCreateRequest(request);
        if (!validationResult.isValid()) {
            return null;
        }

        // 2. Process via engine
        MemoryProcessingResult result = processingEngine.processCreate(request);

        // 3. Service decides what to do with the result
        if (result.successful()) {
            // Persist the memory
            // ...
        }

        return id;
    }
}
```

## Components

### MemoryProcessingEngine (Interface)

Defines the contract for all processing operations. Implementations must be stateless, thread-safe, and side-effect free.

**Methods:**
- `processCreate(CreateMemoryRequest)` - Process a creation request
- `processUpdate(UpdateMemoryRequest)` - Process an update request
- `processDelete(MemoryId)` - Process a deletion request
- `processArchive(MemoryId)` - Process an archive request
- `processRestore(MemoryId)` - Process a restore request
- `prepareSearch(MemorySearchRequest)` - Prepare a search operation
- `prepareImport(MemoryImportRequest)` - Prepare an import operation
- `prepareExport(MemoryExportRequest)` - Prepare an export operation

### DefaultMemoryProcessingEngine (Implementation)

The default implementation that provides basic processing functionality. It normalizes input, prepares metadata, and returns processing results without side effects.

**Characteristics:**
- Final class
- No instance fields
- Public no-argument constructor
- Thread-safe
- Deterministic
- Side-effect free

### MemoryProcessingResult (Value Object)

An immutable value object representing the outcome of a processing operation.

**Fields:**
- `successful` - Whether the processing succeeded
- `operation` - The operation that was processed
- `processedAt` - When the processing occurred
- `metadata` - Additional processing metadata (unmodifiable)

**Characteristics:**
- Final class
- Final fields
- Constructor validation
- Defensive copy of metadata
- Unmodifiable metadata
- `equals()`, `hashCode()`, `toString()` implemented
- No setters

## JavaDocs

All public classes, constructors, and methods are fully documented with JavaDocs. The documentation includes:

- Purpose and responsibility
- Thread safety guarantees
- Side effect declarations
- Parameter descriptions
- Return value descriptions
- Exception specifications
- Architectural context

## Constitutional Authority

This package is governed by:
- **EIO-MEM-106** - Memory Processing Engine implementation specification
- **ADD-201** - Memory Kernel architecture decision

## Thread Safety

All implementations of `MemoryProcessingEngine` must be thread-safe. The `DefaultMemoryProcessingEngine` achieves this through:

1. No mutable instance fields
2. No static mutable state
3. Local variables only (stack-allocated)
4. Immutable return values

## Immutability

The engine and its result objects are immutable:

1. `DefaultMemoryProcessingEngine` has no fields
2. `MemoryProcessingResult` has final fields
3. Metadata maps are defensively copied
4. All returned collections are unmodifiable

## Testing

The engine is designed to be easily testable:

```java
@Test
void shouldProcessCreateRequest() {
    DefaultMemoryProcessingEngine engine = new DefaultMemoryProcessingEngine();
    
    CreateMemoryRequest request = new CreateMemoryRequest(
        content,
        metadata,
        Instant.now()
    );
    
    MemoryProcessingResult result = engine.processCreate(request);
    
    assertTrue(result.successful());
    assertEquals("CREATE", result.operation());
    assertNotNull(result.processedAt());
    assertNotNull(result.metadata());
}