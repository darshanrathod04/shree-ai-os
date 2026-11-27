package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.knowledge.evaluation.RagEvaluator;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.tools.engine.AgentCheckpointStore;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.engine.ToolCallingAgent;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMilestone3And4Test {

    @Test
    @DisplayName("ToolCallingAgent pauses on HITL approvalRequiredTools and resumes from checkpoint")
    void testCheckpointPauseAndResume() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        registry.register(new EchoTool());
        DefaultToolExecutor executor = new DefaultToolExecutor(registry);
        AgentCheckpointStore store = new AgentCheckpointStore();

        AtomicInteger turn = new AtomicInteger(0);
        LlmProvider hitlLlm = new LlmProvider() {
            @Override public String providerName() { return "hitl-llm"; }
            @Override public Stream<String> stream(LlmRequest request) {
                if (turn.getAndIncrement() == 0) {
                    return Stream.of("{\"tool\": \"echo\", \"arguments\": {\"message\": \"sensitive-op\"}}");
                }
                return Stream.of("{\"final\": \"Sensitive operation completed after approval.\"}");
            }
        };

        ToolCallingAgent agent = new ToolCallingAgent(hitlLlm, executor, registry, 4);
        ToolCallingAgent.CheckpointResult paused = agent.runOrPause(
                "default", "Run sensitive echo", Set.of("echo"), store);

        assertTrue(paused.pausedForApproval());
        assertNotNull(paused.checkpointId());
        assertEquals(0, paused.result().steps().size());

        ToolCallingAgent.CheckpointResult resumed = agent.resume(paused.checkpointId(), true, store);
        assertFalse(resumed.pausedForApproval());
        assertTrue(resumed.result().completed());
        assertEquals(1, resumed.result().steps().size());
        assertEquals("Sensitive operation completed after approval.", resumed.result().finalAnswer());
    }

    @Test
    @DisplayName("RagEvaluator scores grounded answers high and hallucinated answers low")
    void testRagEvaluatorTriad() {
        RagEvaluator evaluator = new RagEvaluator();
        List<String> contexts = List.of(
                "Shree AI OS combines HNSW vector similarity and PostgreSQL GIN full-text search using Reciprocal Rank Fusion with k=60."
        );

        RagEvaluator.RagEvaluationReport grounded = evaluator.evaluate(
                "How does Shree AI OS combine vector similarity and full-text search?",
                contexts,
                "Shree AI OS combines HNSW vector similarity and PostgreSQL GIN full-text search using Reciprocal Rank Fusion with k=60."
        );
        assertTrue(grounded.passing());
        assertTrue(grounded.faithfulness() >= 0.9);
        assertTrue(grounded.answerRelevance() >= 0.8);

        RagEvaluator.RagEvaluationReport hallucinated = evaluator.evaluate(
                "How does Shree AI OS combine vector similarity and full-text search?",
                contexts,
                "Bananas grow on tropical palm trees in the ocean during winter."
        );
        assertFalse(hallucinated.passing());
        assertEquals(0.0, hallucinated.faithfulness(), 0.001);
    }
}
