# Context Validation Layer

## Overview

The Context Validation Layer provides validation services for Context domain models within the Context Kernel. It serves as the gatekeeper for Context structure and consistency, ensuring that all Context objects meet the platform's architectural standards.

## Constitutional Authority

- **EIO-CTX-103**: Context Validation Layer Implementation
- **EIO-ARCH-001**: Kernel Development Standard

## Validation Philosophy

The Validation Layer operates on a strict principle: **validate structure and consistency only—never workflow or business behavior**.

### What Validation Does

- Inspects models for structural integrity
- Validates enums and type safety
- Validates timestamps and temporal consistency
- Validates metadata presence and structure
- Builds immutable validation results

### What Validation Never Does

- Modifies Context objects
- Accesses repositories or databases
- Performs persistence operations
- Publishes events
- Invokes AI or business logic
- Performs networking or filesystem operations
- Creates threads or schedules work
- Maintains mutable static state

## Validator Responsibilities

### ContextValidator

Base utility validator for core Context domain models.

**Validates:**
- ContextId structure and non-blank values
- ContextType enum validity
- ContextState enum validity
- ContextPriority enum validity
- ContextScope enum validity
- Timestamp consistency (createdAt, updatedAt)
- Metadata presence

**Methods:**
- `validate(Context)` - Validates a complete Context instance
- `validateContextId(ContextId)` - Validates ContextId
- `validateContextType(ContextType)` - Validates ContextType
- `validateContextState(ContextState)` - Validates ContextState
- `validateContextPriority(ContextPriority)` - Validates ContextPriority
- `validateContextScope(ContextScope)` - Validates ContextScope
- `validateTimestamp(Instant, String)` - Validates a timestamp
- `validateMetadata(Map)` - Validates metadata map

### ConversationContextValidator

Specialized validator for ConversationContext domain models.

**Validates:**
- Active conversation state
- Participant information (participantId)
- Conversation metadata (conversationId, turnCount)
- Snapshot consistency (timestamp validation)

**Methods:**
- `validate(ConversationContext)` - Validates a complete ConversationContext
- `validateParticipantInfo(ConversationContext)` - Validates participant information
- `validateConversationMetadata(ConversationContext)` - Validates conversation metadata
- `validateSnapshotConsistency(ConversationContext)` - Validates snapshot consistency

### ExecutionContextValidator

Specialized validator for ExecutionContext domain models.

**Validates:**
- Execution status and state
- Runtime state (operationName, stepNumber)
- Active operations (executionId, operationName)
- Execution metadata

**Methods:**
- `validate(ExecutionContext)` - Validates a complete ExecutionContext
- `validateExecutionStatus(ExecutionContext)` - Validates execution status
- `validateRuntimeState(ExecutionContext)` - Validates runtime state
- `validateActiveOperations(ExecutionContext)` - Validates active operations
- `validateExecutionMetadata(ExecutionContext)` - Validates execution metadata

### SessionContextValidator

Specialized validator for SessionContext domain models.

**Validates:**
- Session lifecycle (sessionId, userId)
- Session expiration and activity timestamps
- Session metadata

**Methods:**
- `validate(SessionContext)` - Validates a complete SessionContext
- `validateSessionLifecycle(SessionContext)` - Validates session lifecycle
- `validateSessionExpiration(SessionContext)` - Validates session expiration
- `validateActivityTimestamps(SessionContext)` - Validates activity timestamps
- `validateSessionMetadata(SessionContext)` - Validates session metadata

### TaskContextValidator

Specialized validator for TaskContext domain models.

**Validates:**
- Task state and structure
- Parent execution context (executionId, operationName, stepNumber)
- Execution dependencies
- Runtime metadata (taskId, taskName, priority)

**Methods:**
- `validate(TaskContext)` - Validates a complete TaskContext
- `validateTaskState(TaskContext)` - Validates task state
- `validateParentExecutionContext(TaskContext)` - Validates parent execution context
- `validateExecutionDependencies(TaskContext)` - Validates execution dependencies
- `validateRuntimeMetadata(TaskContext)` - Validates runtime metadata

## Design Principles

### Stateless Design

All validators are implemented as utility classes with static methods only. They maintain no instance state and can be safely used from any thread without synchronization.

### Thread Safety

Validators are inherently thread-safe because:
- No instance state exists
- No mutable static state is used
- All methods are pure functions
- All collections are locally created and immutable

### Immutability

The validation result (`ContextValidationResult`) is an immutable value object:
- All fields are `final`
- Collections are defensively copied
- Collections are wrapped with `Collections.unmodifiable*`
- No setters or mutation methods exist

### Pure Validation

Validators:
- Never modify input objects
- Never have side effects
- Never access external resources
- Never maintain state between calls
- Always return a new `ContextValidationResult` instance

## Usage Examples

### Basic Context Validation

```java
import com.shreeai.os.platform.kernels.context.model.Context;
import com.shreeai.os.platform.kernels.context.model.ContextId;
import com.shreeai.os.platform.kernels.context.model.ContextType;
import com.shreeai.os.platform.kernels.context.model.ContextState;
import com.shreeai.os.platform.kernels.context.validation.ContextValidator;
import com.shreeai.os.platform.kernels.context.validation.ContextValidationResult;

import java.time.Instant;
import java.util.Map;

// Create a Context
Context context = Context.of(
        ContextId.of("ctx-123"),
        ContextType.CONVERSATION,
        ContextState.ACTIVE,
        Map.of("key", "value"),
        Instant.now(),
        Instant.now()
);

        // Validate the Context
        ContextValidationResult result = ContextValidator.validate(context);

if(result.

        isValid()){
        System.out.

        println("Context is valid");
}else{
        System.out.

        println("Violations: "+result.getViolations());
        }
```

### ConversationContext Validation

```java
import com.shreeai.os.platform.kernels.context.model.ConversationContext;
import com.shreeai.os.platform.kernels.context.validation.ConversationContextValidator;
import com.shreeai.os.platform.kernels.context.validation.ContextValidationResult;

// Create a ConversationContext
ConversationContext conversation = ConversationContext.of(
        ContextId.of("conv-123"),
        ContextType.CONVERSATION,
        ContextState.ACTIVE,
        Map.of("topic", "support"),
        Instant.now(),
        Instant.now(),
        "conv-456",
        "user-789",
        5
);

        // Validate the ConversationContext
        ContextValidationResult result = ConversationContextValidator.validate(conversation);

        // Check specific aspects
        ContextValidationResult participantResult =
                ConversationContextValidator.validateParticipantInfo(conversation);

        ContextValidationResult metadataResult =
                ConversationContextValidator.validateConversationMetadata(conversation);
```

### ExecutionContext Validation

```java
import com.shreeai.os.platform.kernels.context.model.ExecutionContext;
import com.shreeai.os.platform.kernels.context.validation.ExecutionContextValidator;
import com.shreeai.os.platform.kernels.context.validation.ContextValidationResult;

// Create an ExecutionContext
ExecutionContext execution = ExecutionContext.of(
        ContextId.of("exec-123"),
        ContextType.EXECUTION,
        ContextState.ACTIVE,
        Map.of("step", "processing"),
        Instant.now(),
        Instant.now(),
        "exec-456",
        "processOrder",
        3
);

        // Validate the ExecutionContext
        ContextValidationResult result = ExecutionContextValidator.validate(execution);

        // Check specific aspects
        ContextValidationResult statusResult =
                ExecutionContextValidator.validateExecutionStatus(execution);

        ContextValidationResult runtimeResult =
                ExecutionContextValidator.validateRuntimeState(execution);
```

### SessionContext Validation

```java
import com.shreeai.os.platform.kernels.context.model.SessionContext;
import com.shreeai.os.platform.kernels.context.validation.SessionContextValidator;
import com.shreeai.os.platform.kernels.context.validation.ContextValidationResult;

// Create a SessionContext
SessionContext session = SessionContext.of(
        ContextId.of("sess-123"),
        ContextType.SESSION,
        ContextState.ACTIVE,
        Map.of("theme", "dark"),
        Instant.now(),
        Instant.now(),
        "sess-456",
        "user-789",
        Instant.now().minusSeconds(3600)
);

        // Validate the SessionContext
        ContextValidationResult result = SessionContextValidator.validate(session);

        // Check specific aspects
        ContextValidationResult lifecycleResult =
                SessionContextValidator.validateSessionLifecycle(session);

        ContextValidationResult expirationResult =
                SessionContextValidator.validateSessionExpiration(session);
```

### TaskContext Validation

```java
import com.shreeai.os.platform.kernels.context.model.TaskContext;
import com.shreeai.os.platform.kernels.context.model.ContextPriority;
import com.shreeai.os.platform.kernels.context.validation.TaskContextValidator;
import com.shreeai.os.platform.kernels.context.validation.ContextValidationResult;

// Create a TaskContext
TaskContext task = TaskContext.of(
        ContextId.of("task-123"),
        ContextType.TASK,
        ContextState.ACTIVE,
        Map.of("priority", "high"),
        Instant.now(),
        Instant.now(),
        "exec-456",
        "processOrder",
        3,
        "task-789",
        "Process Customer Order",
        "parent-task-001",
        ContextPriority.HIGH
);

        // Validate the TaskContext
        ContextValidationResult result = TaskContextValidator.validate(task);

        // Check specific aspects
        ContextValidationResult stateResult =
                TaskContextValidator.validateTaskState(task);

        ContextValidationResult parentResult =
                TaskContextValidator.validateParentExecutionContext(task);
```

## Relationship with Context Models

The Validation Layer operates on Context models without modifying them:

```
┌─────────────────────────────────────────┐
│         Context Models                   │
│  (Context, ConversationContext, etc.)    │
│  - Immutable records                      │
│  - Defensive copying                      │
│  - Thread-safe                            │
└──────────────┬──────────────────────────┘
               │
               │ validates (read-only)
               │
               ▼
┌─────────────────────────────────────────┐
│      Validation Layer                    │
│  (ContextValidator, etc.)                │
│  - Static methods only                   │
│  - No instance state                     │
│  - Pure validation                       │
│  - Returns ContextValidationResult       │
└──────────────┬──────────────────────────┘
               │
               │ returns
               │
               ▼
┌─────────────────────────────────────────┐
│    ContextValidationResult               │
│  - Immutable value object                │
│  - valid flag                            │
│  - violations list                       │
│  - validatedAt timestamp                 │
│  - metadata map                          │
└─────────────────────────────────────────┘
```

## ContextValidationResult Structure

The `ContextValidationResult` is an immutable value object containing:

- **valid** (`boolean`): Whether validation passed
- **violations** (`List<String>`): List of validation violations (empty if valid)
- **validatedAt** (`Instant`): When validation was performed
- **metadata** (`Map<String, Object>`): Additional validation metadata

### Example Result

```java
ContextValidationResult result = ContextValidator.validate(context);

// Check validity
boolean isValid = result.isValid();

// Access violations
List<String> violations = result.getViolations();

// Access timestamp
Instant validatedAt = result.getValidatedAt();

// Access metadata
Map<String, Object> metadata = result.getMetadata();
```

## Kernel Standard Compliance

All validators comply with the Kernel Development Standard (EIO-ARCH-001):

✅ Static methods only
✅ Stateless
✅ Pure validation
✅ Thread-safe
✅ No business logic
✅ No persistence
✅ No side effects
✅ No repository access
✅ No database access
✅ No event publishing
✅ No AI logic
✅ No networking
✅ No filesystem operations
✅ No reflection
✅ No mutable static state
✅ Immutable return values
✅ Defensive copying
✅ Comprehensive JavaDocs

## Package Structure

```
platform.kernels.context.validation
├── ContextValidator.java
├── ConversationContextValidator.java
├── ExecutionContextValidator.java
├── SessionContextValidator.java
├── TaskContextValidator.java
├── ContextValidationResult.java
├── package-info.java
└── README.md
```

## Thread Safety Guarantees

All validators can be safely used in multi-threaded environments:

```java
// Multiple threads can validate concurrently
ExecutorService executor = Executors.newFixedThreadPool(10);

for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        ContextValidationResult result = ContextValidator.validate(context);
        // Process result...
    });
}

executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);
```

## Integration Points

The Validation Layer integrates with other Context Kernel components:

- **API Layer**: Validators are called by API services before processing requests
- **Model Layer**: Validators inspect immutable Context models
- **Error Layer**: Validation violations can be converted to domain exceptions
- **Future Service**: Will coordinate validation in business workflows
- **Future Engine**: Will use validation results for processing decisions

## Best Practices

1. **Always validate before use**: Validate Context objects before passing them to services
2. **Check validation results**: Always check `isValid()` before proceeding
3. **Log violations**: Log validation violations for debugging and audit
4. **Use specific validators**: Use specialized validators for specific Context types
5. **Don't modify results**: ContextValidationResult is immutable—don't attempt to modify it
6. **Thread-safe usage**: Validators can be safely used from any thread

## Version History

- **1.0** (EIO-CTX-103): Initial implementation of Context Validation Layer

## Ownership

**Context Kernel** - This package is owned and maintained by the Context Kernel team.

## Constitutional Authority

- **EIO-CTX-103**: Context Validation Layer Implementation Specification
- **EIO-ARCH-001**: Kernel Development Standard