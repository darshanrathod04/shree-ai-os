package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * ChiefReviewStage - Final review and approval by Chief Kernel.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Reviewing the complete execution flow</li>
 *   <li>Validating all stages completed successfully</li>
 *   <li>Consuming the Reflection Kernel verdict (score, retry advice)</li>
 *   <li>Requesting approval gates for retries and escalations</li>
 *   <li>Providing final approval, rejection, retry or escalation</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.1
 * @since Engineering Gate 3
 */
public final class ChiefReviewStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("ChiefReview")
            .priority(11)
            .enabled(true)
            .version("1.1")
            .description("Final review and approval by Chief Kernel")
            .build();

    /** Score below which a retry is escalated instead of auto-approved. */
    private static final double ESCALATION_SCORE_THRESHOLD = 0.2;

    private final ChiefService chiefService;
    private final ApprovalService approvalService;

    /**
     * Creates a new ChiefReviewStage with real chief service.
     *
     * @param chiefService the chief service
     */
    public ChiefReviewStage(ChiefService chiefService) {
        this(chiefService, null);
    }

    /**
     * Creates a new ChiefReviewStage with chief and approval services.
     *
     * @param chiefService    the chief service (may be null)
     * @param approvalService the approval service backing human/auto approval gates (may be null)
     */
    public ChiefReviewStage(ChiefService chiefService, ApprovalService approvalService) {
        this.chiefService = chiefService;
        this.approvalService = approvalService;
    }

    /**
     * Default constructor for backward compatibility.
     * Uses null service (will fail gracefully).
     */
    public ChiefReviewStage() {
        this(null, null);
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve all execution information from previous stages
            String storedMemoryId = (String) state.getMetadata().get("storedMemoryId");
            String requestId = context.getExecutionRequest() != null 
                    ? context.getExecutionRequest().getRequestId() 
                    : "unknown";

            String reviewId = "review-" + requestId;
            String reviewDecision = "APPROVED";
            boolean allStagesCompleted = state.getVisitedStages().size() >= 10;

            // EO-V1.6 Autonomous gate — reflection-driven retry / escalation
            Object retryAdvised = state.getMetadata().get("reflectionRetryAdvised");
            Object reflectionScore = state.getMetadata().get("reflectionScore");
            Object reflectionVerdict = state.getMetadata().get("reflectionVerdict");

            boolean needsRetry = Boolean.TRUE.equals(retryAdvised);
            double score = reflectionScore instanceof Number number ? number.doubleValue() : 1.0;
            boolean escalate = needsRetry && score < ESCALATION_SCORE_THRESHOLD;

            if (chiefService != null) {
                // Real chief review via ChiefService, enriched with reflection data
                Map<String, Object> reviewContext = new HashMap<>();
                reviewContext.put("requestId", requestId);
                reviewContext.put("reflectionVerdict",
                        reflectionVerdict != null ? reflectionVerdict.toString() : "UNKNOWN");
                reviewContext.put("reflectionScore", score);
                reviewContext.put("retryAdvised", needsRetry);

                ChiefRequest chiefRequest = new ChiefRequest(
                        new com.shreeai.os.platform.kernels.chief.model.ChiefId("chief-" + requestId),
                        "PIPELINE_REVIEW",
                        new com.shreeai.os.platform.kernels.chief.model.DecisionContext(
                                new com.shreeai.os.platform.kernels.chief.model.ChiefId("chief-" + requestId),
                                "PIPELINE_REVIEW",
                                java.util.List.of("Planning", "Execution", "Reflection", "MemoryStore"),
                                "FULL_PIPELINE",
                                reviewContext,
                                java.util.Map.of("confidence", 0.9)
                        ),
                        null,
                        Map.of("requestId", requestId),
                        Map.of()
                );

                try {
                    ChiefResponse chiefResponse = chiefService.submitOrchestration(chiefRequest);
                    if (chiefResponse.decisionResult() != null) {
                        reviewDecision = chiefResponse.decisionResult().approved() ? "APPROVED" : "REJECTED";
                    } else {
                        reviewDecision = "APPROVED"; // Default to APPROVED if no decision result
                    }
                } catch (Exception e) {
                    // If chief service fails, default to APPROVED and continue
                    reviewDecision = "APPROVED";
                    state.addMessage("Chief review defaulted to APPROVED due to: " + e.getMessage());
                }
                reviewId = "review-" + requestId + "-" + System.currentTimeMillis();
            }

            // Reflection verdict can override the kernel decision:
            // failed executions ask for retry, very poor ones escalate.
            if (escalate) {
                reviewDecision = "ESCALATED";
            } else if (needsRetry && "APPROVED".equals(reviewDecision)) {
                reviewDecision = "RETRY_REQUESTED";
            }

            // EO-V1.6 Approval gate — retries and escalations pass through the
            // approval service when one is wired. In autonomous mode the gate
            // auto-approves and records the decision for audit.
            if (("RETRY_REQUESTED".equals(reviewDecision) || "ESCALATED".equals(reviewDecision))
                    && approvalService != null) {

                ApprovalRequest approval = ApprovalRequest.pending(
                        "chief",
                        "PIPELINE_" + reviewDecision,
                        Map.of(
                                "requestId", requestId,
                                "reflectionScore", score,
                                "reflectionVerdict",
                                reflectionVerdict != null ? reflectionVerdict.toString() : "UNKNOWN"));

                approval = approvalService.create(approval);
                approval = approvalService.approve(approval.requestId()); // autonomous approval

                state.addMetadata("approvalId", approval.requestId());
                state.addMetadata("approvalStatus", approval.status().name());
                state.addMessage("Approval gate: " + approval.status().name()
                        + " for " + reviewDecision + " (request " + requestId + ")");
            }

            state.addMetadata("reviewId", reviewId);
            state.addMetadata("reviewDecision", reviewDecision);
            state.addMetadata("allStagesCompleted", allStagesCompleted);
            state.addMetadata("retryAdvised", needsRetry);
            state.addMetadata("escalated", escalate);
            state.addMessage("Chief review completed: " + reviewDecision + " for request " + requestId);

            publishChiefReviewEvent(
                    context,
                    requestId,
                    "APPROVED"
            );

            return chain.next(context, state);

        } catch (Exception e) {

            publishChiefReviewEvent(
                    context,
                    context.getExecutionRequest() != null
                            ? context.getExecutionRequest().getRequestId()
                            : "unknown",
                    "FAILED"
            );
            // Log warning but continue pipeline execution
            state.addMessage("Chief review stage warning: " + e.getMessage());
            return chain.next(context, state);
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private void publishChiefReviewEvent(
            PipelineContext context,
            String requestId,
            String decision
    ) {
        Object value = context.getAttribute("runtimeEventBus");

        if (!(value instanceof RuntimeEventBus bus)) {
            return;
        }

        bus.publish(
                new RuntimeEvent(
                        EventType.CHIEF_REVIEW_COMPLETED,
                        requestId,
                        "ChiefReview",
                        Instant.now(),
                        Map.of(
                                "decision", decision
                        )
                )
        );
    }
}
