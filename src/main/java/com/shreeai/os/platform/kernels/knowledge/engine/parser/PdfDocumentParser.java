package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * <b>PdfDocumentParser</b>
 *
 * <p>Deterministic Apache PDFBox parser for {@link ParserType#PDF}. Extracts
 * text page-by-page without summarizing or rewriting content, producing one
 * {@link ParsedSection} per non-empty page ({@code "Page 1"}, {@code "Page 2"},
 * ...).</p>
 *
 * <p>Accepts either raw PDF bytes ({@link #parse(byte[])}), a Base64-encoded
 * PDF payload (with optional {@code data:application/pdf;base64,} prefix), or
 * an ISO-8859-1 encoded {@code %PDF-} stream via {@link #parse(String)}.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.1</p>
 */
public final class PdfDocumentParser implements DocumentParser {

    private static final String DATA_URI_PREFIX = "data:application/pdf;base64,";

    @Override
    public ParserType supports() {
        return ParserType.PDF;
    }

    /**
     * Parses a PDF payload represented as Base64 or ISO-8859-1 {@code %PDF-}
     * string into ordered page sections.
     *
     * @param content the PDF content string (must not be null)
     * @return immutable list of extracted page sections
     * @throws IllegalArgumentException if content is empty, not valid PDF data,
     *                                  or cannot be parsed by PDFBox
     */
    @Override
    public List<ParsedSection> parse(String content) {
        Objects.requireNonNull(content, "content must not be null");
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("PDF content must not be empty");
        }

        byte[] pdfBytes = decodePdfBytes(trimmed, content);
        return parse(pdfBytes);
    }

    /**
     * Parses raw PDF bytes deterministically into ordered page sections.
     *
     * @param pdfBytes raw PDF bytes (must not be null)
     * @return immutable list of extracted page sections
     * @throws IllegalArgumentException if bytes are not a valid PDF document
     */
    public List<ParsedSection> parse(byte[] pdfBytes) {
        Objects.requireNonNull(pdfBytes, "pdfBytes must not be null");
        if (!startsWithPdfHeader(pdfBytes)) {
            throw new IllegalArgumentException(
                    "Invalid PDF content: expected %PDF- header or Base64-encoded PDF bytes");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int pageCount = document.getNumberOfPages();
            if (pageCount == 0) {
                return List.of();
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            List<ParsedSection> sections = new ArrayList<>();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document).strip();
                if (!text.isEmpty()) {
                    sections.add(new ParsedSection("Page " + page, text));
                }
            }
            return List.copyOf(sections);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse PDF document: " + e.getMessage(), e);
        }
    }

    private static byte[] decodePdfBytes(String trimmed, String original) {
        if (original.startsWith("%PDF-")) {
            return original.getBytes(StandardCharsets.ISO_8859_1);
        }
        String candidate = trimmed.startsWith(DATA_URI_PREFIX)
                ? trimmed.substring(DATA_URI_PREFIX.length()).trim()
                : trimmed;
        try {
            byte[] decoded = Base64.getDecoder().decode(candidate);
            if (startsWithPdfHeader(decoded)) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to invalid PDF error
        }
        throw new IllegalArgumentException(
                "Invalid PDF content: expected %PDF- header or Base64-encoded PDF bytes");
    }

    private static boolean startsWithPdfHeader(byte[] bytes) {
        return bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';
    }
}
