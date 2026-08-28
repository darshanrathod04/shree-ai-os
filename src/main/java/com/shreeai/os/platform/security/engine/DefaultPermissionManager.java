package com.shreeai.os.platform.security.engine;

import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;

import java.util.Set;

/**
 * Constitutional Permission Engine.
 *
 * Rules:
 * ALLOW    -> Safe read operations
 * ASK_USER -> Mutating operations
 * DENY     -> Dangerous operations
 */
public final class DefaultPermissionManager implements PermissionManager {

    private static final Set<String> DANGEROUS_TERMINAL = Set.of(
            "rm",
            "rmdir",
            "del",
            "format",
            "shutdown",
            "reboot",
            "mkfs"
    );

    @Override
    public PermissionDecision check(PermissionRequest request) {

        String tool = request.toolId();
        String op = request.operation();

        if ("echo".equals(tool)) {
            return PermissionDecision.ALLOW;
        }

        if ("browser".equals(tool)) {
            return PermissionDecision.ALLOW;
        }

        if ("filesystem".equals(tool)) {

            if ("read".equals(op) || "exists".equals(op)) {
                return PermissionDecision.ALLOW;
            }

            if ("write".equals(op)) {
                return PermissionDecision.ASK_USER;
            }
        }

        if ("git".equals(tool)) {
            return PermissionDecision.ASK_USER;
        }

        if ("terminal".equals(tool)) {

            if (DANGEROUS_TERMINAL.contains(op)) {
                return PermissionDecision.DENY;
            }

            return PermissionDecision.ASK_USER;
        }

        return PermissionDecision.DENY;
    }
}