package com.shreeai.os.platform.runtime.observability;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * <b>RuntimeMetrics</b>
 *
 * <p>Thread-safe, dependency-free metrics registry for the Runtime Kernel.
 * Supports counters, gauges, and histograms, and renders them in the
 * Prometheus text exposition format for scraping by a Prometheus server.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Collects execution, dispatch, approval, and reflection metrics.</li>
 *   <li>Provides a {@link #scrape()} that emits Prometheus text format (0.0.4).</li>
 *   <li>Remains dependency-free so the Runtime stays lightweight.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class RuntimeMetrics {

    private static final double[] DEFAULT_BUCKETS = {
            0.001, 0.005, 0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0, 30.0, 60.0
    };

    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();
    private final Map<String, Histogram> histograms = new ConcurrentHashMap<>();

    // ==========================================================
    // Counters
    // ==========================================================

    /**
     * Increments the named counter by one.
     *
     * @param name the metric name
     */
    public void increment(String name) {
        increment(name, 1);
    }

    /**
     * Increments the named counter by the given amount.
     *
     * @param name   the metric name
     * @param amount the amount to add (must be non-negative)
     */
    public void increment(String name, double amount) {
        Objects.requireNonNull(name, "name must not be null");
        counter(name).add((long) amount);
    }

    /**
     * Returns the current value of the named counter.
     *
     * @param name the metric name
     * @return the counter value (0 if not yet recorded)
     */
    public long counterValue(String name) {
        LongAdder adder = counters.get(name);
        return adder == null ? 0L : adder.sum();
    }

    // ==========================================================
    // Gauges
    // ==========================================================

    /**
     * Sets the named gauge to the given value.
     *
     * @param name  the metric name
     * @param value the gauge value
     */
    public void setGauge(String name, long value) {
        Objects.requireNonNull(name, "name must not be null");
        gauge(name).set(value);
    }

    /**
     * Returns the current value of the named gauge.
     *
     * @param name the metric name
     * @return the gauge value (0 if never set)
     */
    public long gaugeValue(String name) {
        AtomicLong gauge = gauges.get(name);
        return gauge == null ? 0L : gauge.get();
    }

    // ==========================================================
    // Histograms
    // ==========================================================

    /**
     * Records a single observation into the named histogram, aggregating
     * it into the pre-defined exponential buckets.
     *
     * @param name  the metric name
     * @param value the observed value (must be non-negative)
     */
    public void record(String name, double value) {
        Objects.requireNonNull(name, "name must not be null");
        histogram(name).observe(value);
    }

    /**
     * Returns the total number of observations recorded for the named
     * histogram.
     *
     * @param name the metric name
     * @return the observation count (0 if never recorded)
     */
    public long histogramCount(String name) {
        Histogram histogram = histograms.get(name);
        return histogram == null ? 0L : histogram.count();
    }

    // ==========================================================
    // Scraping
    // ==========================================================

    /**
     * Renders all metrics in the Prometheus text exposition format.
     *
     * <p>The output includes counters (as {@code _total}), gauges, and
     * histograms (with {@code _bucket}, {@code _sum}, and {@code _count}
     * series), suitable for scraping by a Prometheus server.</p>
     *
     * @return the Prometheus text representation (never null)
     */
    public String scrape() {
        StringBuilder sb = new StringBuilder();

        counters.forEach((name, adder) -> {
            appendType(sb, name + "_total", "counter");
            sb.append(name).append("_total ").append(adder.sum()).append('\n');
        });

        gauges.forEach((name, gauge) -> {
            appendType(sb, name, "gauge");
            sb.append(name).append(' ').append(gauge.get()).append('\n');
        });

        histograms.forEach((name, histogram) -> {
            appendType(sb, name, "histogram");
            double[] buckets = histogram.buckets();
            long[] bucketCounts = histogram.bucketCounts();
            for (int i = 0; i < buckets.length; i++) {
                // bucketCounts[i] already holds the cumulative count of
                // observations that fall within this bucket's upper bound.
                sb.append(name).append("_bucket{le=\"")
                        .append(format(buckets[i]))
                        .append("\"} ").append(bucketCounts[i]).append('\n');
            }
            sb.append(name).append("_bucket{le=\"+Inf\"} ")
                    .append(histogram.count()).append('\n');
            sb.append(name).append("_sum ").append(histogram.sum()).append('\n');
            sb.append(name).append("_count ").append(histogram.count()).append('\n');
        });

        return sb.toString();
    }

    private void appendType(StringBuilder sb, String name, String type) {
        sb.append("# TYPE ").append(name).append(' ').append(type).append('\n');
    }

    private static String format(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return java.math.BigDecimal.valueOf(value)
                .stripTrailingZeros()
                .toPlainString();
    }

    // ==========================================================
    // Internal
    // ==========================================================

    private LongAdder counter(String name) {
        return counters.computeIfAbsent(name, k -> new LongAdder());
    }

    private AtomicLong gauge(String name) {
        return gauges.computeIfAbsent(name, k -> new AtomicLong());
    }

    private Histogram histogram(String name) {
        return histograms.computeIfAbsent(name, k -> new Histogram(DEFAULT_BUCKETS));
    }

    /** Aggregates observations into fixed upper-bound buckets. */
    private static final class Histogram {

        private final double[] buckets;
        private final long[] bucketCounts;
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();

        private Histogram(double[] buckets) {
            this.buckets = buckets.clone();
            this.bucketCounts = new long[buckets.length];
        }

        private synchronized void observe(double value) {
            if (value < 0) {
                throw new IllegalArgumentException("observation must be non-negative");
            }
            count.increment();
            sum.add(value);
            for (int i = 0; i < buckets.length; i++) {
                if (value <= buckets[i]) {
                    bucketCounts[i]++;
                }
            }
        }

        private long count() {
            return count.sum();
        }

        private double sum() {
            return sum.sum();
        }

        private double[] buckets() {
            return buckets;
        }

        private long[] bucketCounts() {
            return bucketCounts;
        }
    }
}
