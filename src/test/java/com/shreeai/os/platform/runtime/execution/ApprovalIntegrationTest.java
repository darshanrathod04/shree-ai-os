package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.security.engine.InMemoryApprovalService;
import com.shreeai.os.platform.security.model.ApprovalStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApprovalIntegration Tests")
class ApprovalIntegrationTest {

    private KernelRegistry registry;
    private DefaultPermissionPolicy policy;
    private ExecutionDispatcher dispatcher;
    private InMemoryApprovalService approvalService;
    private ApprovalIntegration approvalIntegration;

    @BeforeEach
    void setUp() {
        registry = new KernelRegistry();
        policy = new DefaultPermissionPolicy();
        dispatcher = new ExecutionDispatcher(registry, policy);
        approvalService = new InMemoryApprovalService();
                approvalIntegration = new ApprovalIntegration(dispatcher, approvalService, policy);
    }

    @Test
    @DisplayName("executeTask with ALLOW executes immediately")
    void executeTaskWithAllowExecutesImmediately() {
        registry.register(ExecutionCapability.TASK_EXECUTION,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "tool-output", 0.9));

        RichExecutionResult result = approvalIntegration.executeTask(
                ExecutionCapability.TASK_EXECUTION,
                "run tool",
                Map.of());

        assertEquals(ExecutionStatus.SUCCESS, result.status());
        assertTrue(result.isSuccess());
        assertEquals("tool-output", result.output());
    }

    @Test
    @DisplayName("executeTask with REQUIRE_APPROVAL creates approval request")
    void executeTaskWithRequireApprovalCreatesRequest() {
        policy.set(ExecutionCapability.PROJECT_PLANNING, PermissionDecision.REQUIRE_APPROVAL);

        RichExecutionResult result = approvalIntegration.executeTask(
                ExecutionCapability.PROJECT_PLANNING,
                "plan project",
                Map.of("projectName", "TestProject"));

        assertEquals(ExecutionStatus.PENDING_APPROVAL, result.status());
        assertFalse(result.isSuccess());
        assertTrue(result.metadata().containsKey("approvalRequestId"));
        assertEquals(ApprovalStatus.PENDING.name(),
                result.metadata().get("approvalStatus"));
    }

    @Test
    @DisplayName("executeTask with DENY returns denied result")
    void executeTaskWithDenyReturnsDenied() {
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);

        RichExecutionResult result = approvalIntegration.executeTask(
                ExecutionCapability.TASK_EXECUTION,
                "blocked action",
                Map.of());

        assertEquals(ExecutionStatus.DENIED, result.status());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("approveAndResume re-dispatches after approval")
    void approveAndResumeReDispatchesAfterApproval() {
        // Setup: PROJECT_PLANNING requires approval
        policy.set(ExecutionCapability.PROJECT_PLANNING, PermissionDecision.REQUIRE_APPROVAL);
        registry.register(ExecutionCapability.PROJECT_PLANNING,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "plan-output", 0.85));

        // 1. Execute task → gets PENDING_APPROVAL
        Map<String, Object> context = Map.of("projectName", "TestProject");
        RichExecutionResult pendingResult = approvalIntegration.executeTask(
                ExecutionCapability.PROJECT_PLANNING,
                "plan project",
                context);

        assertEquals(ExecutionStatus.PENDING_APPROVAL, pendingResult.status());

        String approvalRequestId = (String) pendingResult.metadata().get("approvalRequestId");
        assertNotNull(approvalRequestId);

        // 2. Approve and resume
        RichExecutionResult resumedResult = approvalIntegration.approveAndResume(
                approvalRequestId,
                ExecutionCapability.PROJECT_PLANNING,
                "plan project",
                context);

        assertEquals(ExecutionStatus.SUCCESS, resumedResult.status());
        assertEquals("plan-output", resumedResult.output());
        assertEquals(0.85, resumedResult.confidence(), 0.01);
    }

    @Test
    @DisplayName("deny cancels pending approval request")
    void denyCancelsPendingRequest() {
        policy.set(ExecutionCapability.PROJECT_PLANNING, PermissionDecision.REQUIRE_APPROVAL);
        registry.register(ExecutionCapability.PROJECT_PLANNING,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "output", 0.9));

        RichExecutionResult pendingResult = approvalIntegration.executeTask(
                ExecutionCapability.PROJECT_PLANNING,
                "plan",
                Map.of());

        String approvalRequestId = (String) pendingResult.metadata().get("approvalRequestId");
        assertNotNull(approvalRequestId);

        // Check pending status
        assertEquals(ApprovalStatus.PENDING,
                approvalIntegration.getApprovalStatus(approvalRequestId));

        // Deny the request
        RichExecutionResult deniedResult = approvalIntegration.deny(
                approvalRequestId,
                ExecutionCapability.PROJECT_PLANNING);

        assertEquals(ExecutionStatus.DENIED, deniedResult.status());
        assertEquals(ApprovalStatus.DENIED,
                approvalIntegration.getApprovalStatus(approvalRequestId));
    }

    @Test
    @DisplayName("approveAndResume with unknown request throws")
    void approveAndResumeWithUnknownRequestThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> approvalIntegration.approveAndResume(
                        "unknown-id",
                        ExecutionCapability.TASK_EXECUTION,
                        "input",
                        Map.of()));
    }

    @Test
    @DisplayName("getApprovalStatus returns null for unknown request")
    void getApprovalStatusReturnsNullForUnknown() {
        assertNull(approvalIntegration.getApprovalStatus("non-existent"));
    }

    @Test
    @DisplayName("Full approval lifecycle works end-to-end")
    void fullApprovalLifecycle() {
        policy.set(ExecutionCapability.KNOWLEDGE_SEARCH, PermissionDecision.REQUIRE_APPROVAL);
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH,
                (capability, input, context) ->
                        RichExecutionResult.success(capability, "search-results", 0.92));

        Map<String, Object> context = Map.of("query", "Java concurrency");

        // 1. Execute → pending approval
        RichExecutionResult pending = approvalIntegration.executeTask(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "What is Java concurrency?",
                context);

        assertEquals(ExecutionStatus.PENDING_APPROVAL, pending.status());

        String requestId = (String) pending.metadata().get("approvalRequestId");

        // 2. Approval status should be PENDING
        assertEquals(ApprovalStatus.PENDING,
                approvalIntegration.getApprovalStatus(requestId));

        // 3. Approve and resume
        RichExecutionResult resumed = approvalIntegration.approveAndResume(
                requestId,
                ExecutionCapability.KNOWLEDGE_SEARCH,
                "What is Java concurrency?",
                context);

        assertEquals(ExecutionStatus.SUCCESS, resumed.status());
        assertEquals("search-results", resumed.output());

        // 4. Approval status should be APPROVED
        assertEquals(ApprovalStatus.APPROVED,
                approvalIntegration.getApprovalStatus(requestId));
    }
}
