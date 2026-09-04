package com.shreeai.os.platform.runtime.observability;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>RuntimeTracer</b>
 *
 * <p>Lightweight, dependency-free tracer providing OpenTelemetry-style span
 * lifecycle management and in-process context propagation for the Runtime
 * Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Creates root and child spans with W3C-compatible {@link TraceContext}.</li>
 *   <li>Maintains a thread-local current span for in-process propagation.</li>
 *   <li>Records completed spans in a bounded in-memory store for export.</li>
 *   <li>Provides a sample/export hook that downstream adapters can implement.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class RuntimeTracer {

    /** Callback invoked when a span completes, allowing export. */
    public interface SpanExporter {
        void export(RuntimeSpan span);
    }

    private static final int DEFAULT_MAX_STORED_SPANS = 512;

    private final List<RuntimeSpan> storedSpans =
            Collections.synchronizedList(new ArrayList<>());
    private final int maxStoredSpans;
    private final SpanExporter exporter;
    private final ThreadLocal<TraceContext> currentContext = new ThreadLocal<>();

    /**
     * Creates a tracer with default settings (no exporter, 512 stored spans).
     */
    public RuntimeTracer() {
        this(null, DEFAULT_MAX_STORED_SPANS);
    }

    /**
     * Creates a tracer with an optional exporter and bounded span storage.
     *
     * @param exporter        optional export callback (may be null)
     * @param maxStoredSpans  maximum number of completed spans retained
     */
    public RuntimeTracer(SpanExporter exporter, int maxStoredSpans) {
        this.exporter = exporter;
        this.maxStoredSpans = Math.max(1, maxStoredSpans);
    }

    /**
     * Starts a new root span (no parent) and makes it the current span for
     * the calling thread.
     *
     * @param name the span name
     * @return a new active span
     */
    public ActiveSpan startSpan(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return startSpan(name, Map.of());
    }

    /**
     * Starts a new root span with attributes.
     *
     * @param name       the span name
     * @param attributes span attributes (may be empty)
     * @return a new active span
     */
    public ActiveSpan startSpan(String name, Map<String, String> attributes) {
        TraceContext parent = currentContext.get();
        TraceContext context = parent == null
                ? TraceContext.newRoot()
                : TraceContext.childOf(parent);
        return begin(name, context, attributes);
    }

    /**
     * Starts a child span of the given explicit parent context.
     *
     * @param name   the span name
     * @param parent the parent trace context (never null)
     * @return a new active span
     */
    public ActiveSpan startChild(String name, TraceContext parent) {
        Objects.requireNonNull(parent, "parent must not be null");
        return begin(name, TraceContext.childOf(parent), Map.of());
    }

    /**
     * Returns the current span's trace context for the calling thread, or
     * {@code null} when no span is active.
     *
     * @return the current trace context, or null
     */
    public TraceContext currentContext() {
        return currentContext.get();
    }

    /**
     * Returns the current trace ID for the calling thread (used to correlate
     * structured logs with traces), or {@code null} when no span is active.
     *
     * @return the current trace ID, or null
     */
    public String currentTraceId() {
        TraceContext context = currentContext.get();
        return context == null ? null : context.traceId();
    }

    /**
     * Returns an immutable snapshot of all stored completed spans.
     *
     * @return completed spans (never null)
     */
    public List<RuntimeSpan> completedSpans() {
        synchronized (storedSpans) {
            return List.copyOf(storedSpans);
        }
    }

    /**
     * Clears all stored completed spans.
     */
    public void reset() {
        synchronized (storedSpans) {
            storedSpans.clear();
        }
    }

    // ==========================================================
    // Internal
    // ==========================================================

    private ActiveSpan begin(String name, TraceContext context, Map<String, String> attributes) {
        currentContext.set(context);
        return new ActiveSpan(this, name, context, attributes);
    }

    void end(ActiveSpan active, Instant endedAt, RuntimeSpan.Status status,
             Map<String, String> finalAttributes) {
        // Clear context if it still references this span's context (avoids
        // clearing a child that was started and nested afterwards).
        TraceContext current = currentContext.get();
        if (current != null && current.spanId().equals(active.context().spanId())) {
            currentContext.remove();
        }

        RuntimeSpan span = RuntimeSpan.builder()
                .name(active.name())
                .context(active.context())
                .startedAt(active.startedAt())
                .endedAt(endedAt)
                .attributes(finalAttributes)
                .status(status)
                .build();

        synchronized (storedSpans) {
            if (storedSpans.size() >= maxStoredSpans) {
                storedSpans.remove(0);
            }
            storedSpans.add(span);
        }

        if (exporter != null) {
            exporter.export(span);
        }
    }

    /**
     * Handle to a running span. End it to record it with the tracer.
     */
    public static final class ActiveSpan {

        private final RuntimeTracer tracer;
        private final String name;
        private final TraceContext context;
        private final Instant startedAt;
        private final Map<String, String> attributes;
        private boolean ended = false;

        private ActiveSpan(RuntimeTracer tracer, String name, TraceContext context,
                           Map<String, String> attributes) {
            this.tracer = tracer;
            this.name = name;
            this.context = context;
            this.startedAt = Instant.now();
            this.attributes = new java.util.LinkedHashMap<>(attributes);
        }

        /** @return the span name */
        public String name() {
            return name;
        }

        /** @return the trace context */
        public TraceContext context() {
            return context;
        }

        /** @return when the span was started */
        public Instant startedAt() {
            return startedAt;
        }

        /**
         * Adds an attribute to the span.
         *
         * @param key   the attribute key
         * @param value the attribute value
         * @return this active span
         */
        public ActiveSpan attribute(String key, String value) {
            attributes.put(key, value);
            return this;
        }

        /**
         * Ends the span successfully.
         */
        public void end() {
            end(RuntimeSpan.Status.OK);
        }

        /**
         * Ends the span with an explicit status.
         *
         * @param status the terminal status
         */
        public void end(RuntimeSpan.Status status) {
            if (ended) {
                return;
            }
            ended = true;
            tracer.end(this, Instant.now(), status, attributes);
        }
    }
}
