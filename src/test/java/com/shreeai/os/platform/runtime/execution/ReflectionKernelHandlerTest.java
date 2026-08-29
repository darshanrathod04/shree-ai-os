package com.shreeai.os.platform.runtime.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReflectionKernelHandler Tests")
class ReflectionKernelHandlerTest {

    private ReflectionKernelHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReflectionKernelHandler();
    }

    @Test
    @DisplayName("Handle successful execution returns SUCCESS with reflection verdict")
    void handleSuccessfulExecution() {
        Map<String, Object> context = Map.of(
                "requestId", "test-123",
                "executionSuccess", true,
                "confidence", 0.8,
                "planStepCount", 3,
                "responseSummary", "All steps completed successfully");

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "Process the request",
                context);

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.isSuccess());
        assertNotNull(result.output());
        assertTrue(result.output().contains("Verdict"));
        assertTrue(result.metadata().containsKey("reflectionVerdict"));
        assertTrue(result.metadata().containsKey("reflectionScore"));
        assertTrue(result.metadata().containsKey("reflectionLessons"));
                assertEquals(0.94, result.confidence(), 0.1);
    }

    @Test
    @DisplayName("Handle failed execution returns reflection with appropriate lessons")
    void handleFailedExecutionReturnsLessons() {
        Map<String, Object> context = Map.of(
                "requestId", "test-failed",
                "executionSuccess", false,
                "confidence", 0.3,
                "planStepCount", 0,
                "responseSummary", "");

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "Process something",
                context);

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertEquals(0.0, result.confidence(), 0.1); // LOW confidence = score < 0.4
        assertTrue(result.metadata().containsKey("reflectionLessons"));
    }

    @Test
    @DisplayName("Metadata contains all reflection fields")
    void metadataContainsAllReflectionFields() {
        Map<String, Object> context = Map.of(
                "requestId", "meta-test",
                "executionSuccess", true,
                "confidence", 0.9,
                "planStepCount", 5,
                "responseSummary", "Good result");

        RichExecutionResult result = handler.handle(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "input",
                context);

        assertTrue(result.metadata().containsKey("reflectionVerdict"));
        assertTrue(result.metadata().containsKey("reflectionScore"));
        assertTrue(result.metadata().containsKey("reflectionLessons"));
        assertTrue(result.metadata().containsKey("reflectionSummary"));
        assertTrue(result.metadata().containsKey("retryAdvised"));
        assertTrue(result.metadata().containsKey("memoryWorthy"));
        assertTrue(result.metadata().containsKey("evaluatedAt"));
    }

    @Test
    @DisplayName("Handle with defaults when context is minimal")
    void handleWithMinimalContext() {
        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "test input",
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.metadata().containsKey("reflectionVerdict"));
    }

    @Test
    @DisplayName("Handle null input uses context values")
    void handleWithNullInput() {
        Map<String, Object> context = Map.of(
                "requestId", "test-null",
                "requestText", "context input",
                "executionSuccess", true,
                "confidence", 0.7);

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                null,
                context);

        assertEquals(ExecutionStatus.SUCCESS, result.status());
    }
}
