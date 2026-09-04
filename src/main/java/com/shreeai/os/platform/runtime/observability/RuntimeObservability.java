package com.shreeai.os.platform.runtime.observability;

import java.util.Objects;

/**
 * <b>RuntimeObservability</b>
 *
 * <p>Facade that wires the Runtime Kernel's observability stack together
 * from an {@link ObservabilityConfig}: a {@link StructuredLogger}, a
 * {@link RuntimeMetrics} registry, and a {@link RuntimeTracer}. Provides a
 * single entry point so downstream code can instrument executions without
 * depending on each individual component.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Constructs and exposes the three observability components.</li>
 *   <li>Honors the {@link ObservabilityConfig} toggles (enabled flags).</li>
 *   <li>Bridges tracing to structured logs via the shared trace ID.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class RuntimeObservability {

    private final ObservabilityConfig config;
    private final StructuredLogger logger;
    private final RuntimeMetrics metrics;
    private final RuntimeTracer tracer;

    /**
     * Creates a fully-wired observability stack using the given config.
     *
     * @param config the observability configuration (never null)
     */
    public RuntimeObservability(ObservabilityConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.logger = StructuredLogger.of(RuntimeObservability.class);
        this.metrics = new RuntimeMetrics();
        this.tracer = new RuntimeTracer(config.traceExporterEndpoint() != null
                ? new SpanExporterForwarder(config.traceExporterEndpoint())
                : null,
                config.maxStoredSpans());
    }

    // ==========================================================
    // Accessors
    // ==========================================================

    /** @return the underlying configuration */
    public ObservabilityConfig config() {
        return config;
    }

    /** @return the structured logger (never null) */
    public StructuredLogger logger() {
        return logger;
    }

    /** @return the metrics registry (never null) */
    public RuntimeMetrics metrics() {
        return metrics;
    }

    /** @return the tracer (never null) */
    public RuntimeTracer tracer() {
        return tracer;
    }

    // ==========================================================
    // Convenience
    // ==========================================================

    /** @return whether structured logging is enabled per config */
    public boolean loggingEnabled() {
        return config.structuredLoggingEnabled();
    }

    /** @return whether metrics collection is enabled per config */
    public boolean metricsEnabled() {
        return config.metricsEnabled();
    }

    /** @return whether tracing is enabled per config */
    public boolean tracingEnabled() {
        return config.tracingEnabled();
    }

    /**
     * Records an execution outcome across all enabled components.
     *
     * <p>When metrics are enabled, increments the {@code execution_total}
     * counter and records the duration histogram. When tracing is enabled, a
     * span is created and exported. Structured lifecycle logs are emitted
     * when logging is enabled.</p>
     *
     * @param capability    the executed capability
     * @param durationMs    the execution duration in milliseconds
     * @param success       whether the execution succeeded
     */
    public void recordExecution(String capability, long durationMs, boolean success) {
        if (loggingEnabled()) {
            logger.executionEvent(
                    success ? "complete" : "fail",
                    capability,
                    durationMs,
                    "outcome", success ? "success" : "failure");
        }
        if (metricsEnabled()) {
            metrics.increment("execution_total");
            metrics.increment("execution_status_total", success ? 1 : 0);
            metrics.record("execution_duration_seconds", durationMs / 1000.0);
        }
        if (tracingEnabled()) {
            RuntimeTracer.ActiveSpan span = tracer.startSpan("execute:" + capability);
            span.attribute("success", Boolean.toString(success));
            span.end(success ? RuntimeSpan.Status.OK : RuntimeSpan.Status.ERROR);
        }
    }

    /** Minimal exporter that forwards the endpoint string to a log line. */
    private static final class SpanExporterForwarder implements RuntimeTracer.SpanExporter {

        private final String endpoint;

        private SpanExporterForwarder(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void export(RuntimeSpan span) {
            // Downstream adapters (e.g. an OTLP HTTP exporter) would send the
            // span to `endpoint`; here we only expose the endpoint for wiring.
            StructuredLogger.of(RuntimeObservability.class)
                    .executionEvent("span_exported", span.name(), span.durationMs(),
                            "traceId", span.traceId(),
                            "endpoint", endpoint);
        }
    }
}
