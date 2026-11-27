package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>CodeDocumentParser</b>
 *
 * <p>Symbol-aware structural parser for {@link ParserType#CODE}. Splits source
 * files (Java, TypeScript/JavaScript, Python) along top-level type/function/method
 * declarations so RAG retrieval matches whole logical code blocks.</p>
 */
public final class CodeDocumentParser implements DocumentParser {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile(
            "^\\s*(?:(?:public|private|protected|static|final|abstract|export|async|default)\\s+)*"
                    + "(?:class|interface|enum|record|function|def|const|type)\\s+([A-Za-z_][A-Za-z0-9_]*)"
                    + "|^\\s*(?:public|private|protected)\\s+[\\w<>\\[\\], ?]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

    @Override
    public ParserType supports() {
        return ParserType.CODE;
    }

    @Override
    public List<ParsedSection> parse(String content) {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isBlank()) {
            return List.of();
        }

        String[] lines = content.split("\\R", -1);
        List<ParsedSection> sections = new ArrayList<>();
        String currentLabel = "header";
        StringBuilder currentBlock = new StringBuilder();

        for (String line : lines) {
            Matcher m = SYMBOL_PATTERN.matcher(line);
            if (m.find()) {
                if (!currentBlock.toString().isBlank()) {
                    sections.add(new ParsedSection(currentLabel, currentBlock.toString().strip()));
                    currentBlock.setLength(0);
                }
                currentLabel = m.group(1) != null ? m.group(1) : m.group(2);
            }
            currentBlock.append(line).append('\n');
        }
        if (!currentBlock.toString().isBlank()) {
            sections.add(new ParsedSection(currentLabel, currentBlock.toString().strip()));
        }
        return List.copyOf(sections);
    }
}
