package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.kernels.tool.error.ToolError;
import com.shreeai.os.platform.kernels.tool.error.ToolErrorCode;
import com.shreeai.os.platform.kernels.tool.error.ToolException;
import com.shreeai.os.platform.kernels.tool.model.ToolExecutionMetrics;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolRegistryKernelHandler Tests")
class ToolRegistryKernelHandlerTest {

    private ToolService mockToolService;
    private ToolRegistryKernelHandler handler;

    @BeforeEach
    void setUp() {
        mockToolService = new MockToolService();
        handler = new ToolRegistryKernelHandler(mockToolService);
    }

    @Test
    @DisplayName("Handle successful tool execution returns SUCCESS")
    void handleSuccessfulToolExecution() {
        ((MockToolService) mockToolService).setResult(
                ToolResult.success("echo-tool", ToolType.FILES, "echo",
                        Map.of("result", "Hello from tool"),
                        ToolExecutionMetrics.empty()));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "echo Hello from tool",
                Map.of("toolId", "echo-tool", "action", "echo"));

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.isSuccess());
        assertTrue(result.output().contains("Hello from tool"));
        assertEquals(0.9, result.confidence(), 0.01);
    }

    @Test
    @DisplayName("Handle failed tool execution returns FAILURE")
    void handleFailedToolExecution() {
        ((MockToolService) mockToolService).setResult(
                ToolResult.failure("broken-tool", ToolType.FILES, "execute",
                        "Tool error message",
                        ToolExecutionMetrics.empty()));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "do something",
                Map.of("toolId", "broken-tool", "action", "execute"));

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("Tool error message"));
        assertEquals(0.0, result.confidence(), 0.01);
    }

    @Test
    @DisplayName("Metadata contains tool execution details")
    void metadataContainsToolDetails() {
        ((MockToolService) mockToolService).setResult(
                ToolResult.success("git-tool", ToolType.GIT, "status",
                        Map.of("result", "clean"),
                        ToolExecutionMetrics.empty()));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "git status",
                Map.of("toolId", "git-tool", "toolType", "git", "action", "status"));

        assertEquals("git-tool", result.metadata().get("toolId"));
        assertEquals("git", result.metadata().get("toolType"));
        assertEquals("status", result.metadata().get("action"));
        assertEquals("completed", result.metadata().get("toolStatus"));
    }

    @Test
    @DisplayName("Handle tool exception returns failure")
    void handleToolExceptionReturnsFailure() {
        ((MockToolService) mockToolService).setThrowException(
                new ToolException(new ToolError(
                        ToolErrorCode.EXECUTION_FAILURE,
                        "Critical tool failure",
                        "test",
                        Instant.now(),
                        Map.of())));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "input",
                Map.of("toolId", "fail-tool", "action", "execute"));

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertTrue(result.output().contains("Tool execution failed"));
    }

    @Test
    @DisplayName("Default toolType falls back to FILES when unknown")
    void defaultToolTypeFallsBackToFiles() {
        ((MockToolService) mockToolService).setResult(
                ToolResult.success("default-tool", ToolType.FILES, "execute",
                        Map.of(), ToolExecutionMetrics.empty()));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                "test",
                Map.of("toolId", "default-tool", "action", "execute"));

        assertEquals(ExecutionStatus.SUCCESS, result.status());
    }

    @Test
    @DisplayName("Null input handled gracefully")
    void nullInputHandledGracefully() {
        ((MockToolService) mockToolService).setResult(
                ToolResult.success("tool", ToolType.FILES, "execute",
                        Map.of("result", "done"), ToolExecutionMetrics.empty()));

        RichExecutionResult result = handler.handle(
                ExecutionCapability.TASK_EXECUTION,
                null,
                Map.of("toolId", "tool", "action", "execute"));

        assertEquals(ExecutionStatus.SUCCESS, result.status());
    }

    /**
     * Minimal ToolService mock for testing.
     */
    private static final class MockToolService implements ToolService {

        private ToolResult result;
        private RuntimeException exception;

        void setResult(ToolResult result) {
            this.result = result;
        }

        void setThrowException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public ToolResult executeTool(ToolRequest request) {
            if (exception != null) {
                throw exception;
            }
            return result != null ? result : ToolResult.success(
                    request.toolId(), request.toolType(), request.action(),
                    Map.of(), ToolExecutionMetrics.empty());
        }

        @Override
        public ToolStatus getToolStatus(String toolId) {
            return ToolStatus.COMPLETED;
        }

        @Override
        public ToolResult getToolResult(String toolId) {
            return result;
        }

        @Override
        public java.util.List<ToolResult> executeTools(java.util.List<ToolRequest> requests) {
            return java.util.List.of(executeTool(requests.get(0)));
        }

        @Override
        public boolean isToolSupported(ToolType toolType, String action) {
            return true;
        }
    }
}
