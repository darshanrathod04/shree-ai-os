package com.shreeai.os.platform.security.verification;

import com.shreeai.os.platform.security.engine.DefaultPermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;

import java.util.Map;

/**
 * Architectural verification for Permission Kernel.
 */
public final class PermissionVerifier {

    public boolean verify() {

        DefaultPermissionManager manager =
                new DefaultPermissionManager();

        PermissionDecision read =
                manager.check(
                        new PermissionRequest(
                                "filesystem",
                                "read",
                                Map.of()
                        )
                );

        PermissionDecision write =
                manager.check(
                        new PermissionRequest(
                                "filesystem",
                                "write",
                                Map.of()
                        )
                );

        PermissionDecision browser =
                manager.check(
                        new PermissionRequest(
                                "browser",
                                "get",
                                Map.of()
                        )
                );

        return read == PermissionDecision.ALLOW
                && write == PermissionDecision.ASK_USER
                && browser == PermissionDecision.ALLOW;
    }
}