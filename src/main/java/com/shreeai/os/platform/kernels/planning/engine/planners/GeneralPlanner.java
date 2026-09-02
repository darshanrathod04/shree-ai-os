package com.shreeai.os.platform.kernels.planning.engine.planners;

import com.shreeai.os.platform.kernels.planning.engine.MilestoneGenerator;
import com.shreeai.os.platform.kernels.planning.engine.TaskGraphBuilder;
import com.shreeai.os.platform.kernels.planning.model.Milestone;
import com.shreeai.os.platform.kernels.planning.model.Phase;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;

import java.util.List;
import java.util.Map;

/**
 * Fallback planner for unknown or GENERAL domains. Produces a generic
 * but still structured 5-phase plan with clear deliverables.
 */
public final class GeneralPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.GENERAL; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = analysis.goalText().isBlank() ? "the requested objective" : analysis.goalText();

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Discovery & Research",
                        "Planning & Design",
                        "Implementation",
                        "Testing & Review",
                        "Delivery & Handoff"
                },
                new String[]{
                        "Understand the problem, audience, and constraints",
                        "Design the approach, structure, and key milestones",
                        "Build the solution following the plan",
                        "Validate, test, and gather feedback",
                        "Ship, document, and hand over the result"
                },
                new int[]{1, 1, 3, 1, 1},
                new String[][]{
                        {"Research notes", "Stakeholder interviews"},
                        {"Plan document", "Design mockups"},
                        {"Working solution", "Code or artifacts"},
                        {"Test report", "Review feedback"},
                        {"Final deliverable", "Documentation"}
                },
                new String[][]{
                        {"Discovery report completed"},
                        {"Plan approved by stakeholders"},
                        {"Solution functional and demoed"},
                        {"All critical issues resolved"},
                        {"Stakeholder sign-off obtained"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateFromPhases(
                phases, "{title} Phase Complete");

        List<String> risks = List.of(
                "Unclear requirements",
                "Scope creep",
                "Insufficient validation before delivery"
        );

        List<String> successMetrics = List.of(
                "All phases completed on time",
                "Stakeholder sign-off",
                "Solution adopted by users"
        );

        List<String> recommendations = List.of(
                "Define success criteria upfront",
                "Validate early with stakeholders",
                "Iterate based on feedback"
        );

        return new PlanBlueprint(
                "Execution Plan for " + goal,
                "Achieve: " + goal,
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "GENERAL", "version", "1.0")
        );
    }
}
