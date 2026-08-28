package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>MemoryLifecycleService</b>
 *
 * <p>Governs the memory lifecycle: working memory → long-term memory,
 * dynamic importance scoring, and archival of stale memories.</p>
 *
 * <p><b>Importance model (0.0-1.0):</b> base importance (40%), recency with
 * configurable half-life (30%), access frequency (30%).</p>
 *
 * <p><b>Promotion rule:</b> a WORKING memory promotes to the policy's
 * {@code promoteTarget} when accessed at least {@code minAccessCount} times
 * OR its rescored importance reaches {@code importanceThreshold}.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Constitutional Authority:</b> ADD-201, MEMORY-RUNTIME-001</p>
 */
public final class MemoryLifecycleService {

    private final LifecyclePolicy policy;

    /** Creates a lifecycle service with the default policy. */
    public MemoryLifecycleService() {
        this(LifecyclePolicy.DEFAULT);
    }

    /** @param policy the lifecycle policy (must not be null) */
    public MemoryLifecycleService(LifecyclePolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
    }

    /** @return the lifecycle policy (never null) */
    public LifecyclePolicy policy() {
        return policy;
    }

    /**
     * Rescores a memory's importance from recency, frequency and base importance.
     *
     * @param memory the memory to score (must not be null)
     * @return the rescored importance (0.0-1.0)
     */
    public double scoreImportance(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");

        double base = clamp(memory.metadata().importance());
        double recency = recency(memory.metadata().accessedAt());
        double frequency = frequency(memory.metadata().accessCount());

        return clamp(0.4 * base + 0.3 * recency + 0.3 * frequency);
    }

    /**
     * Records an access on a memory: bumps the access count, refreshes
     * {@code accessedAt} and rescores importance. Returns the memory
     * unchanged when it is not in an accessible state.
     *
     * @param memory the accessed memory (must not be null)
     * @return a touched copy (same instance when untouched)
     */
    public Memory touch(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");

        if (memory.metadata().status() != MemoryStatus.ACTIVE) {
            return memory;
        }

        Instant now = Instant.now();
        MemoryMetadata old = memory.metadata();

        MemoryMetadata touched = new MemoryMetadata(
                old.memoryId(),
                old.type(),
                old.status(),
                old.visibility(),
                old.owner(),
                old.tags(),
                scoreImportance(memory),
                old.confidence(),
                old.source(),
                old.createdAt(),
                now,
                now,
                old.accessCount() + 1);

        return new Memory(memory.id(), memory.content(), touched, memory.createdAt(), now);
    }

    /**
     * Promotes a working memory to long-term memory when it qualifies under
     * the policy (access count or rescored importance threshold).
     *
     * @param memory the candidate memory (must not be null)
     * @return the promoted copy, or empty when no promotion applies
     */
    public Optional<Memory> promoteIfEligible(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");

        MemoryMetadata metadata = memory.metadata();

        if (metadata.status() != MemoryStatus.ACTIVE
                || metadata.type() != MemoryType.WORKING) {
            return Optional.empty();
        }

        double rescored = scoreImportance(memory);
        boolean eligible = metadata.accessCount() >= policy.minAccessCount()
                || rescored >= policy.importanceThreshold();

        if (!eligible) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        MemoryMetadata promoted = new MemoryMetadata(
                metadata.memoryId(),
                policy.promoteTarget(),
                metadata.status(),
                metadata.visibility(),
                metadata.owner(),
                metadata.tags(),
                rescored,
                metadata.confidence(),
                metadata.source(),
                metadata.createdAt(),
                now,
                metadata.accessedAt(),
                metadata.accessCount());

        return Optional.of(new Memory(memory.id(), memory.content(), promoted, memory.createdAt(), now));
    }

    /**
     * Archives an ACTIVE memory that has not been accessed within
     * {@code archiveAfterDays} and whose rescored importance is below the
     * promotion threshold. FACT and SYSTEM memories are never archived.
     *
     * @param memory the candidate memory (must not be null)
     * @return the archived copy, or empty when no archival applies
     */
    public Optional<Memory> archiveIfStale(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");

        MemoryMetadata metadata = memory.metadata();

        if (metadata.status() != MemoryStatus.ACTIVE
                || metadata.type() == MemoryType.FACT
                || metadata.type() == MemoryType.SYSTEM) {
            return Optional.empty();
        }

        long daysIdle = Duration.between(metadata.accessedAt(), Instant.now()).toDays();
        if (daysIdle < policy.archiveAfterDays()) {
            return Optional.empty();
        }
        if (scoreImportance(memory) >= policy.importanceThreshold()) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        MemoryMetadata archived = new MemoryMetadata(
                metadata.memoryId(),
                metadata.type(),
                MemoryStatus.ARCHIVED,
                metadata.visibility(),
                metadata.owner(),
                metadata.tags(),
                metadata.importance(),
                metadata.confidence(),
                metadata.source(),
                metadata.createdAt(),
                now,
                metadata.accessedAt(),
                metadata.accessCount());

        return Optional.of(new Memory(memory.id(), memory.content(), archived, memory.createdAt(), now));
    }

    /**
     * Consolidates a batch of memories: promotes eligible working memories
     * and archives stale low-importance memories.
     *
     * @param memories the memories to consolidate (must not be null)
     * @return the list of changed memories (promoted or archived; may be empty)
     */
    public List<Memory> consolidate(List<Memory> memories) {
        Objects.requireNonNull(memories, "memories must not be null");

        java.util.ArrayList<Memory> changed = new java.util.ArrayList<>();

        for (Memory memory : memories) {
            if (memory == null) {
                continue;
            }
            promoteIfEligible(memory).ifPresent(changed::add);
            archiveIfStale(memory).ifPresent(archive -> {
                if (changed.stream().noneMatch(m -> m.id().equals(archive.id()))) {
                    changed.add(archive);
                }
            });
        }

        return List.copyOf(changed);
    }

    private double recency(Instant accessedAt) {
        if (accessedAt == null) {
            return 0.0;
        }
        double days = Math.max(0, Duration.between(accessedAt, Instant.now()).toMillis() / 86_400_000.0);
        return Math.pow(0.5, days / policy.recencyHalfLifeDays());
    }

    private double frequency(long accessCount) {
        return 1.0 - Math.exp(-accessCount / 10.0);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}