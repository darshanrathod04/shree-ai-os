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
 * Domain planner for EDUCATION. Generates a structured learning plan
 * for professors, students, and curriculum designers.
 */
public final class EducationPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.EDUCATION; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = deriveGoal(analysis);

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Curriculum Design",
                        "Learning Objectives & Outcomes",
                        "Module Authoring",
                        "Assessment Framework",
                        "Delivery & Iteration",
                        "Evaluation & Improvement"
                },
                new String[]{
                        "Define scope, audience, prerequisites, and learning outcomes",
                        "Write measurable Bloom's-taxonomy objectives for each module",
                        "Author content, exercises, and projects for each module",
                        "Design quizzes, assignments, and capstone assessments",
                        "Deliver the curriculum and collect learner feedback",
                        "Measure outcomes, refine, and publish a polished version"
                },
                new int[]{2, 2, 4, 2, 4, 2},
                new String[][]{
                        {"Curriculum outline", "Audience profile"},
                        {"Module objectives", "Outcome matrix"},
                        {"Lecture notes", "Hands-on exercises", "Reading list"},
                        {"Quiz bank", "Project rubric", "Capstone spec"},
                        {"Live sessions", "Discussion prompts"},
                        {"Outcome report", "Improvement plan"}
                },
                new String[][]{
                        {"Approved curriculum outline"},
                        {"Objectives mapped to Bloom's levels"},
                        {"All modules drafted"},
                        {"Assessment blueprint complete"},
                        {"First cohort completes course"},
                        {"Published with learner outcome metrics"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateFromPhases(
                phases, "{title} Phase Complete");

        List<String> risks = List.of(
                "Vague or unmeasurable learning objectives",
                "Content overload without scaffolding",
                "Assessment drift from stated objectives",
                "Low engagement in async delivery",
                "No feedback loop for course improvement"
        );

        List<String> successMetrics = List.of(
                "All modules with measurable objectives",
                "Learner completion rate >70%",
                "Average assessment score >80%",
                "Learner NPS >40",
                "Course published with credentials"
        );

        List<String> recommendations = List.of(
                "Start with learning outcomes, not content",
                "Use backward design: assessment before content",
                "Pilot with a small cohort before full launch",
                "Include active learning in every module",
                "Collect structured feedback at the end of each module"
        );

        return new PlanBlueprint(
                "Education Curriculum Plan",
                "Design and ship a " + goal,
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "EDUCATION", "version", "1.0")
        );
    }

    private String deriveGoal(PlanningAnalysisResult analysis) {
        String text = analysis.goalText().toLowerCase();
        if (text.contains("professor")) return "Professor's Course Plan";
        if (text.contains("student")) return "Student Learning Plan";
        if (text.contains("curriculum")) return "Curriculum Development Plan";
        if (text.contains("course")) return "Course Development Plan";
        return "Education Learning Plan";
    }
}
