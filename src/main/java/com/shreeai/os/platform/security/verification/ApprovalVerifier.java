package com.shreeai.os.platform.security.verification;

import com.shreeai.os.platform.security.engine.InMemoryApprovalService;
import com.shreeai.os.platform.security.model.ApprovalRequest;
import com.shreeai.os.platform.security.model.ApprovalStatus;

import java.util.Map;

public final class ApprovalVerifier {

    public boolean verify() {

        InMemoryApprovalService service =
                new InMemoryApprovalService();

        ApprovalRequest pending =
                service.create(
                        ApprovalRequest.pending(
                                "terminal",
                                "echo",
                                Map.of("command", "echo Hello")
                        )
                );

        ApprovalRequest approved =
                service.approve(pending.requestId());

        return approved.status() == ApprovalStatus.APPROVED
                && service.find(pending.requestId()).isPresent();
    }
}