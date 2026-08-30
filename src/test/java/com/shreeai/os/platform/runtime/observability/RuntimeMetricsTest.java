package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuntimeMetrics Tests")
class RuntimeMetricsTest {

    private RuntimeMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new RuntimeMetrics();
    }

    @Test
    @DisplayName("increment by one")
    void incrementByOne() {
        metrics.increment("executions");
        assertEquals(1, metrics.counterValue("executions"));
    }

    @Test
    @DisplayName("increment by amount")
    void incrementByAmount() {
        metrics.increment("executions", 5);
        assertEquals(5, metrics.counterValue("executions"));
    }

    @Test
    @DisplayName("counter starts at zero")
    void counterStartsAtZero() {
        assertEquals(0, metrics.counterValue("missing"));
    }

    @Test
    @DisplayName("set gauge")
    void setGauge() {
        metrics.setGauge("active_sessions", 42);
        assertEquals(42, metrics.gaugeValue("active_sessions"));
    }

    @Test
    @DisplayName("gauge starts at zero")
    void gaugeStartsAtZero() {
        assertEquals(0, metrics.gaugeValue("missing"));
    }

    @Test
    @DisplayName("record histogram observations")
    void recordHistogram() {
        metrics.record("dispatch_duration_seconds", 0.05);
        metrics.record("dispatch_duration_seconds", 0.2);
        metrics.record("dispatch_duration_seconds", 1.5);

        assertEquals(3, metrics.histogramCount("dispatch_duration_seconds"));
    }

    @Test
    @DisplayName("histogram count starts at zero")
    void histogramCountStartsAtZero() {
        assertEquals(0, metrics.histogramCount("missing"));
    }

    @Test
    @DisplayName("scrape includes counter with _total and TYPE")
    void scrapeIncludesCounter() {
        metrics.increment("executions", 3);

        String out = metrics.scrape();

        assertTrue(out.contains("# TYPE executions_total counter"));
        assertTrue(out.contains("executions_total 3"));
    }

    @Test
    @DisplayName("scrape includes gauge with TYPE")
    void scrapeIncludesGauge() {
        metrics.setGauge("active_sessions", 7);

        String out = metrics.scrape();

        assertTrue(out.contains("# TYPE active_sessions gauge"));
        assertTrue(out.contains("active_sessions 7"));
    }

    @Test
    @DisplayName("scrape includes histogram buckets, sum, and count")
    void scrapeIncludesHistogram() {
        metrics.record("dispatch_duration_seconds", 0.05);

        String out = metrics.scrape();

        assertTrue(out.contains("# TYPE dispatch_duration_seconds histogram"));
        assertTrue(out.contains("dispatch_duration_seconds_bucket{le=\"0.05\"} 1"));
        assertTrue(out.contains("dispatch_duration_seconds_bucket{le=\"+Inf\"} 1"));
        assertTrue(out.contains("dispatch_duration_seconds_sum 0.05"));
        assertTrue(out.contains("dispatch_duration_seconds_count 1"));
    }

    @Test
    @DisplayName("scrape with no metrics is empty")
    void scrapeEmpty() {
        String out = metrics.scrape();
        assertEquals("", out);
    }

    @Test
    @DisplayName("histogram buckets accumulate cumulative counts")
    void histogramBucketsCumulative() {
        metrics.record("latency", 0.02);
        metrics.record("latency", 0.5);

        String out = metrics.scrape();

        // Both values are <= 1.0 bucket
        assertTrue(out.contains("latency_bucket{le=\"1\"} 2"));
        // Only the 0.02 value is <= 0.01... wait it's 0.02 so it's <= 0.05 but not <= 0.01
        // 0.02 -> buckets 0.05,0.1,... ; 0.5 -> buckets 1.0, 2.5...
        // le="0.01" should be 0
        assertTrue(out.contains("latency_bucket{le=\"0.01\"} 0"));
        // le="0.05" should be 1 (only 0.02)
        assertTrue(out.contains("latency_bucket{le=\"0.05\"} 1"));
    }

    @Test
    @DisplayName("negative histogram observation throws")
    void negativeHistogramObservationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> metrics.record("latency", -1.0));
    }

    @Test
    @DisplayName("null counter name throws")
    void nullCounterNameThrows() {
        assertThrows(NullPointerException.class,
                () -> metrics.increment(null));
    }

    @Test
    @DisplayName("concurrent increments are thread-safe")
    void concurrentIncrementsAreThreadSafe() throws InterruptedException {
        int threads = 8;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < incrementsPerThread; i++) {
                        metrics.increment("concurrent_total");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals((long) threads * incrementsPerThread,
                metrics.counterValue("concurrent_total"));
    }
}
