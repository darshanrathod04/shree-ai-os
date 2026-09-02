package com.shreeai.os.platform.services;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * <b>ProviderType</b>
 *
 * <p>Identifies the LLM provider for BYOK (Bring Your Own Key) settings.
 * Used by the {@link ByokSettingsService} to manage provider credentials.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 */
public enum ProviderType {
    OPENAI,
    GEMINI,
    OLLAMA;

    /**
     * Parses a provider type from a string (case-insensitive). Returns null
     * if no match is found.
     */
    public static ProviderType fromString(String s) {
        if (s == null) return null;
        for (ProviderType t : values()) {
            if (t.name().equalsIgnoreCase(s.trim())) return t;
        }
        return null;
    }

    public static List<ProviderType> all() {
        return List.of(values());
    }
}
