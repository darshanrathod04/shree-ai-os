package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.IngestionResult;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

/**
 * <b>DocumentIngestionEngine</b>
 *
 * <p>The K2 contract that transforms raw external content into the canonical,
 * immutable {@code KnowledgeDocument} representation of Shree AI OS. The
 * engine performs no retrieval, no graph building, no embeddings, no ranking
 * and no reasoning - its single responsibility is deterministic canonical
 * representation.</p>
 *
 * <p><b>Pipeline position:</b> the {@code KnowledgeSourceRegistry} resolves
 * and validates the source; this engine consumes the registered source and
 * raw content; every future kernel consumes the produced document.</p>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe and
 * deterministic: the same {@code sourceId + title + parserType + content}
 * always produces identical document ids, chunk ids and chunking. Only the
 * observability timestamps ({@code ingestedAt}, processing duration) differ
 * between runs.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultDocumentIngestionEngine
 */
public interface DocumentIngestionEngine {

    /**
     * Ingests raw content for a registered knowledge source and produces the
     * canonical document.
     *
     * @param sourceId   the deterministic id of a registered source (must not
     *                   be null or blank, must be known to the registry)
     * @param title      the canonical document title (must not be null or
     *                   blank)
     * @param parserType the ingestion strategy to apply (must not be null)
     * @param content    the raw content to ingest (must not be null or blank)
     * @return the immutable ingestion result (never null)
     * @throws IllegalArgumentException  on blank input or unknown source id
     * @throws UnsupportedOperationException for parser types without an
     *         available parser (e.g. PDF until PDFBox integration)
     */
    IngestionResult ingest(String sourceId,
                           String title,
                           ParserType parserType,
                           String content);
}
