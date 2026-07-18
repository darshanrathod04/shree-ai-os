# Memory Kernel — Service Layer

## Package
`platform.kernels.memory.service`

## Purpose
The Default Memory Service is the Coordinator of Memory operations. It coordinates requests, delegates validation to the validator layer, delegates processing to the Memory Engine (forward reference), and temporarily stores Memory objects in an in-memory repository.

## Architecture

### Service Implementation
```
DefaultMemoryService
├── MemoryService
│   ├── createMemory(CreateMemoryRequest)
│   ├── updateMemory(UpdateMemoryRequest)
│   ├── deleteMemory(MemoryId)
│   ├── archiveMemory(MemoryId)
│   └── restoreMemory(MemoryId)
├── MemoryQueryService
│   ├── findById(MemoryId)
│   ├── findByType(MemoryType)
│   ├── findByOwner(IdentityId)
│   ├── getRecent(int limit)
│   └── exists(MemoryId)
├── MemorySearchService
│   ├── search(String query)
│   ├── searchByTags(Set<String>)
│   ├── searchByDate(Instant, Instant)
│   ├── searchBySimilarity(String)
│   └── searchByOwner(IdentityId)  // delegates to findByOwner
├── MemoryImportExportService
│   ├── exportMemory(MemoryId)
│   └── importMemory(MemoryImport)
└── MemoryStatisticsService
    ├── getStatistics()
    ├── countByType()
    ├── totalMemoryCount()
    └── archivedCount()
```

### Dependencies
- `MemoryValidator` — for validation
- `MemoryProcessingEngine` — for processing (forward reference)
- `ConcurrentHashMap<MemoryId, Memory>` — in-memory storage

### Engineering Principles
- **Constructor injection only** — no setters, no field injection
- **Thread-safe** — all public methods safe for concurrent access
- **Immutable return collections** — all returned collections are immutable
- **No business logic** — coordinates, delegates, and returns results
- **No persistence, filesystem, networking, or AI logic**
- **Pure Java 21**

## Usage Examples
```java
// Create a memory
CreateMemoryRequest request = new CreateMemoryRequest(
    new MemoryContent("Hello world", null, Map.of(), Instant.now()),
    new MemoryMetadata(
        new MemoryId("id-1"),
        MemoryType.EPISODIC,
        MemoryStatus.ACTIVE,
        MemoryVisibility.PRIVATE,
        new IdentityId("owner-1"),
        Set.of("tag1", "tag2"),
        0.8, 0.9, "source", Instant.now(), Instant.now(), Instant.now(), 0L
    ),
    Instant.now()
);
MemoryId id = service.createMemory(request);

// Find a memory
Optional<Memory> memory = service.findById(id);

// Search by tags
List<Memory> results = service.searchByTags(Set.of("tag1", "tag2"));

// Get statistics
MemoryStatistics stats = service.getStatistics();
```

## Relationships
- **Validator** (`platform.kernels.memory.validator`) — validates structural integrity
- **Engine** (`platform.kernels.memory.engine`) — processes memory operations (forward reference)
- **Error** (`platform.kernels.memory.error`) — provides runtime exceptions for failures