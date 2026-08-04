package com.shreeai.os.platform.core.health.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>HealthMetrics</b>
 *
 * <p>Represents health metrics for a platform component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides health metrics such as availability, response time, and uptime.</li>
 *   <li>Enables quantitative health assessment across Platform components.</li>
 *   <li>No calculations — pure model.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Values map may be empty but never null.</p>
 */
public final class HealthMetrics {

    private final double availability;
    private final double responseTime;
    private final double uptime;
    private final Map<String, Object> values;

    /**
     * Constructs a new {@code HealthMetrics} with the given parameters.
     *
     * @param availability the availability ratio (0.0 to 1.0)
     * @param responseTime the response time in milliseconds
     * @param uptime       the uptime in seconds
     * @param values       the additional metric values (must not be null)
     * @throws IllegalArgumentException if {@code values} is null
     */
    public HealthMetrics(double availability, double responseTime, double uptime, Map<String, Object> values) {
        this.availability = availability;
        this.responseTime = responseTime;
        this.uptime = uptime;
        this.values = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(values, "Values map must not be null")));
    }

    /**
     * Returns the availability ratio.
     *
     * @return the availability ratio (0.0 to 1.0)
     */
    public double availability() {
        return availability;
    }

    /**
     * Returns the response time in milliseconds.
     *
     * @return the response time in milliseconds
     */
    public double responseTime() {
        return responseTime;
    }

    /**
     * Returns the uptime in seconds.
     *
     * @return the uptime in seconds
     */
    public double uptime() {
        return uptime;
    }

    /**
     * Returns the additional metric values.
     *
     * @return an unmodifiable map of metric values (empty if no values)
     */
    public Map<String, Object> values() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthMetrics that = (HealthMetrics) o;
        return Double.compare(that.availability, availability) == 0
                && Double.compare(that.responseTime, responseTime) == 0
                && Double.compare(that.uptime, uptime) == 0
                && values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(availability, responseTime, uptime, values);
    }

    @Override
    public String toString() {
        return "HealthMetrics{"
                + "availability=" + availability
                + ", responseTime=" + responseTime
                + ", uptime=" + uptime
                + ", values=" + values
                + '}';
    }
}