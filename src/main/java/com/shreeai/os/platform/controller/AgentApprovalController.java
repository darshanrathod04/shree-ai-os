package com.shreeai.os.platform.controller;

import com.shreeai.os.platform.approval.ApprovalService;
import com.shreeai.os.platform.dto.ApprovalRequest;
import com.shreeai.os.platform.dto.ApprovalResponse;
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
