package com.shreeai.os.platform.kernels.planning.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The result of {@link PlanningAnalyzer} — extracted structured planning intent.
 *
 * <p>Contains domain, planning type, complexity, duration estimate,
 * keywords, and constraints — all derived deterministically without LLM.</p>
 *
 * @since Sprint-11
 */
public final class PlanningAnalysisResult {

    /** Supported domain categories. */
    public enum Domain {
        JAVA, SPRING, AI, SAAS, FITNESS, EDUCATION, GENERAL
    }

    /** The type of plan being requested. */
    public enum PlanningType {
        ROADMAP, PROJECT, LEARNING, CAREER, FITNESS, BUSINESS, GENERAL
    }

    /** Complexity level of the plan. */
    public enum Complexity {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    private final Domain domain;
    private final PlanningType planningType;
    private final Complexity complexity;
    private final int estimatedWeeks;
    private final List<String> keywords;
    private final List<String> constraints;
    private final String goalText;
    private final Map<String, Object> metadata;

    public PlanningAnalysisResult(
            Domain domain,
            PlanningType planningType,
            Complexity complexity,
            int estimatedWeeks,
            List<String> keywords,
            List<String> constraints,
            String goalText,
            Map<String, Object> metadata
    ) {
        this.domain = domain != null ? domain : Domain.GENERAL;
        this.planningType = planningType != null ? planningType : PlanningType.GENERAL;
        this.complexity = complexity != null ? complexity : Complexity.MEDIUM;
        this.estimatedWeeks = estimatedWeeks > 0 ? estimatedWeeks : 4;
        this.keywords = keywords != null ? List.copyOf(keywords) : List.of();
        this.constraints = constraints != null ? List.copyOf(constraints) : List.of();
        this.goalText = goalText != null ? goalText : "";
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public Domain domain() { return domain; }
    public PlanningType planningType() { return planningType; }
    public Complexity complexity() { return complexity; }
    public int estimatedWeeks() { return estimatedWeeks; }
    public List<String> keywords() { return keywords; }
    public List<String> constraints() { return constraints; }
    public String goalText() { return goalText; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public String toString() {
        return "PlanningAnalysisResult{domain=" + domain
                + ", type=" + planningType
                + ", complexity=" + complexity
                + ", weeks=" + estimatedWeeks + "}";
    }
}
