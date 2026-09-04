package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.analyzer.PlanningAnalyzer;
import com.shreeai.os.platform.kernels.planning.engine.MilestoneGenerator;
import com.shreeai.os.platform.kernels.planning.engine.TaskGraphBuilder;
import com.shreeai.os.platform.kernels.planning.engine.planners.*;
import com.shreeai.os.platform.kernels.planning.model.*;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Complexity;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.PlanningType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlanningIntelligenceTest — Sprint-11 acceptance tests.
 *
 * <p>Validates that the Planning Kernel produces domain-aware structured
 * plans with phases, milestones, risks, and deliverables.</p>
 *
 * <p>All tests are deterministic and require no external dependencies.</p>
 */
@DisplayName("Sprint-11: Planning Intelligence")
public class PlanningIntelligenceTest {

    private PlanningAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new PlanningAnalyzer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 1: Java roadmap generates Java-specific phases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Java roadmap generates Java-specific phases")
    void javaRoadmapGeneratesJavaPhases() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Create a roadmap for Java developer"
        );

        assertEquals(Domain.JAVA, analysis.domain(),
                "Domain should be JAVA for 'Java developer' input");
        assertEquals(PlanningType.ROADMAP, analysis.planningType(),
                "PlanningType should be ROADMAP for 'roadmap' input, got: "
                        + analysis.planningType());
        assertTrue(analysis.keywords().contains("java"),
                "Keywords should contain 'java'");
        assertTrue(analysis.keywords().contains("developer"),
                "Keywords should contain 'developer'");

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        assertNotNull(blueprint, "Blueprint should not be null");
        assertFalse(blueprint.phases().isEmpty(),
                "Java roadmap should have at least one phase");

        // Verify domain-specific phase titles
        List<String> phaseTitles = blueprint.phases().stream()
                .map(Phase::title)
                .toList();

        assertTrue(phaseTitles.stream().anyMatch(t -> t.contains("Java") || t.contains("Fundamentals")),
                "Should contain Java/Fundamentals phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t -> t.contains("Spring") || t.contains("Boot")),
                "Should contain Spring/Boot phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t -> t.contains("REST") || t.contains("API")),
                "Should contain REST/API phase: " + phaseTitles);

        // Verify structured data
        assertNotNull(blueprint.milestones(), "Milestones should not be null");
        assertFalse(blueprint.milestones().isEmpty(),
                "Java roadmap should have milestones");
        assertNotNull(blueprint.risks(), "Risks should not be null");
        assertFalse(blueprint.risks().isEmpty(),
                "Java roadmap should have risks");
        assertNotNull(blueprint.successMetrics(), "Success metrics should not be null");
        assertFalse(blueprint.successMetrics().isEmpty(),
                "Java roadmap should have success metrics");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 2: AI roadmap generates AI architecture phases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AI assistant roadmap generates AI architecture phases")
    void aiRoadmapGeneratesAIArchitecturePhases() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Create a roadmap for AI assistant developer"
        );

        assertEquals(Domain.AI, analysis.domain(),
                "Domain should be AI for 'AI assistant' input");
        assertTrue(analysis.keywords().stream()
                        .anyMatch(k -> k.contains("ai") || k.contains("assistant")),
                "Keywords should detect AI-related terms: " + analysis.keywords());

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        List<String> phaseTitles = blueprint.phases().stream()
                .map(Phase::title)
                .toList();

        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Python") || t.contains("Math")),
                "Should contain Python/Math phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Machine Learning") || t.contains("ML")),
                "Should contain ML phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("LLM") || t.contains("Language Model")),
                "Should contain LLM phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Agent")),
                "Should contain AI Agent phase: " + phaseTitles);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 3: SaaS roadmap generates product milestones
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("SaaS roadmap generates product milestones")
    void saasRoadmapGeneratesProductMilestones() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Build a SaaS product roadmap"
        );

        assertEquals(Domain.SAAS, analysis.domain(),
                "Domain should be SAAS for 'SaaS' input");
        assertEquals(PlanningType.BUSINESS, analysis.planningType(),
                "PlanningType should be BUSINESS for SaaS");

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        assertFalse(blueprint.phases().isEmpty(),
                "SaaS roadmap should have phases");

        // Verify SaaS-specific phases
        List<String> phaseTitles = blueprint.phases().stream()
                .map(Phase::title)
                .toList();

        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Validation") || t.contains("Customer")),
                "Should contain Validation/Customer phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("MVP") || t.contains("Build")),
                "Should contain MVP/Build phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Launch") || t.contains("GTM")),
                "Should contain Launch/GTM phase: " + phaseTitles);

        // Verify milestones
        assertFalse(blueprint.milestones().isEmpty(),
                "SaaS roadmap should have milestones");
        Milestone firstMilestone = blueprint.milestones().get(0);
        assertNotNull(firstMilestone.estimatedWeek(),
                "Milestone should have estimated week");
        assertTrue(firstMilestone.estimatedWeek() > 0,
                "Estimated week should be positive");

        // Verify success metrics
        assertTrue(blueprint.successMetrics().stream()
                        .anyMatch(m -> m.toLowerCase().contains("mrr")
                                || m.toLowerCase().contains("revenue")
                                || m.toLowerCase().contains("churn")),
                "Should contain SaaS metrics like MRR/revenue: "
                        + blueprint.successMetrics());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 4: Professor roadmap generates education timeline
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Professor roadmap generates education timeline")
    void educationRoadmapGeneratesEducationTimeline() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Create a curriculum plan for professor"
        );

        assertEquals(Domain.EDUCATION, analysis.domain(),
                "Domain should be EDUCATION for 'professor' input");
        assertTrue(analysis.keywords().stream()
                        .anyMatch(k -> k.contains("professor") || k.contains("course")),
                "Should detect education keywords");

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        List<String> phaseTitles = blueprint.phases().stream()
                .map(Phase::title)
                .toList();

        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Curriculum") || t.contains("Design")),
                "Should contain Curriculum/Design phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Module") || t.contains("Content")),
                "Should contain Module/Content phase: " + phaseTitles);
        assertTrue(phaseTitles.stream().anyMatch(t ->
                        t.contains("Assessment") || t.contains("Evaluation")),
                "Should contain Assessment phase: " + phaseTitles);

        assertFalse(blueprint.milestones().isEmpty(),
                "Education roadmap should have milestones");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 5: Unknown domain falls back to GeneralPlanner
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unknown domain falls back to GeneralPlanner")
    void unknownDomainFallsBackToGeneralPlanner() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Organize my annual team offsite"
        );

        // Domain should be detected or fall back to GENERAL
        assertNotNull(analysis.domain(),
                "Domain should be detected or fallback to GENERAL");

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        assertNotNull(blueprint, "Blueprint should not be null even for general domain");
        assertNotNull(blueprint.phases(), "Phases should not be null");
        // Verify the registry used the right planner (i.e. blueprint is non-empty
        // and GeneralPlanner produced at least the default 5 phases)
        assertTrue(blueprint.phases().size() >= 3,
                "General planner should produce at least 3 phases, got: "
                        + blueprint.phases().size());

        // General planner registers itself as the fallback
        assertEquals(blueprint.metadata().get("domain"), "GENERAL",
                "Blueprint domain metadata should be GENERAL");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 6: Structured payload contains phases, milestones,
    //                     risks, deliverables
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Structured payload contains phases, milestones, risks, deliverables")
    void structuredPayloadContainsAllRequiredFields() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Build a comprehensive Java developer roadmap in 12 weeks"
        );

        DomainPlannerRegistry registry = new DomainPlannerRegistry();
        PlanBlueprint blueprint = registry.buildPlan(analysis);

        // Title and goal
        assertNotNull(blueprint.title(), "Blueprint should have a title");
        assertFalse(blueprint.title().isBlank(), "Title should not be blank");
        assertNotNull(blueprint.goal(), "Blueprint should have a goal");

        // Timeline
        assertTrue(blueprint.timelineWeeks() > 0,
                "Timeline weeks should be positive, got: " + blueprint.timelineWeeks());

        // Phases with deliverables
        assertFalse(blueprint.phases().isEmpty(), "Phases should not be empty");
        Phase firstPhase = blueprint.phases().get(0);
        assertNotNull(firstPhase.title(), "Phase should have a title");
        assertNotNull(firstPhase.deliverables(),
                "Phase should have deliverables");
        assertFalse(firstPhase.deliverables().isEmpty(),
                "Phase should have at least one deliverable: " + firstPhase);

        // Dependencies (first phase has none, others have at least one)
        for (int i = 0; i < blueprint.phases().size(); i++) {
            Phase phase = blueprint.phases().get(i);
            if (i == 0) {
                assertTrue(phase.dependencies().isEmpty(),
                        "First phase should have no dependencies");
            }
            assertNotNull(phase.objective(),
                    "Phase " + i + " should have an objective");
            assertTrue(phase.durationWeeks() > 0,
                    "Phase " + i + " should have positive duration");
        }

        // Milestones
        assertNotNull(blueprint.milestones(), "Milestones should not be null");
        assertFalse(blueprint.milestones().isEmpty(), "Milestones should not be empty");
        for (Milestone m : blueprint.milestones()) {
            assertNotNull(m.name(), "Milestone should have a name");
            assertTrue(m.estimatedWeek() > 0,
                    "Milestone " + m.name() + " should have positive week");
        }

        // Risks
        assertNotNull(blueprint.risks(), "Risks should not be null");
        assertFalse(blueprint.risks().isEmpty(), "Risks should not be empty");
        for (String risk : blueprint.risks()) {
            assertNotNull(risk, "Risk should not be null");
            assertFalse(risk.isBlank(), "Risk should not be blank: " + risk);
        }

        // Success metrics
        assertNotNull(blueprint.successMetrics(),
                "Success metrics should not be null");
        assertFalse(blueprint.successMetrics().isEmpty(),
                "Success metrics should not be empty");

        // Domain metadata
        assertEquals("JAVA", blueprint.metadata().get("domain"),
                "Domain metadata should be JAVA");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acceptance Case 7: Existing Planning SDK contract remains unchanged
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PlanningAnalyzer is deterministic — same input gives same output")
    void analyzerIsDeterministic() {
        String input = "Create a roadmap for Java developer";

        PlanningAnalysisResult a1 = analyzer.analyze(input);
        PlanningAnalysisResult a2 = analyzer.analyze(input);
        PlanningAnalysisResult a3 = analyzer.analyze(input);

        assertEquals(a1.domain(), a2.domain(),
                "Analyzer should be deterministic — same domain");
        assertEquals(a1.planningType(), a2.planningType(),
                "Analyzer should be deterministic — same planning type");
        assertEquals(a1.complexity(), a2.complexity(),
                "Analyzer should be deterministic — same complexity");
        assertEquals(a1.keywords(), a2.keywords(),
                "Analyzer should be deterministic — same keywords");

        assertEquals(a2.domain(), a3.domain());
        assertEquals(a2.keywords(), a3.keywords());
    }

    @Test
    @DisplayName("PlannerRegistry selects correct planner for each domain")
    void registrySelectsCorrectPlanner() {
        DomainPlannerRegistry registry = new DomainPlannerRegistry();

        assertInstanceOf(JavaPlanner.class,
                registry.plannerFor(Domain.JAVA),
                "JAVA domain should use JavaPlanner");
        assertInstanceOf(AIPlanner.class,
                registry.plannerFor(Domain.AI),
                "AI domain should use AIPlanner");
        assertInstanceOf(SaaSPlanner.class,
                registry.plannerFor(Domain.SAAS),
                "SAAS domain should use SaaSPlanner");
        assertInstanceOf(FitnessPlanner.class,
                registry.plannerFor(Domain.FITNESS),
                "FITNESS domain should use FitnessPlanner");
        assertInstanceOf(EducationPlanner.class,
                registry.plannerFor(Domain.EDUCATION),
                "EDUCATION domain should use EducationPlanner");
        assertInstanceOf(GeneralPlanner.class,
                registry.plannerFor(Domain.GENERAL),
                "GENERAL domain should use GeneralPlanner");
        assertInstanceOf(GeneralPlanner.class,
                registry.plannerFor(null),
                "null domain should fall back to GeneralPlanner");
    }

    @Test
    @DisplayName("DomainPlanner builds valid PlanBlueprint")
    void domainPlannerProducesValidBlueprint() {
        PlanningAnalysisResult analysis = analyzer.analyze(
                "Create a roadmap for Java developer"
        );

        DomainPlanner javaPlanner = new JavaPlanner();
        PlanBlueprint blueprint = javaPlanner.buildPlan(analysis);

        // All required fields present
        assertNotNull(blueprint.title());
        assertNotNull(blueprint.goal());
        assertTrue(blueprint.timelineWeeks() > 0);
        assertNotNull(blueprint.phases());
        assertNotNull(blueprint.milestones());
        assertNotNull(blueprint.risks());
        assertNotNull(blueprint.successMetrics());
        assertNotNull(blueprint.recommendations());

        // At least 3 phases
        assertTrue(blueprint.phases().size() >= 3,
                "Should have at least 3 phases, got: "
                        + blueprint.phases().size());

        // At least 1 milestone
        assertTrue(blueprint.milestones().size() >= 1,
                "Should have at least 1 milestone, got: "
                        + blueprint.milestones().size());

        // Cumulative weeks match timeline
        int calculatedWeeks = TaskGraphBuilder.totalWeeks(blueprint.phases());
        assertEquals(blueprint.timelineWeeks(), calculatedWeeks,
                "Timeline weeks should match sum of phase durations: "
                        + calculatedWeeks + " vs " + blueprint.timelineWeeks());
    }

    @Test
    @DisplayName("MilestoneGenerator produces correctly-spaced milestones")
    void milestoneGeneratorProducesSpacedMilestones() {
        List<Milestone> milestones = MilestoneGenerator.generateSpaced(
                12, 4,
                List.of("Core Done", "API Ready", "Project Live")
        );

        assertEquals(3, milestones.size(), "Should have 3 milestones");

        assertTrue(milestones.get(0).estimatedWeek() <= 4,
                "First milestone should be at or before week 4");
        assertTrue(milestones.get(1).estimatedWeek() <= 8,
                "Second milestone should be at or before week 8");
        assertEquals("Core Done", milestones.get(0).name());
        assertEquals("API Ready", milestones.get(1).name());
        assertEquals("Project Live", milestones.get(2).name());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Analyzer handles null and blank input gracefully")
    void analyzerHandlesNullInput() {
        assertNotNull(analyzer.analyze(null),
                "Should handle null input");
        assertNotNull(analyzer.analyze(""),
                "Should handle empty input");
        assertNotNull(analyzer.analyze("   "),
                "Should handle blank input");
    }

    @Test
    @DisplayName("Analyzer detects duration from input text")
    void analyzerDetectsDurationFromText() {
        PlanningAnalysisResult r1 = analyzer.analyze(
                "Create a 4-week Java roadmap"
        );
        assertEquals(4, r1.estimatedWeeks(),
                "Should detect 4 weeks from '4-week'");

        PlanningAnalysisResult r2 = analyzer.analyze(
                "Create a 3-month SaaS roadmap"
        );
        assertTrue(r2.estimatedWeeks() >= 8,
                "Should detect at least 8 weeks for '3 months', got: "
                        + r2.estimatedWeeks());

        PlanningAnalysisResult r3 = analyzer.analyze(
                "Build a Java developer roadmap"
        );
        assertTrue(r3.estimatedWeeks() > 0,
                "Should have default duration > 0");
    }

    @Test
    @DisplayName("Analyzer detects complexity from input text")
    void analyzerDetectsComplexity() {
        PlanningAnalysisResult beginner = analyzer.analyze(
                "Beginner Java course"
        );
        assertEquals(Complexity.LOW, beginner.complexity(),
                "Should detect LOW complexity for 'beginner'");

        PlanningAnalysisResult advanced = analyzer.analyze(
                "Advanced enterprise full-stack Java architecture"
        );
        assertEquals(Complexity.HIGH, advanced.complexity(),
                "Should detect HIGH complexity for 'advanced enterprise full-stack'");
    }

    @Test
    @DisplayName("DomainPlannerRegistry.register() allows planner extension")
    void registrySupportsDynamicRegistration() {
        DomainPlannerRegistry registry = new DomainPlannerRegistry();

        // Override JAVA planner with a custom one
        DomainPlanner customJavaPlanner = new DomainPlanner() {
            @Override
            public Domain domain() { return Domain.JAVA; }

            @Override
            public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
                return new PlanBlueprint(
                        "Custom Java Plan",
                        analysis.goalText(),
                        1,
                        List.of(new Phase(
                                "Custom Phase",
                                "Custom objective",
                                1,
                                List.of("Custom deliverable"),
                                List.of(),
                                List.of("Custom criterion"),
                                Map.of()
                        )),
                        List.of(),
                        List.of("Custom risk"),
                        List.of("Custom metric"),
                        List.of("Custom recommendation"),
                        Map.of("domain", "JAVA", "custom", true)
                );
            }
        };

        registry.register(customJavaPlanner);

        PlanningAnalysisResult analysis = analyzer.analyze(
                "Create a Java roadmap"
        );

        PlanBlueprint bp = registry.buildPlan(analysis);
        assertEquals("Custom Java Plan", bp.title(),
                "Registry should use the newly registered planner");
        assertEquals("JAVA", bp.metadata().get("domain"));
        assertEquals(true, bp.metadata().get("custom"),
                "Custom metadata should be preserved");
    }

    @Nested
    @DisplayName("Planner implementations")
    class PlannerImplementations {

        @Test
        @DisplayName("JavaPlanner produces 10 phases with correct structure")
        void javaPlannerProducesTenPhases() {
            DomainPlanner planner = new JavaPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("Java developer roadmap"));

            assertEquals(10, bp.phases().size(),
                    "JavaPlanner should produce 10 phases");
            assertTrue(bp.timelineWeeks() >= 10,
                    "Timeline should be at least 10 weeks");

            // First phase has no dependencies
            assertTrue(bp.phases().get(0).dependencies().isEmpty(),
                    "First phase should have no dependencies");

            // All phases after first have dependencies
            for (int i = 1; i < bp.phases().size(); i++) {
                assertFalse(bp.phases().get(i).dependencies().isEmpty(),
                        "Phase " + (i + 1) + " should have dependencies");
            }
        }

        @Test
        @DisplayName("AIPlanner produces ML and LLM phases")
        void aiPlannerProducesMLAndLLMPhases() {
            DomainPlanner planner = new AIPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("AI assistant developer"));

            List<String> titles = bp.phases().stream()
                    .map(Phase::title)
                    .toList();

            assertTrue(titles.stream().anyMatch(t -> t.contains("Python")),
                    "Should have Python phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Machine Learning") || t.contains("Deep Learning")),
                    "Should have ML/DL phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Language Model") || t.contains("LLM")),
                    "Should have LLM phase");
            assertTrue(titles.stream().anyMatch(t -> t.contains("Agent")),
                    "Should have Agent phase");
        }

        @Test
        @DisplayName("SaaSPlanner produces product and launch phases")
        void saasPlannerProducesProductAndLaunchPhases() {
            DomainPlanner planner = new SaaSPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("SaaS startup product roadmap"));

            List<String> titles = bp.phases().stream()
                    .map(Phase::title)
                    .toList();

            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Validation") || t.contains("Customer")),
                    "Should have Validation phase");
            assertTrue(titles.stream().anyMatch(t -> t.contains("MVP")),
                    "Should have MVP phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Launch") || t.contains("GTM")),
                    "Should have Launch/GTM phase");
        }

        @Test
        @DisplayName("FitnessPlanner produces training phases")
        void fitnessPlannerProducesTrainingPhases() {
            DomainPlanner planner = new FitnessPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("Marathon training plan"));

            List<String> titles = bp.phases().stream()
                    .map(Phase::title)
                    .toList();

            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Assessment") || t.contains("Baseline")),
                    "Should have Assessment phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Foundation") || t.contains("Training")),
                    "Should have Foundation/Training phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Peak") || t.contains("Performance")),
                    "Should have Peak/Performance phase");
        }

        @Test
        @DisplayName("EducationPlanner produces curriculum phases")
        void educationPlannerProducesCurriculumPhases() {
            DomainPlanner planner = new EducationPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("Professor curriculum development"));

            List<String> titles = bp.phases().stream()
                    .map(Phase::title)
                    .toList();

            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Curriculum") || t.contains("Design")),
                    "Should have Curriculum/Design phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Module") || t.contains("Author")),
                    "Should have Module phase");
            assertTrue(titles.stream().anyMatch(t ->
                            t.contains("Assessment") || t.contains("Evaluation")),
                    "Should have Assessment phase");
        }

        @Test
        @DisplayName("GeneralPlanner produces 5 default phases")
        void generalPlannerProducesFiveDefaultPhases() {
            DomainPlanner planner = new GeneralPlanner();
            PlanBlueprint bp = planner.buildPlan(
                    analyzer.analyze("Organize team offsite"));

            assertEquals(5, bp.phases().size(),
                    "GeneralPlanner should produce 5 phases");
            assertEquals(7, bp.timelineWeeks(),
                    "GeneralPlanner should have 7-week timeline (1+1+3+1+1)");
        }
    }
}
