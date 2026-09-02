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
 * Domain planner for SaaS. Generates a product-focused roadmap covering
 * validation, MVP, pricing, launch, growth, and scale phases.
 */
public final class SaaSPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.SAAS; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = analysis.goalText();

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Market & Customer Validation",
                        "Product Strategy & Positioning",
                        "MVP Definition & Design",
                        "MVP Build Sprint",
                        "Pricing & Packaging",
                        "Beta Launch",
                        "Public Launch & GTM",
                        "Growth & Retention",
                        "Scale Operations"
                },
                new String[]{
                        "Identify target segment, pain points, and willingness to pay",
                        "Define positioning, value proposition, and competitive moat",
                        "Scope a tight MVP that solves the core problem end-to-end",
                        "Ship a usable MVP in 6-8 weeks with core flows",
                        "Design pricing tiers, free trial, and onboarding motion",
                        "Run a private beta with 20-50 design partners",
                        "Execute public launch, content, and first sales motion",
                        "Drive activation, retention, and viral loops",
                        "Build infrastructure, team, and processes for scale"
                },
                new int[]{3, 2, 2, 8, 2, 4, 4, 8, 12},
                new String[][]{
                        {"Customer interview report", "Problem statement"},
                        {"Positioning canvas", "Competitive analysis"},
                        {"MVP scope doc", "Wireframes"},
                        {"Working MVP", "Onboarding flow"},
                        {"Pricing page", "Billing integration"},
                        {"Beta feedback report", "Iteration log"},
                        {"Launch site", "First 10 customers"},
                        {"Growth experiments", "Retention dashboard"},
                        {"SLA framework", "Hiring plan"}
                },
                new String[][]{
                        {"10 customer interviews completed"},
                        {"Positioning approved by founding team"},
                        {"User flows validated with 5 users"},
                        {"MVP used by internal team daily"},
                        {"First paying customer in beta"},
                        {"20+ active beta accounts"},
                        {"Public launch with 100 signups in week 1"},
                        {"Net revenue retention >100%"},
                        {"SOC 2 audit ready or completed"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateFromPhases(
                phases, "{title} Phase Complete");

        List<String> risks = List.of(
                "Building features without validated demand",
                "Pricing too low or too high without testing",
                "Slow MVP build due to scope creep",
                "Insufficient GTM investment at launch",
                "Churn not addressed in early retention curve"
        );

        List<String> successMetrics = List.of(
                "Product-market fit survey score >40%",
                "MRR growth >10% MoM",
                "Net revenue retention >100%",
                "CAC payback <12 months",
                "NPS >40"
        );

        List<String> recommendations = List.of(
                "Talk to 30+ potential customers before building",
                "Charge from day one — even for beta",
                "Keep MVP scope to 3-5 features maximum",
                "Set up analytics before launch, not after",
                "Hire a founding designer and engineer first"
        );

        return new PlanBlueprint(
                "SaaS Product Roadmap",
                "Ship and grow a SaaS product: " + goal,
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "SAAS", "version", "1.0")
        );
    }
}
