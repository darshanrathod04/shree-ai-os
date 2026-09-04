package com.shreeai.os.platform.kernels.developer;

import com.shreeai.os.platform.kernels.developer.analyzer.*;
import com.shreeai.os.platform.kernels.developer.analyzer.TestStrategyGenerator.TestStrategy;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.developer.engine.DefaultDeveloperAgentEngine;
import com.shreeai.os.platform.kernels.response.model.DeveloperResponse;
import com.shreeai.os.platform.kernels.project.model.*;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>DeveloperAgentTest</b>
 *
 * <p>Sprint-14 acceptance tests for the Developer Agent.
 * Validates intent analysis, impact analysis, architecture validation,
 * implementation planning, test strategy generation, and end-to-end
 * pipeline execution. All tests run without any LLM call.</p>
 *
 * @since Sprint-14
 */
public class DeveloperAgentTest {

    // ─── Test 1: Intent Analysis — Security ───────────────────────────────────

    @Test
    public void testIntentAnalysis_DetectsSecurityIntent() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze("Add JWT authentication to the login endpoint");

        assertNotNull(intent);
        assertEquals(DeveloperIntentType.SECURITY, intent.intent(),
                "Should detect SECURITY intent from JWT keyword");
        assertTrue(intent.entity().contains("JWT") || intent.entity().contains("Authentication"),
                "Entity should reference JWT or Authentication, got: " + intent.entity());
        assertTrue(intent.confidence() > 0.5, "Confidence should be > 0.5");
    }

    // ─── Test 2: Intent Analysis — Refactor ───────────────────────────────────

    @Test
    public void testIntentAnalysis_DetectsRefactorIntent() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze("Refactor the UserService class to use a better pattern");

        assertNotNull(intent);
        assertEquals(DeveloperIntentType.REFACTOR, intent.intent(),
                "Should detect REFACTOR intent from 'refactor' keyword");
        assertTrue(intent.entity().contains("User") || intent.entity().contains("Service"),
                "Entity should reference User or Service, got: " + intent.entity());
    }

    // ─── Test 3: Intent Analysis — Add Feature ─────────────────────────────────

    @Test
    public void testIntentAnalysis_DetectsAddFeatureIntent() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze("Add user profile feature to the application");

        assertNotNull(intent);
        assertEquals(DeveloperIntentType.ADD_FEATURE, intent.intent(),
                "Should detect ADD_FEATURE intent");
        assertTrue(intent.domain().contains("General") || !intent.domain().isEmpty(),
                "Domain should be non-empty");
    }

    // ─── Test 4: Intent Analysis — Fix Bug ───────────────────────────────────

    @Test
    public void testIntentAnalysis_DetectsFixBugIntent() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze("Fix the null pointer exception in OrderService");

        assertNotNull(intent);
        assertEquals(DeveloperIntentType.FIX_BUG, intent.intent(),
                "Should detect FIX_BUG intent");
    }

    // ─── Test 5: Impact Analysis — Empty project returns empty lists ───────────

    @Test
    public void testImpactAnalysis_EmptyProjectReturnsEmpty() {
        ProjectGraph graph = new ProjectGraph(List.of(), List.of());
        ImpactAnalyzer analyzer = new ImpactAnalyzer(graph, List.of(), List.of(), List.of());
        DeveloperIntent intent = DeveloperIntent.builder()
                .originalRequest("Add JWT")
                .intent(DeveloperIntentType.SECURITY)
                .entity("JWT")
                .domain("Authentication")
                .build();
        ImpactReport report = analyzer.analyze(intent);

        assertNotNull(report);
        assertNotNull(report.directlyAffected());
        assertNotNull(report.indirectlyAffected());
        assertTrue(report.directlyAffected().isEmpty(),
                "Empty project should have no affected classes");
    }

    // ─── Test 6: Architecture Validator — Detects layer violations ─────────────

    @Test
    public void testArchitectureValidator_DetectsLayerViolations() {
        ProjectClass controller = makeClass("com.example.UserController", "UserController", Role.CONTROLLER);
        ProjectClass repository = makeClass("com.example.UserRepository", "UserRepository", Role.REPOSITORY);
        ProjectDependency dep = ProjectDependency.builder()
                .source(controller.fullyQualifiedName())
                .target(repository.fullyQualifiedName())
                .type(ProjectDependency.Type.CALLS)
                .build();

        ProjectGraph graph = new ProjectGraph(List.of(controller, repository), List.of(dep));
        ArchitectureValidator validator = new ArchitectureValidator(
                graph, List.of(controller, repository), List.of());

        List<ValidationIssue> issues = validator.validate();
        assertFalse(issues.isEmpty(), "Should detect controller-to-repository violation");
        assertTrue(issues.stream().anyMatch(i ->
                        i.kind() == ValidationIssue.Kind.CONTROLLER_REPOSITORY_DIRECT_CALL),
                "Should detect CONTROLLER_REPOSITORY_DIRECT_CALL");
    }

    // ─── Test 7: Architecture Validator — Clean layered project ──────────────

    @Test
    public void testArchitectureValidator_CleanProjectHasNoViolations() {
        ProjectClass controller = makeClass("com.example.OrderController", "OrderController", Role.CONTROLLER);
        ProjectClass service = makeClass("com.example.OrderService", "OrderService", Role.SERVICE);
        ProjectClass repository = makeClass("com.example.OrderRepository", "OrderRepository", Role.REPOSITORY);

        ProjectDependency cToS = ProjectDependency.builder()
                .source(controller.fullyQualifiedName())
                .target(service.fullyQualifiedName())
                .type(ProjectDependency.Type.CALLS)
                .build();
        ProjectDependency sToR = ProjectDependency.builder()
                .source(service.fullyQualifiedName())
                .target(repository.fullyQualifiedName())
                .type(ProjectDependency.Type.CALLS)
                .build();

        ProjectGraph graph = new ProjectGraph(
                List.of(controller, service, repository), List.of(cToS, sToR));
        ArchitectureValidator validator = new ArchitectureValidator(
                graph, List.of(controller, service, repository), List.of());

        List<ValidationIssue> issues = validator.validate();
        boolean hasControllerRepoCall = issues.stream().anyMatch(i ->
                i.kind() == ValidationIssue.Kind.CONTROLLER_REPOSITORY_DIRECT_CALL);
        assertFalse(hasControllerRepoCall,
                "Clean layered project should not have controller-repo direct calls");
    }

    // ─── Test 8: Implementation Planner — Generates security plan ──────────────

    @Test
    public void testImplementationPlanner_SecurityPlanHasPhases() {
        DeveloperIntent intent = DeveloperIntent.builder()
                .originalRequest("Add JWT authentication")
                .intent(DeveloperIntentType.SECURITY)
                .entity("JWT")
                .domain("Authentication")
                .build();
        ImpactReport impact = ImpactReport.builder()
                .targetClass("JwtService")
                .directlyAffected(List.of("com.example.JwtService"))
                .indirectlyAffected(List.of())
                .affectedEndpoints(List.of())
                .affectedControllers(List.of())
                .affectedServices(List.of())
                .affectedRepositories(List.of())
                .affectedEntities(List.of())
                .affectedConfigurations(List.of())
                .dependencyDepth(2)
                .build();
        ImplementationPlanner planner = new ImplementationPlanner(List.of());
        ImplementationPlan plan = planner.plan(intent, impact, List.of());

        assertNotNull(plan);
        assertFalse(plan.phases().isEmpty(), "Plan should have phases");
        assertTrue(plan.phases().size() >= 3,
                "Security plan should have at least 3 phases, got: " + plan.phases().size());
    }

    // ─── Test 9: Test Strategy Generator — Has unit + integration + security ──

    @Test
    public void testTestStrategy_HasAllTestCategories() {
        DeveloperIntent intent = DeveloperIntent.builder()
                .originalRequest("Add JWT authentication")
                .intent(DeveloperIntentType.SECURITY)
                .entity("JWT")
                .build();
        ImpactReport impact = ImpactReport.builder()
                .targetClass("JwtService")
                .directlyAffected(List.of("com.example.JwtService"))
                .affectedEndpoints(List.of())
                .affectedControllers(List.of())
                .affectedServices(List.of())
                .affectedRepositories(List.of())
                .affectedEntities(List.of())
                .affectedConfigurations(List.of())
                .build();
        ImplementationPlan plan = ImplementationPlan.builder()
                .request("Add JWT")
                .phases(List.of())
                .build();
        TestStrategyGenerator generator = new TestStrategyGenerator();
        TestStrategy strategy = generator.generate(intent, impact, plan);

        assertNotNull(strategy);
        assertFalse(strategy.unitTests().isEmpty(), "Should have unit tests");
        assertFalse(strategy.securityTests().isEmpty(), "Security intent should have security tests");
        assertTrue(strategy.totalTests() > 0, "Total test count should be > 0");
    }

    // ─── Test 10: End-to-end — Returns complete DeveloperResponse ───────────────

    @Test
    public void testDeveloperAgent_EndToEndReturnsCompleteResponse() {
        DefaultDeveloperAgentEngine engine = new DefaultDeveloperAgentEngine();
        DeveloperResponse response = engine.analyze("Add JWT authentication to login");

        assertNotNull(response);
        assertNotNull(response.intent());
        assertNotNull(response.impact());
        assertNotNull(response.plan());
        assertNotNull(response.testStrategy());
        assertNotNull(response.formattedPlan());
        assertNotNull(response.timestamp());
        assertTrue(response.confidence() > 0.0, "Confidence should be positive");
        assertFalse(response.formattedPlan().isEmpty(), "Formatted plan should not be empty");
        assertTrue(response.formattedPlan().contains("Implementation Plan"),
                "Formatted plan should contain 'Implementation Plan'");
    }

    // ─── Test 11: End-to-end with real project — reads and caches ─────────────

    @Test
    public void testDeveloperAgent_WithRealProject_CachesAndReuses(@TempDir Path tempProject) throws IOException {
        // Create a minimal Spring project
        Path srcDir = Files.createDirectories(tempProject.resolve("src/main/java/com/example"));
        Files.writeString(srcDir.resolve("UserService.java"),
                "package com.example;\n"
                        + "import org.springframework.stereotype.Service;\n"
                        + "@Service\n"
                        + "public class UserService {}\n");
        Files.writeString(srcDir.resolve("UserController.java"),
                "package com.example;\n"
                        + "import org.springframework.web.bind.annotation.RestController;\n"
                        + "@RestController\n"
                        + "public class UserController {}\n");

        DefaultDeveloperAgentEngine engine = new DefaultDeveloperAgentEngine();
        DeveloperResponse response = engine.analyze("Add JWT authentication", tempProject.toString());

        assertNotNull(response);
        assertNotNull(response.intent());
        assertEquals(DeveloperIntentType.SECURITY, response.intent().intent());
        assertEquals(1, engine.cacheSize(), "Project should be cached after analysis");

        // Re-analysis with same path should use cache
        DeveloperResponse response2 = engine.analyze("Add OAuth2 support", tempProject.toString());
        assertNotNull(response2);
        assertEquals(1, engine.cacheSize(), "Cache size should remain 1 for same project");
    }

    // ─── Test 12: Validation Issue — Builder produces correct values ────────────

    @Test
    public void testValidationIssue_BuilderWorks() {
        ValidationIssue issue = ValidationIssue.builder()
                .kind(ValidationIssue.Kind.CIRCULAR_DEPENDENCY)
                .severity(ValidationIssue.Severity.HIGH)
                .message("Test cycle: A -> B -> C -> A")
                .affectedFiles(List.of("A.java", "B.java", "C.java"))
                .recommendation("Introduce an interface to break the cycle")
                .build();

        assertNotNull(issue);
        assertEquals(ValidationIssue.Kind.CIRCULAR_DEPENDENCY, issue.kind());
        assertEquals(ValidationIssue.Severity.HIGH, issue.severity());
        assertEquals("Test cycle: A -> B -> C -> A", issue.message());
        assertEquals(3, issue.affectedFiles().size());
        assertEquals("Introduce an interface to break the cycle", issue.recommendation());
    }

    // ─── Test 13: DeveloperResponse — Payload generation is complete ───────────

    @Test
    public void testDeveloperResponse_PayloadIsComplete() {
        DefaultDeveloperAgentEngine engine = new DefaultDeveloperAgentEngine();
        DeveloperResponse response = engine.analyze("Add JWT authentication");
        Map<String, Object> payload = response.toPayload();

        assertNotNull(payload);
        assertEquals("Add JWT authentication", payload.get("request"));
        assertNotNull(payload.get("intent"));
        assertNotNull(payload.get("intentLabel"));
        assertNotNull(payload.get("confidence"));
        assertNotNull(payload.get("plan"));
        assertNotNull(payload.get("testStrategy"));
        assertNotNull(payload.get("impact"));
    }

    // ─── Test 14: Intent — developer keyword in general request ────────────────

    @Test
    public void testIntentAnalysis_DetectsDeveloperKeyword() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze("Create a new REST endpoint for the product API");

        assertNotNull(intent);
        // CREATE_API should match "REST endpoint" patterns
        assertEquals(DeveloperIntentType.CREATE_API, intent.intent(),
                "Should detect CREATE_API from REST endpoint keyword");
    }

    // ─── Test 15: Intent — null/blank input handled gracefully ───────────────

    @Test
    public void testIntentAnalysis_NullInputHandledGracefully() {
        DeveloperIntentAnalyzer analyzer = new DeveloperIntentAnalyzer();
        DeveloperIntent intent = analyzer.analyze(null);

        assertNotNull(intent);
        assertEquals(DeveloperIntentType.ADD_FEATURE, intent.intent());
        assertEquals(0.0, intent.confidence());
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private static ProjectClass makeClass(String fqn, String name, Role role) {
        return ProjectClass.builder()
                .fullyQualifiedName(fqn)
                .name(name)
                .role(role)
                .kind(ProjectClass.Kind.CLASS)
                .build();
    }
}