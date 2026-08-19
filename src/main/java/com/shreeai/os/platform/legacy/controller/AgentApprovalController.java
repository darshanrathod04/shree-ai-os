package com.shreeai.os.platform.legacy.controller;

import com.shreeai.os.platform.legacy.approval.ApprovalService;
import com.shreeai.os.platform.legacy.dto.ApprovalRequest;
import com.shreeai.os.platform.legacy.dto.ApprovalResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentApprovalController {

    private final ApprovalService approvalService;



    public AgentApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/approve")
    public ApprovalResponse approveAction(@RequestBody ApprovalRequest request) {

        String result = approvalService.handleApproval(request.isApproved());

        return new ApprovalResponse(
                request.isApproved() ? "APPROVED" : "REJECTED",
                result
        );
    }



}
