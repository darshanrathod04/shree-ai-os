package com.shreeai.os.platform.runtime.observability;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>RuntimeSpan</b>
 *
 * <p>An immutable, completed tracing span produced by {@link RuntimeTracer}.
 * Captures the span name, trace context, start/end timestamps, duration,
 * attributes, and a terminal status (OK / ERROR).</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class RuntimeSpan {

    /** Terminal status of a completed span. */
    public enum Status {
        OK, ERROR
    }

    private final String name;
    private final TraceContext context;
    private final Instant startedAt;
    private final Instant endedAt;
    private final Map<String, String> attributes;
    private final Status status;

    private RuntimeSpan(Builder builder) {
        this.name = builder.name;
        this.context = builder.context;
        this.startedAt = builder.startedAt;
        this.endedAt = builder.endedAt;
        this.attributes = Collections.unmodifiableMap(
                new LinkedHashMap<>(builder.attributes));
        this.status = builder.status;
    }

    /** @return the span name (never null) */
    public String name() {
        return name;
    }

    /** @return the trace context (never null) */
    public TraceContext context() {
        return context;
    }

    /** @return when the span started (never null) */
    public Instant startedAt() {
        return startedAt;
    }

    /** @return when the span ended (never null) */
    public Instant endedAt() {
        return endedAt;
    }

    /** @return the span duration in milliseconds (never negative) */
    public long durationMs() {
        return java.time.Duration.between(startedAt, endedAt).toMillis();
    }

    /** @return unmodifiable span attributes (never null) */
    public Map<String, String> attributes() {
        return attributes;
    }

    /** @return the terminal status (never null) */
    public Status status() {
        return status;
    }

    /** @return the trace ID (convenience accessor) */
    public String traceId() {
        return context.traceId();
    }

    /** @return the span ID (convenience accessor) */
    public String spanId() {
        return context.spanId();
    }

    /** @return the parent span ID, or null for a root span */
    public String parentSpanId() {
        return context.parentSpanId();
    }

    @Override
    public String toString() {
        return "RuntimeSpan{"
                + "name='" + name + '\''
                + ", traceId='" + context.traceId() + '\''
                + ", spanId='" + context.spanId() + '\''
                + ", durationMs=" + durationMs()
                + ", status=" + status
                + '}';
    }

    /** Creates a new builder for {@link RuntimeSpan}. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link RuntimeSpan}. */
    public static final class Builder {

        private String name;
        private TraceContext context;
        private Instant startedAt = Instant.now();
        private Instant endedAt = Instant.now();
        private Map<String, String> attributes = new LinkedHashMap<>();
        private Status status = Status.OK;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        public Builder context(TraceContext context) {
            this.context = Objects.requireNonNull(context, "context must not be null");
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            this.endedAt = Objects.requireNonNull(endedAt, "endedAt must not be null");
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes == null
                    ? new LinkedHashMap<>()
                    : new LinkedHashMap<>(attributes);
            return this;
        }

        public Builder status(Status status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
            return this;
        }

        public RuntimeSpan build() {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            if (context == null) {
                throw new IllegalArgumentException("context must not be null");
            }
            if (endedAt.isBefore(startedAt)) {
                throw new IllegalArgumentException("endedAt must not be before startedAt");
            }
            return new RuntimeSpan(this);
        }
    }
}
