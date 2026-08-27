package com.shreeai.os.platform.kernels.multiagent.api;

import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;

import java.util.List;
import java.util.Map;

/**
 * Constitutional task orchestration contract.
 *
 * Chief Kernel delegates objectives here.
 */
public interface AgentOrchestrator {

    List<AgentResponse> orchestrate(
            String objective,
            Map<String, Object> context
    );

}