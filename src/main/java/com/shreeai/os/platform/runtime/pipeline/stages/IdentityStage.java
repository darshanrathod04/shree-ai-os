package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.identity.engine.DefaultIdentityProcessingEngine;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.identity.validation.IdentityValidator;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

public final class IdentityStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR =
            PipelineStageDescriptor.builder()
                    .stageName("Identity")
                    .priority(1)
                    .enabled(true)
                    .version("3.0")
                    .description("Canonical Identity Kernel entry point")
                    .build();

    private final DefaultIdentityProcessingEngine processingEngine;

    public IdentityStage() {
        this.processingEngine =
                new DefaultIdentityProcessingEngine(
                        new IdentityValidator()
                );
    }

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
                    processingEngine.resolve(
                            requestId,
                            sessionId,
                            "SHREE_RUNTIME",
                            "DEFAULT"
                    );

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
            state.addMetadata("applicationId", identity.applicationId());
            state.addMetadata("workspaceId", identity.workspaceId());

            state.addMessage(
                    "Identity Kernel resolved → " +
                            identity.identityId().value()
            );

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