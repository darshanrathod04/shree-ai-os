# Context Model Package

## Overview

This package contains the complete immutable domain model for the Context Kernel, implementing the approved architecture from CTX-101 Architecture Review and EIO-CTX-102 implementation specification.

## Model Hierarchy

The Context model follows a strict inheritance hierarchy as mandated by the approved architecture:

```
Context (base)
├── ConversationContext
├── ExecutionContext
│     └── TaskContext
└── SessionContext
```

### Base Model: Context

**Context** is the base runtime context abstraction that provides:
- Common runtime properties shared by all context types
- Common lifecycle state management
- Shared identity through ContextId
- Shared metadata through data map

**Key Principles:**
- Context is runtime, temporary, and lightweight
- Context provides situational awareness, not persistent memory
- Context is mutable as a concept through replacement, not mutable objects
- Context does not represent identity or persistent storage

### Specialized Context Types

#### ConversationContext

**Responsibility:** Represents active user interaction and dialogue runtime state.

**Specialized Properties:**
- `conversationId` - Unique conversation identifier
- `participantId` - Current participant identifier
- `turnCount` - Current dialogue turn count

**Use Cases:**
- Active conversation tracking
- Dialogue state management
- Conversational scope enforcement

#### ExecutionContext

**Responsibility:** Represents current execution flow and running operations.

**Specialized Properties:**
- `executionId` - Unique execution identifier
- `operationName` - Current operation name
- `stepNumber` - Current execution step

**Use Cases:**
- Execution flow tracking
- Operation state management
- Runtime execution metadata

#### TaskContext

**Responsibility:** Represents currently executing task and temporary task-specific information.

**Architectural Note:** TaskContext specializes ExecutionContext as defined by the approved architecture.

**Specialized Properties:**
- Inherits all ExecutionContext properties
- `taskId` - Unique task identifier
- `taskName` - Task name
- `parentTaskId` - Parent task identifier (optional)
- `priority` - Task priority level

**Use Cases:**
- Task execution tracking
- Task-specific runtime information
- Task hierarchy management

#### SessionContext

**Responsibility:** Represents current user session and session lifecycle.

**Specialized Properties:**
- `sessionId` - Unique session identifier
- `userId` - User identifier
- `sessionStartTime` - Session start timestamp

**Use Cases:**
- Session state management
- User session tracking
- Session lifecycle enforcement

## Supporting Models

### ContextSnapshot

Represents runtime state captured at a specific instant.

**Key Principles:**
- Runtime only - not persistent
- Immutable
- Not Memory - no historical semantics
- Lightweight state capture

### Request Models

#### CreateContextRequest
Immutable request object for creating new Context instances with validation.

#### UpdateContextRequest
Immutable request object for updating existing Context instances with validation.

### Value Objects

#### ContextId
Immutable value object for Context identifiers. Never expose primitive identifiers throughout the platform.

### Enumerations

#### ContextState
Defines context lifecycle states: ACTIVE, SUSPENDED, EXPIRED, ARCHIVED

#### ContextType
Defines context types: CONVERSATION, EXECUTION, TASK, SESSION, WORKING, ENVIRONMENTAL

#### ContextPriority
Defines priority levels: LOW, NORMAL, HIGH, CRITICAL

#### ContextScope
Defines scope levels: LOCAL, REQUEST, SESSION, GLOBAL

## Model Rules

All models in this package comply with the Kernel Development Standard (EIO-ARCH-001):

1. **Immutability:** All models are immutable with final fields
2. **Constructor Validation:** All models validate inputs in constructors
3. **Defensive Copying:** All collections are defensively copied and unmodifiable
4. **No Setters:** No setter methods - state changes through replacement
5. **Platform Language:** All identifiers use ContextId (no primitives)
6. **Java 21:** All models use Java 21 features (records, etc.)
7. **Comprehensive JavaDocs:** All public classes, constructors, and methods documented

## Architectural Boundaries

### Context vs Memory

**Context:**
- Runtime state
- Temporary
- Lightweight
- Situational awareness
- No persistence
- No historical semantics

**Memory:**
- Persistent state
- Historical
- Semantic
- Searchable
- Long-term storage

**Rule:** Context must never implement Memory responsibilities.

### Context vs Identity

**Context:**
- Runtime situational awareness
- Temporary execution state
- No authentication/authorization

**Identity:**
- User/entity identification
- Authentication/authorization
- Persistent identity attributes

**Rule:** Context must never implement Identity responsibilities.

## Future Extensibility

The model hierarchy supports future extension through:

1. **New Context Types:** Add new enum values to ContextType
2. **New Context Specializations:** Create new specialized context types following the established pattern
3. **Additional Properties:** Extend specialized contexts with new fields while maintaining immutability
4. **New Enumerations:** Add new enums for additional categorization needs

## Relationship with Memory Kernel

Context and Memory kernels are complementary but distinct:

- **Context Kernel:** Provides runtime situational awareness for current execution
- **Memory Kernel:** Provides persistent, historical, semantic storage

**Integration Points:**
- Context can reference Memory through ContextId
- Memory can capture ContextSnapshot for historical reference
- Context never directly implements Memory responsibilities
- Memory never directly implements Context responsibilities

## Compliance

This implementation complies with:
- EIO-CTX-101: Context Kernel Architecture
- EIO-CTX-102: Context Domain Model Implementation
- EIO-ARCH-001: Kernel Development Standard
- Java 21 Language Standard

## Constitutional Authority

- EIO-CTX-101: Context Kernel Architecture Review
- EIO-CTX-102: Context Domain Model Implementation Specification