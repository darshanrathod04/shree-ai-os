package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.memory.model.MemoryType;

import java.util.Objects;

/**
 * <b>LifecyclePolicy</b>
 *
 * <p>Tunable thresholds governing the memory lifecycle: promotion from
 * working memory to long-term memory, importance scoring decay and
 * archival of stale memories.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Constitutional Authority:</b> ADD-201, MEMORY-RUNTIME-001</p>
 *
 * @param minAccessCount     accesses required before promotion is considered
 * @param importanceThreshold importance at which a working memory promotes immediately
 * @param archiveAfterDays   inactivity (days) after which a low-importance memory archives
 * @param recencyHalfLifeDays half-life (days) used by recency-based importance decay
 * @param promoteTarget      memory type a working memory promotes to
 */
public record LifecyclePolicy(
        int minAccessCount,
        double importanceThreshold,
        long archiveAfterDays,
        long recencyHalfLifeDays,
        MemoryType promoteTarget) {

    /** Default platform lifecycle policy. */
    public static final LifecyclePolicy DEFAULT = new LifecyclePolicy(
            3,
            0.7,
            30,
            7,
            MemoryType.SEMANTIC);

    /**
     * Creates a policy with validation.
     *
     * @throws IllegalArgumentException if any threshold is non-positive
     */
    public LifecyclePolicy {
        if (minAccessCount < 1) {
            throw new IllegalArgumentException("minAccessCount must be >= 1");
        }
        if (importanceThreshold <= 0.0 || importanceThreshold > 1.0) {
            throw new IllegalArgumentException("importanceThreshold must be in (0.0, 1.0]");
        }
        if (archiveAfterDays < 1) {
            throw new IllegalArgumentException("archiveAfterDays must be >= 1");
        }
        if (recencyHalfLifeDays < 1) {
            throw new IllegalArgumentException("recencyHalfLifeDays must be >= 1");
        }
        Objects.requireNonNull(promoteTarget, "promoteTarget must not be null");
    }
}