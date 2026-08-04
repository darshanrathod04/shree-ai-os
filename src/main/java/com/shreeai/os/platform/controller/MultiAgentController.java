package com.shreeai.os.platform.controller;

import com.shreeai.os.platform.orchestrator.AgentOrchestrator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/multi")
public class MultiAgentController {

    private final AgentOrchestrator orchestrator;

    public MultiAgentController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")
    public String runMultiAgent(@RequestBody String task) throws Exception {
        return orchestrator.runTask(task);
    }
}
