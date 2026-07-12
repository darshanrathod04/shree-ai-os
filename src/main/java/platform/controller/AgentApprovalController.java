package platform.controller;

import platform.approval.ApprovalService;
import platform.dto.ApprovalRequest;
import platform.dto.ApprovalResponse;
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
