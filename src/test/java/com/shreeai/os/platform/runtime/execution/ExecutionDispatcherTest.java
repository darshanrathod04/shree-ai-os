package com.shreeai.os.platform.runtime.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExecutionDispatcher}.
 */
@DisplayName("ExecutionDispatcher Tests")
class ExecutionDispatcherTest {

    private KernelRegistry registry;
    private DefaultPermissionPolicy policy;
    private ExecutionDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        registry = new KernelRegistry();
        policy = new DefaultPermissionPolicy();
        dispatcher = new ExecutionDispatcher(registry, policy);
    }

    @Test
    @DisplayName("Dispatch denied capability returns denied result")
    void dispatchDeniedReturnsDenied() {
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION, "input", Map.of());

        assertEquals(ExecutionStatus.DENIED, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("denied"));
    }

    @Test
    @DisplayName("Dispatch requires approval returns pending result")
    void dispatchRequiresApprovalReturnsPending() {
        policy.set(ExecutionCapability.PROJECT_PLANNING, PermissionDecision.REQUIRE_APPROVAL);

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.PROJECT_PLANNING, "input", Map.of());

        assertEquals(ExecutionStatus.PENDING_APPROVAL, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("approval"));
    }

    @Test
    @DisplayName("Dispatch unregistered capability returns failure")
    void dispatchUnregisteredReturnsFailure() {
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.KNOWLEDGE_SEARCH, "input", Map.of());

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("No handler registered"));
    }

    @Test
    @DisplayName("Dispatch registered capability executes handler")
    void dispatchRegisteredExecutesHandler() {
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "knowledge-output", 0.95));

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.KNOWLEDGE_SEARCH, "query", Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.isSuccess());
        assertEquals("knowledge-output", result.output());
        assertEquals(0.95, result.confidence());
        assertEquals(ExecutionCapability.KNOWLEDGE_SEARCH, result.capability());
    }

    @Test
    @DisplayName("Dispatch null capability throws IllegalArgumentException")
    void dispatchNullCapabilityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> dispatcher.dispatch(null, "input", Map.of()));
    }

    @Test
    @DisplayName("Dispatch with null input treats as empty string")
    void dispatchNullInputTreatedAsEmpty() {
        registry.register(ExecutionCapability.MEMORY_RECALL,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.8));

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.MEMORY_RECALL, null, Map.of());

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Dispatch with null context treats as empty map")
    void dispatchNullContextTreatedAsEmpty() {
        registry.register(ExecutionCapability.MEMORY_RECALL,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.8));

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.MEMORY_RECALL, "input", null);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Dispatch handler exception returns failure result")
    void dispatchHandlerExceptionReturnsFailure() {
        registry.register(ExecutionCapability.TASK_EXECUTION,
                (capability, input, context) -> {
                    throw new RuntimeException("Handler error");
                });

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION, "input", Map.of());

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertTrue(result.output().contains("Handler execution failed"));
    }

    @Test
    @DisplayName("isDispatchable returns true for registered and allowed capability")
    void isDispatchableTrueWhenRegisteredAndAllowed() {
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.9));

        assertTrue(dispatcher.isDispatchable(ExecutionCapability.KNOWLEDGE_SEARCH));
    }

    @Test
    @DisplayName("isDispatchable returns false for denied capability")
    void isDispatchableFalseWhenDenied() {
        registry.register(ExecutionCapability.TASK_EXECUTION,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.9));
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        assertFalse(dispatcher.isDispatchable(ExecutionCapability.TASK_EXECUTION));
    }

    @Test
    @DisplayName("isDispatchable returns false for unregistered capability")
    void isDispatchableFalseWhenUnregistered() {
        assertFalse(dispatcher.isDispatchable(ExecutionCapability.PROJECT_PLANNING));
    }

    @Test
    @DisplayName("isDispatchable returns false for null capability")
    void isDispatchableFalseForNull() {
        assertFalse(dispatcher.isDispatchable(null));
    }

    @Test
    @DisplayName("Result contains executionId and timestamps")
    void resultContainsExecutionIdAndTimestamps() {
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.9));

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.KNOWLEDGE_SEARCH, "input", Map.of());

        assertNotNull(result.executionId());
        assertFalse(result.executionId().isBlank());
        assertNotNull(result.startedAt());
        assertNotNull(result.completedAt());
        assertTrue(result.durationMs() >= 0);
    }
}
