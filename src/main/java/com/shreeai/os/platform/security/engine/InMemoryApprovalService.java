package com.shreeai.os.platform.security.engine;

import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;

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
        return Optional.ofNullable(
                requests.get(requestId)
        );
    }
}