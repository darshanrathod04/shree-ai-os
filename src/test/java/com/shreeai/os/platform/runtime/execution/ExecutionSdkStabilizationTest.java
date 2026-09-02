package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.response.model.SynthesizedResponse;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EO-S10 — Sprint-10 Execution SDK Stabilization acceptance tests.
 *
 * <p>Verifies that {@link com.shreeai.os.platform.sdk.ExecutionSDK#execute(String, String)}
 * returns enterprise-grade structured responses for all capability dispatch paths,
 * and that the {@code Goal{...}} Java object dump is eliminated from all SDK responses.</p>
 *
 * <p>Test coverage:</p>
 * <ol>
 *   <li>Project planning returns structured markdown with all required sections.</li>
 *   <li>No {@code Goal{...}} toString dump appears anywhere in the response.</li>
 *   <li>Execution ID is generated and present in answer and structured payload.</li>
 *   <li>Structured payload is fully populated with capability, objective, status, etc.</li>
 *   <li>Unknown capability returns a structured failure — no exception, no dump.</li>
 *   <li>UI-design (unregistered capability) returns a graceful structured failure.</li>
 * </ol>
 *
 * <p>All tests use the real {@code ShreeAI} stack (ShreeBuilder → ShreeClient →
 * DefaultRuntimeService → ExecutionDispatcher → capability handlers) with no mocks.</p>
 *
 * @since Sprint-10
 */
@DisplayName("ExecutionSdkStabilization — Sprint-10")
public class ExecutionSdkStabilizationTest {

    private ShreeAI ai;

    @BeforeEach
    public void setUp() {
        ai = ShreeAI.builder().apiKey("local").build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: project-planning returns structured markdown
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("project-planning returns structured markdown with all required sections")
    public void executionProjectPlanningReturnsStructuredResponse() {
        SDKResponse response = ai.execution().execute(
                "project-planning",
                "create dashboard for AI assistant application"
        );

        String answer = response.answer();
        assertNotNull(answer, "answer must not be null");
        assertFalse(answer.isBlank(), "answer must not be blank");

        // All required markdown sections per Sprint-10 spec
        assertTrue(answer.contains("# Execution Started"),
                "answer must contain '# Execution Started' header");
        assertTrue(answer.contains("## Capability"),
                "answer must contain '## Capability' section");
        assertTrue(answer.contains("Project Planning"),
                "answer must contain 'Project Planning' capability name");
        assertTrue(answer.contains("## Objective"),
                "answer must contain '## Objective' section");
        assertTrue(answer.contains("create dashboard for AI assistant application"),
                "answer must contain the user's original objective");
        assertTrue(answer.contains("## Status"),
                "answer must contain '## Status' section");
        assertTrue(answer.contains("COMPLETED"),
                "answer must contain 'COMPLETED' status");
        assertTrue(answer.contains("## Execution ID"),
                "answer must contain '## Execution ID' section");
        assertTrue(answer.contains("## Deliverables"),
                "answer must contain '## Deliverables' section");
        assertTrue(answer.contains("## Metadata"),
                "answer must contain '## Metadata' section");
        assertTrue(answer.contains("Kernel:"),
                "answer must contain 'Kernel:' in Metadata section");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: no Goal{...} anywhere in response
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("no Goal{...} dump appears in answer or structuredPayload")
    public void noGoalDumpAnywhereInResponse() {
        SDKResponse response = ai.execution().execute(
                "project-planning",
                "create dashboard for AI assistant application"
        );

        String answer = response.answer();
        Map<String, Object> payload = response.structuredPayload();

        assertFalse(answer.contains("Goal{"),
                "answer must not contain 'Goal{' — found the Java toString dump");
        assertFalse(answer.contains("planningId="),
                "answer must not contain 'planningId=' field from Goal toString");
        assertFalse(answer.contains("objective="),
                "answer must not contain 'objective=' field from Goal toString");
        assertFalse(answer.contains("constraints="),
                "answer must not contain 'constraints=' field from Goal toString");

        String payloadStr = payload == null ? "" : payload.toString();
        assertFalse(payloadStr.contains("Goal{"),
                "structuredPayload must not contain 'Goal{'");
        assertFalse(payloadStr.contains("planningId="),
                "structuredPayload must not contain 'planningId='");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: executionId is generated
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("executionId is generated and present in answer and structuredPayload")
    public void executionIdIsGenerated() {
        SDKResponse response = ai.execution().execute(
                "project-planning",
                "create dashboard for AI assistant application"
        );

        String answer = response.answer();

        // The execution ID appears in the "## Execution ID" section
        assertTrue(answer.contains("exec-"),
                "answer must contain an 'exec-' prefixed execution ID");

        // Verify structuredPayload contains executionId
        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "structuredPayload must not be null");

        // Check the SynthesizedResponse in the payload (key: "response")
        Object responseObj = payload.get("response");
        assertNotNull(responseObj, "payload['response'] must not be null");
        assertTrue(responseObj instanceof SynthesizedResponse,
                "payload['response'] must be a SynthesizedResponse, got: "
                        + responseObj.getClass().getName());
        SynthesizedResponse synthesis = (SynthesizedResponse) responseObj;

        Map<String, Object> structuredData = synthesis.structuredData();
        assertNotNull(structuredData, "synthesizedResponse.structuredData must not be null");

        Object executionId = structuredData.get("executionId");
        assertNotNull(executionId, "executionId must be in structuredData");
        assertTrue(executionId.toString().startsWith("exec-"),
                "executionId must start with 'exec-', got: " + executionId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4: structuredPayload is fully populated
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("structuredPayload is populated with capability, objective, status, executionId")
    public void structuredPayloadIsPopulated() {
        SDKResponse response = ai.execution().execute(
                "project-planning",
                "create dashboard for AI assistant application"
        );

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "structuredPayload must not be null");

        Object responseObj = payload.get("response");
        assertNotNull(responseObj, "payload['response'] must not be null");
        assertTrue(responseObj instanceof SynthesizedResponse,
                "payload['response'] must be SynthesizedResponse");
        SynthesizedResponse synthesis = (SynthesizedResponse) responseObj;

        Map<String, Object> structuredData = synthesis.structuredData();

        // Required fields
        assertTrue(structuredData.containsKey("capability"),
                "structuredData must contain 'capability'");
        assertTrue(structuredData.containsKey("capabilityValue"),
                "structuredData must contain 'capabilityValue'");
        assertTrue(structuredData.containsKey("objective"),
                "structuredData must contain 'objective'");
        assertTrue(structuredData.containsKey("status"),
                "structuredData must contain 'status'");
        assertTrue(structuredData.containsKey("executionId"),
                "structuredData must contain 'executionId'");
        assertTrue(structuredData.containsKey("kernel"),
                "structuredData must contain 'kernel'");
        assertTrue(structuredData.containsKey("planId"),
                "structuredData must contain 'planId'");
        assertTrue(structuredData.containsKey("deliverables"),
                "structuredData must contain 'deliverables'");

        // Value checks
        assertEquals("Project Planning", structuredData.get("capability"),
                "capability display name must be 'Project Planning'");
        assertEquals("PROJECT_PLANNING", structuredData.get("capabilityValue"),
                "capabilityValue must be 'PROJECT_PLANNING'");
        assertEquals("COMPLETED", structuredData.get("status"),
                "status must be 'COMPLETED'");
        assertEquals("create dashboard for AI assistant application",
                structuredData.get("objective"),
                "objective must match the user's input");

        @SuppressWarnings("unchecked")
        var deliverables = (java.util.List<String>) structuredData.get("deliverables");
        assertNotNull(deliverables, "deliverables must not be null");
        assertFalse(deliverables.isEmpty(),
                "deliverables must not be empty for project-planning");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 5: unknown capability returns structured failure
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("unknown-capability returns structured failure with FAILED status — no exception")
    public void executionUnknownCapabilityReturnsStructuredFailure() {
        // This must NOT throw — the dispatcher handles unknown capabilities gracefully
        SDKResponse response = ai.execution().execute(
                "unknown-capability",
                "do something impossible"
        );

        String answer = response.answer();
        assertNotNull(answer, "answer must not be null even for unknown capability");
        assertFalse(answer.contains("Goal{"),
                "answer must not contain 'Goal{' for unknown capability");

        // Check structured payload
        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "structuredPayload must not be null");

        Object responseObj = payload.get("response");
        assertNotNull(responseObj, "payload['response'] must not be null");
        assertTrue(responseObj instanceof SynthesizedResponse,
                "payload['response'] must be SynthesizedResponse for unknown capability");
        SynthesizedResponse synthesis = (SynthesizedResponse) responseObj;

        Map<String, Object> structuredData = synthesis.structuredData();
        // Unknown capabilities are gracefully handled with NOT_SUPPORTED status
        assertEquals("NOT_SUPPORTED", structuredData.get("status"),
                "status must be 'NOT_SUPPORTED' for unknown capability");
        assertTrue(answer.contains("NOT_SUPPORTED"),
                "answer must contain 'NOT_SUPPORTED' for unknown capability");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 6: ui-design capability returns graceful structured response
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ui-design returns structured response — graceful degradation")
    public void executionUiDesignCapabilityReturnsStructuredResponse() {
        // ui-design is not registered — dispatcher returns failure.
        // The synthesis layer wraps this into a structured response.
        SDKResponse response = ai.execution().execute(
                "ui-design",
                "design a landing page"
        );

        String answer = response.answer();
        assertNotNull(answer, "answer must not be null");
        assertFalse(answer.contains("Goal{"),
                "ui-design answer must not contain 'Goal{'");

        Map<String, Object> payload = response.structuredPayload();
        assertNotNull(payload, "structuredPayload must not be null");

        Object responseObj = payload.get("response");
        assertNotNull(responseObj, "payload['response'] must not be null");
        assertTrue(responseObj instanceof SynthesizedResponse,
                "payload['response'] must be SynthesizedResponse for ui-design");
        SynthesizedResponse synthesis = (SynthesizedResponse) responseObj;

        // The capability value must be present
        Map<String, Object> structuredData = synthesis.structuredData();
        assertTrue(structuredData.containsKey("executionId"),
                "executionId must be present for ui-design");
        assertTrue(structuredData.containsKey("status"),
                "status must be present for ui-design");
        // ui-design is not registered → NOT_SUPPORTED
        assertEquals("NOT_SUPPORTED", structuredData.get("status"),
                "ui-design status must be 'NOT_SUPPORTED' (not registered)");

        // Verify the "## Capability" section is present in the answer
        assertTrue(answer.contains("## Capability"),
                "ui-design answer must contain '## Capability'");
    }
}
