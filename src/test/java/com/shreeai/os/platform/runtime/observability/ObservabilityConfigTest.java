package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservabilityConfig Tests")
class ObservabilityConfigTest {

    @BeforeEach
    void setUp() {
        System.clearProperty("shree.observability.logging");
        System.clearProperty("shree.observability.metrics");
        System.clearProperty("shree.observability.tracing");
        System.clearProperty("shree.observability.traceSampleRate");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("shree.observability.logging");
        System.clearProperty("shree.observability.metrics");
        System.clearProperty("shree.observability.tracing");
        System.clearProperty("shree.observability.traceSampleRate");
    }

    @Test
    @DisplayName("defaults enable logging and metrics, disable tracing")
    void defaults() {
        ObservabilityConfig config = ObservabilityConfig.builder().build();

        assertTrue(config.structuredLoggingEnabled());
        assertTrue(config.metricsEnabled());
        assertFalse(config.tracingEnabled());
        assertEquals(1.0, config.traceSampleRate());
        assertEquals(ObservabilityConfig.DEFAULT_METRIC_PREFIX, config.metricPrefix());
        assertEquals(512, config.maxStoredSpans());
        assertNull(config.traceExporterEndpoint());
    }

    @Test
    @DisplayName("toggling each option is preserved")
    void togglesPreserved() {
        ObservabilityConfig config = ObservabilityConfig.builder()
                .structuredLoggingEnabled(false)
                .metricsEnabled(false)
                .tracingEnabled(true)
                .traceSampleRate(0.5)
                .metricPrefix("custom_")
                .traceExporterEndpoint("http://localhost:4318")
                .maxStoredSpans(100)
                .build();

        assertFalse(config.structuredLoggingEnabled());
        assertFalse(config.metricsEnabled());
        assertTrue(config.tracingEnabled());
        assertEquals(0.5, config.traceSampleRate());
        assertEquals("custom_", config.metricPrefix());
        assertEquals("http://localhost:4318", config.traceExporterEndpoint());
        assertEquals(100, config.maxStoredSpans());
    }

    @Test
    @DisplayName("invalid sample rate throws")
    void invalidSampleRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ObservabilityConfig.builder().traceSampleRate(1.5).build());
        assertThrows(IllegalArgumentException.class,
                () -> ObservabilityConfig.builder().traceSampleRate(-0.1).build());
    }

    @Test
    @DisplayName("invalid max stored spans throws")
    void invalidMaxStoredSpansThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ObservabilityConfig.builder().maxStoredSpans(0).build());
    }

    @Test
    @DisplayName("system property overrides defaults for sampling flag")
    void systemPropertyOverridesToggles() {
        System.setProperty("shree.observability.tracing", "true");
        System.setProperty("shree.observability.logging", "false");

        ObservabilityConfig config = ObservabilityConfig.fromEnvironment();

        assertTrue(config.tracingEnabled());
        assertFalse(config.structuredLoggingEnabled());
    }

    @Test
    @DisplayName("invalid numeric system property falls back to default")
    void invalidNumericPropertyFallsBack() {
        System.setProperty("shree.observability.traceSampleRate", "not-a-number");

        ObservabilityConfig config = ObservabilityConfig.fromEnvironment();

        assertEquals(1.0, config.traceSampleRate());
    }

    @Test
    @DisplayName("fromEnvironment resolves system property sample rate")
    void fromEnvironmentResolvesSampleRate() {
        System.setProperty("shree.observability.traceSampleRate", "0.25");

        ObservabilityConfig config = ObservabilityConfig.fromEnvironment();

        assertEquals(0.25, config.traceSampleRate());
    }

    @Test
    @DisplayName("null metric prefix throws")
    void nullMetricPrefixThrows() {
        assertThrows(NullPointerException.class,
                () -> ObservabilityConfig.builder().metricPrefix(null).build());
    }
}
