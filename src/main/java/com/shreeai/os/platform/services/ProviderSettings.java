package com.shreeai.os.platform.services;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ProviderSettings</b>
 *
 * <p>Immutable record representing the settings for an LLM provider.
 * Keys are stored masked — raw keys are never persisted or returned.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public final class ProviderSettings {

    private final ProviderType provider;
    private final boolean enabled;
    private final String maskedKey;
    private final String endpoint;

    public ProviderSettings(ProviderType provider, boolean enabled, String maskedKey, String endpoint) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.enabled = enabled;
        this.maskedKey = maskedKey == null ? "" : maskedKey;
        this.endpoint = endpoint == null ? "" : endpoint;
    }

    public ProviderType provider() { return provider; }
    public boolean enabled() { return enabled; }
    public String maskedKey() { return maskedKey; }
    public String endpoint() { return endpoint; }

    /**
     * Masks a raw API key, showing only the last 4 characters.
     * e.g. "sk-abc123xyz" → "sk-****xyz"
     */
    public static String maskKey(String rawKey) {
        if (rawKey == null || rawKey.length() <= 8) {
            return "****";
        }
        String last4 = rawKey.substring(rawKey.length() - 4);
        return "****" + last4;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "provider", provider.name(),
                "enabled", enabled,
                "maskedKey", maskedKey,
                "endpoint", endpoint
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ProviderType provider;
        private boolean enabled;
        private String maskedKey;
        private String endpoint;

        public Builder provider(ProviderType v) { this.provider = v; return this; }
        public Builder enabled(boolean v) { this.enabled = v; return this; }
        public Builder maskedKey(String v) { this.maskedKey = v; return this; }
        public Builder endpoint(String v) { this.endpoint = v; return this; }

        public ProviderSettings build() {
            return new ProviderSettings(provider, enabled, maskedKey, endpoint);
        }
    }
}
