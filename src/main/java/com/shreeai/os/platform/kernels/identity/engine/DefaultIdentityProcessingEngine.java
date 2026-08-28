package com.shreeai.os.platform.kernels.identity.engine;

import com.shreeai.os.platform.kernels.identity.api.IdentityType;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.identity.model.IdentityId;
import com.shreeai.os.platform.kernels.identity.validation.IdentityValidator;

import java.time.Instant;
import java.util.Objects;

public final class DefaultIdentityProcessingEngine {

    private final IdentityValidator validator;

    public DefaultIdentityProcessingEngine(IdentityValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    public IdentityContext resolve(
            String requestId,
            String sessionId,
            String applicationId,
            String workspaceId
    ) {

        IdentityContext context =
                IdentityContext.builder()
                        .identityId(new IdentityId("agent-" + requestId))
                        .identityType(IdentityType.AGENT)
                        .sessionId(sessionId)
                        .applicationId(applicationId)
                        .workspaceId(workspaceId)
                        .authenticated(true)
                        .resolvedAt(Instant.now())
                        .build();

        validator.validate(context);

        return context;
    }
}