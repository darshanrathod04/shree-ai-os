package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.List;

/**
 * <b>PdfDocumentParser</b>
 *
 * <p>Placeholder parser for {@link ParserType#PDF}. The deterministic PDF
 * extraction strategy itself arrives together with the Apache PDFBox
 * integration in a later milestone; until then this stub exists only to make
 * the parser abstraction complete and to fail ingestion <em>loudly</em> for
 * PDF content instead of silently producing empty documents.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class PdfDocumentParser implements DocumentParser {

    /** Message of the exception raised for every PDF ingestion attempt. */
    public static final String NOT_IMPLEMENTED_MESSAGE =
            "PDF ingestion is not implemented yet: the Apache PDFBox parser "
                    + "integration is scheduled for a later milestone";

    @Override
    public ParserType supports() {
        return ParserType.PDF;
    }

    /**
     * Always throws: PDF text extraction is not available yet.
     *
     * @param content the raw PDF content (ignored)
     * @return never
     * @throws UnsupportedOperationException on every call, carrying
     *         {@link #NOT_IMPLEMENTED_MESSAGE}
     */
    @Override
    public List<ParsedSection> parse(String content) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }
}
