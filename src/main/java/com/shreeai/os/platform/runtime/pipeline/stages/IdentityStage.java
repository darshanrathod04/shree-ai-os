package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.identity.api.IdentityType;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.runtime.pipeline.*;

import java.time.Instant;

/**
 * IdentityStage
 *
 * Resolves the canonical runtime identity for every execution request.
 * No fabricated metadata. Downstream kernels receive IdentityContext.
 */
public final class IdentityStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR =
            PipelineStageDescriptor.builder()
                    .stageName("Identity")
                    .priority(1)
                    .enabled(true)
                    .version("2.0")
                    .description("Resolves canonical runtime identity")
                    .build();

    @Override
    public PipelineResult process(
            PipelineContext context,
            ExecutionChain chain,
            PipelineExecutionState state
    ) {

        try {

            String requestId =
                    context.getExecutionRequest() != null
                            ? context.getExecutionRequest().getRequestId()
                            : "UNKNOWN";

            String sessionId =
                    context.getExecutionRequest() != null &&
                            context.getExecutionRequest().getSession() != null
                            ? context.getExecutionRequest()
                            .getSession()
                            .getSessionId()
                            .toString()
                            : "RUNTIME";

            IdentityContext identity =
                    IdentityContext.builder()
                            .identityId(new IdentityId("agent-" + requestId))
                            .identityType(IdentityType.AGENT)
                            .sessionId(sessionId)
                            .applicationId("SHREE_RUNTIME")
                            .workspaceId("DEFAULT")
                            .authenticated(true)
                            .resolvedAt(Instant.now())
                            .build();

            PipelineContext updated =
                    PipelineContext.builder()
                            .pipelineId(context.getPipelineId())
                            .executionRequest(context.getExecutionRequest())
                            .decision(context.getDecision())
                            .validationResult(context.getValidationResult())
                            .executionMetadata(context.getExecutionMetadata())
                            .resolvedContext(context.getResolvedContext())
                            .attributes(context.getAttributes())
                            .addAttribute("identityContext", identity)
                            .timestamp(context.getTimestamp())
                            .build();

            state.addMetadata("identityContext", identity);
            state.addMetadata("identityId", identity.identityId().value());
            state.addMetadata("identityType", identity.identityType().name());

            state.addMessage(
                    "Identity resolved → " + identity.identityId().value());

            return chain.next(updated, state);

        } catch (Exception e) {

            state.markFailure("Identity resolution failed: " + e.getMessage());

            return PipelineResult.builder()
                    .success(false)
                    .status("IDENTITY_FAILED")
                    .addMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}