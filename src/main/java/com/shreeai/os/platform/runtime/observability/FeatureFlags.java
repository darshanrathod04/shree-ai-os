package com.shreeai.os.platform.runtime.observability;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <b>FeatureFlags</b>
 *
 * <p>Runtime feature-flag manager that resolves whether a {@link FeatureFlag}
 * is enabled. Resolution precedence, from highest to lowest:</p>
 * <ol>
 *   <li>Explicit programmatic override via {@link #set(FeatureFlag, boolean)}.</li>
 *   <li>System property {@code shree.feature.&lt;NAME&gt;} (e.g.
 *       {@code -Dshree.feature.OPEN_TELEMETRY=true}).</li>
 *   <li>Environment variable {@code SHREE_FEATURE_&lt;NAME&gt;}</li>
 *   <li>The flag's built-in default.</li>
 * </ol>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Centralizes feature gating so new capabilities can ship dark.</li>
 *   <li>Enables runtime toggling without code changes or redeploys.</li>
 *   <li>Is thread-safe for concurrent reads and writes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform — Runtime Kernel</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class FeatureFlags {

    private final Map<FeatureFlag, Boolean> overrides = new EnumMap<>(FeatureFlag.class);
    private final FeatureFlags delegate;

    /**
     * Creates an independent flag manager.
     */
    public FeatureFlags() {
        this(null);
    }

    /**
     * Creates a flag manager that falls back to a delegate for flags that
     * have not been overridden or resolved here.
     *
     * @param delegate optional parent manager (may be null)
     */
    public FeatureFlags(FeatureFlags delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns whether the given feature flag is enabled, applying the full
     * resolution precedence.
     *
     * @param flag the feature flag (never null)
     * @return true when enabled
     */
    public boolean isEnabled(FeatureFlag flag) {
        Objects.requireNonNull(flag, "flag must not be null");

        Boolean override = overrides.get(flag);
        if (override != null) {
            return override;
        }

        Boolean fromProperty = fromSystemProperty(flag);
        if (fromProperty != null) {
            return fromProperty;
        }

        Boolean fromEnv = fromEnvironment(flag);
        if (fromEnv != null) {
            return fromEnv;
        }

        if (delegate != null) {
            return delegate.isEnabled(flag);
        }

        return flag.defaultEnabled();
    }

    /**
     * Programmatically sets the enabled state for a flag.
     *
     * @param flag    the feature flag (never null)
     * @param enabled whether the flag should be enabled
     */
    public void set(FeatureFlag flag, boolean enabled) {
        Objects.requireNonNull(flag, "flag must not be null");
        overrides.put(flag, enabled);
    }

    /**
     * Clears any programmatic override for the flag, returning resolution to
     * the environment and defaults.
     *
     * @param flag the feature flag (never null)
     */
    public void clear(FeatureFlag flag) {
        Objects.requireNonNull(flag, "flag must not be null");
        overrides.remove(flag);
    }

    /**
     * Returns a snapshot of all flags and their currently-resolved state.
     *
     * @return an immutable map of flag → enabled
     */
    public Map<FeatureFlag, Boolean> snapshot() {
        Map<FeatureFlag, Boolean> result = new EnumMap<>(FeatureFlag.class);
        for (FeatureFlag flag : FeatureFlag.values()) {
            result.put(flag, isEnabled(flag));
        }
        return Map.copyOf(result);
    }

    // ==========================================================
    // Resolution Helpers
    // ==========================================================

    private static Boolean fromSystemProperty(FeatureFlag flag) {
        String value = System.getProperty("shree.feature." + flag.name());
        return parse(value);
    }

    private static Boolean fromEnvironment(FeatureFlag flag) {
        String value = System.getenv("SHREE_FEATURE_" + flag.name());
        return parse(value);
    }

    private static Boolean parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "on".equals(normalized)) {
            return Boolean.TRUE;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "off".equals(normalized)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
