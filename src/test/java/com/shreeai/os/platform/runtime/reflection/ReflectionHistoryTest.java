package com.shreeai.os.platform.runtime.reflection;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReflectionHistory}.
 */
class ReflectionHistoryTest {

    @Test
    void createsValidRecord() {
        Instant now = Instant.now();
        ReflectionHistory history = new ReflectionHistory(
                "tenant-1", "org-1", "exec-1", "req-1",
                "SUCCESS", 0.85, 75, List.of("Lesson 1"), null, false, now
        );

        assertEquals("tenant-1", history.tenantId());
        assertEquals("org-1", history.organizationId());
        assertEquals("exec-1", history.executionId());
        assertEquals(0.85, history.score());
        assertEquals(75, history.importanceScore());
        assertEquals(List.of("Lesson 1"), history.lessons());
        assertFalse(history.retryAdvised());
        assertEquals(now, history.evaluatedAt());
    }

    @Test
    void rejectsNullTenantId() {
        assertThrows(NullPointerException.class, () ->
                new ReflectionHistory(null, "org-1", "exec-1", "req-1",
                        "SUCCESS", 0.85, 75, List.of(), null, false, Instant.now()));
    }

    @Test
    void rejectsNullVerdict() {
        assertThrows(NullPointerException.class, () ->
                new ReflectionHistory("t", "o", "e", "r",
                        null, 0.85, 75, List.of(), null, false, Instant.now()));
    }

    @Test
    void clampsScoreToValidRange() {
        ReflectionHistory h1 = new ReflectionHistory("t", "o", "e", "r",
                "SUCCESS", 1.5, 75, List.of(), null, false, Instant.now());
        assertEquals(1.0, h1.score());

        ReflectionHistory h2 = new ReflectionHistory("t", "o", "e", "r",
                "SUCCESS", -0.5, 75, List.of(), null, false, Instant.now());
        assertEquals(0.0, h2.score());
    }

    @Test
    void clampsImportanceScoreToValidRange() {
        ReflectionHistory h1 = new ReflectionHistory("t", "o", "e", "r",
                "SUCCESS", 0.5, 150, List.of(), null, false, Instant.now());
        assertEquals(100, h1.importanceScore());

        ReflectionHistory h2 = new ReflectionHistory("t", "o", "e", "r",
                "SUCCESS", 0.5, -10, List.of(), null, false, Instant.now());
        assertEquals(0, h2.importanceScore());
    }

    @Test
    void lessonsAreImmutable() {
        ReflectionHistory history = new ReflectionHistory("t", "o", "e", "r",
                "SUCCESS", 0.5, 50, List.of("A", "B"), null, false, Instant.now());
        assertThrows(UnsupportedOperationException.class, () ->
                history.lessons().add("C"));
    }

    @Test
    void handlesNullRootCause() {
        ReflectionHistory history = new ReflectionHistory("t", "o", "e", "r",
                "FAILURE", 0.2, 90, List.of("Failed"), null, true, Instant.now());
        assertNull(history.rootCause());
        assertTrue(history.retryAdvised());
    }
}