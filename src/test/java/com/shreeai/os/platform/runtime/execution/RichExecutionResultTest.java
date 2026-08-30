package com.shreeai.os.platform.runtime.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RichExecutionResult}.
 */
@DisplayName("RichExecutionResult Tests")
class RichExecutionResultTest {

    @Test
    @DisplayName("Success factory creates successful result with output")
    void successFactoryCreatesResult() {
        RichExecutionResult result =
                RichExecutionResult.success(ExecutionCapability.KNOWLEDGE_SEARCH, "output", 0.95);

        assertTrue(result.isSuccess());
        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertEquals("output", result.output());
        assertEquals(0.95, result.confidence());
        assertEquals(ExecutionCapability.KNOWLEDGE_SEARCH, result.capability());
    }

    @Test
    @DisplayName("Failure factory creates failed result with reason")
    void failureFactoryCreatesResult() {
        RichExecutionResult result =
                RichExecutionResult.failure(ExecutionCapability.TASK_EXECUTION, "something broke");

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.FAILED, result.status());
        assertEquals("something broke", result.output());
        assertEquals(0.0, result.confidence());
        assertEquals(ExecutionCapability.TASK_EXECUTION, result.capability());
    }

    @Test
    @DisplayName("Denied factory creates denied result")
    void deniedFactoryCreatesResult() {
        RichExecutionResult result =
                RichExecutionResult.denied(ExecutionCapability.MEMORY_RECALL, "blocked");

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.DENIED, result.status());
        assertEquals("blocked", result.output());
    }

    @Test
    @DisplayName("Pending approval factory creates pending result")
    void pendingApprovalFactoryCreatesResult() {
        RichExecutionResult result =
                RichExecutionResult.pendingApproval(ExecutionCapability.PROJECT_PLANNING, "need approval");

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.PENDING_APPROVAL, result.status());
        assertEquals("need approval", result.output());
        assertEquals(0.0, result.confidence());
    }

    @Test
    @DisplayName("Builder sets all fields correctly")
    void builderSetsAllFields() {
        Instant started = Instant.now().minusSeconds(5);
        Instant completed = Instant.now();

        RichExecutionResult result = RichExecutionResult.builder()
                .executionId("exec-123")
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .status(ExecutionStatus.SUCCESS)
                .startedAt(started)
                .completedAt(completed)
                .confidence(0.85)
                .output("builder output")
                .metadata(Map.of("key", "value"))
                .build();

        assertEquals("exec-123", result.executionId());
        assertEquals(ExecutionCapability.KNOWLEDGE_SEARCH, result.capability());
        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertEquals(started, result.startedAt());
        assertEquals(completed, result.completedAt());
        assertEquals(0.85, result.confidence());
        assertEquals("builder output", result.output());
        assertEquals(Map.of("key", "value"), result.metadata());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Builder generates random executionId by default")
    void builderGeneratesRandomExecutionId() {
        RichExecutionResult result = RichExecutionResult.builder()
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .build();

        assertNotNull(result.executionId());
        assertFalse(result.executionId().isBlank());
    }

    @Test
    @DisplayName("Builder requires capability")
    void builderRequiresCapability() {
        assertThrows(IllegalArgumentException.class,
                () -> RichExecutionResult.builder().build());
    }

    @Test
    @DisplayName("Builder validates confidence range")
    void builderValidatesConfidenceRange() {
        assertThrows(IllegalArgumentException.class, () ->
                RichExecutionResult.builder()
                        .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                        .confidence(-0.1)
                        .build());
        assertThrows(IllegalArgumentException.class, () ->
                RichExecutionResult.builder()
                        .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                        .confidence(1.1)
                        .build());
    }

    @Test
    @DisplayName("Builder null capability throws NPE")
    void builderNullCapabilityThrows() {
        assertThrows(NullPointerException.class,
                () -> RichExecutionResult.builder().capability(null).build());
    }

    @Test
    @DisplayName("Builder null executionId throws NPE")
    void builderNullExecutionIdThrows() {
        assertThrows(NullPointerException.class,
                () -> RichExecutionResult.builder()
                        .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                        .executionId(null)
                        .build());
    }

    @Test
    @DisplayName("Output defaults to empty string for null")
    void outputDefaultsToEmptyForNull() {
        RichExecutionResult result = RichExecutionResult.builder()
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .output(null)
                .build();

        assertEquals("", result.output());
    }

    @Test
    @DisplayName("Metadata defaults to empty map for null")
    void metadataDefaultsToEmptyMapForNull() {
        RichExecutionResult result = RichExecutionResult.builder()
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .metadata(null)
                .build();

        assertEquals(Map.of(), result.metadata());
    }

    @Test
    @DisplayName("Metadata is defensively copied")
    void metadataIsDefensivelyCopied() {
        Map<String, Object> original = new java.util.HashMap<>();
        original.put("key", "original");

        RichExecutionResult result = RichExecutionResult.builder()
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .metadata(original)
                .build();

        original.put("key", "modified");
        assertEquals("original", result.metadata().get("key"));
    }

    @Test
    @DisplayName("Duration is calculated from startedAt and completedAt")
    void durationCalculatedFromTimestamps() {
        Instant started = Instant.parse("2024-01-01T10:00:00Z");
        Instant completed = Instant.parse("2024-01-01T10:00:05Z");

        RichExecutionResult result = RichExecutionResult.builder()
                .capability(ExecutionCapability.KNOWLEDGE_SEARCH)
                .startedAt(started)
                .completedAt(completed)
                .build();

        assertEquals(5000L, result.durationMs());
    }

    @Test
    @DisplayName("ToExecutionResult produces compatible result")
    void toExecutionResultProducesCompatibleResult() {
        RichExecutionResult rich = RichExecutionResult.success(
                ExecutionCapability.KNOWLEDGE_SEARCH, "output", 0.95);

                ExecutionResult legacy = rich.toExecutionResult();

        assertTrue(legacy.isSuccess());
        assertEquals("output", legacy.output().orElseThrow());
        assertEquals(rich.executionId(), legacy.requestId());
        assertEquals(rich.metadata(), legacy.structuredPayload());
    }

    @Test
    @DisplayName("ToExecutionResult produces failure compatible result")
    void toExecutionResultProducesFailureCompatibleResult() {
        RichExecutionResult rich = RichExecutionResult.failure(
                ExecutionCapability.TASK_EXECUTION, "error details");

        ExecutionResult legacy = rich.toExecutionResult();

                assertFalse(legacy.isSuccess());
        assertEquals("error details", legacy.errorMessage().orElseThrow());
        assertEquals(rich.executionId(), legacy.requestId());
    }

    @Test
    @DisplayName("ToString includes key fields")
    void toStringIncludesKeyFields() {
        RichExecutionResult result = RichExecutionResult.success(
                ExecutionCapability.KNOWLEDGE_SEARCH, "output", 0.95);

        String str = result.toString();
        assertTrue(str.contains("executionId"));
        assertTrue(str.contains("capability"));
        assertTrue(str.contains("status"));
        assertTrue(str.contains("durationMs"));
        assertTrue(str.contains("confidence"));
    }

    @Test
    @DisplayName("Pending approval result has metadata flag")
    void pendingApprovalHasMetadataFlag() {
        RichExecutionResult result = RichExecutionResult.pendingApproval(
                ExecutionCapability.PROJECT_PLANNING, "approval needed");

        assertTrue(result.metadata().containsKey("pendingApproval"));
        assertEquals(true, result.metadata().get("pendingApproval"));
    }
}
