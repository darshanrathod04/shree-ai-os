package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.services.ByokSettingsService;
import com.shreeai.os.platform.services.ProviderSettings;
import com.shreeai.os.platform.services.ProviderType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>SettingsSDK</b>
 *
 * <p>SDK façade for the BYOK (Bring Your Own Key) provider settings.
 * Exposed via {@code shree.settings()}.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public final class SettingsSDK {

    private final ByokSettingsService service;

    public SettingsSDK(ByokSettingsService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Returns all configured providers (keys masked).
     */
    public List<ProviderSettings> providers() {
        return service.list();
    }

    /**
     * Returns settings for a specific provider.
     */
    public Optional<ProviderSettings> provider(ProviderType type) {
        return Optional.ofNullable(service.get(type));
    }

    /**
     * Validates provider settings before saving.
     */
    public ValidationResult validate(ProviderType type, String apiKey, String endpoint) {
        ProviderSettings draft = new ProviderSettings(type, true, apiKey, endpoint);
        Optional<String> err = service.validate(draft);
        if (err.isPresent()) {
            return ValidationResult.of(false, err.get());
        }
        return ValidationResult.of(true, "");
    }

    /**
     * Saves provider settings (key will be masked).
     */
    public ProviderSettings save(ProviderType type, String apiKey, String endpoint) {
        return service.save(new ProviderSettings(type, true, apiKey, endpoint));
    }

    /**
     * Deletes provider settings.
     */
    public boolean delete(ProviderType type) {
        return service.delete(type);
    }

    /**
     * <b>ValidationResult</b> — outcome of a validation.
     */
    public record ValidationResult(boolean valid, String error) {
        public static ValidationResult of(boolean valid, String error) {
            return new ValidationResult(valid, error == null ? "" : error);
        }
    }
}
