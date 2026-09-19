package com.shreeai.os.platform.kernels.knowledge.model;

/**
 * <b>ParserType</b>
 *
 * <p>Identifies the deterministic ingestion strategy that must be applied to
 * raw content during document ingestion (Knowledge Kernel - K2).</p>
 *
 * <p>The parser type is part of the deterministic document identity: the
 * document id is derived from {@code sourceId + title + parserType}, so two
 * ingestion runs with different parser types over the same content produce
 * distinct, reproducible documents.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ParserType {

    /** Deterministic structural parsing of PDF documents (parser stub until PDFBox integration). */
    PDF,

    /** Heading-driven structural parsing of Markdown documents. */
    MARKDOWN,

    /** Paragraph-aware windowed parsing of plain text documents. */
    TEXT,

    /** Semantic tag-driven parsing of raw HTML strings. */
    HTML,

    /** Semantic tag-driven parsing of web page HTML. */
    WEB
}
