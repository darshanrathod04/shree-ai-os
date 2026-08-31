package com.shreeai.os.platform.runtime.vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>PgVectorsJson</b>
 *
 * <p>JSON (de)serialization helper for the {@code jsonb} metadata column of
 * the PgVector tables. Uses the platform's existing Jackson dependency.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgVectorsJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {
            };

    private PgVectorsJson() {
        // static utility
    }

    /**
     * Serializes a metadata map to a JSON string.
     *
     * @param metadata the map (must not be null)
     * @return JSON string (never null)
     * @throws VectorRuntimeException if serialization fails
     */
    public static String toJson(Map<String, Object> metadata) {
        try {
            return MAPPER.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (Exception e) {
            throw new VectorRuntimeException("Failed to serialize metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string to a metadata map.
     *
     * @param json the JSON string (may be null or blank)
     * @return the map, or {@code null} when the input is blank
     * @throws VectorRuntimeException if deserialization fails
     */
    public static Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> result = MAPPER.readValue(json, MAP_TYPE);
            return result != null ? new HashMap<>(result) : null;
        } catch (Exception e) {
            throw new VectorRuntimeException("Failed to deserialize metadata: " + e.getMessage(), e);
        }
    }
}
