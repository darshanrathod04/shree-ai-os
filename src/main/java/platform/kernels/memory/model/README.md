# Memory Kernel — Platform Language

## Package
`platform.kernels.memory.model`

## Purpose
This package defines the complete Platform Language for the Memory Kernel. It contains only immutable data contracts that represent Memory entities, value objects, enums, requests, and results.

## Architecture
All types are pure Java 21 records and enums with:
- **Immutable state** — no setters, no mutable fields
- **Constructor validation** — only `Objects.requireNonNull()` for null safety
- **Defensive copying** — collections are copied and wrapped in unmodifiable views
- **No business logic** — pure data contracts only

## Type Catalog

### Enums
| Type | Values |
|------|--------|
| `MemoryType` | EPISODIC, SEMANTIC, PROCEDURAL, WORKING, FACT, DOCUMENT, GOAL, CONVERSATION, OBSERVATION, SYSTEM |
| `MemoryStatus` | ACTIVE, ARCHIVED, PENDING_DELETION, DELETED |
| `MemoryVisibility` | PRIVATE, SHARED, PUBLIC, SYSTEM |

### Value Objects
| Type | Description |
|------|-------------|
| `MemoryId` | Unique identifier for a Memory |
| `MemoryContent` | Content data (text, embedding, metadata) |
| `MemoryMetadata` | Metadata (type, status, visibility, owner, tags, importance) |
| `MemoryResult` | Generic operation result with success/failure |
| `MemoryStatistics` | Aggregated memory metrics |
| `MemoryExport` | Exported memory for transfer |
| `MemoryImport` | Import request |
| `MemoryImportResult` | Import operation result |

### Core Entity
| Type | Description |
|------|-------------|
| `Memory` | Core Memory entity combining content and metadata |

### Requests
| Type | Description |
|------|-------------|
| `CreateMemoryRequest` | Request to create a new Memory |
| `UpdateMemoryRequest` | Request to update an existing Memory |

## Dependencies
- `platform.kernels.identity.model.IdentityId` — for memory ownership

## Constraints
- No framework dependencies (Spring, Lombok, JPA)
- No persistence logic
- No business logic
- No service logic