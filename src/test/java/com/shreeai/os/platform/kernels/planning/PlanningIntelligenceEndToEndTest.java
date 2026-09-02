package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.engine.planners.DomainPlannerRegistry;
import com.shreeai.os.platform.kernels.planning.analyzer.PlanningAnalyzer;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end smoke test: verifies that Sprint-11 produces a rich markdown
 * plan from a sample user input. Mirrors the expected output documented
 * in the Sprint-11 spec.
 */
@DisplayName("Sprint-11: Planning Intelligence E2E")
public class PlanningIntelligenceEndToEndTest {

    @Test
    @DisplayName("E2E: 'create roadmap for java developer' produces structured plan")
    void javaDeveloperRoadmapProducesStructuredPlan() {
        String userInput = "create roadmap for java developer";

        PlanningAnalyzer analyzer = new PlanningAnalyzer();
        DomainPlannerRegistry registry = new DomainPlannerRegistry();

        PlanningAnalysisResult analysis = analyzer.analyze(userInput);
        assertEquals(Domain.JAVA, analysis.domain(),
                "Domain should be JAVA for: " + userInput);

        PlanBlueprint blueprint = registry.buildPlan(analysis);

        // Verify all required Sprint-11 structured fields
        assertNotNull(blueprint.title());
        assertNotNull(blueprint.goal());
        assertTrue(blueprint.timelineWeeks() > 0);

        // Phases
        assertFalse(blueprint.phases().isEmpty(), "Should have phases");
        blueprint.phases().forEach(p -> {
            assertNotNull(p.title());
            assertNotNull(p.objective());
            assertTrue(p.durationWeeks() > 0);
            assertFalse(p.deliverables().isEmpty(),
                    "Phase should have deliverables: " + p);
        });

        // Milestones
        assertFalse(blueprint.milestones().isEmpty(), "Should have milestones");

        // Risks
        assertFalse(blueprint.risks().isEmpty(), "Should have risks");

        // Success metrics
        assertFalse(blueprint.successMetrics().isEmpty(),
                "Should have success metrics");

        // Recommendations
        assertFalse(blueprint.recommendations().isEmpty(),
                "Should have recommendations");

        // Print the plan to console (for manual visual verification)
        System.out.println("\n=== GENERATED PLAN ===");
        System.out.println("Title: " + blueprint.title());
        System.out.println("Goal: " + blueprint.goal());
        System.out.println("Timeline: " + blueprint.timelineWeeks() + " weeks");
        System.out.println("Phases: " + blueprint.phases().size());
        System.out.println("Milestones: " + blueprint.milestones().size());
        System.out.println("Risks: " + blueprint.risks().size());
        System.out.println("Success Metrics: " + blueprint.successMetrics().size());
        System.out.println("======================\n");
    }

    @Test
    @DisplayName("E2E: AI assistant produces ML and LLM phases")
    void aiAssistantRoadmapProducesMLPhases() {
        PlanningAnalyzer analyzer = new PlanningAnalyzer();
        DomainPlannerRegistry registry = new DomainPlannerRegistry();

        PlanningAnalysisResult analysis = analyzer.analyze(
                "create roadmap for AI assistant"
        );
        assertEquals(Domain.AI, analysis.domain());

        PlanBlueprint blueprint = registry.buildPlan(analysis);

        assertNotNull(blueprint);
        assertTrue(blueprint.phases().size() >= 5,
                "AI plan should have at least 5 phases, got: "
                        + blueprint.phases().size());

        // The plan should mention key AI terms
        String fullGoal = blueprint.goal() + " " + blueprint.phases().stream()
                .map(p -> p.title() + " " + p.objective())
                .reduce("", (a, b) -> a + " " + b).toLowerCase();

        assertTrue(fullGoal.contains("ai") || fullGoal.contains("ml")
                        || fullGoal.contains("llm") || fullGoal.contains("agent"),
                "Plan should mention AI/ML/LLM/agent: " + fullGoal);
    }

    @Test
    @DisplayName("E2E: SaaS produces product launch phases")
    void saasProductRoadmapProducesLaunchPhases() {
        PlanningAnalyzer analyzer = new PlanningAnalyzer();
        DomainPlannerRegistry registry = new DomainPlannerRegistry();

        PlanningAnalysisResult analysis = analyzer.analyze(
                "build a SaaS product roadmap"
        );
        assertEquals(Domain.SAAS, analysis.domain());

        PlanBlueprint blueprint = registry.buildPlan(analysis);

        assertNotNull(blueprint);
        assertTrue(blueprint.phases().size() >= 5,
                "SaaS plan should have at least 5 phases, got: "
                        + blueprint.phases().size());
        assertTrue(blueprint.timelineWeeks() >= 12,
                "SaaS plan should be at least 12 weeks, got: "
                        + blueprint.timelineWeeks());
    }
}
