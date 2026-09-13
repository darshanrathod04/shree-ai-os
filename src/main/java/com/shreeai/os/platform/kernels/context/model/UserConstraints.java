package com.shreeai.os.platform.kernels.context.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>UserConstraints</b>
 *
 * <p>Immutable value object holding explicit user constraints extracted from
 * natural language input. Only explicitly mentioned constraints are captured;
 * missing values remain null (never invented or defaulted).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Stores only explicit user constraints (duration, budget, experience, etc.)</li>
 *   <li>Provides explainability via evidence list for each extracted constraint</li>
 *   <li>Single source of truth for user constraints during pipeline execution</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param duration explicit duration constraint (e.g., "30 days", "2 weeks")
 * @param budget explicit budget constraint (e.g., "₹5000", "$200")
 * @param experience explicit experience level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
 * @param platform explicit platform target (WINDOWS, MAC, LINUX, ANDROID, IOS, WEB)
 * @param language explicit language preference (e.g., "English", "Hindi")
 * @param outputPreference explicit output format preference (ROADMAP, CODE, EXPLANATION, CHECKLIST, ARCHITECTURE)
 * @param evidence list of evidence for each extracted constraint (for traceability)
 */
public record UserConstraints(
        String duration,
        String budget,
        ExperienceLevel experience,
        PlatformType platform,
        String language,
        OutputPreference outputPreference,
        List<ConstraintEvidence> evidence
) {
    /**
     * Creates a new UserConstraints with defensive copying.
     *
     * @param duration explicit duration constraint
     * @param budget explicit budget constraint
     * @param experience explicit experience level
     * @param platform explicit platform target
     * @param language explicit language preference
     * @param outputPreference explicit output format preference
     * @param evidence list of evidence for each extracted constraint
     * @return a new UserConstraints instance
     */
    public static UserConstraints of(String duration, String budget, ExperienceLevel experience,
                                     PlatformType platform, String language,
                                     OutputPreference outputPreference,
                                     List<ConstraintEvidence> evidence) {
        List<ConstraintEvidence> safeEvidence = evidence != null
                ? List.copyOf(evidence)
                : List.of();
        return new UserConstraints(duration, budget, experience, platform, language, outputPreference, safeEvidence);
    }

    /**
     * Returns an empty UserConstraints with all fields null.
     *
     * @return an empty UserConstraints instance
     */
    public static UserConstraints empty() {
        return new UserConstraints(null, null, null, null, null, null, List.of());
    }

    public String duration() {
        return duration;
    }

    public String budget() {
        return budget;
    }

    public ExperienceLevel experience() {
        return experience;
    }

    public PlatformType platform() {
        return platform;
    }

    public String language() {
        return language;
    }

    public OutputPreference outputPreference() {
        return outputPreference;
    }

    public List<ConstraintEvidence> evidence() {
        return evidence;
    }

    /**
     * Returns true if any constraint has been extracted.
     *
     * @return true if at least one constraint is non-null
     */
    public boolean hasAnyConstraint() {
        return duration != null || budget != null || experience != null ||
               platform != null || language != null || outputPreference != null;
    }

    @Override
    public String toString() {
        return "UserConstraints{duration='" + duration + "', budget='" + budget +
               "', experience=" + experience + ", platform=" + platform +
               ", language='" + language + "', outputPreference=" + outputPreference +
               ", evidence=" + evidence.size() + "}";
    }
}