package com.shreeai.os.platform.sdk.structured;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <b>StructuredOutputConverter</b>
 *
 * <p>Global-standard type-safe structured output converter (comparable to Spring AI
 * {@code BeanOutputConverter} / LangChain4j {@code AiServices} record binding).
 * Generates a JSON Schema prompt contract from any Java {@code record} or POJO,
 * extracts JSON from LLM prose/code fences, deserializes into {@code T}, and
 * retries once with the validation error if parsing fails.</p>
 */
public final class StructuredOutputConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private StructuredOutputConverter() {}

    /**
     * Builds a strict JSON-only prompt suffix describing the target Java type.
     */
    public static <T> String buildPrompt(String userPrompt, Class<T> targetType) {
        Objects.requireNonNull(userPrompt, "userPrompt must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        return userPrompt
                + "\n\nRespond ONLY with a valid JSON object matching this schema (no markdown, no extra text):\n"
                + describeSchema(targetType);
    }

    /**
     * Executes {@code llmCaller} and converts the response into {@code targetType},
     * retrying once with the parse error if the first response is invalid JSON.
     */
    public static <T> T convertWithRetry(
            String userPrompt,
            Class<T> targetType,
            Function<String, String> llmCaller) {
        Objects.requireNonNull(llmCaller, "llmCaller must not be null");
        String structuredPrompt = buildPrompt(userPrompt, targetType);
        String firstOutput = llmCaller.apply(structuredPrompt);
        try {
            return parse(firstOutput, targetType);
        } catch (IllegalArgumentException firstError) {
            String repairPrompt = structuredPrompt
                    + "\n\nYour previous output was invalid (" + firstError.getMessage() + ")."
                    + "\nPrevious output: " + firstOutput
                    + "\nReturn ONLY valid JSON matching the schema.";
            String retryOutput = llmCaller.apply(repairPrompt);
            return parse(retryOutput, targetType);
        }
    }

    /**
     * Extracts a JSON object from raw LLM output and deserializes it into {@code targetType}.
     */
    public static <T> T parse(String rawOutput, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (rawOutput == null || rawOutput.isBlank()) {
            throw new IllegalArgumentException("Empty LLM response for structured output " + targetType.getSimpleName());
        }
        String json = extractJsonObject(stripCodeFences(rawOutput));
        try {
            return MAPPER.readValue(json, targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to deserialize response into " + targetType.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public static String describeSchema(Class<?> type) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (type.isRecord()) {
            for (RecordComponent rc : type.getRecordComponents()) {
                properties.put(rc.getName(), jsonTypeName(rc.getType()));
            }
        } else {
            for (Field f : type.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    properties.put(f.getName(), jsonTypeName(f.getType()));
                }
            }
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                    "type", "object",
                    "properties", properties
            ));
        } catch (Exception e) {
            return "{\"type\":\"object\"}";
        }
    }

    private static String jsonTypeName(Class<?> fieldType) {
        if (fieldType == String.class) return "string";
        if (fieldType == boolean.class || fieldType == Boolean.class) return "boolean";
        if (fieldType == int.class || fieldType == Integer.class
                || fieldType == long.class || fieldType == Long.class) return "integer";
        if (fieldType == double.class || fieldType == Double.class
                || fieldType == float.class || fieldType == Float.class) return "number";
        if (Iterable.class.isAssignableFrom(fieldType) || fieldType.isArray()) return "array";
        return "object";
    }

    private static String stripCodeFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text;
    }
}
