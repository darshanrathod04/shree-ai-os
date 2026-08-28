package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EO-V1-003 — Goal Intelligence Engine deterministic decomposition tests.
 *
 * <p>Verifies keyword-based domain classification produces meaningful,
 * deterministic subtasks and never emits placeholder names.</p>
 */
public class GoalIntelligenceEngineDecompositionTest {

    private final GoalIntelligenceEngine engine =
            new GoalIntelligenceEngine();

    private List<String> subtasksFor(String goal) {
        GoalAnalysis analysis =
                engine.analyze(GoalRequest.of(goal));
        return analysis.subtasks();
    }

    @Test
    public void testGymDomainProducesWorkoutSubtasks() {

        List<String> subtasks = subtasksFor(
                "Create a 3-day beginner Push Pull Legs workout");

        assertTrue(subtasks.contains("Push workout"),
                "Expected Push workout in: " + subtasks);
        assertTrue(subtasks.contains("Pull workout"),
                "Expected Pull workout in: " + subtasks);
        assertTrue(subtasks.contains("Legs workout"),
                "Expected Legs workout in: " + subtasks);
        assertTrue(subtasks.contains("Recovery strategy"),
                "Expected Recovery strategy in: " + subtasks);
    }

    @Test
    public void testSoftwareDomainProducesEngineeringSubtasks() {

        List<String> subtasks = subtasksFor(
                "Build a backend software application with a database");

        assertTrue(subtasks.contains("Architecture"));
        assertTrue(subtasks.contains("Backend"));
        assertTrue(subtasks.contains("Frontend"));
        assertTrue(subtasks.contains("Database"));
        assertTrue(subtasks.contains("Testing"));
        assertTrue(subtasks.contains("Deployment"));
    }

    @Test
    public void testWebsiteDomainProducesPageSubtasks() {

        List<String> subtasks = subtasksFor(
                "Create a portfolio website with a landing page");

        assertTrue(subtasks.contains("Landing page"));
        assertTrue(subtasks.contains("About"));
        assertTrue(subtasks.contains("Projects"));
        assertTrue(subtasks.contains("Contact"));
        assertTrue(subtasks.contains("Deployment"));
    }

    @Test
    public void testGeneralProjectFallback() {

        List<String> subtasks = subtasksFor(
                "Organize the annual team offsite");

        assertEquals(
                List.of(
                        "Research",
                        "Planning",
                        "Implementation",
                        "Testing",
                        "Review"),
                subtasks);
    }

    @Test
    public void testNeverEmitsPlaceholderNames() {

        List<String> gym = subtasksFor(
                "Create a 3-day beginner Push Pull Legs workout");
        List<String> software = subtasksFor(
                "Build a backend software application");
        List<String> website = subtasksFor(
                "Create a portfolio website");
        List<String> fallback = subtasksFor(
                "Organize the annual team offsite");

        for (String subtask : java.util.stream.Stream.of(gym, software, website, fallback)
                .flatMap(List::stream)
                .toList()) {

            assertFalse(subtask.contains("PLANNING_CREATE"),
                    "Placeholder names must never be emitted: " + subtask);
            assertFalse(subtask.equalsIgnoreCase("PLANNING_CREATE"),
                    "Placeholder names must never be emitted: " + subtask);
        }
    }

    @Test
    public void testDecompositionIsDeterministic() {

        List<String> first = subtasksFor(
                "Create a 3-day beginner Push Pull Legs workout");
        List<String> second = subtasksFor(
                "Create a 3-day beginner Push Pull Legs workout");

        assertEquals(first, second,
                "Decomposition must be deterministic");
    }
}
