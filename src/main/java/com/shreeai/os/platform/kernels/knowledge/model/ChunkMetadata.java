package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>ChunkMetadata</b>
 *
 * <p>Immutable provenance metadata attached to every
 * {@link KnowledgeDocumentChunk}. It records where the chunk came from and
 * where it sits inside its document, so downstream kernels can always trace a
 * chunk back to its canonical document without re-reading the raw source.</p>
 *
 * <p><b>Immutability:</b> every field is final and defensively validated at
 * construction; there are no mutable fields.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param title the canonical document title the chunk belongs to (never null)
 * @param section the section heading the chunk was extracted from (empty when
 *                the chunk precedes the first heading; never null)
 * @param language the document language when declared by the source, e.g.
 *                 {@code "en"} from an HTML {@code lang} attribute (empty when
 *                 unknown; never null)
 * @param chunkIndex zero-based position of the chunk inside its document
 * @param sourceId the deterministic id of the registered knowledge source
 *                 (never null)
 */
public record ChunkMetadata(
        String title,
        String section,
        String language,
        int chunkIndex,
        String sourceId) {

    /**
     * Compact constructor that validates every field defensively.
     *
     * @throws NullPointerException  if any reference field is null
     * @throws IllegalArgumentException if chunkIndex is negative
     */
    public ChunkMetadata {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(section, "section must not be null");
        Objects.requireNonNull(language, "language must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be >= 0: " + chunkIndex);
        }
    }

    /**
     * Creates validated chunk metadata.
     *
     * @param title      canonical document title (must not be null)
     * @param section    originating section heading (must not be null)
     * @param language   declared language or empty (must not be null)
     * @param chunkIndex zero-based chunk position (must be &gt;= 0)
     * @param sourceId   deterministic source id (must not be null)
     * @return a new immutable ChunkMetadata (never null)
     */
    public static ChunkMetadata of(String title,
                                   String section,
                                   String language,
                                   int chunkIndex,
                                   String sourceId) {
        return new ChunkMetadata(title, section, language, chunkIndex, sourceId);
    }

    @Override
    public String toString() {
        return String.format("ChunkMetadata{title=%s, section=%s, language=%s, chunkIndex=%d, sourceId=%s}",
                title, section, language, chunkIndex, sourceId);
    }
}
