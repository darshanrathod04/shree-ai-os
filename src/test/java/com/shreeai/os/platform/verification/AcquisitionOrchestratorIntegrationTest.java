package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.context.engine.DefaultConstraintExtractionEngine;
import com.shreeai.os.platform.kernels.context.engine.DefaultDomainDetector;
import com.shreeai.os.platform.kernels.context.engine.DefaultPrimaryIntentDetector;
import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.factory.DefaultKernelFactory;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.stages.ContextStage;
import com.shreeai.os.platform.runtime.pipeline.stages.IdentityStage;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pipeline-level integration tests for the K0.6.5 Knowledge Acquisition
 * Orchestrator: proves the full K0.6 chain (discover, route, select, decide,
 * execute) flows through {@link ContextStage} into the {@link CognitiveState}
 * acquisition result. The runtime wires no content supply, so pending
 * acquisitions are isolated and recorded as {@code FAILED} - the orchestrator
 * never re-decides and never stops the pipeline.
 */
public class AcquisitionOrchestratorIntegrationTest {

    private static PipelineResult runPipeline(DefaultKnowledgeSourceRegistry registry) {
        IdentityService identityService =
                new DefaultKernelFactory().createIdentityService();
        List<ExecutionStage> stages = List.of(
                new IdentityStage(identityService),
                new ContextStage(new DefaultPrimaryIntentDetector(),
                        new DefaultDomainDetector(),
                        new DefaultConstraintExtractionEngine(),
                        registry));
        DefaultExecutionPipeline pipeline = new DefaultExecutionPipeline(stages);

        PipelineContext context = PipelineContext.builder()
                .pipelineId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .executionRequest(ExecutionRequest.builder()
                        .requestId("k065-int-" + UUID.randomUUID())
                        .requestType("QUERY")
                        .userInput("What is Java programming?")
                        .build())
                .build();
        return pipeline.execute(context);
    }

    private static DefaultKnowledgeSourceRegistry registryWithActiveGuide() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = registry.register(
                KnowledgeSourceType.MARKDOWN, "Java Guide", "java.md", "Java knowledge", Map.of());
        registry.activate(source.sourceId());
        return registry;
    }

    @Test
    void contextStageProducesAcquisitionResult() {
        AcquisitionResult result = runPipeline(new DefaultKnowledgeSourceRegistry())
                .getExecutionState().getCognitiveState().acquisitionResult();

        assertNotNull(result, "AcquisitionResult should be produced");
        assertNotNull(result.documents(), "Document list should not be null");
        assertNotNull(result.records(), "Record list should not be null");
    }

    @Test
    void emptyRegistryProducesEmptyResult() {
        AcquisitionResult result = runPipeline(new DefaultKnowledgeSourceRegistry())
                .getExecutionState().getCognitiveState().acquisitionResult();

        assertTrue(result.records().isEmpty(),
                "Empty registry must produce no acquisition records");
        assertTrue(result.documents().isEmpty(),
                "Empty registry must produce no documents");
    }

    @Test
    void everyDecisionHasExactlyOneExecutionRecord() {
        CognitiveState state = runPipeline(registryWithActiveGuide())
                .getExecutionState().getCognitiveState();
        AcquisitionDecisionPlan plan = state.acquisitionDecisionPlan();
        AcquisitionResult result = state.acquisitionResult();

        assertEquals(plan.size(), result.size(),
                "Every decision must have exactly one execution record");
    }

    @Test
    void orchestratorNeverReDecides() {
        CognitiveState state = runPipeline(registryWithActiveGuide())
                .getExecutionState().getCognitiveState();
        AcquisitionDecisionPlan plan = state.acquisitionDecisionPlan();
        AcquisitionResult result = state.acquisitionResult();

        for (int i = 0; i < result.size(); i++) {
            var record = result.records().get(i);
            var decided = plan.decisionFor(record.topicId());
            assertTrue(decided.isPresent(),
                    "Record topic must exist in the decision plan");
            assertEquals(decided.get().decision(), record.decision(),
                    "The orchestrator must execute, never re-decide");
        }
    }

    @Test
    void pendingAcquisitionWithoutContentSupplyIsIsolatedAsFailed() {
        CognitiveState state = runPipeline(registryWithActiveGuide())
                .getExecutionState().getCognitiveState();
        AcquisitionResult result = state.acquisitionResult();

        result.records().forEach(record -> assertEquals(
                com.shreeai.os.platform.kernels.acquisition.model.AcquisitionStatus.FAILED,
                record.status(),
                "With no content supply wired into the runtime, every decision "
                        + "must be isolated as FAILED - never crash the pipeline"));
    }

    @Test
    void acquisitionResultIsDeterministicAcrossRuns() {
        AcquisitionResult first = runPipeline(registryWithActiveGuide())
                .getExecutionState().getCognitiveState().acquisitionResult();
        AcquisitionResult second = runPipeline(registryWithActiveGuide())
                .getExecutionState().getCognitiveState().acquisitionResult();

        assertEquals(first, second, "Same input must produce identical AcquisitionResult");
    }
}
