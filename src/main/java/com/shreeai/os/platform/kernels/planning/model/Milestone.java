package com.shreeai.os.platform.kernels.planning.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a milestone within a domain-aware execution plan.
 *
 * <p>Milestones mark significant checkpoints with completion criteria,
 * estimated timing, and expected output.</p>
 *
 * @since Sprint-11
 */
public final class Milestone {

    private final String name;
    private final List<String> completionCriteria;
    private final int estimatedWeek;
    private final String output;
    private final Map<String, Object> metadata;

    public Milestone(
            String name,
            List<String> completionCriteria,
            int estimatedWeek,
            String output,
            Map<String, Object> metadata
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.completionCriteria = completionCriteria != null ? List.copyOf(completionCriteria) : List.of();
        this.estimatedWeek = estimatedWeek > 0 ? estimatedWeek : 1;
        this.output = output != null ? output : "";
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public String name() { return name; }
    public List<String> completionCriteria() { return completionCriteria; }
    public int estimatedWeek() { return estimatedWeek; }
    public String output() { return output; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milestone m)) return false;
        return name.equals(m.name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public String toString() {
        return "Milestone{name='" + name + "', week=" + estimatedWeek + "}";
    }
}
