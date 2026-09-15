package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeDocument</b>
 *
 * <p>The canonical, immutable representation of one ingested knowledge
 * source. It is the <em>only</em> artifact downstream kernels consume: no
 * component outside the ingestion engine may ever re-read a raw file or web
 * page.</p>
 *
 * <p><b>Immutability:</b> the chunk list is defensively copied on
 * construction and never exposed mutably; every chunk is itself immutable.</p>
 *
 * <p><b>Deterministic identity:</b> {@code documentId} is derived by the
 * ingestion engine as {@code SHA-256(sourceId + title + parserType)} and is
 * stable across identical ingestion runs. {@code ingestedAt} is the only
 * wall-clock field and is deliberately excluded from identity.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param documentId the deterministic document id (never null)
 * @param sourceId   the deterministic id of the registered knowledge source
 *                   (never null)
 * @param title      the canonical document title (never null)
 * @param parserType the ingestion strategy applied to the raw content (never
 *                   null)
 * @param chunks     the ordered immutable chunk list (never null)
 * @param ingestedAt when the document was produced (never null)
 */
public record KnowledgeDocument(
        String documentId,
        String sourceId,
        String title,
        ParserType parserType,
        List<KnowledgeDocumentChunk> chunks,
        Instant ingestedAt) {

    /**
     * Compact constructor that validates every field and defensively copies
     * the chunk list so the record is deeply immutable.
     *
     * @throws NullPointerException     if any field is null
     * @throws IllegalArgumentException if documentId or sourceId is blank
     */
    public KnowledgeDocument {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(parserType, "parserType must not be null");
        Objects.requireNonNull(chunks, "chunks must not be null");
        Objects.requireNonNull(ingestedAt, "ingestedAt must not be null");
        if (documentId.isBlank()) {
            throw new IllegalArgumentException("documentId must not be blank");
        }
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        chunks = List.copyOf(chunks);
    }

    /**
     * Returns the number of chunks contained in this document.
     *
     * @return the chunk count (never negative)
     */
    public int chunkCount() {
        return chunks.size();
    }

    @Override
    public String toString() {
        return String.format("KnowledgeDocument{documentId=%s, title=%s, parserType=%s, chunks=%d}",
                documentId, title, parserType, chunks.size());
    }
}
