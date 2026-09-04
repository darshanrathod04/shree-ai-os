package com.shreeai.os.platform.kernels.memory.service;

import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.error.MemoryNotFoundException;
import com.shreeai.os.platform.kernels.memory.api.MemoryImportExportService;
import com.shreeai.os.platform.kernels.memory.api.MemoryQueryService;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.api.MemoryStatisticsService;
import com.shreeai.os.platform.kernels.memory.engine.MemoryLifecycleService;
import com.shreeai.os.platform.kernels.memory.engine.MemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryProcessingResult;
import com.shreeai.os.platform.kernels.memory.engine.MemoryVersionLedger;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryExport;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryImport;
import com.shreeai.os.platform.kernels.memory.model.MemoryImportRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryImportResult;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryExportRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryResult;
import com.shreeai.os.platform.kernels.memory.model.MemorySearchRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatistics;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.UpdateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.core.registry.validator.ValidationResult;
import com.shreeai.os.platform.kernels.knowledge.engine.QueryNormalizer;

import java.time.Instant;
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
    private final MemoryLifecycleService lifecycleService;
    private final ConcurrentHashMap<MemoryId, Memory> memories;
    /**
     * Versioning ledger retaining superseded memory versions.
     */
    private final MemoryVersionLedger versionLedger = new MemoryVersionLedger();

    /**
     * Constructs a new {@code DefaultMemoryService} with the given dependencies.
     *
     * <p>Constructor injection is the only allowed injection mechanism.</p>
     *
     * @param validator        the memory validator (must not be null)
     * @param processingEngine the memory processing engine (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DefaultMemoryService(MemoryValidator validator, MemoryProcessingEngine processingEngine) {
        this(validator, processingEngine, new MemoryLifecycleService());
    }

    /**
     * Constructs a new {@code DefaultMemoryService} with the given dependencies
     * and an explicit lifecycle policy service.
     *
     * @param validator        the memory validator (must not be null)
     * @param processingEngine the memory processing engine (must not be null)
     * @param lifecycleService the memory lifecycle service (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DefaultMemoryService(
            MemoryValidator validator,
            MemoryProcessingEngine processingEngine,
            MemoryLifecycleService lifecycleService) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.processingEngine = Objects.requireNonNull(processingEngine, "processingEngine must not be null");
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService must not be null");
        this.memories = new ConcurrentHashMap<>();
    }

    /**
     * Sprint-9: Factory that creates a {@code DefaultMemoryService} wired with
     * in-memory defaults, mirroring the
     * {@code DefaultKnowledgeService.withInMemoryDefaults} pattern.
     *
     * <p>Intended for tests and developer-facing entry points that need a
     * working memory kernel without a dependency-injection container.</p>
     *
     * @param processingEngine the processing engine (never {@code null})
     * @return a fully-wired {@code DefaultMemoryService}
     */
    public static DefaultMemoryService withInMemoryDefaults(MemoryProcessingEngine processingEngine) {
        Objects.requireNonNull(processingEngine, "processingEngine must not be null");
        return new DefaultMemoryService(
                new MemoryValidator(),
                processingEngine,
                new MemoryLifecycleService());
    }

    /**
     * Sprint-9: Test-only accessor for the underlying in-memory store.
     *
     * <p>Allows tests to seed memories directly without going through the
     * full {@link #createMemory} validation pipeline. Returns the live
     * map; callers should not mutate it outside of test code.</p>
     *
     * @return the in-memory memory map (never {@code null})
     */
    public java.util.Map<MemoryId, Memory> getMemoriesForTest() {
        return memories;
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

        // Process via engine
        MemoryProcessingResult result = processingEngine.processCreate(request);

        // Store in memory
        memories.put(id, memory);
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
        // EO-V1.4 Memory Lifecycle — retain the superseded version before overwrite
        versionLedger.snapshot(existing);
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
        versionLedger.snapshot(removed);
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
        versionLedger.snapshot(existing);
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
        versionLedger.snapshot(existing);
        memories.put(id, restored);
        return MemoryResult.success(restored);
    }

    // -----------------------------------------------------------------------
    // EO-V1.4 Memory Lifecycle — version history access
    // -----------------------------------------------------------------------

    /**
     * Returns the current version number of a memory (1 + retained prior versions).
     *
     * @param id the memory id (must not be null)
     * @return the current version number
     */
    public int versionOf(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        return versionLedger.versionOf(id);
    }

    /**
     * Returns all superseded versions of a memory, oldest first.
     *
     * @param id the memory id (must not be null)
     * @return an unmodifiable history list (never null, may be empty)
     */
    public List<Memory> history(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        return versionLedger.history(id);
    }

    /**
     * Returns the immediately superseded version of a memory.
     *
     * @param id the memory id (must not be null)
     * @return the previous version, or empty when the memory has no history
     */
    public Optional<Memory> previousVersion(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        return versionLedger.previousVersion(id);
    }

    // -----------------------------------------------------------------------
    // EO-V1.4 Memory Lifecycle — importance, promotion, archival, consolidation
    // -----------------------------------------------------------------------

    /**
     * Rescores a memory's dynamic importance (recency + frequency + base).
     *
     * @param id the memory id (must not be null)
     * @return the rescored importance (0.0-1.0), or {@code 0.0} when the memory is absent
     */
    public double scoreImportance(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        Memory memory = memories.get(id);
        if (memory == null) {
            return 0.0;
        }
        return lifecycleService.scoreImportance(memory);
    }

    /**
     * Records an access on a memory: bumps access count, refreshes
     * {@code accessedAt} and rescores importance. The memory is persisted
     * back into the store if it was actively accessible.
     *
     * @param id the memory id (must not be null)
     * @return the touched memory, or empty when the memory is absent or not ACTIVE
     */
    public Optional<Memory> touchMemory(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        Memory existing = memories.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        Memory touched = lifecycleService.touch(existing);
        if (touched != existing) {
            // Record version snapshot of the superseded state.
            versionLedger.snapshot(existing);
            memories.put(id, touched);
        }
        return Optional.of(touched);
    }

    /**
     * Promotes a working memory to long-term if lifecycle policy criteria are met.
     *
     * @param id the memory id (must not be null)
     * @return the promoted memory, or empty when promotion is not eligible
     */
    public Optional<Memory> promoteIfEligible(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        Memory existing = memories.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        return lifecycleService.promoteIfEligible(existing)
                .map(promoted -> {
                    versionLedger.snapshot(existing);
                    memories.put(id, promoted);
                    return promoted;
                });
    }

    /**
     * Archives a memory if it is stale and low-importance according to
     * the lifecycle policy. FACT and SYSTEM memories are never archived.
     *
     * @param id the memory id (must not be null)
     * @return the archived memory, or empty when archival does not apply
     */
    public Optional<Memory> archiveIfStale(MemoryId id) {
        Objects.requireNonNull(id, "id must not be null");
        Memory existing = memories.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        return lifecycleService.archiveIfStale(existing)
                .map(archived -> {
                    versionLedger.snapshot(existing);
                    memories.put(id, archived);
                    return archived;
                });
    }

    /**
     * Runs a full lifecycle consolidation pass over all in-store memories:
     * promotes eligible working memories and archives stale low-importance ones.
     *
     * @return the list of memories that changed status (promoted or archived)
     */
    public List<Memory> consolidateMemories() {
        return lifecycleService.consolidate(memories.values().stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableList()))
                .stream()
                .map(this::applyConsolidatedChange)
                .collect(java.util.stream.Collectors.toUnmodifiableList());
    }

    /**
     * Persists a memory produced by lifecycle consolidation back into the
     * store, recording a version snapshot of the superseded state first.
     */
    private Memory applyConsolidatedChange(Memory changed) {
        MemoryId id = changed.id();
        Memory previous = memories.put(id, changed);
        if (previous != null) {
            versionLedger.snapshot(previous);
        }
        return changed;
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

        // Sprint-9: Normalize query (strip interrogative prefixes such as
        // "who is", "what is", "tell me about", "explain") so natural-language
        // queries like "who is darshan" can retrieve memories whose title or
        // content is just "darshan".
        String normalized = QueryNormalizer.normalize(query);
        final String needle = normalized.isEmpty() ? query.toLowerCase() : normalized;

        // Prepare search via engine
        MemorySearchRequest searchRequest = new MemorySearchRequest(needle, null, null, null);
        MemoryProcessingResult result = processingEngine.prepareSearch(searchRequest);

        // Execute search (service responsibility) — match against both content
        // and title metadata so title-based retrieval works.
        return memories.values().stream()
                .filter(m -> {
                    String text = m.content().text() == null ? "" : m.content().text().toLowerCase();
                    if (text.contains(needle)) {
                        return true;
                    }
                    String title = m.metadata().source() == null ? "" : m.metadata().source().toLowerCase();
                    return title.contains(needle);
                })
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Memory> searchByTags(Set<String> tags) {
        Objects.requireNonNull(tags, "tags must not be null");

        // Prepare search via engine
        MemorySearchRequest searchRequest = new MemorySearchRequest("", null, null, tags);
        MemoryProcessingResult result = processingEngine.prepareSearch(searchRequest);

        // Execute search (service responsibility)
        return memories.values().stream()
                .filter(m -> tags.stream().anyMatch(tag -> m.metadata().tags().contains(tag)))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Memory> searchByDate(java.time.Instant from, java.time.Instant to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        // Prepare search via engine
        MemorySearchRequest searchRequest = new MemorySearchRequest("", from, to, null);
        MemoryProcessingResult result = processingEngine.prepareSearch(searchRequest);

        // Execute search (service responsibility)
        return memories.values().stream()
                .filter(m -> !m.createdAt().isBefore(from) && !m.createdAt().isAfter(to))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Memory> searchBySimilarity(String text) {
        Objects.requireNonNull(text, "text must not be null");

        // Prepare search via engine
        MemorySearchRequest searchRequest = new MemorySearchRequest(text, null, null, null);
        MemoryProcessingResult result = processingEngine.prepareSearch(searchRequest);

        // Execute search (service responsibility) - simplified similarity
        return memories.values().stream()
                .filter(m -> m.content().text().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toUnmodifiableList());
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
            throw new MemoryNotFoundException(id);
        }

        // Prepare export via engine
        MemoryExportRequest exportRequest = new MemoryExportRequest("memory-export-v1", Instant.now());
        MemoryProcessingResult result = processingEngine.prepareExport(exportRequest);

        return new MemoryExport(memory, Instant.now(), "memory-export-v1");
    }

    @Override
    public MemoryImportResult importMemory(MemoryImport request) {
        Objects.requireNonNull(request, "request must not be null");

        Memory imported = request.memory();

        // Prepare import via engine
        MemoryImportRequest importRequest = new MemoryImportRequest(
                request.source(),
                "memory-import-v1",
                request.importedAt()
        );
        MemoryProcessingResult result = processingEngine.prepareImport(importRequest);

        // Generate a new ID for the imported memory
        MemoryId newId = new MemoryId(UUID.randomUUID().toString());
        Memory newMemory = new Memory(
                newId,
                imported.content(),
                imported.metadata(),
                imported.createdAt(),
                imported.updatedAt()
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