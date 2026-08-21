package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;
import com.shreeai.os.platform.kernels.identity.engine.DefaultIdentityProcessingEngine;
import com.shreeai.os.platform.kernels.identity.service.DefaultIdentityService;
import com.shreeai.os.platform.kernels.identity.validation.IdentityValidator;

import java.util.Objects;

/**
 * IdentityStage
 *
 * Runtime orchestration only.
 * All identity resolution is delegated to the Identity Kernel.
 */
public final class IdentityStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR =
            PipelineStageDescriptor.builder()
                    .stageName("Identity")
                    .priority(1)
                    .enabled(true)
                    .version("4.0")
                    .description("Delegates identity resolution to Identity Kernel")
                    .build();

    private final IdentityService identityService;


    /**
     * Runtime constructor (Dependency Injection)
     */
    public IdentityStage(IdentityService identityService) {
        this.identityService = Objects.requireNonNull(
                identityService,
                "IdentityService must not be null"
        );
    }

    /**
     * Backward-compatible constructor for tests.
     * Delegates to the canonical Identity Kernel.
     */
    public IdentityStage() {
        this(
                new DefaultIdentityService(
                        new DefaultIdentityProcessingEngine(
                                new IdentityValidator()
                        )
                )
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
                    context.getExecutionRequest() != null
                            && context.getExecutionRequest().getSession() != null
                            ? context.getExecutionRequest()
                            .getSession()
                            .getSessionId()
                            .toString()
                            : "RUNTIME";

            // ==========================================================
            // Delegate to Identity Kernel
            // ==========================================================

            IdentityContext identity =
                    identityService.resolveIdentity(
                            requestId,
                            sessionId,
                            "SHREE_RUNTIME",
                            "DEFAULT"
                    );

            // ==========================================================
            // Propagate canonical identity
            // ==========================================================

            PipelineContext updatedContext =
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

            return chain.next(updatedContext, state);

        } catch (Exception e) {

            state.markFailure(
                    "Identity resolution failed: " + e.getMessage()
            );

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