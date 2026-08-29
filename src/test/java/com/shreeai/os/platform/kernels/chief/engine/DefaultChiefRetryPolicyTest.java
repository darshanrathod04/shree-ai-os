package com.shreeai.os.platform.kernels.chief.engine;

import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.model.RetryPolicy;
import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Chief governance retry policy in
 * {@link DefaultChiefProcessingEngine}.
 */
class DefaultChiefRetryPolicyTest {

    @Test
    void processSucceedsWhenAllAgentsSucceed() {
        CountingOrchestrator orc = new CountingOrchestrator("BUILD", 0);
        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(orc);

        ChiefResponse response = engine.process(request("BUILD"));

        assertTrue(response.success());
        assertEquals(1, orc.calls);
    }

    @Test
    void processFailsWhenAgentFails() {
        CountingOrchestrator orc = new CountingOrchestrator("BUILD", Integer.MAX_VALUE);
        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(orc);

        ChiefResponse response = engine.process(request("BUILD"));

        assertFalse(response.success());
        assertEquals(1, orc.calls);
    }

    @Test
    void retryRecoversAfterTransientFailure() {
        CountingOrchestrator orc = new CountingOrchestrator("BUILD", 2);
        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(orc);

        RetryPolicy policy = new RetryPolicy(3, 0L, true, false, 0.5);
        ChiefResponse response = engine.processWithRetry(request("BUILD"), policy);

        assertTrue(response.success());
        // 1 primary + 2 retries => 3 total calls
        assertEquals(3, orc.calls);
        assertEquals(2, response.metadata().get("retryAttempts"));
        assertEquals(Boolean.FALSE, response.metadata().get("exhausted"));
    }

    @Test
    void retryExhaustsAndReportsFailure() {
        CountingOrchestrator orc = new CountingOrchestrator("BUILD", Integer.MAX_VALUE);
        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(orc);

        RetryPolicy policy = new RetryPolicy(2, 0L, true, false, 0.5);
        ChiefResponse response = engine.processWithRetry(request("BUILD"), policy);

        assertFalse(response.success());
        assertEquals(3, orc.calls); // 1 primary + 2 retries
        assertEquals(2, response.metadata().get("retryAttempts"));
        assertEquals(Boolean.TRUE, response.metadata().get("exhausted"));
    }

    @Test
    void noRetryWhenPolicyDisabled() {
        CountingOrchestrator orc = new CountingOrchestrator("BUILD", Integer.MAX_VALUE);
        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(orc);

        RetryPolicy policy = new RetryPolicy(0, 0L, false, false, 0.5);
        ChiefResponse response = engine.processWithRetry(request("BUILD"), policy);

        assertFalse(response.success());
        assertEquals(1, orc.calls);
        assertEquals(0, response.metadata().get("retryAttempts"));
    }

    @Test
    void nullPolicyThrows() {
        DefaultChiefProcessingEngine engine =
                new DefaultChiefProcessingEngine(new CountingOrchestrator("BUILD", 0));
        assertThrows(NullPointerException.class,
                () -> engine.processWithRetry(request("BUILD"), null));
    }

    private ChiefRequest request(String objective) {
        return new ChiefRequest(
                new ChiefId("chief-1"), "AUTONOMOUS_TASK", null, null,
                Map.of("objective", objective), Map.of());
    }

    /** Orchestrator that fails for the first N calls, then succeeds. */
    private static final class CountingOrchestrator implements AgentOrchestrator {
        private final String agentId;
        private final int failForFirstNCalls;
        private int calls = 0;

        CountingOrchestrator(String agentId, int failForFirstNCalls) {
            this.agentId = agentId;
            this.failForFirstNCalls = failForFirstNCalls;
        }

        @Override
        public List<AgentResponse> orchestrate(
                String objective, Map<String, Object> context) {
            calls++;
            boolean success = calls > failForFirstNCalls;
            return List.of(new AgentResponse(
                    success, success ? "done" : "failed", agentId, Map.of()));
        }

        @Override
        public ParallelOrchestrationResult parallelOrchestrate(
                String objective,
                Map<String, Object> context,
                ParallelExecutionPolicy policy) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
