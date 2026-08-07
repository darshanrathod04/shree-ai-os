# Context Service Package

## Overview

The `platform.kernels.context.service` package provides the **coordination layer** for the Context Kernel. It is the first behavioral entry point for all Context operations, implementing the coordinator pattern mandated by the Kernel Development Standard (EIO-ARCH-001).

## Architecture

```
API
 │
 ▼
DefaultContextService
 │
 ├── ContextValidator (static validation)
 ├── ContextProcessingEngine (delegation)
 └── ContextException (error translation)
```

## Core Responsibilities

The service layer is responsible for:

1. **Receiving API requests** from the API layer
2. **Validating requests** using static `ContextValidator` methods
3. **Delegating processing** to `ContextProcessingEngine`
4. **Translating failures** into the `ContextException` hierarchy
5. **Coordinating lifecycle operations** across all Context services

## What the Service Does NOT Do

The service must NEVER:

- Execute business logic
- Process Context itself
- Mutate Context directly outside delegated engine calls
- Access repositories or persistence
- Invoke AI logic
- Perform networking
- Publish events
- Create threads or schedule work

## Key Components

### DefaultContextService

The default implementation that coordinates all Context operations. It implements four API interfaces:

- `ContextService` - Context creation, updates, and state management
- `ContextQueryService` - Context retrieval and existence checks
- `ContextLifecycleService` - Context lifecycle state transitions
- `ContextSnapshotService` - Context snapshot operations

**Key Characteristics:**
- **Stateless**: No mutable instance state, no cached Context objects
- **Thread-safe**: Immutable state, no synchronization needed
- **Constructor injection only**: No field injection, service locator, or static singleton
- **Zero business logic**: Pure coordination layer

### ContextProcessingEngine

The processing engine interface that defines the contract for all Context processing operations. The engine:

- Contains all business logic
- Handles persistence and state mutations
- Performs actual Context operations
- Is stateless and thread-safe

## Processing Flow

Every request follows this exact flow:

```
Request
    │
    ▼
ContextValidator
    │
    ▼
ContextProcessingEngine
    │
    ▼
Return Result
```

### Validation Flow

1. **Request arrives** at the service
2. **Validate** using `ContextValidator` static methods
3. **If validation fails**: Create `ContextError` and throw `ContextValidationException`
4. **If validation succeeds**: Delegate to `ContextProcessingEngine`
5. **Return result** or translate engine exceptions

**Never continue processing after validation failure.**

## Exception Translation

All failures are translated into the Context exception hierarchy:

- **Validation failures** → `ContextValidationException`
- **Not found errors** → `ContextNotFoundException`
- **Lifecycle errors** → `ContextLifecycleException`
- **Snapshot errors** → `ContextSnapshotException`

### Translation Pattern

```java
// Never expose primitive error information
ContextError error = new ContextError(
    ContextErrorCode.VALIDATION_FAILED,
    "Context validation failed: " + violations,
    Instant.now(),
    Map.of("violations", violations)
);

throw new ContextValidationException(error);
```

## Dependency Injection

The service uses **constructor injection only**:

```java
public DefaultContextService(ContextProcessingEngine processingEngine) {
    this.processingEngine = processingEngine;
}
```

**Forbidden patterns:**
- Field injection (`@Autowired`)
- Service locator
- Static singleton
- Setter injection

## Thread Safety

The service is inherently thread-safe because:

- It is **stateless** - no mutable instance state
- It uses **constructor injection** - dependencies are immutable
- It delegates to **stateless components** - validators and engine
- It returns **immutable objects** - Context models are immutable

No synchronization is required or used.

## Interaction with Validators

The service uses **static validator methods** from `ContextValidator`:

```java
ContextValidationResult result = ContextValidator.validate(request);
if (!result.isValid()) {
    throw createValidationException(result);
}
```

Validators are:
- **Stateless** - no instance state
- **Thread-safe** - immutable operations
- **Pure functions** - no side effects
- **No injection required** - static methods only

## Interaction with Engine

The service delegates all processing to `ContextProcessingEngine`:

```java
// Validation
ContextValidationResult validation = ContextValidator.validate(request);

// Delegation
Context context = processingEngine.createContext(request);
```

The engine:
- Contains all business logic
- Handles persistence
- Performs state mutations
- Is injected via constructor

## Architectural Boundaries

### Service Layer (This Package)
- ✅ Coordinate requests
- ✅ Validate inputs
- ✅ Delegate to engine
- ✅ Translate exceptions
- ❌ No business logic
- ❌ No persistence
- ❌ No state mutations

### Engine Layer (ContextProcessingEngine)
- ✅ Business logic
- ✅ Persistence operations
- ✅ State mutations
- ✅ Processing logic
- ❌ No validation (done by service)
- ❌ No API concerns

### Validator Layer (ContextValidator)
- ✅ Input validation
- ✅ Structure validation
- ✅ Invariant checking
- ❌ No business logic
- ❌ No processing

### Error Layer (ContextException)
- ✅ Structured errors
- ✅ Error codes
- ✅ Metadata
- ❌ No business logic

## Compliance

This implementation complies with:

- **EIO-CTX-105**: Default Context Service specification
- **EIO-ARCH-001**: Kernel Development Standard
- **Java 21**: Uses modern Java features (records, pattern matching, etc.)
- **Zero architectural violations**: Strict adherence to coordinator pattern

## Example Usage

```java
// Create engine implementation
ContextProcessingEngine engine = new DefaultContextProcessingEngine();

// Create service with constructor injection
DefaultContextService service = new DefaultContextService(engine);

// Use service - validation and delegation happen automatically
Context context = service.createContext(request);
```

## Constitutional Authority

- **EIO-CTX-105**: Default Context Service specification
- **EIO-ARCH-001**: Kernel Development Standard
- **EIO-CTX-101**: Context Kernel architecture
- **EIO-CTX-102**: Context models
- **EIO-CTX-103**: Context validators
- **EIO-CTX-104**: Context error hierarchy

## Version

**Version:** 1.0  
**Ownership:** Context Kernel  
**Last Updated:** 2026-07-17