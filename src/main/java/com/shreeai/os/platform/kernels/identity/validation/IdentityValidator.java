package com.shreeai.os.platform.kernels.identity.validation;

import com.shreeai.os.platform.kernels.identity.model.IdentityContext;

import java.util.Objects;

public final class IdentityValidator {

    public void validate(IdentityContext context) {

        Objects.requireNonNull(context, "IdentityContext must not be null");

        if (context.identityId() == null) {
            throw new IllegalStateException("IdentityId is required");
        }

        if (context.identityType() == null) {
            throw new IllegalStateException("IdentityType is required");
        }

        if (context.applicationId() == null || context.applicationId().isBlank()) {
            throw new IllegalStateException("ApplicationId is required");
        }

        if (context.workspaceId() == null || context.workspaceId().isBlank()) {
            throw new IllegalStateException("WorkspaceId is required");
        }
    }
}