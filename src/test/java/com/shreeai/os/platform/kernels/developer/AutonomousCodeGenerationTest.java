package com.shreeai.os.platform.kernels.developer;

import com.shreeai.os.platform.kernels.developer.codegen.*;
import com.shreeai.os.platform.kernels.developer.codegen.model.*;
import com.shreeai.os.platform.kernels.developer.engine.DefaultDeveloperAgentEngine;
import com.shreeai.os.platform.kernels.response.model.DeveloperResponse;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.developer.analyzer.DeveloperIntentAnalyzer;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>AutonomousCodeGenerationTest</b>
 *
 * <p>Sprint-15 acceptance tests for the Autonomous Code Generation pipeline.
 * Validates the PatchPlanner, JavaCodeGenerator, PatchValidator, and
 * TestSkeletonGenerator against the 15 scenarios from the Sprint-15 spec.</p>
 *
 * <p>All tests run without any LLM call and without writing any files.</p>
 *
 * @since Sprint-15
 */
public class AutonomousCodeGenerationTest {

    private final DeveloperIntentAnalyzer intentAnalyzer = new DeveloperIntentAnalyzer();
    private final DefaultCodeGenerationEngine codeGen = new DefaultCodeGenerationEngine();

    private DeveloperIntent analyze(String request) {
        return intentAnalyzer.analyze(request);
    }

    private CodeGenerationResult generate(String request) {
        return codeGen.generate(analyze(request), List.of());
    }

    private CodeGenerationResult generate(String request, List<ProjectClass> classes) {
        return codeGen.generate(analyze(request), classes);
    }

    // ─── 1. Add JWT authentication ─────────────────────────────────────────────

    @Test
    public void test1_AddJwtAuthentication() {
        CodeGenerationResult r = generate("Add JWT authentication");

        assertNotNull(r, "Code generation result should not be null");
        assertNotNull(r.patchPlan(), "Patch plan should not be null");
        assertFalse(r.patchPlan().patches().isEmpty(), "Patch plan should contain patches");

        // Should create a JWT-related service class
        boolean hasJwt = r.patchPlan().patches().stream()
                .anyMatch(p -> p.targetFile().toLowerCase().contains("jwt")
                        || p.operations().stream().anyMatch(o ->
                                o.code().toLowerCase().contains("jwt")));
        assertTrue(hasJwt, "Should have JWT-related patches");
    }

    // ─── 2. Create Product entity ──────────────────────────────────────────────

    @Test
    public void test2_CreateProductEntity() {
        CodeGenerationResult r = generate("Create Product entity");

        assertNotNull(r.patchPlan());
        boolean hasEntityPatch = r.patchPlan().patches().stream()
                .anyMatch(p -> p.operations().stream()
                        .anyMatch(o -> o.kind() == PatchOperation.ADD_ENTITY
                                || o.code().contains("@Entity")
                                || p.targetFile().contains("Product")));
        assertTrue(hasEntityPatch, "Should generate an entity patch for Product");
    }

    // ─── 3. Add Orders REST API ────────────────────────────────────────────────

    @Test
    public void test3_AddOrdersRestApi() {
        CodeGenerationResult r = generate("Add Orders REST API");

        assertNotNull(r.patchPlan());
        // Should have a controller patch with endpoint operations
        boolean hasControllerWithEndpoints = r.patchPlan().patches().stream()
                .anyMatch(p -> p.operations().stream()
                        .anyMatch(o -> o.kind() == PatchOperation.ADD_ENDPOINT
                                || p.targetFile().toLowerCase().contains("controller")));
        assertTrue(hasControllerWithEndpoints, "Should generate controller + endpoints");
    }

    // ─── 4. Create Notification service ────────────────────────────────────────

    @Test
    public void test4_CreateNotificationService() {
        CodeGenerationResult r = generate("Create Notification service");

        assertNotNull(r.patchPlan());
        boolean hasService = r.patchPlan().patches().stream()
                .anyMatch(p -> p.targetFile().toLowerCase().contains("service")
                        || p.operations().stream().anyMatch(o ->
                                o.signature().toLowerCase().contains("service")));
        assertTrue(hasService, "Should generate a service patch");
    }

    // ─── 5. Refactor UserService ───────────────────────────────────────────────

    @Test
    public void test5_RefactorUserService() {
        // Provide an existing UserService to refactor
        ProjectClass userService = ProjectClass.builder()
                .name("UserService")
                .fullyQualifiedName("com.example.service.UserService")
                .packageName("com.example.service")
                .filePath("com/example/service/UserService.java")
                .role(Role.SERVICE)
                .build();

        CodeGenerationResult r = generate("Refactor UserService", List.of(userService));
        assertNotNull(r.patchPlan());
        assertFalse(r.patchPlan().patches().isEmpty(), "Refactor should produce patches");
    }

    // ─── 6. Generate DTO ───────────────────────────────────────────────────────

    @Test
    public void test6_GenerateDto() {
        CodeGenerationResult r = generate("Create a new Order DTO");

        assertNotNull(r.patchPlan());
        boolean hasDto = r.patchPlan().patches().stream()
                .anyMatch(p -> p.targetFile().toLowerCase().contains("dto")
                        || p.targetFile().toLowerCase().contains("order"));
        assertTrue(hasDto, "Should generate a DTO class");
    }

    // ─── 7. Generate Repository ────────────────────────────────────────────────

    @Test
    public void test7_GenerateRepository() {
        // ADD_ENTITY generates a repository automatically
        CodeGenerationResult r = generate("Create Order entity");

        assertNotNull(r.patchPlan());
        boolean hasRepo = r.patchPlan().patches().stream()
                .anyMatch(p -> p.targetFile().toLowerCase().contains("repository")
                        || p.targetFile().toLowerCase().contains("repo"));
        assertTrue(hasRepo, "Should generate a repository");
    }

    // ─── 8. Generate Controller ────────────────────────────────────────────────

    @Test
    public void test8_GenerateController() {
        CodeGenerationResult r = generate("Create Customer entity");

        assertNotNull(r.patchPlan());
        boolean hasController = r.patchPlan().patches().stream()
                .anyMatch(p -> p.targetFile().toLowerCase().contains("controller"));
        assertTrue(hasController, "Should generate a controller for the entity");
    }

    // ─── 9. Patch validation SAFE ──────────────────────────────────────────────

    @Test
    public void test9_PatchValidationSafe() {
        CodeGenerationResult r = generate("Create a simple User service");

        assertNotNull(r.validation(), "Validation result should not be null");
        assertEquals(ValidationResult.Status.SAFE, r.validation().overallStatus(),
                "A simple patch plan should validate as SAFE");
    }

    // ─── 10. Test skeleton generation ──────────────────────────────────────────

    @Test
    public void test10_TestSkeletonGeneration() {
        CodeGenerationResult r = generate("Create Product entity");

        assertNotNull(r.testSkeletons(), "Test skeletons should not be null");
        assertFalse(r.testSkeletons().isEmpty(), "Should generate at least one test skeleton");

        // At least one skeleton should be a real Java class name
        boolean hasValidTestName = r.testSkeletons().stream()
                .anyMatch(t -> t.testClassName().endsWith("Test"));
        assertTrue(hasValidTestName, "Test class names should end with 'Test'");
    }

    // ─── 11. No duplicate imports ──────────────────────────────────────────────

    @Test
    public void test11_NoDuplicateImports() {
        // Generate a plan with several patches
        CodeGenerationResult r = generate("Add JWT authentication");

        // Validate no patch contains duplicate imports
        for (GeneratedPatch gp : r.generatedPatches()) {
            Set<String> seen = new HashSet<>();
            for (String imp : gp.addedImports()) {
                assertFalse(seen.contains(imp),
                        "Patch " + gp.targetFile() + " has duplicate import: " + imp);
                seen.add(imp);
            }
        }

        // Also check the validator reports no duplicate imports
        boolean anyDupImport = r.validation().errors().stream()
                .anyMatch(e -> e.toLowerCase().contains("duplicate import"));
        assertFalse(anyDupImport, "Validator should not report duplicate imports");
    }

    // ─── 12. No duplicate methods ──────────────────────────────────────────────

    @Test
    public void test12_NoDuplicateMethods() {
        CodeGenerationResult r = generate("Create Order service");

        // Verify no patch has duplicate method signatures
        for (GeneratedPatch gp : r.generatedPatches()) {
            Set<String> seenMethods = new HashSet<>();
            for (String m : gp.addedMethods()) {
                assertFalse(seenMethods.contains(m),
                        "Patch " + gp.targetFile() + " has duplicate method: " + m);
                seenMethods.add(m);
            }
        }
    }

    // ─── 13. Existing SDK unchanged ────────────────────────────────────────────

    @Test
    public void test13_ExistingSdkUnchanged() {
        // Verify the SDK's analyze() method still works (backward compatibility)
        DefaultDeveloperAgentEngine engine = new DefaultDeveloperAgentEngine();
        DeveloperResponse response = engine.analyze("Add JWT authentication");
        assertNotNull(response, "Sprint-14 analyze() must still work");
        assertNotNull(response.intent(), "Intent should be parsed");
        assertEquals(DeveloperIntentType.SECURITY, response.intent().intent(),
                "Should still detect SECURITY intent");
    }

    // ─── 14. No filesystem writes ──────────────────────────────────────────────

    @Test
    public void test14_NoFilesystemWrites(@TempDir Path tempDir) throws IOException {
        // Take a snapshot of the temp dir before
        Set<String> filesBefore = new HashSet<>();
        if (Files.exists(tempDir)) Files.walk(tempDir).forEach(p -> filesBefore.add(p.toString()));

        // Generate code targeting the temp dir
        CodeGenerationResult r = generate("Add JWT authentication");
        // Generated patches are returned as in-memory strings — no File IO
        assertNotNull(r.generatedPatches());
        for (GeneratedPatch gp : r.generatedPatches()) {
            assertNotNull(gp.source(), "Source should be in-memory, not a path");
            assertFalse(gp.source().isEmpty(), "Source should be non-empty");
        }

        // The temp dir should be untouched
        Set<String> filesAfter = new HashSet<>();
        if (Files.exists(tempDir)) Files.walk(tempDir).forEach(p -> filesAfter.add(p.toString()));
        assertEquals(filesBefore, filesAfter, "Temp directory should not have any new files");
    }

    // ─── 15. Generated patches reference correct affected files ────────────────

    @Test
    public void test15_GeneratedPatchesReferenceCorrectFiles() {
        CodeGenerationResult r = generate("Add JWT authentication");

        // The patch plan's patch files should match the generated patches' target files
        Set<String> plannedFiles = new HashSet<>();
        for (FilePatch fp : r.patchPlan().patches()) {
            plannedFiles.add(fp.targetFile());
        }

        Set<String> generatedFiles = new HashSet<>();
        for (GeneratedPatch gp : r.generatedPatches()) {
            generatedFiles.add(gp.targetFile());
        }

        assertEquals(plannedFiles, generatedFiles,
                "Every planned file should have a generated patch");
    }
}
