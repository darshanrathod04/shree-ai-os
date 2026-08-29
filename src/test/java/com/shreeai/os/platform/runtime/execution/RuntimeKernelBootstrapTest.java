package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.security.api.ApprovalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuntimeKernelBootstrap Tests")
class RuntimeKernelBootstrapTest {

    private MemorySearchService memorySearchService;
    private ToolService toolService;
    private DefaultReflectionEngine reflectionEngine;
    private RuntimeKernelBootstrap bootstrap;

    @BeforeEach
    void setUp() {
        memorySearchService = new MockMemorySearchService();
        toolService = new MockToolService();
        reflectionEngine = new DefaultReflectionEngine();

        bootstrap = new RuntimeKernelBootstrap(
                memorySearchService,
                toolService,
                reflectionEngine);
    }

    @Test
    @DisplayName("Bootstrap creates dispatcher after registering handlers")
    void bootstrapCreatesDispatcherAfterRegistration() {
        bootstrap.registerAllHandlers();

        ExecutionDispatcher dispatcher = bootstrap.getDispatcher();
        assertNotNull(dispatcher);
    }

    @Test
    @DisplayName("Dispatcher dispatches MEMORY_RECALL to MemoryKernelHandler")
    void dispatcherDispatchesMemoryRecall() {
        bootstrap.registerAllHandlers();
        ExecutionDispatcher dispatcher = bootstrap.getDispatcher();

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.MEMORY_RECALL,
                "search query",
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.metadata().containsKey("memoryCount"));
    }

    @Test
    @DisplayName("Dispatcher dispatches TASK_EXECUTION to ToolRegistryKernelHandler")
    void dispatcherDispatchesTaskExecution() {
        bootstrap.registerAllHandlers();
        ExecutionDispatcher dispatcher = bootstrap.getDispatcher();

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION,
                "tool input",
                Map.of("toolId", "default", "action", "execute"));

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.metadata().containsKey("toolId"));
    }

    @Test
    @DisplayName("Bootstrap allows setting permission policy")
    void bootstrapAllowsSettingPermission() {
        bootstrap.registerAllHandlers();
        bootstrap.setPermission(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        ExecutionDispatcher dispatcher = bootstrap.getDispatcher();

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION,
                "input",
                Map.of());

        assertEquals(ExecutionStatus.DENIED, result.status());
    }

    @Test
    @DisplayName("Bootstrap supports require_approval workflow")
    void bootstrapSupportsApprovalWorkflow() {
        bootstrap.registerAllHandlers();
        bootstrap.setPermission(ExecutionCapability.PROJECT_PLANNING,
                PermissionDecision.REQUIRE_APPROVAL);

        ExecutionDispatcher dispatcher = bootstrap.getDispatcher();
        ApprovalService approvalService = new MockApprovalService();

        ApprovalIntegration integration =
                bootstrap.createApprovalIntegration(approvalService);

        RichExecutionResult result = integration.executeTask(
                ExecutionCapability.PROJECT_PLANNING,
                "plan",
                Map.of());

        assertEquals(ExecutionStatus.PENDING_APPROVAL, result.status());
        assertTrue(result.metadata().containsKey("approvalRequestId"));
    }

    @Test
    @DisplayName("KernelRegistry has all capabilities registered")
    void kernelRegistryHasAllCapabilities() {
        bootstrap.registerAllHandlers();

        KernelRegistry registry = bootstrap.getKernelRegistry();

        for (ExecutionCapability capability : ExecutionCapability.values()) {
            assertTrue(registry.isRegistered(capability),
                    "Capability " + capability + " should be registered");
        }
    }

    @Test
    @DisplayName("GetDispatcher throws before initialization")
    void getDispatcherThrowsBeforeInitialization() {
        assertThrows(IllegalStateException.class,
                () -> bootstrap.getDispatcher());
    }

    // --- Mock Services ---

    private static final class MockMemorySearchService implements MemorySearchService {
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.memory.model.Memory> search(String query) {
            return java.util.List.of();
        }
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.memory.model.Memory> searchByTags(java.util.Set<String> tags) {
            return java.util.List.of();
        }
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.memory.model.Memory> searchByDate(java.time.Instant from, java.time.Instant to) {
            return java.util.List.of();
        }
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.memory.model.Memory> searchBySimilarity(String text) {
            return java.util.List.of();
        }
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.memory.model.Memory> searchByOwner(com.shreeai.os.platform.kernels.identity.model.IdentityId ownerId) {
            return java.util.List.of();
        }
    }

    private static final class MockToolService implements ToolService {
        @Override
        public com.shreeai.os.platform.kernels.tool.model.ToolResult executeTool(com.shreeai.os.platform.kernels.tool.model.ToolRequest request) {
            return com.shreeai.os.platform.kernels.tool.model.ToolResult.success(
                    request.toolId(), request.toolType(), request.action(),
                    java.util.Map.of("result", "mock output"),
                    com.shreeai.os.platform.kernels.tool.model.ToolExecutionMetrics.empty());
        }
        @Override
        public com.shreeai.os.platform.kernels.tool.model.ToolStatus getToolStatus(String toolId) {
            return com.shreeai.os.platform.kernels.tool.model.ToolStatus.COMPLETED;
        }
        @Override
        public com.shreeai.os.platform.kernels.tool.model.ToolResult getToolResult(String toolId) {
            return null;
        }
        @Override
        public java.util.List<com.shreeai.os.platform.kernels.tool.model.ToolResult> executeTools(java.util.List<com.shreeai.os.platform.kernels.tool.model.ToolRequest> requests) {
            return java.util.List.of();
        }
        @Override
        public boolean isToolSupported(com.shreeai.os.platform.kernels.tool.model.ToolType toolType, String action) {
            return true;
        }
    }

    private static final class MockApprovalService implements ApprovalService {
        @Override
        public com.shreeai.os.platform.security.model.ApprovalRequest create(com.shreeai.os.platform.security.model.ApprovalRequest request) {
            return request;
        }
        @Override
        public java.util.Optional<com.shreeai.os.platform.security.model.ApprovalRequest> find(String requestId) {
            return java.util.Optional.empty();
        }
        @Override
        public com.shreeai.os.platform.security.model.ApprovalRequest approve(String requestId) {
            return null;
        }
        @Override
        public com.shreeai.os.platform.security.model.ApprovalRequest deny(String requestId) {
            return null;
        }
    }
}
