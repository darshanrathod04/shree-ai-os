package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>HtmlDocumentParser</b>
 *
 * <p>Deterministic HTML parser used for both {@link ParserType#HTML} and
 * {@link ParserType#WEB}: it extracts the readable text of the semantic tags
 * {@code h1}, {@code h2}, {@code h3} and {@code p} in document order and
 * discards everything else.</p>
 *
 * <p><b>Cleaning rules (locked):</b> comments and the elements {@code script},
 * {@code style}, {@code nav}, {@code header}, {@code footer} and
 * {@code noscript} are removed together with their content before any text is
 * read; every remaining tag is treated as plain markup and stripped; basic
 * entities ({@code &amp;}, {@code &lt;}, {@code &gt;}, {@code &quot;},
 * {@code &#39;}, {@code &apos;}, {@code &nbsp;}) and numeric entities are
 * decoded deterministically; {@code <br>} becomes a newline; whitespace runs
 * are collapsed inside blocks.</p>
 *
 * <p><b>Sections:</b> an {@code h1}/{@code h2}/{@code h3} closes the current
 * section and opens a new one named after the heading; {@code p} paragraphs
 * accumulate into the current section and are joined with blank lines. Text
 * before the first heading forms a preamble section with an empty label.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K2 Document and Web Ingestion</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class HtmlDocumentParser implements DocumentParser {

    @Override
    public ParserType supports() {
        return ParserType.HTML;
    }

    @Override
    public List<ParsedSection> parse(String content) {
        String cleaned = stripIgnored(content);
        List<ParsedSection> sections = new ArrayList<>();
        String currentSection = "";
        StringBuilder currentBody = new StringBuilder();
        int pos = 0;
        int length = cleaned.length();
        while (pos < length) {
            int open = cleaned.indexOf('<', pos);
            if (open < 0) {
                appendText(cleaned.substring(pos), currentBody);
                break;
            }
            if (open > pos) {
                appendText(cleaned.substring(pos, open), currentBody);
            }
            int close = cleaned.indexOf('>', open);
            if (close < 0) {
                break;
            }
            String tag = cleaned.substring(open + 1, close).trim();
            String name = tagName(tag);
            pos = close + 1;
            if (isSemantic(name)) {
                String inner = extractElementText(cleaned, pos, name);
                if (!inner.isEmpty()) {
                    if (isHeadingTag(name)) {
                        if (!currentBody.isEmpty()) {
                            sections.add(new ParsedSection(currentSection, currentBody.toString()));
                            currentBody = new StringBuilder();
                        }
                        currentSection = inner;
                        if (!currentBody.isEmpty()) {
                            currentBody.append("\n\n");
                        }
                        currentBody.append(inner);
                    } else {
                        if (!currentBody.isEmpty()) {
                            currentBody.append("\n\n");
                        }
                        currentBody.append(inner);
                    }
                }
                int elementEnd = elementEnd(cleaned, pos, name);
                pos = Math.max(pos, elementEnd);
            }
        }
        if (!currentBody.isEmpty()) {
            sections.add(new ParsedSection(currentSection, currentBody.toString()));
        }
        return List.copyOf(sections);
    }

    /**
     * Removes comments and ignored elements (script, style, nav, header,
     * footer, noscript) together with their content, deterministically.
     */
    private static String stripIgnored(String content) {
        StringBuilder out = new StringBuilder(content.length());
        int pos = 0;
        while (pos < content.length()) {
            int comment = content.indexOf("<!--", pos);
            int tagOpen = content.indexOf('<', pos);
            if (comment >= 0 && (tagOpen < 0 || comment == tagOpen)) {
                out.append(content, pos, comment);
                int commentEnd = content.indexOf("-->", comment + 4);
                pos = commentEnd < 0 ? content.length() : commentEnd + 3;
                continue;
            }
            if (tagOpen < 0) {
                out.append(content, pos, content.length());
                break;
            }
            String tag = tagName(content.substring(tagOpen + 1, Math.min(tagOpen + 32, content.indexOf('>', tagOpen) < 0 ? content.length() : content.indexOf('>', tagOpen))).trim());
            if (isIgnored(tag)) {
                out.append(content, pos, tagOpen);
                pos = elementEnd(content, tagOpen + 1, tag);
                continue;
            }
            out.append(content, pos, tagOpen + 1);
            pos = tagOpen + 1;
        }
        return out.toString();
    }

    /** Extracts and normalizes the inner text of the next {@code name} element. */
    private static String extractElementText(String html, int from, String name) {
        int close = indexOfCloseTag(html, from, name);
        if (close < 0) {
            return "";
        }
        String inner = html.substring(from, close);
        inner = inner.replaceAll("<br\\s*/?>", "\\n");
        inner = inner.replaceAll("<[^>]*>", "");
        return normalizeWhitespace(decodeEntities(inner));
    }

    /** Index just past the matching close tag of the element opening at {@code from}. */
    private static int elementEnd(String html, int from, String name) {
        String closeTag = "</" + name;
        int selfClose = selfCloseLimit(html, from, name);
        int close = indexOfCloseTag(html, from, name);
        if (close < 0) {
            return selfClose;
        }
        int gt = html.indexOf('>', close);
        return gt < 0 ? html.length() : gt + 1;
    }

    /** Handles void/self-closing forms such as {@code <br/>} safely. */
    private static int selfCloseLimit(String html, int from, String name) {
        int open = html.lastIndexOf('<', Math.max(from - 1, 0));
        if (open >= 0) {
            int gt = html.indexOf('>', open);
            if (gt > open && html.charAt(gt - 1) == '/') {
                return gt + 1;
            }
        }
        return from;
    }

    /** Finds the close tag of {@code name}, skipping nested same-name opens. */
    private static int indexOfCloseTag(String html, int from, String name) {
        int pos = from;
        int depth = 0;
        while (pos < html.length()) {
            int open = html.indexOf('<', pos);
            if (open < 0) {
                return -1;
            }
            int close = html.indexOf('>', open);
            if (close < 0) {
                return -1;
            }
            String tag = html.substring(open + 1, close).trim();
            if (tag.startsWith("/" + name)) {
                if (depth == 0) {
                    return open;
                }
                depth--;
            } else if (opensElement(tag, name)) {
                depth++;
            }
            pos = close + 1;
        }
        return -1;
    }

    /** True when {@code tag} opens the element {@code name} (not void, not self-closing). */
    private static boolean opensElement(String tag, String name) {
        if (!tag.equals(name) && !tag.startsWith(name + " ") && !tag.startsWith(name + "\t")) {
            return false;
        }
        return !tag.endsWith("/") && !isVoidTag(name);
    }

    /** Lower-case tag name without attributes or closing slash. */
    private static String tagName(String tag) {
        String t = tag.trim();
        if (t.startsWith("/")) {
            t = t.substring(1);
        }
        int space = t.indexOf(' ');
        String name = space < 0 ? t : t.substring(0, space);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        return name.toLowerCase();
    }

    private static boolean isSemantic(String name) {
        return isHeadingTag(name) || name.equals("p");
    }

    private static boolean isHeadingTag(String name) {
        return name.equals("h1") || name.equals("h2") || name.equals("h3");
    }

    private static boolean isIgnored(String name) {
        return name.equals("script") || name.equals("style") || name.equals("nav")
                || name.equals("header") || name.equals("footer") || name.equals("noscript");
    }

    private static boolean isVoidTag(String name) {
        return name.equals("br") || name.equals("img") || name.equals("hr") || name.equals("input");
    }

    /** Appends loose text to the body, keeping paragraph separation. */
    private static void appendText(String raw, StringBuilder body) {
        String text = normalizeWhitespace(decodeEntities(raw));
        if (text.isEmpty()) {
            return;
        }
        if (!body.isEmpty()) {
            body.append("\n\n");
        }
        body.append(text);
    }

    /** Collapses whitespace runs, trims the result, preserves single newlines. */
    private static String normalizeWhitespace(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\\n\\n");
    }

    /**
     * Decodes the deterministic subset of HTML entities: the five named
     * entities required by every HTML dialect plus the apostrophe variants
     * and numeric (decimal/hex) character references. Unknown entities are
     * left verbatim so decoding never rewrites meaning.
     */
    static String decodeEntities(String text) {
        if (text.indexOf('&') < 0) {
            return text;
        }
        StringBuilder out = new StringBuilder(text.length());
        int pos = 0;
        int length = text.length();
        while (pos < length) {
            char c = text.charAt(pos);
            if (c != '&') {
                out.append(c);
                pos++;
                continue;
            }
            int semicolon = text.indexOf(';', pos + 1);
            if (semicolon < 0 || semicolon - pos > 10) {
                out.append(c);
                pos++;
                continue;
            }
            String entity = text.substring(pos + 1, semicolon);
            String decoded = namedEntity(entity);
            if (decoded != null) {
                out.append(decoded);
                pos = semicolon + 1;
                continue;
            }
            if (entity.startsWith("#x") || entity.startsWith("#X")) {
                decoded = numericEntity(entity.substring(2), 16);
            } else if (entity.startsWith("#")) {
                decoded = numericEntity(entity.substring(1), 10);
            } else {
                decoded = null;
            }
            if (decoded != null) {
                out.append(decoded);
                pos = semicolon + 1;
            } else {
                out.append(c);
                pos++;
            }
        }
        return out.toString();
    }

    private static String namedEntity(String entity) {
        return switch (entity) {
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            case "quot" -> "\"";
            case "apos", "#39" -> "'";
            case "nbsp" -> " ";
            default -> null;
        };
    }

    private static String numericEntity(String digits, int radix) {
        if (digits.isEmpty()) {
            return null;
        }
        try {
            int code = Integer.parseInt(digits, radix);
            if (code < 0 || code > Character.MAX_CODE_POINT || Character.isISOControl(code)) {
                return null;
            }
            return new String(Character.toChars(code));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extracts the document language from the {@code lang} attribute of the
     * {@code html} element, or empty when none is declared. Used by the
     * ingestion engine to populate {@code ChunkMetadata.language}.
     *
     * @param html the raw HTML (must not be null)
     * @return the declared language tag, or empty (never null)
     */
    public static String documentLanguage(String html) {
        Objects.requireNonNull(html, "html must not be null");
        int htmlOpen = findHtmlElementOpen(html);
        if (htmlOpen < 0) {
            return "";
        }
        int gt = html.indexOf('>', htmlOpen);
        if (gt < 0) {
            return "";
        }
        String attrs = html.substring(htmlOpen + 1, gt).toLowerCase();
        int langIndex = attrs.indexOf("lang");
        if (langIndex < 0) {
            return "";
        }
        int eq = attrs.indexOf('=', langIndex + 4);
        if (eq < 0) {
            return "";
        }
        int valueStart = eq + 1;
        if (valueStart >= attrs.length()) {
            return "";
        }
        char quote = attrs.charAt(valueStart);
        int valueEnd;
        if (quote == '"' || quote == '\'') {
            valueStart++;
            valueEnd = attrs.indexOf(quote, valueStart);
        } else {
            valueEnd = valueStart;
            while (valueEnd < attrs.length() && !Character.isWhitespace(attrs.charAt(valueEnd))) {
                valueEnd++;
            }
        }
        if (valueStart > valueEnd || valueEnd > attrs.length()) {
            return "";
        }
        return attrs.substring(valueStart, Math.min(valueEnd, attrs.length())).trim();
    }

    /** Position of the {@code <html} open tag, or -1 when absent. */
    private static int findHtmlElementOpen(String html) {
        int pos = 0;
        while (pos < html.length()) {
            int open = html.indexOf('<', pos);
            if (open < 0) {
                return -1;
            }
            if (html.regionMatches(true, open + 1, "html", 0, 4)) {
                char next = open + 5 < html.length() ? html.charAt(open + 5) : '>'; 
                if (next == '>' || next == ' ' || next == '\t' || next == '\n' || next == '\r') {
                    return open;
                }
            }
            pos = open + 1;
        }
        return -1;
    }
}
