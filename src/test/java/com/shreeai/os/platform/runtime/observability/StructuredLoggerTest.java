package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StructuredLogger Tests")
class StructuredLoggerTest {

    private StructuredLogger logger;

    @BeforeEach
    void setUp() {
        logger = StructuredLogger.of(StructuredLoggerTest.class);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Logger can be created for a class")
    void ofClassReturnsNonNull() {
        StructuredLogger log = StructuredLogger.of(String.class);
        assertNotNull(log);
    }

    @Test
    @DisplayName("Logger can be created by name")
    void ofNameReturnsNonNull() {
        StructuredLogger log = StructuredLogger.of("test-logger");
        assertNotNull(log);
    }

    @Test
    @DisplayName("withCorrelationId sets MDC key")
    void withCorrelationIdSetsMdc() {
        logger.withCorrelationId("corr-123");
        assertEquals("corr-123", MDC.get("correlationId"));
    }

    @Test
    @DisplayName("withCorrelationId generates ID when null")
    void withNullCorrelationIdGeneratesId() {
        logger.withCorrelationId(null);
        String id = MDC.get("correlationId");
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    @DisplayName("withExecutionId sets MDC key")
    void withExecutionIdSetsMdc() {
        logger.withExecutionId("exec-456");
        assertEquals("exec-456", MDC.get("executionId"));
    }

    @Test
    @DisplayName("withCapability sets MDC key")
    void withCapabilitySetsMdc() {
        logger.withCapability("TASK_EXECUTION");
        assertEquals("TASK_EXECUTION", MDC.get("capability"));
    }

    @Test
    @DisplayName("withPhase sets MDC key")
    void withPhaseSetsMdc() {
        logger.withPhase("dispatch");
        assertEquals("dispatch", MDC.get("phase"));
    }

    @Test
    @DisplayName("Chaining multiple context setters works")
    void chainingContextSetters() {
        logger.withCorrelationId("abc")
               .withExecutionId("xyz")
               .withCapability("MEMORY_RECALL")
               .withPhase("execute");

        assertEquals("abc", MDC.get("correlationId"));
        assertEquals("xyz", MDC.get("executionId"));
        assertEquals("MEMORY_RECALL", MDC.get("capability"));
        assertEquals("execute", MDC.get("phase"));
    }

    @Test
    @DisplayName("clearContext removes all MDC keys")
    void clearContextRemovesAllKeys() {
        logger.withCorrelationId("abc")
               .withExecutionId("xyz")
               .withCapability("TASK_EXECUTION")
               .withPhase("start");

        logger.clearContext();

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("executionId"));
        assertNull(MDC.get("capability"));
        assertNull(MDC.get("phase"));
    }

    @Test
    @DisplayName("withExecutionId does not set MDC when null")
    void withNullExecutionIdDoesNotSetMdc() {
        logger.withExecutionId(null);
        assertNull(MDC.get("executionId"));
    }

    @Test
    @DisplayName("withCapability does not set MDC when null")
    void withNullCapabilityDoesNotSetMdc() {
        logger.withCapability(null);
        assertNull(MDC.get("capability"));
    }

    @Test
    @DisplayName("withPhase does not set MDC when null")
    void withNullPhaseDoesNotSetMdc() {
        logger.withPhase(null);
        assertNull(MDC.get("phase"));
    }

    @Test
    @DisplayName("info does not throw with KV pairs")
    void infoWithKvDoesNotThrow() {
        assertDoesNotThrow(() -> logger.info("test message", "key1", "val1", "key2", "val2"));
    }

    @Test
    @DisplayName("warn does not throw with KV pairs")
    void warnWithKvDoesNotThrow() {
        assertDoesNotThrow(() -> logger.warn("warn message", "status", "timeout"));
    }

    @Test
    @DisplayName("error does not throw with KV pairs")
    void errorWithKvDoesNotThrow() {
        assertDoesNotThrow(() -> logger.error("error message", "code", "500"));
    }

    @Test
    @DisplayName("executionEvent does not throw")
    void executionEventDoesNotThrow() {
        assertDoesNotThrow(() -> logger.executionEvent("start", "TASK_EXECUTION", -1));
    }

    @Test
    @DisplayName("executionEvent with duration does not throw")
    void executionEventWithDurationDoesNotThrow() {
        assertDoesNotThrow(() -> logger.executionEvent("complete", "MEMORY_RECALL", 150, "resultCount", "5"));
    }

    @Test
    @DisplayName("debug does not throw when debug disabled")
    void debugDoesNotThrow() {
        assertDoesNotThrow(() -> logger.debug("debug message", "detail", "value"));
    }
}
