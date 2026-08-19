package com.shreeai.os.platform.execution;

import com.shreeai.os.platform.legacy.context.ConversationSession;
import com.shreeai.os.platform.legacy.cognition.CognitiveDecision;
import com.shreeai.os.platform.legacy.execution.*;
import com.shreeai.os.platform.legacy.production.ResolvedContext;
import org.junit.jupiter.api.Test;
import com.shreeai.os.platform.validation.ValidationResult;
import com.shreeai.os.platform.validation.ValidationStatus;
import com.shreeai.os.platform.validation.ValidationStrategy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Execution Contract (Sprint 6.1).
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>ExecutionRequest - immutability, builder, null safety, equals/hashCode</li>
 *   <li>ExecutionResult - immutability, builder, null safety, equals/hashCode</li>
 *   <li>ExecutionMetadata - immutability, builder, null safety, equals/hashCode</li>
 *   <li>ExecutionContext - immutability, builder, null safety, equals/hashCode</li>
 *   <li>ExecutionStatus enum - all values</li>
 *   <li>ExecutionType enum - all values</li>
 *   <li>Serialization friendliness</li>
 * </ul>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
class ExecutionContractTest {

    // =====================================================
    // EXECUTION STATUS ENUM TESTS
    // =====================================================

    @Test
    void testExecutionStatus_AllValues() {
        ExecutionStatus[] values = ExecutionStatus.values();
        assertEquals(7, values.length);
        assertTrue(contains(values, ExecutionStatus.SUCCESS));
        assertTrue(contains(values, ExecutionStatus.FAILED));
        assertTrue(contains(values, ExecutionStatus.CANCELLED));
        assertTrue(contains(values, ExecutionStatus.TIMEOUT));
        assertTrue(contains(values, ExecutionStatus.RETRY));
        assertTrue(contains(values, ExecutionStatus.PARTIAL_SUCCESS));
        assertTrue(contains(values, ExecutionStatus.UNKNOWN));
    }

    @Test
    void testExecutionStatus_ValueOf() {
        assertEquals(ExecutionStatus.SUCCESS, ExecutionStatus.valueOf("SUCCESS"));
        assertEquals(ExecutionStatus.FAILED, ExecutionStatus.valueOf("FAILED"));
    }

    // =====================================================
    // EXECUTION TYPE ENUM TESTS
    // =====================================================

    @Test
    void testExecutionType_AllValues() {
        ExecutionType[] values = ExecutionType.values();
        assertEquals(5, values.length);
        assertTrue(contains(values, ExecutionType.SYNC));
        assertTrue(contains(values, ExecutionType.ASYNC));
        assertTrue(contains(values, ExecutionType.BACKGROUND));
        assertTrue(contains(values, ExecutionType.STREAMING));
        assertTrue(contains(values, ExecutionType.UNKNOWN));
    }

    @Test
    void testExecutionType_ValueOf() {
        assertEquals(ExecutionType.SYNC, ExecutionType.valueOf("SYNC"));
        assertEquals(ExecutionType.ASYNC, ExecutionType.valueOf("ASYNC"));
    }

    // =====================================================
    // EXECUTION METADATA TESTS
    // =====================================================

    @Test
    void testExecutionMetadata_Builder_DefaultValues() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();

        assertNotNull(metadata.getTraceId());
        assertNotNull(metadata.getExecutionId());
        assertNotNull(metadata.getTimestamp());
        assertNull(metadata.getExecutionSource());
        assertNull(metadata.getSessionId());
        assertTrue(metadata.getCustomValues().isEmpty());
    }

    @Test
    void testExecutionMetadata_Builder_AllFields() {
        Instant now = Instant.now();
        Map<String, Object> customValues = new HashMap<>();
        customValues.put("key1", "value1");
        customValues.put("key2", 123);

        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("AgentBrain")
                .traceId("trace-123")
                .sessionId("session-456")
                .executionId("exec-789")
                .addCustomValue("key1", "value1")
                .addCustomValue("key2", 123)
                .timestamp(now)
                .build();

        assertEquals("AgentBrain", metadata.getExecutionSource());
        assertEquals("trace-123", metadata.getTraceId());
        assertEquals("session-456", metadata.getSessionId());
        assertEquals("exec-789", metadata.getExecutionId());
        assertEquals(now, metadata.getTimestamp());
        assertEquals(2, metadata.getCustomValues().size());
        assertEquals("value1", metadata.getCustomValue("key1"));
        assertEquals(123, metadata.getCustomValue("key2"));
        assertTrue(metadata.hasCustomValue("key1"));
        assertFalse(metadata.hasCustomValue("nonexistent"));
    }

    @Test
    void testExecutionMetadata_Immutability() {
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .build();

        // Verify collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            metadata.getCustomValues().put("key", "value");
        });
    }

    @Test
    void testExecutionMetadata_EqualsAndHashCode() {
        Instant now = Instant.now();
        ExecutionMetadata metadata1 = ExecutionMetadata.builder()
                .executionSource("Test")
                .traceId("trace-1")
                .sessionId("session-1")
                .executionId("exec-1")
                .timestamp(now)
                .build();

        ExecutionMetadata metadata2 = ExecutionMetadata.builder()
                .executionSource("Test")
                .traceId("trace-1")
                .sessionId("session-1")
                .executionId("exec-1")
                .timestamp(now)
                .build();

        ExecutionMetadata metadata3 = ExecutionMetadata.builder()
                .executionSource("Test2")
                .traceId("trace-2")
                .build();

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
        assertNotEquals(metadata1, metadata3);
    }

    @Test
    void testExecutionMetadata_ToString() {
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .build();

        String toString = metadata.toString();
        assertTrue(toString.contains("executionSource='Test'"));
        assertTrue(toString.contains("traceId="));
        assertTrue(toString.contains("executionId="));
    }

    @Test
    void testExecutionMetadata_NullSafety() {
        // Should handle null custom values gracefully
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .customValues(null)
                .build();

        assertNotNull(metadata.getCustomValues());
        assertTrue(metadata.getCustomValues().isEmpty());
    }

    @Test
    void testExecutionMetadata_TypeSafeCustomValue() {
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .addCustomValue("stringValue", "test")
                .addCustomValue("intValue", 42)
                .build();

        assertEquals("test", metadata.getCustomValue("stringValue", String.class));
        assertEquals(42, metadata.getCustomValue("intValue", Integer.class));

        // Test type mismatch
        assertThrows(ClassCastException.class, () -> {
            metadata.getCustomValue("stringValue", Integer.class);
        });
    }

    // =====================================================
    // EXECUTION REQUEST TESTS
    // =====================================================

    @Test
    void testExecutionRequest_Builder_RequiredFields() {
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("decision-1")
                .capabilityName("Capability1")
                .intent("TEST_INTENT")
                .userInput("test input")
                .build();

        assertNotNull(request.getRequestId());
        assertEquals("decision-1", request.getDecisionId());
        assertEquals("Capability1", request.getCapabilityName());
        assertEquals("TEST_INTENT", request.getIntent());
        assertEquals("test input", request.getUserInput());
        assertNotNull(request.getTimestamp());
    }

    @Test
    void testExecutionRequest_Builder_MissingRequiredFields() {
        // Missing decisionId
        assertThrows(IllegalStateException.class, () -> {
            ExecutionRequest.builder()
                    .capabilityName("Cap")
                    .intent("INTENT")
                    .userInput("input")
                    .build();
        });

        // Missing capabilityName
        assertThrows(IllegalStateException.class, () -> {
            ExecutionRequest.builder()
                    .decisionId("dec")
                    .intent("INTENT")
                    .userInput("input")
                    .build();
        });

        // Missing intent
        assertThrows(IllegalStateException.class, () -> {
            ExecutionRequest.builder()
                    .decisionId("dec")
                    .capabilityName("Cap")
                    .userInput("input")
                    .build();
        });

        // Missing userInput
        assertThrows(IllegalStateException.class, () -> {
            ExecutionRequest.builder()
                    .decisionId("dec")
                    .capabilityName("Cap")
                    .intent("INTENT")
                    .build();
        });
    }

    @Test
    void testExecutionRequest_Builder_AllFields() {
        ConversationSession session = new ConversationSession("user1");
        ResolvedContext resolvedContext = new ResolvedContext(
                ResolvedContext.Mode.CHAT, null, 0, 0, false, false, null, false
        );
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .build();

        Instant now = Instant.now();
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("req-123")
                .decisionId("dec-456")
                .capabilityName("TestCap")
                .intent("TEST")
                .userInput("test input")
                .session(session)
                .resolvedContext(resolvedContext)
                .metadata(metadata)
                .timestamp(now)
                .build();

        assertEquals("req-123", request.getRequestId());
        assertEquals("dec-456", request.getDecisionId());
        assertEquals("TestCap", request.getCapabilityName());
        assertEquals("TEST", request.getIntent());
        assertEquals("test input", request.getUserInput());
        assertSame(session, request.getSession());
        assertSame(resolvedContext, request.getResolvedContext());
        assertSame(metadata, request.getMetadata());
        assertEquals(now, request.getTimestamp());
    }

    @Test
    void testExecutionRequest_DefaultRequestId() {
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .build();

        assertNotNull(request.getRequestId());
        assertFalse(request.getRequestId().isBlank());
    }

    @Test
    void testExecutionRequest_EqualsAndHashCode() {
        ConversationSession session = new ConversationSession("user1");
        ResolvedContext resolvedContext = new ResolvedContext(
                ResolvedContext.Mode.CHAT, null, 0, 0, false, false, null, false
        );
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();

        ExecutionRequest request1 = ExecutionRequest.builder()
                .requestId("req-1")
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .session(session)
                .resolvedContext(resolvedContext)
                .metadata(metadata)
                .build();

        ExecutionRequest request2 = ExecutionRequest.builder()
                .requestId("req-1")
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .session(session)
                .resolvedContext(resolvedContext)
                .metadata(metadata)
                .build();

        ExecutionRequest request3 = ExecutionRequest.builder()
                .requestId("req-2")
                .decisionId("dec-2")
                .capabilityName("Cap2")
                .intent("INTENT2")
                .userInput("input2")
                .build();

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
    }

    @Test
    void testExecutionRequest_ToString() {
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("req-123")
                .decisionId("dec-456")
                .capabilityName("TestCap")
                .intent("TEST")
                .userInput("test input")
                .build();

        String toString = request.toString();
        assertTrue(toString.contains("requestId='req-123'"));
        assertTrue(toString.contains("decisionId='dec-456'"));
        assertTrue(toString.contains("capabilityName='TestCap'"));
        assertTrue(toString.contains("intent='TEST'"));
    }

    // =====================================================
    // EXECUTION RESULT TESTS
    // =====================================================

    @Test
    void testExecutionResult_Builder_RequiredFields() {
        ExecutionResult result = ExecutionResult.builder()
                .requestId("req-1")
                .capabilityName("TestCap")
                .build();

        assertNotNull(result.getExecutionId());
        assertEquals("req-1", result.getRequestId());
        assertEquals("TestCap", result.getCapabilityName());
        assertNotNull(result.getTimestamp());
        assertEquals(ExecutionStatus.UNKNOWN, result.getStatus());
        assertFalse(result.isSuccess());
    }

    @Test
    void testExecutionResult_Builder_MissingRequiredFields() {
        // Missing requestId
        assertThrows(IllegalStateException.class, () -> {
            ExecutionResult.builder()
                    .capabilityName("Cap")
                    .build();
        });

        // Missing capabilityName
        assertThrows(IllegalStateException.class, () -> {
            ExecutionResult.builder()
                    .requestId("req")
                    .build();
        });
    }

    @Test
    void testExecutionResult_Builder_AllFields() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();
        Instant now = Instant.now();

        ExecutionResult result = ExecutionResult.builder()
                .executionId("exec-123")
                .requestId("req-456")
                .success(true)
                .status(ExecutionStatus.SUCCESS)
                .response("Success response")
                .errorMessage(null)
                .executionTime(150)
                .capabilityName("TestCap")
                .metadata(metadata)
                .timestamp(now)
                .build();

        assertEquals("exec-123", result.getExecutionId());
        assertEquals("req-456", result.getRequestId());
        assertTrue(result.isSuccess());
        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());
        assertEquals("Success response", result.getResponse());
        assertNull(result.getErrorMessage());
        assertEquals(150, result.getExecutionTime());
        assertEquals("TestCap", result.getCapabilityName());
        assertSame(metadata, result.getMetadata());
        assertEquals(now, result.getTimestamp());
        assertFalse(result.isFailed());
        assertFalse(result.hasError());
    }

    @Test
    void testExecutionResult_FailureFields() {
        ExecutionResult result = ExecutionResult.builder()
                .requestId("req-1")
                .capabilityName("TestCap")
                .success(false)
                .status(ExecutionStatus.FAILED)
                .errorMessage("Something went wrong")
                .executionTime(50)
                .build();

        assertFalse(result.isSuccess());
        assertTrue(result.isFailed());
        assertTrue(result.hasError());
        assertEquals("Something went wrong", result.getErrorMessage());
    }

    @Test
    void testExecutionResult_EqualsAndHashCode() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();

        ExecutionResult result1 = ExecutionResult.builder()
                .executionId("exec-1")
                .requestId("req-1")
                .success(true)
                .status(ExecutionStatus.SUCCESS)
                .capabilityName("Cap")
                .metadata(metadata)
                .build();

        ExecutionResult result2 = ExecutionResult.builder()
                .executionId("exec-1")
                .requestId("req-1")
                .success(true)
                .status(ExecutionStatus.SUCCESS)
                .capabilityName("Cap")
                .metadata(metadata)
                .build();

        ExecutionResult result3 = ExecutionResult.builder()
                .executionId("exec-2")
                .requestId("req-2")
                .capabilityName("Cap2")
                .build();

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    void testExecutionResult_ToString() {
        ExecutionResult result = ExecutionResult.builder()
                .requestId("req-123")
                .capabilityName("TestCap")
                .success(true)
                .status(ExecutionStatus.SUCCESS)
                .response("Test response")
                .executionTime(100)
                .build();

        String toString = result.toString();
        assertTrue(toString.contains("requestId='req-123'"));
        assertTrue(toString.contains("capabilityName='TestCap'"));
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("status=SUCCESS"));
        assertTrue(toString.contains("executionTime=100ms"));
    }

    // =====================================================
    // EXECUTION CONTEXT TESTS
    // =====================================================

    @Test
    void testExecutionContext_Builder_DefaultValues() {
        ExecutionContext context = ExecutionContext.builder().build();

        assertNull(context.getDecision());
        assertNull(context.getValidationResult());
        assertNull(context.getExecutionRequest());
        assertNull(context.getParentExecutionId());
        assertEquals(0, context.getRetryCount());
        assertTrue(context.getFutureExecutionInfo().isEmpty());
        assertNotNull(context.getTimestamp());
        assertFalse(context.isRetry());
        assertFalse(context.hasParent());
    }

    @Test
    void testExecutionContext_Builder_AllFields() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .metadata(metadata)
                .build();

        Map<String, Object> futureInfo = new HashMap<>();
        futureInfo.put("nextCapability", "NextCap");

        Instant now = Instant.now();
        ExecutionContext context = ExecutionContext.builder()
                .decision(new CognitiveDecision(
                        CognitiveDecision.Action.RESPOND, "test"))
                .validationResult(new ValidationResult(
                        "val-1", "dec-1", true,
                        ValidationStatus.VALID,
                        ValidationStrategy.RULE_BASED,
                        ValidationResult.RiskLevel.LOW,
                        0.9, java.util.List.of(), java.util.List.of(),
                        new java.util.HashMap<>(), now, null))
                .executionRequest(request)
                .parentExecutionId("parent-123")
                .retryCount(2)
                .addFutureExecutionInfo("nextCapability", "NextCap")
                .timestamp(now)
                .build();

        assertNotNull(context.getDecision());
        assertNotNull(context.getValidationResult());
        assertSame(request, context.getExecutionRequest());
        assertEquals("parent-123", context.getParentExecutionId());
        assertEquals(2, context.getRetryCount());
        assertTrue(context.isRetry());
        assertTrue(context.hasParent());
        assertEquals("NextCap", context.getFutureExecutionInfo("nextCapability"));
        assertTrue(context.hasFutureExecutionInfo("nextCapability"));
        assertFalse(context.hasFutureExecutionInfo("nonexistent"));
    }

    @Test
    void testExecutionContext_CollectionImmutability() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Verify collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            context.getFutureExecutionInfo().put("key", "value");
        });
    }

    @Test
    void testExecutionContext_EqualsAndHashCode() {
        ExecutionMetadata metadata = ExecutionMetadata.builder().build();
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .metadata(metadata)
                .build();

        ExecutionContext context1 = ExecutionContext.builder()
                .executionRequest(request)
                .retryCount(1)
                .parentExecutionId("parent-1")
                .build();

        ExecutionContext context2 = ExecutionContext.builder()
                .executionRequest(request)
                .retryCount(1)
                .parentExecutionId("parent-1")
                .build();

        ExecutionContext context3 = ExecutionContext.builder()
                .executionRequest(request)
                .retryCount(2)
                .build();

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
        assertNotEquals(context1, context3);
    }

    @Test
    void testExecutionContext_ToString() {
        ExecutionContext context = ExecutionContext.builder()
                .parentExecutionId("parent-123")
                .retryCount(1)
                .build();

        String toString = context.toString();
        assertTrue(toString.contains("parentExecutionId='parent-123'"));
        assertTrue(toString.contains("retryCount=1"));
    }

    // =====================================================
    // IMMUTABILITY TESTS
    // =====================================================

    @Test
    void testExecutionRequest_NoSetters() {
        ExecutionRequest request = ExecutionRequest.builder()
                .decisionId("dec-1")
                .capabilityName("Cap")
                .intent("INTENT")
                .userInput("input")
                .build();

        // Verify no setters exist (compile-time check via reflection)
        assertTrue(hasNoSetters(ExecutionRequest.class));
    }

    @Test
    void testExecutionResult_NoSetters() {
        ExecutionResult result = ExecutionResult.builder()
                .requestId("req-1")
                .capabilityName("Cap")
                .build();

        // Verify no setters exist
        assertTrue(hasNoSetters(ExecutionResult.class));
    }

    @Test
    void testExecutionMetadata_NoSetters() {
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .build();

        // Verify no setters exist
        assertTrue(hasNoSetters(ExecutionMetadata.class));
    }

    @Test
    void testExecutionContext_NoSetters() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Verify no setters exist
        assertTrue(hasNoSetters(ExecutionContext.class));
    }

    // =====================================================
    // SERIALIZATION FRIENDLINESS TESTS
    // =====================================================

    @Test
    void testExecutionStatus_Serializable() {
        // Enums are serializable by default in Java
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        assertNotNull(status);
        assertSame(status, ExecutionStatus.valueOf("SUCCESS"));
    }

    @Test
    void testExecutionType_Serializable() {
        // Enums are serializable by default in Java
        ExecutionType type = ExecutionType.SYNC;
        assertNotNull(type);
        assertSame(type, ExecutionType.valueOf("SYNC"));
    }

    @Test
    void testExecutionMetadata_JsonFriendly() {
        ExecutionMetadata metadata = ExecutionMetadata.builder()
                .executionSource("Test")
                .traceId("trace-123")
                .sessionId("session-456")
                .addCustomValue("customKey", "customValue")
                .build();

        // Verify all fields are accessible for JSON serialization
        assertNotNull(metadata.getExecutionSource());
        assertNotNull(metadata.getTraceId());
        assertNotNull(metadata.getSessionId());
        assertNotNull(metadata.getCustomValues());
        assertNotNull(metadata.getTimestamp());
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private <T> boolean contains(T[] array, T value) {
        for (T item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNoSetters(Class<?> clazz) {
        for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                return false;
            }
        }
        return true;
    }
}