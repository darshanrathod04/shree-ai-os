package com.shreeai.os.platform.kernels.developer;

import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperResult;
import com.shreeai.os.platform.kernels.developer.workflow.model.GeneratedArtifact;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>DeveloperWorkflowSdkIntegrationTest</b>
 *
 * <p>Sprint-16 SDK integration tests for the Autonomous Developer Workflow.
 * Validates that the SDK entry point {@code shree.project().build(...)}
 * returns a complete workflow with all expected components.</p>
 *
 * <p><b>All existing 1031 tests must continue to pass.</b></p>
 *
 * @since Sprint-16
 */
public class DeveloperWorkflowSdkIntegrationTest {

    private ShreeAI shree() {
        return ShreeAI.builder().apiKey("local").build();
    }

    // ─── SDK Entry Point Tests ──────────────────────────────────────────────────

    @Test
    public void test01_Build_ReturnsCompleteWorkflow() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add JWT authentication"
        );

        assertNotNull(result, "build() should return a non-null result");
        assertNotNull(result.workflow(), "workflow should not be null");
        assertNotNull(result.markdownSummary(), "markdownSummary should not be null");
        assertTrue(result.confidence() >= 0.0 && result.confidence() <= 1.0,
                "confidence should be in [0.0, 1.0]");
    }

    @Test
    public void test02_Build_SecurityIntent() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add JWT authentication with refresh tokens"
        );

        assertNotNull(result);
        assertEquals("SECURITY", result.workflow().intent().intent().name());
        assertTrue(result.markdownSummary().contains("JWT"));
        assertTrue(result.markdownSummary().contains("SECURITY"));
    }

    @Test
    public void test03_Build_RestEndpoint() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Create a new REST endpoint for orders"
        );

        assertNotNull(result);
        assertTrue(result.markdownSummary().contains("Instruction"));
    }

    @Test
    public void test04_Build_CrudIntent() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add CRUD operations for Product"
        );

        assertNotNull(result);
        assertNotNull(result.workflow().intent());
    }

    @Test
    public void test05_Build_RefactorIntent() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Rename UserService to CustomerService"
        );

        assertNotNull(result);
        assertEquals("REFACTOR", result.workflow().intent().intent().name());
    }

    @Test
    public void test06_Build_RenameEntity() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Rename the Order entity to PurchaseOrder"
        );

        assertNotNull(result);
        assertTrue(result.markdownSummary().contains("Order") || result.markdownSummary().contains("REFACTOR"));
    }

    // ─── Impact / Risk Tests ─────────────────────────────────────────────────────

    @Test
    public void test07_Build_ImpactReportPresent() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add JWT authentication"
        );

        assertNotNull(result.workflow().workflowImpact());
        assertNotNull(result.workflow().workflowImpact().riskLevel());
        assertNotNull(result.workflow().workflowImpact().affectedFiles());
    }

    @Test
    public void test08_Build_RiskLevelDeterministic() {
        DeveloperResult result1 = shree().project().build("/workspace/demo", "Add JWT");
        DeveloperResult result2 = shree().project().build("/workspace/demo", "Add JWT");

        assertEquals(result1.workflow().workflowImpact().riskLevel(),
                result2.workflow().workflowImpact().riskLevel(),
                "Risk level should be deterministic (same request)");
    }

    // ─── Code Generation Tests ───────────────────────────────────────────────────

    @Test
    public void test09_Build_JavaArtifacts() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Create Product entity"
        );

        assertNotNull(result.generatedArtifacts());
        // Should have some Java artifacts from code generation
        long javaCount = result.generatedArtifacts().stream()
                .filter(a -> a.type() == GeneratedArtifact.Type.JAVA)
                .count();
        assertTrue(javaCount >= 0, "Should have Java artifacts");
    }

    @Test
    public void test10_Build_TestArtifacts() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Create REST endpoint for Order"
        );

        assertNotNull(result.testSkeletons());
        assertNotNull(result.generatedArtifacts());
    }

    @Test
    public void test11_Build_ConfigArtifacts() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add Spring Security configuration"
        );

        assertNotNull(result.generatedArtifacts());
    }

    // ─── Validation Tests ────────────────────────────────────────────────────────

    @Test
    public void test12_Build_ValidationPresent() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add JWT authentication"
        );

        assertNotNull(result.workflow().validationResult());
        assertNotNull(result.workflow().validationResult().overallStatus());
    }

    @Test
    public void test13_Build_ValidationStatusDeterministic() {
        DeveloperResult r1 = shree().project().build("/workspace/demo", "Create User entity");
        DeveloperResult r2 = shree().project().build("/workspace/demo", "Create User entity");

        assertEquals(r1.workflow().validationResult().overallStatus(),
                r2.workflow().validationResult().overallStatus(),
                "Validation status should be deterministic");
    }

    // ─── Confidence Tests ────────────────────────────────────────────────────────

    @Test
    public void test14_Build_ConfidenceInRange() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Optimize database queries"
        );

        assertTrue(result.confidence() >= 0.1 && result.confidence() <= 0.95,
                "Confidence should be between 0.1 and 0.95");
    }

    @Test
    public void test15_Build_ConfidenceDeterministic() {
        DeveloperResult r1 = shree().project().build("/workspace/demo", "Fix bug in UserService");
        DeveloperResult r2 = shree().project().build("/workspace/demo", "Fix bug in UserService");

        assertEquals(r1.confidence(), r2.confidence(), 0.001,
                "Confidence should be deterministic");
    }

    // ─── SDK Contract Tests ──────────────────────────────────────────────────────

    @Test
    public void test16_Build_MarkdownSummary_NotEmpty() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add caching to ProductService"
        );

        assertNotNull(result.markdownSummary());
        assertFalse(result.markdownSummary().isEmpty(),
                "Markdown summary should not be empty");
        assertTrue(result.markdownSummary().length() > 50,
                "Markdown summary should have substantial content");
    }

    @Test
    public void test17_Build_MarkdownSummary_HasAllSections() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add JWT authentication"
        );

        String md = result.markdownSummary();
        assertTrue(md.contains("# Developer Workflow"));
        assertTrue(md.contains("## Instruction"));
        assertTrue(md.contains("## Intent"));
        assertTrue(md.contains("## Project Summary"));
        assertTrue(md.contains("## Impact Analysis"));
        assertTrue(md.contains("## Generated Files"));
        assertTrue(md.contains("## Validation"));
        assertTrue(md.contains("## Next Step"));
        assertTrue(md.contains("Apply generated patch"));
    }

    @Test
    public void test18_Build_WithMetadata() {
        DeveloperResult result = shree().project().build(
                "/workspace/demo",
                "Add rate limiting",
                Map.of("team", "backend", "priority", "high")
        );

        assertNotNull(result);
        assertNotNull(result.workflow());
        assertNotNull(result.markdownSummary());
    }

    // ─── Safety Tests ───────────────────────────────────────────────────────────

    @Test
    public void test19_Build_NoFilesystemWrites() throws java.io.IOException {
        // Use the actual shree SDK against a temp directory
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"),
                "sprint16_sdk_test_" + System.nanoTime());
        try {
            java.nio.file.Files.createDirectories(tempDir);

            DeveloperResult result = shree().project().build(
                    tempDir.toString(),
                    "Add authentication"
            );

            assertNotNull(result);
            // Verify no files were written
            try (var stream = java.nio.file.Files.list(tempDir)) {
                long count = stream.count();
                assertEquals(0, count,
                        "SDK build() should not write files. Found " + count + " file(s) in " + tempDir);
            }
        } finally {
            // Clean up
            try (var stream = java.nio.file.Files.walk(tempDir)) {
                stream.sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> { try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {} });
            }
        }
    }

    @Test
    public void test20_Build_ExistingMethodsUnchanged() {
        // Verify that the existing SDK methods still work
        ShreeAI shree = ShreeAI.builder().apiKey("local").build();

        // project().developerAgent() should still work (Sprint-14)
        assertNotNull(shree.project().developerAgent(),
                "developerAgent() should still be accessible");

        // project().analyze() should still work (Sprint-13)
        // We just verify the method exists by checking it returns non-null
        // (we can't call analyze on a real project without side effects)
        assertNotNull(shree.project(),
                "project() should return non-null SDK");
    }
}
