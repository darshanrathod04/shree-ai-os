package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;
import com.shreeai.os.platform.security.model.ApprovalStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Bridges the {@link ExecutionDispatcher}'s {@code REQUIRE_APPROVAL}
 * permission decisions to a concrete {@link ApprovalService} (typically
 * {@code InMemoryApprovalService}).
 *
 * <p>When the dispatcher returns a {@link RichExecutionResult} with
 * {@link ExecutionStatus#PENDING_APPROVAL}, this integration creates a
 * corresponding {@link ApprovalRequest} so that an external approver (human
 * or autonomous) can review and act. Once approved, the dispatcher can be
 * re-invoked with the original parameters.
 *
 * @since 2.1
 */
public final class ApprovalIntegration {

        private final ExecutionDispatcher dispatcher;
    private final ApprovalService approvalService;
    private final DefaultPermissionPolicy permissionPolicy;

    public ApprovalIntegration(ExecutionDispatcher dispatcher,
                               ApprovalService approvalService) {
        this(dispatcher, approvalService, null);
    }

    public ApprovalIntegration(ExecutionDispatcher dispatcher,
                               ApprovalService approvalService,
                               DefaultPermissionPolicy permissionPolicy) {
        this.dispatcher = Objects.requireNonNull(
                dispatcher, "dispatcher must not be null");
        this.approvalService = Objects.requireNonNull(
                approvalService, "approvalService must not be null");
        this.permissionPolicy = permissionPolicy; // may be null for tests with ALLOW-only
    }

    /**
     * Executes a capability task, creating an approval request when the
     * permission policy requires it.
     *
     * <p>If the permission policy returns {@code ALLOW}, the task executes
     * immediately. If {@code REQUIRE_APPROVAL} is returned, an
     * {@link ApprovalRequest} is created and a pending result is returned.
     *
     * @param capability the capability to dispatch
     * @param input      the request payload
     * @param context    the execution context
     * @return the execution result
     */
    public RichExecutionResult executeTask(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {

        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(context, "context must not be null");

        // Attempt dispatch; the dispatcher will return PENDING_APPROVAL
        // when the permission policy requires approval
        RichExecutionResult result = dispatcher.dispatch(capability, input, context);

        if (result.status() == ExecutionStatus.PENDING_APPROVAL) {
            // Create an approval request for this pending task
            ApprovalRequest approvalRequest = ApprovalRequest.pending(
                    "capability:" + capability.value(),
                    capability.value(),
                    Map.of(
                            "input", input != null ? input : "",
                            "context", context,
                            "reason", result.output()
                    )
            );

            approvalService.create(approvalRequest);

            // Attach the approval request ID to the result metadata
            Map<String, Object> enrichedMetadata = new java.util.HashMap<>(result.metadata());
            enrichedMetadata.put("approvalRequestId", approvalRequest.requestId());
            enrichedMetadata.put("approvalStatus", ApprovalStatus.PENDING.name());

            return RichExecutionResult.builder()
                    .capability(capability)
                    .status(ExecutionStatus.PENDING_APPROVAL)
                    .output(result.output())
                    .metadata(enrichedMetadata)
                    .build();
        }

        return result;
    }

    /**
     * Retrieves the approval status for a given approval request ID.
     *
     * @param approvalRequestId the approval request ID
     * @return the {@link ApprovalStatus} or {@code null} if not found
     */
    public ApprovalStatus getApprovalStatus(String approvalRequestId) {
        Objects.requireNonNull(approvalRequestId, "approvalRequestId must not be null");
        return approvalService.find(approvalRequestId)
                .map(ApprovalRequest::status)
                .orElse(null);
    }

    /**
     * Approves a pending approval request and re-dispatches the original
     * capability task.
     *
     * @param approvalRequestId the approval request ID
     * @param capability        the original capability
     * @param input             the original input
     * @param context           the original context
     * @return the result of re-dispatching after approval
     * @throws IllegalArgumentException if the approval request is not found
     */
    public RichExecutionResult approveAndResume(
            String approvalRequestId,
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {

        Objects.requireNonNull(approvalRequestId, "approvalRequestId must not be null");
        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(context, "context must not be null");

        ApprovalRequest updated = approvalService.approve(approvalRequestId);

        if (updated.status() != ApprovalStatus.APPROVED) {
            return RichExecutionResult.denied(
                    capability,
                    "Approval was not granted for request: " + approvalRequestId);
        }

                // Re-dispatch the original task now that it's approved.
        // Temporarily bypass the permission policy check since approval
        // has already been explicitly granted.
        if (permissionPolicy != null) {
            PermissionDecision previous = permissionPolicy.evaluate(capability);
            try {
                permissionPolicy.set(capability, PermissionDecision.ALLOW);
                return dispatcher.dispatch(capability, input, context);
            } finally {
                permissionPolicy.set(capability, previous);
            }
        }

        return dispatcher.dispatch(capability, input, context);
    }

    /**
     * Denies a pending approval request.
     *
     * @param approvalRequestId the approval request ID
     * @return the denied execution result
     * @throws IllegalArgumentException if the approval request is not found
     */
    public RichExecutionResult deny(String approvalRequestId,
                                    ExecutionCapability capability) {
        Objects.requireNonNull(approvalRequestId, "approvalRequestId must not be null");
        Objects.requireNonNull(capability, "capability must not be null");

        approvalService.deny(approvalRequestId);

        return RichExecutionResult.denied(
                capability,
                "Execution denied: approval request " + approvalRequestId + " was denied");
    }
}
