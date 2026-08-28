package com.shreeai.os.platform.security.model;

import java.util.Map;

public record PermissionRequest(

        String toolId,
        String operation,
        Map<String, Object> metadata

) {

    public PermissionRequest {
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

}