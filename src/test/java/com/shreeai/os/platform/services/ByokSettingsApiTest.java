package com.shreeai.os.platform.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>ByokSettingsApiTest</b>
 *
 * <p>12 test cases for ByokSettingsService and ByokSettingsController logic.</p>
 *
 * @since v1.0
 */
public class ByokSettingsApiTest {

    private final ByokSettingsService service = new ByokSettingsService();

    // ─── ProviderSettings ──────────────────────────────────────────────────

    @Test
    void maskKey_showsLastFourCharacters() {
        String masked = ProviderSettings.maskKey("sk-abc123xyz789");
        assertTrue(masked.startsWith("****"));
        assertTrue(masked.endsWith("z789"));
        assertFalse(masked.contains("abc1"));
    }

    @Test
    void maskKey_shortKey_returnsMasked() {
        String masked = ProviderSettings.maskKey("sk-short");
        assertEquals("****", masked);
    }

    @Test
    void maskKey_nullKey_returnsMasked() {
        String masked = ProviderSettings.maskKey(null);
        assertEquals("****", masked);
    }

    // ─── Validation ─────────────────────────────────────────────────────────

    @Test
    void validate_withValidSettings_returnsEmpty() {
        var settings = new ProviderSettings(ProviderType.OPENAI, true, "sk-valid-key-123456", "");
        Optional<String> result = service.validate(settings);
        assertTrue(result.isEmpty());
    }

    @Test
    void validate_withNullSettings_returnsError() {
        Optional<String> result = service.validate(null);
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("null"));
    }

    @Test
    void validate_withShortKey_returnsError() {
        var settings = new ProviderSettings(ProviderType.OPENAI, true, "short", "");
        Optional<String> result = service.validate(settings);
        assertTrue(result.isPresent());
    }

    @Test
    void validate_ollamaWithoutEndpoint_returnsError() {
        var settings = new ProviderSettings(ProviderType.OLLAMA, true, "local-key-1234", "");
        Optional<String> result = service.validate(settings);
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Endpoint"));
    }

    // ─── Save ──────────────────────────────────────────────────────────────

    @Test
    void save_withValidSettings_storesMasked() {
        var saved = service.save(new ProviderSettings(ProviderType.GEMINI, true,
                "gemini-key-abcdefgh1234", ""));
        assertEquals(ProviderType.GEMINI, saved.provider());
        assertTrue(saved.maskedKey().startsWith("****"));
        assertTrue(saved.maskedKey().endsWith("1234"));
    }

    @Test
    void save_withInvalidSettings_throws() {
        var settings = new ProviderSettings(ProviderType.OPENAI, true, "short", "");
        assertThrows(IllegalArgumentException.class, () -> service.save(settings));
    }

    // ─── List & Delete ────────────────────────────────────────────────────

    @Test
    void list_startsEmpty() {
        assertTrue(service.list().isEmpty());
    }

    @Test
    void delete_removesProvider() {
        service.save(new ProviderSettings(ProviderType.OPENAI, true, "sk-openai-abcdefgh", ""));
        assertTrue(service.exists(ProviderType.OPENAI));
        assertTrue(service.delete(ProviderType.OPENAI));
        assertFalse(service.exists(ProviderType.OPENAI));
    }

    @Test
    void validateRawKey_rejectsEmpty() {
        Optional<String> result = service.validateRawKey(ProviderType.OPENAI, "");
        assertTrue(result.isPresent());
    }
}
