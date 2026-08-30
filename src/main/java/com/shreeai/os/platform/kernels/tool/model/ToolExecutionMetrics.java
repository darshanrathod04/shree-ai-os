package com.shreeai.os.platform.kernels.tool.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ToolExecutionMetrics</b> — Immutable execution metrics for a tool operation.
 *
 * <p><b>Ownership:</b> Tool Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ToolExecutionMetrics {

    private final long durationMs;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Map<String, Object> properties;

    public ToolExecutionMetrics(
            long durationMs,
            Instant startedAt,
            Instant completedAt,
            Map<String, Object> properties) {
        if (startedAt == null) throw new IllegalArgumentException("startedAt must not be null");
        if (completedAt == null) throw new IllegalArgumentException("completedAt must not be null");
        if (properties == null) throw new IllegalArgumentException("properties must not be null");

        this.durationMs = durationMs;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.properties = Map.copyOf(properties);
    }

    public long durationMs() { return durationMs; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Map<String, Object> properties() { return properties; }

    public static ToolExecutionMetrics empty() {
        Instant now = Instant.now();
        return new ToolExecutionMetrics(0L, now, now, Collections.emptyMap());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ToolExecutionMetrics that = (ToolExecutionMetrics) obj;
        return durationMs == that.durationMs &&
                startedAt.equals(that.startedAt) &&
                completedAt.equals(that.completedAt) &&
                properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationMs, startedAt, completedAt, properties);
    }

    @Override
    public String toString() {
        return "ToolExecutionMetrics{durationMs=" + durationMs +
                ", startedAt=" + startedAt + ", completedAt=" + completedAt + '}';
    }
}
