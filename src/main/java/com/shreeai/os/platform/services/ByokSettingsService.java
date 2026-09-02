package com.shreeai.os.platform.services;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>ByokSettingsService</b>
 *
 * <p>Manages per-provider API key settings for the Bring-Your-Own-Key (BYOK)
 * configuration. Keys are masked before being stored. Validation occurs before
 * any settings are saved.</p>
 *
 * <p><b>Security rules:</b></p>
 * <ul>
 *   <li>Keys are stored masked (last 4 characters only)</li>
 *   <li>Raw keys are never returned from any getter</li>
 *   <li>Validation must pass before save</li>
 *   <li>Provider status is tracked alongside the key</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
@Service
public class ByokSettingsService {

    private final Map<ProviderType, ProviderSettings> store = new ConcurrentHashMap<>();

    /**
     * Validates the proposed settings without saving them.
     *
     * @return an empty Optional if valid, or an error message
     */
    public Optional<String> validate(ProviderSettings settings) {
        if (settings == null) {
            return Optional.of("Settings must not be null");
        }
        if (settings.provider() == null) {
            return Optional.of("Provider type must be specified");
        }
        if (settings.maskedKey() == null || settings.maskedKey().isEmpty()) {
            return Optional.of("API key must be provided");
        }
        if (settings.maskedKey().length() < 8) {
            return Optional.of("API key is too short (minimum 8 characters)");
        }
        if (settings.provider() == ProviderType.OLLAMA) {
            if (settings.endpoint() == null || settings.endpoint().isBlank()) {
                return Optional.of("Endpoint required for Ollama");
            }
        }
        return Optional.empty();
    }

    /**
     * Saves the settings if validation passes. Returns the saved (masked)
     * settings or throws on validation failure.
     */
    public ProviderSettings save(ProviderSettings settings) {
        Optional<String> err = validate(settings);
        if (err.isPresent()) {
            throw new IllegalArgumentException(err.get());
        }
        // Store masked
        ProviderSettings masked = ProviderSettings.builder()
                .provider(settings.provider())
                .enabled(settings.enabled())
                .maskedKey(ProviderSettings.maskKey(settings.maskedKey()))
                .endpoint(settings.endpoint() == null ? "" : settings.endpoint())
                .build();
        store.put(settings.provider(), masked);
        return masked;
    }

    /**
     * Returns the settings for a provider (with masked key only).
     */
    public ProviderSettings get(ProviderType provider) {
        return store.get(provider);
    }

    /**
     * Returns all settings, with keys masked.
     */
    public List<ProviderSettings> list() {
        return List.copyOf(store.values());
    }

    /**
     * Returns the settings as a map keyed by provider name.
     */
    public Map<String, Object> asMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (ProviderSettings s : store.values()) {
            out.put(s.provider().name(), s.toMap());
        }
        return out;
    }

    /**
     * Deletes the settings for a provider.
     */
    public boolean delete(ProviderType provider) {
        return store.remove(provider) != null;
    }

    /**
     * Returns true if settings are present for the given provider.
     */
    public boolean exists(ProviderType provider) {
        return store.containsKey(provider);
    }

    /**
     * Returns the total number of stored provider settings.
     */
    public int size() {
        return store.size();
    }

    /**
     * Validates a raw API key. Returns an empty optional if the key looks
     * reasonable, or an error message otherwise.
     */
    public Optional<String> validateRawKey(ProviderType provider, String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.of("API key must not be empty");
        }
        if (rawKey.length() < 8) {
            return Optional.of("API key is too short");
        }
        if (provider == null) {
            return Optional.of("Provider type must be specified");
        }
        return Optional.empty();
    }
}
