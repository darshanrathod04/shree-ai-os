package com.shreeai.os.platform.security.api;

import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;

/**
 * Constitutional permission contract.
 */
public interface PermissionManager {

    PermissionDecision check(
            PermissionRequest request
    );

}