package platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionMetrics</b>
 *
 * <p>Represents immutable execution metrics.
 * This value object encapsulates performance metrics for execution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution performance data.</li>
 *   <li>Provides timing and resource usage metrics.</li>
 *   <li>Enables execution monitoring and analysis.</li>
 *   <li>Contains no metric calculations.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param startTime      the execution start time (must not be {@code null})
 * @param endTime        the execution end time (must not be {@code null})
 * @param durationMs     the execution duration in milliseconds
 * @param retryCount     the number of retries performed
 * @param resourceUsage  the resource usage metrics (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionMetrics {

    private final java.time.Instant startTime;
    private final java.time.Instant endTime;
    private final long durationMs;
    private final int retryCount;
    private final Map<String, Object> resourceUsage;

    /**
     * Constructs an {@code ExecutionMetrics} with the specified parameters.
     *
     * @param startTime      the execution start time (must not be {@code null})
     * @param endTime        the execution end time (must not be {@code null})
     * @param durationMs     the execution duration in milliseconds
     * @param retryCount     the number of retries performed
     * @param resourceUsage  the resource usage metrics (must not be {@code null})
     * @throws IllegalArgumentException if startTime, endTime, or resourceUsage is {@code null}
     */
    public ExecutionMetrics(
            java.time.Instant startTime,
            java.time.Instant endTime,
            long durationMs,
            int retryCount,
            Map<String, Object> resourceUsage) {
        if (startTime == null) {
            throw new IllegalArgumentException("ExecutionMetrics startTime must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("ExecutionMetrics endTime must not be null");
        }
        if (resourceUsage == null) {
            throw new IllegalArgumentException("ExecutionMetrics resourceUsage must not be null");
        }

        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.retryCount = retryCount;
        this.resourceUsage = Collections.unmodifiableMap(new HashMap<>(resourceUsage));
    }

    /**
     * Returns the execution start time.
     *
     * @return the start time
     */
    public java.time.Instant startTime() {
        return startTime;
    }

    /**
     * Returns the execution end time.
     *
     * @return the end time
     */
    public java.time.Instant endTime() {
        return endTime;
    }

    /**
     * Returns the execution duration in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public long durationMs() {
        return durationMs;
    }

    /**
     * Returns the number of retries performed.
     *
     * @return the retry count
     */
    public int retryCount() {
        return retryCount;
    }

    /**
     * Returns an unmodifiable view of the resource usage metrics.
     *
     * <p>The returned map is unmodifiable and reflects the resource usage at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of resource usage metrics
     */
    public Map<String, Object> resourceUsage() {
        return resourceUsage;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionMetrics} instances are equal if they have the same
     * timing, retry count, and resource usage.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionMetrics that = (ExecutionMetrics) obj;
        return durationMs == that.durationMs &&
                retryCount == that.retryCount &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(resourceUsage, that.resourceUsage);
    }

    /**
     * Returns a hash code value for this {@code ExecutionMetrics}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, durationMs, retryCount, resourceUsage);
    }

    /**
     * Returns a string representation of this {@code ExecutionMetrics}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionMetrics{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationMs=" + durationMs +
                ", retryCount=" + retryCount +
                ", resourceUsage=" + resourceUsage +
                '}';
    }
}