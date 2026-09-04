package com.shreeai.os.platform.kernels.project;

import com.shreeai.os.platform.kernels.project.engine.DefaultProjectIntelligenceEngine;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectDependency;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;
import com.shreeai.os.platform.kernels.project.model.ProjectGraph;
import com.shreeai.os.platform.kernels.project.model.ProjectImpact;
import com.shreeai.os.platform.kernels.project.model.ProjectMethod;
import com.shreeai.os.platform.kernels.project.model.ProjectStatistics;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;
import com.shreeai.os.platform.kernels.project.parser.JavaAstParser;
import com.shreeai.os.platform.kernels.project.scanner.RepositoryScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Project Intelligence Kernel (Sprint-13).
 */
public class ProjectIntelligenceTest {

    private static final String TEST_PROJECT = "C:/shree-ai-os";

    @Test
    void repositoryScanner_findsJavaFiles() throws IOException {
        RepositoryScanner scanner = new RepositoryScanner(Path.of(TEST_PROJECT));
        List<Path> javaFiles = scanner.findJavaFiles();
        assertNotNull(javaFiles);
        assertFalse(javaFiles.isEmpty(), "Should find at least some Java files");
    }

    @Test
    void repositoryScanner_findsConfigFiles() throws IOException {
        RepositoryScanner scanner = new RepositoryScanner(Path.of(TEST_PROJECT));
        List<Path> configs = scanner.findConfigFiles();
        assertNotNull(configs);
        assertFalse(configs.isEmpty(), "Should find pom.xml");
    }

    @Test
    void javaAstParser_parsesClass() {
        String source = """
                package com.example;

                import org.springframework.stereotype.Service;

                @Service
                public class UserService {
                    public String greet(String name) {
                        return "Hello, " + name;
                    }
                }
                """;
        JavaAstParser parser = new JavaAstParser();
        ProjectClass cls = parser.parse(source, "UserService.java");

        assertNotNull(cls);
        assertEquals("UserService", cls.name());
        assertEquals("com.example.UserService", cls.fullyQualifiedName());
        assertEquals("com.example", cls.packageName());
        assertEquals(ProjectClass.Kind.CLASS, cls.kind());
        assertEquals(ProjectClass.Role.SERVICE, cls.role());
        assertEquals(List.of("Service"), cls.annotations());
        assertEquals(List.of("public"), cls.modifiers());
    }

    @Test
    void javaAstParser_parsesHttpMapping() {
        String source = """
                package com.example;

                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {
                    @GetMapping("/{id}")
                    public User getUser(@PathVariable Long id) {
                        return null;
                    }

                    @PostMapping
                    public User createUser(@RequestBody User user) {
                        return user;
                    }
                }
                """;
        JavaAstParser parser = new JavaAstParser();
        ProjectClass cls = parser.parse(source, "UserController.java");

        assertNotNull(cls);
        assertEquals(ProjectClass.Role.CONTROLLER, cls.role());
        assertEquals(2, cls.methods().size());

        ProjectMethod getMethod = cls.methods().stream()
                .filter(m -> "getUser".equals(m.name())).findFirst().orElseThrow();
        assertEquals("GET", getMethod.httpMethod());
        // Method-level path (class-level @RequestMapping prefixing is a
        // Sprint-13 enhancement done by SpringAnalyzer at full project
        // analysis time).
        assertEquals("/{id}", getMethod.httpPath());
    }

    @Test
    void javaAstParser_parsesEnum() {
        String source = """
                package com.example;
                public enum Status {
                    ACTIVE, INACTIVE, PENDING
                }
                """;
        JavaAstParser parser = new JavaAstParser();
        ProjectClass cls = parser.parse(source, "Status.java");

        assertNotNull(cls);
        assertEquals(ProjectClass.Kind.ENUM, cls.kind());
        assertEquals("Status", cls.name());
    }

    @Test
    void javaAstParser_parsesRecord() {
        String source = """
                package com.example;
                public record User(String name, int age) {
                }
                """;
        JavaAstParser parser = new JavaAstParser();
        ProjectClass cls = parser.parse(source, "User.java");

        assertNotNull(cls);
        assertEquals(ProjectClass.Kind.RECORD, cls.kind());
        assertEquals(2, cls.fields().size());
    }

    @Test
    void projectEngine_analyzesProject() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        ProjectSummary summary = engine.analyze(Path.of(TEST_PROJECT));

        assertNotNull(summary);
        assertEquals("shree-ai-os", summary.projectName());
        assertEquals("MAVEN", summary.buildSystem());
        assertNotNull(summary.statistics());
        assertTrue(summary.statistics().classCount() > 0, "Should discover some classes");
    }

    @Test
    void projectEngine_extractsControllers() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        engine.analyze(Path.of(TEST_PROJECT));
        ProjectGraph graph = engine.getGraph();

        assertNotNull(graph);
        assertTrue(graph.size() > 0, "Graph should have nodes");
    }

    @Test
    void projectGraph_impactAnalysis() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        engine.analyze(Path.of(TEST_PROJECT));

        ProjectImpact impact = engine.impact("ShreeAI");
        assertNotNull(impact);
        assertEquals("ShreeAI", impact.target());
        // ShreeAI is used by other classes so should have some impact
    }

    @Test
    void projectEngine_statisticsCounts() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        ProjectSummary summary = engine.analyze(Path.of(TEST_PROJECT));

        ProjectStatistics stats = summary.statistics();
        assertNotNull(stats);
        assertTrue(stats.methodCount() >= 0);
        assertTrue(stats.endpointCount() >= 0);
    }

    @Test
    void projectEngine_detectsSpringBoot() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        ProjectSummary summary = engine.analyze(Path.of(TEST_PROJECT));
        // shree-ai-os is a Spring Boot project
        assertEquals("SPRING_BOOT", summary.framework());
    }

    @Test
    void projectEngine_findClass() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        engine.analyze(Path.of(TEST_PROJECT));

        ProjectClass found = engine.findClass("ShreeAI");
        assertNotNull(found, "Should find ShreeAI class");
        assertEquals("ShreeAI", found.name());
    }

    @Test
    void dependencyGraphBuilder_producesEdges() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        engine.analyze(Path.of(TEST_PROJECT));
        ProjectGraph graph = engine.getGraph();

        List<ProjectDependency> edges = graph.edges();
        assertNotNull(edges);
        assertFalse(edges.isEmpty(), "Should produce dependency edges");
    }

    @Test
    void projectGraph_cycleDetection() throws IOException {
        DefaultProjectIntelligenceEngine engine = new DefaultProjectIntelligenceEngine();
        engine.analyze(Path.of(TEST_PROJECT));
        ProjectGraph graph = engine.getGraph();

        List<List<String>> cycles = graph.detectCycles();
        assertNotNull(cycles, "Cycle detection should return a list (possibly empty)");
    }
}
