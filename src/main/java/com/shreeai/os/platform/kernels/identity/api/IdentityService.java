package com.shreeai.os.platform.kernels.identity.api;

import com.shreeai.os.platform.kernels.identity.model.IdentityContext;

public interface IdentityService {

    IdentityContext resolveIdentity(
            String requestId,
            String sessionId,
            String applicationId,
            String workspaceId
    );
}