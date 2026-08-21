package com.shreeai.os.platform.kernels.identity.service;

import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.identity.engine.DefaultIdentityProcessingEngine;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.identity.validation.IdentityValidator;

import java.util.Objects;

public final class DefaultIdentityService implements IdentityService {

    private final DefaultIdentityProcessingEngine processingEngine;

    public DefaultIdentityService() {
        this.processingEngine =
                new DefaultIdentityProcessingEngine(
                        new IdentityValidator()
                );
    }

    public DefaultIdentityService(
            DefaultIdentityProcessingEngine processingEngine
    ) {
        this.processingEngine = Objects.requireNonNull(processingEngine);
    }

    @Override
    public IdentityContext resolveIdentity(
            String requestId,
            String sessionId,
            String applicationId,
            String workspaceId
    ) {
        return processingEngine.resolve(
                requestId,
                sessionId,
                applicationId,
                workspaceId
        );
    }
}