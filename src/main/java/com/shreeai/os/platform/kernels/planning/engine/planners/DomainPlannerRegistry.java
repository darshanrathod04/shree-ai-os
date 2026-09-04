package com.shreeai.os.platform.kernels.planning.engine.planners;

import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of {@link DomainPlanner}s. Selects a planner using
 * {@link PlanningAnalysisResult#domain()}.
 *
 * <p>Strategy pattern — no switch statements required. Unknown domains
 * fall back to {@link GeneralPlanner}.</p>
 *
 * @since Sprint-11
 */
public final class DomainPlannerRegistry {

    private final Map<Domain, DomainPlanner> planners = new HashMap<>();
    private final DomainPlanner fallback;

    public DomainPlannerRegistry() {
        this(new GeneralPlanner());
    }

    public DomainPlannerRegistry(DomainPlanner fallback) {
        this.fallback = fallback != null ? fallback : new GeneralPlanner();
        register(new JavaPlanner());
        register(new AIPlanner());
        register(new SaaSPlanner());
        register(new FitnessPlanner());
        register(new EducationPlanner());
        register(new GeneralPlanner());
    }

    /**
     * Registers a planner, overriding any existing planner for the
     * same domain. Useful for extensions and tests.
     */
    public void register(DomainPlanner planner) {
        if (planner == null) return;
        planners.put(planner.domain(), planner);
    }

    /**
     * Returns the planner for the given domain, or the fallback if
     * no specific planner is registered.
     */
    public DomainPlanner plannerFor(Domain domain) {
        if (domain == null) return fallback;
        return planners.getOrDefault(domain, fallback);
    }

    /**
     * Convenience method: analyze, then build a plan using the selected planner.
     */
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        DomainPlanner planner = plannerFor(analysis.domain());
        return planner.buildPlan(analysis);
    }
}
