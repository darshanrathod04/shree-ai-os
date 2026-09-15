package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.List;

/**
 * <b>DocumentParser</b>
 *
 * <p>The parser abstraction of the K2 ingestion pipeline: one deterministic
 * strategy per {@link ParserType} that turns raw content into an ordered list
 * of {@link ParsedSection}s. Parsers are pure functions of their input - no
 * I/O, no global state, no ids, no timestamps.</p>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe and
 * deterministic: the same content always yields the same sections in the same
 * order. Parsers never summarize and never rewrite content beyond the
 * structural extraction their {@link ParserType} defines.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface DocumentParser {

    /**
     * Returns the parser type this strategy handles.
     *
     * @return the supported parser type (never null)
     */
    ParserType supports();

    /**
     * Parses raw content into ordered semantic sections.
     *
     * @param content the raw content to parse (must not be null)
     * @return the ordered immutable section list (never null)
     */
    List<ParsedSection> parse(String content);
}
