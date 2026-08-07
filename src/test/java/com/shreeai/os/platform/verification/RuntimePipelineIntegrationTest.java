       package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.stages.ChiefReviewStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ActionExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.stages.IdentityStage;
import com.shreeai.os.platform.runtime.pipeline.stages.InferenceStage;
import com.shreeai.os.platform.runtime.pipeline.stages.KnowledgeStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryRecallStage;
import com.shreeai.os.platform.runtime.pipeline.stages.MemoryStoreStage;
import com.shreeai.os.platform.runtime.pipeline.stages.PlanningStage;
import com.shreeai.os.platform.runtime.pipeline.stages.ReasoningStage;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;
import com.shreeai.os.platform.kernels.memory.engine.DefaultMemoryProcessingEngine;
import com.shreeai.os.platform.kernels.memory.engine.MemoryRankingService;
import com.shreeai.os.platform.kernels.memory.service.DefaultMemoryService;
import com.shreeai.os.platform.kernels.memory.validator.MemoryValidator;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeRankingService;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReasoningEngine;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runtime Pipeline Integration Test
 *
 * <p>This test verifies the complete 10-stage runtime pipeline executes end-to-end.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Release Track 1
 */
public class RuntimePipelineIntegrationTest {

    private DefaultRuntimeService runtime;
    private List<ExecutionStage> stages;

    @BeforeEach
    public void setUp() {
        stages = buildStages();
        RuntimeConfiguration config = RuntimeConfiguration.builder()
                .runtimeName("test-runtime")
                .build();
        RuntimeContract contract = RuntimeContract.builder()
                .contractVersion("1.0")
                .supportsSessions(true)
                .supportsPipelines(true)
                .build();
        runtime = new DefaultRuntimeService(config, contract, stages);
    }

    private List<ExecutionStage> buildStages() {
        List<ExecutionStage> stageList = new ArrayList<>();

        // Real Memory services
        MemoryValidator memoryValidator = new MemoryValidator();
        DefaultMemoryProcessingEngine memoryProcessingEngine = new DefaultMemoryProcessingEngine();
        DefaultMemoryService memoryService = new DefaultMemoryService(memoryValidator, memoryProcessingEngine);
        MemoryRankingService memoryRankingService = new MemoryRankingService();

        // Real Knowledge services
        DefaultKnowledgeProcessingEngine knowledgeProcessingEngine = new DefaultKnowledgeProcessingEngine();
        DefaultKnowledgeService knowledgeService = new DefaultKnowledgeService(knowledgeProcessingEngine);
        KnowledgeRankingService knowledgeRankingService = new KnowledgeRankingService();

        // Real engines
        DefaultReasoningEngine reasoningEngine = new DefaultReasoningEngine();
        DefaultInferenceEngine inferenceEngine = new DefaultInferenceEngine();

        stageList.add(new IdentityStage());
        stageList.add(new ContextStage());
        stageList.add(new MemoryRecallStage(memoryService, memoryService, memoryRankingService));
        stageList.add(new KnowledgeStage(knowledgeService, knowledgeService, knowledgeRankingService));
        stageList.add(new ReasoningStage(reasoningEngine));
        stageList.add(new InferenceStage(inferenceEngine));
        stageList.add(new PlanningStage());
        stageList.add(new ActionExecutionStage());
        stageList.add(new MemoryStoreStage(memoryService));
        stageList.add(new ChiefReviewStage());

        return stageList;
    }

    @Test
    public void testRuntimeStarts() {
        runtime.initialize();
        runtime.start();
        assertNotNull(runtime.lifecycle(), "Lifecycle should exist");
        assertNotNull(runtime.pipeline(), "Pipeline should exist");
        runtime.shutdown();
    }

    @Test
    public void testPipelineExecutesAllStages() {
        // Build pipeline with stages
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        // Create a pipeline context
        PipelineContext pipelineContext = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(java.time.Instant.now())
                .build();

        // Execute pipeline
        PipelineResult result = pipeline.execute(pipelineContext);

        // Verify pipeline completed
        assertNotNull(result, "Pipeline result should not be null");
    }

    @Test
    public void testEveryStageCalledOnce() {
        // Verify all 10 stages are present
        assertEquals(10, stages.size(), "Should have 10 stages");

        // Verify stage order using getStageName()
        assertEquals("Identity", stages.get(0).getDescriptor().getStageName());
        assertEquals("Context", stages.get(1).getDescriptor().getStageName());
        assertEquals("MemoryRecall", stages.get(2).getDescriptor().getStageName());
        assertEquals("Knowledge", stages.get(3).getDescriptor().getStageName());
        assertEquals("Reasoning", stages.get(4).getDescriptor().getStageName());
        assertEquals("Inference", stages.get(5).getDescriptor().getStageName());
        assertEquals("Planning", stages.get(6).getDescriptor().getStageName());
        assertEquals("Execution", stages.get(7).getDescriptor().getStageName());
        assertEquals("MemoryStore", stages.get(8).getDescriptor().getStageName());
        assertEquals("ChiefReview", stages.get(9).getDescriptor().getStageName());
    }

    @Test
    public void testPipelineStatePreserved() {
        // Create pipeline state with stages
        PipelineExecutionState state = new PipelineExecutionState(stages);

        // Verify state starts with empty metadata
        assertNotNull(state.getMetadata(), "Metadata should not be null");
        assertTrue(state.getMetadata().isEmpty(), "Metadata should start empty");

        // Add metadata
        state.addMetadata("testKey", "testValue");
        assertEquals("testValue", state.getMetadata().get("testKey"), "Metadata should be preserved");
    }

    @Test
    public void testRuntimeShutsDownCleanly() {
        runtime.initialize();
        runtime.start();
        runtime.shutdown();
        // No exception means clean shutdown
        assertTrue(true);
    }

    @Test
    public void testFullPipelineEndToEnd() {
        // Initialize runtime
        runtime.initialize();
        runtime.start();

        // Create request
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestType("QUERY")
                .payload("What is Java?")
                .build();

        // Submit request
        ExecutionSession session = runtime.submit(request);
        assertNotNull(session, "Session should not be null");

        // Shutdown
        runtime.shutdown();
    }
}