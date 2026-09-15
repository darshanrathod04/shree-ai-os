package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>MarkdownDocumentParser</b>
 *
 * <p>Deterministic Markdown parser: splits content into sections at ATX
 * headings ({@code #} .. {@code ######}) of any level. Every section carries
 * its heading line plus the verbatim body beneath it, so chunk content is a
 * byte-preserving slice of the original document.</p>
 *
 * <p>Content before the first heading becomes one preamble section with an
 * empty section label; fenced code blocks ({@code ```} / {@code ~~~}) are
 * tracked so heading-like lines inside them are never treated as headings.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class MarkdownDocumentParser implements DocumentParser {

    @Override
    public ParserType supports() {
        return ParserType.MARKDOWN;
    }

    @Override
    public List<ParsedSection> parse(String content) {
        List<ParsedSection> sections = new ArrayList<>();
        int headingStart = findFirstHeading(content);
        if (headingStart < 0) {
            sections.add(new ParsedSection("", content));
            return List.copyOf(sections);
        }
        if (headingStart > 0) {
            sections.add(new ParsedSection("", content.substring(0, headingStart)));
        }
        int cursor = headingStart;
        int length = content.length();
        while (cursor < length) {
            Heading heading = readHeading(content, cursor);
            int bodyStart = heading.end();
            int nextHeadingStart = findNextHeading(content, bodyStart);
            int sectionEnd = nextHeadingStart < 0 ? length : nextHeadingStart;
            sections.add(new ParsedSection(heading.title(), content.substring(cursor, sectionEnd)));
            cursor = sectionEnd;
        }
        return List.copyOf(sections);
    }

    /** Position of the first line that is an ATX heading outside a code fence. */
    private static int findFirstHeading(String content) {
        return findNextHeading(content, 0);
    }

    /** Finds the next ATX heading position at or after {@code from}. */
    private static int findNextHeading(String content, int from) {
        int pos = from;
        boolean inFence = false;
        String fenceMarker = "";
        while (pos < content.length()) {
            int lineEnd = content.indexOf('\n', pos);
            int limit = lineEnd < 0 ? content.length() : lineEnd;
            String line = content.substring(pos, limit);
            boolean fenceLine = isFence(line);
            if (!inFence && fenceLine) {
                inFence = true;
                fenceMarker = fenceOf(line);
            } else if (inFence && fenceLine && fenceOf(line).startsWith(fenceMarker)) {
                inFence = false;
            } else if (!inFence && isHeading(line)) {
                return pos;
            }
            pos = limit + 1;
        }
        return -1;
    }

    /** Reads the heading at {@code pos}, returning its label and body offset. */
    private static Heading readHeading(String content, int pos) {
        int lineEnd = content.indexOf('\n', pos);
        if (lineEnd < 0) {
            lineEnd = content.length();
        }
        String line = content.substring(pos, lineEnd);
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') {
            level++;
        }
        String title = line.substring(level).trim();
        int bodyStart = lineEnd < content.length() ? lineEnd + 1 : content.length();
        return new Heading(title, bodyStart);
    }

    /** True when the line starts with 1-6 {@code '#'} followed by space/EOL. */
    private static boolean isHeading(String line) {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') {
            level++;
        }
        if (level == 0 || level > 6) {
            return false;
        }
        return level == line.length()
                || line.charAt(level) == ' '
                || line.charAt(level) == '\t';
    }

    /** True when the line opens or closes a fenced code block. */
    private static boolean isFence(String line) {
        return !fenceOf(line).isEmpty();
    }

    /** The fence marker of the line, or empty when the line is no fence. */
    private static String fenceOf(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("```")) {
            return "```";
        }
        if (trimmed.startsWith("~~~")) {
            return "~~~";
        }
        return "";
    }

    /** Immutable heading position record. */
    private record Heading(String title, int end) {
    }
}
