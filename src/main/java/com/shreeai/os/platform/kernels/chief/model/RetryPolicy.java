package com.shreeai.os.platform.kernels.chief.model;

import java.util.Objects;

/**
 * <b>RetryPolicy</b> — Immutable governance policy governing how the Chief
 * Kernel handles retries during autonomous orchestration.
 *
 * <p>Encapsulates the retry strategy applied when delegated work fails or
 * returns a partial result: the maximum number of retry attempts, the
 * exponential backoff base, the set of verds to retry on, and the confidence
 * threshold above which a failure is escalated to human approval instead.</p>
 *
 * <p><b>Ownership:</b> Chief Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param maxRetries          the maximum number of retry attempts (must be {@code >= 0})
 * @param backoffMs           the base backoff in milliseconds between attempts (must be {@code >= 0})
 * @param retryOnFailure      whether to retry on agent failure
 * @param retryOnPartial      whether to retry on partial results
 * @param escalationThreshold the confidence (0..1) below which a failure is escalated
 *
 * @since 1.0
 */
public final class RetryPolicy {

    private final int maxRetries;
    private final long backoffMs;
    private final boolean retryOnFailure;
    private final boolean retryOnPartial;
    private final double escalationThreshold;

    /**
     * Creates a new {@code RetryPolicy}.
     *
     * @param maxRetries          the maximum number of retry attempts
     * @param backoffMs           the base backoff in milliseconds between attempts
     * @param retryOnFailure      whether to retry on agent failure
     * @param retryOnPartial      whether to retry on partial results
     * @param escalationThreshold the confidence threshold for escalation
     */
    public RetryPolicy(
            int maxRetries,
            long backoffMs,
            boolean retryOnFailure,
            boolean retryOnPartial,
            double escalationThreshold) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (backoffMs < 0) {
            throw new IllegalArgumentException("backoffMs must be >= 0");
        }
        if (escalationThreshold < 0.0 || escalationThreshold > 1.0) {
            throw new IllegalArgumentException("escalationThreshold must be in [0,1]");
        }
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
        this.retryOnFailure = retryOnFailure;
        this.retryOnPartial = retryOnPartial;
        this.escalationThreshold = escalationThreshold;
    }

    /**
     * Returns a default retry policy: 2 retries, 100ms backoff,
     * retry on failure, no retry on partial, escalation threshold 0.5.
     *
     * @return the default policy
     */
    public static RetryPolicy defaults() {
        return new RetryPolicy(2, 100L, true, false, 0.5);
    }

    /**
     * Returns whether retries are effectively enabled.
     *
     * @return {@code true} if maxRetries is greater than 0
     */
    public boolean isEnabled() {
        return maxRetries > 0;
    }

    public int maxRetries() { return maxRetries; }
    public long backoffMs() { return backoffMs; }
    public boolean retryOnFailure() { return retryOnFailure; }
    public boolean retryOnPartial() { return retryOnPartial; }
    public double escalationThreshold() { return escalationThreshold; }

    /**
     * Computes the backoff duration for a given attempt index using
     * exponential backoff from the base backoff.
     *
     * @param attempt the zero-based attempt index
     * @return the backoff duration in milliseconds
     */
    public long backoffForAttempt(int attempt) {
        if (backoffMs == 0) {
            return 0;
        }
        int exponent = Math.max(0, attempt);
        return backoffMs * (1L << exponent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RetryPolicy that = (RetryPolicy) obj;
        return maxRetries == that.maxRetries &&
                backoffMs == that.backoffMs &&
                retryOnFailure == that.retryOnFailure &&
                retryOnPartial == that.retryOnPartial &&
                Double.compare(that.escalationThreshold, escalationThreshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxRetries, backoffMs, retryOnFailure, retryOnPartial, escalationThreshold);
    }

    @Override
    public String toString() {
        return "RetryPolicy{maxRetries=" + maxRetries +
                ", backoffMs=" + backoffMs +
                ", retryOnFailure=" + retryOnFailure +
                ", retryOnPartial=" + retryOnPartial +
                ", escalationThreshold=" + escalationThreshold + '}';
    }
}
