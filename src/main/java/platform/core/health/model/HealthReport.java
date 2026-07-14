package platform.core.health.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <b>HealthReport</b>
 *
 * <p>Represents the complete result of a health check for a platform component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the complete result of a health check operation.</li>
 *   <li>Provides component identity, overall status, detailed indicators, metrics, and timestamp.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Indicators list is unmodifiable.</p>
 */
public final class HealthReport {

    private final HealthComponent component;
    private final HealthStatus status;
    private final List<HealthIndicator> indicators;
    private final HealthMetrics metrics;
    private final Instant timestamp;

    /**
     * Constructs a new {@code HealthReport} with the given parameters.
     *
     * @param component  the health component (must not be null)
     * @param status     the overall health status (must not be null)
     * @param indicators the list of health indicators (must not be null, defensive copy)
     * @param metrics    the health metrics (must not be null)
     * @param timestamp  the report timestamp (must not be null)
     * @throws IllegalArgumentException if any required parameter is null
     */
    public HealthReport(HealthComponent component,
                        HealthStatus status,
                        List<HealthIndicator> indicators,
                        HealthMetrics metrics,
                        Instant timestamp) {
        this.component = Objects.requireNonNull(component, "HealthComponent must not be null");
        this.status = Objects.requireNonNull(status, "HealthStatus must not be null");
        this.indicators = Collections.unmodifiableList(new java.util.ArrayList<>(
                Objects.requireNonNull(indicators, "Indicators list must not be null")));
        this.metrics = Objects.requireNonNull(metrics, "HealthMetrics must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
    }

    /**
     * Returns the health component.
     *
     * @return the health component
     */
    public HealthComponent component() {
        return component;
    }

    /**
     * Returns the overall health status.
     *
     * @return the health status
     */
    public HealthStatus status() {
        return status;
    }

    /**
     * Returns the list of health indicators.
     *
     * @return an unmodifiable list of health indicators
     */
    public List<HealthIndicator> indicators() {
        return indicators;
    }

    /**
     * Returns the health metrics.
     *
     * @return the health metrics
     */
    public HealthMetrics metrics() {
        return metrics;
    }

    /**
     * Returns the report timestamp.
     *
     * @return the report timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthReport that = (HealthReport) o;
        return component.equals(that.component)
                && status == that.status
                && indicators.equals(that.indicators)
                && metrics.equals(that.metrics)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, status, indicators, metrics, timestamp);
    }

    @Override
    public String toString() {
        return "HealthReport{"
                + "component=" + component
                + ", status=" + status
                + ", indicators=" + indicators
                + ", metrics=" + metrics
                + ", timestamp=" + timestamp
                + '}';
    }
}