package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuntimeTracer Tests")
class RuntimeTracerTest {

    private RuntimeTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = new RuntimeTracer();
    }

    @AfterEach
    void tearDown() {
        tracer.reset();
    }

    @Test
    @DisplayName("startSpan creates an active span with root context")
    void startSpanCreatesRootSpan() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("dispatch");
        assertNotNull(span);
        assertNotNull(span.context());
        assertTrue(span.context().isRoot());
        assertEquals("dispatch", span.name());
        span.end();
    }

    @Test
    @DisplayName("ending a span records it as completed")
    void endRecordsCompletedSpan() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("dispatch");
        span.end();

        List<RuntimeSpan> spans = tracer.completedSpans();
        assertEquals(1, spans.size());
        assertEquals("dispatch", spans.get(0).name());
        assertEquals(RuntimeSpan.Status.OK, spans.get(0).status());
    }

    @Test
    @DisplayName("nested spans share trace ID and create parent-child links")
    void nestedSpansShareTraceId() {
        RuntimeTracer.ActiveSpan parent = tracer.startSpan("root");
        RuntimeTracer.ActiveSpan child = tracer.startSpan("child");
        child.end();
        parent.end();

        assertEquals(parent.context().traceId(), child.context().traceId());
        assertEquals(parent.context().spanId(), child.context().parentSpanId());
        assertFalse(child.context().isRoot());
    }

    @Test
    @DisplayName("currentContext returns the active span context")
    void currentContextTracksActiveSpan() {
        assertNull(tracer.currentContext());
        RuntimeTracer.ActiveSpan span = tracer.startSpan("dispatch");
        assertNotNull(tracer.currentContext());
        assertEquals(span.context().spanId(), tracer.currentContext().spanId());
        span.end();
    }

    @Test
    @DisplayName("currentContext clears after span ends")
    void currentContextClearsAfterEnd() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("dispatch");
        assertNotNull(tracer.currentContext());
        span.end();
        assertNull(tracer.currentContext());
    }

    @Test
    @DisplayName("currentTraceId returns null when no span active")
    void currentTraceIdNullWhenNoSpan() {
        assertNull(tracer.currentTraceId());
    }

    @Test
    @DisplayName("currentTraceId returns the active trace ID")
    void currentTraceIdReturnsActive() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("dispatch");
        assertEquals(span.context().traceId(), tracer.currentTraceId());
        span.end();
    }

    @Test
    @DisplayName("startChild creates span under explicit parent")
    void startChildUnderExplicitParent() {
        TraceContext parentContext = TraceContext.newRoot();
        RuntimeTracer.ActiveSpan child = tracer.startChild("child", parentContext);

        assertEquals(parentContext.traceId(), child.context().traceId());
        assertEquals(parentContext.spanId(), child.context().parentSpanId());
        child.end();
    }

    @Test
    @DisplayName("error status is preserved on completed span")
    void errorStatusPreserved() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("failed-op");
        span.end(RuntimeSpan.Status.ERROR);

        List<RuntimeSpan> spans = tracer.completedSpans();
        assertEquals(RuntimeSpan.Status.ERROR, spans.get(0).status());
    }

    @Test
    @DisplayName("attributes are recorded on the span")
    void attributesRecorded() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan(
                "dispatch", Map.of("capability", "TASK_EXECUTION"));
        span.attribute("retryCount", "2");
        span.end();

        RuntimeSpan completed = tracer.completedSpans().get(0);
        assertEquals("TASK_EXECUTION", completed.attributes().get("capability"));
        assertEquals("2", completed.attributes().get("retryCount"));
    }

    @Test
    @DisplayName("span duration is non-negative")
    void spanDurationNonNegative() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("op");
        span.end();
        assertTrue(tracer.completedSpans().get(0).durationMs() >= 0);
    }

    @Test
    @DisplayName("trace context has W3C-compatible traceparent")
    void traceContextTraceParentFormat() {
        TraceContext context = TraceContext.newRoot();
        String traceParent = context.toTraceParent();
        assertTrue(traceParent.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-01"));
    }

    @Test
    @DisplayName("exporter receives completed spans")
    void exporterReceivesSpans() {
        List<RuntimeSpan> exported = new ArrayList<>();
        RuntimeTracer exportingTracer = new RuntimeTracer(exported::add, 10);

        RuntimeTracer.ActiveSpan span = exportingTracer.startSpan("op");
        span.end();

        assertEquals(1, exported.size());
        assertEquals("op", exported.get(0).name());
    }

    @Test
    @DisplayName("double-ending a span does not record twice")
    void doubleEndDoesNotRecordTwice() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("op");
        span.end();
        span.end();
        assertEquals(1, tracer.completedSpans().size());
    }

    @Test
    @DisplayName("reset clears stored spans")
    void resetClearsStoredSpans() {
        RuntimeTracer.ActiveSpan span = tracer.startSpan("op");
        span.end();
        assertEquals(1, tracer.completedSpans().size());
        tracer.reset();
        assertEquals(0, tracer.completedSpans().size());
    }

    @Test
    @DisplayName("bounded storage caps the number of stored spans")
    void boundedStorageCapsStoredSpans() {
        RuntimeTracer bounded = new RuntimeTracer(null, 3);
        for (int i = 0; i < 10; i++) {
            RuntimeTracer.ActiveSpan span = bounded.startSpan("op-" + i);
            span.end();
        }
        assertEquals(3, bounded.completedSpans().size());
    }

    @Test
    @DisplayName("null span name throws")
    void nullSpanNameThrows() {
        assertThrows(NullPointerException.class, () -> tracer.startSpan(null));
    }

    @Test
    @DisplayName("empty trace context trace ID validation")
    void invalidTraceContextThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> TraceContext.builder().traceId("short").build());
    }
}
