package com.shreeai.os.platform.runtime.vector;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>VectorRecord</b>
 *
 * <p>Immutable vector store entry. The record id typically equals the owning
 * knowledge node id, enabling O(1) mapping from a vector search hit back to
 * its domain node.</p>
 *
 * <p><b>Metadata-first document schema.</b> Every record carries, at minimum:</p>
 * <ul>
 *   <li>{@code documentId} — the ingestion document the record belongs to</li>
 *   <li>{@code tenantId} — tenant isolation key (reserved for Phase 2)</li>
 *   <li>{@code embeddingVersion} — provider version that produced the embedding</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class VectorRecord {

    private final String id;
    private final String content;
    private final double[] embedding;
    private final Map<String, Object> metadata;
    private final Instant createdAt;

    private VectorRecord(
            String id,
            String content,
            double[] embedding,
            Map<String, Object> metadata,
            Instant createdAt) {
        this.id = id;
        this.content = content;
        this.embedding = embedding;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new immutable vector record.
     *
     * @param id        unique record id (must not be null or blank)
     * @param content   source text of the embedding (must not be null)
     * @param embedding the embedding vector (must not be null)
     * @param metadata  metadata map; MUST contain documentId, tenantId and
     *                  embeddingVersion per the metadata-first schema
     *                  (must not be null)
     * @return a new immutable record
     */
    public static VectorRecord of(
            String id,
            String content,
            double[] embedding,
            Map<String, Object> metadata) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(embedding, "embedding must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        return new VectorRecord(
                id,
                content,
                java.util.Arrays.copyOf(embedding, embedding.length),
                Collections.unmodifiableMap(Map.copyOf(metadata)),
                Instant.now());
    }

    /**
     * Reconstructs a record with an explicit creation timestamp (used by
     * persistence adapters when mapping stored rows back into records).
     *
     * @param id        unique record id (must not be null or blank)
     * @param content   source text of the embedding (must not be null)
     * @param embedding the embedding vector (must not be null)
     * @param metadata  metadata map (must not be null)
     * @param createdAt creation timestamp (must not be null)
     * @return a new immutable record
     */
    public static VectorRecord of(
            String id,
            String content,
            double[] embedding,
            Map<String, Object> metadata,
            Instant createdAt) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(embedding, "embedding must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        return new VectorRecord(
                id,
                content,
                java.util.Arrays.copyOf(embedding, embedding.length),
                Collections.unmodifiableMap(Map.copyOf(metadata)),
                createdAt);
    }

    /** Returns the unique record id (never null). */
    public String id() {
        return id;
    }

    /** Returns the source text (never null). */
    public String content() {
        return content;
    }

    /** Returns a defensive copy of the embedding (never null). */
    public double[] embedding() {
        return java.util.Arrays.copyOf(embedding, embedding.length);
    }

    /** Returns the unmodifiable metadata map (never null). */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /** Returns the creation timestamp (never null). */
    public Instant createdAt() {
        return createdAt;
    }
}
