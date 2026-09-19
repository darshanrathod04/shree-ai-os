package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * <b>ProviderCapability</b>
 *
 * <p>Immutable description of what one provider type can serve: the provider
 * plus the locked list of topic names it supports.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Declares the routing capability of one {@link ProviderType}.</li>
 *   <li>Provides a deterministic, case-insensitive {@link #supports(String)}
 *       membership check (no fuzzy matching).</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the topic list is
 * defensively copied and unmodifiable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param provider        the provider type this capability describes (must not
 *                        be null)
 * @param supportedTopics the topic names this provider can serve (never null)
 */
public record ProviderCapability(
        ProviderType provider,
        List<String> supportedTopics) {

    /**
     * Creates a deeply-immutable ProviderCapability with defensive copying.
     *
     * @throws NullPointerException if provider is null
     */
    public ProviderCapability {
        Objects.requireNonNull(provider, "provider must not be null");
        supportedTopics = supportedTopics == null
                ? List.of()
                : List.copyOf(supportedTopics);
    }

    /**
     * Creates a capability from a provider and its supported topics.
     *
     * @param provider the provider type (must not be null)
     * @param topics   the supported topic names
     * @return a new immutable ProviderCapability (never null)
     */
    public static ProviderCapability of(ProviderType provider, String... topics) {
        return new ProviderCapability(provider, List.of(topics));
    }

    /**
     * Deterministic, case-insensitive, exact-name membership check. No fuzzy
     * matching - a topic is supported only when its name matches exactly,
     * ignoring case.
     *
     * @param topicName the topic name to check (may be null; returns false)
     * @return {@code true} when the provider supports this topic name
     */
    public boolean supports(String topicName) {
        if (topicName == null) {
            return false;
        }
        String normalized = topicName.toLowerCase(Locale.ROOT);
        return supportedTopics.stream()
                .anyMatch(t -> t.toLowerCase(Locale.ROOT).equals(normalized));
    }

    @Override
    public String toString() {
        return "ProviderCapability{provider=" + provider
                + ", supportedTopics=" + supportedTopics.size() + "}";
    }
}
