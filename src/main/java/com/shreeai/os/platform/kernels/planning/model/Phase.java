package com.shreeai.os.platform.kernels.planning.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a structured phase within a domain-aware execution plan.
 *
 * <p>Each phase contains a title, objective, duration, deliverables,
 * dependencies, and success criteria — forming a rich task graph instead
 * of flat subtasks.</p>
 *
 * @since Sprint-11
 */
public final class Phase {

    private final String title;
    private final String objective;
    private final int durationWeeks;
    private final List<String> deliverables;
    private final List<String> dependencies;
    private final List<String> successCriteria;
    private final Map<String, Object> metadata;

    public Phase(
            String title,
            String objective,
            int durationWeeks,
            List<String> deliverables,
            List<String> dependencies,
            List<String> successCriteria,
            Map<String, Object> metadata
    ) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.objective = objective != null ? objective : "";
        this.durationWeeks = durationWeeks > 0 ? durationWeeks : 1;
        this.deliverables = deliverables != null ? List.copyOf(deliverables) : List.of();
        this.dependencies = dependencies != null ? List.copyOf(dependencies) : List.of();
        this.successCriteria = successCriteria != null ? List.copyOf(successCriteria) : List.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String title() { return title; }
    public String objective() { return objective; }
    public int durationWeeks() { return durationWeeks; }
    public List<String> deliverables() { return deliverables; }
    public List<String> dependencies() { return dependencies; }
    public List<String> successCriteria() { return successCriteria; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phase phase)) return false;
        return title.equals(phase.title);
    }

    @Override
    public int hashCode() { return title.hashCode(); }

    @Override
    public String toString() {
        return "Phase{title='" + title + "', durationWeeks=" + durationWeeks + "}";
    }
}
