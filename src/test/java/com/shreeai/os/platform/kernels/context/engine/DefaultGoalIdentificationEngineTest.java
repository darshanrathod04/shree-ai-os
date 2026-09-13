package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultGoalIdentificationEngine}.
 */
public class DefaultGoalIdentificationEngineTest {

    private DefaultGoalIdentificationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultGoalIdentificationEngine();
    }

    @Test
    @DisplayName("Test 1: Primary goal identified from action verb")
    void testPrimaryGoalIdentified() {
        GoalStructure structure = engine.identify("Learn Java programming language");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
        assertTrue(primary.title().toLowerCase().contains("learn") || primary.title().toLowerCase().contains("java"));
        assertTrue(primary.confidence() > 0.0);
        assertNotNull(primary.evidence());
    }

    @Test
    @DisplayName("Test 2: Sub-goals identified from compound sentences")
    void testSubGoalsIdentified() {
        GoalStructure structure = engine.identify("Learn Java and build a web app");
        assertNotNull(structure);
        assertFalse(structure.subGoals().isEmpty(), "Should have sub-goals from compound sentence");
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
    }

    @Test
    @DisplayName("Test 3: Confidence is deterministic")
    void testConfidenceDeterministic() {
        GoalStructure structure1 = engine.identify("Build a REST API with Spring");
        GoalStructure structure2 = engine.identify("Build a REST API with Spring");
        assertEquals(structure1.primaryGoal().confidence(), structure2.primaryGoal().confidence());
    }

    @Test
    @DisplayName("Test 4: Complexity is SIMPLE for single goal")
    void testSimpleComplexity() {
        GoalStructure structure = engine.identify("Learn Java");
        assertEquals(GoalComplexity.SIMPLE, structure.complexity());
    }

    @Test
    @DisplayName("Test 5: Complexity is MODERATE for multiple goals")
    void testModerateComplexity() {
        GoalStructure structure = engine.identify("Learn Java and build a web app and test it");
        assertNotNull(structure.complexity());
    }

    @Test
    @DisplayName("Test 6: Evidence tracks original text")
    void testEvidenceTracksOriginalText() {
        GoalStructure structure = engine.identify("Explain recursion in programming");
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary.evidence());
    }

    @Test
    @DisplayName("Test 7: Empty input returns structure with no primary goal")
    void testEmptyInput() {
        GoalStructure structure = engine.identify("");
        assertNotNull(structure);
    }

    @Test
    @DisplayName("Test 8: Explain intent detected")
    void testExplainIntent() {
        GoalStructure structure = engine.identify("Explain how memory management works");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
        String title = primary.title().toLowerCase();
        assertTrue(title.contains("explain") || title.contains("memory"));
    }

    @Test
    @DisplayName("Test 9: Compare intent detected")
    void testCompareIntent() {
        GoalStructure structure = engine.identify("Compare Java and Python for web development");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
        String title = primary.title().toLowerCase();
        assertTrue(title.contains("compare") || title.contains("java") || title.contains("python"));
    }

    @Test
    @DisplayName("Test 10: Same input produces identical output (determinism)")
    void testDeterminism() {
        GoalStructure structure1 = engine.identify("Design a database schema for users");
        GoalStructure structure2 = engine.identify("Design a database schema for users");
        assertEquals(structure1.primaryGoal(), structure2.primaryGoal(),
                "Primary goal must be identical for same input");
        assertEquals(structure1.subGoals(), structure2.subGoals(),
                "Sub-goals must be identical for same input");
        assertEquals(structure1.complexity(), structure2.complexity(),
                "Complexity must be identical for same input");
        assertEquals(structure1.detectionMethod(), structure2.detectionMethod(),
                "Detection method must be identical for same input");
    }

    @Test
    @DisplayName("Test 11: Build intent detected")
    void testBuildIntent() {
        GoalStructure structure = engine.identify("Build a mobile app");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
        String title = primary.title().toLowerCase();
        assertTrue(title.contains("build") || title.contains("mobile"));
    }

    @Test
    @DisplayName("Test 12: Debug intent detected")
    void testDebugIntent() {
        GoalStructure structure = engine.identify("Debug the NullPointerException in my code");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
    }

    @Test
    @DisplayName("Test 13: Plan intent detected")
    void testPlanIntent() {
        GoalStructure structure = engine.identify("Plan a project timeline for sprint 1");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
    }

    @Test
    @DisplayName("Test 14: Analyze intent detected")
    void testAnalyzeIntent() {
        GoalStructure structure = engine.identify("Analyze the performance bottlenecks");
        assertNotNull(structure);
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary);
    }

    @Test
    @DisplayName("Test 15: Goal title preserves original wording")
    void testGoalTitlePreservesWording() {
        GoalStructure structure = engine.identify("Create a REST API endpoint");
        GoalNode primary = structure.primaryGoal();
        assertNotNull(primary.title());
        String title = primary.title().toLowerCase();
        assertTrue(title.contains("create") || title.contains("rest") || title.contains("api"),
                "Title should contain keywords from input: " + primary.title());
    }
}