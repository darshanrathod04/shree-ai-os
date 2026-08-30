package com.shreeai.os.platform.runtime.observability;

import java.util.Objects;

/**
 * <b>ObservabilityConfig</b>
 *
 * <p>Immutable, runtime-adjustable configuration for the Runtime Kernel's
 * observability stack: structured logging, Prometheus metrics, and
 * OpenTelemetry-style tracing. Values are resolved from system properties
 * and environment variables at build time, with sensible defaults.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Centralizes all observability toggles and tuning knobs.</li>
 *   <li>Resolves configuration from {@code System.getProperty} and
 *       {@code System.getenv} for runtime/feature-flag style control.</li>
 *   <li>Remains immutable after construction for thread-safe publication.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class ObservabilityConfig {

    public static final String DEFAULT_METRIC_PREFIX = "shreeai_runtime_";

    private final boolean structuredLoggingEnabled;
    private final boolean metricsEnabled;
    private final boolean tracingEnabled;
    private final double traceSampleRate;
    private final String metricPrefix;
    private final String traceExporterEndpoint;
    private final int maxStoredSpans;

    private ObservabilityConfig(Builder builder) {
        this.structuredLoggingEnabled = builder.structuredLoggingEnabled;
        this.metricsEnabled = builder.metricsEnabled;
        this.tracingEnabled = builder.tracingEnabled;
        this.traceSampleRate = builder.traceSampleRate;
        this.metricPrefix = builder.metricPrefix;
        this.traceExporterEndpoint = builder.traceExporterEndpoint;
        this.maxStoredSpans = builder.maxStoredSpans;
    }

    // ==========================================================
    // Accessors
    // ==========================================================

    /** @return whether structured key-value logging is enabled */
    public boolean structuredLoggingEnabled() {
        return structuredLoggingEnabled;
    }

    /** @return whether Prometheus metrics collection is enabled */
    public boolean metricsEnabled() {
        return metricsEnabled;
    }

    /** @return whether tracing is enabled */
    public boolean tracingEnabled() {
        return tracingEnabled;
    }

    /**
     * @return the trace sampling rate in [0.0, 1.0]; 1.0 samples all traces
     */
    public double traceSampleRate() {
        return traceSampleRate;
    }

    /** @return the prefix applied to emitted metric names */
    public String metricPrefix() {
        return metricPrefix;
    }

    /** @return the OTLP exporter endpoint, or {@code null} when unset */
    public String traceExporterEndpoint() {
        return traceExporterEndpoint;
    }

    /** @return the maximum number of completed spans retained in memory */
    public int maxStoredSpans() {
        return maxStoredSpans;
    }

    // ==========================================================
    // Resolution
    // ==========================================================

    /**
     * Builds an {@link ObservabilityConfig} resolved from the environment.
     *
     * <p>Supported keys (system property takes precedence over environment
     * variable):</p>
     * <ul>
     *   <li>{@code shree.observability.logging} / {@code SHREE_OBSERVABILITY_LOGGING}</li>
     *   <li>{@code shree.observability.metrics} / {@code SHREE_OBSERVABILITY_METRICS}</li>
     *   <li>{@code shree.observability.tracing} / {@code SHREE_OBSERVABILITY_TRACING}</li>
     *   <li>{@code shree.observability.traceSampleRate} / {@code SHREE_TRACE_SAMPLE_RATE}</li>
     *   <li>{@code shree.observability.metricPrefix} / {@code SHREE_METRIC_PREFIX}</li>
     *   <li>{@code shree.observability.traceExporterEndpoint} / {@code SHREE_TRACE_EXPORTER_ENDPOINT}</li>
     *   <li>{@code shree.observability.maxStoredSpans} / {@code SHREE_MAX_STORED_SPANS}</li>
     * </ul>
     *
     * @return the resolved configuration
     */
    public static ObservabilityConfig fromEnvironment() {
        return builder()
                .structuredLoggingEnabled(boolProp(
                        "shree.observability.logging",
                        "SHREE_OBSERVABILITY_LOGGING",
                        true))
                .metricsEnabled(boolProp(
                        "shree.observability.metrics",
                        "SHREE_OBSERVABILITY_METRICS",
                        true))
                .tracingEnabled(boolProp(
                        "shree.observability.tracing",
                        "SHREE_OBSERVABILITY_TRACING",
                        false))
                .traceSampleRate(doubleProp(
                        "shree.observability.traceSampleRate",
                        "SHREE_TRACE_SAMPLE_RATE",
                        1.0))
                .metricPrefix(strProp(
                        "shree.observability.metricPrefix",
                        "SHREE_METRIC_PREFIX",
                        DEFAULT_METRIC_PREFIX))
                .traceExporterEndpoint(strProp(
                        "shree.observability.traceExporterEndpoint",
                        "SHREE_TRACE_EXPORTER_ENDPOINT",
                        null))
                .maxStoredSpans(intProp(
                        "shree.observability.maxStoredSpans",
                        "SHREE_MAX_STORED_SPANS",
                        512))
                .build();
    }

    private static boolean boolProp(String property, String env, boolean fallback) {
        String value = firstNonBlank(System.getProperty(property), System.getenv(env));
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static double doubleProp(String property, String env, double fallback) {
        String value = firstNonBlank(System.getProperty(property), System.getenv(env));
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int intProp(String property, String env, int fallback) {
        String value = firstNonBlank(System.getProperty(property), System.getenv(env));
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String strProp(String property, String env, String fallback) {
        return firstNonBlank(System.getProperty(property), System.getenv(env), fallback);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    // ==========================================================
    // Builder
    // ==========================================================

    /** Creates a new builder with defaults. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link ObservabilityConfig}. */
    public static final class Builder {

        private boolean structuredLoggingEnabled = true;
        private boolean metricsEnabled = true;
        private boolean tracingEnabled = false;
        private double traceSampleRate = 1.0;
        private String metricPrefix = DEFAULT_METRIC_PREFIX;
        private String traceExporterEndpoint;
        private int maxStoredSpans = 512;

        private Builder() {
        }

        public Builder structuredLoggingEnabled(boolean enabled) {
            this.structuredLoggingEnabled = enabled;
            return this;
        }

        public Builder metricsEnabled(boolean enabled) {
            this.metricsEnabled = enabled;
            return this;
        }

        public Builder tracingEnabled(boolean enabled) {
            this.tracingEnabled = enabled;
            return this;
        }

        public Builder traceSampleRate(double sampleRate) {
            if (sampleRate < 0.0 || sampleRate > 1.0) {
                throw new IllegalArgumentException("traceSampleRate must be in [0.0, 1.0]");
            }
            this.traceSampleRate = sampleRate;
            return this;
        }

        public Builder metricPrefix(String prefix) {
            this.metricPrefix = Objects.requireNonNull(prefix, "metricPrefix must not be null");
            return this;
        }

        public Builder traceExporterEndpoint(String endpoint) {
            this.traceExporterEndpoint = endpoint;
            return this;
        }

        public Builder maxStoredSpans(int maxStoredSpans) {
            if (maxStoredSpans < 1) {
                throw new IllegalArgumentException("maxStoredSpans must be >= 1");
            }
            this.maxStoredSpans = maxStoredSpans;
            return this;
        }

        public ObservabilityConfig build() {
            return new ObservabilityConfig(this);
        }
    }
}
