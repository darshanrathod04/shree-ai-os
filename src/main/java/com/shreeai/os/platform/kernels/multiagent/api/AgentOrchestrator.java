package com.shreeai.os.platform.kernels.multiagent.api;

import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;

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

    /**
     * Orchestrates multiple agents concurrently against a shared objective.
     *
     * <p>Agents are dispatched in parallel subject to the constraints defined
     * by the supplied {@link ParallelExecutionPolicy} (concurrency cap,
     * per-agent timeout, and fail-fast behavior).</p>
     *
     * @param objective the high-level objective being delegated
     * @param context   the shared orchestration context (may be {@code null})
     * @param policy    the parallel execution policy (must not be {@code null})
     * @return an aggregated parallel orchestration result
     */
    ParallelOrchestrationResult parallelOrchestrate(
            String objective,
            Map<String, Object> context,
            ParallelExecutionPolicy policy
    );

}
