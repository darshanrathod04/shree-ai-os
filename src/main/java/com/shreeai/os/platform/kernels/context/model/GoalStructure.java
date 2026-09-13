package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>GoalStructure</b>
 *
 * <p>Canonical goal artifact representing the identified goals from a user request.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Holds the primary identified goal and its confidence</li>
 *   <li>Provides sub-goals for complex multi-objective requests</li>
 *   <li>Includes detection metadata for auditability</li>
 *   <li>Single source of truth for goals during pipeline execution</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param primaryGoal the primary identified goal (must not be null)
 * @param subGoals list of sub-goals identified from the request
 * @param complexity the complexity level of the goal structure
 * @param detectedAt when the goals were detected (must not be null)
 * @param detectionMethod the method used for detection (must not be null)
 */
public record GoalStructure(
        GoalNode primaryGoal,
        List<GoalNode> subGoals,
        GoalComplexity complexity,
        Instant detectedAt,
        String detectionMethod
) {
    /**
     * Creates a new GoalStructure with defensive copying and validation.
     *
     * @param primaryGoal the primary identified goal (must not be null)
     * @param subGoals list of sub-goals
     * @param complexity the complexity level (must not be null)
     * @param detectedAt when the goals were detected (must not be null)
     * @param detectionMethod the method used for detection (must not be null)
     * @return a new GoalStructure instance
     */
    public static GoalStructure of(GoalNode primaryGoal, List<GoalNode> subGoals,
                                   GoalComplexity complexity, Instant detectedAt,
                                   String detectionMethod) {
        Objects.requireNonNull(primaryGoal, "primaryGoal must not be null");
        Objects.requireNonNull(complexity, "complexity must not be null");
        Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        Objects.requireNonNull(detectionMethod, "detectionMethod must not be null");
        List<GoalNode> safeSubGoals = subGoals != null ? List.copyOf(subGoals) : List.of();
        return new GoalStructure(primaryGoal, safeSubGoals, complexity, detectedAt, detectionMethod);
    }

    public GoalNode primaryGoal() {
        return primaryGoal;
    }

    public List<GoalNode> subGoals() {
        return subGoals;
    }

    public GoalComplexity complexity() {
        return complexity;
    }

    public Instant detectedAt() {
        return detectedAt;
    }

    public String detectionMethod() {
        return detectionMethod;
    }

    /**
     * Creates a GoalStructure with a simple single goal.
     *
     * @param primaryGoal the primary goal
     * @param detectionMethod the detection method
     * @return a simple GoalStructure
     */
    public static GoalStructure simple(GoalNode primaryGoal, String detectionMethod) {
        return new GoalStructure(primaryGoal, List.of(), GoalComplexity.SIMPLE,
                Instant.now(), detectionMethod);
    }

    /**
     * Creates an empty GoalStructure with no identified goals.
     *
     * @param detectionMethod the detection method
     * @return an empty GoalStructure
     */
    public static GoalStructure none(String detectionMethod) {
        GoalEvidence emptyEvidence = new GoalEvidence("", 0, 0);
        GoalNode emptyGoal = new GoalNode("No goal identified", 0.0, emptyEvidence);
        return new GoalStructure(emptyGoal, List.of(), GoalComplexity.SIMPLE,
                Instant.now(), detectionMethod);
    }

    @Override
    public String toString() {
        return String.format("GoalStructure{primaryGoal='%s', subGoals=%d, complexity=%s}",
                primaryGoal.title(), subGoals.size(), complexity);
    }
}