package com.shreeai.os.platform.kernels.planning.engine.planners;

import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;

/**
 * Strategy interface for domain-specific planners.
 *
 * <p>Each implementation produces a rich {@link PlanBlueprint} tailored
 * to a particular domain (Java, AI, SaaS, Fitness, Education, etc).</p>
 *
 * @since Sprint-11
 */
public interface DomainPlanner {

    /**
     * The domain this planner handles.
     */
    PlanningAnalysisResult.Domain domain();

    /**
     * Generates a domain-specific execution blueprint.
     *
     * @param analysis the planning analysis (domain, type, complexity, weeks, keywords)
     * @return a complete plan blueprint with phases, milestones, risks, and metrics
     */
    PlanBlueprint buildPlan(PlanningAnalysisResult analysis);
}
