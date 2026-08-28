# Memory Kernel — Validation Layer

## Package
`platform.kernels.memory.validator`

## Purpose
The Memory Validator protects the Memory Platform Language. It validates that Memory models meet all structural requirements before they are used by the Memory Engine.

## Architecture
`MemoryValidator` is a stateless, thread-safe, and deterministic validator that:
- Validates structural integrity of all Memory model types
- Returns `ValidationResult` (from `platform.core.registry.validator`) with multiple errors per execution
- Never stores, searches, or indexes memories
- Never executes business logic

## Validation Methods

| Method | Validates |
|--------|-----------|
| `validateMemoryId(MemoryId)` | MemoryId is non-null with non-blank value |
| `validateMemory(Memory)` | Memory entity with all required fields |
| `validateContent(MemoryContent)` | Content text, metadata, and timestamps |
| `validateMetadata(MemoryMetadata)` | Metadata fields including owner, type, status, visibility |
| `validateCreateRequest(CreateMemoryRequest)` | Creation request completeness |
| `validateUpdateRequest(UpdateMemoryRequest)` | Update request with optional fields |
| `validateStatistics(MemoryStatistics)` | Statistics with non-negative counters and valid ranges |

## Design Constraints
- **Stateless** — all state is passed as method parameters
- **Thread-safe** — no shared mutable state
- **Deterministic** — same inputs always produce the same result
- **Pure validation only** — no business logic, no persistence, no search
- **No framework dependencies** — Spring, Lombok, JPA are not used
- **Pure Java 21**

## Dependencies
- `platform.core.registry.validator.ValidationResult` — for validation results
- `platform.kernels.memory.model.*` — Memory model types
- `platform.kernels.identity.model.IdentityId` — for owner validation