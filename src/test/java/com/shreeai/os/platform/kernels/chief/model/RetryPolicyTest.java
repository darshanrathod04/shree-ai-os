package com.shreeai.os.platform.kernels.chief.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RetryPolicy} governance model.
 */
class RetryPolicyTest {

    @Test
    void defaultsAreSane() {
        RetryPolicy policy = RetryPolicy.defaults();
        assertTrue(policy.isEnabled());
        assertEquals(2, policy.maxRetries());
        assertTrue(policy.retryOnFailure());
    }

    @Test
    void disabledWhenMaxRetriesZero() {
        assertFalse(new RetryPolicy(0, 0, false, false, 0.0).isEnabled());
    }

    @Test
    void exponentialBackoffComputesCorrectly() {
        RetryPolicy policy = new RetryPolicy(3, 100L, true, false, 0.5);
        assertEquals(100L, policy.backoffForAttempt(0));
        assertEquals(200L, policy.backoffForAttempt(1));
        assertEquals(400L, policy.backoffForAttempt(2));
    }

    @Test
    void zeroBackoffIsAlwaysZero() {
        RetryPolicy policy = new RetryPolicy(3, 0L, true, false, 0.5);
        assertEquals(0L, policy.backoffForAttempt(5));
    }

    @Test
    void rejectsNegativeMaxRetries() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(-1, 0, true, false, 0.5));
    }

    @Test
    void rejectsOutOfRangeThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(1, 0, true, false, 1.5));
    }
}
