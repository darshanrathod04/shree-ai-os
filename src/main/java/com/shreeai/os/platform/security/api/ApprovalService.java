package com.shreeai.os.platform.security.api;

import com.shreeai.os.platform.security.model.ApprovalRequest;

import java.util.Optional;

public interface ApprovalService {

    ApprovalRequest create(ApprovalRequest request);

    Optional<ApprovalRequest> find(String requestId);

    ApprovalRequest approve(String requestId);

    ApprovalRequest deny(String requestId);

}