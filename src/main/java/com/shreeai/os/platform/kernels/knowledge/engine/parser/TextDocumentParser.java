package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>TextDocumentParser</b>
 *
 * <p>Deterministic plain-text parser: splits content into chunks of at most
 * {@value #MAX_CHUNK_LENGTH} characters with paragraph awareness. Chunk
 * boundaries prefer paragraph breaks (blank lines); when a window contains no
 * paragraph break, the boundary falls on the last whitespace so words are
 * never split; a single word longer than the window is cut hard.</p>
 *
 * <p><b>Content preservation:</b> every chunk is a verbatim slice of the
 * original content. Blank-line separators at chunk seams are treated as the
 * boundary itself and are therefore not duplicated into either chunk.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class TextDocumentParser implements DocumentParser {

    /** Maximum characters per text chunk. */
    public static final int MAX_CHUNK_LENGTH = 800;

    @Override
    public ParserType supports() {
        return ParserType.TEXT;
    }

    @Override
    public List<ParsedSection> parse(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        List<ParsedSection> sections = new ArrayList<>();
        int length = content.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, length);
            if (end < length) {
                end = findBoundary(content, start, end);
            }
            sections.add(new ParsedSection("", content.substring(start, end)));
            start = end;
            while (start < length && Character.isWhitespace(content.charAt(start))) {
                start++;
            }
        }
        return List.copyOf(sections);
    }

    /**
     * Finds the chunk boundary inside the window {@code (start, limit]}.
     * Prefers the last paragraph break, falls back to the last whitespace,
     * and only cuts mid-word when the window holds no whitespace at all.
     */
    private static int findBoundary(String content, int start, int limit) {
        int paragraphBreak = lastParagraphBreak(content, start, limit);
        if (paragraphBreak > start) {
            return paragraphBreak;
        }
        int whitespace = lastWhitespace(content, start, limit);
        if (whitespace > start) {
            return whitespace;
        }
        return limit;
    }

    /** Returns the position just before the last blank-line run in the window. */
    private static int lastParagraphBreak(String content, int start, int limit) {
        int i = limit - 1;
        while (i > start) {
            if (content.charAt(i) == '\n' && hasPrecedingNewline(content, start, i)) {
                return i;
            }
            i--;
        }
        return -1;
    }

    /** Returns true when a blank-line run ends at position {@code nlPos}. */
    private static boolean hasPrecedingNewline(String content, int start, int nlPos) {
        int i = nlPos - 1;
        while (i >= start && Character.isWhitespace(content.charAt(i))) {
            if (content.charAt(i) == '\n') {
                return true;
            }
            i--;
        }
        return false;
    }

    /** Returns the exclusive end just past the last whitespace in the window. */
    private static int lastWhitespace(String content, int start, int limit) {
        for (int i = limit; i > start; i--) {
            if (Character.isWhitespace(content.charAt(i - 1))) {
                return i;
            }
        }
        return -1;
    }
}
