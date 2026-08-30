package com.shreeai.os.platform.kernels.multiagent.model;

import java.util.Objects;

/**
 * <b>ParallelExecutionPolicy</b> — Immutable governance policy controlling
 * parallel agent orchestration.
 *
 * <p>Encapsulates the constraints applied when orchestrating multiple agents
 * concurrently, including concurrency limits, per-agent timeouts, and the
 * fail-fast behavior on the first failure.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param maxConcurrency   the maximum number of agents to run concurrently
 *                         (must be {@code >= 1}; 0 or negative means unlimited)
 * @param timeoutMs        the per-agent timeout in milliseconds
 *                         (must be {@code > 0})
 * @param failFast         whether to abort remaining agents on the first failure
 *
 * @since 1.0
 */
public final class ParallelExecutionPolicy {

    private final int maxConcurrency;
    private final long timeoutMs;
    private final boolean failFast;

    /**
     * Creates a new {@code ParallelExecutionPolicy}.
     *
     * @param maxConcurrency the maximum number of concurrent agents
     * @param timeoutMs      the per-agent timeout in milliseconds
     * @param failFast       whether to fail fast on the first failure
     */
    public ParallelExecutionPolicy(
            int maxConcurrency, long timeoutMs, boolean failFast) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0");
        }
        this.maxConcurrency = maxConcurrency;
        this.timeoutMs = timeoutMs;
        this.failFast = failFast;
    }

    /**
     * Returns a default policy: unlimited concurrency, 30s timeout, fail-fast.
     *
     * @return the default policy
     */
    public static ParallelExecutionPolicy defaults() {
        return new ParallelExecutionPolicy(0, 30_000L, true);
    }

    /**
     * Returns the maximum number of concurrent agents.
     *
     * @return 0 if unlimited, otherwise a positive cap
     */
    public int maxConcurrency() {
        return maxConcurrency;
    }

    /**
     * Returns whether concurrency is unlimited.
     *
     * @return {@code true} if maxConcurrency is 0 or negative
     */
    public boolean isUnlimited() {
        return maxConcurrency <= 0;
    }

    /**
     * Returns the per-agent timeout in milliseconds.
     *
     * @return the timeout in milliseconds
     */
    public long timeoutMs() {
        return timeoutMs;
    }

    /**
     * Returns whether to fail fast on the first failure.
     *
     * @return the fail-fast flag
     */
    public boolean failFast() {
        return failFast;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParallelExecutionPolicy that = (ParallelExecutionPolicy) obj;
        return maxConcurrency == that.maxConcurrency &&
                timeoutMs == that.timeoutMs &&
                failFast == that.failFast;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxConcurrency, timeoutMs, failFast);
    }

    @Override
    public String toString() {
        return "ParallelExecutionPolicy{maxConcurrency=" + maxConcurrency +
                ", timeoutMs=" + timeoutMs + ", failFast=" + failFast + '}';
    }
}
