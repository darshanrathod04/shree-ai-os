package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Duration;
import java.util.Objects;

/**
 * <b>IngestionResult</b>
 *
 * <p>Immutable observability wrapper around one deterministic document
 * ingestion: the produced canonical {@link KnowledgeDocument} plus the chunk,
 * character and wall-clock statistics of the run. It carries no behaviour and
 * is never used as a retrieval coordinate.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param document         the canonical document produced by ingestion (never
 *                         null)
 * @param totalChunks      the number of chunks produced (never negative)
 * @param totalCharacters  the number of content characters ingested (never
 *                         negative)
 * @param processingTime   the wall-clock processing duration (never null)
 */
public record IngestionResult(
        KnowledgeDocument document,
        int totalChunks,
        int totalCharacters,
        Duration processingTime) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException     if document or processingTime is null
     * @throws IllegalArgumentException if totalChunks or totalCharacters is
     *                                  negative or processingTime is negative
     */
    public IngestionResult {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(processingTime, "processingTime must not be null");
        if (totalChunks < 0) {
            throw new IllegalArgumentException("totalChunks must be >= 0: " + totalChunks);
        }
        if (totalCharacters < 0) {
            throw new IllegalArgumentException("totalCharacters must be >= 0: " + totalCharacters);
        }
        if (processingTime.isNegative()) {
            throw new IllegalArgumentException("processingTime must not be negative");
        }
    }

    /**
     * Creates a validated ingestion result.
     *
     * @param document        the canonical document (must not be null)
     * @param totalChunks     number of chunks (must be &gt;= 0)
     * @param totalCharacters number of content characters (must be &gt;= 0)
     * @param processingTime  processing duration (must not be null or negative)
     * @return a new immutable IngestionResult (never null)
     */
    public static IngestionResult of(KnowledgeDocument document,
                                     int totalChunks,
                                     int totalCharacters,
                                     Duration processingTime) {
        return new IngestionResult(document, totalChunks, totalCharacters, processingTime);
    }

    @Override
    public String toString() {
        return String.format("IngestionResult{documentId=%s, totalChunks=%d, totalCharacters=%d, processingTime=%s}",
                document.documentId(), totalChunks, totalCharacters, processingTime);
    }
}
