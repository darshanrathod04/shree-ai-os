package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.Milestone;
import com.shreeai.os.platform.kernels.planning.model.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates milestones automatically from a list of {@link Phase}s.
 *
 * <p>Each milestone represents a major checkpoint with completion criteria,
 * estimated week, and expected output.</p>
 *
 * @since Sprint-11
 */
public final class MilestoneGenerator {

    /**
     * Generates one milestone per phase, with cumulative estimated weeks.
     *
     * @param phases        the ordered phases
     * @param template      template for milestone name (e.g. "{title} Complete")
     * @return list of milestones, one per phase
     */
    public static List<Milestone> generateFromPhases(List<Phase> phases, String template) {
        if (phases == null || phases.isEmpty()) return List.of();

        List<Milestone> milestones = new ArrayList<>();
        int cumulative = 0;

        for (Phase phase : phases) {
            cumulative += phase.durationWeeks();
            String name = template
                    .replace("{title}", phase.title())
                    .replace("{week}", String.valueOf(cumulative));
            milestones.add(new Milestone(
                    name,
                    phase.successCriteria(),
                    cumulative,
                    "Completed " + phase.title(),
                    Map.of("phase", phase.title())
            ));
        }
        return List.copyOf(milestones);
    }

    /**
     * Generates evenly-spaced milestones across a fixed timeline.
     *
     * @param totalWeeks  total plan length
     * @param interval    weeks between milestones
     * @param names       ordered list of milestone names
     * @return evenly-distributed milestones
     */
    public static List<Milestone> generateSpaced(int totalWeeks, int interval, List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        int weeks = Math.max(1, totalWeeks);
        int step = Math.max(1, interval);
        List<Milestone> milestones = new ArrayList<>();
        int idx = 0;
        for (int week = step; week <= weeks && idx < names.size(); week += step) {
            milestones.add(new Milestone(
                    names.get(idx),
                    List.of("All tasks up to week " + week + " completed"),
                    week,
                    "Milestone " + names.get(idx) + " achieved",
                    Map.of()
            ));
            idx++;
        }
        // If there are remaining names, place at final week
        while (idx < names.size()) {
            milestones.add(new Milestone(
                    names.get(idx),
                    List.of("All phases complete"),
                    weeks,
                    "Final milestone achieved",
                    Map.of()
            ));
            idx++;
        }
        return List.copyOf(milestones);
    }
}
