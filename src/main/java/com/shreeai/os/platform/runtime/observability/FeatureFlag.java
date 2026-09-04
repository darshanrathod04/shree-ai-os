package com.shreeai.os.platform.runtime.observability;

import java.util.Locale;

/**
 * <b>FeatureFlag</b>
 *
 * <p>Enumerates the feature flags that gate behavior across the Platform.
 * Each flag declares a default state (typically {@code true} once the
 * feature is stable) that can be overridden at runtime via system properties,
 * environment variables, or programmatic toggles managed by
 * {@link FeatureFlags}.</p>
 *
 * <p><b>Ownership:</b> Platform — Runtime Kernel</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public enum FeatureFlag {

    /** Autonomous execution dispatch infrastructure (V2.1). */
    AUTONOMOUS_DISPATCH(true),

    /** Post-execution reflection analysis kernel (V2.1). */
    REFLECTION_KERNEL(true),

    /** Centralized tool registry for capability-driven tool execution (V2.1). */
    TOOL_REGISTRY(true),

    /** Human/autonomous approval workflow for sensitive executions (V2.1). */
    APPROVAL_WORKFLOW(true),

    /** Structured key-value logging with correlation IDs (V2.2). */
    STRUCTURED_LOGGING(true),

    /** Prometheus metrics collection and exposition (V2.2). */
    PROMETHEUS_METRICS(true),

    /** OpenTelemetry-style tracing and context propagation (V2.2). */
    OPEN_TELEMETRY(false),

    /** Adaptive reflection that tunes itself from execution history (V3). */
    ADAPTIVE_REFLECTION(false),

    /** Registry of agents that can be dynamically resolved (V3). */
    AGENT_REGISTRY(false),

    /** Reusable workflow orchestration engine (V3). */
    WORKFLOW_ENGINE(false),

    /** Context-aware routing of requests to the best kernel/agent (V3). */
    CONTEXT_AWARE_ROUTING(false);

    private final boolean defaultEnabled;

    FeatureFlag(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    /**
     * @return the flag's default enabled state
     */
    public boolean defaultEnabled() {
        return defaultEnabled;
    }

    /**
     * Resolves a {@code FeatureFlag} from its name, case-insensitively.
     *
     * @param name the flag name (may be null or blank)
     * @return the matching flag, or empty when unresolvable
     */
    public static java.util.Optional<FeatureFlag> fromName(String name) {
        if (name == null || name.isBlank()) {
            return java.util.Optional.empty();
        }
        String normalized = name.trim().toUpperCase(Locale.ROOT);
        for (FeatureFlag flag : values()) {
            if (flag.name().equals(normalized)) {
                return java.util.Optional.of(flag);
            }
        }
        return java.util.Optional.empty();
    }
}
