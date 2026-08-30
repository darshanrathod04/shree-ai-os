package com.shreeai.os.platform.kernels.multiagent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ParallelExecutionPolicy} governance model.
 */
class ParallelExecutionPolicyTest {

    @Test
    void unlimitedWhenConcurrencyZero() {
        ParallelExecutionPolicy policy = new ParallelExecutionPolicy(0, 1000, true);
        assertTrue(policy.isUnlimited());
    }

    @Test
    void boundedWhenConcurrencyPositive() {
        ParallelExecutionPolicy policy = new ParallelExecutionPolicy(4, 1000, true);
        assertFalse(policy.isUnlimited());
        assertEquals(4, policy.maxConcurrency());
        assertTrue(policy.failFast());
    }

    @Test
    void defaultsAreUnlimitedAndFailFast() {
        ParallelExecutionPolicy policy = ParallelExecutionPolicy.defaults();
        assertTrue(policy.isUnlimited());
        assertTrue(policy.failFast());
        assertEquals(30_000L, policy.timeoutMs());
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> new ParallelExecutionPolicy(0, 0, true));
    }
}
