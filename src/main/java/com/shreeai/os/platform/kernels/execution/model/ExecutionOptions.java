package com.shreeai.os.platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionOptions</b>
 *
 * <p>Represents immutable execution options.
 * This value object encapsulates configurable options for execution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution configuration.</li>
 *   <li>Defines execution constraints and preferences.</li>
 *   <li>Provides timeout and retry configuration.</li>
 *   <li>Contains no execution logic.</li>
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
 * @param timeoutMs          the execution timeout in milliseconds
 * @param maxRetries         the maximum number of retries
 * @param retryDelayMs       the delay between retries in milliseconds
 * @param allowPartial       whether partial execution is allowed
 * @param continueOnError    whether to continue on non-critical errors
 * @param options            additional options (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionOptions {

    private final long timeoutMs;
    private final int maxRetries;
    private final long retryDelayMs;
    private final boolean allowPartial;
    private final boolean continueOnError;
    private final Map<String, Object> options;

    /**
     * Constructs an {@code ExecutionOptions} with the specified parameters.
     *
     * @param timeoutMs       the execution timeout in milliseconds
     * @param maxRetries      the maximum number of retries
     * @param retryDelayMs    the delay between retries in milliseconds
     * @param allowPartial    whether partial execution is allowed
     * @param continueOnError whether to continue on non-critical errors
     * @param options         additional options (must not be {@code null})
     * @throws IllegalArgumentException if options is {@code null}
     */
    public ExecutionOptions(
            long timeoutMs,
            int maxRetries,
            long retryDelayMs,
            boolean allowPartial,
            boolean continueOnError,
            Map<String, Object> options) {
        if (options == null) {
            throw new IllegalArgumentException("ExecutionOptions options must not be null");
        }

        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.allowPartial = allowPartial;
        this.continueOnError = continueOnError;
        this.options = Collections.unmodifiableMap(new HashMap<>(options));
    }

    /**
     * Returns the execution timeout in milliseconds.
     *
     * @return the timeout in milliseconds
     */
    public long timeoutMs() {
        return timeoutMs;
    }

    /**
     * Returns the maximum number of retries.
     *
     * @return the maximum retries
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Returns the delay between retries in milliseconds.
     *
     * @return the retry delay in milliseconds
     */
    public long retryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Returns whether partial execution is allowed.
     *
     * @return {@code true} if partial execution is allowed
     */
    public boolean allowPartial() {
        return allowPartial;
    }

    /**
     * Returns whether to continue on non-critical errors.
     *
     * @return {@code true} if execution continues on non-critical errors
     */
    public boolean continueOnError() {
        return continueOnError;
    }

    /**
     * Returns an unmodifiable view of the additional options.
     *
     * <p>The returned map is unmodifiable and reflects the options at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of options
     */
    public Map<String, Object> options() {
        return options;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionOptions} instances are equal if they have the same
     * timeout, retry configuration, and options.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    /**
     * Returns a default {@code ExecutionOptions}.
     *
     * @return a default execution options instance
     */
    public static ExecutionOptions defaults() {
        return new ExecutionOptions(30000L, 3, 1000L, false, false, new HashMap<>());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionOptions that = (ExecutionOptions) obj;
        return timeoutMs == that.timeoutMs &&
                maxRetries == that.maxRetries &&
                retryDelayMs == that.retryDelayMs &&
                allowPartial == that.allowPartial &&
                continueOnError == that.continueOnError &&
                Objects.equals(options, that.options);
    }

    /**
     * Returns a hash code value for this {@code ExecutionOptions}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(timeoutMs, maxRetries, retryDelayMs, allowPartial, continueOnError, options);
    }

    /**
     * Returns a string representation of this {@code ExecutionOptions}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionOptions{" +
                "timeoutMs=" + timeoutMs +
                ", maxRetries=" + maxRetries +
                ", retryDelayMs=" + retryDelayMs +
                ", allowPartial=" + allowPartial +
                ", continueOnError=" + continueOnError +
                ", options=" + options +
                '}';
    }
}
