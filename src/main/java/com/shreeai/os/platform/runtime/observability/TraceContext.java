package com.shreeai.os.platform.runtime.observability;

import java.util.Objects;
import java.util.UUID;

/**
 * <b>TraceContext</b>
 *
 * <p>Immutable context describing a single distributed-trace span: its trace
 * ID, span ID, parent span ID, and sampling flag. Follows the W3C Trace
 * Context shape ({@code trace-id} and {@code span-id} are 32- and 16-hex
 * character strings respectively) so contexts can be propagated across
 * process boundaries.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class TraceContext {

    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private final boolean sampled;

    private TraceContext(Builder builder) {
        this.traceId = builder.traceId;
        this.spanId = builder.spanId;
        this.parentSpanId = builder.parentSpanId;
        this.sampled = builder.sampled;
    }

    /**
     * @return the 32-hex-character trace ID (never null)
     */
    public String traceId() {
        return traceId;
    }

    /**
     * @return the 16-hex-character span ID (never null)
     */
    public String spanId() {
        return spanId;
    }

    /**
     * @return the parent span ID, or {@code null} for a root span
     */
    public String parentSpanId() {
        return parentSpanId;
    }

    /**
     * @return whether this trace is sampled (recorded)
     */
    public boolean sampled() {
        return sampled;
    }

    /** @return true when this is a root span (no parent) */
    public boolean isRoot() {
        return parentSpanId == null;
    }

    /**
     * Renders the W3C-style traceparent header value.
     *
     * @return {@code 00-<traceId>-<spanId>-<sampledFlag>}
     */
    public String toTraceParent() {
        return "00-" + traceId + "-" + spanId + "-" + (sampled ? "01" : "00");
    }

    @Override
    public String toString() {
        return "TraceContext{"
                + "traceId='" + traceId + '\''
                + ", spanId='" + spanId + '\''
                + ", parentSpanId='" + parentSpanId + '\''
                + ", sampled=" + sampled
                + '}';
    }

    /**
     * Creates a new random root trace context.
     *
     * @return a new root context
     */
    public static TraceContext newRoot() {
        return builder()
                .traceId(randomHex(16))
                .spanId(randomHex(8))
                .parentSpanId(null)
                .sampled(true)
                .build();
    }

    /**
     * Creates a child context under the given parent.
     *
     * @param parent the parent context (never null)
     * @return a new child context sharing the parent's trace ID
     */
    public static TraceContext childOf(TraceContext parent) {
        Objects.requireNonNull(parent, "parent must not be null");
        return builder()
                .traceId(parent.traceId())
                .spanId(randomHex(8))
                .parentSpanId(parent.spanId())
                .sampled(parent.sampled())
                .build();
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static String randomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new java.util.Random().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Fluent builder for {@link TraceContext}. */
    public static final class Builder {

        private String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        private String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        private String parentSpanId;
        private boolean sampled = true;

        private Builder() {
        }

        public Builder traceId(String traceId) {
            this.traceId = Objects.requireNonNull(traceId, "traceId must not be null");
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = Objects.requireNonNull(spanId, "spanId must not be null");
            return this;
        }

        public Builder parentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder sampled(boolean sampled) {
            this.sampled = sampled;
            return this;
        }

        public TraceContext build() {
            if (traceId.length() != 32) {
                throw new IllegalArgumentException("traceId must be 32 hex chars");
            }
            if (spanId.length() != 16) {
                throw new IllegalArgumentException("spanId must be 16 hex chars");
            }
            return new TraceContext(this);
        }
    }
}
