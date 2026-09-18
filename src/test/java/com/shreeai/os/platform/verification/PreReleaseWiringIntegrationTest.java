package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.chief.engine.DefaultChiefProcessingEngine;
import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.model.RetryPolicy;
import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;
import com.shreeai.os.platform.kernels.planning.model.ExecutablePlanningGraph;
import com.shreeai.os.platform.kernels.planning.model.ExecutionNode;
import com.shreeai.os.platform.kernels.planning.model.ExecutionState;
import com.shreeai.os.platform.runtime.agents.EvidenceAgent;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.EvidenceItem;
import com.shreeai.os.platform.runtime.model.EvidenceItem.SourceType;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.stages.ActionExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>PreReleaseWiringIntegrationTest</b>
 *
 * <p>Validates the final pre-release wiring consolidation:
 * 1. ActionExecutionStage executes real task nodes from ExecutablePlanningGraph.
 * 2. ContextStage extracts project context and EvidenceAgent grounds [PROJECT] evidence.
 * 3. DefaultChiefProcessingEngine safely falls back without crashing when MultiAgent bridge is dormant.
 * </p>
 */
public class PreReleaseWiringIntegrationTest {


    private static ExecutionChain createNoOpChain() {
        return new ExecutionChain() {
            @Override
            public boolean hasNext(PipelineContext context, PipelineExecutionState state) {
                return false;
            }

            @Override
            public PipelineResult next(PipelineContext context, PipelineExecutionState state) {
                return PipelineResult.builder().success(true).build();
            }
        };
    }

    @Test
    @DisplayName("ActionExecutionStage executes all resolved planning graph nodes and records rich metrics")
    void testActionExecutionStageWithPlanningGraph() {
        String id1 = "a".repeat(64);
        String id2 = "b".repeat(64);
        String id3 = "c".repeat(64);

        ExecutionNode node1 = new ExecutionNode(id1, "task-1", "Compile Code", 1, ExecutionState.READY);
        ExecutionNode node2 = new ExecutionNode(id2, "task-2", "Run Unit Tests", 2, ExecutionState.READY);
        ExecutionNode node3 = new ExecutionNode(id3, "task-3", "Deploy Artifact", 3, ExecutionState.READY);

        ExecutablePlanningGraph graph = ExecutablePlanningGraph.of(List.of(node1, node2, node3), List.of());

        CognitiveState cognitiveState = CognitiveState.empty().withExecutablePlanningGraph(graph);
        ActionExecutionStage stage = new ActionExecutionStage();
        PipelineExecutionState state = new PipelineExecutionState(List.of(stage));
        state.setCognitiveState(cognitiveState);
        state.addMetadata("planId", "plan-test-123");

        PipelineContext context = PipelineContext.builder()
                .pipelineId("pipe-1")
                .build();

        ExecutionChain noOpChain = createNoOpChain();
        PipelineResult result = stage.process(context, noOpChain, state);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        // Verify metadata reflects all executed nodes and counts
        assertEquals(3, state.getMetadata().get("executedTaskCount"));
        assertEquals("COMPLETED", state.getMetadata().get("executionStatus"));
        assertEquals(Boolean.TRUE, state.getMetadata().get("executionCompleted"));

        Object execIds = state.getMetadata().get("executionIds");
        assertInstanceOf(List.class, execIds);
        List<?> execList = (List<?>) execIds;
        assertEquals(3, execList.size());

        Object execNodes = state.getMetadata().get("executedNodes");
        assertInstanceOf(List.class, execNodes);
        List<?> nodeTitles = (List<?>) execNodes;
        assertEquals(List.of("Compile Code", "Run Unit Tests", "Deploy Artifact"), nodeTitles);

        // Verify primary executionId is also present for backward compatibility
        assertNotNull(state.getMetadata().get("executionId"));
    }

    @Test
    @DisplayName("ContextStage extracts projectSummary and EvidenceAgent grounds [PROJECT] evidence")
    void testProjectIntelligenceMetadataWireUp() {
        ContextStage contextStage = new ContextStage();

        Map<String, Object> projectMap = Map.of(
                "projectName", "shree-core-platform",
                "framework", "SPRING_BOOT",
                "buildSystem", "MAVEN",
                "summary", "Core distributed cognitive runtime with 150 components."
        );

        PipelineContext context = PipelineContext.builder()
                .pipelineId("pipe-proj-1")
                .addAttribute("requestMetadata", Map.of("projectSummary", projectMap))
                .build();

        PipelineExecutionState state = new PipelineExecutionState(List.of(contextStage));
        ExecutionChain noOpChain = createNoOpChain();

        PipelineResult result = contextStage.process(context, noOpChain, state);
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // Verify ContextStage populated projectSummary in metadata
        assertNotNull(state.getMetadata().get("projectSummary"));
        assertEquals("shree-core-platform", state.getMetadata().get("projectName"));

        // Verify EvidenceAgent extracts [PROJECT] evidence
        EvidenceAgent evidenceAgent = new EvidenceAgent();
        EvidenceBundle bundle = evidenceAgent.extractFromPipelineState(state);

        assertNotNull(bundle);
        assertFalse(bundle.isEmpty());

        boolean foundProjectEvidence = false;
        for (EvidenceItem item : bundle.items()) {
            if (item.sourceType() == SourceType.PROJECT) {
                foundProjectEvidence = true;
                assertEquals("shree-core-platform", item.title());
                assertTrue(item.content().contains("Core distributed cognitive runtime"));
                break;
            }
        }
        assertTrue(foundProjectEvidence, "EvidenceBundle must contain [PROJECT] evidence item");
    }

    @Test
    @DisplayName("DefaultChiefProcessingEngine safely handles dormant MultiAgent bridge without crashing")
    void testDefaultChiefProcessingEngineDormantMultiAgent() {
        // AgentOrchestrator that throws exception simulating dormant or failed bridge
        AgentOrchestrator dormantOrchestrator = new AgentOrchestrator() {
            @Override
            public List<AgentResponse> orchestrate(String objective, Map<String, Object> context) {
                throw new IllegalStateException("MultiAgent bridge is dormant or unconfigured");
            }

            @Override
            public ParallelOrchestrationResult parallelOrchestrate(String objective, Map<String, Object> context, ParallelExecutionPolicy policy) {
                throw new IllegalStateException("MultiAgent parallel bridge is dormant");
            }
        };

        DefaultChiefProcessingEngine engine = new DefaultChiefProcessingEngine(dormantOrchestrator);

        ChiefRequest request = new ChiefRequest(
                new ChiefId("chief-1"),
                "AUTONOMOUS_TASK",
                null,
                null,
                Map.of("objective", "System Diagnostics"),
                Map.of("tenant", "default")
        );

        // 1. process() must not throw and report graceful outcome
        ChiefResponse response = assertDoesNotThrow(() -> engine.process(request));
        assertNotNull(response);
        assertTrue(response.success(), "Default empty fallback should report success to prevent cascading failures");
        assertEquals("Chief successfully orchestrated agents", response.message());

        // 2. processWithRetry() must not throw when bridge fails
        RetryPolicy retryPolicy = new RetryPolicy(2, 0L, true, false, 0.5);
        ChiefResponse retryResponse = assertDoesNotThrow(() -> engine.processWithRetry(request, retryPolicy));
        assertNotNull(retryResponse);
        assertTrue(retryResponse.success());
    }
}
