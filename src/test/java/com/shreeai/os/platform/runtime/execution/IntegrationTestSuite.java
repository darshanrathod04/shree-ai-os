package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;
import com.shreeai.os.platform.kernels.memory.model.MemoryStatus;
import com.shreeai.os.platform.kernels.memory.model.MemoryType;
import com.shreeai.os.platform.kernels.memory.model.MemoryVisibility;
import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.kernels.tool.model.ToolExecutionMetrics;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;
import com.shreeai.os.platform.security.engine.InMemoryApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;
import com.shreeai.os.platform.security.model.ApprovalStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration test suite for the V2.1 autonomous execution dispatch
 * infrastructure.
 *
 * <p>Validates the complete flow:
 * <pre>
 *   EXECUTE_TASK → ExecutionDispatcher → KernelRegistry → KernelHandler → RichExecutionResult
 * </pre>
 *
 * <p>Also tests the approval workflow, reflection kernel, and tool registry
 * integration end-to-end.
 *
 * @since 2.1
 */
@DisplayName("Integration Test Suite — EXECUTE_TASK → RichExecutionResult Flow")
class IntegrationTestSuite {

    private KernelRegistry registry;
    private DefaultPermissionPolicy policy;
    private ExecutionDispatcher dispatcher;
    private ApprovalIntegration approvalIntegration;
    private InMemoryApprovalService approvalService;

    // Mocks / fakes
    private FakeMemorySearchService memorySearchService;
    private FakeToolService toolService;

    @BeforeEach
    void setUp() {
        registry = new KernelRegistry();
        policy = new DefaultPermissionPolicy(PermissionDecision.ALLOW);
        dispatcher = new ExecutionDispatcher(registry, policy);
        approvalService = new InMemoryApprovalService();
                approvalIntegration = new ApprovalIntegration(dispatcher, approvalService, policy);

        // Create and register kernel handlers
        memorySearchService = new FakeMemorySearchService();
        toolService = new FakeToolService();

        registry.register(ExecutionCapability.MEMORY_RECALL,
                new MemoryKernelHandler(memorySearchService));
        registry.register(ExecutionCapability.TASK_EXECUTION,
                new ToolRegistryKernelHandler(toolService));
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                new ReflectionKernelHandler(new DefaultReflectionEngine()));
    }

    // ==========================================================
    // EXECUTE_TASK → SDKResponse full-flow tests
    // ==========================================================

    @Test
    @DisplayName("Full flow: EXECUTE_TASK with MEMORY_RECALL routes to MemoryKernelHandler")
    void executeTaskRoutesToMemoryKernel() {
        // Given: memory contains test data
                memorySearchService.add(createMemory("mem-1", "Java programming language"));

        // When: dispatch EXECUTE_TASK with MEMORY_RECALL capability
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.MEMORY_RECALL,
                "Java programming",
                Map.of());

        // Then: memory results returned, RichExecutionResult with metadata
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.output().contains("Found 1 memory"));
                assertTrue(result.output().contains("Java programming language"));
        assertEquals(1, result.metadata().get("memoryCount"));
    }

    @Test
    @DisplayName("Full flow: EXECUTE_TASK with TASK_EXECUTION routes to ToolRegistryKernelHandler")
    void executeTaskRoutesToToolRegistry() {
        // Given: tool service is ready to return a result
        toolService.setResult(ToolResult.success(
                "echo-tool", ToolType.FILES, "echo",
                Map.of("result", "Hello from tool"),
                ToolExecutionMetrics.empty()));

        // When: dispatch EXECUTE_TASK with TASK_EXECUTION capability
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION,
                "echo Hello from tool",
                Map.of("toolId", "echo-tool", "action", "echo"));

        // Then: tool result returned
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.output().contains("Hello from tool"));
        assertEquals("echo-tool", result.metadata().get("toolId"));
        assertEquals(0.9, result.confidence(), 0.01);
    }

    @Test
    @DisplayName("Full flow: EXECUTE_TASK with reflection routes to ReflectionKernelHandler")
    void executeTaskRoutesToReflectionKernel() {
        // When: dispatch with KNOWLEDGE_SEARCH capability (wired to reflection)
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "Evaluate this execution outcome",
                Map.of(
                        "requestId", "integration-reflection-1",
                        "executionSuccess", true,
                        "confidence", 0.9,
                        "planStepCount", 3,
                        "responseSummary", "Task completed successfully"));

        // Then: reflection analysis returned
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.metadata().containsKey("reflectionVerdict"));
        assertEquals("SUCCESS", result.metadata().get("reflectionVerdict"));
        assertEquals(0.9, (Double) result.metadata().get("reflectionScore"), 0.1);
        assertTrue(result.metadata().containsKey("reflectionLessons"));
    }

    @Test
    @DisplayName("Full flow: EXECUTE_TASK with denied capability returns DENIED")
    void executeTaskDeniedCapabilityReturnsDenied() {
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION,
                "input",
                Map.of());

        assertNotNull(result);
        assertEquals(ExecutionStatus.DENIED, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.output().contains("denied"));
    }

    @Test
    @DisplayName("Full flow: EXECUTE_TASK with unregistered capability returns FAILURE")
    void executeTaskUnregisteredCapabilityReturnsFailure() {
        // WORKOUT_PLANNING is not registered in this test setup
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.WORKOUT_PLANNING,
                "plan workout",
                Map.of());

        assertNotNull(result);
        assertEquals(ExecutionStatus.FAILED, result.status());
        assertTrue(result.output().contains("No handler registered"));
    }

    // ==========================================================
    // Approval Workflow integration tests
    // ==========================================================

    @Test
    @DisplayName("Full flow: approval workflow from PENDING_APPROVAL to completion")
    void fullApprovalWorkflow() {
        // Given: PROJECT_PLANNING requires approval
        policy.set(ExecutionCapability.KNOWLEDGE_SEARCH, PermissionDecision.REQUIRE_APPROVAL);
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                new ReflectionKernelHandler(new DefaultReflectionEngine()));

        Map<String, Object> context = Map.of(
                "requestId", "approval-flow-1",
                "executionSuccess", true,
                "confidence", 0.8);

        // When: task is dispatched → requires approval
        RichExecutionResult pending = approvalIntegration.executeTask(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "reflect on execution",
                context);

        // Then: pending approval with approval request ID
        assertEquals(ExecutionStatus.PENDING_APPROVAL, pending.status());
        String approvalId = (String) pending.metadata().get("approvalRequestId");
        assertNotNull(approvalId);

        // Verify approval request was created
        Optional<ApprovalRequest> stored = Optional.ofNullable(
                approvalService.find(approvalId).orElse(null));
        assertTrue(stored.isPresent());
        assertEquals(ApprovalStatus.PENDING, stored.get().status());

        // When: approve and resume
        RichExecutionResult resumed = approvalIntegration.approveAndResume(
                approvalId,
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "reflect on execution",
                context);

        // Then: reflection executed successfully
        assertEquals(ExecutionStatus.SUCCESS, resumed.status());
        assertTrue(resumed.metadata().containsKey("reflectionVerdict"));

        // Verify approval was updated
        assertEquals(ApprovalStatus.APPROVED,
                approvalIntegration.getApprovalStatus(approvalId));
    }

    @Test
    @DisplayName("Full flow: denied approval prevents execution")
    void deniedApprovalPreventsExecution() {
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.REQUIRE_APPROVAL);

        RichExecutionResult pending = approvalIntegration.executeTask(
                ExecutionCapability.TASK_EXECUTION,
                "do something",
                Map.of());

        assertEquals(ExecutionStatus.PENDING_APPROVAL, pending.status());
        String approvalId = (String) pending.metadata().get("approvalRequestId");

        RichExecutionResult denied = approvalIntegration.deny(
                approvalId, ExecutionCapability.TASK_EXECUTION);

        assertEquals(ExecutionStatus.DENIED, denied.status());
        assertEquals(ApprovalStatus.DENIED,
                approvalIntegration.getApprovalStatus(approvalId));
    }

    // ==========================================================
    // Permission policy integration with dispatcher
    // ==========================================================

    @Test
    @DisplayName("Permission policy evaluated before handler is invoked")
    void permissionPolicyEvaluatedBeforeHandler() {
        AtomicInteger invocationCount = new AtomicInteger(0);
        registry.register(ExecutionCapability.TASK_EXECUTION,
                (capability, input, context) -> {
                    invocationCount.incrementAndGet();
                    return RichExecutionResult.success(capability, "executed", 0.9);
                });

        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION,
                "input",
                Map.of());

        assertEquals(ExecutionStatus.DENIED, result.status());
        assertEquals(0, invocationCount.get(),
                "Handler should not be invoked when denied");
    }

    @Test
    @DisplayName("All 5 capabilities can be registered and dispatched")
    void allCapabilitiesRegisteredAndDispatchable() {
        // Re-register all with simple handlers
        for (ExecutionCapability cap : ExecutionCapability.values()) {
            registry.register(cap, (capability, input, ctx) ->
                    RichExecutionResult.success(capability, "result-for-" + capability.value(), 0.5));
        }

        for (ExecutionCapability cap : ExecutionCapability.values()) {
            RichExecutionResult result = dispatcher.dispatch(cap, "test", Map.of());
            assertEquals(ExecutionStatus.SUCCESS, result.status());
            assertTrue(result.output().contains("result-for-" + cap.value()));
        }
    }

    // ==========================================================
    // KernelHandler exception propagation
    // ==========================================================

    @Test
    @DisplayName("Handler exception is caught and returns FAILED result")
    void handlerExceptionReturnsFailedResult() {
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) -> {
                    throw new RuntimeException("Kernel crashed");
                });

        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "trigger error",
                Map.of());

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertTrue(result.output().contains("Kernel crashed"));
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private Memory createMemory(String id, String text) {
        MemoryId memoryId = new MemoryId(id);
        Instant now = Instant.now();
        MemoryContent content = new MemoryContent(text, null, Map.of(), now);
        IdentityId ownerId = new IdentityId("integration-test-user");
                MemoryMetadata metadata = new MemoryMetadata(
                memoryId, MemoryType.WORKING, MemoryStatus.ACTIVE,
                MemoryVisibility.PRIVATE, ownerId, Set.of("test"),
                0.8, 0.9, "integration-test",
                now, now, now, 0);
        return new Memory(memoryId, content, metadata, now, now);
    }

    /**
     * Simple in-memory MemorySearchService for integration testing.
     */
    private static final class FakeMemorySearchService implements MemorySearchService {

        private final List<Memory> memories = new java.util.ArrayList<>();

        void add(Memory memory) {
            memories.add(memory);
        }

        @Override
        public List<Memory> search(String query) {
            if (query == null || query.isBlank()) return List.copyOf(memories);
            String lower = query.toLowerCase();
            return memories.stream()
                    .filter(m -> m.content().text().toLowerCase().contains(lower))
                    .toList();
        }

        @Override
        public List<Memory> searchByTags(Set<String> tags) {
            return List.copyOf(memories);
        }

        @Override
        public List<Memory> searchByDate(Instant from, Instant to) {
            return List.copyOf(memories);
        }

        @Override
        public List<Memory> searchBySimilarity(String text) {
            return List.copyOf(memories);
        }

        @Override
        public List<Memory> searchByOwner(IdentityId ownerId) {
            return List.copyOf(memories);
        }
    }

    /**
     * Simple in-memory ToolService for integration testing.
     */
    private static final class FakeToolService implements ToolService {

        private ToolResult result;
        private RuntimeException exception;

        void setResult(ToolResult result) {
            this.result = result;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public ToolResult executeTool(ToolRequest request) {
            if (exception != null) {
                throw exception;
            }
            return result != null ? result : ToolResult.success(
                    request.toolId(), request.toolType(), request.action(),
                    Map.of("result", "default tool output"),
                    ToolExecutionMetrics.empty());
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
        public List<ToolResult> executeTools(List<ToolRequest> requests) {
            return requests.stream()
                    .map(this::executeTool)
                    .toList();
        }

        @Override
        public boolean isToolSupported(ToolType toolType, String action) {
            return true;
        }
        }
}
