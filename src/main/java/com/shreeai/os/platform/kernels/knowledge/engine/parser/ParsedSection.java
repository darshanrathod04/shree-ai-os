package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import java.util.Objects;

/**
 * <b>ParsedSection</b>
 *
 * <p>Immutable intermediate produced by a {@link DocumentParser}: one semantic
 * section of the raw content together with its heading label. The ingestion
 * engine turns every section into exactly one canonical
 * {@code KnowledgeDocumentChunk}, so parsers never compute ids and never see
 * chunk indices.</p>
 *
 * <p><b>Content preservation:</b> {@code content} carries the section text
 * exactly as extracted; parsers never summarize and never rewrite content.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param section the heading label of the section (empty for content that
 *                precedes the first heading; never null)
 * @param content the original section text (never null)
 */
public record ParsedSection(String section, String content) {

    /**
     * Compact constructor that validates both fields.
     *
     * @throws NullPointerException if section or content is null
     */
    public ParsedSection {
        Objects.requireNonNull(section, "section must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }

    @Override
    public String toString() {
        return String.format("ParsedSection{section=%s, contentLength=%d}",
                section, content.length());
    }
}
