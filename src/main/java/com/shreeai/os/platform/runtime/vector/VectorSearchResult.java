package com.shreeai.os.platform.runtime.vector;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>VectorSearchResult</b>
 *
 * <p>Immutable single result of a vector similarity search: the matching
 * record id, its cosine similarity score, the source content, and its
 * metadata-first document schema (documentId, tenantId, embeddingVersion).</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class VectorSearchResult {

    private final String recordId;
    private final double score;
    private final String content;
    private final Map<String, Object> metadata;

    private VectorSearchResult(
            String recordId,
            double score,
            String content,
            Map<String, Object> metadata) {
        this.recordId = recordId;
        this.score = score;
        this.content = content;
        this.metadata = metadata;
    }

    /**
     * Creates a new immutable search result.
     *
     * @param recordId matching record id (must not be null or blank)
     * @param score    cosine similarity score
     * @param content  source text of the record (must not be null)
     * @param metadata record metadata (must not be null)
     * @return a new immutable result
     */
    public static VectorSearchResult of(
            String recordId,
            double score,
            String content,
            Map<String, Object> metadata) {
        Objects.requireNonNull(recordId, "recordId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        return new VectorSearchResult(
                recordId,
                score,
                content,
                Collections.unmodifiableMap(Map.copyOf(metadata)));
    }

    /** Returns the matching record id (never null). */
    public String recordId() {
        return recordId;
    }

    /** Returns the cosine similarity score. */
    public double score() {
        return score;
    }

    /** Returns the source text (never null). */
    public String content() {
        return content;
    }

    /** Returns the unmodifiable metadata map (never null). */
    public Map<String, Object> metadata() {
        return metadata;
    }
}
