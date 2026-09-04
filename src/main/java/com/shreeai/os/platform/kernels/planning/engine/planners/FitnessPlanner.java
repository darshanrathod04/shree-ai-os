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
 * Domain planner for FITNESS. Generates a structured training plan
 * covering assessment, foundation, progression, and goal-specific phases.
 */
public final class FitnessPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.FITNESS; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = deriveGoal(analysis);

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Assessment & Baseline",
                        "Foundation Building",
                        "Progressive Overload",
                        "Peak Performance",
                        "Maintenance & Longevity"
                },
                new String[]{
                        "Establish baseline metrics: body composition, strength, endurance",
                        "Build consistent training habits, mobility, and nutrition foundation",
                        "Implement structured progressive overload with periodization",
                        "Peak for specific goal: strength, endurance, or body composition",
                        "Sustain results with efficient, injury-free training"
                },
                new int[]{1, 3, 4, 3, 2},
                new String[][]{
                        {"Fitness assessment", "Goal setting", "Baseline measurements"},
                        {"Training split", "Nutrition plan", "Recovery protocol"},
                        {"Weekly progressive load", "Deload weeks", "Progress photos"},
                        {"Peak training blocks", "Goal assessment", "Final measurements"},
                        {"Sustainable routine", "Annual planning", "Long-term monitoring"}
                },
                new String[][]{
                        {"Complete baseline assessment"},
                        {"Train 4x/week for 3 weeks without missing sessions"},
                        {"Increase key lifts by 10% or run 5K"},
                        {"Achieve goal metric"},
                        {"Maintain for 8 consecutive weeks"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateFromPhases(
                phases, "Week {week}: {title}");

        List<String> risks = List.of(
                "Overtraining and injury from rapid progression",
                "Inconsistent nutrition undermining training",
                "Lack of recovery leading to burnout",
                "Goal abandonment without accountability",
                "Chasing random programs without periodization"
        );

        List<String> successMetrics = List.of(
                "Body composition goal achieved",
                "Consistent 4x/week training for 8+ weeks",
                "Strength milestone: squat 1.5x bodyweight, deadlift 2x",
                "Endurance goal: 5K run or equivalent",
                "Maintained for 3 months without injury"
        );

        List<String> recommendations = List.of(
                "Track everything: training, nutrition, sleep, and body weight",
                "Prioritize compound movements: squat, deadlift, press, row",
                "Sleep 7-9 hours per night for recovery",
                "Progress slowly — aim for 2% strength gain per week",
                "Find an accountability partner or coach"
        );

        return new PlanBlueprint(
                "Fitness Training Plan",
                goal + " — " + TaskGraphBuilder.totalWeeks(phases) + "-week fitness roadmap",
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "FITNESS", "version", "1.0")
        );
    }

    private String deriveGoal(PlanningAnalysisResult analysis) {
        String text = analysis.goalText().toLowerCase();
        if (text.contains("marathon")) return "Marathon Training";
        if (text.contains("strength") || text.contains("muscle")) return "Strength Building";
        if (text.contains("weight loss") || text.contains("fat loss")) return "Weight Loss";
        return "General Fitness";
    }
}
