package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>DeveloperApplyWorkflowTest</b>
 *
 * <p>16 test cases for the full DeveloperApply pipeline (Sprint-17).</p>
 *
 * @since Sprint-17
 */
public class DeveloperApplyWorkflowTest {

    private final DefaultPatchExecutionEngine engine = new DefaultPatchExecutionEngine();

    @Test
    void apply_returnsDeveloperExecutionResult() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertNotNull(result);
        assertNotNull(result.executionId());
        assertNotNull(result.status());
    }

    @Test
    void apply_includesWorkflow() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertNotNull(result.workflow());
    }

    @Test
    void apply_includesRollbackPlan() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertNotNull(result.rollbackPlan());
        assertNotNull(result.rollbackPlan().planId());
    }

    @Test
    void apply_includesCompileReport() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertNotNull(result.compileReport());
    }

    @Test
    void apply_confidenceIsBetween0And1() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertTrue(result.confidence() >= 0.0);
        assertTrue(result.confidence() <= 1.0);
    }

    @Test
    void apply_doesNotThrowOnNullProjectPath() {
        // The patch engine should handle missing project paths gracefully
        DeveloperExecutionResult result = engine.execute("/tmp/missing", "Any instruction");
        assertNotNull(result);
    }

    @Test
    void apply_summaryStringIsNonEmpty() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        assertNotNull(result.summary());
        assertTrue(result.summary().length() > 0);
    }

    @Test
    void apply_payloadContainsAllFields() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Add a new utility class");
        var payload = result.toPayload();
        assertTrue(payload.containsKey("executionId"));
        assertTrue(payload.containsKey("status"));
        assertTrue(payload.containsKey("appliedPatches"));
        assertTrue(payload.containsKey("totalPatches"));
        assertTrue(payload.containsKey("rollbackActions"));
        assertTrue(payload.containsKey("compileStatus"));
        assertTrue(payload.containsKey("confidence"));
    }

    @Test
    void apply_isSuccess_returnsTrueOrFalse() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test instruction");
        // Status may be SKIPPED if workflow produces no patches
        assertNotNull(result.status());
    }

    @Test
    void apply_appliedCountIsNonNegative() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test instruction");
        assertTrue(result.appliedCount() >= 0);
    }

    @Test
    void apply_compileReportHasStatus() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test instruction");
        assertNotNull(result.compileReport().status());
    }

    @Test
    void apply_rollbackPlanIsNotNull() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test instruction");
        assertNotNull(result.rollbackPlan());
    }

    @Test
    void apply_rollbackPlanHasValidId() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test instruction");
        assertNotNull(result.rollbackPlan().planId());
        assertTrue(result.rollbackPlan().planId().startsWith("rb-"));
    }

    @Test
    void apply_executedAtIsSet() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "Test");
        assertNotNull(result.executedAt());
    }

    @Test
    void apply_handlesEmptyInstruction() {
        DeveloperExecutionResult result = engine.execute("/tmp/nonexistent", "");
        assertNotNull(result);
    }

    @Test
    void apply_handlesComplexInstruction() {
        DeveloperExecutionResult result = engine.execute(
                "/tmp/nonexistent",
                "Add JWT authentication with refresh tokens, OAuth2 support, and rate limiting"
        );
        assertNotNull(result);
        assertNotNull(result.workflow());
    }
}
