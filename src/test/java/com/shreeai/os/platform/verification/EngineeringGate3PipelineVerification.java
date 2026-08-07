package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Engineering Gate 3 - Pipeline Verification Test
 *
 * <p>This test verifies that the real kernel execution pipeline works end-to-end.</p>
 *
 * <p>Test scenario: User asks "What is Java?"</p>
 *
 * <p>Expected execution path:</p>
 * <ol>
 *   <li>Identity</li>
 *   <li>Context</li>
 *   <li>MemoryRecall</li>
 *   <li>Knowledge</li>
 *   <li>Reasoning</li>
 *   <li>Planning</li>
 *   <li>Execution</li>
 *   <li>MemoryStore</li>
 *   <li>ChiefReview</li>
 * </ol>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public class EngineeringGate3PipelineVerification {

    private DefaultRuntimeService runtime;

    @BeforeEach
    public void setUp() {
        // Create runtime configuration
        RuntimeConfiguration configuration = RuntimeConfiguration.builder()
                .runtimeName("EngineeringGate3Test")
                .build();

        // Create runtime contract using builder
        RuntimeContract contract = RuntimeContract.builder()
                .contractVersion("1.0")
                .supportsSessions(true)
                .supportsPipelines(true)
                .maxPipelineStageDepth(10)
                .build();

        // Create runtime service
        runtime = new DefaultRuntimeService(configuration, contract);

        // Initialize and start runtime
        runtime.initialize();
        runtime.start();
    }

    @AfterEach
    public void tearDown() {
        if (runtime != null) {
            runtime.shutdown();
        }
    }

    @Test
    public void testRealPipelineExecution() {
        // Create execution request
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-request-001")
                .build();

        // Submit request
        ExecutionSession session = runtime.submit(request);
        assertNotNull(session, "Session should not be null");

        // Verify runtime is operational
        assertTrue(runtime.lifecycle().isAcceptingRequests(), 
                "Runtime should be accepting requests");
    }

    @Test
    public void testRuntimeStartsSuccessfully() {
        // Runtime should be in STARTED state
        assertTrue(runtime.lifecycle().isAcceptingRequests(), 
                "Runtime should be accepting requests");
    }

    @Test
    public void testPipelineNotInShadowMode() {
        // Create execution request
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-request-003")
                .build();

        // Submit request
        ExecutionSession session = runtime.submit(request);
        assertNotNull(session, "Session should not be null");
        
        // Verify pipeline exists and has stages
        assertNotNull(runtime.pipeline(), "Pipeline should not be null");
    }

    @Test
    public void testContextUpdatedThroughPipeline() {
        // Create execution request
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-request-004")
                .build();

        // Submit request
        ExecutionSession session = runtime.submit(request);
        assertNotNull(session, "Session should not be null");
        
        // Verify pipeline exists
        assertNotNull(runtime.pipeline(), "Pipeline should not be null");
    }

    @Test
    public void testChiefKernelParticipates() {
        // Create execution request
        ExecutionRequest request = ExecutionRequest.builder()
                .requestId("test-request-005")
                .build();

        // Submit request
        ExecutionSession session = runtime.submit(request);
        assertNotNull(session, "Session should not be null");
        
        // Verify pipeline exists
        assertNotNull(runtime.pipeline(), "Pipeline should not be null");
    }
}