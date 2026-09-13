package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeRegistrySnapshot</b>
 *
 * <p>An immutable, point-in-time view of every registered
 * {@link KnowledgeSource} in a {@code KnowledgeSourceRegistry}.</p>
 *
 * <p>Snapshots are produced for observability: consumers can inspect the full
 * registry state without ever observing concurrent mutation or retaining a
 * handle to the live registry.</p>
 *
 * <p><b>Immutability:</b> The source list is defensively copied on
 * construction; every contained source is itself immutable.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K1 Universal Knowledge Source Registry</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param sources the immutable list of registered sources (never null)
 * @param createdAt when the snapshot was captured (never null)
 */
public record KnowledgeRegistrySnapshot(List<KnowledgeSource> sources, Instant createdAt) {

    /**
     * Compact constructor that defensively copies the source list.
     *
     * @throws NullPointerException if sources or createdAt is null
     */
    public KnowledgeRegistrySnapshot {
        Objects.requireNonNull(sources, "sources must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        sources = List.copyOf(sources);
    }

    /**
     * Returns the number of sources present in this snapshot.
     *
     * @return the source count
     */
    public int size() {
        return sources.size();
    }

    @Override
    public String toString() {
        return String.format("KnowledgeRegistrySnapshot{sources=%d, createdAt=%s}",
                sources.size(), createdAt);
    }
}