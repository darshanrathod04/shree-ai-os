package com.shreeai.os.platform.kernels.planning.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Complete domain-aware plan blueprint produced by a {@link com.shreeai.os.platform.kernels.planning.engine.planners.DomainPlanner}.
 *
 * <p>Contains title, phases, milestones, risks, deliverables summary, and
 * success metrics. This is the rich output of Sprint-11 planning intelligence.</p>
 *
 * @since Sprint-11
 */
public final class PlanBlueprint {

    private final String title;
    private final String goal;
    private final int timelineWeeks;
    private final List<Phase> phases;
    private final List<Milestone> milestones;
    private final List<String> risks;
    private final List<String> successMetrics;
    private final List<String> recommendations;
    private final Map<String, Object> metadata;

    public PlanBlueprint(
            String title,
            String goal,
            int timelineWeeks,
            List<Phase> phases,
            List<Milestone> milestones,
            List<String> risks,
            List<String> successMetrics,
            List<String> recommendations,
            Map<String, Object> metadata
    ) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.goal = goal != null ? goal : "";
        this.timelineWeeks = timelineWeeks > 0 ? timelineWeeks : 4;
        this.phases = phases != null ? List.copyOf(phases) : List.of();
        this.milestones = milestones != null ? List.copyOf(milestones) : List.of();
        this.risks = risks != null ? List.copyOf(risks) : List.of();
        this.successMetrics = successMetrics != null ? List.copyOf(successMetrics) : List.of();
        this.recommendations = recommendations != null ? List.copyOf(recommendations) : List.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String title() { return title; }
    public String goal() { return goal; }
    public int timelineWeeks() { return timelineWeeks; }
    public List<Phase> phases() { return phases; }
    public List<Milestone> milestones() { return milestones; }
    public List<String> risks() { return risks; }
    public List<String> successMetrics() { return successMetrics; }
    public List<String> recommendations() { return recommendations; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public String toString() {
        return "PlanBlueprint{title='" + title + "', phases=" + phases.size()
                + ", milestones=" + milestones.size() + "}";
    }
}
