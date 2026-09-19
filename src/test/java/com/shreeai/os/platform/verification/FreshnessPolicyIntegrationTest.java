package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.context.engine.DefaultPrimaryIntentDetector;
import com.shreeai.os.platform.kernels.context.engine.DefaultDomainDetector;
import com.shreeai.os.platform.kernels.context.engine.DefaultConstraintExtractionEngine;
import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import com.shreeai.os.platform.runtime.pipeline.stages.IdentityStage;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.factory.DefaultKernelFactory;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FreshnessPolicyIntegrationTest {

    @Test
    void contextStageProducesAcquisitionDecisionPlan() {
        IdentityService identityService =
                new DefaultKernelFactory().createIdentityService();
        List<ExecutionStage> stages = List.of(
                new IdentityStage(identityService),
                new ContextStage()
        );
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .executionRequest(ExecutionRequest.builder()
                        .requestId("freshness-int-test-001")
                        .requestType("QUERY")
                        .userInput("What is the latest Java version?")
                        .build())
                .build();

        PipelineResult result = pipeline.execute(context);

        AcquisitionDecisionPlan plan =
                result.getExecutionState().getCognitiveState().acquisitionDecisionPlan();

        assertNotNull(plan, "AcquisitionDecisionPlan should be produced");
        assertNotNull(plan.targets(), "Target list should not be null");
        assertNotNull(plan.reasons(), "Reason list should not be null");
        assertEquals(plan.targets().size(), plan.reasons().size(),
                "Targets and reasons must be position-aligned");
    }

    @Test
    void contextStageEmptyRegistryProducesEmptyPlan() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        ContextStage stage = new ContextStage(
                new DefaultPrimaryIntentDetector(),
                new DefaultDomainDetector(),
                new DefaultConstraintExtractionEngine(),
                registry);

        List<ExecutionStage> stages = List.of(
                new IdentityStage(new DefaultKernelFactory().createIdentityService()),
                stage
        );
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .executionRequest(ExecutionRequest.builder()
                        .requestId("freshness-int-test-003")
                        .requestType("QUERY")
                        .userInput("What is Java?")
                        .build())
                .build();

        PipelineResult result = pipeline.execute(context);

        AcquisitionDecisionPlan plan =
                result.getExecutionState().getCognitiveState().acquisitionDecisionPlan();
        assertNotNull(plan, "Plan should be produced even with empty registry");
        assertTrue(plan.targets().isEmpty(),
                "Empty registry should produce empty target list");
        assertTrue(plan.reasons().isEmpty(),
                "Empty registry should produce empty reason list");
    }

    @Test
    void contextStageReasonsArePositionAlignedAfterPipeline() {
        IdentityService identityService =
                new DefaultKernelFactory().createIdentityService();
        List<ExecutionStage> stages = List.of(
                new IdentityStage(identityService),
                new ContextStage()
        );
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .executionRequest(ExecutionRequest.builder()
                        .requestId("freshness-int-test-004")
                        .requestType("QUERY")
                        .userInput("Tell me about Java programming")
                        .build())
                .build();

        PipelineResult result = pipeline.execute(context);

        AcquisitionDecisionPlan plan =
                result.getExecutionState().getCognitiveState().acquisitionDecisionPlan();
        if (plan.size() > 0) {
            for (int i = 0; i < plan.size(); i++) {
                assertEquals(plan.targets().get(i).sourceId(),
                        plan.reasons().get(i).sourceId(),
                        "Reason sourceId must match target at position " + i);
                assertEquals(plan.targets().get(i).decision(),
                        plan.reasons().get(i).decision(),
                        "Reason decision must match target at position " + i);
            }
        }
    }

    @Test
    void contextStagePlanIsDeterministicAcrossRuns() {
        PipelineContext context = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .executionRequest(ExecutionRequest.builder()
                        .requestId("freshness-int-test-005")
                        .requestType("QUERY")
                        .userInput("What is Java?")
                        .build())
                .build();

        AcquisitionDecisionPlan plan1 = runContextStage(context);
        AcquisitionDecisionPlan plan2 = runContextStage(context);

        assertEquals(plan1, plan2,
                "Same input should produce identical AcquisitionDecisionPlan");
    }

    private AcquisitionDecisionPlan runContextStage(PipelineContext context) {
        IdentityService identityService =
                new DefaultKernelFactory().createIdentityService();
        List<ExecutionStage> stages = List.of(
                new IdentityStage(identityService),
                new ContextStage()
        );
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);
        PipelineResult result = pipeline.execute(context);
        return result.getExecutionState().getCognitiveState().acquisitionDecisionPlan();
    }
}
