package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuntimeObservability Tests")
class RuntimeObservabilityTest {

    @Test
    @DisplayName("wires all components with defaults")
    void wiresAllComponents() {
        RuntimeObservability observability = new RuntimeObservability(
                ObservabilityConfig.builder().build());

        assertNotNull(observability.logger());
        assertNotNull(observability.metrics());
        assertNotNull(observability.tracer());
        assertTrue(observability.metricsEnabled());
        assertTrue(observability.loggingEnabled());
        assertFalse(observability.tracingEnabled());
    }

    @Test
    @DisplayName("recordExecution updates metrics when enabled")
    void recordExecutionUpdatesMetrics() {
        RuntimeObservability observability = new RuntimeObservability(
                ObservabilityConfig.builder().build());

        observability.recordExecution("TASK_EXECUTION", 250, true);

        assertEquals(1, observability.metrics().counterValue("execution_total"));
        assertEquals(1, observability.metrics().counterValue("execution_status_total"));
        assertEquals(1, observability.metrics().histogramCount("execution_duration_seconds"));
    }

    @Test
    @DisplayName("recordExecution creates a span when tracing enabled")
    void recordExecutionCreatesSpanWhenTracingEnabled() {
        RuntimeObservability observability = new RuntimeObservability(
                ObservabilityConfig.builder().tracingEnabled(true).build());

        observability.recordExecution("MEMORY_RECALL", 10, true);

        assertEquals(1, observability.tracer().completedSpans().size());
        assertEquals("execute:MEMORY_RECALL",
                observability.tracer().completedSpans().get(0).name());
    }

    @Test
    @DisplayName("recordExecution does not trace when tracing disabled")
    void recordExecutionDoesNotTraceWhenDisabled() {
        RuntimeObservability observability = new RuntimeObservability(
                ObservabilityConfig.builder().tracingEnabled(false).build());

        observability.recordExecution("TASK_EXECUTION", 10, true);

        assertEquals(0, observability.tracer().completedSpans().size());
    }

    @Test
    @DisplayName("recordExecution does not record metrics when disabled")
    void recordExecutionDoesNotRecordMetricsWhenDisabled() {
        RuntimeObservability observability = new RuntimeObservability(
                ObservabilityConfig.builder().metricsEnabled(false).build());

        observability.recordExecution("TASK_EXECUTION", 10, true);

        assertEquals(0, observability.metrics().counterValue("execution_total"));
    }

    @Test
    @DisplayName("null config throws")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
                () -> new RuntimeObservability(null));
    }
}
