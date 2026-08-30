package com.shreeai.os.platform.kernels.multiagent.engine;

import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.model.AgentCapability;
import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentDescriptor;
import com.shreeai.os.platform.kernels.multiagent.model.AgentRequest;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.MultiAgentMetrics;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for parallel multi-agent orchestration in
 * {@link DefaultAgentOrchestrator}.
 */
class DefaultAgentOrchestratorParallelTest {

    private final FakeMultiAgentService multiAgentService = new FakeMultiAgentService();
    private final DefaultAgentOrchestrator orchestrator =
            new DefaultAgentOrchestrator(multiAgentService);

    @Test
    void parallelOrchestrateRunsAllAgents() {
        multiAgentService.agents = List.of(
                agent("planner"), agent("researcher"), agent("coder"));

        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "Build a feature", Map.of("memory", List.of("m1")),
                ParallelExecutionPolicy.defaults());

        assertEquals(3, result.totalAgents());
        assertEquals(3, result.responses().size());
        assertEquals(3, result.succeededAgents());
        assertEquals(0, result.failedAgents());
        assertTrue(result.allSucceeded());
        assertEquals("Build a feature", result.objective());
    }

    @Test
    void parallelOrchestrateCountsFailures() {
        multiAgentService.agents = List.of(agent("planner"), agent("coder"));
        multiAgentService.failAgentId = "coder";

        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "Deliver", Map.of(), ParallelExecutionPolicy.defaults());

        assertEquals(1, result.succeededAgents());
        assertEquals(1, result.failedAgents());
        assertFalse(result.allSucceeded());
    }

    @Test
    void nullPolicyThrows() {
        assertThrows(NullPointerException.class, () ->
                orchestrator.parallelOrchestrate("x", Map.of(), null));
    }

    @Test
    void boundedConcurrencyStillCompletes() {
        multiAgentService.agents = List.of(agent("a"), agent("b"), agent("c"), agent("d"));

        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "x", Map.of(), new ParallelExecutionPolicy(2, 5000L, true));

        assertEquals(4, result.responses().size());
        assertEquals(4, result.succeededAgents());
    }

    @Test
    void metadataCarriesPolicyInfo() {
        multiAgentService.agents = List.of(agent("a"));

        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "x", Map.of(), new ParallelExecutionPolicy(1, 1000L, false));

        assertTrue(result.metadata().containsKey("policy"));
        assertTrue(result.metadata().containsKey("failFast"));
    }

    @Test
    void noAgentsProducesEmptyResult() {
        multiAgentService.agents = List.of();

        ParallelOrchestrationResult result = orchestrator.parallelOrchestrate(
                "x", Map.of(), ParallelExecutionPolicy.defaults());

        assertEquals(0, result.totalAgents());
        assertEquals(0, result.responses().size());
        assertTrue(result.allSucceeded());
    }

    private AgentDescriptor agent(String id) {
        return new AgentDescriptor(
                id, "WORKER", List.of(new AgentCapability("planning", "1.0", Map.of())),
                "NORMAL", List.of(), Map.of());
    }

    private static final class FakeMultiAgentService implements MultiAgentService {
        List<AgentDescriptor> agents = List.of();
        String failAgentId;

        @Override
        public AgentResponse registerAgent(AgentRequest request) {
            return success(request.agentId());
        }

        @Override
        public AgentResponse unregisterAgent(String agentId) {
            return success(agentId);
        }

        @Override
        public List<AgentDescriptor> discoverAgents(AgentRequest criteria) {
            return agents;
        }

        @Override
        public AgentResponse communicate(AgentCommunication communication) {
            boolean fail = failAgentId != null && failAgentId.equals(communication.receiverId());
            return new AgentResponse(!fail, fail ? "failed" : "done",
                    communication.receiverId(), Map.of());
        }

        @Override
        public MultiAgentMetrics getKernelHealth() {
            return new MultiAgentMetrics(0, 0, 0, java.time.Instant.now(), Map.of());
        }

        private AgentResponse success(String id) {
            return new AgentResponse(true, "ok", id, Map.of());
        }
    }
}
