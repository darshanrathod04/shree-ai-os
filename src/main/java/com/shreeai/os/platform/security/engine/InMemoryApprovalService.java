package com.shreeai.os.platform.security.engine;

import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;
import com.shreeai.os.platform.security.model.ApprovalStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryApprovalService
        implements ApprovalService {

    private final Map<String, ApprovalRequest> requests =
            new ConcurrentHashMap<>();

    @Override
    public ApprovalRequest create(ApprovalRequest request) {
        requests.put(request.requestId(), request);
        return request;
    }

    @Override
    public Optional<ApprovalRequest> find(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    @Override
    public ApprovalRequest approve(String requestId) {

        ApprovalRequest existing = requests.get(requestId);

        if (existing == null) {
            throw new IllegalArgumentException("Approval not found");
        }

        ApprovalRequest updated = new ApprovalRequest(
                existing.requestId(),
                existing.toolId(),
                existing.operation(),
                existing.metadata(),
                ApprovalStatus.APPROVED,
                Instant.now()
        );

        requests.put(requestId, updated);

        return updated;
    }

    @Override
    public ApprovalRequest deny(String requestId) {

        ApprovalRequest existing = requests.get(requestId);

        if (existing == null) {
            throw new IllegalArgumentException("Approval not found");
        }

        ApprovalRequest updated = new ApprovalRequest(
                existing.requestId(),
                existing.toolId(),
                existing.operation(),
                existing.metadata(),
                ApprovalStatus.DENIED,
                Instant.now()
        );

        requests.put(requestId, updated);

        return updated;
    }
}