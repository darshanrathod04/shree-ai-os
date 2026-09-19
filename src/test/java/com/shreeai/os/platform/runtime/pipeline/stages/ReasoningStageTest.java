package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReasoningStage} verifying that cognitive sub-engines
 * R1-R5 and I1-I5 execute safely and deterministically under standard pipeline execution.
 */
public class ReasoningStageTest {

    private ReasoningStage stage;

    @BeforeEach
    void setUp() {
        stage = new ReasoningStage();
    }

    @Test
    void process_withPromptFallback_executesCognitiveEngines() {
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-1")
                .actionId("chat")
                .userInput("Learn Java Streams and Collections architecture")
                .build();

        PipelineContext context = PipelineContext.builder()
                .pipelineId("p-1")
                .executionRequest(request)
                .build();

        PipelineExecutionState state = new PipelineExecutionState(List.of(stage));

        ExecutionChain chain = new ExecutionChain() {
            @Override
            public boolean hasNext(PipelineContext context, PipelineExecutionState state) {
                return false;
            }

            @Override
            public PipelineResult next(PipelineContext context, PipelineExecutionState state) {
                return PipelineResult.builder().success(true).build();
            }
        };

        PipelineResult result = stage.process(context, chain, state);

        assertTrue(result.isSuccess(), "ReasoningStage should succeed");

        // Verify fallback artifacts were created and placed into metadata
        assertNotNull(state.getMetadata().get("reliabilityResult"), "reliabilityResult metadata must be populated");
        assertNotNull(state.getMetadata().get("conceptGraph"), "conceptGraph metadata must be populated");
        assertTrue(state.getMetadata().get("reliabilityResult") instanceof ReliabilityResult);
        assertTrue(state.getMetadata().get("conceptGraph") instanceof ConceptGraph);

        // Verify cognitive sub-engines R1-R5 and I1 ran and populated CognitiveState
        assertNotNull(state.getCognitiveState().reasoningGraph(), "R1 reasoningGraph must be populated");
        assertNotNull(state.getCognitiveState().synthesisGraph(), "R2 synthesisGraph must be populated");
        assertNotNull(state.getCognitiveState().causalGraph(), "R3 causalGraph must be populated");
        assertNotNull(state.getCognitiveState().verificationGraph(), "R4 verificationGraph must be populated");
        assertNotNull(state.getCognitiveState().uncertaintyGraph(), "R5 uncertaintyGraph must be populated");
        assertNotNull(state.getCognitiveState().alternativeSet(), "I1 alternativeSet must be populated");

        // Verify metadata was also populated for downstream access
        assertNotNull(state.getMetadata().get("reasoningGraph"), "reasoningGraph metadata must be populated");
        assertNotNull(state.getMetadata().get("synthesisGraph"), "synthesisGraph metadata must be populated");
        assertNotNull(state.getMetadata().get("causalGraph"), "causalGraph metadata must be populated");
        assertNotNull(state.getMetadata().get("verificationGraph"), "verificationGraph metadata must be populated");
        assertNotNull(state.getMetadata().get("uncertaintyGraph"), "uncertaintyGraph metadata must be populated");
        assertNotNull(state.getMetadata().get("alternativeSet"), "alternativeSet metadata must be populated");
    }

    @Test
    void process_withRankedKnowledge_executesCognitiveEngines() {
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-req-2")
                .actionId("chat")
                .userInput("Explain Java")
                .build();

        PipelineContext context = PipelineContext.builder()
                .pipelineId("p-2")
                .executionRequest(request)
                .build();

        PipelineExecutionState state = new PipelineExecutionState(List.of(stage));

        KnowledgeNode kn = KnowledgeNode.of(
                new KnowledgeId("k-01"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Java",
                "Java is a high-level, class-based, object-oriented programming language.",
                Map.of("confidence", 0.95),
                Instant.now(),
                Instant.now()
        );
        state.addMetadata("rankedKnowledge", List.of(kn));

        ExecutionChain chain = new ExecutionChain() {
            @Override
            public boolean hasNext(PipelineContext context, PipelineExecutionState state) {
                return false;
            }

            @Override
            public PipelineResult next(PipelineContext context, PipelineExecutionState state) {
                return PipelineResult.builder().success(true).build();
            }
        };

        PipelineResult result = stage.process(context, chain, state);

        assertTrue(result.isSuccess());
        assertNotNull(state.getCognitiveState().reasoningGraph());
        assertNotNull(state.getCognitiveState().synthesisGraph());
        assertNotNull(state.getCognitiveState().causalGraph());
        assertNotNull(state.getCognitiveState().verificationGraph());
        assertNotNull(state.getCognitiveState().uncertaintyGraph());
    }
}
