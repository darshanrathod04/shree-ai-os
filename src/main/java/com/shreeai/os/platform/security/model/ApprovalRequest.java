package com.shreeai.os.platform.security.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ApprovalRequest(

        String requestId,
        String toolId,
        String operation,
        Map<String, Object> metadata,
        ApprovalStatus status,
        Instant createdAt

) {

    public static ApprovalRequest pending(
            String toolId,
            String operation,
            Map<String, Object> metadata
    ) {
        return new ApprovalRequest(
                UUID.randomUUID().toString(),
                toolId,
                operation,
                metadata == null ? Map.of() : Map.copyOf(metadata),
                ApprovalStatus.PENDING,
                Instant.now()
        );
    }
}