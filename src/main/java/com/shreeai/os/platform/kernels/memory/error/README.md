# Memory Kernel — Error Architecture

## Package
`platform.kernels.memory.error`

## Purpose
Defines the complete error handling architecture for the Memory Kernel. Provides standardized, immutable, and type-safe representations of Memory-related failures.

## Architecture

### Error Hierarchy
```
RuntimeException
 └── MemoryException                  (base runtime exception)
      ├── MemoryNotFoundException     (memory not found)
      ├── DuplicateMemoryException    (duplicate memory)
      └── InvalidMemoryException      (validation failure)
```

### Error Model
```
MemoryError (immutable)
 ├── MemoryErrorCode code
 ├── String message
 ├── Instant timestamp
 └── Map<String, Object> details
```

### Error Codes
| Code | Description |
|------|-------------|
| `MEMORY_NOT_FOUND` | Requested memory was not found |
| `MEMORY_DUPLICATE` | Duplicate memory identifier |
| `MEMORY_INVALID` | Memory is structurally invalid |
| `MEMORY_VALIDATION_FAILED` | Memory validation failed |
| `MEMORY_ALREADY_EXISTS` | Memory already exists |
| `MEMORY_IMPORT_FAILED` | Import operation failed |
| `MEMORY_EXPORT_FAILED` | Export operation failed |

## Engineering Principles
- **Immutable models** — all error objects are immutable and thread-safe
- **Runtime exceptions only** — no checked exceptions
- **Defensive copying** — details maps are defensively copied and wrapped in unmodifiable views
- **No business logic** — pure error representation only
- **No framework dependencies** — Spring, Lombok, JPA are not used
- **Pure Java 21**

## Relationships
- **Validator** (`platform.kernels.memory.validator`) — validates structural integrity, returns `ValidationResult`
- **Error** (`platform.kernels.memory.error`) — provides runtime exception mechanism when validation fails

## Usage Examples
```java
// Memory not found
throw new MemoryNotFoundException(memoryId);

// Memory not found with details
Map<String, Object> details = Map.of("operation", "findById");
throw new MemoryNotFoundException(memoryId, details);

// Duplicate memory
throw new DuplicateMemoryException(memoryId);

// Invalid memory with reason
throw new InvalidMemoryException(memoryId, "content text must not be blank");

// Invalid memory with reason and details
throw new InvalidMemoryException(memory, "validation failed", details);

// Catching
try {
    // memory operations
} catch (MemoryNotFoundException e) {
    MemoryError error = e.error();
    MemoryErrorCode code = e.code();
    String message = e.getMessage();
}