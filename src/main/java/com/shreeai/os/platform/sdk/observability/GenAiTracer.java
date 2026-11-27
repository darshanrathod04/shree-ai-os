package com.shreeai.os.platform.sdk.observability;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>GenAiTracer</b>
 *
 * <p>OpenTelemetry-style GenAI span & trace recorder for Shree AI OS.
 * Captures per-request trace trees ({@code traceId}, {@code operation},
 * {@code durationMs}, and ordered child {@link Span}s for LLM, RAG,
 * Tool, and Structured Output stages).</p>
 */
public final class GenAiTracer {

    private static final int DEFAULT_MAX_TRACES = 200;

    private final int maxTraces;
    private final Deque<TraceRecord> traces = new ArrayDeque<>();

    public GenAiTracer() {
        this(DEFAULT_MAX_TRACES);
    }

    public GenAiTracer(int maxTraces) {
        if (maxTraces < 1) {
            throw new IllegalArgumentException("maxTraces must be >= 1");
        }
        this.maxTraces = maxTraces;
    }

    public TraceBuilder startTrace(String operation, String model) {
        return new TraceBuilder(this, operation, model);
    }

    public synchronized void record(TraceRecord trace) {
        Objects.requireNonNull(trace, "trace must not be null");
        while (traces.size() >= maxTraces) {
            traces.pollFirst();
        }
        traces.addLast(trace);
    }

    public synchronized List<TraceRecord> recent(int limit) {
        int max = Math.max(1, limit);
        List<TraceRecord> all = new ArrayList<>(traces);
        int from = Math.max(0, all.size() - max);
        return List.copyOf(all.subList(from, all.size()));
    }

    public record Span(
            String spanId,
            String name,
            String category,
            long durationMs,
            Map<String, Object> attributes
    ) {}

    public record TraceRecord(
            String traceId,
            String operation,
            String model,
            String status,
            Instant startedAt,
            long durationMs,
            List<Span> spans
    ) {}

    public static final class TraceBuilder {
        private final GenAiTracer tracer;
        private final String traceId = "tr-" + UUID.randomUUID().toString().substring(0, 12);
        private final String operation;
        private final String model;
        private final Instant startedAt = Instant.now();
        private final long startNanos = System.nanoTime();
        private final List<Span> spans = new ArrayList<>();

        private TraceBuilder(GenAiTracer tracer, String operation, String model) {
            this.tracer = tracer;
            this.operation = operation == null ? "unknown" : operation;
            this.model = model == null ? "default" : model;
        }

        public TraceBuilder addSpan(String name, String category, long durationMs, Map<String, Object> attributes) {
            spans.add(new Span(
                    "sp-" + UUID.randomUUID().toString().substring(0, 8),
                    name,
                    category,
                    Math.max(0L, durationMs),
                    attributes == null ? Map.of() : Map.copyOf(attributes)
            ));
            return this;
        }

        public TraceRecord finish(String status) {
            long totalMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
            TraceRecord record = new TraceRecord(
                    traceId,
                    operation,
                    model,
                    status == null ? "OK" : status,
                    startedAt,
                    totalMs,
                    List.copyOf(spans)
            );
            tracer.record(record);
            return record;
        }
    }
}
