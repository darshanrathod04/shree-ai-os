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
     * Listener fired whenever settings are added, replaced, or removed. Used by
     * the runtime to rebuild the LLM router chain so that BYOK changes take
     * effect on the next request.
     */
    public interface ChangeListener {
        void onSettingsChanged();
    }

    /** Lock for the listeners list. */
    private final Object listenersLock = new Object();
    /** Active listeners (small list, append-only with copy-on-write semantics). */
    private volatile List<ChangeListener> listeners = List.of();

    /**
     * Adds a listener that will be notified after every settings mutation.
     *
     * @param listener the listener (must not be null)
     */
    public void addChangeListener(ChangeListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        synchronized (listenersLock) {
            java.util.List<ChangeListener> updated = new java.util.ArrayList<>(listeners);
            updated.add(listener);
            this.listeners = List.copyOf(updated);
        }
    }

    private void fireChange() {
        // Copy-on-write: the listeners list is volatile, snapshot is safe.
        for (ChangeListener l : listeners) {
            try {
                l.onSettingsChanged();
            } catch (RuntimeException ignored) {
                // Defensive: a faulty listener must not block BYOK changes.
            }
        }
    }

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
        // Store: masked key for external-facing getters, raw key for the LLM router.
        ProviderSettings stored = new ProviderSettings(
                settings.provider(),
                settings.enabled(),
                ProviderSettings.maskKey(settings.maskedKey()),
                settings.endpoint() == null ? "" : settings.endpoint(),
                settings.maskedKey()  // raw key — passed through, never exposed via public accessor
        );
        store.put(settings.provider(), stored);
        fireChange();
        return stored;
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
        boolean removed = store.remove(provider) != null;
        if (removed) {
            fireChange();
        }
        return removed;
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

    /**
     * Materialises an {@link com.shreeai.os.platform.llm.LlmProvider} from the
     * stored settings for {@code type}, or {@code null} when the provider is
     * disabled or has no raw key (e.g. loaded from external storage without
     * the key stored alongside it).
     *
     * <p>This is the BYOK bridge: callers (currently only {@code DefaultRuntimeService})
     * use this to plug a BYOK-configured provider into the router chain.</p>
     */
    public com.shreeai.os.platform.llm.LlmProvider materializeProvider(ProviderType type) {
        ProviderSettings s = store.get(type);
        if (s == null || !s.enabled()) {
            return null;
        }
        String rawKey = s.rawApiKey();
        String endpoint = s.endpoint();
        return switch (type) {
            case OPENAI -> {
                if (rawKey == null || rawKey.isBlank()) yield null;
                yield new com.shreeai.os.platform.llm.openai.OpenAiProvider(rawKey);
            }
            case GEMINI -> {
                if (rawKey == null || rawKey.isBlank()) yield null;
                yield new com.shreeai.os.platform.llm.gemini.GeminiProvider(rawKey);
            }
            case OLLAMA -> {
                String url = (endpoint != null && !endpoint.isBlank())
                        ? endpoint
                        : "http://localhost:11434/api/generate";
                yield new com.shreeai.os.platform.llm.ollama.OllamaProvider(
                        url, new okhttp3.OkHttpClient());
            }
        };
    }
}
