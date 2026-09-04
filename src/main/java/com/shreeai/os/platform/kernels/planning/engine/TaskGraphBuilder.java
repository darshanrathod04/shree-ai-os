package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds rich, ordered {@link Phase} objects for a domain-aware plan.
 *
 * <p>Each phase contains a title, objective, duration, deliverables,
 * dependencies, and success criteria. Phases are returned in execution order
 * (the first phase is the starting point with no dependencies).</p>
 *
 * @since Sprint-11
 */
public final class TaskGraphBuilder {

    /**
     * Returns a copy of {@code phase} with the dependency string
     * "Phase N completed" added to its dependency list. Used to chain phases.
     */
    public static List<String> chainedDependencies(int previousPhaseIndex) {
        return List.of("Phase " + previousPhaseIndex + " completed");
    }

    /**
     * Builds a phase with the given properties. Convenience method.
     */
    public static Phase phase(
            String title,
            String objective,
            int durationWeeks,
            List<String> deliverables,
            List<String> dependencies,
            List<String> successCriteria
    ) {
        return new Phase(
                title, objective, durationWeeks,
                deliverables, dependencies, successCriteria, Map.of()
        );
    }

    /**
     * Aggregates total duration across all phases.
     */
    public static int totalWeeks(List<Phase> phases) {
        int total = 0;
        for (Phase phase : phases) {
            total += phase.durationWeeks();
        }
        return total;
    }

    /**
     * Returns a copy of {@code phases} as an immutable list.
     */
    public static List<Phase> immutable(List<Phase> phases) {
        return phases == null ? List.of() : List.copyOf(phases);
    }

    /**
     * Appends a number-prefixed deliverable list to the given builder.
     */
    public static String formatDeliverables(List<String> deliverables) {
        if (deliverables == null || deliverables.isEmpty()) {
            return "No specific deliverables";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deliverables.size(); i++) {
            sb.append(i + 1).append(". ").append(deliverables.get(i));
            if (i < deliverables.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Helper to build a chain of phases where each depends on the previous.
     */
    public static List<Phase> buildChain(
            String[] titles,
            String[] objectives,
            int[] weeks,
            String[][] deliverables,
            String[][] successCriteria
    ) {
        List<Phase> phases = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            List<String> deps = i == 0
                    ? List.of()
                    : List.of("Phase " + i + " completed");
            phases.add(new Phase(
                    titles[i],
                    objectives[i],
                    weeks[i],
                    List.of(deliverables[i]),
                    deps,
                    List.of(successCriteria[i]),
                    Map.of()
            ));
        }
        return List.copyOf(phases);
    }
}
