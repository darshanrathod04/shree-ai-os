package platform.kernels.memory.service;

import platform.kernels.identity.model.IdentityId;
import platform.kernels.memory.api.MemoryImportExportService;
import platform.kernels.memory.api.MemoryQueryService;
import platform.kernels.memory.api.MemorySearchService;
import platform.kernels.memory.api.MemoryService;
import platform.kernels.memory.api.MemoryStatisticsService;
import platform.kernels.memory.engine.MemoryProcessingEngine;
import platform.kernels.memory.model.CreateMemoryRequest;
import platform.kernels.memory.model.Memory;
import platform.kernels.memory.model.MemoryContent;
import platform.kernels.memory.model.MemoryExport;
import platform.kernels.memory.model.MemoryId;
import platform.kernels.memory.model.MemoryImport;
import platform.kernels.memory.model.MemoryImportResult;
import platform.kernels.memory.model.MemoryMetadata;
import platform.kernels.memory.model.MemoryResult;
import platform.kernels.memory.model.MemoryStatistics;
import platform.kernels.memory.model.MemoryStatus;
import platform.kernels.memory.model.MemoryType;
import platform.kernels.memory.model.UpdateMemoryRequest;
import platform.kernels.memory.validator.MemoryValidator;
import platform.core.registry.validator.ValidationResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <b>DefaultMemoryService</b>
 *
 * <p>The default implementation of all Memory API service interfaces.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates Memory operations across the Memory Kernel layers.</li>
 *   <li>Delegates validation to the {@link MemoryValidator}.</li>
 *   <li>Delegates processing to the {@link MemoryProcessingEngine}.</li>
 *   <li>Manages in-memory storage via {@link ConcurrentHashMap}.</li>
 *   <li>Returns immutable collections to all callers.</li>
 *   <li>Never contains business logic, search algorithms, or AI operations.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This implementation is thread-safe. All public methods
 * are safe for concurrent access. Internal state is protected by
 * {@link ConcurrentHashMap} and immutable return types.</p>
 *
 * <p><b>Immutability:</b> All returned Memory objects and collections are
 * immutable or wrapped in unmodifiable views.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Constructor injection only — no setters, no field injection.</li>
 *   <li>Stateless aside from the in-memory store.</li>
 *   <li>No business logic — coordinates, delegates, and returns results.</li>
 *   <li>No persistence, filesystem, networking, or AI logic.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @see MemoryService
 * @see MemoryQueryService
 * @see MemorySearchService
 * @see MemoryImportExportService
 * @see MemoryStatisticsService
 * @see MemoryValidator
 * @see MemoryProcessingEngine
 */
public final class DefaultMemoryService implements
        MemoryService,
        MemoryQueryService,
        MemorySearchService,
        MemoryImportExportService,
        MemoryStatisticsService {

    private final MemoryValidator validator;
    private final MemoryProcessingEngine processingEngine;
    private final ConcurrentHashMap<MemoryId, Memory> memories;

    /**
     * Constructs a new {@code DefaultMemoryService} with the given dependencies.
     *
     * <p>Constructor injection is the only allowed injection mechanism.</p>
     *
     * @param validator         the memory validator (must not be null)
     * @param processingEngine  the memory processing engine (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DefaultMemoryService(MemoryValidator validator, MemoryProcessingEngine processingEngine) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.processingEngine = Objects.requireNonNull(processingEngine, "processingEngine must not be null");
        this.memories = new ConcurrentHashMap<>();
    }

    // -----------------------------------------------------------------------
    // MemoryService — Write Operations
    // -----------------------------------------------------------------------

    @Override
    public MemoryId createMemory(CreateMemoryRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Validate the request
        ValidationResult validationResult = validator.validateCreateRequest(request);
        if (!validationResult.isValid()) {
            String errorMessage = validationResult.errors().stream()
                    .collect(Collectors.joining("; "));
            return null;
        }

        // Generate a unique identifier
        MemoryId id = new MemoryId(UUID.randomUUID().toString());

        // Build metadata with the generated id
        MemoryMetadata metadata = request.metadata();
        MemoryMetadata updatedMetadata = new MemoryMetadata(
                id,
                metadata.type(),
                metadata.status(),
                metadata.visibility(),
                metadata.owner(),
                metadata.tags(),
                metadata.importance(),
                metadata.confidence(),
                metadata.source(),
                request.createdAt(),
                request.createdAt(),
                request.createdAt(),
                0L
        );

        // Build the memory
        Memory memory = new Memory(
                id,
                request.content(),
                updatedMetadata,
                request.createdAt(),
                request.createdAt()
        );

        // Process for storage via engine
        Memory processed = processingEngine.processForStorage(memory);

        // Store in memory
        memories.put(id, processed);
        return id;
    }

    @Override
    public MemoryResult updateMemory(UpdateMemoryRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Validate the request
        ValidationResult validationResult = validator.validateUpdateRequest(request);
        if (!validationResult.isValid()) {
            String errorMessage = validationResult.errors().stream()
                    .collect(Collectors.joining("; "));
            return MemoryResult.failure("Validation failed: " + errorMessage);
        }

        MemoryId id = request.memoryId();
        Memory existing = memories.get(id);
        if (existing == null) {
            return MemoryResult.failure("Memory not found: " + id.value());
        }

        // Merge existing with updates
        MemoryContent newContent = request.content() != null ? request.content() : existing.content();
        MemoryMetadata newMetadata = request.metadata() != null ? request.metadata() : existing.metadata();
        Instant now = request.updatedAt();

        Memory updated = new Memory(id, newContent, newMetadata, existing.createdAt(), now);
        memories.put(id, updated);
        return MemoryResult.success(updated);
    }

    @Override
    public MemoryResult deleteMemory(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");

        Memory removed = memories.remove(id);
        if (removed == null) {
            return MemoryResult.failure("Memory not found: " + id.value());
        }
        return MemoryResult.success(removed);
    }

    @Override
    public MemoryResult archiveMemory(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");

        Memory existing = memories.get(id);
        if (existing == null) {
            return MemoryResult.failure("Memory not found: " + id.value());
        }

        MemoryMetadata archivedMetadata = new MemoryMetadata(
                existing.metadata().memoryId(),
                existing.metadata().type(),
                MemoryStatus.ARCHIVED,
                existing.metadata().visibility(),
                existing.metadata().owner(),
                existing.metadata().tags(),
                existing.metadata().importance(),
                existing.metadata().confidence(),
                existing.metadata().source(),
                existing.metadata().createdAt(),
                Instant.now(),
                existing.metadata().accessedAt(),
                existing.metadata().accessCount()
        );

        Memory archived = new Memory(id, existing.content(), archivedMetadata, existing.createdAt(), Instant.now());
        memories.put(id, archived);
        return MemoryResult.success(archived);
    }

    @Override
    public MemoryResult restoreMemory(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");

        Memory existing = memories.get(id);
        if (existing == null) {
            return MemoryResult.failure("Memory not found: " + id.value());
        }

        MemoryMetadata restoredMetadata = new MemoryMetadata(
                existing.metadata().memoryId(),
                existing.metadata().type(),
                MemoryStatus.ACTIVE,
                existing.metadata().visibility(),
                existing.metadata().owner(),
                existing.metadata().tags(),
                existing.metadata().importance(),
                existing.metadata().confidence(),
                existing.metadata().source(),
                existing.metadata().createdAt(),
                Instant.now(),
                existing.metadata().accessedAt(),
                existing.metadata().accessCount()
        );

        Memory restored = new Memory(id, existing.content(), restoredMetadata, existing.createdAt(), Instant.now());
        memories.put(id, restored);
        return MemoryResult.success(restored);
    }

    // -----------------------------------------------------------------------
    // MemoryQueryService — Read Operations
    // -----------------------------------------------------------------------

    @Override
    public Optional<Memory> findById(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(memories.get(id));
    }

    @Override
    public List<Memory> findByType(MemoryType type) {
        Objects.requireNonNull(type, "type must not be null");
        return memories.values().stream()
                .filter(m -> m.metadata().type() == type)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Memory> findByOwner(IdentityId owner) {
        Objects.requireNonNull(owner, "owner must not be null");
        return memories.values().stream()
                .filter(m -> m.metadata().owner().equals(owner))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Memory> getRecent(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
        return memories.values().stream()
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .limit(limit)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean exists(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        return memories.containsKey(id);
    }

    // -----------------------------------------------------------------------
    // MemorySearchService — Search Operations (delegate to engine)
    // -----------------------------------------------------------------------

    @Override
    public List<Memory> search(String query) {
        Objects.requireNonNull(query, "query must not be null");
        return processingEngine.search(query);
    }

    @Override
    public List<Memory> searchByTags(Set<String> tags) {
        Objects.requireNonNull(tags, "tags must not be null");
        return processingEngine.searchByTags(tags);
    }

    @Override
    public List<Memory> searchByDate(java.time.Instant from, java.time.Instant to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        return processingEngine.searchByDate(from, to);
    }

    @Override
    public List<Memory> searchBySimilarity(String text) {
        Objects.requireNonNull(text, "text must not be null");
        return processingEngine.searchBySimilarity(text);
    }

    @Override
    public List<Memory> searchByOwner(IdentityId ownerId) {
        // Convenience method that delegates to findByOwner
        return findByOwner(ownerId);
    }

    // -----------------------------------------------------------------------
    // MemoryImportExportService — Import/Export Operations
    // -----------------------------------------------------------------------

    @Override
    public MemoryExport exportMemory(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");

        Memory memory = memories.get(id);
        if (memory == null) {
            throw new platform.kernels.memory.error.MemoryNotFoundException(id);
        }

        Memory processed = processingEngine.processForExport(memory);
        return new MemoryExport(processed, Instant.now(), "memory-export-v1");
    }

    @Override
    public MemoryImportResult importMemory(MemoryImport request) {
        Objects.requireNonNull(request, "request must not be null");

        Memory imported = request.memory();
        Memory processed = processingEngine.processForImport(imported);

        // Generate a new ID for the imported memory
        MemoryId newId = new MemoryId(UUID.randomUUID().toString());
        Memory newMemory = new Memory(
                newId,
                processed.content(),
                processed.metadata(),
                processed.createdAt(),
                processed.updatedAt()
        );

        memories.put(newId, newMemory);
        return MemoryImportResult.success(newId, Instant.now());
    }

    // -----------------------------------------------------------------------
    // MemoryStatisticsService — Statistics Operations
    // -----------------------------------------------------------------------

    @Override
    public MemoryStatistics getStatistics() {
        List<Memory> allMemories = List.copyOf(memories.values());

        long total = allMemories.size();
        long active = allMemories.stream()
                .filter(m -> m.metadata().status() == MemoryStatus.ACTIVE)
                .count();
        long archived = allMemories.stream()
                .filter(m -> m.metadata().status() == MemoryStatus.ARCHIVED)
                .count();

        Map<MemoryType, Long> byType = allMemories.stream()
                .collect(Collectors.groupingBy(
                        m -> m.metadata().type(),
                        Collectors.counting()
                ));

        Map<MemoryStatus, Long> byStatus = allMemories.stream()
                .collect(Collectors.groupingBy(
                        m -> m.metadata().status(),
                        Collectors.counting()
                ));

        long totalAccessCount = allMemories.stream()
                .mapToLong(m -> m.metadata().accessCount())
                .sum();

        double avgImportance = total > 0
                ? allMemories.stream().mapToDouble(m -> m.metadata().importance()).average().orElse(0.0)
                : 0.0;

        double avgConfidence = total > 0
                ? allMemories.stream().mapToDouble(m -> m.metadata().confidence()).average().orElse(0.0)
                : 0.0;

        return new MemoryStatistics(
                total, active, archived,
                byType, byStatus,
                totalAccessCount,
                avgImportance, avgConfidence,
                Instant.now()
        );
    }

    @Override
    public Map<MemoryType, Long> countByType() {
        return memories.values().stream()
                .collect(Collectors.groupingBy(
                        m -> m.metadata().type(),
                        Collectors.counting()
                ));
    }

    @Override
    public long totalMemoryCount() {
        return memories.size();
    }

    @Override
    public long archivedCount() {
        return memories.values().stream()
                .filter(m -> m.metadata().status() == MemoryStatus.ARCHIVED)
                .count();
    }
}