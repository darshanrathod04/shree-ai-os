package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.legacy.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.ShreeClient;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SDK → Runtime → Canonical Pipeline End-to-End Integration Test.
 *
 * <p>Verifies that the exact SDK usage:
 * <pre>
 * ShreeAI shree = ShreeAI.builder().apiKey("local").build();
 * SDKResponse response = shree.chat("Hello Shree");
 * </pre>
 * actually traverses the canonical V1 Runtime → Pipeline → Stage path.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since EO-V1-REL1-INT-003
 */
public class SDKToRuntimePipelineIntegrationTest {

    @Test
    public void testExactSDKUsageExecutesEndToEnd() {
        // Exact target usage
        ShreeAI shree = ShreeAI.builder()
                .apiKey("local")
                .build();

        // 1. SDK object constructs
        assertNotNull(shree, "SDK object should construct");

        // 2. Runtime exists
        ShreeClient client = shree.client();
        assertNotNull(client, "Client should exist");
        Runtime runtime = client.runtime();
        assertNotNull(runtime, "Runtime should exist");
        assertTrue(runtime instanceof DefaultRuntimeService,
                "Runtime should be DefaultRuntimeService");

        // 3. Runtime accepts request
        assertTrue(runtime.lifecycle().isAcceptingRequests(),
                "Runtime should be accepting requests");

        // 4. Runtime reaches canonical pipeline
        assertNotNull(runtime.pipeline(), "Runtime should have a pipeline");
        assertTrue(runtime.pipeline() instanceof DefaultExecutionPipeline,
                "Runtime pipeline should be the canonical DefaultExecutionPipeline");

        // 5. Canonical pipeline has all 11 stages
        DefaultExecutionPipeline pipeline = (DefaultExecutionPipeline) runtime.pipeline();
        List<ExecutionStage> stages = pipeline.getStages();
        assertEquals(11, stages.size(), "Canonical pipeline should have 11 stages");

        // Verify stage order
        assertEquals("Identity", stages.get(0).getDescriptor().getStageName());
        assertEquals("Context", stages.get(1).getDescriptor().getStageName());
        assertEquals("MemoryRecall", stages.get(2).getDescriptor().getStageName());
        assertEquals("Knowledge", stages.get(3).getDescriptor().getStageName());
        assertEquals("Reasoning", stages.get(4).getDescriptor().getStageName());
        assertEquals("Inference", stages.get(5).getDescriptor().getStageName());
        assertEquals("Planning", stages.get(6).getDescriptor().getStageName());
        assertEquals("Execution", stages.get(7).getDescriptor().getStageName());
        assertEquals("Reflection", stages.get(8).getDescriptor().getStageName());
        assertEquals("MemoryStore", stages.get(9).getDescriptor().getStageName());
        assertEquals("ChiefReview", stages.get(10).getDescriptor().getStageName());

        // 6. Execute chat through SDK
        SDKResponse response = shree.chat("Hello Shree");

        // 7. No synthetic SDK response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");
        assertFalse(response.answer().contains("Execution completed via Runtime pipeline"),
                "Response must not contain the synthetic success string");
        assertFalse(response.answer().startsWith("Processed: "),
                "Response must not use the fallback 'Processed:' string when Runtime is available");

        // 8. No null result
        assertTrue(response.confidence() >= 0.0 && response.confidence() <= 1.0,
                "Confidence should be 0-1");
        assertNotNull(response.timestamp(), "Timestamp should not be null");
    }

    @Test
    public void testRuntimeSubmitReturnsSessionWithActualResult() {
        ShreeAI shree = ShreeAI.builder()
                .apiKey("local")
                .build();

        Runtime runtime = shree.client().runtime();
        assertNotNull(runtime);

        // Submit directly to Runtime
        com.shreeai.os.platform.runtime.execution.ExecutionRequest request =
                com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                        .requestId("test-request-1")
                        .requestType("CHAT")
                        .payload("Hello Shree")
                        .build();

        ExecutionSession session = runtime.submit(request);

        // Session must have a result (not null)
        assertNotNull(session, "Session should not be null");
        assertNotNull(session.result(), "Session should carry the actual execution result");
        assertTrue(session.result().isSuccess(), "Execution should succeed");
        assertNotNull(session.result().output().orElse(null),
                "Execution result should have output");
        assertFalse(session.result().output().orElse("").isBlank(),
                "Execution result output should not be blank");
    }

    @Test
    public void testCanonicalPipelineExecutesAllStages() {
        ShreeAI shree = ShreeAI.builder()
                .apiKey("local")
                .build();

        Runtime runtime = shree.client().runtime();
        DefaultExecutionPipeline pipeline = (DefaultExecutionPipeline) runtime.pipeline();

        // Build a PipelineContext with a real execution request
        ExecutionRequest pipelineRequest =
                ExecutionRequest.builder()
                        .requestId("pipeline-test-1")
                        .decisionId("test-decision")
                        .capabilityName("CHAT")
                        .intent("CHAT_REQUEST")
                        .userInput("Hello Shree")
                        .build();

        PipelineContext context = PipelineContext.builder()
                .executionRequest(pipelineRequest)
                .build();

        // Execute the canonical pipeline directly
        PipelineResult result = pipeline.execute(context);

        // Verify pipeline completed successfully
        assertNotNull(result, "Pipeline result should not be null");
        assertTrue(result.isSuccess(), "Pipeline should succeed. Status: " + result.getStatus());
        assertEquals("COMPLETED", result.getStatus(), "Pipeline should complete");
        assertEquals(11, result.getCompletedStages().size(),
                "All 11 stages should complete");
    }

    @Test
    public void testApplicationReadySmokeTest() {
        // Application-ready smoke test: exact SDK usage representing developer experience
        ShreeAI shree = ShreeAI.builder()
                .apiKey("local")
                .build();

        // SDK starts
        assertNotNull(shree, "SDK should construct");

        // Runtime starts
        Runtime runtime = shree.client().runtime();
        assertNotNull(runtime, "Runtime should exist");
        assertTrue(runtime.lifecycle().isAcceptingRequests(),
                "Runtime should accept requests");

        // Canonical pipeline executes through all stages including planning, execution, chief review
        SDKResponse response = shree.chat(
                "Create a plan for building a student management application"
        );

        // Actual result reaches SDKResponse
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");
        assertTrue(response.confidence() >= 0.0 && response.confidence() <= 1.0,
                "Confidence should be 0-1");
        assertNotNull(response.timestamp(), "Timestamp should not be null");

        // No synthetic/fallback response
        assertFalse(response.answer().contains("Execution completed via Runtime pipeline"),
                "Response must not contain synthetic success string");
        assertFalse(response.answer().startsWith("Processed: "),
                "Response must not use fallback 'Processed:' string");

        // Verify pipeline actually executed by checking the runtime session
        ExecutionSession session = runtime.submit(
                com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                        .requestId("smoke-test-verify")
                        .requestType("CHAT")
                        .payload("smoke verification")
                        .build()
        );
        assertNotNull(session.result(), "Session should carry actual execution result");
        assertTrue(session.result().isSuccess(), "Execution should succeed");
    }
}
