package com.shreeai.os.platform.kernels.knowledge.engine.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.kernels.knowledge.model.ParserType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>JsonDocumentParser</b>
 *
 * <p>Deterministic structural parser for {@link ParserType#JSON}. Splits top-level
 * JSON object fields or array elements into semantic {@link ParsedSection}s
 * while preserving exact JSON values.</p>
 */
public final class JsonDocumentParser implements DocumentParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ParserType supports() {
        return ParserType.JSON;
    }

    @Override
    public List<ParsedSection> parse(String content) {
        Objects.requireNonNull(content, "content must not be null");
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        try {
            JsonNode root = MAPPER.readTree(trimmed);
            List<ParsedSection> sections = new ArrayList<>();
            if (root.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue());
                    sections.add(new ParsedSection(entry.getKey(), entry.getKey() + ": " + pretty));
                }
            } else if (root.isArray()) {
                for (int i = 0; i < root.size(); i++) {
                    String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root.get(i));
                    sections.add(new ParsedSection("Item " + i, pretty));
                }
            } else {
                sections.add(new ParsedSection("", root.asText()));
            }
            return List.copyOf(sections);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON content: " + e.getMessage(), e);
        }
    }
}
